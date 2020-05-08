 /**
  * 
  */
 package work;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import javax.xml.bind.JAXBElement;
 
 import work.marshalling.WMarshalling;
 import work.marshalling.WMarshallingAbstract;
 import work.marshalling.WMarshalling;
 
 /**
  * @author Tomasz.Switek
  *
  */
 public class ExpFkQueryDataMarshalling extends WMarshallingAbstract<ExpFkXmlQueryXO>
 									 implements WMarshalling<ExpFkXmlQueryXO> 
 {
 	@Override
 	protected JAXBElement<ExpFkXmlQueryXO> getRootElement(ExpFkXmlQueryXO rootContent) 
 	{
 		return new ObjectFactory().createRoot(rootContent);
 	}
 	
 	public ExpFkXmlQueryXO loadAndUnmarshall(String fileName) throws WMarshallingException, URISyntaxException
 	{
 		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     	URL url = classLoader.getResource(fileName);
 			
 		File file = new File(url.toURI());
 		String fileContent = null;
 		FileReader fileReader;
 	
 		try 
 		{
 			fileReader = new FileReader(file);
 			fileContent = loadFileContent(fileReader);
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 		
 		return this.unmarshall(fileContent);
 	}
 	
 	private String loadFileContent(FileReader file) throws IOException
 	{
 	    BufferedReader reader = new BufferedReader(file);
 	    String         line = null;
 	    StringBuilder  stringBuilder = new StringBuilder();
 	    String         ls = System.getProperty("line.separator");
 
 	    while( ( line = reader.readLine() ) != null ) {
 	        stringBuilder.append( line );
 	        stringBuilder.append( ls );
 	    }
 
 	    return stringBuilder.toString();
 	}
 }
