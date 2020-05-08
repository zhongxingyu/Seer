 package hello;
 
 import processing.core.*;
 
 public class Hello extends PApplet{
 
//    Stripe[] stripes = new Stripe[50];

     public void setup(){
    //    size(200,200);
     }
 
     public String greet() {
         return "Hello, world";
     }
 
 //   public static void main(String[] args) {
 //       Hello h = new Hello();
 //       System.out.println(h.greet());
 //   }
    public void draw(){
         System.out.println(greet());
     }
 }
