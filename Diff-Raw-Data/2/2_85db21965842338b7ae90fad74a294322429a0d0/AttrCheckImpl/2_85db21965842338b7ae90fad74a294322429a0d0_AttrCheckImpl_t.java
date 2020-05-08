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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.w3c.tidy.Versions.*;
 
 /**
  * Check attribute values implementations.
  * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
  * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
  * @author Fabrizio Giustina
  * @version $Revision$ ($Author$)
  */
 public final class AttrCheckImpl
 {
 	public static final AttrCheck PCDATA = null;
 	
 	/**
      * checker for "charset" attribute. Actually null (no validation).
      */
     public static final AttrCheck CHARSET = null;
     
     /**
      * checker for "type" attribute
      */
     public static final AttrCheck TYPE = new CheckType();
     
     public static final AttrCheck XTYPE = null;
     
     /**
      * checker for attributes that can contain a single character. Actually null (no validation).
      */
     public static final AttrCheck CHARACTER = null;
     
     /**
      * checker for attributes which contain a list of urls. Actually null (no validation).
      */
     public static final AttrCheck URLS = null;
     
     /**
      * checker for URLs.
      */
     public static final AttrCheck URL = new CheckUrl();
     
     /**
      * checker for scripts.
      */
     public static final AttrCheck SCRIPT = new CheckScript();
     
     /**
      * checker for "align" attribute.
      */
     public static final AttrCheck ALIGN = new CheckAlign();
 
     /**
      * checker for "valign" attribute.
      */
     public static final AttrCheck VALIGN = new CheckValign();
     
     /**
      * checker for "color" attribute.
      */
     public static final AttrCheck COLOR = new CheckColor();
     
     /**
      * checker for "clear" attribute.
      */
     public static final AttrCheck CLEAR = new CheckClear();
     
     public static final AttrCheck BORDER = new CheckBool(); // kludge
     
     /**
      * checker for "lang" and "xml:lang" attributes.
      */
     public static final AttrCheck LANG = new CheckLang();
     
     /**
      * checker for boolean attributes.
      */
     public static final AttrCheck BOOL = new CheckBool();
     
     /**
      * checker for "cols" attribute. Actually null (no validation).
      */
     public static final AttrCheck COLS = null;
     
     /**
      * checker for "number" attribute.
      */
     public static final AttrCheck NUMBER = new CheckNumber();
     
     /**
      * checker for "length" attribute.
      */
     public static final AttrCheck LENGTH = new CheckLength();
     
     /**
      * checker for "coords" attribute. Actually null (no validation).
      */
     public static final AttrCheck COORDS = null;
     
     /**
      * checker for attributes containing dates. Actually null (no validation).
      */
     public static final AttrCheck DATE = null;
     
     /**
      * checker for "dir" attribute.
      */
     public static final AttrCheck TEXTDIR = new CheckTextDir();
     
     public static final AttrCheck IDREFS = null;
     
     /**
      * checker for attributes referencing an id. Actually null (no validation).
      */
     public static final AttrCheck IDREF = null;
     
     /**
      * checker for ids.
      */
     public static final AttrCheck IDDEF = new CheckId();
     
     /**
      * checker for "name" attribute.
      */
     public static final AttrCheck NAME = new CheckName();
     
     /**
      * checker for table "frame" attribute. Actually null (no validation).
      */
     public static final AttrCheck TFRAME = null;
 
     /**
      * checker for "frameborder" attribute. Actually null (no validation).
      */
     public static final AttrCheck FBORDER = null;
     
     /**
      * checker for "media" attribute. Actually null (no validation).
      */
     public static final AttrCheck MEDIA = null;
     
     /**
      * checker for "submit" attribute.
      */
     public static final AttrCheck FSUBMIT = new CheckFsubmit();
     
     /**
      * checker for "rel" and "rev" attributes. Actually null (no validation).
      */
     public static final AttrCheck LINKTYPES = null;
 
     /**
      * checker for table "rules" attribute. Actually null (no validation).
      */
     public static final AttrCheck TRULES = null;
     
     /**
      * checker for "scope" attribute.
      */
     public static final AttrCheck SCOPE = new CheckScope();
     
     /**
      * checker for "shape" attribute.
      */
     public static final AttrCheck SHAPE = new CheckShape();
     
     /**
      * checker for "scroll" attribute.
      */
     public static final AttrCheck SCROLL = new CheckScroll();
     
     /**
      * checker for "target" attribute.
      */
     public static final AttrCheck TARGET = new CheckTarget();
 
     /**
      * checker for "vtype" attribute.
      */
     public static final AttrCheck VTYPE = new CheckVType();
 
     /**
      * checker for text attributes.
      */
     public static final AttrCheck ACTION = new CheckAction();
     
 
     /**
      * utility class, don't instantiate.
      */
     private AttrCheckImpl() {
         // empty private constructor
     }
 
     /**
      * AttrCheck implementation for checking URLs.
      */
     public static class CheckUrl implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             int escapeCount = 0;
             boolean backslashFound = false;
 
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
 
             String p = attval.value;
             boolean isJavascript = attval.value.startsWith("javascript:");
 
             for (int i = 0; i < p.length(); ++i) {
                 char c = p.charAt(i);
                 // find \
                 if (c == '\\') {
                     backslashFound = true;
                 } else if ((c > 0x7e) || (c <= 0x20) || (c == '<') || (c == '>')) {
                     ++escapeCount;
                 }
             }
 
             // backslashes found, fix them
            if (lexer.configuration.isFixBackslash() && backslashFound && !isJavascript) {
                 attval.value = p = p.replace('\\', '/');
             }
 
             // non-ascii chars found, fix them
             if (lexer.configuration.isFixUri() && escapeCount > 0) {
                 StringBuilder dest = new StringBuilder(p.length() + escapeCount * 2);
 
                 for (int i = 0; i < p.length(); ++i) {
                     char c = p.charAt(i);
                     if ((c > 0x7e) || (c <= 0x20) || (c == '<') || (c == '>')) {
                         dest.append(String.format("%%%02X", (int) c));
                     } else {
                         dest.append(c);
                     }
                 }
 
                 attval.value = dest.toString();
             }
             if (backslashFound) {
                 if (lexer.configuration.isFixBackslash() && !isJavascript) {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.FIXED_BACKSLASH);
                 } else {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.BACKSLASH_IN_URI);
                 }
             }
             if (escapeCount > 0) {
                 if (lexer.configuration.isFixUri()) {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.ESCAPED_ILLEGAL_URI);
                 } else {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.ILLEGAL_URI_REFERENCE);
                 }
 
                 lexer.badChars |= Report.BC_INVALID_URI;
             }
         }
     }
 
     /* RFC 2396, section 4.2 states:
 	    "[...] in the case of HTML's FORM element, [...] an
 	    empty URI reference represents the base URI of the
 	    current document and should be replaced by that URI
 	    when transformed into a request."
 	 */
 	public static class CheckAction implements AttrCheck {
 		public void check(final Lexer lexer, final Node node, final AttVal attval) {
 			if (attval.hasValue()) {
 				URL.check(lexer, node, attval);
 			}
 		}
 	}
 
     /**
      * AttrCheck implementation for checking scripts.
      */
     public static class CheckScript implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             // not implemented
         }
     }
 
     private static void checkAttrValidity(final Lexer lexer, final Node node, final AttVal attval,
             final String list[]) {
     	if (!attval.hasValue()) {
     		lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
     		return;
     	}
 
     	attval.checkLowerCaseAttrValue(lexer, node);
 
     	if (!attval.valueIsAmong(list)) {
     		lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
     	}
     }
 
     /**
      * AttrCheck implementation for checking the "name" attribute.
      */
     public static class CheckName implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
             if (node.isAnchorElement()) {
             	if (lexer.configuration.isXmlOut() && !TidyUtils.isValidNMTOKEN(attval.value)) {
             		lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
             	}
             	final Node old = lexer.configuration.tt.getNodeByAnchor(attval.value);
                 if (old != null && old != node) {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.ANCHOR_NOT_UNIQUE);
                 } else {
                     lexer.configuration.tt.addAnchor(attval.value, node);
                 }
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking ids.
      */
     public static class CheckId implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
             if (!TidyUtils.isValidHTMLID(attval.value)) {
                 if (lexer.isvoyager && TidyUtils.isValidXMLID(attval.value)) {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.XML_ID_SYNTAX);
                 } else {
                 	lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
                 }
             }
 
             final Node old = lexer.configuration.tt.getNodeByAnchor(attval.value);
             if (old != null && old != node) {
             	lexer.report.attrError(lexer, node, attval, ErrorCode.ANCHOR_NOT_UNIQUE);
             } else {
             	lexer.configuration.tt.addAnchor(attval.value, node);
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking boolean attributes.
      */
     public static class CheckBool implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             if (!attval.hasValue()) {
                 return;
             }
 
             attval.checkLowerCaseAttrValue(lexer, node);
         }
     }
 
     /**
      * AttrCheck implementation for checking the "align" attribute.
      */
     public static class CheckAlign implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"left", "right", "center", "justify"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             // IMG, OBJECT, APPLET and EMBED use align for vertical position
             if (node.tag != null && ((node.tag.model & Dict.CM_IMG) != 0)) {
                 VALIGN.check(lexer, node, attval);
                 return;
             }
 
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
 
             attval.checkLowerCaseAttrValue(lexer, node);
             
             // currently CheckCaption(...) takes care of the remaining cases
             if (node.is(TagId.CAPTION)) {
             	return;
             }
             
             if (!attval.valueIsAmong(VALID_VALUES)) {
             	// align="char" is allowed for elements with CM_TABLE|CM_ROW
                 // except CAPTION which is excluded above
             	if (!(attval.valueIs("char") && node.hasCM(Dict.CM_TABLE|Dict.CM_ROW))) {
             		lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
             	}
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking the "valign" attribute.
      */
     public static class CheckValign implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"top", "middle", "bottom", "baseline"};
 
         /**
          * valid values for this attribute (only for img tag).
          */
         private static final String[] VALID_VALUES_IMG = {"left", "right"};
 
         /**
          * proprietary values for this attribute.
          */
         private static final String[] VALID_VALUES_PROPRIETARY = {
             "texttop",
             "absmiddle",
             "absbottom",
             "textbottom"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
 
             attval.checkLowerCaseAttrValue(lexer, node);
 
             if (attval.valueIsAmong(VALID_VALUES)) {
                 // all is fine
             } else if (attval.valueIsAmong(VALID_VALUES_IMG)) {
                 if (!(node.tag != null && ((node.tag.model & Dict.CM_IMG) != 0))) {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
                 }
             } else if (attval.valueIsAmong(VALID_VALUES_PROPRIETARY)) {
                 lexer.constrainVersion(VERS_PROPRIETARY);
                 lexer.report.attrError(lexer, node, attval, ErrorCode.PROPRIETARY_ATTR_VALUE);
             } else {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking the "length" attribute.
      */
     public static class CheckLength implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
 
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
 
             // don't check for <col width=...> and <colgroup width=...>
             if (attval.is(AttrId.WIDTH) && (node.is(TagId.COL) || node.is(TagId.COLGROUP))) {
                 return;
             }
 
             String p = attval.value;
 
             if (p.length() == 0 || !Character.isDigit(p.charAt(0))) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
             } else {
                 for (int j = 1; j < p.length(); j++) {
                     if (!Character.isDigit(p.charAt(j)) && p.charAt(j) != '%') {
                         lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
                         break;
                     }
                 }
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking the "target" attribute.
      */
     public static class CheckTarget implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"_blank", "_self", "_parent", "_top"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
 
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
 
             String value = attval.value;
 
             // target names must begin with A-Za-z ...
             if (value.length() > 0 && Character.isLetter(value.charAt(0))) {
                 return;
             }
 
             // or be one of the allowed list
             if (!attval.valueIsAmong(VALID_VALUES)) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking the "submit" attribute.
      */
     public static class CheckFsubmit implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"get", "post"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	checkAttrValidity(lexer, node, attval, VALID_VALUES);
         }
     }
 
     /**
      * AttrCheck implementation for checking the "clear" attribute.
      */
     public static class CheckClear implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"none", "left", "right", "all"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 if (attval.value == null) { // TODO redundant check?
                 	attval.value = "none";
                 }
                 return;
             }
 
             attval.checkLowerCaseAttrValue(lexer, node);
 
             if (!attval.valueIsAmong(VALID_VALUES)) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking the "shape" attribute.
      */
     public static class CheckShape implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"rect", "default", "circle", "poly"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	checkAttrValidity(lexer, node, attval, VALID_VALUES);
         }
     }
 
     /**
      * AttrCheck implementation for checking Scope.
      */
     public static class CheckScope implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"row", "rowgroup", "col", "colgroup"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	checkAttrValidity(lexer, node, attval, VALID_VALUES);
         }
     }
 
     /**
      * AttrCheck implementation for checking numbers.
      */
     public static class CheckNumber implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
 
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
 
             // don't check <frameset cols=... rows=...>
             if (node.is(TagId.FRAMESET) && (attval.is(AttrId.COLS) || attval.is(AttrId.ROWS))) {
                 return;
             }
 
             String p = attval.value;
 
             int j = 0;
 
             // font size may be preceded by + or -
             if (node.is(TagId.FONT) && p.length() > 0 && (p.charAt(0) == '+' || p.charAt(0) == '-')) {
                 ++j;
             }
 
             for (; j < p.length(); j++) {
                 if (!Character.isDigit(p.charAt(j))) {
                     lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
                     break;
                 }
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking colors.
      */
     public static class CheckColor implements AttrCheck
     {
 
         /**
          * valid html colors.
          */
         private static final Map<String, String> COLORS_BY_NAME = new HashMap<String, String>();
         private static final Map<String, String> COLORS_BY_VALUE = new HashMap<String, String>();
         
         private static void addColor(final String name, final String value) {
         	COLORS_BY_NAME.put(name, value);
         	COLORS_BY_VALUE.put(value, name);
         }
 
         static
         {
             addColor("black", "#000000");
             addColor("green", "#008000");
             addColor("silver", "#C0C0C0");
             addColor("lime", "#00FF00");
             addColor("gray", "#808080");
             addColor("olive", "#808000");
             addColor("white", "#FFFFFF");
             addColor("yellow", "#FFFF00");
             addColor("maroon", "#800000");
             addColor("navy", "#000080");
             addColor("red", "#FF0000");
             addColor("blue", "#0000FF");
             addColor("purple", "#800080");
             addColor("teal", "#008080");
             addColor("fuchsia", "#FF00FF");
             addColor("aqua", "#00FFFF");
         }
         
         private static String getColorCode(final String name) {
         	return COLORS_BY_NAME.get(name.toLowerCase());
         }
         
         private static String getColorName(final String code) {
         	return COLORS_BY_VALUE.get(code.toUpperCase());
         }
         
         /** Checks hexadecimal color value */
         private static boolean isValidColorCode(final String color) {
             if (color.length() != 6) {
                 return false;
             }
 
             // check if valid hex digits and letters
             for (int i = 0; i < 6; i++) {
                 if (!TidyUtils.isxdigit(color.charAt(i))) {
                     return false;
                 }
             }
             return true;
         }
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
             if (!attval.hasValue()) {
                 lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
                 return;
             }
 
             boolean valid = false;
             String given = attval.value;
             
             // 727851 - add hash to hash-less color values
             if (given.length() > 0 && given.charAt(0) != '#' && (valid = isValidColorCode(given))) {
             	String s = '#' + given;
             	lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE_REPLACED);
                 given = attval.value = s;
             }
             
             if (!valid && given.length() > 0 && given.charAt(0) == '#') {
             	valid = isValidColorCode(given.substring(1));
             }
             
             if (valid && given.charAt(0) == '#' && lexer.configuration.isReplaceColor()) {
             	String newName = getColorName(given);
             	if (newName != null) {
             		given = attval.value = newName;
             	}
             }
             
             // if it is not a valid color code, it is a color name
             if (!valid) {
             	valid = getColorCode(given) != null;
             }
 
             if (valid && given.charAt(0) == '#') {
             	attval.value = attval.value.toUpperCase();
             } else {
             	attval.value = attval.value.toLowerCase();
             }
             if (!valid) {
             	lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
             }
         }
     }
 
     /**
      * AttrCheck implementation for checking valuetype attribute for element param.
      */
     public static class CheckVType implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"data", "object", "ref"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	checkAttrValidity(lexer, node, attval, VALID_VALUES);
         }
     }
 
     /**
      * AttrCheck implementation for checking scroll.
      */
     public static class CheckScroll implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"no", "auto", "yes"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	checkAttrValidity(lexer, node, attval, VALID_VALUES);
         }
     }
 
     /**
      * AttrCheck implementation for checking dir.
      */
     public static class CheckTextDir implements AttrCheck {
 
         /**
          * valid values for this attribute.
          */
         private static final String[] VALID_VALUES = {"rtl", "ltr"};
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	checkAttrValidity(lexer, node, attval, VALID_VALUES);
         }
     }
 
     /**
      * AttrCheck implementation for checking lang and xml:lang.
      */
     public static class CheckLang implements AttrCheck {
 
         /**
          * @see AttrCheck#check(Lexer, Node, AttVal)
          */
         public void check(Lexer lexer, Node node, AttVal attval) {
         	// empty xml:lang is allowed through XML 1.0 SE errata
         	if (!attval.hasValue() && !attval.is(AttrId.XML_LANG)) {
         		if (lexer.configuration.getAccessibilityCheckLevel() == 0) {
         			lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
         		}
         		return;
         	}
         }
     }
 
     /** Checks type attribute */
     public static class CheckType implements AttrCheck {
     	
     	private static final String valuesINPUT[] = {"text", "password", "checkbox", "radio",
             "submit", "reset", "file", "hidden", "image", "button"};
         private static final String valuesBUTTON[] = {"button", "submit", "reset"};
         private static final String valuesUL[] = {"disc", "square", "circle"};
         private static final String valuesOL[] = {"1", "a", "i"};
     	
 		public void check(final Lexer lexer, final Node node, final AttVal attval) {
 	        if (node.is(TagId.INPUT)) {
 	            checkAttrValidity(lexer, node, attval, valuesINPUT);
 	        } else if (node.is(TagId.BUTTON)) {
 	            checkAttrValidity(lexer, node, attval, valuesBUTTON);
 	        } else if (node.is(TagId.UL)) {
 	            checkAttrValidity(lexer, node, attval, valuesUL);
 	        } else if (node.is(TagId.OL)) {
 	            if (!attval.hasValue()) {
 	            	lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
 	                return;
 	            }
 	            if (!attval.valueIsAmong(valuesOL)) {
 	            	lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
 	            }
 	        } else if (node.is(TagId.LI)) {
 	            if (!attval.hasValue()) {
 	            	lexer.report.attrError(lexer, node, attval, ErrorCode.MISSING_ATTR_VALUE);
 	                return;
 	            }
 	            if (attval.valueIsAmong(valuesUL)) {
 	            	attval.checkLowerCaseAttrValue(lexer, node);
 	            } else if (!attval.valueIsAmong(valuesOL)) {
 	            	lexer.report.attrError(lexer, node, attval, ErrorCode.BAD_ATTRIBUTE_VALUE);
 	            }
 	        }
 	        return;
 		}
     }
 }
