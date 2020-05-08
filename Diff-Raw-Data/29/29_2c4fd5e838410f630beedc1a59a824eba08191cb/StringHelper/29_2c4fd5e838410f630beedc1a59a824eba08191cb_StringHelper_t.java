 package net.betterverse.chatmanager.util;
 
 public class StringHelper {
 
     public static String parseColors(String parse) {
         return parse.replace('&', '§');
     }

    public static String stripColors(String parse) {
        for (int i = 0; i < parse.length(); i++) {
            char check = parse.charAt(i);
            if (check == '&') {
                parse = parse.replaceFirst("&" + parse.charAt(i + 1), "");
            }
        }

        return parse;
    }
 }
