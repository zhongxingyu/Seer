 package cinnamon.index;
 
 import java.util.Date;
 
 public interface Indexable {
 
 	/**
 	 * An Indexable object should have a field with XML metadata, which is represented as a String.
 	 * For example, an image object may have it's width and height recorded as metadata.<br>
 	 * A PDF manual may have the type of engines it is applicable for stored in the metadata field.   
 	 * @return XML metadata or an empty meta element: {@code <meta/>} 
 	 */
 	String getMetadata();
 	
 	/**
 	 * <p>If an object contains indexable strings, it should return them according to a fixed XML
 	 * Schema. For example, an XML Document should be returned verbatim. A folder may represent its
 	 * content as an XML document. An image document may either return an empty XML document or a automatically
 	 * generated OCR representation.
 	 * </p>
 	 * <p>Note: you should not return the same data as Metadata and as content unless it is really 
 	 * necessary.</p> 
 	 * @return an XML representation of the object's content, as appropriate, or an empty content
 	 * element: {@code <content/>};
      * @param repository The repository where the indexable object is located.
 	 */
 	String getContent(String repository);
 
     byte[] getContentAsBytes(String repository);
 
 	/**
 	 * <p>Return an XML representation of the object's system metadata. This is whatever the system itself
 	 * knows about the object. For example, who created the object, and when.</p>
 	 * <p>The root element of the XML is "sysMeta" and it must have the following two attributes set:</p>
 	 * <ol>
 	 * <li>javaClass</li>
 	 * <li>hibernateId</li>
 	 * </li>
 	 * <p>The LuceneBridge will generate a unique Id from this ($className+$hibernateId).</p>
      * @param withRelations if applicable, add a relations node with a serialized relation element for
      *                      each relation of this indexable.
 	 * @return an XML representation of the object's system metadata - 
 	 * or an empty {@code <sysMeta/>} element with attributes javaClass and hibernateId.
 	 */
 	String getSystemMetadata(Boolean withRelations);
 	
     Boolean hasXmlContent();
     
     Long myId();
 
     /**
      * Schedule the object to be updated by the background index thread at the next opportunity.
      */
     void updateIndex();
     
 }
