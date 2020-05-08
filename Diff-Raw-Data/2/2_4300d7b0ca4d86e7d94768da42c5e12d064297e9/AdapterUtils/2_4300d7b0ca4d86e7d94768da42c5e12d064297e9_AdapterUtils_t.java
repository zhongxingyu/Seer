 package org.eclipse.emf.compare.diff.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * Usefull methods for EMF adapter factories
  * 
 * @author Cedric Brun  <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a> 
  * 
  */
 public class AdapterUtils {
 	/**
 	 * create a new adapter util parsing the emf extension points
 	 * 
 	 */
 	public AdapterUtils() {
 		super();
 		parseExtensionMetadata();
 	}
 
 	private static final String ADAPTER_FACTORY_EXTENSION_POINT = "org.eclipse.emf.edit.itemProviderAdapterFactories"; //$NON-NLS-1$
 
 	/**
 	 * Return the nsURI adapter factory if existing
 	 * 
 	 * @param nsURI
 	 * @return the nsURI adapter factory if existing, null otherwise
 	 */
 	public AdapterFactory findAdapterFactory(String nsURI) {
 		if (factories.containsKey(nsURI))
 			return factories.get(nsURI)
 					.getAdapterInstance();
 		return null;
 	}
 
 	/**
 	 * Return the adapter factory for this eobject
 	 * 
 	 * @param eObj
 	 * @return specific adapter factory or null
 	 */
 	public AdapterFactory findAdapterFactory(EObject eObj) {
 		String uri = eObj.eClass().getEPackage().getNsURI();
 		return findAdapterFactory(uri);
 	}
 
 	protected Map<String, AdapterFactoryDescriptor> factories = new HashMap<String, AdapterFactoryDescriptor>();
 
 	private void parseExtensionMetadata() {
 		IExtension[] extensions = Platform.getExtensionRegistry()
 				.getExtensionPoint(ADAPTER_FACTORY_EXTENSION_POINT)
 				.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IConfigurationElement[] configElements = extensions[i]
 					.getConfigurationElements();
 			for (int j = 0; j < configElements.length; j++) {
 				AdapterFactoryDescriptor desc = parseAdapterFactory(configElements[j]);
 				factories.put(desc.getNsURI(), desc);
 			}
 		}
 
 	}
 
 	private AdapterFactoryDescriptor parseAdapterFactory(
 			IConfigurationElement configElements) {
 		AdapterFactoryDescriptor desc = new AdapterFactoryDescriptor(
 				configElements);
 		return desc;
 	}
 }
 
 class AdapterFactoryDescriptor {
 	String nsURI;
 
 	String className;
 
 	protected IConfigurationElement element;
 
 	/**
 	 * Constructs a new adapter factory descriptor from an IConfigurationElement
 	 * 
 	 * @param configElements
 	 */
 	public AdapterFactoryDescriptor(IConfigurationElement configElements) {
 		element = configElements;
 		this.nsURI = element.getAttribute("uri"); //$NON-NLS-1$
 		this.className = element.getAttribute("class"); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 * @return the adapter class name
 	 */
 	public String getClassName() {
 		return className;
 	}
 
 	/**
 	 * 
 	 * 
 	 * @return The adapter nsURI
 	 */
 	public String getNsURI() {
 		return nsURI;
 	}
 
 	AdapterFactory factory;
 
 	/**
 	 * 
 	 * @return the corresponding adapter factory instance
 	 */
 	public AdapterFactory getAdapterInstance() {
 		if (factory == null) {
 			try {
 				factory = (AdapterFactory) element
 						.createExecutableExtension("class"); //$NON-NLS-1$
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 		return factory;
 	}
 
 }
