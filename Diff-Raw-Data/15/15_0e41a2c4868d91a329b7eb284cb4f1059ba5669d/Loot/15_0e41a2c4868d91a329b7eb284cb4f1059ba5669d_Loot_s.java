 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Version1;
 
 /**
  *
  * @author Ramen
  */
 public class Loot extends Object
 {
     public Loot(float x, float y, String t)
     {
        super("src/resource/" + t.toLowerCase() + ".png", x, y, 32, 32, 0, 0, 32, 32);
         type = t;
     }
 }
