 package com.laytonsmith.abstraction;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * All supported color values for chat
  */
 public enum MCChatColor {
 
     /**
      * Represents black
      */
     BLACK(0x0),
     /**
      * Represents dark blue
      */
     DARK_BLUE(0x1),
     /**
      * Represents dark green
      */
     DARK_GREEN(0x2),
     /**
      * Represents dark blue (aqua)
      */
     DARK_AQUA(0x3),
     /**
      * Represents dark red
      */
     DARK_RED(0x4),
     /**
      * Represents dark purple
      */
     DARK_PURPLE(0x5),
     /**
      * Represents gold
      */
     GOLD(0x6),
     /**
      * Represents gray
      */
     GRAY(0x7),
     /**
      * Represents dark gray
      */
     DARK_GRAY(0x8),
     /**
      * Represents blue
      */
     BLUE(0x9),
     /**
      * Represents green
      */
     GREEN(0xA),
     /**
      * Represents aqua
      */
     AQUA(0xB),
     /**
      * Represents red
      */
     RED(0xC),
     /**
      * Represents light purple
      */
     LIGHT_PURPLE(0xD),
     /**
      * Represents yellow
      */
     YELLOW(0xE),
     /**
      * Represents white
      */
     WHITE(0xF),
     
     //Styles
     /**
      * Represents the random style
      */
     RANDOM('k'),
     
     /**
      * Represents the bold style
      */
     BOLD('l'),
     
     /**
      * Represents the strikethrough style
      */
     STRIKETHROUGH('m'),
     
     /**
      * Represents the underline style
      */
     UNDERLINE('n'),
     
     /**
      * Represents the italic style
      */
     ITALIC('o'),
     
     /**
      * Represents the plain white style
      */
     PLAIN_WHITE('r');
 
     private final char code;
     private final static Map<Integer, MCChatColor> colors = new HashMap<Integer, MCChatColor>();
     private final static Map<Character, MCChatColor> charColors = new HashMap<Character, MCChatColor>();
 
     private MCChatColor(char code){
         this.code = code;
     }
     private MCChatColor(int code) {
         this.code = Integer.toHexString(code).toLowerCase().charAt(0);
     }
 
     /**
      * Gets the data value associated with this color
      *
      * @return An integer value of this color code
      * @deprecated Use getChar in favor of this method
      */
     public int getCode() {
         return code;
     }
     
     public char getChar(){
         return code;
     }
 
     @Override
     public String toString() {
        return String.format("\u00A7%s", code);
     }
 
     /**
      * Gets the color represented by the specified color code
      *
      * @param code Code to check
      * @return Associative {@link com.laytonsmith.abstraction.MCChatColor} with the given code, or null if it doesn't exist
      * @deprecated This should not be used, in favor of the char lookup
      */
     public static MCChatColor getByCode(final int code) {
         return colors.get(code);
     }
     
     public static MCChatColor getByChar(char code){
         return charColors.get(Character.valueOf(code));
     }
     
 
     /**
      * Strips the given message of all color codes
      *
      * @param input String to strip of color
      * @return A copy of the input string, without any coloring
      */
     public static String stripColor(final String input) {
         if (input == null) {
             return null;
         }
 
         return input.replaceAll("(?i)\u00A7[0-9A-F]", "");
     }
 
     static {
         for (MCChatColor color : MCChatColor.values()) {
             colors.put(color.getCode(), color);
             charColors.put(color.getChar(), color);
         }
     }
 }
