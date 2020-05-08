 package fr.aumgn.diamondrush.views;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 
 public class MessagesView implements Iterable<String> {
 
     private List<String> messages;
     private StringBuilder current;
 
     public MessagesView() {
         this.messages = new ArrayList<String>();
         this.current = new StringBuilder();
     }
 
     public void append(Object obj) {
         current.append(obj);
     }
 
     public void nl() {
         messages.add(current.toString());
         current = new StringBuilder();
     }
 
     public void head(Object obj) {
         append(ChatColor.BOLD);
         append(obj);
     }
 
     public void headLn(Object obj) {
         head(obj.toString());
         nl();
     }
 
     public void entry(Object name, Object value) {
         append(ChatColor.BLUE);
         append(name);
         append(" : ");
         append(ChatColor.YELLOW);
         append(value);
     }
 
     public void entryLn(Object name, Object value) {
         entry(name, value);
         nl();
     }
 
     public void merge(StatisticsView statisticsView) {
         for (String message : statisticsView) {
             messages.add(message);
         }
     }
 
     @Override
     public Iterator<String> iterator() {
        if (current != null) {
            nl();
        }
         return messages.iterator();
     }
 }
