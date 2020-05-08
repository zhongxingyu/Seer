 package com.johnpickup.backup;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Build a jbackup catalog file from an existing directory
  * @author John
  *
  */
 public class CatalogBuilder {
	private static final String CATALOG_FILE = "jbackup.cat";
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		if (args.length != 1) {
 			System.err.println("Usage: CatalogBuilder <targetDir>");
 			System.exit(1);
 		}
 		
 		String targetPath = args[0];
 		FileCatalog catalog = new FileCatalog();
 		DirectoryWalker walker = new DirectoryWalker();
 
 		List<File> entries = walker.listEntries(new File(targetPath));
 		for (File file : entries) {
 			String relativePath = ResourceUtils.getRelativePath(file.getAbsolutePath(), targetPath, File.separator);
 			catalog.add(relativePath, file.length(), file.lastModified());
 		}
 		String catalogFilename = targetPath + File.separator + CATALOG_FILE;
 		catalog.saveToFile(catalogFilename);
 	}
 
 }
