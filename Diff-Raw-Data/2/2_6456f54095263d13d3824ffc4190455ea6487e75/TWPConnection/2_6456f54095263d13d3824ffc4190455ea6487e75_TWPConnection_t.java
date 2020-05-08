 package twp.core;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 
 
 public class TWPConnection {
 	
 	private class Listener extends Thread {
 
 		private boolean isActive;
 		
 		public Listener() {
 			this.isActive = true;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				while (isActive) {
 					Message message = readMessage();
 					protocol.onMessage(message);
 				}
 			} catch (EOFException e) {}
 			catch (IOException e) {}
 		}
 		
 		public void disconnect() {
 			isActive = false;
 		}	
 	}
 	
 	private static final String MAGIC_BYTES = "TWP3\n";
 	
 	private Socket socket;
 	private int protocolVersion;
 	private DataOutputStream writer;
 	private DataInputStream reader;
 	private TWPProtocol protocol;
 	private Listener listener;
 	
 	public TWPConnection(String host, int port, TWPProtocol p) throws UnknownHostException, IOException {
 		socket = new Socket(host, port);
 		protocol = p;
 		protocolVersion = p.getVersion();
 		reader = new DataInputStream(socket.getInputStream());
 		writer = new DataOutputStream(socket.getOutputStream());
 		startClient();
 	}
 	
 	public TWPConnection(Socket s, TWPProtocol p) throws IOException {
 		socket = s;
 		reader = new DataInputStream(s.getInputStream());
 		writer = new DataOutputStream(s.getOutputStream());
 		protocol = p;
 		startServer();
 		listen();
 	}
 	
 	public byte[] getLocalAddress() {
 		return socket.getLocalAddress().getAddress();
 	}
 	
 	public int getLocalPort() {
 		return socket.getLocalPort();
 	}
 	
 	private void startClient() throws IOException {
 		writeMagicBytes();
 		writeProtocol(this.protocolVersion);
 	}
 	
 	private void startServer() throws IOException {
 		readMagicBytes();
 		this.protocolVersion = readProtocol();
 	}
 	
 	public void listen() throws IOException {
 		listener = new Listener();
 		listener.start();
 	}
 	
 	public Message readMessage() throws IOException {
 		int tag = readMessageId();
 		return readMessage(tag);
 	}
 	
 	private Parameter createParameter(int tag) throws IOException {
 		ParameterType type = getParameterType(tag);
 		Parameter p = null;
 		switch(type) { 
 			case SHORT_INTEGER: 
 			case LONG_INTEGER:
 				int value0 = readInteger(tag);
 				p = new Parameter(type, value0);
 				break;
 			case SHORT_STRING: 
 			case LONG_STRING:
 				String value1 = readString(tag);
 				p = new Parameter(type, value1);
 				break;
 			case SHORT_BINARY:
 			case LONG_BINARY:
 				byte[] value2 = readByte(tag);
 				p = new Parameter(type, value2);
 				break;
 			case STRUCT:
 			case SEQUENCE:
 			case UNION:
 			case REGISTERED_EXTENSION:
 				TWPContainer value3 = readContainer(tag);
 				p = new Parameter(type, value3);
 				break;
 			case NO_VALUE:
 				p = new Parameter(type, null);
 				break;
 			case RESERVED:
 				break;
 			case APPLICATION_TYPE:
 				TWPContainer value4 = readApplicationType(tag);
 				p = new Parameter(type, value4);
 				break;
 		}
 		return p;
 	}
 	
 	public TWPContainer readContainer(int tag) throws IOException {
 		ParameterType type = getParameterType(tag);
 		TWPContainer container = new TWPContainer(type);
 		if (type == ParameterType.REGISTERED_EXTENSION) {
 			container.setId(reader.readInt());
 		} else if (type == ParameterType.UNION) {
 			container.setId(tag - 4);
 		}
 		tag = reader.readUnsignedByte();
 		type = getParameterType(tag);
 		while (type != ParameterType.END_OF_CONTENT) {
 			Parameter p = createParameter(tag);
 			if (p != null) {
 				container.add(p);
 			}
			if (container.getType() == ParameterType.UNION)
 				tag = 0;
 			else
 				tag = reader.readUnsignedByte();
 			type = getParameterType(tag);
 		}
 		return container;
 	}
 	
 	public void writeContainer(TWPContainer container) throws IOException {
 		switch (container.getType()) {
 		case STRUCT:
 			writer.write(2);
 			break;
 		case SEQUENCE:
 			writer.write(3);
 			break;
 		case UNION:
 			writer.write(container.getId() + 4);
 			break;
 		case REGISTERED_EXTENSION:
 			writer.write(12);
 			writer.writeInt(container.getId());
 			break;
 		}
 		Iterator<Parameter> iterator = container.getParameters().iterator();
 		while (iterator.hasNext()) {
 			Parameter param = iterator.next();
 			writeParameter(param);	
 		}
 		if (container.getType() != ParameterType.UNION)
 			writer.write(0);
 	}
 	
 	private TWPContainer readApplicationType(int tag) throws IOException {
 		TWPContainer container = new TWPContainer();
 		container.setId(tag);
 		int length = reader.readInt();
 		byte[] content = new byte[length];
 		reader.readFully(content);
 		container.add(new Parameter(ParameterType.APPLICATION_TYPE, content));
 		return container;
 	}
 	
 	private void writeApplicationType(TWPContainer container) throws IOException {
 		writer.writeByte(container.getId());
 		if (container.getParameters().size() == 1) {
 			writer.writeInt(((byte[]) container.getParameters().get(0).getValue()).length);
 			writer.write((byte[]) container.getParameters().get(0).getValue());
 		} else {
 			writer.write(0);
 		}
 	}
 	
 	public Message readMessage(int messageId) throws IOException {
 		Message message = new Message(messageId, protocolVersion);
 		int tag = reader.readUnsignedByte();
 		ParameterType type = getParameterType(tag);
 		while (type != ParameterType.END_OF_CONTENT) {
 			Parameter p = createParameter(tag);
 			if (p != null) {
 				message.addParameter(p);
 			}
 			tag = reader.readUnsignedByte();
 			type = getParameterType(tag);
 		}
 		return message;
 	}
 	
 	private void writeParameter(Parameter p) throws IOException {
 		switch(p.getType()) {
 		case SHORT_INTEGER:
 		case LONG_INTEGER: 
 			writeInteger((Integer) p.getValue());
 			break;
 		case SHORT_STRING:
 		case LONG_STRING:
 			writeString((String) p.getValue());
 			break;
 		case SHORT_BINARY:
 		case LONG_BINARY:
 			writeByte((byte[]) p.getValue());
 			break;
 		case STRUCT:
 		case SEQUENCE:
 		case UNION:
 		case REGISTERED_EXTENSION:
 			writeContainer((TWPContainer) p.getValue());
 			break;
 		case NO_VALUE:
 			writer.writeByte(1);
 			break;
 		case RESERVED:
 			break;
 		case APPLICATION_TYPE:
 			writeApplicationType((TWPContainer) p.getValue());
 			break;
 		}
 	}
 	
 	public void writeMessage(Message message) throws IOException {
 		// TODO: reconnect if protocol is different
 		writeMessageId(message.getType());
 		for (Parameter p:message.getParameters()) {
 			writeParameter(p);
 		}
 		writeEndOfMessage();
 	}
 	
 	public void disconnect() throws IOException {
 		if (listener != null && listener.isAlive())
 			listener.disconnect();
 		socket.close();
 	}
 
 	public boolean readMagicBytes() throws IOException {
 		byte[] bytes = new byte[5];
 		reader.readFully(bytes);
 		return MAGIC_BYTES.equals(new String(bytes));
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
 		int message = reader.readUnsignedByte();
 		return message - 4;
 	}
 
 	// TODO: registered extensions
 	public void writeMessageId(int i) throws IOException {
 		writer.write(i + 4);
 	}
 	
 
 	public byte[] readByte() throws IOException {
 		int tag = reader.readUnsignedByte();
 		return readByte(tag);
 	}
 	
 	public byte[] readByte(int tag) throws IOException {
 		int length = 0;
 		if (tag == 15)
 			length = reader.readUnsignedByte();
 		else if (tag == 16)
 			length = reader.readInt();
 		byte[] value = new byte[length];
 		reader.readFully(value);
 		return value;
 	}
 	
 	public void writeByte(byte[] value) throws IOException {
 		if (value.length > 255) {
 			writer.write(15);
 			writer.writeByte(value.length);
 		} else {
 			writer.write(16);
 			writer.writeInt(value.length);
 		}
 		writer.write(value);
 	}
 		
 	public int readInteger() throws IOException {
 		int type = reader.readUnsignedByte();
 		return readInteger(type);
 	}
 	
 	// TODO: throw error if type != SHORT OR LONGINT
 	public int readInteger(int type) throws IOException {
 		int value = 0;
 		if (type == 13)
 			value = reader.readByte();
 		else if (type == 14)
 			value = reader.readInt();
 		return value;
 	}
 	
 	public void writeInteger(int value) throws IOException {
 		if (-128 <= value && value <= 127) {
 			writer.write(13);
 			writer.write(value);
 		} else {
 			writer.write(14);
 			writer.writeInt(value);
 		}
 	}
 
 	public String readString() throws IOException {
 		int type = reader.readUnsignedByte();
 		return readString(type);
 	}
 	
 	public String readString(int type) throws IOException {
 		int length = type == 127 ? reader.readInt() : type - 17;
 		byte[] value = new byte[length];
 		reader.read(value);
 		return new String(value, "UTF-8");
 	}
 
 	public void writeString(String string) throws IOException {
 		byte[] bytes = string.getBytes("UTF-8");
 		int type = bytes.length > 109 ? 127 : bytes.length + 17;
 		writer.write(type);
 		if (type == 127)
 			writer.writeInt(bytes.length);
 		writer.write(bytes);
 	}
 
 	public boolean readEndOfMessage() throws IOException {
 		int tag = reader.readUnsignedByte();
 		return tag == 0;
 	}
 
 	public void writeEndOfMessage() throws IOException {
 		writer.write(0);
 	}
 
 	
 	public static ParameterType getParameterType(int id) {
 		ParameterType type = null;
 		if (id == 0)
 			type = ParameterType.END_OF_CONTENT;
 		else if (id == 1)
 			type = ParameterType.NO_VALUE;
 		else if (id == 2)
 			type = ParameterType.STRUCT;
 		else if (id == 3)
 			type = ParameterType.SEQUENCE;
 		else if (4 <= id && id <= 11)
 			// TODO: message alternative
 			type = ParameterType.UNION;
 		else if (id == 12)
 			type = ParameterType.REGISTERED_EXTENSION;
 		else if (id == 13)
 			type = ParameterType.SHORT_INTEGER;
 		else if (id == 14)
 			type = ParameterType.LONG_INTEGER;
 		else if (id == 15)
 			type = ParameterType.SHORT_BINARY;
 		else if (id == 16)
 			type = ParameterType.LONG_BINARY;
 		else if (17 <= id && id <= 126)
 			type = ParameterType.SHORT_STRING;
 		else if (id == 127)
 			type = ParameterType.LONG_STRING;
 		else if (128 <= id && id <= 159)
 			type = ParameterType.RESERVED;
 		else if (160 <= id && id <= 255)
 			type = ParameterType.APPLICATION_TYPE;
 		return type;
 	}
 }
