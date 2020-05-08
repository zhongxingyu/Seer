 /*******************************************************************************
  * Copyright (c) 2011 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package podsalinan;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.text.DecimalFormat;
 
 import javax.swing.table.DefaultTableModel;
 
 /**
  * @author bugman
  *
  */
 public class Downloader extends NotifyingRunnable{
 	private URL fileDownload;
 	private String destination;
 	private long fileSize;
 	private int percentage=0,
 				listNum;
 	private Object syncObject;
 	private DownloadList downloadTable;
 	private DefaultTableModel downloadGui;
 	private boolean podcast;
 	
 	public String getFilenameDownload(){
 		return fileDownload.toString();
 	}
 	
 	public boolean isPodcast(){
 		return podcast;
 	}
 	
 	public Downloader(URL urlDownload, String outputFile, String size, Object syncObject, DownloadList downloadTable, DefaultTableModel queueGui, int listNum, boolean podcast){
 		fileDownload = urlDownload;
 		destination = outputFile;
 		fileSize = Long.valueOf(size);
 		this.downloadTable = downloadTable;
 		this.syncObject = syncObject;
 		this.podcast=podcast;
 		this.listNum=listNum;
 		downloadGui=queueGui;
 		
 		downloadGui.addRow(new Object[]{fileDownload.toString(),"",""});
 	}
 	
 	public Downloader(URL urlDownload, String outputFile, Object syncObject, DownloadList downloadTable, DefaultTableModel queueGui) {
 		this(urlDownload,outputFile);
 		this.syncObject = syncObject;
 		this.downloadTable=downloadTable;
 		downloadGui=queueGui;
 
 		downloadGui.addRow(new Object[]{fileDownload.toString(),"",""});
 	}
 	
 	public Downloader(URL urlDownload, String outputFile) {
 		fileDownload = urlDownload;
 		destination = outputFile;
 		
 		try {
 			URLConnection conn = fileDownload.openConnection();
 			fileSize = conn.getContentLength();
 		} catch (MalformedURLException e) {
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}	
 
 	/** code found at: 
 	 * http://stackoverflow.com/questions/1139547/detect-internet-connection-using-java
 	 * This test will cover if a download can occur or not.
 	 * @return if its connected to the internet
 	 */
 	public static boolean isInternetReachable()
     {
             try {
             	//System.out.println("Checking the internet");
                     //make a URL to a known source
                     URL url = new URL("http://www.google.com");
 
                     //open a connection to that source
                     HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
 
                     //trying to retrieve data from the source. If there
                     //is no connection, this line will fail
                     @SuppressWarnings("unused")
 					Object objData = urlConnect.getContent();
 
             } catch (UnknownHostException e) {
             		//System.out.println("No it isn't");
                     return false;
             }
             catch (IOException e) {
         			//System.out.println("No it isn't");
                     return false;
             }
             //System.out.println("Yes it is");
             return true;
     }
 	
 	/** This is where the file gets downloaded from
 	 * 
 	 */
 	public int getFile(){
 		RandomAccessFile outStream;
 		boolean remoteFileExists=false;
 		int numTries=0;
 		URLConnection conn;
 		
 			//System.out.println("Is internet Reachable?");
 		if (isInternetReachable()){
 			byte buf[]=new byte[1024];
 			int byteRead;	// Number of bytes read from file being downloaded
 			long saved=0;	// Number of bytes saved
 			String totalSizeModifier;
 
 			// The following if statement checks if the file exists, and then get's the size
 			File outputFile = new File(destination);
 			if (outputFile.exists()){
 				saved=outputFile.length();
 			}
 				
 			
 			while ((!remoteFileExists)&&(numTries<2)){
 				try {
 					conn = fileDownload.openConnection();
					/* The following line gets the file size of the Download. had to do it this 
					 * way cos URLConnection.getContentLength was an int and couldn't handle over
					 * 2GB
					 */
					String length=conn.getHeaderField("Content-Length");
					long tempfileSize=-1;
					if (length!=null)
						tempfileSize = Long.parseLong(length);
 					if (fileSize!=tempfileSize)
 						fileSize=tempfileSize;
 					remoteFileExists=true;
 				} catch (MalformedURLException e) {
 					remoteFileExists=false;
 				} catch (IOException e) {
 					e.printStackTrace();
 					remoteFileExists=false;
 				}
 
				/* Just incase the link (whether it was manually added or via a podcast)
				 * is incorrect, we will try to switch it from http to ftp and vice versa.
				 */
 				if (!remoteFileExists){
 					String protocol;
 					if (fileDownload.toString().substring(0, 3).equals("http")){
 						protocol = "ftp";
 					} else
 						protocol = "http";
 					try {
 						fileDownload = new URL(protocol,fileDownload.getHost(),fileDownload.getPort(),fileDownload.getFile());
 					} catch (MalformedURLException e) {
 						remoteFileExists=false;
 					}
 				}
 				numTries++;
 			}
 			
 			if (remoteFileExists){
 				double fileSizeModified = 0;
 				totalSizeModifier="";
 				if (fileSize>1073741824){
 					totalSizeModifier=" Gb";
 					fileSizeModified=fileSize/1073741824;
 				} else if (fileSize>1048576){
 					totalSizeModifier=" Mb";
 					fileSizeModified=fileSize/1048576;					
 				} else if (fileSize>1024){
 					totalSizeModifier=" Kb";
 					fileSizeModified=fileSize/1024;
 				}
 					
 				try {
 					//System.out.println(fileDownload.toString()+" - "+fileSize);
 					//System.out.println(saved);
 					//System.out.println(outputFile);
 					if ((saved<fileSize)||(fileSize==-1)){
 						outStream = new RandomAccessFile(outputFile,"rw");
 						outStream.seek(saved);
 						
 						conn = fileDownload.openConnection();
						/* Skip incoming connection ahead before we connect a stream to it,
						 * otherwise it'll waste user bandwidth						
						 */
 						conn.setRequestProperty("Range", "bytes="+ saved + "-");
 						conn.connect();
 						InputStream inStream = conn.getInputStream();
 						
 						
 						long time=System.currentTimeMillis();
 						int chunkCount=0;
 						//System.out.println("before while");
 						while ((byteRead = inStream.read(buf)) > 0){
 							//System.out.println("byteRead: "+byteRead);
 							//System.out.println("Downloading");
 							outStream.write(buf, 0, byteRead);
 							saved+=byteRead;
 							chunkCount++;
 							//System.out.println("Saved: "+saved);
 							if (downloadTable!=null)	
 								if (fileSize>0){
 									double temppercent=((double)saved/(double)fileSize);
 									percentage=(int)((temppercent)*100);
 									downloadTable.downloadProgress(listNum, percentage);
 									
 									String savedModifier="";
 									double savedModified=0;
 									// Download window output
 									if (saved>1073741824){
 										savedModifier=" Gb";
 										savedModified = (double)saved/1073741824;
 									} else if (saved>1048576){
 										savedModifier=" Mb";
 										savedModified = (double)saved/1048576;
 									} else if (saved>1024){
 										savedModifier=" Kb";
 										savedModified = (double)saved/1024;
 									} else
 										savedModified=saved;
 
 									savedModified=new Double(new DecimalFormat("#.##").format(savedModified)).doubleValue();
 
 									String outputString = savedModified + savedModifier + " of " + fileSizeModified + totalSizeModifier;
 									downloadGui.setValueAt(outputString, getRow(fileDownload.toString()), 1);
 									if (System.currentTimeMillis()-time>999){
 										downloadGui.setValueAt(chunkCount+"Kbps", getRow(fileDownload.toString()), 2);
 										time=System.currentTimeMillis();
 										chunkCount=0;
 									}
 								} else {
 									String savedModifier="";
 									double savedModified=0;
 									// Download window output
 									if (saved>1073741824){
 										savedModifier=" Gb";
 										savedModified = (double)saved/1073741824;
 									} else if (saved>1048576){
 										savedModifier=" Mb";
 										savedModified = (double)saved/1048576;
 									} else if (saved>1024){
 										savedModifier=" Kb";
 										savedModified = (double)saved/1024;
 									} else
 										savedModified=saved;
 
 									savedModified=new Double(new DecimalFormat("#.##").format(savedModified)).doubleValue();
 									
 									String outputString = savedModified + savedModifier;
 									downloadGui.setValueAt(outputString, getRow(fileDownload.toString()), 1);
 									if (System.currentTimeMillis()-time>999){
 										downloadGui.setValueAt(chunkCount+"Kbps", getRow(fileDownload.toString()), 2);
 										time=System.currentTimeMillis();
 										chunkCount=0;
 									}
 								}
 						}
 						//System.out.println("File Finished");
 						inStream.close();
 						outStream.close();					
 					} else if (saved==fileSize){
 						percentage=100;
 						downloadTable.downloadProgress(listNum, 100);
 					}
 				} catch (UnknownHostException e){
 					return 1;
 				} catch (IOException e) {
 					e.printStackTrace();
 					return 1;
 				}				
 			}
 		} else {
 			// No internet connection.
 			return 1;
 		}
 		return 0;
 	}
 	
 	public int downloadCompleted(){
 		return percentage;
 	}
 	
 	@Override
 	public void doRun() {
 		//System.out.println("Before get file");
 		getFile();
 		//System.out.println("After get file");
 		synchronized (syncObject){
 			syncObject.notify();
 		}		
 	}
 	
 	public int getRow(){
 		return getRow(fileDownload.toString());
 	}
 	
 	public int getRow(String url){
 		int rowNum=0;
 		boolean found=false;
 		
 		//System.out.println("getRow: "+url);
 		while((rowNum<downloadGui.getRowCount())&&(!found)){
 			String tableURL = (String)downloadGui.getValueAt(rowNum, 0);
 			//System.out.println("getRow: tableURL="+tableURL);
 			if (tableURL.equals(url))
 				found=true;
 			else
 				rowNum++;
 		}
 		//System.out.println(rowNum);
 		return rowNum;
 	}
 }
