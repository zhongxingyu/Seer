 package org.apache.ode.simpel;
 
 import org.apache.ode.bpel.rtrep.v2.OProcess;
 
 import org.antlr.runtime.ANTLRReaderStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 import org.antlr.runtime.tree.Tree;
 import org.antlr.runtime.tree.TreeParser;
 import org.apache.ode.simpel.antlr.SimPELLexer;
 import org.apache.ode.simpel.antlr.SimPELParser;
 import org.apache.ode.simpel.antlr.SimPELWalker;
 import org.apache.ode.simpel.util.DefaultErrorListener;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.serialize.ScriptableOutputStream;
 import uk.co.badgersinfoil.e4x.antlr.*;
 
import java.io.StringReader;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public class SimPELCompiler {
 
     private ErrorListener el;
 
     public ErrorListener getErrorListener() {
         return el;
     }
 
     public void setErrorListener(ErrorListener el) {
         this.el = el;
     }
 
     public OProcess compileProcess(String processDoc) {
         // Isolating the process definition from the header containing global state definition (Javascript
         // functions and shared objects)
         Pattern p = Pattern.compile("process [a-zA-Z_]*", Pattern.MULTILINE);
         Matcher m = p.matcher(processDoc);
         if (!m.find()) throw new CompilationException("Couldn't find any process declaration.");
         String header = processDoc.substring(0, m.start());
         String processDef = processDoc.substring(m.start(), processDoc.length());
 
         OProcess model = buildModel(processDef);
         if (header.trim().length() > 0)
             model.globalState = buildGlobalState(header);
         return model;
     }
 
     private byte[] buildGlobalState(String header) {
         Context cx = Context.enter();
         cx.setOptimizationLevel(-1);
         Scriptable sharedScope = cx.initStandardObjects();
 
         Scriptable newScope = cx.newObject(sharedScope);
         newScope.setPrototype(sharedScope);
         newScope.setParentScope(null);
 
         cx.evaluateString(newScope, header, "<cmd>", 1, null);
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ScriptableOutputStream out = null;
         try {
             out = new ScriptableOutputStream(baos, sharedScope);
             out.writeObject(newScope);
             out.close();
         } catch (IOException e) {
             throw new CompilationException("Error when interpreting definitions in the process header.", e);
         }
 
         return baos.toByteArray();
     }
 
     private OProcess buildModel(String processDef) {
         ANTLRReaderStream charstream = null;
         try {
             charstream = new ANTLRReaderStream(new StringReader(processDef));
         } catch (IOException e) {
             throw new CompilationException("Unable to read process string.", e);
         }
         ErrorListener errListener = (el == null ? new DefaultErrorListener() : el);
 
         SimPELLexer lexer = new SimPELLexer(charstream);
         lexer.setErrorListener(errListener);
         LinkedListTokenSource linker = new LinkedListTokenSource(lexer);
         LinkedListTokenStream tokenStream = new LinkedListTokenStream(linker);
 
         SimPELParser parser = new SimPELParser(tokenStream);
         parser.setTreeAdaptor(new LinkedListTreeAdaptor());
         parser.setInput(lexer, charstream);
         parser.setErrorListener(errListener);
 
         SimPELParser.program_return result = null;
         try {
             result = parser.program();
         } catch (RecognitionException e) {
             throw new CompilationException(e);
         }
         // pull out the tree and cast it
         LinkedListTree t = (LinkedListTree)result.getTree();
         StringBuffer b = new StringBuffer();
         toText(t, b);
         System.out.println(b.toString());
 
         if (t != null) {
             //  Handle functions separately
             handleFunctions(t);
 
             // Pass the tree to the walker for compilation
             CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
             SimPELWalker walker = new SimPELWalker(nodes);
             walker.setErrorListener(errListener);
             HashMap<Integer, Integer> tokenMapping = buildTokenMap(E4XParser.tokenNames, E4XLexer.class, SimPELWalker.class);
             rewriteTokens(tokenMapping, E4XParser.tokenNames, t, walker, false);
 
             nodes.setTokenStream(tokenStream);
             try {
                 walker.program();
             } catch (RecognitionException e) {
                 throw new CompilationException(e);
             }
             return walker.getBuilder().getProcess();
         }
         return null;
     }
 
     private void toText(Tree t, StringBuffer b) {
         LinkedListToken tok = ((LinkedListTree)t).getStartToken();
         while((tok = tok.getNext()) != null)
             if (tok.getText() != null) b.append(tok.getText());
     }
 
     private void handleFunctions(LinkedListTree t) {
         ArrayList<Integer> toRemove = new ArrayList<Integer>();
         for(int m = 0; m < t.getChildCount(); m++) {
             if ("function".equals(t.getChild(m).getText())) {
                 Tree funcTree = t.getChild(m);
 
                 // Extracting function structure
                 ArrayList<String> params = new ArrayList<String>();
                 StringBuffer body = new StringBuffer();
                 boolean signature = true;
                 for (int p = 2; p < funcTree.getChildCount(); p++) {
                     String txt = funcTree.getChild(p).getText();
                     if (")".equals(txt)) { signature = false; continue; }
                     
                     if (signature) params.add(txt);
                     else body.append(txt);
                 }
                 System.out.println("Found function: " + funcTree.getChild(0) + "(" + params + ") {"
                         + body.toString() + "}");
 
                 toRemove.add(m);
             }
         }
         // Voluntarily not using an iterator, we want to be index based
         for(int m = 0; m < toRemove.size(); m++) {
             t.deleteChild(toRemove.get(m));
             for(int n = 0; n < toRemove.size(); n++) {
                 if (toRemove.get(n) > toRemove.get(m)) toRemove.set(n, toRemove.get(n) - 1);
             }
         }
     }
 
     private void rewriteTokens(HashMap<Integer, Integer> tokenMapping, String[] tokenNames,
                                       LinkedListTree t, TreeParser targetLexer, boolean xmlNode) {
         if (t.token != null && tokenMapping.get(t.token.getType()) != null && (in(tokenNames, t.token.getText()) || xmlNode)) {
             t.token.setType(tokenMapping.get(t.token.getType()));
             xmlNode = true;
         }
         for(int m = 0; m < t.getChildCount(); m++) {
             rewriteTokens(tokenMapping, tokenNames, (LinkedListTree) t.getChild(m), targetLexer, xmlNode);
         }
     }
 
     /**
      * Maps all token types from the source to a token type for the target when source and target
      * have tokens with matching names.
      * @param tokenNames
      * @param source
      * @param target
      * @return
      */
     private HashMap<Integer, Integer> buildTokenMap(String[] tokenNames, Class source, Class target) {
         HashMap<Integer, Integer> tokenMapping = new HashMap<Integer, Integer>();
         for (String name : tokenNames) {
             try {
                 Field targetField = target.getDeclaredField(name);
                 Field sourceField = source.getDeclaredField(name);
                 tokenMapping.put((Integer)sourceField.get(null), (Integer)targetField.get(null));
             } catch (Exception e) { /* Exception means no such token */ }
         }
         return tokenMapping;
     }
 
     private boolean in(String[] arr, String elmt) {
         for (String s : arr)
             if (s.equals(elmt)) return true;
         return false;
     }
 
 }
 
