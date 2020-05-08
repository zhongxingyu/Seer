 package model;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 
import exceptions.BadConnectionException;
 import exceptions.BadResponseException;
import exceptions.BadServerException;
 
 public abstract class HttpMsg {
 	public enum HttpVersion{
 		HTTP_1_0,HTTP_1_1,BAD_REQUEST
 	}
 	private Map<String,String> headers = new HashMap<String, String>();
 	private final InputStream in;
 
 	public HttpMsg(){
 		in = null;
 	}

 	public HttpMsg(final InputStream in) throws BadResponseException {
 		this.in=in;
 		if(in!=null){
 			parseHeaders();
 		}
 	}
 
 	abstract void parseFirstLine(String line);
 
 	protected String readLine() throws BadResponseException {
 		final ByteArrayOutputStream b = new ByteArrayOutputStream();
 
 		int c;
 		while ((c = this.read()) != '\r' && c != '\n' && c != -1) {
 			b.write(c);
 		}
 
 		if ((c == '\r' && this.read() != '\n') || c == -1) {
 			if (c != -1) {
 				throw new BadResponseException();
 			} else {
				throw new BadConnectionException();
 			}
 		}
 		String line = null;
 		try {
 			line = new String(b.toByteArray(), "ISO-8859-1");
 		} catch (final UnsupportedEncodingException e) {
 		}
 		return line;
 	}
 
 	private int read() {
 		try {
 			return in.read();
 		} catch (final InterruptedIOException e) {
			throw new BadServerException();
 		} catch (final IOException e) {
			throw new BadConnectionException();
 		}
 	}

 	private void parseHeaders() throws BadResponseException{
 		String s=readLine();
 		if(s!=null && !"".equals(s)){
			parseFirstLine(s);
 		}
 		while(null!=(s=readLine()) && !"".equals(s)){
 			parseHeaderLine(s);
 		}
 	}
 	private void parseHeaderLine(String line){
 		String[] aux = line.split(":");
 		if(aux.length>1){
 			String header = aux[0].trim();
 			String value = line.substring(aux[0].length()+1).trim();
 			headers.put(header, value);
 		}
 	}
 	public String getHeader(String key){
 		return headers.get(key);
 	}
 	public Map<String,String> getHeaders(){
 		return headers;
 	}
 	public void setHeaders(Map<String,String> headers){
 		this.headers=headers;
 	}
 	public void setHeader(String header, String value){
 		headers.put(header, value);
 	}
 }
