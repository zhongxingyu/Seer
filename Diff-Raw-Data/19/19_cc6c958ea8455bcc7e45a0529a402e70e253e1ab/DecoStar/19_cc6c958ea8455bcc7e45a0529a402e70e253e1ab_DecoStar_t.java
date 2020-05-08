 package jp.itacademy.samples.oop.deco;
 
 public class DecoStar extends DecoratedString {
 
     public DecoStar(String str) {
         super(str);
     }
 
    public String decorate() {
         return "★" + str + "★";
     }
 }
