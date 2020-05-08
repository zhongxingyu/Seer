 package models.user;
 
 import java.text.Normalizer;
 
 import java.lang.Character;
 import java.lang.CharSequence;
 import java.lang.StringBuilder;
 import java.lang.UnsupportedOperationException;
 import java.lang.RuntimeException;
 
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Calendar;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import com.avaje.ebean.Ebean;
 
 import models.dbentities.UserModel;
 
 /**
  * This class generates Bebras User Identifiers based on the provided user data.
  * It takes mostly the name into account, but other data might be used in case
  * of clashes.
  * @author Felix Van der Jeugt
  */
 public class IDGenerator {
 
     /** The size of the generated usernames. Eight should be plenty. */
     public static final int SIZE = 10;
 
     /* The characters that can seperate two names. */
     private static Set<Character> seperators = new TreeSet<Character>(
         Arrays.asList(' ', '-')
     );
 
     /**
      * Generates a new Identifier based on the user's real name and birthday.
      * @param name The user's real name
      * @param birthday The user's day of birth
      * @return The user's new and unique Bebras ID.
      */
     public static String generate(String name, Calendar birthday) {
         name = cleanName(name);
         UserName generator;
         String id;
 
         generator = new UserName(name);
         do id = generator.next();
             while(taken(id) && generator.hasNext());
         if(! taken(id)) return id;
 
         generator = new UserName(name);
         do id = generator.next() + (birthday.get(Calendar.YEAR) % 100);
             while(taken(id) && generator.hasNext());
         if(! taken(id)) return id;
 
         generator = new UserName(name);
         do id = generator.next() + (birthday.get(Calendar.DATE));
             while(taken(id) && generator.hasNext());
         if(! taken(id)) return id;
 
         generator = new UserName(name);
         do id = generator.next() + (birthday.get(Calendar.MONTH) + 1);
             while(taken(id) && generator.hasNext());
         if(! taken(id)) return id;
 
         generator = new UserName(name);
         int i = 0;
         while(true) {
             do id = generator.next() + i;
                 while(taken(id) && generator.hasNext());
             if(! taken(id)) return id;
             i++;
         }
     }
 
     public static String cleanName(String name) {
         CharSequence comp = Normalizer.normalize(name, Normalizer.Form.NFKD);
         StringBuilder builder = new StringBuilder();
         for(int i = 0; i < comp.length(); i++) {
             if('A' <= comp.charAt(i) && comp.charAt(i) <= 'Z'
             || 'a' <= comp.charAt(i) && comp.charAt(i) <= 'z') {
                 builder.append(comp.charAt(i));
             }
             if(seperators.contains(comp.charAt(i))) builder.append(' ');
         }
         return builder.toString().toLowerCase();
     }
 
     private static boolean taken(String id) {
         UserModel model = null;
         try {
             model = Ebean.find(UserModel.class, id);
         } catch(Exception e) {}
         return model != null;
     }
 
     private static class UserName implements Iterator<String> {
         private String[] parts;
         private int[] lengths;
         private boolean nextReady;
         private String next;
         public UserName(String cleanName) {
             parts = cleanName.split(" ");
             lengths = new int[parts.length - 1];
             for(int i = 0; i < lengths.length; i++) lengths[i] = 1;
             next = makeName();
             nextReady = true;
         }
         @Override public boolean hasNext() {
             if(nextReady) return true;
             int sum = 0;
             for(int i = 0; i < lengths.length; i++) sum += lengths[i];
            return sum < SIZE - 1;
         }
         @Override public void remove() {
             throw new UnsupportedOperationException();
         }
         @Override public String next() {
             if(nextReady) {
                 nextReady = false;
                 return next;
             }
             int i = 0;
             while(i < lengths.length) {
                 if(lengths[i] < parts[i].length()) {
                     lengths[i]++;
                     return makeName();
                 }
                 i++;
             }
             throw new NoSuchElementException("I ran out of names.");
         }
         private String makeName() {
             String name = "";
             int i;
             for(i = 0; i < lengths.length; i++) {
                 name = name + parts[i].substring(0, lengths[i]);
             }
             if(SIZE - name.length() > 0) {
                 if(parts[i].length() > SIZE - name.length()) {
                     name = name + parts[i].substring(0, SIZE - name.length());
                 } else {
                     name = name + parts[i];
                 }
             }
             return name;
         }
     }
 
 }
