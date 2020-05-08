 package com.vectorsf.jvoice.ui.edit.provider;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 import com.vectorsf.jvoice.model.operations.ComponentBean;
 
 public class MethodsBeanContentProvider extends AdapterFactoryContentProvider {
 
 	public MethodsBeanContentProvider(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	@Override
 	public Object[] getElements(Object object) {
 		@SuppressWarnings("unchecked")
 		List<ComponentBean> comp = (List<ComponentBean>) object;
 		Object[] os = comp.toArray();
 		return os;
 
 	}
 
 	@Override
 	public boolean hasChildren(Object object) {
 		if (object instanceof ComponentBean) {
 			return hasMethods((ComponentBean) object).length > 0;
 		}
 		return super.hasChildren(object);
 	}
 
 	@Override
 	public Object[] getChildren(Object object) {
 		if (object instanceof ComponentBean) {
 			return hasMethods((ComponentBean) object);
 		} else {
 			return super.getChildren(object);
 		}
 
 	}
 
 	private Object[] hasMethods(ComponentBean object) {
 		URI uri = EcoreUtil.getURI(object);
 		IProject project = ResourcesPlugin.getWorkspace().getRoot()
 				.findMember(uri.toPlatformString(true)).getProject();
 		IJavaProject jProject = JavaCore.create(project);
 		IType type;
 		try {
 			type = jProject.findType(object.getFqdn());
 			IMethod[] methods = type.getMethods();
 			List<IMethod> lPublicMethods = new ArrayList<IMethod>();
 			for (IMethod iMethod : methods) {
 				if (Flags.isPublic(iMethod.getFlags())) {
 					lPublicMethods.add(iMethod);
 				}
 			}
 			return lPublicMethods.toArray();
		} catch (JavaModelException | NullPointerException e) {
 			return new Object[0];
 		}
 	}
 }
