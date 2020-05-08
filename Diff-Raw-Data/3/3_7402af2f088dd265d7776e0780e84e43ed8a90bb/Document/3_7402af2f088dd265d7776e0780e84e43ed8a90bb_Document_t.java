 package name.kazennikov.annotations;
 
 import gnu.trove.map.hash.TIntObjectHashMap;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import name.kazennikov.xml.XmlWritable;
 
 import com.google.common.base.Predicate;
 
 /**
  * Document is a representation of a text in the annotation framework.
  * It stores the text and all annotations assosiated with it
  * <p>
  * A document also extends {@link Annotation}, so it has all properties of an annotation
  * @author Anton Kazennikov
  *
  */
 public class Document extends Annotation implements CharSequence {
 
 	String text;
 	
 	AnnotationList annotations = new AnnotationList();
     TIntObjectHashMap<Annotation> annotationById = new TIntObjectHashMap<Annotation>();
 	int nextID = 0;
 	
 	public Document() {	
 		this("");
 	}
 	
 	public Document(String text) {
 		this("doc", text);
 	}
 
     /**
      * Construct a document with root annotation and given text
      * @param annotName global document annotation
      * @param text document text
      */
 	public Document(String annotName, String text) {
 		super(null, annotName, 0, text.length());
 		this.text = text;
 		setDoc(this);
 		addAnnotation(this);
 	}
 
 	@Override
 	public String getText() {
 		return text;
 	}
 
     /**
      * Get annotations by type
      * @param types annotations types
      * @return
      */
 	public AnnotationList get(String... types) {
         AnnotationList anns = new AnnotationList();
         
         for(Annotation a : annotations) {
         	for(String type : types) {
         		if(a.getType().equals(type)) {
         			anns.add(a);
         			break;
         		}
         	}
         }
 
         Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	public AnnotationList getAll() {
 		return annotations;
 	}	
 	
 	/**
 	 * Get annotations that covers given span
 	 * @param start span start
 	 * @param end span end
 	 * @return
 	 */
 	public AnnotationList getCovering(int start, int end) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : getAll()) {
 			if(a.getStart() <= start && a.getEnd() >= end)
 				anns.add(a);
 		}
 		
 		return anns;
 		
 	}
 	
 	public AnnotationList get(Predicate<Annotation> predicate) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : getAll()) {
 			if(predicate.apply(a))
 				anns.add(a);
 		}
 		
 		return anns;
 	}
 	
 	/**
 	 * Get all annotations that overlaps with given span
 	 * @param start
 	 * @param end
 	 * @return
 	 */
 	public AnnotationList getOverlapping(int start, int end) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : getAll()) {
 			if(a.getStart() <= start && a.getEnd() >= end)
 				anns.add(a);
 		}
 		
 		return anns;
 		
 	}
 
 
 	
 
     /**
      * Checks if document has any of this annotations
      * @param annotationTypes annotation names
      */
     public boolean contains(String... annotationTypes) {
     	for(Annotation a : annotations) {
     		for(String s : annotationTypes) {
     			if(a.getType().equals(s))
     				return true;
     		}
     	}
 
         return false;
     }
 
 
 
     /**
      * Add single annotation to the document
      * @param ann
      * @return generated annotation id
      */
 	protected Annotation addAnnotation(Annotation ann) {
 		ann.setDoc(this);
 		annotations.add(ann);
 		ann.id = nextID++;
 		annotationById.put(ann.id, ann);
 		return ann;
 	}
 	
 	public Annotation addAnnotation(String name, int start, int end) {
 		Annotation a = new Annotation(this, name, start, end);
 		return addAnnotation(a);
 	}
 	
 	public Annotation addAnnotation(String name, int start, int end, Map<String, Object> features) {
		Annotation a = new Annotation(this, name, start, end, features);
 		return addAnnotation(a);
 	}
 
     /**
      * Add all annotations from annotation list
      * @param ans
      */
 	public void addAnnotations(Collection<? extends Annotation> ans) {
 		for(Annotation a : ans) {
 			addAnnotation(a);
 		}
 	}
 
     public void sortAnnotations() {
     	Collections.sort(annotations, Annotation.COMPARATOR);
     }
 
 	@Override
 	public int length() {
 		return text.length();
 	}
 
 	@Override
 	public char charAt(int index) {
 		return text.charAt(index);
 	}
 
 	@Override
 	public CharSequence subSequence(int start, int end) {
 		return text.subSequence(start, end);
 	}
 	
 	/**
 	 * Get annotations that are constrained by span of given annotation and properties of valid annotations
 	 * @param a constrain span of the output annotation
 	 * @param p select predicate for constrained annotations
 	 * @return list of annotations
 	 */
 	public AnnotationList getAnnotationsWithin(Annotation a, Predicate<Annotation> p) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation an : annotations) {
 				// skip given
 				if(an == a)
 					continue;
 				
 				if(a.contains(an) && p.apply(an))
 					anns.add(an);
 		}
 		
 		Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	/**
 	 * Get annotations from document that satisfies a predicate
 	 * @param predicate
 	 * @return
 	 */
 	@Override
 	public AnnotationList get(Predicate<Annotation>... predicate) {
 		AnnotationList anns = annotations.get(predicate);
 		Collections.sort(anns, Annotation.COMPARATOR);
 		return anns;
 	}
 	
 	/**
 	 * Get annotatations from document that satisfies a predicate
 	 * @param type
 	 * @param predicate
 	 * @return
 	 */
 	public AnnotationList get(String type, Predicate<Annotation> predicate) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : anns) {
 			if(a.getType().equals(type) && predicate.apply(a))
 				anns.add(a);
 		}
 		
 		Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	public AnnotationList getAllAnnotations() {
 		return annotations;
 	}
 	
 
 	public void toXml(XMLStreamWriter writer, Map<String, XmlWritable<Map<String, Object>>> anWriters) throws XMLStreamException {
 		writer.writeStartElement(DOC);
 		writer.writeAttribute("text", getText());
 		writer.writeAttribute("type", getType()); // get root annotation
 
 		for(Annotation a : getAll()) {
 			XmlWritable<Map<String, Object>> featWriter = anWriters != null? anWriters.get(a.getType()) : null;
 			writer.writeStartElement("annotation");
 			writer.writeAttribute("type", a.getType());
 			writer.writeAttribute("start", Integer.toString(a.getStart()));
 			writer.writeAttribute("end", Integer.toString(a.getEnd()));
 
 			if(featWriter != null) {
 				featWriter.write(writer, a.getFeatureMap());
 			} else {
 
 				for(Map.Entry<String, Object> e : a.getFeatureMap().entrySet()) {
 					if(e.getValue() != null) {
 						writer.writeStartElement(e.getKey());
 						writer.writeCharacters(e.getValue().toString());
 						writer.writeEndElement();
 					}
 				}
 			}
 
 			writer.writeEndElement(); // annotation
 		}
 
 		writer.writeEndElement();
 	}
 	
 	
 	private static AnnotationXmlLoader BASE_LOADER = new AnnotationXmlLoader.Base();
 	/**
 	 * Reads document from STAX stream
 	 * @param stream xml stream
 	 * @param anLoaders annotation parsers
 	 * @return
 	 */
 	public static Document read(XMLStreamReader stream, Map<String, AnnotationXmlLoader> anLoaders) throws XMLStreamException {
 		String tag = stream.getName().getLocalPart();
 		if(!tag.equals(DOC))
 			return null;
 		
 		String anDoc = stream.getAttributeValue(null, "type");
 		String text = stream.getAttributeValue(null, "text");
 		Document doc = new Document(anDoc, text);
 		
 		while(stream.hasNext()) {
 			if(stream.isEndElement() && stream.getLocalName().equals(DOC))
 				break;
 			
 			if(stream.isStartElement()) {
 				String ctag = stream.getLocalName();
 				if(ctag.equals("annotation")) {
 					String anType = stream.getAttributeValue(null, "type");
 					AnnotationXmlLoader loader = anLoaders.get(anType);
 					if(loader == null)
 						loader = BASE_LOADER;
 					Annotation a = loader.load(stream);
 					if(anType.equals(anDoc)) {
 						// load root annotation
 						doc.type = anType;
 						doc.features = a.features;
 						doc.data = a.data;
 					} else {
 						// load other annotations
 						doc.addAnnotation(a);
 					}
 				}
 			}
 			
 			stream.next();
 		}
 		
 		return doc;
 	}
 
     /**
      * Rewrites annotations with side-effect rewrite
      * @param rewriter
      */
     public void rewrite(AnnotationRewriter rewriter) {
 
 
         for(Annotation a : getAll()) {
             a = rewriter.rewrite(a);
         }
     }
     
     public void removeIf(Predicate<Annotation> p) {
 
         Iterator<Annotation> it = annotations.iterator();
 
         while(it.hasNext()) {
             Annotation a = it.next();
             if(p.apply(a)) {
                 it.remove();
                 annotationById.remove(a.getId());
             }
         }
     }
     
     public void removeIfNot(final Predicate<Annotation> p) {
     	annotations.removeIfNot(new Predicate<Annotation>() {
             @Override
             public boolean apply(@Nullable Annotation annotation) {
                 return !p.apply(annotation);
             }
         });
     }
 
 	public void remove(Annotation a) {
 		annotations.remove(a);
         annotationById.remove(a.getId());
 	}
 
     public Annotation getById(int id) {
         return annotationById.get(id);
     }
 
     
 	
 	
 }
