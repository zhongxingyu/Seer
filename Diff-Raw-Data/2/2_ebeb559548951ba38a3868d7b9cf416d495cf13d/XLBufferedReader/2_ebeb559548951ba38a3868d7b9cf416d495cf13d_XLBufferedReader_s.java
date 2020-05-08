 package model;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Map;
 
 import util.XLException;
 
 public class XLBufferedReader extends BufferedReader {
     public XLBufferedReader(String name) throws FileNotFoundException {
         super(new FileReader(name));
     }
 
     public void load(Map<String, Slot> map, SlotFactory slotFactory, Sheet sheet) {
         try {
             while (ready()) {
                 String string = readLine();
                 int i = string.indexOf('=');
                map.put(string.substring(0, i-1),slotFactory.build(string.substring(i+1),sheet));
             }
         } catch (Exception e) {
             throw new XLException(e.getMessage());
         }
     }
 }
