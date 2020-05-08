 package jipdbs.admin;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Arrays;
 import java.util.Properties;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
 import com.google.appengine.tools.remoteapi.RemoteApiOptions;
 
 public abstract class Command {
 
 	private RemoteApiInstaller installer;
 	private File file = null;
 	
 	public void run(String[] args) throws Exception {
		OptionParser parser = getCommandOptions();
		parser.acceptsAll(Arrays.asList("u", "username"), "username").withOptionalArg().ofType(String.class);
		parser.acceptsAll(Arrays.asList("p", "password"), "password").withOptionalArg().ofType(String.class);
		parser.acceptsAll(Arrays.asList("h", "?"), "print help");
		
 		try {
 			String username = null;
 			String password = null;
 			OptionSet options = parser.parse(args);

 			if (options.has("?")) {
				System.out.println("=== " + this.getClass().getSimpleName() + " ===\n");
 				parser.printHelpOn(System.out);
 				return;
 			}
 			
 			if (options.hasArgument("u")) {
 				username = (String) options.valueOf("u");
 			}
 			if (options.hasArgument("p")) {
 				password = (String) options.valueOf("p");
 			}
 			initializeRemoteApi(username, password);
 			execute(args);
 		} finally {
 			try {
 				this.installer.uninstall();
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	protected void initializeState(String name) {
 		this.file = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + name + ".resume");
 	}
 	
 	protected void saveCursor(Cursor cursor) throws Exception {
 		if (file == null) throw new Exception("Sate not initialized");
 		try {
 			Writer wrt = new FileWriter(file);
 			wrt.write(cursor.toWebSafeString());
 			wrt.flush();
 		} catch (IOException e) {
 			System.err.println("Unable to save current state. " + e.getMessage());
 		} 
 	}
 
 	protected Cursor loadCursor() throws Exception {
 		if (file == null) throw new Exception("Sate not initialized");
 		if (file.exists()) {
 			try {
 				Reader input = new FileReader(file);
 				BufferedReader reader = new BufferedReader(input);
 				String line = reader.readLine();
 				Cursor cursor = Cursor.fromWebSafeString(line);
 				reader.close();
 				return cursor;
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Perform login
 	 * 
 	 * @throws Exception
 	 */
 	protected void initializeRemoteApi(String username, String password) throws Exception {
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream(
 					"admin.properties"));
 		} catch (Exception e) {
 			throw new RuntimeException("File admin.properties not found");
 		}
 		BufferedReader console = new BufferedReader(new InputStreamReader(
 				System.in));
 		if (StringUtils.isEmpty(username)) {
 			System.out.print("username: ");
 			username = console.readLine();
 		}
 //		char passwd[] = null;
 //		try {
 //			passwd = PasswordField.getPassword(System.in, "Enter password: ");
 //		} catch (IOException ioe) {
 //			ioe.printStackTrace();
 //		}
 //		String password = String.valueOf(passwd);
 		if (password == null) {
 			System.out.print("password for '" + username + "': ");
 			password = console.readLine();
 		}
 		RemoteApiOptions options = new RemoteApiOptions()
 				.server(props.getProperty("url"),
 						Integer.parseInt(props.getProperty("port")))
 				.credentials(username, password).remoteApiPath("/remote_api");
 		this.installer = new RemoteApiInstaller();
 		this.installer.install(options);
 	}
 
 	/**
 	 * Execute command
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	protected abstract void execute(String[] args) throws Exception;
 	
 	protected abstract OptionParser getCommandOptions();
 	
 }
