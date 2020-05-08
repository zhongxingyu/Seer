 package com.web.server;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.xmlrules.DigesterLoader;
 import org.apache.log4j.Logger;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.web.services.ATAConfig;
 import com.web.services.ATAServer;
 import com.web.services.ATAConfigClient;
 import com.web.services.ExecutorServiceThread;
 
 
 public class WebServer extends Thread implements Runnable {
 	HashMap urlClassLoaderMap;
 	Logger logger=Logger.getLogger(WebServer.class);
 	Socket socket;
 	int requestPort;
 	String deployDirectory;
 	int shutdownPort;
 	Digester serverdigester;
 	public WebServer(){
 		this.socket=null;
 		this.requestPort=0;
 		this.deployDirectory=null;
 		this.shutdownPort=0;
 		serverdigester=null;
 		
 	}
 	public WebServer(Socket sock,int requestPort,String deployDirectory,int shutdownPort,Digester serverdigester,HashMap urlClassLoaderMap){
 		this.socket=sock;
 		this.requestPort=requestPort;
 		this.deployDirectory=deployDirectory;
 		this.shutdownPort=shutdownPort;
 		this.serverdigester=serverdigester;
 		this.urlClassLoaderMap=urlClassLoaderMap;
 	}
 	
 	public void run(){
 		byte[] response;
 		byte[] content;
 		byte[] uploadData = null;
 		HttpHeaderClient httpHeaderClient;
 		InputStream istream = null;
 		OutputStream ostream = null;
 		HttpHeaderServer serverParam=new HttpHeaderServer();
 		byte[] bt=new byte[9999999];
 		String value;
 		char c;
 		String endvalue="\r\n\r\n";
 		String urlFormEncoded;
 		int responseCode;
 		try{
 			istream=socket.getInputStream();
 			
 			for(int i=0;i<bt.length;i++){
 				bt[i]=-1;				
 			}
 			int ch;			
 			
			istream.read(bt);
 			
 			int index=containbytes(bt,endvalue.getBytes());
			System.out.println("index containbytes="+index);
 			value=new String(ObtainBytes(bt,0,index));
			
			System.out.println("index="+value);
			
 			//System.out.println("sock="+socket);
 			httpHeaderClient=parseHttpHeaders(value);
 			//if(httpHeaderClient.get)
 			//System.out.println(httpHeaderClient.getResourceToObtain());
 			if(httpHeaderClient.getContentType()!=null&&
 					httpHeaderClient.getContentType().equals(HttpHeaderParamNames.MULTIPARTFORMDATAVALUE)){
 					uploadData=ObtainBytes(bt,index+endvalue.length(),index+endvalue.length()+Integer.parseInt(httpHeaderClient.getContentLength()));
 					//System.out.println(new String(uploadData));
					new MultipartFormData().parseContent(uploadData,httpHeaderClient);
 					//logger.info(uploadData);
 			}
 			else if(httpHeaderClient.getContentType()!=null&&httpHeaderClient.getContentType().equals(HttpHeaderParamNames.URLENCODED)){
 					urlFormEncoded=new String(ObtainBytes(bt,index+endvalue.length(), index+endvalue.length()+Integer.parseInt(httpHeaderClient.getContentLength())));
 					HashMap paramMap=parseUrlEncoded(urlFormEncoded);
 					httpHeaderClient.setParameters(paramMap);
 			}
 			//logger.info(serverconfig.getDeploydirectory()+httpHeaderClient.getResourceToObtain());
 			
 			ostream=socket.getOutputStream();
 			
 			logger.info(new String(bt));
 			serverParam.setContentType("text/html");
 			URLDecoder decoder=new URLDecoder();
 			//System.out.println("content Length= "+socket);
 			responseCode=200;
 			content=ObtainContentExecutor(deployDirectory,httpHeaderClient.getResourceToObtain(),httpHeaderClient,serverdigester);
 			//System.out.println("content Length2= ");
 			if(content==null)content=obtainContent(deployDirectory+decoder.decode(httpHeaderClient.getResourceToObtain()));
 			if(content==null){
 				responseCode=404;
 				content=("<html><body><H1>The Request resource "+httpHeaderClient.resourceToObtain+" Not Found</H1><body></html>").getBytes();
 			}
 			//System.out.println("content Length3= ");
 			serverParam.setContentLength(""+content.length);
 			if(httpHeaderClient.getResourceToObtain().endsWith(".ico")){
 				serverParam.setContentType("image/png");
 			}
 			//System.out.println("content Length4= ");
 			response=formHttpResponseHeader(responseCode,serverParam,content);
 			//System.out.println("Response="+new String(response));
 			//System.out.println("content Length= "+content.length);
 			ostream.write(response);
 			ostream.flush();
 			ostream.close();
 		} 
 		catch (IOException e) {
 			Socket socket;
 			try {
 				socket = new Socket("localhost",shutdownPort);
 				OutputStream outputStream=socket.getOutputStream();
 				outputStream.write("shutdown WebServer\r\n\r\n".getBytes());
 				outputStream.close();
 			} catch (IOException e1) {
 				
 				e1.printStackTrace();
 			}					
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	private byte[] ObtainBytes(byte[] content,int start,int end) throws Exception{
 		if(start>=end){
 			throw new Exception("Start byte should be lesser than end byte");
 		}
 		byte[] resultBt=new byte[end-start];
 		for(int count=start;count<end;count++){
 			resultBt[count-start]=content[count];			
 		}
 		return resultBt;
 	}
 	
 	private int containbytes(byte[] byt1,byte[] byt2){
 		int count=0;
 		boolean isEqual=true;
 		for(count=0;count<byt1.length;count++){
 			isEqual=true;
 			for(int secondbytcount=0;secondbytcount<byt2.length;secondbytcount++){
 				if(byt1[count+secondbytcount]!=byt2[secondbytcount]){
 					isEqual=false;
 					break;
 				}
 			}
 			if(isEqual){
 				return count;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws SAXException 
 	 */
 	public static void main(String[] args) throws IOException, SAXException {
 		
 		
 		HashMap urlClassLoaderMap=new HashMap();
 		HashMap executorServicesMap=new HashMap();
 		HashMap ataMap=new HashMap<String,ATAConfig>();
 		Digester serverdigester = DigesterLoader.createDigester(new InputSource(new FileInputStream("./config/serverconfig-rules.xml")));
 		ServerConfig serverconfig=(ServerConfig)serverdigester.parse(new InputSource(new FileInputStream("./config/serverconfig.xml")));
 		//System.out.println(serverconfig.getPort());;
 		//System.out.println(serverconfig.getDeploydirectory());		
 		
 		
 		WarDeployer warDeployer=new WarDeployer(serverconfig.getDeploydirectory(),urlClassLoaderMap,executorServicesMap);
 		warDeployer.start();
 		byte[] shutdownBt=new byte[50];
 		WebServerRequestProcessor webserverRequestProcessor=new WebServer().new WebServerRequestProcessor(urlClassLoaderMap,Integer.parseInt(serverconfig.getPort()),serverconfig.getDeploydirectory(),Integer.parseInt(serverconfig.getShutdownport()));
 		webserverRequestProcessor.start();
 		ExecutorServiceThread executorService=new ExecutorServiceThread(Integer.parseInt(serverconfig.getServicesport()),executorServicesMap,Integer.parseInt(serverconfig.getShutdownport()),ataMap);
 		executorService.start();
 		ATAServer ataServer=new ATAServer(serverconfig.getAtaaddress(),serverconfig.getAtaport(),ataMap);
 		ataServer.start();
 		ATAConfigClient ataClient=new ATAConfigClient(serverconfig.getAtaaddress(),serverconfig.getAtaport(),serverconfig.getServicesport(),executorServicesMap);
 		ataClient.start();
 		try {
 			ServerSocket serverSocket=new ServerSocket(Integer.parseInt(serverconfig.getShutdownport()));
 			while(true){
 				Socket sock=serverSocket.accept();
 				InputStream istream=sock.getInputStream();
 				istream.read(shutdownBt);
 				String shutdownStr=new String(shutdownBt);
 				String[] shutdownToken=shutdownStr.split("\r\n\r\n");
 				System.out.println(shutdownStr);
 				if(shutdownToken[0].startsWith("shutdown WebServer")){
 					break;
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		webserverRequestProcessor.stop();
 		warDeployer.stop();
 		executorService.stop();
 		ataServer.stop();
 		ataClient.stop();
 		System.exit(0);
 	}
 	
 	public HashMap parseUrlEncoded(String urlEncoded){
 		HashMap ParamValue=new HashMap();
 		URLDecoder urlDecoder=new URLDecoder(); 
 		StringTokenizer paramGroup = new StringTokenizer(urlDecoder.decode(urlEncoded), "&");
 		 
 		   while(paramGroup.hasMoreTokens()){
 	 
 			   StringTokenizer token= new StringTokenizer(paramGroup.nextToken(), "=");
 			   String key="";
 			   String value="";
 			   if(token.hasMoreTokens())key=token.nextToken();
 			   if(token.hasMoreTokens())value=token.nextToken();
 			   ParamValue.put(key, value);
 	 
 		   }
 		   return ParamValue;
 	}
 	public byte[] ObtainContentExecutor(String deployDirectory,String resource,HttpHeaderClient httpHeaderClient,Digester serverdigester){
 		//System.out.println("In content Executor");
 		String[] resourcepath=resource.split("/");
 		//System.out.println("createDigester1");
 		Method method = null;
 		//System.out.println("createDigester2");
 		System.out.println();
 		Executors serverconfig;
 		if(resourcepath.length>1)
 		{
 			//System.out.println(resource);
 			
 			try {
 				File file=new File(deployDirectory+"/"+resourcepath[1]+"/WEB-INF/executor-config.xml");
 				synchronized(serverdigester){
 					serverconfig=(Executors)serverdigester.parse(file);
 				}
 				HashMap urlMap=serverconfig.executorMap;
 			//	System.out.println("ObtainUrlFromResource1");
 				String urlresource=ObtainUrlFromResource(resourcepath);
 				logger.info("urlresource"+urlresource);
 				Executor executor=(Executor) urlMap.get(urlresource);
 				WebClassLoader customClassLoader=null;
 				Class customClass=null;
 				System.out.println("ObtainUrlFromResource2"+executor);
 				synchronized(urlClassLoaderMap){
 				//	System.out.println("custom class Loader1"+urlClassLoaderMap);
 					customClassLoader=(WebClassLoader) urlClassLoaderMap.get(deployDirectory+"/"+resourcepath[1]);
 				//	System.out.println("custom class Loader2"+customClassLoader);
 				}
 				if(customClassLoader==null){
 					return null;
 				}
 					//System.out.println("CUSTOM CLASS lOADER path"+deployDirectory+"/"+resourcepath[1]);
 					//System.out.println("custom class loader" +customClassLoader);
 					if(executor==null){
 						//System.out.println("url resource"+urlresource);
 						String resourceClass=(String)customClassLoader.getClassMap().get(urlresource);
 						//System.out.println(resourceClass);
 						//System.out.println(customClassLoader.getClassMap());
 						if(resourceClass==null)return null;
 						customClass=customClassLoader.loadClass(resourceClass);
 						if(customClass==null)return null;
 					}
 					else customClass=customClassLoader.loadClass(executor.getExecutorclass());
 					logger.info(customClass.getClass());
 					/*if(httpHeaderClient.getHttpMethod().trim().toUpperCase().equals("GET")||(executor==null&&customClass!=null)){
 						method= customClass.getDeclaredMethod("doGet", new Class[]{HttpHeaderClient.class});
 					}
 					else if(httpHeaderClient.getHttpMethod().trim().toUpperCase().equals("POST")){
 						method= customClass.getDeclaredMethod("doPost", new Class[]{HttpHeaderClient.class});
 					}*/
 					
 				    //method.setAccessible(false);
 					ExecutorInterface executorInstance=(ExecutorInterface)customClass.newInstance();
 					Object buffer =executorInstance.doGet(httpHeaderClient);
 					if(executor!=null&&executor.getResponseResource()!=null){
 					//	System.out.println("executor resource"+executor.getResponseResource());
 						String resourceClass=(String)customClassLoader.getClassMap().get(executor.getResponseResource().trim());
 						customClass=customClassLoader.loadClass(resourceClass);
 						executorInstance=(ExecutorInterface)customClass.newInstance();
 						buffer =executorInstance.doGet(httpHeaderClient);
 					}
 					//System.out.println("executor resource 1");
 				    //Object buffer = method.invoke(customClass.newInstance(), new Object[]{httpHeaderClient});
 				    return buffer.toString().getBytes();
 			   // logger.info(buffer.toString());
 				
 			} catch (IOException | SAXException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} /*catch (InvocationTargetException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} */catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}	
 		return null;
 	}
 	private String ObtainUrlFromResource(String[] resource){
 		if(resource!=null&&resource.length>2){
 			StringBuffer resourcepath=new StringBuffer();
 			for(int resourcepathcount=2;resourcepathcount<resource.length;resourcepathcount++){
 				resourcepath.append("/");
 				resourcepath.append(resource[resourcepathcount]);
 			}
 			return resourcepath.toString();
 		}
 		return "";
 	}
 	public HttpHeaderClient parseHttpHeaders(String httpheader){
 		char c=(char)13;
 		String[] header=httpheader.split(""+c);
 		HttpHeaderClient httpClient=new HttpHeaderClient();
 		String httpHeader=header[0];
 		String[] httpHeaderArray=httpHeader.split(" ");
 		httpClient.setHttpMethod(httpHeaderArray[0]);
 		HashMap urlParams=new HashMap();
 		httpClient.setResourceToObtain(ObtainUrlAndParams(httpHeaderArray[1],urlParams));
 		httpClient.setParameters(urlParams);
 		httpClient.setHttpVersion(httpHeaderArray[2]);
 		String tmpHeader;
 		String[] headerParam;
 		String boundary;
 		String boundaryValue;
 		for(int i=1;i<header.length;i++){
 			tmpHeader=header[i];
 			logger.info(tmpHeader);
 			headerParam=splitHeaderParams(tmpHeader);
 			if(headerParam[0].equals(HttpHeaderParamNames.HOST)){
 				httpClient.setHost(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.ACCEPT)){
 				httpClient.setAcceptResource(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.ACCEPT_ENCODING)){
 				httpClient.setAcceptEncoding(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.ACCEPT_LANGUAGE)){
 				httpClient.setAcceptLanguage(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.CONNECTION)){
 				httpClient.setConnection(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.USERAGENT)){
 				httpClient.setUserAgent(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.CONTENTLENGTH)){
 				httpClient.setContentLength(headerParam[1]);
 			}
 			else if(headerParam[0].equals(HttpHeaderParamNames.CONTENTTYPE)){
 				String[] headerParamValue=headerParam[1].split(";");
 				httpClient.setContentType(headerParamValue[0].trim());
 				if(headerParamValue!=null&&headerParamValue.length>1){
 					if(httpClient.getContentType().equalsIgnoreCase(HttpHeaderParamNames.MULTIPARTFORMDATAVALUE)){
 						if(headerParamValue[1].contains(HttpHeaderParamNames.BOUNDARY)){
 							int equalToIndexInBoundary=headerParamValue[1].indexOf('=');
 							boundary=headerParamValue[1].substring(0,equalToIndexInBoundary).trim();
 							boundaryValue=headerParamValue[1].substring(equalToIndexInBoundary+1).trim();
 							httpClient.setBoundary(boundaryValue);
 						}
 					}
 					
 				}
 			}
 		}
 		
 		return httpClient;
 	}
 	public String ObtainUrlAndParams(String url,HashMap params){
 		 
 			URLDecoder decoder=new URLDecoder();
 			url=decoder.decode(url);
 			if (url.indexOf("?") > -1) {
 			   String paramaters = url.substring(url.indexOf("?") + 1);
 			   StringTokenizer paramGroup = new StringTokenizer(paramaters, "&");
 			   
 			   while(paramGroup.hasMoreTokens()){
 		 
 				   StringTokenizer value = new StringTokenizer(paramGroup.nextToken(), "=");
 				   String param=null;
 				   if(value.hasMoreTokens()){
 					   param=value.nextToken();
 				   }
 				   String paramValue=null;
 				   if(value.hasMoreTokens()){
 					   paramValue=value.nextToken();
 				   }
 				   params.put(param,paramValue);
 		 
 			   }
 			}
 			if(url.indexOf("?")!=-1){
 			   return  url.substring(0,url.indexOf("?"));
 			}
 			return url;		   
 	}
 	public String[] splitHeaderParams(String headerParams){
 		int indexToSplit=headerParams.indexOf(":");
 		String[] param=new String[2];
 		param[0]=headerParams.substring(0,indexToSplit).trim();
 		param[1]=headerParams.substring(indexToSplit+1).trim();
 		return param;
 	}
 	
 	private byte[] formHttpResponseHeader(int responseCode,HttpHeaderServer httpHeader,byte[] content){
 		StringBuffer buffer=new StringBuffer();
 		String colon=": ";
 		String crlf="\r\n";
 		if(responseCode==200){
 			buffer.append("HTTP/1.1 200 OK");
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.DATE);
 			buffer.append(colon);
 			buffer.append(httpHeader.getDate());		
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.SERVER);
 			buffer.append(colon);
 			buffer.append(httpHeader.getServer());		
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.CONTENT_TYPE);
 			buffer.append(colon);
 			buffer.append(httpHeader.getContentType());
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.CONTENT_LENGTH);
 			buffer.append(colon);
 			buffer.append(httpHeader.getContentLength());
 			buffer.append(crlf);
 			buffer.append(HttpHeaderServerParamNames.LAST_MODIFIED);
 			buffer.append(colon);
 			buffer.append(httpHeader.getLastModified());
 			
 		}
 		else if(responseCode==404){
 			buffer.append("HTTP/1.1 404 Not Found");
 		}
 		buffer.append(crlf);
 		buffer.append(crlf);
 		byte[] byt1=buffer.toString().getBytes();
 		byte[] byt2=new byte[byt1.length+content.length];
 		///System.out.println("Header="+new String(byt1));
 		for(int count=0;count<byt1.length;count++){
 			byt2[count]=byt1[count];
 		}
 		for(int count=0;count<content.length;count++){
 			byt2[count+byt1.length]=content[count];
 		}
 		//System.out.println("Header with content="+new String(byt2));
 		/*try {
 			buffer.append(new String(content, "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		return byt2;
 	}
 	private byte[] obtainContent(String filepath){
 		byte[] filebt = null;
 		try {
 			File file=new File(filepath);			
 			FileInputStream stream=new FileInputStream(filepath);
 			filebt=new byte[(int) file.length()];
 			stream.read(filebt,0,(int)file.length());
 			stream.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			filebt=null;
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			filebt=null;
 			e.printStackTrace();
 		}
 		catch(Exception ex){
 			filebt=null;
 			ex.printStackTrace();
 		}
 		return filebt;
 		
 	}
 	
 	public void addURL(URL url,WebClassLoader customClassLoader) throws IOException {
 			try {
 			  customClassLoader.addURL(url);
 		  } catch (Throwable t) {
 		     t.printStackTrace();
 		  }//end try catch
 	}//end method
 	class WebServerRequestProcessor extends Thread implements Runnable{
 		int requestPort;
 		String deployDirectory;
 		int shutdownPort;
 		HashMap urlClassLoaderMap;
 		public WebServerRequestProcessor(){
 			
 		}
 		public WebServerRequestProcessor(HashMap urlClassLoaderMap,int requestPort,String deployDirectory,int shutdownPort){
 			this.requestPort=requestPort;
 			this.deployDirectory=deployDirectory;
 			this.shutdownPort=shutdownPort;
 			this.urlClassLoaderMap=urlClassLoaderMap;
 		}
 		public void run(){
 			
 			ServerSocket serverSocket = null;
 			Socket sock=new Socket();
 			try {
 				serverSocket=new ServerSocket(this.requestPort);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			Digester serverdigester = null;
 			try {
 				serverdigester = DigesterLoader.createDigester(new InputSource(new FileInputStream("./config/executorconfig-rules.xml")));
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			while(true){
 				
 				try{
 					sock=serverSocket.accept();
 					//System.out.println("A new Socket="+sock);
 					WebServer webserver=new WebServer(sock,this.requestPort,this.deployDirectory,this.shutdownPort,serverdigester,urlClassLoaderMap);
 					webserver.start();
 				}
 				catch(Exception ex){
 					try {
 						socket = new Socket("localhost",shutdownPort);
 						OutputStream outputStream=socket.getOutputStream();
 						outputStream.write("shutdown WebServer\r\n\r\n".getBytes());
 						outputStream.close();
 					} catch (IOException e1) {
 						
 						e1.printStackTrace();
 					}	
 				}
 			}
 		}
 	}
 }
 
   
