 package cern.devtools.depanalysis.modelfinder;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 
 import cern.devtools.depanalysis.wsmodel.EclipseWorkspace;
 import cern.devtools.depanalysis.wsmodel.NamedElement;
 
 public class WsStructure {
 
 	public static void buildEntireProject(EclipseWorkspace workspace, List<IJavaProject> projects) {
 		try {
 
 			WsBuildPrimitives buildPrimitives = new WsBuildPrimitives(workspace);
 
 			for (IJavaProject project : projects) {
 				NamedElement emfProject = buildPrimitives.addNamedElement(null, project);
 
 				// Add all package fragment roots which are actual folders and not jar archives
 				for (IPackageFragmentRoot pfr : project.getPackageFragmentRoots()) {
 					if (!pfr.isArchive()) {
 						NamedElement emfPfr = buildPrimitives.addNamedElement(emfProject, pfr);
 						for (IJavaElement pkgObject : pfr.getChildren()) {
 							IPackageFragment pkg = (IPackageFragment) pkgObject;
 							NamedElement emfpf = buildPrimitives.addNamedElement(emfPfr, pkg);
 							for (ICompilationUnit cu : pkg.getCompilationUnits()) {
 								NamedElement emfCu = buildPrimitives.addNamedElement(emfpf, cu);
 								for (IType t : cu.getTypes()) {
 									addType(buildPrimitives, emfCu, t);
 								}
 							}
 						}
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 
 		WsBuildPrimitives prim = new WsBuildPrimitives(workspace);
 		try {
 			for (IJavaElement elem : JavaModelWalker.allElements(projects)) {
				if (elem instanceof IType) {
 					WsDeps.searchAndInsertOutgoingDependencies(elem, prim);
 				}
 			}
 
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void removeEntireProject(EclipseWorkspace workspace, List<IJavaProject> projects) {
 		WsBuildPrimitives buildPrimitives = new WsBuildPrimitives(workspace);
 		for (IJavaProject project : projects) {
 			buildPrimitives.removeEntireProject(project);
 		}
 	}
 	
 	private static void addType(WsBuildPrimitives buildPrimitives, NamedElement emfCu, IType t) throws JavaModelException {
 		NamedElement emfType = buildPrimitives.addNamedElement(emfCu, t);
 		for (IType innerType : t.getTypes()) {
 			addType(buildPrimitives, emfCu, innerType);
 		}
 		
 		for(IMethod m : t.getMethods()) {
 			buildPrimitives.addNamedElement(emfType, m);
 		}
 		
 		for(IField f : t.getFields()) {
 			buildPrimitives.addNamedElement(emfType, f);
 		}
 	}
 }
