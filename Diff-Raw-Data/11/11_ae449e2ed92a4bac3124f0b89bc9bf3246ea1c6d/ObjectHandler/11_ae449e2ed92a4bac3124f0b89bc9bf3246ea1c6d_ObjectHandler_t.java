 package lib;
 
 import java.io.*;
 
 import model.User;
 import android.os.Environment;
 import android.util.Log;
 
 public class ObjectHandler implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	public static final String DATATYPE_USER = "user";
 	public static final String DATATYPE_HUNT = "hunt";
 	public static final String DATATYPE_OBJECTIVE = "objective";
 
 	private boolean mExternalStorageAvailable = false;
 	private boolean mExternalStorageWriteable = false;
 	private File root;
 	
 	public ObjectHandler() {
 		//create directory structure if it doesn't exist..
 
 		String state = Environment.getExternalStorageState();
 		
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 		    // We can read and write the media
 		    mExternalStorageAvailable = mExternalStorageWriteable = true;
 		 
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 		    // We can only read the media
 		    mExternalStorageAvailable = true;
 		    mExternalStorageWriteable = false;
 		 
 		} else {
 		    // Something else is wrong. It may be one of many other states, but all we need
 		    //  to know is we can neither read nor write
 		    mExternalStorageAvailable = mExternalStorageWriteable = false;
 		    return;
 		}
 		
 		File home = Environment.getExternalStorageDirectory();
 		root = new File(home, "myData");
 		//File structure already exists..
 		if(root.exists() && root.canRead() && root.canWrite()) {
 			//no need to create a new file structure.. it already exists.
 			Log.d("file", "directory structure exists");
 			return;
 		
 		}
 		//Make the directory structure..
 		
 		root.mkdir();
 		new File(root, DATATYPE_USER).mkdir();
 		new File(root, DATATYPE_HUNT).mkdir();
 		new File(root, DATATYPE_OBJECTIVE).mkdir();
 	}
 	
	public void delete(String type, int id) {
		String path = type + "/" + type + id + ".bin";
		File f = new File(root, path);
		if(f.exists())
			f.delete();
		return;
	}
	
 	public Object readObject(String type, int id) {
 		//get the serialized object from the appropriate file and deserialize it.
 		
		String path = type + "/" + type + id + ".bin";
 		File f = new File(root, path);
 		if(!f.exists()) {
 			return null;
 		} else {
 			return readFile(f);
 		}
 	}
 	
 	private Object readFile(File f) {
 		System.out.println("in readFile, looking for " + f.getAbsolutePath());
 		Object o = null;
 		if(this.mExternalStorageAvailable && f.canRead()) {
 			try {
 				ObjectInputStream in = new ObjectInputStream(
 						new FileInputStream(f));
 				o = in.readObject();
 				
 				in.close();
 		    } catch(IOException i) {
 		          i.printStackTrace();
 		    } catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (ClassCastException c) {
 				
 			}
 		}
 		if(o == null) {
 			System.out.println("not found/couldn't read.");
 		}
 		return o;
 	}
 	
 	public boolean writeObject(String type, int id, Object o) {
 		//serialize and write object to appropriate file.
 		System.out.println("in write object");
 		String path = type + "/"+ type + id+".bin";
 		System.out.println("path: " + path);
 		File f = new File(root, path);
 		if(f.exists()) {
 			System.out.println("File exists already..");
 			f.delete();
 		}
 		return writeFile(f, o);
 	}
 	
 	private boolean writeFile(File f, Object o) {
 		System.out.println("in writeFile, writing to " + f.getAbsolutePath());
 		try {
 			f.createNewFile();
 			if (this.mExternalStorageWriteable && f.canWrite()) {	
 				ObjectOutputStream out = new ObjectOutputStream(
 						new FileOutputStream(f));
 				out.writeObject(o);
 				out.close();
 
 				System.out.println("done writing");
 				return true;
 			} else {
 				System.out.println("storage or file: "+ f.getAbsolutePath() + " is not writeable");
 				return false;
 			}
 		} catch (IOException i) {
 			i.printStackTrace(System.out);
 			return false;
 		}
 	}
 
 	//This method is used to search for a user object in the user database.
 	public User searchUser(String email) {
 		System.out.println("in search user..");
 		File f = new File(root, DATATYPE_USER);
 		File list[] = f.listFiles();
 		if(list == null || list.length == 0) {
 			System.out.println("No list");
 			return null;
 		}
 		User u = null;
 		for(File i : list) {
 			Object o = readFile(i);
 			if(o instanceof User) {
 				u = (User)o;
 			} else {
 				System.out.println(i.getAbsolutePath() + " is not a user file");
 				continue;
 			}
 			
 			if(u.getEmail().equals(email)) {
 				break;
 			}
 		}
 		
 		return u;
 	}
 }
