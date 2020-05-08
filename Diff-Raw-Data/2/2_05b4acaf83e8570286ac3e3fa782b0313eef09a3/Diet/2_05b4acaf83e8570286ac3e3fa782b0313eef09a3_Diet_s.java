 package com.sysfera.godiet;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sysfera.godiet.command.init.InitForwardersCommand;
 import com.sysfera.godiet.command.init.util.XMLLoadingHelper;
 import com.sysfera.godiet.command.prepare.PrepareAgentsCommand;
 import com.sysfera.godiet.command.prepare.PrepareServicesCommand;
 import com.sysfera.godiet.command.start.StartAgentsCommand;
 import com.sysfera.godiet.command.start.StartForwardersCommand;
 import com.sysfera.godiet.command.start.StartServicesCommand;
 import com.sysfera.godiet.command.stop.StopAgentsCommand;
 import com.sysfera.godiet.command.stop.StopForwardersCommand;
 import com.sysfera.godiet.command.stop.StopServicesCommand;
 import com.sysfera.godiet.exceptions.CommandExecutionException;
 import com.sysfera.godiet.exceptions.remote.LaunchException;
 import com.sysfera.godiet.exceptions.remote.PrepareException;
 import com.sysfera.godiet.exceptions.remote.StopException;
 import com.sysfera.godiet.managers.DietManager;
 import com.sysfera.godiet.managers.ResourcesManager;
 import com.sysfera.godiet.managers.user.SSHKeyManager;
 import com.sysfera.godiet.model.DietResourceManaged;
 import com.sysfera.godiet.model.SoftwareController;
 import com.sysfera.godiet.model.SoftwareManager;
 import com.sysfera.godiet.model.factories.GodietAbstractFactory;
 import com.sysfera.godiet.model.states.ResourceState;
 import com.sysfera.godiet.model.states.ResourceState.State;
 import com.sysfera.godiet.model.validators.ForwarderRuntimeValidatorImpl;
 import com.sysfera.godiet.model.validators.LocalAgentRuntimeValidatorImpl;
 import com.sysfera.godiet.model.validators.MasterAgentRuntimeValidatorImpl;
 import com.sysfera.godiet.model.validators.OmniNamesRuntimeValidatorImpl;
 import com.sysfera.godiet.model.validators.SedRuntimeValidatorImpl;
 import com.sysfera.godiet.remote.RemoteConfigurationHelper;
 import com.sysfera.godiet.remote.ssh.ChannelManagerJsch;
 import com.sysfera.godiet.remote.ssh.RemoteAccessJschImpl;
 
 public class Diet {
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	private GodietAbstractFactory godietAbstractFactory;
 	private ResourcesManager rm;
 	private boolean configLoaded = false;
 	private boolean platfromLoaded = false;
 	private boolean dietLoaded = false;
 	private boolean servicesLaunched = false;
 	private boolean agentsLaunched = false;
 
 	public Diet() {
 		this.rm = new ResourcesManager();
 	}
 
 	// INIT
 	/**
 	 * Initialize configuration. Try to load
 	 * ${user.dir}/godiet/configuration.xml. If not found load
 	 * resources/configuration/configuration.xml
 	 * 
 	 */
 	public void initConfig() throws CommandExecutionException {
 		if (configLoaded)
 			new CommandExecutionException("Configuration already loaded");
 		InputStream inputStream = null;
 
 		// TryLoad {user.dir}/godiet/configuration.xml
 		String configFilePath = System.getProperty("user.home")
 				+ "/.godiet/configuration.xml";
 		try {
 			log.debug("Init Config: try to open file url: file:"
 					+ configFilePath);
 			URL url = new URL("file:" + configFilePath);
 
 			File f = null;
 			try {
 				f = new File(url.toURI());
 			} catch (URISyntaxException e) {
 				f = new File(url.getPath());
 			}
 
 			inputStream = new FileInputStream(f);
 		} catch (FileNotFoundException e) {
 			inputStream = getClass().getResourceAsStream(
 					"/configuration/configuration.xml");
 		} catch (Exception e) {
 			log.warn("Unable to open file " + configFilePath, e);
 			inputStream = getClass().getResourceAsStream(
 					"/configuration/configuration.xml");
 		}
 
 		if (inputStream == null) {
 			log.error("Fatal: Unable to load user config file and open default config file");
 			throw new CommandExecutionException("Unable to load configuration");
 		}
 		// TODO: springified this
 		XMLLoadingHelper.initConfig(rm, inputStream);
 		RemoteAccessJschImpl remoteJsch = new RemoteAccessJschImpl();
 		remoteJsch.setChannelManager(new ChannelManagerJsch());
 		this.rm.getUserManager().setRemoteAccessor(remoteJsch);
 		SoftwareController softwareController = new RemoteConfigurationHelper(
 				remoteJsch, rm.getGodietConfiguration()
 						.getGoDietConfiguration(), rm.getPlatformModel());
 		DietManager dietModel = rm.getDietModel();
 		godietAbstractFactory = new GodietAbstractFactory(softwareController,
 				new ForwarderRuntimeValidatorImpl(dietModel),
 				new MasterAgentRuntimeValidatorImpl(dietModel),
 				new LocalAgentRuntimeValidatorImpl(dietModel),
 				new SedRuntimeValidatorImpl(dietModel),
 				new OmniNamesRuntimeValidatorImpl(dietModel));
 
 		configLoaded = true;
 	}
 
 	public List<SSHKeyManager> getManagedKeys() {
 		return this.rm.getUserManager().getManagedKeys();
 
 	}
 
 	public void registerKey(SSHKeyManager key) {
 		this.rm.getUserManager().registerKey(key);
 	}
 
 	public void modifySshKey(SSHKeyManager sshkey, String privateKeyPath,
 			String publicKeyPath, String password) {
 
 		this.rm.getUserManager().modifySSHKey(sshkey, privateKeyPath,
 				publicKeyPath, password);
 	}
 
	public void initPlatform(URL url) throws CommandExecutionException {
 		if (!configLoaded)
 			new CommandExecutionException("Godiet not correctly configured");
 		InputStream inputStream;
 		try {
 			File f = null;
 			try {
 				f = new File(url.toURI());
 			} catch (URISyntaxException e) {
 				f = new File(url.getPath());
 			}
 
 			inputStream = new FileInputStream(f);
 		} catch (FileNotFoundException e) {
 			throw new CommandExecutionException("Unable to open file " + url, e);
 		}
 		XMLLoadingHelper.initPlatform(rm, inputStream);
 		platfromLoaded = true;
 	}
 
 	public void initDiet(URL url) throws CommandExecutionException {
 		if (!configLoaded)
 			new CommandExecutionException("Godiet not correctly configured");
 		if (!platfromLoaded)
 			new CommandExecutionException("Load platform first");
 		InputStream inputStream;
 		try {
 			File f = null;
 			try {
 				f = new File(url.toURI());
 			} catch (URISyntaxException e) {
 				f = new File(url.getPath());
 			}
 
 			inputStream = new FileInputStream(f);
 		} catch (FileNotFoundException e) {
 			throw new CommandExecutionException("Unable to open file " + url, e);
 		}
 		XMLLoadingHelper.initDiet(rm, inputStream, godietAbstractFactory);
 		dietLoaded = true;
 	}
 
 	// LAUNCH
 	public void launchServices() throws CommandExecutionException {
 		if (!dietLoaded)
 			new CommandExecutionException("Load diet description first");
 		try {
 			// Services commands
 			PrepareServicesCommand prepareCommand = new PrepareServicesCommand();
 			prepareCommand.setRm(rm);
 			StartServicesCommand launchServicesCommand = new StartServicesCommand();
 			launchServicesCommand.setRm(rm);
 			prepareCommand.execute();
 			launchServicesCommand.execute();
 		} finally {
 			servicesLaunched = true;
 		}
 	}
 
 	private boolean forwardersInitialize = false;
 
 	public void initForwarders() throws CommandExecutionException {
 		if (forwardersInitialize == false) {
 			InitForwardersCommand initForwardersCommand = new InitForwardersCommand();
 			initForwardersCommand.setRm(rm);
 			initForwardersCommand.setForwarderFactory(godietAbstractFactory);
 			initForwardersCommand.execute();
 
 			forwardersInitialize = true;
 		}
 	}
 
 	//
 	// public void relauch(String resourceId) throws CommandExecutionException {
 	// // Something must be launched
 	// if (!servicesLaunched)
 	// new CommandExecutionException("Nothing launched");
 	// StartSoftwareCommand sf = new StartSoftwareCommand();
 	// sf.setRm(rm);
 	// sf.setSoftwareId(resourceId);
 	//
 	// sf.execute();
 	//
 	// }
 
 
 	public void launchAgents() throws CommandExecutionException {
 		if (!servicesLaunched)
 			new CommandExecutionException("Launch diet services first");
 		try {
 
 			PrepareAgentsCommand prepareAgents = new PrepareAgentsCommand();
 			prepareAgents.setRm(rm);
 
 			StartForwardersCommand launchForwarders = new StartForwardersCommand();
 			launchForwarders.setRm(rm);
 
 			StartAgentsCommand startAgent = new StartAgentsCommand();
 			startAgent.setRm(rm);
 
 			// execute
 
 			prepareAgents.execute();
 
 			launchForwarders.execute();
 			startAgent.execute();
 		} finally {
 			agentsLaunched = true;
 		}
 	}
 
 	// STOP
 	public void stopAgents() throws CommandExecutionException {
 		if (!agentsLaunched)
 			new CommandExecutionException("Agents doesn't running");
 		StopAgentsCommand stopAgents = new StopAgentsCommand();
 		stopAgents.setRm(rm);
 		StopForwardersCommand stopForwarders = new StopForwardersCommand();
 		stopForwarders.setRm(rm);
 
 		stopAgents.execute();
 		stopForwarders.execute();
 		agentsLaunched = false;
 	}
 
 	public void stopServices() throws CommandExecutionException {
 		if (!servicesLaunched)
 			new CommandExecutionException("Diet services doesn't running");
 		StopServicesCommand stopServicesCommand = new StopServicesCommand();
 		stopServicesCommand.setRm(rm);
 		stopServicesCommand.execute();
 		servicesLaunched = false;
 	}
 
 	public void stopSoftware(String softwareId) throws PrepareException,
 			StopException, CommandExecutionException {
 
 		SoftwareManager software = this.rm.getDietModel().getManagedSoftware(
 				softwareId);
 		if (software == null)
 			throw new CommandExecutionException("Unable to find " + softwareId);
 
 		software.stop();
 	}
 
 	public ResourcesManager getRm() {
 		return rm;
 	}
 
 	public void setRm(ResourcesManager rm) {
 		this.rm = rm;
 	}
 
 }
