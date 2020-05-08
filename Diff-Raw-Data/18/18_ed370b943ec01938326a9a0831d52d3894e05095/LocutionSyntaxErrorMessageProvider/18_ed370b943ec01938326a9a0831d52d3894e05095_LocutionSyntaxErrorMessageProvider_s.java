 package com.isb.bks.ivr.voice.dsl.validation;
 
 import org.antlr.runtime.MismatchedTokenException;
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.xtext.AbstractElement;
 import org.eclipse.xtext.CompoundElement;
 import org.eclipse.xtext.Keyword;
 import org.eclipse.xtext.diagnostics.Diagnostic;
 import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;
 import org.eclipse.xtext.parser.antlr.SyntaxErrorMessageProvider;
 
 public class LocutionSyntaxErrorMessageProvider extends SyntaxErrorMessageProvider {
 
 	private static final String RULE_PREFIX = "RULE_";
 
 	@Override
 	public SyntaxErrorMessage getSyntaxErrorMessage(IParserErrorContext context) {
 		if (context instanceof IUnorderedGroupErrorContext) {
 			IUnorderedGroupErrorContext iugec = (IUnorderedGroupErrorContext) context;
 			Keyword keyword = getFirstKeyword(iugec.getMissingMandatoryElements().get(0));
 			if (keyword != null) {
 				return new SyntaxErrorMessage(createMessageFromKeyword(keyword, context.getRecognitionException()),
 						Diagnostic.SYNTAX_DIAGNOSTIC);
 			}
 		}
 
 		if (context.getRecognitionException() instanceof MismatchedTokenException) {
 			MismatchedTokenException exception = (MismatchedTokenException) context.getRecognitionException();
 			if (exception.expecting >= 0 && exception.getUnexpectedType() >= 0) {
 				String expectingTokenTypeName = context.getTokenNames()[exception.expecting];
 				String unexpectedTokenTypeName = context.getTokenNames()[exception.getUnexpectedType()];
 				if ("RULE_ID".equals(expectingTokenTypeName)) {
 					if (!unexpectedTokenTypeName.startsWith(RULE_PREFIX)
 							&& Character.isJavaIdentifierStart(unexpectedTokenTypeName.replace("'", "").charAt(0))) {
 						return new SyntaxErrorMessage(
 								"keywords are to be prefixed with the ^ character when used as an identifier",
 								Diagnostic.SYNTAX_DIAGNOSTIC);
 					} else if (unexpectedTokenTypeName.startsWith(RULE_PREFIX)) {
 						return new SyntaxErrorMessage(String.format(
 								"mismatched input: %s '%s' found instead of the expected identifier",
 								unexpectedTokenTypeName.substring(RULE_PREFIX.length()).toLowerCase(),
 								exception.token.getText()), Diagnostic.SYNTAX_DIAGNOSTIC);
 					}
 				}
 			}
 		}
 
 		return super.getSyntaxErrorMessage(context);
 	}
 
 	private String createMessageFromKeyword(Keyword keyword, RecognitionException re) {
 		return "missing '" + keyword.getValue() + "' at " + re.token.getText();
 	}
 
 	private Keyword getFirstKeyword(AbstractElement abstractElement) {
 		if (abstractElement instanceof Keyword) {
 			return (Keyword) abstractElement;
 		} else if (abstractElement instanceof CompoundElement) {
			return getFirstKeyword(((CompoundElement) abstractElement).getElements().get(0));
 		}
 		return null;
 	}
 }
