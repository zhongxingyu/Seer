 /*
  * Created on Jan 18, 2006
  */
 package uk.org.ponder.darwin.rsf;
 
 import java.io.CharArrayReader;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.lucene.analysis.Token;
 import org.apache.lucene.analysis.TokenStream;
 import org.springframework.core.io.InputStreamSource;
 import org.xmlpull.v1.XmlPullParser;
 
 import uk.org.ponder.darwin.item.ContentInfo;
 import uk.org.ponder.darwin.item.ItemCollection;
 import uk.org.ponder.darwin.item.ItemDetails;
 import uk.org.ponder.darwin.lucene.DarwinAnalyzer;
 import uk.org.ponder.darwin.pages.PageCountDAO;
 import uk.org.ponder.darwin.parse.Attributes;
 import uk.org.ponder.darwin.parse.BaseParser;
 import uk.org.ponder.darwin.parse.Constants;
 import uk.org.ponder.darwin.parse.DocumentTag;
 import uk.org.ponder.darwin.parse.ParseReceiver;
 import uk.org.ponder.darwin.parse.URLMapper;
 import uk.org.ponder.darwin.rsf.params.NavParams;
 import uk.org.ponder.darwin.rsf.params.TextBlockRenderParams;
 import uk.org.ponder.darwin.rsf.producers.FramesetProducer;
 import uk.org.ponder.darwin.rsf.util.DarwinUtil;
 import uk.org.ponder.darwin.search.DocFields;
 import uk.org.ponder.htmlutil.HTMLConstants;
 import uk.org.ponder.rsf.viewstate.ViewParamsMapper;
 import uk.org.ponder.rsf.viewstate.ViewStateHandler;
 import uk.org.ponder.streamutil.StreamCopyUtil;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.stringutil.URLUtil;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 import uk.org.ponder.xml.XMLUtil;
 import uk.org.ponder.xml.XMLWriter;
 
 /**
  * Receives character events from both an upstream CONTENT ContentParser, and
  * ALSO operates its own BaseParser to parser a Template. At the same time,
  * rewrites page links to include Javascript and anchors.
  * 
  * @author Antranig Basman (amb26@ponder.org.uk)
  */
 // This class is written in inestimably poor style. I apologise.
 // It is in this package because of dependence on ViewStateHandler to render
 // links.
 public class RenderingParseReceiver extends BaseParser implements ParseReceiver {
 
   private PrintOutputStream pos;
   private XMLWriter xmlw;
   private CharWrap buffer = new CharWrap();
   // The status of the CONTENT reader.
   boolean editable;
   // currenteditableclass is set ABOVE to reflect TEMPLATE reader, which we
   // are reading with our own tokens.
   private InputStreamSource templatesource;
   private URLMapper urlmapper;
   private String contentpath;
   private ItemCollection collection;
   private ViewStateHandler vsh;
   private Map keytoind;
   private int hitpage;
   private TextBlockRenderParams viewparams;
   private PageCountDAO pagecountDAO;
   private ViewParamsMapper vpmapper;
 
   public void setViewParamsMapper(ViewParamsMapper vpmapper) {
     this.vpmapper = vpmapper;
   }
 
   public void setPageCountDAO(PageCountDAO pagecountDAO) {
     this.pagecountDAO = pagecountDAO;
   }
 
   public void setOutputStream(PrintOutputStream pos) {
     this.pos = pos;
     xmlw = new XMLWriter(pos);
   }
 
   public void setTemplateSource(InputStreamSource templatesource) {
     this.templatesource = templatesource;
   }
 
   public void setURLMapper(URLMapper urlmapper) {
     this.urlmapper = urlmapper;
   }
 
   public void setItemCollection(ItemCollection collection) {
     this.collection = collection;
   }
 
   public void setViewStateHandler(ViewStateHandler vsh) {
     this.vsh = vsh;
   }
 
   public void setViewParams(TextBlockRenderParams viewparams) {
     this.viewparams = viewparams;
   }
 
   public void setHitPage(int hitpage) {
     this.hitpage = hitpage;
   }
 
   int currentpage = -1;
 
   public void metObject(Object tagobj) {
     if (tagobj instanceof DocumentTag) {
       currentpage = ((DocumentTag) tagobj).firstpage;
     }
   }
 
   boolean inpagetag = false;
   boolean doneheader;
   boolean dumponclose = false;
 
   public static String getURLAttr(String tagname) {
     for (int i = 0; i < HTMLConstants.tagtoURL.length; ++i) {
       String[] tags = HTMLConstants.tagtoURL[i];
       String tag = tags[0];
       for (int j = 1; j < tags.length; ++j) {
         if (tagname.equals(tags[j])) {
           return tag;
         }
       }
     }
     return null;
   }
 
   // called by the CONTENT reader when tag is received.
   public void protoTag(String tagname, String clazz, HashMap attrmap,
       boolean isempty) {
     flushBuffer(true);
     if (Attributes.PAGE_CLASS.equals(clazz)) {
       // I am without style
       pos.print("<p>");
       String pageseq = (String) attrmap.get(Attributes.PAGESEQ_ATTR);
       if (pageseq == null) {
         pageseq = Integer.toString(currentpage);
         ++currentpage;
       }
       else {
         currentpage = Integer.parseInt(pageseq);
       }
       if (viewparams.viewtype.equals(NavParams.TEXT_VIEW)) {
         ItemDetails item = collection.getItem(viewparams.itemID);
         if (item.hasimage && item.hastext) {
           attrmap.put("target", "_top");
           NavParams navparams = new NavParams();
           navparams.viewID = FramesetProducer.VIEWID;
           navparams.viewtype = NavParams.SIDE_VIEW;
           navparams.itemID = item.ID;
           navparams.pageseq = currentpage - 1;
           String url = vsh.getFullURL(navparams);
           attrmap.put("href", url);
         }
       }
       else {
         attrmap.put("onClick",
             "onPageClick(this.getAttribute('dar:pageseq')); return false;");
         attrmap.put("href", "#");
         attrmap.put(Attributes.PAGESEQ_ATTR, "" + pageseq);
       }
       attrmap.put("name", pageseq);
       tagname = "a";
       inpagetag = true;
       if (currentpage == hitpage + 1) {
         dumponclose = true;
         doneheader = true;
       }
     }
     String exptag = "<" + tagname + " ";
     // Deal with rewriting for embedded links and images - need to resynthesize
     // top &c
     String urlattr = getURLAttr(exptag);
     if (urlattr != null) {
       String url = (String) attrmap.get(urlattr);
       if (url != null) {
         if (!URLUtil.isAbsolute(url) && !url.equals("#")) {
           if (urlattr.equals("href") && tagname.equals("a")
               && !attrmap.containsKey("target")) {
             // It's an inter-book link
             String targetpath = urlmapper.relURLToAbsolute(url, contentpath);
             ContentInfo ci = collection.getContentInfo(targetpath);
             if (ci == null) {
               Logger.log.warn("Unable to find ContentInfo for target path "
                   + targetpath + " arising from relative link URL " + url
                   + " for content path " + contentpath);
             }
             else {
               NavParams frameparams = new NavParams();
               frameparams.viewID = FramesetProducer.VIEWID;
               frameparams.itemID = ci.itemID;
               frameparams.pageseq = ci.firstpage;
//              ItemDetails id = collection.getItem(ci.itemID);
               DarwinUtil.chooseBestView(frameparams, collection);
 
               String globalurl = vsh.getFullURL(frameparams);
               attrmap.put(urlattr, globalurl);
               attrmap.put("target", "_top");
             }
           }
           else {
             // It's a leaf link - an image (either popup or inline)
             String globalurl = urlmapper.relURLToExternal(url, contentpath);
             attrmap.put(urlattr, globalurl);
           }
         }
         else if (URLUtil.isAbsolute(url)) {
           attrmap.put("target", "_top");
         }
       }
     }
     pos.print("<" + tagname + " ");
     XMLUtil.dumpAttributes(attrmap, xmlw);
 
     pos.print(isempty ? "/>"
         : ">");
   }
 
   public void endTag(String tagname) {
   }
 
   private String getKeyword(int i) {
     for (Iterator it = keytoind.keySet().iterator(); it.hasNext();) {
       String keyword = (String) it.next();
       Integer ind = (Integer) keytoind.get(keyword);
       if (ind.intValue() == i)
         return keyword;
     }
     return "";
   }
 
   private void dumpHeader() {
     if (keytoind != null && keytoind.size() > 0) {
       buffer
           .append("<table border=0 cellpadding=0 cellspacing=0><tr><td bgcolor=#ffc0c0>"
               + "<font face=\"\"  color=black size=-1>The following search terms have been highlighted:&nbsp;</font></td>");
       for (int i = 0; i < keytoind.size(); ++i) {
         String keyword = getKeyword(i);
         String bgcol = TermColours.TERM_COLOURS[i];
         String fgcol = TermColours.getContrastColour(i);
         buffer.append("<td bgcolor=" + bgcol + "><b><font face=\"\" color="
             + fgcol + " size=-1>" + keyword + "&nbsp;</font></b></td>");
       }
       buffer.append("</tr></table>");
     }
   }
 
   // text received from CONTENT.
   public void text(XmlPullParser parser, int token, CharWrap text) {
     // Replace the end of a pageno tag with </a>, skipping </p>
     if (token == XmlPullParser.END_TAG && inpagetag) {
       if (text.size() > 0) {
         buffer.append("</a>");
       }
       inpagetag = false;
       if (dumponclose) {
         try {
           dumpHeader();
         }
         finally {
           dumponclose = false;
         }
       }
     }
     if ((templatesource == null || editable)
         && token != XmlPullParser.START_TAG) {
       buffer.append(text);
     }
   }
 
   // status change from CONTENT
   public void beginEditable(String editclass) {
     editable = true;
     currenteditableclass = editclass;
     if (editclass.equals(Constants.BODY)) {
       dumpRefresher();
     }
   }
 
   private void dumpRefresher() {
     NavParams frameparams = new NavParams();
     frameparams.viewID = FramesetProducer.VIEWID;
     frameparams.itemID = viewparams.itemID;
     frameparams.pageseq = viewparams.hitpage;
 
     DarwinUtil.chooseBestView(frameparams, collection);
 
     String frameset = vsh.getFullURL(frameparams);
     buffer.append("<script type=\"text/javascript\">\n"
         + "if (parent.location.href == self.location.href) {\n"
         + "if (window.location.href.replace)\n" + "window.location.replace('"
         + frameset + "');" + " else\n " + " window.location.href = '"
         + frameset + "';\n" + " }\n" + "</script>");
   }
 
   private void dumpPageCount() {
     TextBlockRenderParams reduced = new TextBlockRenderParams();
     reduced.viewID = viewparams.viewID;
     reduced.itemID = viewparams.itemID;
     reduced.basepage = viewparams.basepage;
     String URL = vpmapper.toHTTPRequest(reduced);
 
     int count = pagecountDAO.registerAccess(URL);
     Date date = pagecountDAO.getStartDate();
     DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.LONG);
     buffer
        .append("<br/><span class=\"style2\" style=\"border-width: 1px; border-style: solid; " +
                 "border-color: #AAAAAA; background-color: #EEEEBB\">" +
                 "This document has been accessed "
             + count + (count == 1 ? " time"
                : " times") + " since " + format.format(date) + "</span>");
 
   }
 
   // status change from CONTENT
   public void endEditable(String editclass) {
     if (editclass.equals(Constants.BODY)) {
       dumpPageCount();
     }
     currenteditableclass = null;
     if (templatesource != null) {
       if (editclass.equals(Constants.TITLE)) {
         scanTemplate(Constants.BODY);
       }
       else {
         scanTemplate(null);
       }
     }
   }
 
   // skim along the TEMPLATE until we hit an editable section, then since we
   // stop scanning, the CONTENT will continue to be delivered from the outside.
   private void scanTemplate(String required) {
     while (true) {
       try {
         int token = parser.nextToken();
         if (token == XmlPullParser.COMMENT) {
           if (token == XmlPullParser.END_DOCUMENT)
             break;
           if (required != null && testComment()) {
             if (currenteditableclass.equals(required))
               return;
           }
           if (currenteditableclass == null) {
             CharWrap text = renderToken(token);
             buffer.append(text);
             flushBuffer(false);
           }
 
         }
       }
       catch (Throwable t) {
         throw UniversalRuntimeException.accumulate(t, "Error parsing file: ");
       }
     }
   }
 
   private void parseKeywords() {
     String keywords = viewparams.keywords;
     if (keywords != null && !keywords.trim().equals("")) {
       keytoind = new HashMap();
       String[] split = keywords.split(" ");
       for (int i = 0; i < split.length; ++i) {
         keytoind.put(split[i], new Integer(i));
       }
       if (keytoind.size() == 0) {
         keytoind = null;
       }
     }
   }
 
   public void beginFile(String contentpath) {
     this.contentpath = contentpath;
     editable = false;
     currenteditableclass = null;
     currentpage = -1;
     inpagetag = false;
     parseKeywords();
     doneheader = false;
     // parser.setFeature(FEATURE_XML_ROUNDTRIP, true);
     if (templatesource != null) {
       try {
         InputStream xmlstream = templatesource.getInputStream();
         parser.setInput(xmlstream, null);
       }
       catch (Exception e) {
         throw UniversalRuntimeException.accumulate(e,
             "Error opening template file");
       }
       scanTemplate(Constants.TITLE);
     }
   }
 
   public void endFile() {
     flushBuffer(true);
   }
 
   private void flushBuffer(boolean unconditional) {
     if (unconditional
         || (buffer.size > StreamCopyUtil.PROCESS_BUFFER_SIZE && keytoind == null)) {
       output();
     }
   }
 
   private void outputHighlight() {
     DarwinAnalyzer analyzer = new DarwinAnalyzer();
     TokenStream ts = analyzer.tokenStream(DocFields.TEXT, new CharArrayReader(
         buffer.storage, 0, buffer.size));
     int writpos = 0;
     while (true) {
       try {
         Token t = ts.next();
         if (t == null)
           break;
         Integer termindi = (Integer) keytoind.get(t.termText());
         if (termindi == null)
           continue;
         else {
           pos.write(buffer.storage, writpos, t.startOffset() - writpos);
           int termind = termindi.intValue() % TermColours.TERM_COLOURS.length;
           String bgcol = TermColours.TERM_COLOURS[termind];
           String fgcol = TermColours.getContrastColour(termind);
           pos.print("<b style=\"color:" + fgcol + ";background-color:" + bgcol
               + "\">");
           pos.write(buffer.storage, t.startOffset(), t.endOffset()
               - t.startOffset());
           pos.print("</b>");
           writpos = t.endOffset();
         }
 
       }
       catch (Exception e) {
         Logger.log.warn("Error reparsing text for highlighting", e);
         break;
       }
     }
     pos.write(buffer.storage, writpos, buffer.size - writpos);
   }
 
   private void output() {
     if (keytoind == null || !"body".equals(currenteditableclass) || inpagetag) {
       pos.write(buffer.storage, 0, buffer.size);
       buffer.clear();
     }
     else {
       outputHighlight();
       buffer.clear();
     }
   }
 
 }
