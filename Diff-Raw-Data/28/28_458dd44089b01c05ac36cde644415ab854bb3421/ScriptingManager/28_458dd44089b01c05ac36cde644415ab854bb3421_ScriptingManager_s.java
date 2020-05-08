 package com.vbitz.MinecraftScript;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.minecraft.src.EntityPlayer;
 
 import org.mozilla.javascript.ClassShutter;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.FunctionObject;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.Undefined;
 
 public class ScriptingManager {
 	private static Context mcJavascriptContext = null;
 	private static ScriptableObject mcJavascriptScope = null;
 	
 	private static File _scriptsDirectory = null;
 	
 	private static EntityPlayer _scriptRunner = null;
 	
 	public static Object runFunction(Function func, Object... args) {
 		return func.call(mcJavascriptContext, mcJavascriptScope, mcJavascriptScope, args);
 	}
 	
 	public static void enterContext() {
 		if (Context.getCurrentContext() != null) {
 			Context.exit();
 		}
 		ContextFactory.getGlobal().enterContext(mcJavascriptContext);
 	}
 	
 	public static void exitContext() {
 		Context.exit();
 	}
 	
 	public static void addGlobal(String jsName, String methodName, Class<?>[] args) throws SecurityException, NoSuchMethodException {
 		mcJavascriptScope.put(jsName, mcJavascriptScope, new FunctionObject(jsName, ScriptingGlobals.class.getMethod(methodName, args), mcJavascriptScope));
 	}
 	
 	public static void loadScriptEngine() {
 		mcJavascriptContext = Context.enter();
 		mcJavascriptContext.setClassShutter(new MinecraftScriptClassShutter());
 		mcJavascriptScope = mcJavascriptContext.initStandardObjects();
 		try {
 			addGlobal("me", "getScriptRunnerJS", null);
 			addGlobal("world", "getWorldJS", null);
 			addGlobal("vector", "newVectorJS", new Class<?>[] { double.class, double.class, double.class});
 			addGlobal("block", "getBlockJS", new Class<?>[] { int.class });
 			addGlobal("log", "logJS", new Class<?>[] { Object.class });
 			addGlobal("chat", "sendChatJS", new Class<?>[] { String.class });
 			addGlobal("itemId", "getItemIdJS", new Class<?>[] { String.class });
 			addGlobal("player", "playerJS", new Class<?>[] { String.class });
 			addGlobal("registerCommand", "registerCommandJS", new Class<?>[] { String.class, Function.class });
 			addGlobal("registerWebpoint", "registerWebpointJS", new Class<?>[] { String.class, Function.class });
 			addGlobal("difficulty", "setDifficultyJS", new Class<?>[] { String.class });
 			addGlobal("addSmeltingRecipe", "addSmeltingRecipeJS", new Class<?>[] { int.class, int.class, int.class });
 		} catch (Exception e) {
 			e.printStackTrace();
 			MinecraftScriptMod.getLogger().severe("Could not load globals");
 		}
 		MinecraftScriptMod.getLogger().info("Loaded Script Engine");
 		Context.exit();
 	}
 
 	public static void loadAllScripts(File scriptsDirectory) {
 		_scriptsDirectory = scriptsDirectory;
 		
 		enterContext();
 		
 		mcJavascriptScope.put("isPreload", mcJavascriptScope, true);
 		Logger.getLogger("MinecraftScriptMod").info("Loading Startup Scripts");
 		for (File f : scriptsDirectory.listFiles()) {
 			try {
 				MinecraftScriptMod.getLogger().info("Loading " + f.toString());
 				FileReader fr = new FileReader(f);
 				mcJavascriptContext.evaluateReader(mcJavascriptScope, fr, f.getName(), 0, null);
 			} catch (FileNotFoundException e) {
 				MinecraftScriptMod.getLogger().severe("Could not find " + f.getName());
 			} catch (IOException e) {
 				MinecraftScriptMod.getLogger().severe(e.toString());
 			}
 		}
 		mcJavascriptScope.put("isPreload", mcJavascriptScope, false);
 		
 		exitContext();
 	}
 
 	public static Object runString(String string, EntityPlayer ply) {
 		Object ret = null;
 		enterContext();
 		_scriptRunner = ply;
 		ret = mcJavascriptContext.compileString(string, "command", 0, null).exec(mcJavascriptContext, mcJavascriptScope);
 		_scriptRunner = null;
 		exitContext();
 		return ret;
 	}
 	
 	public static void doFile(String filename) throws IOException {
 		enterContext();
 		File fullPath = new File(_scriptsDirectory, filename);
 		FileReader fr = new FileReader(fullPath);
 		mcJavascriptContext.evaluateReader(mcJavascriptScope, fr, fullPath.getName(), 0, null);
 		exitContext();
 	}
 	
 	public static EntityPlayer getScriptRunner() {
 		return _scriptRunner;
 	}
 	
 	public static void setScriptRunner(EntityPlayer ply) {
 		_scriptRunner = ply;
 	}
 	
 	public static String getTidyOutput(Object obj) {
 		if (obj instanceof Undefined) {
 			return "";
 		} else if (obj instanceof String) {
 			return (String)obj;
 		} else if (obj instanceof NativeArray) {
 			String ret = "[";
 			for (Object object : ((NativeArray) obj).getAllIds()) {
 				ret += getTidyOutput(((NativeArray) obj).get(object)) + ", ";
 			}
 			return ret + "]";
 		} else if (obj instanceof NativeJavaObject) {
 			NativeJavaObject natObj = (NativeJavaObject)obj;
 			if (!(natObj.unwrap() instanceof String)) {
 				return "JavaObject [" + natObj.getClassName() + "]";
 			} else {
 				return (String) natObj.unwrap();
 			}
 		} else {
 			return obj.toString();
 		}
 	}
 }
