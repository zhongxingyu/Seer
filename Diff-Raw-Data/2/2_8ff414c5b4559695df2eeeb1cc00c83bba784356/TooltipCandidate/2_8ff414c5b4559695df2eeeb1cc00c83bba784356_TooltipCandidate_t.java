 package ru.spbau.bioinf.tagfinder.ui;
 
 import ru.spbau.bioinf.tagfinder.Peak;
 
 public class TooltipCandidate {
     private double value;
     private int line;
     private String text;
     private Peak peak;
 
     public TooltipCandidate(double value, int line, String text, Peak peak) {
         this.value = value;
         this.line = line;
         this.text = text;
         this.peak = peak;
     }
 
     public boolean isValid(double x, int line, double scale) {
        return Math.abs(x/scale - value) <= 5 / scale && line == this.line;
     }
 
     public String getText() {
         return text;
     }
 
     public Peak getPeak() {
         return peak;
     }
 }
