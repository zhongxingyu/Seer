 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
*/
 package practiceUno;
 
import java.util.Scanner;
 /**
  *
  * @author mike
  */
 public class WildCard extends Card implements WildActions
 {
     enum cardWild {WILD, WILDDRFOUR}; 
     cardWild wild; 
     
    private static  Scanner scan = new Scanner(System.in);
     
     public WildCard(cardWild w)
     {
         wild = w; 
     }
     
     public void Print()
     {
         System.out.print(wild);
     }
     
     public Card.cardColor GetColor()
     {
         return color; 
     }
    
    public void Wild()
    {
        System.out.println("Please choose a color!");
        System.out.println("Red, Green, Yellow, Blue");
        
        String input = scan.nextLine(); 
        Card.cardColor color = Card.cardColor.valueOf(input);
        
        System.out.println(color);
    } 
 }
