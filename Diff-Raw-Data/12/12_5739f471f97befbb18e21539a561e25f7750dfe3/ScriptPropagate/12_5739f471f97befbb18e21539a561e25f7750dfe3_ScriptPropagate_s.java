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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author thiago
  */
 public class ScriptPropagate {
     public static List<String> lst = new ArrayList<String>();
     public static List<String> hierarchy_first = new ArrayList<String>();
     public static List<String> hierarchy_second = new ArrayList<String>();
 
     public static HashMap<String, List<String>> paths = new HashMap<String,List<String>>();
     public static void addPath(String prop, String path) {
         List<String> lst;
         if (paths.containsKey(prop)) {
             lst = paths.get(prop);
         } else {
             lst  = new ArrayList<String>();
         }
         lst.add(path);
         paths.put(prop, lst);
     }
 
     public static void logPaths() {
         String out = "====================================\n";
         for (Map.Entry<String, List<String>> entry : paths.entrySet()) {
             out += ("[paths for]: " + entry.getKey() + "\n");
             for (String s : entry.getValue()) {
                 out += "* " + s;
             }
             out += "\n-----------------------------\n";
         }
 
         out += "=======================================\n\n";
         dumpPaths(out);
         paths = new HashMap<String,List<String>>();
     }
 
     public static void logPropagateError(Symbol.MethodSymbol m, Symbol.MethodSymbol overrided, Type.ClassType ct) {
         Symbol.ClassSymbol mcs = (Symbol.ClassSymbol) m.owner;
         Symbol.ClassSymbol cs = (Symbol.ClassSymbol) overrided.owner;
 
         if (((Type.ClassType)mcs.type).supertype_field.tsym != cs.type.tsym) {
            throw new RuntimeException("Relationship between overrided/overriden unknown");
         }
 
         String first = "{*" + mcs.fullname + "::" + m + "*}";
         String second = "{*"+ cs.fullname + "::" + overrided + "*}";
         String s = first + " " + second + " should throw {*"+ct.tsym+"*}";
 
         for (String f : hierarchy_first) {
             if (f.equals(first)) return;
         }
         for (String sec : hierarchy_second) {
             if (sec.equals(second)) return;
         }
 
         hierarchy_first.add(first);
         hierarchy_second.add(second);
         execute(s);
     }
 
     public static void logPropagateError(PendingExit exit, JCMethodDecl tree) {
         String s = "[*"+tree.sym.owner.toString()+"::" + tree.sym.toString()
                 + "*] should throw [*" + exit.thrown.toString()
                 + "*] because it calls [*";
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
                     throw new RuntimeException("++BUG!! ScriptPropagate::logPropagateError");
             }
             s += "*]";
         } else {
             s += "ORIGIN*]";
         }
         execute(s);
     }
 
     static private void dumpPaths(String s) {
         //System.out.println(s);
         try {
             String[] cmd = {"pjavac-log-paths", s};
 
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
 
     static private void execute(String s) {
         for (String old : lst) {
             if(old.equals(s)) return;
         }
         lst.add(s);
 
         //System.out.println(s);
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
