 package com.evanreidland.e.builder;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.Vector;
 import java.util.jar.JarOutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

 import com.evanreidland.e.ftp.DownloadInfo;
 import com.evanreidland.e.launcher.GravityLauncherGUI;
 import com.evanreidland.e.launcher.launcherfunctions;
 import com.evanreidland.e.script.Function;
 import com.evanreidland.e.script.Stack;
 import com.evanreidland.e.script.Value;
 import com.evanreidland.e.script.Variable;
 
 public class Project
 {
 	public static Logger logger = Logger.getLogger("com.evanreidland.e");
 	private Vector<String> sourceFiles, classPaths;
 	private String name;
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public void add(String source)
 	{
 		sourceFiles.add(source);
 	}
 	
 	public void addClassPath(String classPath)
 	{
 		classPaths.add(classPath);
 	}
 	
 	public void add(Vector<String> sourceFiles)
 	{
 		this.sourceFiles.addAll(sourceFiles);
 	}
 	
 	public void removeEntriesWith(String contains)
 	{
 		int i = 0;
 		while (i < sourceFiles.size())
 		{
 			String str = sourceFiles.get(i);
 			if (str.contains(contains))
 			{
 				sourceFiles.remove(i);
 				GravityLauncherGUI.Log("Removed: " + str);
 			}
 			else
 			{
 				i++;
 			}
 		}
 	}
 	
 	public static class LogStream extends PrintWriter
 	{
 		private Logger logger;
 		
 		public void println(Object msg)
 		{
 			println(msg.toString());
 		}
 		
 		public void println(String msg)
 		{
 			logger.log(Level.INFO, msg.toString());
 		}
 		
 		public LogStream(Logger logger, String fout) throws Exception
 		{
 			super(fout);
 			this.logger = logger;
 		}
 	}
 	
 	private static String getDefaultDirectory()
 	{
 		String OS = System.getProperty("os.name").toUpperCase();
 		if (OS.contains("WIN"))
 			return System.getenv("APPDATA");
 		else if (OS.contains("MAC"))
 			return System.getProperty("user.home")
 					+ "/Library/Application Support";
 		else if (OS.contains("NUX"))
 			return System.getProperty("user.home");
 		return System.getProperty("user.dir");
 	}
 	
 	public static String defaultDirectory()
 	{
 		return getDefaultDirectory().replace('\\', '/');
 	}
 	
 	public boolean buildClasses(String outputFolder)
 	{
 		int p = 2;
 		String[] args = new String[sourceFiles.size() + p];
 		
 		for (int i = 0; i < sourceFiles.size(); i++)
 		{
 			args[i] = sourceFiles.get(i);
 		}
 		
 		p = sourceFiles.size();
 		args[p] = "-classpath";
 		p++;
 		args[p] = "";
 		
 		// TODO fix. Something about this is broken. If there is more than one
 		// classpath, it causes the builder to just not run.
 		for (int i = 0; i < classPaths.size(); i++)
 		{
 			args[p] += classPaths.get(i) + ";";
 		}
 		
 		// args[p] += ".";
 		
 		GravityLauncherGUI.Log("Args:");
 		
 		for (int i = 0; i < args.length; i++)
 		{
 			GravityLauncherGUI.Log("\\" + args[i]);
 		}
 		try
 		{
 			GravityLauncherGUI.Log("Building " + args.length + " files.");
			JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
			
			return javac.run(System.in, System.out, System.out, args) == 0;
			
			// com.sun.tools.javac.Main.compile(args, new PrintWriter(
			// new FileWriter(outputFolder + "/buildlog.txt")));
			// return true.
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	// Note: This assumes the class files were put into a src/ folder.
 	public static boolean buildJar(String dir, String outputFile)
 	{
 		try
 		{
 			dir = dir.replace('\\', '/');
 			if (!dir.endsWith("/"))
 				dir += "/";
 			JarOutputStream jar = new JarOutputStream(new FileOutputStream(
 					outputFile));
 			writeToJar(jar, new File(dir).listFiles());
 			jar.close();
 			return true;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	private static void writeToJar(JarOutputStream jar, File[] dir)
 			throws Exception
 	{
 		byte[] buff = new byte[1024];
 		for (int i = 0; i < dir.length; i++)
 		{
 			if (dir[i].isFile() && dir[i].getName().endsWith(".class"))
 			{
 				dir[i].deleteOnExit();
 				String item = dir[i].getCanonicalPath().replace('\\', '/');
 				int end = item.lastIndexOf("src/");
 				if (end >= 0)
 				{
 					item = item.substring(end + 4, item.length());
 					System.out.println("Class: " + item);
 					jar.putNextEntry(new ZipEntry(item));
 					FileInputStream in = new FileInputStream(dir[i]);
 					while ((end = in.read(buff)) > 0)
 					{
 						jar.write(buff, 0, end);
 					}
 				}
 			}
 			else if (dir[i].isDirectory())
 			{
 				if (!dir[i].getName().equals("bin"))
 				{
 					dir[i].deleteOnExit();
 				}
 				writeToJar(jar, dir[i].listFiles());
 			}
 		}
 	}
 	
 	public Project(String name)
 	{
 		this.name = name;
 		
 		sourceFiles = new Vector<String>();
 		classPaths = new Vector<String>();
 	}
 	
 	public static Project selectedProject = null;
 	
 	public static class ProjectNew extends Function
 	{
 		public Value Call(Stack args)
 		{
 			if (args.size() > 0)
 			{
 				selectedProject = new Project(args.at(0).toString());
 				return new Value("Project created and selected, \""
 						+ args.at(0).toString() + "\".");
 			}
 			else
 			{
 				String name = "project";
 				selectedProject = new Project(name);
 				return new Value("Empty arguments. Created project \"" + name
 						+ "\".");
 			}
 			
 		}
 		
 		public ProjectNew()
 		{
 			super("project.new");
 		}
 	}
 	
 	public static class ProjectAdd extends Function
 	{
 		public Value Call(Stack args)
 		{
 			if (args.size() > 0)
 			{
 				if (selectedProject != null)
 				{
 					selectedProject.add(args.at(0).toString());
 					return new Value("Added: " + args.at(0).toString());
 				}
 				else
 				{
 					return new Value("No project selected. Cannot execute \""
 							+ getName() + "\"");
 				}
 			}
 			else
 			{
 				return new Value("Not enough arguments. Format: " + getName()
 						+ " <sourcefile>");
 			}
 			
 		}
 		
 		public ProjectAdd()
 		{
 			super("project.+src");
 		}
 	}
 	
 	public static class ProjectAddDownload extends Function
 	{
 		public Value Call(Stack args)
 		{
 			if (selectedProject != null)
 			{
 				DownloadInfo lastDownload = launcherfunctions.lastDownload;
 				if (lastDownload.success)
 				{
 					GravityLauncherGUI.Log("Setting up to add last download ("
 							+ lastDownload.entryNames.size() + " entries).");
 					
 					String to = args.context.get("path").toString();
 					if (to.isEmpty())
 						to = "./build/bin/";
 					
 					if (!to.endsWith("/"))
 						to += "/";
 					
 					GravityLauncherGUI.Log("Base directory: " + to);
 					
 					if (lastDownload.entryNames.size() > 0)
 					{
 						System.out.println("Head entry: "
 								+ lastDownload.entryNames.firstElement());
 						
 						args.context.add(new Variable("dl.hentry",
 								lastDownload.entryNames.firstElement()));
 					}
 					
 					for (int i = 0; i < lastDownload.entryNames.size(); i++)
 					{
 						String entry = lastDownload.entryNames.get(i);
 						if (entry.endsWith(".java"))
 						{
 							GravityLauncherGUI.Log("Added " + entry);
 							selectedProject.add(to + entry);
 						}
 					}
 					
 					return new Value();
 				}
 				else
 				{
 					return new Value(
 							"Last download was unsuccessful. Did not add files.");
 				}
 			}
 			else
 			{
 				return new Value("No project selected. Cannot execute \""
 						+ getName() + "\"");
 			}
 			
 		}
 		
 		public ProjectAddDownload()
 		{
 			super("project.+dl");
 		}
 	}
 	
 	public static class ProjectAddPath extends Function
 	{
 		public Value Call(Stack args)
 		{
 			if (args.size() > 0)
 			{
 				if (selectedProject != null)
 				{
 					selectedProject.addClassPath(args.at(0).toString());
 					return new Value("Added source path: "
 							+ args.at(0).toString());
 				}
 				else
 				{
 					return new Value("No project selected. Cannot execute \""
 							+ getName() + "\"");
 				}
 			}
 			else
 			{
 				return new Value("Not enough arguments. Format: " + getName()
 						+ " <classpath>");
 			}
 		}
 		
 		public ProjectAddPath()
 		{
 			super("project.+lib");
 		}
 	}
 	
 	public static class ProjectRemove extends Function
 	{
 		public Value Call(Stack args)
 		{
 			if (args.size() > 0)
 			{
 				if (selectedProject != null)
 				{
 					String str = args.at(0).toString();
 					selectedProject.removeEntriesWith(str);
 					return new Value("Removed entries that contained \"" + str
 							+ "\".");
 				}
 				else
 				{
 					return new Value("No project selected. Cannot execute \""
 							+ getName() + "\"");
 				}
 			}
 			else
 			{
 				return new Value("Not enough arguments. Format: " + getName()
 						+ " <contains>");
 			}
 		}
 		
 		public ProjectRemove()
 		{
 			super("project.-src");
 		}
 	}
 	
 	public static class ProjectBuild extends Function
 	{
 		public Value Call(Stack args)
 		{
 			if (selectedProject != null)
 			{
 				
 				String to = args.context.get("path").toString();
 				if (to.isEmpty())
 					to = "./build/bin/";
 				
 				if (!to.endsWith("/"))
 					to += "/";
 				
 				boolean built = selectedProject.buildClasses(to);
 				return new Value((built ? "Built" : "Failed to build")
 						+ " project \"" + selectedProject.getName()
 						+ "\" to \"" + to + "\".");
 			}
 			else
 			{
 				return new Value("No project selected. Cannot execute \""
 						+ getName() + "\"");
 			}
 		}
 		
 		public ProjectBuild()
 		{
 			super("project.build");
 		}
 	}
 	
 	public static class ProjectJar extends Function
 	{
 		public Value Call(Stack args)
 		{
 			if (args.size() > 0)
 			{
 				String to = "";
 				if (args.size() > 1)
 				{
 					to = args.at(1).toString();
 				}
 				else
 				{
 					to = args.context.get("path").toString();
 				}
 				if (to.isEmpty())
 					to = "./build/bin/";
 				
 				if (!to.endsWith("/"))
 					to += "/";
 				
 				System.out.println("Jar file: " + args.at(0).toString());
 				System.out.println("Jar path: " + to);
 				
 				boolean built = Project.buildJar(to, args.at(0).toString());
 				return new Value("Jarred: " + built);
 			}
 			return new Value("Not enough arguments. Format: " + getName()
 					+ " <jar file> " + "<|path to build>");
 		}
 		
 		public ProjectJar()
 		{
 			super("jar");
 		}
 	}
 	
 }
