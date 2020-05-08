 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.beans;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Serializer;
 
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.atlasapi.media.entity.simple.Broadcast;
 import org.atlasapi.media.entity.simple.ContentQueryResult;
 import org.atlasapi.media.entity.simple.Item;
 import org.atlasapi.media.entity.simple.Location;
 import org.atlasapi.media.entity.simple.Playlist;
 import org.atlasapi.media.entity.simple.PublisherDetails;
 import org.atlasapi.media.vocabulary.DC;
 import org.atlasapi.media.vocabulary.PLAY_SIMPLE_XML;
 import org.atlasapi.media.vocabulary.PO;
 import org.xml.sax.SAXException;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Iterables;
 import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
 
 /**
  * Outputs simple URIplay model in plain XML format using JAXB.
  * 
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class JaxbXmlTranslator implements BeanGraphWriter {
 
 	private JAXBContext context;
 
 	public JaxbXmlTranslator() throws JAXBException {
 		context = JAXBContext.newInstance(ContentQueryResult.class, Playlist.class, Item.class, Location.class, Broadcast.class, PublisherDetails.class);
 	}
 	
 	public void writeTo(Collection<Object> graph, OutputStream stream) {
 		
 		try {
 			Marshaller m = context.createMarshaller();
 			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new UriplayNamespacePrefixMapper());
 
 			XMLSerializer serializer = getXMLSerializer(stream);
 			m.marshal(Iterables.getOnlyElement(graph), serializer.asContentHandler());
 			
 		} catch (JAXBException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (SAXException e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 	
 	private static XMLSerializer getXMLSerializer(OutputStream oStream) throws SAXException {
      
         OutputFormat of = new OutputFormat();
 
         of.setCDataElements(new String[] { "^embedCode" });  
         
         XMLSerializer serializer = new XMLSerializer(of);
         serializer.setOutputByteStream(oStream);
 
         return serializer;
     }
 	
 	private static final class UriplayNamespacePrefixMapper extends NamespacePrefixMapper {
 		@Override
 		public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
 			if (PLAY_SIMPLE_XML.NS.equals(namespaceUri)) {
 				return PLAY_SIMPLE_XML.PREFIX;
 			} else if (PO.NS.equals(namespaceUri)) {
 				return PO.PREFIX;
 			} else if (DC.NS.equals(namespaceUri)) {
 				return "dc";
 			}
 			return null;
 		}
 
 		@Override
 		public String[] getPreDeclaredNamespaceUris() {
 		    return new String[] { PLAY_SIMPLE_XML.NS , PO.NS, DC.NS};
 		}
 	}
<<<<<<< HEAD
=======

 	@Override
 	public void writeError(AtlasErrorSummary exception, OutputStream stream) {
 		try {
 			write(stream, xmlFrom(exception));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private Element xmlFrom(AtlasErrorSummary exception) {
 		Element error = new Element("error");
 		error.appendChild(stringElement("message", exception.message()));
 		error.appendChild(stringElement("code", exception.errorCode()));
 		error.appendChild(stringElement("id", exception.id()));
 		return error;
 	}
 
 	private void write(OutputStream out, Element xml) throws UnsupportedEncodingException, IOException {
 		Serializer serializer = new Serializer(out, Charsets.UTF_8.toString());
 		serializer.setIndent(4);
 		serializer.setLineSeparator("\n");
 		serializer.write(new Document(xml));
 	}
 	
 	protected Element stringElement(String name, String value) {
 		Element elem = new Element(name);
 		elem.appendChild(value);
 		return elem;
	}
>>>>>>> 5e7438c46d6ec4bb8fc7642081a7b9ea0100f09b
 }
