 /**
  * Copyright 2013 Ordina
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package nl.ordina.bag.etl.xml;
 
 import java.io.InputStream;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import nl.kadaster.schemas.bag_verstrekkingen.extract_deelbestand_lvc.v20090901.BAGExtractDeelbestandLVC;
 import nl.kadaster.schemas.bag_verstrekkingen.extract_deelbestand_lvc.v20090901.BAGExtractDeelbestandLVC.Antwoord.Vraag;
 import nl.kadaster.schemas.imbag.lvc.v20090901.Ligplaats;
 import nl.kadaster.schemas.imbag.lvc.v20090901.Nummeraanduiding;
 import nl.kadaster.schemas.imbag.lvc.v20090901.OpenbareRuimte;
 import nl.kadaster.schemas.imbag.lvc.v20090901.Pand;
 import nl.kadaster.schemas.imbag.lvc.v20090901.Standplaats;
 import nl.kadaster.schemas.imbag.lvc.v20090901.Verblijfsobject;
 import nl.kadaster.schemas.imbag.lvc.v20090901.Woonplaats;
 import nl.ordina.bag.etl.util.XMLStreamReaderUtils;
 import nl.ordina.bag.etl.util.XMLStreamReaderUtils.Tag;
 
 public abstract class ExtractParser implements ExtractHandler
 {
 	private final String XSD_FILE = "/nl/ordina/bag/etl/xsd/bag-verstrekkingen/extract-deelbestand-lvc/v20090901/BagvsExtractDeelbestandExtractLvc-1.4.xsd";
 	private Schema schema;
 	private XMLMessageBuilder<BAGExtractDeelbestandLVC> objectBuilder;
 
 	public ExtractParser()
 	{
 		try
 		{
 			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 			schema = schemaFactory.newSchema(new StreamSource(SchemaValidator.class.getResourceAsStream(XSD_FILE),SchemaValidator.class.getResource(XSD_FILE).toString()));
 			objectBuilder = XMLMessageBuilder.getInstance(BAGExtractDeelbestandLVC.class);
 		}
 		catch (Exception e)
 		{
 			throw new ParserException(e);
 		}
 	}
 
 	public void parse(InputStream is) throws ParseException, HandlerException
 	{
 		try
 		{
 			XMLStreamReader reader = XMLStreamReaderUtils.getXMLStreamReader(is);
 			parse(reader);
 		}
		catch (XMLStreamException | FactoryConfigurationError | JAXBException e)
 		{
 			throw new ParseException(e);
 		}
 	}
 
 	private void parse(XMLStreamReader reader) throws XMLStreamException, JAXBException
 	{
 		XMLStreamReaderUtils.readStartTag(reader,"BAG-Extract-Deelbestand-LVC");
 		XMLStreamReaderUtils.readStartTag(reader,"antwoord");
 		XMLStreamReaderUtils.readStartTag(reader,"vraag");
 		// objectBuilder.handle(schema,reader,Vraag.class);
 		objectBuilder.handle(reader,Vraag.class);
 		Tag tag = XMLStreamReaderUtils.getTag(reader);
 		if ("producten".equals(tag.getLocalName()))
 		{
 			XMLStreamReaderUtils.readStartTag(reader,"LVC-product");
 			tag = XMLStreamReaderUtils.readTag(reader,new Tag(XMLStreamReader.START_ELEMENT,"Woonplaats"),new Tag(XMLStreamReader.START_ELEMENT,"OpenbareRuimte"),new Tag(XMLStreamReader.START_ELEMENT,"Nummeraanduiding"),new Tag(XMLStreamReader.START_ELEMENT,"Pand"),new Tag(XMLStreamReader.START_ELEMENT,"Verblijfsobject"),new Tag(XMLStreamReader.START_ELEMENT,"Ligplaats"),new Tag(XMLStreamReader.START_ELEMENT,"Standplaats"),new Tag(XMLStreamReader.START_ELEMENT,"productcode"),new Tag(XMLStreamReader.END_ELEMENT,"LVC-product"));
 			while (true)
 			{
 				if ("Woonplaats".equals(tag.getLocalName()))
 					handle(objectBuilder.handle(schema,reader,Woonplaats.class));
 				else if ("OpenbareRuimte".equals(tag.getLocalName()))
 					handle(objectBuilder.handle(schema,reader,OpenbareRuimte.class));
 				else if ("Nummeraanduiding".equals(tag.getLocalName()))
 					handle(objectBuilder.handle(schema,reader,Nummeraanduiding.class));
 				else if ("Pand".equals(tag.getLocalName()))
 					handle(objectBuilder.handle(schema,reader,Pand.class));
 				else if ("Verblijfsobject".equals(tag.getLocalName()))
 					handle(objectBuilder.handle(schema,reader,Verblijfsobject.class));
 				else if ("Ligplaats".equals(tag.getLocalName()))
 					handle(objectBuilder.handle(schema,reader,Ligplaats.class));
 				else if ("Standplaats".equals(tag.getLocalName()))
 					handle(objectBuilder.handle(schema,reader,Standplaats.class));
 				else if ("productcode".equals(tag.getLocalName()))
 				{
 					XMLStreamReaderUtils.readText(reader);
 					XMLStreamReaderUtils.getTag(reader,new Tag(XMLStreamReader.END_ELEMENT,"productcode"));
 					tag = XMLStreamReaderUtils.readTag(reader,new Tag(XMLStreamReader.START_ELEMENT,"Woonplaats"),new Tag(XMLStreamReader.START_ELEMENT,"OpenbareRuimte"),new Tag(XMLStreamReader.START_ELEMENT,"Nummeraanduiding"),new Tag(XMLStreamReader.START_ELEMENT,"Verblijfsobject"),new Tag(XMLStreamReader.START_ELEMENT,"Ligplaats"),new Tag(XMLStreamReader.START_ELEMENT,"Standplaats"),new Tag(XMLStreamReader.START_ELEMENT,"Pand"),new Tag(XMLStreamReader.START_ELEMENT,"productcode"),new Tag(XMLStreamReader.END_ELEMENT,"LVC-product"));
 				}
 				else if ("LVC-product".equals(tag.getLocalName()))
 					break;
 				tag = XMLStreamReaderUtils.getTag(reader);
 			}
 			XMLStreamReaderUtils.readEndTag(reader,"producten");
 			XMLStreamReaderUtils.readEndTag(reader,"antwoord");
 		}
 		XMLStreamReaderUtils.readEndTag(reader,"BAG-Extract-Deelbestand-LVC");
 	}
 
 }
