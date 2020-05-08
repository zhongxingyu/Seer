 package no.uib.semanticweb.semanticflight;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Queue;
 import java.util.Scanner;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import no.uib.semanticweb.semanticflight.rdfstore.TDBconnections;
 import no.uib.semanticweb.semanticflight.rdfstore.TDBwrapper;
 import no.uib.semanticweb.semanticflight.xml.SemanticXMLParser;
 import no.uib.semanticweb.semanticflight.xml.XmlSingle;
 
 import org.ini4j.Ini;
 import org.ini4j.InvalidFileFormatException;
 
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.ReadWrite;
 import com.hp.hpl.jena.rdf.model.Model;
 
 /**
  * Runs our program!
  *
  */
 public class SemanticFlight {
 	
 	private static final File INI_FILE = new File("Settings.ini");
 	private static boolean runOnce = false;
 	public static File getIniFile(){
 		return INI_FILE;
 	}
 
 	/**
 	 * Accepts two arguments. --setup creates folders and ini-file
 	 * --run-once lifts avinor xmls once. No arguments run the application
 	 * as a continual service.
 	 * @param args
 	 */
 	public static void main(String[] args){
 
 		if(args.length >= 1){
 			if(args[0].equals("--setup") || args[0].equals("-s")){
 				setup();
 				System.exit(0);
 			}
 			else if (args[0].equals("--run-once")){
 				System.out.println("Running only once");
 				runOnce = true;
 			}
 			else{
 				System.err.printf(
 						"Option %s not recognized.%nLegal values are:%n -s / --setup  -  Automatic setup for deployment.",
 						args[0]);
 				System.exit(0);
 			}
 		}
 
 		/*
 		 * Checks if folders and inifile are present
 		 */
 		if(!validateEnvironment()){
 			System.out.println("Invalid environment");
 			System.exit(1);
 		}
 
 		// Runnable wich we loop when run as service.
 		Runnable semanticRunnable = new Runnable() {
 			public void run() {
 				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 				Calendar cal = Calendar.getInstance();
 				System.out.println(dateFormat.format(cal.getTime()));
 
 				long time = System.currentTimeMillis();
 
 				Queue<File> xmlFileQueue = pullAvinorXML();
 
 				long timeEnd = System.currentTimeMillis() - time;		
 				System.out.println("Downloading xml took: " + timeEnd/1000 + " XMLs: " + xmlFileQueue.size());
 				time = System.currentTimeMillis();
 
 				// Parse XML and then persist triples
 				parseAndPersistXMLQueue(xmlFileQueue);
 				timeEnd = System.currentTimeMillis() - time;
 				System.out.println("Parsing took: " + timeEnd/1000);		
 				time = System.currentTimeMillis();
 
 				printModelSize();				
 
 				timeEnd = System.currentTimeMillis() - time;
 				System.out.println("Loading model took: " + timeEnd/1000);
 
 			}
 		};
 		System.out.println("Startup complete. Running");
 		
 		// executes the runable once or loop with delay set in Settings.ini
 		if(SemanticFlight.runOnce){
 			semanticRunnable.run();
 		}
 		else{
 			System.out.println("Running as a service");
 			// Third argument in scheduledAtFixedRate define run-time
 			
 			Ini ini = null;
 			try {
 				ini = new Ini(getIniFile());			
 			} catch (InvalidFileFormatException e) { /* Just prints for now */
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// gets the delaytime from the ini. Else default location.
 			int delayTime = 50;
 
 			if(null != ini){
 				delayTime = ini.get("Scrape", "DelayTime", Integer.class);
 				System.out.println("Delay time set to: " + delayTime);
 			}
 			
 			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
 			executor.scheduleAtFixedRate(semanticRunnable, 0, delayTime, TimeUnit.SECONDS);
 
 		}
 	}
 
 	/**
 	 * Checks if all the necessary files and folders are present.
 	 * Writes informative errormessages if folder or file missing
 	 * @return boolean
 	 */
 	private static boolean validateEnvironment() {
 		boolean validity = true;
 		if(!SemanticFlight.INI_FILE.exists()){
 			System.err.printf(
 					"Ini file not found.%nExpected location: %s%nRun this program with the --setup option to recreate the standard Settings.ini.%n",
 					SemanticFlight.INI_FILE.getAbsolutePath());
 			/* If no inifile, then the folders are unset. Return at once.*/
 			return false;
 		}
 		Ini lookup;
 		try {
 			lookup = new Ini(SemanticFlight.getIniFile());
 
 			File arrivalsFolder = new File(lookup.get("XMLParse", "ArrivalsFolder"));
 			File departuresFolder = new File(lookup.get("XMLParse", "DeparturesFolder"));
 			if(!arrivalsFolder.exists() || !arrivalsFolder.isDirectory()){
 				validity = false;
 				System.err.printf(
 						"The folder %s does not exist.%nFor an automatic fix, run this program with the --setup option.%n",
 						arrivalsFolder.getAbsolutePath());
 			}
 			if(!departuresFolder.exists() || !departuresFolder.isDirectory()){
 				validity = false;
 				System.err.printf(
 						"The folder %s does not exist.%nFor an automatic fix, run this program with the --setup option.%n",
 						departuresFolder.getAbsolutePath());
 			}
 			return validity;		
 		}
 
 		/* Error while reading ini: */
 		catch (InvalidFileFormatException e) {			
 			System.err.println("Settings.ini wrong format.\nStacktrace:\n");
 			e.printStackTrace();
 			return false;
 		}
 		catch (IOException e) {
 			System.err.println("Could not read Settings.ini.\nStacktrace:\n");
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private static void setup() {
 		createNewIni();
 		createFolders();
 	}
 
 	/**
 	 * Prerequisite is the existence of Settings.ini
 	 */
 	private static void createFolders(){
 		Ini lookup;
 		try {
 			lookup = new Ini(SemanticFlight.getIniFile());
 			File arrivalsFolder = new File(lookup.get("XMLParse", "ArrivalsFolder"));
 			File departuresFolder = new File(lookup.get("XMLParse", "DeparturesFolder"));
 			boolean success = arrivalsFolder.mkdirs();
 			boolean great = departuresFolder.mkdirs();
 			boolean greatSuccess = great && success;
 
 			System.out.printf("%s tried to create folders%n", greatSuccess? "Successfully":"Unsuccessfully");
 
 		} catch (InvalidFileFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	/**
 	 * Writes a file with default ini-information
 	 */
 	private static void createNewIni(){
 		try{
 			boolean write = true;
 			File newIni = new File(SemanticFlight.getIniFile().getAbsolutePath());
 			if(newIni.exists()){
 				Scanner kb = new Scanner(System.in);
 				boolean done = false;
 				while(!done){
 					System.out.print("Settings.ini already exist. Delete and recreate? [Y/N]: ");
 					String answer = kb.nextLine();
 					if(answer.length() == 1){
 						switch(answer.charAt(0)){
 						case 'Y':
 						case 'y':
 							System.out.println("Deleting file and recreating standard");
 							newIni.delete();
 							done = true;							
 							break;
 
 						case 'N':
 						case 'n':
 							System.out.println("Not creating new Settings.ini file");
 							write = false;
 							done = true;
 							break;
 						default:
 							System.out.println("Please answer either Y or N.");
 						}
 					}
 				}
 				kb.close();
 			}
 
 			if(write){
 				BufferedWriter writer = new BufferedWriter(new FileWriter(newIni));
 				writer.append("[StoreRDF]" + "\n");
 				writer.append("Location = tdb/flightSTORE" + "\n");
 				writer.append("\n");
 				writer.append("[XMLParse]" + "\n");
 				writer.append("ArrivalsFolder = xml/xmlA/" + "\n");
 				writer.append("ArrivalsFilenameSpec = %sA.xml" + "\n");
 				writer.append("DeparturesFolder = xml/xmlD/" + "\n");
 				writer.append("DepatruesFilenameSpec = %sD.xml" + "\n");
 				writer.append("\n");
 				writer.append("[Scrape]" + "\n");
 				writer.append("DelayTime = 40" + "\n");
 				writer.append("\n");
				writer.append("[Host]" + "\n");
 				writer.append("URL = http://localhost:3030/datas1/" + "\n");
 				writer.append("\n");
 				writer.close();
 			}
 		}
 		catch(MalformedURLException e){
 			e.printStackTrace();
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 	private static void printModelSize() {
 		TDBconnections s = TDBconnections.create();
 		Dataset set = s.getDataset();
 		set.begin(ReadWrite.READ);
 		Model mod = set.getDefaultModel();
 		System.out.println("Model size: " + mod.size());
 
 		set.end();
 
 	}
 
 	/**
 	 * Takes a list of xml-files, parse them and persist the resulting triples
 	 * @param xmlFileQueue
 	 */
 	private static void parseAndPersistXMLQueue(Queue<File> xmlFileQueue) {
 		SemanticXMLParser dpe = new SemanticXMLParser();
 		TDBwrapper rdfLoader = new TDBwrapper();
 		try {
 			while(xmlFileQueue.peek() != null) {
 				File xmlFile = xmlFileQueue.poll();
 				dpe.parse(xmlFile.getPath());
 				List<Flight> flights = dpe.getFlights();
 				// Write flight objects to tdb
 				rdfLoader.writeFlightsToTDB(flights);
 				if(!xmlFile.delete()){
 					System.out.println("Not deleted xmlFile");
 				}
 			}
 			TDBwrapper.updateFusekiHTTP();
 		}catch(Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Pulls xmls from all airports connecting to avinor airports.
 	 * @return Queue<File> queue
 	 */
 	private static Queue<File> pullAvinorXML() {
 		XmlSingle single = new XmlSingle();
 		ArrayList<ArrayList<String>> li = new ArrayList<ArrayList<String>>();
 		ArrayList<String> fly = new ArrayList<String>();
 
 		// All avinor-owned airports
 		String[] avinorAirPorts = {"AES","KSU","NVK","FBU","OSL","BVG","BOO","MQN",
 				"MJF","OSY","SDN","ALF","ANX","BJF","BDU","BNN",
 				"FDE","VDB","FRO","HFT","EVE","HAU","HVG","KKN",
 				"KRS","LKL","LKN","MEH","MOL","RRS","RVK","RET",
 				"SOJ","SSJ","SOG","SKN","LYR","SVJ","TOS","TRD",
 				"VRY","VDS","VAW","HOV","BGO","HAA","SVG"};
 		List<String> aviList = Arrays.asList(avinorAirPorts);
 
 		for(int i = 0 ; i < aviList.size() ; i++) {
 			fly.add(aviList.get(i));
 			li.add(fly);
 
 			// Downloads all airports connected to airports in li.
 			single.connections(li, 0, 5);
 		}
 		return single.getXmlQueue();
 	}
 
 }
