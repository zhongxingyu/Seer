 package ru.spbau.bioinf.tagfinder.ui;
 
 import ru.spbau.bioinf.tagfinder.Peak;
 
 public class TooltipCandidate {
    private int x1;
    private int x2;
     private int line;
     private String text;
     private Peak peak;
 
    public TooltipCandidate(int x1, int x2, int line, String text, Peak peak) {
        this.x1 = x1;
        this.x2 = x2;
         this.line = line;
         this.text = text;
         this.peak = peak;
     }
 
    public boolean isValid(int x, int line) {
        return x1 <= x && x <=x2 && line == this.line;
     }
 
     public String getText() {
         return text;
     }
 
     public Peak getPeak() {
         return peak;
     }
 }
