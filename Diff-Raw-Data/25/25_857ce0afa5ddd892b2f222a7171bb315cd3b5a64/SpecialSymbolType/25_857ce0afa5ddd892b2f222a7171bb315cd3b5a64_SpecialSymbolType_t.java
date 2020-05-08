 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package slotgame;
 
 /**
  *
  * @author vitalispiridonov
  */
 public class SpecialSymbolType {
     
     public static final int SYMBOL_TYPE_SPECIAL_NONE = -1;
     
    public static final int SYMBOL_TYPE_SPECIAL_WILD = 10;
    public static final int SYMBOL_TYPE_SPECIAL_BONUS = 11;
    public static final int SYMBOL_TYPE_SPECIAL_SCATTER = 12;
     
     public static final Symbol SYMBOL_NONE = new Symbol(0, SYMBOL_TYPE_SPECIAL_NONE);
 }
