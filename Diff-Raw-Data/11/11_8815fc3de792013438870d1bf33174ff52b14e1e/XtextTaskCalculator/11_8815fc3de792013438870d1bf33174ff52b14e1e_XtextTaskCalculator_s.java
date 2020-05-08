 package org.eclipse.xtext.todo;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.xtext.builder.IXtextBuilderParticipant;
 import org.eclipse.xtext.resource.IResourceDescription;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceFactory;
 
 import com.google.inject.Inject;
 
 public class XtextTaskCalculator implements IXtextBuilderParticipant {
 
 	@Inject
 	private XtextResourceFactory xtextResourceFactory;
 	
 	@Inject
 	private ITaskElementChecker objElementChecker;
 
 	@Override
 	public void build(IBuildContext context, IProgressMonitor monitor) throws CoreException {
 		// TODO: check for DSL file extension
 		for(IResourceDescription.Delta delta : context.getDeltas()) {
 			URI uri = delta.getUri();
 			IResource resource;
 			if (uri.isPlatformResource()) {
 				IPath path = new Path(uri.toPlatformString(true));
 				resource = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 			} else {
				// log warning "unexpected URI"
 				continue;
 			}
 
 			if (resource.exists()) {
 				resource.deleteMarkers(MarkerCreator.getMarkerType(), true, IResource.DEPTH_INFINITE);
 			}
 
 			XtextResource xtextResource = (XtextResource) xtextResourceFactory.createResource(delta.getUri());
 			try {
 				xtextResource.load(Collections.EMPTY_MAP);
 				new MarkerCreator(resource, objElementChecker, monitor).exec(xtextResource);
 			} catch (IOException e) {
				// log error
 			}
 		}
 	}
 }
