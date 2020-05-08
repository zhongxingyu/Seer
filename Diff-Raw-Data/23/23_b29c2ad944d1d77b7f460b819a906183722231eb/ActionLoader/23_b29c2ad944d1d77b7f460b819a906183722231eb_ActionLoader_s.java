 package maro.core;
 
 import jason.asSyntax.Structure;
 import java.net.URL;
 import java.util.Set;
 import java.util.List;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.ArrayList;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.net.URLConnection;
 import java.net.JarURLConnection;
 
 public class ActionLoader
 {
 	static private ActionLoader instance = null;
 	private HashMap<String, EnvironmentAction> eahm;
 
 	synchronized static public ActionLoader getInstance() {
 		if (instance == null) instance = new ActionLoader();
 		return instance;
 	}
 
 	private ActionLoader () { eahm = new HashMap<String, EnvironmentAction> (); }
 
	public void loadActions() { loadAllActions("maro.example.sims.ea"); }

	private void loadAllActions(String packet) {
 		Set<String> names = new HashSet<String> ();
 
 		try {
 			names = getAllClasses(packet);
 		} catch (Exception e) {
 		}
 
 		for (String name: names) {
 			Class c;
 
 			try {
 				c = Class.forName(name);
 			} catch (Exception e) {
 				c = null;
 			}
 
 			if (c == null) continue;
 
 			EnvironmentAction ea;
 
 			try {
 				ea = (EnvironmentAction) c.newInstance();
 			} catch (Exception e) {
 				ea = null;
 			}
 
 			if (ea == null) continue;
 			eahm.put(ea.getName(), ea);
 		}
 	}
 
 	public Integer requiredStepsForAction(Structure action) {
 		EnvironmentAction ea = eahm.get(action.getFunctor());
 		if (ea == null) return null;
 		return ea.requiredSteps();
 	}
 
 	public Boolean executeAction(String agName, Structure action, IntelligentEnvironment ie) {
 		EnvironmentAction ea = eahm.get(action.getFunctor());
 		if (ea == null) return null;
 		return ea.execute(agName, action, ie);
 	}
 
 	private static Set<String> getAllClasses(String packageName)
             throws Exception
     {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         assert classLoader != null;
         String path = packageName.replace('.', '/');
         Enumeration<URL> resources = classLoader.getResources(path);
         List<URLConnection> dirs = new ArrayList<URLConnection>();
         while (resources.hasMoreElements()) {
             URL resource = resources.nextElement();
             dirs.add(resource.openConnection());
         }
 		Set<String> classes = new HashSet<String>();
 		for (URLConnection uc : dirs) {
 			try {
 				// sai com o nome da classe com o .class e com o path completo
 				JarURLConnection juc = (JarURLConnection) uc;
 				Enumeration<JarEntry> eje = juc.getJarFile().entries();
 				while (eje.hasMoreElements()) {
 					JarEntry je = eje.nextElement();
 					String name = je.getName();
 					if (name.endsWith(".class") && name.startsWith(path)) {
 						if (name.contains("$") == false) {
 							classes.add(packageName+'.'+name.substring(name.lastIndexOf("/")+1, name.length()-6));
 						}
 					}
 				}
 			} catch (java.lang.ClassCastException e) {
 				// sai soh com o nome da classe sem o .class e sem o path
 				sun.net.www.protocol.file.FileURLConnection fuc = (sun.net.www.protocol.file.FileURLConnection) uc;
 				InputStream is = fuc.getInputStream();
 				BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
 				String name = br.readLine();
 				while (name != null) {
 					// here dont have the path complete to test again :'(
 					if (name.endsWith(".class") /*&& name.startsWith(path)*/) {
 						if (name.contains("$") == false) {
 							classes.add(packageName+'.'+name.substring(0, name.length()-6));
 						}
 					}
 					name = br.readLine();
 				}
 			}
 		}
         return classes;
     }
 };
