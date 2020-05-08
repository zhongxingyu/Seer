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
 package org.hobsoft.symmetry.support.xml.stax.transform;
 
import javanet.staxutils.helpers.EventMatcher;

 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests {@code NamespaceEventTransformer}.
  * 
  * @author Mark Hobson
  * @see NamespaceEventTransformer
  */
 public class NamespaceEventTransformerTest
 {
 	// fields -----------------------------------------------------------------
 	
 	private XMLEventFactory eventFactory;
 	
 	// public methods ---------------------------------------------------------
 	
 	@Before
 	public void setUp()
 	{
 		eventFactory = XMLEventFactory.newInstance();
 	}
 
 	// tests ------------------------------------------------------------------
 	
 	@Test
 	public void transformWithStartElement()
 	{
 		XMLEvent event = eventFactory.createStartElement("", null, "a", null, null);
 		
 		StartElement expected = eventFactory.createStartElement("", "uri", "a", null, null);
 
 		StartElement actual = new NamespaceEventTransformer("uri").transform(event).asStartElement();
 		assertEventEquals(expected, actual);
 	}
 	
 	@Test
 	public void transformWithEndElement()
 	{
 		XMLEvent event = eventFactory.createEndElement("", null, "a", null);
 		
 		EndElement expected = eventFactory.createEndElement("", "uri", "a", null);
 
 		EndElement actual = new NamespaceEventTransformer("uri").transform(event).asEndElement();
 		assertEventEquals(expected, actual);
 	}
 	
 	// private methods --------------------------------------------------------
 	
 	private static void assertEventEquals(StartElement expected, StartElement actual)
 	{
 		assertTrue(EventMatcher.eventsMatch(expected, actual));
 		// TODO: assert namespace context?
 	}
 	
 	private static void assertEventEquals(EndElement expected, EndElement actual)
 	{
 		assertTrue(EventMatcher.eventsMatch(expected, actual));
 		assertTrue(EventMatcher.matchNamespaces(expected.getNamespaces(), actual.getNamespaces()));
 	}
 }
