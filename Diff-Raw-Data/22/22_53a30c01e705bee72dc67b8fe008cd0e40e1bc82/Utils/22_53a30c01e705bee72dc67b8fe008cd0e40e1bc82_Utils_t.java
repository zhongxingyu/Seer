 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package componentprogramming;
 import componentprogramming.CompoLexical.Token;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 import java.util.Vector;
 /**
  *
  * @author mhdsyrwan
  */
 public class Utils {
     static class Identifier {
         private String name = "";
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
                 return ((Identifier)o).name.equals(name);
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
             return hash;
         }
         
         
     }
     static class RulesSet {
         private Hashtable<Identifier,HashSet<Vector<Identifier> > > rules;
         private boolean first_time = true;
         private Identifier start = null;
         public RulesSet(){
             rules = new Hashtable<Identifier, HashSet<Vector<Identifier>>>();
         }
         
         
         public void addRule(String key ,Vector<Identifier> r1, Vector<Identifier> r2, Vector<Identifier> r3){
             if (first_time) {
                 start = new Identifier(key);
                 first_time = false;
             }
             HashSet<Vector<Identifier> > sets = new HashSet<Vector<Identifier>>();
             if (!r1.isEmpty())
                 sets.add(r1);
             if (!r2.isEmpty())
                 sets.add(r2);
             if (!r3.isEmpty())
                 sets.add(r3);
             rules.put(new Identifier(key), sets);
         }
         
         
         
         public void addRule(Identifier key, HashSet<Vector<Identifier> > contents) {
             if (first_time) {
                 start = key;
                 first_time = false;
             }
             rules.put(key, contents);
         }
         
         
         public Vector<Identifier> getMultiKey(Identifier identifier) {
             Vector<Identifier> hash = new Vector<Identifier>();
             for (Entry<Identifier, HashSet<Vector<Identifier> > > ii : rules.entrySet() ){
                 for (Vector<Identifier> i : ii.getValue()) {
                     if (i.contains(identifier)){
                         hash.add(ii.getKey());
                     }
                 }
             }
             
             return hash;
         }
         
         
         public Identifier getKey(Identifier identifier){
             // TODO S -> AB | a, A -> CDA | a, B -> C
             // content = a & HashSet = S, A
             Vector<Identifier> hash = new Vector<Identifier>();
             hash.add(identifier);
             return getKey(hash);
         }
        private boolean is_in(Vector<Identifier> parent, Vector<Identifier> child) {
            int pi = 0;
            int ci = 0;
            while (true){
                if (parent.get(pi).equals(child.get(ci))) {
                    ci++;
                    pi++;
                }else {
                    pi++;
                }
                if (ci == child.size()){
                    return true;
                }
                if (pi == parent.size()){
                    return false;
                }
            }
        }
         public Identifier getKey(Vector<Identifier> contents){
             // S -> AB | a, A -> CDA | a, B -> C
             // contents = AB
             // return S
             Identifier wanted_key = null;
             for (Entry<Identifier, HashSet<Vector<Identifier> > > ii : rules.entrySet() ){
                 for (Vector<Identifier> i : ii.getValue()) {
                    if (is_in(i, contents)){
                         wanted_key = ii.getKey();
                     }
                 }
             }
             
             return wanted_key;
             
         }
         
         public Identifier getStart() {
             return start;
         }
     }
 }
