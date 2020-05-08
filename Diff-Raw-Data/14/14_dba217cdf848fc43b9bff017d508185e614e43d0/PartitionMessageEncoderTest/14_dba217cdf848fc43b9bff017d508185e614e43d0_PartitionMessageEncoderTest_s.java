 import java.io.ByteArrayOutputStream;
 import java.nio.ByteBuffer;
 import java.util.UUID;
 
 import net.geertvos.theater.api.actors.ActorId;
 import net.geertvos.theater.core.actor.ActorIdImpl;
 import net.geertvos.theater.core.networking.PartitionMessage;
 import net.geertvos.theater.core.networking.PartitionMessageDecoder;
 import net.geertvos.theater.core.networking.PartitionMessageEncoder;
 import net.geertvos.theater.core.serialization.UUIDSerializer;
 
 import org.testng.annotations.Test;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.io.ByteBufferInputStream;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 
 public class PartitionMessageEncoderTest {
 
 	@Test( groups = "unitTest")
 	public void testEncoder() throws Exception {
 		PartitionMessageEncoder encoder = new PartitionMessageEncoder();
 		PartitionMessageDecoder decoder = new PartitionMessageDecoder();
 		
 		ActorId from = new ActorIdImpl(UUID.randomUUID(), "this");
 		ActorId to = new ActorIdImpl(UUID.randomUUID(), "this");
		PartitionMessage message = new PartitionMessage(1,1L,from,to);
 		message.setParameter("test", "value");
 		
 		Object o = encoder.encode(null, null, message);
 		System.out.println(o);
 		decoder.decode(null, null, o);
 	}
 	
 	@Test( groups = "unitTest")
 	public void extraTest() {
 		Kryo kryo = new Kryo();
 		kryo.addDefaultSerializer(UUID.class, UUIDSerializer.class);
 		ActorId from = new ActorIdImpl(UUID.randomUUID(), "this");
 		ActorId to = new ActorIdImpl(UUID.randomUUID(), "this");
		PartitionMessage message = new PartitionMessage(1,1L,from,to);
 		message.setParameter("test", "value");
 
         Output out = new Output(4096);
         kryo.writeObject(out, message);
         ByteBuffer buffer = ByteBuffer.wrap(out.getBuffer());
 		buffer.rewind();
 		ByteBufferInputStream inStream = new ByteBufferInputStream(buffer);
 		Input in = new Input(inStream);
 		PartitionMessage m2 = kryo.readObject(in, PartitionMessage.class);
 	}
 	
 }
