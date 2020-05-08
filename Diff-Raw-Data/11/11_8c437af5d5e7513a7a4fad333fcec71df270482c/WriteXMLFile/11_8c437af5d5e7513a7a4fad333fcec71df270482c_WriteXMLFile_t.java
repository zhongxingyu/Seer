 package amos;
 //package com.mkyong.core;
 
 
 /*
 * Copyright (c) 2013 by The AMOS project, Group 3,
 * http://osr.cs.fau.de/2013/04/17/the-2013-amos-projects-start-today/
 *
 * This file is part of the AMOS OSVAS application.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
  
 import java.io.*;
 import java.util.Arrays;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
  
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
/*
 public class WriteXMLFile {
 
 	public static String answ(){
 		
 		return "Scheint zu gehen";
 	}
 	public static String getStringFromDocument(Document doc)
 	{
 	    try
 	    {
 	       DOMSource domSource = new DOMSource(doc);
 	       StringWriter writer = new StringWriter();
 	       StreamResult result = new StreamResult(writer);
 	       TransformerFactory tf = TransformerFactory.newInstance();
 	       Transformer transformer = tf.newTransformer();
 	       transformer.transform(domSource, result);
 	       return writer.toString();
 	    }
 	    catch(TransformerException ex)
 	    {
 	       ex.printStackTrace();
 	       return null;
 	    }
 	} 
 	
 	public static void main(String[] args) throws Exception{
 		 try {
 			 
 			 	//String xmlpath = "/XMLQuery.xml";
 			 	String csvpath = "/CSVInput.csv";
 			 	
 				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();							
 				
 				
 				
 				//open CSV File 
 				
 				BufferedReader CSVFile = 
 				        new BufferedReader(new FileReader(csvpath));
 				
 				BufferedReader CSVFiletest = 
 				        new BufferedReader(new FileReader(csvpath));
 				
 				String testrow = CSVFiletest.readLine();
 				
 				int rowcounter = 0;
 				
 				while(testrow != null){
 					
 					rowcounter++;
 					testrow = CSVFiletest.readLine();
 				}
 				
 				// table contains all lines in an array
    			CSVLine[] table = new CSVLine[rowcounter-2];
 				
 				String dataRow = CSVFile.readLine();
 				dataRow = CSVFile.readLine();
 				dataRow = CSVFile.readLine();
 				
 				int zaehler = 0;
 				  
 				while (dataRow != null){
 					   String[] dataArray = dataRow.split(";");
 					   
 					 table[zaehler] = new CSVLine(dataArray[2]);
 					 
 					 
 					 zaehler++; 
 					 // Print the data line.
 					   dataRow = CSVFile.readLine(); // Read next line of data.
 				}
 					  // Close the file once all data has been read.
 				CSVFile.close(); 
 				CSVFiletest.close();
 				
 				//Read the required information out of the Inputlist and save it in the array table				
 				
 								
 				// Root Element ComponentList
 				Document doc = docBuilder.newDocument();
 				Element rootElement = doc.createElement("ComponentList");
 				doc.appendChild(rootElement);
 				
 				for(int i=0; i < table.length; i++){
 				
 					String z = String.valueOf(i);
 					
 					//String titlecontent = table[i].title;
 					
 					String titlecontent = table[i].title;
 					
 										
 					//URL String
 					Element urlstring = doc.createElement("URLString");
 					rootElement.appendChild(urlstring);
 					
 					//set ID
 					Attr id = doc.createAttribute("id");
 					id.setValue(z);
 					urlstring.setAttributeNode(id);
 					
 					//Element title
 					Element title = doc.createElement("title");
 					title.appendChild(doc.createTextNode(titlecontent));
 					urlstring.appendChild(title);
 					
 					//Element textType
 					Element textType = doc.createElement("textType");
 					textType.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(textType);
 					
 					//Element bsDate
 					Element bsDate = doc.createElement("bsDate");
 					bsDate.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(bsDate);
 					
 					Element beDate = doc.createElement("beDate");
 					beDate.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(beDate);
 					
 					//Element refId
 					Element refId = doc.createElement("refId");
 					refId.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(refId);
 					
 					//Element refTypes
 					Element refTypes = doc.createElement("refTypes");
 					refTypes.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(refTypes);
 					
 					//Element vendors
 					Element vendors = doc.createElement("vendors");
 					vendors.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(vendors);
 					
 					//Element cvssScoreForm
 					Element cvssScoreForm = doc.createElement("cvssScoreForm");
 					cvssScoreForm.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssScoreForm);
 					
 					//Element cvssScoreTo
 					Element cvssScoreTo = doc.createElement("cvssScoreTo");
 					cvssScoreTo.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssScoreTo);
 					
 					//Element cvssAv
 					Element cvssAv = doc.createElement("cvssAv");
 					cvssAv.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssAv);
 					
 					//Element cvssAc
 					Element cvssAc = doc.createElement("cvssAc");
 					cvssAc.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssAc);
 					
 					//Element cvssA
 					Element cvssA = doc.createElement("cvssA");
 					cvssA.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssA);
 					
 					//Element cvssCi
 					Element cvssCi = doc.createElement("cvssCi");
 					cvssCi.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssCi);
 					
 					//Element cvssIi
 					Element cvssIi = doc.createElement("cvssIi");
 					cvssIi.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssIi);
 					
 					//Element cvssAi
 					Element cvssAi = doc.createElement("cvssAi");
 					cvssAi.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(cvssAi);
 					
 					//Element additionalCriteria
 					Element additionalCriteria = doc.createElement("additionalCriteria");
 					additionalCriteria.appendChild(doc.createTextNode("aa"));
 					urlstring.appendChild(additionalCriteria);
 				
 				}
 				
 				// write the content into xml file
 				TransformerFactory transformerFactory = TransformerFactory.newInstance();
 				Transformer transformer = transformerFactory.newTransformer();
 				DOMSource source = new DOMSource(doc);
 				//StreamResult result = new StreamResult(new File(xmlpath));
 		 
 				// Output to console for testing
 				// StreamResult result = new StreamResult(System.out);
 		 
 				//transformer.transform(source, result);
 		 
 				//System.out.println("File saved sucessfully!");
 				
 				//output String
 				String outs = getStringFromDocument(doc);
 				
 				
 				System.out.println(outs);
 		 
 			  } catch (ParserConfigurationException pce) {
 				pce.printStackTrace();
 			  } catch (TransformerException tfe) {
 				tfe.printStackTrace();
 			  }
 			}
 
 }
*/
