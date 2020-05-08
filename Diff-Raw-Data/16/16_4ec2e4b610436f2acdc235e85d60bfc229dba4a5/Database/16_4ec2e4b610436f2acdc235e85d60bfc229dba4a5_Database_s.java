 package elfville.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.SealedObject;
 import javax.crypto.SecretKey;
 
 import elfville.server.database.*;
 import elfville.server.model.*;
 
 /*
  * Contains data structures that represent the server's database.
  * Is a singleton.
  */
 public class Database {
 	private static Database instance;
 	private ObjectOutputStream stream = null;
 	public final ClanDB clanDB = new ClanDB();
 	public final PostDB postDB = new PostDB();
 	public final ElfDB elfDB = new ElfDB();
 	public final UserDB userDB = new UserDB();
 
 	private static SecretKey databaseSecret;
 	private static Cipher enc;
 	private static Cipher dec;
 
 	// Determines the modelID of all model objects.
 	// getAndIncrementCountID() will increment this by 1.
 	private int countID = -1;
 
 	private final Logger logger;
 
 	protected Database(String dbLocation) throws Exception {
 		logger = Logger.getLogger(dbLocation + " database");
 
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
 					dbLocation));
 			SealedObject msg;
 			while (true) {
 				msg = (SealedObject) ois.readObject();
 				if (msg == null)
 					break;
 				Serializable m = SecurityUtils.decrypt(msg, dec);
 
 				if (m instanceof Model) {
 					Model x = (Model) m;
 					if (x.isDirty())
 						logger.warning(x + " is corrupted.");
 				}
 
 				if (m instanceof Clan) {
 					clanDB.add((Clan) m);
 				} else if (m instanceof Elf) {
 					elfDB.add((Elf) m);
 				} else if (m instanceof Post) {
 					postDB.add((Post) m);
 				} else if (m instanceof User) {
 					userDB.add((User) m);
 				} else if (m instanceof Deletion) {
 					((Deletion) m).deleteObject();
 				}
 			}
 		} catch (FileNotFoundException ex) {
 			logger.info(dbLocation + " not found. Creating...");
 		}
 		stream = new ObjectOutputStream(new FileOutputStream(dbLocation));
 	}
 
 	static public Database getInstance() {
 		return instance;
 	}
 
 	public void persist(Serializable obj) {
 		if (stream != null) {
 			try {
 				Serializable msg = SecurityUtils.encrypt(obj, enc);
 				stream.writeUnshared(msg);
 			} catch (IOException e) {
 				logger.warning(obj + " could not be saved.");
 			} catch (IllegalBlockSizeException e) {
 				logger.log(Level.WARNING, "Bad clock size", e);
 			}
 		}
 		flush();
 	}
 
 	public void flush() {
 		if (stream != null) {
 			logger.info("flush called!");
 			try {
 				stream.flush();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Read the database from disk
 	static public void load() throws Exception {
 		String dbLocation;
 		String db_key_path;
 		if (Server.DEBUG) {
 			dbLocation = "resources/elfville" + System.currentTimeMillis()
 					+ ".db";
			// dbLocation = "resources/elfville.db";
 			db_key_path = "resources/elfville.db.der";
 		} else {
 			// Ask users for database shared key
 			Scanner scanner = new Scanner(System.in);
 			System.out
					.println("Input Database encryption key path (type 'resources/elfville.db' for demonstration: ");
 			dbLocation = scanner.nextLine();
 
 			System.out
 					.println("Input Database encryption key file path\n (type 'resources/elfville.db.der' for demonstration,\n of course you can load one from your flash drive\n that you are inserting right now): ");
			db_key_path = System.currentTimeMillis() + scanner.nextLine();
 		}
 
 		// Initiate database key
 		databaseSecret = SecurityUtils.getKeyFromFile(db_key_path);
 		enc = Cipher.getInstance("AES");
 		dec = Cipher.getInstance("AES");
 		enc.init(Cipher.ENCRYPT_MODE, databaseSecret);
 		dec.init(Cipher.DECRYPT_MODE, databaseSecret);
 
 		instance = new Database(dbLocation);
 	}
 
 	public synchronized int getAndIncrementCountID() {
 		countID++;
 		return countID;
 	}
 }
