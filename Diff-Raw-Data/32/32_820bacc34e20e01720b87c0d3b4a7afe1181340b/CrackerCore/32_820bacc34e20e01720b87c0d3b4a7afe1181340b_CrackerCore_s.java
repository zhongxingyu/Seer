 package cz.cvut.keyczar.homework;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CrackerCore {
    public static byte[] select(List<long[]> stats) {
         // each item of the stats must have length exactly Byte.MAX_VALUE
         double[] means = Stats.mean(stats);
 
        if (means.length >= Byte.MAX_VALUE) {
            throw new RuntimeException("Something gone wrong. Expecting exactly "+ Byte.MAX_VALUE+ " items.");
        }
 
        ArrayList<Byte> idx = new ArrayList<Byte>();
         // TODO: implement your code here **************************
        // for each byte test the condition
        for (byte i = 0; i< means.length; i++) {
             idx.add(i);
         }
         // *********************************************************
 
         if (idx.isEmpty() || idx.size() > 6) return null;
 
        byte[] result = new byte[idx.size()];
        int i = 0; for (byte b : idx) result[i++] = b;
 
         return result;
     }
 }
