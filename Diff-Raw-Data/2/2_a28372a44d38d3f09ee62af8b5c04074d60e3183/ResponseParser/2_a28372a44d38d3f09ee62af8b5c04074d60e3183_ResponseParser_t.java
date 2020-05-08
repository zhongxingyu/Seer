 package ar.edu.itba.pdc.duta.http;
 
 import java.nio.ByteBuffer;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 public class ResponseParser extends MessageParser {
 
 	private String HTTPVersion, reasonPhrase;
 	private int statusCode;
 
 	public ResponseParser(ByteBuffer buffer) {
 		super(buffer);
 	}
 
 	@Override
 	protected MessageHeader createHeader(Map<String, String> fields) {
 
 		return new ResponseHeader(HTTPVersion, statusCode, reasonPhrase, fields);
 	}
 
 	@Override
 	protected void setStartLine(String s) throws Exception{
 
 		Scanner scan = new Scanner(s);
 		
 		try {
 			HTTPVersion = scan.next();
 			statusCode = scan.nextInt();
			reasonPhrase = scan.nextLine().substring(1);
 		} catch (NoSuchElementException e) {
 			throw new Exception();
 		}
 		
 		if (!Grammar.isHTTPVersion(HTTPVersion) || statusCode < 100 || statusCode > 999) {
 			throw new Exception();
 		}
 	}
 }
