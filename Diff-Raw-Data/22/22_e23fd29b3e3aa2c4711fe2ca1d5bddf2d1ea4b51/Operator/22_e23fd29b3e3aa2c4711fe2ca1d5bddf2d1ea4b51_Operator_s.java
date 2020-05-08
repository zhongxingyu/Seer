 package net.sf.beezle.jasmin.scss;
 
 public class Operator {
    public final Operator SLASH = new Operator('/');
    public final Operator COMMA = new Operator(',');
 
     private final char code;
 
     private Operator(char code) {
         this.code = code;
     }
 }
