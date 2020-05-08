 package org.reuseware.sokan.ui.model.sokanui.util;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.reuseware.sokan.Constraint;
 import org.reuseware.sokan.FacetedRequest;
 import org.reuseware.sokan.ID;
 import org.reuseware.sokan.IndexRow;
 import org.reuseware.sokan.index.emodeler.EModelerManager;
 import org.reuseware.sokan.index.solr.SolrConst;
 import org.reuseware.sokan.index.util.FacetUtil;
 import org.reuseware.sokan.index.util.IndexUtil;
 import org.reuseware.sokan.index.util.ResourceUtil;
 import org.reuseware.sokan.index.util.SokanConst;
 import org.reuseware.sokan.ui.model.sokanui.Artifact;
 import org.reuseware.sokan.ui.model.sokanui.Container;
 import org.reuseware.sokan.ui.model.sokanui.Package;
 import org.reuseware.sokan.ui.model.sokanui.Root;
 import org.reuseware.sokan.ui.model.sokanui.RootPackage;
 import org.reuseware.sokan.ui.model.sokanui.SokanuiFactory;
 
 
 public class SokanuiUtil {
 
 	public static final List<String> DEFAULT_CONTAINER = 
 		Arrays.asList("(default package)");
 	
 	public static void init(Root root) {
 		if (root == null) return;
 		if (root instanceof RootPackage) {
 			RootPackage rootPackage = (RootPackage) root;
 			rootPackage.getPackages().clear();
 			fillRootPackage(rootPackage, IndexUtil.INSTANCE.getIndex());
 		}
 		else {
 			// TODO #1449: implement folder structure
 		}
 
 	}
 
 	private static void fillRootPackage(RootPackage rootPackage, List<IndexRow> rows) {
 		if (rows == null || rows.isEmpty())
 			return;
 
 		for (IndexRow row : rows) {
 			List<String> packageID = toPackageID(rootPackage, row.getArtifactID());
 			if (packageID != null) {
 				getPackage(rootPackage, packageID);
 			}
 		}
 	}
 	
 	private static List<String> toPackageID(RootPackage rootPackage, ID id) {
 		if (id == null) return null;
 		
 		boolean isVisible = true;
 		if (!rootPackage.getVisibleTypes().isEmpty()) {
 			isVisible = false;
 			IndexRow row = IndexUtil.INSTANCE.getIndex(id);
 			if (row == null) {
 				//the element is removed and could have been in a package
 				//from which it will be removed
 				isVisible = true;
 			}
 			else {
 				for(EClass eClass : rootPackage.getVisibleTypes()) {
 					if (EModelerManager.canModel(eClass, row)) {
 						isVisible = true;
 						break;
 					}
 				}	
 			}
 		}
 		
 		if (isVisible) {
 			List<String> idSegments = id.getSegments();
 			
 			if (idSegments.size() < 2)
 				return DEFAULT_CONTAINER;
 
 			return idSegments.subList(0, idSegments.size() - 1);	
 		}
 		
 		return null;
 	}
 	
 	public static Package getPackage(RootPackage rootPackage, List<String> packageID) {
 		if (packageID == null) return null;
 		if (rootPackage == null) return null;
 		for(Package p : rootPackage.getPackages()) {
 			if(p.getId().equals(packageID)) {
 				return p;
 			}
 		}
 		Package p = SokanuiFactory.eINSTANCE.createPackage();
 		p.getId().addAll(packageID);
 		rootPackage.getPackages().add(p);
 		
 		return p;
 	}
 	
 	public static boolean deletePackage(RootPackage rootPackage, List<String> packageID) {
 		if (packageID == null) return false;
 		if (rootPackage == null) return false;
 		for(Package p : rootPackage.getPackages()) {
 			if(p.getId().equals(packageID)) {
 				rootPackage.getPackages().remove(p);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void fillContainer(Container container, ResourceSet resourceSet) {
 		if (container == null) return;
 		
 		container.getElements().clear();
 
 		String packageName = ResourceUtil.idString(container.getId());
 
 		// TODO #1261: use of complex query
 		String queryString;
 		if (packageName.equals(ResourceUtil
 				.idString(DEFAULT_CONTAINER))) {
 			queryString = SokanConst.SEPERATOR + "* NOT "
 					+ SokanConst.SEPERATOR + "*" + SokanConst.SEPERATOR
 					+ "*";
 		} else {
 			queryString = packageName +  SokanConst.SEPERATOR + "* NOT " + packageName
 					+ SokanConst.SEPERATOR + "*" + SokanConst.SEPERATOR
 					+ "*";
 		}
 		List<Constraint> constraints = FacetUtil.buildConstraints(
 				SolrConst.SYS_FIELD_ID, queryString);
 		FacetedRequest request = FacetUtil.buildFacetedRequest(constraints);
 		List<IndexRow> response = IndexUtil.INSTANCE.getIndex(request);
 	
 		for (IndexRow row : response) {
 			updateArtifact(container, row.getArtifactID(), true, resourceSet);
 		}
 	}
 	
 	private static void updateArtifact(Container container, ID id, boolean recreate, ResourceSet resourceSet) {
 		if (id == null) return;
 		
		// unload resources that might have been loaded on demand
		URI uri = ResourceUtil.uriFrom(id);
		if (uri != null) {
			Resource resource = resourceSet.getResource(uri, false);
			if (resource != null) {
				resource.unload();
			}
		}

		if (container == null) return;
 		if (!container.areElementsLoaded()) return;
 		
 		//removes existing if any
 		Iterator<EObject> i = container.getElements().iterator();
 		while (i.hasNext()) {
 			EObject elem = i.next();
 			if (elem instanceof Artifact) {
 				Artifact art = (Artifact) elem;
 				if (art.getId().equals(id.getSegments())) {
 					if (art.areContentsLoaded()) {
 						for(EObject content : art.getContents()) {
 							Resource resource = content.eResource();
 							if (resource != null) {
 								resource.unload();
 							}
 						}
 					}
 					i.remove();
 				}
 			}
 			else if (EModelerManager.wasModelled(elem, id)) {
 				// EModeler is responsible for unloading if needed
 				i.remove();
 			}
 		}
 		
 		if (!recreate) return;
 		
 		Root root = (Root) EcoreUtil.getRootContainer(container);
 		IndexRow row = IndexUtil.INSTANCE.getIndex(id);
 		if (root.getVisibleTypes().isEmpty()) {
 			Artifact artifact = SokanuiFactory.eINSTANCE.createArtifact();
 			artifact.getId().addAll(id.getSegments());
 			artifact.setIndexRow(row);
 			artifact.setResourceSet(resourceSet);
 			container.getElements().add(artifact);
 		} else {
 			for (EClass visibleEClass : root.getVisibleTypes()) {
 				container.getElements().addAll(
 						EModelerManager.createModel(visibleEClass, row, resourceSet));
 			}
 		}
 	}
 
 	public static void update(Root root, Set<ID> delta) {
 		if (delta == null) return;
 		if (root == null) return;
 		
 		if (root instanceof RootPackage) {
 			RootPackage rootPackage = (RootPackage) root;
 			for (ID id : delta) {
 				Container container = getPackage(
 						rootPackage, toPackageID(rootPackage, id));
 				
 				IndexRow row = IndexUtil.INSTANCE.getIndex(id);
 				if (row == null) { //handle removed artifacts
 					updateArtifact(
 							container, id, false, root.getResourceSet());
 					// TODO #1449: performance here could be improved: getElements() will load an unloaded package - instead we could check the index
 					if (container != null && container.getElements().isEmpty()) {
 						rootPackage.getPackages().remove(container);
 					}
 				}
 				else {
 					updateArtifact(
 							container, id, true, root.getResourceSet());
 				}
 			}
 		}
 		else {
 			// TODO #1449: implement folder structure
 		}
 	}
 }
