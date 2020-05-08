 package me.limebyte.battlenight.core.chat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 
 public class Page {
 
     String title, text, header, footer;
     private int pageWidth = 320;
 
     public Page(String title, String text) {
         this.title = title;
         this.text = text;
         header = getHeader();
         footer = getFooter();
     }
 
     public String[] getPage() {
         List<String> page = new ArrayList<String>();
         page.add(header);
         page.addAll(processText(text));
         page.add(footer);
         return page.toArray(new String[page.size()]);
     }
 
     private String getHeader() {
         String formattedTitle = " " + ChatColor.WHITE + title + " ";
         int dashCount = 0;
 
         // Calculate total number of dashes
         int dashSpace = getStringWidth("-");
         int spaceAvailable = getWidth() - getStringWidth(formattedTitle);
         int spaceRemaining = spaceAvailable;
 
         while (true) {
             if (dashSpace > spaceRemaining) {
                 break;
             }
 
             dashCount++;
             spaceRemaining -= dashSpace;
         }
 
         // Create dashes String for a single side
         String dashes = ChatColor.DARK_GRAY + "";
         for (int i = 0; i < dashCount / 2; i++) {
             dashes += "-";
         }
 
         String extras = "";
 
         // Add extra space to make the header the proper width
         int spaceSpace = getStringWidth(" ");
         while (true) {
             if (spaceRemaining - spaceSpace < 0) {
                 break;
             }
 
             extras += " ";
             spaceRemaining -= spaceSpace;
         }
 
         // Add the remaining dash if the dashCount was an odd number
         if (dashCount % 2 != 0) {
            extras += ChatColor.DARK_GRAY + "-";
         }
 
         return dashes + formattedTitle + extras + dashes;
     }
 
     private String getFooter() {
         String dashes = ChatColor.DARK_GRAY + "";
         int dashSpace = getStringWidth("-");
         int spaceAvailable = getStringWidth(header);
         int spaceRemaining = spaceAvailable;
 
         while (true) {
             if (dashSpace > spaceRemaining) {
                 break;
             }
 
             dashes += "-";
             spaceRemaining -= dashSpace;
         }
 
         return dashes;
     }
 
     private static int getStringWidth(String text) {
         final int[] characterWidths = new int[] {
                 1, 9, 9, 8, 8, 8, 8, 7, 9, 8, 9, 9, 8, 9, 9, 9,
                 8, 8, 8, 8, 9, 9, 8, 9, 8, 8, 8, 8, 8, 9, 9, 9,
                 4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6,
                 7, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6,
                 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6,
                 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6,
                 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6,
                 6, 3, 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 2, 6, 6,
                 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6,
                 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
                 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9, 9, 9, 5, 9, 9,
                 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7,
                 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1
         };
         final String allowedCharacters = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz {|}~?Ã³ÚÔõÓÕþÛÙÞ´¯ý─┼╔µã¶÷‗¹¨ Í▄°úÏÎâßÝ¾·±Ð¬║┐«¼¢╝í½╗";
         int length = 0;
         for (String line : ChatColor.stripColor(text).split("\n")) {
             int lineLength = 0;
             boolean skip = false;
             for (char ch : line.toCharArray()) {
                 if (skip) {
                     skip = false;
                 } else if (ch == '\u00A7') {
                     skip = true;
                 } else if (allowedCharacters.indexOf(ch) != -1) {
                     lineLength += characterWidths[ch];
                 }
             }
             length = Math.max(length, lineLength);
         }
         return length;
     }
 
     public int getWidth() {
         return pageWidth;
     }
 
     public void setWidth(int width) {
         pageWidth = width;
     }
 
     private List<String> processText(String text) {
         if (text.contains("\n")) {
             List<String> result = new ArrayList<String>();
             String[] lines = text.split("\n");
             for (String line : lines) {
                 result.addAll(wrapText(line));
             }
             return result;
         } else {
             return wrapText(text);
         }
     }
 
     private List<String> wrapText(String text) {
         List<String> lines = new ArrayList<String>();
 
         if (getStringWidth(text) <= getWidth()) {
             lines.add(text);
             return lines;
         }
 
         int spaceSpace = getStringWidth(" ");
         int spaceAvailable = getWidth();
         int spaceRemaining = spaceAvailable;
         String[] words = text.split(" ");
         String currentLine = "";
 
         for (String word : words) {
             int wordSpace = getStringWidth(word);
 
             // Word can't fit
             if (wordSpace > spaceRemaining) {
                 // Line is blank
                 if (wordSpace > spaceAvailable) {
                     // Add each character
                     for (char c : word.toCharArray()) {
                         int charSpace = getStringWidth(String.valueOf(c));
                         if (charSpace > spaceRemaining) {
                             lines.add(currentLine);
                             currentLine = "";
                             spaceRemaining = spaceAvailable;
                         }
                         currentLine += c;
                         spaceRemaining -= charSpace;
                     }
 
                     // Prepare for the next word
                     if (spaceSpace > spaceRemaining) {
                         lines.add(currentLine);
                         currentLine = "";
                         spaceRemaining = spaceAvailable;
                     } else {
                         currentLine += " ";
                         spaceRemaining -= spaceSpace;
                     }
 
                     continue;
                 } else {
                     // Create a new line
                     lines.add(currentLine);
                     currentLine = "";
                     spaceRemaining = spaceAvailable;
                 }
             }
 
             // Add the word
             currentLine += word;
             spaceRemaining -= wordSpace;
 
             // Prepare for the next word
             if (spaceSpace > spaceRemaining) {
                 lines.add(currentLine);
                 currentLine = "";
                 spaceRemaining = spaceAvailable;
             } else {
                 currentLine += " ";
                 spaceRemaining -= spaceSpace;
             }
         }
 
         // Add the last line if it's not blank
         if (!currentLine.isEmpty()) {
             lines.add(currentLine);
         }
 
         return lines;
     }
 }
