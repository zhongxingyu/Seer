 /*
  * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  */
 package org.nuxeo.ecm.platform.semanticentities.service;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.event.EventService;
 import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
 import org.nuxeo.ecm.platform.semanticentities.Constants;
 import org.nuxeo.ecm.platform.semanticentities.LocalEntityService;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntityService;
 import org.nuxeo.ecm.platform.semanticentities.SemanticAnalysisService;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceGroup;
 import org.nuxeo.runtime.api.Framework;
 
 public class SemanticAnalysisServiceTest extends SQLRepositoryTestCase {
 
     @SuppressWarnings("unused")
     private static final Log log = LogFactory.getLog(SemanticAnalysisServiceTest.class);
 
     private DocumentModel john;
 
     private DocumentModel johndoe;
 
     private DocumentModel beatles;
 
     private DocumentModel liverpool;
 
     private LocalEntityService leService;
 
     private RemoteEntityService reService;
 
     private SemanticAnalysisService saService;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         // necessary for the fulltext indexer and text extraction for analysis
         deployBundle("org.nuxeo.ecm.core.convert.api");
         deployBundle("org.nuxeo.ecm.core.convert");
         deployBundle("org.nuxeo.ecm.core.convert.plugins");
 
         // dublincore contributors are required to check whether non system
         // users have edited an entity or not
         deployBundle("org.nuxeo.ecm.platform.dublincore");
 
         // semantic entities types
         deployBundle("org.nuxeo.ecm.platform.semanticentities.core");
 
         // deploy off-line mock for the semantic analysis service
         deployContrib("org.nuxeo.ecm.platform.semanticentities.core.tests",
                 "OSGI-INF/test-semantic-entities-analysis-service.xml");
 
         // deploy off-line mock DBpedia source to override the default source
         // that needs an internet connection: comment the following contrib to
         // test again a real Stanbol server
         Framework.getProperties().put(
                 SemanticAnalysisServiceImpl.STANBOL_URL_PROPERTY,
                 "http://localhost:9090/");
         deployContrib("org.nuxeo.ecm.platform.semanticentities.core.tests",
                 "OSGI-INF/test-semantic-entities-stanbol-entity-contrib.xml");
 
         // CMIS query maker
         deployBundle("org.nuxeo.ecm.core.opencmis.impl");
 
         // initialize the session field
         openSession();
         DocumentModel domain = session.createDocumentModel("/",
                 "default-domain", "Folder");
         session.createDocument(domain);
         session.save();
 
         leService = Framework.getService(LocalEntityService.class);
         assertNotNull(leService);
 
         reService = Framework.getService(RemoteEntityService.class);
         assertNotNull(reService);
 
         saService = Framework.getService(SemanticAnalysisService.class);
         assertNotNull(saService);
     }
 
     @Override
     public void tearDown() throws Exception {
         closeSession();
         super.tearDown();
     }
 
     public void makeSomeEntities() throws ClientException {
         DocumentModel container = leService.getEntityContainer(session);
         assertNotNull(container);
         assertEquals(Constants.ENTITY_CONTAINER_TYPE, container.getType());
 
         john = session.createDocumentModel(container.getPathAsString(), null,
                 "Person");
         john.setPropertyValue("dc:title", "John Lennon");
         john.setPropertyValue(
                 "entity:summary",
                 "John Winston Ono Lennon, MBE (9 October 1940 – 8 December 1980)"
                         + " was an English rock musician, singer-songwriter, author, and peace"
                         + " activist who gained worldwide fame as one of the founding members of"
                         + " The Beatles.");
         john.setPropertyValue(
                 "entity:sameas",
                 (Serializable) Arrays.asList("http://dbpedia.org/resource/John_Lennon"));
         john.setPropertyValue("entity:sameasDisplayLabel",
                 (Serializable) Arrays.asList("John Lennon"));
         john.setPropertyValue(
                 "entity:types",
                 (Serializable) Arrays.asList("http://dbpedia.org/ontology/MusicalArtist"));
         john.setPropertyValue("person:birthDate", new GregorianCalendar(1940,
                 10, 9));
         john.setPropertyValue("person:birthDate", new GregorianCalendar(1980,
                 12, 8));
         john = session.createDocument(john);
 
         // add another john
         johndoe = session.createDocumentModel(container.getPathAsString(),
                 null, "Person");
         johndoe.setPropertyValue("dc:title", "John Doe");
         johndoe = session.createDocument(johndoe);
 
         beatles = session.createDocumentModel(container.getPathAsString(),
                 null, "Organization");
         beatles.setPropertyValue("dc:title", "The Beatles");
         beatles.setPropertyValue(
                 "entity:summary",
                 "The Beatles were an English rock band, formed in Liverpool in 1960"
                         + " and one of the most commercially successful and critically acclaimed"
                         + " acts in the history of popular music.");
 
         beatles.setPropertyValue(
                 "entity:sameas",
                 (Serializable) Arrays.asList("http://dbpedia.org/resource/The_Beatles"));
         beatles.setPropertyValue("entity:sameasDisplayLabel",
                 (Serializable) Arrays.asList("The Beatles"));
         beatles.setPropertyValue(
                 "entity:types",
                 (Serializable) Arrays.asList("http://dbpedia.org/ontology/Band"));
         beatles = session.createDocument(beatles);
 
         liverpool = session.createDocumentModel(container.getPathAsString(),
                 null, "Place");
         liverpool.setPropertyValue("dc:title", "Liverpool");
         liverpool.setPropertyValue(
                 "entity:summary",
                 "Liverpool is a city and metropolitan borough of Merseyside, England, along"
                         + " the eastern side of the Mersey Estuary. It was founded as a borough"
                         + " in 1207 and was granted city status in 1880.");
 
         liverpool.setPropertyValue(
                 "entity:sameas",
                 (Serializable) Arrays.asList("http://dbpedia.org/resource/Liverpool"));
         liverpool.setPropertyValue("entity:sameasDisplayLabel",
                 (Serializable) Arrays.asList("http://Liverpool"));
         liverpool.setPropertyValue(
                 "entity:types",
                 (Serializable) Arrays.asList("http://dbpedia.org/ontology/City"));
         liverpool.setPropertyValue("place:latitude", 53.4);
         liverpool.setPropertyValue("place:longitude", -2.983);
         liverpool = session.createDocument(liverpool);
         session.save();
         Framework.getLocalService(EventService.class).waitForAsyncCompletion();
     }
 
     public DocumentModel createSampleDocumentModel(String id)
             throws ClientException {
         return createSampleDocumentModel(id, true);
     }
 
     public DocumentModel createSampleDocumentModel(String id,
             boolean saveAndWait) throws ClientException {
         DocumentModel doc = session.createDocumentModel("/", id, "Note");
         doc.setPropertyValue("dc:title", "A short bio for John Lennon");
         doc.setPropertyValue(
                 "note:note",
                 "<html><body>"
                         + "<h1>This is an HTML title</h1>"
                         + "<p>John Lennon was born in Liverpool in 1940. John was a musician."
                         + " This document about John Lennon has many occurrences"
                         + " of the words 'John' and 'Lennon' hence should rank high"
                         + " for suggestions on such keywords.</p>"
 
                         + "<!-- this is a HTML comment about Bob Marley. -->"
                         + " </body></html>");
         doc = session.createDocument(doc);
         if (saveAndWait) {
             session.save(); // force write to SQL backend
             Framework.getLocalService(EventService.class).waitForAsyncCompletion(
                     1000 * 10);
         }
         return doc;
     }
 
     public void testAsyncAnalysis() throws Exception {
         EventService es = Framework.getLocalService(EventService.class);
         List<DocumentModel> docs = new ArrayList<DocumentModel>();
         for (int i = 0; i < 5; i++) {
             docs.add(createSampleDocumentModel(String.format("john-bio-%d", i),
                     false));
         }
         session.save(); // force write to SQL backend
         es.waitForAsyncCompletion();
 
         // launch asynchronous analysis on each documents (in concurrently using
         // the thread pool executors)
         for (DocumentModel doc : docs) {
             saService.launchAnalysis(doc.getRepositoryName(), doc.getRef());
         }
 
         // wait for all the analysis to complete
         for (DocumentModel doc : docs) {
             while (saService.getProgressStatus(doc.getRepositoryName(),
                     doc.getRef()) != null) {
                 Thread.sleep(200);
             }
         }
         es.waitForAsyncCompletion();
 
         // check the results of the analysis
         for (DocumentModel doc : docs) {
             // the same entities are linked to all the docs
             checkRelatedEntities(doc);
         }
     }
 
     public void testSynchronousAnalysis() throws Exception {
         DocumentModel doc = createSampleDocumentModel("john-bio1");
         saService.launchSynchronousAnalysis(doc, session);
         checkRemoveRelatedEntities(doc);
     }
 
     public void testSimpleAnalysis() throws Exception {
         makeSomeEntities();
 
         DocumentModel doc = createSampleDocumentModel("john-bio1");
         List<OccurrenceGroup> groups = saService.analyze(session, doc);
         assertEquals(2, groups.size());
 
         OccurrenceGroup og1 = groups.get(0);
         assertEquals("John Lennon", og1.name);
         assertEquals("Person", og1.type);
 
         assertEquals(5, og1.occurrences.size());
         assertEquals("John Lennon", og1.occurrences.get(0).mention);
         assertEquals("John Lennon", og1.occurrences.get(1).mention);
         assertEquals("John", og1.occurrences.get(2).mention);
         assertEquals("John", og1.occurrences.get(3).mention);
         assertEquals("John Lennon", og1.occurrences.get(4).mention);
 
         assertEquals(1, og1.entitySuggestions.size());
         assertEquals("John Lennon", og1.entitySuggestions.get(0).label);
         assertEquals("http://dbpedia.org/resource/John_Lennon",
                 og1.entitySuggestions.get(0).remoteEntityUris.iterator().next());
         assertFalse(og1.entitySuggestions.get(0).isLocal());
 
         // the entity is directly prefetched from the entityhub by the analysis
         // engine on stanbol
         assertNotNull(og1.entitySuggestions.get(0).entity);
 
         OccurrenceGroup og2 = groups.get(1);
         assertEquals("Liverpool", og2.name);
         assertEquals("Place", og2.type);
 
         assertEquals(1, og2.occurrences.size());
         assertEquals("Liverpool", og2.occurrences.get(0).mention);
 
         assertEquals(3, og2.entitySuggestions.size());
         assertEquals("Liverpool", og2.entitySuggestions.get(0).label);
         assertEquals("http://dbpedia.org/resource/Liverpool",
                 og2.entitySuggestions.get(0).remoteEntityUris.iterator().next());
         assertFalse(og2.entitySuggestions.get(0).isLocal());
 
         // there is pre-fetched data in the payload
         DocumentModel liverpool = og2.entitySuggestions.get(0).entity;
         assertNotNull(liverpool);
         assertEquals("Liverpool", liverpool.getTitle());
         assertEquals("Place", liverpool.getType());
         assertEquals(Arrays.asList("http://dbpedia.org/resource/Liverpool"),
                 liverpool.getProperty("entity:sameas").getValue(List.class));
         assertEquals(
                 "Liverpool is a city and metropolitan borough of Merseyside,"
                         + " England, along the eastern side of the Mersey Estuary. It "
                         + "was founded as a borough in 1207 and was granted city status "
                         + "in 1880. Liverpool is the fourth largest city in the United "
                         + "Kingdom (third largest in England) and has a population of "
                         + "435,500, and lies at the centre of the wider Liverpool Urban "
                         + "Area, which has a population of 816,216.",
                 liverpool.getPropertyValue("entity:summary"));
     }
 
     protected void checkRelatedEntities(DocumentModel doc)
             throws ClientException {
         List<DocumentModel> relatedPeople = leService.getRelatedEntities(
                 session, doc.getRef(), "Person").getCurrentPage();
         assertEquals(
                 String.format(doc.getPathAsString()
                         + " should have been linked to an entity"), 1,
                 relatedPeople.size());
         DocumentModel firstPerson = relatedPeople.get(0);
         assertEquals("John Lennon", firstPerson.getTitle());
 
         List<DocumentModel> relatedPlaces = leService.getRelatedEntities(
                 session, doc.getRef(), "Place").getCurrentPage();
         assertEquals(1, relatedPlaces.size());
         assertEquals("Liverpool", relatedPlaces.get(0).getTitle());
     }
 
     /**
      * Check that the expected relations are there and that removal of the link
      * also remove automatically created entities
      */
     protected void checkRemoveRelatedEntities(DocumentModel doc)
             throws ClientException {
         List<DocumentModel> relatedPeople = leService.getRelatedEntities(
                 session, doc.getRef(), "Person").getCurrentPage();
         assertEquals(
                 String.format(doc.getPathAsString()
                         + " should have been linked to an entity"), 1,
                 relatedPeople.size());
         DocumentModel firstPerson = relatedPeople.get(0);
         assertEquals("John Lennon", firstPerson.getTitle());
 
         // check removal of the link
         leService.removeOccurrences(session, doc.getRef(),
                 firstPerson.getRef(), false);
         if (session.exists(firstPerson.getRef())) {
             assertEquals("deleted",
                     session.getCurrentLifeCycleState(firstPerson.getRef()));
         } else {
             fail(firstPerson.getTitle() + " should have been deleted");
         }
 
         List<DocumentModel> relatedPlaces = leService.getRelatedEntities(
                 session, doc.getRef(), "Place").getCurrentPage();
         assertEquals(1, relatedPlaces.size());
         DocumentModel firstPlace = relatedPlaces.get(0);
         assertEquals("Liverpool", firstPlace.getTitle());
 
         // check removal of the link
         leService.removeOccurrences(session, doc.getRef(), firstPlace.getRef(),
                 false);
         if (session.exists(firstPlace.getRef())) {
             assertEquals("deleted",
                     session.getCurrentLifeCycleState(firstPlace.getRef()));
         } else {
             fail(firstPlace.getTitle() + " should have been deleted");
         }
     }
 
     public void testTextExtract() throws ClientException {
         DocumentModel doc = session.createDocumentModel("/",
                 "docWithControlChars", "Note");
         doc.setPropertyValue("dc:title", "A short bio for John Lennon");
         doc.setPropertyValue("dc:description",
                 "'\ud800\udc00' is a valid character outside of the BMP that should be kept.");
         doc.setPropertyValue(
                 "note:note",
                 "<html><body>"
                         + "<h1>This is an HTML title</h1>"
                         + "<p>John Lennon was born in Liverpool in 1940. John was a musician."
                         + " This document about John Lennon has many occurrences"
                         + " of the words 'John' and 'Lennon' hence should rank high"
                         + " for suggestions on such keywords.</p>"
                         + "<p>'\uFFFE' is an invalid control char and should be ignored.</p>"
                         + "<!-- this is a HTML comment about Bob Marley. -->"
                         + "</body></html>");
         SemanticAnalysisServiceImpl sasi = (SemanticAnalysisServiceImpl) saService;
         String extractedText = sasi.extractText(doc);
         assertEquals(
                 "A short bio for John Lennon\n\n"
                         + "'\ud800\udc00' is a valid character outside of the BMP that should be kept.\n\n"
                         + "This is an HTML title\n\n"
                        + "John Lennon was born in Liverpool in 1940. John was a"
                        + " musician. This document about John Lennon has many"
                        + " occurrences of the words 'John' and 'Lennon' hence should"
                        + " rank high for suggestions on such keywords.\n\n"
                        + "'' is an invalid control char and should be ignored.\n\n"
                        + "\n\n", extractedText);
     }
 }
