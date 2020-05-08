 package br.com.boltframework.scan;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 
 import br.com.boltframework.annotation.Action;
 import br.com.boltframework.annotation.Controller;
 import br.com.boltframework.annotation.RunBeforeAction;
 import br.com.boltframework.core.ControllerMapping;
 import br.com.boltframework.http.HttpMethod;
 import br.com.boltframework.util.Constants;
 import br.com.boltframework.util.StringUtils;
 
 public class ClassScanner {
 
 	private String directoryClasses;
 
 	private ClassScanner(String directoryClasses) {
 		this.directoryClasses = directoryClasses;
 	}
 
 	public static class Builder {
 		public ClassScanner withDefaultDirectoryClasses() {
 			return new ClassScanner(null);
 		}
 
 		public ClassScanner withCustomDirectoryClasses(String directory) {
 			return new ClassScanner(directory);
 		}
 	};
 
 	public static Builder createInstance() {
 		return new Builder();
 	}
 
 	public String getDirectoryClasses() {
 		return StringUtils.isNotBlank(directoryClasses) ? directoryClasses : Constants.DEFAULT_DIRECTORY_CLASSES;
 	}
 
 	private Method obtainRunBeforeActionMethod(Class<Object> clazz) {
 		Method[] methods = clazz.getMethods();
 
 		for (Method method : methods) {
 			RunBeforeAction runBeforeAction = method.getAnnotation(RunBeforeAction.class);
 			if (runBeforeAction != null) {
 				return method;
 			}
 		}
 
 		return null;
 	}
 
 	private boolean isMethodAppliedToRunBeforeAction(Method method, String actionName) {
 		RunBeforeAction runBeforeAction = method.getAnnotation(RunBeforeAction.class);
 
 		if (runBeforeAction != null) {
			for (String action : runBeforeAction.applyToActions()) {
				return action.equals(Constants.ALL_ACTIONS) || actionName.equals(action);
 			}
 		}
 
 		return false;
 	}
 
 	public List<ControllerMapping> getActionsByClass(Class<Object> clazz, String controllerMappingName) {
 		List<ControllerMapping> controllersList = new ArrayList<ControllerMapping>();
 		Method[] methods = clazz.getMethods();
 		Method runBeforeAction = obtainRunBeforeActionMethod(clazz);
 
 		for (Method method : methods) {
 			Action action = method.getAnnotation(Action.class);
 
 			if (action != null && action.methods() != null && action.methods().length > 0) {
 				for (HttpMethod httpMethod : action.methods()) {
 					String actionName = method.getName();
 					ControllerMapping controllerMapping = new ControllerMapping();
 					controllerMapping.setController(clazz);
 					controllerMapping.setControllerName(controllerMappingName);
 					controllerMapping.setAction(method);
 					controllerMapping.setActionName(actionName);
 					controllerMapping.setHttpMethod(httpMethod);
 
 					if (isMethodAppliedToRunBeforeAction(runBeforeAction, actionName)) {
 						controllerMapping.setRunBeforeAction(runBeforeAction);
 					}
 					controllersList.add(controllerMapping);
 				}
 			}
 		}
 
 		return controllersList;
 	}
 
 	public boolean isAnnotatedWithController(Class<Object> clazz) {
 		return obtainControllerAnnotationByClass(clazz) != null;
 	}
 
 	public Controller obtainControllerAnnotationByClass(Class<Object> clazz) {
 		Annotation[] annotations = clazz.getAnnotations();
 
 		for (Annotation annotation : annotations) {
 			if (annotation instanceof Controller) {
 				Controller actionAnnotation = (Controller) annotation;
 				return actionAnnotation;
 			}
 		}
 
 		return null;
 	}
 
 	public Class<Object>[] getAllClassesOfApplication(ServletContext servletContext, final String fullPackageName) throws ClassNotFoundException {
 
 		if (servletContext == null) {
 			throw new IllegalArgumentException("The argument 'servletContext' must contain a value.");
 		}
 
 		String outputDirectory = getDirectoryClasses();
 		String actionsDirectoryPath = fullPackageName.replace(Constants.DOT, Constants.FORWARD_SLASH);
 
 		Set<?> libJars = servletContext.getResourcePaths(outputDirectory.concat(actionsDirectoryPath));
 		List<Class<?>> classes = new ArrayList<Class<?>>();
 
 		for (Object jar : libJars) {
 			String file = jar.toString().replace(outputDirectory, Constants.EMPTY_STRING);
 			file = file.replace(actionsDirectoryPath, fullPackageName);
 
 			if (file.endsWith(Constants.FORWARD_SLASH) || file.endsWith(Constants.DOT)) {
 				String actionsSubdirectoryPath = file.substring(0, file.length() - 1).replace(Constants.FORWARD_SLASH, Constants.DOT);
 				Class<?>[] subActions = getAllClassesOfApplication(servletContext, actionsSubdirectoryPath);
 				for (Class<?> subAction : subActions) {
 					classes.add(subAction);
 				}
 			}
 			else if (file.endsWith(Constants.CLASS_EXTENSION)) {
 				file = file.replace(Constants.FORWARD_SLASH, Constants.DOT);
 				file = file.replace(Constants.CLASS_EXTENSION, Constants.EMPTY_STRING);
 
 				if (file.startsWith(Constants.DOT)) {
 					file = file.substring(1);
 				}
 
 				classes.add(Class.forName(file));
 			}
 
 			file = null;
 		}
 
 		@SuppressWarnings("unchecked")
 		Class<Object>[] classArray = classes.toArray(new Class[classes.size()]);
 
 		return classArray;
 	}
 
 }
