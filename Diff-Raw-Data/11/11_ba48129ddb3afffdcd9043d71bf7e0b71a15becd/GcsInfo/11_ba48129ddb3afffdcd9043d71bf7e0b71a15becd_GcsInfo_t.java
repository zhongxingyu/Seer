 package pt.com.gcs.conf;
 
 import java.io.File;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.io.FilenameUtils;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.global.BrokerSecurityPolicy;
 import pt.com.gcs.conf.agent.AgentConfig;
import pt.com.gcs.conf.agent.AgentConfig.Ssl;
 
 public class GcsInfo
 {
 	private static Logger log = LoggerFactory.getLogger(GcsInfo.class);
 
 	public static final String VERSION = "@gcsVersion@";
 
 	private static final GcsInfo instance = new GcsInfo();
 
 	public static String getAgentHost()
 	{
 		String prop = instance.conf.getNet().getIp();
 		if (StringUtils.isBlank(prop))
 		{
 			log.error("Fatal error: Must define valid host.");
 			Shutdown.now();
 		}
 		return prop;
 	}
 
 	public static String getAgentName()
 	{
 		String prop = instance.conf.getName();
 		if (StringUtils.isBlank(prop))
 		{
 			log.error("Fatal error: Must define an Agent name.");
 			Shutdown.now();
 		}
 		return prop;
 	}
 
 	public static int getAgentPort()
 	{
 		int iprop = instance.conf.getNet().getPort();
 		return iprop;
 	}
 
 	public static String getBasePersistentDirectory()
 	{
 		String prop = instance.conf.getPersistency().getDirectory();
 		String defaultDir = FilenameUtils.normalizeNoEndSeparator(System.getProperty("user.dir")) + File.separator + "persistent";
 		if (StringUtils.isBlank(prop))
 		{
 			log.warn("No directory for persistent storage. Using default: {}", defaultDir);
 			return defaultDir;
 		}
 		else
 		{
 			return FilenameUtils.normalizeNoEndSeparator(prop);
 		}
 	}
 
 	public static int getInitialDelay()
 	{
 		int iprop = instance.conf.getNet().getDiscoveryDelay();
 		return iprop;
 	}
 
 	public static String getStatisticsTopic()
 	{
 		String prop = instance.conf.getStatistics().getTopic();
 		return prop;
 	}
 
 	public static String getConfigVersion()
 	{
 		String prop = instance.conf.getConfigVersion();
 		return prop;
 	}
 
 	public static String getGlobalConfigFilePath()
 	{
 		String prop = instance.conf.getNet().getFileRef();
 		if (StringUtils.isBlank(prop))
 		{
 			log.error("Fatal error: Must define a valid path for the world map file.");
 			Shutdown.now();
 		}
 		return prop;
 	}
 
 	private AgentConfig conf;
 
 	private GcsInfo()
 	{
 		String filePath = System.getProperty("config-path");
 		if (StringUtils.isBlank(filePath))
 		{
 			log.error("Fatal error: No configuration file defined. Please set the enviroment variable 'config-path' to valid path for the configuration file");
 			Shutdown.now();
 		}
 		try
 		{
 			JAXBContext jc = JAXBContext.newInstance("pt.com.gcs.conf.agent");
 			Unmarshaller u = jc.createUnmarshaller();
 
 			File f = new File(filePath);
 			boolean b = f.exists();
 			conf = (AgentConfig) u.unmarshal(f);
 		}
 		catch (JAXBException e)
 		{
 			// Throwable t = ErrorAnalyser.findRootCause(e);
 			log.error("Fatal error: {}", e.getMessage());
 			Shutdown.now(e);
 		}
 	}
 
 	public static int getBrokerUdpPort()
 	{
 		int iprop = instance.conf.getNet().getBrokerUdpPort();
 		return iprop;
 	}
 
 	public static int getBrokerHttpPort()
 	{
 		int iprop = instance.conf.getNet().getBrokerHttpPort();
 		return iprop;
 	}
 
 	public static int getBrokerPort()
 	{
 		int iprop = instance.conf.getNet().getBrokerPort();
 		return iprop;
 	}
 
 	public static int getBrokerLegacyPort()
 	{
 		int iprop = instance.conf.getNet().getBrokerLegacyPort();
 		return iprop;
 	}
 
 	public static boolean isDropboxEnabled()
 	{
 		return instance.conf.getMessaging().getDropbox().isEnabled();
 	}
 
 	public static String getDropBoxDir()
 	{
 		return instance.conf.getMessaging().getDropbox().getDir();
 	}
 
 	public static int getDropBoxCheckInterval()
 	{
 		return instance.conf.getMessaging().getDropbox().getCheckInterval();
 	}
 
 	// Access Control related methods
 	
 	public static boolean useAccessControl()
 	{
 		return getSecurityPolicies() != null;
 	}
 	
 	public static BrokerSecurityPolicy getSecurityPolicies()
 	{
 		return GlobalConfig.getSecurityPolicies();
 	}
 	
 	// SSL related methods
 
 	public static boolean createSSLInterface()
 	{
 		return instance.conf.getSsl() != null;
 	}
 	
 	public static int getBrokerSSLPort()
 	{
		System.out.println("GcsInfo.getBrokerSSLPort()");
		Ssl ssl = instance.conf.getSsl();
		if(ssl == null){
			System.out.println("NO SSL");
			return -1;
		}
			
		int sslPort = ssl.getBrokerSslPort();
 		return sslPort;
 	}
 	
 	public static String getKeystoreLocation()
 	{
 		if( instance.conf.getSsl() != null)
 			return instance.conf.getSsl().getKeystoreLocation();
 		return null;
 	}
 
 	public static String getKeystorePassword()
 	{
 		if( instance.conf.getSsl() != null)
 			return instance.conf.getSsl().getKeystorePassword();
 		return null;
 	}
 	
 	public static String getKeyPassword()
 	{
 		if( instance.conf.getSsl() != null)
 			return instance.conf.getSsl().getKeyPassword();
 		return null;
 	}
 	
 	// STS related properties
 	
 	
 	public static Map<String, ProviderInfo> getAuthenticationProviders()
 	{
 		return GlobalConfig.getAuthenticationProviders();
 	}
 	
 	public static Map<String, ProviderInfo> getCredentialValidatorProviders()
 	{
 		return GlobalConfig.getCredentialValidatorProviders();
 	}
 }
