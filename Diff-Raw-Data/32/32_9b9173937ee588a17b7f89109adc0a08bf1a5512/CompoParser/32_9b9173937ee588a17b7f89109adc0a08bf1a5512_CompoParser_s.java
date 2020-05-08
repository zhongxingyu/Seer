 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package componentprogramming;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import componentprogramming.Utils.*;
 
 /**
  *
  * @author mhdaljobory
  */
 public class CompoParser {
     
     class MySet {
         private HashSet<Utils.Identifier> set;
 
         /**
          * @return the set
          */
         public HashSet<Utils.Identifier> getSet() {
             return set;
         }
 
         /**
          * @param set the set to set
          */
         public void setSet(HashSet<Utils.Identifier> set) {
             this.set = set;
         }
     }
     
     public CompoParser(String Text) {
         this.Text = Text;
     }
     
     private HashSet<Utils.Identifier> getIdentifiers(HashSet<Utils.Identifier> h1, 
             HashSet<Utils.Identifier> h2) {
         HashSet<Utils.Identifier> res = new HashSet<Utils.Identifier>();
         for (Utils.Identifier identifier : h1) {
             for (Utils.Identifier iden : h2) {
                 HashSet<Utils.Identifier> hash = new HashSet<Utils.Identifier>();
                 hash.add(iden);
                 hash.add(identifier);
                 res.add(rules.getKey(hash));
                // GET hash for hash put it in res
             }
         }
         return res;
     }
     
     public boolean parse(String string, Utils.RulesSet rules) {
         int n = Text.length();
         this.rules = rules;
        ArrayList<LinkedList<HashSet<Utils.Identifier>> > Table  
                = new ArrayList<LinkedList<HashSet<Utils.Identifier>>>();
         
         LinkedList<HashSet<Utils.Identifier>> row = 
                     new LinkedList<HashSet<Utils.Identifier>>();
         CompoLexical lex = new CompoLexical(string);
         while(!lex.end()) {
             lex.nextToken();
            // GET Gramer for input Text[i] put it in symbol
             HashSet<Identifier> hash = rules.getContents(new Identifier(lex.currentToken()));
            //hash.add(getKey);
             row.add(hash);
            
         }
         // Add Linked list to Table
         Table.add(row);
         
         row.clear();
         HashSet<Utils.Identifier> V = new HashSet<Utils.Identifier>();
         for (int j=1; j<n; j++) {
             for (int i=0; i<n-j+1; i++) {
                 V.clear();
                 for (int k=0; k<j-1; j++) {
                     // GET String Gramer and put it in Set
                    //V.add(string);
                    HashSet<Utils.Identifier> h1 = new HashSet<Utils.Identifier>();
                    HashSet<Utils.Identifier> h2 = new HashSet<Utils.Identifier>();
                     for (Utils.Identifier Identifier :getIdentifiers(h1, h2)) {
                         V.add(Identifier);
                     }
                 }
                 row.add(V);
             }
             Table.add(row);
         }
        //str is S
        //if (Table.getLast().contains(str))
             return true;
        //else
          //  return false;
     }
     private RulesSet rules;
     private String Text;
 }
