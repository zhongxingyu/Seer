 package com.selesse.jxlint.report.color;
 
 @SuppressWarnings("unused")
 public enum Color {
     RESET("\u001B[0m"),
     BLACK("\u001B[30m"),
     RED("\u001B[31m"),
     GREEN("\u001B[32m"),
     YELLOW("\u001B[33m"),
     BLUE("\u001B[34m"),
     PURPLE("\u001B[35m"),
     CYAN("\u001B[36m"),
     WHITE("\u001B[37m");
 
     private String color;
 
     private Color(String s) {
         this.color = s;
     }
 
    public String toAnsi() {
         return this.color;
     }
 
     public static String wrapColor(String s, Color color) {
        return color.toAnsi() + s + Color.RESET.toAnsi();
     }
 }
