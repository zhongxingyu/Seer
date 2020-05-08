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
        List history = getConnectionHistory();
        if (history.contains(element))
        {
            return;
        }
         
         String newPrefEntry = element + SEPARATOR;
        for (int i =0; i < Math.min(limit -1, history.size()); ++i)
         {
            newPrefEntry += history.get(i) + SEPARATOR;
         }
         prefs.put(connectionHistory, newPrefEntry);
     }
     
 }
