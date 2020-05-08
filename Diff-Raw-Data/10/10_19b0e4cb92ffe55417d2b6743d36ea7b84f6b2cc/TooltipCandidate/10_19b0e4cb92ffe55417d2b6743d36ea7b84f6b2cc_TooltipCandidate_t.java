 package ru.spbau.bioinf.tagfinder.ui;
 
 public class TooltipCandidate {
     private int x1;
     private int x2;
     private int y1;
     private int y2;
     private String text;
 
    public TooltipCandidate(double x1, double x2, double y1, double y2, String text) {
        this.x1 = (int)x1;
        this.x2 = (int)x2;
        this.y1 = (int)y1;
        this.y2 = (int)y2;
         this.text = text;
     }
 
     public boolean isValid(int x, int y) {
         return x1 <= x && x <=x2 && y1 <= y && y <= y2;
     }
 
     public String getText() {
         return text;
     }
 }
