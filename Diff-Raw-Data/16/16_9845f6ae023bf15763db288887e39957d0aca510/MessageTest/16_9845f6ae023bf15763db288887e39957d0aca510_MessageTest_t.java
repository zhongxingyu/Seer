 package mon4h.common.models;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Map;
 
import junit.framework.TestCase;

 import org.junit.Test;
 
public class MessageTest extends TestCase {
 
 	@Test
 	public void testMessageReadAndWrite() throws Exception {
 		Message msg = new Message("type1");
 		
 		Map<String, String> headers = msg.getAdditionalHeaders();
 		headers.put("header1", "value1");
 		headers.put("header2", "value2");
 		
 		byte[] body = new byte[128];
 		for (int i = 0; i < body.length; ++i) {
 			body[i] = (byte)i;
 		}
 		
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(bos);
 		oos.writeObject(msg);
 		oos.flush();
 		
 		byte[] stream = bos.toByteArray();
 
 		ByteArrayInputStream bis = new ByteArrayInputStream(stream);
 		ObjectInputStream ois = new ObjectInputStream(bis);
 
 		Message newMsg = (Message)ois.readObject();
		assertTrue("msg.getType() not equal to newMsg.getType(), new type = " + newMsg.getType() , msg.getType().equals(newMsg.getType()));
 		assert(msg.getAdditionalHeaders().size() == newMsg.getAdditionalHeaders().size());
 		assert(msg.getAdditionalHeaders().get("header1") == "value1");
 		assert(msg.getAdditionalHeaders().get("header2") == "value2");
 		assert(msg.getBody().length == newMsg.getBody().length);
 		byte[] newBody = newMsg.getBody();
 		for (int i = 0; i < newBody.length; ++i) {
 			assert(newBody[i] == i);
 		}
 	}
 }
