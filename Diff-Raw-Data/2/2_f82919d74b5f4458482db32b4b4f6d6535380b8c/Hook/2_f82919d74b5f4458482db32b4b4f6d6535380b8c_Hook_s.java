 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.web;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeUnit;
 import net.wgr.server.web.handling.WebCommandHandler;
 import net.wgr.utility.GlobalExecutorService;
 import net.wgr.wcp.Command;
 import net.wgr.wcp.CommandException;
 import net.wgr.xenmaster.api.XenApiEntity;
 import net.wgr.xenmaster.controller.Controller;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 1, 2011
  * @author double-u
  */
 public class Hook extends WebCommandHandler {
     
     protected ConcurrentHashMap<Integer, StoredValue> store;
     protected Class clazz = null;
     protected Object current = null;
     protected String className = "", commandName;
     
     public Hook() {
         super("xen");
         
         store = new ConcurrentHashMap<>();
         GlobalExecutorService.get().scheduleAtFixedRate(new Housekeeper(), 1, 5, TimeUnit.MINUTES);
         GlobalExecutorService.get().schedule(new Runnable() {
             
             @Override
             public void run() {
                 Controller.getSession().loginWithPassword("root", "r00tme");
             }
         }, 0, TimeUnit.DAYS);
     }
     
     public Object execute(Command cmd) {
         // Cleanup
         clazz = null;
         current = null;
         className = commandName = "";
         
         GsonBuilder builder = new GsonBuilder();
         builder.registerTypeAdapter(APICall.class, new APICallDecoder());
         Gson gson = builder.create();
         APICall apic = gson.fromJson(cmd.getData(), APICall.class);
         
         return executeInstruction(cmd.getName(), apic.ref, apic.args);
     }
     
     protected Object deserializeToTargetType(Object value, Class type) throws Exception {
         switch (type.getSimpleName()) {
             case "boolean":
                 return Boolean.parseBoolean(value.toString());
             case "int":
                 return Integer.parseInt(value.toString());
             default:
                 if (type.isEnum()) {
                     for (Object enumType : type.getEnumConstants()) {
                         if (enumType.toString().equals(value)) {
                             return enumType;
                         }
                     }
                 } else if (InetAddress.class.isAssignableFrom(type)) {
                     return InetAddress.getByName(value.toString());
                 } else if (XenApiEntity.class.isAssignableFrom(type)) {
                     Constructor c = type.getConstructor(String.class, boolean.class);
                     return c.newInstance(value.toString(), false);
                 }
                 break;
         }
         return value;
     }
     
     protected <T> String createLocalObject(Class<T> clazz, Object[] args) throws Exception {
         T obj = clazz.newInstance();
         
         if (args == null || args.length < 1 || args[0] == null || !(args[0] instanceof Map)) {
             throw new IllegalArgumentException("Illegal arguments map was given");
         }
         for (Map.Entry<String, Object> entry : ((Map<String, Object>) args[0]).entrySet()) {
             String methodName = "set" + entry.getKey().toLowerCase();
             for (Method m : clazz.getMethods()) {
                 if (!m.getName().toLowerCase().equals(methodName)) {
                     continue;
                 }
                 m.invoke(obj, entry.getValue());
             }
         }
         int ref = store.size();
         store.put(ref, new StoredValue(obj));
         return "LocalRef:" + ref;
     }
     
     protected void determineClass(String ref, int index, String[] values) throws Exception {
         String s = values[index];
         int refOpen = s.indexOf('[');
         if (refOpen != -1) {
             className = s.substring(0, refOpen);
             clazz = Class.forName("net.wgr.xenmaster.api." + className);
         } else if (index == values.length - 2) {
             className += s;
             clazz = Class.forName("net.wgr.xenmaster.api." + className);
         } else {
             className += s + '.';
         }
         
         if (refOpen != -1) {
             ref = s.substring(refOpen + 1, s.indexOf(']'));
         }
 
         // The reference may be an empty string, just not null
         if (ref != null && clazz != null) {
             if (ref.startsWith("LocalRef:")) {
                 Integer localRef = Integer.parseInt(ref.substring(ref.indexOf(":") + 1));
                 current = store.get(localRef).value;
             } else {
                 Constructor c = clazz.getConstructor(String.class, boolean.class);
                 current = c.newInstance(ref, !ref.isEmpty());
             }
         }
     }
     
     protected Object findAndCallMethod(String ref, String s, Object[] args) throws Exception {
         int open = s.indexOf('(');
         String methodName = (open != -1 ? s.substring(0, open) : s);
         
         if (open != -1) {
             String argstr = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
             argstr = argstr.replace(", ", ",");
             args = StringUtils.split(argstr, ',');
         }
         
         if (current != null) {
             clazz = current.getClass();
         }
         
         ArrayList<Method> matches = new ArrayList<>();
 
         // First find name matches
         for (Method m : clazz.getDeclaredMethods()) {
             if (m.getName().equals(methodName) && Modifier.isPublic(m.getModifiers())) {
                 matches.add(m);
             }
         }
 
         // Then param count matches
         for (ListIterator<Method> it = matches.listIterator(); it.hasNext();) {
             Method m = it.next();
             
            if (m.getParameterTypes().length != args.length) {
                 it.remove();
             }
         }
         
         if (matches.size() > 0 && matches.size() < 2) {
             parseAndExecuteMethod(matches.get(0), args);
         } else if (matches.isEmpty()) {
             if (methodName.equals("new")) {
                 return createLocalObject(clazz, args);
             } else {
                 Logger.getLogger(getClass()).warn("Method not found " + s + " in " + commandName);
                 return new CommandException("Method " + methodName + " was not found", commandName);
             }
         } else {
             // We cannot match based on type information as we use the type information to cast the parameters
             return new CommandException("The function call was ambiguous with " + matches.size() + " matched methods", commandName);
         }
         
         return null;
     }
     
     protected void parseAndExecuteMethod(Method m, Object[] args) throws Exception {
         Class<?>[] types = m.getParameterTypes();
         if ((types != null && types.length != 0) && ((types.length > 0 && args == null) || (types.length != args.length))) {
             Logger.getLogger(getClass()).info("Hook call made with incorrect number of arguments: " + commandName);
             current = new CommandException("Illegal number of arguments in " + m.getName() + " call", commandName);
         } else if (args != null) {
             for (int j = 0; j < types.length; j++) {
                 Class<?> type = types[j];
                 Object value = args[j];
                 
                 if (!(value instanceof String) || String.class.isAssignableFrom(type)) {
                     continue;
                 }
                 
                 String str = value.toString();
                 
                 if (str.startsWith("LocalRef:")) {
                     Integer localRef = Integer.parseInt(str.substring(str.indexOf(":") + 1));
                     if (!store.containsKey(localRef)) {
                         current = new CommandException("Local object reference does not exist", commandName);
                     }
                     args[j] = store.get(localRef).value;
                 } else {
                     args[j] = deserializeToTargetType(value, type);
                 }
             }
         }
         
         try {
             if (Modifier.isStatic(m.getModifiers())) {
                 current = m.invoke(null, (Object[]) args);
             } else {
                 if (current == null) {
                     throw new IllegalArgumentException("Instance method called as a static method.");
                 }
                 m.setAccessible(true);
                 current = m.invoke(current, (Object[]) args);
             }
         } catch (InvocationTargetException ex) {
             // If it has the cause, it will be parsed by the next handler
             if (ex.getCause() == null) {
                 Logger.getLogger(getClass()).info("Failed to invoke method", ex);
                 current = new CommandException(ex, commandName);
             } else {
                 Logger.getLogger(getClass()).info("Hook call threw Exception", ex.getCause());
                 current = new CommandException(ex.getCause(), commandName);
             }
         }
         
     }
     
     protected Object executeInstruction(String command, String ref, Object[] args) {
         String[] split = StringUtils.split(command, '.');
         commandName = command;
         
         for (int i = 0; i < split.length; i++) {
             String s = split[i];
             try {
                 if (clazz == null) {
                     determineClass(ref, i, split);
                 } else {
                     Object result = findAndCallMethod(ref, s, args);
                     if (result != null) {
                         return result;
                     }
                 }
             } catch (Exception ex) {
                 Logger.getLogger(getClass()).error("Instruction failed " + s, ex);
                 return new CommandException(ex, commandName);
             }
         }
         
         if (current == null) {
             current = "Success";
         }
         return current;
     }
     
     public static class APICall {
         
         public String ref;
         public Object[] args;
     }
     
     public static class StoredValue {
         
         public long lastAccess = System.currentTimeMillis();
         public Object value;
         
         public StoredValue(Object value) {
             this.value = value;
         }
     }
     
     protected class Housekeeper implements Runnable {
         
         @Override
         public void run() {
             for (Iterator<Map.Entry<Integer, StoredValue>> it = store.entrySet().iterator(); it.hasNext();) {
                 Map.Entry<Integer, StoredValue> entry = it.next();
                 Logger.getLogger(getClass()).debug("Deleting stale object with index LocalRef:" + entry.getKey());
                 if (System.currentTimeMillis() - entry.getValue().lastAccess > 60000) {
                     it.remove();
                 }
             }
         }
     }
 }
