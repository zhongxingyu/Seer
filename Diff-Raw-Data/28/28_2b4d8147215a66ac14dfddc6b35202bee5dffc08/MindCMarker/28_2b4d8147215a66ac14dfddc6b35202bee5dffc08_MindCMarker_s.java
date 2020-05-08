 package org.ow2.fractal.mind.ide;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 
 public class MindCMarker {
 	
	private final static String MARKER_ID 			= MindActivator.ID+".mindcProblem";
 	
 	private final static String MARKER_DESCRIPTION 	= "description";
 	private final static String MARKER_PARAMETERS 	= "parameters";
 
 	
 	public static IMarker mark(IResource resource) throws CoreException {
 		return resource.createMarker(MARKER_ID);
 	}
 
 	public static void unmark(IResource resource, boolean includeSubtypes, int depth) throws CoreException {
 		resource.deleteMarkers(MARKER_ID,includeSubtypes,depth);
 	}
 
 	public static void setSeverity(IMarker marker, int severity) throws CoreException {
 		marker.setAttribute(IMarker.SEVERITY,severity);
 		
 	}
 
 	public static void setDescription(IMarker marker, String description, Object ... parameters) throws CoreException {
 		
 		StringBuffer encodedParameters = new StringBuffer();
 		for (Object parameter : parameters) {
 			encodedParameters.append(parameter);
 			encodedParameters.append(";");
 		}
 		
 		marker.setAttribute(MARKER_DESCRIPTION,description);
 		marker.setAttribute(MARKER_PARAMETERS,encodedParameters.toString());
 		
 		marker.setAttribute(IMarker.MESSAGE, MessageFormat.format(description,parameters));
 	}
 
 	public static String getMessage(IMarker marker) {
 		return marker.getAttribute(IMarker.MESSAGE,"");
 	}
 
 	public static String getDescription(IMarker marker) {
 		return marker.getAttribute(MARKER_DESCRIPTION,"");
 	}
 	
 	public static String[] getParameters(IMarker marker) {
 		return marker.getAttribute(MARKER_PARAMETERS,"").split(";");
 	}
 
 }
