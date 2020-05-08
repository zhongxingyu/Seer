 package org.w3c.tidy;
 
 public enum ErrorCode {
 	/**
      * invalid entity: missing semicolon.
      */
     MISSING_SEMICOLON(1),
 
     /**
      * invalid entity: missing semicolon.
      */
     MISSING_SEMICOLON_NCR(2),
 
     /**
      * invalid entity: unknown entity.
      */
     UNKNOWN_ENTITY(3),
 
     /**
      * invalid entity: unescaped ampersand.
      */
     UNESCAPED_AMPERSAND(4),
 
     /**
      * invalid entity: apos undefined in current definition.
      */
     APOS_UNDEFINED(5),
 
     /**
      * missing an end tag.
      */
     MISSING_ENDTAG_FOR(6),
 
     /**
      * missing end tag before.
      */
     MISSING_ENDTAG_BEFORE(7),
 
     /**
      * discarding unexpected element.
      */
     DISCARDING_UNEXPECTED(8),
 
     /**
      * nested emphasis.
      */
     NESTED_EMPHASIS(9),
 
     /**
      * non matching end tag.
      */
     NON_MATCHING_ENDTAG(10),
 
     /**
      * tag not allowed in.
      */
     TAG_NOT_ALLOWED_IN(11),
 
     /**
      * missing start tag.
      */
     MISSING_STARTTAG(12),
 
     /**
      * unexpected end tag.
      */
     UNEXPECTED_ENDTAG(13),
 
     /**
      * unsing br in place of.
      */
     USING_BR_INPLACE_OF(14),
 
     /**
      * inserting tag.
      */
     INSERTING_TAG(15),
 
     /**
      * suspected missing quote.
      */
     SUSPECTED_MISSING_QUOTE(16),
 
     /**
      * missing title element.
      */
     MISSING_TITLE_ELEMENT(17),
 
     /**
      * duplicate frameset.
      */
     DUPLICATE_FRAMESET(18),
 
     /**
     * elements can't be nested.
      */
     CANT_BE_NESTED(19),
 
     /**
      * obsolete element.
      */
     OBSOLETE_ELEMENT(20),
 
     /**
      * proprietary element.
      */
     PROPRIETARY_ELEMENT(21),
 
     /**
      * unknown element.
      */
     UNKNOWN_ELEMENT(22),
 
     /**
      * trim empty element.
      */
     TRIM_EMPTY_ELEMENT(23),
 
     /**
      * coerce to end tag.
      */
     COERCE_TO_ENDTAG(24),
 
     /**
      * illegal nesting.
      */
     ILLEGAL_NESTING(25),
 
     /**
      * noframes content.
      */
     NOFRAMES_CONTENT(26),
 
     /**
      * content after body.
      */
     CONTENT_AFTER_BODY(27),
 
     /**
      * inconsistent version.
      */
     INCONSISTENT_VERSION(28),
 
     /**
      * malformed comment.
      */
     MALFORMED_COMMENT(29),
 
     /**
     * bad comment chars.
      */
     BAD_COMMENT_CHARS(30),
 
     /**
      * bad xml comment.
      */
     BAD_XML_COMMENT(31),
 
     /**
      * bad cdata comment.
      */
     BAD_CDATA_CONTENT(32),
 
     /**
      * inconsistent namespace.
      */
     INCONSISTENT_NAMESPACE(33),
 
     /**
      * doctype after tags.
      */
     DOCTYPE_AFTER_TAGS(34),
 
     /**
      * malformed doctype.
      */
     MALFORMED_DOCTYPE(35),
 
     /**
      * unexpected end of file.
      */
     UNEXPECTED_END_OF_FILE(36),
 
     /**
      * doctype not upper case.
      */
     DTYPE_NOT_UPPER_CASE(37),
 
     /**
      * too many element.
      */
     TOO_MANY_ELEMENTS(38),
 
     /**
      * unescaped element.
      */
     UNESCAPED_ELEMENT(39),
 
     /**
      * nested quotation.
      */
     NESTED_QUOTATION(40),
 
     /**
      * element not empty.
      */
     ELEMENT_NOT_EMPTY(41),
 
     /**
      * encoding IO conflict.
      */
     ENCODING_IO_CONFLICT(42),
 
     /**
      * mixed content in block.
      */
     MIXED_CONTENT_IN_BLOCK(43),
 
     /**
      * missing doctype.
      */
     MISSING_DOCTYPE(44),
 
     /**
      * space preceding xml declaration.
      */
     SPACE_PRECEDING_XMLDECL(45),
 
     /**
      * too many elements in.
      */
     TOO_MANY_ELEMENTS_IN(46),
 
     /**
      * unexpected endag in.
      */
     UNEXPECTED_ENDTAG_IN(47),
 
     /**
      * replacing element.
      */
     REPLACING_ELEMENT(83),
 
     /**
     * replacing unexpected element.
      */
     REPLACING_UNEX_ELEMENT(84),
 
     /**
      * coerce to endtag.
      */
     COERCE_TO_ENDTAG_WARN(85),
 
     /**
      * attribute: unknown attribute.
      */
     UNKNOWN_ATTRIBUTE(48),
 
    INSERTING_ATTRIBUTE(49),
    
     /**
      * attribute: missing attribute value.
      */
     MISSING_ATTR_VALUE(50),
 
     /**
      * attribute: bad attribute value.
      */
     BAD_ATTRIBUTE_VALUE(51),
 
     /**
      * attribute: unexpected gt.
      */
     UNEXPECTED_GT(52),
 
     /**
      * attribute: proprietary attribute.
      */
     PROPRIETARY_ATTRIBUTE(53),
 
     /**
      * attribute: proprietary attribute value.
      */
     PROPRIETARY_ATTR_VALUE(54),
 
     /**
      * attribute: repeated attribute.
      */
     REPEATED_ATTRIBUTE(55),
 
     /**
      * attribute: missing image map.
      */
     MISSING_IMAGEMAP(56),
 
     /**
      * attribute: xml attribute value.
      */
     XML_ATTRIBUTE_VALUE(57),
 
     /**
      * attribute: missing quotemark.
      */
     MISSING_QUOTEMARK(58),
 
     /**
      * attribute: unexpected quotemark.
      */
     UNEXPECTED_QUOTEMARK(59),
 
     /**
      * attribute: id and name mismatch.
      */
     ID_NAME_MISMATCH(60),
 
     /**
      * attribute: backslash in URI.
      */
     BACKSLASH_IN_URI(61),
 
     /**
      * attribute: fixed backslash.
      */
     FIXED_BACKSLASH(62),
 
     /**
      * attribute: illegal URI reference.
      */
     ILLEGAL_URI_REFERENCE(63),
 
     /**
      * attribute: escaped illegal URI.
      */
     ESCAPED_ILLEGAL_URI(64),
 
     /**
      * attribute: newline in URI.
      */
     NEWLINE_IN_URI(65),
 
     /**
      * attribute: anchor not unique.
      */
     ANCHOR_NOT_UNIQUE(66),
 
     /**
      * attribute: entity in id.
      */
     ENTITY_IN_ID(67),
 
     /**
      * attribute: joining attribute.
      */
     JOINING_ATTRIBUTE(68),
 
     /**
     * attribute: expected equal sign.
      */
     UNEXPECTED_EQUALSIGN(69),
 
     /**
      * attribute: attribute value not lower case.
      */
     ATTR_VALUE_NOT_LCASE(70),
 
     /**
     * attribute: id syntax.
      */
     XML_ID_SYNTAX(71),
 
     /**
      * attribute: invalid attribute.
      */
     INVALID_ATTRIBUTE(72),
 
     /**
      * attribute: bad attribute value replaced.
      */
     BAD_ATTRIBUTE_VALUE_REPLACED(73),
 
     /**
      * attribute: invalid xml id.
      */
     INVALID_XML_ID(74),
 
     /**
      * attribute: unexpected end of file.
      */
     UNEXPECTED_END_OF_FILE_ATTR(75),
 
     /**
      * character encoding: vendor specific chars.
      */
     VENDOR_SPECIFIC_CHARS(76),
 
     /**
      * character encoding: invalid sgml chars.
      */
     INVALID_SGML_CHARS(77),
 
     /**
      * character encoding: invalid utf8.
      */
     INVALID_UTF8(78),
 
     /**
      * character encoding: invalid utf16.
      */
     INVALID_UTF16(79),
 
     /**
      * character encoding: encoding mismatch.
      */
     ENCODING_MISMATCH(80),
 
     /**
     * character encoding: invalid URI.
      */
     INVALID_URI(81),
 
     /**
      * character encoding: invalid NCR.
      */
     INVALID_NCR(82),
     
    /**
     * attribute: missing attribute.
     */
    MISSING_ATTRIBUTE(86),
    
     PREVIOUS_LOCATION(88),
 
     /**
      * Constant used for reporting of given doctype.
      */
     DOCTYPE_GIVEN_SUMMARY(110),
 
     /**
      * Constant used for reporting of version summary.
      */
     REPORT_VERSION_SUMMARY(111),
 
     /**
      * Constant used for reporting of bad access summary.
      */
     BADACCESS_SUMMARY(112),
 
     /**
      * Constant used for reporting of bad form summary.
      */
     BADFORM_SUMMARY(113);
     
     private final int code;
 
 	private ErrorCode(final int code) {
 		this.code = code;
 	}
 	
 	public int code() {
 		return code;
 	}
 }
