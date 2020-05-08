 package net.sf.beezle.jasmin.scss;
 
 public class Stylesheet {
     private final Import[] imports;
    private final Statement[] statements;
 
    public Stylesheet(Import[] imports, Statement[] statements) {
         this.imports = imports;
        this.statements = statements;
     }
 
     public String toCss() {
         return "css style sheet"; // TODO
     }
 }
