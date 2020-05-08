 /*******************************************************************************
  * Copyright (c) 2006-2013
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.onthefly_javac;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.tools.DiagnosticCollector;
 import javax.tools.JavaFileObject;
 
 import de.devboost.onthefly_javac.internal.ByteArrayJavaFileObject;
 
 public class CompilationResult {
 
     private Map<String, ByteArrayJavaFileObject> store = new LinkedHashMap<String, ByteArrayJavaFileObject>();
     
     private DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
 	private boolean success;
 
 	protected Map<String, ByteArrayJavaFileObject> getStore() {
 		return store;
 	}
     
     private ByteArrayJavaFileObject getFileByName(String name) {
 		return store.get(name);
 	}
 
 	public void setSuccess(boolean status) {
 		this.success = status;
 	}
 
 	public boolean isSuccess() {
 		return success;
 	}
 
 	public DiagnosticCollector<JavaFileObject> getDiagnosticsCollector() {
 		return diagnosticsCollector;
 	}
 
 	public byte[] getByteCode(String className) {
 		ByteArrayJavaFileObject file = getFileByName(className);
 		if (file == null) {
 			return null;
 		}
 		return file.getByteArray();
 	}
 
 	public Class<?> loadClass(String className) throws ClassNotFoundException {
		// we do use 'null' as parent class loaded to avoid the classes are
		// loaded using the system class loader. this ensure that we load only
 		// classes which were explicitly compiled in memory.
 		ClassLoader _classLoader = new ClassLoader(null) {
 			
 			@Override
 			protected Class<?> findClass(String name)
 					throws ClassNotFoundException {
 				
 				byte[] bytes = getByteCode(name);
 				return defineClass(name, bytes, 0, bytes.length);
 			}
 		};
 		
 		Class<?> loadedClass = _classLoader.loadClass(className);
 		return loadedClass;
 	}
 }
