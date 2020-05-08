 package net.sf.beezle.jasmin.scss;
 
 public class Stylesheet {
     private final Import[] imports;
 
    public Stylesheet(Import[] imports) {
         this.imports = imports;
     }
 
     public String toCss() {
         return "css style sheet"; // TODO
     }
 }
