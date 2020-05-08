 package wIRC;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.util.ArrayList;
 
 /**
  * Plug-in loading library
  * <br><br>
  * This class is responsible for the translation 
  * of a file path (remote or local) to a useable 
  * Class of unknown type which may cast later on.
  * <br><br>
  * @author 	wisteso@gmail.com
  */
 public class PlugInLoader extends ClassLoader
 {
 	public Class<?> findClass(String name)
 	{
 	    byte[] b = loadClassData(name);
 	    
 	    if (b != null)
 	    {
 	    	try
 	    	{
 	    		Class<?> x = defineClass(null, b, 0, b.length);
 	    		
 	    		if (x != null)
 			    	return x;
 	    	}
 	    	catch (java.lang.ClassFormatError e)
 	    	{
 	    		System.err.println(e.toString());
 	    	}
 	    }
 	    
 	    return null;
 	}
 	
 	private byte[] loadClassData(String path)
 	{
 		try
 		{
 			BufferedInputStream in;
 			
 			if (path.indexOf("http://") == 0)
 			{
 				in = new BufferedInputStream(new URL(path).openConnection().getInputStream());
 			}
 			else
 			{
				in = new BufferedInputStream(new FileInputStream(new File(path)));
 			}
 			
 			ArrayList<Byte> out = new ArrayList<Byte>();
 			
 			byte b = (byte)in.read();
 
 			while (b != -1)
 			{
 				out.add(b);
 				b = (byte)in.read();
 			}
 			
 			byte[] data = new byte[out.size()];
 			
 			for (int i = 0; i < out.size(); ++i)
 				data[i] = out.get(i);
 			
 			return data;
 		}
 		catch (Exception e)
 		{
 			System.err.println(e.toString());
 		}
 		
 		return null;
     }
 }
