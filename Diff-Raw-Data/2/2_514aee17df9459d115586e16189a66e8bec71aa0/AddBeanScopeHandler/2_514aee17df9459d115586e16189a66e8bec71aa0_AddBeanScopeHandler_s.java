 package com.vectorsf.jvoice.ui.navigator.handler;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IImportDeclaration;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.JavaElementLabelProvider;
 import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import com.vectorsf.jvoice.model.operations.Flow;
 import com.vectorsf.jvoice.model.operations.provider.flow.ScopeItemProvider;
 
 public class AddBeanScopeHandler extends AbstractHandler implements IHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 
 		Flow flow = getFlow(event);
 		if (flow == null) {
 			return null;
 		}
 
 		IProject project = getProject(flow);
 		IJavaProject jProject = JavaCore.create(project);
 
 		Shell shell = HandlerUtil.getActiveShell(event);
 
 		StandardJavaElementContentProvider contentProvider = new StandardJavaElementContentProvider(
 				false);
 		ILabelProvider labelProvider = new JavaElementLabelProvider(
 				JavaElementLabelProvider.SHOW_BASICS);
 
 		ComponentsSelectionDialog dialog = new ComponentsSelectionDialog(shell,
 				labelProvider, contentProvider);
 		dialog.setInput(jProject);
 		dialog.addFilter(new ComponentFilter());
 		dialog.open();
 
 		return null;
 	}
 
 	private IProject getProject(Flow flow) {
 		URI uri = flow.eResource().getURI();
 		IPath path = new Path(uri.toPlatformString(true));
 		return ResourcesPlugin.getWorkspace().getRoot().getFile(path)
 				.getProject();
 	}
 
 	private Flow getFlow(ExecutionEvent event) {
 		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
 		if (currentSelection instanceof IStructuredSelection) {
 			Object firstElement = ((IStructuredSelection) currentSelection)
 					.getFirstElement();
 			if (firstElement instanceof ScopeItemProvider) {
 				return ((ScopeItemProvider) firstElement).getFlow();
 			}
 		}
 
 		return null;
 	}
 
 	public class ComponentFilter extends ViewerFilter {
 
 		@Override
 		public boolean select(Viewer viewer, Object parentElement,
 				Object element) {
			System.out.println("=>"+element);

 			if (element instanceof ITypeRoot) {
 				ITypeRoot unit = (ITypeRoot) element;
 				IType type = unit.findPrimaryType();
 				
 				if(type == null)
 					return false;
 				
 				IAnnotation[] annotations;
 				try {
 					annotations = type.getAnnotations();
 					for (IAnnotation annotation : annotations) {
 						String elementName = annotation.getElementName();
 						if (elementName
 								.equals("org.springframework.stereotype.Component")) {
 							return true;
 						}  
 						else if (elementName.equals("Component") && element instanceof ICompilationUnit){
 							for (IImportDeclaration _import : ((ICompilationUnit) unit).getImports()) {
 								String importedType = _import.getElementName();
 								if (importedType
 										.equals("org.springframework.stereotype.Component")) {
 									return true;
 								}
 								if (importedType
 										.equals("org.springframework.stereotype.*")) {
 									return true;
 								}
 							}
 						}
 					}
 				} catch (JavaModelException e) {
 					return false;
 				}
 
 				return false;
 			}
 
 			if (element instanceof IPackageFragment) {
 				try {
 					return ((IPackageFragment) element).hasChildren();
 				} catch (JavaModelException e) {
 					return false;
 				}
 			}
 			if (element instanceof IPackageFragmentRoot) {
 //				return !((IPackageFragmentRoot) element).isArchive();
 				return true;
 			}
 			return false;
 		}
 	}
 }
