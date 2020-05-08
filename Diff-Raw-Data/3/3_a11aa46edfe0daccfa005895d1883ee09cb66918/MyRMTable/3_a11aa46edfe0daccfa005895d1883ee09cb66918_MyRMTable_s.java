 // MyResearchManager - LICENSE AGPLv3 - 2012
 
 // MyRMTable - LICENSE GPLv3
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Scanner;
 
 
 public class MyRMTable {
 
 	static String hostname = "";
 	static String key = "";
 	static String filename = "";
 	static int monitorTime = -1; // time in seconds
 		
 	static boolean automaticRowNumber = true;
 	static int row = 0;
 
 	public static String remote(String text)
 	{
 		String r = "";
 
 		try {
 			URL url = new URL(text);
 			URLConnection conn = url.openConnection();
 			// fake request coming from browser
 			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
 			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
 			r = in.readLine();
 			in.close();
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 
 		return r;
 	}
 
 	public static void main(String[] args) throws IOException, InterruptedException {
 
 		int parameters = args.length/2;
 
 		for(int i=0; i<parameters; i++)
 			if((2*i+1)<args.length)
 			{
 				if(args[2*i].equals("--host"))
 					hostname = args[2*i+1];
 
 				if(args[2*i].equals("--key"))
 					key = args[2*i+1];
 
 				if(args[2*i].equals("--file"))
 					filename = args[2*i+1];
 
 				if(args[2*i].equals("--monitor"))
 					monitorTime = Integer.parseInt(args[2*i+1]);
 			}
 
 		if(hostname.equals("") || key.equals("") || filename.equals(""))
 		{
 			System.out.println("Usage: MyRMTable [--monitor timelimit] --file table.txt --host http://website/myrm/ --key bb631e6b8f17c8c658f42706be558550");
 			System.out.println("found "+args.length+" arguments");
 			System.out.println("file=\""+filename+"\"");
 			System.out.println("key=\""+key+"\"");
 			System.out.println("host=\""+hostname+"\"");
 			System.out.println("monitor=\""+monitorTime+"\"");
 			System.out.println("Missing arguments... aborting!");
 			return;
 		}	
 
 		BufferedWriter log = new BufferedWriter(new FileWriter("myrm-updater.log"));
 		int errors = 0;
 
 		System.out.println("=======================================================");
 		System.out.println("MyResearchManager Dynamic Table updater 0.1 - 2012 AGPL");
 		System.out.println("=======================================================");
 		log.write("MyResearchManager Dynamic Table updater 0.1 - 2012 AGPL\n");
 		System.out.println("hostname: " + hostname);
 		log.write("hostname: " + hostname+'\n');
 		
 		String version = remote(hostname+"version.php");
 		System.out.println("version (remote): "+version);
 		log.write("version (remote): "+version+'\n');
 		
 		System.out.println("key: " + key);
 		log.write("key: " + key+'\n');
 		System.out.println("filename: " + filename);
 		log.write("filename: " + filename+'\n');
 
 		System.out.println("-------------------------------------------------------");
 		System.out.println("automatic row numbering = " + automaticRowNumber);
 		log.write("automatic row numbering = " + automaticRowNumber+'\n');
 		System.out.println("=======================================================");
 
 
 		row = 1;
 
 		if(monitorTime<=0) // no monitor
 		{
 			errors += sendFile(log);
 			errors += lockTable(log);
 		}
 		
 		if(monitorTime>0) // monitoring file (time in seconds)
 			while(true)
 			{
 				errors += sendFile(log);
 				System.out.println("Waiting for more "+monitorTime+" seconds...");
 				Thread.sleep(monitorTime*1000); // 'monitorTime' seconds
 			}
 
 		System.out.println("=======================");
 		System.out.println("Finished with "+errors+" errors!");
 		System.out.println("=======================");
 		
 		log.write("Finished with "+errors+" errors!\n");
 
 		log.close();
 	}
 
 
 	public static int sendFile(BufferedWriter log) throws IOException {
 
 		int errors = 0;
 
 		Scanner table = new Scanner(new File(filename));
 
                 int drop_row = row-1;
                 while((drop_row>0) && (table.hasNextLine()))
                 {
                     System.out.println("ignoring line: "+table.nextLine());
                     drop_row--;
                 }
 
 		while(table.hasNextLine())
 		{
 			String line = table.nextLine();
 			Scanner scanLine = new Scanner(line);
 
 			String url = hostname+"dtableinsert.php?key="+key;
 			if(!automaticRowNumber)
 				url += "&row="+row;
 
 			int col = 1;
 
 			while(scanLine.hasNext())
 			{
 				String v = scanLine.next();
 				url = url + "&c"+col+"="+v;
 
 				col++;
 			}
 
 			for(int i=1; i<=3; i++)
 			{
 				System.out.print("row "+(automaticRowNumber?"auto":row)+" : ("+i+") Connecting to: "+url+" ...");
 				String r = remote(url);
 				if(r.equals("OK"))
 				{
 					System.out.println("OK!");
 					log.write("row "+(automaticRowNumber?"auto":row)+" : ("+i+") Connecting to: "+url+" ...OK!\n");
 					break;
 				}
 				else
 				{
 					System.out.println("failed! (with message: '"+r+"')");
 					log.write("row "+(automaticRowNumber?"auto":row)+" : ("+i+") Connecting to: "+url+" ...failed! (with message: '"+r+"')\n");
 
 					if(i==3)
 					{
 						errors++;
 						System.out.println("ERROR: FAILED 3 TIMES!");
 						log.write("ERROR: FAILED 3 TIMES!\n");
 					}
 				}
 			}
 
 			row++;
 		}
 		
 		return errors;
 	}
 
 
 	public static int lockTable(BufferedWriter log) throws IOException {
 
 		int errors = 0;
 
 		String url = hostname+"dtablelock.php?key="+key;
 		for(int i=1; i<=3; i++)
 		{
 			System.out.print("lock: ("+i+") Connecting to: "+url+" ...");
 			String r = remote(url);
 			if(r.equals("OK"))
 			{
 				System.out.println("OK!");
 				log.write("lock: ("+i+") Connecting to: "+url+" ...OK!\n");
 				break;
 			}
 			else
 			{
 				System.out.println("failed! (with message: '"+r+"')");
 				log.write("lock: ("+i+") Connecting to: "+url+" ...failed! (with message: '"+r+"')\n");
 
 				if(i==3)
 				{
 					errors++;
 					System.out.println("ERROR: FAILED 3 TIMES!");
 					log.write("ERROR: FAILED 3 TIMES!\n");
 				}
 			}
 		}
 
 		return errors;
 	}
 }
 
 
