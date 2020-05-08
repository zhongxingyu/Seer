 package gov.cida.sedmap.io;
 
 import gov.cida.sedmap.io.util.StrUtils;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.log4j.Logger;
 
 public class IoUtils {
 
 	private static final Logger logger = Logger.getLogger(IoUtils.class);
 
 	public static final String LINE_SEPARATOR  = System.getProperty("line.separator");
 
 	public static void quiteClose(Object ... open) {
 
 		if (open == null) return;
 
 		for (Object o : open) {
 			if (o == null) continue;
 
 			try {
 				logger.debug("Closing resource. " + o.getClass().getName());
 				if        (o instanceof Connection) {
 					if ( !((Connection)o).isClosed() ) {
 						((Connection)o).close();
 					}
 				} else if (o instanceof Statement) {
 					if (  !( (Statement)o).isClosed() ) {
 						( (Statement)o).close();
 					}
 				} else if (o instanceof ResultSet) {
 					if (  !( (ResultSet)o).isClosed() ) {
 						( (ResultSet)o).close();
 					}
 				} else if (o instanceof Closeable) {
 					// cannot test for close so have to catch exception
 					try { ((Closeable)o).close(); } catch (Exception e) {}
 				} else {
 					throw new UnsupportedOperationException("Cannot handle closing instances of " + o.getClass().getName());
 				}
 
 			} catch (UnsupportedOperationException e) {
 				throw e;
 
 			} catch (Exception e) {
 				logger.warn("Failed to close resource. " + o.getClass().getName(), e);
 			}
 		}
 	}
 
 
 
 	public static String readTextResource(String resource) {
 		String contents = "";
 
 		try {
 			InputStream in = IoUtils.class.getResourceAsStream(resource); //, "UTF-8");
 			contents = readStream(in);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			throw new RuntimeException("Failed to open the rdb-metadata.txt file.",ex);
 		}
 		return contents;
 	}
 
 
 
 	public static String readStream(InputStream in) throws IOException {
 		StringBuilder buf = new StringBuilder();
 
 		InputStreamReader reader = new InputStreamReader(in);
 
 		char[] chars = new char[1024];
 
 		int count = 0;
 		while ( (count = reader.read(chars)) > 0 ) {
 			buf.append(chars,0,count);
 		}
 
 		return buf.toString();
 	}
 
 
 
 	public static WriterWithFile createTmpZipWriter(String fileName, String extention) throws IOException {
 		File   file = File.createTempFile(fileName + StrUtils.uniqueName(12), ".zip");
 
 		FileOutputStream out   = new FileOutputStream(file);
 		ZipOutputStream zip    = new ZipOutputStream(out);
 		ZipEntry entry         = new ZipEntry(fileName + extention);
 		OutputStreamWriter osw = new OutputStreamWriter(zip);
 		WriterWithFile tmp     = new WriterWithFile(osw, file);
 
 		zip.putNextEntry(entry);
 
 		return tmp;
 	}
 
 
 
 	public static FileInputStreamWithFile createTmpZipStream(File file) throws IOException {
 		FileInputStream fis = new FileInputStream(file);
 		ZipInputStream  zip = new ZipInputStream(fis);
 		FileInputStreamWithFile fisf = new FileInputStreamWithFile(zip, file);
 
 		zip.getNextEntry();
 
 		return fisf;
 	}
 
 }
