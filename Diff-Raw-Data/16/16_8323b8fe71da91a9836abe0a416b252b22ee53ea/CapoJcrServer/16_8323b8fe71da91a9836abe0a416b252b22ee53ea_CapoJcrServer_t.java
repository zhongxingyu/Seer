 package com.delcyon.capo.server.jackrabbit;
import java.util.HashMap;
 import java.util.logging.Level;
 
 import javax.jcr.NamespaceRegistry;
 import javax.jcr.Repository;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 
 import org.apache.jackrabbit.core.RepositoryImpl;
 import org.apache.jackrabbit.core.config.RepositoryConfig;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.Configuration;
 import com.delcyon.capo.xml.XPath;
 
 public class CapoJcrServer implements Runnable
 {
 	
 	private String xml;
 	private String dir;
 	private RepositoryConfig config;
 	private static Repository repository;
 	
 	public static Repository getRepository()
 	{
 		return repository;
 	}
 	
 	public CapoJcrServer()
 	{
 		String configDir = CapoApplication.getConfiguration().getValue(Configuration.PREFERENCE.CONFIG_DIR);
 		String capoDir = CapoApplication.getConfiguration().getValue(Configuration.PREFERENCE.CAPO_DIR);
 		xml = capoDir+"/"+configDir+"/repository.xml";
 		dir = capoDir;
 	}
 	
 	@Override
 	public void run()
 	{
 		try
 		{
 			config = RepositoryConfig.create(xml, dir);
 			Repository repository = RepositoryImpl.create(config);
 			Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
 			NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
			String[] uris = namespaceRegistry.getURIs();
			HashMap<String, String> uriHashMap = new HashMap<>(uris.length);
			for (String uri : uris)
            {
                uriHashMap.put(uri, "");
            }
			if(uriHashMap.containsKey(CapoApplication.SERVER_NAMESPACE_URI) == false)
 			{
 			    namespaceRegistry.registerNamespace("server", CapoApplication.SERVER_NAMESPACE_URI);
 			}
			if(uriHashMap.containsKey(CapoApplication.CLIENT_NAMESPACE_URI) == false)
 			{
 			    namespaceRegistry.registerNamespace("client", CapoApplication.CLIENT_NAMESPACE_URI);
 			}
			if(uriHashMap.containsKey(CapoApplication.RESOURCE_NAMESPACE_URI) == false)
 			{
 			    namespaceRegistry.registerNamespace("resource", CapoApplication.RESOURCE_NAMESPACE_URI);
 			}
 			session.logout();
 			CapoJcrServer.repository = repository;
 		} catch (Exception exception)
 		{
 			exception.printStackTrace();
 		}
 	}
 	
 	public void start() throws Exception
 	{
 		Thread serverThread = new Thread(this, "JackrabbitServerThread");
 		serverThread.start();
 		CapoApplication.logger.log(Level.INFO, "Waiting for jackrabbit to start");
 		while (repository == null)
 		{
 			Thread.sleep(1000);
 		}
 		CapoApplication.logger.log(Level.INFO, "Apache Jackrabbit started");
 	}
 
 
 	public void shutdown() throws Exception
 	{
 		((RepositoryImpl) repository).shutdown();
 		
 	}
 	
 }
