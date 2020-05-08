// Partners: Cara Lauritzen & Radford Smith
// This code uses inTokens as an input, and produces inTokens.out and
// observedNum.out. A diff can be used to check observedNum.out 
// against expectedNum.out to make sure LineNum and CharNum are correct.

 // This program is to be used to test the Little scanner.
 // This version is set up to test all tokens, but more code is
 // needed to test other aspects of the scanner (e.g., input that
 // causes errors, character numbers, values associated with tokens).
 
 import java.util.*;
 import java.io.*;
 import java_cup.runtime.*;  // defines Symbol
 
 class P2 {
     public static void main(String[] args) throws IOException // may be thrown by yylex
     {
 	// test all tokens
 	testAllTokens();
 	CharNum.num = 1;
	
 	// ADD CALLS TO OTHER TEST METHODS HERE
     }
 
     // **********************************************************************
     // testAllTokens
     //
     // open and read from file inTokens
     //
     // for each token read, write the corresponding string to inTokens.out
     //
     // if the input file contains all tokens, one per line, we can verify
     // correctness of the scanner by comparing the input and output files
     // **********************************************************************
     private static void testAllTokens() throws IOException {
 	// open input and output files
 	FileReader inFile = null;
 	PrintWriter outFile = null;
	PrintWriter outFile2 = null;

 	try {
 	    inFile = new FileReader("inTokens");
 	    outFile = new PrintWriter(new FileWriter("inTokens.out"));
			outFile2 = new PrintWriter(new FileWriter("observedNum.out"));
 	} catch (FileNotFoundException ex) {
 	    System.err.println("File inTokens not found.");
 	    System.exit(-1);
 	} catch (IOException ex) {
 	    System.err.println("inTokens.out cannot be opened.");
 	    System.exit(-1);
 	}
 
 	// create and call the scanner
 	Yylex scanner = new Yylex(inFile);
 	Symbol token = scanner.next_token();
 	while (token.sym != sym.EOF) {
		TokenVal val = (TokenVal)token.value;
		outFile2.println("Line " + val.linenum + " Char " + val.charnum);
 	    switch (token.sym) {
 	    case sym.INT:
 		outFile.println("int");
 		break;
 	    case sym.DBL:
 		outFile.println("double");
 		break;
 	    case sym.VOID:
 		outFile.println("void");
 		break;
 	    case sym.IF:
 		outFile.println("if");
 		break;
 	    case sym.ELSE:
 		outFile.println("else");
 		break;
 	    case sym.WHILE:
 		outFile.println("while");
 		break;
 	    case sym.RETURN:
 		outFile.println("return");
 		break;
 	    case sym.SCANF:
 		outFile.println("scanf");
 		break;
 	    case sym.PRINTF:
 		outFile.println("printf");
 		break;
 	    case sym.ID:
 		outFile.println(((IdTokenVal)token.value).idVal);
		outFile2.println("ID found " + ((IdTokenVal)token.value).idVal);
 		break;
 	    case sym.INTLITERAL:
 		outFile.println(((IntLitTokenVal)token.value).intVal);
 		break;
 	    case sym.DBLLITERAL:
 		outFile.println(((DblLitTokenVal)token.value).dblVal);
 		break;
 	    case sym.STRINGLITERAL:
 		outFile.println(((StrLitTokenVal)token.value).strVal);
 		break;
 	    case sym.LCURLY:
 		outFile.println("{");
 		break;
 	    case sym.RCURLY:
 		outFile.println("}");
 		break;
 	    case sym.LPAREN:
 		outFile.println("(");
 		break;
 	    case sym.RPAREN:
 		outFile.println(")");
 		break;
 	    case sym.COMMA:
 		outFile.println(",");
 		break;
 	    case sym.ASSIGN:
 		outFile.println("=");
 		break;
 	    case sym.SEMICOLON:
 		outFile.println(";");
 		break;
 	    case sym.PLUS:
 		outFile.println("+");
 		break;
 	    case sym.MINUS:
 		outFile.println("-");
 		break;
 	    case sym.STAR:
 		outFile.println("*");
 		break;
 	    case sym.DIVIDE:
 		outFile.println("/");
 		break;
 	    case sym.PLUSPLUS:
 		outFile.println("++");
 		break;
 	    case sym.MINUSMINUS:
 		outFile.println("--");
 		break;
 	    case sym.NOT:
 		outFile.println("!");
 		break;
 	    case sym.AND:
 		outFile.println("&&");
 		break;
 	    case sym.OR:
 		outFile.println("||");
 		break;
 	    case sym.EQUALS:
 		outFile.println("==");
 		break;
 	    case sym.NOTEQUALS:
 		outFile.println("!=");
 		break;
 	    case sym.LESS:
 		outFile.println("<");
 		break;
 	    case sym.GREATER:
 		outFile.println(">");
 		break;
 	    case sym.LESSEQ:
 		outFile.println("<=");
 		break;
 	    case sym.GREATEREQ:
 		outFile.println(">=");
 		break;
 	    case sym.AMPERSAND:
 		outFile.println("&");
 		break;
 	    case sym.INT_FORMAT:
 		outFile.println("\"%d\"");
 		break;
 	    case sym.DBL_FORMAT:
 		outFile.println("\"%f\"");
 		break;
 	    }
 
 	    token = scanner.next_token();
 	}
 	outFile.close();
  outFile2.close();
     }
 }
