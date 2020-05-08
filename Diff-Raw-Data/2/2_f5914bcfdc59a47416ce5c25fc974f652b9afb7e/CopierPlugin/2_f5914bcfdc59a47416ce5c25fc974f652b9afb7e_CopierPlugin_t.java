 package net.sf.jmoney.copier;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.DatastoreManager;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 import net.sf.jmoney.model2.ObjectCollection;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class CopierPlugin extends AbstractUIPlugin {
 	//The shared instance.
 	private static CopierPlugin plugin;
 	//Resource bundle.
 	private ResourceBundle resourceBundle;
 	private static DatastoreManager savedSessionManager = null;
 	
 	/**
 	 * The constructor.
 	 */
 	public CopierPlugin() {
 		super();
 		plugin = this;
 		try {
 			resourceBundle = ResourceBundle.getBundle("net.sf.jmoney.copier.CopierPluginResources");
 		} catch (MissingResourceException x) {
 			resourceBundle = null;
 		}
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance.
 	 */
 	public static CopierPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the string from the plugin's resource bundle,
 	 * or 'key' if not found.
 	 */
 	public static String getResourceString(String key) {
 		ResourceBundle bundle = CopierPlugin.getDefault().getResourceBundle();
 		try {
 			return (bundle != null) ? bundle.getString(key) : key;
 		} catch (MissingResourceException e) {
 			return key;
 		}
 	}
 
 	/**
 	 * Returns the plugin's resource bundle,
 	 */
 	public ResourceBundle getResourceBundle() {
 		return resourceBundle;
 	}
 
 	/**
 	 * @param sessionManager
 	 */
 	public static void setSessionManager(DatastoreManager sessionManager) {
 		savedSessionManager = sessionManager;
 	}
 
 	/**
 	 * @return
 	 */
 	public static DatastoreManager getSessionManager() {
 		return savedSessionManager;
 	}
 	
     /**
      * Copy the contents of one session to another.
      *
      * The two sessions may be interfaces into different implementations.
      * This is therefore more than just a deep copy.  It is a conversion.
      */
     public void populateSession(Session newSession, Session oldSession) {
         Map objectMap = new Hashtable();
         
         ExtendablePropertySet propertySet = PropertySet.getPropertySet(oldSession.getClass());
     	populateObject(propertySet, oldSession, newSession, objectMap);
     }
 
     private void populateObject(ExtendablePropertySet<?> propertySet, ExtendableObject oldObject, ExtendableObject newObject, Map objectMap) {
     	// For all non-extension properties (including properties
     	// in base classes), read the property value from the
     	// old object and write it to the new object.
     	
     	// The scalar properties
     	for (ScalarPropertyAccessor<?> scalarAccessor: propertySet.getScalarProperties3()) {
     		if (!scalarAccessor.getPropertySet().isExtension()) {
 				copyScalarProperty(scalarAccessor, oldObject, newObject, objectMap);
     		}
     	}
     	
     	// The list properties
     	for (ListPropertyAccessor<?> listAccessor: propertySet.getListProperties3()) {
     		if (!listAccessor.getPropertySet().isExtension()) {
 				copyList(newObject, oldObject, listAccessor, objectMap);
     		}
     	}
     	
     	// Now copy the extensions.  This is done by looping through the extensions
     	// in the old object and, for every extension that exists in the old object,
     	// copy the properties to the new object.
     	for (Iterator extensionIter = oldObject.getExtensionIterator(); extensionIter.hasNext(); ) {
     		Map.Entry mapEntry = (Map.Entry)extensionIter.next();
     		PropertySet<?> extensionPropertySet = (PropertySet)mapEntry.getKey();
     		for (PropertyAccessor propertyAccessor: extensionPropertySet.getProperties1()) {
     			if (propertyAccessor.isScalar()) {
 					ScalarPropertyAccessor<?> scalarAccessor = (ScalarPropertyAccessor)propertyAccessor;
 					copyScalarProperty(scalarAccessor, oldObject, newObject, objectMap);
 				} else {
 					// Property is a list property.
 					ListPropertyAccessor<?> listAccessor = (ListPropertyAccessor)propertyAccessor;
 					copyList(newObject, oldObject, listAccessor, objectMap);
 				}
     		}
     	}
     
     	// TODO: This code works because
     	// Commodity and Account objects are the
     	// only objects referenced by other objects.
     	// Plug-ins could change this, thus breaking this code.
     	if (oldObject instanceof Commodity
     			|| oldObject instanceof Account) {
     		objectMap.put(oldObject, newObject);
     	}
     }
 
     private <V> void copyScalarProperty(ScalarPropertyAccessor<V> propertyAccessor, ExtendableObject oldObject, ExtendableObject newObject, Map objectMap) {
     		V oldValue = oldObject.getPropertyValue(propertyAccessor);
     		V newValue;
     		if (oldValue instanceof ExtendableObject) {
     			newValue = (V)objectMap.get(oldValue);
     		} else {
     			newValue = oldValue;
     		}
 			newObject.setPropertyValue(
 					propertyAccessor,
 					newValue);
     }
     
     private <E extends ExtendableObject> void copyList(ExtendableObject newParent, ExtendableObject oldParent, ListPropertyAccessor<E> listAccessor, Map objectMap) {
 		ObjectCollection<E> newList = newParent.getListPropertyValue(listAccessor);
 		for (E oldSubObject: oldParent.getListPropertyValue(listAccessor)) {
			ExtendablePropertySet<? extends E> listElementPropertySet = listAccessor.getElementPropertySet().getActualPropertySet((Class<? extends E>)oldSubObject.getClass());
 			ExtendableObject newSubObject = newList.createNewElement(listElementPropertySet);
 			populateObject(listElementPropertySet, oldSubObject, newSubObject, objectMap);
 		}
     }
 }
