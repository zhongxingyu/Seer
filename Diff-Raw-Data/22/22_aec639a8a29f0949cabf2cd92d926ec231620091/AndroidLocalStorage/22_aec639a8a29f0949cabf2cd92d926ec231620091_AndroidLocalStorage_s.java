 package psb;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 
 class AndroidLocalStorage implements ILocalStorage {
 
 	private static Object context, preferences, editor;
 	private static Method putStringMethod, getStringMethod,
 		removeKeyMethod, applyMethod;
 	
 	private static Class<?> getContextClass(){
 		try {
 			return AndroidLocalStorage.class.getClassLoader().loadClass("android.content.Context");
 		} catch (ClassNotFoundException e) {}
 		return null;
 	}
 	
 	static {
 		context = PSBContext.getContext();
 		if(context == null)
 			throw new NullPointerException("Must call PSBContext.setContext(android.content.Context) for android application");
		if(context.getClass() != getContextClass()) 
 			throw new RuntimeException("PSBContext.context must be an instance of android.content.Context"); 
 		try {
 		    preferences = ReflectionHelper.getMethod(context.getClass(),
 		    		"getSharedPreferences").invoke(context,
 		    				"PServiceBus_Settings", 0);
 		    editor = ReflectionHelper.getMethod(preferences.getClass(), "edit")
 		    		.invoke(preferences);
 		    Class<?> editorType = editor.getClass(),
 		    		preferenceType = preferences.getClass();
 		    putStringMethod = ReflectionHelper.getMethod(editorType, "putString");
 		    removeKeyMethod = ReflectionHelper.getMethod(editorType, "remove");
 		    applyMethod = ReflectionHelper.getMethod(editorType, "apply");
 		    getStringMethod = ReflectionHelper.getMethod(preferenceType, "getString");
 		}
 		catch (SecurityException e) {} 
 		catch (IllegalAccessException e) {} 
 		catch (IllegalArgumentException e) {} 
 		catch (InvocationTargetException e) {}
 	}
 	
 	@Override
 	public void set(String key, String value) {
 		try {
 			putStringMethod.invoke(editor, key, value);
 		    applyMethod.invoke(editor);
 		} catch (IllegalAccessException e) {} 
 		catch (IllegalArgumentException e) {} 
 		catch (InvocationTargetException e) {}
 	}
 
 	@Override
 	public String get(String key) {
 		String value = null;
 		try {
 			value = (String)getStringMethod.invoke(preferences, key, value);
 		} catch (IllegalAccessException e) {} 
 		catch (IllegalArgumentException e) {} 
 		catch (InvocationTargetException e) {}
 		return value;
 	}
 	
 	@Override
 	public void remove(String key){
 		try {
 			removeKeyMethod.invoke(editor, key);
 		    applyMethod.invoke(editor);
 		} catch (IllegalAccessException e) {} 
 		catch (IllegalArgumentException e) {} 
 		catch (InvocationTargetException e) {}
 	}
 
 	@Override
 	public String getType(){
 		return "Android";
 	}
 }
