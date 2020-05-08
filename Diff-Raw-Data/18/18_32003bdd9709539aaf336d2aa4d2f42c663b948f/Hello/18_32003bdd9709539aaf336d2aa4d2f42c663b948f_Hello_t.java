 package hello;
 
 import processing.core.*;
 
 public class Hello extends PApplet{
 
     public void setup(){
        size(200,200);
        smooth();
     }
 
     public String greet() {
         return "Hello, world";
     }
 
    public static void main(String args[]) {
      PApplet.main(new String[] { "--present", "hello.Hello" });
    }

   public void draw(){
        background(100);
         System.out.println(greet());
     }
 }
