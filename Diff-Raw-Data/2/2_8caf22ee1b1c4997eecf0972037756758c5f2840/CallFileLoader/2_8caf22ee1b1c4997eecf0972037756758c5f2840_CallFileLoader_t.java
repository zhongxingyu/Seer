 package net.cscott.sdr.calls.transform;
 
 import java.io.Reader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Map;
 
 import org.junit.runner.RunWith;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.parser.CallFileBuilder;
 
 /** This class contains the code to parse and load a call list. */
 @RunWith(value=JDoctestRunner.class)
 public abstract class CallFileLoader {
     // This does the load
     /**
      * Parse a call list and add its calls to the given map.
      * @doc.test Loading call list resources from the path:
      *  js> function u(name) {
      *    >   c=java.lang.Class.forName("net.cscott.sdr.calls.transform.CallFileLoader")
      *    >   p="net/cscott/sdr/calls/lists/"+name+".calls"
      *    >   return c.getClassLoader().getResource(p)
      *    > }
      *  js> m = new java.util.LinkedHashMap()
      *  {}
      *  js> CallFileLoader.load(u('basic'), m)
      *  js> CallFileLoader.load(u('mainstream'), m)
      *  js> CallFileLoader.load(u('plus'), m)
      *  js> CallFileLoader.load(u('a1'), m)
      *  js> CallFileLoader.load(u('a2'), m)
      */
     public static void load(URL file, Map<String,Call> db) {
         try {
 	    Reader r = new InputStreamReader(file.openStream(), "utf-8");
            for (Call c : CallFileBuilder.parseCalllist(r, file.getFile())) {
                 assert !db.containsKey(c.getName()) :
                     "duplicate call: "+c.getName();
                 db.put(c.getName(), c);
             }
         }
         catch (Exception e) {
             System.err.println("parser exception loading "+file);
             e.printStackTrace();   // so we can get stack trace             
         }
     }
 }
