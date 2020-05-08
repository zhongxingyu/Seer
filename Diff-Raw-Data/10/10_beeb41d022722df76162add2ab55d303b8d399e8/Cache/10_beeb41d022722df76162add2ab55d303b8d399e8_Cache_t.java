 package cz.cuni.mff.css_parser.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
 import org.jsoup.parser.Parser;
 import org.slf4j.Logger;
 /**
  *  Document cache. It stores downloaded files to hard drive.
  * 
  *  @author Jakub Starka
  */
 public class Cache {
 
 	private static int downloaded = 0;
 
 	public static String basePath;
 
 	public static Logger logger;
 
 	public static int getInterval() {
 		return interval;
 	}
 	public static void setInterval(int interval) {
 		Cache.interval = interval;
 	}
 
 	public static void setTimeout(int timeout) {
 		Cache.timeout = timeout;
 	}
 	public static void setBaseDir(String basedir)
 	{
 		basePath = basedir;
 	}
 
 	private static int interval;
 	private static int timeout;
 
 	private static long lastDownload = 0;
 
 	public static boolean isCached(URL url) throws IOException, InterruptedException {   
 		String host = url.getHost();
 		if (url.getPath().lastIndexOf("/") == -1) {
 			return false;
 		}
 
 		String path;
 		String file;
 		if (url.getPath().lastIndexOf("/") == 0)
 		{
 			path = url.getPath().substring(1).replace("?", "_");
 			file = url.getFile().substring(1).replace("/", "@").replace("?", "@");
 			if (file.isEmpty()) return false;
 		}
 		else
 		{
 			//System.out.println();
 			//System.out.println(url);
 			//System.out.println(url.getPath());
 			path = url.getPath().substring(1, url.getPath().lastIndexOf("/")).replace("?", "_");
 			//System.out.println(path);
 			file = url.getFile().substring(path.length() + 2).replace("/", "@").replace("?", "@");
 			//System.out.println(url.getFile());
 			//System.out.println(file);
 			//System.out.println();
 			if (file.isEmpty()) return false;
 		}
 
 		//File hHost = new File(Cache.basePath, host);
 		File hPath = new File(Cache.basePath, host + File.separatorChar + path);
 		File hFile = new File(hPath, file);
 
 		return (hFile.exists() && (hFile.length() > 0));
 	}
 
 	public static Document getDocument(URL url, int maxAttempts, String datatype) throws IOException, InterruptedException {   
 		String host = url.getHost();
 		if (url.getPath().lastIndexOf("/") == -1) {
 			return null;
 		}
 
 		String path;
 		String file;
 		if (url.getPath().lastIndexOf("/") == 0)
 		{
 			path = url.getPath().substring(1).replace("?", "_");
 			file = url.getFile().substring(1).replace("/", "@").replace("?", "@");
 			if (file.isEmpty()) return null;
 		}
 		else
 		{
 			//System.out.println();
 			//System.out.println(url);
 			//System.out.println(url.getPath());
 			path = url.getPath().substring(1, url.getPath().lastIndexOf("/")).replace("?", "_");
 			//System.out.println(path);
 			file = url.getFile().substring(path.length() + 2).replace("/", "@").replace("?", "@");
 			//System.out.println(url.getFile());
 			//System.out.println(file);
 			//System.out.println();
 			if (file.isEmpty()) return null;
 		}
 
 		//File hHost = new File(Cache.basePath, host);
 		File hPath = new File(Cache.basePath, host + File.separatorChar + path);
 		File hFile = new File(hPath, file);
 
 		Document out = null;
 
 		if (!hFile.exists()) {
 			//if (!s.contains(file)) {
 			hPath.mkdirs();
 			int attempt = 0;
 			while (attempt < maxAttempts) {
 				java.util.Date date= new java.util.Date();
 				long curTS = date.getTime();
 				logger.debug("Downloading URL (attempt " + attempt + "): " + url.getHost() + url.getFile());
 				if (lastDownload + interval > curTS ) {
 					/*                    System.out.println("LastDownload: " + lastDownload);
                     System.out.println("CurTS: " + curTS);
                     System.out.println("Interval: " + interval);*/
 					logger.debug("Sleeping: " + (lastDownload + interval - curTS));
 					Thread.sleep(lastDownload + interval - curTS);
 				}
 				try {
 
 					if (datatype.equals("xml"))
 					{
 						out = Jsoup.connect(url.toString()).parser(Parser.xmlParser()).timeout(timeout).get();
 					}
 					else out = Jsoup.parse(url, timeout);
					out.outputSettings().escapeMode(EscapeMode.xhtml);
 
 					java.util.Date date2= new java.util.Date();
 					lastDownload = date2.getTime();
 					logger.debug("Downloaded URL (attempt " + attempt + ") in " + (lastDownload - curTS) + " ms : " + url.getHost() + url.getFile());
 					break;
 				} catch (SocketTimeoutException ex) {
 					java.util.Date date3= new java.util.Date();
 					long failed = date3.getTime();
 					logger.debug("Timeout (attempt " + attempt + ") in " + (failed - curTS)+ " : " + url.getHost() + url.getFile());
 
 					//Comment to retry when timeout
 					//if (!url.getHost().equals("www.vestnikverejnychzakazek.cz"))
 					//{
 					//    BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
 					//   fw.close();
 					//    return null;
 					//}
 
 					//END comment
 
 					//Thread.sleep(interval);
 				} catch (java.io.IOException ex) {
 					logger.warn("Warning (retrying): " + ex.getMessage());
 					if (
 							ex.getMessage() == null 
 							|| ex.getMessage().equals("HTTP error fetching URL")
 							|| ex.getMessage().equals("Connection reset")
 							|| ex.getMessage().startsWith("Too many redirects occurred trying to load URL")
 							|| ex.getMessage().startsWith("Unhandled content type.")
 							|| ex.getMessage().startsWith("handshake alert:")
 							|| ex.getMessage().equals(url.getHost())
 							)
 					{
 						//This makes sure that next run will see the errorneous page as cached. Does not have to be always desirable
 						//BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
 						//fw.close();
 						//return null;
 					}
 					Thread.sleep(interval);
 				}
 				attempt ++;
 			}
 			if (attempt == maxAttempts) {
 				logger.error("ERROR: " + url.getHost() + url.getPath());
 				/*throw new SocketTimeoutException();*/
 				return null;
 			}
 			try 
 			{
 				BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hFile), "UTF-8"));
 				fw.append(out.outerHtml());
 				fw.close();
 				++downloaded;
 			}
 			catch (Exception e)
 			{
 				if (e.getClass() == InterruptedException.class)
 				{
 					throw e;
 				}
 				else logger.error("ERROR caching");
 			}
 		} else {
 			//System.out.println("Using cache for URL: " + url.getHost() + url.getFile());
 
 			if (datatype.equals("xml"))
 			{
 				out = Jsoup.parse(new FileInputStream(hFile), "UTF-8", host, Parser.xmlParser());
 
 			}
			else
			{
				out = Jsoup.parse(hFile, "UTF-8", host);
			}
			out.outputSettings().escapeMode(EscapeMode.xhtml);
 
 		}
 		return out;
 	}
 
 }
