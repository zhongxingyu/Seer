 // Copyright 2010 Nathaniel Harward
 //
 // This file is part of ndh-commons.
 //
 // ndh-commons is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // ndh-commons is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with ndh-commons. If not, see <http://www.gnu.org/licenses/>.
 
 package nerds.antelax.commons.xml.saxbp;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.namespace.QName;
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.events.XMLEvent;
 import javax.xml.transform.Source;
 
 import nerds.antelax.commons.xml.saxbp.annotations.JAXBHandler;
 import nerds.antelax.commons.xml.saxbp.annotations.SAXBPHandler;
 
 
 import com.google.common.base.Preconditions;
 
 /**
  * An XML parser that is [an attempt at] a best-of combination of SAX/JAXB/StAX, hence the name. The idea is to combine the
  * convenience of JAXB and StAX with the low overhead and callback style of SAX. Good for handling large XML files where long lists
  * of repeated and perhaps cumbersome elements exist and memory consumption rules out DOM or a "full" JAXB parse but where it would
  * be very convenient to perhaps use JAXB (or StAX) for sub-elements.
  */
 public class SAXBPParser {
 
     /**
      * Wrapper around {@link #parse(XMLEventReader, JAXBContext, Object...)}.
      * 
      * @throws SAXBPException
      * @see #parse(XMLEventReader, JAXBContext, Object...)
      */
     public void parse(final InputStream is, final JAXBContext context, final Object... saxbpHandlers) throws XMLStreamException,
             FactoryConfigurationError, JAXBException, SAXBPException {
         Preconditions.checkNotNull(is, "InputStream may not be null");
         parse(XMLInputFactory.newInstance().createXMLEventReader(is), context, saxbpHandlers);
     }
 
     /**
      * Wrapper around {@link #parse(XMLEventReader, JAXBContext, Object...)}.
      * 
      * @throws SAXBPException
      * @see #parse(XMLEventReader, JAXBContext, Object...)
      */
     public void parse(final Reader reader, final JAXBContext context, final Object... saxbpHandlers) throws XMLStreamException,
             FactoryConfigurationError, JAXBException, SAXBPException {
         Preconditions.checkNotNull(reader, "Reader may not be null");
         parse(XMLInputFactory.newInstance().createXMLEventReader(reader), context, saxbpHandlers);
     }
 
     /**
      * Wrapper around {@link #parse(XMLEventReader, JAXBContext, Object...)}.
      * 
      * @throws SAXBPException
      * @see #parse(XMLEventReader, JAXBContext, Object...)
      */
     public void parse(final Source source, final JAXBContext context, final Object... saxbpHandlers) throws XMLStreamException,
             FactoryConfigurationError, JAXBException, SAXBPException {
         Preconditions.checkNotNull(source, "Source may not be null");
         parse(XMLInputFactory.newInstance().createXMLEventReader(source), context, saxbpHandlers);
     }
 
     /**
      * Wrapper around {@link #parse(XMLEventReader, JAXBContext, Object...)}.
      * 
      * @throws SAXBPException
      * @see #parse(XMLEventReader, JAXBContext, Object...)
      */
     public void parse(final XMLStreamReader reader, final JAXBContext context, final Object... saxbpHandlers)
             throws XMLStreamException, FactoryConfigurationError, JAXBException, SAXBPException {
         Preconditions.checkNotNull(reader, "XMLStreamReader may not be null");
         parse(XMLInputFactory.newInstance().createXMLEventReader(reader), context, saxbpHandlers);
     }
 
     /**
      * Parses an {@link XMLEventReader} to completion, passing on StAX or JAXB parsing events to handlers as indicated by their
      * annotated methods.
      * 
     * @param reader
     *            The {@link XMLEventReader} to read XML events from
     * @param jaxbContext
      *            if using JAXB for any callbacks, the {@link JAXBContext} to use
      * @param saxbpHandlers
      *            each handler must be annotated with {@link SAXBPHandler}, and should have methods annotated with one or more of
      *            {@link JAXBHandler} (for JAXB callbacks), or {@link XmlElement} or {@link XmlElements} (for StAX callbacks)
      * @throws XMLStreamException
      * @throws FactoryConfigurationError
      * @throws JAXBException
      * @throws SAXBPException
      */
     public void parse(final XMLEventReader reader, final JAXBContext jaxbContext, final Object... saxbpHandlers)
             throws XMLStreamException, FactoryConfigurationError, JAXBException, SAXBPException {
         Preconditions.checkNotNull(reader, "XMLEventReader cannot be null");
         Preconditions.checkNotNull(saxbpHandlers, "At least one handler must be specified");
         Preconditions.checkArgument(saxbpHandlers.length > 0, "At least one handler must be specified");
         final Map<QName, Collection<ParseInterest>> interestMap = groupInterestByQName(jaxbContext, saxbpHandlers);
         final Deque<QName> context = new LinkedList<QName>();
         Preconditions.checkArgument(context.isEmpty());
         for (XMLEvent event = reader.peek(); event != null; event = reader.peek()) {
             if (event.isStartDocument()) {
                 context.push(UNNAMED);
             } else if (event.isStartElement()) {
                 context.push(event.asStartElement().getName());
             }
             boolean interesting = false;
             if (interestMap.containsKey(context.peek())) {
                 for (final ParseInterest interest : interestMap.get(context.peek())) {
                     if (interest.accept(event)) {
                         interest.handleNextEvent(reader);
                         interesting = true;
                         if (interest.isJAXB())
                             context.pop();
                         break;
                     }
                 }
             }
             if (event.isEndDocument() || event.isEndElement())
                 context.pop();
             if (!interesting)
                 reader.next();
         }
         Preconditions.checkArgument(context.isEmpty());
     }
 
     private static Map<QName, Collection<ParseInterest>> groupInterestByQName(final JAXBContext jaxbContext,
             final Object... saxbpHandlers) throws SAXBPException, JAXBException {
         final Map<QName, Collection<ParseInterest>> interestMap = new HashMap<QName, Collection<ParseInterest>>();
         for (final ParseInterest interest : ParseInterest.fromHandlers(jaxbContext, saxbpHandlers)) {
             Collection<ParseInterest> chain = interestMap.get(interest.qName());
             if (chain == null)
                 chain = new LinkedList<ParseInterest>();
             chain.add(interest);
             interestMap.put(interest.qName(), chain);
         }
         return Collections.unmodifiableMap(interestMap);
     }
 
     static final QName UNNAMED = new QName(XMLConstants.NULL_NS_URI, "");
 
 }
