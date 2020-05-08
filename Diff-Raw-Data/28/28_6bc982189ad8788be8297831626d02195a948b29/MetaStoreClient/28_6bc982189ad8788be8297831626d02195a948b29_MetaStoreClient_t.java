 package iie.metastore;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.hadoop.hive.metastore.HiveMetaHook;
 import org.apache.hadoop.hive.metastore.HiveMetaHookLoader;
 import org.apache.hadoop.hive.metastore.IMetaStoreClient;
 import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
 import org.apache.hadoop.hive.metastore.MetaStoreConst;
 import org.apache.hadoop.hive.metastore.api.Datacenter;
 import org.apache.hadoop.hive.metastore.api.FileOperationException;
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
 import org.apache.hadoop.hive.metastore.api.Node;
 import org.apache.hadoop.hive.metastore.api.SFile;
 import org.apache.hadoop.hive.metastore.api.SFileLocation;
 import org.apache.thrift.TApplicationException;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TProtocolException;
 import org.apache.thrift.transport.TTransportException;
 
 import devmap.DevMap;
 import devmap.DevMap.DevStat;
 
 public class MetaStoreClient {
 	// client should be the local datacenter;
 	public Datacenter local_dc;
 	public IMetaStoreClient client;
 	private ConcurrentHashMap<String, IMetaStoreClient> climap = new ConcurrentHashMap<String, IMetaStoreClient>();
 	
 	public static <T> T newInstance(Class<T> theClass,
 			Class<?>[] parameterTypes, Object[] initargs) {
 		// Perform some sanity checks on the arguments.
 		if (parameterTypes.length != initargs.length) {
 			throw new IllegalArgumentException(
 					"Number of constructor parameter types doesn't match number of arguments");
 		}
 		for (int i = 0; i < parameterTypes.length; i++) {
 			Class<?> clazz = parameterTypes[i];
 			if (!(clazz.isInstance(initargs[i]))) {
 				throw new IllegalArgumentException("Object : " + initargs[i]
 						+ " is not an instance of " + clazz);
 			}
 		}
 
 		try {
 			Constructor<T> meth = theClass
 					.getDeclaredConstructor(parameterTypes);
 			meth.setAccessible(true);
 			return meth.newInstance(initargs);
 		} catch (Exception e) {
 			throw new RuntimeException("Unable to instantiate "
 					+ theClass.getName(), e);
 		}
 	}
 
 	public static class RetryingMetaStoreClient implements InvocationHandler {
 
 		private final IMetaStoreClient base;
 		private final int retryLimit;
 		private final int retryDelaySeconds;
 
 		protected RetryingMetaStoreClient(
 				String msUri, int retry, int retryDelay, 
 				HiveMetaHookLoader hookLoader,
 				Class<? extends IMetaStoreClient> msClientClass)
 				throws MetaException {
 			this.retryLimit = retry;
 			this.retryDelaySeconds = retryDelay;
 			this.base = (IMetaStoreClient) newInstance(
 					msClientClass, new Class[] { String.class, Integer.class, Integer.class, HiveMetaHookLoader.class }, 
 					new Object[] { msUri, new Integer(retry), new Integer(retryDelay), hookLoader });
 		}
 		
 		public static Class<?> getClass(String rawStoreClassName)
 				throws MetaException {
 			try {
 				return Class.forName(rawStoreClassName, true,
 						JavaUtils.getClassLoader());
 			} catch (ClassNotFoundException e) {
 				throw new MetaException(rawStoreClassName + " class not found");
 			}
 		}
 
 		public static IMetaStoreClient getProxy(String msUri, int retry, int retryDelay, HiveMetaHookLoader hookLoader, String mscClassName)
 				throws MetaException {
 
 			Class<? extends IMetaStoreClient> baseClass = (Class<? extends IMetaStoreClient>) getClass(mscClassName);
 
 			RetryingMetaStoreClient handler = new RetryingMetaStoreClient(msUri, retry, retryDelay, hookLoader, baseClass);
 
 			return (IMetaStoreClient) Proxy.newProxyInstance(
 					RetryingMetaStoreClient.class.getClassLoader(),
 					baseClass.getInterfaces(), handler);
 		}
 
 		@Override
 		public Object invoke(Object proxy, Method method, Object[] args)
 				throws Throwable {
 			Object ret = null;
 			int retriesMade = 0;
 			TException caughtException = null;
 			while (true) {
 				try {
 					ret = method.invoke(base, args);
 					break;
 				} catch (UndeclaredThrowableException e) {
 					throw e.getCause();
 				} catch (InvocationTargetException e) {
 					if ((e.getCause() instanceof TApplicationException)
 							|| (e.getCause() instanceof TProtocolException)
 							|| (e.getCause() instanceof TTransportException)) {
 						caughtException = (TException) e.getCause();
 					} else if ((e.getCause() instanceof MetaException)
 							&& e.getCause().getMessage()
 									.matches("JDO[a-zA-Z]*Exception")) {
 						caughtException = (MetaException) e.getCause();
 					} else {
 						throw e.getCause();
 					}
 				}
 
 				if (retriesMade >= retryLimit) {
 					throw caughtException;
 				}
 				retriesMade++;
 				System.out.println(
 						"MetaStoreClient lost connection. Attempting to reconnect." + 
 						caughtException);
 				Thread.sleep(retryDelaySeconds * 1000);
 				base.reconnect();
 			}
 			return ret;
 		}
 	}
 	
 	public IMetaStoreClient createMetaStoreClient() throws MetaException {
 		return createMetaStoreClient("localhost", 9083);
 	}
 	
 	public IMetaStoreClient createMetaStoreClient(String uri) throws MetaException {
 		HiveMetaHookLoader hookLoader = new HiveMetaHookLoader() {
 			public HiveMetaHook getHook(
 					org.apache.hadoop.hive.metastore.api.Table tbl)
 					throws MetaException {
 
 				return null;
 			}
 		};
 		return RetryingMetaStoreClient.getProxy(uri, 5, 1, hookLoader, HiveMetaStoreClient.class.getName());
 	}
 
 	public IMetaStoreClient createMetaStoreClient(String serverName, int port) throws MetaException {
 		HiveMetaHookLoader hookLoader = new HiveMetaHookLoader() {
 			public HiveMetaHook getHook(
 					org.apache.hadoop.hive.metastore.api.Table tbl)
 					throws MetaException {
 
 				return null;
 			}
 		};
 		return RetryingMetaStoreClient.getProxy("thrift://" + serverName + ":" + port, 5, 1, hookLoader, HiveMetaStoreClient.class.getName());
 	}
 	
 	public MetaStoreClient() throws MetaException {
 		client = createMetaStoreClient();
 		initmap(false);
 	}
 	
 	public MetaStoreClient(String serverName, int port) throws MetaException {
 		client = createMetaStoreClient(serverName, port);
 		initmap(false);
 	}
 
 	public MetaStoreClient(String serverName, boolean preconnect) throws MetaException {
 		client = createMetaStoreClient(serverName, 9083);
 		initmap(preconnect);
 	}
 	
 	public MetaStoreClient(String serverName) throws MetaException {
 		client = createMetaStoreClient(serverName, 9083);
 		initmap(false);
 	}
 
 	
 	private void initmap(boolean preconnect) throws MetaException {
 		// get local datacenter
 		try {
 			this.local_dc = client.get_local_center();
 		} catch (TException e) {
 			throw new MetaException(e.toString());
 		}
 		climap.put(local_dc.getName(), client);
 		
 		// get all datacenters
 		List<Datacenter> ld;
 		try {
 			ld = client.get_all_centers();
 		} catch (TException e) {
 			throw new MetaException(e.toString());
 		}
 		for (Datacenter dc : ld) {
 			if (!dc.getName().equals(local_dc.getName())) {
 				if (preconnect) {
 					System.out.println("Try to connect to Datacenter " + dc.getName() + ", uri=" + dc.getLocationUri());
 					try { 
 						IMetaStoreClient cli = createMetaStoreClient(dc.getLocationUri());
 						climap.put(dc.getName(), cli);
 					} catch (MetaException me) {
 						System.out.println("Connect to Datacenter " + dc.getName() + ", uri=" + dc.getLocationUri() + " failed!");
 					}
 				}
 			}
 		}
 	}
 	
 	public IMetaStoreClient getCli(String dc_name) {
 		IMetaStoreClient cli =  climap.get(dc_name);
 		if (cli == null) {
 			// do reconnect now
 			try {
 				Datacenter rdc = client.get_center(dc_name);
 				cli = createMetaStoreClient(rdc.getLocationUri());
 				climap.put(dc_name, cli);
 			} catch (NoSuchObjectException e) {
 				System.out.println(e);
 			} catch (MetaException e) {
 				System.out.println(e);
 			} catch (TException e) {
 				System.out.println(e);
 			}
 		}
 		return cli;
 	}
 	
 	public IMetaStoreClient getLocalCli() {
 		return client;
 	}
 	
 	public void stop() {
 		if (client != null) {
 			client.close();
 		}
 		for (Map.Entry<String, IMetaStoreClient> e : climap.entrySet()) {
 			e.getValue().close();
 		}
 	}
 	
 	public static String toStringSFile(SFile file) {
 		if (file == null) {
 			return "null";
 		}
 		
 		String r = "<";
 		r += "fid:" + file.getFid() + ", ";
 		r += "placement:" + file.getPlacement() + ", ";
 		r += "storestatus: " + file.getStore_status() + ", ";
 		r += "repnr: " + file.getRep_nr() + ", ";
 		r += "digest: " + file.getDigest() + ", ";
 		r += "record_nr: " + file.getRecord_nr() + ", ";
 		r += "allrecord_nr: " + file.getAll_record_nr() + ", [\n";
 		if (file.getLocations() != null) {
 			for (SFileLocation loc : file.getLocations()) {
 				r += loc.getNode_name() + ":" + loc.getDevid() + ":"
 						+ loc.getLocation() + ":" + loc.getRep_id() + ":"
 						+ loc.getUpdate_time() + ":" + loc.getVisit_status()
 						+ ":" + loc.getDigest() + "\n";
 			}
 		} else {
 			r += "NULL";
 		}
 		r += "]>";
 		
 		return r;
 	}
 	
 	public static boolean runRemoteCmd(String cmd) throws IOException {
 		Process p = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", cmd});
 		try {
 			InputStream err = p.getErrorStream();
 			InputStreamReader isr = new InputStreamReader(err);
 			BufferedReader br = new BufferedReader(isr);
 
 			String line = null;
 
 			System.out.println("<ERROR>");
 
 			while ((line = br.readLine()) != null)
 
 				System.out.println(line);
 			System.out.println("</ERROR>");
 
 			int exitVal = p.waitFor();
 			System.out.println(" -> exit w/ " + exitVal);
 			if (exitVal > 0)
 				return false;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	public static String runRemoteCmdWithResult(String cmd) throws IOException {
 		Process p = Runtime.getRuntime().exec(new String[] {"/bin/bash", "-c", cmd});
 		String result = "";
 		
 		try {
 			InputStream err = p.getErrorStream();
 			InputStreamReader isr = new InputStreamReader(err);
 			BufferedReader br = new BufferedReader(isr);
 
 			String line = null;
 
 			System.out.println("<ERROR>");
 
 			while ((line = br.readLine()) != null) {
 				System.out.println(line);
 			}
 			System.out.println("</ERROR>");
			
			InputStream out = p.getInputStream();
			isr = new InputStreamReader(out);
			br = new BufferedReader(isr);

			System.out.println("<OUTPUT>");

			while ((line = br.readLine()) != null) {
				result += line;
				System.out.println(line);
			}
			System.out.println("</OUTPUT>");
 
 			int exitVal = p.waitFor();
 			System.out.println(" -> exit w/ " + exitVal);
 			if (exitVal > 0)
 				return result;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	public static class Option {
 	     String flag, opt;
 	     public Option(String flag, String opt) { this.flag = flag; this.opt = opt; }
 	}
 	
 	public static void main(String[] args) throws IOException {
 		MetaStoreClient cli = null;
 		String node = null;
 		String serverName = null;
 		int serverPort = 9083;
 		List<String> ipl = new ArrayList<String>();
 		int repnr = 3;
 		SFile file = null, r = null;
 		List<String> argsList = new ArrayList<String>();  
 	    List<Option> optsList = new ArrayList<Option>();
 	    List<String> doubleOptsList = new ArrayList<String>();
 	    String dbName = null, tableName = null, partName = null, to_dc = null, to_db = null, to_nas_devid = null,
 	    		tunnel_in = null, tunnel_out = null, tunnel_node = null, tunnel_user = null;
 	    
 	    // parse the args
 	    for (int i = 0; i < args.length; i++) {
 	    	System.out.println("Args " + i + ", " + args[i]);
 	        switch (args[i].charAt(0)) {
 	        case '-':
 	            if (args[i].length() < 2)
 	                throw new IllegalArgumentException("Not a valid argument: "+args[i]);
 	            if (args[i].charAt(1) == '-') {
 	                if (args[i].length() < 3)
 	                    throw new IllegalArgumentException("Not a valid argument: "+args[i]);
 	                doubleOptsList.add(args[i].substring(2, args[i].length()));
 	            } else {
 	                if (args.length-1 > i)
 	                    if (args[i + 1].charAt(0) == '-') {
 	                    	optsList.add(new Option(args[i], null));
 	                    } else {
 	                    	optsList.add(new MetaStoreClient.Option(args[i], args[i+1]));
 	                    	i++;
 	                    }
 	                else {
 	                	optsList.add(new Option(args[i], null));
 	                }
 	            }
 	            break;
 	        default:
 	            // arg
 	            argsList.add(args[i]);
 	            break;
 	        }
 	    }
 		
 	    for (Option o : optsList) {
 	    	if (o.flag.equals("-h")) {
 	    		// print help message
 	    		System.out.println("-r : server name.");
 	    		System.out.println("-n : add localhost as a new node.");
 	    		System.out.println("-f : test file operations.");
 	    		break;
 	    	}
 	    	if (o.flag.equals("-r")) {
 	    		// set servername;
 	    		serverName = o.opt;
 	    	}
 	    	if (o.flag.equals("-p")) {
 	    		// set serverPort
 	    		serverPort = Integer.parseInt(o.opt);
 	    	}
 	    	if (o.flag.equals("-table")) {
 	    		// set table name
 	    		if (o.opt == null) {
 	    			System.out.println("-table tableName");
 	    			System.exit(0);
 	    		}
 	    		tableName = o.opt;
 	    	}
 	    	if (o.flag.equals("-db")) {
 	    		// set db name
 	    		if (o.opt == null) {
 	    			System.out.println("-db dbName");
 	    			System.exit(0);
 	    		}
 	    		dbName = o.opt;
 	    	}
 	    	if (o.flag.equals("-part")) {
 	    		// set part name
 	    		if (o.opt == null) {
 	    			System.out.println("-part partName");
 	    			System.exit(0);
 	    		}
 	    		partName = o.opt;
 	    	}
 	    	if (o.flag.equals("-todc")) {
 	    		// set to_dc name
 	    		if (o.opt == null) {
 	    			System.out.println("-todc target_dc");
 	    			System.exit(0);
 	    		}
 	    		to_dc = o.opt;
 	    	}
 	    	if (o.flag.equals("-todb")) {
 	    		// set to_db name
 	    		if (o.opt == null) {
 	    			System.out.println("-todb target_db");
 	    			System.exit(0);
 	    		}
 	    		to_db = o.opt;
 	    	}
 	    	if (o.flag.equals("-tonasdev")) {
 	    		// set to_nas_devid name
 	    		if (o.opt == null) {
 	    			System.out.println("-tonasdev NAS_DEVID");
 	    			System.exit(0);
 	    		}
 	    		to_nas_devid = o.opt;
 	    	}
 	    	if (o.flag.equals("-tunnel_in")) {
 	    		// set tunnel name
 	    		if (o.opt == null) {
 	    			System.out.println("-tunnel_in TUNNEL_IN_PATH");
 	    			System.exit(0);
 	    		}
 	    		tunnel_in = o.opt;
 	    	}
 	    	if (o.flag.equals("-tunnel_out")) {
 	    		// set tunnel_out name
 	    		if (o.opt == null) {
 	    			System.out.println("-tunnel_out TUNNEL_OUT_PATH");
 	    			System.exit(0);
 	    		}
 	    		tunnel_out = o.opt;
 	    	}
 	    	if (o.flag.equals("-tunnel_node")) {
 	    		// set tunnel_node name
 	    		if (o.opt == null) {
 	    			System.out.println("-tunnel_node TUNNEL_NODE_NAME");
 	    			System.exit(0);
 	    		}
 	    		tunnel_node = o.opt;
 	    	}
 	    	if (o.flag.equals("-tunnel_user")) {
 	    		// set tunnel_user name
 	    		if (o.opt == null) {
 	    			System.out.println("-tunnel_user TUNNEL_USER_NAME");
 	    			System.exit(0);
 	    		}
 	    		tunnel_user = o.opt;
 	    	}
 	    }
 	    if (cli == null) {
 	    	try {
 	    		if (serverName == null)
 	    			cli = new MetaStoreClient();
 	    		else
 	    			cli = new MetaStoreClient(serverName, serverPort);
 			} catch (MetaException e) {
 				e.printStackTrace();
 				System.exit(0);
 			}
 	    }
 	    try {
 			node = InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 		}
 
 	    for (Option o : optsList) {
 	    	if (o.flag.equals("-m")) {
 	    		// migrate
 	    		if (dbName == null || tableName == null || partName == null || to_dc == null) {
 	    			System.out.println("Please set dbname,tableName,partName,to_dc!");
 	    			System.exit(0);
 	    		}
 	    		List<String> partNames = new ArrayList<String>();
 	    		partNames.add(partName);
 	    		try {
 	    			cli.client.migrate_out(dbName, tableName, partNames, to_dc);
 	    		} catch (MetaException me) {
 	    			me.printStackTrace();
 	    			break;
 	    		} catch (TException e) {
 	    			e.printStackTrace();
 	    			break;
 	    		}
 	    	}
 	    	if (o.flag.equals("-m21")) {
 	    		// migrate by NAS stage 1
 	    		if (dbName == null || tableName == null || partName == null || to_dc == null || to_nas_devid == null || tunnel_in == null ||
 	    				tunnel_out == null || tunnel_node == null) {
 	    			System.out.println("Please set dbname,tableName,partName,to_dc,tonasdev,tunnel_in,tunnel_out,tunnel_node!");
 	    			System.exit(0);
 	    		}
 	    		if (tunnel_user == null) {
 	    			System.out.println("#Copy using default user, this might not be your purpose.");
 	    		}
 	    		List<String> partNames = new ArrayList<String>();
 	    		partNames.add(partName);
 	    		try {
 	    			List<SFileLocation> lsfl = cli.client.migrate2_stage1(dbName, tableName, partNames, to_dc);
 	    			//SFileLocation t = new SFileLocation("", 90, "devid", "/data/default/swjl/1031676804", 9, 1000, 1, "digest");
 	    			//lsfl.add(t);
 	    			if (lsfl.size() > 0) {
 	    				// copy NAS or non-NAS LOC to TUNNEL 
 	    				for (SFileLocation sfl : lsfl) {
 	    					if (sfl.getNode_name().equals("")) {
	    						System.out.println("#Get NAS LOC " + sfl.getDevid() + ":" + sfl.getLocation() + " : " + sfl.getDigest());
 	    					} else {
 	    						System.out.println("#Get non-NAS LOC " + sfl.getDevid() + ":" + sfl.getLocation());
 	    					}
 	    					// calculate the source path: tunnel_in + location
 	    					String sourceFile = tunnel_in + sfl.getLocation();
 	    					// calculate the target path
 	    					String targetFile = tunnel_out + sfl.getLocation();
 	    					// create the target directory now
 	    					File tf = new File(targetFile);
 	    					System.out.println("#Create parent DIR " + "@" + tunnel_node + ": " + tf.getParent());
 	    					String cmd = "mkdir -p " + tf.getParent() + "; " + "chmod ugo+rw " + tf.getParent() + ";";
 	    					System.out.println(cmd);
 	    						    					
 	    					// do copy now
 	    					if (sfl.getNode_name().equals("")) {
 	    						System.out.println("#Copy     NAS SFL by TUNNEL: " + sourceFile + " -> " + tf.getParent());
 	    						cmd = "cp -r " + sourceFile + " " + tf.getParent() + "; " + "chmod -R ugo+rw " + targetFile + ";";
 	    						System.out.println(cmd);
 	    						cmd = "find " + targetFile + " -type f -exec md5sum {} + | awk '{print $1}' | sort | md5sum | awk '{print $1}';";
 	    						System.out.println(cmd);
 	    					} else {
 	    						// reset sourceFile
 	    						sourceFile = cli.client.getMP(sfl.getNode_name(), sfl.getDevid()) + "/" + sfl.getLocation();
 	    						System.out.println("#Copy non-NAS SFL by TUNNEL: " + sourceFile + " -> " + tf.getParent());
 	    						cmd = "scp -r metastore@" + sfl.getNode_name() + ":" + sourceFile + " " + tf.getParent() + "; " + "chmod -R ugo+rw " + targetFile + ";";
 	    						System.out.println(cmd);
 	    						cmd = "find " + targetFile + " -type f -exec md5sum {} + | awk '{print $1}' | sort | md5sum | awk '{print $1}';";
 	    						System.out.println(cmd);
 	    					}
 	    				}
 	    			} else {
 	    				System.out.println("No data to migrate");
 	    			}
 	    		} catch (MetaException me) {
 	    			me.printStackTrace();
 	    			break;
 	    		} catch (TException e) {
 	    			e.printStackTrace();
 	    			break;
 	    		}
 	    	}
 	    	if (o.flag.equals("-m22")) {
 	    		// migrate by NAS
 	    		if (dbName == null || tableName == null || partName == null || to_dc == null || to_nas_devid == null || tunnel_in == null ||
 	    				tunnel_out == null || tunnel_node == null) {
 	    			System.out.println("Please set dbname,tableName,partName,to_dc,tonasdev,tunnel_in,tunnel_out,tunnel_node!");
 	    			System.exit(0);
 	    		}
 	    		List<String> partNames = new ArrayList<String>();
 	    		partNames.add(partName);
 	    		try {
 	    			List<SFileLocation> lsfl = cli.client.migrate2_stage1(dbName, tableName, partNames, to_dc);
 	    			if (lsfl.size() > 0) {
 	    				// migrate by NAS stage2
 	    				if (cli.client.migrate2_stage2(dbName, tableName, partNames, to_dc, to_db, to_nas_devid)) {
 	    					System.out.println("Migrate2 Stage2 Done.");
 	    				} else 
 	    					System.out.println("Migrate2 Stage2 Failed.");
 	    			} else {
 	    				System.out.println("No data to migrate");
 	    			}
 	    		} catch (MetaException me) {
 	    			me.printStackTrace();
 	    			break;
 	    		} catch (TException e) {
 	    			e.printStackTrace();
 	    			break;
 	    		}
 	    	}
 	    	if (o.flag.equals("-m2")) {
 	    		// migrate by NAS
 	    		if (dbName == null || tableName == null || partName == null || to_dc == null || to_nas_devid == null || tunnel_in == null ||
 	    				tunnel_out == null || tunnel_node == null) {
 	    			System.out.println("Please set dbname,tableName,partName,to_dc,tonasdev,tunnel_in,tunnel_out,tunnel_node!");
 	    			System.exit(0);
 	    		}
 	    		if (tunnel_user == null) {
 	    			System.out.println("Copy using default user, this might not be your purpose.");
 	    		}
 	    		List<String> partNames = new ArrayList<String>();
 	    		partNames.add(partName);
 	    		try {
 	    			List<SFileLocation> lsfl = cli.client.migrate2_stage1(dbName, tableName, partNames, to_dc);
 	    			//SFileLocation t = new SFileLocation("", 90, "devid", "/data/default/swjl/1031676804", 9, 1000, 1, "digest");
 	    			//lsfl.add(t);
 	    			if (lsfl.size() > 0) {
 	    				// copy NAS or non-NAS LOC to TUNNEL 
 	    				for (SFileLocation sfl : lsfl) {
 	    					if (sfl.getNode_name().equals("")) {
	    						System.out.println("Get NAS LOC " + sfl.getDevid() + ":" + sfl.getLocation() + " : " + sfl.getDigest());
 	    					} else {
 	    						System.out.println("Get non-NAS LOC " + sfl.getDevid() + ":" + sfl.getLocation());
 	    					}
 	    					// calculate the source path: tunnel_in + location
 	    					String sourceFile = tunnel_in + sfl.getLocation();
 	    					// calculate the target path
 	    					String targetFile = tunnel_out + sfl.getLocation();
 	    					// create the target directory now
 	    					File tf = new File(targetFile);
 	    					System.out.println("Create parent DIR " + "@" + tunnel_node + ": " + tf.getParent());
 	    					String cmd = "ssh " + (tunnel_user == null ? "" : tunnel_user + "@") + tunnel_node + " 'mkdir -p " + tf.getParent() + "; " + "chmod ugo+rw " + tf.getParent() + ";'";
 	    					System.out.println(cmd);
 	    					if (!runRemoteCmd(cmd)) {
 	    						System.exit(1);
 	    					}
 	    						    					
 	    					// do copy now
 	    					if (sfl.getNode_name().equals("")) {
 	    						System.out.println("Copy     NAS SFL by TUNNEL: " + sourceFile + " -> " + tf.getParent());
 	    						cmd = "ssh " + (tunnel_user == null ? "" : tunnel_user + "@") + tunnel_node + " 'cp -r " + sourceFile + " " + tf.getParent() + "; " + "chmod -R ugo+rw " + targetFile + ";'";
 	    						System.out.println(cmd);
 	    						if (!runRemoteCmd(cmd)) {
 	    							System.exit(1);
 	    						}
 	    						
	    						cmd = "ssh " + (tunnel_user == null ? "" : tunnel_user + "@") + tunnel_node;
	    						cmd += " find " + targetFile + " -type f -exec md5sum {} + | awk '{print $1}' | sort | md5sum | awk '{print $1}';";
 	    						System.out.println(cmd);
 	    						String md5 = runRemoteCmdWithResult(cmd);
 	    						if (!sfl.getDigest().equalsIgnoreCase(md5) && !sfl.getDigest().equals("MIGRATE2-DIGESTED!") && !sfl.getDigest().equals("REMOTE-DIGESTED!") && !sfl.getDigest().equals("SFL_DEFAULT")) {
 	    							System.out.println("MD5 mismatch: original MD5 " + sfl.getDigest() + ", target MD5 " + md5 + ".");
 	    							System.exit(1);
 	    						}
 	    					} else {
 	    						// reset sourceFile
 	    						sourceFile = cli.client.getMP(sfl.getNode_name(), sfl.getDevid()) + "/" + sfl.getLocation();
 	    						System.out.println("Copy non-NAS SFL by TUNNEL: " + sourceFile + " -> " + tf.getParent());
 	    						cmd = "ssh " + (tunnel_user == null ? "" : tunnel_user + "@") + tunnel_node + " 'scp -r metastore@" + sfl.getNode_name() + ":" + sourceFile + " " + tf.getParent() + "; " + "chmod -R ugo+rw " + targetFile + ";'";
 	    						System.out.println(cmd);
 	    						if (!runRemoteCmd(cmd)) {
 	    							System.exit(1);
 	    						}
 	    						
	    						cmd = "ssh " + (tunnel_user == null ? "" : tunnel_user + "@") + tunnel_node;
	    						cmd += " \"find " + targetFile + " -type f -exec md5sum {} + | awk '{print $1}' | sort | md5sum | awk '{print $1}';\"";
 	    						System.out.println(cmd);
 	    						String md5 = runRemoteCmdWithResult(cmd);
 	    						if (!sfl.getDigest().equalsIgnoreCase(md5) && !sfl.getDigest().equals("MIGRATE2-DIGESTED!") && !sfl.getDigest().equals("REMOTE-DIGESTED!") && !sfl.getDigest().equals("SFL_DEFAULT")) {
 	    							System.out.println("MD5 mismatch: original MD5 " + sfl.getDigest() + ", target MD5 " + md5 + ".");
 	    							System.exit(1);
 	    						}
 	    					}
 	    				}
 	    				// begin stage2
 	    				if (cli.client.migrate2_stage2(dbName, tableName, partNames, to_dc, to_db, to_nas_devid)) {
 	    					System.out.println("Migrate2 Stage2 Done.");
 	    				} else 
 	    					System.out.println("Migrate2 Stage2 Failed.");
 	    			} else {
 	    				System.out.println("No data to migrate");
 	    			}
 	    		} catch (MetaException me) {
 	    			me.printStackTrace();
 	    			break;
 	    		} catch (TException e) {
 	    			e.printStackTrace();
 	    			break;
 	    		}
 	    	}
 			if (o.flag.equals("-n")) {
 				// add Node
 				try {
 					ipl.add(InetAddress.getLocalHost().getHostAddress());
 					System.out.println("Add Node: " + node + ", IPL: " + ipl.get(0));
 					Node n = cli.client.add_node(node, ipl);
 				} catch (MetaException me) {
 					me.printStackTrace();
 					break;
 				} catch (TException e) {
 					e.printStackTrace();
 					return;
 				} catch (UnknownHostException e) {
 					e.printStackTrace();
 					break;
 				}
 			}
 			if (o.flag.equals("-nn")) {
 				// add Node with specified name
 				if (o.opt != null) {
 					try {
 						ipl.add(InetAddress.getLocalHost().getHostAddress());
 						System.out.println("Add Node: " + o.opt + ", IPL: " + ipl.get(0));
 						Node n = cli.client.add_node(o.opt, ipl);
 					} catch (UnknownHostException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					} catch (MetaException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (TException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			if (o.flag.equals("-dn")) {
 				// del Node
 				try {
 					cli.client.del_node(o.opt);
 				} catch (MetaException e) {
 					e.printStackTrace();
 					break;
 				} catch (TException e) {
 					e.printStackTrace();
 					break;
 				}
 			}
 			if (o.flag.equals("-ln")) {
 				// list all Nodes
 				List<Node> lns;
 				try {
 					lns = cli.client.get_all_nodes();
 				} catch (MetaException e) {
 					e.printStackTrace();
 					break;
 				} catch (TException e) {
 					e.printStackTrace();
 					break;
 				}
 				for (Node n : lns) {
 					System.out.println("Node '" + n.getNode_name() + "' {" + n.getIps().toString() + "}");
 				}
 			}
 			if (o.flag.equals("-dms")) {
 				// get DM status
 				String dms;
 				try {
 					dms = cli.client.getDMStatus();
 				} catch (MetaException e) {
 					e.printStackTrace();
 					break;
 				} catch (TException e) {
 					e.printStackTrace();
 					break;
 				}
 				System.out.println(dms);
 			}
 			if (o.flag.equals("-fcr")) {
 				// create a new file and return the fid
 				try {
 					file = cli.client.create_file(node, repnr, null, null);
 					System.out.println("Create file: " + toStringSFile(file));
 				} catch (FileOperationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if (o.flag.equals("-frr")) {
 				// read the file object
 				try {
 					file = cli.client.get_file_by_id(Long.parseLong(o.opt));
 					for (SFileLocation sfl : file.getLocations()) {
 						System.out.println("SFL: node " + sfl.getNode_name() + ", dev " + sfl.getDevid() + ", loc " + sfl.getLocation());
 					}
 				} catch (NumberFormatException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (FileOperationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (MetaException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if (o.flag.equals("-fcl")) {
 				// close the file
 				try {
 					file = cli.client.get_file_by_id(Long.parseLong(o.opt));
 					file.setDigest("MSTOOL Digested!");
 					cli.client.close_file(file);
 					System.out.println("Close file: " + toStringSFile(file));
 					DevMap dm = new DevMap();
 					String path = dm.getPath(file.getLocations().get(0).getDevid(), file.getLocations().get(0).getLocation());
 					System.out.println("File local location is : " + path);
 					File nf = new File(path);
 					nf.mkdirs();
 				} catch (NumberFormatException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (FileOperationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (MetaException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if (o.flag.equals("-fcd")) {
 				// delete the file
 				try {
 					file = cli.client.get_file_by_id(Long.parseLong(o.opt));
 					cli.client.rm_file_physical(file);
 				} catch (FileOperationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (MetaException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if (o.flag.equals("-f")) {
 				// test file
 				try {
 					file = cli.client.create_file(node, repnr, null, null);
 					System.out.println("Create file: " + toStringSFile(file));
 					// write something here
 					String filepath;
 					DevMap dm = new DevMap();
 					dm.refreshDevMap();
 					DevStat ds;
 					do {
 						ds = dm.findDev(file.getLocations().get(0).getDevid());
 						if (ds == null || ds.mount_point == null) {
 							dm.refreshDevMap();
 						} else 
 							break;
 					} while (true);
 					filepath = ds.mount_point + "/" + file.getLocations().get(0).getLocation();
 					System.out.println("Trying to write to file location: " + filepath);
 					File newfile = new File(filepath);
 					try {
 						newfile.getParentFile().mkdirs();
 						newfile.createNewFile();
 						FileOutputStream out = new FileOutputStream(filepath);
 						out.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 						cli.client.rm_file_physical(file);
 						break;
 					}
 					file.setDigest("DIGESTED!");
 					cli.client.close_file(file);
 					System.out.println("Closed file: " + toStringSFile(file));
 					r = cli.client.get_file_by_id(file.getFid());
 					while (r.getStore_status() != MetaStoreConst.MFileStoreStatus.REPLICATED) {
 						try {
 							Thread.sleep(10000);
 							r = cli.client.get_file_by_id(file.getFid());
 						} catch (InterruptedException ex) {
 							Thread.currentThread().interrupt();
 						}
 					}
 					System.out.println("Read   file: " + toStringSFile(r));
 					cli.client.rm_file_physical(r);
 					System.out.println("DEL    file: " + r.getFid());
 				} catch (FileOperationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 	    }
 	}
 }
