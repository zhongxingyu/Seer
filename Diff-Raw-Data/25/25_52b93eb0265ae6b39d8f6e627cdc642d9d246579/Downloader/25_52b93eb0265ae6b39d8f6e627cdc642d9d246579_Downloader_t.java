 package eu.trentorise.opendata.ckanalyze.downloader;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * This class provides an easy way to download files in single thread with the
  * resume functionality. It could be used as a standard class (creating an
  * instance) or using the singleton pattern (using the getInstance method).
  * 
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  * 
  */
 
 public class Downloader {
 	private String url;
 	private URLConnection connection = null;
 	private BufferedInputStream in = null;
 	private FileOutputStream fos = null;
 	private String filepath;
 	private String filename;
 	private long size;
 	private static final int MAX_BYTE_SIZE = 1024;
 	private static Downloader instance = null;
 
 	public String getFilename() {
 		return filename;
 	}
 
 	public long getSize() {
 		return size;
 	}
 
 	/**
 	 * 
 	 * @param url
 	 *            - The URL to retrieve
 	 * @param filepath
 	 *            - The path of the file (requires "/" char at the end)
 	 */
 	public Downloader(String url, String filepath) {
 		this.filepath = filepath;
 		this.url = url;
 		if (instance == null) {
 			instance = this;
 		}
 	}
 
 	private Downloader() {
 		super();
 	}
 
 	/**
 	 * You need to specify Url and filepath using setters
 	 */
 	public static Downloader getInstance() {
 		if (instance == null) {
 			instance = new Downloader();
 		}
 		return instance;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getFilepath() {
 		return filepath;
 	}
 
 	public void setFilepath(String filepath) {
 		this.filepath = filepath;
 	}
 
 	/**
 	 * Download using properties values
 	 */
 	public void download() {
 		download(url, filepath);
 	}
 
 	/**
 	 * 
 	 * @param url
 	 *            - The URL to retrieve
 	 * @param filepath
 	 *            - The path of the file (requires "/" char at the end)
 	 */
 
 	private String extractRealFilename(URL urlo)
 	{
 		String retval = null;
 		String rawFn = connection.getHeaderField("Content-Disposition");
 		if (rawFn != null && rawFn.contains("attachment")) {
 			rawFn = rawFn.replace("attachment; filename=\"", "");
 			rawFn = rawFn.replaceAll("\"", "");
 			retval = rawFn;
 		} else {
 			retval = urlo.getPath().substring(
 					urlo.getPath().lastIndexOf("/") + 1);
 		}
 		return retval;
 	}
 	
 	private long extractRemoteSize()
 	{
 		long retval = -1;
 		if ((connection.getHeaderFields().get("Content-Length") != null)
 				&& (!connection.getHeaderFields().get("Content-Length")
 						.isEmpty())) {
 			retval = Long.parseLong(connection.getHeaderFields()
 					.get("Content-Length").get(0));
 		}
 		return retval;
 	}
 	
 	private void performDownload(File fcheck,URL urlo) throws IOException
 	{
 		boolean redownload = false;
 		String destination = fcheck.getAbsolutePath();
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		try {
 			in = new BufferedInputStream(connection.getInputStream());
 		} catch (IOException e) {
 			connection = urlo.openConnection();
 			in = new BufferedInputStream(connection.getInputStream());
 			redownload = true;
 		}
 
 		if (fcheck.exists()) {
 			if ((!redownload)
 					|| (connection.getHeaderField("Accept-Ranges") != null && connection
 							.getHeaderField("Accept-Ranges").equals(
 									"bytes"))) {
 				fos = new java.io.FileOutputStream(destination, true);
 			} else {
 				fos = new java.io.FileOutputStream(destination);
 			}
 		} else {
 			fcheck.createNewFile();
 			fos = new java.io.FileOutputStream(destination);
 		}
 
 		byte data[] = new byte[MAX_BYTE_SIZE];
 		int count;
 		while ((count = in.read(data, 0, MAX_BYTE_SIZE)) != -1) {
 			fos.write(data, 0, count);
 		}
 	}
 	
 	private void closeConnections()
 	{
 		try {
 			if (fos != null) {
 				fos.close();
 				fos = null;
 			}
 			if (in != null) {
 				in.close();
 				fos = null;
 			}
 		} catch (IOException e) {	}
 	}
 	
 	public void download(String url, String filepath) {
 		
 		try {
 			URL urlo = new URL(url);
 			connection = urlo.openConnection();
 			// Get real filename in dynamic urls
 			filename = extractRealFilename(urlo);
 			String destination = filepath + filename;
 			boolean skip = false;
 			File fcheck = new File(destination);
 			long remoteSize = extractRemoteSize();
 			long localSize = fcheck.length();
 			if (fcheck.exists() && remoteSize > 0) {
 				skip = remoteSize == localSize;
 			}
 			connection = urlo.openConnection();
 			if (fcheck.exists()) {
 				connection.setRequestProperty("Range",
 						"Bytes=" + (fcheck.length()) + "-");
 			}
 			if (!skip) {
 				performDownload(fcheck, urlo);
 			}
 			this.size = new File(destination).length();
 
 		} catch (Exception e) {
 		} finally {
 			closeConnections();
 		}
 	}
 }
