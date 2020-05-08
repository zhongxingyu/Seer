 package org.rhino.js.dependencies.parser;
 
 import org.mozilla.javascript.CompilerEnvirons;
 import org.mozilla.javascript.IRFactory;
 import org.mozilla.javascript.ast.AstRoot;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 
 public class GetAstRoot {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GetAstRoot.class);
 
     private static CompilerEnvirons env = new CompilerEnvirons();
 
     static {
         env.setRecoverFromErrors(true);
         env.setGenerateDebugInfo(true);
         env.setRecordingComments(true);
     }
 
     private GetAstRoot() {}
 
     public static AstRoot getRoot(String fileName) {
         return getRoot(new File(fileName));
     }
 
     public static AstRoot getRoot(File file) {
         try {
             LOGGER.debug("Getting ast root from {}", file.getAbsolutePath());
 
             return getRoot(new FileReader(file));
         } catch (FileNotFoundException e) {
             throw new IllegalStateException("Fail to read file " + file.getAbsolutePath());
         }
     }
 
     public static AstRoot getRoot(Reader reader) {
         try {
             LOGGER.debug("Parsing...");
 
             IRFactory factory = new IRFactory(env);
             return factory.parse(reader, null, 1);
         } catch (IOException e) {
             throw new IllegalStateException("Fail to parse root node", e);
         }
     }
 
 }
