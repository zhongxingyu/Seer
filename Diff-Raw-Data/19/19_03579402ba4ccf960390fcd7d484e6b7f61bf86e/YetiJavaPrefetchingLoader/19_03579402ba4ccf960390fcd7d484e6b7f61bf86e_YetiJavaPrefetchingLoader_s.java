 package yeti.environments.java;
 
 import java.io.*;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import yeti.YetiLog;
 import yeti.YetiModule;
 import yeti.YetiName;
 import yeti.YetiRoutine;
 import yeti.YetiType;
 import yeti.environments.YetiLoader;
 
 /**
  * Class that represents the custom class loader to load classes of the program.
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jun 22, 2009
  *
  */
 public class YetiJavaPrefetchingLoader extends YetiLoader {
 
 	/**
 	 * Constructor that creates a new loader.
 	 * 
 	 * @param path the classpath to load classes.
 	 */
 	public YetiJavaPrefetchingLoader(String path) {
 		super(path);
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
 	public Class loadClass(String name, boolean resolve)	throws ClassNotFoundException{ 
 
 		Class clazz = findLoadedClass(name);
 		// has the class already been loaded
 		if (clazz!=null) return clazz;
 		// is it a standard Java Class
		if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.")) {
 			// we load it from within the standard loader
 			clazz=findSystemClass(name);
 			YetiLog.printDebugLog("Class loaded in parent class loader: " + clazz.getName(), this);
 			resolveClass(clazz);
		} else {
			// otherwise, we try to find it...
			clazz=findClass(name);
			// if we could not find it we delegate to the system class loader
			if (clazz==null)
				clazz=findSystemClass(name);
 			resolveClass(clazz);
		}
 		return addDefinition(clazz);
 	}
 
 	/**
 	 * We add the definition of the parameter class to the Yeti structures.
 	 * 
 	 * @param clazz the class to add.
 	 * @return the class that was added.
 	 */
 	@SuppressWarnings("unchecked")
 	public Class addDefinition(Class clazz) {
 
 		// we add the type to the types
 		YetiType type=new YetiJavaSpecificType(clazz.getName());
 		YetiType.allTypes.put(type.getName(), type);
 		YetiLog.printDebugLog("adding " + type.getName() + " to yeti types ", this);
 		
 		// we link this class to the parent class type
 		Class parent = clazz.getSuperclass();
 		
 		if (parent!=null && YetiType.allTypes.containsKey(parent.getName())){
 			YetiLog.printDebugLog("linking " + type.getName() + " to " + parent.getName(), this);
 			YetiType.allTypes.get(parent.getName()).allSubtypes.put(clazz.getName(), type);
 		}
 		
 		// we link this class to the parent interfaces
 		Class []interfaces = clazz.getInterfaces();
 		for (Class i: interfaces ) 
 			if (YetiType.allTypes.containsKey(i.getName())){
 				YetiLog.printDebugLog("linking " + type.getName() + " to " +i.getName(), this);
 				YetiType.allTypes.get(i.getName()).allSubtypes.put(clazz.getName(), type);
 			}
 
 		// we create the YetiModule out of the class
 		YetiModule mod = this.makeModuleFromClass(clazz);
 		YetiModule.allModules.put(clazz.getName(), mod);
 
 		// we add the constructors to the type information	
 		addConstructors(clazz, type, mod);
 		
 		// we add methods to the module in which they were defined		
 		addMethods(clazz, mod);
 		
 		// we add inner classes
 		for(Class declaredClazz: clazz.getDeclaredClasses()){
 			YetiLog.printDebugLog("Adding inner class: " + declaredClazz.getName(), this);
 			addDefinition(declaredClazz);
 		}
 		
 		return clazz;
 	}
 
 	/**
 	 * We add the methods of the class to the module.
 	 * 
 	 * @param clazz the class to add.
 	 * @param module the module in which ad it.
 	 */
 	@SuppressWarnings("unchecked")
 	public void addMethods(Class clazz, YetiModule module) {
 		
 		// we add all methods
 		Method[] methods = clazz.getMethods();
 		for (Method method: methods){
 			boolean usable = true;
 			Class []classes=method.getParameterTypes();
 		
 			// check if method is static
 			boolean isStatic = Modifier.isStatic((method.getModifiers()));
 			YetiType []paramTypes;
 		
 			// if the method is static we do not introduce a slot for the target.
 			int offset = 0;
 			if (isStatic) {
 				paramTypes=new YetiType[classes.length];
 			} else {
 				paramTypes=new YetiType[classes.length+1];
 				offset = 1;
 				if (YetiType.allTypes.containsKey(clazz.getName())){
 					paramTypes[0]=YetiType.allTypes.get(clazz.getName());						
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
 			addMethodToModuleIfUsable(module, method, usable, paramTypes);
 		}
 	}
 
 	/**
 	 * Adds a method to the module if it is usable.
 	 * 
 	 * @param module the module to which add the method.
 	 * @param method the method to add.
 	 * @param usable True if it should be added.
 	 * @param paramTypes the types of the parameters.
 	 */
 	public void addMethodToModuleIfUsable(YetiModule module, Method method, boolean usable, YetiType[] paramTypes) {
 		// if we don't know a type from the method we don't add it
 		if (usable && !YetiJavaMethod.isMethodNotToAdd(method.getName())){
 			YetiLog.printDebugLog("adding method "+method.getName()+" in module "+module.getModuleName(), this);
 			// add it as a creation routine for the return type
 			YetiType returnType = YetiType.allTypes.get(method.getReturnType().getName());
 			if (returnType==null)
 				returnType = new YetiJavaSpecificType(method.getReturnType().getName());
 			YetiRoutine methodRoutine = generateRoutineFromMethod(module , method, paramTypes , returnType);
 			// add it as a creation routine for the type
 			returnType.addCreationRoutine(methodRoutine);
 			// add the method as a routine to test
 			module.addRoutineInModule(methodRoutine);
 		}
 	}
 	
 	/**
 	 * Create a Yeti routine for the Java method to test
 	 * 
 	 * @param module the module to which add the method.
 	 * @param method the method to add.
 	 * @param paramTypes the types of the parameters.
 	 * @param returnType the type returned by the method 
 	 * @return a Yeti routine for this method
 	 */
 	protected YetiRoutine generateRoutineFromMethod(YetiModule module, Method method, YetiType[] paramTypes, YetiType returnType) {
 		return new YetiJavaMethod(YetiName.getFreshNameFrom(method.getName()), paramTypes , returnType, module, method);
 	}
 	
 	/**
 	 * Add the constructors of a class.
 	 * 
 	 * @param clazz the class of the constructor.
 	 * @param type the type of the instance created.
 	 * @param module the module to which add the class.
 	 */
 	@SuppressWarnings("unchecked")
 	public void addConstructors(Class clazz, YetiType type, YetiModule module) {
 		// if the class is abstract, the constructors should not be called
 		if (Modifier.isAbstract(clazz.getModifiers()))
 			return;
 
 		// we add the constructors
 		Constructor[] constructors = clazz.getConstructors();
 		for (Constructor con: constructors){
 			boolean usable = true;
 			Class[] classes= con.getParameterTypes();
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
 			addConstructorFromClassToTypeInModuleIfUsable(clazz, type, module, con, usable, paramTypes);
 		}
 	}
 
 	/**
 	 * Add a constructor to a  module and a type if usable.
 	 * 
 	 * @param clazz the originating class.
 	 * @param type the type of the created object.
 	 * @param module the module to which we should add it.
 	 * @param con the constructor.
 	 * @param usable True if it is usable.
 	 * @param paramTypes the types of the parameters.
 	 */
 	@SuppressWarnings("unchecked")
 	public void addConstructorFromClassToTypeInModuleIfUsable(Class clazz,	YetiType type, YetiModule module, Constructor con, boolean usable,
 			YetiType[] paramTypes) {
 		// if we don't know a type from the constructor we don't add it
 		if (usable){
 			YetiLog.printDebugLog("adding constructor to "+type.getName()+" in module "+module.getModuleName(), this);
 			YetiRoutine constructorRoutine = generateRoutineFromConstructor(clazz, paramTypes , type, module, con);
 			// add it as a creation routine for the type
 			type.addCreationRoutine(constructorRoutine);
 			// add the constructor as a routine to test
 			module.addRoutineInModule(constructorRoutine);
 		}
 	}
 	
 	/**
 	 * Create a Yeti routine for the Java constructor to test
 	 * 
 	 * @param clazz the originating class.
 	 * @param paramTypes the types of the parameters.
 	 * @param type the type of the created object.
 	 * @param mod the module to which we should add it.
 	 * @param m the constructor.
 	 * @return the Yeti routine for the constructor of the class c
 	 */
 	@SuppressWarnings("unchecked")
 	protected YetiRoutine generateRoutineFromConstructor(Class clazz, YetiType[] paramTypes, YetiType type, YetiModule mod, Constructor con) {
 		return new YetiJavaConstructor(YetiName.getFreshNameFrom(clazz.getName()), paramTypes , type, mod, con);
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
 	public Class findClass(String name) throws ClassNotFoundException{
 		File fc=null;
 		Class c=null;
 		
 		// for all paths in class path, we try to load the class 
 		for (String classpath: classpaths){
 			fc=new File(classpath+System.getProperty("file.separator")+name.replace('.', System.getProperty("file.separator").charAt(0))+".class");
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
 		Class clazz ;
 		try {
 			BufferedInputStream fr=new BufferedInputStream(new FileInputStream(file));
 			long l=file.length();
 			byte[] bBuf=new byte[(int)l];
 			// we try to read the file as a byte array
 			fr.read(bBuf,0,(int)l);
 			YetiLog.printDebugLog(name+" read in byte[]", this);
 			// we try to define the class
 			clazz = defineClass(name, bBuf,0,bBuf.length);
 			YetiLog.printDebugLog(name+" defined ", this);
 			return clazz ;
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
 		for (String classpath: classpaths){
 			if (!classpath.endsWith(".jar"))
 			loadAllClassesIn(classpath, "");
 		}
 	}
 	
 	/**
 	 * We load all classes in a directory.
 	 * 
 	 * @param directoryName the name of the directory.
 	 * @param prefix the prefix for the class.
 	 */
 	public void loadAllClassesIn(String directoryName, String prefix) {
 		// we create the directory
 		File dir = new File(directoryName);
 		YetiLog.printDebugLog("loading from classpath: " + directoryName, this);
 		
 		if (dir.isDirectory()) {
 			// we iterate through the content
 			for (File file: dir.listFiles()) {
 				// For each subdirectory we load recursively
 				String cname=file.getName();
 				if (file.isDirectory()){
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
 }
