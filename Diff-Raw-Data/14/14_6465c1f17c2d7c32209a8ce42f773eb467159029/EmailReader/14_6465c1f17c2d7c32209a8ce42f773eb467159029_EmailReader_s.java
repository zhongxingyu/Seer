 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import AndrewCassidy.PluggableBot.DefaultPlugin;
 
 import com.eleventytwo.EmailReader.Connection;
 
 public class EmailReader extends DefaultPlugin {
 
 	private List<Connection> connections = new ArrayList<Connection>();
 	
 	private String location = "emailreader";
 
 	public EmailReader() {
 		
 		File file = new File(location);
 		for (File f : file.listFiles()) {
 			if (f.isFile()) {
 				if (f.getName().endsWith(".properties")) {
 					try {
 						FileInputStream fis = new FileInputStream(f);
 						Properties p = new Properties();
 						p.load(fis);
 						Connection c = new Connection(p); 
 						connections.add(c);
 					} catch (Exception e) {
 					}
 					
 				}
 			}
 		}
 		
 		
 		for (Connection x : connections) {
 			new Thread(x).start();
 		}
 	}
 
 	@Override
 	public void unload() {
 		
 		for (Connection con : connections) {
 			try {
 				con.kill();
 				
 				Properties p = con.getProps();
 				
 				File f = new File (location + "/"
 						+ p.getProperty("username") + "_" + p.getProperty("server") + ".properties");
 				System.out.println(f.toString());
 				FileOutputStream os = new FileOutputStream(f);
 				p.store(os, "");
 				
 				os.close();
 			} catch (FileNotFoundException e) {
 			} catch (IOException e) {
 			}
 		}
 
 		
 		
 	}
 
 	public static void main(String[] args) throws InterruptedException {
 		EmailReader m = new EmailReader();
 		Thread.sleep(60 * 1000 * 10);
 		m.unload();
 	}
 
 	@Override
 	public String getHelp() {
 		return "Reads Subjects of recieved mail in a mailbox.";
 	}
 
 }
