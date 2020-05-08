 package hu.modembed;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import hu.modembed.impl.ReferencedResourceProvider;
 
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EPackage.Registry;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.osgi.framework.BundleContext;
 
 public class MODembedCore extends Plugin {
 	
 	public static final String PLUGIN_ID = "hu.modembed";
 	
 	private static MODembedCore plugin = null;
 
 	private ProgrammerRegistry progreg = null;
 	
 	
 	public ProgrammerRegistry getProgrammerRegistry() {
 		if (progreg == null){
 			progreg = new ProgrammerRegistry();
 		}
 		return progreg;
 	}
 	
 	public static MODembedCore getDefault(){
 		return plugin;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		super.start(bundleContext);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		super.stop(bundleContext);
 	}
 
 	public static URI resolveStringReference(URI current, String reference){
 		String[] sections = reference.split("/");
 		URI uri = current.trimSegments(1);
 		for(String s : sections){
 			if ("..".equals(s)) uri = uri.trimSegments(1);
 			else uri = uri.appendSegment(s);
 		}
 		return uri;
 	}
 	
 	IReferencedResourceProvider resourceProvider;
 	
 	public IReferencedResourceProvider getResourceProvider() {
 		if (resourceProvider == null){
 			resourceProvider = new ReferencedResourceProvider();//PDEReferencedResourceProvider();
 		}
 		return resourceProvider;
 	}
 	
 	public static List<EClass> getAllSubTypes(ResourceSet resourceSet, EClass base) {
 		List<EClass> subs = new ArrayList<EClass>();
 		subs.add(base);
 
		Registry reg = Registry.INSTANCE;
		if (resourceSet != null){
			reg = resourceSet.getPackageRegistry();
		}
 		
 		for (Object oo : reg.values()) {
 
 			if (oo instanceof EPackage) {
 				Iterator<EObject> iter = ((EPackage) oo).eContents().iterator();
 				while (iter.hasNext()) {
 					EObject o = iter.next();
 					if (o instanceof EClass) {
 						EClass cls = (EClass) o;
 						if (cls.getEAllSuperTypes().contains(base))
 							subs.add(cls);
 					}
 				}
 			}
 		}
 		return subs;
 	}
 	
 }
