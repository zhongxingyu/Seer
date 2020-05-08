 package ru.compscicenter.lidis.client;
 
 import ru.compscicenter.lidis.LidisException;
 import ru.compscicenter.lidis.Main;
 import ru.compscicenter.lidis.Query;
 
 /**
  * Author: Vasiliy Khomutov
  * Date: 13.10.2013
  */
 public class SyntheticDataClient {
 
     public static void main(String[] args) throws LidisException {
         if (!ConsoleClient.checksArgs(args)) {
             return;
         }
         Client client = new Client(args[0], Integer.parseInt(args[1]));
 
         long start = System.currentTimeMillis();
 
         for (int i = 0; i < 500000; i++) {
             String randomKey = Main.generateRandomKey(10, 90);
             String randomValue = Main.generateRandomValue(100, 9900);
            if (i % 10000 == 0 && i != 0) {
                 System.out.println("Added " + i + " keys");
             }
             client.makeQuery(Query.parse("PUT " + randomKey + " " + randomValue));
         }
 
         System.out.println("Done in " + (System.currentTimeMillis() - start) / 1000 + " sec");
     }
 }
