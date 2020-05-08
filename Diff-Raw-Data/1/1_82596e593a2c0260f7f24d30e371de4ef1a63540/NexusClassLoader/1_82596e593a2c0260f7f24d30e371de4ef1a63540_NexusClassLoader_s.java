 package com.nexus.classloading;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLConnection;
 import java.security.CodeSigner;
 import java.security.CodeSource;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.Attributes.Name;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.logging.Level;
 
 import com.nexus.logging.NexusLog;
 
 public class NexusClassLoader extends URLClassLoader{
 	
 	private final List<URL> Sources;
 	private final URLClassLoader Parent;
 	
 	private final List<IClassTransformer> Transformers;
 	private final Map<String, Class<?>> CachedClasses;
 	private final Set<String> InvalidClasses;
 	
 	private final Set<String> ClassLoaderExceptions = new HashSet<String>();
 	private final Set<String> TransformerExceptions = new HashSet<String>();
 	private final Map<Package, Manifest> PackageManifests = new HashMap<Package, Manifest>();
 	
 	private static Manifest EMPTY = new Manifest();
 	
 	private static final String[] RESERVED = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
 	
     private static final boolean DEBUG_CLASSLOADING = Boolean.parseBoolean(System.getProperty("nexus.debugClassLoading", "false"));
     //private static final boolean DEBUG_CLASSLOADING_FINER = DEBUG_CLASSLOADING && Boolean.parseBoolean(System.getProperty("nexus.debugClassLoadingFiner", "false"));
     private static final boolean DEBUG_CLASSLOADING_SAVE = DEBUG_CLASSLOADING && Boolean.parseBoolean(System.getProperty("nexus.debugClassLoadingSave", "false"));
     private static File temp_folder = null;
 	
 	public NexusClassLoader(URL[] sources){
 		super(sources, null);
 		
 		this.Sources = new ArrayList<URL>(Arrays.asList(sources));
 		this.Parent = (URLClassLoader) this.getClass().getClassLoader();
 		this.CachedClasses = new HashMap<String, Class<?>>(1000);
 		this.InvalidClasses = new HashSet<String>(1000);
 		this.Transformers = new ArrayList<IClassTransformer>(2);
 		
 		Thread.currentThread().setContextClassLoader(this);
 
 		this.addClassLoaderExclusion("java.");
 		this.addClassLoaderExclusion("javax.");
 		this.addClassLoaderExclusion("sun.");
 		this.addClassLoaderExclusion("com.mysql.jdbc.");
 		this.addClassLoaderExclusion("com.nexus.classloading.");
 		this.addClassLoaderExclusion("com.nexus.logging.");
 		this.addClassLoaderExclusion("com.google.");
 		this.addClassLoaderExclusion("twitter4j.");
 		this.addClassLoaderExclusion("org.java_websocket.");
 		this.addClassLoaderExclusion("org.objectweb.asm.");
 		
 		if(DEBUG_CLASSLOADING_SAVE){
 			int x = 1;
 			temp_folder = new File("CLASSLOADER_TEMP");
 			while (temp_folder.exists() && x <= 10){
 				temp_folder = new File("CLASSLOADER_TEMP" + x++);
 			}
 			
 			if(temp_folder.exists()){
 				NexusLog.info("DEBUG_CLASSLOADING_SAVE enabled,  but 10 temp directories already exist, clean them and try again.");
 				temp_folder = null;
 			}else{
 				NexusLog.info(String.format("DEBUG_CLASSLOADING_SAVE Enabled, saving all classes to \"%s\"", temp_folder.getAbsolutePath().replace('\\', '/')));
 				temp_folder.mkdirs();
 			}
 		}
 	}
 	
 	public void registerTransformer(String transformerClassName){
 		try{
 			Object transformer = this.loadClass(transformerClassName).newInstance();
 			if(IClassTransformer.class.isInstance(transformer)){
 				Transformers.add((IClassTransformer) transformer);
 			}else{
 				NexusLog.log(Level.SEVERE, "ASM Transformer %s is invalid! It cannot be cast to IClassTransformer", transformerClassName);
 			}
 		}catch(Exception e){
 			NexusLog.log(Level.SEVERE, e, "A critical problem occured registering the ASM transformer class %s", transformerClassName);
 		}
 	}
 	
 	@Override
 	public Class<?> findClass(String name) throws ClassNotFoundException{
 		if(this.InvalidClasses.contains(name)){
 			throw new ClassNotFoundException(name);
 		}
 		
 		for(String st : this.ClassLoaderExceptions){
 			if(name.startsWith(st)){
 				return this.Parent.loadClass(name);
 			}
 		}
 		
 		if(this.CachedClasses.containsKey(name)){
 			return this.CachedClasses.get(name);
 		}
 		
 		for(String st : this.TransformerExceptions){
 			if(name.startsWith(st)){
 				try{
 					Class<?> cl = super.findClass(name);
 					this.CachedClasses.put(name, cl);
 					return cl;
 				}catch(ClassNotFoundException e){
 					this.InvalidClasses.add(name);
 					throw e;
 				}
 			}
 		}
 		
 		try{
 			CodeSigner[] signers = null;
 			int lastDot = name.lastIndexOf('.');
 			String pkgname = lastDot == -1 ? "" : name.substring(0, lastDot);
 			String fName = name.replace('.', '/').concat(".class");
 			//String pkgPath = pkgname.replace('.', '/');
 			URLConnection urlConnection = findCodeSourceConnectionFor(fName);
 			
 			if(urlConnection instanceof JarURLConnection && lastDot > -1){
 				JarURLConnection jarUrlConn = (JarURLConnection) urlConnection;
 				JarFile jf = jarUrlConn.getJarFile();
 				
 				if(jf != null && jf.getManifest() != null){
 					Manifest mf = jf.getManifest();
 					JarEntry ent = jf.getJarEntry(fName);
 					Package pkg = getPackage(pkgname);
 					getClassBytes(name);
 					signers = ent.getCodeSigners();
 					
 					if(pkg == null){
 						pkg = definePackage(pkgname, mf, jarUrlConn.getJarFileURL());
 						this.PackageManifests.put(pkg, mf);
 					}else{
 						if(pkg.isSealed() && !pkg.isSealed(jarUrlConn.getJarFileURL())){
 							NexusLog.log(Level.SEVERE, "The jar file %s is trying to seal already secured path %s", jf.getName(), pkgname);
 						}else if(isSealed(pkgname, mf)){
 							NexusLog.log(Level.SEVERE, "The jar file %s has a security seal for path %s, but that path is defined and not secure", jf.getName(), pkgname);
 						}
 					}
 				}
 				jf.close(); // correct?
 			}else if(lastDot > -1){
 				Package pkg = getPackage(pkgname);
 				
 				if(pkg == null){
 					pkg = definePackage(pkgname, null, null, null, null, null, null, null);
 					this.PackageManifests.put(pkg, EMPTY);
 				}else if(pkg.isSealed()){
 					NexusLog.log(Level.SEVERE, "The URL %s is defining elements for sealed path %s", urlConnection.getURL(), pkgname);
 				}
 			}
 			byte[] basicClass = getClassBytes(name);
 			byte[] transformedClass = runTransformers(name, basicClass);
             saveTransformedClass(transformedClass, name);
 			Class<?> cl = defineClass(name, transformedClass, 0, transformedClass.length, new CodeSource(urlConnection.getURL(), signers));
 			this.CachedClasses.put(name, cl);
 			
 			return cl;
 		}catch(Throwable e){
 			this.InvalidClasses.add(name);
 			if(DEBUG_CLASSLOADING){
 				NexusLog.log(Level.WARNING, e, "Exception encountered attempting classloading of %s", name);
 			}
 			throw new ClassNotFoundException(name, e);
 		}
 	}
 	
 	private boolean isSealed(String path, Manifest man){
 		Attributes attr = man.getAttributes(path);
 		String sealed = null;
 		if(attr != null){
 			sealed = attr.getValue(Name.SEALED);
 		}
 		if(sealed == null){
 			if((attr = man.getMainAttributes()) != null){
 				sealed = attr.getValue(Name.SEALED);
 			}
 		}
 		return sealed.equalsIgnoreCase("true");
 	}
 	
 	private URLConnection findCodeSourceConnectionFor(String name){
 		URL res = findResource(name);
 		if(res != null){
 			try{
 				return res.openConnection();
 			}catch(IOException e){
 				throw new RuntimeException(e);
 			}
 		}
 		return null;
 	}
 	
 	private byte[] runTransformers(String name, byte[] basicClass){
 		for(IClassTransformer transformer : this.Transformers){
 			basicClass = transformer.transform(name, basicClass);
 		}
 		return basicClass;
 	}
 	
 	@Override
 	public void addURL(URL url){
 		super.addURL(url);
 		this.Sources.add(url);
 	}
 	
 	public List<URL> getSources(){
 		return this.Sources;
 	}
 	
 	private byte[] readFully(InputStream stream){
 		try{
 			ByteArrayOutputStream bos = new ByteArrayOutputStream(stream.available());
 			
 			int r;
 			while ((r = stream.read()) != -1){
 				bos.write(r);
 			}
 			
 			return bos.toByteArray();
 		}catch(Throwable t){
 			NexusLog.log(Level.WARNING, "Problem loading class", t);
 			return new byte[0];
 		}
 	}
 	
 	public List<IClassTransformer> getTransformers(){
 		return Collections.unmodifiableList(this.Transformers);
 	}
 	
 	private void addClassLoaderExclusion(String toExclude){
 		this.ClassLoaderExceptions.add(toExclude);
 	}
 	
 	public void addTransformerExclusion(String toExclude){
 		this.TransformerExceptions.add(toExclude);
 	}
 	
 	private void saveTransformedClass(byte[] data, String transformedName){
 		if(!DEBUG_CLASSLOADING_SAVE || temp_folder == null){
 			return;
 		}
 		
 		File outFile = new File(temp_folder, transformedName.replace('.', File.separatorChar) + ".class");
 		File outDir = outFile.getParentFile();
 		
 		if(!outDir.exists()){
 			outDir.mkdirs();
 		}
 		
 		if(outFile.exists()){
 			outFile.delete();
 		}
 		
 		try{
 			NexusLog.fine(String.format("Saving transformed class \"%s\" to \"%s\"", transformedName, outFile.getAbsolutePath().replace('\\', '/')));
 			OutputStream output = new FileOutputStream(outFile);
 			output.write(data);
 			output.close();
 		}catch(IOException ex){
 			NexusLog.log(Level.WARNING, "Could not save transformed class \"%s\"", transformedName);
 			ex.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("resource")
 	public byte[] getClassBytes(String name) throws IOException{
 		if(name.indexOf('.') == -1){
 			for(String res : RESERVED){
 				if(name.toUpperCase(Locale.ENGLISH).startsWith(res)){
 					byte[] data = getClassBytes("_" + name);
 					if(data != null){
 						return data;
 					}
 				}
 			}
 		}
 		
 		InputStream classStream = null;
 		try{
 			URL classResource = findResource(name.replace('.', '/').concat(".class"));
 			if(classResource == null){
 				if(DEBUG_CLASSLOADING){
 					NexusLog.finest(String.format("Failed to find class resource %s", name.replace('.', '/').concat(".class")));
 				}
 				return null;
 			}
 			classStream = classResource.openStream();
 			if(DEBUG_CLASSLOADING){
 				NexusLog.finest(String.format("Loading class %s from resource %s", name, classResource.toString()));
 			}
 			return readFully(classStream);
 		}finally{
 			if(classStream != null){
 				try{
 					classStream.close();
 				}catch(IOException e){
 				}
 			}
 		}
 	}
 }
