 package yeti.environments.java;
 
 import java.io.*;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import yeti.YetiLog;
 import yeti.YetiModule;
 import yeti.YetiName;
 import yeti.YetiType;
 
 /**
  * Class that represents the custom class loader to load classes of the program.
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jun 22, 2009
  *
  */
 public class YetiJavaPrefetchingLoader extends ClassLoader{
 
 	/**
 	 * The classpath of classes to load.
 	 */
 	String []classpaths;
 
 
 	/**
 	 * The general loader.
 	 */
 	public static YetiJavaPrefetchingLoader yetiLoader;
 	
 	/**
 	 * Constructor that creates a new loader.
 	 * 
 	 * @param path the classpath to load classes.
 	 */
 	public YetiJavaPrefetchingLoader(String path) {
 		super();
 		
 		this.classpaths = path.split(System.getProperty("path.separator"));
 		yetiLoader = this;
 	}
 	
 	
 
 	/* (non-Javadoc)
 	 * 
 	 * Standard class loader method to load a class and resolve it.
 	 * 
 	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	public Class loadClass(String name)throws ClassNotFoundException{
 		return loadClass(name,true);
 	}
 	
 	/* (non-Javadoc)
 	 * Standard 
 	 * 
 	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
 	 */
 	@SuppressWarnings("unchecked")
 	public Class loadClass(String name,boolean resolve)	throws ClassNotFoundException{ 
 
 		 
 		Class c;
 		c=findLoadedClass(name);
 		// has the class already been loaded
 		if (c!=null) return c;
 		// is it a standard Java Class
 		if (name.startsWith("java.") || name.startsWith("javax.")||
				name.startsWith("sun.")||name.endsWith(".jar")) {
 			// we load it from within the standard loader
 			c=findSystemClass(name);
 			YetiLog.printDebugLog("Class loaded in parent class loader: "+c.getName(), this);
 			resolveClass(c);
 		}else {
 			// otherwise, we try to find it...
 			c=findClass(name);
 			resolveClass(c);
 		}
 			return addDefinition(c);
 
 	}
 
 	/**
 	 * We add the definition of the parameter class to the Yeti structures.
 	 * 
 	 * @param c the class to add.
 	 * @return the class that was added.
 	 */
 	@SuppressWarnings("unchecked")
 	public Class addDefinition(Class c) {
 
 		// we add the type to the types
 		YetiType type=new YetiJavaSpecificType(c.getName());
 		YetiType.allTypes.put(type.getName(), type);
 		YetiLog.printDebugLog("adding "+type.getName()+" to yeti types ", this);
 		
 		
 		// we link this class to the parent class type
 		Class parent = c.getSuperclass();
 		
 		if (parent!=null && YetiType.allTypes.containsKey(parent.getName())){
 			YetiLog.printDebugLog("linking "+type.getName()+" to "+parent.getName(), this);
 			YetiType.allTypes.get(parent.getName()).allSubtypes.put(c.getName(), type);
 		}
 		
 		// we link this class to the parent interfaces
 		Class []interfaces = c.getInterfaces();
 		for (Class i: interfaces ) 
 			if (YetiType.allTypes.containsKey(i.getName())){
 				YetiLog.printDebugLog("linking "+type.getName()+" to "+i.getName(), this);
 				YetiType.allTypes.get(i.getName()).allSubtypes.put(c.getName(), type);
 			}
 
 		// we create the YetiModule out of the class
 		YetiModule mod = this.makeModuleFromClass(c);
 		YetiModule.allModules.put(c.getName(), mod);
 
 		// we add the constructors to the type information	
 		addConstructors(c, type, mod);
 		
 		// we add methods to the module in which they were defined		
 		addMethods(c, mod);
 		
 		// we add inner classes
 		for(Class c0: c.getDeclaredClasses()){
 			YetiLog.printDebugLog("Adding inner class: "+c0.getName(), this);
 			addDefinition(c0);
 		}
 		
 		
 		return c;
 	}
 
 	/**
 	 * We add the methods of the class to the module.
 	 * 
 	 * @param c the class to add.
 	 * @param mod the module in which ad it.
 	 */
 	@SuppressWarnings("unchecked")
 	public void addMethods(Class c, YetiModule mod) {
 		
 		
 		// we add all methods
 		Method[] methods = c.getMethods();
 		for (Method m: methods){
 			boolean usable = true;
 			Class []classes=m.getParameterTypes();
 		
 			// check if method is static
 			boolean isStatic = Modifier.isStatic((m.getModifiers()));
 			YetiType []paramTypes;
 		
 			// if the method is static we do not introduce a slot for the target.
 			int offset = 0;
 			if (isStatic){
 				paramTypes=new YetiType[classes.length];
 			} else {
 				paramTypes=new YetiType[classes.length+1];
 				offset = 1;
 				if (YetiType.allTypes.containsKey(c.getName())){
 					paramTypes[0]=YetiType.allTypes.get(c.getName());						
 				} else {
 					usable = false;
 				}
 			}
 			
 			// for all types we box the types.
 			for (int i=0; i<classes.length; i++){
 				Class c0 = classes[i];
 				if (YetiType.allTypes.containsKey(c0.getName())){
 					paramTypes[i+offset]=YetiType.allTypes.get(c0.getName());						
 				} else {
 					usable = false;
 				}
 			}
 			addMethodToModuleIfUsable(mod, m, usable, paramTypes);
 		}
 	}
 
 
 
 	/**
 	 * Adds a method to the module if it is usable.
 	 * 
 	 * @param mod the module to which add the method.
 	 * @param m the method to add.
 	 * @param usable True if it should be added.
 	 * @param paramTypes the types of the parameters.
 	 */
 	public void addMethodToModuleIfUsable(YetiModule mod, Method m, boolean usable, YetiType[] paramTypes) {
 		// if we don't know a type from the method we don't add it
 		if (usable && !YetiJavaMethod.isMethodNotToAdd(m.getName())){
 			YetiLog.printDebugLog("adding method "+m.getName()+" in module "+mod.getModuleName(), this);
 			// add it as a creation routine for the return type
 			YetiType returnType = YetiType.allTypes.get(m.getReturnType().getName());
 			if (returnType==null)
 				returnType = new YetiJavaSpecificType(m.getReturnType().getName());
 			YetiJavaMethod method = new YetiJavaMethod(YetiName.getFreshNameFrom(m.getName()), paramTypes , returnType, mod,m);
 			returnType.addCreationRoutine(method);
 			// add the constructor as a routines to test
 			mod.addRoutineInModule(method);
 		}
 	}
 
 	/**
 	 * Add the constructors of a class.
 	 * 
 	 * @param c the class of the constructor.
 	 * @param type the type of the instance created.
 	 * @param mod teh module to which add the class.
 	 */
 	@SuppressWarnings("unchecked")
 	public void addConstructors(Class c, YetiType type, YetiModule mod) {
 		// if the class is abstract, the constructors should not be called
 		if (Modifier.isAbstract(c.getModifiers()))
 			return;
 
 		// we add the constructors
 		Constructor[]constructors = c.getConstructors();
 		for (Constructor m: constructors){
 			boolean usable = true;
 			Class []classes= m.getParameterTypes();
 			YetiType []paramTypes=new YetiType[classes.length];
 			// for all types we box the types.
 			for (int i=0; i<classes.length; i++){
 				Class c0 = classes[i];
 				if (YetiType.allTypes.containsKey(c0.getName())){
 					paramTypes[i]=YetiType.allTypes.get(c0.getName());						
 				} else {
 					usable = false;
 				}
 			}
 			addConstructorFromClassToTypeInModuleIfUsable(c, type, mod, m,
 					usable, paramTypes);
 		}
 	}
 
 
 
 	/**
 	 * Add a constructor to a  module and a type if usable.
 	 * 
 	 * @param c the originating class.
 	 * @param type the type of the created object.
 	 * @param mod the module to which we should add it.
 	 * @param m the constructor.
 	 * @param usable True if it is usable.
 	 * @param paramTypes the types of the parameters.
 	 */
 	@SuppressWarnings("unchecked")
 	public void addConstructorFromClassToTypeInModuleIfUsable(Class c,	YetiType type, YetiModule mod, Constructor m, boolean usable,
 			YetiType[] paramTypes) {
 		// if we don't know a type from the constructor we don't add it
 		if (usable){
 			YetiLog.printDebugLog("adding constructor to "+type.getName()+" in module "+mod.getModuleName(), this);
 			YetiJavaConstructor construct = new YetiJavaConstructor(YetiName.getFreshNameFrom(c.getName()), paramTypes , type, mod,m);
 			// add it as a creation routine for the type
 			type.addCreationRoutine(construct);
 			// add the constructor as a routines to test
 			mod.addRoutineInModule(construct);
 		}
 	}
 
 	/**
 	 * Create an empty module from a class (using its class name).
 	 * 
 	 * @param c the class to make a module from.
 	 * @return The module created.
 	 */
 	@SuppressWarnings("unchecked")
 	public YetiModule makeModuleFromClass(Class c){
 		YetiModule mod=new YetiJavaModule(c.getName());
 		
 		return mod;
 	}
 	
 	/* (non-Javadoc)
 	 * 
 	 * Standard javadoc function.
 	 * 
 	 * @see java.lang.ClassLoader#findClass(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	public Class findClass(String name)throws ClassNotFoundException{
 		File fc=null;
 		Class c=null;
 		
 		// for all paths in class path, we try to load the class 
 		for (String s0:classpaths){
 			fc=new File(s0+System.getProperty("file.separator")+name.replace('.', System.getProperty("file.separator").charAt(0))+".class");
 			YetiLog.printDebugLog("trying: "+fc.getAbsolutePath(), this);
 			// we actually check that the class exists
 			if (fc.exists()){
 				YetiLog.printDebugLog("found it", this);
 				c=readClass(fc,name);
 				break;
 			}
 		}
 		if (c==null) throw new ClassNotFoundException(name);
 		return c;
 	}
 
 	/**
 	 *  Utility function to read the class from disk. Should be extended in the future to add reading from a jar file.
 	 * 
 	 * @param file the file in which the class is.
 	 * @param name the name of the class.
 	 * @return the class read.
 	 */
 	@SuppressWarnings("unchecked")
 	public Class readClass(File file,String name){
 		Class c;
 		try {
 			BufferedInputStream fr=new BufferedInputStream(new FileInputStream(file));
 			long l=file.length();
 			byte[] bBuf=new byte[(int)l];
 			// we try to read the file as a byte array
 			fr.read(bBuf,0,(int)l);
 			YetiLog.printDebugLog(name+" read in byte[]", this);
 			// we try to define the class
 			c=defineClass(name, bBuf,0,bBuf.length);
 			YetiLog.printDebugLog(name+" defined ", this);
 			return c;
 		} catch (Throwable e){
 			e.printStackTrace();
 			YetiLog.printDebugLog(name+" not loaded", this);
 			return null;
 		}
 	}
 
 	/**
 	 * We load all the classes in the classpath.
 	 */
 	public void loadAllClassesInPath(){
 		for (String s0:classpaths){
 			loadAllClassesIn(s0,"");
 		}
 		
 	}
 	/**
 	 * We load all classes in a directory.
 	 * 
 	 * @param directoryName the name of the directory.
 	 * @param prefix the prefix for the class.
 	 */
 	public void loadAllClassesIn(String directoryName, String prefix){
 		File dir=null;
 		String s=directoryName;
 		YetiLog.printDebugLog("loading from classpath: "+s, this);
 		// we create the directory
 		dir=new File(directoryName);
 		
 		// we iterate through the content
 		for (File f0:dir.listFiles()){
 			// For each subdirectory we load recursively
 			String cname=f0.getName();
 			if (f0.isDirectory()){
 				if (prefix.equals("")){
 					loadAllClassesIn(directoryName+System.getProperty("file.separator")+cname,cname);
 				}else{
 					loadAllClassesIn(directoryName+System.getProperty("file.separator")+cname,prefix+"."+cname);
 					}
 			} else
 				// otherwise we load the class
 				if (cname.endsWith(".class")){
 					String className=cname;
 					className=className.substring(0,className.length()-6);
 					YetiLog.printDebugLog("reading "+className, this);
 					try {
 						// we actually try to load the class
 						if (prefix.equals(""))
 							loadClass(className);
 						else
 							loadClass(prefix+"."+className);
 					} catch (ClassNotFoundException e) {
 						// should never happen
 						e.printStackTrace();
 					}
 				}
 		}
 	}
 }
