 package se.l4.aurochs.config.internal;
 
 import java.io.File;
 import java.io.IOException;
 
 import se.l4.aurochs.serialization.Serializer;
 import se.l4.aurochs.serialization.format.StreamingInput;
 import se.l4.aurochs.serialization.format.StreamingInput.Token;
 import se.l4.aurochs.serialization.format.StreamingOutput;
 
 /**
  * Serializer for {@link File}.
  * 
  * @author Andreas Holstenson
  *
  */
 public class FileSerializer
 	implements Serializer<File>
 {
 	private final File root;
 
 	public FileSerializer(File root)
 	{
 		this.root = root;
 	}
 	
 	@Override
 	public File read(StreamingInput in)
 		throws IOException
 	{
 		in.next(Token.VALUE);
 		
 		String file = in.getString();
		File current = root;
		for(String part : file.split(File.separator))
 		{
			current = new File(current, part);
 		}
		
		return current;
 	}
 
 	@Override
 	public void write(File object, String name, StreamingOutput stream)
 		throws IOException
 	{
 	}
 
 }
