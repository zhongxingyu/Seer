 /*
  * Thibaut Colar Aug 14, 2009
  */
 package net.colar.netbeans.fan.project;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Properties;
 import org.netbeans.api.project.ProjectManager;
 import org.openide.filesystems.FileAttributeEvent;
 import org.openide.filesystems.FileChangeListener;
 import org.openide.filesystems.FileEvent;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileRenameEvent;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.Exceptions;
 import org.openide.util.Mutex;
 import org.openide.util.MutexException;
 
 /**
  * Properties for Fan Pod/Project
  * @author thibautc
  */
 public class FanProjectProperties
 {
 
 	public static final String DEFAULT_MAIN_METHOD = "Main.main";
 	public static final String DEFAULT_BUILD_TARGET = "";
 	public static final String PROJ_PROPS_PATH = "nbproject/project.properties";
 	public static final String PRIVATE_PROPS_PATH = "nbproject/private/private.properties";
 	public static final String MAIN_METHOD = "pod.main.method";
 	public static final String BUILD_TARGET = "build.target";
 	private final FanProject project;
 	private volatile String mainMethod;
 	private volatile String buildTarget;
 	private File propFile = null;
 
 	public FanProjectProperties(FanProject project)
 	{
 		assert project != null;
 		this.project = project;
		propFile = new File(project.getProjectDirectory().getPath() + File.separator + PROJ_PROPS_PATH);
 		load();
 		FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(propFile));
 		fo.addFileChangeListener(new FanPropsListener());
 	}
 
 	public FanProject getProject()
 	{
 		return this.project;
 	}
 
 	public String getMainMethod()
 	{
 		return mainMethod;
 	}
 
 	public void setMainMethod(String method)
 	{
 		this.mainMethod = method;
 	}
 
 	public void save()
 	{
 		try
 		{
 			ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>()
 			{
 
 				public Void run() throws IOException
 				{
 					saveProperties(propFile);
 					return null;
 				}
 			});
 			ProjectManager.getDefault().saveProject(project);
 		} catch (MutexException e)
 		{
 			Exceptions.printStackTrace((IOException) e.getException());
 		} catch (IOException ex)
 		{
 			Exceptions.printStackTrace(ex);
 		}
 	}
 
 	private void saveProperties(File dest) throws IOException
 	{
 		Properties projectProperties = new Properties();
 
 		if (mainMethod != null)
 		{
 			projectProperties.put(MAIN_METHOD, mainMethod);
 		}
 
 		if (buildTarget != null)
 		{
 			projectProperties.put(BUILD_TARGET, buildTarget);
 		}
 		dest.getParentFile().mkdirs();
 		FileOutputStream fos = new FileOutputStream(dest);
 		projectProperties.store(fos, "Fantom Project Properties");
 		fos.close();
 	}
 
 	private void load()
 	{
 		if (!propFile.exists())
 		{
 			propFile.getParentFile().mkdirs();
 			try
 			{
 				propFile.createNewFile();
 			} catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		FileInputStream fis = null;
 		try
 		{
 			fis = new FileInputStream(propFile);
 			Properties props = new Properties();
 			props.load(fis);
 			mainMethod = props.getProperty(MAIN_METHOD, DEFAULT_MAIN_METHOD);
 			buildTarget = props.getProperty(BUILD_TARGET, DEFAULT_BUILD_TARGET);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		} finally
 		{
 			if (fis != null)
 			{
 				try
 				{
 					fis.close();
 				} catch (IOException e2)
 				{
 				}
 			}
 		}
 
 	}
 
 	public void setBuildTarget(String target)
 	{
 		buildTarget = target;
 	}
 
 	public String getBuildTarget()
 	{
 		return buildTarget;
 	}
 
 	public static FanProjectProperties getProperties(FanProject prj)
 	{
 		return prj.getLookup().lookup(FanProjectProperties.class);
 	}
 
 	public static void createFromScratch(Hashtable<String, String> hash, File dest)
 	{
 		Properties props = new Properties();
 		props.putAll(hash);
 		try
 		{
 			FileOutputStream fos = new FileOutputStream(dest);
 			props.store(fos, "Fantom Project Properties");
 			fos.close();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		//FanProjectProperties curProps=project.getLookup().lookup(FanProjectProperties.class);
 
 	}
 
 	// listener for props file changes
 	private class FanPropsListener implements FileChangeListener
 	{
 
 		public void fileChanged(FileEvent event)
 		{
 			//System.out.println("Props file changed :" + event.getFile().getPath());
 			// refresh
 			load();
 		}
 		// don't care about the rest
 
 		public void fileDataCreated(FileEvent arg0)
 		{
 		}
 
 		public void fileDeleted(FileEvent arg0)
 		{
 		}
 
 		public void fileRenamed(FileRenameEvent arg0)
 		{
 		}
 
 		public void fileFolderCreated(FileEvent arg0)
 		{
 		}
 
 		public void fileAttributeChanged(FileAttributeEvent event)
 		{
 			//System.out.println("Props file attr changed :" + event.getFile().getPath() + event.toString());
 		}
 	}
 }
 
 
