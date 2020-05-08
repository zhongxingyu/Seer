package org.mystic.opencup.round1;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 
 public class B {
 
     int[] tt = null;
     int tx = 0;
     private BufferedReader reader;
     private PrintWriter writer;
 
     public B() throws IOException {
 
         reader = new BufferedReader(new FileReader("checkers.in"));
         writer = new PrintWriter(new FileWriter("checkers.out"));
 
 //        reader = new BufferedReader(new InputStreamReader(System.in));
 //        writer = new PrintWriter(System.out);
     }
 
     public static void main(String[] args) throws IOException {
         new B().solve();
     }
 
     int[] readInts() throws IOException {
         String[] s = reader.readLine().split(" ");
         int[] ints = new int[s.length];
         for (int i = 0; i < ints.length; i++) {
             ints[i] = Integer.parseInt(s[i]);
         }
         return ints;
     }
 
     int readInt() throws IOException {
         if (tt == null || tx == tt.length) {
             tt = readInts();
             tx = 0;
         }
         return tt[tx++];
     }
 
     private void solve() throws IOException {
         int[] first = readInts();
         int n = first[0];
         int k = first[1];
         int[] initialLengths = new int[n];
 
         char[][] initialHistory = new char[n][120];
 
         for (int i = 0; i < n; ++i) {
             char[] chars = reader.readLine().toCharArray();
 
             for (int j = 0; j < chars.length; ++j) {
                 initialHistory[i][j] = chars[j];
             }
             initialLengths[i] = chars.length;
         }
         int[] currentLengths = new int[n];
 
         int maxWhite = 0;
         char[] vasyasHistory = new char[100];
         int vl = 0;
         for (int i = 0; i < 1L << 20; ++i) {
             int currentWhite = 0;
             int playedGames = 0;
             for(int j = 0; j < n; j++) {
                 currentLengths[j] = initialLengths[j];
             }
             vl = 0;
             for (int j = 0; j < k && playedGames < k; ++j) {
                 int gtp = 2;
                 if ((i & (1 << j)) == 0) {
                     gtp = 1;
                 }
                 for(int games = 0; games < gtp && playedGames < k; games++) {
                     playedGames++;
                     char[] historyForCurrentEngine = initialHistory[j % n];
                     int hi = currentLengths[j % n];
                     int vi = vl;
                     boolean vasyaPlaysWhite = false;
                     boolean found = false;
                     while (vi > 0 && hi > 0 && !found) {
                         char v = vasyasHistory[--vi];
                         char e = historyForCurrentEngine[--hi];
                         if (v == e) {
                             continue;
                         } else {
                             vasyaPlaysWhite = v == 'B';
                             found = true;
 
                         }
                     }
                     if (!found) {
                         vasyaPlaysWhite = true;
                     }
                     if (vasyaPlaysWhite) {
                         currentWhite++;
                         vasyasHistory[vl++] = ('W');
                         historyForCurrentEngine[currentLengths[j % n]++] = ('B');
                     } else {
                         vasyasHistory[vl++] = ('B');
                         historyForCurrentEngine[currentLengths[j % n]++] = ('W');
                     }
 
                 }
             }
             if (currentWhite > maxWhite) {
                 maxWhite = currentWhite;
             }
         }
 
         writer.println(maxWhite);
         writer.flush();
     }
 }
