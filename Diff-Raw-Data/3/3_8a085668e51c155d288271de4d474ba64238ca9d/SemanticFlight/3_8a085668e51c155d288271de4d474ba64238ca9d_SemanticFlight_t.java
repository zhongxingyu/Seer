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
 
 public class SemanticFlight {
 	/**
 	 * Hvor vi forventer at inifilen skal ligge. Vi slipper unna med statisk variabel, siden den bare er på et sted.
 	 * Merk at vi må hardkode hvor minst en konfigurasjonsfil er, så vi bruker Settings.ini, og holder oss til at den må være et spesifikt sted.
 	 */
 	private static final File INI_FILE = new File("Settings.ini");
 	private static boolean runOnce = false;
 	public static File getIniFile(){
 		return INI_FILE;
 	}
 
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
 		 * Dersom vi ikke har alt vi trenger for å kjøre når vi starter,
 		 *  så krasjer vi med en gang, istedenfor å sløse med alles tid.
 		 */
 		if(!validateEnvironment()){
 			System.out.println("Invalid environment");
 			System.exit(1);
 		}
 
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
 
 				debugQuerys();				
 				//				backupTriples();
 
 				timeEnd = System.currentTimeMillis() - time;
 				System.out.println("Loading model took: " + timeEnd/1000);
 
 				//		rdfLoader.loadAirportsDbpedia();
 			}
 		};
 		System.out.println("Startup complete. Running");
 		if(SemanticFlight.runOnce){
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
 
 			debugQuerys();				
 			//				backupTriples();
 
 			timeEnd = System.currentTimeMillis() - time;
 			System.out.println("Loading model took: " + timeEnd/1000);
 
 			//		rdfLoader.loadAirportsDbpedia();
 		}
 		else{
 			System.out.println("Running as a service");
 			// Third argument in scheduledAtFixedRate define run-time
 			// TODO put scheduled time in ini-file
 			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
 			executor.scheduleAtFixedRate(semanticRunnable, 0, 40, TimeUnit.SECONDS);
 
 		}
 	}
 
 	/**
 	 * @author Haakon Løtveit
 	 * (Ikke ferdig)
 	 * Sjekker om alle mapper og filer som må være tilstede er tilstede.
 	 * Skriver informative feilmeldinger dersom filer/mapper mangler.
 	 * @return false dersom de ikke er det, true dersom de er det.
 	 * TODO: Sjekke at alle mappene er der. (Nå sjekker den kun at xml/xmlA og xml/xmlB er der.
 	 * TODO: La resten av koden bruke INI-filen, slik at sjekken faktisk blir nyttig.	
 	 */
 	private static boolean validateEnvironment() {
 		boolean validity = true;
 		if(!SemanticFlight.INI_FILE.exists()){
 			System.err.printf(
 					"Ini file not found.%nExpected location: %s%nRun this program with the --setup option to recreate the standard Settings.ini.%n",
 					SemanticFlight.INI_FILE.getAbsolutePath());
 			/* Hvis vi ikke har ini filen kan vi ikke sjekke mapper, etc. Derfor returnerer vi med en gang.*/
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
 
 		/* Dersom noe går galt når vi leser Inifilen: */
 		catch (InvalidFileFormatException e) {			
 			System.err.println("Settings.ini er feilformatert.\nStacktrace:\n");
 			e.printStackTrace();
 			return false;
 		}
 		catch (IOException e) {
 			System.err.println("Kunne ikke lese Settings.ini.\nStacktrace:\n");
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private static void setup() {
 		createNewIni();
 		createFolders();
 	}
 
 	/**
 	 * NB! Denne metoden er komplett avhengig av at Settings.ini eksisterer.
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
 	private static void debugQuerys() {
 		TDBconnections s = TDBconnections.create();
 		Dataset set = s.getDataset();
 		set.begin(ReadWrite.READ);
 		Model mod = set.getDefaultModel();
 		System.out.println("Model size: " + mod.size());
 
 		//		Query query = QueryFactory.create(""
 		//				+ "PREFIX avi: <http://awesemantic.com/property/>"        		        	
 		//				+ "PREFIX avires: <http://awesemantic.com/resource/>"
 		//				+ "SELECT ?pred ?subject WHERE {"
 		//				+ "avires:SK263 ?pred ?subject ."
 		//				+ "}");
 		//
 		//		QueryExecution queryExecution = QueryExecutionFactory.create(query, set);
 		//
 		//		ResultSet res = queryExecution.execSelect();
 		//		System.out.println("out");
 		//		while (res.hasNext()){
 		//			System.out.println(res.next().toString());
 		//		}
 
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
 
 		}catch(Exception e) {
 			e.printStackTrace();
 			//				rdfLoader.writeFlightsToTDB(flights);
 		}
 
 	}
 
 	/**
 	 * Pulls xmls from all airports connecting to avinor airports
 	 * @return Queue<File> queue
 	 */
 	private static Queue<File> pullAvinorXML() {
 		XmlSingle single = new XmlSingle();
 		ArrayList<ArrayList<String>> li = new ArrayList<ArrayList<String>>();
 		ArrayList<String> fly = new ArrayList<String>();
 
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
 
 
 	/**
 	 * Writes triples to file for validation or backup.
 	 */
 //	private static void backupTriples() {
 //		TDBconnections s = TDBconnections.create();
 //		Dataset set = s.getDataset();
 //		set.begin(ReadWrite.READ);
 //		Model model = set.getDefaultModel();
 //		try {
 //			model.write(new FileOutputStream(new File("backup.rdf")));
 //		} catch (FileNotFoundException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		set.end();
 //	}
 
 }
