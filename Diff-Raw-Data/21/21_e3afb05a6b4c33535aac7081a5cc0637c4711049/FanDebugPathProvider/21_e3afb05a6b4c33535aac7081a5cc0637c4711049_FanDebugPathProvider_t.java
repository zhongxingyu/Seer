 /*
  * Thibaut Colar Aug 25, 2009
  */
 package net.colar.netbeans.fan.debugger;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.platform.FanPlatform;
 import net.colar.netbeans.fan.project.FanProject;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.java.classpath.GlobalPathRegistry;
 import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
 import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
 import org.netbeans.api.java.platform.JavaPlatform;
 import org.netbeans.api.java.platform.JavaPlatformManager;
 import org.netbeans.api.java.project.JavaProjectConstants;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.project.ProjectUtils;
 import org.netbeans.api.project.SourceGroup;
 import org.netbeans.api.project.Sources;
 import org.netbeans.api.project.ui.OpenProjects;
 import org.netbeans.spi.debugger.ContextProvider;
 import org.netbeans.spi.debugger.jpda.SourcePathProvider;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileStateInvalidException;
 import org.openide.filesystems.FileUtil;
 import org.openide.filesystems.JarFileSystem;
 import org.openide.filesystems.URLMapper;
 import org.openide.util.WeakListeners;
 
 /**
  * We need a custom source path provider because with fan sources such as
  *	MyPod/fan/Main.java   doe not get mapped to fan.Main.class as in java
  *  but instead to fan.MyPod.Main.class
  * So we need a custom impl. here to find the correct source path and make the debugger happy.
  *
  * @author thibautc
  */
 public class FanDebugPathProvider extends SourcePathProvider
 {
 
 	private static final Pattern thisDirectoryPattern = Pattern.compile("(/|\\A)\\./");
 	private static final Pattern parentDirectoryPattern = Pattern.compile("(/|\\A)([^/]+?)/\\.\\./");
 	// open prjs
 	private Set<ClassPath> projectSources = new HashSet<ClassPath>();
 	// fan distro sources
 	private Set<ClassPath> fanSources = new HashSet<ClassPath>();
 	private Set<ClassPath> jdkSources = new HashSet<ClassPath>();
 	// All resources known to IDE...
 	private Set<ClassPath> globalSources = new HashSet<ClassPath>();
 	// user provided paths (through listener)
 	private Set<ClassPath> customSources = new HashSet<ClassPath>();
 	private PropertyChangeSupport pcs;
 	private PathRegistryListener pathRegistryListener;
 	private String[] sourceRoots;
 	private String[] originalRoots;
 
 	public FanDebugPathProvider()
 	{
 		pcs = new PropertyChangeSupport(this);
 	}
 
 	public FanDebugPathProvider(ContextProvider contextProvider)
 	{
 		pcs = new PropertyChangeSupport(this);
 		pathRegistryListener = new PathRegistryListener();
 		GlobalPathRegistry.getDefault().addGlobalPathRegistryListener(WeakListeners.create(GlobalPathRegistryListener.class, pathRegistryListener, GlobalPathRegistry.getDefault()));
 		JavaPlatformManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(pathRegistryListener, JavaPlatformManager.getDefault()));
 		// TODO: listener for project sources ?
 
 		// Add open project sources
 		Project[] projects = OpenProjects.getDefault().getOpenProjects();
 		for (Project prj : projects)
 		{
 			if (prj != null)
 			{
 				SourceGroup[] sgs;
 				if (prj instanceof FanProject)
 				{
 					sgs = ProjectUtils.getSources(prj).getSourceGroups(Sources.TYPE_GENERIC);
 				} else
 				{
 					sgs = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
 				}
 
 				for (SourceGroup sg : sgs)
 				{
 					ClassPath cp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
 					if (cp != null)
 					{
 						projectSources.add(cp);
 					}
 				}
 			}
 		}
 		// Jdk sources
 		JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
 		if (platform != null)
 		{
 			ClassPath cp = platform.getSourceFolders();
 			if (cp != null)
 			{
 				jdkSources.add(cp);
 			}
 		}
 		// Fan distro sources
 		//TODO: those are already in open projects path ????
         if ( ! FanPlatform.isConfigured()) {
             throw new RuntimeException("Fantom SDK not defined");
         }
 
 		fanSources = FanPlatform.getInstance().getSourceClassPaths();
 
 		// update roots list
 		updateRoots();
 		originalRoots = sourceRoots;
 
 		// global sources
 		globalSources.addAll(GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE));
 
 		// debugging
 		if (projectSources != null)
 		{
 			FanUtilities.GENERIC_LOGGER.info("===========Prj roots=============");
 			dump(projectSources);
 		}
 		if (customSources != null)
 		{
 			FanUtilities.GENERIC_LOGGER.info("===========Custom roots=============");
 			dump(customSources);
 		}
 		if (fanSources != null)
 		{
 			FanUtilities.GENERIC_LOGGER.info("===========Fan roots=============");
 			dump(fanSources);
 		}
 		if (jdkSources != null)
 		{
 			FanUtilities.GENERIC_LOGGER.info("===========Jdk roots=============");
 			dump(jdkSources);
 		}
 	}
 
 	/**
 	 * Translates a relative path ("java/lang/Thread.java") to url (as it is in jar)
 	 * A little convoluted dure to fan pod structure
 	 *
 	 * Check in Fan pods first (using fan style pathing)
 	 *
 	 * @param relativePath a relative path (java/lang/Thread.java)
 	 * @param global true if global path should be used
 	 * @return url or <code>null</code>
 	 */
 	public String getURL(String relativePath, boolean global)
 	{
 		if (relativePath.startsWith(File.separator))
 		{
 			relativePath = relativePath.substring(1);
 		}
 
 		FanUtilities.GENERIC_LOGGER.debug("+++ Initial path: " + relativePath);
 		String path = null;
 		if (relativePath != null && (relativePath.endsWith(".fan") || relativePath.endsWith(".fwt")))
 		{
 			// This is how the path looks, when coming from JPDA java breakpoint ("fan/myPod/Main.fan)
 			if (relativePath.startsWith("fan"+File.separator) || relativePath.startsWith("fanx"+File.separator))
 			{
 				path = relativePath.substring(relativePath.indexOf(File.separator)+1);
 				int index = path.indexOf(File.separator);
 				String fanPath = path.substring(index + 1);
 				String pod = path.substring(0, index);
 				path = getURLPath(fanPath, pod, false);
 				if (path != null)
 				{
 					return path;
 				}
 
 			} else
 			{
 				// normal source lookup, we get the path from getUrl()
 				// we will get something like dummy3/fan/Main.fan (the actual path under project root is just fan/Main.fan) as specially crafted in getRelativePath()
 				int index = relativePath.indexOf(File.separator) + 1;
 				String fanPath = relativePath.substring(index);
 				String pod = relativePath.substring(0, index-1);
 				// We uwill check agaisnt "pod" to help find the right Source
 				path = getURLPath(fanPath, pod, false);
 				if (path != null)
 				{
 					return path;
 				}
 			}
 		}
 
 		// If not a fan file, or not found in pods, then try "standard" way
 		path = getURLPath(relativePath, null, global);
 
 		return path;
 	}
 
 	/**
 	 * Translates a relative path ("java/lang/Thread.java") to url (as it is in jar)
 	 * We search in open projects, then fansources then jdk sources, then global
 	 *
 	 * @param relativePath
 	 * @param cpSuffix
 	 * @param global
 	 * @return
 	 */
 	private String getURLPath(String relativePath, String pod, boolean global)
 	{
 		//System.out.println(getClass().getName() + " getUrl :" + relativePath);
 		relativePath = normalize(relativePath);
 
 		// Try to find it.
 		FileObject fo = findResource(projectSources, pod, relativePath);
 		if (fo == null)
 		{
 			fo = findResource(customSources, pod, relativePath);
 		}
 		if (fo == null)
 		{
 			fo = findResource(fanSources, pod, relativePath);
 		}
 		// pod ==null means Don't look in the following path for fan sources(shouldn't be there)
 		if (pod == null && fo == null)
 		{
 			fo = findResource(jdkSources, null, relativePath);
 		}
 		if (pod == null && fo == null && global)
 		{
 			fo = findResource(globalSources, null, relativePath);
 		}
 
 		if (fo == null)
 		{
 			FanUtilities.GENERIC_LOGGER.debug(getClass().getName() + " Url not found for " + relativePath);
 			return null;
 		}
 
 		try
 		{
 			//System.out.println(getClass().getName() + " getUrl result:" + fo.getURL().toString());
 			return fo.getURL().toString();
 		} catch (FileStateInvalidException e)
 		{
 			return null;
 		}
 
 	}
 
 	/**
 	 * Translates a relative path to all possible URLs.
 	 *
 	 * @param relativePath a relative path (java/lang/Thread.java)
 	 * @param global true if global path should be used
 	 * @return url
 	 */
 	public String[] getAllURLs(String relativePath, boolean global)
 	{
 		relativePath = normalize(relativePath);
 		Set<FileObject> fos = findAllResources(relativePath);
 		List<String> urls = new ArrayList<String>(fos.size());
 		for (FileObject fo : fos)
 		{
 			try
 			{
 				urls.add(fo.getURL().toString());
 			} catch (FileStateInvalidException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return urls.toArray(new String[0]);
 	}
 
 	/**
 	 * Returns relative path for given url.
 	 * In theoru that would return just Main.fan for example, but if we do that we won't be able
 	 * to resolve the source path correctly in getUrl()
 	 * So we craft a path such as MyPod/fan/Main.fan instead.
 	 *
 	 * @param url a url of resource file
 	 * @param directorySeparator a directory separator character
 	 * @param includeExtension whether the file extension should be included
 	 *        in the result
 	 *
 	 * @return relative path
 	 */
 	public String getRelativePath(String url, char directorySeparator, boolean includeExtension)
 	{
 		// 1) url -> FileObject
 		FileObject fo = null;
 		try
 		{
 			fo = URLMapper.findFileObject(new URL(url));
 		} catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 
 		String path = fo.getPath();
 		File folder = FanUtilities.getPodFolderForPath(path).getParentFile();
 		if (folder != null)
 		{ // Fantom sources
 			// construct an url like MyPod/fan/Main.fan - we will need the pod name in getUrl() to resolve the source location
 			if (path.startsWith(folder.getPath()))
 			{
 				String relPath = path.substring(folder.getPath().length());
 				// TODO: Ugly: I should lookup the java folder from build.fan rather than hardcoded
				if (relPath.endsWith(".java") && relPath.startsWith(File.separator+"java"+File.separator))
 				{
 					// Will have a fantom java path, such as : "fan/MyPod/main.java"
 					relPath = relPath.substring(6);
 				}
 				return relPath;
 			}
 		}
 		
 		// Otherwise do a "standard" lookup
 		String relativePath = null;
 		//TODO: globalSources.getResourceName(fo,	directorySeparator,	includeExtension);
 		// fallback to FileObject's class path
 		ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
 		if (cp == null)
 		{
 			cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
 		}
 
 		if (cp == null)
 		{
 			return null;
 		}
 
 		relativePath = cp.getResourceName(fo, directorySeparator, includeExtension);
 
 		return relativePath;
 	}
 
 	/**
 	 * Returns the source root (if any) for given url.
 	 *
 	 * @param url a url of resource file
 	 *
 	 * @return the source root or <code>null</code> when no source root was found.
 	 */
 	@Override
 	public synchronized String getSourceRoot(String url)
 	{
 		FileObject fo;
 		try
 		{
 			fo = URLMapper.findFileObject(new java.net.URL(url));
 		} catch (java.net.MalformedURLException ex)
 		{
 			fo = null;
 		}
 
 		FileObject[] roots = null;
 		if (fo != null)
 		{
 			ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
 			if (cp != null)
 			{
 				roots = cp.getRoots();
 			}
 
 		}
 		for (FileObject fileObject : roots)
 		{
 			try
 			{
 				String rootURL = fileObject.getURL().toString();
 				if (url.startsWith(rootURL))
 				{
 					String root = getRoot(fileObject);
 					if (root != null)
 					{
 						return root;
 					}
 
 				}
 			} catch (FileStateInvalidException ex)
 			{
 				// Invalid source root - skip
 			}
 		}
 		return null; // not found
 	}
 
 	/**
 	 * Adds property change listener.
 	 *
 	 * @param l new listener.
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener l)
 	{
 		pcs.addPropertyChangeListener(l);
 	}
 
 	/**
 	 * Removes property change listener.
 	 *
 	 * @param l removed listener.
 	 */
 	public void removePropertyChangeListener(
 		PropertyChangeListener l)
 	{
 		pcs.removePropertyChangeListener(l);
 	}
 
 // helper methods ..........................................................
 	/**
 	 * Normalizes the given path by removing unnecessary "." and ".." sequences.
 	 * This normalization is needed because the compiler stores source paths like "foo/../inc.jsp" into .class files.
 	 * Such paths are not supported by our ClassPath API.
 	 * TODO: compiler bug? report to JDK?
 	 *
 	 * @param path path to normalize
 	 * @return normalized path without "." and ".." elements
 	 */
 	public static String normalize(String path)
 	{
 		for (Matcher m = thisDirectoryPattern.matcher(path); m.find();)
 		{
 			path = m.replaceAll("$1");
 			m =
 				thisDirectoryPattern.matcher(path);
 		}
 
 		for (Matcher m = parentDirectoryPattern.matcher(path); m.find();)
 		{
 			if (!m.group(2).equals(".."))
 			{
 				path = path.substring(0, m.start()) + m.group(1) + path.substring(m.end());
 				m =
 					parentDirectoryPattern.matcher(path);
 			}
 
 		}
 		return path;
 	}
 
 	/**
 	 * Returns source root for given ClassPath root as String, or <code>null</code>.
 	 */
 	private static String getRoot(FileObject fileObject)
 	{
 		File f = null;
 		String path = "";
 		try
 		{
 			if (fileObject.getFileSystem() instanceof JarFileSystem)
 			{
 				f = ((JarFileSystem) fileObject.getFileSystem()).getJarFile();
 				if (!fileObject.isRoot())
 				{
 					path = "!/" + fileObject.getPath();
 				}
 
 			} else
 			{
 				f = FileUtil.toFile(fileObject);
 			}
 
 		} catch (FileStateInvalidException ex)
 		{
 		}
 		if (f != null)
 		{
 			return f.getAbsolutePath() + path;
 		} else
 		{
 			return null;
 		}
 
 	}
 
 	/**
 	 * Returns FileObject for given String.
 	 */
 	private FileObject getFileObject(String file)
 	{
 		File f = new File(file);
 		FileObject fo = FileUtil.toFileObject(f);
 		String path = null;
 		if (fo == null && file.contains("!/"))
 		{
 			int index = file.indexOf("!/");
 			f =
 				new File(file.substring(0, index));
 			fo =
 				FileUtil.toFileObject(f);
 			path =
 				file.substring(index + "!/".length());
 		}
 
 		if (fo != null && FileUtil.isArchiveFile(fo))
 		{
 			fo = FileUtil.getArchiveRoot(fo);
 			if (path != null)
 			{
 				fo = fo.getFileObject(path);
 			}
 
 		}
 		return fo;
 	}
 
 	private void dump(Set<ClassPath> cps)
 	{
 		for (ClassPath cp : cps)
 		{
 			for (ClassPath.Entry entry : cp.entries())
 			{
 				FanUtilities.GENERIC_LOGGER.debug(entry.toString());
 				System.out.println(entry);
 			}
 		}
 	}
 
 	private Set<FileObject> findAllResources(String path)
 	{
 		Set<FileObject> fos = new HashSet<FileObject>();
 		findAllResources(fos, globalSources, path);
 		return fos;
 	}
 
 	private void findAllResources(Set<FileObject> fos, Set<ClassPath> cps, String path)
 	{
 		for (ClassPath cp : cps)
 		{
 			FileObject fo = cp.findResource(path);
 			if (fo != null)
 			{
 				fos.add(fo);
 			}
 		}
 	}
 
 	private FileObject findResource(Set<ClassPath> classPaths, String pod, String path)
 	{
 		FileObject fo = null;
 		for (ClassPath cp : classPaths)
 		{
 			if (pod == null)
 			{
 				fo = cp.findResource(path);
 				//System.out.println("---- Looking for " + path + "in " + cp.toString()+ "-> "+fo==null?"null":fo.getPath());
 			} else
 			{
 				//System.out.println("---- Searching all resources for " + path);
 				List<FileObject> fos = cp.findAllResources(path);
 				Iterator<FileObject> it = fos.iterator();
 				while (it.hasNext())
 				{
 					FileObject fob = it.next();
 					FanUtilities.GENERIC_LOGGER.debug("---- Checking " + fob.getPath() + " vs " + pod);
 					if (FanUtilities.getPodFolderForPath(fob.getPath()).getName().equals(pod))
 					{
  						fo = fob;
 						//System.out.println("---- MATCH " + fob.getPath() + " vs " + tail);
 						break;
 					}
 				}
 
 			}
 			if (fo != null)
 			{
 				break;
 			}
 		}
 		return fo;
 	}
 
 	@Override
 	public String[] getSourceRoots()
 	{
 		return sourceRoots;
 	}
 
 	@Override
 	public void setSourceRoots(String[] sourceRoots)
 	{
 		List<String> roots = new ArrayList<String>();
 		for (String rt : sourceRoots)
 		{
 			if (rt != null)
 			{
 				roots.add(rt);
 			}
 		}
 		this.sourceRoots = roots.toArray(new String[roots.size()]);
 	}
 
 	@Override
 	public String[] getOriginalSourceRoots()
 	{
 		return originalRoots;
 	}
 
 	private void updateRoots()
 	{
 		Set<FileObject> roots = new HashSet<FileObject>();
 		for (ClassPath cp : projectSources)
 		{
 			Collections.addAll(roots, cp.getRoots());
 		}
 		for (ClassPath cp : customSources)
 		{
 			Collections.addAll(roots, cp.getRoots());
 		}
 		for (ClassPath cp : fanSources)
 		{
 			Collections.addAll(roots, cp.getRoots());
 		}
 		for (ClassPath cp : jdkSources)
 		{
 			Collections.addAll(roots, cp.getRoots());
 		}
 		/*for(ClassPath cp : globalSources)
 		{
 		Collections.addAll(roots, cp.getRoots());
 		}*/
 		synchronized (FanDebugHelper.class)
 		{
 			sourceRoots = new String[roots.size()];
 			int i = 0;
 			for (FileObject root : roots)
 			{
 				String rt = root.getPath();
 				sourceRoots[i] = rt;
 				i++;
 			}
 		}
 		//System.out.println(getClass().getName() + " Update roots: " + sourceRoots.length);
 	}
 
 	private class PathRegistryListener implements GlobalPathRegistryListener, PropertyChangeListener
 	{
 
 		public void pathsAdded(GlobalPathRegistryEvent event)
 		{
 			boolean changed = false;
 			for (ClassPath cp : event.getChangedPaths())
 			{
 				synchronized (FanDebugPathProvider.this)
 				{
 					globalSources.add(cp);
 				}
 				changed = true;
 			}
 			if (changed)
 			{
 				pcs.firePropertyChange(PROP_SOURCE_ROOTS, null, null);
 				updateRoots();
 			}
 		}
 
 		public void pathsRemoved(GlobalPathRegistryEvent event)
 		{
 			boolean changed = false;
 			for (ClassPath cp : event.getChangedPaths())
 			{
 				synchronized (FanDebugPathProvider.this)
 				{
 					globalSources.remove(cp);
 				}
 				changed = true;
 			}
 			if (changed)
 			{
 				pcs.firePropertyChange(PROP_SOURCE_ROOTS, null, null);
 				updateRoots();
 			}
 		}
 
 		public void propertyChange(PropertyChangeEvent evt)
 		{
 			// JDK sources changed
 			JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
 			boolean changed = false;
 			synchronized (FanDebugPathProvider.this)
 			{
 				jdkSources = new HashSet<ClassPath>();
 				for (JavaPlatform jp : platforms)
 				{
 					jdkSources.add(jp.getSourceFolders());
 				}
 				changed = true;
 			}
 			if (changed)
 			{
 				pcs.firePropertyChange(PROP_SOURCE_ROOTS, null, null);
 				updateRoots();
 			}
 		}
 	}
 
 	private static final class FileObjectComparator implements Comparator<FileObject>
 	{
 
 		public int compare(FileObject fo1, FileObject fo2)
 		{
 			String r1 = getRoot(fo1);
 			String r2 = getRoot(fo2);
 			if (r1 == null)
 			{
 				return -1;
 			}
 			if (r2 == null)
 			{
 				return +1;
 			}
 			return r1.compareTo(r2);
 		}
 	}
 }
