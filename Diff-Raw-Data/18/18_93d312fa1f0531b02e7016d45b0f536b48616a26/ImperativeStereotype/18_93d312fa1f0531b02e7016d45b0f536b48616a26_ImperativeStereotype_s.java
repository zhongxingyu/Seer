 package example;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 public enum ImperativeStereotype {
     australia("a person from australia", "aussie", "legend"),
     newzealand("a person from new zealand", "kiwi"),
     preston("a person from preston", "scally"),
     liverpool("a person from liverpool", "scouser", "thief"),
     manchester("a person from manchester", "manc", "mancunian", "fighter");
 
     private String description;
     private List<String> aliases;
 
     ImperativeStereotype(String description, String... aliases) {
         this.aliases = Arrays.asList(aliases);
         this.description = description;
     }
 
     public static ImperativeStereotype find(String name) throws IllegalArgumentException {
        List<String> validStereotypes = new ArrayList<String>();
         try {
             return ImperativeStereotype.valueOf(name);
         } catch (IllegalArgumentException e) {
             for (ImperativeStereotype stereotype : ImperativeStereotype.values()) {
                 validStereotypes.add(stereotype.name());
                 validStereotypes.add(stereotype.description);
                 if(stereotype.description.equals(name)){
                     return stereotype;
                 }
                 for (String alternativeName : stereotype.aliases) {
                     validStereotypes.add(alternativeName);
                     if (alternativeName.equalsIgnoreCase(name)) return stereotype;
                 }
             }
             Collections.sort(validStereotypes);
            throw new IllegalArgumentException(("Invalid Portal [" + name + "]. Must be one of [" + join(validStereotypes) + "]"));
         }
     }
 
    private static String join(List<String> stereotypes) {
         final StringBuilder result = new StringBuilder();
         for (String stereotype : stereotypes) {
             result.append(stereotype);
             if((stereotypes.size() > 1) && (!stereotype.equals(stereotypes.get(stereotypes.size()-1)))) {
                result.append(", ");
             }
         }
         return result.toString();
     }
 }
