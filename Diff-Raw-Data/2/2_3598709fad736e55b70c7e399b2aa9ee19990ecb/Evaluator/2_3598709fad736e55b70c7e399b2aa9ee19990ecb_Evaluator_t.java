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
 package ru.histone.evaluator;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.histone.GlobalProperty;
 import ru.histone.Histone;
 import ru.histone.HistoneException;
 import ru.histone.evaluator.functions.global.*;
 import ru.histone.evaluator.functions.node.*;
 import ru.histone.evaluator.functions.node.number.*;
 import ru.histone.evaluator.functions.node.object.*;
 import ru.histone.evaluator.functions.node.object.Slice;
 import ru.histone.evaluator.functions.node.string.*;
 import ru.histone.evaluator.functions.node.string.Size;
 import ru.histone.evaluator.nodes.*;
 import ru.histone.parser.AstNodeType;
 import ru.histone.parser.Parser;
 import ru.histone.parser.ParserException;
 import ru.histone.resourceloaders.ContentType;
 import ru.histone.resourceloaders.Resource;
 import ru.histone.resourceloaders.ResourceLoadException;
 import ru.histone.resourceloaders.ResourceLoader;
 import ru.histone.utils.ArrayUtils;
 import ru.histone.utils.IOUtils;
 import ru.histone.utils.StringUtils;
 
 import java.io.*;
 import java.lang.reflect.Array;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Histone AST evaluator<br/>
  * This class takes AST for input and evaluates it's nodes producing output
  */
 public class Evaluator {
     private static final Logger log = LoggerFactory.getLogger(Evaluator.class);
 
     private final Parser parser;
     private final NodeFactory nodeFactory;
     private final ResourceLoader resourceLoader;
     private final GlobalFunctionsManager globalFunctionsManager;
     private final NodeFunctionsManager nodeFunctionsManager;
     private final GlobalObjectNode global;
 
 
     public Evaluator(EvaluatorBootstrap bootstrap) {
         this.parser = bootstrap.getParser();
         this.nodeFactory = bootstrap.getNodeFactory();
         this.resourceLoader = bootstrap.getResourceLoader();
         this.globalFunctionsManager = registerMandatoryGlobalFunctions(bootstrap);
         this.nodeFunctionsManager = registerMandatoryNodeFunctions(bootstrap);
         this.global = bootstrap.getGlobal();
     }
 
     /**
      * Register Histone built-in node functions, forcing them to override any custom user functions
      *
      * @param bootstrap bootstrap object
      * @return node functions manager
      */
     private static NodeFunctionsManager registerMandatoryNodeFunctions(EvaluatorBootstrap bootstrap) {
         NodeFunctionsManager nodeFunctionsManager = bootstrap.getNodeFunctionsManager();
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new Join(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new Slice(bootstrap.getNodeFactory()));
 
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new HasKey(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new Keys(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new Values(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new Remove(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new ru.histone.evaluator.functions.node.object.Size(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new ru.histone.evaluator.functions.node.object.Resize(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new ru.histone.evaluator.functions.node.object.Set(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new ru.histone.evaluator.functions.node.object.Search(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(ObjectHistoneNode.class, new ToQueryString(bootstrap.getNodeFactory()));
 
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new Abs(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new Ceil(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new Floor(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new Round(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new Pow(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new ToChar(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new ToFixed(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(NumberHistoneNode.class, new Log(bootstrap.getNodeFactory()));
 
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new CharCodeAt(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new ru.histone.evaluator.functions.node.string.Slice(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new Split(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new Strip(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new ToLowerCase(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new ToNumber(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new ToUpperCase(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new Test(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(StringHistoneNode.class, new Size(bootstrap.getNodeFactory()));
 
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsBoolean(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsFloat(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsInteger(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsNull(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsNumber(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsMap(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsString(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new IsUndefined(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new ToJson(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new ToBoolean(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new ToString(bootstrap.getNodeFactory()));
         nodeFunctionsManager.registerBuiltInFunction(Node.class, new ToMap(bootstrap.getNodeFactory()));
 
         return nodeFunctionsManager;
     }
 
     /**
      * Register Histone built-in global functions, forcing them to override any custom user functions
      *
      * @param bootstrap bootstrap object
      * @return global functions manager
      */
     private static GlobalFunctionsManager registerMandatoryGlobalFunctions(EvaluatorBootstrap bootstrap) {
         GlobalFunctionsManager globalFunctionsManager = bootstrap.getGlobalFunctionsManager();
         globalFunctionsManager.registerBuiltInFunction(new Min(bootstrap.getNodeFactory()));
         globalFunctionsManager.registerBuiltInFunction(new Max(bootstrap.getNodeFactory()));
         globalFunctionsManager.registerBuiltInFunction(new UniqueId(bootstrap.getNodeFactory()));
         globalFunctionsManager.registerBuiltInFunction(new Range(bootstrap.getNodeFactory()));
         globalFunctionsManager.registerBuiltInFunction(new DayOfWeek(bootstrap.getNodeFactory()));
         globalFunctionsManager.registerBuiltInFunction(new DaysInMonth(bootstrap.getNodeFactory()));
         globalFunctionsManager.registerBuiltInFunction(new Rand(bootstrap.getNodeFactory()));
         globalFunctionsManager.registerBuiltInFunction(new ResolveURI(bootstrap.getNodeFactory()));
         return globalFunctionsManager;
     }
 
     public void setGlobalProperty(GlobalProperty property, String value) {
         global.add(property.getName(), nodeFactory.string(value));
     }
 
 //    /**
 //     * Process and evaluate template using specified evaluator context
 //     *
 //     * @param input   template content
 //     * @param jsonCtx json object for evaluator context
 //     * @return evaluation result
 //     * @throws ru.histone.HistoneException in case of eny errors
 //     */
 //    public String process(URI baseURI, String input, JsonNode jsonCtx) throws HistoneException {
 //        ArrayNode ast = parser.parse(input);
 //        return process(baseURI, ast, jsonCtx);
 //    }
 //
 //    /**
 //     * Process and evaluate template using specified evaluator context
 //     *
 //     * @param input   template content
 //     * @param jsonCtx json object for evaluator context
 //     * @return evaluation result
 //     * @throws ru.histone.HistoneException in case of eny errors
 //     */
 //    public String process(String input, JsonNode jsonCtx) throws HistoneException {
 //        ArrayNode ast = parser.parse(input);
 //        return process(null, ast, jsonCtx);
 //    }
 //
 //    /**
 //     * Process and evaluate template using specified evaluator context
 //     *
 //     * @param input   template content
 //     * @param context special context object for evaluator context
 //     * @return evaluation result
 //     * @throws ru.histone.HistoneException in case of eny errors
 //     */
 //    public String process(URI baseURI, String input, EvaluatorContext context) throws HistoneException {
 //        ArrayNode ast = parser.parse(input);
 //        return process(baseURI, ast, context);
 //    }
 //
 //    /**
 //     * Process and evaluate template using specified evaluator context
 //     *
 //     * @param input   template content
 //     * @param context special context object for evaluator context
 //     * @return evaluation result
 //     * @throws ru.histone.HistoneException in case of eny errors
 //     */
 //    public String process(String input, EvaluatorContext context) throws HistoneException {
 //        ArrayNode ast = parser.parse(input);
 //        return process(null, ast, context);
 //    }
 //
 //    /**
 //     * Evaluate template AST using specified evaluator context
 //     *
 //     * @param ast     tempalte AST in json representation
 //     * @param jsonCtx json object for evaluator context
 //     * @return evaluation result
 //     * @throws ru.histone.HistoneException in case of eny errors
 //     */
 //    public String process(ArrayNode ast, JsonNode jsonCtx) throws HistoneException {
 //        return process(ast, EvaluatorContext.createFromJson(nodeFactory, global, jsonCtx));
 //    }
 
     /**
      * Evaluate template AST using specified evaluator context
      *
      * @param ast         tempalte AST in json representation
      * @param jsonContext special context object for evaluator context
      * @return evaluation result
      * @throws ru.histone.HistoneException in case of eny errors
      */
     public String process(String baseURI, ArrayNode ast, JsonNode jsonContext) throws HistoneException {
         EvaluatorContext context = EvaluatorContext.createFromJson(nodeFactory, global, jsonContext);
         if (jsonContext.has("baseURI")) {
             String globalBaseURI = jsonContext.path("baseURI").asText();
             context.setBaseURI(baseURI);
             context.setGlobalValue(GlobalProperty.BASE_URI, nodeFactory.string(globalBaseURI));
         } else {
             context.setBaseURI(baseURI);
         }
         return processInternal(ast, context);
     }
 
     public Node evaluate(JsonNode ast) throws EvaluatorException {
         EvaluatorContext context = EvaluatorContext.createFromJson(nodeFactory, global, nodeFactory.jsonObject());
         return processNode(ast, context);
     }
 
     private String processInternal(JsonNode jsonElement, EvaluatorContext context) throws EvaluatorException {
         return processInternal((ArrayNode) jsonElement, context);
     }
 
     private String processInternal(ArrayNode ast, EvaluatorContext context) throws EvaluatorException {
         log.debug("processInternal(): template={}, context={}", ast, context);
 
         if ("HISTONE".equals(ast.path(0).path(0).asText())) {
             //TODO should be checked in HSTJ-7
             ast = (ArrayNode) ast.path(1);
         }
 
         StringBuilder out = new StringBuilder();
         for (JsonNode element : ast) {
             log.debug("process(): fragment={}", element);
             Node node = processNode(element, context);
             log.debug("process(): node={}", node);
             out.append(node.getAsString().getValue());
         }
         return out.toString();
     }
 
     private Node processNode(JsonNode jsonElement, EvaluatorContext context) throws EvaluatorException {
         log.debug("processNode(): element={}, context={}", new Object[]{jsonElement, context});
 
         if (jsonElement.isTextual()) {
             return nodeFactory.string(jsonElement);
 //            return nodeFactory.string(jsonElement.getAsJsonPrimitive().getAsString());
         }
 
         if (!jsonElement.isArray()) {
             Histone.runtime_log_warn("Invalid JSON element! Neither 'string', nor 'array'. Element: '{}'", jsonElement.toString());
             return nodeFactory.UNDEFINED;
         }
 
         ArrayNode astArray = (ArrayNode) jsonElement;
 
         int nodeType = astArray.get(0).intValue();
         switch (nodeType) {
             case AstNodeType.TRUE:
                 return nodeFactory.TRUE;
 
             case AstNodeType.FALSE:
                 return nodeFactory.FALSE;
 
             case AstNodeType.NULL:
                 return nodeFactory.NULL;
 
             case AstNodeType.INT:
                 return nodeFactory.number(astArray.get(1).decimalValue());
 
             case AstNodeType.DOUBLE:
                 return nodeFactory.number(astArray.get(1).decimalValue());
 
             case AstNodeType.STRING:
                 return nodeFactory.string(astArray.get(1).asText());
 
             case AstNodeType.MAP:
                 return processMap((ArrayNode) astArray.get(1), context);
 //
 //            case AstNodeType.ARRAY:
 //                return processArray(astArray.get(1).getAsArrayNode(), context);
 //
 //            case AstNodeType.OBJECT:
 //                return processObject(astArray.get(1).getAsArrayNode(), context);
 
             case AstNodeType.ADD:
                 return processAdd((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.SUB:
                 return processSub((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.MUL:
                 return processMul((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.DIV:
                 return processDiv((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.MOD:
                 return processMod((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
 
             case AstNodeType.NEGATE:
                 return processNegate((ArrayNode) astArray.get(1), context);
 
             case AstNodeType.OR:
                 return processOr((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.AND:
                 return processAnd((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.NOT:
                 return processNot((ArrayNode) astArray.get(1), context);
 
             case AstNodeType.EQUAL:
                 return processEqual((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.NOT_EQUAL:
                 return processNotEqual((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
 
             case AstNodeType.LESS_OR_EQUAL:
                 return processLessOrEqual((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.LESS_THAN:
                 return processLessThan((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.GREATER_OR_EQUAL:
                 return processGreaterOrEqual((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.GREATER_THAN:
                 return processGreaterThan((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), context);
             case AstNodeType.TERNARY:
                 return processTernary((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), (astArray.size() > 3) ? (ArrayNode) astArray.get(3) : null, context);
             case AstNodeType.IF:
                 return processIf((ArrayNode) astArray.get(1), context);
             case AstNodeType.FOR:
                 return processFor((ArrayNode) astArray.get(1), (ArrayNode) astArray.get(2), (ArrayNode) astArray.get(3), context);
 
             case AstNodeType.STATEMENTS:
                 return processStatements(astArray.get(1), context);
 
             case AstNodeType.VAR:
                 return processVar(astArray.get(1), (ArrayNode) astArray.get(2), context);
 
             case AstNodeType.SELECTOR:
                 return processSelector((ArrayNode) astArray.get(1), context);
 
             case AstNodeType.CALL:
                 return processCall(astArray.get(1), astArray.get(2), astArray.get(3), context);
 
             case AstNodeType.IMPORT:
                 return processImport(astArray.get(1), context);
 
             case AstNodeType.MACRO:
                 return processMacro(astArray.get(1), (ArrayNode) astArray.get(2), (ArrayNode) astArray.get(3), context);
 
             default:
                 Histone.runtime_log_error("Unknown nodeType", null, nodeType);
                 return nodeFactory.UNDEFINED;
         }
     }
 
     private ArrayNode slice(ArrayNode astArray, int fromIndex) {
         ArrayNode result = nodeFactory.jsonArray();
 
         for (int i = fromIndex; i < astArray.size(); i++) {
             result.add(astArray.get(i));
         }
 
         return result;
     }
 
     private Node processMacro(JsonNode ident, ArrayNode args, ArrayNode statements, EvaluatorContext context) throws EvaluatorException {
         String name = ident.asText();
 
         MacroFunc func = new MacroFunc();
         func.setArgs(args);
         func.setStatements(statements);
         func.setBaseURI(getContextBaseURI(context));
         context.putMacro(name, func);
 
         return nodeFactory.UNDEFINED;
     }
 
     private Node runMacro(String name, List<Node> args, EvaluatorContext context) throws EvaluatorException {
         MacroFunc macro = context.getMacro(name);
         if (macro == null) {
             Histone.runtime_log_warn("No macro found by name = '{}'", name);
             return nodeFactory.UNDEFINED;
         }
         ObjectHistoneNode self = nodeFactory.object();
         ObjectHistoneNode argsNode = nodeFactory.object(args);
         self.add("arguments", argsNode);
         context.putProp("self", self);
 
         Iterator<Node> argsItr = args.iterator();
         for (JsonNode macroArg : macro.getArgs()) {
             context.putProp(macroArg.asText(), (argsItr.hasNext() ? argsItr.next() : nodeFactory.UNDEFINED));
         }
         String currentBaseURI = getContextBaseURI(context);
         String macroBaseURI = macro.getBaseURI();
         if (macroBaseURI != null/* && macroBaseURI.isAbsolute() && !macroBaseURI.isOpaque()*/) {
             context.setBaseURI(macroBaseURI);
         }
         StringHistoneNode result = nodeFactory.string(processInternal(macro.getStatements(), context));
         context.setBaseURI(currentBaseURI);
         return result;
     }
 
 
     private Node runNameSpaceMacro(MacroFunc macro, List<Node> args, EvaluatorContext context) throws EvaluatorException {
        // MacroFunc macro = context.getMacro(name);
         if (macro == null) {
             Histone.runtime_log_warn("macro not found");
             return nodeFactory.UNDEFINED;
         }
         ObjectHistoneNode self = nodeFactory.object();
         ObjectHistoneNode argsNode = nodeFactory.object(args);
         self.add("arguments", argsNode);
         context.putProp("self", self);
 
         Iterator<Node> argsItr = args.iterator();
         for (JsonNode macroArg : macro.getArgs()) {
             context.putProp(macroArg.asText(), (argsItr.hasNext() ? argsItr.next() : nodeFactory.UNDEFINED));
         }
         String currentBaseURI = getContextBaseURI(context);
         String macroBaseURI = macro.getBaseURI();
         if (macroBaseURI != null/* && macroBaseURI.isAbsolute() && !macroBaseURI.isOpaque()*/) {
             context.setBaseURI(macroBaseURI);
         }
         StringHistoneNode result = nodeFactory.string(processInternal(macro.getStatements(), context));
         context.setBaseURI(currentBaseURI);
         return result;
     }
 
 
     private Node processImport(JsonNode pathElement, EvaluatorContext context) throws EvaluatorException {
         if (!pathElement.isTextual()) {
             Histone.runtime_log_warn("Invalid path to imported template: '{}'", pathElement.toString());
             return nodeFactory.UNDEFINED;
         }
         String path = pathElement.asText();
         Resource resource = null;
         InputStream resourceStream = null;
         try {
             String currentBaseURI = getContextBaseURI(context);
             String resourceFullPath = resourceLoader.resolveFullPath(path, currentBaseURI);
 
             if (context.hasImportedResource(resourceFullPath)) {
                 Histone.runtime_log_info("Resource already imported.");
                 return nodeFactory.UNDEFINED;
             } else {
                 resource = resourceLoader.load(path, currentBaseURI, new String[]{ContentType.TEXT});
                 if (resource == null) {
                     Histone.runtime_log_warn("Can't import resource by path = '{}'. Resource was not found.", path);
                     return nodeFactory.UNDEFINED;
                 }
                 resourceStream = resource.getInputStream();
                 if (resourceStream == null) {
                     Histone.runtime_log_warn("Can't import resource by path = '{}'. Resource is unreadable", path);
                     return nodeFactory.UNDEFINED;
                 }
                 String templateContent = IOUtils.toString(resourceStream); //yeah... full file reading, because of our tokenizer is regexp-based :(
 
                 // Add this resource full path to context
                 context.addImportedResource(resourceFullPath.toString());
 
                 JsonNode parseResult = parser.parse(templateContent);
                 URI resourceURI = (resource.getBaseHref() != null) ? URI.create(resource.getBaseHref()) : null;
                 if (resourceURI != null && resourceURI.isAbsolute() && !resourceURI.isOpaque()) {
                     context.setBaseURI(resourceURI.toString());
 //                    context.setGlobalValue(GlobalProperty.BASE_URI, nodeFactory.string(resourceURI.toString()));
                 }
 //                nodeFactory.string(processInternal(parseResult, context));  -  WTF??
                 processInternal(parseResult, context);
                 context.setBaseURI(currentBaseURI);
 //                context.setGlobalValue(GlobalProperty.BASE_URI, currentBaseURI == null ? nodeFactory.NULL : nodeFactory.string(currentBaseURI.toString()));
 
                 return nodeFactory.UNDEFINED;
             }
         } catch (ResourceLoadException e) {
             Histone.runtime_log_warn_e("Resource import failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } catch (IOException e) {
             Histone.runtime_log_warn_e("Resource import failed! Resource reading error.", e);
             return nodeFactory.UNDEFINED;
         } catch (ParserException e) {
             Histone.runtime_log_warn_e("Resource import failed! Resource parsing error.", e);
             return nodeFactory.UNDEFINED;
         } finally {
             IOUtils.closeQuietly(resourceStream, log);
             IOUtils.closeQuietly(resource, log);
         }
     }
 
     private String getContextBaseURI(EvaluatorContext context) {
         return context.getBaseURI() == null ? null : context.getBaseURI().toString();
     }
 
     private Node processCall(JsonNode target, JsonNode nameElement, JsonNode args, EvaluatorContext context) throws EvaluatorException {
         if (nameElement.isArray()) {
             Node functionNameNode = processNode(nameElement, context);
             nameElement = functionNameNode.getAsJsonNode();
         }
         if (!nameElement.isTextual()) {
             Histone.runtime_log_warn("call to undefined function '{}'", nameElement.toString());
             return nodeFactory.UNDEFINED;
         }
         String name = nameElement.asText();
         if (name == null || name.length() == 0) {
             Histone.runtime_log_warn("call to undefined anonymous function");
             return nodeFactory.UNDEFINED;
         }
 
         List<Node> argsList = new ArrayList<Node>();
         if (!args.isNull()) {
             Iterator<JsonNode> iter = args.iterator();
             while (iter.hasNext()) {
                 JsonNode arg = iter.next();
                 Node argNode = processNode(arg, context);
                 argsList.add(argNode);
             }
         }
 
         try {
             context.saveState();
 
 
             if (!target.isNull()) {
                 // if target is not null, then it means we want to run Node fucntion or global.functionName()
 
                 Node targetNode = processNode(target, context);
                 // if target is reserved word 'global' then we will run ObjectHistoneNode.function() or global function
                 if (targetNode.isObject() && targetNode.getAsObject().isGlobalObject()) {
 
 
                     if (nodeFunctionsManager.hasFunction(targetNode, name)) {
                         // if we have such function for ObjectHistoneNode type (e.g. global.isObject())
                         return runNodeFunc(targetNode, name, argsList);
                     } else {
                         // next we need to check if we have such global function
                         if (globalFunctionsManager.hasFunction(name)) {
                             return runGlobalFunc(name, argsList);
                         } else {
                             return nodeFactory.UNDEFINED;
                         }
                     }
                 } else {
                     // if target wasn't global object, then we need to check if we have Node function
                     if (!nodeFunctionsManager.hasFunction(targetNode, name)) {
                         if(targetNode.isNamespace()){
                             NameSpaceNode  nameSpaceNode = (NameSpaceNode) targetNode;
                             if (nameSpaceNode.hasMacro(name)) {
                                 return runNameSpaceMacro(nameSpaceNode.getMacro(name), argsList, context);
                             }
                         }
                         Histone.runtime_log_warn("'{}' is undefined function for type '{}'", name, targetNode.toString());
                         return nodeFactory.UNDEFINED;
                     }
                     return runNodeFunc(targetNode, name, argsList);
                 }
             //check for anonymous macros for prop elements
             } else if(context.hasProp(name) && context.getProp(name).isNamespace()) {
                 NameSpaceNode  nameSpaceNode = (NameSpaceNode)  context.getProp(name);
                 if (nameSpaceNode.hasMacro("")) {
                     return runNameSpaceMacro(nameSpaceNode.getMacro(""), argsList, context);
                 }
 
             }
 
             // next we will call macro if it exists
             if (context.hasMacro(name)) {
                 return runMacro(name, argsList, context);
             }
 
             // if we don't have macro with such name, then check for globalFunction
             if (globalFunctionsManager.hasFunction(name)) {
                 return runGlobalFunc(name, argsList);
             }
             if ("include".equals(name)) {
                 // we need to be able to override include function via user GlobalFunction,
                 // that's why we need to check this here, after GlobalFunctionManager check
                 return processInclude(argsList, context);
             }
             if ("loadJSON".equals(name)) {
                 // we need to be able to override loadJSON function via user GlobalFunction,
                 // that's why we need to check this here, after GlobalFunctionManager check
                 return processLoadJSON(argsList, context);
             }
             if ("loadText".equals(name)) {
                 // we need to be able to override loadText function via user GlobalFunction,
                 // that's why we need to check this here, after GlobalFunctionManager check
                 return processLoadText(argsList, context);
             }
             if ("require".equals(name)) {
                 Node requireNode = processRequire(argsList, context);
 //                return processNode(requireNode, context);
 
                 return requireNode;
 
             }
 
             return nodeFactory.UNDEFINED;
         } finally {
             context.restoreState();
         }
     }
 
     private Node processLoadJSON(List<Node> argsList, EvaluatorContext context) {
         Node[] args = argsList.toArray((Node[]) Array.newInstance(Node.class, argsList.size()));
         if (ArrayUtils.isEmpty(args)) {
             return nodeFactory.UNDEFINED;
         }
         if (!args[0].isString()) {
             throw new GlobalFunctionExecutionException("Non-string path for JSON resource location: " + args[0].getAsString().getValue());
         }
         //
         String path = args[0].getAsString().getValue();
         final String currentBaseURI = getContextBaseURI(context);
         boolean isJsonp = false;
         ObjectHistoneNode requestMap = null;
         if (args.length == 2) {
             if (args[1].isBoolean()) {
                 isJsonp = args[1].getAsBoolean().getValue();
             } else if (args[1].isObject()) {
                 requestMap = args[1].getAsObject();
             } else {
                 throw new GlobalFunctionExecutionException("Wrong argument type: " + args[1].getAsString().getValue());
             }
         }
         // if we are doing http request, then check query parameters and if
         // there no 'callback' parameter add it with random string value (random
         // string should be generated, using all english symbols, length = 6)
         {
             if (isJsonp && path.contains("http://") && !path.contains("callback=")) {
                 if (!path.contains("?")) {
                     path = path + "?callback=" + StringUtils.randomString(6);
                 } else {
                     path = path + "&callback=" + StringUtils.randomString(6);
                 }
             }
         }
 
         Resource resource = null;
         InputStream resourceStream = null;
         BufferedReader reader = null;
         try {
             resource = resourceLoader.load(path, currentBaseURI, new String[]{ContentType.TEXT}, requestMap);
             if (resource == null) {
                 Histone.runtime_log_warn(String.format("Can't load resource by path = '%s'. Resource was not found.", path));
                 return nodeFactory.UNDEFINED;
 
             }
             resourceStream = resource.getInputStream();
             if (resourceStream == null) {
                 Histone.runtime_log_warn(String.format("Can't load resource by path = '%s'. Resource is unreadable.", path));
                 return nodeFactory.UNDEFINED;
             }
 
             JsonNode json = null;
 
             StringWriter writer = new StringWriter();
             IOUtils.copy(resourceStream, writer);
             String s = writer.toString();
 
             //if server returned normal json, not jsonp we need to return it, regardless jsonp boolean flag
             try {
 
                 json = nodeFactory.jsonNode(new StringReader(s));
             } catch (JsonProcessingException e) {
                 // try isJsonp
                 if (isJsonp) {
                     int i1 = s.indexOf("(");
                     int i2 = s.indexOf(")");
 
                     if (i1 > 0 && i2 > 0 && i1 < i2) {
                         s = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
                         json = nodeFactory.jsonNode(new StringReader(s));
                     }
                 }
             }
             if (json == null) {
                 Histone.runtime_log_warn("Invalid JSON data found by path: " + path);
                 return nodeFactory.UNDEFINED;
             }
             return nodeFactory.jsonToNode(json);
         } catch (ResourceLoadException e) {
             Histone.runtime_log_warn_e("Resource loadJSON failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } catch (JsonProcessingException e) {
             Histone.runtime_log_warn_e("Resource loadJSON failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } catch (IOException e) {
             Histone.runtime_log_warn_e("Resource loadJSON failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } finally {
             IOUtils.closeQuietly(reader, log);
             IOUtils.closeQuietly(resourceStream, log);
             IOUtils.closeQuietly(resource, log);
         }
     }
 
     private Node processLoadText(List<Node> argsList, EvaluatorContext context) {
         Node[] args = argsList.toArray((Node[]) Array.newInstance(Node.class, argsList.size()));
         if (ArrayUtils.isEmpty(args)) {
             return nodeFactory.UNDEFINED;
         }
         if (!args[0].isString()) {
             throw new GlobalFunctionExecutionException("Non-string path for JSON resource location: " + args[0].getAsString().getValue());
         }
         //
         final String path = args[0].getAsString().getValue();
         final String currentBaseURI = getContextBaseURI(context);
         ObjectHistoneNode requestMap = null;
         if (args.length == 2) {
             if (args[1].isObject()) {
                 requestMap = args[1].getAsObject();
             } else {
                 throw new GlobalFunctionExecutionException("Wrong argument type: " + args[1].getAsString().getValue());
             }
         }
 
         Resource resource = null;
         InputStream resourceStream = null;
         try {
             resource = resourceLoader.load(path, currentBaseURI, new String[]{ContentType.TEXT}, requestMap);
             if (resource == null) {
                 Histone.runtime_log_warn(String.format("Can't load resource by path = '%s'. Resource was not found.", path));
                 return nodeFactory.UNDEFINED;
             }
             resourceStream = resource.getInputStream();
             if (resourceStream == null) {
                 Histone.runtime_log_warn(String.format("Can't load resource by path = '%s'. Resource is unreadable.", path));
                 return nodeFactory.UNDEFINED;
             }
             return nodeFactory.string(resourceStream);
         } catch (ResourceLoadException e) {
             Histone.runtime_log_warn_e("Resource loadText failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } catch (IOException e) {
             Histone.runtime_log_warn_e("Resource loadText failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } finally {
             IOUtils.closeQuietly(resourceStream, log);
             IOUtils.closeQuietly(resource, log);
         }
     }
 
 
     private Node processInclude(List<Node> args, EvaluatorContext context) {
         if (args.size() == 0) {
             return nodeFactory.UNDEFINED;
         }
         if (!args.get(0).isString()) {
             throw new GlobalFunctionExecutionException("Non-string path for JSON resource location: "
                     + args.get(0).getAsString().getValue());
         }
         //
         final String path = args.get(0).getAsString().getValue();
         final String currentBaseURI = getContextBaseURI(context);
         ObjectHistoneNode requestMap = null;
         if (args.size() == 2) {
             if (args.get(1).isObject()) {
                 requestMap = args.get(1).getAsObject();
             } else {
                // throw new GlobalFunctionExecutionException("Wrong argument type: " + args.get(1).getAsString().getValue());
             }
         }
 
         Resource resource = null;
         InputStream resourceStream = null;
         try {
             resource = resourceLoader.load(path, currentBaseURI, new String[]{ContentType.TEXT}, requestMap);
             if (resource == null) {
                 Histone.runtime_log_warn("Can't include resource by path = '{}'. Resource was not found.", path);
                 return nodeFactory.UNDEFINED;
             }
             resourceStream = resource.getInputStream();
             if (resourceStream == null) {
                 Histone.runtime_log_warn("Can't include resource by path = '{}'. Resource is unreadable", path);
                 return nodeFactory.UNDEFINED;
             }
             String templateContent = IOUtils.toString(resourceStream); //yeah... full file reading, because of our tokenizer is regexp-based :(
             ArrayNode parseResult = parser.parse(templateContent);
             GlobalObjectNode globalCopy = new GlobalObjectNode(nodeFactory, global);
             URI resourceUri = (resource.getBaseHref() != null) ? URI.create(resource.getBaseHref()) : null;
             if (args.size() <= 1) {
                 EvaluatorContext includeContext = EvaluatorContext.createEmpty(nodeFactory, globalCopy);
                 if (resourceUri != null && resourceUri.isAbsolute() && !resourceUri.isOpaque()) {
                     includeContext.setBaseURI(resourceUri.toString());
 //                globalCopy.add(GlobalProperty.BASE_URI.getName(), nodeFactory.string(resourceUri.toString()));
                 }
                 String includeOutput = processInternal(parseResult, includeContext);
                 return nodeFactory.string(includeOutput);
             }
             EvaluatorContext includeContext = EvaluatorContext.createFromJson(nodeFactory, globalCopy, args.get(1).getAsJsonNode());
             includeContext.setBaseURI(resourceUri.toString());
             StringHistoneNode result = nodeFactory.string(processInternal(parseResult, includeContext));
             context.setBaseURI(currentBaseURI);
             return result;
         } catch (ResourceLoadException e) {
             Histone.runtime_log_warn_e("Resource include failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } catch (IOException e) {
             Histone.runtime_log_warn_e("Resource include failed! Resource reading error.", e);
             return nodeFactory.UNDEFINED;
         } catch (ParserException e) {
             Histone.runtime_log_warn_e("Resource include failed! Resource parsing error.", e);
             return nodeFactory.UNDEFINED;
         } catch (EvaluatorException e) {
             Histone.runtime_log_warn_e("Resource include failed! Resource evaluation error.", e);
             return nodeFactory.UNDEFINED;
         } finally {
             IOUtils.closeQuietly(resourceStream, log);
             IOUtils.closeQuietly(resource, log);
         }
     }
 
     private Node processRequire(List<Node> args, EvaluatorContext context) {
         if (args.size() == 0) {
             return nodeFactory.UNDEFINED;
         }
         if (!args.get(0).isString()) {
             throw new GlobalFunctionExecutionException("Non-string path for JSON resource location: "
                     + args.get(0).getAsString().getValue());
         }
         final String path = args.get(0).getAsString().getValue();
         final String currentBaseURI = getContextBaseURI(context);
 
         Resource resource = null;
         InputStream resourceStream = null;
         try {
             resource = resourceLoader.load(path, currentBaseURI, new String[]{ContentType.TEXT});
             if (resource == null) {
                 Histone.runtime_log_warn("Can't get required resource by path = '{}'. Resource was not found.", path);
                 return nodeFactory.UNDEFINED;
             }
             resourceStream = resource.getInputStream();
             if (resourceStream == null) {
                 Histone.runtime_log_warn("Can't get required resource by path = '{}'. Resource is unreadable", path);
                 return nodeFactory.UNDEFINED;
             }
 
             String templateContent = IOUtils.toString(resourceStream); //yeah... full file reading, because of our tokenizer is regexp-based :(
             ArrayNode parseResult = parser.parse(templateContent);
 
             GlobalObjectNode globalCopy = new GlobalObjectNode(nodeFactory, global);
             URI resourceUri = (resource.getBaseHref() != null) ? URI.create(resource.getBaseHref()) : null;
             EvaluatorContext includeContext = EvaluatorContext.createEmpty(nodeFactory, globalCopy);
             if (resourceUri != null && resourceUri.isAbsolute() && !resourceUri.isOpaque()) {
                 includeContext.setBaseURI(resourceUri.toString());
             }
             processInternal(parseResult, includeContext);
             NameSpaceNode nameSpaceNode = nodeFactory.nameSpace();
             Map<String, Node> props = includeContext.getProps();
             for(String key: props.keySet()){
                nameSpaceNode.add(key, props.get(key));
             }
 
             Map<String, MacroFunc> macros = includeContext.getMacros();
             for(String key: macros.keySet()){
                 nameSpaceNode.addMacro(key, macros.get(key));
             }
 
             return nameSpaceNode;
         } catch (ResourceLoadException e) {
             Histone.runtime_log_warn_e("Required resource failed! Unresolvable resource.", e);
             return nodeFactory.UNDEFINED;
         } catch (IOException e) {
             Histone.runtime_log_warn_e("Required resource failed! Resource reading error.", e);
             return nodeFactory.UNDEFINED;
         } catch (ParserException e) {
             Histone.runtime_log_warn_e("Required resource failed! Resource parsing error.", e);
             return nodeFactory.UNDEFINED;
         } catch (EvaluatorException e) {
                 Histone.runtime_log_warn_e("Required resource failed! Resource evaluation error.", e);
                 return nodeFactory.UNDEFINED;
         } finally {
             IOUtils.closeQuietly(resourceStream, log);
             IOUtils.closeQuietly(resource, log);
         }
     }
 
     private Node runNodeFunc(Node targetNode, String name, List<Node> args) throws EvaluatorException {
         try {
             return nodeFunctionsManager.execute(targetNode, name, args.toArray((Node[]) Array.newInstance(Node.class, args.size())));
         } catch (NodeFunctionExecutionException e) {
             Histone.runtime_log_warn_e("Node function '{}' execution on node '{}' failed!", e, name, targetNode.getAsString());
             return nodeFactory.UNDEFINED;
         }
     }
 
     private Node runGlobalFunc(String name, List<Node> args) throws EvaluatorException {
         try {
             return globalFunctionsManager.execute(name, args.toArray((Node[]) Array.newInstance(Node.class, args.size())));
         } catch (GlobalFunctionExecutionException e) {
             Histone.runtime_log_warn("Global function '%s' execution failed!", e, name);
             return nodeFactory.UNDEFINED;
         }
     }
 
     private Node processVar(JsonNode ident, ArrayNode expression, EvaluatorContext context) throws EvaluatorException {
         log.debug("processVar(): ident={}, expression={}, context={}", new Object[]{ident, expression, context});
 
         Node exprNode = processNode(expression, context);
         context.putProp(ident.asText(), exprNode);
         return nodeFactory.UNDEFINED;
     }
 
     private Node processStatements(JsonNode jsonElement, EvaluatorContext context) throws EvaluatorException {
         log.debug("processStatements(): JsonNode={}, context={}", new Object[]{jsonElement, context});
 
         return nodeFactory.string(processInternal(jsonElement, context));
     }
 
     private Node processFor(ArrayNode iterator, ArrayNode collection, ArrayNode statements, EvaluatorContext context) throws EvaluatorException {
         log.debug("processFor(): iterator={}, collection={}, statements={}, context={}", new Object[]{iterator, collection, statements, context});
 
         StringBuilder sb = new StringBuilder();
 
         String iterVal = iterator.get(0).asText();
         String iterKey = (iterator.size() > 1) ? iterator.get(1).asText() : null;
 
 
         Node collectionNode = processNode(collection, context);
 
         ObjectHistoneNode self = nodeFactory.object();
         context.putProp("self", self);
 
 
        if (collectionNode.isObject() && collectionNode.getAsObject().size() > 0) {
             int idx = 0;
             self.add("last", nodeFactory.number(collectionNode.getAsObject().size() - 1));
             Map<Object, Node> elements = collectionNode.getAsObject().getElements();
             for (Object key : elements.keySet()) {
                 // Save context state on each iteration
                 // HSTJ-26
                 context.saveState();
 
                 self.add("index", nodeFactory.number(idx));
 
                 context.putProp(iterVal, elements.get(key));
                 if (iterKey != null) {
                     context.putProp(iterKey, nodeFactory.string(key.toString()));
                 }
 
                 context.putProp("self", self);
                 sb.append(processInternal(statements.get(0), context));
 
                 idx++;
 
                 // HSTJ-26
                 context.restoreState();
             }
         } else if (statements.size() > 1) {
             String result = processInternal(statements.get(1), context);
             sb.append(result);
         }
 
 
         return nodeFactory.string(sb.toString());
     }
 
     private Node processIf(ArrayNode conditions, EvaluatorContext context) throws EvaluatorException {
         log.debug("processIf(): conditions={} context={}", new Object[]{conditions, context});
 
         StringHistoneNode result = nodeFactory.string();
 
         context.saveState();
 
         for (JsonNode condition : conditions) {
             Node conditionResult = processNode(condition.get(0), context);
 
             if (conditionResult.getAsBoolean().getValue()) {
                 result = nodeFactory.string(processInternal(condition.get(1), context));
                 break;
             }
         }
 
         context.restoreState();
 
         return result;
     }
 
     private Node processTernary(ArrayNode condition, ArrayNode trueNode, ArrayNode falseNode, EvaluatorContext context) throws EvaluatorException {
         log.debug("processTernary(): conditions={}, trueNode={}, falseNode={}, context={}", new Object[]{condition, trueNode, falseNode, context});
 
         Node conditionResult = processNode(condition, context);
         Node result;
 
         if (conditionResult.getAsBoolean().getValue()) {
             result = processNode(trueNode, context);
         } else if (falseNode != null) {
             result = processNode(falseNode, context);
         } else {
             result = nodeFactory.UNDEFINED;
         }
 
         return result;
     }
 
     private Node processGreaterThan(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processGreaterThan(): left={}, right={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("processGreaterThan(): left={}, right={}, context={}", new Object[]{left, right, context});
 
         return left.oper_greaterThan(right);
     }
 
     private Node processGreaterOrEqual(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processGreaterOrEqual(): left={}, right={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("processGreaterOrEqual(): left={}, right={}, context={}", new Object[]{left, right, context});
 
         return left.oper_greaterOrEqual(right);
     }
 
     private Node processLessThan(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processLessThan(): left={}, right={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("processLessThan(): left={}, right={}, context={}", new Object[]{left, right, context});
 
         return left.oper_lessThan(right);
     }
 
     private Node processLessOrEqual(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processLessOrEqual(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("processLessOrEqual(): left={}, right={}, context={}", new Object[]{left, right, context});
 
         return left.oper_lessOrEqual(right);
     }
 
     private Node processNotEqual(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processNotEqual(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("processNotEqual(): left={}, right={}, context={}", new Object[]{left, right, context});
 
         return left.oper_notEqual(right);
     }
 
     private Node processEqual(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processEqual(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         log.trace("processEqual(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft.getClass(), nodeRight.getClass(), context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("processEqual(): nodeLeft={}, nodeRight={}, context={}", new Object[]{left.getClass(), right.getClass(), context});
 
         return left.oper_equal(right);
     }
 
     private Node processNot(ArrayNode node, EvaluatorContext context) throws EvaluatorException {
         log.debug("processNot(): node={}, context={}", new Object[]{node, context});
         Node left = processNode(node, context);
         log.trace("processNot(): node={}, context={}", new Object[]{node.getClass(), context});
 
         return left.oper_not();
     }
 
     private Node processAnd(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("pricessAnd(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         log.trace("pricessAnd(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft.getClass(), nodeRight.getClass(), context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("pricessAnd(): nodeLeft={}, nodeRight={}, context={}", new Object[]{left.getClass(), right.getClass(), context});
 
         return left.oper_and(right);
     }
 
     private Node processOr(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("pricessOr(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         log.trace("pricessOr(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft.getClass(), nodeRight.getClass(), context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("pricessOr(): nodeLeft={}, nodeRight={}, context={}", new Object[]{left.getClass(), right.getClass(), context});
 
         return left.oper_or(right);
     }
 
     private Node processAdd(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processAdd(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
 
         log.trace("processAdd(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft.getClass(), nodeRight.getClass(), context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
         log.trace("processAdd(): nodeLeft={}, nodeRight={}, context={}", new Object[]{left.getClass(), right.getClass(), context});
 
         return left.oper_add(right);
     }
 
     private Node processSub(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processSub(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
 
         return left.oper_sub(right);
     }
 
     private Node processMul(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processMul(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
 
         return left.oper_mul(right);
     }
 
     private Node processDiv(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processDiv(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
 
         return left.oper_div(right);
     }
 
     private Node processMod(ArrayNode nodeLeft, ArrayNode nodeRight, EvaluatorContext context) throws EvaluatorException {
         log.debug("processMod(): nodeLeft={}, nodeRight={}, context={}", new Object[]{nodeLeft, nodeRight, context});
         Node left = processNode(nodeLeft, context);
         Node right = processNode(nodeRight, context);
 
         return left.oper_mod(right);
     }
 
     private Node processNegate(ArrayNode node, EvaluatorContext context) throws EvaluatorException {
         log.debug("processNegate(): node={}, context={}", new Object[]{node, context});
         Node left = processNode(node, context);
         return left.oper_negate();
     }
 
     private Node processMap(ArrayNode element, EvaluatorContext context) throws EvaluatorException {
         log.debug("processMap(): element={}, context={}", new Object[]{element, context});
         ObjectHistoneNode result = nodeFactory.object();
 
         for (JsonNode item : element) {
             JsonNode key = item.get(0);
             if (key.isNull()) {
                 result.add(processNode(item.get(1), context));
             } else {
                 if (key.isNumber()) {
                     result.add(key.intValue(), processNode(item.get(1), context));
                 } else if (key.isTextual()) {
                     result.add(key.asText(), processNode(item.get(1), context));
                 }
             }
         }
 
         return result;
     }
 
     private Node processSelector(ArrayNode element, EvaluatorContext context) throws EvaluatorException {
         log.debug("processSelector(): element={}, context={}", new Object[]{element, context});
 
         int startIdx = 0;
         Node ctx;
         if (element.get(0).isArray()) {
             ctx = processNode(element.get(startIdx++), context);
         } else if ("this".equals(element.get(0).asText())) {
             ctx = context.getInitialContext();
             startIdx++;
         } else if ("global".equals(element.get(0).asText())) {
             if ("baseURI".equals(element.path(1).asText())) {
                 if (context.getGlobal().hasProp("baseURI")) {
                     ctx = context.getGlobal().getProp("baseURI");
                     startIdx = startIdx + 2;
                 } else {
                     ctx = nodeFactory.string(context.getBaseURI());//context.getProp("baseURI");
                     startIdx = startIdx + 2;
                 }
             } else {
                 ctx = context.getGlobal();
                 startIdx++;
             }
         } else if ("self".equals(element.get(0).asText())) {
             ctx = context.getProp("self");
             startIdx++;
         } else {
             if ("baseURI".equals(element.path(0).asText()) && !context.hasStackProp("baseURI")) {
                 if (context.getGlobal().hasProp("baseURI") ) {
                     ctx = context.getGlobal().getProp("baseURI");
                     startIdx = startIdx + 1;
                 } else {
                     ctx = nodeFactory.string(context.getBaseURI());//context.getProp("baseURI");
                     startIdx = startIdx + 1;
                 }
             } else {
                 ctx = context.getAsNode();
             }
         }
 
         for (int j = startIdx; j < element.size(); j++) {
             JsonNode selector = element.get(j);
             String propName;
 
             if (selector.isTextual()) {
                 // selector is written as it is
                 propName = selector.asText();
             } else {
                 // selector is written inside ['...'], like access to array
                 propName = processNode(selector, context).getAsString().getValue();
             }
 
             if (ctx.hasProp(propName)) {
                 ctx = ctx.getProp(propName);
             } else if (context.getGlobal().hasProp(propName)) {
                 ctx = context.getGlobal().getProp(propName);
             } else {
                 Histone.runtime_log_warn("Selector: in selector '{}' object '{}' doesn't have property '{}'", element, ctx, propName);
                 ctx = null;
                 break;
             }
 
             // }
         }
 
         Node result;
 
         if (ctx == null) {
             Histone.runtime_log_warn("Property value was null, returning 'undefined()'");
             result = nodeFactory.UNDEFINED;
         } else {
             result = ctx;
         }
 
         log.debug("processSelector(): result={}", new Object[]{result});
 
         return result;
     }
 }
