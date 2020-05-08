 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.sun.tools.javac.comp;
 
 
 import com.sun.tools.javac.code.Type;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.comp.Flow.PendingExit;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
 import com.sun.tools.javac.tree.JCTree.JCIdent;
 import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
 import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 /**
  *
  * @author thiago
  */
 public class ScriptPropagate {
     public static void logPropagateError(Symbol.MethodSymbol m, Symbol.MethodSymbol overrided, Type.ClassType ct) {
        String s = "{!";
         Symbol.ClassSymbol mcs = (Symbol.ClassSymbol) m.owner;
         Symbol.ClassSymbol cs = (Symbol.ClassSymbol) overrided.owner;
        s += mcs.fullname + "::" + m + "!} {!"+ cs.fullname + "::" + overrided + "!} should throw {!"+ct.tsym+"!}";
        //System.out.println(s);
         execute(s);
     }
 
     public static void logPropagateError(PendingExit exit, JCMethodDecl tree) {
        String s = "[!"+tree.sym.owner.toString()+"::" + tree.sym.toString()
                + "!] should throw [!" + exit.thrown.toString()
                + "!] because it calls [!";
         //+ " because of " + f.selected.type.toString() + "::"+f.name + "["+f.type+"]");
         //+ " because of " + f.selected.type.toString() + "::" +f.sym;
         if (exit.tree.getTag() == JCTree.APPLY) {
             JCMethodInvocation method = (JCMethodInvocation)exit.tree;
             switch(method.meth.getTag()) {
                 case JCTree.SELECT:
                     JCFieldAccess f = (JCFieldAccess) method.meth;
                     Type.ClassType ct = (Type.ClassType)f.selected.type;
                     Symbol.ClassSymbol cs = (Symbol.ClassSymbol)ct.tsym;
                     s += cs.fullname + "::" + f.sym.toString();
                     break;
                 case JCTree.IDENT:
                     JCIdent i = (JCIdent) method.meth;
                     Symbol.ClassSymbol css = (Symbol.ClassSymbol) i.sym.owner;
                     s += css.fullname + "::" + i.sym;
                     break;
                 default:
                     System.err.println("++BUG!!");
             }
            s += "!]";
         } else {
            s += "ORIGIN!]";
         }
        //System.out.println(s);
         execute(s);
     }
 
     static private void execute(String s) {
         try {
             String[] cmd = {"pjavac-script", s};
 
             Runtime runtime = Runtime.getRuntime();
             Process process = runtime.exec(cmd);
             InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             String line;
             while ((line = br.readLine()) != null) {
                 System.out.println(line);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
