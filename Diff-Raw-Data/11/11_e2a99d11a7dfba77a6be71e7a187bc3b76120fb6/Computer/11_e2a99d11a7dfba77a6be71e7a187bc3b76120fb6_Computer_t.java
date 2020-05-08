 package ru.pondohva.player;
 
 import java.util.*;
 import ru.pondohva.game.Field;
 import ru.pondohva.game.Game;
 
 public class Computer extends Player {
     private static Field newPole;
     private static Game newGame;
     private char[][] field;
     private char sign;
 
     public Computer(Field pole, Game game, char sign) {
         this.newPole = pole;
         this.newGame = game;
         this.sign = sign;
     }
 
     public void step() {
         field = newPole.getField();
         System.out.println("Now: " + sign);
         List<LinkedHashMap<Integer, Integer>> result = new ArrayList<LinkedHashMap<Integer, Integer>> ();
         if (sign == 'X') {
             result.addAll(findLine('X', 2));
             result.addAll(findCol('X', 2));
             result.addAll(findDiag('X', 2));
             result.addAll(findLine('O', 2));
             result.addAll(findCol('O', 2));
             result.addAll(findDiag('O', 2));
             result.addAll(findLine('X', 1));
             result.addAll(findCol('X', 1));
             result.addAll(findDiag('X', 1));
         } else {
             result.addAll(findLine('O', 2));
             result.addAll(findCol('O', 2));
             result.addAll(findDiag('O', 2));
             result.addAll(findLine('X', 2));
             result.addAll(findCol('X', 2));
             result.addAll(findDiag('X', 2));
             result.addAll(findLine('O', 1));
             result.addAll(findCol('O', 1));
             result.addAll(findDiag('O', 1));
         }
 
         if (result.isEmpty()) {
             setRandomClever();
         } else {
             Map<Integer, Integer> temp = new LinkedHashMap<Integer, Integer> ();
             temp = result.get(0);
             if (!temp.isEmpty()) {
                 List<Integer> keys = new ArrayList<Integer>(temp.keySet());
                 Integer x = keys.get(0);
                 Integer y = temp.get(x);
                 newPole.setVal(x, y, sign);
             }
 
         }
     }
 
     private List<LinkedHashMap<Integer, Integer>> findLine(char symbol, int match) {
         List<LinkedHashMap<Integer, Integer>> result = new ArrayList<LinkedHashMap<Integer, Integer>> ();
         for (int i = 0; i < newPole.field_size; i++) {
             int matchCount = 0;
             List<LinkedHashMap<Integer, Integer>> temp = new ArrayList<LinkedHashMap<Integer, Integer>> ();
             for (int j = 0; j < newPole.field_size; j++) {
                 if (field[i][j] == symbol || field[i][j] == ' ') {
                     if (field[i][j] == symbol) {
                         matchCount++;
                     } else {
                        LinkedHashMap<Integer, Integer> mmap = new LinkedHashMap<Integer, Integer>();
                         mmap.put(i,j);
                         temp.add(mmap);
                     }
                     if (j == (newPole.field_size - 1) && matchCount == match) {
                         result.addAll(temp);
                     } else {
                         continue;
                     }
                 } else {
                     break;
                 }
             }
         }
         return result;
     }
 
     private List<LinkedHashMap<Integer, Integer>> findCol(char symbol, int match) {
         List<LinkedHashMap<Integer, Integer>> result = new ArrayList<LinkedHashMap<Integer, Integer>> ();
         for (int j = 0; j < newPole.field_size; j++) {
             int matchCount = 0;
             List<LinkedHashMap<Integer, Integer>> temp = new ArrayList<LinkedHashMap<Integer, Integer>> ();
             for (int i = 0; i < newPole.field_size; i++) {
                 if (field[i][j] == symbol || field[i][j] == ' ') {
                     if (field[i][j] == symbol) {
                         matchCount++;
                     } else {
                        LinkedHashMap<Integer, Integer> mmap = new LinkedHashMap<Integer, Integer>();
                         mmap.put(i,j);
                         temp.add(mmap);
                     }
                     if (i == (newPole.field_size - 1) && matchCount == match) {
                         result.addAll(temp);
                     } else {
                         continue;
                     }
                 } else {
                     break;
                 }
             }
         }
         return result;
     }
 
     private List<LinkedHashMap<Integer, Integer>> findDiag(char symbol, int match) {
         List<LinkedHashMap<Integer, Integer>> result = new ArrayList<LinkedHashMap<Integer, Integer>> ();
         int matchCount = 0;
         int matchCount2 = 0;
         List<LinkedHashMap<Integer, Integer>> temp = new ArrayList<LinkedHashMap<Integer, Integer>> ();
         List<LinkedHashMap<Integer, Integer>> temp2 = new ArrayList<LinkedHashMap<Integer, Integer>> ();
         for (int i = 0; i < newPole.field_size; i++) {
             if (field[i][i] == symbol || field[i][i] == ' ') {
                 if (field[i][i] == symbol) {
                     matchCount++;
                 } else {
                    LinkedHashMap<Integer, Integer> mmap = new LinkedHashMap<Integer, Integer>();
                     mmap.put(i,i);
                     temp.add(mmap);
                 }
                 if (i == (newPole.field_size - 1) && matchCount == match) {
                     result.addAll(temp);
                 }
             }
             if (field[i][newPole.field_size - i - 1] == symbol || field[i][newPole.field_size - i - 1] == ' ') {
                 if (field[i][newPole.field_size - i - 1] == symbol) {
                     matchCount2++;
                 } else {
                    LinkedHashMap<Integer, Integer> mmap2 = new LinkedHashMap<Integer, Integer>();
                     mmap2.put(i,newPole.field_size - i - 1);
                     temp2.add(mmap2);
                 }
                 if (i == (newPole.field_size - 1) && matchCount2 == match) {
                     result.addAll(temp2);
                 }
             } else {
                 break;
             }
         }
         return result;
     }
 
     private void setRandom() {
         Random randomGenerator = new Random();
         int x = randomGenerator.nextInt(newPole.field_size);
         int y = randomGenerator.nextInt(newPole.field_size);
         if (field[x][y] != ' ') {
             setRandom();
         } else {
             newPole.setVal(x, y, sign);
         }
     }
 
     private void setRandomClever() {
         int x = 0, y = 0;
         List<LinkedHashMap<Integer, Integer>> result = new ArrayList<LinkedHashMap<Integer, Integer>> ();
         LinkedHashMap<Integer, Integer> temp = new LinkedHashMap<Integer, Integer> ();
         Random randomGenerator = new Random();
         result.addAll(findLine(sign, 0));
         result.addAll(findCol(sign, 0));
         result.addAll(findDiag(sign, 0));
         if (result.isEmpty()) {
             setRandom();
             return;
         }
         temp = result.get(randomGenerator.nextInt(result.size()));
         for (int key: temp.keySet()) {
             x = key;
             y = temp.get(key);
             break;
         }
         newPole.setVal(x, y, sign);
     }
 }
