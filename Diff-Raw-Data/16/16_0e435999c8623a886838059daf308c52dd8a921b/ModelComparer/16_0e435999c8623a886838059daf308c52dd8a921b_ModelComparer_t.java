 package hu.bme.incquery.deps.core;
 
 import hu.bme.incquery.deps.wsmodel.WMethod;
 import hu.bme.incquery.deps.wsmodel.WNamedElement;
 import hu.bme.incquery.deps.wsmodel.WType;
 import hu.bme.incquery.deps.wsmodel.WWorkspace;
 import hu.bme.incquery.deps.wsmodel.WsmodelFactory;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 public class ModelComparer {
 
 	@SuppressWarnings("unused")
 	private WWorkspace wsBeforeChanges;
 	private IJavaElementDelta delta;
 	private WWorkspace wsAfterChanges;
 	private WsBuildPrimitives buildOld;
 	private WsBuildPrimitives buildNew;
 
 	private ModelComparer(WWorkspace wsBeforeChanges, IJavaElementDelta delta) {
 		this.wsBeforeChanges = wsBeforeChanges;
 		this.delta = delta;
 		this.buildOld = new WsBuildPrimitives(wsBeforeChanges);
 	}
 
 	public static void compareAndUpdateModel(WWorkspace wsBeforeChanges, IJavaElementDelta delta) {
 		new ModelComparer(wsBeforeChanges, delta).doUpdate();
 	}
 
 	private void doUpdate() {
 		wsAfterChanges = createNewModel();
 		buildNew = new WsBuildPrimitives(wsAfterChanges);
 		findAndApplyChanges(delta);
 	}
 
 	private void findAndApplyChanges(IJavaElementDelta delta2) {
		
 		// Leave out every delta which does not belong to the traced projects.
 		if (delta2.getElement().getJavaProject() != null
 				&& !Activator.getDefault().getWsService().isTracedProject(delta2.getElement().getJavaProject())) {
 			return;
 		}
		
		if (delta2.getElement().getJavaProject() != null)
			System.out.println(">>>" + delta2.getElement().getJavaProject().getElementName());
 
 		switch (delta2.getKind()) {
 		case IJavaElementDelta.ADDED:
 			addElement(delta2);
 			break;
 		case IJavaElementDelta.REMOVED:
 			removeElement(delta2);
 			break;
 		case IJavaElementDelta.CHANGED:
 			if (delta2.getElement() instanceof ICompilationUnit) {
 				refreshCompilationUnit((ICompilationUnit) delta2.getElement());
 			} else {
 				for (IJavaElementDelta child : delta2.getAffectedChildren()) {
 					findAndApplyChanges(child);
 				}
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void addElement(IJavaElementDelta delta) {
 		if (delta.getMovedFromElement() != null) {
 			buildOld.removeNamedElement(delta.getMovedFromElement());
 		}
 		IJavaElement newElem = delta.getElement();
 		WNamedElement emfOldParent = buildOld.findJdtElementInEmfModel(newElem.getParent());
 		WNamedElement emfNewObject = buildNew.findJdtElementInEmfModel(newElem);
 		buildOld.moveItemToNewParent(wsAfterChanges, emfOldParent, emfNewObject);
 		updateChildrenDependenciesRecurive(emfNewObject.getChildren());
 	}
 
 	private void removeElement(IJavaElementDelta delta) {
 		if (delta.getMovedToElement() == null) {
 			buildOld.removeNamedElement(delta.getElement());
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void updateChildrenDependenciesRecurive(List<WNamedElement> children) {
 		for (WNamedElement elem : children) {
 			// Insert deps
 			WsDeps deps = new WsDeps(buildOld);
 			if (elem instanceof WType) {
 				IType type = (IType) JavaCore.create(elem.getHandler());
 				deps.addTypeToSearch(type);
 				
 			}
 			else if (elem instanceof WMethod) {
 				IMethod method = (IMethod) JavaCore.create(elem.getHandler());
 				deps.addMethodToSearch(method);
 			}
 			
 			deps.execute();
 			updateChildrenDependenciesRecurive(elem.getChildren());
 		}
 	}
 
 	private void refreshCompilationUnit(ICompilationUnit cu) {
 		WNamedElement oldEmfCu = buildOld.findJdtElementInEmfModel(cu);
 		WNamedElement oldEmfParent = oldEmfCu.getParent();
 		WNamedElement newEmfCu = buildNew.findJdtElementInEmfModel(cu);
 		
 		
 		buildOld.removeNamedElementsRecursive(Arrays.asList(oldEmfCu));
 		buildOld.moveItemToNewParent(wsAfterChanges, oldEmfParent, newEmfCu);
 		updateChildrenDependenciesRecurive(Arrays.asList(newEmfCu));	
 	}
 
 	private WWorkspace createNewModel() {
 		try {
 			WWorkspace wsAfterChanges = WsmodelFactory.eINSTANCE.createWWorkspace();
 			WsBuildPrimitives buildPrimitives = new WsBuildPrimitives(wsAfterChanges);
 
 			Set<IJavaProject> projects = new HashSet<IJavaProject>();
 			for (IJavaElementDelta child : delta.getAffectedChildren()) {
				if (Activator.getDefault().getWsService().isTracedProject(delta.getElement().getJavaProject())) {
					projects.add(child.getElement().getJavaProject());	
				}
 			}
 
 			for (IJavaProject project : projects) {
 				WNamedElement emfProject = buildPrimitives.addNamedElement(null, project);
 
 				// Add all package fragment roots which are actual folders and not jar archives
 				for (IPackageFragmentRoot pfr : project.getPackageFragmentRoots()) {
 					if (!pfr.isArchive()) {
 						WNamedElement emfPfr = buildPrimitives.addNamedElement(emfProject, pfr);
 						for (IJavaElement pkgObject : pfr.getChildren()) {
 							IPackageFragment pkg = (IPackageFragment) pkgObject;
 							WNamedElement emfpf = buildPrimitives.addNamedElement(emfPfr, pkg);
 							for (ICompilationUnit cu : pkg.getCompilationUnits()) {
 								WNamedElement emfCu = buildPrimitives.addNamedElement(emfpf, cu);
 								for (IType t : cu.getTypes()) {
 									addType(emfCu, t, buildPrimitives);
 								}
 							}
 						}
 					}
 				}
 			}
 
 			return wsAfterChanges;
 		} catch (JavaModelException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void addType(WNamedElement emfCu, IType t, WsBuildPrimitives buildPrimitives) throws JavaModelException {
 		WNamedElement emfType = buildPrimitives.addNamedElement(emfCu, t);
 		for (IType innerType : t.getTypes()) {
 			addType(emfCu, innerType, buildPrimitives);
 		}
 
 		for (IMethod m : t.getMethods()) {
 			buildPrimitives.addNamedElement(emfType, m);
 		}
 
 		for (IField f : t.getFields()) {
 			buildPrimitives.addNamedElement(emfType, f);
 		}
 	}
 
 	
 }
