 package li.rudin.arduino.core.test;
 
 import java.util.List;
 
 import li.rudin.arduino.core.util.Message;
 import li.rudin.arduino.core.util.MessageParser;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class MessageParserTest
 {
 	
 	@Test
 	public void testAssemble()
 	{
 		Assert.assertEquals("x:y;", new Message("x", "y").toString());
 	}
 	
 	@Test
 	public void testDisassembleSingle()
 	{
 		List<Message> list = MessageParser.parse("x:y;");
 		
 		Assert.assertEquals(1, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals("y", list.get(0).value);
 	}
 
 	@Test
	public void testDisassembleKeyOnly()
	{
		List<Message> list = MessageParser.parse("x:;");
		
		Assert.assertEquals(1, list.size());
		
		Assert.assertEquals("x", list.get(0).key);
		Assert.assertEquals("", list.get(0).value);
	}

	@Test
 	public void testDisassembleMultiple()
 	{
 		List<Message> list = MessageParser.parse("x:y;1:2;");
 		
 		Assert.assertEquals(2, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals("y", list.get(0).value);
 		
 		Assert.assertEquals("1", list.get(1).key);
 		Assert.assertEquals("2", list.get(1).value);
 	}
 
 	@Test
 	public void testDisassembleMultipleWithInvalid()
 	{
 		List<Message> list = MessageParser.parse("x:y;xxx;1:2;");
 		
 		Assert.assertEquals(2, list.size());
 		
 		Assert.assertEquals("x", list.get(0).key);
 		Assert.assertEquals("y", list.get(0).value);
 		
 		Assert.assertEquals("1", list.get(1).key);
 		Assert.assertEquals("2", list.get(1).value);
 	}
 
 	@Test
 	public void testDisassembleEmpty()
 	{
 		List<Message> list = MessageParser.parse("");
 		
 		Assert.assertEquals(0, list.size());
 		
 	}
 
 }
