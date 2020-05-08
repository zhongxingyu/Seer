 package co.edu.eafit.dis.st0270.regexpr;
 
 public class Concatenation extends RegExpr {
 
     private RegExpr lregexpr;
     private RegExpr rregexpr;
 
     public Concatenation(RegExpr lregexpr, RegExpr rregexpr) {

 	this.lregexpr = lregexpr;
 	this.rregexpr = rregexpr;
     }
 }
