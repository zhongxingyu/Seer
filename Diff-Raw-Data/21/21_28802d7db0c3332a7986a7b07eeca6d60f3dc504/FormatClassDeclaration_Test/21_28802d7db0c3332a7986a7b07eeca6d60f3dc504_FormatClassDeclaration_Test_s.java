 package descent.tests.format;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import descent.core.formatter.DefaultCodeFormatterConstants;
 
 public class FormatClassDeclaration_Test extends AbstractFormatBrace_Test {
 	
 	@Override
 	protected Map getDefaultOptions() {
 		Map options = new HashMap();
 		options.put(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION, DefaultCodeFormatterConstants.END_OF_LINE);
 		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CLASS_TEMPLATE_PARAMS, DefaultCodeFormatterConstants.FALSE);
 		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CLASS_TEMPLATE_PARAMS, DefaultCodeFormatterConstants.FALSE);
 		return options;
 	}
 
 	@Override
 	protected String getBracePositionOptionName() {
 		return DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_TYPE_DECLARATION;
 	}
 	
 	@Override
 	protected String getIndentCompareToParentOptionName() {
 		return DefaultCodeFormatterConstants.FORMATTER_INDENT_BODY_DECLARATIONS_COMPARE_TO_TYPE_HEADER;
 	}
 
 	@Override
 	protected String getFormattedPrefixForBrace() {
 		return "class Class";
 	}
 
 	@Override
 	protected String getUnformattedPrefixForBrace() {
 		return "class    Class";
 	}
 	
 	public void testDontINSERT_SPACE_BEFORE_OPENING_PAREN_IN_CLASS_TEMPLATE_PARAMS() throws Exception {
 		assertFormat(
 				"class Foo(T) {\r\n" +
 				"}",
 				
 				"class Foo   (   T    ) { }"
 				);
 	}
 	
 	public void testINSERT_SPACE_BEFORE_OPENING_PAREN_IN_CLASS_TEMPLATE_PARAMS() throws Exception {
 		Map options = new HashMap();
 		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_OPENING_PAREN_IN_CLASS_TEMPLATE_PARAMS, DefaultCodeFormatterConstants.TRUE);
 		assertFormat(
 				"class Foo (T) {\r\n" +
 				"}",
 				
 				"class Foo   (   T    ) { }",
 				
 				options
 				);
 	}
 	
 	public void testINSERT_SPACE_AFTER_OPENING_PAREN_IN_CLASS_TEMPLATE_PARAMS() throws Exception {
 		Map options = new HashMap();
 		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_OPENING_PAREN_IN_CLASS_TEMPLATE_PARAMS, DefaultCodeFormatterConstants.TRUE);
 		assertFormat(
 				"class Foo( T) {\r\n" +
 				"}",
 				
 				"class Foo   (   T    ) { }",
 				
 				options
 				);
 	}
 
 
 }
