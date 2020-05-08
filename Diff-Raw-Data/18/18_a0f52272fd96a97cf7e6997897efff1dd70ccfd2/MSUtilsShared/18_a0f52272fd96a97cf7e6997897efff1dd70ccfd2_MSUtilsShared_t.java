 package uk.org.smithfamily.mslogger.ecuDef;
 
 import java.util.List;
 /**
  *
  */
 public class MSUtilsShared
 {
     static String digits = "0123456789abcdef";
     
     /**
      * Method that return the width and height of an array from it's shape (from the INI)
      * 
      * @param shape Shape defined in the INI
      * @return width and height
      */
     public static int[] getArraySize(String shape)
     {
         String arraySpec = shape.replace("[", "").replace("]", "");
         String[] sizes = arraySpec.split("x");
         int width = Integer.parseInt(sizes[0].trim());
         int height = sizes.length == 2 ? Integer.parseInt(sizes[1].trim()) : -1;
         
        int[] size = { width, height };
         
         return size;
     }
     
     /**
      * Used with pageReadCommand, pageValueWrite and pageChunkWrite to translate the ini command to a command the MegaSquirt ECU will understand
      * 
      * @param listPageCommand The list of page command
      * @param stringToConvert The page command to translate
      * @param offset The offset (often represented by "%o" in the page command)
      * @param count The count (often represented by "%c" in the page command)
      * @param value The value(s) (often represented by "%v" in the page command)
      * @param pageNo The page number
      * 
      * @return The command to send to the MegaSquirt with the place holder replaced
      */
     public static String HexStringToBytes(List<String> listPageCommand, String stringToConvert, int offset, int count, int[] value, int pageNo)
     {
         String ret = "";
         boolean first = true;
         stringToConvert = stringToConvert.replace("$tsCanId", "x00");
         for (int positionInString = 0; positionInString < stringToConvert.length(); positionInString++)
         {
             if (!first)
             {
                 ret += ",";
             }
             
             char currentCharacter = stringToConvert.charAt(positionInString);
             switch (currentCharacter)
             {
             case '\\':
                 positionInString++;
                 currentCharacter = stringToConvert.charAt(positionInString);
                 if (currentCharacter == '0')
                 {
                     ret += OctalByteToDec(stringToConvert.substring(positionInString));
                 }
                 else
                 {
                     ret += HexByteToDec(stringToConvert.substring(positionInString));
                 }
                 positionInString = positionInString + 2;
                 break;
 
             case '%':
                 positionInString++;
                 currentCharacter = stringToConvert.charAt(positionInString);
 
                 if (currentCharacter == '2')
                 {
                     positionInString++;
                     currentCharacter = stringToConvert.charAt(positionInString);
                     if (currentCharacter == 'o')
                     {
                         ret += bytes(offset);
                     }
                     else if (currentCharacter == 'c')
                     {
                         ret += bytes(count);
                     }
                     else if (currentCharacter == 'i')
                     {
                         String identifier = listPageCommand.get(pageNo - 1);
     
                         ret += HexStringToBytes(listPageCommand, identifier, offset, count, value, pageNo);
                     }
                 }
                else if (currentCharacter == 'o')
                {
                    ret += bytes(offset);
                }
                else if (currentCharacter == 'c')
                {
                    ret += bytes(count);
                }
                 else if (currentCharacter == 'v')
                 {
                     // Loop over all the values we received
                     for (int i = 0; i < value.length; i++)
                     {
                         ret += bytes(value[i]) + ",";
                     }
                 }
                 
                 break;
 
             default:
                 ret += Byte.toString((byte) currentCharacter);
                 break;
             }
             first = false;
         }
         return ret;
     }
     
     /**
      * Convert a string from hexadecimal to decimal
      * 
      * @param s String containing hexadecimal
      * @return The decimal in integer type
      */
     public static int HexByteToDec(String s)
     {
         int i = 0;
         char c = s.charAt(i++);
         assert c == 'x';
         c = s.charAt(i++);
         c = Character.toLowerCase(c);
         int val = 0;
         int digit = digits.indexOf(c);
         val = digit * 16;
         c = s.charAt(i++);
         c = Character.toLowerCase(c);
         digit = digits.indexOf(c);
         val = val + digit;
         return val;
     }
     
     /**
      * Convert a string from octal to decimal
      * 
      * @param s String containing octal
      * @return The decimal in integer type
      */
     public static int OctalByteToDec(String s)
     {
         int i = 0;
         char c = s.charAt(i++);
         assert c == '0';
         c = s.charAt(i++);
         c = Character.toLowerCase(c);
         int val = 0;
         int digit = digits.indexOf(c);
         val = digit * 8;
         c = s.charAt(i++);
         c = Character.toLowerCase(c);
         digit = digits.indexOf(c);
         val = val + digit;
         return val;
     }
     
     /**
      * Take a value and return its hi, low representation
      * 
      * @param val Value to convert
      * @return hi, low representation
      */
     private static String bytes(int val)
     {
         int hi = val / 256;
         int low = val % 256;
         if (hi > 127)
             hi -= 256;
         if (low > 127)
             low -= 256;
         
         return "" + hi + "," + low;
     }
 }
