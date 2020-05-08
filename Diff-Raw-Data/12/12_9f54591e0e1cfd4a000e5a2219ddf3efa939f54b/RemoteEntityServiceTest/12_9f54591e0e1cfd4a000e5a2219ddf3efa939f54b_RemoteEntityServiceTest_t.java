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
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 
 import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntity;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntityService;
 import org.nuxeo.runtime.api.Framework;
 
 public class RemoteEntityServiceTest extends SQLRepositoryTestCase {
 
     RemoteEntityService service;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         // necessary for the fulltext indexer
         deployBundle("org.nuxeo.ecm.core.convert.api");
         deployBundle("org.nuxeo.ecm.core.convert");
         deployBundle("org.nuxeo.ecm.core.convert.plugins");
 
         // semantic entities types
         deployBundle("org.nuxeo.ecm.platform.semanticentities.core");
 
         // deploy off-line mock DBpedia source to override the default source
         // that needs an internet connection: comment the following contrib to
         // test again the real DBpedia server
         deployContrib("org.nuxeo.ecm.platform.semanticentities.core.tests",
                 "OSGI-INF/test-semantic-entities-remote-entity-contrib.xml");
 
         // initialize the session field
         openSession();
 
         service = Framework.getService(RemoteEntityService.class);
         assertNotNull(service);
     }
 
     public void testSuggestRemoteEntity() throws IOException {
         assertTrue(service.canSuggestRemoteEntity());
         List<RemoteEntity> suggestions = service.suggestRemoteEntity(
                 "the 44th president of the United States", "Person", 3);
         assertNotNull(suggestions);
         assertEquals(1, suggestions.size());
 
         RemoteEntity suggested = suggestions.get(0);
         assertEquals("Barack Obama", suggested.label);
         assertEquals(URI.create("http://dbpedia.org/resource/Barack_Obama"),
                 suggested.uri);
 
         // this should also work for a null type
         suggestions = service.suggestRemoteEntity(
                 "the 44th president of the United States", null, 3);
         assertNotNull(suggestions);
         assertEquals(1, suggestions.size());
 
         suggested = suggestions.get(0);
         assertEquals("Barack Obama", suggested.label);
         assertEquals(URI.create("http://dbpedia.org/resource/Barack_Obama"),
                 suggested.uri);
 
         // however no organization should match this name
         suggestions = service.suggestRemoteEntity(
                 "the 44th president of the United States", "Place", 3);
         assertNotNull(suggestions);
 
         // XXX: the QueryClass is apparently no implemented on the
         // lookup.dbpedia.org service, hence the restriction does not work
         assertEquals(1, suggestions.size());
     }
 
    public void testCanDereferenceRemoteEntity() throws Exception {
        assertTrue(service.canDereference(URI.create("http://dbpedia.org/resource/London")));
        assertFalse(service.canDereference(URI.create("http://en.wikipedia.org/wiki/London")));
    }

 }
