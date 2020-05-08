 package info.guardianproject.test.iocipher;
 
 import info.guardianproject.iocipher.File;
 import info.guardianproject.iocipher.FileInputStream;
 import info.guardianproject.iocipher.FileOutputStream;
 import info.guardianproject.iocipher.FileReader;
 import info.guardianproject.iocipher.FileWriter;
 import info.guardianproject.iocipher.IOCipherFileChannel;
 import info.guardianproject.iocipher.RandomAccessFile;
 import info.guardianproject.iocipher.VirtualFileSystem;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.Formatter;
 import java.util.Random;
 
 import android.content.Context;
 import android.test.AndroidTestCase;
 import android.test.suitebuilder.annotation.SmallTest;
 import android.util.Log;
 
 public class CipherFileTest extends AndroidTestCase {
 	private final static String TAG = "FileTest";
 
 	private VirtualFileSystem vfs;
 	private File ROOT = null;
 
 	// Utility methods
 	private String randomFileName(String testName) {
 		String name = null;
 		do {
 			name = "/" + testName + "." + Integer.toString((int) (Math.random() * Integer.MAX_VALUE));
 		} while((new File(name)).exists());
 		return name;
 	}
 
 	private static String toHex(byte[] digest) {
         Formatter formatter = new Formatter();
         for (byte b : digest) {
             formatter.format("%02x", b);
         }
         return formatter.toString();
     }
 
 	private boolean writeRandomBytes(int bytes, String filename) {
 		try {
 			File f = new File(filename);
 			FileOutputStream out = new FileOutputStream(f);
 
 			Random prng = new Random();
 			byte[] random_buf = new byte[bytes];
 			prng.nextBytes(random_buf);
 
 			out.write(random_buf);
 			out.close();
 
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 		return true;
 	}
 
 	@Override
 	protected void setUp() {
 		java.io.File db = new java.io.File(mContext.getDir("vfs",
 				Context.MODE_PRIVATE).getAbsoluteFile(), TAG + ".db");
 		if (db.exists())
 			db.delete();
 		Log.v(TAG, "database file: " + db.getAbsolutePath());
 		if (db.exists())
 			Log.v(TAG, "exists: " + db.getAbsolutePath());
 		if (!db.canRead())
 			Log.v(TAG, "can't read: " + db.getAbsolutePath());
 		if (!db.canWrite())
 			Log.v(TAG, "can't write: " + db.getAbsolutePath());
 		vfs = new VirtualFileSystem(db.getAbsolutePath());
 		vfs.mount("this is my secure password");
 		ROOT = new File("/");
 	}
 
 	@Override
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
 
 //	public void testGetFreeSpace() {
 //		File f = new File(ROOT, "");
 //		try {
 //			long free = f.getFreeSpace();
 //			Log.v(TAG, "f.getFreeSpace: " + Long.toString(free));
 //			assertTrue(free > 0);
 //		} catch (ExceptionInInitializerError e) {
 //			Log.e(TAG, e.getCause().toString());
 //			assertFalse(true);
 //		}
 //	}
 
 //	public void testGetUsableSpace() {
 //		File f = new File(ROOT, "");
 //		try {
 //			long total = f.getUsableSpace();
 //			Log.v(TAG, "f.getUsableSpace: " + Long.toString(total));
 //			assertTrue(total > 0);
 //		} catch (ExceptionInInitializerError e) {
 //			Log.e(TAG, e.getCause().toString());
 //			assertFalse(true);
 //		}
 //	}
 
 //	public void testGetTotalSpace() {
 //		File f = new File(ROOT, "");
 //		try {
 //			long total = f.getTotalSpace();
 //			Log.v(TAG, "f.getTotalSpace: " + Long.toString(total));
 //			assertTrue(total > 0);
 //		} catch (ExceptionInInitializerError e) {
 //			Log.e(TAG, e.getCause().toString());
 //			assertFalse(true);
 //		}
 //	}
 
 	public void testMkdirExists() {
 		File f = new File(ROOT, "test.iocipher.dir."
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
 		File f0 = new File(ROOT, Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		File f1 = new File(f0,
 				Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		File f2 = new File(f1,
 				Integer.toString((int) (Math.random() * Integer.MAX_VALUE)));
 		Log.v(TAG, "f2: " + f2.getAbsolutePath());
 		try {
 			f2.mkdirs();
 			for (String f : f0.list()) {
 				Log.v(TAG, "file in f0: " + f);
 			}
 			for (String f : f1.list()) {
 				Log.v(TAG, "file in f1: " + f);
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
 		File f = ROOT;
 		try {
 			assertTrue(f.isDirectory());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testCanReadSlash() {
 		File f = ROOT;
 		try {
 			assertTrue(f.isDirectory());
 			assertTrue(f.canRead());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testCanWriteSlash() {
 		File f = ROOT;
 		try {
 			assertTrue(f.isDirectory());
 			assertTrue(f.canWrite());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testSlashIsFile() {
 		File f = ROOT;
 		try {
 			assertFalse(f.isFile());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testSlashIsAbsolute() {
 		File f = ROOT;
 		try {
 			assertTrue(f.isAbsolute());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirRemove() {
 		File f = new File(ROOT, "mkdir-to-remove");
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
 
 	public void testARenameDirFailure() {
 		File d = new File(ROOT, "dir-to-rename");
 		try {
 			d.mkdir();
 			assertFalse(d.renameTo(new File(ROOT, "somethingelse")));
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 
 	public void testMkdirRename() {
 		// TODO libsqlfs does not support renaming directories
 		fail("TODO libsqlfs does not support renaming directories");
 		/*
 		String dir = "/mkdir-to-rename";
 		String newdir = "/renamed";
 		String firstfile = "first-file";
 		File root = ROOT;
 		File d = new File(dir);
 		File newd = new File(newdir);
 		try {
 			d.mkdir();
 			File f1 = new File(d, firstfile);
 			f1.createNewFile();
 			assertTrue(f1.exists());
 			String[] files = d.list();
 			for (String filename : files) {
				Log.v(TAG, "testMkdirList " + dir + ": " + filename);
 			}
 			assertTrue(d.renameTo(newd));
 			assertTrue(new File(newd, firstfile).exists());
 			File f2 = new File(newd, "second-file");
 			f2.createNewFile();
 			files = root.list();
 			for (String filename : files) {
				Log.v(TAG, "testMkdirList root: " + filename);
 			}
 			files = newd.list();
 			for (String filename : files) {
				Log.v(TAG, "testMkdirList " + newdir + ": " + filename);
 			}
 			assertFalse(d.exists());
 			assertTrue(newd.exists());
 			assertTrue(f2.exists());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}*/
 	}
 
 	public void testNewFileRename() {
 		File root = ROOT;
 		File f = new File(randomFileName("testNewFileRename-NEW"));
 		File newf = new File(randomFileName("testNewFileRename-RENAMED"));
 		try {
 			f.createNewFile();
 			assertTrue(f.renameTo(newf));
 			final String[] files = root.list();
 			for (String filename : files) {
 				Log.v(TAG, "testNewFileRename file: " + filename);
 			}
 			assertFalse(f.exists());
 			assertTrue(newf.exists());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirIsDirectory() {
 		File f = new File(ROOT, "mkdir-to-test");
 		try {
 			f.mkdir();
 			assertTrue(f.isDirectory());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testMkdirList() {
 		File root = ROOT;
 		File f = new File(ROOT, "mkdir-to-list");
 		try {
 			f.mkdir();
 			final String[] files = root.list();
 			for (String filename : files) {
 				Log.v(TAG, "testMkdirList file: " + filename);
 			}
 			Log.v(TAG, "testMkdirList list: " + files.length);
 			assertTrue(files.length == 1); // ".." and "." shouldn't be included
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 /*
 // TODO testMkdirLastModified fails
 	public void testMkdirLastModified() {
 		File root = ROOT;
 		File f = new File(randomFileName("test.iocipher.dir"));
 		try {
 			long lasttime = root.lastModified();
 			Log.v(TAG, "f.lastModified: " + Long.toString(lasttime));
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
 
 // TODO testMkdirMtime fails
 	public void testMkdirMtime() {
 		File f = new File("/mkdir-with-mtime");
 		long faketime = 1000000000L;
 		try {
 			f.mkdir();
 			Log.v(TAG, "f.lastModified: " + Long.toString(f.lastModified()));
 			f.setLastModified(faketime);
 			long time = f.lastModified();
 			Log.v(TAG, "f.lastModified after setting: " + Long.toString(time));
 			assertTrue(time == faketime);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 */
 
 	public void testCreateNewFile() {
 		File root = ROOT;
 		File f = new File(randomFileName("testCreateNewFile"));
 		try {
 			assertFalse(f.exists());
 			f.createNewFile();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			final String[] files = root.list();
 			for (String filename : files) {
 				Log.v(TAG, "testCreateNewFile file: " + filename);
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
 		File root = ROOT;
 		File f = new File(randomFileName("testWriteNewFile"));
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
 				Log.v(TAG, "testWriteNewFile file: " + filename);
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
 		File root = ROOT;
 		File f = new File(randomFileName("testWriteNewFile"));
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
 			Log.v(TAG, "read: " + Integer.toString(b));
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
 		File f = new File(randomFileName("testWriteTextInNewFileThenReadByByte"));
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
 			Log.v(TAG, "read: " + dataString);
 			assertTrue(dataString.equals(testString));
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteTextInNewFileThenReadIntoByteArray() {
 		String testString = "this is a test of IOCipher!";
 		File f = new File(randomFileName("testWriteTextInNewFileThenReadIntoByteArray"));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			out.write(testString);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			FileInputStream in = new FileInputStream(f);
 			byte[] data = new byte[testString.length()];
 			int ret = in.read(data);
 			assertTrue(ret == data.length);
 			String dataString = new String(data);
 			assertTrue(dataString.equals(testString));
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteTextInNewFileThenReadOneByteByByte() {
 		String testString = "01234567890abcdefgh";
 		File f = new File(randomFileName("testWriteTextInNewFileThenReadOneByteByByte."));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			out.write(testString);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			FileInputStream in = new FileInputStream(f);
 			byte[] data = new byte[testString.length()];
 			int ret = 0;
 			int i = 0;
 			while (i < data.length) {
 				ret = in.read();
 				if (ret != -1) {
 					data[i] = (byte) ret;
 					i++;
 				} else {
 					break;
 				}
 			}
 			String dataString = new String(data);
 			Log.v(TAG, "read: " + dataString);
 			assertTrue(dataString.equals(testString));
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteTextInNewFileThenCheckSize() {
 		String testString = "01234567890abcdefgh";
 		File f = new File(randomFileName("testWriteTextInNewFileThenCheckSize"));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			out.write(testString);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			FileInputStream in = new FileInputStream(f);
 			IOCipherFileChannel channel = in.getChannel();
 			assertTrue(channel.size() == testString.length());
 			assertTrue(testString.length() == f.length());
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteTextInNewFileThenSkipAndRead() {
 		String testString = "01234567890abcdefghijklmnopqrstuvxyz";
 		File f = new File(randomFileName("testWriteTextInNewFileThenSkipAndRead"));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			out.write(testString);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			FileInputStream in = new FileInputStream(f);
 			char c = (char) in.read();
 			assertTrue(c == testString.charAt(0));
 			in.skip(5);
 			c = (char) in.read();
 			Log.v(TAG, "c: " + c + "  testString.charAt(6): "
 							+ testString.charAt(6));
 			assertTrue(c == testString.charAt(6));
 			in.skip(20);
 			c = (char) in.read();
 			Log.v(TAG,"c: " + c + "  testString.charAt(27): "
 							+ testString.charAt(27));
 			assertTrue(c == testString.charAt(27));
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteRepeat() {
 		int i, repeat = 1000;
 		String testString = "01234567890abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\n";
 		File f = new File(randomFileName("testWriteRepeat"));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			for (i = 0; i< repeat ; i++)
 				out.write(testString);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			Log.v(TAG, f.toString() + ".length(): " + f.length() + " " + testString.length() * repeat);
 			assertTrue(f.length() == testString.length() * repeat);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteSkipWrite() {
 		int skip = 100;
 		String testString = "testWriteSkipWrite0123456789\n";
 		File f = new File(randomFileName("testWriteSkipWrite"));
 		try {
 			assertFalse(f.exists());
 			RandomAccessFile inout = new RandomAccessFile(f, "rw");
 			inout.writeBytes(testString);
 			inout.seek(skip);
 			inout.writeBytes(testString);
 			inout.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			int inputLength = (testString.length() * 2) + skip;
 			Log.v(TAG, f.toString() + ".length(): " + f.length() + " " + inputLength);
 			assertTrue(f.length() == inputLength);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteTextInNewFileThenFileInputStream() {
 		String testString = "01234567890abcdefgh";
 		File f = new File(randomFileName("testWriteTextInNewFileThenFileInputStream"));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			out.write(testString);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			BufferedReader in = new BufferedReader(new FileReader(f));
 			String tmp = in.readLine();
 			Log.v(TAG, "in.readline(): " + tmp);
 			assertTrue(testString.equals(tmp));
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteManyLinesInNewFileThenFileInputStream() {
 		String testString = "01234567890abcdefghijklmnopqrstuvwxyz";
 		File f = new File(randomFileName("testWriteManyLinesInNewFileThenFileInputStream"));
 		try {
 			assertFalse(f.exists());
 			BufferedWriter out = new BufferedWriter(new FileWriter(f));
 			for (int i = 0; i < 25; i++)
 				out.write(testString + "\n");
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			BufferedReader in = new BufferedReader(new FileReader(f));
 			for (int i = 0; i < 25; i++) {
 				String tmp = in.readLine();
 				Log.v(TAG, "in.readline(): " + tmp);
 				assertTrue(testString.equals(tmp));
 			}
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	private byte[] digest(File f) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("SHA-1");
 			FileInputStream fstr = new FileInputStream(f);
 			DigestInputStream dstr = new DigestInputStream(fstr, md);
 
 			// read to EOF, really Java? *le sigh*
 			while (dstr.read() != -1);
 
 			dstr.close();
 			return md.digest();
 		} catch (NoSuchAlgorithmException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 		return null;
 	}
 
 	public void testFileChannelTransferTo() {
 		String input_name = "/testCopyFileChannels-input";
 		String output_name = "/testCopyFileChannels-output";
 		writeRandomBytes(1000, input_name);
 		File inputFile = new File(input_name);
 		File outputFile = new File(output_name);
 
 		try {
 			assertTrue(inputFile.exists());
 			assertTrue(inputFile.isFile());
 
 			assertFalse(outputFile.exists());
 
 			FileInputStream source = new FileInputStream(inputFile);
 			FileOutputStream destination = new FileOutputStream(output_name);
 			IOCipherFileChannel sourceFileChannel = source.getChannel();
 			IOCipherFileChannel destinationFileChannel = destination.getChannel();
 
 	        sourceFileChannel.transferTo(0, sourceFileChannel.size(), destinationFileChannel);
 	        sourceFileChannel.close();
 	        destinationFileChannel.close();
 
 	        assertTrue(outputFile.exists());
 			assertTrue(outputFile.isFile());
 			assertEquals(inputFile.length(), outputFile.length());
 
 			byte[] expected = digest(inputFile);
 			byte[] actual = digest(outputFile);
 
 			Log.i(TAG, "file hashes:" + toHex(expected) +"    "+ toHex(actual));
 			assertTrue( Arrays.equals(expected, actual));
 
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testFileChannelTransferFrom() {
 		String input_name = "/testCopyFileChannels-input";
 		String output_name = "/testCopyFileChannels-output";
 		writeRandomBytes(1000, input_name);
 		File inputFile = new File(input_name);
 		File outputFile = new File(output_name);
 
 		try {
 			assertTrue(inputFile.exists());
 			assertTrue(inputFile.isFile());
 
 			assertFalse(outputFile.exists());
 
 			FileInputStream source = new FileInputStream(inputFile);
 			FileOutputStream destination = new FileOutputStream(output_name);
 			IOCipherFileChannel sourceFileChannel = source.getChannel();
 			IOCipherFileChannel destinationFileChannel = destination.getChannel();
 
 	        destinationFileChannel.transferFrom(sourceFileChannel, 0, sourceFileChannel.size());
 	        sourceFileChannel.close();
 	        destinationFileChannel.close();
 
 	        assertTrue(outputFile.exists());
 			assertTrue(outputFile.isFile());
 			assertEquals(inputFile.length(), outputFile.length());
 
 			byte[] expected = digest(inputFile);
 			byte[] actual = digest(outputFile);
 
 			Log.i(TAG, "file hashes:" + toHex(expected) +"    "+ toHex(actual));
 			assertTrue( Arrays.equals(expected, actual));
 
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	@SmallTest
 	public void testFileExistingTruncate() {
 		String name = randomFileName("testFileExistingTruncate");
 		writeRandomBytes(500, name);
 
 		File f = new File(name);
 		assertEquals(500, f.length());
 
 		try {
 			FileOutputStream out = new FileOutputStream(f);
 			out.close();
 			assertEquals(0, f.length());
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	@SmallTest
 	public void testFileExistingAppend() {
 		String name = randomFileName("testFileExistingAppend");
 		writeRandomBytes(500, name);
 
 		File f = new File(name);
 		assertEquals(500, f.length());
 
 		try {
 			FileOutputStream out = new FileOutputStream(f, true);
 
 			//write 2 bytes
 			out.write(1);
 			out.write(2);
 			out.close();
 
 			assertEquals(502, f.length());
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testWriteByteInExistingFileThenRead() {
 		byte testValue = 43;
 		byte secondTestValue = 100;
 		File root = ROOT;
 		File f = new File(randomFileName("testWriteByteInExistingFileThenRead"));
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
 			in.close();
 			Log.v(TAG, "read: " + Integer.toString(b));
 			assertTrue(b == testValue);
 
 			// now overwrite
 			out = new FileOutputStream(f);
 			out.write(secondTestValue);
 			out.close();
 			assertTrue(f.exists());
 			assertTrue(f.isFile());
 			in = new FileInputStream(f);
 			b = in.read();
 			in.close();
 			assertTrue(b == secondTestValue);
 		} catch (ExceptionInInitializerError e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		} catch (IOException e) {
 			Log.e(TAG, e.getCause().toString());
 			assertFalse(true);
 		}
 	}
 
 	public void testEqualsAndCompareTo() {
 		String filename = "thisisafile";
 		File f = new File(ROOT, filename);
 		File dup = new File(ROOT,filename);
 		File diff = new File(ROOT, "differentfile");
 		assertTrue(f.equals(dup));
 		assertTrue(f.compareTo(dup) == 0);
 		assertFalse(f.equals(diff));
 		assertTrue(f.compareTo(diff) != 0);
 	}
 }
