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
     
     public static Vector<Identifier> _st(typeToken t){
         return _s(_t(t),"");
     }
     
     private static Vector<Identifier> empty = new Vector<Identifier>();
     public static void main(String[] args) {
        
         RulesSet rules = new RulesSet();
         rules.addRule("S", _s("S", "S5"),_s("S1","S2"));
         rules.addRule("S5", _s("AMP","S"));        
         rules.addRule("S", _s("S","S"),_s("S4","CAB"));
         rules.addRule("S4", _s("OAB", "S"));
         rules.addRule("S5", _s("AMP","S"));
         rules.addRule("S1", _s("OB","N"));
         rules.addRule("S2", _s("COM","S3"));
         rules.addRule("S3", _s("N","CB"));
         rules.addRule("OB", _st(typeToken.openTok_brace));
         rules.addRule("CB", _st(typeToken.closeTok_brace));
         rules.addRule("COM", _st(typeToken.comma));
         rules.addRule("N", _st(typeToken.Num));
         rules.addRule("AMP", _st(typeToken.ParallelSign));
         rules.addRule("CAB", _st(typeToken.closeBrace));
         rules.addRule("OAB", _st(typeToken.openBrace));
         CompoParser cp = new CompoParser(rules);
        CompoParser.Comp res_comp = cp.getOverallComp("(<3,4>&<4,5>)<9,2>");
         if (res_comp != null)
             System.out.println(String.format("valid, overall component is %s .", res_comp.toString()));
         else 
             System.out.println("not valid.");
         
  
         //"<3,4><4,3>"
   
         // the other way
         //Boolean res = cp.check("(<3,4><4,4>)(<4,2>)");
         //System.out.println(res.toString());
         //System.out.println(cp.checkValidate());
     }
 }
