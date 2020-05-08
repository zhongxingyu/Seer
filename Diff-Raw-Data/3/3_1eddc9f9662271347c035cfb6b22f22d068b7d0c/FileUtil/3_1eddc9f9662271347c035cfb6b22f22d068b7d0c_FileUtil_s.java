 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.net.URL;
 import java.util.UUID;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import net.sf.json.JSONObject;
 import org.apache.log4j.Logger;
 
 import com.gooddata.util.CSVReader;
 import com.gooddata.util.CSVWriter;
 
 /**
  * File utils
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class FileUtil {
 
     private static Logger l = Logger.getLogger(FileUtil.class);
 
     private static final int BUF_SIZE = 2048;
 
     /**
      * Compresses local directory to the archiveName
      *
      * @param dirPath     path to the directory
      * @param archiveName the name of the ZIP archive that is going to be created
      * @throws IOException
      */
     public static void compressDir(String dirPath, String archiveName) throws IOException {
         l.debug("Compressing "+dirPath + " -> "+archiveName);
         File d = new File(dirPath);
         if (d.isDirectory()) {
             File[] files = d.listFiles();
             byte data[] = new byte[BUF_SIZE];
             ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archiveName)));
             for (File file : files) {
                 BufferedInputStream fi = new BufferedInputStream(new FileInputStream(file), BUF_SIZE);
                 ZipEntry entry = new ZipEntry(file.getName());
                 out.putNextEntry(entry);
                 int count;
                 while ((count = fi.read(data, 0, BUF_SIZE)) != -1) {
                     out.write(data, 0, count);
                 }
                 fi.close();
             }
             out.close();
             File file = new File(archiveName);
         } else
             throw new IOException("The referenced directory isn't directory!");
         l.debug("Compressed "+dirPath + " -> "+archiveName);
 
     }
 
     /**
      * writes the data from the input stream to the provided output stream
      * @param is
      * @param os
      * @throws IOException
      */
     public static void copy(InputStream is, OutputStream os) throws IOException {
     	if (is == null || os == null) {
     		throw new IllegalArgumentException("both input and output streams must be non-null");
     	}
     	try {
             byte[] buf = new byte[1024];
             int i = 0;
             while ((i = is.read(buf)) != -1) {
                 os.write(buf, 0, i);
             }
         } finally {
             is.close();
             os.close();
         }
     }
     
     /**
      * Create a new temporary directory. Use something like
      * {@link #recursiveDelete(File)} to clean this directory up since it isn't
      * deleted automatically
      *
      * @return the new directory
      * @throws IOException if there is an error creating the temporary directory
      */
     public static File createTempDir() throws IOException {
         l.debug("Creating a new tmp directory.");
         final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
         File newTempDir;
         final int maxAttempts = 9;
         int attemptCount = 0;
         do {
             attemptCount++;
             if (attemptCount > maxAttempts) {
                 throw new IOException(
                         "The highly improbable has occurred! Failed to " +
                                 "create a unique temporary directory after " +
                                 maxAttempts + " attempts.");
             }
             String dirName = UUID.randomUUID().toString();
             newTempDir = new File(sysTempDir, dirName);
         } while (newTempDir.exists());
 
         if (newTempDir.mkdirs()) {
             l.debug("Created new tmp directory="+newTempDir.getAbsolutePath());
             return newTempDir;
         } else {
             throw new IOException(
                     "Failed to create temp dir named " +
                             newTempDir.getAbsolutePath());
         }
     }
 
     /**
      * Create a new temporary file. Use something like
      *
      * @return the new file
      * @throws IOException if there is an error creating the temporary file
      */
     public static File getTempFile() throws IOException {
         l.debug("Creating a new tmp file.");
         final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
         File newTempFile;
         final int maxAttempts = 9;
         int attemptCount = 0;
         do {
             attemptCount++;
             if (attemptCount > maxAttempts) {
                 throw new IOException(
                         "The highly improbable has occurred! Failed to " +
                                 "create a unique temporary directory after " +
                                 maxAttempts + " attempts.");
             }
             String fileName = UUID.randomUUID().toString() + ".csv";
             newTempFile = new File(sysTempDir, fileName);
         } while (newTempFile.exists());
         l.debug("Created new tmp file="+newTempFile.getAbsolutePath());
         return newTempFile;
     }
 
     /**
      * Recursively delete file or directory
      *
      * @param fileOrDir the file or dir to delete
      * @return true if all files are successfully deleted
      */
     public static boolean recursiveDelete(File fileOrDir) {
         l.debug("Deleting "+fileOrDir+" recursively.");
         if (fileOrDir.isDirectory()) {
             // recursively delete contents
             for (File innerFile : fileOrDir.listFiles()) {
                 if (!recursiveDelete(innerFile)) {
                     return false;
                 }
             }
         }
         l.debug("Deleted"+fileOrDir+" recursively.");
         return fileOrDir.delete();
     }
 
     /**
      * Writes a string to a file.
      *
      * @param content  the content
      * @param fileName the file
      * @throws IOException
      */
     public static void writeStringToFile(String content, String fileName) throws IOException {
         BufferedWriter fw = createBufferedUtf8Writer(fileName);
         fw.write(content);
         fw.flush();
         fw.close();
     }
 
     /**
      * Reads the entire file and returns its content as a single {@link String} 
      *
      * @param fileName the file
      * @return the file content as String
      * @throws IOException
      */
     public static String readStringFromFile(String fileName) throws IOException {
         return readStringFromBufferedReader(createBufferedUtf8Reader(fileName));
     }
 
     /**
      * Writes a JSON object to a file.
      *
      * @param content  the content
      * @param fileName the file
      * @throws IOException
      */
     public static void writeJSONToFile(JSONObject content, String fileName) throws IOException {
         BufferedWriter fw = createBufferedUtf8Writer(fileName);
         fw.write(content.toString(2));
         fw.flush();
         fw.close();
     }
 
     /**
      * Reads a JSON object from a file
      *
      * @param fileName the file
      * @return the file content as JSON object
      * @throws IOException
      */
     public static JSONObject readJSONFromFile(String fileName) throws IOException {
         return JSONObject.fromObject(readStringFromBufferedReader(createBufferedUtf8Reader(fileName)));
     }
 
     /**
      * Reads the entire {@link InputStream} and returns its content as a single {@link String} 
      *
      * @param is the file
      * @return the file content as String
      * @throws IOException
      */
     public static String readStringFromStream(InputStream is) throws IOException {
     	return readStringFromBufferedReader(createBufferedUtf8Reader(is));
     }
     
     /**
      * Reads all content from the given {@link Reader} and returns it as a single {@link String} 
      *
      * @param br the file
      * @return the file content as String
      * @throws IOException
      */
     private static String readStringFromBufferedReader(BufferedReader br) throws IOException {
         StringBuffer sbr = new StringBuffer();
         for(String ln = br.readLine(); ln != null; ln = br.readLine())
             sbr.append(ln+"\n");
         br.close();
         return sbr.toString();
     }
     
     /**
      * Retrieves CSV headers from an URL
      * 
      * @param url CSV url
      * @return the headers as String[]
      * @throws IOException in case of IO issues
      */
     public static String[] getCsvHeader(URL url, char separator) throws IOException {
         CSVReader csvIn = new CSVReader(createBufferedUtf8Reader(url), separator);        
         return csvIn.readNext();
     }
 
     /**
      * Constructs a new File and optionally checks if it exists 
      * @param fileName file name
      * @param ignoreMissingFile flag that ignores the fact that the file doesn't exists
      * @return the File
      * @throws IOException if the file doesn't exists and the ignoreMissingFile is false
      */
     public static File getFile(String fileName, boolean ignoreMissingFile) throws IOException {
         File f = new File(fileName);
         if(!f.exists()) {
         	if (!ignoreMissingFile)
         		throw new IOException("File '" + fileName + "' doesn't exist.");
         	else
         		return null;
         }
         return f;
     }
 
     /**
      * Constructs a new File and checks if it exists
      * @param fileName file name
      * @return the File
      * @throws IOException if the file doesn't exists
      */
     public static File getFile(String fileName) throws IOException {
         return getFile(fileName, false);
     }
 	
 	/**
 	 * returns the last element of the URL's path
 	 * @param url to parse
 	 * @return the last element of the URL's path
 	 */
 	public static String getFileName(URL url) {
 		String[] pathElements = url.getPath().split("/");
 		return pathElements[pathElements.length - 1];
 	}
 	
 	/**
 	 * Opens a file given by a path and returns its {@link BufferedReader} using the
 	 * UTF-8 encoding
 	 * 
 	 * @param path path to a file to be read
 	 * @return UTF8 BufferedReader of the file <tt>path</tt>
 	 * @throws IOException
 	 */
 	public static BufferedReader createBufferedUtf8Reader(String path) throws IOException {
 		return createBufferedUtf8Reader(new File(path));
 	}
 	
 	/**
 	 * Opens a file given by a path and returns its {@link BufferedWriter} using the
 	 * UTF-8 encoding
 	 * 
 	 * @param path path to a file to write to
 	 * @return UTF8 BufferedWriter of the file <tt>path</tt>
 	 * @throws IOException
 	 */
 	public static BufferedWriter createBufferedUtf8Writer(String path) throws IOException {
 		return createBufferedUtf8Writer(new File(path));
 	}
 	
 	/**
 	 * Opens a file given by a path and returns its {@link BufferedReader} using the
 	 * UTF-8 encoding
 	 * 
 	 * @param file file to be read
 	 * @return UTF8 BufferedReader of the <tt>file</tt>
 	 * @throws IOException
 	 */
 	public static BufferedReader createBufferedUtf8Reader(File file) throws IOException {
 		return createBufferedUtf8Reader(new FileInputStream(file));
 	}
 	
 	/**
 	 * Opens a file given by a path and returns its {@link BufferedWriter} using the
 	 * UTF-8 encoding
 	 * 
 	 * @param file file to write to
 	 * @return UTF8 BufferedWriter of the <tt>file</tt>
 	 * @throws IOException
 	 */
 	public static BufferedWriter createBufferedUtf8Writer(File file) throws IOException {
 		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf8"));
 	}
 
 	/**
 	 * Opens a URL and returns its {@link BufferedReader} using the UTF-8 encoding
 	 * 
 	 * @param url to be read
 	 * @return UTF8 BufferedReader of the <tt>url</tt>
 	 * @throws IOException
 	 */
 	public static BufferedReader createBufferedUtf8Reader(URL url) throws IOException {
 		return createBufferedUtf8Reader(url.openStream());
 	}
 	
 	/**
 	 * Creates a {@link BufferedReader} on the top of the given {@link InputStream} using the
 	 * UTF-8 encoding
 	 * 
 	 * @param is file to be read
 	 * @return UTF8 BufferedReader of the <tt>file</tt>
 	 * @throws IOException
 	 */
 	public static BufferedReader createBufferedUtf8Reader(InputStream is) throws IOException {
 		return new BufferedReader(new InputStreamReader(is, "utf8"));
 	}
 	
 	/**
 	 * Creates a UTF-8 {@link CSVReader} of the resource on classpath represented by
 	 * given <tt>path</tt>. Calls {@link Class#getResourceAsStream(String)} internally to create
 	 * the underlying {@link InputStream}.
 	 * 
 	 * @param path
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVReader getResourceAsCsvReader(String path) throws IOException {
 		InputStream is = FileUtil.class.getResource(path).openStream();
 		return createUtf8CsvReader(is);    	
 	}
 	
 	/**
 	 * Creates a UTF-8 {@link CSVReader} of the given <tt>file</tt>.
 	 * 
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVReader createUtf8CsvReader(File file) throws IOException {
 		return createUtf8CsvReader(new FileInputStream(file));
 	}
 
     /**
 	 * Creates a UTF-8 {@link CSVReader} of the given <tt>file</tt>.
 	 *
 	 * @param file
      * @param separator field separator
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVReader createUtf8CsvReader(File file, char separator) throws IOException {
 		return createUtf8CsvReader(new FileInputStream(file), separator);
 	}
 
     /**
 	 * Creates a UTF-8 {@link CSVReader} of the given <tt>inputStream</tt>.
 	 *
 	 * @param inputStream
      * @param separator field separator
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVReader createUtf8CsvReader(InputStream inputStream, char separator) throws IOException {
 		return new CSVReader(new InputStreamReader(inputStream, "utf8"), separator);
 	}
 
 	/**
 	 * Creates a UTF-8 {@link CSVReader} of the given <tt>inputStream</tt>.
 	 * 
 	 * @param inputStream
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVReader createUtf8CsvReader(InputStream inputStream) throws IOException {
 		return new CSVReader(new InputStreamReader(inputStream, "utf8"));
 	}
 	
 	/**
 	 * Creates a UTF-8 {@link CSVWriter} of the given <tt>file</tt>.
 	 * 
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVWriter createUtf8CsvWriter(File file) throws IOException {
 		return createUtf8CsvWriter(new FileOutputStream(file));
 	}
 	
 	/**
 	 * Creates a UTF-8 {@link CSVWriter} of the given <tt>outputStream</tt>.
 	 * 
 	 * @param outputStream
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVWriter createUtf8CsvWriter(OutputStream outputStream) throws IOException {
 		return new CSVWriter(new OutputStreamWriter(outputStream, "utf8"));
 	}
 
 
     /**
 	 * Creates a UTF-8 {@link CSVWriter} of the given <tt>file</tt>.
 	 *
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
 	public static CSVWriter createUtf8CsvEscapingWriter(File file) throws IOException {
		return new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), "utf8"),',','"',
                '\\');
 	}
 
 }
