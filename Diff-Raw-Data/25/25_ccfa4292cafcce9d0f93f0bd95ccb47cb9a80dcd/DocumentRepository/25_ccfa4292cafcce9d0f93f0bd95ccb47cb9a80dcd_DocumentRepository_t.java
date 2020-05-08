 package com.mlj.documents;
 
import com.google.common.collect.Lists;
 import com.mlj.events.Event;
 import com.mlj.events.EventStore;
 
 import java.util.*;
 
 import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;
 
 public class DocumentRepository {
 
     private final EventStore eventStore;
 
     public DocumentRepository(EventStore eventStore) {
         this.eventStore = eventStore;
     }
 
     public Document registerDocument(RegisterDocumentCommand registerDocumentCommand) {
         Document document = new Document(UUID.randomUUID().toString()).register(registerDocumentCommand);
         store(document);
         return document;
     }
 
     public Document supersedeDocument(String id, SupersedeDocumentCommand supersedeDocumentCommand) {
         Document document = retrieveDocument(id).supersede(supersedeDocumentCommand);
         store(document);
         return document;
     }
 
     private void store(Document document) {
         eventStore.store(document);
     }
 
     public Document retrieveDocument(String id) {
         return createDocument(id, eventStore.eventsFor(id));
     }
 
     public List<Document> allDocuments() {
         List<Document> result = new ArrayList<Document>();
         for (Map.Entry<String, Collection<Event>> events : eventStore.allEvents().entrySet()) {
             result.add(createDocument(events.getKey(), events.getValue()));
         }
         return result;
     }
 
     private Document createDocument(String id, Collection<Event> events) {
         Document document = new Document(id);
         for (Event event : events) {
             if (event instanceof DocumentRegisteredEvent) {
                 document = document.apply((DocumentRegisteredEvent) event);
             } else if (event instanceof DocumentSupersededEvent) {
                 document = document.apply((DocumentSupersededEvent) event);
             }
         }
         document.markCommitted();
         return document;
 
     }
 
     public List<Document> historyFor(String documentId) {
         List<Document> documentHistory = newArrayList();
         Document document = new Document(documentId);
         for (Event event : eventStore.eventsFor(documentId)) {
             if (event instanceof DocumentRegisteredEvent) {
                 document = document.apply((DocumentRegisteredEvent) event);
             } else if (event instanceof DocumentSupersededEvent) {
                 document = document.apply((DocumentSupersededEvent) event);
             }
             document.markCommitted();
             documentHistory.add(document);
         }
        return reverse(documentHistory);
     }
 
 }
