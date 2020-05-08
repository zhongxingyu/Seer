 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import net.sf.okapi.common.exceptions.OkapiIOException;
 
 /**
  * Helper methods for manipulating files. 
  */
 public final class FileUtil {
 
 	/**
 	 * Gets an array of the files in a given directory.
 	 * <p>This method searches all {@link File}s recursively that pass the {@link FilenameFilter}.
 	 * Adapted from http://snippets.dzone.com/posts/show/1875
 	 * @param directory root directory
 	 * @param filter {@link FilenameFilter} used to filter the File candidates
 	 * @param recurse true to recurse in the sub-directories, false to not.
 	 * @return an array of {@link File}s (File[])
 	 */
 	public static File[] getFilteredFilesAsArray (File directory,
 		FilenameFilter filter,
 		boolean recurse)
 	{
 		Collection<File> files = FileUtil.getFilteredFiles(directory, filter, recurse);
 		File[] arr = new File[files.size()];
 		return files.toArray(arr);
 	}
 
 	/**
 	 * Gets a collection of the files in a given directory.
 	 * <p>This method search all {@link File}s recursively that pass the {@link FilenameFilter}.
 	 * Adapted from http://snippets.dzone.com/posts/show/1875
 	 * @param directory root directory
 	 * @param filter {@link FilenameFilter} used to filter the File candidates
 	 * @param recurse true to recurse in the sub-directories, false to not.
 	 * @return {@link Collection} of {@link File}s
 	 */
 	public static Collection<File> getFilteredFiles (File directory,
 		FilenameFilter filter,
 		boolean recurse)
 	{
 		// List of files / directories
 		List<File> files = new LinkedList<File>();
 	
 		// Get files / directories in the directory
 		File[] entries = directory.listFiles();
 	
 		if (entries == null) {
 			return files;
 		}
 		
 		// Go over entries
 		for (File entry : entries) {
 			// If there is no filter or the filter accepts the
 			// file / directory, add it to the list
 			if (filter == null || filter.accept(directory, entry.getName())) {
 				files.add(entry);
 			}
 	
 			// If the file is a directory and the recurse flag
 			// is set, recurse into the directory
 			if (recurse && entry.isDirectory()) {
 				files.addAll(getFilteredFiles(entry, filter, recurse));
 			}
 		}
 	
 		// Return collection of files
 		return files;
 	}
 
 	/**
 	 * Tries to guess the language(s) declared in the given input file. The method should work with XLIFF, TMX, TTX and TS files.
 	 * <p>The methods looks in the file line by line, in the 10 first KB, or until a source and at least
 	 * one target are detected, whichever comes first.
 	 * @param path the full path of the file to process.
 	 * @return a list of strings that can be empty (never null). The first string is the possible source
 	 * language, the next strings are the potential target languages. 
 	 */
 	public static List<String> guessLanguages (String path) {
 		ArrayList<String> list = new ArrayList<String>();
 		BufferedReader reader = null;
 		
 		try {
 			// Deal with the potential BOM
 			String encoding = Charset.defaultCharset().name();
 			BOMAwareInputStream bis = new BOMAwareInputStream(new FileInputStream(path), encoding);
 			encoding = bis.detectEncoding();
 			
 			// Open the input document with BOM-aware reader
 			reader = new BufferedReader(new InputStreamReader(bis, encoding));
 			
 			// Read the top of the file
 			String trgValue = null;
 			Pattern pattern = Pattern.compile(
 				"\\s(srclang|source-?language|xml:lang|lang|(target-?)?language)\\s*?=\\s*?['\"](.*?)['\"]",
 				Pattern.CASE_INSENSITIVE);
 
 			int scanned = 0;
 			while ( true ) {
 				
 				String line = reader.readLine();
 				if ( line == null ) return list;
 				scanned += line.length();
 			
 				// Else: Try the detect the language codes
 				// For XLIFF: source-language, xml:lang, lang, target-language
 				// For TMX: srcLang, xml:lang, lang
 				// For TTX: SourceLanguage, TargetLanguage, Lang
 				// For TS: sourcelanguage, language
 				// Note: the order matter: target cases should be last
 				Matcher m = pattern.matcher(line);
 				int pos = 0;
 				while ( m.find(pos) ) {
 					String lang = m.group(3).toLowerCase();
 					if ( lang.isEmpty() ) {
 						pos = m.end();
 						continue;
 					}
 					String name = m.group(1).toLowerCase();
 				
 					// If we have a header-type target declaration
 					if ( name.equalsIgnoreCase("language") || name.startsWith("target") ) {
 						if ( list.isEmpty() ) {
 							// Note that we don't do anything to handle a second match, but that should be OK
 							trgValue = lang;
 							pos = m.end();
 							continue; // Move to the next
 						}
 						// Else: we can add to the normal list as the source is defined already
 					}
 					
 					// Else: add the language
 					if ( !list.contains(lang) ) {
 						list.add(lang);
 					}
 					// Then check if we have a target to add. This will be done only once.
 					if ( trgValue != null ) {
 						// Add the target
 						list.add(trgValue);
 						trgValue = null;
 					}
 					pos = m.end();
 				}
 			
 				// Don't scan the  whole file
 				if (( scanned > 10240 ) || ( list.size() > 1 )) break;
 			}
 			
 		}
 		catch ( Throwable e ) {
 			new RuntimeException("Error while trying to guess language information.\n"+e.getLocalizedMessage());
 		}
 		finally {
 			if ( reader != null ) {
 				try {
 					reader.close();
 				}
 				catch ( IOException e ) {
 					// Swallow this error
 				}
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * Compresses a given directory. Creates in the same parent folder a ZIP file with the folder name
 	 * as the file name and a given extension.
 	 * The given directory is not deleted after compression.
 	 * <p>This method uses the Java ZIP package and does not supports files to zip that have a path 
 	 * with extended characters. 
 	 * @param sourceDir the given directory to be compressed
 	 * @param zipExtension an extension for the output ZIP file (default is .zip if a null or empty 
 	 * string is passed by the caller).
 	 * The extension is expected to contain the leading period.
 	 */
 	public static void zipDirectory (String sourceDir,
 		String zipExtension)
 	{
 		ZipOutputStream os = null;
 		String zipPath = null;
 
 		// Set the zip full path
 		if ( Util.isEmpty(zipExtension) ) {
 	    	zipExtension = ".zip";
 	    }
 	    if ( sourceDir.endsWith(File.separator) || sourceDir.endsWith("/") ) {
 	    	zipPath = sourceDir.substring(0, sourceDir.length()-1) + zipExtension;
 	    }
 	    else {
 	    	zipPath = sourceDir + zipExtension;
 	    }
 
 	    // Compress the directory
 	    try { 
 			File dir = new File(sourceDir); 
 			if( !dir.isDirectory() ) {
 				return; 
 			}
 			os = new ZipOutputStream(new FileOutputStream(zipPath)); 
             addDirectoryToZip(dir, os, null); 
         }
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error while zipping.", e);
 		} 
 		finally {
 			if ( os != null ) {
 				try {
 					os.close();
 				}
 				catch ( IOException e ) {
 					throw new OkapiIOException("Error closing stream while zipping.", e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Adds a directory to a ZIP output.
 	 * @param dir the directory to add.
 	 * @param os the output stream where to add it.
 	 * @param subDir the sub-directory.
 	 * @throws IOException signals an I/O error.
 	 */
 	private static void addDirectoryToZip (File dir,
 		ZipOutputStream os,
 		String subDir)
 		throws IOException
 	{
         FileInputStream input = null;
         try {
         	byte[] aBuf = new byte[1024]; 
         	for ( File file : dir.listFiles() ) {
         		// Go recursively if the entry is a sub-directory
         		if( file.isDirectory() ) {
         			addDirectoryToZip(file, os,
         				((subDir==null)? "" : subDir + "\\") + file.getName());
         			continue;
         		}
         		// Or add the file to the zip
         		input = new FileInputStream(file.getPath()); 
         		os.putNextEntry(new ZipEntry(
         			((subDir==null)? "" : subDir + "\\") + file.getName())); 
 
         		int nCount; 
         		while( (nCount = input.read(aBuf)) > 0 ) { 
         			os.write(aBuf, 0, nCount); 
         		}
         		os.closeEntry();
         	}
         }
         finally {
         	if ( input != null ) {
         		input.close(); 
         	}
         }
 	}
 
 	  /**
 	   * Extract a given ZIP file to a given destination folder.
	   * From http://www.java2s.com/Code/Java/File-Input-Output/Extractzipfiletodestinationfolder.htm
 	   * @param zipFileName full path of the given ZIP file
 	   * @param destPath destination folder
 	   */
 	  public static void unzip (String zipFileName,
 		  String destPath)
 	  {
 		  ZipInputStream in = null;
 		  OutputStream out = null;
 		  try {
 			  // Open the ZIP file
 			  in = new ZipInputStream(new FileInputStream(zipFileName));
 			  byte[] buf = new byte[1024];
 			  ZipEntry entry = null;
 
 			  // Process the entries
 			  while (( entry = in.getNextEntry() ) != null ) {
 				  String outFilename = entry.getName();
 				  if ( entry.isDirectory() ) {
 					  new File(destPath, outFilename).mkdirs();
 				  }
 				  else {
 					  out = new FileOutputStream(new File(destPath, outFilename));
 					  // Transfer bytes from the ZIP file to the output file
 					  int len;
 					  while (( len = in.read(buf) ) > 0 ) {
 						  out.write(buf, 0, len);
 					  }
 					  // Close the stream
 					  out.close();
 				  }
 			  }
 		  }
 		  catch ( IOException e ) {
 			  throw new OkapiIOException("Error unzipping file.", e);
 		  }
 		  finally {
 			  // Close the stream
 			  if ( in != null ) {
 				  try {
 					  in.close();
 				  }
 				  catch ( IOException e ) {
 					  throw new OkapiIOException("Error closing input while unzipping file.", e);
 				  }
 			  }
 			  if ( out != null ) {
 				  try {
 					  out.close();
 				  }
 				  catch ( IOException e ) {
 					  throw new OkapiIOException("Error closing output while unzipping file.", e);
 				  }
 			  }
 		  } 
 	  }
 
 }
