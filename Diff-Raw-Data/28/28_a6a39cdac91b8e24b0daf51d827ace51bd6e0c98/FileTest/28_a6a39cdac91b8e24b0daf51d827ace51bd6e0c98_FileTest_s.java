 package info.guardianproject.test.iocipher;
 
 import info.guardianproject.iocipher.File;
 import info.guardianproject.iocipher.FileInputStream;
 import info.guardianproject.iocipher.FileOutputStream;
 import info.guardianproject.iocipher.FileWriter;
 import info.guardianproject.iocipher.VirtualFileSystem;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 
 import android.content.Context;
 import android.test.AndroidTestCase;
 import android.util.Log;
 
 public class FileTest extends AndroidTestCase {
 	private final static String TAG = "FileTest";
 
 	private VirtualFileSystem vfs;
 
 	protected void setUp() {
 		java.io.File db = new java.io.File(mContext.getDir("vfs",
 				Context.MODE_PRIVATE).getAbsoluteFile(), "sqlcipherfs.db");
 		if (db.exists())
 			db.delete();
 		vfs = new VirtualFileSystem(db.getAbsolutePath());
 		vfs.mount();
 	}
 
 	protected void tearDown() {
 		vfs.unmount();
 	}
 
 	public void testExists() {
 		File f = new File("");
 		try {
 			assertFalse(f.exists());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testGetFreeSpace() {
 		File f = new File("");
 		try {
 			long free = f.getFreeSpace();
 			Log.i(TAG, "f.getFreeSpace: " + Long.toString(free));
 			assertTrue(free > 0);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testGetUsableSpace() {
 		File f = new File("");
 		try {
 			long total = f.getUsableSpace();
 			Log.i(TAG, "f.getUsableSpace: " + Long.toString(total));
 			assertTrue(total > 0);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testGetTotalSpace() {
 		File f = new File("");
 		try {
 			long total = f.getTotalSpace();
 			Log.i(TAG, "f.getTotalSpace: " + Long.toString(total));
 			assertTrue(total > 0);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirExists() {
 		File f = new File("/test.iocipher.dir."
 				+ Integer.toString((int) (Math.random() * 1024)));
 		try {
 			assertFalse(f.exists());
 			assertTrue(f.mkdir());
 			assertTrue(f.exists());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirs() {
 		File f0 = new File("/"
 				+ Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		File f1 = new File(f0,
 				Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		File f2 = new File(f1,
 				Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		Log.i(TAG, "f2: " + f2.getAbsolutePath());
 		try {
 			f2.mkdirs();
 			for (String f : f0.list()) {
 				Log.i(TAG, "file in f0: " + f);
 			}
 			for (String f : f1.list()) {
 				Log.i(TAG, "file in f1: " + f);
 			}
 			assertTrue(f0.exists());
 			assertTrue(f1.exists());
 			assertTrue(f2.exists());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testSlashIsDirectory() {
 		File f = new File("/");
 		try {
 			assertTrue(f.isDirectory());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testSlashIsFile() {
 		File f = new File("/");
 		try {
 			assertFalse(f.isFile());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testSlashIsAbsolute() {
 		File f = new File("/");
 		try {
 			assertTrue(f.isAbsolute());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirRemove() {
 		File f = new File("/mkdir-to-remove");
 		try {
 			assertTrue(f.mkdir());
 			assertTrue(f.exists());
 			assertTrue(f.delete());
 			assertFalse(f.exists());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirRename() {
 		File f = new File("/mkdir-to-rename");
 		try {
 			f.mkdir();
 			f.renameTo(new File("/renamed"));
 			assertFalse(new File("/mkdir-to-rename").exists());
 			assertTrue(new File("/renamed").exists());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirIsDirectory() {
 		File f = new File("/mkdir-to-test");
 		try {
 			f.mkdir();
 			assertTrue(f.isDirectory());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	/*
 	 * public void testCreateFile() { File f = new File("/TESTFILE." +
 	 * Integer.toString((int) (Math.random() * Integer.MAX_VALUE))); try {
 	 * assertFalse(f.exists()); //f.createNewFile(); assertTrue(f.isFile()); }
 	 * catch (Exception e) { Log.e(TAG, e.getCause().toString());
 	 * assertFalse(true); } }
 	 */
 	public void testMkdirList() {
 		File root = new File("/");
 		File f = new File("/mkdir-to-list");
 		try {
 			f.mkdir();
 			final String[] files = root.list();
 			for (String filename : files) {
 				Log.i(TAG, "testMkdirList file: " + filename);
 			}
 			assertTrue(files.length > 2); // it should always give us "." and
 											// ".."
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirLastModified() {
 		File root = new File("/");
 		File f = new File("/test.iocipher.dir."
 				+ Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		try {
 			long lasttime = root.lastModified();
 			Log.i(TAG, "f.lastModified: " + Long.toString(lasttime));
 			f.mkdir();
 			long thistime = root.lastModified();
 			Log.i(TAG,
 					"f.lastModified after setting: " + Long.toString(thistime));
 			assertTrue(thistime > lasttime);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirMtime() {
 		File f = new File("/mkdir-with-mtime");
 		long faketime = 1000000000L;
 		try {
 			f.mkdir();
 			Log.i(TAG, "f.lastModified: " + Long.toString(f.lastModified()));
 			f.setLastModified(faketime);
 			long time = f.lastModified();
 			Log.i(TAG, "f.lastModified after setting: " + Long.toString(time));
 			assertTrue(time == faketime);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testCreateNewFile() {
 		File root = new File("/");
 		File f = new File("/testCreateNewFile."
 				+ Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		try {
 			assertFalse(f.exists());
 			f.createNewFile();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			final String[] files = root.list();
 			for (String filename : files) {
 				Log.i(TAG, "testCreateNewFile file: " + filename);
 			}
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteNewFile() {
 		File root = new File("/");
 		File f = new File("/testWriteNewFile."
 				+ Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		try {
 			assertTrue(root.isDirectory());
 			assertFalse(f.exists());
 			FileOutputStream out = new FileOutputStream(f);
 			out.write(123);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			final String[] files = root.list();
 			for (String filename : files) {
 				Log.i(TAG, "testWriteNewFile file: " + filename);
 			}
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteByteInNewFileThenRead() {
 		byte testValue = 43;
 		File root = new File("/");
 		File f = new File("/testWriteNewFile."
 				+ Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		try {
 			assertTrue(root.isDirectory());
 			assertFalse(f.exists());
 			FileOutputStream out = new FileOutputStream(f);
 			out.write(testValue);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			FileInputStream in = new FileInputStream(f);
 			int b = in.read();
 			Log.i(TAG, "read: " + Integer.toString(b));
 			assertTrue(b == testValue);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteTextInNewFileThenReadByByte() {
 		String testString = "this is a test of IOCipher!";
		File f = new File("/testAWriteTextInNewFileThenReadByByte."
 				+ Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			out.write(testString);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			FileInputStream in = new FileInputStream(f);
 			byte[] data = new byte[testString.length()];
 			in.read(data, 0, data.length);
 			String dataString = new String(data);
 			Log.i(TAG, "read: " + dataString);
 			assertTrue(dataString.equals(testString));
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 }
