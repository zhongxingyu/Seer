 package echode.api;
 
 import com.google.common.eventbus.EventBus;
 import echode.Test;
 import echode.Time;
 import echode.test.TestListener;
 
 import javax.net.ssl.HttpsURLConnection;
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.UnknownHostException;
 import java.util.*;
 
 public class Echode {
 	 Scanner scan;
 	private  String in;
 	private  List<Class> loaded = new ArrayList<>();
 	 Class<?> c;
 	boolean started = false;
 	private PrintStream out;
         public final EventBus EVENT_BUS = new EventBus();
         
         public UUID uuid;
         
         //begin eventbus-specific stuff
         public abstract class ProgramLoadedEvent {
             public Class program;
             public String name;
             public ProgramLoadedEvent(Class program, String name) {
                 this.program = program;
                 this.name = name;
             }
         }
         public abstract class ExecEvent {
             public Class program;
             public String[] args;
             public ExecEvent(Class program, String[] args) {
                 this.program = program;
                 this.args = args;
             }
         }
 
 
 	
 	public Echode(PrintStream out   ) {
 		this.out = out;
 	}
 
 	// welcome message
 	public  void intro() throws ReflectiveOperationException, MalformedURLException, IOException {
 		EVENT_BUS.register(new TestListener());
                 
 		out.println("Welcome to ECHODE version " + getAPIVersion());
                 reportRunAnalytic();
                 resetLoaded();
 		load();
 	}
 
 	private  void load() throws ReflectiveOperationException, MalformedURLException {
                 String currentDir = System.getProperty("user.dir");
                 out.println("Loading programs...");
         loadBuiltins();
 		File dir = new File(currentDir + "\\programs\\");
                 if (!dir.isDirectory()) {
                     boolean mkdir = dir.mkdir();
                     if (!mkdir) {
                         throw new RuntimeException("Making the directory failed.");
                     }
                 }
                 URL url = new URL("file", currentDir, "programs/");
 		//out.println(dir);
                 URL[] urls = new URL[1];
                 urls[0] = url;
                 URLClassLoader loader = new URLClassLoader(urls);
 		for (File f: dir.listFiles()) {
 			//out.println(f); Debugging
 			if (f.getName().trim().endsWith(".class")) {
 				String name = f.getName();
                                 name = name.replaceAll(".class", "");
                                 name = name.replace(dir.getAbsolutePath(), "");
                                 name = name.replaceAll("/", ".");
                                 name = name.replaceAll("\\\\", ".");
                                 //out.println();
 				//out.println(name);
 				Class c = loader.loadClass(name);
                                 //System.err.println(c);
 				
                                     //System.err.println(c);
 					if (c.getSuperclass().equals(Program.class)) {
 						add(c);
 					
 				}
 				
 			}
 		}
 		
 
 		
 	}
 
 	
 
 	public  void parse(String in2) throws ReflectiveOperationException {
 		/**
 		 * Commented this out, in case needed.
 		 * 
 		 * if (in2.equalsIgnoreCase("about")) { out.println(
 		 * "Echode version 0.2.2\nMade by Erik Konijn and Marks Polakovs"); }
 		 * else { if (in2.equalsIgnoreCase("kill")){
 		 * out.println("Echode shut down successfully."); System.exit(0);
 		 * } else{ out.println("Not implemented yet."); } }
 		 **/
 		String[] result = in2.split(" ");
                 if (!(result.length == 0)) {
                 //System.err.println(result[0]);
 		switch (result[0]) {
 		case "about":
			out
					.println("Echode version " + getApiVersion()+"\nMade by Erik Konijn and Marks Polakovs");
 			break;
 		case "kill": case "exit": case "quit":
 			out.println("Echode terminated succesfully.");
 			System.exit(0);
 			break;
                 //Help may be reimplemented at a later date
 		case "version":
 			out.println("0.3");
 			break;
                 
 		default:
                     Class noparams[] = {};
 			for (Class ct:loaded) {
                             //System.err.println(ct);
                             String checkingName = getProgramName(ct);
                 if (checkingName.equalsIgnoreCase(result[0])) {
                     out.println("Starting program " + checkingName);
                                     //System.err.println("equals");
                                     String[] argv = new String[result.length - 1];
                                     for(int i = 0;i<result.length;i++) {
                                         if (!(i == 0)) {
                                             argv[i-1] = result[i];
                                         }
                                     }
                                         EVENT_BUS.post(new ExecEvent(ct, argv) {});
 					ct.getDeclaredMethod("run", PrintStream.class, String[].class).invoke(ct.getConstructors()[0].newInstance((Object[]) noparams), out, argv);
                                         break;
                                                 
 				} else {
 			}
             out.println("No program found with name " + result[0]);
         }
 
         }
 		}
 	}
 
 
     private void resetLoaded() {
         loaded.clear();
     }
 
     private void add(Class c) throws ReflectiveOperationException {
         loaded.add(c);
         EVENT_BUS.post(new ProgramLoadedEvent(c, c.getName()) {
         });
         out.println("Loaded " + getProgramName(c));
     }
     private void loadBuiltins() throws ReflectiveOperationException {
         add(Test.class);
         add(Time.class);
     }
     
     private void reportRunAnalytic() throws IOException, ClassNotFoundException {
         out.println("Beginning Google analytics report:");
         writeUuidToFile();
              String url = "https://ssl.google-analytics.com/collect";
 		URL obj = new URL(url);
 		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
  
 		//add reuqest header
 		con.setRequestMethod("POST");
 		con.setRequestProperty("User-Agent", "Java/1.6.0_26");
  
 		String urlParameters1 = "v=1&tid=UA-44877650-2&cid=" + uuid.toString() + "&t=event&ec=echode&ea=run&el=Run&ev=300";
  
 		// Send post request
 		con.setDoOutput(true);
                 try {
 		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
 		wr.writeBytes(urlParameters1);
 		wr.flush();
 		wr.close();
  
 		int responseCode = con.getResponseCode();
 		out.println("Response Code from Google Analytics: "
                         + responseCode);
                 } catch (UnknownHostException e) {
                     out.println("Google Analytics reporting failed (if this"
                             + " persists, contact the devs and attach the"
                             + " following:\n" + e);
                 }
  
 		//print result
     }
     public void writeUuidToFile() throws IOException, ClassNotFoundException {
         File file = new File("echode_uuid.ser");
         if (!file.exists()) {
             file.createNewFile();
             FileOutputStream fout = new FileOutputStream(file);
 	ObjectOutputStream oos = new ObjectOutputStream(fout); 
         if (uuid == null) {
             uuid = UUID.randomUUID();
             
             //System.err.println(uuid);
         }
         oos.writeObject(uuid);
         oos.flush();
         oos.close();
         } else {
             FileInputStream fin = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fin);
             uuid = (UUID) ois.readObject();
             //System.err.println(uuid);
         }
         out.println("Google Analytics tracking ID: " + uuid);
         
 
     }
     
     public String getAPIVersion() {
 	String path = "/version.prop";
 	InputStream stream = getClass().getResourceAsStream(path);
 	if (stream == null) return "UNKNOWN";
 	Properties props = new Properties();
 	try {
 		props.load(stream);
 		stream.close();
 		return (String)props.get("version");
 	} catch (IOException e) {
 		return "UNKNOWN";
 	}
 }
 
 public String getProgramName(Class<? extends Program> ct) throws ReflectiveOperationException {
     Program checking = ct.getConstructor(null).newInstance(null);
     return checking.getName();
 }
 
 
     
 }
