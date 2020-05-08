 package sk.stuba.fiit.perconik.preferences;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
import com.google.common.base.Charsets;
 
 final class Serialization
 {
	private static final String charset = Charsets.UTF_8.name();
	
 	private Serialization()
 	{
 		throw new AssertionError();
 	}
 	
 	static final Object readFromString(final String s) throws IOException, ClassNotFoundException
 	{
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(s.getBytes(charset)))
 		{
 			try (ObjectInputStream objects = new ObjectInputStream(bytes))
 			{
 				return objects.readObject();
 			}
 		}
 	}
 	
 	static final String writeToString(final Object o) throws IOException
 	{
 		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream())
 		{
 			try (ObjectOutputStream objects = new ObjectOutputStream(bytes))
 			{
 				objects.writeObject(o);
 			}
 			
			return bytes.toString(charset);
 		}
 	}
 }
