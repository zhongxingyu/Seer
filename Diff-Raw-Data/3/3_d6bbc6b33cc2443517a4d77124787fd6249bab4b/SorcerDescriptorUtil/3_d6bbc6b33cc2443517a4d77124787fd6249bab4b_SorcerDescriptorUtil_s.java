 /*
  * Copyright 2008 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.provider.boot;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import com.sun.jini.start.NonActivatableServiceDescriptor;
 import com.sun.jini.start.ServiceDescriptor;
 import org.apache.commons.lang3.StringUtils;
 import sorcer.core.SorcerEnv;
 import sorcer.resolver.Resolver;
 import sorcer.util.ArtifactCoordinates;
 
 import static sorcer.util.ArtifactCoordinates.coords;
 import static sorcer.util.Artifact.sorcer;
 
 /**
  * Holds static attributes used during the startup of services and provides
  * utilities to obtain {@link com.sun.jini.start.ServiceDescriptor} instances
  * for SORCER services
  */
 public class SorcerDescriptorUtil {
     final static Logger logger = Logger.getLogger("sorcer.provider.boot");
     public static final ArtifactCoordinates SOS_PLATFORM = sorcer("sos-platform");
     public static final ArtifactCoordinates SOS_ENV = sorcer("sos-env");
     public static final ArtifactCoordinates COMMONS_PRV = sorcer("commons-prv");
     public static final ArtifactCoordinates EXERTMONITOR_SERVICE = sorcer("exertmonitor-prv");
     public static final ArtifactCoordinates EXERTLET_UI = sorcer("sos-exertlet-sui");
     public static final ArtifactCoordinates DBP_PRV = sorcer("dbp-prv");
     public static final ArtifactCoordinates DSP_PRV = sorcer("dsp-prv");
     public static final ArtifactCoordinates CATALOGER_PRV = sorcer("cataloger-prv");
     public static final ArtifactCoordinates LOGGER_PRV = sorcer("logger-prv");
     public static final ArtifactCoordinates LOGGER_SUI = sorcer("logger-sui");
     public static final ArtifactCoordinates WEBSTER = sorcer("sos-webster");
     public static final ArtifactCoordinates SPACER_PRV = sorcer("spacer-prv");
     public static final ArtifactCoordinates JOBBER_PRV = sorcer("jobber-prv");
 
     public static final ArtifactCoordinates SLEEPYCAT = coords("com.sleepycat:je");
     public static final ArtifactCoordinates SERVICEUI = coords("net.jini.lookup:serviceui");
 
     private static String fs = File.separator;
 	private static String ps = File.pathSeparator;
 	private static String sorcerHome = SorcerEnv.getHomeDir().getAbsolutePath();
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
      * Webster.
      *
      * @param policy
      *            The security policy file to use
      *            The roots webster should serve
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
      *         webster using an anonymous port. The <tt>webster.jar</tt> file
      *         will be loaded from <tt>sorcer.home/common/sorcer/webster.com</tt>
      *
      * @throws IOException
      *             If there are problems getting the anonymous port
      * @throws RuntimeException
      *             If the <tt>sorcer.home</tt> system property is not set
      */
     public static ServiceDescriptor getWebster(String policy, int port)
             throws IOException {
         if (Resolver.isMaven()) {
             String[] roots = {
                     sorcerHome + "/deploy",
                     sorcerHome + "/lib/river",
                     Resolver.getRepoDir()
             };
             return (getWebster(policy, port, roots));
         } else {
             String[] roots = {
                     sorcerHome + "/deploy",
                     sorcerHome + "/lib/river",
                    Resolver.getRootDir()
             };
             return (getWebster(policy, port, roots));
         }
     }
 
 
 
     public static ServiceDescriptor getWebster(String policy)
             throws IOException {
             return (getWebster(policy, 0));
     }
 
     /**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Webster.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param roots
 	 *            The roots webster should serve
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 *         webster using an anonymous port. The <tt>webster.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/common/sorcer/webster.com</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getWebster(String policy, String[] roots)
 			throws IOException {
 		return (getWebster(policy, 0, roots));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Webster.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port webster should use
 	 * @param roots
 	 *            The roots webster should serve
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 *         webster using a specified port. The <tt>webster.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/common/sorcer</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getWebster(String policy, int port,
 			String[] roots) throws IOException {
 		return (getWebster(policy, port, roots, null, false));
 	}
 
     public static ServiceDescriptor getWebster(String policy, int port,
 			String address, String[] roots) throws IOException {
 		return (getWebster(policy, port, roots, address, false));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Webster.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port webster should use
 	 * @param roots
 	 *            The roots webster should serve
 	 * @param debug
 	 *            If true, set the <tt>sorcer.tools.debug</tt> property
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 *         webster using a specified port. The <tt>webster.jar</tt> file
 	 *         will be loaded from <tt>${sorcer.home}/common/sorcer</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 */
 	public static ServiceDescriptor getWebster(String policy, int port,
 			String[] roots, String address, boolean debug) throws IOException {
 		return (getWebster(policy, port, roots, address, 0, 0, debug, true));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Webster
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port webster should use
 	 * @param roots
 	 *            The roots webster should serve
 	 * @param debug
 	 *            If true, set the <tt>sorcer.tools.debug</tt> property
 	 *
 	 *
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 *         webster using a specified port. The <tt>webster.jar</tt> file
 	 *         will be loaded from <tt>${sorcer.home}/common/sorcer</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 */
 	public static ServiceDescriptor getWebster(String policy, int port,
 			String[] roots, String address, int startPort, int endPort,
 			boolean debug, boolean isDaemon) throws IOException {
 //		logger.finer("policy: " + policy + ", port: " + port + ", roots: "
 //				+ Arrays.toString(roots) + ", address: " + "startPort: "
 //				+ startPort + ", endPort:" + endPort);
 		int websterPort = 0;
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 		// anonymous case
 		if (port == 0) {
 			websterPort = Booter.getAnonymousPort();
 			System.setProperty("provider.webster.port", "" + websterPort);
 		}
 		else if (port > 0) {
 			websterPort = port;
 		} else {
 			// use SORCER environment configuration
 			websterPort = Booter.getPort();
 		}
 		
 		String importCodeBase = Resolver.resolveAbsolute(WEBSTER);
 		String websterRoots = StringUtils.join(roots, ';');
 		String websterClass = "sorcer.tools.webster.Webster";
 		if (debug) {
 			System.setProperty("webster.debug", "1");
 		}
 		String websterAddress = address;
 		if (address == null || address.length() == 0) {
 			websterAddress = Booter.getWebsterHostName();
 			//Booter.getHostAddressFromProperty("java.rmi.server.hostname");
 		}
 		System.setProperty("provider.webster.interface", websterAddress);
 		logger.fine("webster started at: " + websterAddress + ":" + websterPort + ", hostname: " + address);
 		return (new NonActivatableServiceDescriptor("", policy, importCodeBase,
 				websterClass,
 				new String[] { "-port", "" + websterPort, "-roots", websterRoots,
 						"-bindAddress", websterAddress, "-startPort",
 						"" + startPort, "-endPort", "" + endPort, "-isDaemon", "" + isDaemon }));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Spacer using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param spacerConfig
 	 *            The configuration file the Spacer will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Spacer using an anonymous port. The <tt>jobberConfig</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getSpacer(String policy, String spacerConfig)
 			throws IOException {
 		return (getSpacer(policy, new String[] { spacerConfig }));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Spacer using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param spacerConfig
 	 *            The configuration options the Spacer will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Spacer using an anonymous port. The <tt>spacer.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getSpacer(String policy,
 			String... spacerConfig) throws IOException {
 		return (getSpacer(policy, Booter.getPort(), spacerConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Spacer.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param spacerConfig
 	 *            The configuration options the Spacer will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Spacer using an anonymous port. The <tt>spacer.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getSpacer(String policy, int port,
 			String... spacerConfig) throws IOException {
 		return (getSpacer(policy, Booter.getHostAddress(), port, spacerConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Spacer.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param spacerConfig
 	 *            The configuration options the Spacer will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Spacer using an anonymous port. The <tt>spacer.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getSpacer(String policy,
 			String hostAddress, int port, String... spacerConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 		
 		// service provider classpath
 		String spacerClasspath = Resolver.resolveClassPath(SPACER_PRV, COMMONS_PRV);
 
 		// service provider codebase
         String spacerCodebase = getCodebase(new ArtifactCoordinates[]{
 				SOS_PLATFORM,
 				//SOS_ENV,
 				SERVICEUI,
 				EXERTLET_UI,
 		}, hostAddress, Integer.toString(port));
 		String implClass = "sorcer.core.provider.jobber.ServiceSpacer";
 		return (new SorcerServiceDescriptor(spacerCodebase, policy,
 				spacerClasspath, implClass, spacerConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * ServiceJobber using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param jobberConfig
 	 *            The configuration file the Monitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>jobberConfig</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getJobber(String policy, String jobberConfig)
 			throws IOException {
 		return (getJobber(policy, new String[] { jobberConfig }));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * ServiceJobber using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param jobberConfig
 	 *            The configuration options the Monitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getJobber(String policy,
 			String... jobberConfig) throws IOException {
 		return (getJobber(policy, Booter.getPort(), jobberConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * ServiceJobber
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param jobberConfig
 	 *            The configuration options the Monitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getJobber(String policy, int port,
 			String... jobberConfig) throws IOException {
 		return (getJobber(policy, Booter.getHostAddress(), port, jobberConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * ServiceJobber.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param jobberConfig
 	 *            The configuration options the Monitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getJobber(String policy,
 			String hostAddress, int port, String... jobberConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 		
 		// service provider classpath
 		String jobberClasspath = Resolver.resolveClassPath(JOBBER_PRV, COMMONS_PRV);
 		
 		// service provider codebase
         String jobberCodebase = getCodebase(new ArtifactCoordinates[]{
 				SOS_PLATFORM,
 				//SOS_ENV,
 				SERVICEUI,
 				EXERTLET_UI,
 		}, hostAddress, Integer.toString(port));
 		String implClass = "sorcer.core.provider.jobber.ServiceJobber";
 		return (new SorcerServiceDescriptor(jobberCodebase, policy,
 				jobberClasspath, implClass, jobberConfig));
 
 	}
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
      * ServiceJobber using the Webster port created
      * by this utility.
      *
      * @param policy
      *            The security policy file to use
      * @param exerterConfig
      *            The configuration file the Monitor will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using an anonymous port. The <tt>jobberConfig</tt> file
      *         will be loaded from <tt>sorcer.home/lib</tt>
      *
      * @throws IOException
      *             If there are problems getting the anonymous port
      * @throws RuntimeException
      *             If the <tt>sorcer.home</tt> system property is not set
      */
     public static ServiceDescriptor getExerter(String policy, String exerterConfig)
             throws IOException {
         return (getExerter(policy, new String[] { exerterConfig }));
 
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
      * ServiceJobber using the Webster port created
      * by this utility.
      *
      * @param policy
      *            The security policy file to use
      * @param exerterConfig
      *            The configuration options the Monitor will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
      *         will be loaded from <tt>sorcer.home/lib</tt>
      *
      * @throws IOException
      *             If there are problems getting the anonymous port
      * @throws RuntimeException
      *             If the <tt>sorcer.home</tt> system property is not set
      */
     public static ServiceDescriptor getExerter(String policy,
                                               String... exerterConfig) throws IOException {
         return (getExerter(policy, Booter.getPort(), exerterConfig));
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
      * ServiceJobber
      *
      * @param policy
      *            The security policy file to use
      * @param port
      *            The port to use when constructing the codebase
      * @param exerterConfig
      *            The configuration options the Monitor will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
      *         will be loaded from <tt>sorcer.home/lib</tt>
      *
      * @throws IOException
      *             If there are problems getting the anonymous port
      * @throws RuntimeException
      *             If the <tt>sorcer.home</tt> system property is not set
      */
     public static ServiceDescriptor getExerter(String policy, int port,
                                               String... exerterConfig) throws IOException {
         return (getExerter(policy, Booter.getHostAddress(), port, exerterConfig));
 
     }
 
     /**
      * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
      * {@link sorcer.core.provider.ServiceTasker} with beaned
      * {@link sorcer.util.ExertProcessor}, called Exerter.
      *
      * @param policy
      *            The security policy file to use
      * @param hostAddress
      *            The address to use when constructing the codebase
      * @param port
      *            The port to use when constructing the codebase
      * @param exerterConfig
      *            The configuration options the ExertProcessor provider will use
      * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
      *         Exerter using an anonymous port. The <tt>sorcer-prv.jar</tt> file
      *         will be loaded from <tt>iGrid.home/lib/sorcer/lib</tt>
      *
      * @throws IOException
      *             If there are problems getting the anonymous port
      * @throws RuntimeException
      *             If the <tt>iGrid.home</tt> system property is not set
      */
     public static ServiceDescriptor getExerter(String policy,
                                               String hostAddress, int port, String... exerterConfig)
             throws IOException {
         if (sorcerHome == null)
             throw new RuntimeException("'sorcer.home' property not declared");
 
         // service provider classpath
         String exerterClasspath = Resolver.resolveClassPath(COMMONS_PRV);
 
         // service provider codebase
         String exerterCodebase = getCodebase(new ArtifactCoordinates[]{
                 SOS_PLATFORM,
                 //SOS_ENV,
                 SERVICEUI,
                 EXERTLET_UI,
         }, hostAddress, Integer.toString(port));
         String implClass = "sorcer.core.provider.ServiceTasker";
         return (new SorcerServiceDescriptor(exerterCodebase, policy,
                 exerterClasspath, implClass, exerterConfig));
 
     }
 
     /**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * ExertMonitor using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param exertmonitorConfig
 	 *            The configuration file the ExertMonitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>exertmonitor.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getExertMonitor(String policy, String exertmonitorConfig)
 			throws IOException {
 		return (getExertMonitor(policy, new String[] { exertmonitorConfig }));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * .ExertMonitor} using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param exertmonitorConfig
 	 *            The configuration options the ExertMonitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>exertmonitor.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getExertMonitor(String policy,
 			String... exertmonitorConfig) throws IOException {
 		return (getExertMonitor(policy, Booter.getPort(), exertmonitorConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * .ExertMonitor}.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param exertmonitorConfig
 	 *            The configuration options the ExertMonitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>exertmonitor.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getExertMonitor(String policy, int port,
 			String... exertmonitorConfig) throws IOException {
 		return (getExertMonitor(policy, Booter.getHostAddress(), port, exertmonitorConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * .ExertMonitor}.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param exertmonitorConfig
 	 *            The configuration options the ExertMonitor will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>exertmonitor.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getExertMonitor(String policy,
 			String hostAddress, int port, String... exertmonitorConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 
 
 		// service provider classpath
 		String exertmonitorClasspath = Resolver.resolveClassPath(
                 //SOS_PLATFORM,
 				EXERTMONITOR_SERVICE,
 				SLEEPYCAT,
 				COMMONS_PRV
 		);
 
 	// service provider codebase
         String exertmonitorCodebase = getCodebase(new ArtifactCoordinates[]{
 				SOS_PLATFORM,
 				//SOS_ENV,
 				SERVICEUI,
 				EXERTLET_UI,
 		}, hostAddress, Integer.toString(port));
 
 
 		// service provider codebase
 		String implClass = "sorcer.core.provider.exertmonitor.ExertMonitor";
 		return (new SorcerServiceDescriptor(exertmonitorCodebase, policy,
 				exertmonitorClasspath, implClass, exertmonitorConfig));
 
 	}
 	
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DatabaseStorerusing the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param sdbConfig
 	 *            The configuration file the DatabaseStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         ObjectStore using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDatabaseStorer(String policy, String sdbConfig)
 			throws IOException {
 		return (getDatabaseStorer(policy, new String[] { sdbConfig }));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DatabaseStorerusing the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param sdbConfig
 	 *            The configuration options the DatabaseStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDatabaseStorer(String policy,
 			String... sdbConfig) throws IOException {
 		return (getDatabaseStorer(policy, Booter.getPort(), sdbConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DatabaseStorer.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param sdbConfig
 	 *            The configuration options the DatabaseStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDatabaseStorer(String policy, int port,
 			String... sdbConfig) throws IOException {
 		return (getDatabaseStorer(policy, Booter.getHostAddress(), port, sdbConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DatabaseStorer.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param sdbConfig
 	 *            The configuration options the DatabaseStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDatabaseStorer(String policy,
 			String hostAddress, int port, String... sdbConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 		
 
 		// service provider classpath
 		String dbpc = Resolver.resolveClassPath(
                 //SOS_PLATFORM,
 				DBP_PRV,
 				SLEEPYCAT,
 				COMMONS_PRV
 		);
 		
 		// service provider codebase
         String dbpCodebase = getCodebase(new ArtifactCoordinates[]{
 				SOS_PLATFORM,
 				//SOS_ENV,
 				SERVICEUI,
 				EXERTLET_UI,
 		}, hostAddress, Integer.toString(port));
 		
 		String implClass = "sorcer.core.provider.dbp.DatabaseProvider";
 		return (new SorcerServiceDescriptor(dbpCodebase, policy,
 				dbpc, implClass, sdbConfig));
 
 	}
 	
 	
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DataspaceStorer using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param sdbConfig
 	 *            The configuration file the DataspaceStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         ObjectStore using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDataspaceStorer(String policy, String sdbConfig)
 			throws IOException {
 		return (getDataspaceStorer(policy, new String[] { sdbConfig }));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DataspaceStorer using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param sdbConfig
 	 *            The configuration options the DataspaceStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDataspaceStorer(String policy,
 			String... sdbConfig) throws IOException {
 		return (getDataspaceStorer(policy, Booter.getPort(), sdbConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DataspaceStorer.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param sdbConfig
 	 *            The configuration options the DataspaceStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDataspaceStorer(String policy, int port,
 			String... sdbConfig) throws IOException {
 		return (getDataspaceStorer(policy, Booter.getHostAddress(), port, sdbConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * DataspaceStorer.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param sdbConfig
 	 *            The configuration options the DataspaceStore will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getDataspaceStorer(String policy,
 			String hostAddress, int port, String... sdbConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 		
 		// service provider classpath
 		String dbpc = Resolver.resolveClassPath(DSP_PRV, SLEEPYCAT, COMMONS_PRV);
 		
 		// service provider codebase
         String dbpCodebase = getCodebase(new ArtifactCoordinates[]{
 				SOS_PLATFORM,
 				//SOS_ENV,
 				SERVICEUI,
 				EXERTLET_UI,
 		}, hostAddress, Integer.toString(port));
 		
 		String implClass = "sorcer.core.provider.dsp.DataspaceProvider";
 		return (new SorcerServiceDescriptor(dbpCodebase, policy,
 				dbpc, implClass, sdbConfig));
 
 	}
 	
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Cataloger using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param catalogerConfig
 	 *            The configuration file the Cataloger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Cataloger using an anonymous port. The <tt>catalogerConfig</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getCataloger(String policy, String catalogerConfig)
 			throws IOException {
 		return (getCataloger(policy, new String[] { catalogerConfig }));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Cataloger using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param catalogerConfig
 	 *            The configuration options the Cataloger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Cataloger using an anonymous port. The <tt>cataloger.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getCataloger(String policy,
 			String... catalogerConfig) throws IOException {
 		return (getCataloger(policy, Booter.getPort(), catalogerConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Cataloger.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param catalogerConfig
 	 *            The configuration options the Cataloger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Cataloger using an anonymous port. The <tt>cataloger.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getCataloger(String policy, int port,
 			String... catalogerConfig) throws IOException {
 		return (getCataloger(policy, Booter.getHostAddress(), port, catalogerConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * Cataloger.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param catalogerConfig
 	 *            The configuration options the Cataloger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         Cataloger using an anonymous port. The <tt>cataloger.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getCataloger(String policy,
 			String hostAddress, int port, String... catalogerConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 		
 		// service provider classpath
 		String catalogClasspath = Resolver.resolveClassPath(CATALOGER_PRV, COMMONS_PRV);
 
 		// service provider codebase		
 		String catalogCodebase = getCodebase(new ArtifactCoordinates[]{
 			    SOS_PLATFORM,
 				//SOS_ENV,
 				SERVICEUI,
 				EXERTLET_UI,
 		}, hostAddress, Integer.toString(port));
 		
 		String implClass = "sorcer.core.provider.cataloger.ServiceCataloger";
 		return (new SorcerServiceDescriptor(catalogCodebase, policy,
 				catalogClasspath, implClass, catalogerConfig));
 
 	}
 
 	
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * RemoteLoggerManager using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param loggerConfig
 	 *            The configuration file the ServiceLogger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         ServiceLogger using an anonymous port. The <tt>loggerConfig</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLogger(String policy, String loggerConfig)
 			throws IOException {
 		return (getLogger(policy, new String[] { loggerConfig }));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * RemoteLoggerManager using the Webster port created
 	 * by this utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param loggerConfig
 	 *            The configuration options the ServiceLogger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         ServiceLogger using an anonymous port. The <tt>logger.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLogger(String policy,
 			String... loggerConfig) throws IOException {
 		return (getLogger(policy, Booter.getPort(), loggerConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * RemoteLoggerManager.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param loggerConfig
 	 *            The configuration options the ServiceLogger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         ServiceLogger using an anonymous port. The <tt>logger.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLogger(String policy, int port,
 			String... loggerConfig) throws IOException {
 		return (getLogger(policy, Booter.getHostAddress(), port, loggerConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * RemoteLoggerManager.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase
 	 * @param loggerConfig
 	 *            The configuration options the ServiceLogger will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 *         ServiceLogger using an anonymous port. The <tt>logger.jar</tt> file
 	 *         will be loaded from <tt>sorcer.home/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>sorcer.home</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLogger(String policy,
 			String hostAddress, int port, String... loggerConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' property not declared");
 		
 		// service provider classpath
 		String loggerClasspath = Resolver.resolveClassPath(LOGGER_PRV, LOGGER_SUI, COMMONS_PRV);
 
 		// service provider codebase
 		String loggerCodebase = getCodebase(new ArtifactCoordinates[]{
 				SOS_PLATFORM,
 				//SOS_ENV,
 				SERVICEUI,
 				EXERTLET_UI,
 				LOGGER_SUI,
 		}, hostAddress, Integer.toString(port));
 		// Logger is a partner to ServiceTasker
 		String implClass = "sorcer.core.provider.logger.RemoteLoggerManager";
 		return (new SorcerServiceDescriptor(loggerCodebase, policy,
 				loggerClasspath, implClass, loggerConfig));
 
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 * Jini Lookup Service (Reggie), using the Webster port created by this
 	 * utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param lookupConfig
 	 *            The configuration file Reggie will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 *         Reggie using an anonymous port. The <tt>reggie.jar</tt> file will
 	 *         be loaded from <tt>JINI_HOME/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>JINI_HOME</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLookup(String policy, String lookupConfig)
 			throws IOException {
 		return (getLookup(policy, new String[] { lookupConfig }));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 * Jini Lookup Service (Reggie), using the Webster port created by this
 	 * utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param lookupConfig
 	 *            The configuration file Reggie will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 *         Reggie using an anonymous port. The <tt>reggie.jar</tt> file will
 	 *         be loaded from <tt>JINI_HOME/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>JINI_HOME</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLookup(String policy,
 			String... lookupConfig) throws IOException {
 		return (getLookup(policy, Booter.getPort(), lookupConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 * Jini Lookup Service, Reggie.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @param lookupConfig
 	 *            The configuration options Reggie will use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * @param port
 	 *            The port to use when constructing the codebase Reggie using an
 	 *            anonymous port. The <tt>reggie.jar</tt> file will be loaded
 	 *            from <tt>JINI_HOME/lib</tt>
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>JINI_HOME</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLookup(String policy, int port,
 			String... lookupConfig) throws IOException {
 		return (getLookup(policy, Booter.getHostAddress(), port, lookupConfig));
 	}
 
 	/**
 	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for the
 	 * Jini Lookup Service (Reggie), using the Webster port created by this
 	 * utility.
 	 * 
 	 * @param policy
 	 *            The security policy file to use
 	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
 	 * @param hostAddress
 	 *            The address to use when constructing the codebase
 	 * @param port
 	 *            The port to use when constructing the codebase Reggie using an
 	 *            anonymous port. The <tt>reggie.jar</tt> file will be loaded
 	 *            from <tt>JINI_HOME/lib</tt>
 	 * @param lookupConfig
 	 *            The configuration options Reggie will use
 	 * 
 	 * @throws IOException
 	 *             If there are problems getting the anonymous port
 	 * @throws RuntimeException
 	 *             If the <tt>JINI_HOME</tt> system property is not set
 	 */
 	public static ServiceDescriptor getLookup(String policy,
 			String hostAddress, int port, String... lookupConfig)
 			throws IOException {
 		if (sorcerHome == null)
 			throw new RuntimeException("'sorcer.home' system property not declared");
 		String reggieClasspath = Resolver.resolveClassPath(
 				coords("org.apache.river:reggie"));
 		String reggieCodebase = getCodebase(new ArtifactCoordinates[]{
 				ArtifactCoordinates.coords("org.apache.river:reggie-dl")
 		}, hostAddress, Integer.toString(port));
  		String implClass = "com.sun.jini.reggie.TransientRegistrarImpl";
 		return (new SorcerServiceDescriptor(reggieCodebase, policy,
 				reggieClasspath, implClass, lookupConfig));
 	}
 	
 	protected static String[] getArray(String s, String[] array) {
 		String[] sArray;
 		if (array != null && array.length > 0) {
 			sArray = new String[array.length + 1];
 			sArray[0] = s;
 			System.arraycopy(array, 0, sArray, 1, sArray.length - 1);
 		} else {
 			sArray = new String[] { s };
 		}
 		return (sArray);
 	}
 
 	public static String getCodebase(ArtifactCoordinates[] artifacts, String address, String port) {
 		String[] jars = new String[artifacts.length];
 		for (int i = 0; i < artifacts.length; i++) {
 			jars[i] = Resolver.resolveRelative(artifacts[i]);
 		}
 		return Booter.getCodebase(jars, address, port);
 	}
 }
