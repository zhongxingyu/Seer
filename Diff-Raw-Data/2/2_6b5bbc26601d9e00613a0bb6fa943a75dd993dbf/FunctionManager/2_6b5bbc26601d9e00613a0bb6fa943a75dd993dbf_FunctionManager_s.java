 package jmathlib.core.functions;
 
 import jmathlib.core.tokens.FunctionToken;
 import jmathlib.core.tokens.VariableToken;
 import jmathlib.core.tokens.Token;
 import jmathlib.core.tokens.Expression;
 import jmathlib.core.interpreter.*;
 import java.applet.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 /**Class for storing and managing the functions being used*/
 public class FunctionManager {
   class SystemFileFunctionLoader extends FileFunctionLoader {
     SystemFileFunctionLoader(File _functionDir, boolean _traverseChildren) {
       super(_functionDir, _traverseChildren, true);
     }
   }
     // indicates if FunctionManager is running in an application or an applet
     private boolean runningStandalone;
 
     // different function loaders
     private Vector functionLoaders = new Vector();
 
     // flag for caching of p files
     boolean pFileCachingEnabledB = false;
 
     /*pointer to applet structure. Value is only unequal null if MathLib runs
     inside a web-browser */
     Applet applet = null;
 
     // loader for m files via the web
     WebFunctionLoader webFunctionLoader;
 
     /**Creates the function manager and defines any internal functions
     if this is an application then it creates a class loader to load external functions
     @param runningStandalone = true if the program is running as an application*/
     public FunctionManager(boolean _runningStandalone, Applet _applet) {
        
         runningStandalone = _runningStandalone;
         applet            = _applet;
         
         if (runningStandalone) {
             //Add the predefined (system) function loader for the current directory.
            functionLoaders.add(new SystemFileFunctionLoader(new File("." + File.separator), false));
             //webLoader   = null;
 
             String classPath = System.getProperty("java.class.path");
             //Find and then setup the base JMathlib (pre-defined) functions
             boolean end = false;
             while (!end) {
                 int pos = classPath.indexOf(File.pathSeparator);
                 String path = classPath;
                 if (pos > -1) {
                     path = classPath.substring(0, pos);
                     classPath = classPath.substring(pos + 1);
                 } else {
                     classPath = "";
                     end = true;
                 }
                 File basePath = new File(path, "jmathlib/toolbox");
 
                 if (basePath.exists()) {
                     //Add the predefined (system) function loader for the predefined Mathlib function directory.
                     functionLoaders.add(new SystemFileFunctionLoader(new File(path), true));
                     break; //exit loop (found pre-defined function location)
                 }
             }
         } else {
                 System.out.println("web:"+applet);
                 functionLoaders.add(new WebFunctionLoader(applet.getCodeBase(), ""));
         }
     }
 
     /**
      * For each of the FunctionLoaders, check that any cached functions are up to date. If
      * some are out of date, or have been deleted, ensure that the cache it updated.
      */
     public void checkAndRehashTimeStamps() {
         for (int i = 0; i < this.functionLoaders.size(); i++) {
             FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);
             l.checkAndRehashTimeStamps();
         }
     }
 
     /**find a function
     It checks user functions then external functions then internal functions
     @param token - The function token containing the name of the function
     @return the Function found*/
     public Function findFunction(FunctionToken token) throws java.lang.Exception {
         Function func = null;
         String funcName = token.getName().toLowerCase();
         int index = -1;
 
         //then check the external functions
         try {
             if (runningStandalone) {
                 // JMathlib is running as a standalone application
                 //Search for class, m or p file
                 for (int i = 0; i < functionLoaders.size(); i++) {
                     FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);
 
                     func = l.findFunction(funcName);
                     if (func != null) {
                         return func;
                     }
                 }
 
                 // functions not found (no class or m- or p-file)
                 //
                 if (token.getOperand(0) != null) {
                     ErrorLogger.debugLine("************checking first param****************");
                     //get first parameter
                     //Token first = ((Expression)token.getOperand(0)).getLeft();
                     Token first = token.getOperand(0);
                     ErrorLogger.debugLine("class = " + first.getClass());
                     //if parameter is variable token
                     if ((first instanceof VariableToken) || (first instanceof Expression)) {
                         ErrorLogger.debugLine("************searching for java class***********");
                         String className = "";
                         if (first instanceof VariableToken) {
                             className = ((VariableToken) first).getName();
                         } else if (first instanceof FunctionToken) {
                             className = ((FunctionToken) first).getName();
                         } else {
                             className = first.toString();
                             className = className.substring(0, className.length() - 2);
                         }
 
                         ErrorLogger.debugLine("classname = " + className);
 
                         for (int i = 0; i < functionLoaders.size(); i++) {
                             FileFunctionLoader l = (FileFunctionLoader) functionLoaders.elementAt(i);
                             Class extFunctionClass = l.findOnlyFunctionClass(className);
                             if (extFunctionClass != null) {
                                 ErrorLogger.debugLine("found class " + className);
                                 ReflectionFunctionCall reflect = new ReflectionFunctionCall(extFunctionClass, token.toString());
                                 ErrorLogger.debugLine("+++++func1 " + reflect.toString());
                                 return reflect;
                             }
                         }
                     }
                 }
             } else {
                 // NOT standalone, but APPLET
                 // use webloader
                 //Search for class, m or p file
                 for (int i = 0; i < functionLoaders.size(); i++) {
                     FunctionLoader l = (WebFunctionLoader) functionLoaders.elementAt(i);
 
                     func = l.findFunction(funcName);
                     if (func != null) {
                         return func;
                     }
                 }
 
                 return null;
             } // end webLoader
         } catch (Exception exception) {
             exception.printStackTrace();
         }
 
         return func;
     }
 
     public Function findFunctionByName(String funcName) throws java.lang.Exception {
         FunctionToken token = new FunctionToken(funcName);
         return findFunction(token);
     }
 
     public void clear() {
         ErrorLogger.debugLine("FunctionManager: clear user functions");
         for (int i = 0; i < functionLoaders.size(); i++) {
             FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);
             l.clearCache();
         }
     }
 
     /** set caching of p-file to on of off
      *
      * @param pFileCaching  true= caching of p-files on; false: caching of p-files off
      */
     public void setPFileCaching(boolean pFileCaching) {
         pFileCachingEnabledB = pFileCaching;
         for (int i = 0; i < this.functionLoaders.size(); i++) {
             FunctionLoader l = (FunctionLoader) functionLoaders.elementAt(i);
             l.setPFileCaching(pFileCaching);
         }
     }
 
     /** return whether of not caching of p-files is enabled of not
      *
      * @return status of caching p-files
      */
     public boolean getPFileCaching() {
         return pFileCachingEnabledB;
     }
 
     public int getFunctionLoaderCount() {
         return functionLoaders.size();
     }
 
     public FunctionLoader getFunctionLoader(int index) {
         return (FunctionLoader) functionLoaders.elementAt(index);
     }
 
     public boolean removeFunctionLoader(FunctionLoader loader) {
         if (loader.isSystemLoader())
             throw new IllegalArgumentException("Cannot remove a System Function Loader");
         return functionLoaders.remove(loader);
     }
 
     public boolean addFunctionLoader(FunctionLoader loader) {
         return functionLoaders.add(loader);
     }
 
     public void addFunctionLoaderAt(int index, FunctionLoader loader) {
         functionLoaders.add(index, loader);
     }
 
     public void clearCustomFunctionLoaders() {
         Iterator itr = functionLoaders.iterator();
         while (itr.hasNext()) {
             FunctionLoader fl = (FunctionLoader)itr.next();
             if (!fl.isSystemLoader())
                 itr.remove();
         }
     }
 
     public void setWorkingDirectory(File path) {
         if (runningStandalone) {
             FileFunctionLoader l = (FileFunctionLoader) functionLoaders.get(0);
             l.setBaseDirectory(path);
         }
     }
 
     public File getWorkingDirectory() {
         if (runningStandalone) {
             FileFunctionLoader l = (FileFunctionLoader) functionLoaders.get(0);
             return l.getBaseDirectory();
         }
         return null;
     }
 }
