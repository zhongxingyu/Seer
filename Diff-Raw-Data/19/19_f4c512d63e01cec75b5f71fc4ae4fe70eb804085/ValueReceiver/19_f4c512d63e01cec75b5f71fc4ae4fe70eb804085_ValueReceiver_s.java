 package com.rakuten.rit.roma.romac4j;
 
 import java.util.concurrent.TimeoutException;
 
 import com.rakuten.rit.roma.romac4j.pool.Connection;
 
 public class ValueReceiver extends Receiver {
     String str;
     byte[] value;
 
     @Override
     public void receive(Connection con) throws TimeoutException {
         int len = 0;
         str = con.readLine();
        String[] header = str.split(" ");
        if (header.length >= 4) {
            len = Integer.valueOf(header[3]);
        } else {
            len = Integer.parseInt(str);
         }
         value = con.readValue(len);
     }
 
     public byte[] getValue() {
         return value;
     }
 
     public int getCasid() {
         String[] header = str.split(" ");
         int len = 0;
         if (header.length == 5) {
             len = Integer.valueOf(header[4]);
         }
         return len;
     }
 }
