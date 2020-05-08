 package net.sf.beezle.jasmin.scss;
 
 public class UnaryOperator {
    public final UnaryOperator PLUS = new UnaryOperator('+');
    public final UnaryOperator MINUS = new UnaryOperator('-');
 
     private final char code;
 
     private UnaryOperator(char code) {
         this.code = code;
     }
 }
