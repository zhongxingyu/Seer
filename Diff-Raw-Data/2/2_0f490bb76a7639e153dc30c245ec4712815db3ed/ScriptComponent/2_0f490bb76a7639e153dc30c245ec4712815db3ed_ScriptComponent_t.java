 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.components.script;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.jbi.JBIException;
 import javax.jbi.messaging.InOnly;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.script.Bindings;
 import javax.script.Compilable;
 import javax.script.CompiledScript;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.components.util.TransformComponentSupport;
 import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
 import org.springframework.core.io.Resource;
 
 /**
  * A component which is capable of invoking a compiledScript using
  * <a href="http://servicemix.org/JSR+223">JSR 223</a> to allow any scripting language to be integrated.
  *
  * @version $Revision$
  */
 public class ScriptComponent extends TransformComponentSupport {
 
     public static final QName SERVICE = new QName("http://servicemix.org/example/", "receiver");
     public static final String ENDPOINT = "receiver";
 
     private ScriptEngine engine;
     private String scriptEngineName;
     private CompiledScript compiledScript;
     private String scriptText;
     private Resource script;
     private String logResourceBundle;
 
     private boolean disableOutput;
     private Logger scriptLogger;
     private Map bindings = new HashMap();
 
     public ScriptComponent() {
         super(SERVICE, ENDPOINT);
     }
 
     public ScriptComponent(QName service, String endpoint) {
         super(service, endpoint);
     }
 
     public void start() throws JBIException {
 
         if (engine == null) {
             if (compiledScript != null) {
                 engine = compiledScript.getEngine();
             }
             else {
                 if (scriptEngineName != null) {
                     engine = createScriptEngine();
                 }
                 if (engine == null) {
                     throw new JBIException("Must be configured with either the 'compiledScript' or 'engine' property");
                 }
             }
         }
         if (compiledScript == null) {
             checkScriptTextAvailable();
         }
         if (compiledScript == null) {
             if (engine instanceof Compilable) {
                 Compilable compilable = (Compilable) engine;
                 compileScript(compilable);
             }
         }
     }
 
     // Properties
     //-------------------------------------------------------------------------
     public ScriptEngine getEngine() {
         return engine;
     }
 
     public void setEngine(ScriptEngine engine) {
         this.engine = engine;
     }
 
     public CompiledScript getCompiledScript() {
         return compiledScript;
     }
 
     public void setCompiledScript(CompiledScript compiledScript) {
         this.compiledScript = compiledScript;
     }
 
     public String getScriptText() {
         return scriptText;
     }
 
     /**
      * Sets the script as a String
      *
      * @param scriptText
      */
     public void setScriptText(String scriptText) {
         this.scriptText = scriptText;
     }
 
     /**
      * Returns the script as a spring resource
      *
      * @return
      */
     public Resource getScript() {
         return script;
     }
 
     /**
      * Sets the script as a Spring resource
      *
      * @param script
      */
     public void setScript(Resource script) {
         this.script = script;
     }
 
     public String getScriptEngineName() {
         return scriptEngineName;
     }
 
     public void setScriptEngineName(String scriptEngineName) {
         this.scriptEngineName = scriptEngineName;
     }
 
     public boolean isDisableOutput() {
         return disableOutput;
     }
 
     public void setDisableOutput(boolean disableOutput) {
         this.disableOutput = disableOutput;
     }
 
     public String getLogResourceBundle() {
         return logResourceBundle;
     }
 
     public Map getBindings() {
         return bindings;
     }
 
     /**
      * Sets the component level bindings available to scripts as a variable called 'bindings'
      *
      * @param bindings
      */
     public void setBindings(Map bindings) {
         this.bindings = bindings;
     }
 
     /**
      * Sets the resource bundle to use when creating a logger, if one is not
      * explicitly configured.
      *
      * @param logResourceBundle
      */
     public void setLogResourceBundle(String logResourceBundle) {
         this.logResourceBundle = logResourceBundle;
     }
 
     public Logger getScriptLogger() throws MessagingException {
         if (scriptLogger == null) {
             scriptLogger = createScriptLogger();
         }
         return scriptLogger;
     }
 
     /**
      * Sets the logger to use if the script decides to log
      *
      * @param scriptLogger
      */
     public void setScriptLogger(Logger scriptLogger) {
         this.scriptLogger = scriptLogger;
     }
 
 
     // Implementation methods
     //-------------------------------------------------------------------------
     protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
         Bindings bindings = engine.createBindings();
         
         populateBindings(bindings, exchange, in, out);
         try {
             runScript(bindings);
             return !isDisableOutput();
         }
         catch (ScriptException e) {
             System.out.println("Caught: " + e);
             e.printStackTrace();
             throw new MessagingException("Failed to run compiledScript. Reason: " + e, e);
         }
     }
 
     protected void populateBindings(Bindings bindings, MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
         bindings.put("componentContext", getContext());
         bindings.put("deliveryChannel", getDeliveryChannel());
         bindings.put("exchange", exchange);
         bindings.put("inMessage", in);
         bindings.put("log", getScriptLogger());
         bindings.put("componentNamespace", bindings);
        bindings.put("bindings", this.bindings);
 
         InOnly outExchange = null;
         if (isInAndOut(exchange)) {
             bindings.put("outMessage", out);
         }
         else if (!isDisableOutput()) {
             outExchange = getExchangeFactory().createInOnlyExchange();
             if (out instanceof NormalizedMessageImpl) {
                 bindings.put("outExchange", ((NormalizedMessageImpl) out).getExchange());
             }
             bindings.put("outMessage", out);
         }
     }
 
     protected Logger createScriptLogger() throws MessagingException {
         if (logResourceBundle != null) {
             try {
                 return getContext().getLogger(getClass().getName(), logResourceBundle);
             }
             catch (JBIException e) {
                 throw new MessagingException(e);
             }
         }
         else {
         return Logger.getLogger(getClass().getName());
         }    }
 
     protected void runScript(Bindings bindings) throws ScriptException {
         if (compiledScript != null) {
             compiledScript.eval(bindings);
         }
         else {
             evaluteScript(bindings);
         }
     }
 
     protected void evaluteScript(Bindings bindings) throws ScriptException {
         engine.eval(scriptText, bindings);
     }
 
     protected void compileScript(Compilable compilable) throws JBIException {
         try {
             if (scriptText != null) {
                 compiledScript = compilable.compile(scriptText);
             }
             else if (script != null) {
                 compiledScript = compilable.compile(new InputStreamReader(script.getInputStream()));
 
             }
         }
         catch (ScriptException e) {
             throw new JBIException("Failed to parse compiledScript. Reason:  " + e, e);
         }
         catch (IOException e) {
             throw new JBIException("Failed to parse compiledScript. Reason:  " + e, e);
         }
     }
 
     protected ScriptEngine createScriptEngine() {
         ScriptEngineManager manager = new ScriptEngineManager();
         return manager.getEngineByName(scriptEngineName);
     }
 
     protected void checkScriptTextAvailable() throws JBIException {
         if (scriptText == null && script == null) {
             throw new JBIException("If no 'compiledScript' is specified you must specify the 'scriptText' or 'script'");
         }
     }
 }
