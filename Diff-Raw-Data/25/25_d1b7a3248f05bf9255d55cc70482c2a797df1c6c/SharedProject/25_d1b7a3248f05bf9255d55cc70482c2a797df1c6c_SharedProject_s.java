 package org.vaadin.mideaas.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 
 import org.apache.commons.io.FileUtils;
 import org.vaadin.chatbox.SharedChat;
 import org.vaadin.mideaas.frontend.PomXml;
 import org.vaadin.mideaas.frontend.PomXml.Dependency;
 import org.vaadin.mideaas.java.util.CompilingService;
 import org.xml.sax.SAXException;
 
 import com.vaadin.ui.Notification;
 
 /**
  * A MIDEaaS project.
  * 
  * Project contains mainly {@link ProjectFile}s and {@link SharedView}s.
  * 
  * It also keeps track of the {@link User}s in the project.
  *
  * Thread-safe. (should be)
  */
 public class SharedProject {
 
 	/**
 	 * For notifying on project structure changes. (File add/remove etc.)
 	 */
 	public interface ProjectListener {
 		// TODO: maybe multiple methods, not just one all-purpose "changed"
 		public void changed();
 	}
 
 	/**
 	 * For notifying when the project classpath changes.
 	 *
 	 */
 	public interface ClasspathListener {
 		public void classpathChanged();
 	}
 	
 	// CopyOnWriteArrayList requires no synchronization.
 	// We may miss a listener call at some point put not a big deal.
 	private final CopyOnWriteArrayList<ProjectListener> listeners = new CopyOnWriteArrayList<ProjectListener>();
 	private final CopyOnWriteArrayList<ClasspathListener> cpListeners = new CopyOnWriteArrayList<ClasspathListener>();
 
 
 	private static/* XXX static or not? */final ExecutorService pool = Executors
 			.newSingleThreadExecutor();
 
 	/**
 	 * The dir where all the projects are saved.
 	 */
 	private static File projectRootDir;
 	
 	/**
 	 * Projects package is projectRootPackage+"."+projectName
 	 */
 	private static String projectRootPackage;
 
 	/*
 	 * All the projects.
 	 * Must use synchronized(projects) when accessing.
 	 */
 	private static TreeMap<String,SharedProject> projects = new TreeMap<String,SharedProject>();
 
 	private static Set<String> projectNames;
 	
 	/** The users currently editing this project. */
 	private final TreeSet<User> users = new TreeSet<User>();
 
 	/** The name of this project. */
 	private final String name;
 
 	/** The java package where the source files are. */
 	private final String packageName;
 
 	/** Where this project is saved. */
 	private final File projectDir;
 
 	// Project consists of mostly two things: views and files.
 	private TreeMap<String, SharedView> views = new TreeMap<String, SharedView>();
 	private TreeMap<String, ProjectFile> files = new TreeMap<String, ProjectFile>();
 
 	private PomXml pomXml;
 
 	private String classPath;
 
 	/** Project-wide chat. */
 	private SharedChat chat = new SharedChat();
 
 	private CompilingService compiler;
 	
 	private final ProjectLog log;
 
 	/**
 	 * Sets the static project properties such as root dir and package.
 	 * This must be called before doing anything with SharedProjects.
 	 * 
 	 * @throws IOException
 	 */
 	// TODO: how should we synchronize the static fields?
 	public static void initializeProjectRoot(File projectRootDir,
 			String projectRootPackage) throws IOException {
 		SharedProject.projectRootDir = projectRootDir;
 		SharedProject.projectRootPackage = projectRootPackage;
 		
 		// TODO: it's not a very good idea to read all the projects
 		// to memory right away. because we're most likely gonna need
 		// many of them...
 		loadProjectlistFromFiles(projectRootDir);
 	}
 
 	private static String getProjectPackageFor(String projectName) {
 		return projectRootPackage + "." + projectPackageName(projectName);
 	}
 	
 	private static String projectPackageName(String projectName) {
 		return projectName.toLowerCase().replaceAll("[^a-z0-9]", "");
 	}
 	
 	static private File createNewProjectDir(String projectName)
 			throws IOException {
 		File dir = new File(projectRootDir, projectName);
 		dir.mkdirs();
 		return dir;
 	}
 
 	/**
 	 * Creates a new project without any files.
 	 */
 	public static SharedProject createEmptyProject(String name) {
 		SharedProject s;
 		File projectDir;
 		synchronized (projects) {
 			if (SharedProject.getProjectNames().contains(name)) {
 				return null;
 			}
 			try {
 				projectDir = createNewProjectDir(name);
 				s = new SharedProject(name, projectDir);
 			} catch (IOException e) {
 				e.printStackTrace();
 				removeProject(name);
 				return null;
 			}
 			putProject(s);
 		}
 		return s;
 	}
 
 	/**
 	 * Creates a new project with default initial files.
 	 */
 	public static SharedProject createNewProject(String name, UserSettings settings) {
 
 		SharedProject s = createEmptyProject(name);
 		if (s == null) {
 			return null;
 		}
 
 		try {
 			s.log.logCreated();
 			ProjectFileUtils.writeInitialFilesToDisk(s.getProjectDir(),
 					s.getPackageName(), settings);
 			s.pomXml = new PomXml(ProjectFileUtils.generatePomXml(s
 					.getPackageName(),settings));
 			s.writePomXml();
 			s.addUiClass();
 			s.addHelloWorldSkeleton();
 			s.writeToDisk();
 			s.compileAll();
 			
 		} catch (IOException | ParserConfigurationException | SAXException
 				| TransformerFactoryConfigurationError | TransformerException e) {
 			e.printStackTrace();
 			Notification.show("Error: failed creating project: " + name,
 					Notification.Type.ERROR_MESSAGE);
 			removeProject(name);
 			return null;
 		}
 
 		return s;
 	}
 	
 	private void addUiClass() {
 		String code = ProjectFileUtils.generateApp(getPackageName());
 		ProjectFile pf = ProjectFile.newJavaFile("App.java", code, getSourceFileLocation("App.java"), getLog());
 		addFile(pf, null);
 	}
 
 	/**
 	 * Compiles all the files of this project.
 	 * Normally a file is compiled after it's changed but this
 	 * may be needed in some situations. (?)
 	 */
 	public synchronized void compileAll() {
 		getCompiler().compileAll(getJavaClasses());
 
 	}
 	
 	/**
 	 * @return map<java class name, the class java code>
 	 */
 	private Map<String, String> getJavaClasses() {
 		Map<String,String> classes = new HashMap<String,String>();
 		for (Entry<String, ProjectFile> e : files.entrySet()) {
 			if (e.getKey().endsWith(".java")) {
 				String cls = fullJavaClassNameFromFilename(e.getKey());
 				String content = e.getValue().getMud().getBase().getText();
 				classes.put(cls, content);
 			}
 		}
 		for (Entry<String, SharedView> e : views.entrySet()) {
 			String cls = e.getValue().getControllerFullName();
 			String content = e.getValue().getControllerMUD().getBase().getText(); // XXX
 			classes.put(cls, content);
 		}
 		
 		// Assuming every project has this MideaasComponent class...
 		// TODO: this is a bit of a hack...
 		String cls = "org.vaadin.mideaas.MideaasComponent";
 		try {
 			String content = ProjectFileUtils.generateMideaasComponent();
 			classes.put(cls, content);
 		} catch (IOException e) {
 			System.err.println("WARNING: could not compile MideaasComponent");
 			e.printStackTrace();
 		}
 		
 		return classes;
 	}
 	
 	private String fullJavaClassNameFromFilename(String name) {
 		return getPackageName()+"."+name.substring(0, name.length() - ".java".length());
 	}
 
 	/**
 	 * Removes the project and directory.
 	 * 
 	 * @param projectName
 	 *            the name of the project to be destroyer
 	 * @return true, if successful
 	 */
 	public static boolean removeProject(String projectName) {
 		SharedProject project = SharedProject.getProject(projectName);
 		return removeProject(project);
 	}
 
 	/**
 	 * Removes the project and directory.
 	 * 
 	 * @param project
 	 *            to be destroyer
 	 * @return true, if successful
 	 */
 	private static boolean removeProject(SharedProject project) {
 		if (project != null) {
 			project.destroy();
 			synchronized (projects) {
 				projects.remove(project.getName());
 				projectNames.remove(project.getName());
 			}
 			project.log.logRemoved();
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	private void destroy() {
 		removeAllUsers();
 		try {
 			FileUtils.deleteDirectory(getProjectDir());
 		} catch (IOException e) {
 			// TODO what?
 			e.printStackTrace();
 		}
 	}
 
 	private void addHelloWorldSkeleton() {
 		createView(ProjectFileUtils.getFirstViewName(), null);
 	}
 
 	
 
 	/**
 	 * Instantiates a new shared project.
 	 * 
 	 * Private because
 	 * {@link #createEmptyProject(String)} or
 	 * {@link #createNewProject(String)} 
 	 * should be used instead.
 	 * 
 	 * @param projectName
 	 *            the project name
 	 * @param projectDir
 	 *            the project dir
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	private SharedProject(String projectName, File projectDir)
 			throws IOException {
 		this.name = projectName;
 		this.projectDir = projectDir;
 		this.packageName = getProjectPackageFor(projectName);
 		log = new ProjectLog(projectName);
 		chat.addLine("Project " + projectName + " started.");
 	}
 
 	/**
 	 * @return the java classpath.
 	 */
 	public synchronized String getClassPath() {
 		if (classPath == null) {
 			classPath = ProjectFileUtils.getClassPath(projectDir);
 		}
 		return classPath;
 	}
 
 	public synchronized void addListener(ProjectListener li) {
 		listeners.add(li);
 	}
 
 	public synchronized void removeListener(ProjectListener li) {
 		listeners.remove(li);
 	}
 
 	public synchronized void addClasspathListener(ClasspathListener li) {
 		cpListeners.add(li);
 	}
 
 	public synchronized void removeClasspathListener(ClasspathListener li) {
 		cpListeners.remove(li);
 	}
 
 	private void fireChanged() {
 		pool.submit(new Runnable() {
 			@Override
 			public void run() {
 				for (ProjectListener listener : listeners) {
 					listener.changed();
 				}
 			}
 		});
 	}
 
 	private void fireClasspathChanged() {
 		classPath = null;
 		pool.submit(new Runnable() {
 			@Override
 			public void run() {
 				for (ClasspathListener listener : cpListeners) {
 					listener.classpathChanged();
 				}
 			}
 		});
 	}
 
 	/**
 	 * @return the location of pom.xml file on disk.
 	 */
 	public File getPomXmlFile() {
 		return ProjectFileUtils.getPomXmlFile(projectDir);
 	}
 
 	/**
 	 * Creates a new view.
 	 * 
 	 * @param name
 	 * @param byUser
 	 * @return the view; null if failed.
 	 */
 	public SharedView createView(String name, User byUser) {
 		if (!SharedView.isvalidName(name)) {
 			System.err.println(name + " is not a valid name.");
 			return null;
 		}
 
 		SharedView c = null;
 		synchronized (this) {
 			if (containsView(name)) {
 				System.err.println("View " + name + " already exists.");
 				return null;
 			}
 
 			c = new SharedView(getPackageName(), name, getSourceDir(), log);
 			addView(c, byUser);
 		}
 
 		fireChanged();
 		fireClasspathChanged();
 
 		return c;
 	}
 
 	/**
 	 * The project's name.
 	 */
 	public String getName() {
 		// No sync because final.
 		return name;
 	}
 
 	/** 
 	 * The java package of the project.
 	 */
 	public String getPackageName() {
 		// No sync because final.
 		return packageName;
 	}
 
 	/**
 	 * Writes all the project files to the disk.
 	 * 
 	 * @throws IOException
 	 */
 	public void writeToDisk() throws IOException {
 		writeToDisk(getProjectDir());
 	}
 	
 	public synchronized void writeToDisk(File dir) throws IOException {
 		File src = ProjectFileUtils.getSourceDir(dir, getPackageName());
 		for (SharedView v : views.values()) {
 			v.writeBaseToDisk(src);
 		}
 
 		for (ProjectFile e : files.values()) {
 			String s = e.getMud().getBase().getText();
 			FileUtils.write(new File(src, e.getName()), s);
 		}
 
 		try {
 			writePomXml(dir);
 		} catch (TransformerFactoryConfigurationError | TransformerException e) {
 			throw new IOException(e);
 		}
 	}
 	
 	public synchronized void writeToDiskIncludingInitial(File dir, 	UserSettings settings) throws IOException {
 		ProjectFileUtils.writeInitialFilesToDisk(dir, getPackageName(), settings);
 		writeToDisk(dir);
 	}
 	
 	/**
 	 * The dir where the projects files are saved.
 	 */
 	public File getProjectDir() {
 		return projectDir;
 	}
 
 	/**
 	 * The dir where all the .java and other source files of this project are.
 	 * Eg. <projectDir>/src/main/java/com/arvue/apps/<projectName>/
 	 */
 	private File getSourceDir() {
 		return ProjectFileUtils.getSourceDir(getProjectDir(), getPackageName());
 	}
 
 	private void writePomXml(File dir) throws IOException,
 			TransformerFactoryConfigurationError, TransformerException {
 		String s = pomXml.getAsString();
 		ProjectFileUtils.writePomXml(dir, s);
 	}
 
 	private void writePomXml() throws IOException,
 			TransformerFactoryConfigurationError, TransformerException {
 		writePomXml(getProjectDir());
 	}
 
 	/**
 	 * Reads the projects contents from disk.
 	 * 
 	 * TODO XXX: This probably works only once, when the project is empty???
 	 */
 	public void refreshFromDisk() {
 		try {
 			synchronized (this) {
 				Map<String, String> srcFiles = ProjectFileUtils.readSourceFiles(
 						projectDir, this.getPackageName());
 				setSourceFiles(srcFiles);
 				this.pomXml = new PomXml(ProjectFileUtils.readPomXml(projectDir));
 			}
 			
 		} catch (IOException | ParserConfigurationException | SAXException e) {
 			System.err.println("WARNING: could not refresh from disk: "
 					+ e.getMessage());
 			e.printStackTrace();
 		}
 		// I think it's the right thing to do to compile all after refresh. (?)
 		compileAll();
 		
 		// The project may have not have always changed
 		// but too lazy to actually check that...
 		fireChanged();
 	}
 	
 	/**
 	 * Sets the source files.
 	 * 
 	 * XXX.java files with corresponding XXX.clara.xml are treated as views.
 	 * 
 	 */
 	private void setSourceFiles(Map<String, String> srcFiles) {
 		HashMap<String, String> claraXmls = new HashMap<String, String>();
 		HashMap<String, String> files = new HashMap<String, String>();
 
 		for (Entry<String, String> e : srcFiles.entrySet()) {
 			if (e.getKey().endsWith(".clara.xml")) {
 				claraXmls.put(e.getKey(), e.getValue());
 			} else if (isEditableFile(e.getKey())) {
 				files.put(e.getKey(), e.getValue());
 			}
 		}
 
 		TreeMap<String, SharedView> views = new TreeMap<String, SharedView>();
 		TreeMap<String, ProjectFile> projFiles = new TreeMap<String, ProjectFile>();
 
 		for (Entry<String, String> e : files.entrySet()) {
 			String n = e.getKey();
 			if (n.endsWith(".java")) {
 				String vn = n.substring(0, n.length() - 5);
 				String xmlName = vn + ".clara.xml";
 				if (claraXmls.containsKey(xmlName)) {
 					SharedView v = new SharedView(getPackageName(), vn, getSourceDir(), log);
 					v.setControllerBase(e.getValue());
 					v.setModelBase(claraXmls.get(xmlName));
 					views.put(vn, v);
 				} else {
 					File saveTo = new File(getSourceDir(), n);
 					projFiles.put(n, ProjectFile.newJavaFile(n, e.getValue(), saveTo, log));
 				}
 			} else {
 				File saveTo = new File(getSourceDir(), n);
 				projFiles.put(n, new ProjectFile(n, e.getValue(), null, saveTo, log));
 			}
 		}
 		this.views = views;
 		this.files = projFiles;
 	}
 
 	/**
 	 * @return true iff the users are supposed to edit this file.
 	 */
 	private static boolean isEditableFile(String name) {
 		// TODO: what?
 		return true;
 		//return !name.equals(ProjectFileUtils.getAppClassName() + ".java");
 	}
 
 	public synchronized SharedView getView(String name) {
 		return views.get(name);
 	}
 
 	public synchronized boolean containsView(String name) {
 		return views.containsKey(name);
 	}
 
 	public synchronized boolean containsFile(String name) {
 		return files.containsKey(name);
 	}
 
 	public synchronized List<String> getViewNames() {
 		// Defensive copy because we don't want others to mess with the contents.
 		return new LinkedList<String>(views.keySet());
 	}
 
 	public synchronized List<String> getFileNames() {
 		// Defensive copy because we don't want others to mess with the contents.
 		return new LinkedList<String>(files.keySet());
 	}
 
 	public synchronized List<Dependency> getDependencies() {
 		// Defensive copy because we don't want others to mess with the contents.
 		return new LinkedList<Dependency>(pomXml.getDependencies());
 	}
 
 	/**
 	 * Sets a new Maven dependency.
 	 * 
 	 * Takes an xml snippet such as:
 	 * <dependency>
      * 	<groupId>org.vaadin.addons</groupId>
      * 	<artifactId>aceeditor</artifactId>
      * 	<version>0.8.2</version>
 	 * </dependency>
 	 * 
 	 * @param xmlSnippet
 	 * @throws UnsupportedEncodingException
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws TransformerFactoryConfigurationError
 	 * @throws TransformerException
 	 */
 	public void addDependency(String xmlSnippet)
 			throws UnsupportedEncodingException, ParserConfigurationException,
 			SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
 		synchronized (this) {
 			pomXml.addDependency(xmlSnippet);
 			writePomXml();
 		}
 		fireClasspathChanged();
 		fireChanged(); // TODO should we fire this changed, too(?)
 	}
 	
 	public void removeDependency(Dependency dep) {
 		synchronized (this) {
 			pomXml.removeDependency(dep);
 		}
 		fireClasspathChanged();
 		fireChanged();
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Gets the list of the users in the project.
 	 * 
 	 * @return the names of collaborators separated with comma
 	 */
 	public synchronized List<User> getUsers() {
 		return new ArrayList<User>(users);
 	}
 
 
 	/**
 	 * Adds new collaborator.
 	 * 
 	 * @param user
 	 *            the user
 	 */
 	public void addUser(User user) {
 		boolean added;
 		synchronized (this) {
 			added = users.add(user);
 		}
 		if (added) {
 			getChat().addLine(user.getName() + " joined");
 			// TODO: should we fire some changed event?
 		}
 	}
 
 	/**
 	 * Removes the user from project.
 	 * 
 	 * @param user
 	 *            the user
 	 */
 	public void removeUser(User user) {
 		boolean removed;
 		synchronized (this) {
 			removed = users.remove(user);
 		}
 		if (removed) {
 			getChat().addLine(user.getName() + " left");
 			// TODO fire something?
 		}
 	}
 	
 	private void removeAllUsers() {
 		@SuppressWarnings("unused")
 		Collection<User> removed;
 		synchronized (this) {
 			removed = new TreeSet<User>(users);
 			users.clear();
 		}
 		// TODO fire users removed!
 	}
 
 	/**
 	 * Removes user from all of the projects.
 	 * 
 	 * @param user
 	 *            the user to be removed from projects
 	 */
 	public static void removeFromProjects(User user) {
 		synchronized (projects) {
 			for (SharedProject project : projects.values()) {
 				project.removeUser(user);
 			}
 		}
 	}
 
 	/**
 	 * Checks if user is currently in any project.
 	 * 
 	 * @param user
 	 * @return true, if user is collaborating in one of the projects
 	 */
 	public static boolean isInProject(User user) {
 		if (user == null) {
 			return false;
 		}
 		synchronized (projects) {
 			for (SharedProject project : projects.values()) {
 				if (project.hasUser(user)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if user is in this project.
 	 * 
 	 * @param user
 	 * @return true, if user is collaborating in this project
 	 */
 	public synchronized boolean hasUser(User user) {
 		if (user == null) {
 			return false;
 		} else {
 			return users.contains(user);
 		}
 	}
 
 	/**
 	 * Gets a project by name.
 	 * 
 	 * @return the project; null if no such project
 	 */
 	public static SharedProject getProject(String name) {
 		synchronized (projects) {
 			SharedProject p = projects.get(name);
 			if (p!=null) {
 				return p;
 			}
 			else {
 				if (projectNames.contains(name)) {
 					try {
 						// XXX This may take a long time, so shouldn't be inside synchronized...
 						return loadProject(name);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static SharedProject loadProject(String name) throws IOException {
 		SharedProject p = loadProjectFromFiles(new File(projectRootDir, name));
 		synchronized (projects) {
 			projects.put(name, p);
 		}
 		return p;
 	}
 
 	/**
 	 * Gets the project names.
 	 * 
 	 * @return the project names
 	 */
 	public static Collection<String> getProjectNames() {
 		synchronized (projects) {
 			return new TreeSet<String>(projectNames);
 			//return new TreeSet<String>(projects.keySet());
 		}
 	}
 
 	/**
 	 * Load projects from disk.
 	 * 
 	 * @return the linked list
 	 * @throws IOException
 	 */
 	private static void loadProjectlistFromFiles(
 			File projectRoot) throws IOException {
 		synchronized (projects) {
 			if (projectRoot.exists()) {
 				projectNames = new HashSet<String>();
 				for (File projectDir : projectRoot.listFiles()) {
 					projectNames.add(projectDir.getName());
 				}
 			} else {
 				throw new FileNotFoundException(projectRoot + " not found!");
 			}
 		}
 	}
 
 	/**
 	 * Reads the project from the file location and adds it into the list of projects.
 	 * 
 	 * @param projectDir
 	 *            the project dir
 	 * @throws IOException
 	 */
 	public static void addProjectFromFiles(File projectDir) throws IOException {
 		putProject(loadProjectFromFiles(projectDir));
 	}
 	
 	/**
 	 * Adds the project. 
 	 */
 	private static void putProject(SharedProject p) {
 		synchronized (projects) {
 			projects.put(p.getName(), p);
 			projectNames.add(p.getName());
 		}
 	}
 
 	/**
 	 * Load project from files.
 	 * 
 	 * @param projectDir
 	 *            the project dir
 	 * @return the shared project
 	 * @throws IOException
 	 */
 	private static SharedProject loadProjectFromFiles(File projectDir)
 			throws IOException {
 		if (!projectDir.isDirectory()) {
 			throw new IllegalArgumentException("No such dir: " + projectDir);
 		}
 
 		SharedProject s = new SharedProject(projectDir.getName(), projectDir);
 		s.refreshFromDisk();
 		s.log.logLoadedFromDisk(projectDir);
 		return s;
 	}
 
 	/**
 	 * Returns the project chat.
 	 */
 	public SharedChat getChat() {
 		return chat;
 	}
 
 	/**
 	 * The java compiler of the project.
 	 */
 	public synchronized CompilingService getCompiler() {
 		if (compiler == null) {
 			compiler = new CompilingService(this);
 		}
 		return compiler;
 	}
 
 	/**
 	 * 
 	 * @return the file; null if no such file.
 	 */
 	public synchronized ProjectFile getFile(String name) {
 		return files.get(name);
 	}
 	
 	private void addView(SharedView view, User byUser) {
 		views.put(view.getName(), view);
 		getCompiler().compile(view);
 		if (byUser!=null) {
 			getChat().addLine(byUser.getName() + " created a view: " + view.getName());
 		}
 	}
 
 	public boolean addFile(ProjectFile f, User byUser) {
 		synchronized (this) {
 			if (containsFile(f.getName())) {
 				return false;
 			}
 			files.put(f.getName(), f);
 		}
 		compileFile(f);
 		if (byUser!=null) {
 			getChat().addLine(byUser.getName() + " created a file: " + f.getName());
 		}
 		fireChanged();
 		return true;
 	}
 	
 	private void compileFile(ProjectFile f) {
 		if (f.getName().endsWith(".java")) {
 			String cls = fullJavaClassNameFromFilename(f.getName());
 			getCompiler().compile(cls, f.getMud().getBase().getText(), null);
 		}
 	}
 
 	/**
 	 * Removes the view from the project.
 	 * 
 	 * @param name
 	 * @param byUser
 	 */
 	public void removeView(String name, User byUser) {
 		SharedView removedView;
 		synchronized (this) {
 			removedView = views.remove(name);
 		}
 		// TODO: remove from git repository!
 		if (removedView!=null) {
 			removeViewFromDisk(removedView);
 			getCompiler().removeClass(removedView.getControllerFullName());
 			getChat().addLine(byUser.getName() + " deleted a view: " + name);
 			fireChanged();
 		}
 	}
 
 	/**
 	 * Removes the file from the project.
 	 * 
 	 * @param name
 	 * @param byUser
 	 */
 	public void removeFile(String name, User byUser) {
 		boolean removed;
 		synchronized (this) {
 			removed = files.remove(name) != null;
 		}
 		// TODO: remove from git repository!
 		if (removed) {
 			removeFileFromDisk(name);
 			getCompiler().removeClass(fullJavaClassNameFromFilename(name));
 			getChat().addLine(byUser.getName() + " deleted a file: " + name);
 			fireChanged();
 		}
 	}
 	
 	private void removeViewFromDisk(SharedView view) {
 		removeFileFromDisk(view.getName()+".java");
 		removeFileFromDisk(view.getName()+".clara.xml");
 	}
 	
 	private void removeFileFromDisk(String filename) {
 		File f = new File(getSourceDir(), filename);
 		f.delete();
 	}
 
 	public File getSourceFileLocation(String filename) {
 		return new File(getSourceDir(), filename);
 	}
 
 	public ProjectLog getLog() {
 		// No need to sync because final.
 		return log;
 	}
 
 	public static List<User> getProjectUsers(String projectName) {
 		synchronized (projects) {
 			if (projects.containsKey(projectName)) {
 				return new ArrayList<User>(projects.get(projectName).getUsers());
 			}
 		}
 		return Collections.emptyList();
 	}
 
 	public static boolean projectExists(String name) {
 		return getProjectNames().contains(name);
 	}
 
 	public synchronized void removeDiffering(User user) {
 		String uid = user.getUserId();
 		for (ProjectFile f : files.values()) {
 			f.getMud().removeDiffering(uid);
 		}
 		for (SharedView v : views.values()) {
 			v.getControllerMUD().removeDiffering(uid);
 			v.getModelMud().removeDiffering(uid);
 		}
 	}
 
	public File getTargetDir() {
		return new File(this.getProjectDir().getAbsolutePath(),"target-1");
 	}
 
 	
 
 }
