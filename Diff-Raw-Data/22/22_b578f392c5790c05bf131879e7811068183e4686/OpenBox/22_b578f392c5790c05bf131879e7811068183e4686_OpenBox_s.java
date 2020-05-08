 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 public class OpenBox {
 	
 	public static int block_size=16*1024;
 	public static long poll_delay=1000;
 	public static long server_sync_delay=15000; 
 	public static int debug_level=1;
 	public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
 	static boolean server=false;
 	static boolean client=false;
 	static boolean use_ssl=true;
 	
 	static boolean set_host_name=false;
 	static String host_name;
 	static boolean set_host_port=false;
 	static int host_port;
 	static boolean set_listen_port=false;
 	static int listen_port;
 	static boolean set_repo_root=false;
 	static String repo_root;
	public static int client_bytes_in_per_second=1000000;
	public static int client_bytes_out_per_second=1000000;
	public static int server_bytes_in_per_second=1000000;
	public static int server_bytes_out_per_second=1000000;
 	public static int num_workers=3;
 	public static long status_period=5000;
 	
 	
 	private final static int bytes_per_kilobyte=1024;
 	private final static int mili_per_second=1000;
 	
 	private static String trust_store_path=null;
 	private static String trust_store_password="123456";
 	
 	
 	
 	public static void log(int level, String s) {
 		if (debug_level>level) {
 			System.out.println( dateFormat.format(new Date()) + "\tThread: " + Thread.currentThread().getId() + "\t"+s);
 		}
 	}	
 	
 	public static void err(boolean exit, String s) {
 		System.out.println( dateFormat.format(new Date()) + "\tThread: " + Thread.currentThread().getId() + "\t"+s);
 		if (exit) {
 			System.exit(1);
 		}
 	}
 	
 	public static void usage() {
 		String program_name="java OpenBox";
 		System.out.println("OpenBox"+"\n"+"------------------");
 		System.out.println("Using in server mode: " + program_name + "-p port_to_listen_on -r repository_root");
 		System.out.println("Using in client mode: " + program_name + "-p port_to_connect_on -s servername_or_ip -r repository_root");
 		System.out.println("------------------");
 		System.out.println("--port/-p\tthe port to connect or listen on");
 		System.out.println("--repo/-r\tthe path to repository root");
 		System.out.println("--server/-s\tthe server ip/hostname to connect to");
 		System.out.println("--up/-u\tthe total maximum kilo-bytes per second upload");
 		System.out.println("--down/-d\tthe total maximum kilo-bytes per second download");
		System.out.println("--threads/-t\tthe number of threads to use");
 		System.out.println("--trust-store-path/-tsp\tthe path to ssl trust/keystore - default : repo_root/myKeystore");
 		System.out.println("--trust-store-password/-tspass\tthe password for the trust/key store - default : \"123456\"");
		System.out.println("--block-size/-bs\tthe block size (in kilo-bytes) to use for file segmentation");
		System.out.println("--file-system-poll-delay/-poll\tthe time (in seconds) to use for file system polling");
		System.out.println("--client-timeout-sync/-cts\tthe maximum time (in seconds) to wait before client checks in with server");
		System.out.println("--status-period/-sp\tthe period (in seconds) between network status updates");
		System.out.println("--no-ssl/-ns\tdon't use ssl");
 		System.out.println("------------------");
 		
 	}
 	
 	
 	
 	public static void main(String[] args) {
 		int up_bytes=0;
 		int down_bytes=0;
 		
 		//parse the command line arguments
 		int port=-1;
 		for (int i=0; i<args.length; i++) {
 			String arg=args[i];
 			
 			//looking for the port number
 			if (arg.equals("-p") || arg.equals("--port")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing port");
 					usage();
 					System.exit(1);
 				}
 				port=Integer.parseInt(args[i]);
 			}
 			
 			//looking for server address
 			if (arg.equals("-s") || arg.equals("--server")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing hostname");
 					usage();
 					System.exit(1);
 				}
 				host_name = args[i];
 				client=true;
 				set_host_name=true;
 			}
 			
 			//looking for repo root
 			if (arg.equals("-r") || arg.equals("--repo")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing repo root");
 					usage();
 					System.exit(1);
 				}
 				repo_root=args[i];
 				set_repo_root=true;
 			}
 
 			//looking for the upload speed
 			if (arg.equals("-u") || arg.equals("--up")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing upload speed");
 					usage();
 					System.exit(1);
 				}
 				up_bytes=Integer.parseInt(args[i])*bytes_per_kilobyte;
 			}
 
 			//looking for the download speed
 			if (arg.equals("-d") || arg.equals("--down")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing download speed");
 					usage();
 					System.exit(1);
 				}
 				down_bytes=Integer.parseInt(args[i])*bytes_per_kilobyte;
 			}
 			
 			//looking for the number of threads
 			if (arg.equals("-t") || arg.equals("--threads")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing number of threads");
 					usage();
 					System.exit(1);
 				}
 				num_workers=Integer.parseInt(args[i]);
 			}
 
 			//looking for trust store path
 			if (arg.equals("-tsp") || arg.equals("--trust-store-path")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing trust-store-path");
 					usage();
 					System.exit(1);
 				}
 				trust_store_path=args[i];
 			}
 			
 
 			//looking for trust store pass
 			if (arg.equals("-tspass") || arg.equals("--trust-store-password")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing trust-store-password");
 					usage();
 					System.exit(1);
 				}
 				trust_store_password=args[i];
 			}
 			
 
 			//looking for block size
 			if (arg.equals("-bs") || arg.equals("--block-size")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing block size");
 					usage();
 					System.exit(1);
 				}
 				block_size=Integer.parseInt(args[i])*bytes_per_kilobyte;
 			}
 			
 			
 			//looking for file system poll delay
 			if (arg.equals("-poll") || arg.equals("--file-system-poll-delay")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing poll delay");
 					usage();
 					System.exit(1);
 				}
 				poll_delay=Integer.parseInt(args[i])*mili_per_second;
 			}
 			
 			//looking for client timeout delay
 			if (arg.equals("-cts") || arg.equals("--client-timeout-sync")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing client timeout sync");
 					usage();
 					System.exit(1);
 				}
 				server_sync_delay=Integer.parseInt(args[i])*mili_per_second;
 			}
 			
 			//looking for status period 
 			if (arg.equals("-sp") || arg.equals("--status-period")) {
 				i++;
 				if (i==args.length) {
 					System.out.println("ERROR missing status period");
 					usage();
 					System.exit(1);
 				}
 				status_period=Integer.parseInt(args[i])*mili_per_second;
 			}
 			
 
 			//looking for no ssl
 			if (arg.equals("-ns") || arg.equals("--no-ssl")) {
 				use_ssl=false;
 			}
 			
 		}
 		if (port>=0) {
 			if (client) {
 				set_host_port=true;
 				host_port=port;
 			} else {
 				set_listen_port=true;
 				listen_port=port;
 			}
 		}
 
 		if (!client) {
 			server=true;
 		}
 		
 		
 		if (client && set_host_port && set_host_name && set_repo_root) { 
 			//everything is good
 		} else if (!client && set_listen_port && set_repo_root) {
 			//everything is good
 		} else {
 			//something is not right!
 			usage();
 			System.exit(1);
 		}
 		
 		File root = new File(repo_root);
 		try {
 			repo_root=root.getCanonicalPath();
 			if (!root.exists()) {
 				OpenBox.err(true,"The repository root does not exist! repo_root="+repo_root);
 			} else if (!root.isDirectory()) {
 				OpenBox.err(true,"The repository root is not a directory! repo_root="+repo_root);
 			}
 		} catch (IOException e) {
 			OpenBox.err(true, "Failed to open repository path : " +repo_root + " , "+ e);
 		}
 		
 		
 		if (repo_root==null) {
 			System.out.println("Must specify the repo root!");
 			System.exit(1);
 		}
 		
 		
 		
 		//set the bw limits
 		if (server) {
 			server_bytes_in_per_second=down_bytes;
 			server_bytes_out_per_second=up_bytes;
 		} else if (client) {
 			client_bytes_in_per_second=down_bytes;
 			client_bytes_out_per_second=up_bytes;
 		}
 		
 		
 		//lets make the initial state
 		State state = new State(repo_root);
 		try {
 			state.update_state();
 		} catch (IOException e) {
 			OpenBox.err(true, "Failed to open repository path : " +repo_root + " , " + e);
 		}
 		
 		
 		if (use_ssl) {
 			//find out what the ssl certificate path is 
 			if (trust_store_path==null) {
 				trust_store_path=repo_root + File.separatorChar +  "myKeystore";
 				File f = new File(trust_store_path);
 				if (!f.exists() || !f.isFile() || !f.canRead()) {
 					OpenBox.err(true, "Cannot find/read ssl keystore! " + trust_store_path);
 				}
 				OpenBox.log(0,"Missing trust_store path, using default " + trust_store_path);
 				
 			}
 			//lets check that the ssl certificate exists!
 			File f = new File(trust_store_path);
 			if (!f.exists() || !f.isFile() || !f.canRead()) {
 				OpenBox.err(true,"SSL certificate file does not exist/is not a file/is not readable! " + trust_store_path);
 			}
 			
 			OpenBox.log(0,"Using ssl " + trust_store_path + " / " + trust_store_password);
 			
 			if (client) {
 				System.setProperty("javax.net.ssl.trustStore", trust_store_path);
 				if (trust_store_password!=null) {
 					System.setProperty("javax.net.ssl.trustStorePassword", trust_store_password);
 				}
 			} else {
 				System.setProperty("javax.net.ssl.keyStore", trust_store_path);
 				if (trust_store_password!=null) {
 					System.setProperty("javax.net.ssl.keyStorePassword", trust_store_password);
 				}
 			}
 		}
 		
 		
 		if (server) {
 
 			listen_port=port;
 			Server s;
 			s = new Server(listen_port, repo_root,state);
 
 			while (true) {
 				s.listen();
 			}
 		} else if (client) {
 			host_port=port;
 			try {
 				Client c = new Client(host_name, host_port, repo_root,state);
 				c.run();
 			} catch (UnknownHostException e) {
 
 				OpenBox.err(true, "Failed to connect to host: " + e);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 }
