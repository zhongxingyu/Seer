 package Roma.PlayerInterfaceFiles;
 
 import Roma.Cards.*;
 import Roma.*;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 14/05/12
  * Desc:
  */
 
 public abstract class PlayerInterface {
     public static final String BREAK_LINE = "-------------------------------------";
 
     public final static int CANCEL = -1;
 
     abstract public int readInput(String title, boolean cancelOn, String... choices);
 
     abstract public int readIndex(String title, boolean cancelOn, String... choices);
 
     abstract public int getIndex(int bound);
 
     abstract public String getPlayerName(int num);
 
     abstract public String readString();
 
     abstract public void printDiceList(ArrayList<Dice> diceList);
 
     abstract public void printCardList(ArrayList<CardHolder> cardList);
 
     public static void printOut(Object object, boolean newLine){
         if (newLine){
             System.out.println(object.toString());
         } else {
             System.out.print(object.toString());
         }
     }
 
     public static String padCentre(String input, int outputLength){
        int side = outputLength/2 + input.length()/2;
        return padRight(padLeft(input, side), outputLength);
     }
 
     public static String padRight(String input, int outputLength) {
         return String.format("%1$-" + outputLength + "s", input);
     }
 
     public static String padLeft(String input, int outputLength) {
         return String.format("%1$#" + outputLength + "s", input);
     }
 
     public abstract void printFilteredDiceList(ArrayList<CardHolder> currPlayer, ArrayList<CardHolder> opposingPlayer,
                                                boolean filterCurr, boolean filterOther);
 
     public abstract void printFilteredCardList(ArrayList<CardHolder> cardList, boolean filter);
 
     //Keeps reading till valid input is recieved
     public abstract int getIntegerInput(int... range);
 }
