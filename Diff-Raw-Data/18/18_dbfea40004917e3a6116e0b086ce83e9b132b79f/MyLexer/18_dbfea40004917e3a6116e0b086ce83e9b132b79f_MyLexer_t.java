 // MyLexer.java
 // Subclass implementation of the Lexer for customization purposes
 // James Coleman, 2010
 
 package cps450;
 
 import java.io.IOException;
 import java.io.PushbackReader;
 
 import cps450.oodle.lexer.Lexer;
 import cps450.oodle.lexer.LexerException;
 import cps450.oodle.node.*;
 
 public class MyLexer extends Lexer {
 	
	public int errorCount = 0;
	
 	public MyLexer(PushbackReader in) {
 		super(in);
 	}
 	
 	// Override filter() defined in Lexer to prevent illegal characters from being
 	// reported to the parser; also handles token printing
 	protected void filter() throws LexerException, IOException  {
 		if (Options.instance().debugTokens) {
 			String tokenText = tokenTextFor(this.token);
 			printToken(this.token, tokenText);
 		}
 		
 		String errMsg = checkForLexicalErrors(this.token);
 	    if (errMsg != null) {
	    	this.errorCount++;
 	        printToken(this.token, errMsg);
 	        this.token = null; // prevent Lexer from returning this token
 	    }
 	    
 	    // Ignore tokens that the parser will not need to consume
 	    if (this.token instanceof TBlank || this.token instanceof TComment || this.token instanceof TContinuation) {
 	    	this.token = null;
 	    }
 	}
 	
 	// Reports a lexical error (if the token represents and error occurring in the scanner)
 	protected String checkForLexicalErrors(Token t) {
 		if (t instanceof TUnterminatedString) {  return "Unterminated string:" + t.getText(); }
 		else if (t instanceof TIllegalString) {  return "Illegal string:" + t.getText(); }
 		else if (t instanceof TIllegalChar) {  return "Unrecognized char: " + t.getText(); }
 		else { return null; }
 	}
 	
 	// Prints the specified token <t> in the correct format with the description <tokenText>
 	protected void printToken(Token t, String tokenText) {
 		if (tokenText != null) {
 			System.out.println(SourceHolder.instance().getFilenameFor(t) + ":" + SourceHolder.instance().getLineNumberFor(t) + "," + t.getPos() + ":" + tokenText);
 		}
 	}
 	
 	// Determine the description text for a non-lexical-error token
 	protected String tokenTextFor(Token t) {
 		if (t instanceof TAnd) { return "keyword:and"; }
 		else if (t instanceof TBoolean) {  return "keyword:boolean"; }
 		else if (t instanceof TBegin) {  return "keyword:begin"; }
 		else if (t instanceof TKlass) {  return "keyword:class"; }
 		else if (t instanceof TElse) {  return "keyword:else"; }
 		else if (t instanceof TEnd) {  return "keyword:end"; }
 		else if (t instanceof TFalse) {  return "keyword:false"; }
 		else if (t instanceof TFrom) {  return "keyword:from"; }
 		else if (t instanceof TIf) {  return "keyword:if"; }
 		else if (t instanceof TInherits) {  return "keyword:inherits"; }
 		else if (t instanceof TInt) {  return "keyword:int"; }
 		else if (t instanceof TIs) {  return "keyword:is"; }
 		else if (t instanceof TLoop) {  return "keyword:loop"; }
 		else if (t instanceof TMe) {  return "keyword:me"; }
 		else if (t instanceof TNew) {  return "keyword:new"; }
 		else if (t instanceof TNot) {  return "keyword:not"; }
 		else if (t instanceof TNull) {  return "keyword:null"; }
 		else if (t instanceof TString) {  return "keyword:string"; }
 		else if (t instanceof TThen) {  return "keyword:then"; }
 		else if (t instanceof TTrue) {  return "keyword:true"; }
 		else if (t instanceof TWhile) {  return "keyword:while"; }
 		else if (t instanceof TOr) {  return "keyword:or"; }
 		
 		else if (t instanceof TConcatOp) {  return "operator:'&'"; }
 		else if (t instanceof TPlusOp) {  return "operator:'+'"; }
 		else if (t instanceof TMinusOp) {  return "operator:'-'"; }
 		else if (t instanceof TMultOp) {  return "operator:'*'"; }
 		else if (t instanceof TDivOp) {  return "operator:'/'"; }
 		else if (t instanceof TGtOp) {  return "operator:'>'"; }
 		else if (t instanceof TGteOp) {  return "operator:'>='"; }
 		else if (t instanceof TEqOp) {  return "operator:'='"; }
 		
 		else if (t instanceof TEol) {  return "newline"; }
 		else if (t instanceof TAssignmentOp) {  return "assignment"; }
 		else if (t instanceof TLPar) {  return "'('"; }
 		else if (t instanceof TRPar) {  return "')'"; }
 		else if (t instanceof TLBracket) {  return "'['"; }
 		else if (t instanceof TRBracket) {  return "']'"; }
 		else if (t instanceof TComma) {  return "','"; }
 		else if (t instanceof TSemicolon) {  return "';'"; }
 		else if (t instanceof TColon) {  return "':'"; }
 		else if (t instanceof TPeriod) {  return "'.'"; }
 		
 		else if (t instanceof TId) {  return "identifier:" + t.getText(); }
 		
 		else if (t instanceof TStrlit) {  return "string lit:" + t.getText(); }
 		else if (t instanceof TIntlit) {  return "int lit:" + t.getText(); }
 		
 		else { return null; }
 	}
 
 }
