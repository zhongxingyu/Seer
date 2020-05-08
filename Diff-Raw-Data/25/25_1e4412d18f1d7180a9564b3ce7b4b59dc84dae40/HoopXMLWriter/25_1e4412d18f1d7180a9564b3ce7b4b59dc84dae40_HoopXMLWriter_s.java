 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.hoop.hoops.save;
 
 import java.util.ArrayList;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import edu.cmu.cs.in.base.HoopDataType;
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.base.kv.HoopKV;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.base.HoopFileSaveBase;
import edu.cmu.cs.in.hoop.hoops.base.HoopSaveBase;
 import edu.cmu.cs.in.hoop.properties.types.HoopEnumSerializable;
 
 /**
 * 
 */
 public class HoopXMLWriter extends HoopFileSaveBase
 {    		
 	private HoopEnumSerializable writeMode=null; // APPEND, OVERWRITE	
 	
 	/**
 	 *
 	 */
     public HoopXMLWriter () 
     {
 		setClassName ("HoopXMLWriter");
 		debug ("HoopXMLWriter ()");
 												
 		setHoopDescription ("Write to an XML file");
 		setFileExtension ("xml");
 		
 		writeMode=new HoopEnumSerializable (this,"writeMode","OVERWRITE,APPEND");
     }
 	/**
 	 *
 	 */
 	public Boolean runHoop (HoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 		
 		ArrayList <HoopKV> inData=inHoop.getData();
 		
 		if (inData==null)
 		{
 			this.setErrorString("Error: no input data to work with");
 			return (false);
 		}
 				
 		Element fileElement=new Element ("hoopoutput");
 				
 		Element typeElement=new Element ("datatypes");
 		
 		fileElement.addContent(typeElement);
 						
 		ArrayList <HoopDataType> types=inHoop.getTypes();
 						
 		for (int n=0;n<types.size();n++)
 		{			
 			HoopDataType aType=types.get(n);
 			
 			Element aTypeElement=new Element ("type");
 			
 			aTypeElement.setAttribute("type",aType.getTypeValue ());
 			aTypeElement.setAttribute("value",aType.typeToString ());
 						
 			typeElement.addContent(aTypeElement);
 		}		
 										
 		Element dataElement=new Element ("data");
 			
 		for (int t=0;t<inData.size();t++)
 		{
 			HoopKV aKV=inData.get(t);
 								
			Element keyElement=new Element ("key");
			keyElement.setText(aKV.getKeyString ());
 							
 			ArrayList<Object> vals=aKV.getValuesRaw();
 				
 			for (int i=0;i<vals.size();i++)
			{								
				HoopDataType aType=types.get(i);
				
 				Element valueElement=new Element ("value");
				valueElement.setAttribute("name",aType.getTypeValue());
				valueElement.setAttribute("type",aType.typeToString());
 				valueElement.setText((String) vals.get(i));
 				keyElement.addContent(valueElement);
 			}
 			
 			dataElement.addContent(keyElement);
 		}	
 					
 		fileElement.addContent (dataElement);
 		
 		Document document = new Document();
 				
 		document.setContent(fileElement);
 		
 		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
         String xmlString = outputter.outputString(document);		
 		        
         String fullPath=this.projectToFullPath (URI.getValue());
         
 		HoopLink.fManager.saveContents (HoopLink.fManager.createSequenceFilename(fullPath,this.getExecutionCount()),xmlString);
 						
 		return (true);							
 	}	
 	/**
 	 * 
 	 */
 	public HoopBase copy ()
 	{
 		return (new HoopXMLWriter ());
 	}		
 }
