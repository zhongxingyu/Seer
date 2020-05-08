 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package componentprogramming;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import componentprogramming.Utils.*;
 import java.util.Vector;
 
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
         //this.Text = Text;
     }
     
     private Vector<Utils.Identifier> getIdentifiers(Vector<Utils.Identifier> v1, 
             Vector<Utils.Identifier> v2) {
         Vector<Utils.Identifier> res = new Vector<Utils.Identifier>();
         if (v1.isEmpty()) {
             for (Utils.Identifier iden : v2) {
                 Vector<Utils.Identifier> vect = new Vector<Utils.Identifier>();
                 //if (!vect.contains(identifier))
                 //if (!vect.contains(iden))
                     vect.add(iden);
                 // GET hash for hash & put it in res
                 res.add(rules.getKey(vect));
             }
         }
            
         for (Utils.Identifier identifier : v1) {
             for (Utils.Identifier iden : v2) {
                 Vector<Utils.Identifier> vect = new Vector<Utils.Identifier>();
                 //if (!vect.contains(identifier))
                     vect.add(identifier);
                 //if (!vect.contains(iden))
                     vect.add(iden);
                 // GET hash for hash & put it in res
                 res.add(rules.getKey(vect));
             }
         }
         return res;
     }
     
     public boolean parse(String string, Utils.RulesSet rules) {
         int n = 0;
         this.rules = rules;
         LinkedList<LinkedList<Vector<Utils.Identifier>> > Table  
                 = new LinkedList<LinkedList<Vector<Utils.Identifier>>>();
         
         LinkedList<Vector<Utils.Identifier>> row = 
                     new LinkedList<Vector<Utils.Identifier>>();
         CompoLexical lex = new CompoLexical(string);
         while(lex.nextToken() != null) {
             // GET Gramer for input and put it in hash
             Vector<Identifier> vect = rules.getMultiKey(new Identifier(lex.currentToken()));
             row.add(vect);
             n++;
         }
         // Add Linked list to Table
         Table.add((LinkedList<Vector<Identifier>>)row.clone());
         
        row.clear();
         Vector<Utils.Identifier> V = new Vector<Utils.Identifier>();
         for (int j=1; j<n; j++) {
             for (int i=0; i<n-j; i++) {
                 V.clear();
                 for (int k=0; k<j; k++) {
                     // GET String Gramer and put it in Set
                     LinkedList<Vector<Utils.Identifier>> FirstList = Table.get(k);
                     LinkedList<Vector<Utils.Identifier>> SecondList = Table.get(j-k-1);
                     Vector<Utils.Identifier> h1 = FirstList.get(i);
                     Vector<Utils.Identifier> h2 = SecondList.get(i+k+1);
                     for (Utils.Identifier Identifier :getIdentifiers(h1, h2)) {
                         if (Identifier != null)
                             if (!V.contains(Identifier))
                                 V.add(Identifier);
                     }
                 }
                 row.add((Vector<Identifier>) V.clone());   
             }
             Table.add((LinkedList<Vector<Identifier>>)row.clone());
             //TODO do somehting
         }
         if (Table.getLast().getLast().contains(rules.getStart()))
             return true;
         else
             return false;
     }
     
     private RulesSet rules;
 }
