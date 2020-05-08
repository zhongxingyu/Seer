 package webserver;
 
 import http_parser.HTTPCallback;
 import http_parser.HTTPDataCallback;
 import http_parser.HTTPMethod;
 import http_parser.HTTPParser;
 import http_parser.ParserSettings;
 import http_parser.ParserType;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import middleware.Middleware;
 import middleware.MiddlewareException;
 
 
 public class Client implements Comparable<Client>{
 	protected final SocketChannel ch;
 	private Request request;
 	private ParserSettings settings;
 	private HTTPParser parser;
 	private String lastHeader;
 	private boolean returnVal;
 	protected long lastCommunication;
 	protected SelectionKey key;
 
 	public Client(SocketChannel ch, SelectionKey key){
 		this.ch = ch;
 		this.key = key;
 		updateLastCommunication();
 		
 		parser = new HTTPParser(ParserType.HTTP_REQUEST);
 		
 		settings = new ParserSettings();
 		
 		settings.on_message_begin = new HTTPCallback() {
 			
 			@Override
 			public int cb(HTTPParser parser) {
				request = new Request(parser.getHTTPMethod());
 				return 0;
 			}
 		};
 		
 		settings.on_path = new HTTPDataCallback(){
 			@Override
 			public int cb(HTTPParser p, byte[] by, int pos, int len) {
 				request.path = new String(by);
 				return 0;
 				
 			}
 		};
 		
 		settings.on_header_field = new HTTPDataCallback(){
 			@Override
 			public int cb(HTTPParser p, byte[] by, int pos, int len) {
 				lastHeader = new String(by);
 				
 				if(request.httpMajor == 0){
 					request.httpMajor = p.http_major;
 					request.httpMinor = p.http_minor;
 				}
 				return 0;
 				
 			}
 		};
 		
 		settings.on_header_value = new HTTPDataCallback(){
 			@Override
 			public int cb(HTTPParser p, byte[] by, int pos, int len) {
 				request.headers.put(lastHeader,new String(by));
 				return 0;
 				
 			}
 		};
 		
 		settings.on_message_complete = new HTTPCallback() {
 			
 			@Override
 			public int cb(HTTPParser parser) {
 				request.completed = true;
 				sendResponse();
 				return 0;
 			}
 		};
 		
 		
 	}
 	public void requestFinished() {
 		request = null;
 	}
 
 	public void close() {
 		returnVal = false;
 	}
 	
 	//returns false if connection should be closed.
 	public boolean doRead() {
 		returnVal = true;
 		
 		ByteBuffer buff = ByteBuffer.allocate(Server.BUFFER_SIZE); //TODO: one buffer per thread?
 		try {
 			int res = ch.read(buff);
 			if(res == -1){
 				returnVal = false; //close
 			}
 		} catch (IOException e) {
 			System.err.println("Socket read failed");
 			returnVal = false;
 			e.printStackTrace();
 		}
 		buff.flip();
 		parser.execute(settings,buff);
 		buff.clear();
 
 		return returnVal;
 	}
 	
 	
 	private void sendResponse() {
 		Response response = new Response(this);
 		for(Middleware middleware: Server.middlewares){
 			try{
 				middleware.execute(request, response);
 			}catch(MiddlewareException ex){
 				break;
 			}
 		}
 		response.sendHeaders();
 		response.end();
 		//TODO: if connection:close, then returnVal=false!
 	}
 	
 	public void updateLastCommunication() {
 		this.lastCommunication = System.currentTimeMillis();
 		
 	}
 
 	@Override
 	public int compareTo(Client o) {
 		return (int)((o.lastCommunication - this.lastCommunication)/1000);
 	}
 	
 }
