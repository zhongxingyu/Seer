 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.hobsoft.symmetry.support.xml.stax;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
import javanet.staxutils.helpers.EventMatcher;

 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.events.Attribute;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.Comment;
 import javax.xml.stream.events.EndDocument;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartDocument;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.junit.Before;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * 
  * 
  * @author Mark Hobson
  */
 public abstract class AbstractXMLEventTest
 {
 	// fields -----------------------------------------------------------------
 	
 	private XMLEventFactory eventFactory;
 	
 	// public methods ---------------------------------------------------------
 	
 	@Before
 	public final void setUpAbstractXMLEventTest()
 	{
 		eventFactory = XMLEventFactory.newInstance();
 	}
 	
 	// protected methods ------------------------------------------------------
 	
 	protected final XMLEventFactory getXMLEventFactory()
 	{
 		return eventFactory;
 	}
 	
 	protected final XMLEvent createEvent()
 	{
 		return eventFactory.createCharacters(randomString());
 	}
 	
 	protected final StartDocument createStartDocument()
 	{
 		return eventFactory.createStartDocument();
 	}
 	
 	protected final EndDocument createEndDocument()
 	{
 		return eventFactory.createEndDocument();
 	}
 	
 	protected final StartElement createStartElement()
 	{
 		return createStartElement(createName());
 	}
 	
 	protected final StartElement createStartElement(String localName)
 	{
 		return createStartElement(new QName(localName));
 	}
 	
 	protected final StartElement createNamespacedStartElement(String namespaceUri, String localName)
 	{
 		return createStartElement(new QName(namespaceUri, localName));
 	}
 	
 	protected final StartElement createNamespacedStartElement(String namespaceUri, String localName, String prefix)
 	{
 		return createStartElement(new QName(namespaceUri, localName, prefix));
 	}
 	
 	protected final StartElement createStartElementWithAnyNameAndAttributes(String... attributeLocalNames)
 	{
 		return createStartElementWithAttributes(createName(), attributeLocalNames);
 	}
 	
 	protected final StartElement createStartElementWithAttributes(String localName, String... attributeLocalNames)
 	{
 		return createStartElementWithAttributes(localName, createAttributeValues(attributeLocalNames));
 	}
 	
 	protected final StartElement createStartElementWithAttributes(String localName,
 		Map<String, String> attributeValuesByLocalNames)
 	{
 		return createStartElementWithNamespacedAttributes(localName, createAttributeNames(attributeValuesByLocalNames));
 	}
 	
 	protected final StartElement createStartElementWithNamespacedAttributes(String localName,
 		Map<QName, String> attributeValuesByNames)
 	{
 		return createStartElementWithNamespacedAttributes(new QName(localName), attributeValuesByNames);
 	}
 	
 	protected final StartElement createNamespacedStartElementWithAttributes(String namespaceUri, String localName,
 		String prefix, String... attributeLocalNames)
 	{
 		return createNamespacedStartElementWithAttributes(namespaceUri, localName, prefix,
 			createAttributeValues(attributeLocalNames));
 	}
 	
 	protected final StartElement createNamespacedStartElementWithAttributes(String namespaceUri, String localName,
 		String prefix, Map<String, String> attributeValuesByLocalNames)
 	{
 		return createNamespacedStartElementWithNamespacedAttributes(namespaceUri, localName, prefix,
 			createAttributeNames(attributeValuesByLocalNames));
 	}
 	
 	protected final StartElement createNamespacedStartElementWithNamespacedAttributes(String namespaceUri,
 		String localName, String prefix, Map<QName, String> attributeValuesByNames)
 	{
 		return createStartElementWithNamespacedAttributes(new QName(namespaceUri, localName, prefix),
 			attributeValuesByNames);
 	}
 	
 	protected final EndElement createEndElement()
 	{
 		return createEndElement(createName());
 	}
 	
 	protected final EndElement createEndElement(String localName)
 	{
 		return createEndElement(new QName(localName));
 	}
 	
 	protected final EndElement createNamespacedEndElement(String namespaceUri, String localName)
 	{
 		return createEndElement(new QName(namespaceUri, localName));
 	}
 	
 	protected final EndElement createNamespacedEndElement(String namespaceUri, String localName, String prefix)
 	{
 		return createEndElement(new QName(namespaceUri, localName, prefix));
 	}
 	
 	protected final Characters createCharacters(String content)
 	{
 		return eventFactory.createCharacters(content);
 	}
 	
 	protected final Comment createComment(String text)
 	{
 		return eventFactory.createComment(text);
 	}
 	
 	protected final Characters createSpace(String content)
 	{
 		return eventFactory.createSpace(content);
 	}
 	
 	protected final Attribute createAttribute()
 	{
 		return createAttribute(randomString());
 	}
 	
 	protected final Attribute createAttribute(String localName)
 	{
 		return eventFactory.createAttribute(localName, createAttributeValue());
 	}
 	
 	protected final Attribute createNamespacedAttribute(String localName)
 	{
 		return createNamespacedAttribute(randomString(), localName);
 	}
 	
 	protected final Attribute createNamespacedAttribute(String namespaceUri, String localName)
 	{
 		return createNamespacedAttribute(namespaceUri, localName, randomString());
 	}
 	
 	protected final Attribute createNamespacedAttribute(String namespaceUri, String localName, String prefix)
 	{
 		return eventFactory.createAttribute(prefix, namespaceUri, localName, createAttributeValue());
 	}
 	
 	protected final void assertEventEquals(XMLEvent expected, XMLEvent actual)
 	{
 		assertTrue(EventMatcher.eventsMatch(expected, actual));
 	}
 	
 	protected final void assertCharactersEquals(String expectedContent, XMLEvent actualEvent)
 	{
 		assertEventEquals(createCharacters(expectedContent), actualEvent);
 	}
 	
 	// private methods --------------------------------------------------------
 	
 	private StartElement createStartElement(QName name)
 	{
 		return eventFactory.createStartElement(name, null, null);
 	}
 	
 	private StartElement createStartElementWithAttributes(QName name, String... attributeLocalNames)
 	{
 		return createStartElementWithAttributes(name, createAttributeValues(attributeLocalNames));
 	}
 	
 	private StartElement createStartElementWithAttributes(QName name, Map<String, String> attributeValuesByLocalNames)
 	{
 		return createStartElementWithNamespacedAttributes(name, createAttributeNames(attributeValuesByLocalNames));
 	}
 	
 	private StartElement createStartElementWithNamespacedAttributes(QName name,
 		Map<QName, String> attributeValuesByNames)
 	{
 		Iterator<Attribute> attributes = createNamespacedAttributes(attributeValuesByNames);
 		
 		return eventFactory.createStartElement(name, attributes, null);
 	}
 	
 	protected final EndElement createEndElement(QName name)
 	{
 		return eventFactory.createEndElement(name, null);
 	}
 	
 	private Map<String, String> createAttributeValues(String... localNames)
 	{
 		Map<String, String> attributeValuesByLocalNames = new HashMap<String, String>();
 		
 		for (String localName : localNames)
 		{
 			String value = createAttributeValue();
 			
 			attributeValuesByLocalNames.put(localName, value);
 		}
 		
 		return attributeValuesByLocalNames;
 	}
 	
 	private Map<QName, String> createAttributeNames(Map<String, String> attributeValuesByLocalNames)
 	{
 		Map<QName, String> attributeValuesByNames = new HashMap<QName, String>();
 		
 		for (Entry<String, String> entry : attributeValuesByLocalNames.entrySet())
 		{
 			String localName = entry.getKey();
 			String value = entry.getValue();
 
 			attributeValuesByNames.put(new QName(localName), value);
 		}
 		
 		return attributeValuesByNames;
 	}
 	
 	private Iterator<Attribute> createNamespacedAttributes(Map<QName, String> attributeValuesByNames)
 	{
 		if (attributeValuesByNames.isEmpty())
 		{
 			return null;
 		}
 		
 		List<Attribute> attributes = new ArrayList<Attribute>();
 		
 		for (Entry<QName, String> entry : attributeValuesByNames.entrySet())
 		{
 			QName name = entry.getKey();
 			String value = entry.getValue();
 			
 			Attribute attribute = eventFactory.createAttribute(name, value);
 			
 			attributes.add(attribute);
 		}
 		
 		return attributes.iterator();
 	}
 	
 	private QName createName()
 	{
 		return new QName(randomString(), randomString(), randomString());
 	}
 	
 	private String createAttributeValue()
 	{
 		return randomString();
 	}
 	
 	private String randomString()
 	{
 		return RandomStringUtils.random(20);
 	}
 }
