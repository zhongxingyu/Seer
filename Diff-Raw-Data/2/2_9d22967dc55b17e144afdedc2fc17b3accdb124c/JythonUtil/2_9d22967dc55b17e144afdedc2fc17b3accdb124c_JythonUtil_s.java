 package edu.berkeley.gamesman.util;
 
 import org.python.core.PyList;
 import org.python.core.PyString;
 import org.python.core.__builtin__;
 import org.python.core.PyModule;
 import org.python.core.PyObject;
 import org.python.util.InteractiveConsole;
 import org.python.util.PythonInterpreter;
 
 /**
  * Utility function to load classes from .py files.
  */
 public class JythonUtil {
	static String[] globalPath = {"jython_lib","junk","jobs"};
 	static {
 		PythonInterpreter.initialize(System.getProperties(), null, new String[0]);
 		for (String pathElement : globalPath) {
 			addpath(pathElement);
 		}
 	}
 	private static void addpath(String pathElement){
 		PyObject sys = __builtin__.__import__("sys");
 		PyList syspath = (PyList) sys.__getattr__("path");
 		String homedir = System.getProperty("user.dir");
 		syspath.append(new PyString(homedir + "/" + pathElement));
 		syspath.append(new PyString(homedir + "/../" + pathElement));
 	}
 
 	/**
 	 * Initializes the jython interpreter, and adds all necessary paths.
 	 */
 	public static void init() {
 	}
 	/**
 	 * @param <T>  The type of baseClass that python must implement.
 	 * @param module     The .py file (without the extension) to import from.
 	 * @param className  The name of the python class within (usually the same as module).
 	 * @param baseClass  A Java class/interface that python should extend
 	 * @return A java class that implements baseClass.
 	 * @throws ClassNotFoundException  If the class does not exist, or it does not implement baseClass.
 	 */
 	public static <T> Class<? extends T> getClass(String module, String className, Class<T> baseClass) 
 			throws ClassNotFoundException {
 		PyModule mod = (PyModule) __builtin__.__import__(module);
 		if (mod == null) {
 			throw new ClassNotFoundException("Python module "+module+" could not be loaded!");
 		}
 		PyObject myclass = mod.__getattr__(className);
 		if (myclass == null) {
 			throw new ClassNotFoundException("Module "+module+" has no class named "+className);
 		}
 		Object pythonClass = myclass.__tojava__(Class.class);
 		if (pythonClass != null && pythonClass instanceof Class) {
 			Class<? extends T> castedPythonClass = ((Class<?>)pythonClass).asSubclass(baseClass);
 			return castedPythonClass;
 		} else {
 			throw new ClassNotFoundException("Object "+module+"."+className+" is not a class");
 		}
 	}
 }
