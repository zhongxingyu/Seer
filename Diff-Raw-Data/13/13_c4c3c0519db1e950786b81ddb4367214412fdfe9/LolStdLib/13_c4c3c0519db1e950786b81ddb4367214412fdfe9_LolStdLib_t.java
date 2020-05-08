 package com.lolcode.runtime;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class LolStdLib {
     private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
     protected void putStr(String str) {
        System.out.print(str);
     }
 
     protected String readStr() {
         try {
             return br.readLine();
         } catch (IOException e) {
             putStr(e.getMessage());
         }
         return "";
     }
 
     public void print(LolObject lolObject) {
         putStr(lolObject.toLolString().strVal);
     }
 
     public LolObject read() {
         String str = readStr();
         if (str.equals("WIN")) {
             return new LolBool(true);
         } else if (str.equals("FAIL")) {
             return new LolBool(false);
         }
         try {
             return new LolInt(Integer.parseInt(str));
         } catch (Exception e) {
             try {
                 return new LolDouble(Double.parseDouble(str));
             } catch (Exception ex) {
                 return new LolString(str);
             }
         }
     }
 }
