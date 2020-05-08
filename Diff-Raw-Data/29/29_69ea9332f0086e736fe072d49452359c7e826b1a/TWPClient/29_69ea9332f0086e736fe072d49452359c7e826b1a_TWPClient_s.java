 package twp;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 
 public class TWPClient {
 	private static final String MAGIC_BYTES = "TWP3\n";
 	private static final int SHORT_INTEGER = 13;
 	private static final int LONG_INTEGER = 14;
 	private static final int LONG_STRING = 127;
 	
 	private String host;
 	private int port;
 	
 	private Socket socket;
 	private DataOutputStream writer;
 	private DataInputStream reader;
 	
 	public TWPClient(String host, int port) {
 		this.host = host;
 		this.port = port;
 	}
 
 	public void connect() throws UnknownHostException, IOException {
 		this.socket = new Socket(host, port);
 		this.writer = new DataOutputStream(socket.getOutputStream());
 		this.reader = new DataInputStream(socket.getInputStream());
 	}
 
 	public TWPClient(Socket socket) throws IOException {
 		this.socket = socket;
 		this.writer = new DataOutputStream(socket.getOutputStream());
 		this.reader = new DataInputStream(socket.getInputStream());
 	}
 	
 	public void disconnect() throws IOException {
 		socket.close();
 	}
 
 	public String readMagicBytes() throws IOException {
 		byte[] magicBytes = new byte[5];
 		reader.readFully(magicBytes);
 		return new String(magicBytes);
 	}
 
 	public void writeMagicBytes() throws IOException {
 		writer.writeBytes(MAGIC_BYTES);
 	}
 
 	public int readProtocol() throws IOException {
 		return readInteger();
 	}
 
 	public void writeProtocol(int protocol) throws IOException {
 		writeInteger(protocol);
 	}
 
 	// TODO: registered extensions
 	public int readMessageId() throws IOException {
 		byte message = reader.readByte();
 		return message - 4;
 	}
 
 	// TODO: registered extensions
 	public void writeMessageId(int i) throws IOException {
 		writer.write(i + 4);
 	}
 	
 	public int readInteger() throws IOException {
 		int type = reader.readByte();
 		int value = 0;
 		if (type == SHORT_INTEGER)
 			value = reader.readByte();
 		else if (type == LONG_INTEGER)
 			value = reader.readInt();
 		return value;
 	}
 	
 	public void writeInteger(int value) throws IOException {
 		if (-128 <= value && value <= 127) {
 			writer.write(SHORT_INTEGER);
 			writer.write(value);
 		} else {
 			writer.write(LONG_INTEGER);
 			writer.writeInt(value);
 		}
 	}
 
 	public String readString() throws IOException {
 		int type = reader.readByte();
 		int length = type == LONG_STRING ? reader.readInt() : type - 17;
 		byte[] value = new byte[length];
 		reader.readFully(value);
 		return new String(value, "UTF-8");
 	}
 
 	public void writeString(String string) throws IOException {
 		byte[] bytes = string.getBytes("UTF-8");
 		int type = bytes.length > 109 ? LONG_STRING : bytes.length + 17;
 		writer.write(type);
 		if (type == LONG_STRING)
 			writer.writeInt(bytes.length);
 		writer.write(bytes);
 	}
 
 	public void readEndOfMessage() throws IOException {
 		reader.readByte();
 	}
 
 	public void writeEndOfMessage() throws IOException {
 		writer.write(0);
 	}
 
 	// example
 	public static void main(String[] args) throws UnknownHostException, IOException {
 		
 		if (args.length < 1) {
 			System.err.println("Please provide a text to echo.");
 			return;
 		}
 		
 		TWPClient twp = new TWPClient("www.dcl.hpi.uni-potsdam.de", 80);
 		twp.connect();
 		
 		int requestProtocol  = 2;
 		int requestMessageId = 0;
 		String requestText   = args[0];
 		
 		twp.writeMagicBytes();
 		twp.writeProtocol(requestProtocol);
 		twp.writeMessageId(requestMessageId);
 		twp.writeString(requestText);
 		twp.writeEndOfMessage();
 
 		System.out.println();
 		System.out.println("Request:");
 		System.out.println("- Protocol: " + requestProtocol);
 		System.out.println("- Message ID: " + requestMessageId);
 		System.out.println("- Parameters:");
 		System.out.println("  - Text: " + requestText);
 		
 		int responseMessageId = twp.readMessageId();
 		String responseText = twp.readString();
 		int responseLength = twp.readInteger();
 		twp.readEndOfMessage();
 		
 		System.out.println();
 		System.out.println("Response:");
 		System.out.println("- Message ID: " + responseMessageId);
 		System.out.println("- Parameters:");
 		System.out.println("  - Text: " + responseText);
 		System.out.println("  - Number of Letters: " + responseLength);
 		System.out.println();
 
 		twp.disconnect();
 	}
 }
