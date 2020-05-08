 package com.cotescu.radu.commons;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * This class provides various methods useful for downloading files.
  * @author Radu Cotescu
  *
  */
 public class Downloader {
 	private final static String UA = "Mozilla/5.0 (X11; Linux x86_64; rv:5.0) Gecko/20100101 Firefox/5.0";
 
 	/**
 	 * This method downloads a file from a specified URL. If it is not specified
 	 * <i>where</i> to download the file it is assumed that the desired folder
 	 * is the current directory.
 	 * 
 	 * @param from
 	 *            a String containing the URL from where the file must be
 	 *            downloaded
 	 * @param where
 	 *            a String containing the folder where the folder must be
 	 *            downloaded; if it is null or the empty String ("") the file
 	 *            will be downloaded in the current directory
 	 * @return	a String containing the full path to the downloaded file
 	 */
 	public static String downloadFileFromURL(String from, String where) {
 		String file = null;
 		try {
 			String to = new File("." + File.separator).getCanonicalPath();
 			if (StringUtils.isEmpty(where)) {
 				where = to;
 			}
 			URL url = new URL(from);
 			URLConnection connection = url.openConnection();
 			/**
 			 * some servers check the user agent and if they find something
 			 * suspicious they return HTTP 403 error code
 			 */
 			connection.setRequestProperty("User-Agent", UA);
 			InputStream is = connection.getInputStream();
 			ReadableByteChannel rbc = Channels.newChannel(is);
			String tmpfile = url.getFile().replace("/", "");
			if (StringUtils.isEmpty(tmpfile)) {
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
 				tmpfile = sdf.format(new Date());
 			}
 			if (where.endsWith(File.separator)) {
 				tmpfile = where + tmpfile;
 			} else {
 				tmpfile = where + File.separator + tmpfile;
 			}
 			FileOutputStream fos = new FileOutputStream(tmpfile);
 			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 			file = tmpfile;
 		} catch (MalformedURLException e) {
 			System.err.println("Malformed URL...");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("Unable to open URL stream...");
 			e.printStackTrace();
 		}
 		return file;
 	}
 }
