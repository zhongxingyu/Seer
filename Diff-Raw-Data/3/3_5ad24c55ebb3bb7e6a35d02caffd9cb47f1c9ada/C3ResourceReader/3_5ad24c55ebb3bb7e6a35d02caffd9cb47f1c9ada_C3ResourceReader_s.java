 package de.zib.gndms.c3resource;
 
 import com.google.common.base.Function;
import com.google.common.base.Nullable;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import de.zib.gndms.c3resource.jaxb.C3GridResource;
 import de.zib.gndms.c3resource.jaxb.Site;
 import org.jetbrains.annotations.NotNull;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.namespace.QName;
 import javax.xml.stream.*;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 import java.io.*;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 
 /**
  * ThingAMagic.
  *
  * @author Stefan Plantikow<plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 04.11.2008 Time: 17:59:34
  */
 @SuppressWarnings({ "ClassNamingConvention" })
 public class C3ResourceReader {
 	private final QName gridResourceElemQName =
 		  new QName("http://c3grid.de/language/resouces/2007-05-16-resources.xsd", "C3GridResource");
 
 	@SuppressWarnings({ "FieldCanBeLocal" })
 	private final JAXBContext context;
 	private final XMLInputFactory inFactory;
 	private final Unmarshaller unmarshaller;
 	private final XMLOutputFactory outFactory;
 	private final XMLEventFactory evFactory;
 
 
 	public C3ResourceReader()  {
 		try {
 			context = JAXBContext.newInstance("de.zib.gndms.c3resource.jaxb");
 			evFactory = XMLEventFactory.newInstance();
 			inFactory = XMLInputFactory.newInstance();
 			outFactory = XMLOutputFactory.newInstance();
 			unmarshaller = context.createUnmarshaller();
 			unmarshaller.setSchema(null);
 		}
 		catch (JAXBException jxe) {
 			throw new RuntimeException("Failed to initialize C3ResourceReader", jxe);
 		}
 	}
 
 
 	public XMLEventReader readXML(final InputStream stream) {
 		try {
 			final XMLEventReader eventReader = inFactory.createXMLEventReader(stream);
 			return inFactory.createFilteredReader(eventReader, new C3GridResourceFilter());
 		}
 		catch (XMLStreamException e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 
 
 	public void writeXML(final XMLEventReader resourceReaderParam, final OutputStream writer)
 		  throws XMLStreamException {
 		final XMLEventWriter out = outFactory.createXMLEventWriter(writer);
 		out.add(evFactory.createStartDocument());
 		//out.add(eventFactory.createStartElement("foo", "bar", "baz"));
 		out.add(resourceReaderParam);
 		//out.add(eventFactory.createEndElement("foo", "bar", "baz"));
 		out.add(evFactory.createEndDocument());
 	}
 
 
 
 	public Iterator<C3GridResource> readResources(final InputStream stream) {
 		final XMLEventReader resourceReader = readXML(stream);
 		return new C3ResourceIterator(resourceReader);
 	}
 
 	public Iterator<Site> readXmlSites(final @NotNull String requiredPrefixParam,
 	                                   final @NotNull InputStream in) {
 		return Iterators.concat(
 			Iterators.transform(validResources(in), new Function<C3GridResource, Iterator<Site>>() {
 				public Iterator<Site> apply(@Nullable final C3GridResource resourceParam) {
 				   return Iterators.filter(resourceParam.getSite().iterator(),
 					   new Predicate<Site>() {
 						   public boolean apply(@Nullable final Site siteParam) {
 							   final String id = siteParam.getId();
 							   return id != null && id.startsWith(requiredPrefixParam);
 
 						   }
 					   });
 			}
 		}));
 	}
 
 
 	private Iterator<C3GridResource> validResources(final InputStream in) {
 		return Iterators.filter(readResources(in), new Predicate<C3GridResource>() {
 			public boolean apply(@Nullable final C3GridResource resourceParam) {
 				return resourceParam != null && resourceParam.getSite() != null;
 			}
 		});
 	}
 
 	/*
 	public Iterator<GridSite> readGridSites(final InputStream in)
 		  throws JAXBException, XMLStreamException {
 		return Iterators.concat(Iterators.transform(readXmlSites(in), new Function<Site, Iterator<GridSite>>() {
 			public Iterator<GridSite> apply(@Nullable final Site xmlSite) {
 				final GridSite gridSite = new GridSite();
 				//gridSite.setSiteId(xmlSite.getId());
 				//Workspace ws;
 				//Workspace.Archive a;
 				// ws.
 				return null;
 			}
 		}));
 	} */
 
 	@SuppressWarnings({ "ClassNamingConvention" })
 	private class C3GridResourceFilter implements EventFilter {
 		private int nestingLevel;
 
 
 		public synchronized boolean accept(final XMLEvent event) {
 			if (event.isStartDocument())
 			   return false;
 
 			if (event.isEndDocument())
 			   return false;
 
 			if (event.isStartElement()) {
 				final StartElement startElement = (StartElement) event;
 				if (startElement.getName().equals(gridResourceElemQName))
 					nestingLevel++;
 			}
 
 			if (event.isEndElement()) {
 				final EndElement endElement = (EndElement)event;
 					if (endElement.getName().equals(gridResourceElemQName)) {
 						nestingLevel--;
 						return nestingLevel >= 0;
 					}
 			}
 
 			return nestingLevel > 0;
 		}
 	}
 
 	public class C3ResourceIterator implements Iterator<C3GridResource> {
 		private final XMLEventReader resourceReader;
 
 
 		public C3ResourceIterator(final XMLEventReader resourceReaderParam) {
 			resourceReader = resourceReaderParam;
 		}
 
 
 		public boolean hasNext() {
 			try {
 				while (resourceReader.hasNext()) {
 					final XMLEvent event = resourceReader.peek();
 					if (event.isStartElement()
 						  && gridResourceElemQName.equals(((StartElement)event).getName()))
 						return true;
 					else
 						resourceReader.nextEvent();
 				}
 				try {
 					return false;
 				}
 				finally {
 					resourceReader.close();
 				}
 			}
 			catch (XMLStreamException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 
 
 		public C3GridResource next() {
 			try {
 				if (hasNext()) {
 					final JAXBElement<C3GridResource> element =
 						  unmarshaller.unmarshal(resourceReader, C3GridResource.class);
 					return element.getValue();
 				}
 				else
 					throw new NoSuchElementException();
 			}
 			catch (JAXBException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	@SuppressWarnings({ "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr" })
 	public static void main(String[] args) throws JAXBException, XMLStreamException,
 		  FileNotFoundException {
 		C3ResourceReader reader = new C3ResourceReader();
 		Iterator<C3GridResource> resourceEnum = reader.readResources(new FileInputStream(args[0]));
 		while (resourceEnum.hasNext()) {
 			System.out.println(resourceEnum.next().toString());
 		}			  
 	}
 }
