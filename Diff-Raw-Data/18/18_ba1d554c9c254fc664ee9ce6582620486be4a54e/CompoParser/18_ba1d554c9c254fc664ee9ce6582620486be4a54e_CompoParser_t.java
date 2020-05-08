 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package componentprogramming;
 
 
 import java.util.LinkedList;
 import componentprogramming.Utils.*;
 import componentprogramming.CompoLexical.*;
 import java.util.Vector;
 
 public class CompoParser {
     
     private RulesSet rules;
     private CompoLexical lex;
     
     public CompoParser(RulesSet rulesSet) {
         rules = rulesSet;
     }
     
     private boolean getInandOut(Integer in, Integer out) {
         boolean coma = false, parallel = false, openTok_brace = false, first = true;
         int outNum = 0, inNum = 0;
         in = out = 0;
 //        while(lex.nextToken() != null) {
             while(!lex.End()) {
                 lex.nextToken();
             Token token = lex.currentToken();
             if (token.getType() == typeToken.closeBrace) {
                 return true;
             }
             
             if (openTok_brace) {
                 inNum = token.getInt();
                 if (!parallel && inNum != outNum && !first)
                     return false;
                 else if (parallel) {
                     in += inNum;
                     out += outNum;
                 }
                 openTok_brace = false;
                 parallel = false;
                 outNum = inNum = 0;
                 first = false;
             }
             
             if (coma) {
                 outNum = token.getInt();
                 coma = false;
             }
             
             if (token.getType() == typeToken.comma)
                 coma = true;
             if (token.getType() == typeToken.ParallelSign)
                 parallel = true;
             if (token.getType() == typeToken.openTok_brace)
                 openTok_brace = true;
         }
         return false;
     }
     
     public boolean checkValidate() {
         boolean coma = false, parallel = false, openTok_brace = false, first = true;
         int outNum = 0, inNum = 0;
 //        while(lex.nextToken() != null) {
             while(!lex.End()) {
                 lex.nextToken();
             Token token = lex.currentToken();
             
             if (openTok_brace) {
                 inNum = token.getInt();
                 if (!parallel && inNum != outNum && !first)
                     return false;
                 openTok_brace = false;
                 parallel = false;
                 outNum = inNum = 0;
                 first = false;
             }
             
             if (coma) {
                 outNum = token.getInt();
                 coma = false;
             }
             
             if (token.getType() == typeToken.comma)
                 coma = true;
             if (token.getType() == typeToken.ParallelSign)
                 parallel = true;
             if (token.getType() == typeToken.openTok_brace)
                 openTok_brace = true;
             
         }
         return true;
     }
     
     
     public boolean check(String text) {
         return (getOverallComp(text) != null);
     }
     
     public Comp getOverallComp(String text) {
        System.out.println("started");
         if (!parse(text))
             return null;
        System.out.println("first passed");
         return (checkBuffer(readBuffer()));
     }
     
     private Vector<Token> readBuffer() {
         Vector<Token> buffer = new Vector<Token>();
         lex.reset();
 //        while (lex.nextToken() != null){
             while(!lex.End()) {
                 lex.nextToken();
             buffer.add(lex.currentToken());
         }
         return buffer;
     }
         private Comp checkBuffer(Vector<Token> buffer) {
         
         int n = 0;
         Comp comp = new Comp();
         Comp acc = new Comp();
         while(n < buffer.size()) {
             Token token = buffer.get(n);
             switch (token.getType()){
                 case Num:
                     if (comp.isEmpty()){
                         comp.i = token.getInt();
                         n += 2;
                         comp.j = buffer.get(n).getInt();
                     }
                     else {
                         if (comp.j == token.getInt()){
                             n += 2;
                             comp.j = buffer.get(n).getInt();
                         }else 
                             return null;
                     }
                     break;
                     
                 case ParallelSign:
                     acc.joinParallel(comp);
                     comp.clear();
                     break;
                     
                 case openBrace:
                     n++;
                     int balance = 1;
                     token = buffer.get(n);
                     Vector<Token> inner_buffer = new Vector<Token>();
                     while (balance != 0){
                         if (token.getType() == typeToken.openBrace)
                             balance++;
                         else if (token.getType() == typeToken.closeBrace)
                             balance--;
                         if (balance != 0) {
                             inner_buffer.add(token);
                             n++;
                             token = buffer.get(n);
                         }
                     }
                     Comp inner_comp = checkBuffer(inner_buffer);
                     if (inner_comp == null)
                         return null;
                     if (!comp.join(inner_comp))
                         return null;
                     break;
             }
             n++;
         }
         acc.joinParallel(comp);
         return acc;
     }
     public static class Comp {
         int i;
         int j;
         
         boolean isEmpty(){
             return  (i == 0 || j == 0);
         }
         void clear(){
             i = j = 0;
         }
         public Comp(){
             i = j = 0;
         }
         public Comp(int i , int j) {
             this.i = i;
             this.j = j;
         }
         
         Comp(Token a, Token b){
             i = a.getInt();
             j = b.getInt();
         }
         Comp(CompoLexical lex) {
             i = lex.currentToken().getInt();
             lex.nextToken();
             j = lex.currentToken().getInt();
         }
 
         public Comp(Vector<Token> v) {
             i = v.get(i).getInt();
             j = v.get(i+1).getInt();
         }
         
         void joinParallel(Comp other) {
             i += other.i;
             j += other.j;
         }
         
         boolean join(Comp other) {
             if (isEmpty()){
                 this.i = other.i;
                 this.j = other.j;
                 return true;
             }
             if (j == other.i){
                 j = other.j;
                 return true;
             }
             else
                 return false;
         }
                 
         Comp check(Comp other) {
             if (j == other.i)
                 return new Comp(i,other.j);
             else
                 return null;
         }
 
         @Override
         public String toString() {
             return String.format("<%d,%d>", i,j);
         }
         
     }
 
     private Vector<Utils.Identifier> getIdentifiers(Vector<Utils.Identifier> v1, 
             Vector<Utils.Identifier> v2) {
         Vector<Utils.Identifier> res = new Vector<Utils.Identifier>();
         if (v1.isEmpty()) {
             for (Utils.Identifier iden : v2) {
                 Vector<Utils.Identifier> vect = new Vector<Utils.Identifier>();
                 vect.add(iden);
                 res.add(rules.getKey(vect));
             }
         }
         else if (v2.isEmpty()){
             for (Utils.Identifier identifier : v1) {
                 Vector<Utils.Identifier> vect = new Vector<Utils.Identifier>();
                 vect.add(identifier);
                 res.add(rules.getKey(vect));
             }
         }
         for (Utils.Identifier identifier : v1) {
             for (Utils.Identifier iden : v2) {
                 Vector<Utils.Identifier> vect = new Vector<Utils.Identifier>();
                 vect.add(identifier);
                 vect.add(iden);
                 res.add(rules.getKey(vect));
             }
         }
         return res;
     }
     
     public boolean parse(String text) {
         int n = 0;
         lex = new CompoLexical(text);
         LinkedList<LinkedList<Vector<Utils.Identifier>> > Table  
                 = new LinkedList<LinkedList<Vector<Utils.Identifier>>>();
         
         LinkedList<Vector<Utils.Identifier>> row = 
                     new LinkedList<Vector<Utils.Identifier>>();
 //        while(lex.nextToken() != null) {
             while(!lex.End()) {
                 lex.nextToken();
             // GET Gramer for input and put it in hash
             Vector<Identifier> vect = rules.getMultiKey(new Identifier(lex.currentToken()));
             row.add(vect);
             n++;
         }
         // Add Linked list to Table
         Table.add((LinkedList<Vector<Identifier>>)row.clone());
         
         Vector<Utils.Identifier> V = new Vector<Utils.Identifier>();
         for (int j=1; j<n; j++) {
             row.clear();
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
         }
        return (Table.getLast().getLast().contains(rules.getStart()));
     }
 }
