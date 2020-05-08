 
 import  java.io.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Random;
 
 public class client{
   
    String 		lwd;
    String 		rwd;
    String		temp_folder;
    String		config_file;
    String 		username;
    String 		password;
    messaging[] 	servers;
    String[]     server_list;
    String		ca_server_ip = "127.0.0.1";
    int 		  	port_num;
    int 		  	qur_size;
    public static void main (String[] args) {
 	client cl = new client(1,10001);
 	//lwd = System.getProperty("user.dir");
 	cl.rwd = "";
       	while(true){
 			System.out.print("quarac> ");
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			String ccmd = null;
 			try {
 				ccmd = br.readLine();
 			} catch (IOException ioe) {
 				 System.out.println("IO error trying to read your name!");
 				 System.exit(1);
 			}
 			StringTokenizer Tok = new StringTokenizer(ccmd);
 			if(!Tok.hasMoreElements()) continue;
 			String cmdstr = (String)Tok.nextElement();
 			int cmdargc =Tok.countTokens();
 			String[] cmdargs = new String[cmdargc];
 			for(int i = 0; i < cmdargc; i++) cmdargs[i] = (String)Tok.nextElement(); 
 			// System.out.println(cmdstr);
 			if(cmdstr.equals("quit") || cmdstr.equals("exit")) return;
 			// Transactions
 			else if(cmdstr.equals("login")) cl.login(cmdargs);
 			else if(cmdstr.equals("get")) cl.get(cmdargs);
 			else if(cmdstr.equals("put")) cl.put(cmdargs);
 			// Local Commands
 			else if(cmdstr.equals("config")) cl.config(cmdargs);
 			else if(cmdstr.equals("help")) cl.help(cmdargs);
 			else if(cmdstr.equals("lls")) cl.lls(cmdargs);
 			else if(cmdstr.equals("lpwd")) cl.lpwd(cmdargs);
 			else if(cmdstr.equals("lcd")) cl.lcd(cmdargs);
 			// Remote Commands
 			else if(cmdstr.equals("cd")) cl.cd(cmdargs);
 			else if(cmdstr.equals("ls")) cl.ls(cmdargs);
 			else if(cmdstr.equals("pwd")) cl.pwd(cmdargs);
 			else System.out.println("Invalid Command");
 	}
    }	
    client(int qs,int pn){
 	// Initialize
 	qur_size = qs;
 	port_num = pn;
 	lwd =  new File(".").getAbsolutePath();
 	temp_folder = lwd + "/.temp_qurac";
 	config_file = lwd + "/.quarac_config";
 	String ip_addr = "127.0.0.1";
 	server_list = new String[qur_size];
 	for(int i =0; i <qur_size;i++) server_list[i] = ip_addr;
 	File rconf = new File(config_file);
 	if(rconf.exists()) read_config(rconf);
 	File tf = new File(temp_folder);
 	if(!tf.exists()) tf.mkdirs();
 	servers = new messaging[qur_size];
    }
    void config(String[] argv){
 	   System.out.println("config_file= " + config_file);
 	   System.out.println("temp_folder= " + temp_folder);
 	   System.out.println("qur_size= " + qur_size);
 	   System.out.println("port_num= " + port_num);
 	   System.out.println("[server_list]");
 	   for(int i = 0; i < server_list.length; i++) System.out.println(server_list[i]);
 	   System.out.println("[server_list]");
    };
 	// Commands
 	// Transactions
 	void login(String[] argv){
 		String dom = "";
 		System.out.println("Domain: ");
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         try {
          dom = br.readLine();
          find_domain(dom);
         } catch (IOException ioe) {
          System.out.println("IO error trying to read your name!");
          System.exit(1);
         }
 		System.out.println("Username: ");
         try {
         	username = br.readLine();
         } catch (IOException ioe) {
 	        System.out.println("IO error trying to read your name!");
 	        System.exit(1);
         } 
 		System.out.println("Password: ");
         try {
         	password = br.readLine();
         } catch (IOException ioe) {
              System.out.println("IO error trying to read your password!");
              System.exit(1);
         } 
 		System.out.println("login:  " + username + "@" + dom + " with: " + password);
                 //  read the username from the command-line; need to use try/catch with the
                 //  readLine() method
 		String username_hash = username;//+ password; // TODO: CHANGE THIS TO INCLUDE VINAY's stuff	
 		generate_random_connections();
 		for(int i = 0; i< qur_size; i++){
 			if(!servers[i].clientLogin(username_hash,ca_server_ip)){
 				System.out.println("Login failed!");
 				return;
 			}
 		}
 	}
 	void get(String[] argv){
 		QFILE[] file_list = new QFILE[qur_size];
 		// File not found is -1
 		// Incorrect command string sent -2
 		if(argv.length < 2) System.out.println("Not enough arguments");
 		System.out.println("Attempting to retrieve file: " + rwd + "/" + argv[1]);
 		 for(int i = 0; i< qur_size; i++){
 			 // Grab data from each remote server
 			 System.out.println("Grabbing file from a server");
 			 servers[i].get(temp_folder +"/temp_file_" + i, rwd + "/" + argv[1]);
 			 file_list[i] = new QFILE(new File(temp_folder +"/temp_file_" + i),	// Temp file directory
 					 					0,										// timestamp
 					 					i										// ServerID
 					 				);
 		 }
 		 quorum.quorum_files_static(file_list, qur_size,lwd + "/" + argv[0]);
 	}
 	void put(String[] argv){
 		 //String local_path;
 		 if(argv.length != 2) return;
                  for(int i = 0; i< qur_size; i++){
                 	 int opcode = servers[i].put(lwd + "/" +  argv[0], rwd + "/" + argv[1],"tempstamp"); //TODO: Add correct timestamp names
                	 if(opcode == -1) System.out.println("Failed to communicate with the server!");
                  }
 	}
 	// Local Commands
 	void help(String[] argv){	
 		System.out.println(	 "This program allows for read and write to a remote file repository.");
 		System.out.println(	 "List of Commands: ");
 		System.out.println(	 "help: prints this help message.");
 		System.out.println(	 "login [username]@[domain]: reloads the login prompt for username and will " );
 		System.out.println(	 "                           attempt to connect to the domain");
 		System.out.println(	 "quit: Exits this prompt with code 0");
 		System.out.println(	 "Local System Commands:");
 		System.out.println(	 "lls -alF [path]: Displays the local directory path relative to");
 		System.out.println(	 "                 the current working directory.");
 		System.out.println(	 "lcd [path]: Changes the local current working directory to path relative to lpwd.");
 		System.out.println(	 "lpwd: Displays the present working directory on the local computer.");
 		System.out.println(	 "Remote System Commands:");
 		System.out.println(         "ls -alF [path]: Displays the directory path  on the remote system relative to"); 
 		System.out.println(         "                the current working directory.");
 		System.out.println(         "cd [path]: Changes the current working directory on the remote system to path.");
 		System.out.println(         "pwd: Displays the present working directory on the remote system.");
 		System.out.println(	 "File System Interaction: ");
 		System.out.println(	 "mkdir [name]: creates the directory name on the remote system.");
 		System.out.println(	 "rmdir [name]: Removes the directory name on the remote system fails if name is not empty.");
 		System.out.println(	 "rm -rf [name]: Removes the file name. If name is a directory -r will delete recursively.");
 		System.out.println(	 "put -r [name]: Puts the file name on the remote system.");
 		System.out.println(	 "               If name is a directory -r will add recursively");
 		System.out.println(	 "get -r [name]: Gets the file name from the remote system.");
 		System.out.println(	 "               If name is a directory -r will recursively get the directory structure");
 	}
 	void lls(String[] argv){
 		String path;
                 if(argv.length == 0) path = lwd;
                 else if(argv.length > 1) {System.out.println("This does not accept that many arguments"); return;}
                 else {
 			if(argv[0].charAt(0) == '/') path = argv[0];
                 	else path = lwd + "/" + argv[0];
 		}
                 File check = new File(path);
                 if(check.exists() && check.isDirectory())
 		 	list(check,false,false,true);
                 else System.out.println("Invalid path to a directory");
 
 	}
 	void lpwd(String[] argv){System.out.println(lwd);};
 	void lcd(String[] argv){
 		String path;
 		if(argv.length == 0) return;
 		if(argv.length > 1) {System.out.println("This does not accept that many arguments"); return;}
 		if(argv[0].charAt(0) == '/') path = argv[0];
 		else path = lwd + "/" + argv[0];
 		File check = new File(path);
 		if(check.exists() && check.isDirectory()) lwd = check.getAbsolutePath();
 		else System.out.println("Invalid path to a directory");	
 	}
 	// Remote Commands
 	void cd(String[] argv){
 		boolean succ = true;
 		 for(int i = 0; i< qur_size; i++){
              int opcode = servers[i].ls(temp_folder +"/" + i +"_ls.txt", rwd + "/" + argv[1]);
 		 }
 	}
 	void ls(String[] argv){
 		generate_random_connections();
 		int opcode[] = new int[qur_size];
 		QFILE[] file_list = new QFILE[qur_size];
 		 for(int i = 0; i< qur_size; i++){
 			 if(argv.length  == 0)
 				 opcode[i] = servers[i].ls(temp_folder +"/" + i +"_ls.txt", rwd + "/");
 			 else if(argv.length  == 1)
 				 opcode[i] = servers[i].ls(temp_folder +"/" + i +"_ls.txt", rwd + "/" + argv[0]);
 			 else{ System.out.println("Incorrect number of inputs!");
 			 		return;
 			 }
 			 file_list[i] = new QFILE(new File(temp_folder +"/" + i +"_ls.txt"),	// Temp file directory
 	 					0,															// timestamp
 	 					i															// ServerID
 	 				);
 		 }
 		 if(quorum.quorum_files_static(file_list, qur_size, temp_folder + "/ls.txt") == -1){
 			 System.out.println("Unable to sucessfully generate ls faultly servers!"); return;
 		 }
 		 File ls_file = new File(temp_folder + "/ls.txt");
 		 System.out.println("Printing remote folder:");
 		 FileInputStream inst = null;
 			String ccmd = null;
 			try {
 				inst = new FileInputStream(ls_file); 
 				BufferedReader bis = new BufferedReader(new InputStreamReader(inst));
 				String str = "";
 				while((str = bis.readLine()) != null) System.out.println(str);
 			} catch (IOException ioe) {
 				 System.out.println("IO error trying to read ls back!");
 				 System.exit(1);
 			}
 			
 		 
 	}
 	void pwd(String[] argv){System.out.println(rwd);};	
 	void generate_random_connections(){
 		List<Integer> pick = new ArrayList<Integer>();
 		Random randomGenerator = new Random();
 		while(pick.size() < qur_size){
 			Integer rand = randomGenerator.nextInt(server_list.length);
 			while( pick.contains(rand) ) rand = randomGenerator.nextInt(server_list.length);
 			pick.add(rand);
 		}
 		servers = new messaging[qur_size];
 		for(int i = 0; i < qur_size;i++) {
 			System.out.println("Connecting to server#" + pick.get(i) + " : " + server_list[pick.get(i)]);
 			servers[i] = new messaging(server_list[pick.get(i)],port_num);
 		}
 	}
 	private static void list(File dir, boolean recur, boolean size, boolean hidden) {
 	    if ((dir != null) && dir.exists() && dir.isDirectory()) {
 		File[] filelist = dir.listFiles();
 		for (int index = 0; index < filelist.length; index++) {
 		    File file = filelist[index];
 		    // Print hidden Files
 		    if (hidden || !file.isHidden()) {
 			    String name = file.getName();
 			    // Print Size
 			    if (file.isDirectory()) System.out.println(name+'/');
 			    else System.out.println(name);
 
 		    }
 		}
 	    } else {
 		System.err.println("Error: " + dir + " is not a Directory!");
 	    }
 	}
 	void read_config(File config){
 		   BufferedReader br = null;
 		   Pattern tf = Pattern.compile("temp_folder=(.*)");
 		   try {
 			   String sCurrentLine;
 				br = new BufferedReader(new FileReader(config.getAbsolutePath()));
 				while ((sCurrentLine = br.readLine()) != null) {
 					Matcher m = tf.matcher(sCurrentLine);
 					if(m.find()) temp_folder = lwd + "/" + m.group(1);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (br != null)br.close();
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 	   }
 	void find_domain(String domain){
 		   // Helper function to search the config file for the domain
 		   System.out.println("Locating Domain: " + domain);
 		   BufferedReader br = null;
 		   List<String> temp_list = new ArrayList<String>();
 		   boolean df = false;
 		   Pattern dom = Pattern.compile("\\[([^\\]]*)\\]");
 		   try {
 			   String sCurrentLine;
 				br = new BufferedReader(new FileReader(config_file));
 				while ((sCurrentLine = br.readLine()) != null) {
 					Matcher m = dom.matcher(sCurrentLine);
 					if(m.find()){ 
 						if(m.group(1).equals(domain)) df = !df; 
 						continue;
 					}
 					if(df) temp_list.add(sCurrentLine);
 				}
 				if(temp_list.size() != 0){
 					server_list = new String[ temp_list.size() ];
 					temp_list.toArray( server_list );
 					// CALCULATE QUORUM SIZE
 					qur_size = temp_list.size() >> 1;
 				}
 				else{
 					System.out.println("Unable to locate targeted domain: "+ domain);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (br != null)br.close();
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 		   
 	   }
 }
