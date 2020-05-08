 package edu.berkeley.gamesman.database;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 
 public class SplitDatabase extends Database {
 	Database[] databases;
 	private long location;
 	private int databaseNum;
 
 	@Override
 	public void close() {
 		for (int i = 0; i < databases.length; i++) {
 			databases[i].close();
 		}
 	}
 
 	@Override
 	public void flush() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void getBytes(long location, byte[] arr, int off, int len) {
 		fetchBytes(getDatabase(location), location, arr, off, len);
 	}
 
 	@Override
 	public void getBytes(byte[] arr, int off, int len) {
 		location = fetchBytes(databaseNum, location, arr, off, len);
 		while (databaseNum < databases.length - 1
 				&& location >= databases[databaseNum + 1].firstByte())
 			databaseNum++;
 	}
 
 	private long fetchBytes(int firstDatabase, long location, byte[] arr,
 			int off, int len) {
 		long nextStart;
 		int database = firstDatabase;
 		while (len > 0) {
 			if (database < databases.length - 1)
 				nextStart = Math.min(location + len, databases[database + 1]
 						.firstByte());
 			else
 				nextStart = location + len;
 			databases[database]
 					.getBytes(location, arr, off, (int) (nextStart - location));
 			off += nextStart - location;
 			len -= nextStart - location;
 			location = nextStart;
 			database++;
 		}
 		return location;
 	}
 
 	private int getDatabase(long location) {
 		int low = 0, high = databases.length;
 		int guess;
 		while (high - low > 1) {
			guess = low + high / 2;
 			if (location < databases[guess].firstByte())
 				high = guess;
 			else
 				low = guess;
 		}
 		return low;
 	}
 
 	@Override
 	public void initialize(String uri, boolean solve) {
 		String[] dbs = uri.split(";");
 		if (dbs.length == 1) {
 			try {
 				File f = new File(uri);
 				FileInputStream fis = new FileInputStream(f);
 				int confLength = 0;
 				for (int i = 0; i < 4; i++) {
 					confLength <<= 8;
 					confLength |= fis.read();
 				}
 				byte[] confBytes = new byte[confLength];
 				fis.read(confBytes);
 				fis.close();
 				conf = Configuration.load(confBytes);
 				dbs = conf.getProperty("gamesman.db.uri").split(";");
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		databases = new Database[dbs.length];
 		location = firstByte();
 		for (int d = 0; d < databases.length; d++) {
 			String[] dString = dbs[d].split("-");
 			try {
 				File f = new File(dString[0]);
 				FileInputStream fis = new FileInputStream(f);
 				int confLength = 0;
 				for (int i = 0; i < 4; i++) {
 					confLength <<= 8;
 					confLength |= fis.read();
 				}
 				byte[] confBytes = new byte[confLength];
 				fis.read(confBytes);
 				fis.close();
 				Configuration dconf = Configuration.load(confBytes);
 				databases[d] = dconf.openDatabase(false, location, Long
 						.parseLong(dString[1]));
 				location += databases[d].getByteSize();
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		location = firstByte();
 		databaseNum = 0;
 	}
 
 	@Override
 	public void putBytes(byte[] arr, int off, int len) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void seek(long loc) {
 		location = loc;
 		databaseNum = getDatabase(loc);
 	}
 
 	public static void main(String[] args) throws ClassNotFoundException,
 			IOException {
 		Configuration conf = new Configuration(Configuration
 				.readProperties(args[0]));
 		byte[] confBytes = conf.store();
 		File confFile = new File(args[1]);
 		FileOutputStream fos = new FileOutputStream(confFile);
 		for (int i = 24; i >= 0; i -= 8)
 			fos.write(confBytes.length >> i);
 		fos.write(confBytes);
 		fos.close();
 	}
 }
