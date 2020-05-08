 package com.jclark.microxml.tree;
 
 /**
  * @author James Clark
  */
 enum ParseError {
     ISOLATED_SURROGATE("isolated surrogate"),
     INVALID_CODE_POINT("invalid code point"),
     UNESCAPED_GT("unescaped \"<\""),
     UNESCAPED_LT("unescaped \">\""),
     UNESCAPED_AMP("unescaped \"&\""),
     REF_CODE_POINT_TOO_BIG("character number must not exceed #x10FFFF"),
    MISSING_QUOTE("missing quote"),
     UNKNOWN_CHAR_NAME("reference to unknown character name"),
     UNTERMINATED_COMMENT("unterminated comment"),
     DOUBLE_MINUS_IN_COMMENT("comment must not contain \"--\""),
     TEXT_BEFORE_ROOT("text before root element"),
     CONTENT_AFTER_ROOT("text or elements after root element"),
     MISSING_END_TAG("start-tag does not have an end-tag"),
     EMPTY_DOCUMENT("empty document"),
     FORBIDDEN_CODE_POINT_REF("reference to forbidden code point"),
     MISMATCHED_END_TAG("name in end-tag does not match the name of any open element"),
     DUPLICATE_ATTRIBUTE("duplicate attribute name"),
     XMLNS_ATTRIBUTE("an attribute must not have the name \"xmlns\""),
    SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME("an attribute name must be preceded by whitespace");
 
     private final String format;
 
     ParseError(String format) {
         this.format = format;
     }
 
     String format(Object... args) {
         return String.format(format, args);
     }
 }
