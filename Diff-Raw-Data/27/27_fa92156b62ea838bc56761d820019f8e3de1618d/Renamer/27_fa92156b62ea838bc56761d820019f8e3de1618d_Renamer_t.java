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
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.NotFoundException;
 import javassist.Translator;
 
 /**
  * @author Rob
  *
  */
 public class Renamer implements Translator {
 	Logger l = Logger.getLogger("Minecraft");
 	List<String> skippedPackages = new ArrayList<String>();
 
 	/**
 	 * Deobfuscate classes
 	 * @see javassist.Translator#onLoad(javassist.ClassPool, java.lang.String)
 	 */
 	@Override
 	public void onLoad(ClassPool cp, String className) throws NotFoundException,
 	CannotCompileException {
 		className=SmartReflector.getOldClassName(className);
 		
 		// Get class we'll be fucking with
 		ClassInfo ci = SmartReflector.classes.get(className);
 		if(ci==null)
 		{
 			SmartReflector.addObfuscatedClassDefinition(className,cp.get(className).getSuperclass().getName());
 			ci = SmartReflector.classes.get(className);
 		}
 
 		// New class name...
 		String ncn = ci.name;
 		try {
 			if(className!="net.minecraft.server.MinecraftServer") {
 				if(className==ncn) throw new NotFoundException(className);
 
 				l.fine("Renaming class "+className+" to "+ncn+".");
 				CtClass cc = cp.get(className);
 
 				cc.setName(ncn);
 
 				if(ci.realSuperClass!=cc.getSuperclass().getName()) {
 					ci.realSuperClass=cc.getSuperclass().getName(); 
 					SmartReflector.classes.put(className,ci);
 				}
 
 				String pkg = cc.getPackageName();
 				if(pkg==null || pkg == "net.minecraft.server")
 				{
 					remapFields(cp,cc,className);
 					remapMethods(cp,cc,className);
 				}
 				try {
 					cc=ci.DoPatch(cp, cc);
 				} catch(FileNotFoundException e) {}
 
 				l.fine("Renaming class references");
 				cc.replaceClassName(SmartReflector.deobfuscationMap);
 
 				//cc.writeFile("data/server/"+SmartReflector.serverVersion+"/patched/");
 
 			}
 		} catch(NotFoundException e) {
 			l.log(Level.WARNING,"Failed to get new classname for "+className);
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * @param cp
 	 * @param cc
 	 */
 	private void remapFields(ClassPool cp, CtClass cc, String className) {
 		if(cc.isFrozen())
 			cc.defrost();
 		for(CtField field : cc.getFields()) {
 			String f_pkg=field.getDeclaringClass().getPackageName();
 			if(!Utils.isMinecraftPackage(f_pkg))
 			{
 				if(!skippedPackages.contains(f_pkg)) {
 					l.log(Level.INFO,"Skipping package "+f_pkg);
 					skippedPackages.add(f_pkg);
 				}
 				continue;
 			}
 			try
 			{
 				FieldInfo fi = SmartReflector.getField(className,field.getName(),field.getType().getName());
 
 				if(fi!=null) {
 					if(fi.name!="*" || fi.name!="") continue;
 					if(cc.isFrozen())
 						cc.defrost();
 					field.setName(fi.name);
 				}
 			} catch(Exception e){}
 		}
 	}
 
 	/**
 	 * @param cp
 	 * @param cc
 	 */
 	private void remapMethods(ClassPool cp, CtClass cc, String className) {
 		if(cc.isFrozen())
 			cc.defrost();
 		for(final CtMethod method : cc.getMethods()) {
 			String m_pkg=method.getDeclaringClass().getPackageName();
 			if(!Utils.isMinecraftPackage(m_pkg))
 			{
 				if(!skippedPackages.contains(m_pkg)) {
 					l.log(Level.INFO,"Skipping package "+m_pkg);
 					skippedPackages.add(m_pkg);
 				}
 				continue;
 			}
 			MethodInfo mi = SmartReflector.getMethod(className,method.getName(),method.getSignature());
 
 			if(mi!=null) {
 				if(mi.name!="*" || mi.name!="") continue;
 				if(cc.isFrozen())
 					cc.defrost();
 				l.log(Level.INFO,String.format(" [M] [%s]%s%s to \"%s\"...",method.getDeclaringClass().getPackageName(),method.getLongName(),mi.signature,mi.name));
 				method.setName(mi.name);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see javassist.Translator#start(javassist.ClassPool)
 	 */
 	@Override
 	public void start(ClassPool cp) throws NotFoundException,
 	CannotCompileException {
 	}
 
 }
