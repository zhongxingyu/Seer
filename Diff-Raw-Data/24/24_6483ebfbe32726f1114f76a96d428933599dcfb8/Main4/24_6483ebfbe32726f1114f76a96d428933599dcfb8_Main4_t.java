 package lesson1;
 
 import lesson1.ams.*;
 
 /**
  * Author: Ilya Varlamov aka privr@tnik
  * Date: 04.11.11
  * Time: 22:33
  */
 
 public class Main4 {
     public static void main( String[] args ){
 
         Animal[] animal = new Animal[6];
         animal[0] = new Fish();
         animal[1] = new Bird();
         animal[2] = new Dog();
         animal[3] = new Eagle();
         animal[4] = new Shark();
         animal[5] = new Whale();
 
        for( Animal anl : animal ){
            System.out.println( anl.output() );
         }
 
     }
 }
