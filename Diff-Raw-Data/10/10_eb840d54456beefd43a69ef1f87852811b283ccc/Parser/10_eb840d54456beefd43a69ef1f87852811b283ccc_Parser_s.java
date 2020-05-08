 package vb.week2.tabular;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 public class Parser {
 	protected Token currentToken;
 	protected Scanner scanner;
 
 	protected void accept(Token.Kind expected) throws SyntaxError {
 		if (currentToken.getKind() == expected) {
 			currentToken = scanner.scan();
 		} else {
 			throw new SyntaxError("Error: expected " + expected
 					+ " but received " + currentToken.getKind());
 		}
 	}
 
 	protected void acceptIt() throws SyntaxError {
 		currentToken = scanner.scan();
 	}
 	
 	public void parse() {
 		try {
 			parseLatexTabular();
 		} catch (SyntaxError e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	protected void parseLatexTabular() throws SyntaxError {
 		parseBeginTabular();
 		parseColsSpec();
 		parseRows();
 		parseEndTabular();
 	}
 
 	protected void parseColsSpec() throws SyntaxError {
 		accept(Token.Kind.LCURLY);
 		parseIdentifier();
 		accept(Token.Kind.RCURLY);
 	}
 
 	protected void parseRows() throws SyntaxError {
 		while (currentToken.getKind() == Token.Kind.IDENTIFIER
 		    || currentToken.getKind() == Token.Kind.NUM
		    || currentToken.getKind() == Token.Kind.AMPERSAND) {
 			parseRow();
 		}
 	}
 
 	protected void parseRow() throws SyntaxError {
 		parseEntries();
 		accept(Token.Kind.DOUBLE_BSLASH);
 	}
 
 	protected void parseEntries() throws SyntaxError {
 		parseEntry();
 		while (currentToken.getKind().equals(Token.Kind.AMPERSAND)) {
 			acceptIt();
 			parseEntry();
 		}
 	}
 
 	protected void parseEntry() throws SyntaxError {
 		switch (currentToken.getKind()) {
 		case NUM:
 			parseNum();
 			break;
 		case IDENTIFIER:
 			parseIdentifier();
 			break;
		case AMPERSAND:
			break;
 		}
 	}
 
 	protected void parseBeginTabular() throws SyntaxError {
 		// BSLASH BEGIN LCURLY TABULAR RCURLY
 		accept(Token.Kind.BSLASH);
 		accept(Token.Kind.BEGIN);
 		accept(Token.Kind.LCURLY);
 		accept(Token.Kind.TABULAR);
 		accept(Token.Kind.RCURLY);
 	}
 
 	protected void parseEndTabular() throws SyntaxError {
 		// BSLASH END LCURLY TABULAR RCURLY
 		accept(Token.Kind.BSLASH);
 		accept(Token.Kind.END);
 		accept(Token.Kind.LCURLY);
 		accept(Token.Kind.TABULAR);
 		accept(Token.Kind.RCURLY);
 	}
 
 	protected void parseNum() throws SyntaxError {
 		accept(Token.Kind.NUM);
 	}
 
 	protected void parseIdentifier() throws SyntaxError {
 		accept(Token.Kind.IDENTIFIER);
 	}
 
 	public Parser(Scanner scanner) {
 		try {
 			this.scanner = scanner;
 			currentToken = scanner.scan();
 		} catch (SyntaxError e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	public static void main(String[] args) {
 		for (int i = 0; i < args.length; i++) {
 			String fname = args[i];
 			Scanner scanner;
 			try {
 				scanner = new Scanner(new FileInputStream(fname));
 				Parser parser = new Parser(scanner);
 				System.out.println(fname);
 				parser.parse();
 			} catch (FileNotFoundException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
}
