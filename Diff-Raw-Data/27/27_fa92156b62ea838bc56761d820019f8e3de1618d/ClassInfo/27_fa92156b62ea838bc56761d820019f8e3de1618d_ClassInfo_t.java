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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.NotFoundException;
 import javassist.bytecode.Descriptor;
 
 /**
  * @author Rob
  *
  */
 public class ClassInfo {
 	//realName,name,realSuperClass,superClass,description
 	public static final String[] header = new String[]{"Real Name","MCP Name","Real Superclass","MiddleCraft Superclass","Description"};
 	public String description="*";
 	public HashMap<String,FieldInfo> fieldNames = new HashMap<String,FieldInfo>();
 	Logger l = Logger.getLogger("Minecraft");
 	public HashMap<String,MethodInfo> methodNames = new HashMap<String,MethodInfo>();
 	public String name;
 	public String realName;
 	public String realSuperClass;
 	public String superClass="*";
 	public ClassInfo() {}
 	public ClassInfo(List<String> cells) {
 		realName=cells.get(0);
 		name=cells.get(1);
 		realSuperClass=cells.get(2);
 		superClass=cells.get(3);
 		description=cells.get(4);
 
 		// Also patch, if required.
 		File methodPatch = new File(String.format("data/server/%s/patches/%s.mcp",SmartReflector.serverVersion,name));
 		if(methodPatch.exists()) {
 			Utils.getFileContents(methodPatch);
 		}
 	}
 	/**
 	 * @param cc
 	 * @param string
 	 * @param string2
 	 * @return
 	 */
 	private CtClass createField(ClassPool cp, CtClass cc, String type, String name) {
 		CtClass ctType;
 		try {
 			ctType = cp.get(type);
 			CtField newfield = new CtField(ctType, name, cc);
 			cc.addField(newfield);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return cc;
 	}
 	public CtClass DoPatch(ClassPool cp, CtClass cc) throws CannotCompileException, FileNotFoundException {
 		int line_num= 0;
 		File f = new File(String.format("data/server/%s/patches/%s.mcp",SmartReflector.serverVersion,name));
 		if(f.exists()) {
 			l.info(String.format("Patching %s...",cc.getName()));
 			Scanner scanner = new Scanner(new FileInputStream(f));
 			while(scanner.hasNext()) {
 				line_num++;
 				String line = scanner.nextLine();
 				if(line.startsWith("#")) continue;
 				String[] chunks = line.split("\t");
 				String context = chunks[0].toLowerCase();
 				try {
 					if(context.equals("newmethod")) {
 						l.info(" + Creating new method: "+chunks[1].substring(0,chunks[1].indexOf('{')));
 						// newmethod	void callOnTick (int x, int y, int z) {...}
 						CtMethod nm = CtNewMethod.make(chunks[1], cc);
 						cc.addMethod(nm);
 					} else if(context.equals("newfield")) {
 						l.info(String.format(" + Creating new field: %s %s",chunks[1],chunks[2]));
 						// newfield	int xPosition
 						cc=createField(cp,cc,chunks[1],chunks[2]);
 					} else if(context.equals("method")) {
 						String methodName = chunks[1].substring(0,chunks[1].indexOf('('));
 						CtClass[] args = parseParams(cp, chunks[1].substring(chunks[1].indexOf('(')+1,chunks[1].lastIndexOf(')')));
 						CtMethod cm = cc.getDeclaredMethod(
 								methodName,
 								args
 						);
 						String operation = chunks[2].toLowerCase().trim();
 						if(operation.equals("setbody")) {
 							l.info(String.format(" * Setting body of %s to contents of %s",chunks[1],chunks[3]));
 							cm.setBody(Utils.getFileContents(new File(String.format("data/server/%s/patches/%s",SmartReflector.serverVersion,chunks[3]))));
 						} else if(operation.equals("prependbody")) {
 							l.info(String.format(" + Prepending body of %s with: %s",chunks[1],chunks[3]));
 							cm.insertBefore(chunks[3]);
 						} else if(operation.equals("appendbody")) {
 							l.info(String.format(" * Appending body of %s with: %s",chunks[1],chunks[3]));
 							cm.insertAfter(chunks[3]);
 						} else if(operation.equals("insertat")) {
 							int lineNum=Integer.parseInt(chunks[3]);
 							l.info(String.format(" * Inserting code into %s:%d: %s",chunks[1],lineNum,chunks[4]));
 							cm.insertAt(lineNum,chunks[4]);
 						}
 					} else if(context.equals("constructor")) {
 						String sig = chunks[1];
 
 						sig=Descriptor.rename(sig,SmartReflector.obfuscationMap);
 						//CtClass[] types = Descriptor.getParameterTypes(sig, cp);
 						
 						CtConstructor cm=null;
 						try {
 							cm = cc.getConstructor(sig);
 						} catch(NotFoundException e) {
 							l.severe("Can't find constructor with signature "+sig+".  Here are some alternatives:");
 							for(CtConstructor ctor : cc.getConstructors()) {
 								l.info(ctor.getSignature());
 							}
 							System.exit(1);
 						}
 						String operation = chunks[2].toLowerCase().trim();
 						if(operation.equals("setbody")) {
 							l.info(String.format(" * Setting body of %s to contents of %s",chunks[1],chunks[3]));
 							cm.setBody(Utils.getFileContents(new File(String.format("data/server/%s/patches/%s",SmartReflector.serverVersion,chunks[3]))));
 						} else if(operation.equals("prependbody")) {
 							l.info(String.format(" + Prepending body of %s with: %s",chunks[1],chunks[3]));
 							cm.insertBefore(chunks[3]);
 						} else if(operation.equals("appendbody")) {
 							l.info(String.format(" * Appending body of %s with: %s",chunks[1],chunks[3]));
 							cm.insertAfter(chunks[3]);
 						} else if(operation.equals("insertat")) {
 							int lineNum=Integer.parseInt(chunks[3]);
 							l.info(String.format(" * Inserting code into %s:%d: %s",chunks[1],lineNum,chunks[4]));
 							cm.insertAt(lineNum,chunks[4]);
 						}
 					}
 				} catch(Throwable e) {
 					e.printStackTrace();
 					l.severe(e.getMessage());
 					l.severe(String.format("data/server/%s/patches/%s.mcp:%d",SmartReflector.serverVersion,name,line_num));
 				}
 			}
 			
 			l.info("Success!");
 		}
 		return cc;
 	}
 
 	/**
 	 * @param name2
 	 * @return
 	 */
 	public FieldInfo getField(String name2) {
 		return fieldNames.get(name2);
 	}
 	/**
 	 * @param name2
 	 * @return
 	 */
 	public MethodInfo getMethod(String name2) {
 		return methodNames.get(name2);
 	}
 
 	/**
 	 * @param substring
 	 * @return
 	 * @throws NotFoundException 
 	 */
 	private CtClass[] parseParams(ClassPool cp, String args) throws NotFoundException {
 		args=args.trim();
 		if(args.length()==0) 
 			return new CtClass[]{};
 		String[] chunks=args.split(",");
 		CtClass[] argTypes = new CtClass[chunks.length];
 		if(chunks.length==0) 
 			return argTypes;
 		int i = 0;
 		l.info(String.format("CtClass count = "+Integer.toString(chunks.length)));
 		for(String type : chunks) {
 			argTypes[i++]=cp.get(type.trim());
 		}
 		return argTypes;
 	}
 	public List<String> toList() {
 		List<String> list = new ArrayList<String>();
 		list.add(realName);
 		list.add(name);
 		list.add(realSuperClass);
 		list.add(superClass);
 		list.add(description);
 		return list;
 	}
 	public String toString() {
 		return String.format("%s,%s,%s,%s,%s",realName,name,realSuperClass,superClass,description);
 	}
 }
