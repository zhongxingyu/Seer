 package cytoscape.weblaunch;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.ProgressMonitorInputStream;
 import javax.swing.UIManager;
 
 public class LaunchHelper {
 
 	private static String[] versions = new String[] {"Cytoscape_v3.0.2","Cytoscape_v3.0.1","Cytoscape_v3.0.0"};
 	private static String[] appUrls = new String[] { 
 			"http://apps.cytoscape.org/download/genomespace/2.0.0" // GenomeSpace
 		};
 
 	private static String win64InstallerUrl = "http://chianti.ucsd.edu/cytoscape-3.0.2/Cytoscape_3_0_2_windows_64bit.exe";
 	private static String win32InstallerUrl = "http://chianti.ucsd.edu/cytoscape-3.0.2/Cytoscape_3_0_2_windows_32bit.exe";
 	private static String unixInstallerUrl = "http://chianti.ucsd.edu/cytoscape-3.0.2/Cytoscape_3_0_2_unix.sh";
 	private static String macInstallerUrl = "http://chianti.ucsd.edu/cytoscape-3.0.2/Cytoscape_3_0_2_macos.dmg";
 
 	private static final String MAC = "mac os x";
 	private static final String WINDOWS = "windows";
 	private static final String PREFERRED_PATH = "preferred.path";
 
 	public static void main(String[] args) {
 		try {
 		    UIManager.setLookAndFeel(
 		        UIManager.getSystemLookAndFeelClassName());
 		}
 		catch(Exception e) {}
 		
 		String home = System.getProperty("user.home");
 		String os = System.getProperty("os.name").toLowerCase();
 		String arch = System.getProperty("os.arch");
 		String exe = getExecutable(os);
 		String path = checkForCytoscapeInstallation(os, arch, exe); 
 
 		if ( path == null )
 			return;
 		
 		File appDir = new File(home + File.separator + "CytoscapeConfiguration" + File.separator +
 				"3" + File.separator + "apps" + File.separator + "installed");
 		if(!appDir.exists())
 			appDir.mkdirs();
 		
 		if(!downloadApps(appDir))
 			return;
 		
 		// OK, all systems go!
 		File file = getFile(path,exe);
 		String[] command = createCommand(file,args,os);
 		launch(command, file.getParentFile());
 		
 		System.exit(0);
 	}
 
 	private static Process launch(final String[] command, final File path) {
 		try {
 			Runtime rt = Runtime.getRuntime();
 			Process p = rt.exec(command, null, path);
 			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
 			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
 			errorGobbler.start();
 			outputGobbler.start();
 			return p;
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null,
 							  "Error launching: " + command[0] + "\n\nCaused by\n" + e.getMessage(),
 							  "Error",
 							  JOptionPane.ERROR_MESSAGE);
 			return null;
 		}
 	}
 
 	private static void storePreferredPath(String path) {
 		try {
 			Properties props = new Properties();
 			props.setProperty(PREFERRED_PATH,path);
 			props.store( new FileOutputStream( getPropsFile() ), "Properties for GenomeSpace");
 		} catch (IOException ioe) { ioe.printStackTrace(System.err); }
 	}
 
 	private static String checkForCytoscapeInstallation(final String os, final String arch, final String exe) {
 		String path = getCytoscapeInstallationPath(os, exe);
 		if (path != null)
 			return path;
 		
 		int isInstalled = JOptionPane.showConfirmDialog(null, "Is Cytoscape installed on this computer?", "Select Cytoscape Installation Directory", JOptionPane.YES_NO_OPTION);	
 
 		if(isInstalled == JOptionPane.NO_OPTION) {
 			int install = JOptionPane.showConfirmDialog(null, "Do you want to install Cytoscape on this computer?", "Install Cytoscape", JOptionPane.YES_NO_OPTION);	
 			if(install == JOptionPane.YES_OPTION) {
 				while(!installCytoscape(os, arch)) {
 					int retry = JOptionPane.showConfirmDialog(null, "Cytoscape installation did not complete successfully. Retry?", "Install Cytoscape", JOptionPane.YES_NO_OPTION);
 					if(retry != JOptionPane.YES_OPTION)
 						return null;
 				}
 				path = getCytoscapeInstallationPath(os, exe);
 				if (path != null)
 					return path;
 			}
 			else
 				return null;
 		}
 		else if(isInstalled == JOptionPane.CLOSED_OPTION)
 			return null;
 
 		JOptionPane.showMessageDialog(null, "Please choose the directory where\nCytoscape is installed on your system.", "Select Cytoscape Installation Directory", JOptionPane.INFORMATION_MESSAGE);	
 		File file = selectCytoscapeInstallationDirectory();
 		if ( file != null )
 			path = file.getAbsolutePath();
 		else
 			return null;
 
 		while ( !executableExists(path,exe) ) {
 			int res = JOptionPane.showConfirmDialog(null, "We can't find the Cytoscape executable in the specified location.\nWould you like to try another location?", "Select Cytoscape Installation Directory", JOptionPane.YES_NO_OPTION);	
 
 			if ( res == JOptionPane.YES_OPTION ) {
 				file = selectCytoscapeInstallationDirectory();
 				if ( file != null )
 					path = file.getAbsolutePath();
 				else
 					return null;
 			} else {
 				return null;
 			}
 		}
 		storePreferredPath(path);
 		return path;
 	}
 
 	private static String[] createCommand(final File f, final String[] args, final String os) {
 		int i = -1;
 		String[] command;
 		if(os.startsWith(WINDOWS)) {
 			command = new String[ args.length + 3];
 			command[++i] = "cmd.exe";
 			command[++i] = "/c";
 		}
 		else {
 			command = new String[ args.length + 1];
 		}
 		command[++i] = f.getAbsolutePath();
 		for( String arg : args) {
 			command[++i] = arg;
 		}
 		return command;
 	}
 
 	private static boolean downloadApps(File appDir) {
 		for (String appUrl : appUrls) { 
 			try {
 				URL urlRef = new URL(appUrl);
 				URLConnection uc = urlRef.openConnection();
 				long lastModified = uc.getLastModified();
 
 				String path = uc.getURL().getPath();
 				String baseName = path.substring(path.lastIndexOf("/")+1);
 				String version = appUrl.substring(appUrl.lastIndexOf("/")+1);
 				String prefix = baseName.substring(0,baseName.lastIndexOf("."));
 				String suffix = baseName.substring(baseName.lastIndexOf(".")+1);
 				String name = prefix + "-v" + version + "." + suffix;
 				
 				boolean upToDate = false;
 				List<File> filesToDelete = new ArrayList<File>();
 				for(File file: appDir.listFiles()) {
 					if(!file.getName().startsWith(prefix))
 						continue;
					if(lastModified > file.lastModified() || !file.getName().endsWith(suffix))
 						filesToDelete.add(file);
 					else
 						upToDate = true;
 				}
 				if(!upToDate) {
 					File appFile = File.createTempFile(prefix, suffix, appDir);
 					if(!downloadURL(urlRef, appFile)) return false;
 					for(File file: filesToDelete)
 						file.delete();
 					appFile.renameTo(new File(appDir, name));
 				}
 			}
 			catch(Exception e) {
 				JOptionPane.showMessageDialog(null,
 						"Couldn't download plugin: " + appUrl + 
 						"\n\nCaused by\n" + e.getMessage(),
 						"Error", JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private static boolean downloadURL(final URL url, final File file) throws IOException {
 		FileOutputStream fos = null; 
 		boolean finished = false;
 		try {
 			URLConnection uc = url.openConnection();
 			int size = uc.getContentLength();
 			ProgressMonitorInputStream is = new ProgressMonitorInputStream(null, "Downloading " + file.getName() +"...", uc.getInputStream());
 			is.getProgressMonitor().setMaximum(size);
 			ReadableByteChannel rbc = Channels.newChannel(is);
 			fos = new FileOutputStream(file);
 			finished = (fos.getChannel().transferFrom(rbc, 0, size) == size);
 		} 
 		finally {
 			if (fos != null) {
 				fos.close();
 			}
 			if (!finished) {
 				file.delete();
 			}
 		}
 		return finished;
 	}
 
 	private static boolean executableExists(final String path, final String exe) {
 		if ( path == null || exe == null )
 			return false;
 		File file = getFile(path,exe); 
 		return file.exists(); 
 	}
 
 	private static File getFile(final String path, final String exe) {
 		return new File(path + File.separator + exe);
 	}
 
 	private static String getExecutable(final String os) {
 		String exe; 
 		if (os.startsWith(WINDOWS))	
 			exe = "cytoscape.exe";
 		else if (os.startsWith(MAC))
 			exe = "Cytoscape.app/Contents/MacOS/JavaApplicationStub";
 		else 
 			exe = "Cytoscape";
 
 		return exe;
 	}
 
 	private static String getCytoscapeInstallationPath(final String os, final String exe) {
 		String basePath = null;
 		if(os.startsWith(WINDOWS))
 			basePath = "C:" + File.separator + "Program Files";
 		else if(os.startsWith(MAC))  
 			basePath = File.separator + "Applications";
 		else						
 			basePath = System.getProperty("user.home");
 		
 		for ( String version : versions ) {	
 			String path = basePath + File.separator + version;
 			if ( executableExists(path,exe) ) {
 				return path;	
 			} 
 		}
 		
 		String path=getPreferredPath();
 		if(executableExists(path,exe))
 			return path;
 		else
 			return null;
 	}
 
 	private static File getPropsFile() {
 		File f = new File( System.getProperty("user.home") + File.separator + 
 		                   "CytoscapeConfiguration" +  File.separator + "genomespace-cytoscape.props" );
 		return f;
 	}
 
 	private static String getPreferredPath() {
 		try { 
 			File f = getPropsFile(); 
 			if ( !f.exists() )
 				return null;
 
 			Properties props = new Properties();
 			props.load( new FileInputStream(f) );
 			return props.getProperty(PREFERRED_PATH);
 
 		} catch (IOException ioe) { ioe.printStackTrace(System.err); }
 		return null;
 	}
 
 	
 
 	private static File selectCytoscapeInstallationDirectory() {
 		JFileChooser fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fc.setDialogTitle("Select Cytoscape Installation Directory");
 
 		int returnVal = fc.showOpenDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) 
 			return fc.getSelectedFile();
 		else
 			return null;
 	}
 	
 	private static boolean installCytoscape(final String os, final String arch) {
 		String installerUrl = null;
 		String volumePath = null;
 		if(os.startsWith(WINDOWS)) {
 			if(arch.contains("64")) 
 				installerUrl = win64InstallerUrl;
 			else
 				installerUrl = win32InstallerUrl;
 		}
 		else if(os.startsWith(MAC))
 			installerUrl = macInstallerUrl;
 		else
 			installerUrl = unixInstallerUrl;
 		String fileName = installerUrl.substring(installerUrl.lastIndexOf("/")+1);
 		try {
 			File installerFile = new File(System.getProperty("java.io.tmpdir"), fileName);
 			URL urlRef = new URL(installerUrl);
 			if(!downloadURL(urlRef, installerFile)) return false;
 			if(os.startsWith(MAC)) {
 				String[] command = {"/usr/bin/hdiutil", "attach", installerFile.getAbsolutePath()};
 				if(launch(command, installerFile.getParentFile()).waitFor() != 0)
 					return false;
 				int underscoreIndex = fileName.lastIndexOf("_");
 				volumePath = "/Volumes/" + fileName.substring(0, underscoreIndex+1);
 				installerFile = new File(volumePath, "Cytoscape Installer.app/Contents/MacOS/JavaApplicationStub");
 			}
 			else
 				installerFile.setExecutable(true);
 			String[] command = createCommand(installerFile, new String[0], os);
 			if(launch(command, installerFile.getParentFile()).waitFor() != 0)
 				return false;
 		}
 		catch(Exception e){
 			e.printStackTrace(System.err);
 			return false;
 		}
 		finally {
 			if(os.startsWith(MAC) && volumePath != null) {
 				String[] command = {"/usr/bin/hdiutil", "detach", volumePath};
 				launch(command, null);
 			}
 		}
 		return true;
 	}
 }
