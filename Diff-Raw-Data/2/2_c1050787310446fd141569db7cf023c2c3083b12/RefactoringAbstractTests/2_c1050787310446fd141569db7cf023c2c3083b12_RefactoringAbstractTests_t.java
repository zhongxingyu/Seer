 package org.ita.neutrino.tests;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.junit.After;
 import org.junit.Before;
 
 /**
  * Super classe especializada em testes de refatorações. Fornece um ambiente
  * previamente preparado através dos métodos <code>getPackage()</code> e
  * <code>getProject()</code>.
  * 
  */
 public class RefactoringAbstractTests {
 
 	private Map<String, IPackageFragment> knownPackages = new HashMap<String, IPackageFragment>();
 	private IJavaProject javaProject;
 	private IProject project;
 	private boolean deleteTestProject;
 
 	protected IPackageFragment getPackageByName(String packageName)
 			throws JavaModelException {
 		if (knownPackages.containsKey(packageName)) {
 			return knownPackages.get(packageName);
 		} else {
 			// Se não encontrou o pacote solicitado, cria um novo com o mesmo
 			// nome
 			IPackageFragmentRoot root = javaProject
 					.getPackageFragmentRoot(project);
 			IPackageFragment newPackage = root.createPackageFragment(
 					packageName, false, null);
 
 			knownPackages.put(packageName, newPackage);
 
 			return newPackage;
 		}
 
 	}
 
 	protected IProject getProject() {
 		return project;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Before
 	public void prepareEnvironment() throws CoreException {
 		// Cria um novo projeto
 		project = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
 		project.create(null);
 		project.open(null);
 
 		// set the Java nature and Java build path
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
 		project.setDescription(description, null);
 
 		javaProject = JavaCore.create(project);
 
 		// Cria as entradas no classpath....
 		IClasspathEntry[] cpentry = new IClasspathEntry[] {
 				JavaCore.newSourceEntry(javaProject.getPath()),
 				JavaRuntime.getDefaultJREContainerEntry(),
 				JavaCore.newLibraryEntry(
 						new Path(
 								"/home/programs/eclipse/plugins/org.junit_4.8.1.v4_8_1_v20100427-1100/junit.jar"),
 						null, null) };
 		javaProject.setRawClasspath(cpentry, javaProject.getPath(), null);
 		Map options = new HashMap();
 		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR,
 				JavaCore.SPACE);
 		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
 		javaProject.setOptions(options);
 
 		// Apaga o projeto de testes após rodar cada teste.
 		setDeleteTestProject(true);
 	}
 
 	@After
 	public void releaseEnvironment() throws CoreException {
		if (deleteTestProject) {
 			project.delete(false, null);
 		}
 	}
 
 	protected void setDeleteTestProject(boolean value) {
 		deleteTestProject = value;
 	}
 
 	protected ICompilationUnit createSourceFile(String packageName,
 			String fileName, StringBuilder source) throws JavaModelException {
 		ICompilationUnit compilationUnit = getPackageByName(packageName)
 				.createCompilationUnit(fileName, source.toString(), false, null);
 		return compilationUnit;
 	}
 
 }
