 /*
  * This file is part of OTC.
  * 
  * Copyright (c) 2011, Marius MOULIS <moulis.marius@gmail.com>
  *
  * OTC is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OTC is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OTC.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /***
  * 
  * <p>
  * This class provided features to parse AMISPIM XML Template files
  * For more information about the grammar of this XML file, you could ask for the grammar specification
  * </p>
  * 
  */
 package utils.xml;
 
 import java.util.Vector;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import core.metadata.AmiSpimData;
 
 public class AmiSpimXLMParser extends DefaultHandler {
 	
 	/**
 	 * 
 	 * This vector contains AMISPIM parsed data
 	 * 
 	 * */
 	private Vector<AmiSpimData> data;
 	
 	/**
 	 * 
 	 * Current parsed data
 	 * 
 	 * */
 	private AmiSpimData currentData;
 	
 	/**
 	 * 
 	 * Default Constructor
 	 * */
 	public AmiSpimXLMParser(){}
 	
 	@Override
 	public void startDocument(){
 		data = new Vector<AmiSpimData>();
 	}
 	
 	@Override
 	public void endDocument(){
 		// Nothing to do
 	}
 	
 	@Override
 	public void startElement(String nameSpaceURI, String localName, String rawName, Attributes attributes) throws SAXException {
 		if (rawName.equals("Tag"))
			currentData = new AmiSpimData(attributes.getValue(0), attributes.getValue(1), Integer.parseInt(attributes.getValue(2)));
 		else if (rawName.equals("Attribute"))
			currentData = new AmiSpimData(attributes.getValue(0), attributes.getValue(1), attributes.getValue(2), Integer.parseInt(attributes.getValue(3)));
 	}
 	
 	@Override
 	public void endElement(String nameSpaceURI, String localName, String rawName) throws SAXException {
 		if (rawName.equals("Tag"))
 			data.add(currentData);
 		else if (rawName.equals("Attribute"))
 			data.add(currentData);
 	}
 	
 	@Override
 	public void characters(char[] data, int start, int length) {
 		// No characters to parse. Do nothing
 	}
 
 	/**
 	 * 
 	 * Return the parsed data
 	 * 
 	 * */
 	public Vector<AmiSpimData> getParsedData(){
 		return data;
 	}
 }
