 class Operator {
   public enum Kind {Unary, Binary, TernaryPair};
   public Kind kind;
   public int tokenType;
   public String tokenText;
   public enum Associativity {Left, Right};
   public Associativity assoc;
   public Operator ternary = null;
   public boolean ternaryAfter;
   public String ternaryText;
   public boolean isBinary() {
     return kind == Kind.Binary;
   }
   public boolean isUnary() {
     return kind == Kind.Unary;
   }
   public boolean isTernary() {
     return kind == Kind.TernaryPair;
   }
   public boolean isRightAssoc() {
     return assoc == Associativity.Right;
   }
   public boolean isLeftAssoc() {
     return assoc == Associativity.Left;
   }
   public Operator(int tokenType, String tokenText, Associativity assoc) {
     this.tokenType = tokenType; this.assoc=assoc; this.tokenText = tokenText;
   }
   public String toString() {
     String result = kind == Kind.Binary ? "B" : kind == Kind.Unary ? "U" : "T";
     result += assoc == Associativity.Right ? "R " : "L ";
     result += '"' + tokenText + '"';
     if (ternary != null)
       result += " (" + ternary.toString() + ")";
     return result; 
   }
   public String sndop() {
     return ternary.tokenText;
   }
 }
