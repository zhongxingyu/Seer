 package storagecontroller;
 
 import data.TaskArrayList;
 import data.TaskHashMap;
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 

 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 public class FileHandler 
 {
 	
 	private static Logger logger = Logger.getLogger(FileHandler.class);
 	private static String fileName;
 	/** Constructor to set the filename 
 	 * 
 	 * @param name name of the file to which xml encoder writes and from which xml decoder reads
 	 */
 	public FileHandler(String name)
 	{
 		fileName=name;
 	}
 	/** function to write to the file with name as fileName 
 	 * 
 	 * @param instance the TaskHashMap instance. Is also the live storage.
 	 * @return true if written to the file without any errors, otherwise false
 	 */
 	public boolean writeToFile(TaskHashMap instance) 
 	{
 		try
 		{
 		BufferedOutputStream xmlOut=new BufferedOutputStream(new FileOutputStream(fileName));
 		XMLEncoder writeToXml=new XMLEncoder(xmlOut);
 		logger.debug(instance.getKeySet().size());
 		for(String key: instance.getKeySet())
 		{
 			writeToXml.writeObject(instance.getTaskById(key));
 		}
 		writeToXml.close();
 		return true;
 		}
 		catch(FileNotFoundException e)
 		{
 			System.out.println("File Not Found!");
 			return false;
 		}
 	}
 	/** function to write to the file with name as fileName 
 	 * 
 	 * @param instance the TaskHashMap instance. Is also the live storage.
 	 * @return true if read from the file without any errors, otherwise false.
 	 */
 	public boolean readFromFile(TaskHashMap instance) 
 	{
 		try
 		{
 		BufferedInputStream xmlIn=new BufferedInputStream(new FileInputStream(fileName));
 		XMLDecoder readFromXml=new XMLDecoder(xmlIn);
 			while(true)
 			{
				Object obj=readFromXml.readObject();
				TaskArrayList.addTask(obj);
				
 			}
 		}
 		catch(FileNotFoundException e)
 		{
 			logger.debug("File Not Found!");
 			return false;
 		}
 		catch(ArrayIndexOutOfBoundsException e)
 		{
 			logger.debug("array out of bounds!");
 			return false;
 		}
 		catch(NullPointerException e)
 		{
 			logger.debug("null pointer exception");
 			return false;
 		}
 	}
 }
