 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.client.config.ClientConfiguration;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.config.ServerConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.topicindex.rest.entities.interfaces.RESTUserV1;
 
 @Parameters(commandDescription = "Setup the Content Specification Processor configuration files")
 public class SetupCommand extends BaseCommandImpl
 {
 
 	public SetupCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig)
 	{
 		super(parser, cspConfig, clientConfig);
 	}
 
 	@Override
 	public void printHelp()
 	{
 		printHelp(Constants.SETUP_COMMAND_NAME);
 	}
 
 	@Override
 	public void printError(final String errorMsg, final boolean displayHelp)
 	{
 		printError(errorMsg, displayHelp, Constants.SEARCH_COMMAND_NAME);
 	}
 
 	@Override
 	public RESTUserV1 authenticate(final RESTReader reader)
 	{
 		return null;
 	}
 
 	@Override
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final RESTUserV1 user)
 	{	
 		String username = "";
 		String defaultServerName = "";
 		String rootDir = "";
 		
 		String publicanParams = "";
 		String publicanFormat = "";
 		
		String kojiHubUrl = "";
		String publishCommand = "";
		
 		String zanataUrl = "";
 		String zanataProject = "";
 		String zanataVersion = "";
 		
 		final HashMap<String, ServerConfiguration> servers = new HashMap<String, ServerConfiguration>();
 		
 		JCommander.getConsole().println("Use the default server configuration? (Yes/No)");
 		String answer = JCommander.getConsole().readLine();
 		
 		/* We are using the default setup so we only need to get the default server and a username */
 		if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y"))
 		{
 			// Get which server they want to connect to by default
 			while (!defaultServerName.equalsIgnoreCase("test") && !defaultServerName.equalsIgnoreCase("production"))
 			{
 				JCommander.getConsole().println("Which server do you want to connect to by default? (test/production)");
 				defaultServerName = JCommander.getConsole().readLine().toLowerCase();
 				
 				// Good point to check for a shutdown
 				if (isAppShuttingDown())
 				{
 					shutdown.set(true);
 					return;
 				}
 			}
 			
 			// Get the users username
 			JCommander.getConsole().println("Please enter a username to connect to the servers: ");
 			username = JCommander.getConsole().readLine();
 			
 			// Create the default settings
 			servers.put(Constants.DEFAULT_SERVER_NAME, new ServerConfiguration(Constants.DEFAULT_SERVER_NAME, defaultServerName, username));
 			
 			// Setup each servers settings
 			servers.put("test", new ServerConfiguration("test", Constants.DEFAULT_TEST_SERVER));
 			servers.put("production", new ServerConfiguration("production", Constants.DEFAULT_PROD_SERVER));
 			
 		}
 		/* We need to read in a list of servers and then get the default server */
 		else if (answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n"))
 		{
 			while (!answer.matches("^[0-9]+$"))
 			{
 				JCommander.getConsole().print("How many servers are to be configured? ");
 				answer = JCommander.getConsole().readLine();
 				
 				// Good point to check for a shutdown
 				if (isAppShuttingDown())
 				{
 					shutdown.set(true);
 					return;
 				}
 			}
 			
 			// Get the server setup details from the user
 			final Integer numServers = Integer.parseInt(answer);
 			String serverNames = "";
 			for (int i = 1; i <= numServers; i++)
 			{
 				final ServerConfiguration config = new ServerConfiguration();
 				
 				// Get the name of the server
 				JCommander.getConsole().println("Please enter the name of server no. " + i + ": ");
 				config.setName(JCommander.getConsole().readLine().toLowerCase());
 				
 				// Get the url of the server
 				JCommander.getConsole().println("Please enter the URL of server no. " + i + ": ");
 				config.setUrl(ClientUtilities.validateHost(JCommander.getConsole().readLine()));
 				
 				// Get the username for the server
 				JCommander.getConsole().println("Please enter the username of server no. " + i + ": ");
 				config.setUsername(JCommander.getConsole().readLine());
 				
 				// Add the server configuration and add the name to the list of displayable strings
 				servers.put(config.getName(), config);
 				serverNames += config.getName() + "/";
 				
 				// Good point to check for a shutdown
 				if (isAppShuttingDown())
 				{
 					shutdown.set(true);
 					return;
 				}
 			}
 			
 			/* Only ask for the default when there are multiple servers */
 			if (servers.size() > 1)
 			{
 				// Get which server they want to connect to
 				while (!servers.containsKey(defaultServerName))
 				{
 					JCommander.getConsole().println("Which server do you want to connect to by default? (" + serverNames.substring(0, serverNames.length() - 1) + ")");
 					defaultServerName = JCommander.getConsole().readLine().toLowerCase();
 					
 					// Good point to check for a shutdown
 					if (isAppShuttingDown()) 
 					{
 						shutdown.set(true);
 						return;
 					}
 				}
 			}
 			else
 			{
 				defaultServerName = serverNames.substring(0, serverNames.length() - 1);
 			}
 			
 			// Create the default settings
 			servers.put(Constants.DEFAULT_SERVER_NAME, new ServerConfiguration(Constants.DEFAULT_SERVER_NAME, defaultServerName));
 		}
 		else
 		{
 			printError(Constants.INVALID_ARG_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Get the root directory to store content specifications
 		JCommander.getConsole().println("Enter a root directory to store Content Specifications. (Press enter for no root directory)");
 		rootDir = JCommander.getConsole().readLine();
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) 
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		// Get the publican options
 		JCommander.getConsole().println("Please enter the publican build command line options. (\"" + Constants.DEFAULT_PUBLICAN_OPTIONS + "\")");
 		publicanParams = JCommander.getConsole().readLine();
 		JCommander.getConsole().println("Please enter the preferred publican preview format. (" + Constants.DEFAULT_PUBLICAN_FORMAT + ")");
 		publicanFormat = JCommander.getConsole().readLine();
 		
 		// Get the publishing options
 		JCommander.getConsole().println("Setup the publishing options? (Yes/No)");
 		answer = JCommander.getConsole().readLine();
 		
 		if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y"))
 		{
			// Get the publish options
 			JCommander.getConsole().println("Please enter the URL of the " + Constants.KOJI_NAME + "hub. (" + Constants.DEFAULT_KOJIHUB_URL + ")");
			kojiHubUrl = JCommander.getConsole().readLine();
			JCommander.getConsole().println("Please enter the default command to publish Content Specifications." + (Constants.DEFAULT_PUBLISH_COMMAND.isEmpty() ? "" : ("(" + Constants.DEFAULT_PUBLISH_COMMAND + ")")));
			publishCommand = JCommander.getConsole().readLine();
 		}
 		
 		JCommander.getConsole().println("Setup zanata translation editor link information? (Yes/No)");
 		answer = JCommander.getConsole().readLine();
 		
 		if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y"))
 		{
 			// Get the translation options
 			JCommander.getConsole().println("Please enter the default URL for zanata. (" + Constants.DEFAULT_ZANATA_URL + ")");
 			publicanParams = JCommander.getConsole().readLine();
 			JCommander.getConsole().println("Please enter the default Project to use from zanata. (" + Constants.DEFAULT_ZANATA_PROJECT + ")");
 			publicanFormat = JCommander.getConsole().readLine();
 			JCommander.getConsole().println("Please enter the default Version of the Project to use from zanata. (" + Constants.DEFAULT_ZANATA_VERSION + ")");
 			publicanFormat = JCommander.getConsole().readLine();
 		}
 		
 		// Create the configuration file
 		final StringBuilder configFile = new StringBuilder("[servers]\n");
 		for (final String serverName: servers.keySet())
 		{
 			final ServerConfiguration config = servers.get(serverName);
 			
 			// Setup the url for the server
 			if (serverName.equals(Constants.DEFAULT_SERVER_NAME))
 			{
 				configFile.append(serverName + "=" + config.getUrl() + "\n");
 			}
 			else
 			{
 				configFile.append(serverName + ".url=" + config.getUrl() + "\n");
 			}
 			
 			// Setup the username for the server
 			if (config.getUsername() != null && !config.getUsername().equals(""))
 				configFile.append(serverName + ".username=" + config.getUsername() + "\n");
 			else if (!serverName.equals(Constants.DEFAULT_SERVER_NAME))
 				configFile.append(serverName + ".username=\n");
 			
 			// Add a blank line to separate servers
 			configFile.append("\n");
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown())
 			{
 				shutdown.set(true);
 				return;
 			}
 		}
 		
 		// Create the Root Directory
 		configFile.append("[directory]\n");
 		configFile.append("root=" + rootDir + "\n\n");
 		
 		// Create the publican options
 		configFile.append("[publican]\n");
 		configFile.append("build.parameters=" + (publicanParams.isEmpty() ? Constants.DEFAULT_PUBLICAN_OPTIONS : publicanParams) + "\n");
 		configFile.append("preview.format=" + (publicanFormat.isEmpty() ? Constants.DEFAULT_PUBLICAN_FORMAT : publicanFormat) + "\n\n");
 		
 		// Create the zanata translation options
 		configFile.append("[translations]\n");
 		configFile.append("zanata.url=" + (zanataUrl.isEmpty() ? Constants.DEFAULT_ZANATA_URL : ClientUtilities.validateHost(zanataUrl)) + "\n");
 		configFile.append("zanata.project.name=" + (zanataProject.isEmpty() ? Constants.DEFAULT_ZANATA_PROJECT : zanataProject) + "\n");
		configFile.append("zanata.project.version=" + (zanataVersion.isEmpty() ? Constants.DEFAULT_ZANATA_VERSION : zanataVersion) + "\n\n");
		
		// Create the Root Directory
		configFile.append("[publish]\n");
		configFile.append("koji.huburl=" + kojiHubUrl + "\n");
		configFile.append("command=" + publishCommand + "\n\n");
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		// Save the configuration file
 		final File file = new File(System.getProperty("user.home") + "/.config/csprocessor.ini");
 		try {
 			// Make sure the directory exists
 			if (file.getParentFile() != null)
 			{
 				file.getParentFile().mkdirs();
 			}
 			
 			// Make a backup of any existing csprocessor.ini
 			if (file.exists())
 			{
 				file.renameTo(new File(file.getAbsolutePath() + ".backup"));
 			}
 			
 			// Save the config
 			final FileOutputStream fos = new FileOutputStream(file);
 			fos.write(configFile.toString().getBytes());
 			fos.flush();
 			fos.close();
 		}
 		catch (IOException e)
 		{
 			printError(Constants.ERROR_FAILED_CREATING_CONFIG_MSG, false);
 			shutdown(Constants.EXIT_CONFIG_ERROR);
 		}
 		
 		JCommander.getConsole().println(Constants.SUCCESSFUL_SETUP_MSG);
 	}
 
 	@Override
 	public boolean loadFromCSProcessorCfg()
 	{
 		/* 
 		 * No need to load from csprocessor.cfg as this
 		 * command configures the client before use.
 		 */
 		return false;
 	}
 
 }
