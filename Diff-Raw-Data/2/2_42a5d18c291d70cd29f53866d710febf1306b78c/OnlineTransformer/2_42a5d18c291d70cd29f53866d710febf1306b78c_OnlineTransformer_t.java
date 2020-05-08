 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  * Licensed under the terms of the New BSD License.
  */
 package chord.instr;
 
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.lang.instrument.UnmodifiableClassException;
 import java.lang.instrument.Instrumentation;
 import java.security.ProtectionDomain;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import chord.util.StringUtils;
 import chord.project.Config;
 import chord.project.Project;
 
 import javassist.CtClass;
 import javassist.CannotCompileException;
 import javassist.NotFoundException;
 import java.util.Properties;
 
 import chord.project.Messages;
 
 /**
  * Online (load-time) class-file transformer.
  *
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public final class OnlineTransformer implements ClassFileTransformer {
 	private static final String RETRANSFORM_NOT_SUPPORTED =
 		"ERROR: JVM does not support retransforming classes.";
 	private static final String CANNOT_RETRANSFORM_LOADED_CLASSES =
 		"ERROR: Failed to retransform alreaded loaded classes; reason follows.";
 	private static final String CANNOT_INSTRUMENT_CLASS =
 		"ERROR: Skipping instrumenting class %s; reason follows.";
 	private static final String CANNOT_MODIFY_CLASS =
 		"WARN: Cannot modify class %s.";
 
     private final CoreInstrumentor instrumentor;
 
 	public OnlineTransformer(CoreInstrumentor instr) {
 		instrumentor = instr;
 	}
 
     public static void premain(String agentArgs, Instrumentation instrumentation) {
         boolean isSupported = instrumentation.isRetransformClassesSupported();
         if (!isSupported) {
             Messages.fatal(RETRANSFORM_NOT_SUPPORTED);
         }
 		Map<String, String> argsMap = new HashMap<String, String>();
 		if (agentArgs != null) {
 			String[] args = agentArgs.split("=");
 			int n = args.length / 2;
 			for (int i = 0; i < n; i++)
 				argsMap.put(args[i*2], args[i*2+1]);
 		}
		String instrClassName = argsMap.get(CoreInstrumentor.INSTRUMENTOR_CLASS_KEY);
 		Class instrClass = null;
 		if (instrClassName != null) {
 			try {
 				instrClass = Class.forName(instrClassName.replace('/', '.'));
 			} catch (ClassNotFoundException ex) {
 				Messages.fatal(ex);
 			}
 		} else
 			instrClass = CoreInstrumentor.class;
 		CoreInstrumentor instr = null;
 		Exception ex = null;
 		Project.init();
 		try {
 			Constructor c = instrClass.getConstructor(new Class[] { Map.class });
 			Object o = c.newInstance(new Object[] { argsMap });
 			instr = (CoreInstrumentor) o;
 		} catch (InstantiationException e) {
 			ex = e;
 		} catch (NoSuchMethodException e) {
 			ex = e;
 		} catch (InvocationTargetException e) {
 			ex = e;
 		} catch (IllegalAccessException e) {
 			ex = e;
 		}
 		if (ex != null)
 			Messages.fatal(ex);
         OnlineTransformer transformer = new OnlineTransformer(instr);
         instrumentation.addTransformer(transformer, true);
         Class[] classes = instrumentation.getAllLoadedClasses();
         List<Class> retransformClasses = new ArrayList<Class>();
         for (Class c : classes) {
             if (c.getName().startsWith("[")) 
 				continue;
 			if (!instrumentation.isModifiableClass(c)) {
 				if (!Config.dynamicSilent)
 					Messages.log(CANNOT_MODIFY_CLASS, c.getName());
 				continue;
 			}
 			retransformClasses.add(c);
         }
 		Class[] retransformClassesAry =
 			retransformClasses.toArray(new Class[retransformClasses.size()]);
         try {
             instrumentation.retransformClasses(retransformClassesAry);
         } catch (UnmodifiableClassException e) {
 			Messages.fatal("UNREACHABLE");
         }
     }
 
 	@Override
     public byte[] transform(ClassLoader loader, String className,
 			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
 			byte[] classfileBuffer) throws IllegalClassFormatException {
 		// Note from javadoc:
 		//  If this method determines that no transformations are
 		//  needed, it should return null. Otherwise, it should create
 		//  a new byte[] array, copy the input classfileBuffer into it,
 		//  along with all desired transformations, and return the new
 		//  array. The input classfileBuffer must not be modified.
 		// className is of the form "java/lang/Object"
 		String cName = className.replace('/', '.');
 		Exception ex = null;
 		try {
 			CtClass clazz = instrumentor.edit(cName);
 			if (clazz != null) {
 				return clazz.toBytecode();
 			}
 		} catch (IOException e) {
 			ex = e;
 		} catch (NotFoundException e) {
 			ex = e; 
 		} catch (CannotCompileException e) {
 			ex = e; 
 		}
 		if (ex != null) {
 			if (!Config.dynamicSilent) {
 				Messages.log(CANNOT_INSTRUMENT_CLASS, cName);
 				ex.printStackTrace();
 			}
 		}
 		return null;
     }
 }
 
