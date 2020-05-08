 package sk.stuba.fiit.perconik.preferences;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 final class Serialization
 {
 	private Serialization()
 	{
 		throw new AssertionError();
 	}
 	
 	static final Object readFromString(final String s) throws IOException, ClassNotFoundException
 	{
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(s.getBytes()))
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
				objects.flush();
 			}
 			
			return bytes.toString();
 		}
 	}
 }
