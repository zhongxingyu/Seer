 package me.limebyte.battlenight.core.chat;
 
 import java.util.List;
 
 public class ListPage extends StandardPage {
 
     public ListPage(String title, List<String> list) {
         super(title, "");
 
         String text = "";
         for (String item : list) {
             text += item + "\n";
         }
        this.text = text.substring(0, text.length() - 1);
     }
 
 }
