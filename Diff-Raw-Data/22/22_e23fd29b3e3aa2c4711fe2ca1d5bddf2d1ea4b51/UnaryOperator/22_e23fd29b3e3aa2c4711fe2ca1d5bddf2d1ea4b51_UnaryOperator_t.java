 package net.sf.beezle.jasmin.scss;
 
 public class UnaryOperator {
    public static final UnaryOperator PLUS = new UnaryOperator('+');
    public static final UnaryOperator MINUS = new UnaryOperator('-');
 
     private final char code;
 
     private UnaryOperator(char code) {
         this.code = code;
     }
 }
