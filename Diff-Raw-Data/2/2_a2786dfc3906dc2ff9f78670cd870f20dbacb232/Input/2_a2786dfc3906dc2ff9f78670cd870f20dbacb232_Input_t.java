 package input;
 
 import java.lang.annotation.Annotation;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.swing.JComponent;
 
 /**
  * Input API built to allow games to accept input from an expandable set of input devices.
  * 
  * @author Gavin Ovsak, aaronkrolik
  */
 @SuppressWarnings("rawtypes")
 public class Input {
 
 	private List<WeakReference> myWeakReferences = new ArrayList<WeakReference>();
 	private Map<String, Method> gameBehaviors = new HashMap<String, Method>();
 	private ArrayList<InputDevice> inputDevices = new ArrayList<InputDevice>(); 
 	private ResourceMappingObject myInputMap;
 	private ResourceBundle myDefaultSettings;
 	private ResourceBundle myOverrideSettings;
 	
 	public Input(String inputMapResourcePath, JComponent component) {
 		myInputMap = new ResourceMappingObject("mapping", inputMapResourcePath);
 		myDefaultSettings = ResourceBundle.getBundle("input/DefaultSettings");
 		inputDevices.add(new KeyboardInput(component, this));
 		inputDevices.add(new MouseInput(component, this));
 	}
 	
 	public Input(String inputMapResourcePath, String overrideSettingsResourcePath, JComponent component) {
 		this(inputMapResourcePath, component);
 		myOverrideSettings = ResourceBundle.getBundle(overrideSettingsResourcePath);
 	}
 	
 	/**
 	 * put new key/values in our input/game behavior mapping object
 	 * @param inputBehavior
 	 * @param gameBehavior
 	 */
 	public void overrideMapping(String gameBehavior, String inputBehavior) {
 		myInputMap.overrideMapping(gameBehavior, inputBehavior);
 	}
 	
 	public void replaceMappingResourcePath(String resourcePath) {
 		myInputMap.setResourcePath(resourcePath);
 	}
 	
 	public void restoreMappingDefaults(){
 		myInputMap.restoreDefault();
 	}
 
 	/**
 	 * Override our default input settings
 	 * @param String resourcePath is the relative location to the settings resource file
 	 */
 	public void overrideSettings(String resourcePath){
 		myOverrideSettings = ResourceBundle.getBundle(resourcePath);
 	}
 
 	/**
 	 * Include input instance in our collection of instances that can have
 	 * annotated methods invoked
 	 * 
 	 * @param Object input
 	 */
 	@SuppressWarnings("unchecked")
 	public void addListenerTo(Object in) {
 
 		myWeakReferences.add(new WeakReference(in));
 		Class inputClass = in.getClass();
 		
 		while (inputClass != Object.class){
 			if (inputClass.getAnnotation(InputClassTarget.class) != null) {
 				for (Method method : inputClass.getMethods()) {
 					Annotation annotation = method.getAnnotation(InputMethodTarget.class);
 					if (annotation instanceof InputMethodTarget) {
 						gameBehaviors.put(((InputMethodTarget) annotation).name(),method);
 					}
 				}
 			}
 			inputClass = inputClass.getSuperclass();
 		}
 	}
 	
 	/**
 	 * Removes instance from our cache of instances to invoke methods on
 	 * @param Instance to be removed
 	 */
 	public void removeListener(Object in) {
 		for (int i = 0; i < myWeakReferences.size(); i++) {
 			if (in == myWeakReferences.get(i).get()) {
 				myWeakReferences.remove(i);
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Get a setting from our settings resource file objects
	 * @param String settingName
 	 * @return String value out
 	 */
 	protected String getSetting(String settingName){
 		if(myOverrideSettings != null && myOverrideSettings.containsKey(settingName)) {
 			return myOverrideSettings.getString(settingName);
 		} else {
 			return myDefaultSettings.getString(settingName);
 		}
 	}
 
 	/**
 	 * Notification receiver from input devices
 	 * TODO better exception handling
 	 * @param String action (key for dynamicMapping)
 	 * @param AlertObject object (input state and specifics)
 	 */
 	void actionNotification(String inputBehavior, AlertObject object) {
 		System.out.println(inputBehavior);
 		try {
 			if (myInputMap.containsInputBehavior(inputBehavior)) {
 				execute(myInputMap.getGameBehavior(inputBehavior), object);
 			}
 		} catch (NullPointerException e) {
 			System.out.println("Null Pointer Exception");
 		} catch (MissingResourceException e) {
 			System.out.println("Missing Resource Exception! Resources did not contain: "
 							   + inputBehavior);
 		}
 	}
 	
 	/**
 	 * Executes methods using reflection
 	 * TODO: handle exceptions 
 	 * @param key
 	 * @param in
 	 */
 	private void execute(String gameBehavior, AlertObject in) {
 		//System.out.println(gameBehavior);
 		for (WeakReference x : myWeakReferences) {
 			try {
 				Class[] paramClasses = gameBehaviors.get(gameBehavior).getParameterTypes();
 				if(paramClasses.length == 0) {
  					gameBehaviors.get(gameBehavior).invoke(x.get()); //An argument is not required for game behvaiors
 				} else if(paramClasses[0].isInstance(in)) {
  					gameBehaviors.get(gameBehavior).invoke(x.get(), paramClasses[0].cast(in));
 				}
 			} catch (IllegalArgumentException e) {
 			} catch (IllegalAccessException e) {
 			} catch (InvocationTargetException e) {
 			} catch (NullPointerException e) {
 			}
 		}
 	}
 	
 }
 
