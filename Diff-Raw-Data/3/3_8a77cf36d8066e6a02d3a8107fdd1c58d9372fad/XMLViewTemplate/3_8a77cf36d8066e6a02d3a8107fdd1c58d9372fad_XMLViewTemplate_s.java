 /*
  * Created on Jul 27, 2005
  */
 package uk.org.ponder.rsf.template;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.xmlpull.mxp1.MXParser;
 import org.xmlpull.v1.XmlPullParser;
 
 import uk.org.ponder.arrayutil.ArrayUtil;
 import uk.org.ponder.rsf.util.RSFUtil;
 import uk.org.ponder.rsf.util.SplitID;
 import uk.org.ponder.rsf.view.ViewTemplate;
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The parser for the IKAT view template, implemented using the XPP3 "rapid" XML
  * pull parser. After parsing, this representation of the template is discarded,
  * in favour of its raw constituents, being i) The XMLLump[] array, ii) The
  * "root lump" holding the initial downmap, and iii) the global headmap. The
  * system assumes that renders will be much more frequent than template reads,
  * and so takes special efforts to condense the representation for rapid
  * render-time access, at the expense of slightly slower parsing. TODO: fix this
  * ridiculous dependency mixup - parsing code should be OUTSIDE and state should
  * be INSIDE!
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class XMLViewTemplate implements ViewTemplate {
   public int INITIAL_LUMP_SIZE = 1000;
   // a hypothetical "root lump" whose downmap contains root RSF components.
   public XMLLump rootlump;
   public XMLLumpMMap globalmap;
 
   // private HashMap foridtocomponent = new HashMap();
 
   public XMLLump[] lumps;
   // index of the first lump holding root document tag
   public int roottagindex;
   private CharWrap buffer;
 
   public boolean hasComponent(String ID) {
     return globalmap.hasID(ID);
   }
 
   private List parseinterceptors;
 
   // TODO: This method belongs in XMLViewTemplateParser
   public void setTemplateParseInterceptors(List parseinterceptors) {
     this.parseinterceptors = parseinterceptors;
   }
 
   private void writeToken(int token, XmlPullParser parser, CharWrap w) {
 
     char[] chars = parser.getTextCharacters(limits);
     switch (token) {
     case XmlPullParser.COMMENT:
       w.append("<!--");
       break;
     case XmlPullParser.ENTITY_REF:
       w.append("&");
       break;
     case XmlPullParser.CDSECT:
       w.append("<![CDATA[");
       break;
     case XmlPullParser.PROCESSING_INSTRUCTION:
       w.append("<?");
       break;
     case XmlPullParser.DOCDECL:
       w.append("<!DOCTYPE");
       break;
 
     }
     w.append(chars, limits[0], limits[1]);
     switch (token) {
     case XmlPullParser.COMMENT:
       w.append("-->");
       break;
     case XmlPullParser.ENTITY_REF:
       w.append(";");
       break;
     case XmlPullParser.CDSECT:
       w.append("]]>");
       break;
     case XmlPullParser.PROCESSING_INSTRUCTION:
       w.append("?>");
       break;
     case XmlPullParser.DOCDECL:
       w.append(">");
       break;
     }
   }
 
   private void setLumpChars(XMLLump lump, char[] chars, int start, int length) {
     lump.start = buffer.size;
     lump.length = length;
     if (length > 0) {
       buffer.append(chars, start, length);
     }
   }
 
   private void setLumpString(XMLLump lump, String string) {
     lump.start = buffer.size;
     lump.length = string.length();
     buffer.append(string);
   }
 
   private void simpleTagText(XmlPullParser parser) {
     justended = false;
     char[] chars = parser.getTextCharacters(limits);
     XMLLump newlump = newLump(parser);
     setLumpChars(newlump, chars, limits[0], limits[1]);
     // String text = new String(chars, limits[0], limits[1]);
     // lumps[lumpindex - 1].text = text;
   }
 
   private void processDefaultTag(int token, XmlPullParser parser) {
     justended = false;
     CharWrap w = new CharWrap();
     writeToken(token, parser, w);
     XMLLump newlump = newLump(parser);
     setLumpChars(newlump, w.storage, 0, w.size);
     // lumps[lumpindex - 1].text = w.toString();
   }
 
   private void processTagStart(XmlPullParser parser, boolean isempty) {
     if (justended) {
       // avoid the pathological case where we have for example
       // <td class="tmiblock1" rsf:id="tmiblock:"></td><td> which makes it
       // hard to spot run ends on the basis of recursion uncession.
       justended = false;
       XMLLump backlump = newLump(parser);
       backlump.nestingdepth--;
       setLumpChars(backlump, null, 0, 0);
     }
     XMLLump headlump = newLump(parser);
     if (roottagindex == -1)
       roottagindex = headlump.lumpindex;
     String tagname = parser.getName();
     // standard text of |<tagname | to allow easy identification.
     setLumpString(headlump, XMLLump.tagToText(tagname));
     // HashMap forwardmap = new HashMap();
     // headlump.forwardmap = forwardmap;
     // current policy - every open tag gets a forwardmap, and separate lumps.
     // eventually we only want a lump where there is an rsf:id.
     int attrs = parser.getAttributeCount();
     if (attrs > 0) {
       headlump.attributemap = new HashMap(attrs < 3? (attrs + 1)*2 : attrs * 2);
 
       for (int i = 0; i < attrs; ++i) {
         String attrname = parser.getAttributeName(i);
         String attrvalue = parser.getAttributeValue(i);
         headlump.attributemap.put(attrname, attrvalue);
       }
       if (parseinterceptors != null) {
         for (int i = 0; i < parseinterceptors.size(); ++i) {
           TemplateParseInterceptor parseinterceptor = (TemplateParseInterceptor) parseinterceptors
               .get(i);
           parseinterceptor.adjustAttributes(tagname, headlump.attributemap);
         }
       }
       boolean firstattr = true;
       for (Iterator keyit = headlump.attributemap.keySet().iterator(); keyit
           .hasNext();) {
         String attrname = (String) keyit.next();
         String attrvalue = (String) headlump.attributemap.get(attrname);
         XMLLump frontlump = newLump(parser);
         CharWrap lumpac = new CharWrap();
         if (!firstattr) {
           lumpac.append("\" ");
          firstattr = false;
         }
         lumpac.append(attrname).append("=\"");
         setLumpChars(frontlump, lumpac.storage, 0, lumpac.size);
         // frontlump holds |" name="|
         // valuelump just holds the value.
 
         XMLLump valuelump = newLump(parser);
         setLumpString(valuelump, attrvalue);
 
         if (attrname.equals(XMLLump.ID_ATTRIBUTE)) {
           String ID = attrvalue;
           if (ID.startsWith(XMLLump.FORID_PREFIX)
               && ID.endsWith(XMLLump.FORID_SUFFIX)) {
             ID = ID.substring(0, ID.length() - XMLLump.FORID_SUFFIX.length());
           }
           headlump.rsfID = ID;
 
           XMLLump stacktop = findTopContainer();
           stacktop.downmap.addLump(ID, headlump);
           globalmap.addLump(ID, headlump);
 
           SplitID split = new SplitID(ID);
 
           if (split.prefix.equals(XMLLump.FORID_PREFIX)) {
             // no special note, just prevent suffix logic.
           }
           // we need really to be able to locate 3 levels of id -
           // for-message:message:to
           // ideally we would also like to be able to locate repetition
           // constructs too, hopefully the standard suffix-based computation
           // will allow this. However we previously never allowed BOTH
           // repetitious and non-repetitious constructs to share the same
           // prefix, so revisit this to solve.
           // }
           else if (split.suffix != null) {
             // a repetitive tag is found.
             headlump.downmap = new XMLLumpMMap();
 
             // Repetitions within a SCOPE should be UNIQUE and CONTIGUOUS.
             XMLLump prevlast = stacktop.downmap.getFinal(split.prefix);
             stacktop.downmap.setFinal(split.prefix, headlump);
             if (prevlast != null) {
               // only store transitions from non-initial state -
               // TODO: see if transition system will ever be needed.
               String prevsuffix = SplitID.getSuffix(prevlast.rsfID);
               String transitionkey = split.prefix + SplitID.SEPARATOR
                   + prevsuffix + XMLLump.TRANSITION_SEPARATOR + split.suffix;
               stacktop.downmap.addLump(transitionkey, prevlast);
               globalmap.addLump(transitionkey, prevlast);
             }
           }
         } // end if rsf:id attribute
       } // end for each attribute
     }
     XMLLump finallump = newLump(parser);
 
     String closetext = attrs == 0 ? (isempty ? "/>"
         : ">")
         : (isempty ? "\"/>"
             : "\">");
     setLumpString(finallump, closetext);
     headlump.open_end = finallump;
 
     tagstack.add(nestingdepth, headlump);
     if (isempty) {
       processTagEnd(parser);
     }
   }
 
   boolean justended;
 
   private void processTagEnd(XmlPullParser parser) {
     // String tagname = parser.getName();
     XMLLump oldtop = tagstack.lumpAt(nestingdepth);
 
     oldtop.close_tag = lumps[lumpindex - 1];
     tagstack.remove(nestingdepth);
     justended = true;
   }
 
   private XMLLump findTopContainer() {
     for (int i = tagstack.size() - 1; i >= 0; --i) {
       XMLLump lump = tagstack.lumpAt(i);
       if (lump.rsfID != null && lump.rsfID.indexOf(SplitID.SEPARATOR) != -1)
         return lump;
     }
     return rootlump;
   }
 
   private String getTopFullID() {
     CharWrap togo = new CharWrap();
     for (int i = 0; i < tagstack.size(); ++i) {
       XMLLump lump = tagstack.lumpAt(i);
       if (lump.rsfID != null && lump.rsfID.indexOf(SplitID.SEPARATOR) != -1) {
         togo.append(RSFUtil.getFullIDSegment(lump.rsfID,
             SplitID.WILDCARD_COMPONENT));
       }
 
     }
     return togo.toString();
   }
 
   // temporary array for getCharacterText
   private int[] limits = new int[2];
   private int lumpindex = 0;
   private int nestingdepth = 0;
   // only stores repetitive tags.
   private XMLLumpList tagstack = new XMLLumpList();
 
   private XMLLump newLump(XmlPullParser parser) {
     if (lumpindex == lumps.length) {
       lumps = (XMLLump[]) ArrayUtil.expand(lumps, 2.0);
     }
     XMLLump togo = new XMLLump(lumpindex, nestingdepth);
     togo.line = parser.getLineNumber();
     togo.column = parser.getColumnNumber();
     lumps[lumpindex] = togo;
     ++lumpindex;
     return togo;
   }
 
   // XPP tag depths:
   // <!-- outside --> 0
   // <root> 1
   // sometext 1
   // <foobar> 2
   // </foobar> 2
   // </root> 1
   // <!-- outside --> 0
 
   public void init() {
     lumps = new XMLLump[INITIAL_LUMP_SIZE];
     buffer = new CharWrap(INITIAL_LUMP_SIZE * 10);
     lumpindex = 0;
     tagstack.clear();
     rootlump = new XMLLump();
     rootlump.downmap = new XMLLumpMMap();
     rootlump.nestingdepth = -1;
     roottagindex = -1;
     globalmap = new XMLLumpMMap();
     justended = false;
   }
 
   public void parse(InputStream xmlstream) {
     long time = System.currentTimeMillis();
     init();
     XmlPullParser parser = new MXParser();
     try {
       // parser.setFeature(FEATURE_XML_ROUNDTRIP, true);
       parser.setInput(xmlstream, "UTF-8");
       while (true) {
         int token = parser.nextToken();
         if (token == XmlPullParser.END_DOCUMENT)
           break;
         // currently 1 lump for each token - an optimisation would collapse
         // provable irrelevant lumps. but watch out for end tags! Some might
         // be fused, some not.
         nestingdepth = parser.getDepth() - 1;
 
         switch (token) {
         case XmlPullParser.START_TAG:
           boolean isempty = parser.isEmptyElementTag();
           processTagStart(parser, isempty);
           if (isempty) {
             parser.next();
           }
           break;
         case XmlPullParser.END_TAG:
           simpleTagText(parser);
           processTagEnd(parser);
           break;
         default:
           processDefaultTag(token, parser);
         }
       }
 
     }
     catch (Throwable t) {
       throw UniversalRuntimeException.accumulate(t, "Error parsing template");
     }
     endParse();
     // Logger.log.info("Template parsed in " + (System.currentTimeMillis() -
     // time) + "ms");
   }
 
   private void endParse() {
     lumps = (XMLLump[]) ArrayUtil.trim(lumps, lumpindex);
     tagstack.clear();
     char[] compacted = new char[buffer.size];
     System.arraycopy(buffer.storage, 0, compacted, 0, buffer.size);
     buffer = null;
     for (int i = 0; i < lumps.length; ++i) {
       lumps[i].buffer = compacted;
     }
   }
 
   private String resourcebase;
 
   public void setResourceBase(String resourcebase) {
     this.resourcebase = resourcebase;
   }
 
   public String getResourceBase() {
     return resourcebase;
   }
 
 }
