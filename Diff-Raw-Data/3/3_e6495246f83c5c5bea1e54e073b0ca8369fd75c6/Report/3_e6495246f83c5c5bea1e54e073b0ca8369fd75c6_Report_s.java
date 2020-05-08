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
 
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.text.MessageFormat;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import org.w3c.tidy.Node.NodeType;
 import org.w3c.tidy.TidyMessage.Level;
 
 import static org.w3c.tidy.ErrorCode.*;
 
 /**
  * Error/informational message reporter. You should only need to edit the file TidyMessages.properties to localize HTML
  * tidy.
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public final class Report
 {
 
     /**
      * used to point to Web Accessibility Guidelines.
      */
     public static final String ACCESS_URL = "http://www.w3.org/WAI/GL";
 
     /**
      * Release date String.
      */
     public static final String RELEASE_DATE_STRING = readReleaseDate();
     
     private static String readReleaseDate() {
     	final Properties p = new Properties();
     	try {
     		final InputStream s = Report.class.getResourceAsStream("/jtidy.properties");
 			p.load(s);
 			s.close();
     	} catch (Exception e) {
 			throw new RuntimeException("Failed to load jtidy.properties", e);
 		}
 		return p.getProperty("date");
     }
 
 
     /**
      * accessibility flaw: missing image map.
      */
     public static final short MISSING_IMAGE_ALT = 1;
 
     /**
      * accessibility flaw: missing link alt.
      */
     public static final short MISSING_LINK_ALT = 2;
 
     /**
      * accessibility flaw: missing summary.
      */
     public static final short MISSING_SUMMARY = 4;
 
     /**
      * accessibility flaw: missing image map.
      */
     public static final short MISSING_IMAGE_MAP = 8;
 
     /**
      * accessibility flaw: using frames.
      */
     public static final short USING_FRAMES = 16;
 
     /**
      * accessibility flaw: using noframes.
      */
     public static final short USING_NOFRAMES = 32;
 
     /**
      * presentation flaw: using spacer.
      */
     public static final short USING_SPACER = 1;
 
     /**
      * presentation flaw: using layer.
      */
     public static final short USING_LAYER = 2;
 
     /**
      * presentation flaw: using nobr.
      */
     public static final short USING_NOBR = 4;
 
     /**
      * presentation flaw: using font.
      */
     public static final short USING_FONT = 8;
 
     /**
      * presentation flaw: using body.
      */
     public static final short USING_BODY = 16;
 
     /**
      * character encoding error: windows chars.
      */
     public static final short WINDOWS_CHARS = 1;
 
     /**
      * character encoding error: non ascii.
      */
     public static final short NON_ASCII = 2;
 
     /**
      * character encoding error: found utf16.
      */
     public static final short FOUND_UTF16 = 4;
 
     /**
      * char has been replaced.
      */
     public static final short REPLACED_CHAR = 0;
 
     /**
      * char has been discarded.
      */
     public static final short DISCARDED_CHAR = 1;
     
     /* badchar bit field */
 
     public static final int BC_VENDOR_SPECIFIC_CHARS  = 1;
     public static final int BC_INVALID_SGML_CHARS     = 2;
     public static final int BC_INVALID_UTF8           = 4;
     public static final int BC_INVALID_UTF16          = 8;
     public static final int BC_ENCODING_MISMATCH      = 16; /* fatal error */
     public static final int BC_INVALID_URI            = 32;
     public static final int BC_INVALID_NCR            = 64;
 
     /**
      * Resource bundle with messages.
      */
     private static ResourceBundle res;
 
     /**
      * Printed in GNU Emacs messages.
      */
     private String currentFile;
 
     /**
      * message listener for error reporting.
      */
     private TidyMessageListener listener;
 
     static
     {
         try
         {
             res = ResourceBundle.getBundle("org/w3c/tidy/TidyMessages");
         }
         catch (MissingResourceException e)
         {
             throw new Error(e.toString());
         }
     }
 
     /**
      * Instantiated only in Tidy() constructor.
      */
     protected Report()
     {
         super();
     }
 
     /**
      * Generates a complete message for the warning/error. The message is composed by:
      * <ul>
      * <li>position in file</li>
      * <li>prefix for the error level (warning: | error:)</li>
      * <li>message read from ResourceBundle</li>
      * <li>optional parameters added to message using MessageFormat</li>
      * </ul>
      * @param errorCode tidy error code
      * @param lexer Lexer
      * @param level message level. One of <code>TidyMessage.LEVEL_ERROR</code>,
      * <code>TidyMessage.LEVEL_WARNING</code>,<code>TidyMessage.LEVEL_INFO</code>
      * @param messageKey key for the ResourceBundle
      * @param params optional parameters added with MessageFormat
      * @return formatted message
      * @throws MissingResourceException if <code>message</code> key is not available in jtidy resource bundle.
      * @see TidyMessage
      */
     protected String getMessageLexer(int errorCode, Lexer lexer, Level level, String messageKey, Object... params)
         	throws MissingResourceException {
     	final boolean b = lexer != null && level != null;
     	return getMessagePos(errorCode, lexer, level, b ? lexer.lines : 0, b ? lexer.columns : 0,
     			messageKey, params);
     }
     
     /* Updates document message counts and
      ** compares counts to options to see if message
      ** display should go forward.
      */
  	private boolean updateCount(final Lexer lexer, final Level level) {
  		if (lexer == null || level == null) {
  			return true;
  		}
  		/* keep quiet after <ShowErrors> errors */
  		boolean go = lexer.errors < lexer.configuration.getShowErrors();
 
  		switch (level) {
  		case INFO:
  			lexer.infoMessages++;
  			break;
  		case WARNING:
  			lexer.warnings++;
  			go = go && lexer.configuration.isShowWarnings();
  			break;
  		case CONFIG:
  			lexer.optionErrors++;
  			break;
  		case ACCESS:
  			lexer.accessErrors++;
  			break;
  		case ERROR:
  			lexer.errors++;
  			break;
  		case BAD_DOCUMENT:
  			lexer.docErrors++;
  			break;
  		case FATAL:
  			/* Ack! */;
  			break;
  		}
  		return go;
  	}
     
     private String getMessagePos(final int errorCode, final Lexer lexer, final Level level, final int line, final int col,
     		final String messageKey, final Object... args) throws MissingResourceException {
     	boolean go = updateCount(lexer, level);
     	if (go) {
 	    	String position = line > 0 && col > 0 ? getPosition(lexer, line, col) : "";
 	        String prefix = level == Level.SUMMARY ? "" : (level + ": ");
 	        String messageString = MessageFormat.format(res.getString(messageKey), args);
 	        if (listener != null) {
 	            TidyMessage msg = new TidyMessage(errorCode, line, col, level, messageString);
 	            listener.messageReceived(msg);
 	        }
 	        return position + prefix + messageString;
     	}
     	return null;
     }
 
     /**
      * Prints a message to lexer.errout after calling getMessage().
      * @param lexer Lexer
      * @param level message level. One of <code>TidyMessage.LEVEL_ERROR</code>,
      * <code>TidyMessage.LEVEL_WARNING</code>,<code>TidyMessage.LEVEL_INFO</code>
      * @param errorCode tidy error code
      * @param params optional parameters added with MessageFormat
      * @see TidyMessage
      */
     private void messageLexer(final Lexer lexer, final Level level, final ErrorCode errorCode, final Object... params) {
     	messageLexer(errorCode.code(), lexer, level, errorCode.name().toLowerCase(), params);
     }
     
     private void messageNode(final Lexer lexer, final Level level, final Node node, final ErrorCode errorCode, final Object... params) {
     	if (node == null) {
     		messageLexer(lexer, level, errorCode, params);
     	} else {
     		messagePos(errorCode.code(), lexer, level, node.line, node.column, errorCode.name().toLowerCase(), params);
     	}
     }
     
     public void missingAttr(final Lexer lexer, final Node node, final String name) {
         messageNode(lexer, Level.WARNING, node, MISSING_ATTRIBUTE, getTagName(node), name);
     }
     
     private void messagePos(final int errorCode, final Lexer lexer, final Level level, final int line, final int col,
     		final String messageKey, final Object... params) {
         try {
             final String s = getMessagePos(errorCode, lexer, level, line, col, messageKey, params);
             if (s != null) {
             	lexer.errout.println(s);
             }
         } catch (MissingResourceException e) {
             lexer.errout.println("Can't find message string for \"" + messageKey + "\"!");
         }
     }
     
     private void messageLexer(final int errorCode, final Lexer lexer, final Level level, final String messageKey,
     		final Object... params) {
     	final boolean b = lexer != null && level != Level.SUMMARY;
     	messagePos(errorCode, lexer, level, b ? lexer.lines : 0, b ? lexer.columns : 0, messageKey, params);
     }
     
     private void simpleMessage(final int errorCode, final Lexer lexer, final Level level, final String messageKey,
     		final Object... params) {
     	messagePos(errorCode, lexer, level, 0, 0, messageKey, params);
     }
     
     /**
      * Prints a message to errout after calling getMessage(). Used when lexer is not yet defined.
      * @param errout PrintWriter
      * @param level message level. One of <code>TidyMessage.LEVEL_ERROR</code>,
      * <code>TidyMessage.LEVEL_WARNING</code>,<code>TidyMessage.LEVEL_INFO</code>
      * @param message key for the ResourceBundle
      * @param params optional parameters added with MessageFormat
      * @see TidyMessage
      */
     private void printMessage(PrintWriter errout, Level level, String message, Object... params) {
         try {
         	errout.println(getMessageLexer(-1, null, level, message, params));
         } catch (MissingResourceException e) {
         	errout.println("Can't find message string for \"" + message + "\"!");
         }
     }
 
     /**
      * print version information.
      * @param p printWriter
      */
     public void showVersion(final PrintWriter p) {
         printMessage(p, null, "version_summary", RELEASE_DATE_STRING);
     }
 
     /**
      * Returns a formatted tag name handling start and ent tags, nulls, doctypes, and text.
      * @param tag Node
      * @return formatted tag name
      */
     private String getTagName(final Node tag) {
         if (tag != null) {
             if (tag.isElement()) {
                 return "<" + tag.element + ">";
             }
             else if (tag.type == NodeType.EndTag) {
                 return "</" + tag.element + ">";
             }
             else if (tag.type == NodeType.DocTypeTag) {
                 return "<!DOCTYPE>";
             }
             else if (tag.type == NodeType.TextNode) {
                 return "plain text";
             }
             else if (tag.type == NodeType.XmlDecl) {
                 return "XML declaration";
             }
             else if (tag.element != null) {
                 return tag.element;
             }
         }
         return "";
     }
 
     /**
      * Prints an "unknown option" error message. Lexer is not defined when this is called.
      * @param option unknown option name
      */
     public void unknownOption(String option)
     {
         try
         {
             System.err.println(MessageFormat.format(res.getString("unknown_option"), option));
         }
         catch (MissingResourceException e)
         {
             System.err.println(e.toString());
         }
     }
 
     /**
      * Prints a "bad argument" error message. Lexer is not defined when this is called.
      * @param value bad argument value
      * @param option option object
      */
     public void badArgument(final String value, final Option option) {
         try {
             System.err.println(MessageFormat.format(res.getString("bad_argument"), option.getName(), value));
         } catch (MissingResourceException e) {
             System.err.println(e.toString());
         }
     }
 
     /**
      * Returns a formatted String describing the current position in file.
      */
     private String getPosition(final Lexer lexer, final int line, final int col) {
         // Change formatting to be parsable by GNU Emacs
         if (lexer.configuration.isEmacs()) {
             return MessageFormat.format(res.getString("emacs_format"), this.currentFile,
                 line, col) + " ";
         }
         // traditional format
         return MessageFormat.format(res.getString("line_column"), line, col);
     }
 
     /**
      * Prints encoding error messages.
      * @param lexer Lexer
      * @param code error code
      * @param c invalid char
      */
     public void encodingError(Lexer lexer, ErrorCode code, int c, int replaceMode)
     {
         if (lexer.errors > lexer.configuration.getShowErrors()) // keep quiet after <showErrors> errors
         {
             return;
         }
 
         if (lexer.configuration.isShowWarnings())
         {
             String buf = Integer.toHexString(c);
 
             // An encoding mismatch is currently treated as a non-fatal error
             switch(code) {
             case ENCODING_MISMATCH:
                 // actual encoding passed in "c"
                 lexer.badChars |= BC_ENCODING_MISMATCH;
                 messageLexer(
                     code.code(),
                     lexer,
                     Level.WARNING,
                     "encoding_mismatch",
                     
                         lexer.configuration.getInCharEncodingName(),
                         ParsePropertyImpl.CHAR_ENCODING.getFriendlyName(null, new Integer(c), lexer.configuration));
                 break;
             case VENDOR_SPECIFIC_CHARS:
                 lexer.badChars |= BC_VENDOR_SPECIFIC_CHARS;
                 messageLexer(
                     code.code(),
                     lexer,
                     Level.WARNING,
                     "invalid_char",
                     replaceMode, buf);
                 break;
             case INVALID_SGML_CHARS:
                 lexer.badChars |= BC_INVALID_SGML_CHARS;
                 messageLexer(
                     code.code(),
                     lexer,
                     Level.WARNING,
                     "invalid_char",
                     replaceMode, buf);
                 break;
             case INVALID_UTF8:
                 lexer.badChars |= BC_INVALID_UTF8;
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     replaceMode, buf);
                 break;
             case INVALID_UTF16:
                 lexer.badChars |= BC_INVALID_UTF16;
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     replaceMode, buf);
                 break;
             case INVALID_NCR:
                 lexer.badChars |= BC_INVALID_NCR;
                 messageLexer(lexer, Level.WARNING, code, replaceMode, c);
                 break;
             }
         }
     }
 
     /**
      * Prints entity error messages.
      * @param lexer Lexer
      * @param code error code
      * @param entity invalid entity String
      * @param c invalid char
      */
     public void entityError(final Lexer lexer, final ErrorCode code, final String entity, int c) {
         messageLexer(lexer, Level.WARNING, code, entity);
     }
 
     /**
      * Prints error messages for attributes.
      * @param lexer Lexer
      * @param node current tag
      * @param attribute attribute
      * @param code error code
      */
     public void attrError(Lexer lexer, Node node, AttVal attribute, ErrorCode code)
     {
         if (lexer.errors > lexer.configuration.getShowErrors()) // keep quiet after <showErrors> errors
         {
             return;
         }
 
         if (!lexer.configuration.isShowWarnings()) // warnings
         {
             return;
         }
         
         final String tagdesc = getTagName(node);
         final String name = attribute == null ? null : attribute.attribute;
         final String value = attribute == null ? null : attribute.value;
 
         switch (code)
         {
             case UNKNOWN_ATTRIBUTE :
                 messageLexer(lexer, Level.WARNING, code, attribute.attribute);
                 break;
                 
             case INSERTING_ATTRIBUTE:
             case MISSING_ATTR_VALUE :
             	messageNode(lexer, Level.WARNING, node, code, tagdesc, name);
                 break;
 
             case MISSING_ATTRIBUTE :
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(node), attribute.attribute);
                 break;
 
             case MISSING_IMAGEMAP :
             	messageNode(lexer, Level.WARNING, node, code, tagdesc);
                 lexer.badAccess |= MISSING_IMAGE_MAP;
                 break;
 
            case BAD_ATTRIBUTE_VALUE :
             case INVALID_ATTRIBUTE:
                 messageNode(lexer, Level.WARNING, node, code, tagdesc, name, value);
                 break;
 
             case XML_ID_SYNTAX :
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(node), attribute.attribute);
                 break;
 
             case XML_ATTRIBUTE_VALUE :
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(node), attribute.attribute);
                 break;
 
             case UNEXPECTED_QUOTEMARK :
             case MISSING_QUOTEMARK :
             case ID_NAME_MISMATCH :
             case BACKSLASH_IN_URI :
             case FIXED_BACKSLASH :
             case ILLEGAL_URI_REFERENCE :
             case ESCAPED_ILLEGAL_URI :
             case NEWLINE_IN_URI :
             case WHITE_IN_URI:
             case UNEXPECTED_GT:
             case INVALID_XML_ID:
                 messageNode(lexer, Level.WARNING, node, code, tagdesc);
                 break;
 
             case REPEATED_ATTRIBUTE :
                 messageLexer(lexer, Level.WARNING, code, 
 					    getTagName(node),
 					    attribute.value,
 					    attribute.attribute);
                 break;
 
             case PROPRIETARY_ATTR_VALUE :
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(node), attribute.value);
                 break;
 
             case PROPRIETARY_ATTRIBUTE :
             	messageNode(lexer, Level.WARNING, node, code, tagdesc, name);
                 break;
 
             case UNEXPECTED_END_OF_FILE :
                 // on end of file adjust reported position to end of input
                 lexer.lines = lexer.in.getCurline();
                 lexer.columns = lexer.in.getCurcol();
                 messageLexer(lexer, Level.WARNING, code, getTagName(node));
                 break;
 
             case ANCHOR_NOT_UNIQUE :
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(node), attribute.value);
                 break;
 
             case ENTITY_IN_ID :
                 messageLexer(lexer, Level.WARNING, code);
                 break;
 
             case JOINING_ATTRIBUTE :
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(node), attribute.attribute);
                 break;
 
             case UNEXPECTED_EQUALSIGN :
                 messageLexer(lexer, Level.WARNING, code, getTagName(node));
                 break;
 
             case ATTR_VALUE_NOT_LCASE :
             	messageNode(lexer, Level.WARNING, node, code, tagdesc, value); 
                 break;
 
             default :
                 break;
         }
     }
 
     /**
      * Prints warnings.
      * @param lexer Lexer
      * @param element parent/missing tag
      * @param node current tag
      * @param code error code
      */
     public void warning(Lexer lexer, Node element, Node node, ErrorCode code)
     {
     	final String nodedesc = getTagName(node);
     	final Node rpt = element != null ? element : node;
     	
         // keep quiet after <showErrors> errors
         if (lexer.errors > lexer.configuration.getShowErrors())
         {
             return;
         }
 
         switch (code) {
             case MISSING_ENDTAG_FOR :
                 messageNode(lexer, Level.WARNING, rpt, code, element.element);
                 break;
 
             case MISSING_ENDTAG_BEFORE :
             	messageNode(lexer, Level.WARNING, rpt, code, element.element, nodedesc);
                 break;
 
             case DISCARDING_UNEXPECTED :
                 if (lexer.badForm == 0)
                 {
                     // the case for when this is an error not a warning, is handled later
                     messageLexer(
                         lexer,
                         Level.WARNING,
                         code,
                         getTagName(node));
                 }
                 break;
 
             case NESTED_EMPHASIS :
                 messageLexer(lexer, Level.INFO, code, getTagName(node));
                 break;
 
             case COERCE_TO_ENDTAG :
                 messageNode(lexer, Level.WARNING, rpt, code, element.element);
                 break;
 
             case NON_MATCHING_ENDTAG :
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(node), element.element);
                 break;
 
             case TAG_NOT_ALLOWED_IN :
             	messageNode(lexer, Level.WARNING, node, code, nodedesc, element.element);
                 if (lexer.configuration.isShowWarnings()) {
                 	messageNode(lexer, Level.INFO, element, PREVIOUS_LOCATION, element.element);
                 }
                 break;
 
             case DOCTYPE_AFTER_TAGS :
                 messageLexer(lexer, Level.WARNING, code);
                 break;
 
             case MISSING_STARTTAG :
             case UNEXPECTED_ENDTAG :
             case TOO_MANY_ELEMENTS :
             case INSERTING_TAG :
             	messageNode(lexer, Level.WARNING, node, code, node.element);
                 break;
 
             case USING_BR_INPLACE_OF :
             case CANT_BE_NESTED :
             case PROPRIETARY_ELEMENT :
             	messageNode(lexer, Level.WARNING, node, code, nodedesc);
                 break;
 
             case OBSOLETE_ELEMENT :
                 messageLexer(lexer, Level.WARNING, code, 
 						    getTagName(element),
 						    getTagName(node));
                 break;
 
             case UNESCAPED_ELEMENT :
                 messageLexer(lexer, Level.WARNING, code, getTagName(element));
                 break;
 
             case TRIM_EMPTY_ELEMENT :
             	messageNode(lexer, Level.WARNING, element, code, getTagName(element));
                 break;
 
             case ILLEGAL_NESTING :
                 messageLexer(lexer, Level.WARNING, code, getTagName(element));
                 break;
 
             case NOFRAMES_CONTENT :
                 messageLexer(lexer, Level.WARNING, code, getTagName(node));
                 break;
 
             case MISSING_TITLE_ELEMENT :
             case INCONSISTENT_VERSION :
             case MALFORMED_DOCTYPE :
             case CONTENT_AFTER_BODY :
             case MALFORMED_COMMENT :
             case BAD_COMMENT_CHARS :
             case BAD_XML_COMMENT :
             case BAD_CDATA_CONTENT :
             case INCONSISTENT_NAMESPACE :
             case DTYPE_NOT_UPPER_CASE :
             	messageNode(lexer, Level.WARNING, rpt, code);
                 break;
 
             case UNEXPECTED_END_OF_FILE :
                 // on end of file adjust reported position to end of input
                 lexer.lines = lexer.in.getCurline();
                 lexer.columns = lexer.in.getCurcol();
                 messageLexer(
                     lexer,
                     Level.WARNING,
                     code,
                     getTagName(element));
                 break;
 
             case NESTED_QUOTATION :
             	messageNode(lexer, Level.WARNING, rpt, code);
                 break;
 
             case ELEMENT_NOT_EMPTY :
                 messageLexer(lexer, Level.WARNING, code, getTagName(element));
                 break;
 
             case MISSING_DOCTYPE :
                 messageLexer(lexer, Level.WARNING, code);
                 break;
                 
             case REPLACING_ELEMENT:
             case REPLACING_UNEX_ELEMENT:
                 messageNode(lexer, Level.WARNING, rpt, code, getTagName(element), nodedesc);
                 break;
 
             default :
                 break;
         }
 
         if ((code == DISCARDING_UNEXPECTED) && lexer.badForm != 0)
         {
             // the case for when this is a warning not an error, is handled earlier
             messageLexer(lexer, Level.ERROR, code, getTagName(node));
         }
 
     }
 
     /**
      * Prints errors.
      * @param lexer Lexer
      * @param element parent/missing tag
      * @param node current tag
      * @param code error code
      */
     public void error(Lexer lexer, Node element, Node node, ErrorCode code) {
     	final Node rpt = element != null ? element : node;
     	
         switch (code) {
         case SUSPECTED_MISSING_QUOTE:
             messageLexer(lexer, Level.ERROR, code);
             break;
         case DUPLICATE_FRAMESET:
         	messageNode(lexer, Level.ERROR, rpt, code);
             break;
         case UNKNOWN_ELEMENT:
             messageLexer(lexer, Level.ERROR, code, getTagName(node));
             break;
         case UNEXPECTED_ENDTAG_IN:
             messageLexer(lexer, Level.ERROR, code, node.element, element.element);
             break;
         case UNEXPECTED_ENDTAG:
             messageLexer(lexer, Level.ERROR, code, node.element);
             break;
         }
     }
 
     /**
      * Prints error summary.
      * @param lexer Lexer
      */
     public void errorSummary(Lexer lexer)
     {
         // adjust badAccess to that its null if frames are ok
         if ((lexer.badAccess & (USING_FRAMES | USING_NOFRAMES)) != 0)
         {
             if (!(((lexer.badAccess & USING_FRAMES) != 0) && ((lexer.badAccess & USING_NOFRAMES) == 0)))
             {
                 lexer.badAccess &= ~(USING_FRAMES | USING_NOFRAMES);
             }
         }
         if (lexer.badChars != 0)
         {
             if ((lexer.badChars & BC_VENDOR_SPECIFIC_CHARS) != 0)
             {
                 int encodingChoiche = 0;
 
                 if ("Cp1252".equals(lexer.configuration.getInCharEncodingName()))
                 {
                     encodingChoiche = 1;
                 }
                 else if ("MacRoman".equals(lexer.configuration.getInCharEncodingName()))
                 {
                     encodingChoiche = 2;
                 }
 
                 messageLexer(lexer, Level.SUMMARY, VENDOR_SPECIFIC_CHARS, encodingChoiche);
             }
 
             if ((lexer.badChars & BC_INVALID_SGML_CHARS) != 0 || (lexer.badChars & BC_INVALID_NCR) != 0)
             {
                 int encodingChoiche = 0;
 
                 if ("Cp1252".equals(lexer.configuration.getInCharEncodingName()))
                 {
                     encodingChoiche = 1;
                 }
                 else if ("MacRoman".equals(lexer.configuration.getInCharEncodingName()))
                 {
                     encodingChoiche = 2;
                 }
 
                 messageLexer(lexer, Level.SUMMARY, INVALID_SGML_CHARS, encodingChoiche);
             }
 
             if ((lexer.badChars & BC_INVALID_UTF8) != 0)
             {
                 messageLexer(INVALID_UTF8.code(), lexer, Level.SUMMARY, "invalid_utf8_summary");
             }
 
             if ((lexer.badChars & BC_INVALID_UTF16) != 0)
             {
                 messageLexer(INVALID_UTF16.code(), lexer, Level.SUMMARY, "invalid_utf16_summary");
             }
 
             if ((lexer.badChars & BC_INVALID_URI) != 0)
             {
                 messageLexer(INVALID_URI.code(), lexer, Level.SUMMARY, "invaliduri_summary");
             }
         }
 
         if (lexer.badForm != 0)
         {
             messageLexer(lexer, Level.SUMMARY, BADFORM_SUMMARY);
         }
 
         if (lexer.badAccess != 0)
         {
             if ((lexer.badAccess & MISSING_SUMMARY) != 0)
             {
                 messageLexer(MISSING_SUMMARY, lexer, Level.SUMMARY, "badaccess_missing_summary");
             }
 
             if ((lexer.badAccess & MISSING_IMAGE_ALT) != 0)
             {
                 messageLexer(MISSING_IMAGE_ALT, lexer, Level.SUMMARY, "badaccess_missing_image_alt");
             }
 
             if ((lexer.badAccess & MISSING_IMAGE_MAP) != 0)
             {
                 messageLexer(MISSING_IMAGE_MAP, lexer, Level.SUMMARY, "badaccess_missing_image_map");
             }
 
             if ((lexer.badAccess & MISSING_LINK_ALT) != 0)
             {
                 messageLexer(MISSING_LINK_ALT, lexer, Level.SUMMARY, "badaccess_missing_link_alt");
             }
 
             if (((lexer.badAccess & USING_FRAMES) != 0) && ((lexer.badAccess & USING_NOFRAMES) == 0))
             {
                 messageLexer(USING_FRAMES, lexer, Level.SUMMARY, "badaccess_frames");
             }
 
             messageLexer(lexer, Level.SUMMARY, lexer.configuration.isTidyCompat() ? BADACCESS_SUMMARY_BAD
             		: BADACCESS_SUMMARY, ACCESS_URL);
         }
 
         if (lexer.badLayout != 0)
         {
             if ((lexer.badLayout & USING_LAYER) != 0)
             {
                 messageLexer(USING_LAYER, lexer, Level.SUMMARY, "badlayout_using_layer");
             }
 
             if ((lexer.badLayout & USING_SPACER) != 0)
             {
                 messageLexer(USING_SPACER, lexer, Level.SUMMARY, "badlayout_using_spacer");
             }
 
             if ((lexer.badLayout & USING_FONT) != 0)
             {
                 messageLexer(USING_FONT, lexer, Level.SUMMARY, "badlayout_using_font");
             }
 
             if ((lexer.badLayout & USING_NOBR) != 0)
             {
                 messageLexer(USING_NOBR, lexer, Level.SUMMARY, "badlayout_using_nobr");
             }
 
             if ((lexer.badLayout & USING_BODY) != 0)
             {
                 messageLexer(USING_BODY, lexer, Level.SUMMARY, "badlayout_using_body");
             }
         }
     }
 
     /**
      * Prints the "unknown option" message.
      * @param errout PrintWriter
      * @param c invalid option char
      */
     public void unknownOption(PrintWriter errout, char c)
     {
         printMessage(errout, Level.ERROR, "unrecognized_option", new String(new char[]{c}));
     }
 
     /**
      * Prints the "unknown file" message.
      * @param errout PrintWriter
      * @param file invalid file name
      */
     public void unknownFile(PrintWriter errout, String file)
     {
         printMessage(errout, Level.ERROR, "unknown_file", "Tidy", file);
     }
 
     /**
      * Prints the "needs author intervention" message.
      * @param errout PrintWriter
      */
     public void needsAuthorIntervention(PrintWriter errout)
     {
         printMessage(errout, Level.SUMMARY, "needs_author_intervention");
     }
 
     /**
      * Prints the "missing body" message.
      * @param errout PrintWriter
      */
     public void missingBody(PrintWriter errout)
     {
         printMessage(errout, Level.ERROR, "missing_body");
     }
 
     /**
      * Prints the number of generated slides.
      * @param errout PrintWriter
      * @param count slides count
      */
     public void reportNumberOfSlides(PrintWriter errout, int count)
     {
         printMessage(errout, Level.SUMMARY, "slides_found", new Integer(count));
     }
 
     /**
      * Prints tidy general info.
      * @param errout PrintWriter
      */
     public void generalInfo(PrintWriter errout)
     {
         printMessage(errout, null, "general_info");
     }
 
     /**
      * Sets the current file name.
      * @param filename current file.
      */
     public void setFilename(String filename)
     {
         this.currentFile = filename; // for use with Gnu Emacs
     }
 
     /**
      * Prints information for html version in input file.
      * @param lexer Lexer
      */
     public void reportVersion(final Lexer lexer) {
     	if (lexer.givenDoctype != null) {
     		simpleMessage(DOCTYPE_GIVEN_SUMMARY.code(), lexer, Level.INFO, "doctype_given",
                     lexer.givenDoctype);
     	}
     	if (!lexer.configuration.isXmlTags()) {
     		final int apparentVers = lexer.apparentVersion();
     		final String vers = Lexer.getNameFromVers(apparentVers);
     		
             simpleMessage(REPORT_VERSION_SUMMARY.code(), lexer, Level.INFO, "report_version", 
     			    (vers != null ? vers : "HTML Proprietary"));
             if (lexer.warnMissingSIInEmittedDocType()) {
             	simpleMessage(-1, lexer, Level.INFO, "no_si");
             }
     	}
     }
 
     /**
      * Prints the number of error/warnings found.
      * @param errout PrintWriter
      * @param lexer Lexer
      */
     public void reportNumWarnings(PrintWriter errout, Lexer lexer)
     {
         if (lexer.warnings > 0 || lexer.errors > 0)
         {
             printMessage(
                 errout,
                 Level.SUMMARY,
                 "num_warnings",
                 lexer.warnings, lexer.errors);
         }
         else
         {
             printMessage(errout, Level.SUMMARY, "no_warnings");
         }
     }
 
     /**
      * Prints tidy help.
      * @param out PrintWriter
      */
     public void helpText(PrintWriter out)
     {
         printMessage(out, null, "help_text", "Tidy", RELEASE_DATE_STRING);
     }
 
     /**
      * Prints the "bad tree" message.
      * @param errout PrintWriter
      */
     public void badTree(PrintWriter errout)
     {
         printMessage(errout, Level.ERROR, "bad_tree");
     }
 
     /**
      * Adds a message listener.
      * @param listener TidyMessageListener
      */
     public void addMessageListener(TidyMessageListener listener)
     {
         this.listener = listener;
     }
 }
