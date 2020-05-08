 package de.skuzzle.polly.parsing.declarations;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 
 
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 import de.skuzzle.polly.parsing.util.CopyTool;
 
 public class Declarations implements Serializable {
     
     private static final long serialVersionUID = 1L;
     
     private LinkedList<Map<String, Declaration>> levels;
     private Map<String, Declaration> rootLevel;
 
 
     public Declarations() {
         this.levels = new LinkedList<Map<String, Declaration>>();
         this.enter();
         this.rootLevel = this.levels.getFirst();
     }
 
     
     
     public synchronized Set<Declaration> getDeclarations() {
         Set<Declaration> result = new HashSet<Declaration>();
         
         // iterate descending, so higher level declarations get added first
         Iterator<Map<String, Declaration>> it = this.levels.descendingIterator();
         while (it.hasNext()) {
             Map<String, Declaration> level = it.next();
             result.addAll(level.values());
         }
         return result;
     }
 
     
 
     public synchronized void enter() {
         this.levels.addFirst(new HashMap<String, Declaration>());
     }
 
 
 
     public synchronized void leave() {
         this.levels.removeFirst();
     }
 
 
 
     private synchronized void add(Map<String, Declaration> level, 
             Declaration declaration) {
         if (declaration.getType() == null) {
             throw new IllegalStateException(
                 "Can not store Declaration with no type: " + declaration);
         }
         level.put(declaration.getName().getIdentifier(), declaration);
     }
     
     
     
     public void add(Declaration declaration, boolean root) {
         if (root) {
             this.add(this.rootLevel, declaration);
         } else {
             this.add(this.levels.getFirst(), declaration);
         }
     }
     
     
     
     public void add(Declaration declaration) {
         this.add(this.levels.getFirst(), declaration);
     }
 
 
 
     public synchronized Declaration tryResolve(IdentifierLiteral id, boolean root) {
         if (root) {
             return this.tryResolveRoot(id);
         }
         
         for (Map<String, Declaration> level : this.levels) {
             Declaration decl = this.tryResolve(level, id);
             if (decl != null) {
                 /*if (hideLocals && decl instanceof VarDeclaration && 
                         !((VarDeclaration)decl).isLocal()) {
                     return CopyTool.copyOf(decl);
                 } else if (!hideLocals) {
                     return CopyTool.copyOf(decl);
                 }*/
                 return CopyTool.copyOf(decl);
             }
         }
 
         return null;
     }
     
     
     
     public Declaration tryResolveRoot(IdentifierLiteral id) {
         return this.tryResolve(this.rootLevel, id);
     }
     
     
     
     private synchronized Declaration tryResolve(Map<String, Declaration> level, 
             IdentifierLiteral id) {
         Declaration decl = level.get(id.getIdentifier());
         return decl;
     }
     
     
     
     public synchronized void remove(String name) {
         for (Map<String, Declaration> level : this.levels) {
            level.remove(name);
         }
     }
     
     
     
     public void store(File file) throws IOException {
         ObjectOutputStream output = null;
         try {
             output = new ObjectOutputStream(new FileOutputStream(file));
             output.writeObject(this);
             output.flush();
         } finally {
             if (output != null) {
                 output.close();
             }
         }
     }
     
     
     
     public static Declarations restore(File file) throws IOException {
         ObjectInputStream input = null;
         try {
             input = new ObjectInputStream(new FileInputStream(file));
             Declarations result = (Declarations) input.readObject();
             return result;
         } catch (ClassNotFoundException e) {
             throw new IOException("invalid declaration file: " + file);
         } finally {
             if (input != null) {
                 input.close();
             }
         }
     }
     
     
     
     @Override
     public Object clone() {
         Declarations result = new Declarations();
         result.levels = new LinkedList<Map<String, Declaration>>(this.levels);
         return result;
     }
     
     
     
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         int i = 0;
         for (Map<String, Declaration> level : this.levels) {
             result.append("Level " + i++);
             result.append("\n");
             for (Entry<String, Declaration> decl : level.entrySet()) {
                 result.append("    ");
                 result.append(decl.getKey());
                 result.append(" = ");
                 result.append(decl.getValue().toString());
                 result.append("\n");
             }
         }
         return result.toString();
     }
 }
