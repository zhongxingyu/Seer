 package com.vectorsf.jvoice.ui.navigator.filter;
 
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
 
 import com.vectorsf.jvoice.model.base.JVPackage;
 
public class EmptyPackagesFilter extends ViewerFilter {
 
	public EmptyPackagesFilter() {
 	}
 
 	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof JVPackage) {
 			JVPackage jvPackage = (JVPackage) element;
			if (!jvPackage.getBeans().isEmpty()) {
 				return true;
 			}
 			String name = jvPackage.getName();
			for (JVPackage pck : jvPackage.getOwnerModule().getPackages()) {
				if (pck != jvPackage && pck.getName().startsWith(name)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 }
