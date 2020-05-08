 package jp.itacademy.samples.oop.deco;
 
 public abstract class DecoratedString {
 
     protected String str;
 
     protected DecoratedString(String str) {
         this.str = str;
     }
 
     public void show() {
        System.out.println(decorate(str));
     }
 
    public abstract String decorate(String str);
 }
