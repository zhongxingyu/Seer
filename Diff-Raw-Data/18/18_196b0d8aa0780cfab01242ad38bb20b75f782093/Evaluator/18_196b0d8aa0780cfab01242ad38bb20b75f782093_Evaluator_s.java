 /*
  * junitflood - An automatic junit test generator
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.junitflood.evaluator;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.Set;
 
 import com.mebigfatguy.junitflood.Configuration;
 import com.mebigfatguy.junitflood.generator.StatementList;
 import com.mebigfatguy.junitflood.security.SaneSecurityManager;
 import com.mebigfatguy.junitflood.security.SecurityManagerFactory;
 import com.mebigfatguy.junitflood.util.SignatureUtils;
 
 public class Evaluator {
 	private final Configuration configuration;
 	private final ClassLoader classLoader;
 
 	public Evaluator(Configuration config) {
 		configuration = config;
 		classLoader = configuration.getRepository().createClassLoader();
 
 	}
 
 	public StatementList attemptExecution(String clsName, String methodName, String signature) {
 		try {
 			StatementList statementList = new StatementList();
 
 			Set<String> constructorSignatures = configuration.getRepository().getConstructors(clsName, clsName);
 			SecurityManagerFactory.setSecurityManager(new SaneSecurityManager());
 
 			Class<?> cls = classLoader.loadClass(clsName.replaceAll("/", "."));
 
 			for (String constructorSignature : constructorSignatures) {
 				try {
 					Constructor<?> cons = cls.getDeclaredConstructor(SignatureUtils.convertMethodParameterSignaturesToClassArray(classLoader, constructorSignature));
 					Object[] args = SignatureUtils.createDefaultArgsForSignature(constructorSignature);
 					Object o = cons.newInstance(args);
 
 					String objectName = statementList.addConstructor(clsName, args);
 
 					Method m = cls.getMethod(methodName, SignatureUtils.convertMethodParameterSignaturesToClassArray(classLoader, signature));
 					args = SignatureUtils.createDefaultArgsForSignature(constructorSignature);
 					m.invoke(o, args);
 
					statementList.addMethodCall(objectName, methodName, args, methodName, args);
 
 					return statementList;
 				} catch (Exception e) {
 					statementList.clear();
 				}
 			}
 
 			return null;
 		} catch (Exception e) {
 			return null;
 		} finally {
 			SecurityManagerFactory.setSecurityManager(null);
 		}
 	}
 }
