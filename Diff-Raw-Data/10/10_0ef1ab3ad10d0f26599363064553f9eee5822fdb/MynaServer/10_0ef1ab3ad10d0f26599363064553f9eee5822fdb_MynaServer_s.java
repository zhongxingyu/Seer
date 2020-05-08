 package bootstrap;
 
 import org.apache.commons.cli.*;
 import java.io.IOException;
 import java.io.File;
  
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 import org.apache.commons.io.FileUtils; 
 import org.apache.commons.io.IOUtils;
 import java.util.zip.*;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.lang.reflect.*;
 
 import org.apache.commons.lang.StringUtils;
 
 public class MynaServer extends Thread
 {
 	public static Server 					server;
 	public static boolean 					hasWatchdog	= false;
 	public static String 					webctx 		= "/";
 	public static String						webroot		= "./myna";
 	public static String						logFile		= null;
 	public static int							port			= 8180;
 	public static Process					p				= null;
 	public static java.util.List 			javaOpts 	= new java.util.ArrayList();
 	public static java.util.Properties 	props			= new java.util.Properties();
 	public static boolean					isJar			= false;
 	public static String 					mode 			= "";
 	public static String 					user 			= "nobody";
 	public static Vector						modeOptions	= new Vector();
 	public static String 					classUrl;
 	
 	
 	
 	public static void restart() throws Exception
 	{
 		(new Thread() {
 			public void run() {
 				System.out.print("Stoping server...");
 			try {
 				server.stop();
 				server.join();
 				server.destroy();
 				System.out.println("Stopped.");
 			} catch(Exception e) {}
 			}
 		}).start();
 		if (hasWatchdog) {
 			System.exit(1);      
 		}
 	}
 	public void run() 
 	{
 		if (MynaServer.p != null) p.destroy();
 	}
 	public static void main(String[] args) throws Exception
 	{
 		modeOptions.add("server");
 		modeOptions.add("upgrade");
 		modeOptions.add("install");
 		classUrl = MynaServer.class.getResource("MynaServer.class").toString();
 		isJar = (classUrl.indexOf("jar") == 0);
 	
 		Thread.sleep(1000);
 		
 		
 		CommandLineParser parser = new PosixParser();
 	
 		// create the Options
 		Options options = new Options();
 		options.addOption( "c", "context", true, "Webapp context. Must Start with \"/\" Default: " + webctx);
 		options.addOption( "h", "help", false, "Displays help." );
 		options.addOption( "l", "logfile", true, "Log file to use. Will be created if it does not exist. Default: ./<context>.log" );
 		options.addOption( "m", "mode", true, "Mode: one of "+modeOptions.toString()+". \n"+
 			"*server:	 unpacks to webroot and launches  Myna Server\n"+
 			"*upgrade: upgrades myna installation in webroot and exits"
 		);
 		options.addOption( "p", "port", true, "Webserver port. Default: " + port );
 		options.addOption( "w", "webroot", true, "Webroot to use. Will be created if webroot/WEB-INF does not exist. Default: " + webroot );
 		options.addOption( "u", "user", true, "User to own and run the Myna installation. Only applies to unix installs. Default: nobody" );
 		
 		HelpFormatter formatter = new HelpFormatter();
 		
 		String cmdSyntax = "MynaServer -m <mode> [options] [ -- [jvm arguments]]";
 		try {
 			if (args.length == 0){
 				formatter.printHelp(cmdSyntax, options );
 				System.exit(1);	
 			}
 			CommandLine line = parser.parse( options, args );
 			
 			if( line.hasOption( "help" ) ) {
 				formatter.printHelp(cmdSyntax, options);
 				System.exit(0);
 			}
 			
 			if( line.hasOption( "mode" ) ) {
 				mode = line.getOptionValue( "mode" );
 				if (!modeOptions.contains(mode)){
 					System.err.println( "Invalid Arguments.  Reason: Mode must be in " + modeOptions.toString());
 					formatter.printHelp( cmdSyntax, options );
 					System.exit(0);
 				}
 				
 			}
 			
 			if( line.hasOption( "port" ) ) {
 				port = Integer.parseInt(line.getOptionValue( "port" ));
 			}
 			if( line.hasOption( "context" ) ) {
 				webctx=line.getOptionValue( "context" );
 			}
 			if( line.hasOption( "user" ) ) {
 				user=line.getOptionValue( "user" );
 			}
 			if( line.hasOption( "logfile" ) ) {
 				logFile= line.getOptionValue( "logfile" );
 			} else if (!webctx.equals("/")){
 				logFile=webctx.substring(1)+".log";	
 			} else{
 				logFile="myna.log";
 			}
 			if( line.hasOption( "webroot" ) ) {
 				webroot=line.getOptionValue( "webroot" );
 			}
 			javaOpts = line.getArgList();
 		} 
 		catch (ParseException exp ) {
 			System.err.println( "Invalid Arguments.	Reason: " + exp.getMessage() );
 			
 			formatter.printHelp(cmdSyntax, options );
 			System.exit(1);
 		}
 		File wrFile = new File(webroot);
 		webroot= wrFile.toString();
 		//unpack myna if necessary
 		if (!wrFile.exists() || mode.equals("upgrade") || mode.equals("install")){
 			upgrade(wrFile);
 		}
 		
 		if (mode.equals("install")){
 			if (java.lang.System.getProperty("os.name").toLowerCase().equals("windows")){
 				System.out.println("installation is currently only supported on Linux platforms using /etc/init.d");
 			} else if (new File("/etc/init.d").exists()){
 				String curUser=java.lang.System.getProperty("user.name") ;
 				if (!curUser.equals("root")){
 					System.out.println("Install mode must be run as root.");
 					System.exit(1);
 				}
 				
				String serverName = webctx.replaceAll("\\/","")
 				if (!new File(logFile).isAbsolute()){
 					logFile = new File(wrFile.toURI().resolve("WEB-INF/" + logFile)).toString();
 				}
 				File templateFile = new File(
 					wrFile.toURI().resolve("WEB-INF/myna/install/linux/init_script")
 				);          
 				String initScript=FileUtils.readFileToString(templateFile)
 				.replaceAll("\\{webctx\\}",webctx)      
 				.replaceAll("\\{server\\}",serverName)
 				.replaceAll("\\{user\\}",user)
 				.replaceAll("\\{webroot\\}",webroot)
 				.replaceAll("\\{logfile\\}",logFile)
 				.replaceAll("\\{port\\}",new Integer(port).toString());
 				
 				File scriptFile =new File(
 					wrFile.toURI().resolve("WEB-INF/" + serverName)
 				);          
 				
 				FileUtils.writeStringToFile(scriptFile,initScript);
 				
 				/*templateFile = new File(
 					wrFile.toURI().resolve("WEB-INF/myna/install/linux/install_script")
 				);  
 				String installScript=FileUtils.readFileToString(templateFile)
 				.replaceAll("\\{init_script\\}",scriptFile.toString())
 				.replaceAll("\\{webctx\\}",webctx)      
 				.replaceAll("\\{server\\}",server)
 				.replaceAll("\\{user\\}",user)
 				.replaceAll("\\{webroot\\}",webroot)
 				.replaceAll("\\{logfile\\}",logFile)
 				.replaceAll("\\{port\\}",new Integer(port).toString()); */
 				
 				
 				exec("chown  -R "+user+" "+webroot);
 				exec("chown root " +scriptFile.toString());
 				exec("chmod 700 " +scriptFile.toString());
 				exec("cp " +scriptFile.toString() +" /etc/init.d/");
 				
 
 				
 				System.out.println("\nInit script '/etc/init.d/" + serverName +"' created with the following settings:\n");
 				System.out.println("user=" + user);
 				System.out.println("memory=256MB");
 				System.out.println("server="+ serverName);
 				System.out.println("context=" +webctx);
 				System.out.println("port=" + port);
 				System.out.println("myna_home=" + webroot);
 				System.out.println("logfile=" + logFile);
 				
 				
 				System.out.println("\nEdit this file to customize startup behavior");
 			} else {
 				System.out.println("installation is currently only supported on Linux platforms using /etc/init.d");
 			}
 		}
		
		if (mode.equals("server")){
 			try {
 				FileOutputStream fileOut = new FileOutputStream(logFile,true);
 				PrintStream newOut= new PrintStream(fileOut,true);
 				System.out.println("Launching Myna Server. See " + new File(logFile).getCanonicalPath() +" for details.");
 				System.setOut(newOut);
 				System.setErr(newOut);
 				System.out.println("logging to: " + logFile);
 			} catch(Exception logEx){
 				System.out.println("Unable to log output to '" + logFile+"'. Make sure the path exists and is writable by this user.");	
 			}
 			
			hasWatchdog = System.getProperty("myna.hasWatchdog") != null;
 			if (hasWatchdog){
 				runAsServer();
 			} else {//run as watchdog and spawn a separate process
 				runAsWatchdog();
 			}
 		}
 	}
 	public static void runAsServer() throws Exception{
 		server = new Server(port);
 		System.out.println(
 			"Using web root: " +webroot
 		);
 		WebAppContext context = new WebAppContext();
 		context.setDescriptor("/WEB-INF/web.xml");
 		context.setResourceBase(webroot);
 		context.setContextPath(webctx);
 		context.setParentLoaderPriority(true);
 		
 		server.setHandler(context);
 		
 		server.start();
 		server.join();
 	}
 	public static void runAsWatchdog() throws Exception{
 		java.lang.Runtime rt = java.lang.Runtime.getRuntime();
 		rt.addShutdownHook(new MynaServer());
 		String osName 		= System.getProperty("os.name").toLowerCase();
 		String javaHome		= System.getProperty("java.home");
 		String pathSep 		= System.getProperty("path.separator");
 		String fileSep 		= System.getProperty("file.separator");
 		String additionalCp	= System.getProperty("myna.classpath");
 		String web_inf =webroot+fileSep+"WEB-INF"+fileSep;
 		String cp = System.getProperty("java.class.path");
 		//if we are launching from a jar file, we ignore the CP and pull from the 
 		//webroot's WEB-INF directory
 		if (isJar){
 			cp = 
 				web_inf+"classes" + pathSep
 				+web_inf+"lib" + fileSep+"*";
 			
 		}
 		//additional CP paths can be added via -Dmyna.classpath
 		if (additionalCp != null){
 			cp += fileSep +additionalCp;	
 		}
 		 
 		
 		String javaExe;
 		if (osName.indexOf("windows") != -1){
 			javaExe = javaHome + "\\bin\\java.exe ";
 		} else {
 			javaExe = javaHome + "/bin/java ";
 		}
 		
 		
 		
 		StringBuffer cmd =new StringBuffer(javaExe);
 		for (Object option : javaOpts){
 			cmd.append(option + " ");
 		}
 		cmd.append(
 			" -Dmyna.hasWatchdog=true "+
 			" -cp " + cp+
 			" bootstrap.MynaServer "+
 			" -c "+ webctx+
 			" -p "+ new Integer(port).toString()+
 			" -w "+ webroot+
 			" -l "+ logFile
 		);
 		;
 		System.out.println("Starting " + cmd.toString());
 		int retVal=1;
 		
 		do{
 			if (p != null) {
 				System.err.println("Restarting MynaServer");
 				p.destroy();
 			}
 			p =rt.exec(cmd.toString(),null,new File("."));
 			//load general properties
 				File propsFile = new File(
 					new File(webroot).toURI().resolve("WEB-INF/classes/general.properties")
 				);
 				FileInputStream fis = new FileInputStream(propsFile);
 				props.load(fis);
 				fis.close();
 				int startupDelay =30;
 				if (props.getProperty("watchdog_request_startup_delay") != null){
 					startupDelay = Integer.parseInt(props.getProperty("watchdog_request_startup_delay"));
 				}
 			Thread.sleep(startupDelay*1000);//give the server a chance to start
 		}
 		while(!isServerHealthy());
 			
 		System.exit(0);
 	}
 	public static boolean isServerHealthy() throws Exception {
 		int urlFailCount = 0;
 		int memFailCount=0;
 		long started = System.currentTimeMillis();
 		
 		//load general properties
 		File propsFile = new File(
 			new File(webroot).toURI().resolve("WEB-INF/classes/general.properties")
 		);
 		FileInputStream fis = new FileInputStream(propsFile);
 		props.load(fis);
 		fis.close();
 		
 		int checkInterval =10;
 		if (props.getProperty("watchdog_request_check_interval") != null){
 			checkInterval = Integer.parseInt(props.getProperty("watchdog_request_check_interval"));
 		}
 		int requestTimeout =10;
 		if (props.getProperty("watchdog_request_timeout") != null){
 			requestTimeout = Integer.parseInt(props.getProperty("watchdog_request_timeout"));
 		}
 		int requestFailAfter =2;
 		if (props.getProperty("watchdog_request_failcount") != null){
 			requestFailAfter = Integer.parseInt(props.getProperty("watchdog_request_failcount"));
 		}
 		float memFreePercent =(float)15.00;
 		if (props.getProperty("watchdog_request_mem_free_percent") != null){
 			memFreePercent = Float.parseFloat(props.getProperty("watchdog_request_mem_free_percent"));
 		}
 		int memFailAfter =2;
 		if (props.getProperty("watchdog_request_mem_failcount") != null){
 			memFailAfter = Integer.parseInt(props.getProperty("watchdog_request_mem_failcount"));
 		}
 
 		while(true){
 			try{
 				Thread.sleep(checkInterval*1000);
 			}catch(InterruptedException ie){}
 			//check if running
 			try{	
 				if (p.exitValue() != 0) {
 					System.err.println("MynaServer exited unexpectedly.");
 					return false;
 				}
 			} catch(IllegalThreadStateException itse){ }
 			//check URL and get mem counter
 			try{
 				String[] stats = readStats();
 				
 				//stats[0] is free mem percent in 99.99 form
 				if (Float.parseFloat(stats[0]) < memFreePercent){
 					++memFailCount;
 					if (memFailCount >=memFailAfter) {
 						System.err.println("MynaServer out of memory.");
 						return false; 
 					}
 				} else memFailCount=0;
 				//System.out.println("Mem free: " + stats[0]+"%");
 				urlFailCount=0;    
 			} catch (Throwable t){
 				t.printStackTrace();
 				++urlFailCount;
 				if (urlFailCount >= requestFailAfter) {
 					System.err.println("Failed connection to MynaServer");
 					return false;
 				}
 			}
 		}
 	}
 	public static String[] readStats() throws Exception{
 			URL url = null;
 			BufferedReader reader = null;
 			StringBuilder stringBuilder;
 			
 			try
 			{
 				// create the HttpURLConnection
 				url = new URL("http://localhost:" + port + webctx+"/shared/js/libOO/health_check.sjs");
 				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 				
 				// just want to do an HTTP GET here
 				connection.setRequestMethod("GET");
 				
 				// uncomment this if you want to write output to this url
 				//connection.setDoOutput(true);
 				
 				// give it 15 seconds to respond
 				connection.setReadTimeout(10*1000);
 				connection.connect();
 	
 				// read the output from the server
 				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 				stringBuilder = new StringBuilder();
 	
 				String line = null;
 				while ((line = reader.readLine()) != null)
 				{
 					stringBuilder.append(line);
 				}
 				return stringBuilder.toString().split(",");
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				throw e;
 			}
 			finally
 			{
 				// close the reader; this can throw an exception too, so
 				// wrap it in another try/catch block.
 				if (reader != null)
 				{
 					try
 					{
 						reader.close();
 					}
 					catch (IOException ioe)
 					{
 						ioe.printStackTrace();
 					}
 				}
 			}
 	}
 	public static boolean exec(String cmd) throws Exception{
 		int exitVal = -1;
 		try
 		{            
 			Runtime rt = Runtime.getRuntime();
 			Process proc = rt.exec(new String[] {"/bin/bash", "-c", cmd});
 			
 			OutputHandler err = new 
 			OutputHandler(proc.getErrorStream(), cmd);            
 			err.start();
 			
 			OutputHandler out = new 
 			OutputHandler(proc.getInputStream(), cmd);
 			out.start();
 			
 						
 			
 			exitVal = proc.waitFor();
 			
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 			
 		}
 		return (exitVal == 0);
 	}
 	public static void upgrade(File wrFile) throws Exception
 	{
 		System.out.println("Installing/upgrading Myna in '"+wrFile.toString()+"'...");
 		wrFile.mkdirs();
 		File web_inf =  new File(wrFile.toURI().resolve("WEB-INF"));
 		boolean isUpgrade = false;
 		File backupDir = null;
 		if (web_inf.exists()){
 			
 			String dateString = new java.text.SimpleDateFormat("MM-dd-yyyy_HH.mm.ss.S").format(new Date());
 			String backupBase = 	"WEB-INF/upgrade_backups/backup_" + dateString;
 			backupDir = new File(wrFile.toURI().resolve(backupBase));
 			backupDir.mkdirs();
 			isUpgrade=true;
 			System.out.println("Backups stored in " + backupDir);
 		}
 		
 		if (isJar){
 			String jarFilePath = classUrl.substring(
 				classUrl.indexOf(":")+1,
 				classUrl.indexOf("!")
 			);
 			File jarFile = new File(new java.net.URL(jarFilePath).toURI());
 			ZipFile zipFile= new ZipFile(jarFile);
 			
 			for (ZipEntry entry :  java.util.Collections.list(zipFile.entries())){
 				;
 				File outputFile = new File(
 					wrFile.toURI().resolve(java.net.URLEncoder.encode(entry.getName()))
 				);
 				File backupFile = null;
 				if (isUpgrade){
 					backupFile= new File(
 						backupDir.toURI().resolve(java.net.URLEncoder.encode(entry.getName()))
 					);
 				}
 				if(entry.isDirectory()) {
 					outputFile.mkdirs();
 					if (isUpgrade) backupFile.mkdirs();
 				} else {
 					if (isUpgrade && outputFile.exists()){
 						
 						java.io.InputStream sourceIS = zipFile.getInputStream(entry);
 						java.io.InputStream targetIS = FileUtils.openInputStream(outputFile);
 						boolean isSame =IOUtils.contentEquals(sourceIS,targetIS);
 						sourceIS.close();
 						targetIS.close();
 						
 						if (isSame
 							|| entry.toString().equals("index.html")
 							|| entry.toString().equals("application.sjs")
 							|| entry.toString().equals("WEB-INF/classes/general.properties")
 							|| entry.toString().startsWith("WEB-INF/myna/ds")
 						){
 							continue;
 						} else { 
 							System.out.println("...backing up " + entry);
 							FileUtils.copyFile(outputFile, backupFile, true) ;
 							//outputFile.copyTo(backupFile);
 							//fusebox.upgradeLog("Backup: " + backupFile);
 						}
 						
 						
 					} 
 					java.io.InputStream is = zipFile.getInputStream(entry);
 					java.io.OutputStream os = FileUtils.openOutputStream(outputFile);
 					IOUtils.copyLarge(is,os);
 					is.close();
 					os.close();
 					
 				}
 			}
 			zipFile.close();
 			//FileUtils.deleteDirectory()
 			
 			System.out.println("Done unpacking.");
 		}
 		 
 	}
 }
