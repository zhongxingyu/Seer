 package de.dhbw.mannheim.cloudraid.fs;
 
 import java.io.File;
 import java.nio.file.Files;
 import java.util.Vector;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class RecursiveFileSystemWatcher extends Thread {
 
 	private File dir;
 	private final static String TMP = System.getProperty("os.name")
 			.toLowerCase().contains("windows") ? "C:\\temp\\cloudraid\\"
 			: "/tmp/cloudraid/";
 	private final static File TMP_FILE = new File(TMP);
 
 	/**
 	 * A map containing all known files.
 	 */
 	private static ConcurrentHashMap<String, Long> fileMap = new ConcurrentHashMap<String, Long>();
 
 	private Vector<String> keySet = new Vector<String>();
 
 	private long sleepTime = 10000;
 
 	/**
 	 * Creates a RecursiveFileSystemWatcher that runs every 10s.
 	 */
 	public RecursiveFileSystemWatcher() {
 		dir = new File(System.getProperty("user.home") + "/tmp/");
 		System.out.println("Watching directory " + dir.getAbsolutePath());
 		this.setPriority(MIN_PRIORITY);
 	}
 
 	/**
 	 * Creates a RecursiveFileSystemWatcher that runs in the given interval.
 	 * 
 	 * @param sleepTime
 	 *            The sleeping time in ms.
 	 */
 	public RecursiveFileSystemWatcher(long sleepTime) {
 		this();
 		this.sleepTime = sleepTime;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void run() {
 		while (!isInterrupted()) {
 			keySet = new Vector<String>(fileMap.keySet());
 
 			if (!this.dir.exists()) {
 				System.err.println("The watch directory does not exist");
 				break;
 			} else {
 				this.checkDir(this.dir);
 			}
 
 			// all files still in "keySet" were not found, this means they were
 			// deleted
 			for (String k : keySet) {
 				System.out.println(k + " was deleted.");
 				fileMap.remove(k);
 			}
 
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		System.err.println("The file system watcher is stopped");
 	}
 
 	/**
 	 * Runs through the list of files in the given directory and handles the
 	 * files according to their type.
 	 * 
 	 * @param file
 	 *            The directory to be handled.
 	 */
 	private void checkDir(File dir) {
		if (dir.isFile()) {
 			for (File f : dir.listFiles()) {
 				if (Files.isSymbolicLink(f.toPath())) {
 					System.err.println("I do not handle the symbolic link at "
 							+ f.getAbsolutePath());
 				} else if (f.isDirectory()) {
 					this.checkDir(f);
 				} else if (f.isFile()) {
 					this.checkFile(f);
 				} else {
 					System.err
 							.println("Whoops! I don't know how to handle the file "
 									+ f.getAbsolutePath());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks the given file and handles it according to the status.
 	 * 
 	 * @param file
 	 *            The file to be handled.
 	 */
 	private void checkFile(File file) {
 		String name = file.getAbsolutePath();
 		if (fileMap.containsKey(name)) {
 			if (file.lastModified() == fileMap.get(name)) {
 				// nothing to do, file already indexed
 				// System.out.println(file.getAbsolutePath() +
 				// " already exists.");
 			} else {
 				// the file changed
 				System.out.println(file.getAbsolutePath() + " was changed.");
 			}
 			keySet.remove(name);
 		} else {
 			// a new file is found
 			System.out.println(file.getAbsolutePath() + " is a new file.");
 			fileMap.put(file.getAbsolutePath(), file.lastModified());
 		}
 	}
 
 	public static void main(String[] args) {
 		new RecursiveFileSystemWatcher(60000).start();
 		new RecursiveFileSystemWatcher(50000).start();
 	}
 }
