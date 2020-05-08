 package de.skuzzle.polly.parsing;
 
 public enum TokenType {
     SEPERATOR("Leerzeichen"),
     LITERAL("Literal"),                     /* only for debug output */
     LIST("Liste"),
     CHANNEL("Channel"),   /* Channel literals */
     USER("User"),               /* User literals */
     CHOOSE("choose"),    /* binomial coefficients */
     RADIX("0x"),
     ASSIGNMENT("->"),
     ADD("+"), 
     SUB("-"),
     WAVE("~"), 
     ADDWAVE("+~"), 
     MUL("*"), 
     DIV("/"), 
     INTDIV("\\"), 
     MOD("%"), 
     POWER("^"), 
     BOOLEAN_AND("&&"), 
     BOOLEAN_OR("||"), 
     XOR("   "), 
     GT(">"), 
     LT("<"), 
     LEFT_SHIFT("<<"),
     RIGHT_SHIFT(">>"),
     URIGHT_SHIFT(">>>"),
     EQ("="), 
     NEQ("!="), 
     EGT(">="), 
     ELT("<="),
     INT_AND("&"), 
     INT_OR("|"), 
     INT_XOR(""), 
     DOTDOT(".."), 
     DOT("."),
     COMMA(","), 
     DOLLAR("$"),
     EXCLAMATION("!"), 
     QUESTION("?"), 
     COLON(":"), 
     POUND("#"),
     NUMBER("Zahl"), 
     IDENTIFIER("Bezeichner"), 
     TRUE("true"), 
     FALSE("false"), 
     STRING("String"), 
     DATETIME("Zeitangabe"),
     TIMESPAN("Zeitspanne"),
     OPENBR("("), 
     CLOSEDBR(")"), 
     OPENSQBR("["), 
     CLOSEDSQBR("]"), 
     OPENCURLBR("{"), 
     CLOSEDCURLBR("}"), 
     INDEX("Indizierung"),
     KEYWORD("Schl�sselwort"),
     UNKNOWN("Unbekanntes Zeichen"),
     COMMAND("Befehl"),
     POLLY("Polly"),
     PUBLIC("public"),
    TEMP("temp"),
    EOS("Ende der Eingabe");
     
     private String string;
     
     private TokenType(String string) {
         this.string = string;
     }
     
     
     public boolean isChannelLiteral() {
         switch (this) {
             case CHANNEL:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     public boolean isUserLiteral() {
         switch (this) {
             case USER:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     public boolean isDisjunctionOperator() {
         switch(this) {
             case BOOLEAN_OR:
             case INT_OR:
             case XOR:
             case INT_XOR:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     public boolean isConjunctionOperator() {
         switch(this) {
             case BOOLEAN_AND:
             case INT_AND:
                 return true;
             default:
                 return false;
         }
     }
     
     
     public boolean isExpressionOperator() {
         switch (this) {
             case ADD:
             case ADDWAVE:
             case SUB:
             case WAVE:
                 return true;
             default: 
                 return false;
         }
     }
     
     
     
     public boolean isTermOperator() {
         switch (this) {
             case MUL:
             case DIV:
             case INTDIV:
             case MOD:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     public boolean isFactorOperator() {
         return this == POWER;
     }
     
     
     
     public boolean isStringOperator() {
         switch(this) {
             case QUESTION:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     public boolean isBooleanOperator() {
         switch (this) {
             case BOOLEAN_AND:
             case BOOLEAN_OR:
             case XOR:
             case EXCLAMATION:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     public boolean isNumericOperator() {
         switch (this) {
             case ADD:
             case SUB:
             case MUL:
             case DIV:
             case INTDIV:
             case POWER:
             case MOD:
             case INT_AND:
             case INT_OR:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     public boolean isRelationalOperator() {
         switch (this) {
             case GT:
             case LT: 
             case EGT:
             case ELT:
                 return true;
             default: return false;
         }
     }
     
     
     
     public boolean isEqualityOperator() {
         switch (this) {
             case EQ:
             case NEQ:
                 return true;
             default:
                 return false;
         }
     }
     
     
     
     @Override
     public String toString() {
         return this.string;
     }
 }
