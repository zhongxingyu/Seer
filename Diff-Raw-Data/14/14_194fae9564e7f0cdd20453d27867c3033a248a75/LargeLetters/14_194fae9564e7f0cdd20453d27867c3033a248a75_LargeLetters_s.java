 /*
  * 3.      Writing Large Letters (30%)
  * Chapter 4. Problem 4.16 Page 168
  * A large letter H can be produced like this:
 
 *     *  ******  *       ******
 *     *  *       *       *    *
  *****   ****    *       *    *
 *     *  *       *       *    *
 *     *  ******  ******  ******
 
  * Use the class
 
  public class LetterH
       {
            public String stoString()
                   {
                         return “*     *\n*     *\n******\n*     *\n*     *\n”;
                   }
       }
  * Declare similar classes for the letters E, L, and O. Then write the message.
  */
 
 package homework4;
 
 import homework4.letters.*;
 /**
 *
 * @author JDavis
 * @github jason-s-davis
 *
 */
 public class LargeLetters {
     private String space = "\u0020\u0020";
     private String nl = "\n";
     private String msg = "";
 
 
     public static void main(String[] args){
 
         LargeLetters message = new LargeLetters();
         System.out.println(message);
 
     }
 
     public LargeLetters(){
 
         this.makemsg();
 
     }
 
     private void makemsg(){
 
         for(int i = 0; i<5; i++){
         this.msg += LetterH.toString(i) + space + LetterE.toString(i) + space
                 + LetterL.toString(i) + space + LetterL.toString(i) + space + LetterO.toString(i);
         this.msg += nl;
         }
     }
 
 
     @Override
     public String toString(){
         return this.msg;
     }
}
