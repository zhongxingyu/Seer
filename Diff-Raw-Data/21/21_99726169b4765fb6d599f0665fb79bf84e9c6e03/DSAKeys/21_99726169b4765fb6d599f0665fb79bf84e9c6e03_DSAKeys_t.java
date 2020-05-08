 package zipcodeman.glookup.authentication;
 
 import java.io.FileNotFoundException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import zipcodeman.glookup.util.Constants;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.KeyPair;
 import com.jcraft.jsch.ChannelSftp;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.SftpException;
 
 public class DSAKeys {
 	private String uname;
 	private String server;
 	private Context c;
 	private String pass;
 	
 	public DSAKeys(Context c, String uname, String server, String pass) {
 		this.pass = pass;
 		this.uname = uname;
 		this.server = server;
 		this.c = c;
 	}
 	public DSAKeys(Context c, String uname, String server) {
 		this.uname = uname;
 		this.server = server;
 		this.c = c;
 	}
 	
     static byte []SHA(String input) {
     	try {
 	        MessageDigest mDigest = MessageDigest.getInstance("SHA-512");
 	        return mDigest.digest(input.getBytes());
     	} catch (NoSuchAlgorithmException nsae) {
     		return input.getBytes();
     	}
     }
 	
     public byte []getPassword() {
     	return SHA(uname + Constants.DB_SECRET + this.server);
     }
     
    public static boolean removeKeys(Context c, String uname) {
    	return new DSAKeys(c, uname, null).removeKeys();
     }
     
     public boolean removeKeys() {
     	return c.deleteFile(uname + "_dsa.pub") && c.deleteFile(uname + "_dsa");
     }
     
 	public boolean createKeys() {
 		JSch jsch = new JSch();
 		try {
 			KeyPair kp = KeyPair.genKeyPair(jsch, KeyPair.DSA);
 			
 			kp.setPassphrase(getPassword());
 			kp.writePublicKey(c.openFileOutput(uname + "_dsa.pub", Context.MODE_PRIVATE), uname + "@" + Constants.SSH_KEY_HOST_NAME);
 			kp.writePrivateKey(c.openFileOutput(uname + "_dsa", Context.MODE_PRIVATE));
 			kp.dispose();
 			return true;
 		} catch (JSchException jse) {
 			Log.e("DSAKeys", "Can't Create Keys", jse);
 			return false;
 		} catch (FileNotFoundException fnfe) {
 			Log.e("DSAKeys", "Can't Create Keys", fnfe);
 			return false;
 		}
 	}
 	
 	public boolean installKeys() {
 		JSch jsch = new JSch();
 		try {
 			Session s = jsch.getSession(this.uname, this.server, 22);
 			s.setPassword(this.pass);
 			java.util.Properties config = new java.util.Properties();
 			config.put("StrictHostKeyChecking", "no");
 			s.setConfig(config);
 			s.connect();
 			Channel chan = s.openChannel("sftp");
 			chan.connect();
 			ChannelSftp csftp = (ChannelSftp)chan;
 			try {
 				csftp.mkdir(".ssh2");
 			} catch (SftpException se) {
 				Log.i("DSAKeys", "Could not make directory. Assuming it exists");
 			}
 			csftp.cd(".ssh");
 			csftp.put(c.openFileInput(uname + "_dsa.pub"), 
 					  "authorized_keys", ChannelSftp.APPEND);
 			csftp.disconnect();
 			s.disconnect();
 		} catch (JSchException jse) {
 			Log.e("DSAKeys", "JSch Failure", jse);
 			return false;
 		} catch (SftpException se) {
 			Log.e("DSAKeys", "Sftp Failure", se);
 			return false;
 		} catch (FileNotFoundException fnfe) {
 			Log.e("DSAKeys", "No file to upload", fnfe);
 			return false;
 		}
 		return true;
 	}
 	public String getPrivate() throws FileNotFoundException {
 		return c.getFilesDir() + "/" + uname + "_dsa";
 	}
 	public String getPublic() throws FileNotFoundException  {
 		return c.getFilesDir() + "/" + uname + "_dsa.pub";
 	}
 }
