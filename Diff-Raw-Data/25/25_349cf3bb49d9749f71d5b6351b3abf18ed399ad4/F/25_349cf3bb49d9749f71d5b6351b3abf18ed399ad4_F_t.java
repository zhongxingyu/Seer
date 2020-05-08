 package org.mystic.opencup.round1;
 
import java.io.*;
 
 public class F {
 
     int[] tt = null;
     int tx = 0;
     private BufferedReader reader;
     private PrintWriter writer;
 
     public F() throws IOException {
        reader = new BufferedReader(new FileReader("intset.in"));
        writer = new PrintWriter(new FileWriter("intset.out"));
     }
 
     public static void main(String[] args) throws IOException {
         new F().solve();
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
         int n = readInt();
         int l = readInt();
         int m = readInt();
        // @todo calculate with C's, using log
         writer.flush();
     }
 }
