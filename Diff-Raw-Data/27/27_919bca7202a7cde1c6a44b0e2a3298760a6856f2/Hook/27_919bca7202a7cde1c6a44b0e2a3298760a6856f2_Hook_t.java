 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.web;
 
 import com.google.gson.Gson;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import net.wgr.server.web.handling.WebCommandHandler;
 import net.wgr.wcp.Command;
 import net.wgr.wcp.CommandException;
 import net.wgr.xenmaster.controller.Controller;
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
         Controller.getSession().loginWithPassword("root", "r00tme");
     }
 
     public Object execute(Command cmd) {
         Gson gson = new Gson();
         APICall apic = gson.fromJson(cmd.getData(), APICall.class);
 
         return executeInstruction(cmd.getName(), apic.ref, apic.args);
     }
 
     protected Object executeInstruction(String command, String ref, Object[] args) {
         String[] split = StringUtils.split(command, '.');
         Class clazz = null;
         Object current = null;
         String className = "";
 
         for (int i = 0; i < split.length; i++) {
             String s = split[i];
             try {
                 if (clazz == null) {
                     int refOpen = s.indexOf('[');
                    if (refOpen != -1) {
                         className = s.substring(0, refOpen);
                         clazz = Class.forName("net.wgr.xenmaster.api." + className);
                    } else if (i == split.length - 2) {
                         className += s;
                        clazz = Class.forName("net.wgr.xenmaster.api." + className);
                    } else {
                        className += s + '.';
                     }
 
                     if (refOpen != -1) {
                         ref = s.substring(refOpen + 1, s.indexOf(']'));
                     }

                    // The reference may be an empty string, just not null
                     if (ref != null) {
                         Constructor c = clazz.getConstructor(String.class, boolean.class);
                         current = c.newInstance(ref, !ref.isEmpty());
                     }
                 } else {
                     int open = s.indexOf('(');
                     String methodName = (open != -1 ? s.substring(0, open) : s);
                     if (open != -1) {
                         String argstr = s.substring(s.indexOf('(') + 1, s.indexOf(')'));
                         argstr = argstr.replace(", ", ",");
                         args = StringUtils.split(argstr, ',');
                    }

                     boolean match = false;
                     if (current != null) {
                         clazz = current.getClass();
                     }
 
                     for (Method m : clazz.getDeclaredMethods()) {
                         if (m.getName().equals(methodName)) {
                             match = true;
 
                             Class<?>[] types = m.getParameterTypes();
                             if ((types != null && types.length != 0) && ((types.length > 0 && args == null) || (types.length != args.length))) {
                                Logger.getLogger(getClass()).info("Hook call made with incorrect number of arguments: " + command);
                                 return new CommandException("Illegal number of arguments in " + methodName + " call", command);
                             } else if (args != null) {
                                 for (int j = 0; j < types.length - 1; j++) {
                                     Class<?> type = types[j];
                                     Object value = args[j];
 
                                     if (!(value instanceof String)) {
                                         continue;
                                     }
 
                                     switch (type.getSimpleName()) {
                                         case "boolean":
                                             args[j] = Boolean.parseBoolean(value.toString());
                                             break;
                                         case "int":
                                             args[j] = Integer.parseInt(value.toString());
                                             break;
                                     }
                                 }
                             }
 
                             try {
                                 if (Modifier.isStatic(m.getModifiers())) {
                                     current = m.invoke(null, (Object[]) args);
                                 } else {
                                     m.setAccessible(true);
                                     current = m.invoke(current, (Object[]) args);
                                 }
                             } catch (Exception ex) {
                                 Logger.getLogger(getClass()).info("Hook call threw Exception", ex);
                                 return new CommandException(ex, command);
                             }
 
                             break;
                         }
                     }
 
                     if (!match) {
                         Logger.getLogger(getClass()).warn("Method not found " + s + " in " + command);
                         return new CommandException("Method " + methodName + " was not found", command);
                     }
                 }
             } catch (Exception ex) {
                 Logger.getLogger(getClass()).error("Instruction failed " + s, ex);
                 return new CommandException(ex, command);
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
 }
