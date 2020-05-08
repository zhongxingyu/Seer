 package ussr.builder.saveLoadXML;
 
 /**
  * Supports saving and loading of data in XML format.
 * Is  based on SAX and DOM. Follows design pattern called TEMPLATE METHOD.
  * @author Konstantinas
  */
 public interface SaveLoadXMLFileTemplate {
 	
 	/**	
 	 * Saves the data about simulation in chosen XML format file.
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.
 	 * @param fileDirectoryName, the name of directory, like for example: "C:/newXMLfile". 
 	 */
 	public void saveXMLfile(String fileDirectoryName);
 		
 	/**  
 	 * Loads the data about simulation from chosen XML file into simulation.
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.
 	 * @param fileDirectoryName, the name of directory, like for example: "C:/newXMLfile".	 
 	 */
 	public void loadXMLfile(String fileDirectoryName);
 	
 	/**
 	 * Method for defining the format of reading the data from XML file.  In other words
 	 * what to read from the file into simulation.
 	 *  This method is so-called "Primitive operation" for above TEMPLATE method, called "loadXMLfile(String fileDirectoryName)". 	  
 	 * @param document,DOM object of document. 
 	 */
 	//public abstract void loadInXML(Document document);	
 	
 	/**
 	 * Method for defining the format of XML to print into the xml file. In other words
 	 * what to save in the file about simulation.
 	 * This method is so-called "Primitive operation" for above TEMPLATE method, called "saveXMLfile(String fileDirectoryName)". 	  
 	 * @param transformerHandler,the content handler used to print out XML format. 
 	 */
 	//public abstract void printOutXML(TransformerHandler transformerHandler);
 }
