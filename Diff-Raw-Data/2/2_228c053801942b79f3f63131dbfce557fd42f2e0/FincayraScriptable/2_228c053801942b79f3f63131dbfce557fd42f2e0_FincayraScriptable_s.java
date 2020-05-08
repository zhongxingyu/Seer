 package org.innobuilt.fincayra;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.ImporterTopLevel;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FincayraScriptable extends ImporterTopLevel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static Logger LOGGER = LoggerFactory.getLogger(FincayraScriptable.class);
 	private MergeEngine mergeEngine = null;
 	
 	public FincayraScriptable(MergeEngine mergeEngine) {
 		this.mergeEngine = mergeEngine;
 	}
 	
 	public MergeEngine getMergeEngine() {
 		return mergeEngine;
 	}
 
 	// TODO also create a method called error, which will work like extend
 	// except it will not process any more javascript
 	// after the page it's loading and companion javascript have executed
 
 	/**
 	 * Get the logger object
 	 */
 	public static Logger logger(Context cx, Scriptable scope, Object[] args,
 			Function funObj) {
 		return LOGGER;
 	}
 
 	/**
 	 * Print the string values of its arguments.
 	 * 
 	 * This method is defined as a JavaScript function. Note that its arguments
 	 * are of the "varargs" form, which allows it to handle an arbitrary number
 	 * of arguments supplied to the JavaScript function.
 	 * 
 	 */
 	public static void print(Context cx, Scriptable scope, Object[] args,
 			Function funObj) {
 		for (int i = 0; i < args.length; i++) {
 			if (i > 0)
 				System.out.print(" ");
 
 			// Convert the arbitrary JavaScript value into a string form.
 			String s = Context.toString(args[i]);
 
 			System.out.print(StringEscapeUtils.unescapeXml(s));
 		}
 		System.out.println();
 	}
 
 	/**
 	 * Load and execute a set of JavaScript source files.
 	 * 
 	 * This method is defined as a JavaScript function.
 	 * @throws IOException 
 	 * @throws RhinoException 
 	 * 
 	 */
 	public static void load(Context cx, Scriptable scope, Object[] args,
 			Function funObj) throws RhinoException, IOException {
 		FincayraScriptable shell = (FincayraScriptable) getTopLevelScope(scope);
 		for (int i = 0; i < args.length; i++) {
 			String jsFile = Context.toString(args[i]);
 			if (!jsFile.startsWith("/")) {
 				jsFile = ((FincayraScriptable)shell).getMergeEngine().getJsDir()
 				+ "/" + jsFile;
 			}
 			
 			loadFile(cx, shell, jsFile);
 		}
 	}
 	
 	public static boolean hasProperty(Context cx, Scriptable scope, Object[] args, Function funObj) {
 		return ScriptableObject.hasProperty((Scriptable)args[0], (String)args[1]);
 	}
 	
 	public static void loadFile(Context cx, Scriptable scope, String jsFile) throws RhinoException, IOException {
 		LOGGER.debug("Loading file: {}", jsFile);
 		Reader reader = null;
 		try {
 			reader = getReaderForFile(jsFile);
 			cx.evaluateReader(scope, reader, jsFile, 1, null);
 		} catch (IOException e) {
 			LOGGER.error("Problem openeing file: {}", jsFile);
 			throw e;
 		} catch (RhinoException re) {
 			// Some form of JavaScript error.
 			LOGGER.error("Evaluator Exception: {}", re.getMessage());
 			throw re;
 		} finally {
 			if (reader != null) reader.close();
 		}
 	}
 	
 	public static void loadString(Context cx, Scriptable scope, String js, String name) throws RhinoException {
 		LOGGER.debug("Evaluating: {}", name);
 		try {
 			cx.evaluateString(scope, js, name, 1, null);
 		}  catch (RhinoException re) {
 			// Some form of JavaScript error.
			LOGGER.error("Evaluator Exception: {}", re.getMessage());
 			throw re;
 		}
 	}
 
 	public static Reader getReaderForFile(String file) throws FileNotFoundException {
 		InputStream is = null;
 		is = new FileInputStream(file);
 		Reader reader = new InputStreamReader(is);
 		return reader;
 	}
 
 	public static FincayraContext getFincayraContext(Scriptable scope) {
 		return (FincayraContext) Context.jsToJava(scope.get("context", scope), FincayraContext.class);
 	}
 
 }
