 package gov.usgs.cida.gml;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLStreamException;
 import org.geotools.data.FeatureListener;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.simple.SimpleFeatureImpl;
 import org.geotools.feature.simple.SimpleFeatureTypeImpl;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.xml.Configuration;
 import org.geotools.xml.PullParser;
 import org.opengis.feature.Feature;
 import org.opengis.feature.FeatureVisitor;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.filter.Filter;
 import org.opengis.filter.FilterVisitor;
 import org.opengis.filter.sort.SortBy;
 import org.opengis.util.ProgressListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 /**
  * Modified slightly to ignore features with no geometry (jiwalker)
  * @author tkunicki
  */
 public class GMLStreamingFeatureCollection implements SimpleFeatureCollection {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(GMLStreamingFeatureCollection.class);
     
 	final private File file;
 	final private SimpleFeatureType featureType;
 	final private Configuration configuration;
 
 	public GMLStreamingFeatureCollection(File file) {
 
         LOGGER.debug("Starting parse of file {}", file.getName());
 		this.file = file;
 		this.configuration = GMLUtil.generateGMLConfiguration(file);
         try (SimpleFeatureIterator iterator = new StreamingFeatureIterator(FILTER_PASSTHRU, false)) {
             if (iterator.hasNext()) {
                 SimpleFeature feature = iterator.next();
                 this.featureType = feature.getFeatureType();
             } else {
                 throw new RuntimeException("Empty Feature Collection");
             }
         } catch (IOException | SAXException | ParserConfigurationException e) {
 			throw new RuntimeException(e);
 		}
     }
 
 	@Override
 	public SimpleFeatureIterator features() {
 		return createStreamingFeatureIterator();
 	}
 
 	public Iterator iterator() {
 		return createStreamingFeatureIterator();
 	}
 
 	private StreamingFeatureIterator createStreamingFeatureIterator() {
 		StreamingFeatureIterator iterator = null;
 		try {
 			iterator = new StreamingFeatureIterator(FILTER_PASSTHRU);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return iterator;
 	}
 
 	public void close(FeatureIterator close) {
 		destroyStreamingFeatureIterator(close);
 	}
 
 	public void close(Iterator close) {
 		destroyStreamingFeatureIterator(close);
 	}
 
 	protected void destroyStreamingFeatureIterator(Object object) {
 		if (object instanceof StreamingFeatureIterator) {
 			StreamingFeatureIterator iterator = (StreamingFeatureIterator) object;
 			if (iterator.getParent() == this) {
 				iterator.close();
 				return;
 			}
 		}
 		throw new RuntimeException("iterator not known by this instance");
 	}
 
 	public void addListener(FeatureListener listener) throws NullPointerException {
 		// do nothing, this collection is read-only
 	}
 
 	public void removeListener(FeatureListener listener) throws NullPointerException {
 		// do nothing, this collection is read-only
 	}
 
 	@Override
 	public SimpleFeatureType getSchema() {
 		return featureType;
 	}
 
 	@Override
 	public String getID() {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public SimpleFeatureCollection subCollection(Filter filter) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public SimpleFeatureCollection sort(SortBy order) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public ReferencedEnvelope getBounds() {
 		throw new UnsupportedOperationException("Not supported in simplified version.");
 	}
 
 	@Override
 	public int size() {
 		throw new UnsupportedOperationException("Not supported in simplified version.");
 	}
 
 	public void purge() {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	public boolean add(Feature obj) {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	public boolean addAll(Collection collection) {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	public boolean addAll(FeatureCollection resource) {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	public void clear() {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	@Override
 	public boolean contains(Object o) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public boolean containsAll(Collection o) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return size() > 0;
 	}
 
 	public boolean remove(Object o) {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	public boolean removeAll(Collection c) {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	public boolean retainAll(Collection c) {
 		throw new UnsupportedOperationException("This instance is read-only");
 	}
 
 	@Override
 	public Object[] toArray() {
 		throw new UnsupportedOperationException("Only streaming supported");
 	}
 
 	@Override
 	public Object[] toArray(Object[] a) {
 		throw new UnsupportedOperationException("Only streaming supported");
 	}
 
     private SimpleFeature wrap(SimpleFeature base) {
         return new SimpleFeatureImpl(
                 base.getAttributes(),
                 featureType,
                 base.getIdentifier());
     }
     
     public static SimpleFeatureType unwrapSchema(SimpleFeatureType wrappedFeatureType) {
         List<AttributeDescriptor> allowable = new ArrayList<>();
         List<String> disallowed = new ArrayList<>();
         disallowed.add("metaDataProperty");
         disallowed.add("boundedBy");
         disallowed.add("name");
         disallowed.add("location");
         disallowed.add("description");
        disallowed.add("descriptio");
         
         for (AttributeDescriptor desc : wrappedFeatureType.getAttributeDescriptors()) {
             if(!disallowed.contains(desc.getLocalName())) {
                 allowable.add(desc);
             }
         }
         SimpleFeatureType unwrapped = new SimpleFeatureTypeImpl(
                 wrappedFeatureType.getName(),
                 allowable,
                 wrappedFeatureType.getGeometryDescriptor(),
                 wrappedFeatureType.isAbstract(),
                 wrappedFeatureType.getRestrictions(),
                 wrappedFeatureType.getSuper(),
                 wrappedFeatureType.getDescription());
         
         return unwrapped;
     }
 
 	private final class StreamingFeatureIterator implements SimpleFeatureIterator, Iterator {
 
 		private PullParser parser;
 		private InputStream inputStream;
 		private Filter filter;
 		private SimpleFeature next;
 		private boolean open;
         private boolean wrap;
 
         private StreamingFeatureIterator(Filter filter) throws ParserConfigurationException, SAXException, FileNotFoundException {
             this(filter, false);
         }
 
 		private StreamingFeatureIterator(Filter filter, boolean wrap) throws ParserConfigurationException, SAXException, FileNotFoundException {
 			this.filter = filter;
             this.wrap = wrap;
 
 			inputStream = new BufferedInputStream(
 					new FileInputStream(file),
 					16 << 10);
 			parser = new PullParser(
 					configuration,
 					inputStream,
 					SimpleFeature.class);
 			open = true;
 		}
 
 		@Override
 		public synchronized boolean hasNext() {
 			if (next == null) {
 				findNext();
 			}
 			return next != null;
 		}
 
 		@Override
 		public synchronized SimpleFeature next() throws NoSuchElementException {
 			if (!hasNext()) {
 				throw new NoSuchElementException();
 			}
 			SimpleFeature current = next;
 			next = null;
 			return current;
 		}
 
 		@Override
 		public synchronized void close() {
 			if (open) {
 				next = null;
 				filter = null;
 				parser = null;
 				if (inputStream != null) {
 					try {
 						inputStream.close();
 					} catch (IOException e) {
 						// do nothing, cleaning up
 					}
 					inputStream = null;
 				}
 				open = false;
 			}
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		public GMLStreamingFeatureCollection getParent() {
 			return GMLStreamingFeatureCollection.this;
 		}
 
 		protected void findNext() {
 			while (next == null && open) {
                 try {
                     Object parsed = parser.parse();
                     if (parsed instanceof SimpleFeature) {
                         SimpleFeature candidate = (SimpleFeature) parsed;
                         if (filter.evaluate(candidate)) {
                             next = wrap ? wrap(candidate) : candidate;
                         }
                     } else {
                         close();
                     }
                 } catch (IOException | XMLStreamException | SAXException ex) {
                     LOGGER.debug("Exception caught, moving to next");
                 }
 			}
 		}
 	}
 
 	private static Filter FILTER_PASSTHRU = new Filter() {
 		@Override public boolean evaluate(Object object) { return true; }
 		@Override public Object accept(FilterVisitor visitor, Object extraData) { return true; }
 	};
 
 }
