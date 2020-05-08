 import java.util.*;
 
 class Entry {
     private String name;        // name of symbol.
     private int lineNumber;     // line on which declared
     private TK varOrConst;      // variable or const?
     private boolean isIV ;      // is presently index variable?
     
     // Array
     public int arrSize;
     public int lbound;
     public int ubound;
 
     public Entry(String name, int lineNumber, TK varOrConst) {
         this.name = name;
         this.lineNumber = lineNumber;
         this.varOrConst = varOrConst;
         this.isIV = false;
         this.arrSize = 0;
         this.lbound = 0;
         this.ubound = 0;
     }
     String getName() {
         return name;
     }
     int getLineNumber() {
         return lineNumber;
     }
     boolean isVar() {
         return (varOrConst == TK.VAR);
     }
     boolean isConst() {
         return (varOrConst == TK.CONST);
     }
     boolean getIsIV() {
         return isIV;
     }
     void setIsIV(boolean is) {
         isIV = is;
     }
     public String whatAreYou() {
         return  isVar()?"variable"
                :isConst()?"constant"
                :"OOPS Entry whatAreYou()";
     }
     public String toString() {
         return name + "  declared on line " + lineNumber + " as "+ varOrConst;
     }
 }
