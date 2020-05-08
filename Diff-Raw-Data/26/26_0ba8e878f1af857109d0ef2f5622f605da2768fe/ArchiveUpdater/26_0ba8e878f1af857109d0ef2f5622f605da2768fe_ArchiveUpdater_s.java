 package com.froyo;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.JarOutputStream;
 
 /**
  * Simple utility class for updating an archive with one or more class files.
  */
 public class ArchiveUpdater {
 
 	private static final String CLASSESDIR = "patches";
 	private static final File patchFileDir = new File(CLASSESDIR);
 	private boolean interactive = true;
 	private static final byte[] BUFFER = new byte[1024];
 
 	public ArchiveUpdater() {
 	}
 
 	/**
 	 * Execute archive update process.
 	 */
 	public void execute(String archive) throws Exception {
 
 		File archiveFile = new File(archive);
 		File tempArchive = new File(archive + ".tmp");
 
 		JarFile existingJar = new JarFile(archiveFile);
 
 		boolean jarUpdated = false;
 
 		jarUpdated = applyPatchesToArchive(existingJar, tempArchive);
 
 		if (jarUpdated) {
 			archiveFile.delete();
 			tempArchive.renameTo(archiveFile);
 		}
 	}
 
 	/**
 	 * Update the temporary archive.
 	 */
 	private boolean applyPatchesToArchive(JarFile existingJar, File tempJarFile)
 			throws Exception {
 
 		boolean updated = false;
 
 		System.out.println("Patching " + existingJar.getName() + " ...");
 		try {
 
 			JarOutputStream temp = new JarOutputStream(new FileOutputStream(
 					tempJarFile));
 
 			try {
 
 				List<File> patchFiles = Arrays.asList(patchFileDir.listFiles());
 
 				for (Enumeration entries = existingJar.entries(); entries
 						.hasMoreElements();) {
 
 					JarEntry entry = (JarEntry) entries.nextElement();
 
 					for (File patchFile : patchFiles) {
 
 						processPatchFile(existingJar, temp, entry, patchFile);
 					}
 				}
 
 				updated = true;
 			} catch (Exception ex) {
 				System.out.println(ex);
 				temp.putNextEntry(new JarEntry("stub"));
 			} finally {
 				temp.close();
 			}
 
 		} finally {
 			existingJar.close();
 
 			if (!updated) {
 				tempJarFile.delete();
 			}
 		}
 		System.out.println("Finished patching ...");
 		return updated;
 	}
 
 	/**
 	 * Process a single patch file.
 	 */
 	private void processPatchFile(JarFile existingJar, JarOutputStream tempJar,
 			JarEntry entry, File patchFile) throws Exception {
 
 		final String className = patchFile.getName();
		FileInputStream classFile = null;
 
 		try {
 
 			String entryName = new File(entry.getName()).getName();
			classFile = new FileInputStream(patchFile);
 
 			System.out.println(entry.getName());
 			if (!entryName.equals(className)) {
 
 				writeEntry(existingJar, tempJar, entry);
 
 			} else {
 
 				if (interactive) {
 
 					if (prompt("\nPatch " + entry.getName() + "? [y/n]: ")) {
 						patchClassFile(tempJar, className, classFile);
 					} else {
 						System.out.println("Skipping...\n");
 						writeEntry(existingJar, tempJar, entry);
 					}
 
 				} else {
					patchClassFile(tempJar, className, classFile);
 				}
 			}
 
 		} finally {
			classFile.close();
 		}
 	}
 
 	/**
 	 * Write an entry to the temp jar 
 	 */
 	private void writeEntry(JarFile existingJar, JarOutputStream tempJar,
 			JarEntry entry) throws IOException {
 		
 		int bytesRead;
 		InputStream entryStream = existingJar.getInputStream(entry);
 
 		// Read the entry and write it to the temp jar.
 		tempJar.putNextEntry(entry);
 
 		while ((bytesRead = entryStream.read(BUFFER)) != -1) {
 			tempJar.write(BUFFER, 0, bytesRead);
 		}
 	}
 
 	/**
 	 * Patch a class file
 	 */
 	private void patchClassFile(JarOutputStream tempJar, 
 			final String fileName, 
 			FileInputStream origFile) throws IOException {
 
 		int bytesRead;
 		JarEntry patch = new JarEntry(fileName);
 		tempJar.putNextEntry(patch);
 
 		// Read the file and write it to the jar.
 		while ((bytesRead = origFile.read(BUFFER)) != -1) {
 			tempJar.write(BUFFER, 0, bytesRead);
 		}
 		System.out.println(patch.getName() + " patched");
 	}
 
 	/**
 	 * Prompt for yes/no response from user
 	 */
 	private boolean prompt(String msg) {
 
 		System.out.print(msg);
 
 		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

 		String answer = null;
 
 		try {
 			answer = br.readLine();
 
 			if (answer.equalsIgnoreCase("y")) {
 				return true;
 			}
 
 		} catch (IOException ioe) {
			exit("IO error trying to read your name!");
 		}
 		return false;
 	}
 
 	/**
 	 * Exit with a msg.
 	 */
 	private static void exit(String msg) {
 		System.err.println(msg);
 		System.exit(1);
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		if (args == null || args.length < 1) {
 			exit("Usage: " + ArchiveUpdater.class.getCanonicalName()
 					+ " <archivefiletoupdate>");
 		}
 
 		String jarName = args[0];
 		File jarFile = new File(jarName);
 
 		if (!jarFile.exists()) {
 			exit("Archive file specified: " + jarFile + " does not exist");
 		}
 
 		if (!patchFileDir.exists()) {
 			exit("Cannot find the directory containing the patches.  Please create: ./"
 					+ CLASSESDIR);
 		}
 
 		if (Arrays.asList(patchFileDir.listFiles()).size() == 0) {
 			exit("No patch files found in directory: ./" + CLASSESDIR
 					+ " to replace in archive: " + jarName);
 		}
 
 		new ArchiveUpdater().execute(jarName);
 
 	}
}
