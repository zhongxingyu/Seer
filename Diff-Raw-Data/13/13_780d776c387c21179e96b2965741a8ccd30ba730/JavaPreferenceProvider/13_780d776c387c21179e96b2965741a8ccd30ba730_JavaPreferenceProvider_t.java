 package us.physion.ovation.dbconnection;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.prefs.Preferences;
 
 /**
  *
  * @author huecotanks
  */
 public class JavaPreferenceProvider implements ConnectionHistoryProvider{
     
     private Preferences prefs;
     private static final String SEPARATOR = ";";
     private String connectionHistory = "connectionHistory";
     private static final int limit = 10;
     public JavaPreferenceProvider(Preferences p)
     {
         prefs = p;
     }
     
     public List<String> getConnectionHistory()
     {
         String s = prefs.get(connectionHistory, "");
         if (s.isEmpty())
         {
             return new ArrayList<String>();
         }
                
         ArrayList a = new ArrayList<String>();
         for (String str: s.split(SEPARATOR))
         {
             a.add(str);
         }
         
         return a;
     }
     
     public void addConnectionFile(String element)
     {
        List<String> history = getConnectionHistory();
         
         String newPrefEntry = element + SEPARATOR;
        for (String item : history)
         {
            if (!item.equals(element))
                newPrefEntry += item + SEPARATOR;
         }
         prefs.put(connectionHistory, newPrefEntry);
     }
     
 }
