 package mapeper.minecraft.modloader;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.AccessControlContext;
 import java.security.AccessController;
 import java.security.CodeSource;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivilegedExceptionAction;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.Attributes.Name;
 import java.util.jar.Manifest;
 
 import sun.misc.Resource;
 import sun.misc.URLClassPath;
 
 import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
 
 class ReplacingClassLoader extends URLClassLoader {
 
         private static final String PACKAGE_NOFIND = getPackageName(ReplacingClassLoader.class.getName());
 
         private final AccessControlContext acc;
         private final URLClassPath ucp;
         private URLClassPath replacement;
         private PrintStream log;
         private final Map avoidPackages;
 
         static ReplacingClassLoader inject(URL[] replacingURLs) throws Exception {
                 ClassLoader replacedLoader = ReplacingClassLoader.class.getClassLoader();
                 if (!(replacedLoader instanceof URLClassLoader)) {
                         throw new IllegalStateException();
                 }
 
                 ClassLoader parentLoader = replacedLoader.getParent();
                 if (parentLoader instanceof ReplacingClassLoader) {
                         return (ReplacingClassLoader) parentLoader;
                 }
 
                 URL[] urls = ((URLClassLoader)replacedLoader).getURLs();
                 
                 ReplacingClassLoader patchLoader = new ReplacingClassLoader(
                                 urls, parentLoader,
                                 replacedLoader, replacingURLs);
                 replaceParent(replacedLoader, patchLoader);
                 return patchLoader;
         }
 
         private static void replaceParent(ClassLoader victim,
                         ReplacingClassLoader newParent) throws NoSuchFieldException,
                         IllegalAccessException {
                 Field f = ClassLoader.class.getDeclaredField("parent");
                 f.setAccessible(true);
                 f.set(victim, newParent);
         }
 
         ReplacingClassLoader(URL[] urls, ClassLoader parent, ClassLoader child, URL[] replacingURLs) throws NoSuchAlgorithmException {
                 super(urls, parent);
                 ucp = new URLClassPath(urls);
                 acc = AccessController.getContext();
 				replacement= new URLClassPath(replacingURLs);
 				for(URL u: replacingURLs)
 				{
 					addURL(u);
 				}
                 avoidPackages = getPackagesToAvoid(child);
                 log = loadLog();
                 md5 = MessageDigest.getInstance("md5");
                 onInit();
         }
 
         private Map getPackagesToAvoid(ClassLoader child) {
                 try {
                         Field pkg = ClassLoader.class.getDeclaredField("packages");
                         pkg.setAccessible(true);
                         return (Map) pkg.get(child);
                 } catch (Throwable t) {
                         throw new RuntimeException(t);
                 }
         }
 
         private PrintStream loadLog() {
 //                try {
 //                        Class clazz = loadClass(LOG_CLASS_NAME, true);
 //                        Field instance = clazz.getDeclaredField(LOG_INSTANCE_NAME);
 //                        return (PrintStream) instance.get(null);
 //                } catch (Throwable e) {
 //                        e.printStackTrace();
 //                }
                 return System.err;
         }
 
         private void onInit() {
                 log("");
                 log("Log start timestamp: " + new Date());
                 log("Bootstrap OK, PatchingClassLoader instantiated");
                 log("   Packages still handled by original ClassLoader:");
                 Iterator it = avoidPackages.keySet().iterator();
                 while (it.hasNext()) {
                         log("   - " + it.next().toString());
                 }
                 log("");
         }
 
         protected Class findClass(final String name) throws ClassNotFoundException {
 //        	System.out.println("findClass("+name);
                 if (name.startsWith(PACKAGE_NOFIND))
                         throw new ClassNotFoundException();
                 try {
                         try {
                                 return (Class) AccessController.doPrivileged(
                                                 new PrivilegedExceptionAction() {
                                                         public Object run() throws ClassNotFoundException {
                                                                 String path = name.replace('.', '/').concat(
                                                                                 ".class");
                                                                 Resource res = ucp.getResource(path, false);
                                                                 if (res != null) {
                                                                         try {
                                                                                 return defineClass(name, res);
                                                                         } catch (IOException e) {
                                                                                 throw new ClassNotFoundException(name, e);
                                                                         }
                                                                 } else {
                                                                         throw new ClassNotFoundException(name);
                                                                 }
                                                         }
                                                 }, acc);
                         } catch (java.security.PrivilegedActionException pae) {
                                 throw (ClassNotFoundException) pae.getException();
                         }
                 } catch (ClassNotFoundException nf) {
                         Throwable cause = nf.getCause();
                         if (cause != null && cause instanceof AvoidThisPackageException) {
 //                              log("avoiding: "+name);
                                 throw nf;
                         }
                         // I have absolutely no idea why this actually works.
                         return super.findClass(name);
                 }
         }
 
         private Class defineClass(String name, Resource res) throws IOException {
                 URL url = res.getCodeSourceURL();
                 String pkgname = getPackageName(name);
                 if (pkgname != null) {
                         if (avoidPackages.containsKey(pkgname)) {
                                 throw new AvoidThisPackageException();
                         }
                         defineOrVerifyPackage(res, url, pkgname);
                 }
                 return defineClass(name, res, url);
         }
 
         private static String getPackageName(String classname) {
                 int i = classname.lastIndexOf('.');
                 if (i == -1) {
                         return null;
                 }
                 String pkgname = classname.substring(0, i);
                 return pkgname;
         }
 
         private void defineOrVerifyPackage(Resource res, URL url, String pkgname)
                         throws IOException {
                 Manifest man = res.getManifest();
                 Package pkg = getPackage(pkgname);
                 if (pkg != null) {
                         verifyPackageSecurity(url, pkgname, pkg, man);
                 } else {
                         definePackage(url, pkgname, man);
                 }
         }
 
         private void verifyPackageSecurity(URL url, String pkgname, Package pkg,
                         Manifest man) {
                 // Package found, so check package sealing.
                 if (pkg.isSealed()) {
                         // Verify that code source URL is the same.
                         if (!pkg.isSealed(url)) {
                                 throw new SecurityException("sealing violation: package "
                                                 + pkgname + " is sealed");
                         }
 
                 } else {
                         // Make sure we are not attempting to seal the package
                         // at this code source URL.
                         if ((man != null) && isSealed(pkgname, man)) {
                                 throw new SecurityException(
                                                 "sealing violation: can't seal package " + pkgname
                                                                 + ": already loaded");
                         }
                 }
         }
 
         private void definePackage(URL url, String pkgname, Manifest man) {
                 if (man != null) {
                         definePackage(pkgname, man, url);
                 } else {
                         definePackage(pkgname, null, null, null, null, null, null, null);
                 }
         }
 
         private Class defineClass(String name, Resource res, URL url)
                         throws IOException {
                 byte[] b = res.getBytes();
                 if (okToPatch(name)) {
                         b = patch(name, b);
                 }
 //                else
 //                {
 //                	System.out.println("Will not allow to patch "+name);
 //                }
                java.security.cert.Certificate[] certs = res.getCertificates();
                 CodeSource cs = new CodeSource(url, certs);
                 return defineClass(name, b, 0, b.length, cs);
         }
 
         private boolean okToPatch(String name) {
                 return !name.startsWith("net.minecraft.")||name.startsWith("net.minecraft.client");
         }
 
         Class defineClass(String name, byte[] b) {
                 return defineClass(name, b, 0, b.length, (CodeSource) null);
         }
 
         private boolean isSealed(String name, Manifest man) {
                 String path = name.replace('.', '/').concat("/");
                 Attributes attr = man.getAttributes(path);
                 String sealed = null;
                 if (attr != null) {
                         sealed = attr.getValue(Name.SEALED);
                 }
                 if (sealed == null) {
                         if ((attr = man.getMainAttributes()) != null) {
                                 sealed = attr.getValue(Name.SEALED);
                         }
                 }
                 return "true".equalsIgnoreCase(sealed);
         }
 
         void log(String msg) {
                 log.println(msg);
                 log.flush();
         }
 
         MessageDigest md5;
         private byte[] patch(String name, byte[] input) {
         	Resource r = replacement.getResource(name.replace('.', '/').concat(".class"));
         	if(r!=null)
         	{
         		byte[] oldmd5=md5.digest(input);
         		System.out.println("Patching "+name);
         		try {
 					input=r.getBytes();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         		System.out.println("OLD: "+HexBin.encode(oldmd5));
         		System.out.println("NEW: "+HexBin.encode(md5.digest(input)));
         	}
         	return input;
             
         }
 
 
         boolean injectUrl(URL jar) {
                 URL[] before = getURLs();
                 addURL(jar);
                 URL[] after = getURLs();
                 return before.length + 1 == after.length;
         }
         @Override
         public URL getResource(String name)
         {
         	Resource r;
 			if((r = ucp.getResource(name))!=null)
 				return r.getURL();
 			else if ((r = replacement.getResource(name))!=null)
 			{
 				return r.getURL();
 			}
 			else return super.getResource(name);
         	
         }
         private static class AvoidThisPackageException extends IOException {
                 private static final long serialVersionUID = 1L;
                 
         }
 }
