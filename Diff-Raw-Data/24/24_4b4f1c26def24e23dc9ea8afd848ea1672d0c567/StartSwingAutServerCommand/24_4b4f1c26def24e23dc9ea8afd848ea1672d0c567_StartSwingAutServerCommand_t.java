 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.autagent.commands;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.eclipse.jubula.autagent.AutStarter;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.CommandConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jul 6, 2007
  * 
  */
 public class StartSwingAutServerCommand extends AbstractStartJavaAut {
 
     /** the logger */
     private static final  Logger LOG = 
         LoggerFactory.getLogger(StartSwingAutServerCommand.class);
     
     /** separates the environment variables */
     private static final String ENV_SEPARATOR = "\n"; //$NON-NLS-1$
     
     /** the classpath of the Aut Server */
     private String m_autServerClasspath = "AutServerClasspath"; //$NON-NLS-1$
     
     /**
      * {@inheritDoc}
      */
     protected String[] createEnvArray(Map parameters, boolean isAgentSet) {
         
         if (isRunningFromExecutable(parameters) 
                 || isRunningWithMonitoring(parameters)) {
             setEnv(parameters);
             boolean agentActive = true;
             return super.createEnvArray(parameters, agentActive);
         }       
           
         return super.createEnvArray(parameters, isAgentSet);
         
     }
     
     /**
      * Sets -javaagent, JRE arguments and the arguments for 
      * the AutServer as environment variables.
      * @param parameters The parameters for starting the AUT
      */
     private void setEnv(Map parameters) {
         String env = (String)parameters.get(AutConfigConstants.ENVIRONMENT);
         if (env == null) {
             env = StringConstants.EMPTY;
         } else {
             env += ENV_SEPARATOR;
         }
         env += setJavaOptions(parameters);
         if (isRunningFromExecutable(parameters)) {
         // agent arguments
             String serverPort = "null"; //$NON-NLS-1$
             if (AutStarter.getInstance().getAutCommunicator() != null) {
                 serverPort = String.valueOf(AutStarter.getInstance()
                     .getAutCommunicator().getLocalPort());
             }
         
             env += ENV_SEPARATOR + "AUT_SERVER_PORT=" + serverPort; ////$NON-NLS-1$
             env += ENV_SEPARATOR + "AUT_SERVER_CLASSPATH=" + m_autServerClasspath; //$NON-NLS-1$
             env += ENV_SEPARATOR + "AUT_SERVER_NAME=" + getServerClassName(); //$NON-NLS-1$
         
             // Aut Agent variables
             env += ENV_SEPARATOR + AutConfigConstants.AUT_AGENT_HOST + "=" + parameters.get(AutConfigConstants.AUT_AGENT_HOST); //$NON-NLS-1$
             env += ENV_SEPARATOR + AutConfigConstants.AUT_AGENT_PORT + "=" + parameters.get(AutConfigConstants.AUT_AGENT_PORT); //$NON-NLS-1$
             env += ENV_SEPARATOR + AutConfigConstants.AUT_NAME + "=" + parameters.get(AutConfigConstants.AUT_NAME); //$NON-NLS-1$
         }
         // create environment
         parameters.put(AutConfigConstants.ENVIRONMENT, env);
     }
     
     /**
      * {@inheritDoc}
      */
     protected String[] createCmdArray(String baseCmd, Map parameters) {
         List cmds = new Vector();
         cmds.add(baseCmd);
         
         StringBuffer autServerClasspath = new StringBuffer();
         createServerClasspath(autServerClasspath);
         
         List autAgentArgs = new ArrayList();
         autAgentArgs.add(String.valueOf(
                 parameters.get(AutConfigConstants.AUT_AGENT_HOST)));
         autAgentArgs.add(String.valueOf(
                 parameters.get(AutConfigConstants.AUT_AGENT_PORT)));
         autAgentArgs.add(String.valueOf(
                 parameters.get(AutConfigConstants.AUT_NAME)));
         
         if (!isRunningFromExecutable(parameters)) {
             createAutServerLauncherClasspath(
                     cmds, autServerClasspath, parameters);
             createAutServerClasspath(autServerClasspath, cmds, parameters);
             cmds.addAll(autAgentArgs);
             // information for aut server that agent is not used
             cmds.add(CommandConstants.RC_COMMON_AGENT_INACTIVE);
         } else { 
             String serverBasePath = createServerBasePath(); 
             autServerClasspath.append(PATH_SEPARATOR)
                 .append(serverBasePath).append(PATH_SEPARATOR)
                 .append(getRcBundleClassPath());
             m_autServerClasspath = autServerClasspath.toString();
                        
         }
         cmds.addAll(createAutArguments(parameters));
         return (String[])cmds.toArray(new String[cmds.size()]);
     }
     
     /**
      * 
      * @return an array of Strings representing the launch classpath.
      */
     public String[] getLaunchClasspath() {
         StringBuffer autServerClasspath = new StringBuffer();
         createServerClasspath(autServerClasspath);
         autServerClasspath.append(PATH_SEPARATOR)
             .append(getRcBundleClassPath());
         return autServerClasspath.toString().split(PATH_SEPARATOR);
     }
     
     /**     * Creates the AUT settings.
      * @param cmds the commands list
      * @param parameters The parameters for starting the AUT.
      */
     private void addBaseSettings(List cmds, Map parameters) {
         // add locale
         addLocale(cmds, (Locale)parameters.get(IStartAut.LOCALE)); 
         
         // add jre params
         final String jreParams = (String)parameters.get(
                 AutConfigConstants.JRE_PARAMETER);
         if (jreParams != null && jreParams.length() > 0) {
             StringTokenizer tok = new StringTokenizer(jreParams, 
                 WHITESPACE_DELIMITER);
             while (tok.hasMoreTokens()) {
                 cmds.add(tok.nextToken());
             }
         }
                 
         // add debug options (if neccessary)
         addDebugParams(cmds);
         // add -Duser.dir and workingDir here
     }
     /**
      * Creates the Server classpath.
      * @param serverClasspath the server classpath
      */
     private void createServerClasspath(StringBuffer serverClasspath) {
 
         String [] bundlesToAddToClasspath = {
             CommandConstants.TOOLS_BUNDLE_ID, 
             CommandConstants.COMMUNICATION_BUNDLE_ID, 
             CommandConstants.RC_COMMON_BUNDLE_ID,
             CommandConstants.SLF4J_API_BUNDLE_ID,
             CommandConstants.COMMONS_LANG_BUNDLE_ID,
             CommandConstants.APACHE_ORO_BUNDLE_ID,
             CommandConstants.COMMONS_BEAN_UTILS_BUNDLE_ID,
             CommandConstants.COMMONS_COLLECTIONS_BUNDLE_ID
         };
         
         for (String bundleId : bundlesToAddToClasspath) {
             serverClasspath.append(
                     AbstractStartToolkitAut.getClasspathForBundleId(bundleId));
             serverClasspath.append(PATH_SEPARATOR);
         }
         
         
         serverClasspath.append(getAbsExtImplClassesPath());       
         if (LOG.isDebugEnabled()) {
             LOG.debug("serverClasspath" + serverClasspath); //$NON-NLS-1$
         }
     }
     
     /**
      * @param cmds the commands list
      * @param autServerClasspath the autServerClassPath to change
      * @param parameters The parameters for starting the AUT.
      */
     private void createAutServerLauncherClasspath(List cmds, 
             StringBuffer autServerClasspath, Map parameters) {
         
         addBaseSettings(cmds, parameters);
         cmds.add("-classpath"); //$NON-NLS-1$
         StringBuffer autClassPath = createAutClasspath(parameters);
         String serverBasePath = createServerBasePath(); 
         cmds.add(
                 autClassPath.append(PATH_SEPARATOR)
                     .append(serverBasePath).toString());
         // add classname of autLauncher
         cmds.add(CommandConstants.AUT_SERVER_LAUNCHER);
         // add autServerBase dirs to autServerClassPath
         autServerClasspath.append(PATH_SEPARATOR).append(serverBasePath);
     }
     
     /**
      * Creates the AUT classpath. 
      * @param parameters The parameters for starting the AUT.
      * @return The classpath of the AUT.
      */
     private StringBuffer createAutClasspath(Map parameters) {
         // Add AUT classpath
         String autClassPathStr = (String)parameters.get(
                 AutConfigConstants.CLASSPATH);
         if (autClassPathStr == null) {
             autClassPathStr = StringConstants.EMPTY;
         }
         StringBuffer autClassPath = new StringBuffer(
                 convertClientSeparator(autClassPathStr)); 
         if (autClassPath.length() > 0) {
             autClassPath.append(PATH_SEPARATOR);
         }
         String jarFile = (String)parameters.get(AutConfigConstants.JAR_FILE);
         if (jarFile == null) {
             jarFile = StringConstants.EMPTY;
         }
         String manifestClassPath = getClassPathFromManifest(parameters);
         if (manifestClassPath.length() > 0) {
             autClassPath.append(manifestClassPath).append(PATH_SEPARATOR);
         }
         autClassPath.append(jarFile);
         if (jarFile != null && jarFile.length() > 0) {
             autClassPath.append(PATH_SEPARATOR);
         }
         return autClassPath;
     }
     
     /**
      * 
      * @return the server base path including resources path
      */
     private String createServerBasePath() {
         return AbstractStartToolkitAut.getClasspathForBundleId(
                 CommandConstants.RC_COMMON_BUNDLE_ID);
     }
     
     /**
      * Adds elements to the given cmds List.
      * @param autServerClasspath the server classpath
      * @param cmds the 1st part of the cmd array
      * @param parameters The parameters for starting the AUT.
      */
     private void createAutServerClasspath(StringBuffer autServerClasspath, 
         List cmds, Map parameters) {
         
         if (AutStarter.getInstance().getAutCommunicator() != null) {
             cmds.add(String.valueOf(
                     AutStarter.getInstance().getAutCommunicator()
                         .getLocalPort()));
         } else {
             cmds.add("null"); //$NON-NLS-1$
         }
         
         String autMain = getAUTMainClass(parameters);
         if (autMain == null) {
             return;
         }
         cmds.add(autMain);
         autServerClasspath.append(PATH_SEPARATOR)
             .append(getRcBundleClassPath());
         cmds.add(autServerClasspath.toString());
         cmds.add(getServerClassName());
     }
 
     /**
      * 
      * @return the class path corresponding to the receiver's RC bundle. 
      */ 
     protected String getRcBundleClassPath() {
         return AbstractStartToolkitAut.getClasspathForBundleId(getRcBundleId());
     }
 
     /**
      * 
      * @return the ID of the receiver's RC bundle.
      */
     protected String getRcBundleId() {
         return CommandConstants.RC_SWING_BUNDLE_ID;
     }
     
     /**
      * @param parameters The parameters for starting the AUT.
      * @return The arguments for the AUT that were found in the 
      *         given parameters.
      */
     private List createAutArguments(Map parameters) {
         List argsList = new Vector();
         if (parameters.get(AutConfigConstants.AUT_RUN_AUT_ARGUMENTS)
                 instanceof String[]) {
             String[] autArgs = (String[])parameters
                     .get(AutConfigConstants.AUT_RUN_AUT_ARGUMENTS);
             return Arrays.asList(autArgs);
         }
         String autArguments = 
             (String)parameters.get(AutConfigConstants.AUT_ARGUMENTS);
         
         if (autArguments == null) {
             autArguments = StringConstants.EMPTY;
         }
        
         StringTokenizer args = new StringTokenizer(autArguments, 
             WHITESPACE_DELIMITER);
         while (args.hasMoreTokens()) {
             String arg = args.nextToken();
             argsList.add(arg);
         }
         
         return argsList;
     }
     
     /**
      * Adds the parameters for remote debugging to the given command List.
      * @param cmds the command List
      */
     private void addDebugParams(List cmds) {
         if (BXDEBUG != null) {
             cmds.add("-Xdebug"); //$NON-NLS-1$
             cmds.add("-Xnoagent"); //$NON-NLS-1$
             cmds.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + BXDEBUG); //$NON-NLS-1$
             cmds.add("-Djava.compiler=NONE"); //$NON-NLS-1$
         }
     }
     
     /**
      * Gets the absolute path of the location of the external ImplClasses.
      * @return the absolute path
      */
     private String getAbsExtImplClassesPath() {
         
         final File implDir = new File(CommandConstants.EXT_IMPLCLASSES_PATH);
         final StringBuffer paths = new StringBuffer(implDir.getAbsolutePath());
         final File[] jars = implDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".jar"); //$NON-NLS-1$
             }
         });
         
         if (jars != null) {
             final int maxIdx = jars.length;
             for (int i = 0; i < maxIdx; i++) {
                 File f = jars[i];
                 paths.append(PATH_SEPARATOR);
                 paths.append(f.getAbsolutePath());
             }
         }
         return paths.toString();
     }
     
     /**
      * Gets the absolute path of the GDAgent.jar file.
      * @return the absolute path
      */
     protected String getAbsoluteAgentJarPath() {
         return AbstractStartToolkitAut.getClasspathForBundleId(
                 CommandConstants.RC_COMMON_AGENT_BUNDLE_ID);
     }
     
     /**
      * {@inheritDoc}
      */
     protected String getServerClassName() {
         return CommandConstants.AUT_SWING_SERVER;
     }
     
 }
