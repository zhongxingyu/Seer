 package gino;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.NativeJavaClass;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Functions {
 
   private static final Logger logger = LoggerFactory.getLogger(Functions.class);
 
   public static void defineFunctions(ScriptableObject scope) {
     scope.defineFunctionProperties(new String[] {
         "eval",
         "getConstructor",
         "getResource",
         "getResourceAsStream",
         "getResourceAsString",
         "getResourceAsLines",
         "getResourceListing",
         "isKindOf",
         "jscontext",
         "jsonQuote",
         "load",
         "sync",
         "tryLoad",
         "unhex"
     }, Functions.class, 0);
   }
 
   public static Object eval(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length == 0) {
       logInvalidArgs("eval");
       return Context.getUndefinedValue();
     }
     Scriptable thisScope = thisObj == null ? funObj : thisObj;
     Object result = null;
     for (Object arg : args)
       result = cx.evaluateString(thisScope, Context.toString(arg), null, 0, null);
     return result;
   }
 
   public static Object getConstructor(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length == 0) {
       logInvalidArgs("getConstructor");
       return Context.getUndefinedValue();
     }
     try {
       Object classObj = args[0];
       if (classObj instanceof NativeJavaClass)
         classObj = ((NativeJavaClass) classObj).unwrap();
       if (!(classObj instanceof Class<?>))
         throw new Exception(MessageFormat.format("getConstructor: argument {0} is expected to be a class", 0));
       List<Class<?>> ctorArgs = new LinkedList<Class<?>>();
       for (int i = 1; i < args.length; i++) {
         Object c = args[i];
         if (c instanceof NativeJavaClass)
           c = ((NativeJavaClass) c).unwrap();
         if (!(c instanceof Class<?>))
           throw new Exception(MessageFormat.format("getConstructor: argument {0} is expected to be a class", i));
         ctorArgs.add((Class<?>) c);
       }
       return ((Class<?>) classObj).getConstructor(ctorArgs.toArray(new Class<?>[0]));
     } catch (Throwable x) {
       Context.throwAsScriptRuntimeEx(x);
     }
     return Context.getUndefinedValue();
   }
 
   public static Object getResource(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length != 1) {
       logInvalidArgs("getResource");
       return Context.getUndefinedValue();
     }
     ClassLoader loader = cx.getApplicationClassLoader();
     return loader.getResource(Context.toString(args[0]));
   }
 
   public static Object getResourceAsLines(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length < 1 || args.length > 2) {
       logInvalidArgs("getResourceAsLines");
       return Context.getUndefinedValue();
     }
     ClassLoader loader = cx.getApplicationClassLoader();
     String encoding = args.length == 2 ? Context.toString(args[1]) : "UTF-8";
     try {
       InputStream stm = loader.getResourceAsStream(Context.toString(args[0]));
       if (stm != null) {
         List<String> result;
         try {
           result = org.apache.commons.io.IOUtils.readLines(stm, encoding);
         } finally {
           stm.close();
         }
         return Context.toObject(result.toArray(new String[result.size()]), thisObj);
       }
     } catch (Throwable x) {
       Context.throwAsScriptRuntimeEx(x);
     }
     return Context.getUndefinedValue();
   }
 
   public static Object getResourceAsStream(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length != 1) {
       logInvalidArgs("getResourceAsStream");
       return Context.getUndefinedValue();
     }
     ClassLoader loader = cx.getApplicationClassLoader();
     Object stm = loader.getResourceAsStream(Context.toString(args[0]));
     return stm == null ? null : Context.toObject(stm, thisObj);
   }
 
   public static Object getResourceAsString(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length < 1 || args.length > 2) {
       logInvalidArgs("getResourceAsString");
       return Context.getUndefinedValue();
     }
     ClassLoader loader = cx.getApplicationClassLoader();
     String encoding = args.length == 2 ? Context.toString(args[1]) : "UTF-8";
     try {
       InputStream stm = loader.getResourceAsStream(Context.toString(args[0]));
       if (stm != null) {
         String result;
         try {
           result = org.apache.commons.io.IOUtils.toString(stm, encoding);
         } finally {
           stm.close();
         }
         return Context.toObject(result, thisObj);
       }
     } catch (Throwable x) {
       Context.throwAsScriptRuntimeEx(x);
     }
     return Context.getUndefinedValue();
   }
 
   public static Object getResourceListing(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length != 1) {
       logInvalidArgs("getResourceListing");
       return Context.getUndefinedValue();
     }
     try {
       ClassLoader loader = cx.getApplicationClassLoader();
 
       String resourceName = Context.toString(args[0]);
 
       URL dirURL = loader.getResource(resourceName);
 
       if (dirURL != null && dirURL.getProtocol().equals("file"))
         return Context.toObject(new File(dirURL.toURI()).list(), thisObj);
 
       List<String> result = new LinkedList<String>();
       String classpath = System.getProperty("java.class.path");
       String pathSeparator = System.getProperty("path.separator");
       StringTokenizer st = new StringTokenizer(classpath, pathSeparator);
       while (st.hasMoreTokens()) {
         File file = new File(st.nextToken());
         if (file.exists() && file.getName().endsWith(".jar"))
           getResourceListing(result, file, resourceName);
       }
       return result.toArray(new String[result.size()]);
     } catch (Throwable x) {
       Context.throwAsScriptRuntimeEx(x);
     }
     return Context.getUndefinedValue();
   }
 
   private static void getResourceListing(List<String> list, File jarFile, String resourceName) throws IOException {
     File tempJarsFolder = null;
     JarFile jar = new JarFile(jarFile);
     try {
       Enumeration<JarEntry> entries = jar.entries();
       while (entries.hasMoreElements()) {
         JarEntry jarEntry = entries.nextElement();
         String name = jarEntry.getName();
         if (name.startsWith(resourceName) && !name.endsWith("/")) {
           if (name.startsWith(resourceName + "/"))
             list.add(name.substring(resourceName.length() + 1));
           else
             list.add(name.substring(resourceName.length()));
         }
         else if (name.endsWith(".jar")) {
           if (tempJarsFolder == null) {
             String fileMd5;
             InputStream stm = new BufferedInputStream(new FileInputStream(jarFile));
             try {
               fileMd5 = DigestUtils.md5Hex(stm);
             } finally {
               stm.close();
             }
             tempJarsFolder = new File(System.getProperty("java.io.tmpdir"), jarFile.getName() + "-" + fileMd5);
           }
           File outFile = new File(tempJarsFolder, name);
           if (!outFile.exists()) {
             outFile.getParentFile().mkdirs();
             InputStream ins = jar.getInputStream(jarEntry);
             OutputStream outs = new BufferedOutputStream(new FileOutputStream(outFile));
             try {
               IOUtils.copy(ins, outs);
             } finally {
               outs.close();
               ins.close();
             }
           }
           getResourceListing(list, outFile, resourceName);
         }
       }
     } finally {
       jar.close();
     }
   }
 
   public static Object isKindOf(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length != 2) {
       logInvalidArgs("isKindOf");
       return false;
     }
     Object obj = args[0];
     if (obj instanceof NativeJavaObject)
       obj = ((NativeJavaObject) obj).unwrap();
     Object classObj = args[1];
     if (classObj instanceof NativeArray) {
       NativeArray classes = (NativeArray) classObj;
       for (int i = 0; i < classes.getLength(); i++) {
         Object clazz = classes.get(i, classes);
         if (clazz instanceof NativeJavaClass)
           clazz = ((NativeJavaClass) clazz).unwrap();
         if (clazz instanceof Class<?> && ((Class<?>) clazz).isInstance(obj))
           return true;
       }
       return false;
     }
     if (classObj instanceof NativeJavaClass)
       classObj = ((NativeJavaClass) classObj).unwrap();
     return classObj instanceof Class<?> && ((Class<?>) classObj).isInstance(obj);
   }
 
   public static Object jscontext(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     return cx;
   }
 
   public static Object jsonQuote(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length != 1 && args.length != 2) {
       logInvalidArgs("jsonQuote");
       return Context.getUndefinedValue();
     }
     Object result = Context.getUndefinedValue();
     try {
       if (args.length == 2) {
         Object writer = args[1] instanceof NativeJavaObject ? ((NativeJavaObject) args[1]).unwrap() : null;
         if (writer instanceof Writer) {
           jsonQuote_(Context.toString(args[0]), (Writer) writer);
           return result;
         }
       }
       StringWriter writer = new StringWriter();
       try {
         jsonQuote_(Context.toString(args[0]), writer);
       } finally {
         writer.close();
       }
       result = Context.javaToJS(writer.toString(), thisObj);
     } catch (Throwable x) {
       Context.throwAsScriptRuntimeEx(x);
     }
     return result;
   }
 
   public static void jsonQuote_(String str, Writer writer) throws IOException {
     writer.write('\"');
     for (int i = 0; i < str.length(); i++) {
       int ch = str.codePointAt(i);
       if (ch == '\"')
         writer.write("\\\"");
       else if (ch == '\\')
         writer.write("\\\\");
       else if (ch == '\b')
         writer.write("\\b");
       else if (ch == '\f')
         writer.write("\\f");
       else if (ch == '\n')
         writer.write("\\n");
       else if (ch == '\r')
         writer.write("\\r");
       else if (ch == '\t')
         writer.write("\\t");
       else if (Character.isISOControl(ch)) {
         writer.write("\\u");
         writer.write(StringUtils.leftPad(Integer.toHexString(ch), 4, '0'));
       } else
         writer.write(Character.toChars(ch));
     }
     writer.write('\"');
   }
 
   public static Object load(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length == 0) {
       logInvalidArgs("load");
       return Context.getUndefinedValue();
     }
     Object result = Context.getUndefinedValue();
     try {
       for (Object arg : args) {
         Scriptable thisScope = thisObj == null ? funObj : thisObj;
         result = loadScript(cx, thisScope, arg, true);
       }
     } catch (Throwable x) {
       Context.throwAsScriptRuntimeEx(x);
     }
     return result;
   }
 
   public static Object loadScript(Context cx, Scriptable scope, Object scriptFile, boolean throwExceptionWhenNotFound) throws IOException {
     String scriptName;
     String script;
     File file = scriptFile instanceof java.io.File ? (File) scriptFile : new File(Context.toString(scriptFile));
     if (file.exists()) {
       scriptName = file.getPath();
       InputStream ins = new BufferedInputStream(new FileInputStream(file));
       try {
         script = IOUtils.toString(ins, "UTF-8");
       } finally {
         ins.close();
       }
     }
     else {
       scriptName = scriptFile instanceof java.io.File ? ((File) scriptFile).getPath() : Context.toString(scriptFile);
      scriptName = scriptName.replace('\\', '/');
       InputStream ins = cx.getApplicationClassLoader().getResourceAsStream(scriptName);
       if (ins == null) {
         if (throwExceptionWhenNotFound)
           throw new FileNotFoundException(MessageFormat.format("Script ''{0}'' not found", scriptName));
         return null;
       }
       try {
         script = IOUtils.toString(ins, "UTF-8");
       } finally {
         ins.close();
       }
     }
     return cx.evaluateString(scope, script, scriptName, 1, null);
   }
 
   private static void logInvalidArgs(String functionName) {
     logger.warn("Called '{}' with incorrect arguments", functionName);
   }
 
   public static Object sync(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || !(args.length == 2 || args.length == 3) || args[0] == null || !(args[1] instanceof Function)) {
       logInvalidArgs("sync");
       return false;
     }
     Object[] delegatedArgs;
     if (args.length == 2)
       delegatedArgs = new Object[0];
     else if (args[2] instanceof Object[])
       delegatedArgs = (Object[]) args[2];
     else if (args[2] instanceof NativeArray)
       delegatedArgs = ((NativeArray) args[2]).toArray();
     else
       delegatedArgs = new Object[] { args[2] };
     Scriptable thisScope = thisObj == null ? funObj : thisObj;
     Object result;
     synchronized (args[0]) {
       result = ((Function) args[1]).call(cx, thisScope, thisObj, delegatedArgs);
     }
     return result;
   }
 
   public static Object tryLoad(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length == 0) {
       logInvalidArgs("tryLoad");
       return Context.getUndefinedValue();
     }
     Object result = Context.getUndefinedValue();
     try {
       for (Object arg : args) {
         Scriptable thisScope = thisObj == null ? funObj : thisObj;
         result = loadScript(cx, thisScope, arg, false);
       }
     } catch (Throwable x) {
       Context.throwAsScriptRuntimeEx(x);
     }
     return result;
   }
 
   public static Object unhex(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
     if (args == null || args.length == 0) {
       logInvalidArgs("unhex");
       return Context.getUndefinedValue();
     }
     if (args.length == 1)
       return unhex(Context.toString(args[0]));
     List<Object> list = new LinkedList<Object>();
     for (Object arg : args)
       list.add(unhex(Context.toString(arg)));
     return Context.javaToJS(list, thisObj);
   }
 
   private static byte[] unhex(String s) {
     int len = s.length();
     byte[] data = new byte[len / 2];
     int j = 0;
     int i = 0;
     while (i < len - 1) {
       int v1 = Character.digit(s.charAt(i), 16);
       int v2 = Character.digit(s.charAt(i + 1), 16);
       if (v1 < 0 || v2 < 0)
         ++i;
       else {
         data[j++] = (byte) ((v1 << 4) + v2);
         i += 2;
       }
     }
     if (j != data.length)
       data = Arrays.copyOf(data, j);
     return data;
   }
 }
