 package battlesys;
 
 import battlesys.exception.BadRandomEventFileException;
 import battlesys.io.BattleSysLogger;
 import com.csvreader.CsvReader;
 import java.util.*;
 import java.util.logging.Level;
import java.io.*;
 
 /**
  * random event related things, package private class
  * @author Peter
  */
 class RandomEvent {
 
     //Type of ability affected
     private int affectType;
     //Rates for Relative change and Value for Absolute change.
     //For relative change, value greater than 0 mean increase, and vice versa
     private int affectValueMean, affectValueVar;
     private double affectRateMean, affectRateVar;
     //Number of rounds of the RE affect
     private int affectRoundMean, affectRoundVar;
     //Move time effect
     private int moveTimeAffectMean, moveTimeAffectVar;
     //Status applied due to this random event
     private int statusAffect;
     //Does it affect both sides?
     private boolean global;
     //String shown when this happens.
     private String message;
     //loaded random events
     private static List<RandomEvent> loadedEvents = null;
 
     /**
      * Holder for a random event "struct"
      */
     private RandomEvent() {
         affectType = Player.statusId.NO_EFFECT.getId();
         affectValueMean = 0;
         affectValueVar = 0;
         affectRateMean = 0;
         affectRateVar = 0;
         affectRoundMean = 0;
         affectRoundVar = 0;
         moveTimeAffectMean = 0;
         moveTimeAffectVar = 0;
         statusAffect = Player.statusId.NO_EFFECT.getId();
         message = "";
     }
 
     /**
      * Convert the original string into showing form.
      * %n For name of player affected
      * @param p The data of the player
      * @param av The absolute change of value due to this event
      * @param rv The relative change of value due to this event
      * @param mtv The change in move time due to this event
      * @return the String converted
      */
     private String convertMessage(Player p, int av, double rv, int mtv) {
 
         //Replace name
         String result = message.replaceAll("%n", p.getName());
 
         if (Double.compare(rv, 0.0) != 0 || av != 0) {
             //What quantity is affected
             result += Move.AFFECT_STRING[affectType];
 
             //Absolute increase/decrease
             if (av != 0) {
                 result += (av > 0 ? "上升" : "下降") + Math.abs(av);
             }
 
             //connector between absolute and relative increase/decrease
             if (av != 0 && rv != 0) {
                 result += "再";
             } else if (rv != 0) {
                 result += rv > 0 ? "上升" : "下降";
             }
 
             //Relative increase/decrease
             if (rv != 0) {
                 result += Utility.percentageForm(rv) + "%";
             }
 
             result += "！";
         }
 
         //movetime effect
         if (mtv != 0) {
             result += "所有招式使用次數" + (mtv > 0 ? "上升" : "下降") + Math.abs(mtv) + "次！";
         }
 
         //Status effect
         if (statusAffect != Player.statusId.NO_EFFECT.getId()) {
             result += (global ? "場上所有人" : p.getName()) + Player.STATUS_STRING[statusAffect] + "了！";
         }
 
         return result;
     }
 
     /**
      * Execute the random event
      * @param p Data of all the players
      * @return Result of the random event
      */
     private RandomEventResult execute(PlayerList p) {
         int actualAffectValue = affectValueMean + Utility.randBetween(-affectValueVar, affectValueVar);
         double actualAffectRate = affectRateMean + Utility.randBetween(-affectRateVar, affectRateVar);
         int actualAffectRound = affectRoundMean + Utility.randBetween(-affectRoundVar, affectRoundVar);
         int actualMoveTimeAffect = moveTimeAffectMean + Utility.randBetween(-moveTimeAffectVar, moveTimeAffectVar);
 
         Player affectedPlayer = p.randomPick();
 
         //Actual value change of the quality due to this random event
         int actualChange = 0;
 
         //Change the values
         switch (affectType) {
             case Move.AFFECT_HP: {
                 if (!global) {
 
                     //Store the original value for computing actual change
                     int tp = affectedPlayer.hp;
                     affectedPlayer.hp = (int) (affectedPlayer.hp * (1 + actualAffectRate) + actualAffectValue);
 
                     //Actual change
                     actualChange = affectedPlayer.hp - tp;
 
                 } else {
                     for (Player thisPlayer : p) {
                         if (thisPlayer.getHp() < 0) {
                             continue;
                         }
                         thisPlayer.hp = (int) (thisPlayer.hp * (1 + actualAffectRate) + actualAffectValue);
                     }
                 }
                 break;
             }
             case Move.AFFECT_ATK: {
                 if (!global) {
 
                     //Store the original value for computing actual change
                     int tp = affectedPlayer.atk;
                     affectedPlayer.atk = (int) (affectedPlayer.atk * (1 + actualAffectRate) + actualAffectValue);
 
                     //Actual change
                     actualChange = affectedPlayer.atk - tp;
 
                 } else {
                     for (Player thisPlayer : p) {
                         thisPlayer.atk = (int) (thisPlayer.atk * (1 + actualAffectRate) + actualAffectValue);
                     }
                 }
                 break;
             }
             case Move.AFFECT_DEF: {
                 if (!global) {
 
                     //Store the original value for computing actual change
                     int tp = affectedPlayer.def;
                     affectedPlayer.def = (int) (affectedPlayer.def * (1 + actualAffectRate) + actualAffectValue);
 
                     //Actual change
                     actualChange = affectedPlayer.def - tp;
 
                 } else {
                     for (Player thisPlayer : p) {
                         thisPlayer.def = (int) (thisPlayer.def * (1 + actualAffectRate) + actualAffectValue);
                     }
                 }
                 break;
             }
             case Move.AFFECT_SPD: {
                 if (!global) {
 
                     //Store the original value for computing actual change
                     int tp = affectedPlayer.spd;
                     affectedPlayer.spd = (int) (affectedPlayer.spd * (1 + actualAffectRate) + actualAffectValue);
 
                     //Actual change
                     actualChange = affectedPlayer.spd - tp;
 
                 } else {
                     for (Player thisPlayer : p) {
                         thisPlayer.spd = (int) (thisPlayer.spd * (1 + actualAffectRate) + actualAffectValue);
                     }
                 }
                 break;
             }
             case Move.AFFECT_MOR: {
                 if (!global) {
 
                     //Store the original value for computing actual change
                     int tp = affectedPlayer.mor;
                     affectedPlayer.mor = (int) (affectedPlayer.mor * (1 + actualAffectRate) + actualAffectValue);
 
                     //Actual change
                     actualChange = affectedPlayer.mor - tp;
 
                 } else {
                     for (Player thisPlayer : p) {
                         thisPlayer.mor = (int) (thisPlayer.mor * (1 + actualAffectRate) + actualAffectValue);
                     }
                 }
                 break;
             }
         }
 
         //Movetime effect
         if (actualMoveTimeAffect != 0) {
             if (!global) {
                 for (Move m : affectedPlayer.boughtAtk) {
                     m.setMoveTime(Utility.noSmallerThan(m.getMoveTime() + actualMoveTimeAffect, 0));
                 }
             } else {
                 for (Player thisPlayer : p) {
                     for (Move m : thisPlayer.boughtAtk) {
                         m.setMoveTime(Utility.noSmallerThan(m.getMoveTime() + actualMoveTimeAffect, 0));
                     }
                 }
             }
 
         }
 
         //Status effect
         if (statusAffect != Player.statusId.NO_EFFECT.getId()) {
             if (!global) {
                 affectedPlayer.status[statusAffect] = true;
                 affectedPlayer.reStatus[statusAffect] = true;
             } else {
                 for (Player thisPlayer : p) {
                     thisPlayer.status[statusAffect] = true;
                     thisPlayer.reStatus[statusAffect] = true;
                 }
             }
         }
 
         //Length of effect
         if (affectType != Player.statusId.NO_EFFECT.getId()) {
             if (!global) {
                 affectedPlayer.reAffect[affectType] += actualAffectRound;
             } else {
                 for (Player thisPlayer : p) {
                     thisPlayer.reAffect[affectType] += actualAffectRound;
                 }
             }
         }
 
         //Message
         String resultString = convertMessage(affectedPlayer, actualAffectValue, actualAffectRate, actualMoveTimeAffect) + '\n';
 
         //Return Random Event Result
         return new RandomEventResult(global ? affectedPlayer.getTeamName() : Player.NO_TEAM, affectType, actualChange, actualAffectRound, resultString);
     }
 
     /**
      * Load a list of random events and store it into loadedEvents field
      */
     private static void loadRandomEvent() throws BadRandomEventFileException {
         CsvReader r;
         try {
            r = new CsvReader(new InputStreamReader(new FileInputStream("RandomEvents.csv"), "UTF-8"));
         } catch (Exception ex) {
             throw new BadRandomEventFileException("RandomEvent.csv does not exist or cannot be loaded.", ex);
         }
 
         loadedEvents = new ArrayList<RandomEvent>();
         String[] s;
 
         int line = 0;
         try {
             while (r.readRecord()) {
                 line++;
                 s = r.getValues();
 
                 RandomEvent t = null;
                         
                 try {
                     //ignore lines that start with //
                     if (s[0].startsWith("//")) {
                         continue;
                     }
 
                     //store the read things into loaded events
                     t = new RandomEvent();
                     t.affectType = Integer.parseInt(s[0]);
                     t.affectValueMean = Integer.parseInt(s[1]);
                     t.affectValueVar = Integer.parseInt(s[2]);
                     t.affectRateMean = Double.parseDouble(s[3]);
                     t.affectRateVar = Double.parseDouble(s[4]);
                     t.affectRoundMean = Integer.parseInt(s[5]);
                     t.affectRoundVar = Integer.parseInt(s[6]);
                     t.moveTimeAffectMean = Integer.parseInt(s[7]);
                     t.moveTimeAffectVar = Integer.parseInt(s[8]);
                     t.statusAffect = Integer.parseInt(s[9]);
                     t.global = Integer.parseInt(s[10]) > 0 ? true : false;
                     t.message = s[11];
                 } catch (NumberFormatException ex){
                     throw new BadRandomEventFileException("Error on line " + line + ": Numeric value expected. ", ex);
                 } catch (ArrayIndexOutOfBoundsException ex){
                     throw new BadRandomEventFileException("Error on line " + line + ": More values expected. ", ex);
                 }
 
                 loadedEvents.add(t);
             }
         } catch (Exception ex) {
             throw new BadRandomEventFileException("RandomEvent.csv cannot be loaded.", ex);
         }
 
         r.close();
 
 
 
     }
 
     /**
      * Apply a random event and execute it
      * @param code The code of the event
      * @param p Data for all players
      */
     private static RandomEventResult applyRandomEvent(PlayerList p) {
         int code = Utility.randBetween(0, loadedEvents.size() - 1);
         return loadedEvents.get(code).execute(p);
     }
 
     /**
      * Execute random event stage
      * @param p Data for all players
      */
     static RandomEventResult randomEvent(PlayerList p) {
         try {
             if (loadedEvents == null) {
                 loadRandomEvent();
             }
         } catch (BadRandomEventFileException ex) {
             javax.swing.JOptionPane.showMessageDialog(null, " 無法讀取隨機事件設定檔！", "", javax.swing.JOptionPane.ERROR_MESSAGE);
             BattleSysLogger.getLogger().logp(Level.WARNING, "RandomEvent", "randomEvent", " 無法讀取隨機事件設定檔！", ex);
         }
 
         String s = "";
         for (int i = 0; i < Move.AFFECT_COUNT; ++i) {
             for (Player thisPlayer : p) {
                 //Skip all dead players
                 if (thisPlayer.getHp() <= 0) {
                     continue;
                 }
 
                 if (thisPlayer.reAffect[i] > 0) {
                     thisPlayer.reAffect[i]--;
                     if (thisPlayer.reAffect[i] == 0) {
                         s += thisPlayer.getName() + "的" + Move.AFFECT_STRING[i] + "回復正常了。";
                         switch (i) {
                             case Move.AFFECT_ATK: {
                                 thisPlayer.atk = thisPlayer.btAtk;
                                 break;
                             }
                             case Move.AFFECT_DEF: {
                                 thisPlayer.def = thisPlayer.btDef;
                                 break;
                             }
                             case Move.AFFECT_SPD: {
                                 thisPlayer.spd = thisPlayer.btSpd;
                                 break;
                             }
                             case Move.AFFECT_MOR: {
                                 thisPlayer.mor = thisPlayer.btMor;
                                 break;
                             }
                         }
                         for (int k = 0; k < Player.STATUS_COUNT; ++k) {
                             if (thisPlayer.reStatus[k] == true && thisPlayer.status[k] == true) {
                                 s += thisPlayer.getName() + "從" + Player.STATUS_STRING[k] + "狀態中恢復過來了。";
                                 thisPlayer.reStatus[k] = false;
                                 thisPlayer.status[k] = false;
                             }
                         }
                     }
                 }
 
             }
 
         }
         if (Utility.probTest(15)) {
             s += "[color=#770000]";
             RandomEventResult re = applyRandomEvent(p);
             s += re.getResultString();
             s += " [/color]";
             return new RandomEventResult(re, s);
         } else {
             return new RandomEventResult(Player.NO_TEAM, -1, -1, -1, "");
         }
     }
 }
