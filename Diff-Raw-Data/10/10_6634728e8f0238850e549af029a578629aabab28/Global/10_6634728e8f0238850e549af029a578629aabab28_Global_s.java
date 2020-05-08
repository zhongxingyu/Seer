 package org.monkeyscript.lite;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 
 import org.mozilla.javascript.*;
 
 public class Global extends ImporterTopLevel {
 	
 	public Global() {}
 	public Global(Context cx) { init(cx); }
 	public void init(ContextFactory factory) {
 		factory.call(new ContextAction() {
 			public Object run(Context cx) {
 				init(cx);
 				return null;
 			}
 		});
 	}
 	
 	public void init(Context cx) {
 		boolean sealed = cx.isSealed();
 		initStandardObjects( cx, sealed );
 		defineProperty("global", this, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
 		Kernel.init( this, sealed );
 		NativeBlob.init( this, sealed );
 		//NativeBuffer.init( this, sealed );
 		String[] functions = {
 			"exec",
 			"globalExec",
 			"include",
 			"includeOnce",
 			"includeIfExists",
 			"print",
 		};
 		defineFunctionProperties(functions, Global.class, ScriptableObject.DONTENUM);
 		try {
 			defineProperty("__FILE__", this, Global.class.getMethod("js_FILE_", ScriptRuntime.ScriptableObjectClass), null, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
 			defineProperty("__DIR__", this, Global.class.getMethod("js_DIR_", ScriptRuntime.ScriptableObjectClass), null, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
 			defineProperty("__LINE__", this, Global.class.getMethod("js_LINE_", ScriptRuntime.ScriptableObjectClass), null, ScriptableObject.DONTENUM | ScriptableObject.READONLY);
 		} catch ( NoSuchMethodException e ) {
 			System.out.println(e);
 			System.exit(1);
 		}
 	}
 	
 	public static Object print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
 		PrintStream out = System.out;
 		for (int i=0; i < args.length; i++) {
 			if (i > 0)
 				out.print(" ");
 
 			// Convert the arbitrary JavaScript value into a string form.
 			String s = Context.toString(args[i]);
 
 			out.print(s);
 		}
 		out.println();
 		return Context.getUndefinedValue();
 	}
 	
 	protected static final int SCRIPT_RESULT = 1;
 	protected static final int IF_EXISTS     = 2;
 	protected static final int ADD_INCLUDED  = 4;
 	protected static final int NOT_INCLUDED  = 8;
 	
 	private static Object runScript( Context cx, Scriptable thisObj, Object[] args, Function funObj, int flags ) {
 		if ( args.length == 0 )
 			throw ScriptRuntime.typeError("No script name");
 		String scriptFileName = (String)args[0];
 		File scriptFile = new File(scriptFileName);
 		try {
 			if ( scriptFile.canRead() ) {
 				if ( (flags & NOT_INCLUDED ) != 0 ) {
					Scriptable included = getIncluded(thisObj);
 					if ( ScriptableObject.callMethod(included, "has", new Object[] { scriptFile.getCanonicalPath() }) == Boolean.TRUE )
 						return Boolean.TRUE;
 				}
 				FileInputStream is = new FileInputStream(scriptFile);
 				ScriptReader in = new ScriptReader(is);
 				if ( (flags & ADD_INCLUDED) != 0 ) {
					Scriptable included = getIncluded(thisObj);
 					ScriptableObject.callMethod(included, "push", new Object[] { scriptFile.getCanonicalPath() });
 				}
				Object res = cx.evaluateReader( thisObj, in, scriptFile.getAbsolutePath(), in.getFirstLine(), null );
 				return (flags & SCRIPT_RESULT) != 0 ? res : Boolean.TRUE;
 			} else {
 				if ( (flags & IF_EXISTS) != 0 )
 					return false;
 				throw jsIOError("Script "+scriptFileName+" does not exist or cannot be read");
 			}
 		} catch( FileNotFoundException e ) {
 			if ( (flags & IF_EXISTS) != 0 )
 				return false;
 			throw jsIOError("Script "+scriptFileName+" does not exist");
 		} catch( UnsupportedEncodingException e ) {
 			throw jsIOError("Unsupported character encoding: " + e.getMessage());
 		} catch( IOException e ) {
 			throw jsIOError(e.getMessage());
 		}
 		//return Boolean.FALSE;
 	}
 	
 	public static Object exec( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
 		// @todo
 		throw ScriptRuntime.constructError("Error", "Unimplemented");
 	}
 	
 	public static Object globalExec( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
 		return runScript(cx, thisObj, args, funObj, SCRIPT_RESULT);
 	}
 	
 	public static Object include( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
 		return runScript(cx, thisObj, args, funObj, ADD_INCLUDED);
 	}
 	
 	public static Object includeOnce( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
 		return runScript(cx, thisObj, args, funObj, ADD_INCLUDED | NOT_INCLUDED);
 	}
 	
 	public static Object includeIfExists( Context cx, Scriptable thisObj, Object[] args, Function funObj ) {
 		return runScript(cx, thisObj, args, funObj, IF_EXISTS);
 	}
 	
 	public static Scriptable getIncluded( Scriptable scope ) {
 		Object monkeyscript = ScriptRuntime.getTopLevelProp(scope, "monkeyscript");
 		if (!(monkeyscript instanceof Scriptable))
 			throw new ScriptPanic("monkeyscript global does not exist");
 		Object included = ScriptableObject.getProperty((Scriptable)monkeyscript, "included");
 		if (!(included instanceof Scriptable))
 			throw new ScriptPanic("monkeyscript.included does not exist");
 		if (!ScriptRuntime.isArrayObject(included))
 			throw new ScriptPanic("monkeyscript.included is not an array");
 		return (Scriptable)included;
 	}
 	
 	public static EcmaError jsIOError(String message) {
 		return ScriptRuntime.constructError("IOError", message);
 	}
 	
 	public static String js_FILE_(ScriptableObject obj) {
 		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
 		//int[] linep = new int[1];
 		//String fileName = Context.getSourcePositionFromStackPublic(linep);
 		//return fileName;
 		return ScriptRuntime.constructError("Error", "").sourceName();
 	}
 	
 	public static String js_DIR_(ScriptableObject obj) {
 		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
 		//int[] linep = new int[1];
 		//String fileName = Context.getSourcePositionFromStackPublic(linep);
 		//return (new File(fileName)).getParent();
 		return (new File(ScriptRuntime.constructError("Error", "").sourceName())).getParent();
 	}
 	
 	public static int js_LINE_(ScriptableObject obj) {
 		//This is the correct way to do this, but doesn't work because it's protected and can't be proxied
 		//int[] linep = new int[1];
 		//String fileName = Context.getSourcePositionFromStackPublic(linep);
 		//return linep[0];
 		return ScriptRuntime.constructError("Error", "").lineNumber();
 	}
 	
 }
