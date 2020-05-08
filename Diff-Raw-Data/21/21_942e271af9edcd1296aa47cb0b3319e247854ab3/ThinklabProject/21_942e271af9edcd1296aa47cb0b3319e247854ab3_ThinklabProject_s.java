 package org.integratedmodelling.thinklab.client.project;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 
 import org.integratedmodelling.exceptions.ThinklabException;
 import org.integratedmodelling.exceptions.ThinklabIOException;
 import org.integratedmodelling.exceptions.ThinklabRuntimeException;
 import org.integratedmodelling.thinklab.api.modelling.INamespace;
 import org.integratedmodelling.thinklab.api.project.IProject;
 import org.integratedmodelling.thinklab.client.Configuration;
 import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
 import org.integratedmodelling.thinklab.client.modelling.ModelManager;
 import org.integratedmodelling.thinklab.client.utils.CamelCase;
 import org.integratedmodelling.thinklab.client.utils.FolderZiper;
 import org.integratedmodelling.thinklab.client.utils.MiscUtilities;
 
 public class ThinklabProject implements IProject {
 	
 	String _id = null;
 	Properties _properties = null;
 	private ArrayList<INamespace> namespaces = new ArrayList<INamespace>();
 	private List<IProject> dependencies = new ArrayList<IProject>();
 
 	/*
 	 * this is <= _errors.size()
 	 */
 	private int _namespacesInError = 0;
 	/*
 	 * these 2 are in sync
 	 */
 	private ArrayList<File> _resourcesInError = new ArrayList<File>();
 	private ArrayList<String> _errors = new ArrayList<String>();
 	
 	public ThinklabProject(String pluginId) {		
 		_id = pluginId;
 	}
 	
 	public ThinklabProject(File dir) throws ThinklabException {		
 		_id = MiscUtilities.getFileName(dir.toString());
 		load();
 	}
 	
 	public boolean hasErrors() {
 		return _namespacesInError > 0;
 	}
 	
 	/**
 	 * Get the content of META-INF/thinklab.properties if the plugin contains that
 	 * directory, or null if it doesn't. Can be used to check if a plugin is a 
 	 * thinklab plugin based on the null return value.
 	 * 
 	 * TODO move to client library and load the library in the server package
 	 * 
 	 * @param plugin
 	 * @return
 	 * @throws ThinklabIOException
 	 */
 	public static Properties getPluginProperties(String plugin) throws ThinklabClientException {
 		
 			Properties ret = null;
 			File pfile = 
 				new File(
 					Configuration.getProjectDirectory(plugin) + 
 					File.separator + 
 					"META-INF" +
 					File.separator + 
 					"thinklab.properties");
 			
 			if (pfile.exists()) {
 				try {
 					ret = new Properties();
 					ret.load(new FileInputStream(pfile));
 				} catch (Exception e) {
 					throw new ThinklabClientException(e);
 				}
 			}
 			
 			return ret;
 		}
 	
 	public static ThinklabProject create(String id) throws ThinklabClientException {
 	
 		ThinklabProject ret = new ThinklabProject(id);
 
 		if (ret.exists()) {
 			throw new ThinklabClientException("project " + id + " already exists");
 		}
 		ret.create((String[])null);
 		return ret;	
 	}
 	
 	/**
 	 * Create a namespace in the first sourcefolder.
 	 * @param p 
 	 * 
 	 * @param ns
 	 * @return file path relative to project directory
 	 * @throws ThinklabException
 	 */
 	public String createNamespace(IProject p, String ns) throws ThinklabException {
 				
 		File ret = new File(getSourceDirectory() + File.separator + 
 							ns.replace('.', File.separatorChar) + ".tql");
 		File dir = new File(MiscUtilities.getFilePath(ret.toString()));
 		
 		try {
 			dir.mkdirs();
 			PrintWriter out = new PrintWriter(ret);
 			out.println("namespace " + ns + ";\n");
 			out.close();
 		} catch (Exception e) {
 			throw new ThinklabClientException(e);
 		}
 		
 		return getSourceFolderNames().iterator().next() + File.separator + 
 				ns.replace('.', File.separatorChar) + ".tql";
 	}
 	
 	/**
 	 * Get the relative file path of given namespace. Doesn't have to exist.
 	 * 
 	 * FIXME only uses the first source folder.
 	 * 
 	 * @param ns
 	 * @return
 	 */
 	public String getNamespaceSourcePath(String ns) {
 		return getSourceFolderNames().iterator().next() + File.separator + 
 				ns.replace('.', File.separatorChar) + ".tql";
 	}
 	
 	public static ThinklabProject load(String id) throws ThinklabException {
 	
 		ThinklabProject ret = new ThinklabProject(id);
 
 		if (!ret.exists()) {
 			throw new ThinklabClientException("project " + id + " does not exist");
 		}
 		ret.load();
 		return ret;	
 	}
 	
 	/**
 	 * Re-read all resources.
 	 * 
 	 * @throws ThinklabClientException
 	 */
 	public void refresh() throws ThinklabException {
 		namespaces.clear();
 		load();
 	}
 
 	private void create(String[] dependencies) throws ThinklabClientException {
 
 		/*
 		 * TODO these need to make sense. Probably the modelling plugin should
 		 * just contain dependencies for the commonly used ones, such as
 		 * space and time, and all other deps should be other project chosen
 		 * from the workspace.
 		 */
 		if (dependencies == null) {
 			String dps = Configuration.getProperties().getProperty(
 					"default.project.dependencies",
 					"org.integratedmodelling.thinklab.core");
 			
 			dependencies = dps.split(",");
 		}
 		
 		File plugdir = Configuration.getProjectDirectory(_id);
 		createManifest(plugdir, dependencies);
 		
 	}
 	
 	public String getId() {
 		return _id;
 	}
 	
 	public boolean exists() {
 		File f = 
 			new File(Configuration.getProjectDirectory() + 
 					File.separator + _id + File.separator + "META-INF" + File.separator + "thinklab.properties");
 		
 		return f.exists();
 	}
 	
 	public static boolean exists(File dir) {
 		
 		File f = 
 			new File(dir + File.separator + "META-INF" + File.separator + "thinklab.properties");
 		return f.exists();
 	}
 	
 	public static boolean exists(String id) {
 		
 		File f = 
 			new File(Configuration.getProjectDirectory() + 
 					File.separator + id + File.separator + "META-INF" + File.separator + "thinklab.properties");
 		
 		return f.exists();
 	}
 	
 	@Override
 	public void addDependency(String plugin, boolean reload) throws ThinklabException {
 	
 		String pp = getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "");
 		String[] deps = 
 			pp.isEmpty() ? new String[0] :
 			getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "").split(",");
 		
 		String dps = "";
 		for (String s : deps) {
 			if (s.equals(plugin))
 				return;
 			dps += (dps.isEmpty()? "" : ",") + s;
 		}
 		
 		dps += (dps.isEmpty()? "" : ",") + plugin;
 		getProperties().setProperty(IProject.PREREQUISITES_PROPERTY, dps);
 		deps = dps.split(",");
 
 		/*
 		 * TBC this one is correct, but loading such projects within a TQL context messes up
 		 * something obscure with the classloader that Guice uses to initialize the parser at
 		 * the server side. Because we don't put any code in the project plugins, and we
 		 * handle imports explicitly, we can do without the JPF dependency.
 		 */
 //		createManifest(getPath(), deps);
 		saveProperties();
 		
 		if (reload)
 			load();
 	}
 	
 	public void createManifest(File pluginDir, String[] dependencies) throws ThinklabClientException {
 
 		try {
 			FileOutputStream fout = 
 				new FileOutputStream(new File(pluginDir + File.separator + "plugin.xml"));
 			PrintStream out = new PrintStream(fout);
 			out.print( 
 				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" + 
 				"<!DOCTYPE plugin PUBLIC \"-//JPF//Java Plug-in Manifest 1.0\" \"http://jpf.sourceforge.net/plugin_1_0.dtd\">\r\n" + 
 				"<plugin id=\"__ID__\" version=\"0.8.1.20110428103733\">\r\n".replaceAll("__ID__", _id) + 
 				"   <requires>\r\n");
 
 			for (String s : dependencies) {
 				out.print(
 					"	     <import exported=\"false\" match=\"compatible\" " +
 					"optional=\"false\" plugin-id=\"__ID__\" reverse-lookup=\"true\"/>\r\n".replaceAll("__ID__", s));
 			}
 			
 			out.print(
 				"   </requires>\r\n" + 
 				"</plugin>");
 		
 			fout.close();
 			
 			File td = new File(pluginDir + File.separator + "META-INF");
 			td.mkdirs();
 			this._properties = new Properties();
 
 			
 			/*
 			 * create the default source folder 
 			 * TODO also create an empty plugin configuration file.
 			 */
 			this._properties.put(IProject.SOURCE_FOLDER_PROPERTY, "src");
 			new File(pluginDir + File.separator + "src").mkdirs();
 			
 			saveProperties();
 			
 		} catch (IOException e) {
 			throw new ThinklabClientException(e);
 		}
 	}
 
 	private void saveProperties() throws ThinklabClientException {
 		
 		File td = 
 			new File(Configuration.getProjectDirectory(_id) +
 				File.separator + "META-INF" +
 				File.separator + "thinklab.properties");
 		
 		try {
 			_properties.store(new FileOutputStream(td), null);
 		} catch (Exception e) {
 			throw new ThinklabClientException(e);
 		}
 		
 	}
 	
 	public File getZipArchive() throws ThinklabClientException {
 		
 		File ret = null;
 		try {
 			ret = File.createTempFile("tpr", ".zip", Configuration.getTemporaryPath());
 			FolderZiper.zipFolder(
 				Configuration.getProjectDirectory(_id).toString(), 
 				ret.toString());
 		} catch (Exception e) {
 			throw new ThinklabClientException(e);
 		}
 		return ret;
 	}
 
 	public File getPath() {
 		return Configuration.getProjectDirectory(_id);
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		return o instanceof ThinklabProject && ((ThinklabProject)o).getId().equals(_id);
 	}
 
 	@Override
 	public int hashCode() {
 		return _id.hashCode();
 	}
 
 	@Override
 	public Properties getProperties() {
 		if (_properties == null) {
 			try {
 				_properties = getPluginProperties(getId());
 			} catch (ThinklabClientException e) {
 				throw new ThinklabRuntimeException(e);
 			}
 		}
 		return _properties;
 	}
 
 	@Override
 	public Collection<INamespace> getNamespaces() {
 		return namespaces;
 	}
 
 	@Override
 	public File getSourceDirectory() {
 		return new File(getPath() + File.separator + 
 				getProperties().getProperty(IProject.SOURCE_FOLDER_PROPERTY, "src"));
 	}
 	
 	public Collection<String> getSourceFolderNames() {
 		String[] folders = getProperties().getProperty(IProject.SOURCE_FOLDER_PROPERTY, "src").split(",");
 		ArrayList<String> ret = new ArrayList<String>();
 		for (String f : folders) {
 			ret.add(f);
 		} 
 		return ret;
 	}
 
 	@Override
 	public String getOntologyNamespacePrefix() {
 		return getProperties().getProperty(
 				IProject.ONTOLOGY_NAMESPACE_PREFIX_PROPERTY, "http://www.integratedmodelling.org/ns");
 	}
 
 	@Override
 	public void load() throws ThinklabException {
 	
 		_properties = getPluginProperties(_id);
 
 		loadDependencies();
 		
 		namespaces = new ArrayList<INamespace>();
 		HashSet<File> read = new HashSet<File>();
 			
 		loadInternal(this.getSourceDirectory(), read, namespaces, "", this);
 		
 	}
 
 	private void loadDependencies() throws ThinklabClientException {
 
 		String pp = getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "");
 		String[] deps = 
 			pp.isEmpty() ? new String[0] :
 			getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "").split(",");
 			
 		for (String dep : deps) {
 			IProject p = ProjectFactory.get().getProject(dep, true);
 			if (p == null)
 				throw new ThinklabClientException(_id + ": cannot load prerequisite project " + dep);
 			dependencies.add(p);
 		}
 	}
 
 	private void loadInternal(File f, HashSet<File> read, ArrayList<INamespace> ret, String path,
 			IProject project) throws ThinklabClientException {
 
 		String pth = 
 				path == null ? 
 					"" : 
 					(path + (path.isEmpty() ? "" : ".") + CamelCase.toLowerCase(MiscUtilities.getFileBaseName(f.toString()), '-'));
 		
 		if (f. isDirectory()) {
 		
 			for (File fl : f.listFiles()) {
 				loadInternal(fl, read, ret, pth, project);
 			}
 			
 		} else if (ModelManager.get().canParseExtension(MiscUtilities.getFileExtension(f.toString()))) {
 
 			INamespace ns;
 			try {
 				ns = ModelManager.get().loadFile(f.toString(), pth, this);
				ret.add(ns);
 			} catch (ThinklabException e) {
 				_namespacesInError ++;
 				_resourcesInError.add(f);
 				_errors.add(e.getMessage());
 			}
 		}
 		
 	}
 
 	@Override
 	public List<IProject> getPrerequisites() {
 		return dependencies;
 	}
 
 	@Override
 	public File findResource(String resource) {
 
 		File ff = new File(getSourceDirectory() + File.separator + resource);
 		if (ff.exists()) {
 			return ff;
 		}
 		return null;
 	}
 	
 	@Override
 	public File findResourceForNamespace(String namespace, String extension) {
 
 		String fp = namespace.replace('.', File.separatorChar);
 		File ff = new File(getSourceDirectory() + File.separator + fp + "." + extension);
 		if (ff.exists()) {
 			return ff;
 		}
 			
 		return null;
 	}
 
 	public Collection<String> getErrors() {
 		return _errors;
 	}
 
 	@Override
 	public void unload() throws ThinklabException {
 
 	}
 
 	@Override
 	public File getWorkspace() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public File getWorkspace(String subspace) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public File getScratchArea() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public File getScratchArea(String subArea) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public File getTempArea(String subArea) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public File getLoadPath(String subArea) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public File getLoadPath() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 }
