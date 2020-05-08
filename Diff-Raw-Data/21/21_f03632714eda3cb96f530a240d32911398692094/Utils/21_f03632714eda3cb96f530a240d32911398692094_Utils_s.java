 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package componentprogramming;
 import componentprogramming.CompoLexical.Token;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 /**
  *
  * @author mhdsyrwan
  */
 public class Utils {
     static class Identifier {
         private String name;
         private Token token = null;
         public Identifier(Token t) {
             this.token = t;
         }
         
         public Identifier(String name){
             this.name = name;
         }
         boolean isToken() {
             return (getToken() != null);
         }
 
         @Override
         public String toString() {
             if (isToken())
                 return getToken().toString();
             else 
                 return name;
         }
 
         /**
          * @return the token
          */
         public Token getToken() {
             return token;
         }
 
         @Override
         public boolean equals(Object o) {
             if (isToken())
                 return token.equals(((Identifier)o).token);
             else
                 return name.equals(name);
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
             return hash;
         }
         
         
     }
     static class RulesSet {
        private Hashtable<Identifier,HashSet<Identifier> > rules;
         private boolean first_time = false;
         private Identifier start = null;
         public RulesSet(){
            rules = new Hashtable<Identifier, HashSet<Identifier>>();
         }
         
        public void addRule(Identifier key, HashSet<Identifier> contents) {
             if (first_time) {
                 start = key;
                 first_time = false;
             }
             rules.put(key, contents);
         }
         
        public HashSet<Identifier> getContents(Identifier key) {
            return rules.get(key);
        }
         
         public Identifier getKey(HashSet<Identifier> contents){
             Identifier wanted_key = null;
            for (Entry<Identifier,HashSet<Identifier> > i : rules.entrySet()) {
                if (i.getValue().equals(contents)){
                    wanted_key = i.getKey();
                 }
             }
             
             return wanted_key;
             
         }
         
         public Identifier getStart() {
             return start;
         }
     }
 }
