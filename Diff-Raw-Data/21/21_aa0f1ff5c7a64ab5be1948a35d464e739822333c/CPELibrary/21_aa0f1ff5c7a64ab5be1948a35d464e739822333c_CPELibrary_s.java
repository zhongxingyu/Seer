 package net.kbserve.pyjdt.properties.models;
 
 import org.eclipse.core.resources.IProject;

 
 public class CPELibrary extends CPEAbstractContainer implements ICPEType {
 
 	@Override
 	public String getRealPath(IProject project) {
		//TODO: what we should do is get the parent path and check if we're relative I thinks.
		return getPath();//CPE_LIBRARY is always the abspath
 	}
	

 }
