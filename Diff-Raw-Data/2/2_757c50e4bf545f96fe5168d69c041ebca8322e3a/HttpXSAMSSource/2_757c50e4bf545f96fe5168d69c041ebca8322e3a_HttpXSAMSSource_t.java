 package org.vamdc.validator.source.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.zip.GZIPInputStream;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.vamdc.validator.Settings;
 import org.vamdc.validator.interfaces.XSAMSSource;
 import org.vamdc.validator.interfaces.XSAMSSourceException;
 
 public class HttpXSAMSSource extends XSAMSSource {
 
 	private String baseURLStr = "";
 
 	//Default connect and read timeouts for http connections
 
 	private CapabilitiesClient caps;
 	private AvailabilityClient avail;
 
 	private Pretty prettyprinter = new Pretty(); 
 
 	public HttpXSAMSSource() throws XSAMSSourceException{
 		String vosiURL = Settings.get(Settings.ServiceVOSIURL);
 		String tapURL = Settings.get(Settings.ServiceTAPURL);
 		if (vosiURL!=null && vosiURL.length()>0){
 			caps = new CapabilitiesClient(vosiURL);
 			if (caps!=null){
 				baseURLStr = caps.getTapEndpoint()+Settings.get(Settings.ServiceTAPSuffix);
 
 				try {
 					new URL(baseURLStr);
 				} catch (MalformedURLException e) {
 					throw new XSAMSSourceException("Service base URL '"+baseURLStr+"' is malformed \r\n");
 				}
 			}
 		}else{
 			baseURLStr = tapURL+Settings.get(Settings.ServiceTAPSuffix);
 		}
 
 	}
 
 	@Override
 	public InputStream getXsamsStream(String query) throws XSAMSSourceException {
 		if (baseURLStr==null)
 			throw new XSAMSSourceException("base URL is null :(");
 		if (caps!=null){
 			if ( caps.getAvailabilityEndpoint().equals(""))
 				throw new XSAMSSourceException("availability endpoint is not defined");
 			avail = new AvailabilityClient(caps.getAvailabilityEndpoint());
 
 			if (!avail.isAvailable()){
 				throw new XSAMSSourceException("Service not available: "+avail.getMessage());
 			}else{
 				return doQuery(query);
 			}
 		}else{
 			return doQuery(query);
 		}
 	}
 
 	/**
 	 * Perform query
 	 * @param query
 	 * @return
 	 * @throws XSAMSSourceException
 	 */
 	public InputStream doQuery(String query) throws XSAMSSourceException{
 		URL requestURL;
 		try {
 			requestURL = new URL(baseURLStr+URLEncoder.encode(query,"UTF-8"));
 
 			//Prettyprint if requested
 			if (Settings.getBoolean(Settings.ServicePrettyOut))
 				return prettyprinter.transform(
 						new NoSpaceStream(
 								openConnection(
 										requestURL)));
			return openConnection(requestURL);
 		} catch (MalformedURLException e) {
 			try {
 				throw new XSAMSSourceException("Service base URL '"+baseURLStr+URLEncoder.encode(query,"UTF-8")+"' is malformed \r\n");
 			} catch (UnsupportedEncodingException e1) {
 				e1.printStackTrace();
 			}
 		} catch (IOException e) {
 			throw new XSAMSSourceException("IO exception while opening http connection:"+e.getMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public Collection<String> getRestrictables() {
 		if (caps!=null)
 			return caps.getRestrictables();
 		else return new ArrayList<String>();
 	}
 
 	/**
 	 * Open URL connection, with timeouts set
 	 * @param address connection URL
 	 * @return Stream of data
 	 * @throws IOException 
 	 */
 	private static InputStream openConnection(URL adress) throws IOException{
 		URLConnection conn = adress.openConnection();
 		//Allow gzip encoding
 		conn.setRequestProperty("Accept-Encoding", "gzip");
 		//Set timeouts
 		conn.setConnectTimeout(Settings.getInt(Settings.HTTP_CONNECT_TIMEOUT));
 		conn.setReadTimeout(Settings.getInt(Settings.HTTP_DATA_TIMEOUT));
 
 		InputStream responseStream = conn.getInputStream();
 		String contentEncoding = conn.getContentEncoding();
 		if ("gzip".equalsIgnoreCase(contentEncoding)) {
 			responseStream = new GZIPInputStream(responseStream);
 		}
 		return responseStream;
 	}
 
 	/**
 	 * Class to remove extra space between tags, 
 	 * needed to make prettyprinter work correctly
 	 * 
 	 * outputs data from input stream, if not in tag then skip all empty space if there is only empty space between tags.
 	 * @author doronin
 	 *
 	 */
 	private class NoSpaceStream extends InputStream{
 		private byte[] buffer;
 		private int index;
 
 		private InputStream backStream;
 
 		private int quote='\0';//Quote used
 		private boolean backslash=false;//Is this symbol backslashed?
 		private boolean intag=false;
 
 		public NoSpaceStream(InputStream back){
 			buffer=new byte[0];
 			index=0;
 			backStream = back;
 
 		}
 
 		@Override
 		public int read() throws IOException {
 
 			//If we have buffer filled, return data from it:
 			if (index<buffer.length)
 				return buffer[index++];
 
 			//else if in tag, look for end of tag, read bytes normally
 			if (intag){
 				//Read a byte
 				int newbyte = backStream.read();
 				//Check if we encounter quotes:
 				if (newbyte == '\'' || newbyte == '"' && !backslash){
 					if (quote == newbyte)
 						quote='\0';//We are out of quotes now
 					else if (quote=='\0')
 						quote = newbyte;//Entered quotes
 				}
 
 				//Check if symbol is backslashed, just return if yes
 				if (backslash){
 					backslash=false;
 				}else if (newbyte=='\\') backslash=true;
 
 				if (newbyte == '>' && quote=='\0')
 					intag=false;//Getting out of tag
 				return newbyte;
 			}else{
 				int newbyte='\0';
 				//Buffer to keep data that occured between tags and consisted of not only empty space
 				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
 				//Not in tag, read all data up to next tag start, decide if they make sense
 				while ((newbyte = backStream.read())!='<' && newbyte!=-1){
 					bytestream.write(newbyte);
 				}
 				//if (newbyte!=-1)
 				bytestream.write(newbyte);
 				byte[] bytes = bytestream.toByteArray();
 				boolean hasdata=false;
 				for (int i=0;i<bytes.length-1;i++){
 					byte thisbyte = bytes[i];
 					if (thisbyte!='\n' && thisbyte!='\r' && thisbyte!='\t' && thisbyte!=' '){
 						hasdata=true;
 						break;
 					}
 				}
 
 				intag=true;//Set intag to true since we must have encountered new tag
 
 				if (hasdata){//Print first byte of buffer if we got some data
 					buffer=bytes;
 					index=1;
 					return buffer[0];
 				}else{
 					//Otherwise start a new tag, go on
 					return newbyte;
 				}	
 			}
 		}
 
 	}
 
 
 
 	/**
 	 * Class to pretty-print XML (works only with extra space removed from XML beforehand)
 	 * @author doronin
 	 *
 	 */
 	private class Pretty implements Runnable{
 		private Transformer transformer;
 		private Source src=null;
 		private StreamResult xmlOutput;
 		private PipedInputStream in;
 		private PipedOutputStream out;
 		public Pretty(){
 			try {
 				TransformerFactory transFactory = TransformerFactory.newInstance();
 				transFactory.setAttribute("indent-number",new Integer(2));
 				transformer = transFactory.newTransformer();
 				transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
 				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); 
 			} catch (TransformerConfigurationException e) {
 				e.printStackTrace();
 			} catch (TransformerFactoryConfigurationError e) {
 				e.printStackTrace();
 			}  
 
 		}
 
 		public InputStream transform(InputStream source){
 			if (src == null ){
 				//Setup streams
 				try{
 					src = new StreamSource(source);
 					in = new PipedInputStream();
 					out = new PipedOutputStream(in);
 					xmlOutput = new StreamResult(new OutputStreamWriter(out, "utf-8"));
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				new Thread(this).start();
 				return in;
 			}else{
 				return source;
 			}
 		}
 
 		@Override
 		public void run() {
 			try {
 				transformer.transform(src, xmlOutput);
 				out.close();
 				src = null;
 			} catch (Exception e) {
 				try {
 					out.close();
 				} catch (IOException e1) {
 				}
 				src=null;
 			}
 
 		}
 
 
 	}
 
 }
