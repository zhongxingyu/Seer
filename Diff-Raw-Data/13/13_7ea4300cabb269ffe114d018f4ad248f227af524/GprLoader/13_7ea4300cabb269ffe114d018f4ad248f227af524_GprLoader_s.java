 package org.padacore.core.gnat;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.antlr.runtime.ANTLRFileStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.padacore.core.utils.ErrorLog;
 
 public class GprLoader {
 
 	private static String GPR_EXTENSION = "gpr";
 
 	private Stack<Project> projectsToLoad;
 	private List<Project> loadedProjects;
 
 	public GprLoader() {
 		this.projectsToLoad = new Stack<Project>();
 		this.loadedProjects = new ArrayList<Project>();
 	}
 
 	/**
 	 * Add a variable to the current project.
 	 * 
 	 * @param name
 	 *            Name of the variable.
 	 * @param value
 	 *            Value of the variable.
 	 */
 	public void addVariable(String name, Symbol value) {
 		this.getCurrentProject().addVariable(name, value);
 	}
 
 	/**
 	 * Add an attribute to the current project.
 	 * 
 	 * @param name
 	 *            Name of the attribute.
 	 * @param value
 	 *            Value of the attribute.
 	 */
 	public void addAttribute(String name, Symbol value) {
 		this.getCurrentProject().addAttribute(name, value);
 	}
 
 	/**
 	 * Return whether the variable is defined in the current project.
 	 * 
 	 * @param name
 	 *            Name of the variable.
 	 * @return True is returned if variable is defined in the current project.
 	 *         False otherwise.
 	 */
 	public boolean variableIsDefined(String name) {
 		return this.getCurrentProject().variableIsDefined(name);
 	}
 
 	/**
 	 * Return variable symbol corresponding to given name in the current
 	 * project.
 	 * 
 	 * @param name
 	 *            Qualified name of the variable.
 	 * @return Symbol associated to given name.
 	 */
 	public Symbol getVariable(String name) {
 		return this.getCurrentProject().getVariable(name);
 	}
 
 	/**
 	 * Return attribute symbol corresponding to given name in the current
 	 * project.
 	 * 
 	 * @param name
 	 *            Qualified name of the attribute.
 	 * @return Symbol associated to given name.
 	 */
 	public Symbol getAttribute(String name) {
 		return this.getCurrentProject().getAttribute(name);
 	}
 
 	/**
 	 * Run the parser on Gpr file located at given path.
 	 * 
 	 * @param path
 	 *            Path to the Gpr file.
 	 */
 	private void parseGpr(IPath path) {
 		try {
 			GprLexer lexer = new GprLexer(new ANTLRFileStream(path.toOSString()));
 			GprParser parser = new GprParser(this, new CommonTokenStream(lexer));
 			parser.project();
 		} catch (IOException e) {
 			ErrorLog.appendMessage("Error while opening GPR file " + path.toOSString(),
 					IStatus.ERROR);
 		} catch (RecognitionException e) {
 			ErrorLog.appendMessage("GPR file " + path.toOSString() + " format is incorrect",
 					IStatus.ERROR);
 		}
 	}
 
 	/**
 	 * Evaluate the path of a Gpr file according to its relativeProjectPath.
 	 * Evaluation is based on the path of the project in progress.
 	 * 
 	 * @param relativeProjectPath
 	 * @return
 	 */
 	private IPath evaluatePath(String relativeProjectPath) {
 		IPath referencePath = this.getCurrentProject().getPath().removeLastSegments(1);
 		IPath path = referencePath.append(relativeProjectPath);
 		String extension = path.getFileExtension();
 
 		if (extension == null) {
 			path = path.addFileExtension(GPR_EXTENSION);
 		}
 		return path;
 	}
 
 	/**
 	 * Load the project located at pathToGpr and add it the loaded project list.
 	 * 
 	 * @param pathToGpr
 	 *            Path to a Gpr file.
 	 */
 	public void load(IPath pathToGpr) {
 		Project projectToAdd = new Project(pathToGpr);
 		this.projectsToLoad.push(projectToAdd);
 		this.parseGpr(this.getCurrentProject().getPath());
 		this.projectsToLoad.pop();
 		this.loadedProjects.add(0, projectToAdd);
 		if (!this.projectsToLoad.isEmpty()) {
 			this.getCurrentProject().addReferenceProject(projectToAdd);
 		}
 	}
 
 	/**
 	 * Add a project to current project. If the project is already loaded, the
 	 * parsing is not performed.
 	 * 
 	 * @param relativeProjectPath
 	 *            Relative path to the Gpr file of the project to add.
 	 */
 	public void addProject(String relativeProjectPath) {
 		IPath projectToAddPath = evaluatePath(relativeProjectPath);
 
 		if (this.projectIsAlreadyLoaded(projectToAddPath)) {
 			Project projectToAdd = this.getProject(projectToAddPath);
 			this.getCurrentProject().addReferenceProject(projectToAdd);
 		} else {
 			this.load(projectToAddPath);
 		}
 	}
 
 	/**
 	 * 
 	 * @return List of the loaded projects.
 	 */
 	public List<Project> getLoadedProjects() {
 		return this.loadedProjects;
 	}
 
 	/**
 	 * Notify current project that a begin package has been found.
 	 * 
 	 * @param packageName
 	 *            Name of the package.
 	 */
 	public void beginPackage(String packageName) {
 		this.getCurrentProject().beginPackage(packageName);
 	}
 
 	/**
 	 * Notify current project that an end package has been found.
 	 */
 	public void endPackage() {
 		this.getCurrentProject().endPackage();
 	}
 
 	/**
 	 * Add a new package to the current project based on another package.
	 * @param newPackageName Name of the package to be added.
	 * @param projectName Name of the project containing the package to copy.
	 * @param packageName Name of the package to copy.
 	 */
 	public void addPackageFrom(String newPackageName, String projectName, String packageName) {
 		this.getCurrentProject().addPackageFrom(newPackageName, projectName, packageName);
 	}
 
 	/**
 	 * @return The project in progress.
 	 */
 	public Project getCurrentProject() {
 		return this.projectsToLoad.peek();
 	}
 
 	/**
 	 * Return the project associated to given path in the list of the loaded
 	 * project.
 	 * 
 	 * @param pathToGpr
 	 *            Path to Gpr file.
 	 * @return Project associated to given path in the list of the loaded
 	 *         project or null if not found.
 	 */
 	private Project getProject(IPath pathToGpr) {
 		Project project = null;
 		for (Project loadedProject : this.loadedProjects) {
			if (loadedProject.getPath().equals(pathToGpr)) {
 				project = loadedProject;
 				break;
 			}
 		}
 		return project;
 	}
 
 	/**
 	 * @param pathToGpr
 	 *            Path to Gpr project file.
 	 * @return True if project located at given path has already been loaded.
 	 *         False otherwise.
 	 */
 	private boolean projectIsAlreadyLoaded(IPath pathToGpr) {
 		return getProject(pathToGpr) != null;
 	}
 }
