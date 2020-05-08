 package descent.internal.unittest.launcher;
 
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import descent.core.ICompilationUnit;
 import descent.core.IInitializer;
 import descent.core.IJavaElement;
 import descent.core.IJavaProject;
 import descent.core.IPackageFragment;
 import descent.core.IPackageFragmentRoot;
 import descent.core.IParent;
 import descent.core.IType;
 import descent.core.JavaModelException;
 
 import descent.internal.unittest.util.TestSearchEngine;
 
 public class DUnittestFinder implements ITestFinder
 {
 	private static DUnittestFinder singleton = null;
 	
 	public static DUnittestFinder getInstance()
 	{
 		if(null == singleton)
 			singleton = new DUnittestFinder();
 		return singleton;
 	}
 	
 	public void findTestsInContainer(Object[] elements,
 			Set<ICompilationUnit> result, IProgressMonitor pm)
 	{
 		try {
 			for (int i= 0; i < elements.length; i++) {
 				Object container= TestSearchEngine.computeScope(elements[i]);
 				if (container instanceof IJavaProject) {
 					IJavaProject project= (IJavaProject) container;
 					findTestsInProject(project, result);
 				} else if (container instanceof IPackageFragmentRoot) {
 					IPackageFragmentRoot root= (IPackageFragmentRoot) container;
 					findTestsInPackageFragmentRoot(root, result);
 				} else if (container instanceof IPackageFragment) {
 					IPackageFragment fragment= (IPackageFragment) container;
 					findTestsInPackageFragment(fragment, result);
 				} else if (container instanceof ICompilationUnit) {
 					ICompilationUnit module= (ICompilationUnit) container;
 					findTestsInModule(module, result);
 				}
 			}			
 		} catch (JavaModelException e) {
 			// do nothing
 		}
 	}
 	
 	private void findTestsInProject(IJavaProject project,
 			Set<ICompilationUnit> result) throws JavaModelException {
 		IPackageFragmentRoot[] roots= project.getPackageFragmentRoots();
 		for (int i= 0; i < roots.length; i++) {
 			IPackageFragmentRoot root= roots[i];
 			findTestsInPackageFragmentRoot(root, result);
 		}
 	}
 
 	private void findTestsInPackageFragmentRoot(IPackageFragmentRoot root, 
 			Set<ICompilationUnit> result) throws JavaModelException {
 		IJavaElement[] children= root.getChildren();
 		for (int j= 0; j < children.length; j++) {
 			IPackageFragment fragment= (IPackageFragment) children[j];
 			findTestsInPackageFragment(fragment, result);
 		}
 	}
 
 	private void findTestsInPackageFragment(IPackageFragment fragment,
 			Set<ICompilationUnit> result) throws JavaModelException {
 		ICompilationUnit[] compilationUnits= fragment.getCompilationUnits();
 		for (int k= 0; k < compilationUnits.length; k++) {
 			ICompilationUnit module = compilationUnits[k];
 			findTestsInModule(module, result);
 		}
 	}
 	
 	private void findTestsInModule(ICompilationUnit module,
 			Set<ICompilationUnit> result) throws JavaModelException
 	{
 		if(hasTests(module))
 			result.add(module);
 	}
 	
 	public boolean hasTests(ICompilationUnit module) throws JavaModelException
 	{
 		return testSearch(module);
 	}
 	
 	public boolean testSearch(IParent element) throws JavaModelException
 	{
 		for(IJavaElement child : element.getChildren())
 		{
 			if(child instanceof IType)
 			{
 				if(testSearch((IType) child))
 					return true;
 			}
 			
 			if(child instanceof IInitializer)
 				if(((IInitializer) child).isUnitTest())
 					return true;
 		}
 		return false;
 	}
 }
