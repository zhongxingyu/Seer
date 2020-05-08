 package com.kisel.algo;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
 * Hello world!
  *
  */
 public class App {
 
     public static void main(String... arr) {
         int n;
         try {
             BufferedReader br = new BufferedReader(new FileReader("1.in"));
             n = Integer.parseInt(br.readLine());
             BufferedWriter bw = new BufferedWriter(new FileWriter("1.out"));
             for (int i = 1; i <= n; i++) {
                 String[] args = br.readLine().split(" ");
                 Coder coder = new Coder(args[1], args[2]);
                 bw.append("Case #" + i + ": " + coder.code(args[0]) + "\n");
             }
             br.close();
             bw.flush();
             bw.close();
         } catch (IOException e) {
             System.out.println("smth frong with file");
         }
     }
 }
