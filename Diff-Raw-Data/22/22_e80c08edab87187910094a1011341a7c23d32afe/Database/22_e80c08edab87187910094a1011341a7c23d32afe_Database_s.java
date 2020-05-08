 package elfville.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Scanner;
 
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
 	private static Database instance = new Database();
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
 
 	protected Database() {
 		// Solely to prevent outside instantiation.
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
 				System.err.println(obj + " could not be saved.");
 			} catch (IllegalBlockSizeException e) {
 				e.printStackTrace();
 			}
 		}
 		flush();
 	}
 
 	public void flush() {
 		if (stream != null) {
 			System.out.println("flush called!");
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
 
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
 					dbLocation));
 			SealedObject msg;
 			// TODO: handle deleted objects
 			while (true) {
 				msg = (SealedObject) ois.readObject();
 				if (msg == null)
 					break;
 				Serializable m = SecurityUtils.decrypt(msg, dec);
 
 				if (m instanceof Clan) {
 					instance.clanDB.add((Clan) m);
 				} else if (m instanceof Elf) {
 					instance.elfDB.add((Elf) m);
 				} else if (m instanceof Post) {
 					instance.postDB.add((Post) m);
 				} else if (m instanceof User) {
 					instance.userDB.add((User) m);
 				} else if (m instanceof Deletion) {
 					((Deletion) m).deleteObject();
 				}
 			}
 		} catch (FileNotFoundException ex) {
 			System.err.println(dbLocation + " not found. Creating...");
 		}
 		instance.stream = new ObjectOutputStream(new FileOutputStream(
 				dbLocation));
 	}
 
 	public synchronized int getAndIncrementCountID() {
 		countID++;
 		return countID;
 	}
 }
