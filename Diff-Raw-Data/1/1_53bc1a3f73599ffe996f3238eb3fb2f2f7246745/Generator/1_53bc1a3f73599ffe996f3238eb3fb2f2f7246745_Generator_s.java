 package org.hazelwire.main;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.hazelwire.modules.Module;
 import org.hazelwire.modules.ModuleSelector;
 import org.hazelwire.virtualmachine.InstallScriptGenerator;
 import org.hazelwire.virtualmachine.SSHConnection;
 import org.hazelwire.virtualmachine.VMHandler;
 import org.hazelwire.xml.ConfigGenerator;
 import org.hazelwire.xml.ManifestGenerator;
 
 /**
  * This class manages the main purpose of the software, which is generating the virtualmachine, it doesn't actually implement the low-level
  * functionality itself but manages everything that is required to generate vm's on a high level. Therefore it also manages the ModuleSelector.
  * 
  * Because this is such an important class that has high level responsibility over everything that goes on in the backend it's implemented as singleton.
  * @author Tim Strijdhorst
  * 
  * It is absolutely vital that you initiate this class first because it will setup all the configuration
  *
  */
 public class Generator
 {
 	private static Generator instance;
 	private static String INSTALLNAME = "install.sh";
 	private static String MANIFESTFILENAME = "manifest.xml"; //you should probably not change this...
 	private static boolean DELETEVM = true;
 	private ModuleSelector moduleSelector;
 	private InterfaceOutput tui;
 	Configuration config;
 	private char fileSeperator;
 	VMHandler vmHandler;
 	private boolean keepGenerating;
 	
 	public static synchronized Generator getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new Generator();
 			
 			try
 			{
 				instance.getModuleSelector().init();
 			}
 			catch(Exception e)
 			{
 				instance.getTui().println("ERROR: Cannot load the configuration, exiting."); //this is pretty ugly... have to find a better solution
 				System.exit(0);
 			}
 		}
 		
 		return instance;
 	}
 	
 	private Generator()
 	{
 		keepGenerating = false;
 		try
 		{		
 	    	if(System.getProperty("os.name").equals("windows")) setFileSeperator('\\');
 	    	else setFileSeperator('/');
 			
 			//Initialize configuration
 	    	config = Configuration.getInstance();
 	    	config.loadDefaultProperties("config"+this.getFileSeperator()+"defaultProperties");
 	    	config.loadUserProperties();
 		}
 		catch(IOException e)
 		{
 			tui.println("ERROR: Cannot load the configuration, exiting.");
 			System.exit(0);
 		}
 		
 		moduleSelector = new ModuleSelector();
 	}
 
 	public ModuleSelector getModuleSelector()
 	{
 		return moduleSelector;
 	}
 	
 	public ArrayList<String> getAvailableModules()
 	{
 		HashMap<Integer,Module> availableModules = moduleSelector.getAvailableModules();
 		Iterator<Integer> iterator = availableModules.keySet().iterator();
 		ArrayList<String> descriptions = new ArrayList<String>();
 		
 		while(iterator.hasNext())
 		{
 			descriptions.add(availableModules.get(iterator.next()).toString());
 		}
 		
 		return descriptions;
 	}
 	
 	public ArrayList<String> getSelectedModules()
 	{
 		HashMap<Integer,Module> enabledModules = moduleSelector.getSelectedModules();
 		Iterator<Integer> iterator = enabledModules.keySet().iterator();
 		ArrayList<String> descriptions = new ArrayList<String>();
 		
 		while(iterator.hasNext())
 		{
 			descriptions.add(enabledModules.get(iterator.next()).toString());
 		}
 		
 		return descriptions;
 	}
 	
 	public void shutDown(boolean output, boolean export) throws Exception
 	{
     	if(vmHandler != null && vmHandler.checkIfImported())
     	{
 	    	if(output) tui.println("Stopping VM");
 	    	vmHandler.stopVM();
 	    	if(output) tui.setProgress(90);
 	    	if(output) tui.println("Removing port forwarding");
 	    	vmHandler.removeForward("ssh");
 	    	if(output) tui.setProgress(92);
 	    	if(export)
 	    	{
 	    		FileName exportPath = new FileName(Configuration.getInstance().getVMExportPath(),Generator.getInstance().getFileSeperator(),'.');
 	    		findUniqueExportName(exportPath);
 	    		
 	    		if(output) tui.println("Exporting vm to: "+exportPath.getFullPath());
 	    		
 	    		vmHandler.exportVM(exportPath.getFullPath());
 	    	}
 	    	if(output) tui.setProgress(98);
 	    	if(output) tui.println("Unregistering vm from virtualbox");
 	    	if(output) tui.setProgress(99);
 	    	vmHandler.unregisterVM(DELETEVM);
 	    	if(output) tui.setProgress(100);
     	}
     	
     	this.keepGenerating = false; //this actually should have never been true unless you are very quick with pushing cancel
 	}
 	
 	/**
 	 * @todo it should wait with stopping the vm until the callback is done
 	 */
 	public void generateVM() throws Exception
 	{
     	try
     	{
     		this.keepGenerating = true;
 			vmHandler = new VMHandler(config.getVirtualBoxPath(),config.getVMName(), config.getVMPath(), true);
 	    	if(!vmHandler.checkIfImported())
 	    	{
 	    		tui.println("Importing VM");
 	    		vmHandler.importAndDiscover();
 	    	}
 	    	
 	    	tui.setProgress(10);
 	    	tui.println("Adding portforward for ssh: "+config.getSSHHostPort()+" to "+config.getSSHGuestPort()+" in the vm");
 	    	vmHandler.addForward("ssh", config.getSSHHostPort(), config.getSSHGuestPort(), "TCP"); //add forward so we can reach the VM
 
 	    	tui.setProgress(15);
 	    	tui.println("Starting VM");
 	    	vmHandler.startVM();    	
 
 	    	if(vmHandler.discoverBootedVM(config.getSSHHostPort())) //wait for it to actually be booted so we can use the SSH service
 	    	{
 	    		SSHConnection ssh = new SSHConnection("localhost",config.getSSHHostPort(),config.getSSHUsername(),config.getSSHPassword());
 	    		String externalPath = config.getExternalScriptDirectory()+INSTALLNAME;
 
 		    	tui.setProgress(20);
 		    	tui.println("Creating directories");
 		    	prepareVM(ssh);
 	    	
 		    	tui.setProgress(25);
 		    	tui.println("Uploading modules");
 		    	uploadModules(ssh);
 
 		    	tui.setProgress(50);
 		    	tui.println("Generating installation script");
 		    	generateInstallScript(config.getOutputDirectory()+INSTALLNAME);
 	    	
 		    	tui.setProgress(55);
 		    	tui.println("Generating manifest");
 		    	generateManifest(config.getOutputDirectory()+MANIFESTFILENAME);
 
 		    	tui.setProgress(60);
 		    	tui.println("Uploading installation script");
 		    	uploadInstallScript(ssh, config.getOutputDirectory()+INSTALLNAME,externalPath);
 	    	
 		    	tui.setProgress(65);
 		    	tui.println("Executing installation script");
 		    	executeInstallScript(ssh,externalPath);
 	    	
 		    	tui.setProgress(85);
 		    	shutDown(true,true);
 		    	tui.println("Succesfully created VM");
 	    	}
 	    	else
 	    	{
 	    		throw new Exception("Could not connect to the virtualmachine");
 	    	}
     	}
     	catch(InterruptedException e)
     	{
     		tui.println("Generation process canceled by user.");
     		this.shutDown(true, false);
     	}
     	catch(Exception e)
     	{
     		tui.println(e.getMessage());
     		this.shutDown(true, false);
     	}
 	}
 	
 	public void uploadModules(SSHConnection ssh) throws Exception
 	{
 		ArrayList<Module> modules = moduleSelector.getMarkedModules();
 		Iterator<Module> iterate = modules.iterator();
 		String configPath = config.getTempDirectory()+"config.xml";
 		
 		while(iterate.hasNext())
 		{
 			Module tempModule = iterate.next();
 			String externalDir = config.getExternalModuleDirectory()+tempModule.getFileNameWithoutExtension()+"/";
 			
 			try
 			{
 				ConfigGenerator.saveConfigToDisk(tempModule, configPath);
 				
 				ssh.executeRemoteCommand("mkdir "+externalDir);
 				ssh.scpUpload(tempModule.getFullPath(),externalDir+tempModule.getFileName());
 				ssh.scpUpload(configPath,externalDir+"config.xml");
 			}
 			catch(Exception e)
 			{
 				tui.println("ERROR: something went wrong while transferring the modules to the VM");
 				e.printStackTrace();
 			}
 		}
 		
 		try
 		{
 			File removeFile = new File(configPath);
 			removeFile.delete();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new Exception("ERROR: something went wrong while trying to delete the generated configfile"+e.getMessage());
 		}
 	}
 	
 	/**
 	 * Makes sure all the directories that are needed are indeed created.
 	 * It doesn't matter whether they already exist or not this is just for safety.
 	 * @throws Exception 
 	 */
 	public void prepareVM(SSHConnection ssh) throws Exception
 	{
 		try
 		{
 			Configuration config = Configuration.getInstance();
 			ssh.executeRemoteCommand("mkdir "+config.getExternalModuleDirectory());
 			ssh.executeRemoteCommand("mkdir "+config.getExternalScriptDirectory());
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new Exception("ERROR: something went wrong while preparing the directory structure on the VM. Message: "+e.getMessage());
 		}
 	}
 	
 	public void uploadInstallScript(SSHConnection ssh, String scriptPathLocal, String scriptPathExternal) throws Exception
 	{
 		try
 		{
 			ssh.scpUpload(scriptPathLocal, scriptPathExternal);
 			new File(scriptPathLocal).delete(); //delete the local install file
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new Exception("ERROR: something went wrong while transferring the installscript to the VM");
 		}
 	}
 	
 	public void executeInstallScript(SSHConnection ssh, String scriptPathExternal) throws Exception
 	{
 		try
 		{
 			ssh.executeRemoteCommand("chmod u+x "+scriptPathExternal); //make it executable
 			ssh.executeRemoteCommand(scriptPathExternal); //run the script
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw new Exception("ERROR: something went wrong while executing the installscript");
 		}
 	}
 	
 	public void generateInstallScript(String filePath) throws Exception
 	{
 		try
 		{
 			InstallScriptGenerator.saveScriptToDisk(filePath);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new Exception("ERROR: something went wrong while generating the installscript");
 		}
 	}
 	
 	public void generateManifest(String filePath) throws Exception
 	{
 		try
 		{
 			ManifestGenerator.saveManifestToDisk(filePath);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new Exception("ERROR: something went wrong while generating the manifest file");
 		}
 	}
 	
 	public void findUniqueExportName(FileName exportPath)
 	{
 		boolean unique = false;
 		String fileName = exportPath.getFileName();
 		int i=2;
 		while(!unique)
 		{
 			File tempFile = new File(exportPath.getFullPath());
 			unique = !tempFile.exists();
 			if(!unique) exportPath.setFileName(fileName+i++);
 		}
 	}
 
 	public void setFileSeperator(char fileSeperator)
 	{
 		this.fileSeperator = fileSeperator;
 	}
 
 	public char getFileSeperator()
 	{
 		return fileSeperator;
 	}
 
 	public InterfaceOutput getTui()
 	{
 		return tui;
 	}
 
 	public void setTui(InterfaceOutput tui)
 	{
 		this.tui = tui;
 	}
 
 	public synchronized boolean isKeepGenerating() {
 		return keepGenerating;
 	}
 
 	public synchronized void setKeepGenerating(boolean keepGenerating) {
 		this.keepGenerating = keepGenerating;
 	}
 }
