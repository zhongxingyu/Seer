 package net.sourcewalker.picrename;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FileNameFilter {
 
     private static final FileNameFilter[] FILTERS;
 
     static {
         FILTERS = new FileNameFilter[] { new FileNameFilter("^\\d+$", ""),
                 new FileNameFilter("^DSC\\d+$", ""),
                 new FileNameFilter("^IMG_\\d+$", ""),
                new FileNameFilter("([^_]+_)?\\d{3}(_|$)", "") };
     }
 
     public static String filterName(String name) {
         for (FileNameFilter filter : FILTERS) {
             name = filter.apply(name);
         }
         return name;
     }
 
     private Pattern pattern;
     private String replacement;
 
     public FileNameFilter(String pattern, String replacement) {
         this.pattern = Pattern.compile(pattern);
         this.replacement = replacement;
     }
 
     public String apply(String input) {
         Matcher match = pattern.matcher(input);
         if (match.find()) {
             return match.replaceAll(replacement);
         } else {
             return input;
         }
     }
 
     @Override
     public String toString() {
         return String.format("Filter: %s -> %s", pattern, replacement);
     }
 
 }
