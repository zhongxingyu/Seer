 package de.htwg.wzzrd.model;
 
 import java.util.ArrayList;
import java.util.List;
 
 /**
  * Contains static values for general game settings.
  * 
  * @author Michael
  */
 public final class Settings {
     protected Settings() {}
 
     private static final int MAX_PLAYERCOUNT = 6;
     private static final int NATIONCOUNT = CardNames.getNationCount();
     private static final int CARDCOUNT = CardNames.getCardsPerNation();
     private static final int PORT_NUMBER = 25566;
    private static final List<Integer> SPECIAL_CARDS = new ArrayList<Integer>();
    private static final int FOOL = 0;
    private static final int WIZARD = 14;
 
     public static int getMaxPlayercount() {
         return MAX_PLAYERCOUNT;
     }
 
     public static int getNationcount() {
         return NATIONCOUNT;
     }
 
     public static int getCardcount() {
         return CARDCOUNT;
     }
 
     public static int getPortNumber() {
         return PORT_NUMBER;
     }
 
    public static List<Integer> getSpecialCards() {
         if (SPECIAL_CARDS.size() == 0) {
            SPECIAL_CARDS.add(FOOL);
            SPECIAL_CARDS.add(WIZARD);
         }
         return SPECIAL_CARDS;
     }
 }
