 package console;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * The PackageInspector allows to get all classes from a specified package.
  * It also has support to return only classes which are subclasses of a
  * specific superclass or interface.
  *
  * @author Stefan Graupner <stefan.graupner@gmail.com>
  **/
 public class PackageInspector {
 	String packageName;
 	LinkedList<Class<?>> classes;
 
 	/**
 	 * Constructor to set the package name (Fully qualified!)
 	 * @param packageName
 	 */
 	public PackageInspector(String packageName) {
 		this.packageName = packageName;
 		this.classes = new LinkedList<Class<?>>();
 	}
 
 	/**
 	 *
 	 * @return the full list of Classes found in the Package
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	public LinkedList<Class<?>> getClasses() throws IOException, ClassNotFoundException {
 		obtainClassList();
 		return classes;
 	}
 
 	/**
 	 *
 	 * @param baseClass fully qualified name of a class (not necessarily inside the same package)
 	 * @return list of classes from the package having baseClass as interface or superclass
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	public LinkedList<Class<?>> getClasses(String baseClass) throws IOException,
 	  ClassNotFoundException  {
 		LinkedList<Class<?>> classes = getClasses();
 		for (Iterator<?> it = classes.iterator(); it.hasNext(); )
 	        if (!isSuperClass((Class<?>) it.next(), baseClass))
 	            it.remove();
 		return classes;
 	}
 
 	/**
 	 *
 	 * @param baseClass Class-instance of a baseClass
 	 * @return list of all classes which have baseClass as interface or superclass
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	public LinkedList<Class<?>> getClasses(Class<?> baseClass) throws IOException,
 	  ClassNotFoundException {
 		return getClasses(baseClass.getCanonicalName());
 	}
 
 	/**
 	 * determines if a class is interface or superclass of a given class
 	 * @param next
 	 * @param baseClass
 	 * @return true if condition is fulfilled
 	 */
 	private boolean isSuperClass(Class<?> next, String baseClass) {
 		boolean isSuperclass = false;
 		Class<?> current = next;
 
 		// check interfaces
 		Class<?>[] interfaces = current.getInterfaces();
 		for (Class<?> anInterface : interfaces) {
 			if (anInterface.getName().equals(baseClass)) return true;
 		}
 
 		// check for superclass
 		while (!isSuperclass) {
 			current = current.getSuperclass();
 			if (current.getName().equals("java.lang.Object")) break;
 			if (current.getName().equals(baseClass)) {
 				isSuperclass = true;
 				break;
 			}
 		}
 		return isSuperclass;
 	}
 
 	/**
 	 * This method is the entry point method to obtain the class list of a package
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	protected void obtainClassList() throws IOException, ClassNotFoundException {
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		obtainClassList(cl);
 	}
 
 	/**
 	 * This obtainClassList() method does the real work with the current class loader
 	 * Using the thread's class loader enables the package inspector to be used with
 	 * any available loader.
 	 *
 	 * @param loader
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	protected void obtainClassList(ClassLoader loader) throws IOException, ClassNotFoundException {
 		String path = packageName.replace('.', '/');
 
 		Enumeration<URL> resources = loader.getResources(path);
 		if (resources != null) {
 			String filePath = resources.nextElement().getFile();
 			if (filePath.indexOf("%20") > 0) {
 				filePath = filePath.replaceAll("%20", " ");
 			}
 			if (filePath != null) {
			  if (!isInJar(filePath)) {
          classes.addAll(getFromDirectory(new File(filePath)));
        } else {
          classes.addAll(getFromJar(new File(sanitizeJarPath(filePath))));
        }
 			}
 		}
 	}
 
 	/**
 	 * getFromDirectory gets all class files that reside inside a directory
 	 * @param directory
 	 * @return
 	 * @throws ClassNotFoundException
 	 */
 	protected Collection<? extends Class<?>> getFromDirectory(File directory)
 	  throws ClassNotFoundException {
 		Collection<Class<?>> classes = new LinkedList<Class<?>>();
 		if (directory.exists()) {
 			for (String file: directory.list()) {
 				if (file.endsWith(".class")) {
 				  System.out.println(file);
 					String className = packageName + "." + file.replaceAll(".class", "");
 					Class<?> cl = Class.forName(className);
 					classes.add(cl);
 				}
 			}
 		}
 		return classes;
 	}
 
 	/**
 	 * isInJar tests (simple String match) if a requested class is inside a jar-file
 	 * and if the jar is loaded.
 	 * @param className fq class name
 	 * @return true if both conditions hold
 	 **/
 	protected boolean isInJar(String className) {
	  if (className.indexOf(".jar") != 0) {
	    return true;
	  }
 	  return false;
 	}
 
 	/**
 	 * sanitize a jar path
 	 * @param className a class name inside of the requested jar
 	 * @return the path
 	 **/
 	protected String sanitizeJarPath(String className) {
 	  return null;
 	}
 
 	/**
 	 * Fetches all matching classes out of a jar-resident package.
	 * @param jarPath sanitized path to the jar
 	 * @return list of packages
 	 **/
	protected Collection<? extends Class<?>> getFromJar(File jarPath) {

 	  return null;
 	}
 }
