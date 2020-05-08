 package euler.level2;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import euler.Problem;
 
 public class Problem084 extends Problem<Integer> {
 
     class ChanceField extends Field {
         public ChanceField(int id, String name) {
             super(id, name);
         }
 
         @Override
         protected void addToMap(ChanceMap chances, double chance) {
             board[0].addToMap(chances, 1 / 16. * chance); // GO
             board[10].addToMap(chances, 1 / 16. * chance); // JAIL
             board[11].addToMap(chances, 1 / 16. * chance); // C1
             board[24].addToMap(chances, 1 / 16. * chance); // E3
             board[39].addToMap(chances, 1 / 16. * chance); // H2
             board[5].addToMap(chances, 1 / 16. * chance); // R1
             nextRailway().addToMap(chances, 2 / 16. * chance);
             nextUtility().addToMap(chances, 1 / 16. * chance);
             board[getId() - 3].addToMap(chances, 1 / 16. * chance); // back 3
             super.addToMap(chances, 6 / 16. * chance);
         }
 
         private Field nextRailway() {
             for (int ix = getId() + 1;; ix = (ix + 1) % board.length) {
                 if (board[ix].getName().startsWith("R")) {
                     return board[ix];
                 }
             }
         }
 
         private Field nextUtility() {
             for (int ix = getId() + 1;; ix = (ix + 1) % board.length) {
                 if (board[ix].getName().startsWith("U")) {
                     return board[ix];
                 }
             }
         }
     }
 
     static class ChanceMap extends HashMap<Field, Double> {
         private static final long serialVersionUID = -2044704161069978938L;
 
         public void add(Field field, double chance) {
             if (containsKey(field)) {
                 chance += get(field);
             }
             put(field, chance);
         }
     }
 
     class CommunityChestField extends Field {
         public CommunityChestField(int id, String name) {
             super(id, name);
         }
 
         @Override
         protected void addToMap(ChanceMap chances, double chance) {
             board[0].addToMap(chances, 1 / 16. * chance); // GO
             board[10].addToMap(chances, 1 / 16. * chance); // JAIL
             super.addToMap(chances, 14 / 16. * chance);
         }
     }
 
     class Field {
         private final int id;
         private final String name;
         private double chance, newChance;
 
         public Field(int id, String name) {
             this.id = id;
             this.name = name;
             chance = 1;
         }
 
         protected void addToMap(ChanceMap chances, double chance) {
             chances.add(this, chance);
         }
 
         public double getChance() {
             return chance;
         }
 
         public int getId() {
             return id;
         }
 
         public String getName() {
             return name;
         }
 
         public double getNewChance() {
             return newChance;
         }
 
         private void propagateChance(ChanceMap chances, int nrDice, double baseChance, int doublesTrown) {
             for (int dice1 = 1; dice1 <= nrDice; dice1++) {
                 for (int dice2 = 1; dice2 <= nrDice; dice2++) {
                     Field next = board[(id + dice1 + dice2) % board.length];
                     if (dice1 == dice2 && next != board[10]) {
                         if (doublesTrown < 2) {
                             // Trow again from the new fields?
                             ChanceMap nextMap = new ChanceMap();
                             next.addToMap(nextMap, baseChance);
                             for (Entry<Field, Double> entry : nextMap.entrySet()) {
                                 entry.getKey().propagateChance(chances, nrDice, entry.getValue() * baseChance, doublesTrown + 1);
                             }
                         } else {
                             board[10].addToMap(chances, baseChance); // 3 doubles, go to jail!
                         }
                     } else {
                         next.addToMap(chances, baseChance);
                     }
                 }
             }
         }
 
         public void propagateChance(int nrDice) {
             ChanceMap chances = new ChanceMap();
             propagateChance(chances, nrDice, 1. / (nrDice * nrDice), 0);
             for (Entry<Field, Double> chance : chances.entrySet()) {
                 chance.getKey().newChance += this.chance * chance.getValue();
             }
         }
 
         public void reset(double factor) {
             chance = newChance * factor;
             newChance = 0;
         }
 
         @Override
         public String toString() {
             return String.format("(%02d) %4s -> %1.2f%%", id, name, chance * 100);
         }
     }
 
     class GotoJailField extends Field {
         public GotoJailField(int id, String name) {
             super(id, name);
         }
 
         @Override
         protected void addToMap(ChanceMap chances, double chance) {
             board[10].addToMap(chances, chance); // JAIL
         }
     }
 
     private final Field[] board = new Field[] { new Field(0, "GO"),
                                                new Field(1, "A1"),
                                                new CommunityChestField(2, "CC1"),
                                                new Field(3, "A2"),
                                                new Field(4, "T1"),
                                                new Field(5, "R1"),
                                                new Field(6, "B1"),
                                                new ChanceField(7, "CH1"),
                                                new Field(8, "B2"),
                                                new Field(9, "B3"),
                                                new Field(10, "JAIL"),
                                                new Field(11, "C1"),
                                                new Field(12, "U1"),
                                                new Field(13, "C2"),
                                                new Field(14, "C3"),
                                                new Field(15, "R2"),
                                                new Field(16, "D1"),
                                                new CommunityChestField(17, "CC2"),
                                                new Field(18, "D2"),
                                                new Field(19, "D3"),
                                                new Field(20, "FP"),
                                                new Field(21, "E1"),
                                                new ChanceField(22, "CH2"),
                                                new Field(23, "E2"),
                                                new Field(24, "E3"),
                                                new Field(25, "R3"),
                                                new Field(26, "F1"),
                                                new Field(27, "F2"),
                                                new Field(28, "U2"),
                                                new Field(29, "F3"),
                                                new GotoJailField(30, "G2J"),
                                                new Field(31, "G1"),
                                                new Field(32, "G2"),
                                                new CommunityChestField(33, "CC3"),
                                                new Field(34, "G3"),
                                                new Field(35, "R4"),
                                                new ChanceField(36, "CH3"),
                                                new Field(37, "H1"),
                                                new Field(38, "T2"),
                                                new Field(39, "H2"), };
 
     @Override
     public Integer solve() {
         double total = 0;
         for (int it = 0; it < 10; it++) {
             for (Field field : board) {
                field.propagateChance(4);
             }
 
             total = 0;
             for (Field field : board) {
                 total += field.getNewChance();
             }
 
             double factor = 1 / total;
             for (Field field : board) {
                 field.reset(factor);
             }
         }
 
         Arrays.sort(board, new Comparator<Field>() {
             @Override
             public int compare(Field o1, Field o2) {
                 return (int) ((o2.getChance() - o1.getChance()) * 10000);
             };
         });
 
         return board[0].getId() * 10000 + board[1].getId() * 100 + board[2].getId();
     }
 }
