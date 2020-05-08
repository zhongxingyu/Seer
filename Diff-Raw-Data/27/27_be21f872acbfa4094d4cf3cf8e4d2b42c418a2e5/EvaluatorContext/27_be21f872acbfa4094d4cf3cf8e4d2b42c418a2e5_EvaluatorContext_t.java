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
 
 import com.fasterxml.jackson.databind.JsonNode;
 import ru.histone.GlobalProperty;
 import ru.histone.evaluator.nodes.ContextWrapperNode;
 import ru.histone.evaluator.nodes.GlobalObjectNode;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.*;
 import ru.histone.evaluator.nodes.ObjectHistoneNode;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.*;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Special object for storing evaluator context<br/>
  */
 public class EvaluatorContext {
     private NodeFactory nodeFactory;
     private Deque<Map<String, Node>> stacksProps;
     private Deque<Map<String, MacroFunc>> stacksMacro;
     private Node initialContext;
     private ObjectHistoneNode global;
     private Set<String> importedResources;
     private String baseURI;
 
     /**
      * Creates evaluator context from JSON object
      *
      * @param global  this variable stores 'global' variables values
      * @param jsonCtx JSON object for context
      * @return evaluator context object
      */
     public static EvaluatorContext createFromJson(NodeFactory nodeFactory, ObjectHistoneNode global, JsonNode jsonCtx) {
         if (global == null) {
             global = new GlobalObjectNode(nodeFactory);
         }
         if (jsonCtx == null) {
             jsonCtx = nodeFactory.jsonObject();
         }
         return new EvaluatorContext(nodeFactory, global, jsonCtx);
     }
 
     /**
      * Create empty evaluator context
      *
      * @param global this variable stores 'global' variables values
      * @return evaluator context object
      */
     public static EvaluatorContext createEmpty(NodeFactory nodeFactory, GlobalObjectNode global) {
         if (global == null) {
             return new EvaluatorContext(nodeFactory, new GlobalObjectNode(nodeFactory), nodeFactory.jsonObject());
         }
         return new EvaluatorContext(nodeFactory, global, nodeFactory.jsonObject());
     }
 
     private EvaluatorContext(NodeFactory nodeFactory, ObjectHistoneNode global, JsonNode initialContext) {
         this.stacksProps = new ArrayDeque<Map<String, Node>>();
         this.stacksMacro = new ArrayDeque<Map<String, MacroFunc>>();
 
         this.stacksProps.push(new LinkedHashMap<String, Node>());
         this.stacksMacro.push(new LinkedHashMap<String, MacroFunc>());
 
         this.importedResources = new HashSet<String>();
 
         this.nodeFactory = nodeFactory;
 
         if (initialContext != null) {
             this.initialContext = this.nodeFactory.jsonToNode(initialContext);
         } else {
             this.initialContext = this.nodeFactory.UNDEFINED;
         }
 
         this.global = global;
     }
 
     /**
      * Check if context has specified variable
      *
      * @param name variable name
      * @return true if context has specified variable
      */
     public boolean hasProp(String name) {
        return "baseURI".equals(name) || hasStackProp(name) || initialContext.hasProp(name);
    }

    /**
     * Check if context has specified variable in stack
     *
     * @param name variable name
     * @return true if context has specified variable
     */
    public boolean hasStackProp(String name){
         for (Map<String, Node> stack : stacksProps) {
             if (stack.containsKey(name)) {
                 return true;
             }
         }
        return false;
     }
 
     /**
      * Return sepcified variable
      *
      * @param name variable name
      * @return variable object, or UNDEFINED if variable doesn't exists in context
      */
     public Node getProp(String name) {
         for (Map<String, Node> stack : stacksProps) {
             if (stack.containsKey(name)) {
                 return stack.get(name);
             }
         }
 
         if (initialContext.hasProp(name)) {
             return initialContext.getProp(name);
         } else if ("baseURI".equals(name)){
             return nodeFactory.string(baseURI);
         } else {
             return nodeFactory.UNDEFINED;
         }
     }
 
 
     public Map<String, Node> getProps(){
         Map<String, Node> props = new HashMap<String, Node>();
         for (Map<String, Node> stack : stacksProps) {
             props.putAll(stack);
         }
         return props;
     }
 
     public Map<String, MacroFunc> getMacros(){
         Map<String, MacroFunc> macros = new HashMap<String, MacroFunc>();
         for (Map<String, MacroFunc> macro : stacksMacro) {
             macros.putAll(macro);
         }
         return macros;
     }
 
     /**
      * Stores variable to context
      *
      * @param name  variable name
      * @param value variable value
      */
     public void putProp(String name, Node value) {
         stacksProps.getFirst().put(name, value);
     }
 
     // ----------------------------------------------------
     // Macro methods --------------------------------------
 
     /**
      * Check if context has specified macro
      *
      * @param name macro name
      * @return true if context has specified macro
      */
     public boolean hasMacro(String name) {
         return getMacro(name) != null;
     }
 
     /**
      * Return macro object
      *
      * @param name macro name
      * @return macro object
      */
     public MacroFunc getMacro(String name) {
 
         for (Map<String, MacroFunc> stack : stacksMacro) {
             if (stack.containsKey(name)) {
                 return stack.get(name);
             }
         }
 
         return null;
     }
 
     /**
      * Store macro in context
      *
      * @param name  macro name
      * @param macro macro object
      */
     public void putMacro(String name, MacroFunc macro) {
         stacksMacro.getFirst().put(name, macro);
     }
 
     // ----------------------------------------------------
     // State save/pop methods -------------------------
 
     /**
      * Save current context state to stack
      */
     public void saveState() {
         stacksProps.push(new LinkedHashMap<String, Node>());
         stacksMacro.push(new LinkedHashMap<String, MacroFunc>());
     }
 
     /**
      * Restore previously saved context state from stack
      */
     public void restoreState() {
         if (stacksProps.size() == 1) {
             throw new RuntimeException("Can't restore, when saveState wasn't run");
         }
         stacksProps.pollFirst();
         stacksMacro.pollFirst();
     }
 
     // ----------------------------------------------------
     // Improted resource set ------------------------------
 
     /**
      * Check if following resource has been loaded already
      *
      * @param resourceFullPath resource full path
      * @return true if specified resource has been loaded already
      */
     public boolean hasImportedResource(String resourceFullPath) {
         return importedResources.contains(resourceFullPath);
     }
 
     /**
      * Add resource to list of loaded resources
      *
      * @param resourceFullPath resource full path
      */
     public void addImportedResource(String resourceFullPath) {
         importedResources.add(resourceFullPath);
     }
 
     // ----------------------------------------------------
     // Other methods --------------------------------------
 
     /**
      * Return current context variables as Node object
      *
      * @return Node object representing context
      */
     public Node getAsNode() {
         return ContextWrapperNode.create(nodeFactory, this);
     }
 
     /**
      * Return initial context object
      *
      * @return initial context
      */
     public Node getInitialContext() {
         return initialContext;
     }
 
     /**
      * Get global node object
      *
      * @return global node object
      */
     public ObjectHistoneNode getGlobal() {
         return global;
     }
 
     /**
      * Return global property
      *
      * @param property property name
      * @return property value
      */
     public Node getGlobalValue(GlobalProperty property) {
         if (global == null) {
             return nodeFactory.NULL;
         }
         return global.getProp(property.getName());
     }
 
     /**
      * Uзdate global property value
      *
      * @param property property name
      * @param value    property value
      */
     public void setGlobalValue(GlobalProperty property, Node value) {
         if (property == null) {
             return;
         }
         if (value == null) {
             value = nodeFactory.NULL;
         }
         global.add(property.getName(), value);
     }
 
     @Deprecated
     public Set<Entry<String, Node>> entries() {
         Set<Entry<String, Node>> result = new LinkedHashSet<Map.Entry<String, Node>>();
 
         for (Map<String, Node> stack : stacksProps) {
             for (Entry<String, Node> entry : stack.entrySet()) {
                 if (!"this".equals(entry.getKey())) {
                     result.add(entry);
                 }
             }
         }
 
         return result;
     }
 
     // ----------------------------------------------------
 
 
     public String getBaseURI() {
         return baseURI;
     }
 
     public void setBaseURI(String baseURI) {
         if (baseURI != null) {
 //            if (!global.hasProp("baseURI")) {
 //                global.add("baseURI", nodeFactory.string(baseURI.toString()));
 //            }
 
             this.baseURI = baseURI;
         }
     }
 }
