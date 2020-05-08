 package Utils.Monopoly;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: bsankar
  * Date: 9/11/12
  */
 public class MonopolyGame {
     private HashMap<String, Long> popularSquare = new HashMap<String, Long>();
     private int communityChestPile = new Random().nextInt(16);
     private int chancePile = new Random().nextInt(16);
 
     private void init() {
         for (MonopolySquares square : MonopolySquares.values()) {
             popularSquare.put(square.modalValue, (long) 0);
         }
     }
 
     public void startGame(MonopolyDice dice, long numberOfRolls) throws Exception {
         init();
        n = numberOfRolls;
         MonopolySquares currentSquare = MonopolySquares.GO;
         popularSquare.put(currentSquare.getModalValue(), (long) 1);
         for (long sample = 1; sample <= numberOfRolls; sample++) {
             int diceRoll = dice.roll();
             if (diceRoll == 0) {
                 currentSquare = MonopolySquares.JAIL;
             } else {
                 currentSquare = nextSquare(currentSquare, diceRoll);
             }
             long counter = popularSquare.get(currentSquare.getModalValue());
             popularSquare.put(currentSquare.getModalValue(), counter + 1);
         }
     }
 
     public String getModalString() {
         String sq1 = null, sq2 = null, sq3 = null;
         long max1 = 0, max2 = 0, max3 = 0;
         for (Map.Entry<String, Long> entry : popularSquare.entrySet()) {
             long val = entry.getValue();
             String key = entry.getKey();
             if (val > max1) {
                 max3 = max2;
                 sq3 = sq2;
 
                 max2 = max1;
                 sq2 = sq1;
 
                 max1 = val;
                 sq1 = key;
             } else if (val > max2) {
                 max3 = max2;
                 sq3 = sq2;
 
                 max2 = val;
                 sq2 = key;
             } else if (val > max3) {
                 max3 = val;
                 sq3 = key;
             }
         }
         /*System.out.println((((double) max1 / (double) n) * 100) + " "
                 + (((double) max2 / (double) n) * 100) + " "
                 + (((double) max3 / (double) n) * 100));*/
         return (sq1 + sq2 + sq3);
     }
 
     private MonopolySquares nextSquare(MonopolySquares square, int steps) throws Exception {
         int val = ((square.getValue() + steps) % MonopolySquares.size);
         MonopolySquares sq = MonopolySquares.getByValue(val);
         if (sq.equals(MonopolySquares.CH1)
                 || sq.equals(MonopolySquares.CH2)
                 || sq.equals(MonopolySquares.CH3)) {
             return Chance(sq);
         } else if (sq.equals(MonopolySquares.CC1)
                 || sq.equals(MonopolySquares.CC2)
                 || sq.equals(MonopolySquares.CC3)) {
             return CommunityChest(sq);
         } else if (sq.equals(MonopolySquares.G2J)) {
             return MonopolySquares.JAIL;
         }
         return sq;
     }
 
     private MonopolySquares CommunityChest(MonopolySquares square) {
         communityChestPile = (communityChestPile + 1) % 16;
         //communityChestPile = (new Random().nextInt(16));
         //Assumption the Go card is the 0th position of the pile
         if (communityChestPile == 0) {
             return MonopolySquares.GO;
         }
         //Assumption the Jail card is the 2th position of the pile
         else if (communityChestPile == 1) {
             return MonopolySquares.JAIL;
         }
         return square;
     }
 
     private MonopolySquares Chance(MonopolySquares square) {
         chancePile = (chancePile + 1) % 16;
         //chancePile = (new Random().nextInt(16));
         switch (chancePile) {
             case 0:
                 return MonopolySquares.GO;
             case 1:
                 return MonopolySquares.JAIL;
             case 2:
                 return MonopolySquares.C1;
             case 3:
                 return MonopolySquares.E3;
             case 4:
                 return MonopolySquares.H2;
             case 5:
                 return MonopolySquares.R1;
             case 7:
             case 8:
                 return nextRailway(square);
             case 9:
                 return nextUtility(square);
             case 10:
                 return backThreeSquares(square);
             default:
                 return square;
         }
     }
 
     private MonopolySquares nextRailway(MonopolySquares square) {
         if (square.equals(MonopolySquares.CH1)) {
             return MonopolySquares.R2;
         } else if (square.equals(MonopolySquares.CH2)) {
             return MonopolySquares.R3;
         }
         return MonopolySquares.R1;
     }
 
     private MonopolySquares nextUtility(MonopolySquares square) {
         if (square.equals(MonopolySquares.CH1) || square.equals(MonopolySquares.CH3)) {
             return MonopolySquares.U1;
         }
         return MonopolySquares.U2;
     }
 
     private MonopolySquares backThreeSquares(MonopolySquares square) {
         if (square.equals(MonopolySquares.CH1)) {
             return MonopolySquares.T1;
         } else if (square.equals(MonopolySquares.CH2)) {
             return MonopolySquares.D3;
         }
         return CommunityChest(square);
         //return MonopolySquares.CC3;
     }
 }
