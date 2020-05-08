 package me.limebyte.battlenight.core.Other;
 
 import org.bukkit.ChatColor;
 import org.bukkit.util.ChatPaginator;
 
 public class Page {
 
     String title, text, header, footer;
 
     public Page(String title, String text) {
         this.title = title;
         this.text = text;
         header = getHeader();
         footer = getFooter();
     }
 
     public String[] getPage() {
         String[] page = new String[3];
         page[0] = header;
         page[1] = text;
         page[2] = footer;
         return page;
     }
 
     private String getHeader() {
        String formattedTitle = " " + ChatColor.WHITE + title + " ";
         int dashCount = 0;
 
         // Calculate total number of dashes
         int i = formattedTitle.length();
         while (i < ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH) {
             i++;
             dashCount++;
         }
 
         // Create dashes String for a single side
         StringBuilder dashes = new StringBuilder();
         dashes.append(ChatColor.DARK_GRAY);
         for (i = 0; i < dashCount / 2; i++) {
             dashes.append("-");
         }
 
        return dashes.toString() + formattedTitle + dashes.toString();
     }
 
     private String getFooter() {
         StringBuilder dashes = new StringBuilder();
         dashes.append(ChatColor.DARK_GRAY);

        if (header != null) {
            // Create dashes String
            for (int i = 0; i < header.length(); i++) {
                dashes.append("-");
            }
        } else {
            // Create dashes String
            for (int i = 0; i < ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH; i++) {
                dashes.append("-");
            }
         }
 
         return dashes.toString();
     }
 
 }
