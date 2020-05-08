 // etravers@uab.edu
 // token class for microscala
 
 public class Token {
 
   private TokenClass symbol;	// current token
   private String lexeme;	// lexeme
 
   public Token () { }
 
   public Token (TokenClass symbol) {
     this (symbol, null);
   }
 
   public Token (TokenClass symbol, String lexeme) {
     this . symbol = symbol;
     this . lexeme  = lexeme;
   }
 
   public TokenClass symbol () { return symbol; }
 
   public String lexeme () { return lexeme; }
 
   public String toString () {
     switch (symbol) {
 		case OBJECT :   	return "(keyword, object) ";
 		case DEF :      	return "(keyword, def) ";
 		case MAIN :     	return "(keyword, main)";
 		case ARGS :			return "(keyword, args)";
 		case ARRAY : 		return "(keyword, array)";
 		case INT : 			return "(keyword, int)";
 		case LIST : 		return "(keyword, list)";
 		case STRING : 		return "(keyword, string)";
 		case VAR : 			return "(keyword, var)";
 		case IF :			return "(keyword, if)";
 		case ELSE : 		return "(keyword, else)";
 		case WHILE : 		return "(keyword, while)";
 		case PRINT : 		return "(keyword, println)";
 		case RETURN : 		return "(keyword, return)";
 		case HEAD : 		return "(keyword, head)";
 		case TAIL :			return "(keyword, tail)";
 		case ISEMPTY : 		return "(keyword, isEmpty)";
 		case DOT : 			return "(operator, .)";
 		case PLUS :			return "(operator, +) ";
 		case MINUS :		return "(operator, -) ";
 		case TIMES :    	return "(operator, *) ";
       	case SLASH :     	return "(operator, /) ";
		case LPAREN :    	return "(operator, () ";
      	case RPAREN :    	return "(operator, )) ";
		case LBRAC :    	return "(operator, [) ";
      	case RBRAC :    	return "(operator, ]) ";
		case LCURL :    	return "(operator, {) ";
      	case RCURL :    	return "(operator, }) ";
 		case NOT :     		return "(operator, !) ";
 		case AND :     		return "(operator, &&) ";
 		case OR :     		return "(operator, ||) ";
 		case EQ :     		return "(operator, =) ";
 		case EQU :     		return "(operator, ==) ";
 		case LT :     		return "(operator, <) ";
 		case GT :     		return "(operator, >) ";
 		case NE :     		return "(operator, !=) ";
 		case LE :     		return "(operator, <=) ";
 		case GE :     		return "(operator, >=) ";
 		case COMMA :    	return "(punctuation, ,) ";
 		case PERIOD :   	return "(punctuation, .) ";
 		case COLON :		return "(punctuation, :) ";
 		case SEMICOLON :	return "(punctuation, ;) ";
 		case LITERAL :  	return "(integer, " + lexeme + ") ";
 		case IDENTIFIER :   return "(identifier, " + lexeme + ") ";
 		case NIL :  		return "(nil) ";
 default : 
 	ErrorMessage . print (0, "Unrecognized token");
         return null;
     }
   }
 
 }
