 
 package org.kryomq.kryonet;
 
 import java.nio.ByteBuffer;
 
 import org.kryomq.kryo.Kryo;
 import org.kryomq.kryo.io.ByteBufferInputStream;
 import org.kryomq.kryo.io.ByteBufferOutputStream;
 import org.kryomq.kryo.io.Input;
 import org.kryomq.kryo.io.Output;
 import org.kryomq.kryonet.FrameworkMessage.DiscoverHost;
 import org.kryomq.kryonet.FrameworkMessage.KeepAlive;
 import org.kryomq.kryonet.FrameworkMessage.Ping;
 import org.kryomq.kryonet.FrameworkMessage.RegisterTCP;
 import org.kryomq.kryonet.FrameworkMessage.RegisterUDP;
 
 public class KryoSerialization implements Serialization {
 	private final Kryo kryo;
 	private final Input input;
 	private final Output output;
 	private final ByteBufferInputStream byteBufferInputStream = new ByteBufferInputStream();
 	private final ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream();
 
 	public KryoSerialization () {
 		this(new Kryo());
 		kryo.setReferences(false);
 		kryo.setRegistrationRequired(true);
 	}
 
 	public KryoSerialization (Kryo kryo) {
 		this.kryo = kryo;
 
 		kryo.register(RegisterTCP.class);
 		kryo.register(RegisterUDP.class);
 		kryo.register(KeepAlive.class);
 		kryo.register(DiscoverHost.class);
 		kryo.register(Ping.class);
 
		input = new Input(byteBufferInputStream, 512);
		output = new Output(byteBufferOutputStream, 512);
 	}
 
 	public Kryo getKryo () {
 		return kryo;
 	}
 
 	public synchronized void write (Connection connection, ByteBuffer buffer, Object object) {
 		byteBufferOutputStream.setByteBuffer(buffer);
		kryo.getContext().put("connection", connection);
 		kryo.writeClassAndObject(output, object);
 		output.flush();
 	}
 
 	public synchronized Object read (Connection connection, ByteBuffer buffer) {
 		byteBufferInputStream.setByteBuffer(buffer);
		kryo.getContext().put("connection", connection);
 		return kryo.readClassAndObject(input);
 	}
 
 	public void writeLength (ByteBuffer buffer, int length) {
 		buffer.putInt(length);
 	}
 
 	public int readLength (ByteBuffer buffer) {
 		return buffer.getInt();
 	}
 
 	public int getLengthLength () {
 		return 4;
 	}
 }
