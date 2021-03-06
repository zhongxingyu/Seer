 package org.jboss.ide.eclipse.as.reddeer.server.requirement;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import org.apache.log4j.Logger;
 import org.jboss.ide.eclipse.as.reddeer.server.family.FamilyWildFly;
 import org.jboss.ide.eclipse.as.reddeer.server.family.ServerFamily;
 import org.jboss.ide.eclipse.as.reddeer.server.requirement.ServerRequirement.Server;
 import org.jboss.ide.eclipse.as.reddeer.server.wizard.NewServerWizardDialog;
 import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.DefineNewServerWizardPage;
 import org.jboss.ide.eclipse.as.reddeer.server.wizard.page.JBossRuntimeWizardPage;
 import org.jboss.reddeer.eclipse.exception.EclipseLayerException;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersViewEnums.ServerState;
 import org.jboss.reddeer.junit.requirement.CustomConfiguration;
 import org.jboss.reddeer.junit.requirement.Requirement;
 import org.jboss.reddeer.swt.api.Combo;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.impl.combo.DefaultCombo;
 import org.jboss.reddeer.workbench.view.impl.WorkbenchView;
 
 /**
  * 
  * @author psrna, Radoslav Rabara
  *
  */
 
 public class ServerRequirement implements Requirement<Server>, CustomConfiguration<ServerRequirementConfig> {
 
 	private static final Logger LOGGER = Logger.getLogger(ServerRequirement.class);
 	
 	private static ConfiguredServerInfo lastServerConfiguration;
 	
 	private ServerRequirementConfig config;
 	private Server server;
 	
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.TYPE)
 	public @interface Server {
 		ServerReqState state() default ServerReqState.RUNNING;
 		ServerReqType type() default ServerReqType.ANY;
 		String version() default "";
 		ServerReqOperator operator() default ServerReqOperator.EQUAL;
 	}
 	
 	
 	@Override
 	public boolean canFulfill() {
 		//requirement can be fulfilled only when required server's type and version matches to
 		//configured server's type and version
 		return ServerMatcher.matchServerType(server.type(), config.getServerFamily()) &&
 				ServerMatcher.matchServerVersion(server.version(), server.operator(),
 				config.getServerFamily().getVersion());
 	}
 
 	@Override
 	public void fulfill() {
 		if(lastServerConfiguration != null) {
 			boolean differentConfig = !config.equals(lastServerConfiguration.getConfig());
 			if(differentConfig) {
 				removeLastRequiredServer();
 				lastServerConfiguration = null;
 			}
 		}
 		if(lastServerConfiguration == null || !isLastConfiguredServerPresent()) {
 			LOGGER.info("Setup server");
 			setupServerAdapter();
 			lastServerConfiguration = new ConfiguredServerInfo(getServerNameLabelText(), config);
 		}
 		setupServerState();
 	}
 
 	private void setupServerState() throws ConfiguredServerNotFoundException {
 		LOGGER.info("Checking the state of the server '"+lastServerConfiguration.getServerName()+"'");
 		
 		org.jboss.reddeer.eclipse.wst.server.ui.view.Server serverInView = getConfiguredServer();
 		
 		ServerState state = serverInView.getLabel().getState();
 		ServerReqState requiredState = server.state();
 		switch(state) {
 			case STARTED:
 				if(requiredState == ServerReqState.STOPPED)
 					serverInView.stop();
 				break;
 			case STOPPED:
 				if(requiredState == ServerReqState.RUNNING)
 					serverInView.start();
 				break;
 			default:
 				new AssertionError("It was expected to have server in "
 						+ ServerState.STARTED + " or " + ServerState.STOPPED
 						+ "state." + " Not in state "+state+".");
 		}
 	}
 	
 	private void removeLastRequiredServer() {
 		try {
 			org.jboss.reddeer.eclipse.wst.server.ui.view.Server serverInView = getConfiguredServer();
 			//remove server added by last requirement
 			serverInView.delete(true);
 		} catch(ConfiguredServerNotFoundException e) {
 			//server had been already removed
 		}
 		//current state = there is no server defined
 		lastServerConfiguration = null;
 	}
 	
 	private org.jboss.reddeer.eclipse.wst.server.ui.view.Server getConfiguredServer()
 			throws ConfiguredServerNotFoundException {
 		ServersView serversView = new ServersView();
 		final String serverName = lastServerConfiguration.getServerName();
 		try {
 			return serversView.getServer(serverName);
 		} catch(EclipseLayerException e) {
 			LOGGER.warn("Server \"" + serverName + "\" not found. It had been removed.");
 			throw new ConfiguredServerNotFoundException();
 		}
 	}
 	
 	private boolean isLastConfiguredServerPresent() {
 		try {
 			getConfiguredServer();
 		} catch(ConfiguredServerNotFoundException e) {
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public void setDeclaration(Server server) {
 		this.server = server;
 	}
 
 	@Override
 	public Class getConfigurationClass() {
 		return ServerRequirementConfig.class;
 	}
 
 	@Override
 	public void setConfiguration(ServerRequirementConfig config) {
 		this.config = config;
 	}
 
 	public ServerRequirementConfig getConfig() {
 		return this.config;
 	}
 	
 	public String getServerTypeLabelText() {
 		ServerFamily server = config.getServerFamily();
 		if (server instanceof FamilyWildFly) {
 			if (server.getVersion().equals("8.0")) {
 				return "WildFly 8.0 (Experimental)";
 			}
 		}
 		return config.getServerFamily().getLabel() + " "
 				+ config.getServerFamily().getVersion();
 	}
 	
 	public String getServerNameLabelText() {
 		return getServerTypeLabelText() + " Server";
 	}
 
 	public String getRuntimeNameLabelText() {
 		return getServerTypeLabelText() + " Runtime";
 	}
 
 	protected void setupServerAdapter() {
 		NewServerWizardDialog serverW = new NewServerWizardDialog();
 		try {
 			serverW.open();
 			
			DefineNewServerWizardPage sp = new DefineNewServerWizardPage();
 	
 			sp.selectType(config.getServerFamily().getCategory(),
 					getServerTypeLabelText());
 			checkIfThereIsAnyOtherServerWithTheSameType();
 			
 			sp.setName(getServerNameLabelText());
 			checkTheServerName();
 			
 			serverW.next();
 			
 			JBossRuntimeWizardPage rp = new JBossRuntimeWizardPage();
 			
 			rp.setRuntimeName(getRuntimeNameLabelText());
 			checkTheServerName();
 			
 			rp.setRuntimeDir(config.getRuntime());
 			checkTheHomeDirectory();
 			
 			checkOtherErrors();
 			
 			serverW.finish();
 		} catch(AssertionError e) {
 			serverW.cancel();
 			throw e;
 		}
 	}
 	
 	private void checkIfThereIsAnyOtherServerWithTheSameType() {
 		try {
 			//combo box indicate other servers with the same type
 			Combo combo = new DefaultCombo();
 			throw new AssertionError("There is another server with the same type.\n"
 					+ "Type: "+getServerTypeLabelText()+"\n"
 					+ "Present server: "+combo.getText());
 		} catch(SWTLayerException e) {
 			//combo box is not present so there is not any other server with the same type
 		}
 	}
 
 	private void checkTheServerName() {
 		String text = new org.jboss.reddeer.swt.impl.text.DefaultText(3).getText();
 		if(text.contains("The server name is already in use. Specify a different name.")) {
 			throw new AssertionError("The server name '"+getServerNameLabelText()+"' is already in use.");
 		}
 		if(text.contains("The name field must not be blank")) {
 			throw new AssertionError("The server name '"+getServerNameLabelText()+"' is empty.");
 		}
 	}
 	
 	private void checkTheHomeDirectory() {
 		String text = new org.jboss.reddeer.swt.impl.text.DefaultText(3).getText();
 		if(text.contains("The home directory does not exist or is not a directory.")) {
 			throw new AssertionError("The home directory '"+config.getRuntime()+"'"
 					+" does not exist or is not a directory.");
 		}
 		if(text.contains("The home directory is missing a required file or folder:")) {
 			throw new AssertionError("The home directory '"+config.getRuntime()+"'"
 					+" is missing a required file or folder:"+text.split(":")[1]);
 		}
 	}
 	
 	private void checkOtherErrors() {
 		String text = new org.jboss.reddeer.swt.impl.text.DefaultText(3).getText();
 		if(text.contains("No valid JREs found for execution environment")) {
 			throw new AssertionError(text);
 		}
 	}
 	
 	/**
 	 * Configured server was not found.
 	 * 
 	 * @author rrabara
 	 *
 	 */
 	private class ConfiguredServerNotFoundException extends RuntimeException {
 		
 	}
 
 }
