 package eclipse.actions;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IBuffer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 
 public class TestCounterpartCreator {
 	private int caretPosition=-1;
 
 	public IResource createOrRetrieve(IJavaElement baseElem)  {
 		if (baseElem == null) {
 			throw new NullElementNotAllowedException();
 		}
 
 		IPackageFragmentRoot newClassRoot = getNewClassRootBasedOnSelectedElement(baseElem);
 		IPackageFragment unitPackage = getNewUnitPackage(baseElem, newClassRoot);
 
 		String newUnitNameFileName = makeNewUnitName(baseElem);
 		ICompilationUnit compilationUnit = unitPackage.getCompilationUnit(newUnitNameFileName);
 		boolean testAlreadyExists = compilationUnit.getResource().exists();
 		if (testAlreadyExists)
 			return compilationUnit.getResource();
 		
 		ICompilationUnit createdClass = createTestClass(unitPackage, newUnitNameFileName);
 		return createdClass.getResource();
 	}
 
 	public int getCaretPositionForNewFile() {
 		return caretPosition;
 	}
 	private ICompilationUnit createTestClass(
 			IPackageFragment unitPackage,
 			String unitFileName) {
		ICompilationUnit parentUnit = null;
 		try {
 			IProgressMonitor monitor = getMonitor();
			parentUnit = unitPackage.createCompilationUnit(unitFileName, "", false, monitor);
 	
 			parentUnit.becomeWorkingCopy(monitor);
 			
 			String typeName = unitFileName.replace(".java", "");
 			IBuffer buffer = parentUnit.getBuffer();
 			String unitContent = getUnitContent(unitPackage, typeName);
 			buffer.setContents(unitContent);
 			IType createdType = parentUnit.getType(typeName);
 			ICompilationUnit createdTestUnit = createdType.getCompilationUnit();
 			createdTestUnit.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
 			
 			this.caretPosition = unitContent.length()-2;
 			return createdTestUnit;
 		}catch(JavaModelException e) {
 			throw new UnhandledJavaModelException(e);
 		}
		finally {
			discardWorkingCopyOrCry(parentUnit);
		}
	}

	private void discardWorkingCopyOrCry(ICompilationUnit parentUnit) {
		if (parentUnit != null) {
			try {
				parentUnit.discardWorkingCopy();
			} catch (JavaModelException e) {
				throw new UnhandledJavaModelException(e);
			}
		}
 	}
 	
 	private String getUnitContent(IPackageFragment unitPackage, String typeName) {
 		return String.format(
 		"package %s;\n"+
 		"\n" +
 		"public class %s { \n" +
 		"\t\n"+
 		"}", 
 		unitPackage.getElementName(), typeName);
 	}
 
 	private String makeNewUnitName(IJavaElement elem) {
 		String elementName = elem.getElementName();
 		if (elementName.endsWith("Test.java")) {
 			return elementName.replaceFirst("Test.java", ".java");
 		}
 		return elementName.replaceFirst(".java", "Test.java");
 	}
 
 	private IPackageFragment getNewUnitPackage(
 			IJavaElement elem,
 			IPackageFragmentRoot newUnitRoot)
 	{
 		try {
 			IProgressMonitor monitor = getMonitor();
 			IPackageFragment originalPkg = (IPackageFragment) elem.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
 			
 			boolean force = true;
 			IPackageFragment newUnitPackage = newUnitRoot.createPackageFragment(
 					originalPkg.getElementName(), 
 					force, 
 					monitor);
 			
 			return newUnitPackage;
 		}catch(JavaModelException e) {
 			throw new UnhandledJavaModelException(e);
 		}
 	}
 
 	private IPackageFragmentRoot getNewClassRootBasedOnSelectedElement(IJavaElement elem){
 		IJavaProject project = elem.getJavaProject();
 		
 		String counterpartFolder = getCounterpartFolder(elem);
 		IFolder folder = project.getProject().getFolder(counterpartFolder);
 		if (!folder.exists())
 			return createMissingFolder(project, counterpartFolder);
 		return project.getPackageFragmentRoot(folder);
 	}
 	
 	private String getCounterpartFolder(IJavaElement elem) {
 		String selecteElemPath = elem.getResource().getFullPath().toFile().getAbsolutePath();
 		String target = "test";
 		if (selecteElemPath.contains("/test/java")) {
 			target = "main";
 		}
 		
 		String counterpartFolder = "/src/"+target+"/java";
 		return counterpartFolder;
 	}
 	
 	private IPackageFragmentRoot createMissingFolder(IJavaProject project, String missingFolderPath) {
 		IFolder folder = createFolderAndParents(project, missingFolderPath);
 		addFolderToClasspath(project, missingFolderPath);
 		
 		return project.getPackageFragmentRoot(folder);
 	}
 
 	private IFolder createFolderAndParents(IJavaProject project, String missingFolderPath) {
 		String[] parts = missingFolderPath.replaceFirst("^/","").split("/");
 		String pathToCreate="";
 		for (String folderPart : parts) {
 			pathToCreate +="/" + folderPart;
 			createFolder(project, pathToCreate);
 		}
 		
 		return project.getProject().getFolder(missingFolderPath);
 	}
 
 	private IFolder createFolder(IJavaProject project, String pathToCreate) {
 		IFolder folder = project.getProject().getFolder(pathToCreate);
 		if (!folder.exists()) {
 			try {
 				folder.create(true, true, null);
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 		return folder;
 	}
 	
 	private void addFolderToClasspath(
 			IJavaProject project,
 			String missingFolder) 
 	{
 		try {
 			IPath missingFolderPath = project.getProject().getFullPath().append(missingFolder);
 			IClasspathEntry newSourceEntry = JavaCore.newSourceEntry(missingFolderPath);
 			
 			IClasspathEntry[] classpathEntries = addNewEntryToClasspath(project, newSourceEntry);
 			project.setRawClasspath(classpathEntries, getMonitor());
 		}catch(JavaModelException e) {
 			throw new UnhandledJavaModelException(e);
 		}
 	}
 
 	private IClasspathEntry[] addNewEntryToClasspath(IJavaProject project,
 			IClasspathEntry newSourceEntry) throws JavaModelException {
 		IClasspathEntry[] rawClasspath = project.getRawClasspath();
 		List<IClasspathEntry> unmodifiableList = Arrays.asList(rawClasspath);
 		List<IClasspathEntry> linked = new LinkedList<IClasspathEntry>(unmodifiableList);
 		linked.add(newSourceEntry);
 		IClasspathEntry[] classpathEntries = linked.toArray(new IClasspathEntry[0]);
 		return classpathEntries;
 	}
 	
 	private IProgressMonitor getMonitor() {
 		return new NullProgressMonitor();
 	}
 	
 	@SuppressWarnings("serial")
 	static class NullElementNotAllowedException extends RuntimeException {
 		public NullElementNotAllowedException(){
 			super("Can't create counterpart for null java element");
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	static class UnhandledJavaModelException extends RuntimeException {
 		public UnhandledJavaModelException(Throwable e){
 			super(e);
 		}
 	}
 }
