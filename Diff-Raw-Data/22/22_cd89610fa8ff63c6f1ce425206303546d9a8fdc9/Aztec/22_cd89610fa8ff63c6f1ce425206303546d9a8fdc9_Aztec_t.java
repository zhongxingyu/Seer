 package lobos.andrew.aztec;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import lobos.andrew.aztec.http.HTTPRequestHandler;
 import lobos.andrew.aztec.plugin.Plugin;
 import lobos.andrew.aztec.plugin.VirtualHost;
 
 public class Aztec {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	
 	public static void registerPlugin(Plugin p)
 	{
 		HTTPRequestHandler.getInstance().registerPlugin(p);
 	}
 	
 	public static void start() throws IOException
 	{
 		new Listener();
 	}
 	
	public static void main(String[] args) 
	{
 		File configPath = new File("AztecConfig.conf");
 		if ( args.length > 0 )
 			configPath = new File(args[0]);
 		if ( !configPath.exists() || !configPath.isFile() || !configPath.canRead() )
 		{
 			System.out.println("Unable to open config file: "+configPath.getAbsolutePath());
 			System.exit(0);
 		}
 		
 		try {
 			Config.init(configPath.getAbsolutePath());
 		} catch (IOException e1) {
 			System.out.println("Reading of config file at "+configPath.getAbsolutePath()+" failed!");
 			e1.printStackTrace();
 			System.exit(0);
 		}		
 		
 		String globalDocroot = Config.getString("global", "docroot", "/var/www");
 		
 		Iterator<String> domains = Config.getDomainSet().iterator();
 		
 		while ( domains.hasNext() )
 		{
 			String thisDomain = (String) domains.next();
 			if ( !thisDomain.equals("global") )
 			{
 				registerPlugin(new VirtualHost(thisDomain, Config.getString(thisDomain, "docroot", globalDocroot)));
 			}
 		}
 		registerPlugin(new VirtualHost(globalDocroot));
 		
 		try { start(); }
 		catch (IOException e)
 		{
 			System.out.println("Aztec failed to start!");
 		}
 	}
 }
