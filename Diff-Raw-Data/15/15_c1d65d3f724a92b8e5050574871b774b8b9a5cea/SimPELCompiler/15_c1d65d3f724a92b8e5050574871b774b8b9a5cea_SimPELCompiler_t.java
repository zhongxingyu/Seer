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
 import org.apache.ode.simpel.util.E4XExprParserHelper;
 import org.apache.ode.simpel.omodel.OBuilder;
 import org.apache.ode.Descriptor;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.NativeObject;
 import org.mozilla.javascript.serialize.ScriptableOutputStream;
 import org.mozilla.javascript.serialize.ScriptableInputStream;
 import uk.co.badgersinfoil.e4x.antlr.*;
 import uk.co.badgersinfoil.e4x.E4XHelper;
 
 import java.io.*;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.text.MessageFormat;
 
 public class SimPELCompiler {
 
     private ErrorListener el;
 
     public ErrorListener getErrorListener() {
         return el;
     }
 
     public void setErrorListener(ErrorListener el) {
         this.el = el;
     }
 
     public OProcess compileProcess(File processDoc, Descriptor desc) {
         StringBuffer scriptCnt = new StringBuffer();
         try {
             String thisLine;
             BufferedReader r = new BufferedReader(new FileReader(processDoc));
             while ((thisLine = r.readLine()) != null) scriptCnt.append(thisLine).append("\n");
             r.close();
         } catch (IOException e) {
             fatalCompilationError("Couldn't read process file: " + processDoc.getAbsolutePath());
         }
 
         return compileProcess(processDoc, scriptCnt.toString(), desc);
     }
 
     public OProcess compileProcess(String processDoc, Descriptor desc) {
        return compileProcess(new File(".").getAbsoluteFile(), processDoc, desc);
     }
 
     public OProcess compileProcess(File f, String processDoc, Descriptor desc) {
         // Isolating the process definition from the header containing global state definition (Javascript
         // functions and shared objects)
         Pattern p = Pattern.compile("process [a-zA-Z_]*", Pattern.MULTILINE);
         Matcher m = p.matcher(processDoc);
         if (!m.find())
             fatalCompilationError("Couldn't find any process declaration in file.");
 
         String header = processDoc.substring(0, m.start());
         String processDef = processDoc.substring(m.start(), processDoc.length());
 
         byte[] globals = null;
         if (header.trim().length() > 0)
             globals = buildGlobalState(f, header, desc);
 
         OProcess model = buildModel(processDef, desc);
         if (globals != null) model.globalState = globals;
         return model;
     }
 
     private byte[] buildGlobalState(File f, String header, Descriptor desc) {
         Context cx = Context.enter();
         cx.setOptimizationLevel(-1);
         Scriptable sharedScope = cx.initStandardObjects();
 
         Scriptable newScope = cx.newObject(sharedScope);
         newScope.setPrototype(sharedScope);
         newScope.setParentScope(null);
 
         // Setting some globals part of the environment in which processes execute
        cx.evaluateString(newScope, MessageFormat.format(GLOBALS, f == null ? "." : f.getParentFile().getAbsolutePath()), "<cmd>", 1, null);
         try {
            cx.evaluateString(newScope, header, f == null ? "." : f.getAbsolutePath(), 1, null);
         } catch (Exception e) {
             fatalCompilationError("Error when interpreting definitions in the process header: " + e.toString());
         }
 
         NativeObject processConf = (NativeObject) cx.evaluateString(newScope, "processConfig;", "<cmd>", 1, null);
         if (desc.getAddress() == null && processConf.has("address", null)) desc.setAddress((String) processConf.get("address", null));
         if (processConf.has("inMem", null)) desc.setTransient((Boolean) processConf.get("inMem", null));
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ScriptableOutputStream out;
         try {
             out = new ScriptableOutputStream(baos, sharedScope);
             out.writeObject(newScope);
             out.close();
         } catch (IOException e) {
             fatalCompilationError("Error when serializing header definitions: " + e.toString());
         }
 
         return baos.toByteArray();
     }
 
     private static final String GLOBALS =
             "var processConfig = '{}'; \n" +
 
             "function load(f) '{' \n" +
             "    var cnt = \"\"; \n" +
             "    var l = \"\"; \n" +
             "    var r = new java.io.BufferedReader(new java.io.FileReader(\"{0}/\" + f)); \n" +
             "    while ((l = r.readLine()) != null) cnt = cnt + l + \"\\n\"; \n" +
             "    r.close(); \n" +
             "    return eval(cnt); \n" +
             "'}'";
 
     private OProcess buildModel(String processDef, Descriptor desc) {
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
         E4XHelper e4xHelper = new E4XHelper();
         E4XExprParserHelper e4xParserHelper = new E4XExprParserHelper();
         e4xParserHelper.setErrorListener(errListener);
         e4xHelper.setExpressionParser(e4xParserHelper);
         parser.setE4XHelper(e4xHelper);
 
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
 
         if (t != null) {
             // Handle functions separately
             handleFunctions(t);
 
             // Pass the tree to the walker for compilation
             CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
             SimPELWalker walker = new SimPELWalker(nodes);
             OBuilder obuilder = new OBuilder(desc, errListener);
             walker.setBuilder(obuilder);
             HashMap<Integer, Integer> tokenMapping = buildTokenMap(E4XParser.tokenNames, E4XLexer.class, SimPELWalker.class);
             rewriteTokens(tokenMapping, E4XParser.tokenNames, t, walker, false);
 
             nodes.setTokenStream(tokenStream);
             try {
                 walker.program();
             } catch (RecognitionException e) {
                 throw new CompilationException(e);
             }
             if (errListener.getErrors() != null && errListener.getErrors().size() > 0)
                 throw new CompilationException(errListener.getErrors());
 
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
 
     private void fatalCompilationError(String s) {
         CompilationException.Error err = new CompilationException.Error(0, 0, s, null);
         ArrayList<CompilationException.Error> errs = new ArrayList<CompilationException.Error>();
         errs.add(err);
         throw new CompilationException(errs);
     }
 }
 
