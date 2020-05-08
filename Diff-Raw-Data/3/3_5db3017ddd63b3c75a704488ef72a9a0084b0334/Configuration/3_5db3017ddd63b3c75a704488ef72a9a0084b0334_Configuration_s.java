 /*
  *  Java HTML Tidy - JTidy
  *  HTML parser and pretty printer
  *
  *  Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts
  *  Institute of Technology, Institut National de Recherche en
  *  Informatique et en Automatique, Keio University). All Rights
  *  Reserved.
  *
  *  Contributing Author(s):
  *
  *     Dave Raggett <dsr@w3.org>
  *     Andy Quick <ac.quick@sympatico.ca> (translation to Java)
  *     Gary L Peskin <garyp@firstech.com> (Java development)
  *     Sami Lempinen <sami@lempinen.net> (release management)
  *     Fabrizio Giustina <fgiust at users.sourceforge.net>
  *
  *  The contributing author(s) would like to thank all those who
  *  helped with testing, bug fixes, and patience.  This wouldn't
  *  have been possible without all of you.
  *
  *  COPYRIGHT NOTICE:
  * 
  *  This software and documentation is provided "as is," and
  *  the copyright holders and contributing author(s) make no
  *  representations or warranties, express or implied, including
  *  but not limited to, warranties of merchantability or fitness
  *  for any particular purpose or that the use of the software or
  *  documentation will not infringe any third party patents,
  *  copyrights, trademarks or other rights. 
  *
  *  The copyright holders and contributing author(s) will not be
  *  liable for any direct, indirect, special or consequential damages
  *  arising out of any use of the software or documentation, even if
  *  advised of the possibility of such damage.
  *
  *  Permission is hereby granted to use, copy, modify, and distribute
  *  this source code, or portions hereof, documentation and executables,
  *  for any purpose, without fee, subject to the following restrictions:
  *
  *  1. The origin of this source code must not be misrepresented.
  *  2. Altered versions must be plainly marked as such and must
  *     not be misrepresented as being the original source.
  *  3. This Copyright notice may not be removed or altered from any
  *     source or altered source distribution.
  * 
  *  The copyright holders and contributing author(s) specifically
  *  permit, without fee, and encourage the use of this source code
  *  as a component for supporting the Hypertext Markup Language in
  *  commercial products. If you use this source code in a product,
  *  acknowledgment is not required but would be appreciated.
  *
  */
 package org.w3c.tidy;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.Writer;
 import java.util.EnumMap;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.Properties;
 
 import org.w3c.tidy.Options.AttrSortStrategy;
 import org.w3c.tidy.Options.DoctypeModes;
 import org.w3c.tidy.Options.DupAttrModes;
 import org.w3c.tidy.Options.LineEnding;
 import org.w3c.tidy.Options.OptionEnum;
 import org.w3c.tidy.Options.TriState;
 
 /**
  * Read configuration file and manage configuration properties. Configuration files associate a property name with a
  * value. The format is that of a Java .properties file.
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public class Configuration implements Serializable
 {
 
     /**
      * character encoding = RAW.
      * @deprecated use <code>Tidy.setRawOut(true)</code> for raw output
      */
     public static final int RAW = 0;
 
     /**
      * character encoding = ASCII.
      * @deprecated
      */
     public static final int ASCII = 1;
 
     /**
      * character encoding = LATIN1.
      * @deprecated
      */
     public static final int LATIN1 = 2;
 
     /**
      * character encoding = UTF8.
      * @deprecated
      */
     public static final int UTF8 = 3;
 
     /**
      * character encoding = ISO2022.
      * @deprecated
      */
     public static final int ISO2022 = 4;
 
     /**
      * character encoding = MACROMAN.
      * @deprecated
      */
     public static final int MACROMAN = 5;
 
     /**
      * character encoding = UTF16LE.
      * @deprecated
      */
     public static final int UTF16LE = 6;
 
     /**
      * character encoding = UTF16BE.
      * @deprecated
      */
     public static final int UTF16BE = 7;
 
     /**
      * character encoding = UTF16.
      * @deprecated
      */
     public static final int UTF16 = 8;
 
     /**
      * character encoding = WIN1252.
      * @deprecated
      */
     public static final int WIN1252 = 9;
 
     /**
      * character encoding = BIG5.
      * @deprecated
      */
     public static final int BIG5 = 10;
 
     /**
      * character encoding = SHIFTJIS.
      * @deprecated
      */
     public static final int SHIFTJIS = 11;
 
     /**
      * Convert from deprecated tidy encoding constant to standard java encoding name.
      */
     private final String[] ENCODING_NAMES = new String[]{"raw", // rawOut, it will not be mapped to a java encoding
         "ASCII",
         "ISO8859_1",
         "UTF8",
         "JIS",
         "MacRoman",
         "UnicodeLittle",
         "UnicodeBig",
         "Unicode",
         "Cp1252",
         "Big5",
         "SJIS"};
 
     private final Map<Option, Object> options = new EnumMap<Option, Object>(Option.class);
 
     /**
      * serial version UID for this class.
      */
     private static final long serialVersionUID = -4955155037138560842L;
 
 //    static
 //    {
 //    	// missing: unknown
 //        addConfigOption(new Flag("indent-spaces", "spaces", ParsePropertyImpl.INT));
 //        addConfigOption(new Flag("wrap", "wraplen", ParsePropertyImpl.INT));
 //        addConfigOption(new Flag("tab-size", "tabsize", ParsePropertyImpl.INT));
 //        addConfigOption(new Flag("char-encoding", null, ParsePropertyImpl.CHAR_ENCODING));
 //        addConfigOption(new Flag("input-encoding", null, ParsePropertyImpl.CHAR_ENCODING));
 //        addConfigOption(new Flag("output-encoding", null, ParsePropertyImpl.CHAR_ENCODING));
 //        addConfigOption(new Flag("newline", null, ParsePropertyImpl.NEWLINE));
 //        // missing: doctype-mode
 //        addConfigOption(new Flag("doctype", "docTypeStr", ParsePropertyImpl.DOCTYPE));
 //        addConfigOption(new Flag("repeated-attributes", "duplicateAttrs", ParsePropertyImpl.REPEATED_ATTRIBUTES));
 //        addConfigOption(new Flag("alt-text", "altText", ParsePropertyImpl.STRING));
 //        
 //        // obsolete
 //        addConfigOption(new Flag("slide-style", "slidestyle", ParsePropertyImpl.NAME));
 //        
 //        addConfigOption(new Flag("error-file", "errfile", ParsePropertyImpl.NAME));
 //        // missing: output-file
 //        addConfigOption(new Flag("write-back", "writeback", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("markup", "onlyErrors", ParsePropertyImpl.INVBOOL));
 //        addConfigOption(new Flag("show-warnings", "showWarnings", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("quiet", "quiet", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("indent", "indentContent", ParsePropertyImpl.INDENT));
 //        addConfigOption(new Flag("hide-endtags", "hideEndTags", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("input-xml", "xmlTags", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("output-xml", "xmlOut", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("output-xhtml", "xHTML", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("output-html", "htmlOut", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("add-xml-decl", "xmlPi", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("uppercase-tags", "upperCaseTags", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("uppercase-attributes", "upperCaseAttrs", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("bare", "makeBare", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("clean", "makeClean", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("logical-emphasis", "logicalEmphasis", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("drop-proprietary-attributes", "dropProprietaryAttributes", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("drop-font-tags", "dropFontTags", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("drop-empty-paras", "dropEmptyParas", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("fix-bad-comments", "fixComments", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("break-before-br", "breakBeforeBR", ParsePropertyImpl.BOOL));
 //        
 //        // obsolete
 //        addConfigOption(new Flag("split", "burstSlides", ParsePropertyImpl.BOOL));
 //        
 //        addConfigOption(new Flag("numeric-entities", "numEntities", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("quote-marks", "quoteMarks", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("quote-nbsp", "quoteNbsp", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("quote-ampersand", "quoteAmpersand", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("wrap-attributes", "wrapAttVals", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("wrap-script-literals", "wrapScriptlets", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("wrap-sections", "wrapSection", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("wrap-asp", "wrapAsp", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("wrap-jste", "wrapJste", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("wrap-php", "wrapPhp", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("fix-backslash", "fixBackslash", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("indent-attributes", "indentAttributes", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("assume-xml-procins", "xmlPIs", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("add-xml-space", "xmlSpace", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("enclose-text", "encloseBodyText", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("enclose-block-text", "encloseBlockText", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("keep-time", "keepFileTimes", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("word-2000", "word2000", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("tidy-mark", "tidyMark", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("gnu-emacs", "emacs", ParsePropertyImpl.BOOL));
 //        // missing: gnu-emacs-file
 //        addConfigOption(new Flag("literal-attributes", "literalAttribs", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("show-body-only", "bodyOnly", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("fix-uri", "fixUri", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("lower-literals", "lowerLiterals", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("hide-comments", "hideComments", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("indent-cdata", "indentCdata", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("force-output", "forceOutput", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("show-errors", "showErrors", ParsePropertyImpl.INT));
 //        addConfigOption(new Flag("ascii-chars", "asciiChars", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("join-classes", "joinClasses", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("join-styles", "joinStyles", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("escape-cdata", "escapeCdata", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("language", "language", ParsePropertyImpl.NAME));
 //        addConfigOption(new Flag("ncr", "ncr", ParsePropertyImpl.BOOL));
 //        // missing: output-bom
 //        addConfigOption(new Flag("replace-color", "replaceColor", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("css-prefix", "cssPrefix", ParsePropertyImpl.CSS1SELECTOR));
 //        addConfigOption(new Flag("new-inline-tags", null, ParsePropertyImpl.TAGNAMES));
 //        addConfigOption(new Flag("new-blocklevel-tags", null, ParsePropertyImpl.TAGNAMES));
 //        addConfigOption(new Flag("new-empty-tags", null, ParsePropertyImpl.TAGNAMES));
 //        addConfigOption(new Flag("new-pre-tags", null, ParsePropertyImpl.TAGNAMES));
 //        // missing: accessibility-check
 //        // missing: vertical-space
 //        // missing: punctuation-wrap
 //        // missing: merge-divs
 //        // missing: decorate-inferred-ul
 //        // missing: preserve-entities
 //        // missing: sort-attributes
 //        // missing: merge-spans
 //        // missing: anchor-as-name
 //        
 //        // options not found in Tidy
 //        addConfigOption(new Flag("output-raw", "rawOut", ParsePropertyImpl.BOOL));
 //        addConfigOption(new Flag("trim-empty-elements", "trimEmpty", ParsePropertyImpl.BOOL));
 //    }
 
     /**
      * trim empty elements.
      */
     private boolean trimEmpty = true;
 
     /**
      * char encoding used when replacing illegal SGML chars, regardless of specified encoding.
      */
     private String replacementCharEncoding = "WIN1252"; // by default
 
     /**
      * TagTable associated with this Configuration.
      */
     protected TagTable tt;
 
     /**
      * Report instance. Used for messages.
      */
     protected Report report;
 
     /**
      * track what types of tags user has defined to eliminate unnecessary searches.
      */
     private int definedTags;
 
     /**
      * Input character encoding (defaults to "ISO8859_1").
      */
     private String inCharEncoding = "ISO8859_1";
 
     /**
      * Output character encoding (defaults to "ASCII").
      */
     private String outCharEncoding = "ASCII";
 
     /**
      * Avoid mapping values > 127 to entities.
      */
     private boolean rawOut;
     
     /**
      * configuration properties.
      */
     private transient Properties properties = new Properties();
 
     /**
      * Instantiates a new Configuration. This method should be called by Tidy only.
      * @param report Report instance
      */
     protected Configuration(Report report)
     {
         this.report = report;
     }
 
     /**
      * adds configuration Properties.
      * @param p Properties
      */
     public void addProps(Properties p)
     {
         Enumeration<?> propEnum = p.propertyNames();
         while (propEnum.hasMoreElements())
         {
             String key = (String) propEnum.nextElement();
             String value = p.getProperty(key);
             properties.put(key, value);
         }
         parseProps();
     }
 
     /**
      * Parses a property file.
      * @param filename file name
      */
     public void parseFile(String filename)
     {
         try
         {
             properties.load(new FileInputStream(filename));
         }
         catch (IOException e)
         {
             System.err.println(filename + " " + e.toString());
             return;
         }
         parseProps();
     }
 
     /**
      * Is the given String a valid configuration flag?
      * @param name configuration parameter name
      * @return <code>true</code> if the given String is a valid config option
      */
     public static boolean isKnownOption(String name)
     {
         return name != null && Options.getOption(name) != null;
     }
 
     /**
      * Parses the configuration properties file.
      */
     private void parseProps()
     {
         for (Object o : properties.keySet())
         {
         	String key = (String) o;
         	if (key.startsWith("//")) {
             	continue;
             }
         	Option flag = Options.getOption(key);
             if (flag == null)
             {
                 report.unknownOption(key);
                 continue;
             }
 
             String stringValue = properties.getProperty(key);
             Object value = flag.getParser().parse(stringValue, flag, this);
             options.put(flag, value);
         }
     }
 
     /**
      * Ensure that config is self consistent.
      */
     public void adjust() {
         if (isEncloseBlockText()) {
             setEncloseBodyText(true);
         }
 
         if (getIndentContent() == TriState.No) {
         	setSpaces(0);
         }
 
         // disable wrapping
         if (getWraplen() == 0) {
             setWraplen(0x7FFFFFFF);
         }
 
         // Word 2000 needs o:p to be declared as inline
         if (isWord2000()) {
             definedTags |= Dict.TAGTYPE_INLINE;
             tt.defineTag(Dict.TAGTYPE_INLINE, "o:p");
         }
 
         // #480701 disable XHTML output flag if both output-xhtml and xml are set
         if (isXmlTags()) {
             setXHTML(false);
         }
 
         // XHTML is written in lower case
         if (isXHTML()) {
             setXmlOut(true);
             setUpperCaseTags(false);
             setUpperCaseAttrs(false);
         }
 
         // if XML in, then XML out
         if (isXmlTags()) {
             setXmlOut(true);
             setXmlPIs(true);
         }
 
         // #427837 - fix by Dave Raggett 02 Jun 01
         // generate <?xml version="1.0" encoding="iso-8859-1"?> if the output character encoding is Latin-1 etc.
        if (!"UTF8".equals(getOutCharEncodingName()) && !"ASCII".equals(getOutCharEncodingName()) && isXmlOut()) {
             setXmlDecl(true);
         }
 
         // XML requires end tags
         if (isXmlOut()) {
             setQuoteAmpersand(true);
             setHideEndTags(false);
         }
     }
 
     /**
      * prints available configuration options.
      * @param errout where to write
      * @param showActualConfiguration print actual configuration values
      */
     public void printConfigOptions(Writer errout, boolean showActualConfiguration)
     {
         String pad = "                                                                               ";
         try
         {
             errout.write("\nConfiguration File Settings:\n\n");
 
             if (showActualConfiguration)
             {
                 errout.write("Name                        Type       Current Value\n");
             }
             else
             {
                 errout.write("Name                        Type       Allowable values\n");
             }
 
             errout.write("=========================== =========  ========================================\n");
 
 			for (Option configItem : Options.getOptions()) {
 				final ParseProperty parser = configItem.getParser();
                 if (parser == null) {
                 	continue;
                 }
                 
                 errout.write(configItem.getName());
                 errout.write(pad, 0, 28 - configItem.getName().length());
 
                 String type = parser.getType();
                 if (type == null) { // only for ParseFromValues
                 	type = "Enum";
                 }
 				errout.write(type);
                 errout.write(pad, 0, 11 - type.length());
 
                 if (showActualConfiguration) {
                     final Object actualValue = options.get(configItem);
                     errout.write(parser.getFriendlyName(configItem.getName(), actualValue, this));
                 } else {
                     String values = parser.getOptionValues();
                     if (values == null) { // only for ParseFromValues
                     	values = configItem.getPickList().getDescription();
                     }
 					errout.write(values);
                 }
                 errout.write("\n");
             }
             errout.flush();
         }
         catch (IOException e)
         {
             throw new RuntimeException(e.getMessage());
         }
 
     }
 
     /**
      * Getter for <code>inCharEncodingName</code>.
      * @return Returns the inCharEncodingName.
      */
     protected String getInCharEncodingName()
     {
         return this.inCharEncoding;
     }
 
     /**
      * Setter for <code>inCharEncodingName</code>.
      * @param encoding The inCharEncodingName to set.
      */
     protected void setInCharEncodingName(String encoding)
     {
         String javaEncoding = EncodingNameMapper.toJava(encoding);
         if (javaEncoding != null)
         {
             this.inCharEncoding = javaEncoding;
         }
     }
 
     /**
      * Getter for <code>outCharEncodingName</code>.
      * @return Returns the outCharEncodingName.
      */
     protected String getOutCharEncodingName()
     {
         return this.outCharEncoding;
     }
 
     /**
      * Setter for <code>outCharEncodingName</code>.
      * @param encoding The outCharEncodingName to set.
      */
     protected void setOutCharEncodingName(String encoding)
     {
         String javaEncoding = EncodingNameMapper.toJava(encoding);
         if (javaEncoding != null)
         {
             this.outCharEncoding = javaEncoding;
         }
     }
 
     /**
      * Setter for <code>inOutCharEncodingName</code>.
      * @param encoding The CharEncodingName to set.
      */
     protected void setInOutEncodingName(String encoding)
     {
         setInCharEncodingName(encoding);
         setOutCharEncodingName(encoding);
     }
 
     /**
      * Setter for <code>outCharEncoding</code>.
      * @param encoding The outCharEncoding to set.
      * @deprecated use setOutCharEncodingName(String)
      */
     protected void setOutCharEncoding(int encoding)
     {
         setOutCharEncodingName(convertCharEncoding(encoding));
     }
 
     /**
      * Setter for <code>inCharEncoding</code>.
      * @param encoding The inCharEncoding to set.
      * @deprecated use setInCharEncodingName(String)
      */
     protected void setInCharEncoding(int encoding)
     {
         setInCharEncodingName(convertCharEncoding(encoding));
     }
 
     /**
      * Convert a char encoding from the deprecated tidy constant to a standard java encoding name.
      * @param code encoding code
      * @return encoding name
      */
     protected String convertCharEncoding(int code)
     {
         if (code != 0 && code < ENCODING_NAMES.length)
         {
             return ENCODING_NAMES[code];
         }
         return null;
     }
     
     private static RuntimeException badType(final Object x) {
     	if (x == null) {
     		return new RuntimeException("Null option value");
     	}
     	return new RuntimeException("Unexpected value type: " + x.getClass().getName());
     }
     
     private Object get(final Option option) {
     	final Object x = options.get(option);
     	return x == null ? option.getDflt() : x;
     }
     
     private int getInt(final Option option) {
     	final Object x = get(option);
     	if (x instanceof Integer) {
     		return (Integer) x;
     	}
     	throw badType(x);
     }
     
     private OptionEnum getOptionEnum(final Option option) {
     	final Object x = get(option);
     	if (x instanceof OptionEnum) {
     		return (OptionEnum) x;
     	}
     	throw badType(x);
     }
     
     private String getString(final Option option) {
     	final Object x = get(option);
     	if (x == null || x instanceof String) {
     		return (String) x;
     	}
     	throw badType(x);
     }
     
     private boolean getBool(final Option option) {
     	final Object x = get(option);
     	if (x instanceof Boolean) {
     		return (Boolean) x;
     	}
     	throw badType(x);
     }
     
     private void set(final Option option, final Object value) {
     	options.put(option, value);
     }
     
     protected void reset(final Option option) {
 		options.put(option, null);
 	}
 
 	protected void setSpaces(final int spaces) {
 		set(Option.IndentSpaces, spaces);
 	}
 
 	protected int getSpaces() {
 		return getInt(Option.IndentSpaces);
 	}
 
 	protected void setWraplen(final int wraplen) {
 		set(Option.WrapLen, wraplen);
 	}
 
 	protected int getWraplen() {
 		return getInt(Option.WrapLen);
 	}
 
 	protected void setTabsize(final int tabsize) {
 		set(Option.TabSize, tabsize);
 	}
 
 	protected int getTabsize() {
 		return getInt(Option.TabSize);
 	}
 
 	protected void setDocTypeMode(final DoctypeModes docTypeMode) {
 		set(Option.DoctypeMode, docTypeMode);
 	}
 
 	protected DoctypeModes getDocTypeMode() {
 		return (DoctypeModes) getOptionEnum(Option.DoctypeMode);
 	}
 
 	protected void setDuplicateAttrs(final DupAttrModes duplicateAttrs) {
 		set(Option.DuplicateAttrs, duplicateAttrs);
 	}
 
 	protected DupAttrModes getDuplicateAttrs() {
 		return (DupAttrModes) getOptionEnum(Option.DuplicateAttrs);
 	}
 
 	protected void setAltText(final String altText) {
 		set(Option.AltText, altText);
 	}
 
 	protected String getAltText() {
 		return getString(Option.AltText);
 	}
 
 	protected void setLanguage(final String language) {
 		set(Option.Language, language);
 	}
 
 	protected String getLanguage() {
 		return getString(Option.Language);
 	}
 
 	protected void setDocTypeStr(final String docTypeStr) {
 		set(Option.Doctype, docTypeStr);
 	}
 
 	protected String getDocTypeStr() {
 		return getString(Option.Doctype);
 	}
 
 	protected void setErrfile(final String errfile) {
 		set(Option.ErrFile, errfile);
 	}
 
 	protected String getErrfile() {
 		return getString(Option.ErrFile);
 	}
 
 	protected void setWriteback(final boolean writeback) {
 		set(Option.WriteBack, writeback);
 	}
 
 	protected boolean isWriteback() {
 		return getBool(Option.WriteBack);
 	}
 
 	protected void setShowMarkup(final boolean showMarkup) {
 		set(Option.ShowMarkup, showMarkup);
 	}
 
 	protected boolean isShowMarkup() {
 		return getBool(Option.ShowMarkup);
 	}
 
 	protected void setShowWarnings(final boolean showWarnings) {
 		set(Option.ShowWarnings, showWarnings);
 	}
 
 	protected boolean isShowWarnings() {
 		return getBool(Option.ShowWarnings);
 	}
 
 	protected void setQuiet(final boolean quiet) {
 		set(Option.Quiet, quiet);
 	}
 
 	protected boolean isQuiet() {
 		return getBool(Option.Quiet);
 	}
 
 	protected void setIndentContent(final TriState indentContent) {
 		set(Option.IndentContent, indentContent);
 	}
 
 	protected TriState getIndentContent() {
 		return (TriState) getOptionEnum(Option.IndentContent);
 	}
 
 	protected void setHideEndTags(final boolean hideEndTags) {
 		set(Option.HideEndTags, hideEndTags);
 	}
 
 	protected boolean isHideEndTags() {
 		return getBool(Option.HideEndTags);
 	}
 
 	protected void setXmlTags(final boolean xmlTags) {
 		set(Option.XmlTags, xmlTags);
 	}
 
 	protected boolean isXmlTags() {
 		return getBool(Option.XmlTags);
 	}
 
 	protected void setXmlOut(final boolean xmlOut) {
 		set(Option.XmlOut, xmlOut);
 	}
 
 	protected boolean isXmlOut() {
 		return getBool(Option.XmlOut);
 	}
 
 	protected void setXHTML(final boolean xHTML) {
 		set(Option.XhtmlOut, xHTML);
 	}
 
 	protected boolean isXHTML() {
 		return getBool(Option.XhtmlOut);
 	}
 
 	protected void setHtmlOut(final boolean htmlOut) {
 		set(Option.HtmlOut, htmlOut);
 	}
 
 	protected boolean isHtmlOut() {
 		return getBool(Option.HtmlOut);
 	}
 
 	protected void setXmlDecl(final boolean xmlDecl) {
 		set(Option.XmlDecl, xmlDecl);
 	}
 
 	protected boolean isXmlDecl() {
 		return getBool(Option.XmlDecl);
 	}
 
 	protected void setUpperCaseTags(final boolean upperCaseTags) {
 		set(Option.UpperCaseTags, upperCaseTags);
 	}
 
 	protected boolean isUpperCaseTags() {
 		return getBool(Option.UpperCaseTags);
 	}
 
 	protected void setUpperCaseAttrs(final boolean upperCaseAttrs) {
 		set(Option.UpperCaseAttrs, upperCaseAttrs);
 	}
 
 	protected boolean isUpperCaseAttrs() {
 		return getBool(Option.UpperCaseAttrs);
 	}
 
 	protected void setMakeClean(final boolean makeClean) {
 		set(Option.MakeClean, makeClean);
 	}
 
 	protected boolean isMakeClean() {
 		return getBool(Option.MakeClean);
 	}
 
 	protected void setMakeBare(final boolean makeBare) {
 		set(Option.MakeBare, makeBare);
 	}
 
 	protected boolean isMakeBare() {
 		return getBool(Option.MakeBare);
 	}
 
 	protected void setLogicalEmphasis(final boolean logicalEmphasis) {
 		set(Option.LogicalEmphasis, logicalEmphasis);
 	}
 
 	protected boolean isLogicalEmphasis() {
 		return getBool(Option.LogicalEmphasis);
 	}
 
 	protected void setDropFontTags(final boolean dropFontTags) {
 		set(Option.DropFontTags, dropFontTags);
 	}
 
 	protected boolean isDropFontTags() {
 		return getBool(Option.DropFontTags);
 	}
 
 	protected void setDropProprietaryAttributes(final boolean dropProprietaryAttributes) {
 		set(Option.DropPropAttrs, dropProprietaryAttributes);
 	}
 
 	protected boolean isDropProprietaryAttributes() {
 		return getBool(Option.DropPropAttrs);
 	}
 
 	protected void setDropEmptyParas(final boolean dropEmptyParas) {
 		set(Option.DropEmptyParas, dropEmptyParas);
 	}
 
 	protected boolean isDropEmptyParas() {
 		return getBool(Option.DropEmptyParas);
 	}
 
 	protected void setFixComments(final boolean fixComments) {
 		set(Option.FixComments, fixComments);
 	}
 
 	protected boolean isFixComments() {
 		return getBool(Option.FixComments);
 	}
 
 	protected void setTrimEmpty(final boolean trimEmpty) {
 		this.trimEmpty = trimEmpty;
 	}
 
 	protected boolean isTrimEmpty() {
 		return trimEmpty;
 	}
 
 	protected void setBreakBeforeBR(final boolean breakBeforeBR) {
 		set(Option.BreakBeforeBR, breakBeforeBR);
 	}
 
 	protected boolean isBreakBeforeBR() {
 		return getBool(Option.BreakBeforeBR);
 	}
 
 	protected void setNumEntities(final boolean numEntities) {
 		set(Option.NumEntities, numEntities);
 	}
 
 	protected boolean isNumEntities() {
 		return getBool(Option.NumEntities);
 	}
 
 	protected void setQuoteMarks(final boolean quoteMarks) {
 		set(Option.QuoteMarks, quoteMarks);
 	}
 
 	protected boolean isQuoteMarks() {
 		return getBool(Option.QuoteMarks);
 	}
 
 	protected void setQuoteNbsp(final boolean quoteNbsp) {
 		set(Option.QuoteNbsp, quoteNbsp);
 	}
 
 	protected boolean isQuoteNbsp() {
 		return getBool(Option.QuoteNbsp);
 	}
 
 	protected void setQuoteAmpersand(final boolean quoteAmpersand) {
 		set(Option.QuoteAmpersand, quoteAmpersand);
 	}
 
 	protected boolean isQuoteAmpersand() {
 		return getBool(Option.QuoteAmpersand);
 	}
 
 	protected void setWrapAttVals(final boolean wrapAttVals) {
 		set(Option.WrapAttVals, wrapAttVals);
 	}
 
 	protected boolean isWrapAttVals() {
 		return getBool(Option.WrapAttVals);
 	}
 
 	protected void setWrapScriptlets(final boolean wrapScriptlets) {
 		set(Option.WrapScriptlets, wrapScriptlets);
 	}
 
 	protected boolean isWrapScriptlets() {
 		return getBool(Option.WrapScriptlets);
 	}
 
 	protected void setWrapSection(final boolean wrapSection) {
 		set(Option.WrapSection, wrapSection);
 	}
 
 	protected boolean isWrapSection() {
 		return getBool(Option.WrapSection);
 	}
 
 	protected void setWrapAsp(final boolean wrapAsp) {
 		set(Option.WrapAsp, wrapAsp);
 	}
 
 	protected boolean isWrapAsp() {
 		return getBool(Option.WrapAsp);
 	}
 
 	protected void setWrapJste(final boolean wrapJste) {
 		set(Option.WrapJste, wrapJste);
 	}
 
 	protected boolean isWrapJste() {
 		return getBool(Option.WrapJste);
 	}
 
 	protected void setWrapPhp(final boolean wrapPhp) {
 		set(Option.WrapPhp, wrapPhp);
 	}
 
 	protected boolean isWrapPhp() {
 		return getBool(Option.WrapPhp);
 	}
 
 	protected void setFixBackslash(final boolean fixBackslash) {
 		set(Option.FixBackslash, fixBackslash);
 	}
 
 	protected boolean isFixBackslash() {
 		return getBool(Option.FixBackslash);
 	}
 
 	protected void setIndentAttributes(final boolean indentAttributes) {
 		set(Option.IndentAttributes, indentAttributes);
 	}
 
 	protected boolean isIndentAttributes() {
 		return getBool(Option.IndentAttributes);
 	}
 
 	protected void setXmlPIs(final boolean xmlPIs) {
 		set(Option.XmlPIs, xmlPIs);
 	}
 
 	protected boolean isXmlPIs() {
 		return getBool(Option.XmlPIs);
 	}
 
 	protected void setXmlSpace(final boolean xmlSpace) {
 		set(Option.XmlSpace, xmlSpace);
 	}
 
 	protected boolean isXmlSpace() {
 		return getBool(Option.XmlSpace);
 	}
 
 	protected void setEncloseBodyText(final boolean encloseBodyText) {
 		set(Option.EncloseBodyText, encloseBodyText);
 	}
 
 	protected boolean isEncloseBodyText() {
 		return getBool(Option.EncloseBodyText);
 	}
 
 	protected void setEncloseBlockText(final boolean encloseBlockText) {
 		set(Option.EncloseBlockText, encloseBlockText);
 	}
 
 	protected boolean isEncloseBlockText() {
 		return getBool(Option.EncloseBlockText);
 	}
 
 	protected void setKeepFileTimes(final boolean keepFileTimes) {
 		set(Option.KeepFileTimes, keepFileTimes);
 	}
 
 	protected boolean isKeepFileTimes() {
 		return getBool(Option.KeepFileTimes);
 	}
 
 	protected void setWord2000(final boolean word2000) {
 		set(Option.Word2000, word2000);
 	}
 
 	protected boolean isWord2000() {
 		return getBool(Option.Word2000);
 	}
 
 	protected void setTidyMark(final boolean tidyMark) {
 		set(Option.Mark, tidyMark);
 	}
 
 	protected boolean isTidyMark() {
 		return getBool(Option.Mark);
 	}
 
 	protected void setEmacs(final boolean emacs) {
 		set(Option.Emacs, emacs);
 	}
 
 	protected boolean isEmacs() {
 		return getBool(Option.Emacs);
 	}
 
 	protected void setLiteralAttribs(final boolean literalAttribs) {
 		set(Option.LiteralAttribs, literalAttribs);
 	}
 
 	protected boolean isLiteralAttribs() {
 		return getBool(Option.LiteralAttribs);
 	}
 
 	protected void setBodyOnly(final TriState bodyOnly) {
 		set(Option.BodyOnly, bodyOnly);
 	}
 
 	protected TriState getBodyOnly() {
 		return (TriState) getOptionEnum(Option.BodyOnly);
 	}
 
 	protected void setFixUri(final boolean fixUri) {
 		set(Option.FixUri, fixUri);
 	}
 
 	protected boolean isFixUri() {
 		return getBool(Option.FixUri);
 	}
 
 	protected void setLowerLiterals(final boolean lowerLiterals) {
 		set(Option.LowerLiterals, lowerLiterals);
 	}
 
 	protected boolean isLowerLiterals() {
 		return getBool(Option.LowerLiterals);
 	}
 
 	protected void setReplaceColor(final boolean replaceColor) {
 		set(Option.ReplaceColor, replaceColor);
 	}
 
 	protected boolean isReplaceColor() {
 		return getBool(Option.ReplaceColor);
 	}
 
 	protected void setHideComments(final boolean hideComments) {
 		set(Option.HideComments, hideComments);
 	}
 
 	protected boolean isHideComments() {
 		return getBool(Option.HideComments);
 	}
 
 	protected void setIndentCdata(final boolean indentCdata) {
 		set(Option.IndentCdata, indentCdata);
 	}
 
 	protected boolean isIndentCdata() {
 		return getBool(Option.IndentCdata);
 	}
 
 	protected void setForceOutput(final boolean forceOutput) {
 		set(Option.ForceOutput, forceOutput);
 	}
 
 	protected boolean isForceOutput() {
 		return getBool(Option.ForceOutput);
 	}
 
 	protected void setShowErrors(final int showErrors) {
 		set(Option.ShowErrors, showErrors);
 	}
 
 	protected int getShowErrors() {
 		return getInt(Option.ShowErrors);
 	}
 
 	protected void setAsciiChars(final boolean asciiChars) {
 		set(Option.AsciiChars, asciiChars);
 	}
 
 	protected boolean isAsciiChars() {
 		return getBool(Option.AsciiChars);
 	}
 
 	protected void setJoinClasses(final boolean joinClasses) {
 		set(Option.JoinClasses, joinClasses);
 	}
 
 	protected boolean isJoinClasses() {
 		return getBool(Option.JoinClasses);
 	}
 
 	protected void setJoinStyles(final boolean joinStyles) {
 		set(Option.JoinStyles, joinStyles);
 	}
 
 	protected boolean isJoinStyles() {
 		return getBool(Option.JoinStyles);
 	}
 
 	protected void setEscapeCdata(final boolean escapeCdata) {
 		set(Option.EscapeCdata, escapeCdata);
 	}
 
 	protected boolean isEscapeCdata() {
 		return getBool(Option.EscapeCdata);
 	}
 
 	protected void setNcr(final boolean ncr) {
 		set(Option.NCR, ncr);
 	}
 
 	protected boolean isNcr() {
 		return getBool(Option.NCR);
 	}
 
 	protected void setCssPrefix(final String cssPrefix) {
 		set(Option.CSSPrefix, cssPrefix);
 	}
 
 	protected String getCssPrefix() {
 		return getString(Option.CSSPrefix);
 	}
 
 	protected void setReplacementCharEncoding(final String replacementCharEncoding) {
 		this.replacementCharEncoding = replacementCharEncoding;
 	}
 
 	protected String getReplacementCharEncoding() {
 		return replacementCharEncoding;
 	}
 
 	protected void setDefinedTags(final int definedTags) {
 		this.definedTags = definedTags;
 	}
 
 	protected int getDefinedTags() {
 		return definedTags;
 	}
 
 	protected void setNewline(final LineEnding newline) {
 		set(Option.Newline, newline);
 	}
 
 	protected LineEnding getNewline() {
 		return (LineEnding) getOptionEnum(Option.Newline);
 	}
 
 	protected void setRawOut(boolean rawOut) {
 		this.rawOut = rawOut;
 	}
 
 	protected boolean isRawOut() {
 		return rawOut;
 	}
 
 	protected void setAccessibilityCheckLevel(final int accessibilityCheckLevel) {
 		set(Option.AccessibilityCheckLevel, accessibilityCheckLevel);
 	}
 
 	protected int getAccessibilityCheckLevel() {
 		return getInt(Option.AccessibilityCheckLevel);
 	}
 	
 	protected void setVertSpace(final boolean vertSpace) {
 		set(Option.VertSpace, vertSpace);
 	}
 	
 	protected boolean isVertSpace() {
 		return getBool(Option.VertSpace);
 	}
 	
 	protected void setAnchorAsName(final boolean anchorAsName) {
 		set(Option.AnchorAsName, anchorAsName);
 	}
 	
 	protected boolean isAnchorAsName() {
 		return getBool(Option.AnchorAsName);
 	}
 	
 	protected void setPreserveEntities(final boolean preserveEntities) {
 		set(Option.PreserveEntities, preserveEntities);
 	}
 	
 	protected boolean isPreserveEntities() {
 		return getBool(Option.PreserveEntities);
 	}
 	
 	protected void setMergeDivs(final TriState mergeDivs) {
 		set(Option.MergeDivs, mergeDivs);
 	}
 
 	protected TriState getMergeDivs() {
 		return (TriState) getOptionEnum(Option.MergeDivs);
 	}
 
 	protected void setMergeSpans(final TriState mergeSpans) {
 		set(Option.MergeSpans, mergeSpans);
 	}
 
 	protected TriState getMergeSpans() {
 		return (TriState) getOptionEnum(Option.MergeSpans);
 	}
 
 	protected void setTidyCompat(final boolean tidyCompat) {
 		set(Option.TidyCompat, tidyCompat);
 	}
 
 	protected boolean isTidyCompat() {
 		return getBool(Option.TidyCompat);
 	}
 
 	public AttrSortStrategy getSortAttributes() {
 		return (AttrSortStrategy) getOptionEnum(Option.SortAttributes);
 	}
 	
 	public void setSortAttributes(final AttrSortStrategy sortAttributes) {
 		set(Option.SortAttributes, sortAttributes);
 	}
 	
 	public boolean isPunctWrap() {
 		return getBool(Option.PunctWrap);
 	}
 	
 	public void setPunctWrap(final boolean punctWrap) {
 		set(Option.PunctWrap, punctWrap);
 	}
 
 	public boolean isDecorateInferredUL() {
 		return getBool(Option.DecorateInferredUL);
 	}
 	
 	public void setDecorateInferredUL(final boolean decorateInferredUL) {
 		set(Option.DecorateInferredUL, decorateInferredUL);
 	}
 }
