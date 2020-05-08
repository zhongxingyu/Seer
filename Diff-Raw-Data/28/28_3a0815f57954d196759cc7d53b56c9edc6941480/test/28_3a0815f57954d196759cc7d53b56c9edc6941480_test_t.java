 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package componentprogramming;
 import componentprogramming.Utils.*;
 import java.util.HashSet;
 import componentprogramming.CompoLexical.*;
 import java.util.Vector;
 /**
  *
  * @author mhdaljobory
  */
 public class test {
     public static Vector<Identifier> _s(String s1, String s2){
             return _s(new Identifier(s1), new Identifier(s2));
     }
     public static Vector<Identifier> _s(String s1, Identifier t2){
         return _s(new Identifier(s1), t2);
     }
     
     public static Vector<Identifier> _s(Identifier t1, String s2){
         return _s(t1, new Identifier(s2));
     }
     
     public static Vector<Identifier> _s(Identifier t1, Identifier t2){
         Vector<Identifier> hash = new Vector<Identifier>();
         if (!t1.toString().equals(""))
             hash.add(t1);
         if (!t2.toString().equals(""))
             hash.add(t2);
         return hash;
     }
     public static Identifier _t(String s){
         return new Identifier(new Token(s));
     }
     
     public static Identifier _t(typeToken t){
         return new Identifier(new Token(t));
     }
     private static Vector<Identifier> empty = new Vector<Identifier>();
     public static void main(String[] args) {
         RulesSet rules = new RulesSet();
         //rules.addRule("S", getSet(new Identifier(new Token("<")), ""), getSet(new Identifier(new Token(">")), ""), empty);
        rules.addRule("S", _s("S", "S5"),_s("C",""));
        rules.addRule("S5", _s("AMB","C"));
        rules.addRule("C", _s("S1","S2"));
        rules.addRule("S1", _s(_t("<"),_t(typeToken.Num)));
        rules.addRule("S2", _s(_t(typeToken.comma),"S3"));
        rules.addRule("S3", _s(_t(typeToken.Num),_t(typeToken.closeTok_brace)));
         CompoParser cp = new CompoParser("<3,4>");
         Boolean res = cp.parse(rules);
         System.out.println(res.toString());
     }
 }
