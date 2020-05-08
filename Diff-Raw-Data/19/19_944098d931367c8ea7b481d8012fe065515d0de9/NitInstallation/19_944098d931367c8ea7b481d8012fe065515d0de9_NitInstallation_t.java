 package org.nitlanguage.ndt.core.plugin;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.nitlanguage.ndt.core.PluginParams;
 import org.osgi.service.prefs.BackingStoreException;
 
 //import com.sun.xml.internal.ws.util.StringUtils;
 
 public class NitInstallation {
 	// The shared nit installation instance
 	private static NitInstallation installation;
 	
 	// Root path string
 	public static final String ROOT_PATH_ID = "root";	
 	// Standard library path
 	public static final String STDLIB_FOLDER_ID = "lib";
 	// Compiler path string
 	public static final String COMPILER_PATH_ID = "nitc";	
 	// Interpreter path string
 	public static final String INTERPRETER_PATHS_ID = "nit";
 	// Debugger path string
 	public static final String DEBUGGER_PATH_ID = "nitd";
 	
 	private String root = null;
 	private String compiler = null;
 	private String debugger = null;
 	private String interpreter = null;
 	private String stdlib = null;
 	private IPreferenceStore store = null;
 	IEclipsePreferences prefs;
 	
 	public static NitInstallation getInstance(IPreferenceStore store) {
 		if(installation == null){
 			installation = new NitInstallation(store);
 		}
 		return installation;
 	}
 	
	private NitInstallation(IPreferenceStore store){	
 		this.store = store;
 		init();
 	}
 	
 	public boolean save(){
 		  try {
 			  //prefs are automatically flushed during a plugin's "super.stop()".
 			  prefs.flush();
 			  return true;
 		  } catch(BackingStoreException e) {
 			  //TODO write a real exception handler.
 			  e.printStackTrace();
 			  return false;
 		  }
 	}
 	public boolean clear(){
 		root = compiler = debugger = interpreter = stdlib = "";
 		prefs.remove(ROOT_PATH_ID);
 		prefs.remove(COMPILER_PATH_ID);
 		prefs.remove(DEBUGGER_PATH_ID);
 		prefs.remove(INTERPRETER_PATHS_ID);
 		prefs.remove(STDLIB_FOLDER_ID);
 		return save();
 	}
 	
 	private void init(){
 		prefs = InstanceScope.INSTANCE.getNode(PluginParams.PLUGIN_ID); 
 		root = prefs.get(ROOT_PATH_ID, null);
 		compiler = prefs.get(COMPILER_PATH_ID, null);
 		debugger = prefs.get(DEBUGGER_PATH_ID, null);
 		interpreter = prefs.get(INTERPRETER_PATHS_ID, null);
 		stdlib = prefs.get(STDLIB_FOLDER_ID, null);
 		//binaries = setCompiler(store.getString(NIT_BINARIES_DIR));
 		//compiler = store.getString(COMPILER_PATH_ID);
 		//debugger = store.getString(DEBUGGER_PATH_ID);   
 		//interpreter = store.getString(INTERPRETER_PATHS_ID);
 		//stdlib = store.getString(STDLIB_FOLDER_ID);
 	}
 	
 	public String getRoot(){
 		return root;
 	}
 	
 	public boolean setRoot(String path){
 		if(setParam(ROOT_PATH_ID, path, true)){
 			root = path;
 			return true;
 		}
 		return false;
 	}
 	
 	public String getCompiler(){
 		return compiler;
 	}
 	
 	public boolean setCompiler(String path){
 		if(setParam(COMPILER_PATH_ID, path, false)){
 			compiler = path;
 			return true;
 		}
 		return false;
 	}
 	
 	public String getDebugger(){
 		return debugger;
 	}
 	
 	public boolean setDebugger(String path){
 		if(setParam(DEBUGGER_PATH_ID, path, false)){
 			debugger = path;
 			return true;
 		}
 		return false;
 	}
 	
 	public String getInterpreter(){
 		return interpreter;
 	}
 	
 	public boolean setInterpreter(String path){
 		if(setParam(INTERPRETER_PATHS_ID, path, false)){
 			interpreter = path;
 			return true;
 		}
 		return false;
 	}
 	
 	public String getStdLib(){
 		return stdlib;
 	}
 	
 	public boolean setStdLib(String path){
 		if(setParam(STDLIB_FOLDER_ID, path, true)){
 			stdlib = path;
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean set(String paramId, String path){
 		if(paramId.equals(STDLIB_FOLDER_ID)) 
 			return setStdLib(path);
 		else if(paramId.equals(COMPILER_PATH_ID)) 
 			return setCompiler(path);
 		else if(paramId.equals(INTERPRETER_PATHS_ID))
 			return setInterpreter(path);
 		else if(paramId.equals(DEBUGGER_PATH_ID))
 			return setDebugger(path);
 		else return false;
 	}
 	
 	public boolean isFunctional(){
 		if(compiler == null || stdlib == null) return false;
 		if(compiler.equals("") || stdlib.equals("")) return false;
 		else return true;
 	}
 	
 	private boolean setParam(String param_id, String pathname, boolean isDir) {
 		if(pathname == null) return false;
 		File temp = new File(pathname);
 		if(!temp.exists()) return false;
 		if(!isDir && temp.isFile()){
 			//store.setValue(param_id, pathname);
 			prefs.put(param_id, pathname);
 			return true;
 		} else if(isDir && temp.isDirectory()){
 			//store.setValue(param_id, pathname);
 			prefs.put(param_id, pathname);
 			return true;
 		}
 		return false;
 	}
 }
