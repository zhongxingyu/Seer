 package org.sakaiproject.search.producer;
 
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.search.api.EntityContentProducer;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.*;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * Tool allowing to easily create an {@link EntityContentProducer} following some rules.
  * <p>
  * This class isn't thread safe and should never be used in a multithreads context.
  * </p>
  *
  * @author Colin Hebert
  */
 public final class ProducerBuilder {
     private final EntityContentProducer contentProducer;
     private final Map<String, Document> documentReferenceToDocument = new HashMap<String, Document>();
     private final Map<String, Set<String>> siteToDocuments = new HashMap<String, Set<String>>();
     private final Set<String> supportedEvents = new HashSet<String>();
 
     private ProducerBuilder(ProducerType producerType, String toolName) {
         if (producerType == ProducerType.STREAM) {
             contentProducer = mock(BinaryEntityContentProducer.class);
         } else {
             contentProducer = mock(EntityContentProducer.class);
         }
 
         when(contentProducer.isContentFromReader(anyString())).thenReturn(producerType == ProducerType.READER);
         when(contentProducer.getTool()).thenReturn(toolName);
         when(contentProducer.matches(any(Event.class))).then(new Answer<Boolean>() {
             public Boolean answer(InvocationOnMock invocation) throws Throwable {
                 Event event = (Event) invocation.getArguments()[0];
                 return supportedEvents.contains(event.getEvent());
             }
         });
         when(contentProducer.matches(anyString())).then(new Answer<Boolean>() {
             public Boolean answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.containsKey(reference);
             }
         });
         when(contentProducer.isForIndex(anyString())).then(new Answer<Boolean>() {
             public Boolean answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.containsKey(reference);
             }
         });
 
         when(contentProducer.getSubType(anyString())).then(new Answer<String>() {
             public String answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getSubtype();
             }
         });
         when(contentProducer.getSiteContentIterator(anyString())).then(new Answer<Iterator<String>>() {
             public Iterator<String> answer(InvocationOnMock invocation) throws Throwable {
                 String siteId = (String) invocation.getArguments()[0];
                 return siteToDocuments.get(siteId).iterator();
             }
         });
         when(contentProducer.canRead(anyString())).then(new Answer<Boolean>() {
             public Boolean answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.containsKey(reference);
             }
         });
         when(contentProducer.getContainer(anyString())).then(new Answer<String>() {
             public String answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getContainer();
             }
         });
         when(contentProducer.getSiteId(anyString())).then(new Answer<String>() {
             public String answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getSiteId();
             }
         });
         when(contentProducer.getTitle(anyString())).then(new Answer<String>() {
             public String answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getTitle();
             }
         });
         when(contentProducer.getType(anyString())).then(new Answer<String>() {
             public String answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getType();
             }
         });
         when(contentProducer.getId(anyString())).then(new Answer<String>() {
             public String answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getId();
             }
         });
         when(contentProducer.getUrl(anyString())).then(new Answer<String>() {
             public String answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getUrl();
             }
         });
         when(contentProducer.getCustomProperties(anyString())).then(new Answer<Map<String, Object>>() {
             public Map<String, Object> answer(InvocationOnMock invocation) throws Throwable {
                 String reference = (String) invocation.getArguments()[0];
                 return documentReferenceToDocument.get(reference).getProperties();
             }
         });
 
         if (producerType == ProducerType.STRING)
             when(contentProducer.getContent(anyString())).then(new Answer<String>() {
                 public String answer(InvocationOnMock invocation) throws Throwable {
                     String reference = (String) invocation.getArguments()[0];
                     return documentReferenceToDocument.get(reference).getContent();
                 }
             });
         else if (producerType == ProducerType.READER)
             when(contentProducer.getContentReader(anyString())).then(new Answer<Reader>() {
                 public Reader answer(InvocationOnMock invocation) throws Throwable {
                     String reference = (String) invocation.getArguments()[0];
                     String content = documentReferenceToDocument.get(reference).getContent();
                     return new StringReader(content);
                 }
             });
         else if (producerType == ProducerType.STREAM) {
             BinaryEntityContentProducer bProducer = (BinaryEntityContentProducer) contentProducer;
             when(bProducer.getContentStream(anyString())).then(new Answer<InputStream>() {
                 public InputStream answer(InvocationOnMock invocation) throws Throwable {
                     String reference = (String) invocation.getArguments()[0];
                     String content = documentReferenceToDocument.get(reference).getContent();
                     return new ByteArrayInputStream(content.getBytes());
                 }
             });
         }
     }
 
     /**
      * Prepares a ProducerBuilder to mock a simple {@link EntityContentProducer}.
      * <p>
      * The tool name will be an automatically generated UUID.
      * </p>
      *
      * @return a ProducerBuilder allowing to chain calls until {@link #build()} is called.
      */
     public static ProducerBuilder create() {
         return create(ProducerType.STRING);
     }
 
     /**
      * Prepares a ProducerBuilder to mock an {@link EntityContentProducer} of a specific type.
      * <p>
      * The tool name will be an automatically generated UUID.
      * </p>
      *
      * @return a ProducerBuilder allowing to chain calls until {@link #build()} is called.
      */
     public static ProducerBuilder create(ProducerType producerType) {
         return create(producerType, UUID.randomUUID().toString());
     }
 
     /**
      * Prepares a ProducerBuilder to mock an {@link EntityContentProducer} of a specific type
      * and a given tool name.
      *
      * @return a ProducerBuilder allowing to chain calls until {@link #build()} is called.
      */
     public static ProducerBuilder create(ProducerType producerType, String toolName) {
         return new ProducerBuilder(producerType, toolName);
     }
 
     //---------------------------------------------------------------------------------------------------------
     // Manage single documents
     //---------------------------------------------------------------------------------------------------------
 
     /**
      * Adds a single document to the EntityContentProducer.
      * <p>
      * The document already provides every required information (siteId, reference) for the insertion and shouldn't
      * be modified afterwards.
      * </p>
      *
      * @param newDocument document handled by the EntityContentProducer.
      * @return the current builder for chained calls.
      */
     public ProducerBuilder addDoc(Document newDocument) {
         String reference = newDocument.getReference();
         Document oldDocument = documentReferenceToDocument.put(reference, newDocument);
         if (oldDocument != null) {
             getDocsForSite(oldDocument.getSiteId()).remove(reference);
         }
         getDocsForSite(newDocument.getSiteId()).add(reference);
 
         return this;
     }
 
     /**
      * Adds a single document to the EntityContentProducer.
      * <p>
      * Generate a document with the given reference for the given site.<br />
      * If a document with the same reference already exists, it will be overridden by the new document.
      * </p>
      *
      * @param reference reference of the new document.
      * @param siteId    site to which the document is attached.
      * @return the current builder for chained calls.
      */
     public ProducerBuilder addDoc(String reference, String siteId) {
         Document document = new Document();
         document.setReference(reference);
         document.setSiteId(siteId);
         document.setContainer(reference + "Container");
         document.setId(reference + "Id");
         document.setSubtype(reference + "SubType");
         document.setUrl(reference + "Url");
         document.setType(reference + "Type");
         document.setContent(reference + "Content");
         document.setTitle(reference + "Title");
         document.setProperties(Collections.<String, Object>emptyMap());
 
         return addDoc(document);
     }
 
     /**
      * Adds a single document to the EntityContentProducer.
      * <p>
      * Generate a document with the given reference for a random site.<br />
      * If a document with the same reference already exists, it will be overridden by the new document.
      * </p>
      *
      * @param reference reference of the new document.
      * @return the current builder for chained calls.
      */
     public ProducerBuilder addDoc(String reference) {
         return addDoc(reference, UUID.randomUUID().toString());
     }
 
     /**
      * Removes a document from the EntityContentProducer.
      *
      * @param reference reference of the document to remove
      * @return the current builder for chained calls.
      */
     public ProducerBuilder removeDoc(String reference) {
         if (documentReferenceToDocument.containsKey(reference)) {
             Document document = documentReferenceToDocument.get(reference);
             documentReferenceToDocument.remove(reference);
             siteToDocuments.get(document.getSiteId()).remove(reference);
         }
         return this;
     }
 
     //---------------------------------------------------------------------------------------------------------
     // Manage site documents
     //---------------------------------------------------------------------------------------------------------
 
     /**
      * Generates multiple documents for a site.
      *
      * @param siteId            site in which the documents should be created.
      * @param numberOfDocuments number of documents to generate.
      * @return the current builder for chained calls.
      */
    public ProducerBuilder addDocsToSite(String siteId, int numberOfDocuments) {
         for (int i = 0; i < numberOfDocuments; i++) {
             addDoc(UUID.randomUUID().toString(), siteId);
         }
         return this;
     }
 
     /**
      * Removes every document from a site.
      *
      * @param siteId site from which the documents should be removed.
      * @return the current builder for chained calls.
      */
     public ProducerBuilder emptySite(String siteId) {
         for (Iterator<String> iterator = siteToDocuments.get(siteId).iterator(); iterator.hasNext(); ) {
             String reference = iterator.next();
             iterator.remove();
             removeDoc(reference);
         }
 
         return this;
     }
 
     //---------------------------------------------------------------------------------------------------------
     // Manage events
     //---------------------------------------------------------------------------------------------------------
 
     /**
      * Adds a new event type to handle.
      *
      * @param eventType name of the event to handle with the EntityContentProducer.
      * @return the current builder for chained calls.
      */
     public ProducerBuilder addEvent(String eventType) {
         supportedEvents.add(eventType);
         return this;
     }
 
     /**
      * Removes an event type to stop handling it.
      *
      * @param eventType name of the event handled by the EntityContentProducer.
      * @return the current builder for chained calls.
      */
     public ProducerBuilder removeEvent(String eventType) {
         supportedEvents.remove(eventType);
         return this;
     }
 
     //---------------------------------------------------------------------------------------------------------
     // Access to informations
     //---------------------------------------------------------------------------------------------------------
 
     /**
      * Gets the list of every document available in a site (unmodifiable).
      *
      * @param siteId id of the site in which the documents are.
      * @return every document contained in the site.
      */
     public Set<String> getSiteDocs(String siteId) {
         return Collections.unmodifiableSet(getDocsForSite(siteId));
     }
 
     /**
      * Gets the reference of every supported document (unmodifiable).
      *
      * @return every document's reference currently handled.
      */
     public Set<String> getDocReferences() {
         return Collections.unmodifiableSet(documentReferenceToDocument.keySet());
     }
 
     /**
      * Gets every document (unmodifiable).
      *
      * @return every document currently handled.
      */
     public Set<Document> getDocs() {
         return Collections.unmodifiableSet(new HashSet<Document>(documentReferenceToDocument.values()));
     }
 
     /**
      * Gets the supported events (unmodifiable).
      *
      * @return every supported event type.
      */
     public Set<String> getEvents() {
         return Collections.unmodifiableSet(supportedEvents);
     }
 
     /**
      * Gets the document's references for a site (modifiable).
      * <p>
      * If the site doesn't exist yet, create it.
      * </p>
      *
      * @param siteId id of the site in which the documents are.
      * @return documents
      */
     private Set<String> getDocsForSite(String siteId) {
         Set<String> documents = siteToDocuments.get(siteId);
         if (documents == null) {
             documents = new HashSet<String>();
             siteToDocuments.put(siteId, documents);
         }
         return documents;
     }
 
     public EntityContentProducer build() {
         return contentProducer;
     }
 
     public static enum ProducerType {
         STRING,
         READER,
         STREAM
     }
 
     public final static class Document implements Cloneable {
         private String reference;
         private String id;
         private String type;
         private String subtype;
         private String container;
         private String siteId;
         private String url;
         private String content;
         private String title;
         private Map<String, Object> properties;
 
         public String getReference() {
             return reference;
         }
 
         public void setReference(String reference) {
             this.reference = reference;
         }
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public String getType() {
             return type;
         }
 
         public void setType(String type) {
             this.type = type;
         }
 
         public String getSubtype() {
             return subtype;
         }
 
         public void setSubtype(String subtype) {
             this.subtype = subtype;
         }
 
         public String getContainer() {
             return container;
         }
 
         public void setContainer(String container) {
             this.container = container;
         }
 
         public String getSiteId() {
             return siteId;
         }
 
         public void setSiteId(String siteId) {
             this.siteId = siteId;
         }
 
         public String getUrl() {
             return url;
         }
 
         public void setUrl(String url) {
             this.url = url;
         }
 
         public String getContent() {
             return content;
         }
 
         public void setContent(String content) {
             this.content = content;
         }
 
         public Map<String, Object> getProperties() {
             return properties;
         }
 
         public void setProperties(Map<String, Object> properties) {
             this.properties = properties;
         }
 
         public String getTitle() {
             return title;
         }
 
         public void setTitle(String title) {
             this.title = title;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Document document = (Document) o;
 
             return reference.equals(document.reference);
 
         }
 
         @Override
         public int hashCode() {
             return reference.hashCode();
         }
     }
 }
