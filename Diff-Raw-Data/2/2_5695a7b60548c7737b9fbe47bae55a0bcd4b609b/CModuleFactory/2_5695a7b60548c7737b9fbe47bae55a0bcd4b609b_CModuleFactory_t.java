 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package fr.prima.gsp.framework;
 
 import com.sun.jna.Callback;
 import com.sun.jna.Function;
 import com.sun.jna.Native;
 import com.sun.jna.NativeLibrary;
 import com.sun.jna.Pointer;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  *
  * @author emonet
  */
 public class CModuleFactory {
 
     {
         // code to avoid freeze in library loading (don't know why)
         // we just access to the Native class before anything else
         if (Native.POINTER_SIZE < 4) {
             System.err.println("This is quite strange");
         }
     }
 
     Map<String, Bundle> bundles = new HashMap<String, Bundle>();
     List<Module> modules = new LinkedList<Module>();
     CModuleFactory() {
 
     }
 
     public Module createModule(String bundleName, String moduleTypeName) {
         //System.err.println("loading " + bundleName);
         Bundle bundle = getBundle(bundleName);
         if (bundle == null) {
             return null;
         }
         //System.err.println("creating "+bundleName+":"+moduleTypeName);
         CModule newModule = createCModule(bundleName, moduleTypeName);
         //System.err.println("done");
         return newModule;
     }
 
     private Bundle getBundle(String bundleName) {
         Bundle bundle = bundles.get(bundleName);
         if (bundle == null) {
             NativeLibrary.addSearchPath(bundleName, ".");
 
             String module_path = System.getenv("GSP_MODULES_PATH");
            if( module_path!=null && !module_path.isEmpty()) {
                 String delims = ":";
                 String[] tokens = module_path.split(delims);
                 for (int i = 0; i < tokens.length; i++) {
                     if(!tokens[i].isEmpty())
                         NativeLibrary.addSearchPath(bundleName, tokens[i]);
                 }
             }
 
             try {
                 bundle = new Bundle(NativeLibrary.getInstance(bundleName));
                 bundles.put(bundleName, bundle);
             } catch (Exception e) {
                 // cannot load
                 // TODO
             }
         }
         return bundle;
     }
 
     private CModule createCModule(String bundleName, String moduleTypeName) {
         Bundle bundle = bundles.get(bundleName);
         CModule module = new CModule(bundle, bundleName, moduleTypeName);
         if (module.that != Pointer.NULL) {
 //            long moduleNumber = module.number;
 //            String moduleId = bundleName + " " + moduleTypeName + " " + moduleNumber;
             modules.add(module);
             return module;
         } else {
             return null;
         }
     }
 
     public static String capFirst(String s) {
         return s.substring(0, 1).toUpperCase() + s.substring(1);
     }
 
     public static String setter(String parameterName) {
         return "set" + capFirst(parameterName);
     }
     
     private static String sep = "__v__";
     private static CppMangler mangler = new CppMangler();
     private class Bundle {
 
         NativeLibrary library;
 
         private Bundle(NativeLibrary library) {
             this.library = library;
         }
 
         private Pointer createModule(String moduleTypeName, FrameworkCallback f) {
             Pointer res = f(moduleTypeName, "create").invokePointer(new Object[]{f});
             if (res != Pointer.NULL) {
                 fOpt(moduleTypeName, "created", res);
             }
             return res;
         }
 
         private void setModuleParameter(String moduleTypeName, Pointer that, String parameterName, Object value) {
             try {
                 f(moduleTypeName, "set" + sep + parameterName).invoke(new Object[]{that, value});
             } catch (UnsatisfiedLinkError err) {
                 f(mangler.mangleVoidMethod(moduleTypeName, setter(parameterName), new Object[]{value})).invoke(new Object[]{that, value});
             }
         }
 
         private void initModule(String moduleTypeName, Pointer that) {
             try {
                 f(moduleTypeName, "init").invoke(new Object[]{that});
             } catch (UnsatisfiedLinkError err) {
                 try {
                     f(mangler.mangleVoidMethod(moduleTypeName, "initModule", new Object[]{})).invoke(new Object[]{that});
                 } catch (UnsatisfiedLinkError err2) {
                     // swallow exception
                 }
             }
         }
 
         private void stopModule(String moduleTypeName, Pointer that) {
             try {
                 f(moduleTypeName, "stop").invoke(new Object[]{that});
             } catch (UnsatisfiedLinkError err) {
                 try {
                     f(mangler.mangleVoidMethod(moduleTypeName, "stopModule", new Object[]{})).invoke(new Object[]{that});
                     f(moduleTypeName, "delete").invoke(new Object[]{that});
                 } catch (UnsatisfiedLinkError err2) {
                     // swallow exception
                 }
             }
         }
 
         private void receiveEvent(String moduleTypeName, Pointer that, String portName, Event e) {
             Object[] information = e.getInformation();
             Object[] allParams = new Object[information.length + 1];
             System.arraycopy(information, 0, allParams, 1, information.length);
             allParams[0] = that;
             try {
                 f(moduleTypeName, "event" + sep + portName).invoke(allParams);
             } catch (UnsatisfiedLinkError err) {
                 f(mangler.mangleVoidMethod(moduleTypeName, portName, information, e.getAdditionalTypeInformation())).invoke(allParams);
             }
         }
 
         private Function f(String functionName) {
             return f(null, functionName);
         }
         private Function f(String prefix, String functionName) {
             return library.getFunction((prefix == null ? "" : prefix + sep) + functionName);
         }
 
         private void fOpt(String prefix, String functionName, Object... args) {
             try {
                 f(prefix, functionName).invoke(args);
             } catch (UnsatisfiedLinkError err) {
                 // swallow exception
             }
         }
 
     }
 
     private static interface FrameworkCallback extends Callback {
         void callback(String commandName, Pointer parameters);
     }
 
     private static class CModule extends BaseAbstractModule implements Module {
 
         Pointer that;
         FrameworkCallback framework;
 
         Bundle bundle;
         String bundleName;
         String moduleTypeName;
         Map<String, String> parameterTypes = new HashMap<String, String>();
         private Set<String> nonParameters = new HashSet<String>() {{
             add("id");
             add("type");
         }};
 
         private CModule(Bundle bundle, String bundleName, String moduleTypeName) {
             this.bundle = bundle;
             this.bundleName = bundleName;
             this.moduleTypeName = moduleTypeName;
             this.framework = new FrameworkCallback() {
                 public void callback(String commandName, Pointer parameters) {
                     cCallback(commandName, parameters.getPointerArray(0));
                 }
             };
             that = bundle.createModule(moduleTypeName, framework);
             if (that == Pointer.NULL) return;
         }
 
         public EventReceiver getEventReceiver(final String portName) {
             return new EventReceiver() {
                 public void receiveEvent(Event e) {
                     bundle.receiveEvent(moduleTypeName, that, portName, e);
                 }
             };
         }
 
         public void addConnector(String portName, EventReceiver eventReceiver) {
             listenersFor(portName).add(eventReceiver);
         }
 
         public void configure(Element conf) {
             NamedNodeMap attributes = conf.getAttributes();
             for (int i = 0; i < attributes.getLength(); i++) {
                 Node node = attributes.item(i);
                 String parameterName = node.getNodeName();
                 if (nonParameters.contains(parameterName)) {
                     continue;
                 }
                 String text = node.getTextContent();
                 setParameter(parameterName, text);
             }
         }
 
         @Override
         protected void initModule() {
             bundle.initModule(moduleTypeName, that);
         }
 
         @Override
         protected void stopModule() {
             bundle.stopModule(moduleTypeName, that);
         }
 
         // callback from C
         private void cCallback(String commandName, Pointer[] parameters) {
             if ("param".equals(commandName)) {
                 parameterTypes.put(parameters[1].getString(0), parameters[0].getString(0));
             } else if ("emit".equals(commandName)) {
                 Object[] eventParameters = new Object[parameters.length / 2];
                 String[] eventParametersTypes = new String[parameters.length / 2];
                 for (int i = 1; i < parameters.length; i += 2) {
                     String type = patchReportedType(parameters[i].getString(0));
                     Object value = getValueFromNative(type, parameters[i + 1]);
                     eventParameters[i / 2] = value;
                     eventParametersTypes[i / 2] = type;
                 }
                 emitNamedEvent(parameters[0].getString(0), eventParameters, eventParametersTypes);
             } else {
                 System.err.println("Unsupported callback type " + commandName);
             }
         }
 
 
 
         // used for xml parameter interpretation
         private static Object getNativeFromString(String type, String text) {
             // could find a way to reuse jna mapping but I didn't managed to :(
             try {
                 return stringToNatives.get(type).toNative(text);
             } catch (NullPointerException ex) {
                 System.err.println("problem with type '" + type + "' to interpret '" + text + "'");
                 throw ex;
             }
         }
 
         private void setParameter(String parameterName, String text) {
             String registeredType = parameterTypes.get(parameterName);
             if (registeredType != null) {
                 Object value = getNativeFromString(registeredType, text);
                 bundle.setModuleParameter(moduleTypeName, that, parameterName, value);
             } else {
                 String type = mangler.findSingleParameterTypeForSingleVoidMethod(bundle.library, moduleTypeName, setter(parameterName));
                 System.err.println("TYPE is "+type);
                 Object value = getNativeFromString(type, text);
                 bundle.setModuleParameter(moduleTypeName, that, parameterName, value);
                 // could cache here
             }
         }
 
 
         private Map<String, String> cTypeToTypeid = new HashMap<String, String>() {{
             put("int", "i");
             put("unsigned int", "j");
             put("float", "f");
             put("long", "l");
             put("double", "d");
             put("char", "c");
             put("bool", "b");
         }};
         private String patchReportedType(String type) {
             boolean isPointer = true;
             if (type.startsWith("A")) {
                 type = type.replaceFirst("A\\d+_", "P");
             } else if (type.startsWith("P")) {
                 type = type.substring(1);
             } else if (type.endsWith("*")) {
                 type = type.substring(0, type.length() - 1);
             } else {
                 isPointer = false;
             }
             type = type.trim();
             String res = cTypeToTypeid.get(type);
             if (res != null) type = res;
             return isPointer ? "P" + type : type;
         }
         private static interface StringToNative {
             Object toNative(String text);
         }
         private static Map<String, StringToNative> stringToNatives = new HashMap<String, StringToNative>() {
             {
                 put("char*", new StringToNative() {
                     public Object toNative(String text) {
                         return text;
                     }
                 });
                 put("float", new StringToNative() {
                     public Object toNative(String text) {
                         return Float.parseFloat(text);
                     }
                 });
                 put("double", new StringToNative() {
                     public Object toNative(String text) {
                         return Double.parseDouble(text);
                     }
                 });
                 put("int", new StringToNative() {
                     public Object toNative(String text) {
                         return Integer.parseInt(text);
                     }
                 });
                 put("long", new StringToNative() {
                     public Object toNative(String text) {
                         return Long.parseLong(text);
                     }
                 });
                 put("bool", new StringToNative() {
                     public Object toNative(String text) {
                         return Boolean.parseBoolean(text);
                     }
                 });
             }
         };
         private static interface NativeInterpreter {
             Object interpret(Pointer pointer);
         }
         private static Map<String, NativeInterpreter> nativeInterpreters = new HashMap<String, NativeInterpreter>() {
             {
                 put("f", new NativeInterpreter() {
                     public Object interpret(Pointer pointer) {
                         return pointer.getFloat(0);
                     }
                 });
                 put("d", new NativeInterpreter() {
                     public Object interpret(Pointer pointer) {
                         return pointer.getDouble(0);
                     }
                 });
                 put("i", new NativeInterpreter() {
                     public Object interpret(Pointer pointer) {
                         return pointer.getInt(0);
                     }
                 });
                 put("j", new NativeInterpreter() {
                     public Object interpret(Pointer pointer) {
                         return pointer.getInt(0);
                     }
                 });
                 put("l", new NativeInterpreter() {
                     public Object interpret(Pointer pointer) {
                         return pointer.getLong(0);
                     }
                 });
             }
         };
         private static Object getValueFromNative(String type, Pointer pointer) {
             if (type.startsWith("P")) {
                 return pointer.getPointer(0);
             }
             // could find a way to reuse jna mapping but I didn't managed to :(
             try {
                 return nativeInterpreters.get(type).interpret(pointer);
             } catch (NullPointerException ex) {
                 throw new RuntimeException("NPE while reading value for native type '" + type + "'", ex);
             }
         }
 
     }
 
 }
