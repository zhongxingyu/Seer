 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.netlet.util;
 
 import groovy.lang.GroovyShell;
 import net.jini.core.transaction.TransactionException;
 import org.codehaus.groovy.control.CompilationFailedException;
 import sorcer.service.Exertion;
 import sorcer.service.ExertionException;
 import sorcer.util.ExertProcessor;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.rmi.RemoteException;
 import java.util.logging.Logger;
 
 public class ScriptThread extends Thread {
 		private String script;
 		private File scriptFile;
 		private Object result;
 		private Object target = null;
 		private GroovyShell gShell;
 
         private final static Logger logger = Logger.getLogger(ScriptThread.class
                 .getName());
 
         public ScriptThread(String script, URL[] jarsToAdd, ClassLoader classLoader, PrintStream out) {
             RootLoader loader = null;
             if (classLoader==null) {
                 loader = new RootLoader(jarsToAdd, this.getClass().getClassLoader());
                 if (out!=null) out.println("New Script classloader: " + printUrls(loader.getURLs()));
             }
             else if (classLoader instanceof RootLoader) {
                 loader = (RootLoader)classLoader;
                 for (URL url : jarsToAdd)
                     loader.addURL(url);
                 if (out!=null) out.println("Existing Script classloader: " + printUrls(loader.getURLs()));
             } else if (classLoader instanceof URLClassLoader) {
                 loader = new RootLoader(jarsToAdd, classLoader);
                 if (out!=null) out.println("Existing Script classloader: " + printUrls(loader.getURLs()));
             }
             gShell = new GroovyShell(loader!=null ? loader : classLoader);
 			this.script = script;
             this.parseScript();
 		}
 
         public ScriptThread(String script, URL[] jarsToAdd, PrintStream out) {
             this(script, jarsToAdd, null, out);
         }
 
         public ScriptThread(String script, URL[] jarsToAdd) {
             this(script, jarsToAdd, null, null);
         }
 
         public ScriptThread(String script, ClassLoader classLoader) {
             this.gShell = new GroovyShell(classLoader);
             this.script = script;
             this.parseScript();
         }
 
 		public ScriptThread(File file, ClassLoader classLoader) {
             this.gShell = new GroovyShell(classLoader);
 			this.scriptFile = file;
             this.parseScript();
 		}
 
         public void parseScript() {
             synchronized (gShell) {
                 if (script != null) {
                     target = gShell.evaluate(script);
                 }
                 else {
                     try {
                         target = gShell.evaluate(scriptFile);
                     } catch (CompilationFailedException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
 
 		public void run() {
 			if (target!=null && target instanceof Exertion) {
 				ExertProcessor esh = new ExertProcessor((Exertion) target);
 				try {
 					result = esh.exert();
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				} catch (TransactionException e) {
 					e.printStackTrace();
 				} catch (ExertionException e) {
 					e.printStackTrace();
 				}
 			} else {
 				result = target;
 			}
 		}
 
 		public Object getResult() {
 			return result;
 		}
 
 		public Object getTarget() {
 			return target;
 		}
 
         public String printUrls(URL[] urls) {
             StringBuilder sb = new StringBuilder("URLs: [");
             for (URL url : urls) {
                 sb.append("\n").append(url.toString());
             }
             sb.append(" ]");
             return sb.toString();
         }
 
 	}
