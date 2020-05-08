 /**
  *    Copyright 2013 MegaFon
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
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.NullNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.histone.deparser.Deparser;
 import ru.histone.deparser.IDeparser;
 import ru.histone.evaluator.Evaluator;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.optimizer.AbstractASTWalker;
 import ru.histone.optimizer.AdditionalDataForOptimizationDebug;
 import ru.histone.optimizer.ConstantsSubstitutionOptimizer;
 import ru.histone.optimizer.EliminateSingleNodeArrayOptimizer;
 import ru.histone.optimizer.FragmentsConcatinationOptimizer;
 import ru.histone.optimizer.InlineMacroOptimizer;
 import ru.histone.optimizer.OptimizationProfile;
 import ru.histone.optimizer.OptimizationTrace;
 import ru.histone.optimizer.OptimizationTypes;
 import ru.histone.optimizer.SafeASTEvaluationOptimizer;
 import ru.histone.optimizer.SafeASTNodesMarker;
 import ru.histone.parser.Parser;
 import ru.histone.parser.ParserException;
 import ru.histone.resourceloaders.AstResource;
 import ru.histone.resourceloaders.ContentType;
 import ru.histone.resourceloaders.Resource;
 import ru.histone.resourceloaders.ResourceLoadException;
 import ru.histone.resourceloaders.ResourceLoader;
 import ru.histone.resourceloaders.StreamResource;
 import ru.histone.resourceloaders.StringResource;
 import ru.histone.utils.IOUtils;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.Writer;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Set;
 import java.util.TreeSet;
 
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
 
     private Parser parser;
     private Evaluator evaluator;
     private NodeFactory nodeFactory;
     private ResourceLoader resourceLoader;
 
     private final IDeparser deparser = new Deparser();
 
     public Histone(HistoneBootstrap bootstrap) {
         this.parser = bootstrap.getParser();
         this.evaluator = bootstrap.getEvaluator();
         this.nodeFactory = bootstrap.getNodeFactory();
         this.resourceLoader = bootstrap.getResourceLoader();
     }
 
     public ArrayNode parseTemplateToAST(String templateData) throws HistoneException {
         return parseTemplateToAST(new StringReader(templateData));
     }
 
     public ArrayNode parseTemplateToAST(Reader templateReader) throws HistoneException {
         String inputString = null;
         try {
             inputString = IOUtils.toString(templateReader);
         } catch (IOException e) {
             log.error("Error reading input Reader", e);
             throw new HistoneException("Error reading input Reader", e);
         }
         return parser.parse(inputString);
     }
 
     /**
      * Optimize specified AST.
      */
     public ArrayNode optimizeAST(ArrayNode templateAST, OptimizationTypes... optimizationsToRun) throws HistoneException {
         return optimizeAST(templateAST, nodeFactory.jsonObject(), optimizationsToRun);
     }
 
     public ArrayNode optimizeAST(ArrayNode templateAST, ObjectNode context, OptimizationTypes... optimizationsToRun) throws HistoneException {
 //        Deque<AbstractASTWalker> optimizationsList = new LinkedList<AbstractASTWalker>();
         ArrayList<AbstractASTWalker> optimizationsList = new ArrayList<AbstractASTWalker>();
 
         Set<OptimizationTypes> optimizationsToRunSet = new TreeSet<OptimizationTypes>();
         Collections.addAll(optimizationsToRunSet, optimizationsToRun);
 
         ConstantsSubstitutionOptimizer constantsSubstitutionOptimizer = new ConstantsSubstitutionOptimizer(nodeFactory, context);
         if (optimizationsToRunSet.contains(OptimizationTypes.CONSTANTS_SUBSTITUTION)) {
             optimizationsList.add(0, constantsSubstitutionOptimizer);
         }
 
         SafeASTNodesMarker safeASTNodesMarker = new SafeASTNodesMarker(nodeFactory, evaluator);
         if (optimizationsToRunSet.contains(OptimizationTypes.SAFE_CODE_MARKER)) {
             int idx = optimizationsList.indexOf(constantsSubstitutionOptimizer);
             // if we don't have constantsSubstitutionOptimizer, then we will insert at idx=0,
             // otherwise right after constantsSubstitutionOptimizer
             optimizationsList.add(++idx, safeASTNodesMarker);
         }
 
         SafeASTEvaluationOptimizer safeASTEvaluationOptimizer = new SafeASTEvaluationOptimizer(nodeFactory, evaluator);
         if (optimizationsToRunSet.contains(OptimizationTypes.SAFE_CODE_EVALUATION)) {
             int idx = optimizationsList.indexOf(safeASTNodesMarker);
             if (idx < 0) {
                 idx = optimizationsList.indexOf(constantsSubstitutionOptimizer);
                 // if we don't have constantsSubstitutionOptimizer, then we will insert at idx=0,
                 // otherwise right after constantsSubstitutionOptimizer
                 optimizationsList.add(++idx, safeASTNodesMarker);
                 optimizationsList.add(++idx, safeASTEvaluationOptimizer);
             } else {
                 optimizationsList.add(++idx, safeASTEvaluationOptimizer);
             }
         }
 
 //        InlineMacroOptimizer inlineMacroOptimizer = new InlineMacroOptimizer(nodeFactory);
 //        if (optimizationsToRunSet.contains(OptimizationTypes.INLINE_MACRO)) {
 //            int idx = optimizationsList.indexOf(safeASTEvaluationOptimizer);
 //            optimizationsList.add(++idx, inlineMacroOptimizer);
 //        }
 
         if (optimizationsList.size() > 0 || optimizationsToRunSet.contains(OptimizationTypes.FRAGMENT_CONCATENATION)) {
             optimizationsList.add(new FragmentsConcatinationOptimizer(nodeFactory));
         }
         if (optimizationsList.size() > 0 || optimizationsToRunSet.contains(OptimizationTypes.ELIMINATE_SINGLE_NODE)) {
//            optimizationsList.add(new EliminateSingleNodeArrayOptimizer(nodeFactory));
         }
 
         ArrayNode ast = templateAST;
 
         for (AbstractASTWalker optimization : optimizationsList) {
             ast = optimization.process(ast);
         }
 
         return ast;
     }
 
     //<editor-fold desc="Histone optimization">
 
     /**
      * Optimize specified AST, saving debug data of optimization to {@link OptimizationTrace}.
      *
      * @see OptimizationProfile
      * @see AdditionalDataForOptimizationDebug
      */
     /*
     public ArrayNode optimizeASTWithTrace(ArrayNode templateAST,
                                           OptimizationTrace optimizationTrace,
                                           OptimizationProfile optimizationProfile,
                                           AdditionalDataForOptimizationDebug debugInfo) throws HistoneException {
         optimizationTrace.setOriginalAstAndSource(templateAST, deparser.deparse(templateAST));
 
         ArrayNode ast = templateAST;
 
         if (optimizationProfile.isUseImportsResolving()) {
             ast = astImportResolver.resolve(templateAST);
             addFrame(optimizationTrace, "ImportsResolving", ast, debugInfo);
         }
 
         if (optimizationProfile.getUseOptimizationCycle()) {
             long L1 = 0, L2 = 0; // counter for infinite loops
             long g1 = AbstractASTWalker.hash(ast);
             while (true) {
                 L1++;
                 if (L1 == 10) break;
 
                 if (optimizationProfile.isUseConstantsFolding()) {
                     long h1 = AbstractASTWalker.hash(templateAST);
                     while (true) {
                         L2++;
                         if (L2 == 10) {
                             L2 = 0;
                             break;
                         }
 
                         ast = constantFolding.foldConstants(ast);
                         long h2 = AbstractASTWalker.hash(ast);
                         if (h1 == h2) break;
                         else {
                             addFrame(optimizationTrace, "ConstantsFolding", ast, debugInfo);
                             h1 = h2;
                         }
                     }
                 }
                 if (optimizationProfile.isUseConstantPropagation()) {
                     long h1 = AbstractASTWalker.hash(templateAST);
                     while (true) {
                         L2++;
                         if (L2 == 10) {
                             L2 = 0;
                             break;
                         }
 
                         ast = constantPropagation.propagateConstants(ast);
                         long h2 = AbstractASTWalker.hash(ast);
                         if (h1 == h2) break;
                         else {
                             addFrame(optimizationTrace, "ConstantsPropagation", ast, debugInfo);
                             h1 = h2;
                         }
                     }
                 }
                 if (optimizationProfile.isRemoveConstantIfCases()) {
                     long h1 = AbstractASTWalker.hash(templateAST);
                     while (true) {
                         L2++;
                         if (L2 == 10) {
                             L2 = 0;
                             break;
                         }
 
                         ast = constantIfCases.replaceConstantIfs(ast);
                         long h2 = AbstractASTWalker.hash(ast);
                         if (h1 == h2) break;
                         else {
                             addFrame(optimizationTrace, "ConstantIfCases", ast, debugInfo);
                             h1 = h2;
                         }
                     }
                 }
                 if (optimizationProfile.isRemoveUselessVariables()) {
                     long h1 = AbstractASTWalker.hash(templateAST);
                     while (true) {
                         L2++;
                         if (L2 == 10) {
                             L2 = 0;
                             break;
                         }
 
                         ast = uselessVariables.removeUselessVariables(ast);
                         long h2 = AbstractASTWalker.hash(ast);
                         if (h1 == h2) break;
                         else {
                             addFrame(optimizationTrace, "RemoveUselessVariables", ast, debugInfo);
                             h1 = h2;
                         }
                     }
                 }
 
                 long g2 = AbstractASTWalker.hash(ast);
                 if (g1 == g2) break;
                 g1 = g2;
             }
         }
 
         if (optimizationProfile.isUseAstOptimizer()) {
             {
                 long j1 = AbstractASTWalker.hash(ast);
                 ast = astMarker.mark(ast);
                 long j2 = AbstractASTWalker.hash(ast);
                 if (j1 != j2) addFrame(optimizationTrace, "SafeASTNodesMarker", ast, debugInfo);
             }
 
             {
                 long j1 = AbstractASTWalker.hash(ast);
                 ast = astOptimizer.optimize(ast);
                 long j2 = AbstractASTWalker.hash(ast);
                 if (j1 != j2) addFrame(optimizationTrace, "SafeASTEvaluationOptimizer", ast, debugInfo);
             }
         }
 
         if (optimizationProfile.isUseAstSimplifier()) {
             long j1 = AbstractASTWalker.hash(ast);
             ast = fragmentsConcatinationOptimization.simplify(ast);
             long j2 = AbstractASTWalker.hash(ast);
             if (j1 != j2) addFrame(optimizationTrace, "AstSimplifier", ast, debugInfo);
         }
 
         optimizationTrace.setProcessedAstAndSource(ast, deparser.deparse(ast));
         return ast;
     } */
 
     /**
      * Add frame information to {@link OptimizationTrace}.
      */
     /* protected void addFrame(OptimizationTrace optimizationTrace, String name, ArrayNode optimizedAst, AdditionalDataForOptimizationDebug debugInfo) throws HistoneException {
         long t1 = System.currentTimeMillis();
         String outputAfterThisStep = evaluateAST(debugInfo.getTemplateLocation(), optimizedAst, debugInfo.getEvaluationContext());
         long t2 = System.currentTimeMillis();
 
         OptimizationTrace.Frame frame = new OptimizationTrace.Frame();
         frame.setName(name);
         frame.setProcessedAst(optimizedAst);
         frame.setProcessedSource(deparser.deparse(optimizedAst));
         frame.setDidBrokeCompability(!debugInfo.getOriginalOutput().equals(outputAfterThisStep));
         frame.setEvaluationTimeAfterThisStep(t2 - t1);
         frame.setAstLengthAfterThisStep(AbstractASTWalker.countNodes(optimizedAst));
         optimizationTrace.getFrames().add(frame);
     } */
     //</editor-fold>
     public String evaluateAST(ArrayNode templateAST) throws HistoneException {
         return evaluateAST(null, templateAST, NullNode.instance);
     }
 
     public String evaluateAST(String baseURI, ArrayNode templateAST, JsonNode context) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         return evaluator.process(baseURI, templateAST, context);
     }
 
     public void evaluateAST(ArrayNode templateAST, Writer output) throws HistoneException {
         evaluateAST(null, templateAST, NullNode.instance, output);
     }
 
     public void evaluateAST(String baseURI, ArrayNode templateAST, Writer output) throws HistoneException {
         evaluateAST(baseURI, templateAST, NullNode.instance, output);
     }
 
     public void evaluateAST(ArrayNode templateAST, JsonNode context, Writer output) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         evaluateAST(null, templateAST, context, output);
     }
 
     public void evaluateAST(String baseURI, ArrayNode templateAST, JsonNode context, Writer output) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         String result = evaluateAST(baseURI, templateAST, context);
         try {
             output.write(result);
         } catch (IOException e) {
             throw new HistoneException("Error writing to output Writer", e);
         }
     }
 
     /**
      * Main function for Histone template evaluation.
      */
     public String evaluate(String baseURI, String templateContent, JsonNode context) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         ArrayNode ast = parser.parse(templateContent);
 //        ArrayNode optimizedAst = optimizeAST(ast);
         return evaluator.process(baseURI, ast, context);
     }
 
     public ArrayNode evaluateAsAST(String baseURI, String templateContent, JsonNode context) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         return parser.parse(templateContent);
     }
 
     public String evaluateURI(String uri, JsonNode context) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         if (resourceLoader == null) throw new IllegalStateException("Resource loader is null for Histone instance");
 
         Resource resource = resourceLoader.load(uri, null, new String[]{ContentType.TEXT, ContentType.AST});
         String baseUri = resource.getBaseHref();
 
         JsonNode ast = readAstFromResource(resource, uri, null);
         return evaluateAST(baseUri, (ArrayNode) ast, context);
     }
 
     public String evaluate(String templateContent) throws HistoneException {
         return evaluate(null, templateContent, nodeFactory.jsonNull());
     }
 
     public String evaluate(String templateContent, JsonNode context) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         return evaluate(null, templateContent, context);
     }
 
     public String evaluate(Reader templateReader) throws HistoneException {
         return evaluate(null, templateReader, nodeFactory.jsonNull());
     }
 
     public String evaluate(String baseURI, Reader templateReader, JsonNode context) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         String templateContent = null;
         try {
             templateContent = IOUtils.toString(templateReader);
         } catch (IOException e) {
             throw new HistoneException("Error reading input Reader");
         }
         return evaluate(baseURI, templateContent, context);
     }
 
     public void evaluate(String baseURI, Reader templateReader, JsonNode context, Writer outputWriter) throws HistoneException {
         if (context == null) context = nodeFactory.jsonObject();
         String result = evaluate(baseURI, templateReader, context);
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
 
     private JsonNode readAstFromResource(Resource resource, String path, String currentBaseURI) throws HistoneException {
         JsonNode ast = null;
         try {
             if (resource == null) {
                 throw new ResourceLoadException(MessageFormat.format("Can't import resource by path = '{}'. Resource was not found.", path));
             }
 
             if (!(resource instanceof StringResource) && !(resource instanceof StreamResource) && !(resource instanceof AstResource)) {
                 throw new ResourceLoadException(MessageFormat.format("Can't import resource by path = '{}'. Resource type '{}' is unknown", path, resource.getClass()));
             }
 
             String templateContent = null;
             if (resource instanceof StringResource) {
                 templateContent = ((StringResource) resource).getContent();
             } else if (resource instanceof StreamResource) {
                 templateContent = IOUtils.toString(((StreamResource) resource).getContent());
             } else if (resource instanceof AstResource) {
                 ast = ((AstResource) resource).getContent();
             } else {
                 throw new ResourceLoadException(MessageFormat.format("Unsupported resource class: {0}", resource.getClass()));
             }
 
             if (resource instanceof StringResource || resource instanceof StreamResource) {
                 if (templateContent == null) {
                     throw new ResourceLoadException(MessageFormat.format("Can't import resource by path: {0}. Resource is unreadable", path));
                 }
 
                 if (resource.getContentType() == ContentType.TEXT) {
                     ast = parser.parse(templateContent);
                 } else if (resource.getContentType() == ContentType.AST) {
                     ast = nodeFactory.jsonNode(templateContent);
                 } else {
                     throw new ResourceLoadException(MessageFormat.format("Unsupported content-type:{0} of resource href:{1}, baseHref:{2}", resource.getContentType(), path, currentBaseURI));
                 }
             } else {
                 if (ast == null) {
                     throw new ResourceLoadException(MessageFormat.format("Can't import resource by path = {0}. Resource is unreadable", path));
                 }
             }
         } catch (IOException e) {
             throw new ResourceLoadException("Resource import failed! Resource reading error.", e);
         } catch (ParserException e) {
             throw new ResourceLoadException("Resource import failed! Resource parsing error.", e);
         }
 
         return ast;
     }
 }
