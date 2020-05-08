 package nfo.data;
 
 /**
  * Profile manipulator, creates nfo from a template ( profile ) and a content
  *
  * @author Rio Alexandre
  * @version 1.0
  */
 public abstract class ProfileCreator {
 
   /** Margin between the border and the first character of the body. */
   private static final int BORDER_CONTENT_MARGIN = 2;
 
   /**
    * Create a nfo from a profile, the content is still in the profile.
    *
    * @param profile Profile used as a template
    * @return String containing the whole nfo
    */
   public static String create(Profile profile) {
     String[] headerRow;
     String[] borderRow;
     String[] bodyRow;
     String[] bodyRowRaw;
     String row;
     String border;
     int margin     = 0;
     int totalWidth = 0;
     int bodyWidth;
 
     String total = "";
 
     // Start to append text on text area.
     total += profile.getHeader();
     total += "\n\n";
     borderRow = profile.getBorder().split("\n");
 
     headerRow = profile.getHeader().split("\n");
     // Determine the longest line in the header
     for (int i=0; i<headerRow.length; i++)
       if (headerRow[i].length() > totalWidth)
         totalWidth = headerRow[i].length();
 
     // Determine the longest line in the border pattern
     for (int i=0; i<borderRow.length; i++)
       if (borderRow[i].length() > margin)
         margin = borderRow[i].length();
 
     // TODO adapt if the border is only on one side
     bodyWidth = totalWidth - 2*margin - 2*BORDER_CONTENT_MARGIN;
 
     bodyRow = profile.getBody().split("\n");
     for (int i=0; i<bodyRow.length; i++) {
       //if ()
     }
 
    // TODO if bodyRow[i].length() > space left in line it will fail
     for (int i=0; i<bodyRow.length; i++) {
       // TODO if the modulo is ≠ 0 it will print a non-complete border pattern, not cool bro !
       border = borderRow[i%borderRow.length];
       row = border;
       row = appendSpaces(row, margin - border.length() + BORDER_CONTENT_MARGIN);
       row += bodyRow[i]; // TODO
       row = appendSpaces(row, totalWidth - 2*margin - row.length());
       //row = this.appendSpaces(row, margin - border.length());
       row += border;
       row += "\n";
       total += row;
     }
     total += "\n" + profile.getFooter();
 
     return total;
   }
 
   /**
    * Append spaces at the end of a string.
    *
    * @param string String to add spaces.
    * @param num Number of spaces to add.
    * @return The new formed String.
    */
   private static String appendSpaces(String string, int num) {
     if (string == null)
       string = "";
     for (int i=0; i<num; i++)
       string += " ";
     return string;
   }
 
 }
