 package playacem.allrondism.lib;
 
 import net.minecraft.util.EnumChatFormatting;
 
 public class Text {
 
     // Colors
     public static final String COLOR_BLACK = "\u00A70";
     public static final String COLOR_DARK_BLUE = "\u00A71";
     public static final String COLOR_DARK_GREEN = "\u00A72";
     public static final String COLOR_DARK_AQUA = "\u00A73";
     public static final String COLOR_DARK_RED = "\u00A74";
     public static final String COLOR_PURPLE = "\u00A75";
     public static final String COLOR_ORANGE = "\u00A76";
     public static final String COLOR_LIGHT_GREY = "\u00A77";
     public static final String COLOR_DARK_GREY = "\u00A78";
     public static final String COLOR_BLUE = "\u00A79";
     public static final String COLOR_LIGHT_GREEN = "\u00A7a";
     public static final String COLOR_AQUA = "\u00A7b";
     public static final String COLOR_LIGHT_RED = "\u00A7c";
     public static final String COLOR_PINK = "\u00A7d";
     public static final String COLOR_YELLOW = "\u00A7e";
     public static final String COLOR_WHITE = "\u00A7f";
     
     // Text modifier
     public static final String RESET = "\u00A7r";
     public static final String BOLD = EnumChatFormatting.BOLD.toString();
     public static final String ITALIC = EnumChatFormatting.ITALIC.toString();
     public static final String UNDERLINE = EnumChatFormatting.UNDERLINE.toString();
     public static final String STRIKETHROUGH = EnumChatFormatting.STRIKETHROUGH.toString();
     public static final String OBFUSCATED = EnumChatFormatting.OBFUSCATED.toString();
     
     /** Adds a given color to the given text */
     public static String add(String text, String color) {
        return color + text + COLOR_LIGHT_GREY;
     }
     
     /** Writes the given String <b>bold</b> */
     public static String bold(String text) {
        return BOLD + text + COLOR_LIGHT_GREY;
     }
     
     /** Writes the given String <i>italic</i> */
     public static String italic(String text) {
        return ITALIC + text + COLOR_LIGHT_GREY;
     }
     
     /** Writes the given String <u>underlined</u> */
     public static String underline(String text) {
        return UNDERLINE + text + COLOR_LIGHT_GREY;
     }
     
     /** Writes the given String <s>striked</s> */
     public static String strike(String text) {
        return STRIKETHROUGH + text + COLOR_LIGHT_GREY;
     }
     
     /** Writes the given String obfuscated */
    public static String obfuscate(String text) {
        return OBFUSCATED + text + COLOR_LIGHT_GREY;
     }
     
     /** Writes the given String <b>bold</b> */
     public static String b(String s) { return bold(s); }
     
     /** Writes the given String <i>italic</i> */
     public static String i(String s) { return italic(s); }
     
     /** Writes the given String <u>underlined</u> */
     public static String u(String s) { return underline(s); }
     
     /** Writes the given String <s>striked</s> */
     public static String s(String s) { return strike(s); }
     
     /** Writes the given String obfuscated */
    public static String o(String s) { return obfuscate(s); }
     
 }
