 package com.sysfera.godiet.model.factories;
 
 import com.sysfera.godiet.exceptions.DietResourceCreationException;
 import com.sysfera.godiet.model.DietResourceManaged;
 import com.sysfera.godiet.model.SoftwareController;
 import com.sysfera.godiet.model.SoftwareManager;
 import com.sysfera.godiet.model.generated.Forwarder;
 import com.sysfera.godiet.model.generated.Forwarders;
 import com.sysfera.godiet.model.generated.ObjectFactory;
 import com.sysfera.godiet.model.generated.OmniNames;
 import com.sysfera.godiet.model.generated.Options;
 import com.sysfera.godiet.model.generated.Options.Option;
 import com.sysfera.godiet.model.utils.ResourceUtil;
 import com.sysfera.godiet.model.validators.RuntimeValidator;
 
 /**
  * Managed Forwarder factory
  * 
  * @author phi
  * 
  */
 public class ForwardersFactory {
 
 	private final SoftwareController softwareController;
 
 	private final RuntimeValidator forwarderValidator;
 
 	public static enum ForwarderType {
 		CLIENT("CLIENT"), SERVER("SERVER");
 		public final String label;
 
 		private ForwarderType(String label) {
 			this.label = label;
 		}
 
 	}
 
 
 
 	public ForwardersFactory(SoftwareController softwareController,RuntimeValidator forwarderValidator) {
 		this.softwareController = softwareController;
 		this.forwarderValidator = forwarderValidator;
 	}
 	
 
 	/**
 	 * Create a managed diet resource. Check description validity and add
 	 * default parameters if needed.
 	 * 
 	 * @param forwarders
 	 *            Forwarders description
 	 * @return The Managed forwarders. DietResourceManaged[0] the client and
 	 *         DietResourceManaged[1] the server
 	 * @throws DietResourceCreationException
 	 *             if resource not plugged
 	 */
 	public DietResourceManaged[] create(Forwarders forwarders, OmniNames omniNamesClient, OmniNames omniNamesServer)
 			throws DietResourceCreationException {
 
 		DietResourceManaged[] forwardersManager = new DietResourceManaged[2];
 		DietResourceManaged clientForwarderManager = new DietResourceManaged(softwareController,forwarderValidator);
 		DietResourceManaged serverForwarderManager = new DietResourceManaged(softwareController,forwarderValidator);
 
 		clientForwarderManager.setManagedSoftware(forwarders.getClient());
 		serverForwarderManager.setManagedSoftware(forwarders.getServer());
 
 		settingConfigurationOptions(clientForwarderManager,
 				serverForwarderManager);
 		buildForwarderCommand(clientForwarderManager, serverForwarderManager,omniNamesClient,omniNamesServer);
 
 		forwardersManager[0] = clientForwarderManager;
 		forwardersManager[1] = serverForwarderManager;
 		return forwardersManager;
 	}
 
 	/**
 	 * Init default value
 	 * 
 	 * @param forwarders
 	 * @throws DietResourceCreationException
 	 *             if resource not plugged
 	 */
 	private void settingConfigurationOptions(DietResourceManaged managedClient,
 			DietResourceManaged managedServer)
 			throws DietResourceCreationException {
 		if (managedClient.getPluggedOn() == null
 				|| managedServer.getPluggedOn() == null) {
 			throw new DietResourceCreationException(managedClient
 					.getSoftwareDescription().getId()
 					+ " or "
 					+ managedServer.getSoftwareDescription().getId()
 					+ " not plugged on physical resource");
 		}
 		/**
 		 * Client
 		 */
 		{
 			ObjectFactory factory = new ObjectFactory();
 			Options opts = factory.createOptions();
 
 			Option accept = factory.createOptionsOption();
 			accept.setKey("accept");
 			accept.setValue(".*");
 			Option reject = factory.createOptionsOption();
 			reject.setKey("reject");
 			reject.setValue("localhost");
 			opts.getOption().add(accept);
 			opts.getOption().add(reject);
 			managedClient.getSoftwareDescription().setCfgOptions(opts);
 		}
 		/**
 		 * Server
 		 */
 		{
 			ObjectFactory factory = new ObjectFactory();
 			Options opts = factory.createOptions();
 
 			Option accept = factory.createOptionsOption();
 			accept.setKey("accept");
 			accept.setValue(".*");
 			Option reject = factory.createOptionsOption();
 			reject.setKey("reject");
 			reject.setValue("localhost");
 			opts.getOption().add(accept);
 			opts.getOption().add(reject);
 			managedServer.getSoftwareDescription().setCfgOptions(opts);
 		}
 	}
 
 	/**
 	 * Build the diet forwarder running command
 	 * OMNIORB_CONFIG={scratch_runtime}/{omniNamesId}.cfg TODO: What's the hell
 	 * 
 	 * @param softManaged
 	 * @return
 	 */
 	private void buildForwarderCommand(SoftwareManager managedClient,
 			SoftwareManager managedServer,OmniNames omniNamesClient,OmniNames omniNamesServer) {
 		buildForwarderServerCommand(managedClient, managedServer,omniNamesClient,omniNamesServer);
 		buildForwarderClientCommand(managedClient, managedServer,omniNamesClient,omniNamesServer);
 	}
 
 	/**
 	 * PATH={phyNode.getEnv(Path)}:$PATH
 	 * OMNIORB_CONFIG={phyNode.scratchdir}/{omninames.id}.cfg nohup
 	 * {forwarderBinaryname} --name {forwarderName} --net-config
 	 * {phyNode.scratchDir}/{forwarderName}.cfg >
 	 * {phyNode.scratchdir}/{forwarderName}.out 2>
 	 * {phyNode.scratchdir}/{forwarderName}.err &
 	 * 
 	 * @param managedClient
 	 * @param managedServer
 	 */
 	private void buildForwarderServerCommand(SoftwareManager managedClient,
 			SoftwareManager managedServer,OmniNames omniNamesClient,OmniNames omniNamesServer) {
 		String command = "";
 		String scratchDir = managedServer.getPluggedOn().getDisk().getScratch()
 				.getDir();
 		Forwarder forwarderDescription = (Forwarder) managedServer
 				.getSoftwareDescription();
 		//Env PATH
		String envPath = ResourceUtil.getEnvValue(managedServer.getPluggedOn(),"PATH");
 		command+= "PATH="+envPath+":$PATH ";
 		// find the OmniOrbConfig file on the remote host to set OmniOrb.cfg
 		String omniOrbconfig = "OMNIORB_CONFIG=" + scratchDir + "/"
 				+ omniNamesServer.getId() + ".cfg";
 		command += omniOrbconfig + " ";
 
 		// nohup {binaryName}
 		command += "nohup "
 				+ forwarderDescription.getConfig().getRemoteBinary() + " ";
 		
 		//--name
 		command += "--name " + forwarderDescription.getId() + " ";
 		
 		//--net-config
 		command += "--net-config " + scratchDir + "/"
 				+ forwarderDescription.getId() + ".cfg ";
 
 		// > {phyNode.scratchdir}/{forwarderName}.out
 		command += "> " + scratchDir + "/" + forwarderDescription.getId()
 				+ ".out ";
 		// 2> {phyNode.scratchdir}/{forwarderName}.err
 		command += "2> " + scratchDir + "/" + forwarderDescription.getId()
 				+ ".err &";
 		managedServer.setRunningCommand(command);
 	}
 
 	/**
 	 * 
 	 * PATH={phyNode.getEnv(Path)}:$PATH
 	 * OMNIORB_CONFIG={phyNode.scratchdir}/{omninames.id}.cfg nohup
 	 * {forwarderBinaryname} --name {forwarderName} --net-config
 	 * {phyNode.scratchDir}/{forwarderName}.cfg --peer-name
 	 * {remoteForwarderName} --ssh-host {serverHost} --ssh-login {serverLogin}
 	 *  --remote-host 127.0.0.1 -C &
 	 * 
 	 * @param managedClient
 	 * @param managedServer
 	 */
 	private void buildForwarderClientCommand(SoftwareManager managedClient,
 			SoftwareManager managedServer,OmniNames omniNamesClient,OmniNames omniNamesServer) {
 		String command = "";
 		String scratchDir = managedClient.getPluggedOn().getDisk().getScratch()
 				.getDir();
 		Forwarder forwarderDescription = (Forwarder) managedClient
 				.getSoftwareDescription();
 		//Env PATH
 		String envPath = ResourceUtil.getEnvValue(managedClient.getPluggedOn(),"PATH");
 		command+= "PATH="+envPath+":$PATH ";
 		// find the OmniOrbConfig file on the remote host to set OmniOrb.cfg
 		String omniOrbconfig = "OMNIORB_CONFIG=" + scratchDir + "/"
 				+ omniNamesClient.getId() + ".cfg";
 		command += omniOrbconfig + " ";
 
 		// nohup {binaryName}
 		command += "nohup "
 				+ forwarderDescription.getConfig().getRemoteBinary() + " ";
 		// {name}
 		command += "--name " + forwarderDescription.getId() + " ";
 		// --net-config {phyNode.scratchDir}/{forwarderName}.cfg
 		command += "--net-config " + scratchDir + "/"
 				+ forwarderDescription.getId() + ".cfg ";
 		// --peer-name {remoteForwarderName}
 		command += "--peer-name "
 				+ managedServer.getSoftwareDescription().getId() + " ";
 		// --ssh-host {remoteHost}
 		command += " --ssh-host "
 				+ managedServer.getPluggedOn().getSsh().getServer() + " ";
 		// --ssh-login {remoteLogin}
 		command += "--ssh-login "
 				+ managedServer.getPluggedOn().getSsh().getLogin() + " ";
 		// --remote-port {remotePort} -C &
 	
 		//--remote-host 127.0.0.1 -C 
 		command += "--remote-host 127.0.0.1 -C ";
 		
 		// > {phyNode.scratchdir}/{forwarderName}.out
 		command += "> " + scratchDir + "/" + forwarderDescription.getId()
 				+ ".out ";
 		// 2> {phyNode.scratchdir}/{forwarderName}.err
 		command += "2> " + scratchDir + "/" + forwarderDescription.getId()
 				+ ".err ";
 		//&
 		command +="&";
 
 
 		managedClient.setRunningCommand(command);
 	}
 }
