 package poke.server.Launcher;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import poke.server.Server;
 import poke.server.conf.JsonUtil;
 import poke.server.conf.ServerConf;
 import poke.server.resources.ResourceFactory;
 
 public class ServerLauncher {
 	
 	private static Logger logger = LoggerFactory.getLogger("serverlauncher");
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//			if (args.length != 1) {
 		//				System.err.println("Usage: java "
 		//						+ Server.class.getClass().getName() + " conf-file");
 		//				System.exit(1);
 		//			}
 
 		File cfg = new File("runtime/server.conf");
 		if (!cfg.exists()) {
 			logger.error("configuration file does not exist: " + cfg);
 			System.exit(2);
 		}
 		
 		ServerConf conf = readConfig(cfg);
 		
 		List<ServerConf.GeneralConf> servers = conf.getServer();
 		List<Server> svrs = new LinkedList<Server>();
		for( ServerConf.GeneralConf generalConf : servers ) {
 			Server svr = new Server(conf);
 			svrs.add(svr);
			svr.run(generalConf);
 		}
 	}
 
 	private static ServerConf readConfig(File cfg) {
 		ServerConf conf = null;
 		// resource initialization - how message are processed
 		BufferedInputStream br = null;
 		try {
 			byte[] raw = new byte[(int) cfg.length()];
 			br = new BufferedInputStream(new FileInputStream(cfg));
 			br.read(raw);
 			conf = JsonUtil.decode(new String(raw), ServerConf.class);
 			ResourceFactory.initialize(conf);
 		} catch (Exception e) {
 		}
 		return conf;
 	}
 }
