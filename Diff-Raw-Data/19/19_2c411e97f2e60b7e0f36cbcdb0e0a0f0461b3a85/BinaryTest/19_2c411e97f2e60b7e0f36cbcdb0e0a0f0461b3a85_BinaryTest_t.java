 package se.l4.aurochs.serialization.format;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static se.l4.aurochs.serialization.format.StreamingInput.Token.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.base.Charsets;
 
 /**
  * Tests for the binary format. Tests by first writing some values and then
  * checking that it is possible to read the serialized stream.
  * 
  * @author Andreas Holstenson
  *
  */
 public class BinaryTest
 {
 	private ByteArrayOutputStream out;
 	private BinaryOutput output;
 	
 	@Test
 	public void emptyObject()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, OBJECT_END);
 	}
 	
 	@Test
 	public void emptyList()
 		throws IOException
 	{
 		output.writeListStart("");
 		output.writeListEnd("");
 		
 		assertStream(LIST_START, LIST_END);
 	}
 	
 	@Test
 	public void singleNull()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.writeNull("name");
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", null);
 	}
 	
 	@Test
 	public void singleInt()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("name", 12);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", 12);
 	}
 	
 	@Test
 	public void singleLong()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("name", 12l);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", 12l);
 	}
 	
 	@Test
 	public void singleNegativeLong()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("name", -12l);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", -12l);
 	}
 	
 	@Test
 	public void singleBoolean()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("name", true);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", true);
 	}
 	
 	@Test
 	public void singleFloat()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("name", 3.14f);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", 3.14f);
 	}
 	
 	@Test
 	public void singleDouble()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("name", 89765.0);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", 89765.0);
 	}
 	
 	@Test
 	public void singleString()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("name", "string");
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", "string");
 	}
 	
 	@Test
 	public void singleByteArray()
 		throws IOException
 	{
 		byte[] in = "kaka".getBytes(Charsets.UTF_8);
 		output.writeObjectStart("");
 		output.write("name", in);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, OBJECT_END);
 		assertStreamValues("name", in);
 	}
 	
 	@Test
 	public void severalObjectValues()
 		throws IOException
 	{
 		output.writeObjectStart("");
 		output.write("key1", "value1");
 		output.write("key2", 12);
 		output.writeObjectEnd("");
 		
 		assertStream(OBJECT_START, KEY, VALUE, KEY, VALUE, OBJECT_END);
 		assertStreamValues("key1", "value1", "key2", 12);
 	}
 	
 	@Test
 	public void listWithSeveralValues()
 		throws IOException
 	{
 		output.writeListStart("");
 		output.write("entry", "value");
 		output.write("entry", 74749);
 		output.writeListEnd("");
 		
 		assertStream(LIST_START, VALUE, VALUE, LIST_END);
 		assertStreamValues("value", 74749);
 	}
 	
	@Test
	public void largeLong()
		throws IOException
	{
		output.writeListStart("");
		output.write("entry", 1324475548554l);
		output.writeListEnd("");
		
		assertStream(LIST_START, VALUE, LIST_END);
		assertStreamValues(1324475548554l);
	}
	
 	@Before
 	public void beforeTest()
 	{
 		out = new ByteArrayOutputStream();
 		output = new BinaryOutput(out);
 	}
 	
 	protected StreamingInput createInput()
 	{
 		return new BinaryInput(new ByteArrayInputStream(out.toByteArray()));
 	}
 	
 	/**
 	 * Assert that the stream contains the specified tokens.
 	 * 
 	 * @param in
 	 * @param tokens
 	 * @throws IOException
 	 */
 	protected void assertStream(StreamingInput.Token... tokens)
 		throws IOException
 	{
 		StreamingInput in = createInput();
 		int i = 0;
 		while(in.peek() != null)
 		{
 			StreamingInput.Token t = in.next();
 			if(i == tokens.length)
 			{
 				fail("Did not expect more tokens, but got " + t);
 			}
 			else if(t != tokens[i++])
 			{
 				fail("Token at " + (i-1) + " was expected to be " + tokens[i-1] + ", but found " + t);
 			}
 		}
 		
 		if(i < tokens.length)
 		{
 			fail("Did not read all tokens, expected " + tokens.length + " but only read " + i);
 		}
 	}
 	
 	/**
 	 * Assert that KEY and VALUE tokens contain the specified values.
 	 * 
 	 * @param in
 	 * @param values
 	 * @throws IOException
 	 */
 	protected void assertStreamValues(Object... values)
 		throws IOException
 	{
 		StreamingInput in = createInput();
 		int i = 0;
 		while(in.peek() != null)
 		{
 			StreamingInput.Token t = in.next();
 			switch(t)
 			{
 				case KEY:
 				case VALUE:
 					if(i == values.length)
 					{
 						fail("Did not expect more values, but got " + in.getValue());
 					}
 					
 					assertThat(values[i++], is(in.getValue()));
 					break;
 			}
 		}
 		
 		if(i < values.length)
 		{
 			fail("Did not read all values, expected " + values.length + " but only read " + i);
 		}
 	}
 }
