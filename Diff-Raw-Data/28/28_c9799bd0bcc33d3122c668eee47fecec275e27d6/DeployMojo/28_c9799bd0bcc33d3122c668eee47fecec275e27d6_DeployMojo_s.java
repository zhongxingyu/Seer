 package org.ccci.deployment.maven;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Layout;
 import org.apache.log4j.spi.ErrorHandler;
 import org.apache.log4j.spi.Filter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.ccci.deployment.ApplicationLookup;
 import org.ccci.deployment.ConfigurationException;
 import org.ccci.deployment.DeploymentDriver;
 import org.ccci.deployment.Options;
 import org.ccci.util.ConsoleUtil;
 import org.ccci.util.logging.JuliToLog4jHandler;
 
 /**
  * Goal which deploys the configured application
  *
  * @goal deploy
  * 
  */
 public class DeployMojo
     extends AbstractMojo
 {
     /**
      * The name of the environment variable that contains the password to use for deployment.
      * 
      * @parameter expression="${deployments.passwordEnvironmentVariableName}" 
      */
     private String passwordEnvironmentVariableName;
 
     /**
      * The password to use for transfer & service restarts
      * 
      * @parameter expression="${deployments.password}" 
      */
     private String password;
     
     /**
      * The username to use for for transfer & service restarts
      * 
      * @parameter expression="${deployments.username}" 
      */
     private String username;
     
     
     /**
      * Which environment (dev, staging, production, etc) to deploy to. Each application defines its own set of possible environments.
      * 
      * @required
      * @parameter expression="${deployments.environment}" 
      */
     private String environment;
     
     
     /**
      * the url for the continuous integration server running this deployment
      * 
      * @parameter expression="${deployments.continuousIntegrationUrl}" 
      */
     private String continuousIntegrationUrl;
     
     
     /**
      * which directory contains the application to deploy
      * 
      * @parameter expression="${deployments.sourceDirectory}" 
      */
     private File sourceDirectory;
     
     /**
      * the Active Directory domain for the username, if this is a deployment to a windows machine
      * 
      * @parameter expression="${deployments.domain}" 
      */
     private String domain;
     
     /**
      * which web application to deploy.  Optional if a default application is on the classpath.
      * 
      * @parameter expression="${deployments.application}" 
      */
     private String application;
     
     /**
      * Whether to prompt for a password (requires keyboard interaction)
      * 
      * @parameter 
      *   expression="${deployments.promptPassword}"
      *   default-value="true" 
      */
     private boolean promptPassword;
     
     
     
     public void execute()
         throws MojoExecutionException, MojoFailureException
     {
         setUpLogging();
         lookupPasswordIfNecessary();
 
         Options options = initializeOptions();
         
         DeploymentDriver driver;
         try
         {
             driver = new DeploymentDriver(options);
         }
         catch (ConfigurationException e)
         {
             throw buildFailureException(e);
         }
         
         driver.deploy();
     }
 
     private void setUpLogging()
     {
         org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
         root.setLevel(org.apache.log4j.Level.INFO);
         root.addAppender(new MavenPluginAppender(getLog()));
         
        org.apache.log4j.Logger jinteropLogger = org.apache.log4j.Logger.getLogger("org.jinterop");
        jinteropLogger.setLevel(org.apache.log4j.Level.ERROR);
         
         redirectJavaUtilLoggingToLog4j();
         
         Logger.getLogger(DeployMojo.class.getName()).info("starting up deploy mojo");
     }
 
    private static void redirectJavaUtilLoggingToLog4j()
     {
         Logger rootLogger = LogManager.getLogManager().getLogger("");
         
         for (Handler handler : rootLogger.getHandlers()) {
             rootLogger.removeHandler(handler);
         }
         
         Handler activeHandler = new JuliToLog4jHandler();
         activeHandler.setLevel(Level.ALL);
         
         rootLogger.addHandler(activeHandler);
         rootLogger.setLevel(Level.ALL);
     }
 
     private Options initializeOptions() throws MojoFailureException
     {
         Options options = new Options();
         
         require(environment, "environment");
         
         options.username = username;
         options.password = password;
         options.domain = domain;
         options.environment = environment;
         options.continuousIntegrationUrl = continuousIntegrationUrl;
         options.sourceDirectory = sourceDirectory;
         
         if (application != null)
         {
             try
             {
                 options.application = new ApplicationLookup().lookupApplication(application);
             }
             catch (IllegalArgumentException e)
             {
                 throw buildFailureException(e);
             }
         }
 
         
         try
         {
             options.initializeDefaults();
         }
         catch (IllegalStateException e)
         {
             throw buildFailureException(e);
         }
         return options;
     }
 
 
     private void lookupPasswordIfNecessary() throws MojoFailureException, MojoExecutionException
     {
         if (password == null)
         {
             if (passwordEnvironmentVariableName != null)
             {
                 getPasswordFromEnvironmentVariable();
             }
             else if (promptPassword)
             {
                 getPasswordFromConsolePrompt();
             }
             else
             {
                 getLog().warn("using an empty password");
             }
         }
     }
 
     private void getPasswordFromEnvironmentVariable() throws MojoFailureException
     {
         password = System.getenv(passwordEnvironmentVariableName);
         if (password == null)
         {
             throw new MojoFailureException("Unable to get password from environment varible " + passwordEnvironmentVariableName);
         }
     }
 
     private void getPasswordFromConsolePrompt() throws MojoExecutionException
     {
         try
         {
             password = ConsoleUtil.readPasswordFromInput();
         }
         catch (IOException e)
         {
             throw new MojoExecutionException("unable to read password", e);
         }
     }
 
     private MojoFailureException buildFailureException(Exception e) throws MojoFailureException
     {
         MojoFailureException mojoFailureException = new MojoFailureException(e.getMessage());
         mojoFailureException.initCause(e);
         throw mojoFailureException;
     }
 
     private void require(Object parameter, String parameterName) throws MojoFailureException
     {
         if (parameter == null)
             throw new MojoFailureException(String.format("Parameter %s is required", parameterName));
     }
 
     public String getPasswordEnvironmentVariableName()
     {
         return passwordEnvironmentVariableName;
     }
 
     public void setPasswordEnvironmentVariableName(String passwordEnvironmentVariableName)
     {
         this.passwordEnvironmentVariableName = passwordEnvironmentVariableName;
     }
 
     public String getPassword()
     {
         return password;
     }
 
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     public String getUsername()
     {
         return username;
     }
 
     public void setUsername(String username)
     {
         this.username = username;
     }
 
     public String getEnvironment()
     {
         return environment;
     }
 
     public void setEnvironment(String environment)
     {
         this.environment = environment;
     }
 
     public String getContinuousIntegrationUrl()
     {
         return continuousIntegrationUrl;
     }
 
     public void setContinuousIntegrationUrl(String continuousIntegrationUrl)
     {
         this.continuousIntegrationUrl = continuousIntegrationUrl;
     }
 
     public File getSourceDirectory()
     {
         return sourceDirectory;
     }
 
     public void setSourceDirectory(File sourceDirectory)
     {
         this.sourceDirectory = sourceDirectory;
     }
 
     public String getDomain()
     {
         return domain;
     }
 
     public void setDomain(String domain)
     {
         this.domain = domain;
     }
 
     public String getApplication()
     {
         return application;
     }
 
     public void setApplication(String application)
     {
         this.application = application;
     }
 
     public boolean isPromptPassword()
     {
         return promptPassword;
     }
 
     public void setPromptPassword(boolean promptPassword)
     {
         this.promptPassword = promptPassword;
     }
 
     
 }
