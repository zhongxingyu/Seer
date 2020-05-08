 package au.edu.labshare.schedserver.scormpackager.lila;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 //Needed for XML generation
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.Namespace;
 import javax.xml.stream.events.StartDocument;
 import javax.xml.stream.events.StartElement;
 
 //Needed for constructing the Manifest in object format
 import au.edu.labshare.schedserver.scormpackager.lila.Manifest;
 import au.edu.labshare.schedserver.scormpackager.manifest.Item;
 import au.edu.labshare.schedserver.scormpackager.manifest.Organization;
 import au.edu.labshare.schedserver.scormpackager.manifest.Resource;
 
 public class ManifestXMLDecorator 
 {	
 	//TODO Should place in properties file
 	private static final String SCO_INSTITUTION = "UTS";
 	
 	//The name of the manifest. This should never change
 	public static final String MANFEST_NAME = "imsmanfest.xml";
 	
 	//Properties that are associated with the imsmanifest file
 	public static final String MANIFEST_NODE_NAME = "manifest";	
 	public static final String MANIFEST_ORG_NODE_NAME = "organizations";
 	public static final String MANIFEST_ORG_ELEM_NAME = "organization";
 	public static final String MANIFEST_TITLE = "title";
 	public static final String MANIFEST_ITEM = "item";
 	public static final String MANIFEST_RESOURCES_NODE_NAME = "resources";
 	public static final String MANIFEST_RESOURCE_ELEM_NAME = "resource";
 	public static final String MANIFEST_FILE_ELEM_NAME = "file";
 	public static final String MANIFEST_DEPENDENCY_ELEM_NAME = "dependency";
 	public static final String MANIFEST_SCORMTYPE_SCO = "sco";
 	public static final String MANIFEST_SCORMTYPE_ASSET = "asset";
 	
 	/**
 	 * Generates the lmsmanifest.xml file. This is not going to be public
 	 * 
 	 * Based on: http://www.vogella.de/articles/JavaXML/article.html
 	 * @param Manifest. This param is compulsory in order to decorate the Manifest Object. 
 	 */
 	public String decorateManifest(Manifest manifest, String outputFilePath)
 	{
 		XMLEventFactory eventFactory = null;
 		XMLEventWriter  eventWriter  = null;
 			
 		//Generate the manifest file imsmanifest.xml
 		try
 		{
 			// Create a XMLOutputFactory
 			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
 			
 			// Create XMLEventWriter
 			eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(outputFilePath + MANFEST_NAME));
 			
 			// Create a EventFactory
 			eventFactory = XMLEventFactory.newInstance();
 			
 			// Create and write Start Tag
 			StartDocument startDocument = eventFactory.createStartDocument();
 			eventWriter.add(startDocument);
 			
 			//Invoke the following to generate the imsmanifest.xml file:
 			//1. decorateManifestHeader()
 			//2. decorateOrganizations()
 			//3. decoreateResources()
 			decorateManifestHeader(manifest, eventFactory, eventWriter);
 			decorateOrganizations(manifest, eventFactory, eventWriter);
 			decorateResources(manifest, eventFactory, eventWriter);
 			
 			//Close off the XML Manifest node
 			eventWriter.add(eventFactory.createEndElement("", "", MANIFEST_NODE_NAME));
 			eventWriter.flush();
 			eventWriter.add(eventFactory.createEndDocument());
 			eventWriter.close();		
 		}
 		catch(XMLStreamException e)
 		{
 			e.printStackTrace(); //TODO replace with SchedServer Logger.
 		} 
 		catch (FileNotFoundException e) 
 		{
 			e.printStackTrace(); //TODO replace with SchedServer Logger.
 		}
 		
		return null; //TODO Need to actually return the path of the file in question. 
 	}
 	
 	private void decorateManifestHeader(Manifest manifest, XMLEventFactory eventFactory, XMLEventWriter eventWriter)
 	{
 		ArrayList<Attribute> attributeList = null;
 		ArrayList<Namespace> namespaceList = null;
 		
 		// Create manifest node name open tag with relevant attributes	
 		attributeList = new ArrayList<Attribute>();
 		namespaceList = new ArrayList<Namespace>();
 		attributeList.add(eventFactory.createAttribute("identifier", manifest.getMetaData().getSchemaValue("identifier")));
 		attributeList.add(eventFactory.createAttribute("version", manifest.getMetaData().getSchemaValue("version")));
 		attributeList.add(eventFactory.createAttribute("xmlns", manifest.getMetaData().getSchemaValue("xmlns")));
 		attributeList.add(eventFactory.createAttribute("xmlns:adlcp", manifest.getMetaData().getSchemaValue("xmlns:adlcp")));
 		attributeList.add(eventFactory.createAttribute("xmlns:xsi",manifest.getMetaData().getSchemaValue("xmlns:xsi")));
 		attributeList.add(eventFactory.createAttribute("xsi:schemalocation", manifest.getMetaData().getSchemaValue("xsi:schemalocation")));
 		namespaceList.add(eventFactory.createNamespace(Manifest.NAMESPACE));
 	    StartElement manifestStartElem = eventFactory.createStartElement("", "",  MANIFEST_NODE_NAME, attributeList.iterator(), namespaceList.iterator());
 	    try 
 	    {
 			eventWriter.add(manifestStartElem);
 			eventWriter.flush();
 		} 
 	    catch (XMLStreamException e) 
 	    {
 			e.printStackTrace();	// TODO Replace with SchedServer Logger 
 		}
 	}
 	
 	private void decorateOrganizations(Manifest manifest, XMLEventFactory eventFactory, XMLEventWriter eventWriter)
 	{
 		int i = 0;
 		ArrayList<Attribute> attributeList = null;
 		ArrayList<Namespace> namespaceList = null;
 				
 		// Create Organization Manifest component information
 		// Create the attribute default="" for organizations node
 		attributeList = new ArrayList<Attribute>();
 		namespaceList = new ArrayList<Namespace>();
 		attributeList.add(eventFactory.createAttribute("default", SCO_INSTITUTION));
 		namespaceList.add(eventFactory.createNamespace(Manifest.NAMESPACE));
 		StartElement organizationsStartElem = eventFactory.createStartElement("", "",  MANIFEST_ORG_NODE_NAME, attributeList.iterator(), namespaceList.iterator());
 	    try 
 	    {
 			eventWriter.add(organizationsStartElem); // Write the organizations start node to file
 			eventWriter.flush();
 		} 
 	    catch (XMLStreamException e) 
 	    {
 			e.printStackTrace(); // TODO Replace with SchedServer Logger 
 		}
 
 		//TODO Add each organization
 		for(Iterator<Organization> iterOrg = manifest.getOrganizations().iterator(); iterOrg.hasNext();)
 		{
 			// Create the organization element node
 			attributeList = new ArrayList<Attribute>();
 			attributeList.add(eventFactory.createAttribute("identifier", SCO_INSTITUTION));
 			StartElement organizationStartElem = eventFactory.createStartElement("", "",  MANIFEST_ORG_ELEM_NAME, attributeList.iterator(), namespaceList.iterator());
 		    try 
 		    {
 				eventWriter.add(organizationStartElem); // Write the <organization> start elem to file
 				eventWriter.flush();
 			} 
 		    catch (XMLStreamException e) 
 		    {
 				e.printStackTrace(); // TODO Replace with SchedServer Logger 
 			}
 		    
 			//Get the actual organization object to obtain information
 			Organization organization = iterOrg.next();
 		
 			//Decorate the <title></title> nodes
 			createNode(eventWriter, MANIFEST_TITLE, organization.getTitle());
 			
 			//TODO Need to extract the title from the <item identifierref> atttribute.
 		    for(Iterator<Item> iterItem = organization.getItemList().iterator(); iterItem.hasNext();)
 		    {
 		    	Item orgItem = iterItem.next();
 		    	attributeList = new ArrayList<Attribute>();
 				attributeList.add(eventFactory.createAttribute("identifier", "item" + Integer.toString(i)));
 				attributeList.add(eventFactory.createAttribute("identifierref", orgItem.getReference())); 
 				StartElement itemStartElem = eventFactory.createStartElement("", "",  MANIFEST_ITEM, attributeList.iterator(), namespaceList.iterator());
 				try 
 			    {
 					eventWriter.add(itemStartElem);
 					eventWriter.flush();
 				} 
 			    catch (XMLStreamException e) 
 			    {
 					e.printStackTrace(); // TODO Replace with SchedServer Logger 
 				}
 			    
 			    //TODO Need to extract the title from the <item identifierref> attribute. 
 				createNode(eventWriter, MANIFEST_TITLE, orgItem.getTitle());  
 				EndElement itemEndElem = eventFactory.createEndElement("", "",  MANIFEST_ITEM);
 				
 				try 
 			    {
 					eventWriter.add(itemEndElem);
 					eventWriter.flush();
 				} 
 			    catch (XMLStreamException e) 
 				{
 					e.printStackTrace(); 
 				}
 
 				i++;
 		    }
 		    
 		    // Close the organization element node
 			EndElement organizationEndElem = eventFactory.createEndElement("", "", MANIFEST_ORG_ELEM_NAME);
 			try 
 			{
 				eventWriter.add(organizationEndElem);
 				eventWriter.flush();
 			} 
 			catch (XMLStreamException e) 
 			{
 				e.printStackTrace(); // TODO Replace with SchedServer Logger
 			}
 		}
 		
 		EndElement organizationsEndElem = eventFactory.createEndElement("", "",  MANIFEST_ORG_NODE_NAME);
 		try 
 		{
 			eventWriter.add(organizationsEndElem);
 			eventWriter.flush();
 		} 
 		catch (XMLStreamException e) 
 		{
 			e.printStackTrace(); // TODO Replace with SchedServer Logger
 		}
 	}
 
 	private void decorateResources(Manifest manifest, XMLEventFactory eventFactory, XMLEventWriter eventWriter)
 	{
 		ArrayList<Attribute> attributeList = null;
 		ArrayList<Namespace> namespaceList = null;
 				
 		//Create the resources node
 		StartElement resourcesStartElem = eventFactory.createStartElement("", "",  MANIFEST_RESOURCES_NODE_NAME);
 		
 		namespaceList = new ArrayList<Namespace>();
 		namespaceList.add(eventFactory.createNamespace(Manifest.NAMESPACE));
 		
 		try 
 	    {
 	    	// Write the resources start node to file
 			eventWriter.add(resourcesStartElem);
 			eventWriter.flush();
 		} 
 	    catch (XMLStreamException e) 
 	    {
 			e.printStackTrace(); // TODO Replace with SchedServer Logger 
 		}
 		
 		
 		//Create each separate resource section
 		for(Iterator<Resource> iterResource = manifest.getResources().iterator(); iterResource.hasNext();)
 		{
 			Resource resourceManifest = iterResource.next();
 			
 			//Decorate the attributes: identifier=; type=; adlcp:scormtype=; href=;
 			attributeList = new ArrayList<Attribute>();
 			attributeList.add(eventFactory.createAttribute("identifier", resourceManifest.getIdentifier()));
 			attributeList.add(eventFactory.createAttribute("type", resourceManifest.getType()));
 			attributeList.add(eventFactory.createAttribute("adlcp:scormtype", resourceManifest.getScormType()));
 			attributeList.add(eventFactory.createAttribute("href", resourceManifest.getHRef()));
 				
 			StartElement resourceStartElem = eventFactory.createStartElement("", "",  MANIFEST_RESOURCE_ELEM_NAME, attributeList.iterator(), namespaceList.iterator());
 
 			try 
 		    {
 				eventWriter.add(resourceStartElem);
 				eventWriter.flush();
 			} 
 		    catch (XMLStreamException e) 
 		    {
 				e.printStackTrace(); // TODO Replace with SchedServer Logger 
 			}
 			
 			//Add File node and HREF attribute
 		    attributeList = new ArrayList<Attribute>();
 		    attributeList.add(eventFactory.createAttribute("href", resourceManifest.getHRef()));
 		    StartElement fileStartElem = eventFactory.createStartElement("", "",  MANIFEST_FILE_ELEM_NAME, attributeList.iterator(), namespaceList.iterator());
 			
 		    try 
 		    {
 				eventWriter.add(fileStartElem);
 				eventWriter.flush();
 			} 
 		    catch (XMLStreamException e) 
 		    {
 				e.printStackTrace(); // TODO Replace with SchedServer Logger 
 			}
 		    
 		    // Close the file element node
 			EndElement fileEndElem = eventFactory.createEndElement("", "", MANIFEST_FILE_ELEM_NAME);
 			try 
 			{
 				eventWriter.add(fileEndElem);
 				eventWriter.flush();
 			} 
 			catch (XMLStreamException e) 
 			{
 				e.printStackTrace(); // TODO Replace with SchedServer Logger
 			}
 		    
 			//Create a Dependency node if it is a SCO
 			if(resourceManifest.getScormType().equals(Resource.SCORMTYPE_SCO))
 			{
 				attributeList = new ArrayList<Attribute>();
 				attributeList.add(eventFactory.createAttribute("identifierref", "stub"));
 				StartElement dependencyStartElem = eventFactory.createStartElement("", "",  MANIFEST_DEPENDENCY_ELEM_NAME, attributeList.iterator(), namespaceList.iterator());
 				 
 				try 
 				{
 					eventWriter.add(dependencyStartElem);
 					eventWriter.flush();
 				} 
 				catch (XMLStreamException e) 
 				{
 					e.printStackTrace(); // TODO Replace with SchedServer Logger 
 				}
 							
 				
 				 // Close the Dependency element node
 				EndElement dependencyEndElem = eventFactory.createEndElement("", "", MANIFEST_DEPENDENCY_ELEM_NAME);
 				try 
 				{
 					eventWriter.add(dependencyEndElem);
 					eventWriter.flush();
 				} 
 				catch (XMLStreamException e) 
 				{
 					e.printStackTrace(); // TODO Replace with SchedServer Logger
 				}
 			}
 			
 		    //Close the resource element tag
 		    EndElement resourceEndElem = eventFactory.createEndElement("", "", MANIFEST_RESOURCE_ELEM_NAME);
 			try 
 			{
 				eventWriter.add(resourceEndElem);
 				eventWriter.flush();
 			} 
 			catch (XMLStreamException e) 
 			{
 				e.printStackTrace(); // TODO Replace with SchedServer Logger
 			}
 		}
 		
 		//Add lmsstub.js at the end before closing off resources node!!!
 		attributeList = new ArrayList<Attribute>();
 		attributeList.add(eventFactory.createAttribute("identifier", "stub"));
 		attributeList.add(eventFactory.createAttribute("type", Resource.LILA_TYPE));
 		attributeList.add(eventFactory.createAttribute("adlcp:scormtype", Resource.SCORMTYPE_ASSET));
 		StartElement lmsstubStartElem = eventFactory.createStartElement("", "",  MANIFEST_RESOURCE_ELEM_NAME, attributeList.iterator(), namespaceList.iterator());
 
 		try 
 	    {
 			eventWriter.add(lmsstubStartElem);
 			eventWriter.flush();
 		} 
 	    catch (XMLStreamException e) 
 	    {
 			e.printStackTrace(); // TODO Replace with SchedServer Logger 
 		}
 		
 	    //Add File node and HREF attribute
 	    attributeList = new ArrayList<Attribute>();
 	    attributeList.add(eventFactory.createAttribute("href", "lmsstub.js"));
 	    StartElement fileStartElem = eventFactory.createStartElement("", "",  MANIFEST_FILE_ELEM_NAME, attributeList.iterator(), namespaceList.iterator());
 		
 	    try 
 	    {
 			eventWriter.add(fileStartElem);
 			eventWriter.flush();
 		} 
 	    catch (XMLStreamException e) 
 	    {
 			e.printStackTrace(); // TODO Replace with SchedServer Logger 
 		}
 	    
 	    // Close the file element node
 		EndElement fileEndElem = eventFactory.createEndElement("", "", MANIFEST_FILE_ELEM_NAME);
 		try 
 		{
 			eventWriter.add(fileEndElem);
 			eventWriter.flush();
 		} 
 		catch (XMLStreamException e) 
 		{
 			e.printStackTrace(); // TODO Replace with SchedServer Logger
 		}
 	    
 	    //Close the resource node for lmsstub.js
 	    EndElement lmsstubEndElem = eventFactory.createEndElement("", "", MANIFEST_RESOURCE_ELEM_NAME);
 		try 
 		{
 			eventWriter.add(lmsstubEndElem);
 			eventWriter.flush();
 		} 
 		catch (XMLStreamException e) 
 		{
 			e.printStackTrace(); // TODO Replace with SchedServer Logger
 		}
 	    
 		//Close the resources node
 		EndElement resourcesEndElem = eventFactory.createEndElement("", "", MANIFEST_RESOURCES_NODE_NAME);
 		try 
 		{
 			eventWriter.add(resourcesEndElem);
 			eventWriter.flush();
 		} 
 		catch (XMLStreamException e) 
 		{
 			e.printStackTrace(); // TODO Replace with SchedServer Logger
 		}
 	}
 	
 	
 	/**
 	 * Takes the value of a name to produce an underscore version of the name.
 	 * This is to allow naming consistency.
 	 * 
 	 * This is from: http://www.vogella.de/articles/JavaXML/article.html
 	 * @param eventWriter
 	 * @param name 
 	 * @param value
 	 */
 	private void createNode(XMLEventWriter eventWriter, String name, String value)
 	{
 		try
 		{
 			XMLEventFactory eventFactory = XMLEventFactory.newInstance();
 
 			// Create Start node
 			StartElement sElement = eventFactory.createStartElement("", "", name);
 			eventWriter.add(sElement);
 
 			// Create Content
 			Characters characters = eventFactory.createCharacters(value);
 			eventWriter.add(characters);
 
 			// Create End node
 			EndElement eElement = eventFactory.createEndElement("", "", name);
 			eventWriter.add(eElement);
 			
 			eventWriter.flush();
 		}
 		catch(XMLStreamException e)
 		{
 			e.printStackTrace(); //TODO replace with Sahara Logger as part of refactoring.
 		}
 	}
 
 }
