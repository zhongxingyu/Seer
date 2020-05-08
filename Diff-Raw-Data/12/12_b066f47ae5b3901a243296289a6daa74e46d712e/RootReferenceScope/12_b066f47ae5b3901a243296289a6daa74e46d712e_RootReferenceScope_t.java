 /**
  * 
  */
 package hu.e.parser.convert.impl;
 
 import hu.modembed.model.core.NamedElement;
 import hu.modembed.model.core.RootElement;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 
 /**
  * @author balazs.grill
  *
  */
 public class RootReferenceScope extends AbstractCrossReferenceScope {
 
 	private final List<RootElement> libraries;
 	
 	private final ResourceSet resourceSet;
 	private final IProject project;
 	
 	public RootReferenceScope(IProject project, ResourceSet resourceSet, RootElement library, List<String> libraries) {
 		this.libraries = new ArrayList<RootElement>(libraries.size()+1);
 		this.libraries.add(library);
 		this.resourceSet = resourceSet;
 		this.project = project;
 		
 		for(String use : libraries){
 			URI useduri = getReferredLibURI(use);
 			RootElement usedlib = null;
 			if (useduri != null){
 				Resource ur = resourceSet.getResource(useduri, true);
 				for(EObject eo : ur.getContents()){
 					if (eo instanceof RootElement){
 						usedlib = (RootElement)eo;
 					}
 				}
 			}
 			if (usedlib != null){
 				this.libraries.add(usedlib);
 			}else{
 				//TODO Cannot resolve library!
 			}
 		}
 	}
 
 	private void search(RootElement re, String id, List<EObject> results){
 		for(EObject le : re.eContents()){
 			if (le instanceof NamedElement){
 				if (id.equals(((NamedElement) le).getName())){
 					results.add(le);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public List<EObject> resolve(String id) {
 		List<EObject> result = new LinkedList<EObject>();
 		if (id.contains("::")){
 			String[] ss = id.split("::");
 			if (ss.length == 2){
 				URI uri = getReferredLibURI(ss[0]);
				if (uri != null){
					Resource r = resourceSet.getResource(uri, true);
					if (r != null){
						for(EObject eo : r.getContents()){
							if (eo instanceof RootElement){
								search((RootElement)eo, ss[1], result);
							}
 						}
 					}
 				}
 			}
 		}else{
 			URI uri = getReferredLibURI(id);
 			if (uri != null){
 				Resource r = resourceSet.getResource(uri, true);
 				if (r != null){
 					for(EObject eo : r.getContents()){
 						if (eo instanceof RootElement){
 							if (id.equals(((RootElement) eo).getName())) result.add(eo);
 						}
 					}
 				}
 			}
 			for(RootElement l : libraries){
 				search(l, id, result);
 			}
 		}
 		return result;
 	}
 
 	private URI getReferredLibURI(String name){
 		List<IProject> projects = new LinkedList<IProject>();
 		projects.add(project);
 		try {
 			projects.addAll(Arrays.asList(project.getDescription().getReferencedProjects()));
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		final List<IFile> possibleFiles = new LinkedList<IFile>();
 		final String filename = name+".xmi";
 		
 		for(IProject p : projects){
 			try {
 				if (p.exists()) p.accept(new IResourceVisitor() {
 					
 					@Override
 					public boolean visit(IResource resource) throws CoreException {
 						if (resource instanceof IFile){
 							if (filename.equals(resource.getName())){
 								possibleFiles.add((IFile)resource);
 							}
 							return false;
 						}
 						return true;
 					}
 				});
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		if (!possibleFiles.isEmpty()){
 			return URI.createPlatformResourceURI(possibleFiles.get(0).getFullPath().toString(), true);
 		}
 		return null;
 	}
 	
 }
