 package org.bitbucket.theimplementer.mipsdisassembler;
 
 import net.emaze.dysfunctional.contracts.dbc;
 import net.emaze.dysfunctional.dispatching.delegates.Delegate;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 public class StreamToIntegers implements Delegate<List<Integer>, InputStream> {
 
     @Override
     public List<Integer> perform(InputStream inputStream) {
         dbc.precondition(inputStream != null, "inputStream cannot be null");
         final DataInputStream dataInputStream = new DataInputStream(inputStream);
         final List<Integer> integers = new LinkedList<Integer>();
         while (true) {
             try {
                 final Integer integer = Integer.valueOf(dataInputStream.readInt());
                integers.add(integer);
             } catch (IOException e) {
                 break;
             }
         }
 
         return integers;
     }
 }
