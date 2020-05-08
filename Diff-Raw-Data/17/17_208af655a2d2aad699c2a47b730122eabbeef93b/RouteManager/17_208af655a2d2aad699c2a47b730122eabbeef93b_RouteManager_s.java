 package com.roshka.raf.route;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 
 import javax.servlet.ServletContext;
 
 import com.roshka.raf.annotations.RAFContext;
 import com.roshka.raf.annotations.RAFMethod;
 import com.roshka.raf.annotations.RAFRequest;
 import com.roshka.raf.refl.RAFParameter;
 
 public class RouteManager {
 	
 	private static boolean _initialized;
 	private static Map<String, Route> _routes;
 	private static Set<CtClass> _classesToProcess;
 
 	public static void initialize(ServletContext ctx)
 	{
 		if (!_initialized)
 			doInitialize(ctx);
 	}
  
 	private static void doInitialize(ServletContext ctx) {
 		_routes = new HashMap<String, Route>();
 		_classesToProcess = new HashSet<CtClass>();
 		String rp = ctx.getRealPath("WEB-INF/classes/");
 		loadClasses(new File(rp));
 		try {
 			loadRoutes();
 		} catch (CannotCompileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		_classesToProcess.clear();
 		_classesToProcess = null;
 		_initialized = true;
 	}
 	
 	public static Route getRoute(String path)
 	{
 		return _routes.get(path);
 	}
 	
 	private static RAFParameter createRAFParameter(Class<?> clazz, com.roshka.raf.annotations.RAFParameter rpAnnotation)
 	{
 		RAFParameter ret = new RAFParameter(clazz, rpAnnotation.name());
 		ret.setMandatory(rpAnnotation.mandatory());
 		if (!rpAnnotation.defaultValue().equals("[unassigned]")) {
 			ret.setDefaultValue(rpAnnotation.defaultValue());
 		}
 		ret.setDefaultValue(rpAnnotation.defaultValue());
 		ret.setDateFormat(rpAnnotation.dateFormat());
 		return ret;
 	}
 	
 	private static void loadRoute(Class<?> clazz, List<Field> contextFields, List<Field> requestFields, Method m, RAFMethod rafMethodAnnotation) 
 	{
 		com.roshka.raf.refl.RAFMethod rafMethod = new com.roshka.raf.refl.RAFMethod(m);
 		
 		Annotation[][] annotations = m.getParameterAnnotations();
 		List<RAFParameter> params = new ArrayList<RAFParameter>();
 		for (int i = 0; i < annotations.length; i++) {
 			Annotation[] paramAnnotations = annotations[i];
 			Class<?> parameterClass = m.getParameterTypes()[i];
 			com.roshka.raf.annotations.RAFParameter rpAnnotation = null;
 			for (Annotation annotation : paramAnnotations) {
 				if (annotation instanceof com.roshka.raf.annotations.RAFParameter) {
 					rpAnnotation = (com.roshka.raf.annotations.RAFParameter) annotation;
 				}
 			}
 			if (rpAnnotation != null)  {
 				params.add(createRAFParameter(parameterClass, rpAnnotation));
 			}
 			//else
 				// TODO: throws exception. invalid route.
 				//params.add(new RAFParameter(parameterClass, parameterClass));
 		}
 		
 		Route r = new Route(rafMethodAnnotation.value(), clazz, rafMethod, params, contextFields, requestFields, rafMethodAnnotation.acceptedMethods());
 		_routes.put(rafMethodAnnotation.value(), r);
 	}
 	
 	private static void loadRoutesFromClass(Class<?> clazz) 
 	{
 		
 		Field[] fields = clazz.getDeclaredFields();
 //		clazz.get
 		List<Field> contextFields = new ArrayList<Field>();
 		List<Field> requestFields = new ArrayList<Field>();
 		for (Field field : fields) {
 			RAFContext rc = field.getAnnotation(RAFContext.class);
 			if (rc != null) {
				if (field.getType().isAssignableFrom(com.roshka.raf.context.RAFContext.class)) {
 					contextFields.add(field);
 				}
 			}
 			RAFRequest rr = field.getAnnotation(RAFRequest.class);
 			if (rr != null) {
 				if (field.getType().isAssignableFrom(com.roshka.raf.request.RAFRequest.class)) {
 					requestFields.add(field);
 				}
 			}
 		}
 		
 		Method[] methods = clazz.getMethods();
 		for (Method m : methods) {
 			RAFMethod rm = m.getAnnotation(RAFMethod.class);
 			if (rm != null) {
 				loadRoute(clazz, contextFields, requestFields, m, rm);
 			}
 		}
 		
 	}
 	
 	private static void loadRoutes() 
 		throws CannotCompileException
 	{
 		for (CtClass ctClass : _classesToProcess) {
 			
 			Class<?> clazz = ctClass.toClass();
 			loadRoutesFromClass(clazz);
 		}
 	}
 	
 	private static void loadClasses(File directory)
 	{
 		// return if directory does not exist
         if (!directory.exists()) {
             return;
         }
         
         // 
         File[] files = directory.listFiles();
         for (File file : files) {
             if (file.isDirectory()) {
                 loadClasses(file);
             } else if (file.getName().endsWith(".class")) {
             	try {
                 	DataInputStream dstream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
 
                 	ClassPool cp = ClassPool.getDefault();
                 	CtClass ctClass = cp.makeClass(dstream);
                 	
                 	CtMethod[] ctMethods = ctClass.getMethods();
                 	for (CtMethod ctMethod : ctMethods) {
                 		Object[] annotations = ctMethod.getAvailableAnnotations();
                 		for (Object annotation : annotations) {
                 			if (annotation instanceof RAFMethod) {
                 				_classesToProcess.add(ctClass);
                 				break;
                 				
                 			}
                 		}
                 	}
                 	
             	} catch (Exception e) {
             		// TODO: improve exception handling
             		e.printStackTrace();
             	}
             	
             	
             }
         }
 
 	}
 	
 }
