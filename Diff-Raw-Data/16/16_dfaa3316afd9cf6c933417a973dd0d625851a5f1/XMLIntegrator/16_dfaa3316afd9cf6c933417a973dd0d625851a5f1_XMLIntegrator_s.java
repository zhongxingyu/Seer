 package edu.cmu.mse.aes.project1.dataaccess;
 import edu.cmu.mse.aes.project1.data.*;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 public class XMLIntegrator implements IXMLIntegrator
 {
 	final String JAXB_CONTEXT_PACKAGE = "edu.cmu.mse.aes.project1.data";
 	final String XML_FILES_FOLDER = "data";
 	@Override
 	public void integrateXMLs(String xmlFileName) 
 	{
 		//Will collect Bikelists from all XML files.
 		List<Bikelist> listOfBikeList = new ArrayList<Bikelist>();
 		
 		try
 		{
 			//Folder where all the XML files are stored.
 			File f = new File("data");
 			//Searching every xml file in the data folder.
 			for (final File fileEntry : f.listFiles())
 				{
 						int i = fileEntry.getName().lastIndexOf(".");
 						String extension = fileEntry.getName().substring(i+1);
 						
				    	if(fileEntry.isFile() && !fileEntry.getName().equalsIgnoreCase("IntegratedXML.xml") && extension.equals("xml"))
 				    	{
 				    		JAXBContext context = JAXBContext.newInstance(JAXB_CONTEXT_PACKAGE);
 					    	Bikelist b = (Bikelist) context.createUnmarshaller().unmarshal(fileEntry);
 					    
 					    	listOfBikeList.add(b);
 				    	}
				    	
 				}
 			
 				//A final list of bikes gathered from different bikelists.
 				Bikelist bikelistclass = new Bikelist();
 				List<Bike> finalBikeList = bikelistclass.getBike();
 				
 				//Merging bikelists from all the XML files
 				for(Bikelist bikelist:listOfBikeList)
 				{
 					for(Bike bike:bikelist.getBike())
 					{
 						finalBikeList.add(bike);
 					}
 				}
 				
 				//have to merge this list with list from the Integrated XML file.
 				
 				JAXBContext context1 = JAXBContext.newInstance(JAXB_CONTEXT_PACKAGE);
 				f = new File("xmlfiles"+File.separator+"IntegratedXML.xml");
				
				Bikelist bikeListFromIntegratedXMLFile = null;
				if(f.exists())
					 bikeListFromIntegratedXMLFile = (Bikelist) context1.createUnmarshaller().unmarshal(f);
				else
					bikeListFromIntegratedXMLFile = new Bikelist();
		    	
		    	for(Bike bike:bikeListFromIntegratedXMLFile.getBike())
		    	{
		    		finalBikeList.add(bike);
		    	}
 		    	
 		    	FileOutputStream fos = null;
 				try 
 				{
 					JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT_PACKAGE);
 					Marshaller marshaller = jaxbContext.createMarshaller();
 					fos = new FileOutputStream(XML_FILES_FOLDER+File.separator+"integratedXMLFile.xml");
 					
 					marshaller.marshal(bikelistclass, fos);
 				}
 				catch (IOException | JAXBException e)
 				{
 					e.printStackTrace();
 				}
 				finally
 				{
 					try 
 					{
 						fos.close();
 					} 
 					catch (IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 		    
 				
 				
 		} 
 		catch (JAXBException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 }
