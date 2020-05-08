 package net.sf.beezle.jasmin.scss;
 
 import com.sun.org.apache.xpath.internal.operations.Plus;
 
 public class Combinator {
    public final Combinator PLUS = new Combinator('+');
    public final Combinator GT = new Combinator('>');
 
     private final char code;
 
     private Combinator(char code) {
         this.code = code;
     }
 }
