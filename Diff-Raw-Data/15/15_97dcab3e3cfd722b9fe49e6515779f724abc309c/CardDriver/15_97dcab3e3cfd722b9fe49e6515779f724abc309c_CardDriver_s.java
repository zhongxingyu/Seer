 /**
  * Programmer: kyle
  * Date: 1/24/13
  * Time: 8:00 AM
  * Exercise:
  */
 public class CardDriver {
 
     public static void main(String[] args) {
         Card card = new Card(1,4);

        System.out.println(card.getSuit());
         System.out.println(card);
 
        Card card2 = new Card(5, 3);
 
        System.out.println(card2.getSuit());
         System.out.println(card2);
     }
 }
