 package mesquite.treecmp.metrics;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 
 import mesquite.trunk.ClassPathHacker;
 
 import org.xeustechnologies.jcl.JarClassLoader;
 
 public class PalFacade {
 	
 	public static Tree readTree(String s) throws IOException {
 		try {
 			return new Tree(getReadTreeMethod().invoke(null, new StringReader(s)));
 		} catch (InvocationTargetException e) {
 			if (e.getCause() instanceof IOException)
 				throw (IOException)e.getCause();
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			return null;
 		}
 	}
 	
 	private static JarClassLoader getLoader() {
 		if (loader == null)
 			synchronized (PalFacade.class) {
 				if (loader==null) {
 					final URL adkPath = resolveResource("lib/ADK-1.1.jar");
 					final URL gtpPath = resolveResource("lib/gtp.jar");
 					final URL jclPath = resolveResource("lib/jcl-core-2.2.2.jar");
 					final URL log4jPath = resolveResource("lib/log4j-1.2.16.jar");
 					final URL treecmp4GuiPath = resolveResource("lib/treecmp.jar");
 					final URL palPath = resolveResource("lib/pal-1.5.1.f1.jar");
 					
 					try {
 						ClassPathHacker.addURL(jclPath);
 						ClassPathHacker.addURL(log4jPath);
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 
 					loader = new JarClassLoader();
 					loader.add(treecmp4GuiPath);
 					loader.add(adkPath);
 					loader.add(gtpPath);
 					loader.add(palPath);
 				}
 			}
 		return loader;
 	}
 	private static JarClassLoader loader;
 	
 	private static URL resolveResource(String name) {
 		return PalFacade.class.getClassLoader().getResource(name);
 	}
 	
 	private static Method getReadTreeMethod() {
 		if (readTreeMethod == null)
 			synchronized (PalFacade.class) {
 				if (readTreeMethod == null) {
 					try {
 						final Class<?> treeToolClass = getLoader().loadClass("pal.tree.TreeTool");
 						readTreeMethod = treeToolClass.getMethod("readTree", Reader.class);
 					} catch (Exception e) {
 						//this should not happen
 						throw new RuntimeException(e);
 					}
 				}
 			}
 		return readTreeMethod;
 	}
 	private static Method readTreeMethod;
 	
 	private static Class<?> getPalTreeClass() {
 		if (palTreeClass == null)
 			synchronized (PalFacade.class) {
 				if (palTreeClass == null) {
 					try {
 						palTreeClass = getLoader().loadClass("pal.tree.Tree");
 					} catch (ClassNotFoundException e) {
 						// this should not happen
 						throw new RuntimeException(e);
 					}
 				}		
 			}
 		return palTreeClass;
 	}
 	private static Class<?> palTreeClass;	
 
 	public static class Tree {
 		public Tree(Object treeObj) {
 			this.treeObj = treeObj;
 			try {
 				toStringMethod = treeObj.getClass().getMethod("toString");
 			} catch (Exception e) {
 				//this should not happen
 				throw new RuntimeException(e);
 			}
 		}
 		
 		@Override
 		public String toString() {
 			try {
 				return (String)toStringMethod.invoke(treeObj);
 			} catch (Exception e) {
 				//this should not happen
 				throw new RuntimeException(e);
 			}
 		}
 
 		private final Method toStringMethod;
 		private final Object treeObj;
 	}
 	
 	public static class TreeCmpMetric {
 		public TreeCmpMetric(String className) {
 			try {
 				final Class<?> metricClass = getLoader().loadClass(className); 
 				metricObject = metricClass.getConstructor().newInstance();
 				getDistanceMethod = metricObject.getClass().getMethod("getDistance", getPalTreeClass(), getPalTreeClass());
 			} catch (Exception e) {
 				//this should not happen
 				throw new RuntimeException(e);
 			}
 		}
 		
 		public double getDistance(Tree t1, Tree t2) {			
 			try {
 				return (Double)getDistanceMethod.invoke(metricObject, t1.treeObj, t2.treeObj);
 			} catch (Exception e) {
 				//this should not happen
				throw new RuntimeException("There was a problem when computing distance between trees: " + t1.toString() + " " + t2.toString(), e);
 			}			 
 		}
 		
 		private final Method getDistanceMethod;
 		private final Object metricObject;
 	}
 }
