 /*
  * Copyright (c) 2006-2013 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.scriptplugin;
 
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.interfaces.ActionController;
 import com.dmdirc.interfaces.ActionListener;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.interfaces.actions.ActionType;
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.plugins.implementations.BaseCommandPlugin;
 import com.dmdirc.util.io.StreamUtils;
 import com.dmdirc.util.validators.ValidationResponse;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 /**
  * This allows javascript scripts to be used in DMDirc.
  */
 public class ScriptPlugin extends BaseCommandPlugin implements ActionListener {
 
     /** Script Directory */
     private final String scriptDir;
     /** Script Engine Manager */
     private ScriptEngineManager scriptFactory = new ScriptEngineManager();
     /** Instance of the javaScriptHelper class */
     private JavaScriptHelper jsHelper = new JavaScriptHelper();
     /** Store Script State Name,Engine */
     private Map<String, ScriptEngineWrapper> scripts = new HashMap<>();
     /** Used to store permanent variables */
     protected TypedProperties globalVariables = new TypedProperties();
     /** The action controller to use. */
     private final ActionController actionController;
 
     /**
      * Creates a new instance of the Script Plugin.
      *
      * @param actionController The action controller to register listeners with
      * @param identityController The Identity Manager that controls the current config
      * @param commandController Command controller to register commands
      */
     public ScriptPlugin(final ActionController actionController,
             final IdentityController identityController,
             final CommandController commandController) {
         super(commandController);
         scriptDir = identityController.getConfigurationDirectory() + "scripts/";
         this.actionController = actionController;
 
         // Add the JS Helper to the scriptFactory
         getScriptFactory().put("globalHelper", getJavaScriptHelper());
         getScriptFactory().put("globalVariables", getGlobalVariables());
         registerCommand(new ScriptCommand(this, identityController, commandController),
                 ScriptCommand.INFO);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onLoad() {
         // Register the plugin_loaded action initially, this will be called
         // after this method finishes for us to register the rest.
         actionController.registerListener(this, CoreActionType.PLUGIN_LOADED);
 
         // Make sure our scripts dir exists
         final File newDir = new File(scriptDir);
         if (!newDir.exists()) { newDir.mkdirs(); }
 
         final File savedVariables = new File(scriptDir+"storedVariables");
         if (savedVariables.exists()) {
             FileInputStream fis = null;
             try {
                 fis = new FileInputStream(savedVariables);
                 globalVariables.load(fis);
             } catch (IOException e) {
                 Logger.userError(ErrorLevel.LOW, "Error reading savedVariables from '"+savedVariables.getPath()+"': "+e.getMessage(), e);
             } finally {
                 StreamUtils.close(fis);
             }
         }
         super.onLoad();
     }
 
     /** {@inheritDoc} */
     @Override
     public void onUnload() {
         actionController.unregisterListener(this);
 
         final File savedVariables = new File(scriptDir+"storedVariables");
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(savedVariables);
             globalVariables.store(fos, "# DMDirc Script Plugin savedVariables");
         } catch (IOException e) {
             Logger.userError(ErrorLevel.LOW, "Error reading savedVariables to '"+savedVariables.getPath()+"': "+e.getMessage(), e);
         } finally {
             StreamUtils.close(fos);
         }
         super.onUnload();
     }
 
     /**
      * Register all the action types.
      * This will unregister all the actions first.
      */
     private void registerAll() {
        actionController.registerListener(this);
         for (Map.Entry<String, List<ActionType>> entry
                 : actionController.getGroupedTypes().entrySet()) {
             final List<ActionType> types = entry.getValue();
             actionController.registerListener(this,
                     types.toArray(new ActionType[types.size()]));
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
         // Plugins may to register/unregister action types, so lets reregister all
         // the action types. This
         if (type.equals(CoreActionType.PLUGIN_LOADED) || type.equals(CoreActionType.PLUGIN_UNLOADED)) {
             registerAll();
         }
         callFunctionAll("action_"+type.toString().toLowerCase(), arguments);
     }
 
     /**
      * Get a clone of the scripts map.
      *
      * @return a clone of the scripts map
      */
     protected Map<String, ScriptEngineWrapper> getScripts() { return new HashMap<>(scripts); }
 
     /**
      * Get a reference to the scriptFactory.
      *
      * @return a reference to the scriptFactory
      */
     protected ScriptEngineManager getScriptFactory() { return scriptFactory; }
 
     /**
      * Get a reference to the JavaScriptHelper
      *
      * @return a reference to the JavaScriptHelper
      */
     protected JavaScriptHelper getJavaScriptHelper() { return jsHelper; }
 
     /**
      * Get a reference to the GlobalVariables Properties
      *
      * @return a reference to the GlobalVariables Properties
      */
     protected TypedProperties getGlobalVariables() { return globalVariables; }
 
     /**
      * Get the name of the directory where scripts should be stored.
      *
      * @return The name of the directory where scripts should be stored.
      */
     protected String getScriptDir() { return scriptDir; }
 
     /** Reload all scripts */
     public void rehash() {
         for (final ScriptEngineWrapper engine : scripts.values()) {
             engine.reload();
         }
         // Advise the Garbage collector that now would be a good time to run
         System.gc();
     }
 
     /**
      * Call a function in all scripts.
      *
      * @param functionName Name of function
      * @param args Arguments for function
      */
     private void callFunctionAll(final String functionName, final Object... args) {
         for (final ScriptEngineWrapper engine : scripts.values()) {
             engine.callFunction(functionName, args);
         }
     }
 
     /**
      * Unload a script file.
      *
      * @param scriptFilename Path to script
      */
     public void unloadScript(final String scriptFilename) {
         if (scripts.containsKey(scriptFilename)) {
             // Tell it that its about to be unloaded.
             (scripts.get(scriptFilename)).callFunction("onUnload");
             // Remove the script
             scripts.remove(scriptFilename);
             // Advise the Garbage collector that now would be a good time to run
             System.gc();
         }
     }
 
     /**
      * Load a script file into a new jsEngine
      *
      * @param scriptFilename Path to script
      * @return true for Success (or already loaded), false for fail. (Fail occurs if script already exists, or if it has errors)
      */
     public boolean loadScript(final String scriptFilename) {
         if (!scripts.containsKey(scriptFilename)) {
             try {
                 final ScriptEngineWrapper wrapper = new ScriptEngineWrapper(this, scriptFilename);
                 scripts.put(scriptFilename, wrapper);
             } catch (FileNotFoundException | ScriptException e) {
                 Logger.userError(ErrorLevel.LOW, "Error loading '"+scriptFilename+"': "+e.getMessage(), e);
                 return false;
             }
         }
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public ValidationResponse checkPrerequisites() {
         if (getScriptFactory().getEngineByName("JavaScript") == null) {
             return new ValidationResponse("JavaScript Scripting Engine not found.");
         } else {
             return new ValidationResponse();
         }
     }
 
     /**
      * Get the reason for checkPrerequisites failing.
      *
      * @return Human-Readble reason for checkPrerequisites failing.
      */
     public String checkPrerequisitesReason() {
         if (getScriptFactory().getEngineByName("JavaScript") == null) {
             return "JavaScript Scripting Engine not found.";
         } else {
             return "";
         }
     }
 }
 
