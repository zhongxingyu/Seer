 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.web;
 
 import com.google.gson.Gson;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import net.wgr.server.web.handling.WebCommandHandler;
 import net.wgr.wcp.Command;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 1, 2011
  * @author double-u
  */
 public class Hook extends WebCommandHandler {
 
     public Hook() {
         super("xen");
     }
 
     public Object execute(Command cmd) {
         Gson gson = new Gson();
         APICall apic = gson.fromJson(cmd.getData(), APICall.class);
        if (apic.args == null) apic.args = new Object[0];
         String[] splitsies = StringUtils.split(cmd.getName(), '.');
         return invoke(splitsies[0], splitsies[1], apic.args, apic.ref);
     }
 
     protected Object invoke(String className, String methodName, Object[] args, String ref) {
         try {
             Class clazz = Class.forName("net.wgr.xenmaster.entities." + className);
             for (Method m : clazz.getDeclaredMethods()) {
                 if (m.getName().equals(methodName)) {
                     if (Modifier.isStatic(m.getModifiers())) {
                         return m.invoke(null, args);
                     } else {
                         m.setAccessible(true);
                         Constructor c = clazz.getConstructor(String.class, boolean.class);
                         if (methodName.equals("get")) {
                             return c.newInstance(ref, true);
                         } else {
                            return m.invoke(c.newInstance(ref, false), args);
                         }
                     }
                 }
             }
         } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchMethodException ex) {
             Logger.getLogger(getClass()).error("Entity not found", ex);
         }
         return null;
     }
 
     public static class APICall {
 
         public Object[] args;
         public String ref;
     }
 }
