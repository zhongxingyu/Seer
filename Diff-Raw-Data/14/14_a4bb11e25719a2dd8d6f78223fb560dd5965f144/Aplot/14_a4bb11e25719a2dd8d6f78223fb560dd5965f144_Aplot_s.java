 package org.iugonet.www;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.InetSocketAddress;
 import java.net.MalformedURLException;
 import java.net.Proxy;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 
 abstract public class Aplot {
 
 
 	private String rootDataDir = "~/data";
 	private String themisDataDir = "~/themis";
 	private String themisRemoteDataDir = "http://themis.stp.isas.jaxa.jp/data/themis/";
 
 	Aplot(){
 		String rootDataDir = System.getenv("ROOT_DATA_DIR");
 		if ( rootDataDir != null ){
 			setRootDataDir(rootDataDir);
 		}
 		
 		String themisDataDir = System.getenv("THEMIS_DATA_DIR");
 		if ( themisDataDir != null ){
 			setThemisDataDir(themisDataDir);
 		}
 		
 		String themisRemoteDataDir = System.getenv("THEMIS_REMOTE_DATA_DIR");
 		if ( themisRemoteDataDir != null ){
 			setThemisRemoteDataDir(themisRemoteDataDir);
 		}
 	}
 	
 	public void setRootDataDir(String rootDataDir) {
 		this.rootDataDir = rootDataDir;
 	}
 
 	public String getRootDataDir() {
 		return this.rootDataDir;
 	}
 
 	public void setThemisDataDir(String themisDataDir) {
 		this.themisDataDir = themisDataDir;
 	}
 
 	public String getThemisDataDir() {
 		return this.themisDataDir;
 	}
 
 	public void setThemisRemoteDataDir(String themisRemoteDataDir) {
 		this.themisRemoteDataDir = themisRemoteDataDir;
 	}
 
 	public String getThemisRemoteDataDir() {
 		return this.themisRemoteDataDir;
 	}
 	
	
	
 	@Deprecated
 	public void readData(String strUrl) {
 		try {
 			URL url = new URL(strUrl);
 			this.readData(url);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	abstract void readData(URL url);
 
 	public void readData(URI uri) {
 		String query_head = "http://search.iugonet.org/iugonet/open-search/request?query=ResourceID:";
 		String query_tail = "&Granule=granule";
 
 		try {
 			String query = query_head + uri.getRawSchemeSpecificPart() + query_tail;
 			URL urlQuery = new URL(query);
 			
 			// check proxy
 			String http_proxy_upper_case = System.getenv("HTTP_PROXY");
 			String http_proxy_lower_case = System.getenv("http_proxy");
 
 			URLConnection urlConnection;
 			if( http_proxy_upper_case != null || http_proxy_lower_case != null ){ // with proxy
 				URL urlProxy;
 				if( http_proxy_upper_case != null ){
 					urlProxy = new URL(http_proxy_upper_case);
 				}else{
 					urlProxy = new URL(http_proxy_lower_case);
 				}
 				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(urlProxy.getHost(),urlProxy.getPort()));
 				urlConnection = urlQuery.openConnection(proxy);
 			}else{                                                                // without proxy
 				urlConnection = urlQuery.openConnection();
 			}
 			
 			urlConnection.connect();
 			
 			// contents retrieve
 			
 			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();			
 			InputStream inputStream = urlConnection.getInputStream();
 			
 			int b;
 			
 			while( (b = inputStream.read()) != -1 ){
 				byteArrayOutputStream.write(b);
 			}
 			
 			inputStream.close();
 
 			// retrieve metadata
 			
 			byte[] byteAtom = byteArrayOutputStream.toByteArray();
 			String strAtom = new String(byteAtom, "UTF-8");
 //			Document atomFeed = new SAXBuilder().build(url);
 //			Element root = atomFeed.getRootElement();
 			/*
 			List list = root.getChildren("Spase");
 			for(int i=0;i<list.size();i++){
 				Element node = (Element) list.get(i);
 			}
 			*/
 
 //			XMLOutputter xmlOutputter = new XMLOutputter();
 //			System.out.println(xmlOutputter.outputString(atomFeed));
 
 			byteArrayOutputStream.close();
 		
 			// a rush job!
 			URL url = new URL(strAtom.substring(strAtom.indexOf("<URL>")+5,strAtom.indexOf("</URL>")));
 			readData(url);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Deprecated
 	public void file_http_copy(URL url) {
		this.downloadData(url);
 	}
 	
	public void downloadData(String strUrl){
 		try {
 			URL url = new URL(strUrl);
			this.downloadData(url);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}		
 	}
 	
	public void downloadData(URL url) {
 		try {
 			String[] strArray = url.getPath().split("/");
 			String strDir = "/tmp";
 			for (int i = 0; i < strArray.length - 1; i++) {
 				strDir = strDir + "/" + strArray[i];
 			}
 
 			File fileDir = new File(strDir);
 
 			if (fileDir.exists()) {
 				System.out.println(fileDir + "Directory exists.");
 			} else {
 				if (fileDir.mkdirs()) {
 					System.out.println(fileDir.getPath()
 							+ " Created directories to store data.");
 				} else {
 					System.out.println(fileDir.getPath()
 							+ " Couldn't created directories to store data.");
 				}
 			}
 
 			URLConnection conn = url.openConnection();
 			InputStream in = conn.getInputStream();
 
 			File file = new File("/tmp" + url.getPath());
 			FileOutputStream out = new FileOutputStream(file, false);
 			int b;
 			while ((b = in.read()) != -1) {
 				out.write(b);
 			}
 			out.close();
 			in.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
