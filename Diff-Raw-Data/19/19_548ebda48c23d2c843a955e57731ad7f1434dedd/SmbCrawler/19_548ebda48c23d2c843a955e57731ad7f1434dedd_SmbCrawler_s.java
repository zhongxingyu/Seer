 package org.paxle.crawler.smb.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.Date;
 
 import jcifs.smb.SmbException;
 import jcifs.smb.SmbFile;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.doc.CrawlerDocument;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.crawler.CrawlerTools;
 import org.paxle.crawler.ISubCrawler;
 import org.paxle.crawler.smb.ISmbCrawler;
 
 public class SmbCrawler implements ISubCrawler, ISmbCrawler {
 	public static final String[] PROTOCOLS = new String[]{"smb"};
 
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * @see ISubCrawler#getProtocols()
 	 */	
 	public String[] getProtocols() {
 		return PROTOCOLS;
 	}
 
 	public ICrawlerDocument request(URI requestUri) {
 		if (requestUri == null) throw new NullPointerException("URL was null");
 		this.logger.info(String.format("Crawling URL '%s' ...", requestUri));	
 		
 		CrawlerDocument crawlerDoc = new CrawlerDocument();
 		crawlerDoc.setCrawlerDate(new Date());
 		crawlerDoc.setLocation(requestUri);
 		
 		try {
			SmbFile smbFile = new SmbFile(requestUri.toURL());
 			if (!smbFile.exists()) {
 				crawlerDoc.setStatus(ICrawlerDocument.Status.NOT_FOUND, "The resource does not exist");				
 				this.logger.info(String.format("The resource '%s' does not exit.",requestUri));			
 				return crawlerDoc;
 			} else if (!smbFile.canRead()) {
 				crawlerDoc.setStatus(ICrawlerDocument.Status.NOT_FOUND, "The resource can not be read.");				
 				this.logger.info(String.format("The resource '%s' can not be read.",requestUri));			
 				return crawlerDoc;				
 			}
 			
 			InputStream input = null;
 			if (smbFile.isDirectory()) {
 				/* Append '/' if necessary. Otherwise we will get:
 				 * jcifs.smb.SmbException: smb://srver/dir directory must end with '/'
 				 */
 				// XXX still needed with the SmbFile(URL)-constructor?
 				String uriString = requestUri.toASCIIString();
 				if (!uriString.endsWith("/")) {
 					uriString += "/";
 					smbFile = new SmbFile(uriString);
 				}
 				
 				// set the mimetype accordingly
 				crawlerDoc.setMimeType("text/html");
 				
 				// using the dir creation date as last-mod date
 				long creationTimeStamp = smbFile.createTime();
 				if (creationTimeStamp != 0) {
 					crawlerDoc.setLastModDate(new Date(creationTimeStamp));
 				}
 				
 				// generate dir listing
 				input = this.generateDirListing(smbFile);
 			} else if (smbFile.isFile()) {
 				// last modified timestamp
 				long modTimeStamp = smbFile.getLastModified();
 				if (modTimeStamp != 0) {
 					crawlerDoc.setLastModDate(new Date(modTimeStamp));
 				}
 				
 				// get file content
 				input = smbFile.getInputStream();
 			}
 			
 			// copy data into file
 			CrawlerTools.saveInto(crawlerDoc, input);
 			
 			// close connection
 			input.close();
 				
 			// finished
 			crawlerDoc.setStatus(ICrawlerDocument.Status.OK);
		} catch(IOException e) {
 			crawlerDoc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, "Unexpected Exception: " + e.getMessage());
 			
 			this.logger.warn(String.format("Unexpected '%s' while trying to crawl resource '%s'.",
 					e.getClass().getName(),
 					requestUri
 			),e);
 		} 		
 
 		return crawlerDoc;			
 	}
 	
 	private InputStream generateDirListing(SmbFile smbDir) throws SmbException {
 		StringWriter writer = new StringWriter();
 		
 		String parentDir = smbDir.getParent();
 		writer.append(String.format("<html><head><title>Index of %s</title></head><hr><table><tbody>\r\n",smbDir.getURL()));
 		if (parentDir.length() > "smb://".length()) {
 			writer.append(String.format("<tr><td colspan=\"3\"><a href=\"%s\">Up to higher level directory</a></td></tr>\r\n",parentDir));
 		}
 
 		// generate directory listing
 		SmbFile[] smbFiles = smbDir.listFiles();
 		for (SmbFile smbFile : smbFiles) {
 			if (!smbFile.exists()) continue;
 			if (!smbFile.canRead()) continue;
 			if (smbFile.isHidden()) continue;
 			
 			// FIXME: we need to escape the urls properly here.
 			writer.append(
 					String.format(
 							"<tr>" + 
 								"<td><a href=\"%1$s\">%2$s</a></td>" + 
 								"<td>%3$d Bytes</td>" +
 								"<td>%4$tY-%4$tm-%4$td %4$tT</td>" +
 							"</tr>\r\n",
 							smbFile.getURL(),
 							smbFile.getName(),
 							Long.valueOf(smbFile.isDirectory() ? 0l : smbFile.getContentLength()),
 							Long.valueOf(smbFile.getLastModified())
 					)
 			);
 		}
 		writer.append("</tbody></table><hr></body></html>");	
 		
 		try {
 			return new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			assert(false) : "Unexpected error: " + e.getMessage();
 			return null;
 		}
 	}	
 }
