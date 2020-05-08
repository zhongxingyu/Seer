 /**
  * Copyright (c) 2010, MiddleCraft Contributors
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice, this list of 
  *    conditions and the following disclaimer.
  * 
  *  * Redistributions in binary form must reproduce the above copyright notice, this list 
  *    of conditions and the following disclaimer in the documentation and/or other materials 
  *    provided with the distribution.
  * 
  *  * Neither the name of MiddleCraft nor the names of its contributors may be 
  *    used to endorse or promote products derived from this software without specific prior 
  *    written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.middlecraft.server;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.sun.org.apache.bcel.internal.generic.NEW;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 
 /**
  * @author Rob
  *
  */
 public class SmartReflector {
 
 	static Logger l = Logger.getLogger("Minecraft");
 	
 	public static String serverVersion="1.1_02"; // Loads the appropriate object mappings 
 	
 	public static HashMap<String,ClassInfo> classes = new HashMap<String,ClassInfo>();
 	public static Map<String,Class<?>> loadedClasses = new HashMap<String,Class<?>>();
 	
 	public static void initialize() throws IOException {
 		// Read data from our simplified MCP deobfuscation mappings
 		//  (I'll make a few python scripts to do this - N3X)
 		File data = new File(String.format("data/server/%s/", serverVersion));
 		if(!data.exists()) {
 			l.log(Level.WARNING,"No mappings folder exists, creating...");
 			data.mkdirs();
 			save();
 		}
 		if(ReadClasses()) {
 			ReadMethods();
 			ReadFields();
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private static void ReadFields() throws IOException {
 		l.info("Loading field mappings...");
 		// CLASS_NAME,FIELD_NAME,READABLE_FIELD_NAME
 		File f  = new File(String.format("data/server/%s/fields.csv", serverVersion));
 		if(!f.exists()) {
 			l.log(Level.WARNING,"No field mapping table!");
 			return;
 		}
 		Scanner scanner = new Scanner(new FileInputStream(f));
 	    try {
 	    	while (scanner.hasNextLine()) {
 	    		String line = scanner.nextLine();
 	    		String[] chunks = line.split(",");
 	    		
 	    		String className = chunks[0];
 	    		String oldFieldName = chunks[1];
 	    		String fieldName = chunks[2];
 	    		
 	    		if(!classes.containsKey(className))
 	    			continue;
 	    		ClassInfo ci = classes.get(className);
 	    		ci.FieldNames.put(oldFieldName,fieldName);
 	    		classes.put(className, ci);
 	    	}
 	    } catch(Exception e) { e.printStackTrace(); }
 		
 	}
 
 	/**
 	 * 
 	 */
 	private static void ReadMethods() throws IOException {
 		l.info("Loading method mappings...");
 		//CLASS,FUNC_NAME,FUNC_SIG,READABLE_FUNC_NAME
 		File f  = new File(String.format("data/server/%s/methods.csv", serverVersion));
 		if(!f.exists()) {
 			l.log(Level.WARNING,"No method mapping table!");
 			return;
 		}
 		Scanner scanner = new Scanner(new FileInputStream(f));
 	    try {
 	    	while (scanner.hasNextLine()) {
 	    		String line = scanner.nextLine();
 	    		MethodInfo mi = new MethodInfo(line);
 	    		
 	    		if(!classes.containsKey(mi.parentClass))
 	    			continue;
 	    		
 	    		ClassInfo ci = classes.get(mi.parentClass);
 	    		ci.MethodNames.put(mi.toIndex(), mi);
 	    		classes.put(mi.parentClass, ci);
 	    	}
 	    } catch(Exception e) { e.printStackTrace(); }
 	}
 
 	private static boolean ReadClasses() throws IOException {
 		l.info("Loading class mappings...");
 		// CLASS_NAME,OBF_CLASS_NAME,DESCRIPTION
 		File f  = new File(String.format("data/server/%s/classes.csv", serverVersion));
 		if(!f.exists()) {
			l.severe("No class mapping table!");
 			return false;
 		}
 		Scanner scanner = new Scanner(new FileInputStream(f));
 		boolean hasReadHeader=false;
 	    try {
 	    	while (scanner.hasNextLine()){
 	    		String line = scanner.nextLine();
 	    		if(!hasReadHeader) {
 	    			hasReadHeader=true;
 	    			continue;
 	    		}
 	    		String[] chunks = line.split(",");
     		
     			ClassInfo ci = new ClassInfo(line);
     			
     			if(ci.name.startsWith("UNKNOWN_")) {
     				l.log(Level.WARNING, "Skipping WIP mapping for "+ci.name+".");
     				continue;
     			}
     			if(!classes.containsKey(ci.realName))
     				classes.put(ci.realName, ci);
 	    	}
 	    }
 	    finally{
 	    	scanner.close();
 	    }
 	    return true;
 	}
 
 	/**
 	 * Find the named class and load it.
 	 * @param className
 	 * @param defaultSuperClass
 	 * @param params
 	 * @return
 	 * @throws IllegalArgumentException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 */
 	public static Object GrabClassInstance(String className, String defaultSuperClass, Object... params) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		Class<?> theClass = GrabClass(className,defaultSuperClass);
 		// Find a suitable constructor, if possible.
 		for( Constructor<?> c : theClass.getConstructors()) {
 			Class<?>[] c_params = c.getParameterTypes();
 			if(c_params.length != params.length)
 				continue;
 			if(c_params.length==0)
 			{
 				// Constructor doesn't need arguments, just initialize it.
 				return c.newInstance();
 			}
 			// Check types
 			boolean types_ok = true;
 			for(int i = 0;i<c_params.length;i++) {
 				if(c_params[i].getName()!=params[i].getClass().getName())
 					types_ok=false;
 			}
 			if(!types_ok) continue;
 			
 			// LETS DO DIS
 			l.info("[SmartReflector] Initializing "+c.toString());
 			return c.newInstance(params);
 		}
 		return null;
 	}
 	
 	/**
 	 * Add a class to the list.
 	 * @param name
 	 */
 	public static void addObfuscatedClassDefinition(String name) {
 		unkClasses++;
 		ClassInfo ci = new ClassInfo();
 		ci.name="*";
 		ci.realName=name;
 		ci.description="*";
 		classes.put(ci.realName, ci);
 		setDirty();
 	}
 	static int unkClasses=0;
 	public static void addReadableClassDefinition(String name) {
 		unkClasses++;
 		ClassInfo ci = new ClassInfo();
 		ci.name=name;
 		ci.realName="UNKNOWN_"+Integer.toString(unkClasses);
 		ci.description="*";
 		classes.put(ci.realName, ci);
 		setDirty();
 	}
 	
 	static int unkFields=0; 
 	public static void addFieldDefinition(String className, String fieldName) {
 		unkFields++;
 		ClassInfo ci = classes.get(className);
 		ci.FieldNames.put("UNKNOWN_"+Integer.toString(unkFields),fieldName);
 		classes.put(className, ci);
 		setDirty();
 	}
 	
 	static int unkMethods=0;
 
 	private static boolean dirty;
 	public static void addObfuscatedMethodDefinition(String className, String methodName, String signature) {
 		//unkMethods++;
 		//l.log(Level.WARNING,String.format(" + [M] %s.%s %s",className,methodName,signature));
 		ClassInfo ci = classes.get(className);
 		
 		MethodInfo mi = new MethodInfo();
 		mi.name="*";
 		mi.parentClass=className;
 		mi.realName=methodName;
 		mi.signature=signature;
 		ci.MethodNames.put(mi.toIndex(), mi);
 		//ci.MethodNames.put("UNKNOWN_"+Integer.toString(unkMethods),methodName+" "+signature);
 		classes.put(className, ci);
 		setDirty();
 	}
 	
 	/**
 	 * 
 	 */
 	private static void setDirty() {
 		dirty=true;
 	}
 
 	public static void save() {
 		if(!dirty) return;
 		File data = new File(String.format("data/server/%s/", serverVersion));
 		if(!data.exists())
 			data.mkdirs();
 		writeClasses();
 		writeFields();
 		writeMethods();
 	}
 
 	/**
 	 * 
 	 */
 	private static void writeMethods() {
 		PrintStream f=null;
 		try {
 			f = new PrintStream(new FileOutputStream(String.format("data/server/%s/methods.csv", serverVersion)));
 			int num=0;
 			for(ClassInfo ci : classes.values()) {
 				//l.log(Level.INFO,"Class "+ci.name+" contains "+Integer.toString(ci.MethodNames.size())+" known methods.");
 				for(MethodInfo mi : ci.MethodNames.values()) {
 					f.println(mi.toString());
 					num++;
 				}
 			}
 			l.log(Level.INFO,"Wrote "+Integer.toString(num)+" methods to disk.");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if(f!=null)
 				f.close();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private static void writeFields() {		
 		PrintStream f=null;
 		try {
 			f = new PrintStream(new FileOutputStream(String.format("data/server/%s/fields.csv", serverVersion)));
 		
 			for(ClassInfo ci : classes.values()) {
 				for(Entry<String,String> mi : ci.FieldNames.entrySet()) {
 					String realFieldName = mi.getKey();
 					String fieldName = mi.getValue();
 					String className = ci.name;
 					f.println(className+","+fieldName+","+realFieldName);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if(f!=null)
 				f.close();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private static void writeClasses() {		
 		PrintStream f=null;
 		try {
 			f = new PrintStream(new FileOutputStream(String.format("data/server/%s/classes.csv", serverVersion)));
 			List<String> keys = new ArrayList<String>(classes.keySet());
 			Collections.sort(keys);
 			f.println(ClassInfo.header);
 			for(String key : keys) {
 				f.println(classes.get(key).toString());
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if(f!=null)
 				f.close();
 		}
 	}
 
 
 	/**
 	 * @param string
 	 * @return
 	 * @throws NotFoundException 
 	 * @throws CannotCompileException 
 	 */
 	private static CtClass GrabCtClass(String className,String defaultSuperClass){
 		try
 		{
 			ClassPool cp = ClassPool.getDefault();
 			CtClass cl = cp.getOrNull(className);
 			ClassInfo ci = classes.get(className);
 			if(ci.superClass=="") {
 				ci.superClass=defaultSuperClass;
 				classes.put(className, ci);
 				save();
 			}
 			try {
 				cl.setSuperclass(cp.get(ci.superClass));
 			} catch (CannotCompileException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			// Fix field names...
 			for(CtField cf : cl.getDeclaredFields()) {
 				if(ci.FieldNames.containsKey(cf.getName())) {
 					String newName = ci.FieldNames.get(cf.getName());
 					l.log(Level.INFO,"Fixing field "+cf.getName()+" to "+newName);
 					cf.setName(newName);
 				}
 			}
 			// Fix methods
 			for(CtMethod cm : cl.getDeclaredMethods()) {
 				String methodID=cm.getName()+" "+cm.getSignature();
 				l.log(Level.INFO,"Fixing method "+className+"."+methodID);
 				if(ci.MethodNames.containsKey(methodID))
 					cm.setName(ci.MethodNames.get(methodID).name);
 				
 				// Also patch, if required.
 				File methodPatch = new File(String.format("data/server/%s/patches/%s/%s.%s.java",serverVersion,ci.name,cm.getName(),cm.getSignature()));
 				if(methodPatch.exists()) {
 					l.log(Level.INFO,"Patching method "+className+"."+methodID);
 					try {
 						cm.setBody(Utils.getFileContents(methodPatch));
 					} catch (CannotCompileException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			return cl;
 		} catch(NotFoundException e) {
 			l.log(Level.WARNING,"Can't find class "+className+"!");
 			addReadableClassDefinition(className);
 			return null;
 		}
 	}
 
 	/**
 	 * @param string
 	 * @return
 	 * @throws NotFoundException 
 	 * @throws CannotCompileException 
 	 */
 	public static Class<?> GrabClass(String className,String defaultSuperClass){
 		if(!loadedClasses.containsKey(className))
 		{
 			CtClass cc = GrabCtClass(className,defaultSuperClass);
 			if(cc==null) return null;
 			try {
 				loadedClasses.put(className, cc.toClass());
 			} catch (CannotCompileException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 		}
 		return loadedClasses.get(className);
 	}
 	/**
 	 * @param className
 	 * @return
 	 */
 	public static String getNewClassName(String className) {
 		ClassInfo ci = classes.get(className);
 		if(ci==null) return className;
 		return ci.name;
 	}
 
 	/**
 	 * 
 	 * @param className OBFUSCATED class name.
 	 * @param name
 	 * @param signature
 	 * @return
 	 */
 	public static MethodInfo getMethod(String className, String name, String signature) {
 		ClassInfo ci = classes.get(className);
 		if(ci==null) {
 			l.log(Level.WARNING, "Can't find parent class "+className+" for method "+name);
 			return null;
 		}
 		MethodInfo mi = new MethodInfo();
 		mi.realName=name;
 		mi.signature=signature;
 		String index = mi.toIndex();
 		if(!ci.MethodNames.containsKey(index)) {
 			addObfuscatedMethodDefinition(className,name,signature);
 			return null;
 		}
 		return ci.MethodNames.get(index);
 	}
 }
