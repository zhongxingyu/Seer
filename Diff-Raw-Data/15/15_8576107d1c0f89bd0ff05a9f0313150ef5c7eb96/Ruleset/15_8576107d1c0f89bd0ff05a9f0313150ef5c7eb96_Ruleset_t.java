 package net.sf.beezle.jasmin.scss;
 
 public class Ruleset extends Statement {
    private Selector[] selectors;

    public Ruleset(Selector[] selectors) {
        this.selectors = selectors;
     }
 }
