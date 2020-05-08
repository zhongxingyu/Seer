 {% set package, java_subpackage = "common", "common" %}
 package {{ java_package }};
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 public class MessageFactory {
 	
 	private ByteArrayOutputStream ba_out;
 	private DataOutputStream out;
 	private MessageType mainMessage;
 	
 	public MessageFactory(MessageType mainMessage) {
 		this.ba_out = new ByteArrayOutputStream();
 		this.out = new DataOutputStream(ba_out);
 		this.mainMessage = mainMessage;
 	}
 	
 	public synchronized byte[] composeMessage(Object... args) {
 		composeMessage(mainMessage, args);
 		/*
 		 * Get the composed message as byte[]
 		 */
 		byte[] message = ba_out.toByteArray();
 		ba_out.reset();
 		/*
 		 * Get the length of the message as a 4-byte integer
 		 */
 		try {
 			out.writeInt(message.length);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		byte[] lengthPrefix = ba_out.toByteArray();
 		ba_out.reset();
 		/*
 		 * Compose the complete message with the length prefixed
 		 */
 		int lpf = lengthPrefix.length;
 		byte[] result = new byte[message.length+lpf];
 		for (int i = 0; i < lpf; i++) {
 			result[i] = lengthPrefix[i];
 		}
 		for (int i = 0; i < message.length; i++) {
 			result[i+lpf] = message[i];
 		}
 		return result;
 	}
 	
 	/**
 	 * Compose a message (and possible submessages)
 	 * @param message
 	 * @param args
 	 */
 	private void composeMessage(MessageType message, Object... args) {
 		int objectIndex = 0;
 		Object o;
 		ParameterType pt;
 		/*
 		 * Send the parameters first
 		 */
 //		System.out.println( String.format("composeMessage(%s)", message.getName() ) );
 		for (int i = 0; i < message.getParameterTypes().length; i++) {
 			o = args[objectIndex++];
 			pt = message.getParameterTypes()[i];
 			if (!matchType( pt.getType(), o ) ) {
 				throw new RuntimeException( String.format( "Message %s expects parameter #%d to be of DataType %s, but was java type %s", message.getName(), i, pt.getType(), o.getClass().getName() ) );
 			}
 //			System.out.println( String.format(" parameter #%d: %s", i+1, o ) );
 			writeParameter(pt.getType(), o);
 		}
 		
 		/*
 		 * Then possibly a submessage
 		 */
 		if (message.getMessageCount() > 0) {
 			o = args[objectIndex++];
 			if (!matchType( DataType.String, o )) {
 				throw new RuntimeException( String.format( "Message %s expects parameter #%d to be name of submessage, but was java type %s", message.getName(), objectIndex, o.getClass().getName() ) );
 			}
 			String name = (String)o;
 			if (!message.getMessagesByName().containsKey(name)) {
 				throw new RuntimeException( String.format( "Message %s does not contain a submessage named '%s'", message.getName(), name ) );
 			}
 			MessageType subMessage = message.getMessagesByName().get(name);
 //			System.out.println( String.format(" submessage '%s': %d", name, subMessage.getId() ) );
 			writeParameter(DataType.Int, subMessage.getId());
 			Object[] subArgs = new Object[args.length-objectIndex];
 			for (int i = objectIndex; i < args.length; i++) {
 				subArgs[i-objectIndex] = args[i];
 			}
 			composeMessage(subMessage, subArgs);
 		}
 	}
 	
 	private boolean matchType(DataType datatype, Object value) {
 		switch (datatype) {
 			case Byte: return value instanceof Byte;
 			case Int: return value instanceof Integer;
 			case IntList: return value instanceof int[];
 			case Float: return value instanceof Float;
 			case String: return value instanceof String;
 			case StringList: return value instanceof String[];
 			case Binary: return value instanceof byte[];
 			default: return false;
 		}
 	}
 	
 	private void writeParameter(DataType datatype, Object value) {
 		try {
 			switch (datatype) {
 				case Byte:
 					out.writeByte( (Byte) value );
 					break;
 				case Int: 
 					out.writeInt( (Integer) value );
 					break;
 				case IntList: 
 					writeIntList( (int[]) value );
 					break;
 				case Float: 
 					out.writeFloat( (Float) value );
 					break;
 				case String: 
 					out.writeUTF( (String) value );
 					break;
 				case StringList:
 					writeStringList( (String[]) value );
 					break;
 				case Binary:
					out.write( (byte[]) value );
 					break;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 	
 	private void writeIntList(int[] value) throws IOException {
 		out.writeInt( value.length );
 		for (int i = 0; i < value.length; i++)
 			out.writeInt( value[i] );
 	}
 	
 	private void writeStringList(String[] value) throws IOException {
 		out.writeInt( value.length );
 		for (int i = 0; i < value.length; i++)
 			out.writeUTF( value[i] );
 	}
 	
}
