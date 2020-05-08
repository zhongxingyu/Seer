 package liana;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lia
  * Date: 24.09.13
  * Time: 23:01
  * To change this template use File | Settings | File Templates.
  */
 public class Query {
     public enum Type {
         ADD,
         DELETE,
         GET,
         UPDATE
     }
 
     private String key;
     private Type type;
     private Entry entry;
 
     public Query(Type type, String key, Entry entry) {
         this.key = key;
         this.type = type;
         this.entry = entry;
     }
 
     public String getKey() {
         return key;
     }
 
     public Type getType() {
         return type;
     }
 
     public Entry getEntry() {
         return entry;
     }
 
     public static Query parse(String line) {
         String[] tokens = line.split(" ", 3);
         Type queryType = null;
         for (Type type : Type.values()) {
             if (type.name().equalsIgnoreCase(tokens[0])) {
                 queryType = type;
             }
         }
         if (queryType == null) {
             throw new IllegalArgumentException("Wrong operation. Try use" +
                     " add, delete, update, get");
         }
        if (tokens.length < 2) {
            throw new IllegalArgumentException("Nickname is missing");
        }
         String queryKey = tokens[1];
         if (queryType == Type.UPDATE || queryType == Type.ADD) {
             if (tokens.length < 3) {
                 throw  new IllegalArgumentException("Entry is missing");
             }
             return new Query(queryType, queryKey, Entry.parse(tokens[2]));
         }
         if (tokens.length > 2) {
             throw new IllegalArgumentException("Too many arguments for " + queryType.name());
         }
         return new Query(queryType, queryKey, null);
 
     }
 }
