 package sfs.request.http;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import sfs.entry.HTTPHeaderEntry;
 import sfs.header.http.HTTPHeader;
 import sfs.header.http.HeaderEntry;
 import sfs.header.http.RequestHeaderEntry;
 import sfs.header.http.ResponseHeaderEntry;
 import sfs.header.http.ending.Ending;
 import sfs.header.http.separator.WhiteSpace;
 import sfs.request.Request;
 import sfs.response.http.ResponseMessage;
 import sfs.verb.http.Verb;
 
 public class RequestMessage extends Request {
 
 	private String verb;
 	private String contextPath;
 	private String requestHTTPVersion;
 	private final List<HTTPHeaderEntry> header;
 	private String content;
 	private static final String HTTP_VERSION = "HTTP/1.1";
 
 	public RequestMessage(){
 		header = new LinkedList<HTTPHeaderEntry>();
 	}
 	
 	public String getVerb() {
 		return verb;
 	}
 
 	public void setVerb(String verb) {
 		this.verb = verb;
 	}
 
 	public String getContextPath() {
 		return contextPath;
 	}
 
 	public void setContextPath(String contextPath) {
 		this.contextPath = contextPath;
 	}
 
 	public String getRequestHTTPVersion() {
 		return requestHTTPVersion;
 	}
 
 	public void setRequestHTTPVersion(String requestHTTPVersion) {
 		this.requestHTTPVersion = requestHTTPVersion;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public List<HTTPHeaderEntry> getHeader() {
 		return header;
 	}
 	
 	@Override
 	protected String doRequest(Verb verb, String resource) {
 
 		return verb.toString() + new WhiteSpace().getSeparator() + resource + new WhiteSpace().getSeparator()
 				+ HTTP_VERSION + Ending.CRLF;
 	}
 
 	/**
 	 * Creates Message with the specified verb, resource and HTTP headers.
 	 * @param verb Verb used for the resource.
 	 * @param resource Resource where the message to be sent.
 	 * @param entries Headers.
 	 * @return Message as String.
 	 */
 	public String createMessage(Verb verb, String resource, HTTPHeaderEntry[] entries) {
 
 		String str = doRequest( verb, resource );
 		HTTPHeader header = new HTTPHeader();
 		for ( HTTPHeaderEntry entry : entries ) {
 			header.put( entry.getKey(), entry.getValue() );
 		}
 
 		str += header.format();
 		return str;
 	}
 	
 	/**
 	 * Copied from sfs.response.http.ResponseMessage.extractMessage().Extracts status line, header and content from the
 	 * specified string.
 	 * 
	 * @param request
	 *            HTTP request where status line, header and content are extracted.
 	 * @return This object.
 	 */
	public RequestMessage extractMessage(String request) {
 
		String[] res = request.split( Ending.CRLF.toString() );
 		if ( ( res == null ) || ( res.length == 1 ) ) {
 			content = "";
 		}
 		else {
 			int count = 0;
 			int current = 0;
 			for ( String str : res ) {
 
 				if ( current == 0 ) {
 					setStatusCode( str );
 					current++;
 					continue;
 				}
 				else {
 					if ( str.isEmpty() ) {
 						count++;
 					}
 					else {
 
 						if ( count == 1 ) {
 							content = str;
 						}
 						else {
 							extractHeader( str );
 						}
 					}
 				}
 
 				current++;
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Sets status line from the specified string.
 	 * 
 	 * @param statusLine
 	 *            String holds the status line.
 	 */
 	private void setStatusCode(String statusLine) {
 
 		String[] each = statusLine.split( " " );
 		verb = each[0];
 		contextPath = each[1];
 		requestHTTPVersion = each[2];
 	}
 
 	/**
 	 * Extracts each header entry from the specified string.
 	 * 
 	 * @param headerStr
 	 *            String holds each header key and value.
 	 * @return Header key and value as HTTPHeaderEntry.
 	 */
 	private HTTPHeaderEntry extractHeader(String headerStr) {
 
 		HTTPHeaderEntry headerEntry = null;
 		String[] each = headerStr.split( ": " );
 		if ( each[0].equals( RequestHeaderEntry.ACCEPT.toString() ) ) {
 			headerEntry = new HTTPHeaderEntry( HeaderEntry.CONTENT_TYPE, each[1] );
 		}
 		else if ( each[0].equals( HeaderEntry.DATE.toString() ) ) {
 			headerEntry = new HTTPHeaderEntry( HeaderEntry.DATE, each[1] );
 		}
 		else if ( each[0].equals( RequestHeaderEntry.GREETING.toString() ) ) {
 			headerEntry = new HTTPHeaderEntry( RequestHeaderEntry.GREETING, each[1] );
 		}
 		else if ( each[0].equals( RequestHeaderEntry.HOST.toString() ) ) {
 			headerEntry = new HTTPHeaderEntry( RequestHeaderEntry.HOST, each[1] );
 		}
 		else if ( each[0].equals( RequestHeaderEntry.TYPE.toString() ) ) {
 			headerEntry = new HTTPHeaderEntry( RequestHeaderEntry.TYPE, each[1] );
 		}
 		else if ( each[0].equals( RequestHeaderEntry.LIVENESS.toString() ) ) {
 			headerEntry = new HTTPHeaderEntry( RequestHeaderEntry.LIVENESS, each[1] );
 		}
 
 		header.add( headerEntry );
 		return headerEntry;
 	}
 
 }
