 package net.kbserve.pyjdt;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.junit.Assert;
 import org.python.pydev.core.IPythonPathNature;
 import org.python.pydev.plugin.nature.PythonNature;
 
 public class TestUtilities {
 
 	protected static final Map<String, IProject> projects = new HashMap<String, IProject>();
 
 	public static IJavaProject createJDTProject(String projectName)
 			throws CoreException {
 		return createJDTProject(projectName, "src");
 	}
 
 	public static IJavaProject createJDTProject(String projectName,
 			String sourceDir) throws CoreException {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot()
 				.getProject(projectName);
 		project.create(null);
 		project.open(null);

 		IJavaProject jdtProject = JavaCore.create(project);
 		IFolder binFolder = project.getFolder("bin");
 		binFolder.create(false, true, null);
		jdtProject.setOutputLocation(binFolder.getFullPath(), null);
 		if (sourceDir != null) {
 			addSourceFolder(project, sourceDir);
 		}
		Assert.assertNotNull("Project not found", ResourcesPlugin
				.getWorkspace().getRoot().findMember(project.getLocation()));
 
 		projects.put(projectName, project);
 
 		System.out.println("Created project: " + projectName + " ("
 				+ project.getLocation() + ")");
 		return jdtProject;
 	}
 
 	public static void addLibraryContainer(IPath container, IJavaProject project)
 			throws JavaModelException {
 		// IClasspathEntry containerEntry = JavaCore.newContainerEntry(container);
 		IJavaProject[] javaProjects = { project };
 		IClasspathContainer[] containers = { null };
 		JavaCore.setClasspathContainer(container, javaProjects, containers,
 				null);
 		for (IClasspathEntry ice : project.getRawClasspath()) {
 			if (ice.getPath().equals(container)) {
 				return;
 			}
 		}
 		throw new AssertionFailedError("The container " + container
 				+ " was not added to the project "
 				+ project.getProject().getName());
 	}
 
 	public static void addPyDevNature(IProject project, String sourceDir)
 			throws CoreException {
 		if (sourceDir != null) {
 			PythonNature.addNature(project, null,
 					PythonNature.JYTHON_VERSION_2_5,
 					project.findMember(sourceDir).getLocation()
 							.toPortableString(), null, null, null);
 		} else {
 			PythonNature.addNature(project, null,
 					PythonNature.JYTHON_VERSION_2_5,
 					project.findMember(sourceDir).getLocation()
 							.toPortableString(), null, null, null);
 		}
 	}
 
 	public static void addSourceFolder(IProject project, String sourceDir)
 			throws CoreException {
 		IFolder src = project.getFolder(sourceDir);
 		if (!src.exists()) {
 			src.create(false, true, null);
 		}
 		if (project.hasNature(JavaCore.NATURE_ID)) {
 			IJavaProject javaProject = JavaCore.create(project);
 			IPackageFragmentRoot sourceFragment = javaProject
 					.getPackageFragmentRoot(src);
 			IClasspathEntry newSourceEntry = JavaCore
 					.newSourceEntry(sourceFragment.getPath());
 
 			IClasspathEntry[] origionalEntries = javaProject.getRawClasspath();
 
 			boolean duplicated = false;
 			for (IClasspathEntry ce : origionalEntries) {
 				if (ce.getPath().equals(newSourceEntry)) {
 					duplicated = true;
 					break;
 				}
 			}
 			if (!duplicated) {
 				IClasspathEntry[] newEntries = Arrays.copyOf(origionalEntries,
 						origionalEntries.length + 1);
				newEntries[newEntries.length] = newSourceEntry;
 				javaProject.setRawClasspath(newEntries, null);
 			}
 		}
 		if (project.hasNature(PythonNature.PYTHON_NATURE_ID)) {
 			IPythonPathNature pythonPathNature = PythonNature.getPythonNature(
 					project).getPythonPathNature();
 			String pyPath = pythonPathNature.getProjectSourcePath(false);
 			String localString = src.getLocation().toPortableString();
 			if (pyPath.toString().matches(".*\\b" + localString + "\\b.*")) {
 				if (pyPath.length() > 0) {
 					pyPath += '|';
 				}
 				pyPath += localString;
 				System.out.println("New PyDev path for project "
 						+ project.getName() + " is '" + pyPath + "'");
 				pythonPathNature.setProjectSourcePath(pyPath);
 			} else {
 				System.out.println("The PyDev path already contains "
 						+ localString + " for project " + project.getName());
 			}
 		}
 
 	}
 
 	public static void deleteAllTestProjects() throws CoreException {
 		for (IProject project : projects.values()) {
 			project.delete(true, null);
 			System.out.println("Deleted project named: " + project.getName());
 		}
 	}
 
 }
