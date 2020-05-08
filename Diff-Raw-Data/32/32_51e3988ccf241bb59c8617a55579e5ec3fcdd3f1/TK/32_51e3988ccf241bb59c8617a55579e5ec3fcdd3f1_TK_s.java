 //package program.e2c;
 
 /* *** This file is given as part of the programming assignment. *** */
 
 //Token Kind (internal representations of tokens)
 
 public class TK {
  private final String name;
 
  // declaring constructor as private prevents outsiders
  // from creating new tokens;
  // and so can test equality using ==.
  private TK(String name) {
      this.name = name;
  }
  public String toString() { // make it printable for debugging
      return name;
  }
 
  // each token has a TK object.
 
  public static final TK VAR     = new TK("TK.VAR");     // var
  public static final TK CONST   = new TK("TK.CONST");   // const
 
  public static final TK PRINT   = new TK("TK.PRINT");   // print
  public static final TK IF      = new TK("TK.IF");      // if
  public static final TK THEN    = new TK("TK.THEN");    // then
  public static final TK ELSIF   = new TK("TK.ELSIF");   // elsif
  public static final TK ELSE    = new TK("TK.ELSE");    // else
  public static final TK END     = new TK("TK.END");     // end
  public static final TK WHILE   = new TK("TK.WHILE");   // while
  public static final TK DO      = new TK("TK.DO");      // do
  public static final TK FOR     = new TK("TK.FOR");     // for
  public static final TK TO      = new TK("TK.TO");      // to
  public static final TK DOWNTO  = new TK("TK.DOWNTO");  // downto
 
 
  public static final TK COMMA  = new TK("TK.COMMA");    // ,
 
  public static final TK ASSIGN = new TK("TK.ASSIGN");   // :=
  public static final TK LPAREN = new TK("TK.LPAREN");   // (
  public static final TK RPAREN = new TK("TK.RPAREN");   // )
  public static final TK PLUS   = new TK("TK.PLUS");     // +
  public static final TK MINUS  = new TK("TK.MINUS");    // -
  public static final TK TIMES  = new TK("TK.TIMES");    // *
  public static final TK DIVIDE = new TK("TK.DIVIDE");   // /
 
  public static final TK EQ     = new TK("TK.EQ");       // =
  public static final TK NE     = new TK("TK.NE");       // /=
  public static final TK LT     = new TK("TK.LT");       // <
  public static final TK GT     = new TK("TK.GT");       // >
  public static final TK LE     = new TK("TK.LE");       // <=
  public static final TK GE     = new TK("TK.GE");       // >=
  
  ///////////////////////////////////////////////////
  //public static final TK QM 	   = new TK("TK.QM");			// ""
  public static final TK QUESTION = new TK("TK.QUESTION"); 	// ?
  public static final TK PERCENT= new TK("TK.PERCENT");  	// %
  //public static final TK BACKSLASH = new TK("TK.BACKSLASH"); // /
 /////////////////////////////////////////////////////
 
  public static final TK ID     = new TK("TK.ID");       // identifier
 
  public static final TK NUM    = new TK("TK.NUM");      // number
  ////////////////////////////////////////////////////////
  public static final TK STR  = new TK("TK.STR");  // string
  /////////////////////////////////////////////////////
  public static final TK EOF    = new TK("TK.EOF");      // end of file
 
  // TK.ERROR special error token kind (for scanner to return to parser)
  public static final TK ERROR  = new TK("TK.ERROR");
  // TK.none marks end of each first set in parsing.
  // you might not need this.
  public static final TK none   = new TK("TK.none");
 }
