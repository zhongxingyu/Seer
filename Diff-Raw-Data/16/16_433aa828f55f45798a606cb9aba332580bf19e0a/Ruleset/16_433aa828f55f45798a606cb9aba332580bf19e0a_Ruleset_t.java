 package net.sf.beezle.jasmin.scss;
 
 public class Ruleset extends Statement {
     private Selector[] selectors;
    private Declaration[] declarations;
 
    public Ruleset(Selector[] selectors, Declaration[] declarations) {
         this.selectors = selectors;
        this.declarations = declarations;
     }
 }
