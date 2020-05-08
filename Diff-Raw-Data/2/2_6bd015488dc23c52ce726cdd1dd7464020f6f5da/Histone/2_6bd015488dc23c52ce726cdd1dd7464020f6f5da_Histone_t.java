 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonNull;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.histone.evaluator.Evaluator;
 import ru.histone.optimizer.AstImportResolver;
 import ru.histone.optimizer.AstInlineOptimizer;
 import ru.histone.optimizer.AstMarker;
 import ru.histone.optimizer.AstOptimizer;
 import ru.histone.parser.Parser;
 import ru.histone.utils.IOUtils;
 
 /**
  * Main Histone engine class. Histone template parsing/evaluation is done here.<br/>
  * Histone class is thread safe!<br/>
  * You shouldn't create this class by yourself, use {@link HistoneBuilder} instead.
  *
  * @see HistoneBuilder
  */
 public class Histone {
     /**
      * General Histone logger. All debug information and general logging goes here
      */
     private static final Logger log = LoggerFactory.getLogger(Histone.class);
 
     /**
      * Special logger for histone template syntax errors
      */
    private static final Logger RUNTIME_LOG = LoggerFactory.getLogger(Histone.class.getName() + ".RUNTIME_LOG");
 
     /**
      * @deprecated (should be moved to GlobalProperties)
      */
     private static boolean devMode = false;
 
     private final Parser parser;
     private final Evaluator evaluator;
     private final Gson gson;
     private AstOptimizer astAstOptimizer;
     private AstImportResolver astImportResolver;
     private AstMarker astMarker;
     private AstInlineOptimizer astInlineOptimizer;
 
     public Histone(Parser parser, Evaluator evaluator, AstImportResolver astImportResolver, AstMarker astMarker, AstInlineOptimizer astInlineOptimizer, AstOptimizer astAstOptimizer, Gson gson) {
         this.parser = parser;
         this.evaluator = evaluator;
         this.gson = gson;
         this.astImportResolver = astImportResolver;
         this.astMarker = astMarker;
         this.astInlineOptimizer = astInlineOptimizer;
         this.astAstOptimizer = astAstOptimizer;
     }
 
 
     public JsonArray parseTemplateToAST(Reader templateReader) throws HistoneException {
         String inputString = null;
         try {
             inputString = IOUtils.toString(templateReader);
         } catch (IOException e) {
             log.error("Error reading input Reader", e);
             throw new HistoneException("Error reading input Reader", e);
         }
         return parser.parse(inputString);
     }
 
     public JsonArray optimizeAST(JsonArray templateAST) throws HistoneException {
         JsonArray importsResolved = astImportResolver.resolve(templateAST);
 
         JsonArray markedAst = astMarker.mark(importsResolved);
 
         JsonArray inlinedAst = astInlineOptimizer.inline(markedAst);
 
         JsonArray optimizedAst = astAstOptimizer.optimize(inlinedAst);
 
         return optimizedAst;
     }
 
     public String evaluateAST(JsonArray templateAST) throws HistoneException {
         return evaluateAST(templateAST, JsonNull.INSTANCE);
     }
 
     public String evaluateAST(JsonArray templateAST, JsonElement context) throws HistoneException {
         return evaluator.process(templateAST, context);
     }
 
     public void evaluateAST(JsonArray templateAST, Writer outputWriter) throws HistoneException {
         evaluateAST(templateAST, JsonNull.INSTANCE, outputWriter);
     }
 
     public void evaluateAST(JsonArray templateAST, JsonElement context, Writer output) throws HistoneException {
         String result = evaluateAST(templateAST, context);
         try {
             output.write(result);
         } catch (IOException e) {
             throw new HistoneException("Error writing to output Writer", e);
         }
     }
 
     public String evaluate(String templateContent) throws HistoneException {
         return evaluator.process(templateContent, JsonNull.INSTANCE);
     }
 
     public String evaluate(Reader templateReader) throws HistoneException {
         return evaluate(templateReader, JsonNull.INSTANCE);
     }
 
     public String evaluate(Reader templateReader, JsonElement context) throws HistoneException {
         String templateContent = null;
         try {
             templateContent = IOUtils.toString(templateReader);
         } catch (IOException e) {
             throw new HistoneException("Error reading input Reader");
         }
 
         return evaluator.process(templateContent, context);
     }
 
         public void evaluate(Reader templateReader, Writer outputWriter) throws HistoneException {
         evaluate(templateReader, JsonNull.INSTANCE, outputWriter);
     }
 
     public void evaluate(Reader templateReader, JsonElement context, Writer outputWriter) throws HistoneException {
         String result = evaluate(templateReader, context);
         try {
             outputWriter.write(result);
         } catch (IOException e) {
             throw new HistoneException("Error writing to output Writer", e);
         }
     }
 
     public void setGlobalProperty(GlobalProperty property, String value) {
         evaluator.setGlobalProperty(property, value);
     }
 
     /**
      * Logs histone syntax error to special logger
      *
      * @param msg  message
      * @param e    exception
      * @param args arguments values that should be replaced in message
      */
     public static void runtime_log_error(String msg, Throwable e, Object... args) {
         RUNTIME_LOG.error(msg, args);
 
         StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
         // throw new HistoneException();
 
     }
 
     /**
      * Logs histone syntax info to special logger
      *
      * @param msg  message
      * @param args arguments values that should be replaced in message
      */
     public static void runtime_log_info(String msg, Object... args) {
         if (devMode) {
             runtime_log_error(msg, null, args);
         } else {
             RUNTIME_LOG.info(msg, args);
         }
     }
 
     /**
      * Logs histone syntax warning to special logger
      *
      * @param msg  message
      * @param args arguments values that should be replaced in message
      */
 
     public static void runtime_log_warn(String msg, Object... args) {
         if (devMode) {
             runtime_log_error(msg, null, args);
         } else {
             RUNTIME_LOG.warn(msg, args);
         }
     }
 
     /**
      * Logs histone syntax error to special logger
      *
      * @param msg  message
      * @param e    exception
      * @param args arguments values that should be replaced in message
      */
 
     public static void runtime_log_warn_e(String msg, Throwable e, Object... args) {
         if (devMode) {
             runtime_log_error(msg, e, args);
         } else {
             RUNTIME_LOG.warn(msg, e, args);
         }
     }
 
     protected Gson getGson() {
         return gson;
     }
 }
