 package org.vdfp.uml.common.util;
 
 import java.net.URL;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.ocl.Environment;
 import org.eclipse.ocl.uml.OCL;
 import org.eclipse.ocl.uml.UMLEnvironmentFactory;
 import org.eclipse.uml2.uml.UMLPackage;
 import org.eclipse.uml2.uml.resource.UMLResource;
 import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;
 
 public class UmlResourceSetFactory {
 	public static final String VDFP_MAPPING_PROFILE = "MappingProfile.profile.uml";
 	IFileLocator fileLocator;
 
 	public UmlResourceSetFactory(IFileLocator locator) {
 		this.fileLocator=locator;
 	}
 
 	public ResourceSet prepareResourceSet() {
 //		String oclDelegateURI = OCLDelegateDomain.OCL_DELEGATE_URI;
 //		EOperation.Internal.InvocationDelegate.Factory.Registry.INSTANCE.put(oclDelegateURI,
 //		    new OCLInvocationDelegateFactory.Global());
 //		EStructuralFeature.Internal.SettingDelegate.Factory.Registry.INSTANCE.put(oclDelegateURI,
 //		    new OCLSettingDelegateFactory.Global());
 //		EValidator.ValidationDelegate.Registry.INSTANCE.put(oclDelegateURI,
 //		    new OCLValidationDelegateFactory.Global());
 		Environment.Registry.INSTANCE.registerEnvironment(
 				new UMLEnvironmentFactory().createEnvironment());
 
 		ResourceSet rst = new ResourceSetImpl();
 		EPackage.Registry.INSTANCE.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.put(org.eclipse.ocl.uml.UMLPackage.eNS_URI, org.eclipse.ocl.uml.UMLPackage.eINSTANCE);
 		rst.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,UMLResource.Factory.INSTANCE);
 //		putPathMap(rst, "model/oclstdlib.uml", UMLEnvironment.OCL_STANDARD_LIBRARY_NS_URI,OCLUMLUtil.class,0);
 		putPathMap(rst, "models/profiles/" +VDFP_MAPPING_PROFILE , StereotypeNames.MODELS_PATHMAP,UmlResourceSetFactory.class,2);
 //		putPathMap(rst, "libraries/UMLPrimitiveTypes.library.uml", UMLResource.LIBRARIES_PATHMAP);
 //		putPathMap(rst, "metamodels/UML.metamodel.uml", UMLResource.METAMODELS_PATHMAP);
 //		putPathMap(rst, "profiles/UML2.profile.uml", UMLResource.PROFILES_PATHMAP);
 //		UMLResourcesUtil.init(rst);
 		OCL.initialize(rst);
 //		Environment.Registry.INSTANCE.registerEnvironment();
 		return rst;
 	}
 
 	public void putPathMap(ResourceSet rst, String umlResourceName,
 			String pathMap) {
 		putPathMap(rst, umlResourceName, pathMap, UMLResourcesUtil.class,1);
 	}
 
 	private void putPathMap(ResourceSet rst, String umlResourceName, String pathMap, Class<?> class1, int trimSegments) {
 		URL url = class1.getClassLoader().getResource(umlResourceName);
 		url=fileLocator.resolve(url);
 		URI uri = URI.createURI(url.toExternalForm());
 		Map<URI, URI> uriMap = rst.getURIConverter().getURIMap();
 		uriMap.put(URI.createURI(pathMap), uri.trimSegments(trimSegments).appendSegment(""));
 	}
 
 
 }
