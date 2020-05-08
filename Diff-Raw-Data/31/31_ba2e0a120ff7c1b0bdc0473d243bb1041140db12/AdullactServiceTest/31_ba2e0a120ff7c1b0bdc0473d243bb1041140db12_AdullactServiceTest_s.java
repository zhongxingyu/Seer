 /*
  * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
  *     Benjamin JALON
  */
 package org.nuxeo.adullact.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.nuxeo.adullact.test.AdullactTestHelper.loadProperties;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.nuxeo.adullact.AdullactDossier;
 import org.nuxeo.adullact.service.AdullactService;
 import org.nuxeo.adullact.jaxws.GetDossierResponse;
 import org.nuxeo.adullact.jaxws.Visibilite;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
 import org.nuxeo.ecm.core.test.CoreFeature;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.test.runner.Deploy;
 import org.nuxeo.runtime.test.runner.Features;
 import org.nuxeo.runtime.test.runner.FeaturesRunner;
 import org.nuxeo.runtime.test.runner.RuntimeHarness;
 
 import com.google.inject.Inject;
 
 /**
  * @author <a href="mailto:bjalon@nuxeo.com">Benjamin JALON</a>
  *
  */
 @RunWith(FeaturesRunner.class)
 @Features(CoreFeature.class)
 @Deploy({ "nuxeo-adullact-iparapheur-integration:OSGI-INF/adullact-coretype-contrib.xml" })
 public class AdullactServiceTest {
 
     private AdullactService service;
 
     private DocumentModel file;
 
     @Inject
     public RuntimeHarness runtimeHarness;
 
     @Inject
     protected CoreSession session;
 
     @Before
     public void init() throws Exception {
         loadProperties();
 
         runtimeHarness.deployContrib("nuxeo-adullact-iparapheur-integration",
                 "OSGI-INF/extensions/org.nuxeo.adullact.service.AdullactService.xml");
         runtimeHarness.deployContrib("nuxeo-adullact-iparapheur-integration",
                 "OSGI-INF/extensions/org.nuxeo.adullact.AdullactAdapterFactory.xml");
 
         file = null;
         service = Framework.getService(AdullactService.class);
     }
 
     @Test
     public void shouldCreateNewFolderAndFetchInformation()
             throws ClientException {
         file = session.createDocumentModel("/", "test", "File");
         file = session.createDocument(file);
         session.save();
 
         file.addFacet("AdullactDossier");
         file = session.saveDocument(file);
         session.save();
        
         file.setPropertyValue("adudos:typeTechnique", "Bureautique");
         file.setPropertyValue("adudos:sousType", "Document");
         file.setPropertyValue("adudos:visibilite", "PUBLIC");
         Blob content = new StringBlob("Test, Ceci est un test", "text/plain");
 
         file.setPropertyValue("file:content", (Serializable) content);
         file.setPropertyValue("file:content", new StringBlob("Test"));
         file = session.saveDocument(file);
         session.save();
 
         AdullactDossier adapter = file.getAdapter(AdullactDossier.class);
 
        
         service.creerDossier(adapter);
 
         DocumentModelList histoEntries = service.getHistogrammeDossier(file.getId());
         assertEquals(3, histoEntries.size());
         assertEquals("Application GED",
                 histoEntries.get(0).getPropertyValue("aduhisto:nom"));
         assertEquals("NonLu",
                 histoEntries.get(0).getPropertyValue("aduhisto:status"));
         assertEquals("Creation de dossier",
                 histoEntries.get(0).getPropertyValue("aduhisto:annotation"));
         assertEquals("Application GED",
                 histoEntries.get(1).getPropertyValue("aduhisto:nom"));
         ;
         assertEquals("NonLu",
                 histoEntries.get(1).getPropertyValue("aduhisto:status"));
         assertEquals("Emission du dossier",
                 histoEntries.get(1).getPropertyValue("aduhisto:annotation"));
         assertEquals("Application GED",
                 histoEntries.get(2).getPropertyValue("aduhisto:nom"));
         assertEquals("NonLu",
                 histoEntries.get(2).getPropertyValue("aduhisto:status"));
        assertEquals("Dossier déposé chez Non-attribué pour signature",
                 histoEntries.get(2).getPropertyValue("aduhisto:annotation"));
     }
 
     @Test
     public void shouldRemordRemoveDossier() throws ClientException {
         shouldCreateNewFolderAndFetchInformation();
 
         // Remord quand tu nous tiens
         service.exercerDroitRemordDossier(file.getId());
 
         try {
             service.exercerDroitRemordDossier(file.getId());
             fail("Waiting exception as droit remord already done");
         } catch (ClientException e) {
            String responseMsgPattern = "request failed: Le dossierID '%s' n'a pas été émis.";
             String responseMsg = String.format(responseMsgPattern, file.getId());
             assertEquals(responseMsg, e.getMessage());
         }
     }
 
     @Test
     public void shouldGetDossier() throws ClientException {
    	shouldRemordRemoveDossier();
    	GetDossierResponse doc = service.getDossier(file.getId());
    	assertEquals("Bureautique",
    			doc.getTypeTechnique());
    	assertEquals("Document",
    			doc.getSousType());
    	assertEquals(Visibilite.PUBLIC,
    			doc.getVisibilite());
     }
    
     @Test
     public void shouldArchiveDossierFailed() throws ClientException {
         shouldCreateNewFolderAndFetchInformation();
 
         AdullactDossier dossier = file.getAdapter(AdullactDossier.class);
 
         try {
             service.archiverDossier(dossier.getDossierID(), "ARCHIVER");
             fail("Waiting exception as dossier not archivable");
         } catch (ClientException e) {
             String responseMsgPattern = "request failed: Le dossierID '%s' n'est pas archivable.";
             String responseMsg = String.format(responseMsgPattern,
                     dossier.getDossierID());
             assertEquals(responseMsg, e.getMessage());
         }
 
         DocumentModelList histoEntries = service.getHistogrammeDossier(dossier.getDossierID());
         assertEquals(3, histoEntries.size());
 
     }
 
     @Test
     public void shouldReturnListeTypeTechnique() throws ClientException {
 
         List<String> types = service.getListeTypes();
         assertEquals(1, types.size());
         assertEquals("Bureautique", types.get(0));
 
     }
 
     @Test
     public void shouldReturnListeSousType() throws ClientException {
 
         List<String> sousTypes = service.getListeSousTypes("Bureautique");
         assertEquals(3, sousTypes.size());
         assertEquals("Document", sousTypes.get(0));
         assertEquals("Courrier", sousTypes.get(1));
         assertEquals("Note de Service", sousTypes.get(2));
     }
 
     @Test
     public void shouldSendEcho() throws Exception {
 
         String message = "test";
         String response = service.echo(message);
         assertEquals("[webservice@nuxeo.com] m'a dit: \"test\"!", response);
     }
 
 }
