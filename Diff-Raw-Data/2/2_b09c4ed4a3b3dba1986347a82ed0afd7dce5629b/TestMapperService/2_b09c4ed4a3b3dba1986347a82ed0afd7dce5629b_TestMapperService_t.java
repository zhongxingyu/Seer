 /*
  * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Benjamin JALON<bjalon@nuxeo.com>
  */
 package org.nuxeo.adullact.webdelib.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.nuxeo.adullact.webdelib.WebDelibConstants.DOC_TYPE_ACTE;
 import static org.nuxeo.adullact.webdelib.WebDelibConstants.DOC_TYPE_SEANCE;
 import static org.nuxeo.adullact.webdelib.WebDelibConstants.DOC_TYPE_SIGNATURE;
 import static org.nuxeo.adullact.webdelib.WebDelibConstants.STUDIO_SYMBOLIC_NAME;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.nuxeo.adullact.importer.XmlImporterSevice;
 import org.nuxeo.common.utils.FileUtils;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.test.CoreFeature;
 import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
 import org.nuxeo.ecm.core.test.annotations.Granularity;
 import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.test.runner.Deploy;
 import org.nuxeo.runtime.test.runner.Features;
 import org.nuxeo.runtime.test.runner.FeaturesRunner;
 
 import com.google.inject.Inject;
 
 @RunWith(FeaturesRunner.class)
 @Features(CoreFeature.class)
 @Deploy({ "org.nuxeo.adullact.importer",
         "org.nuxeo.ecm.platform.content.template",
         "nuxeo-adullact-webdelib-core", STUDIO_SYMBOLIC_NAME })
 @RepositoryConfig(cleanup = Granularity.METHOD, init = DefaultRepositoryInit.class)
 public class TestMapperService {
 
     public static final String DATE_FORMAT = "%d-%02d-%02d %02d:%02d:%02d";
 
     @Inject
     CoreSession session;
 
     @Inject
     XmlImporterSevice importerService;
 
     @Test
     public void testImportXML() throws Exception {
 
         File xml = FileUtils.getResourceFileFromContext("test-export-webdelib.xml");
         assertNotNull(xml);
 
         DocumentModel root = session.getRootDocument();
 
         XmlImporterSevice importer = Framework.getLocalService(XmlImporterSevice.class);
         assertNotNull(importer);
         importer.importDocuments(root, xml);
 
         session.save();
 
         checkImport();
     }
 
 
     @Test
     public void testImportZip() throws Exception {
 
         File zip = FileUtils.getResourceFileFromContext("test-export-webdelib.zip");
         assertNotNull(zip);
 
         DocumentModel root = session.getRootDocument();
 
         XmlImporterSevice importer = Framework.getLocalService(XmlImporterSevice.class);
         assertNotNull(importer);
         importer.importDocuments(root, zip);
 
         session.save();
 
         checkImport();
     }
 
 
     public void checkImport() throws ClientException {
         // ****** SEANCES *******
         List<DocumentModel> docs = session.query("select * from "
                 + DOC_TYPE_SEANCE);
         assertEquals("we should have only one Seance", 1, docs.size());
         DocumentModel seanceDoc = docs.get(0);
         assertEquals("/2013/02/07/WebDelibSeance-1",
                 seanceDoc.getPathAsString());
         assertEquals("Conseil Général",
                 seanceDoc.getPropertyValue("webdelibseance:type"));
         assertEqualsDate("2013-02-07 14:00:00",
                 seanceDoc.getPropertyValue("webdelibseance:date_seance"));
         assertEqualsDate("2012-11-30 18:16:01",
                 seanceDoc.getPropertyValue("webdelibseance:date_convocation"));
         assertEquals("12",
                 seanceDoc.getPropertyValue("webdelibseance:idSeance"));
         assertEqualsFile("convocation.pdf", "application/pdf", "utf-8",
                 seanceDoc.getPropertyValue("webdelibseance:convocation_file"));
         assertEqualsFile("odj.pdf", "application/pdf", "utf-8",
                 seanceDoc.getPropertyValue("webdelibseance:ordre_du_jour_file"));
         assertEqualsFile("pv.pdf", "application/pdf", "utf-8",
                 seanceDoc.getPropertyValue("webdelibseance:pv_sommaire_file"));
         assertEqualsFile("pvcomplet.pdf", "application/pdf", "utf-8",
                 seanceDoc.getPropertyValue("webdelibseance:pv_complet_file"));
 
         // ****** ACTES *******
         docs = session.query("select * from " + DOC_TYPE_ACTE
                 + " ORDER BY dc:created");
         assertEquals("we should have 5 actes", 5, docs.size());
         DocumentModel acte = docs.get(0);
         docs = session.query("select * from " + DOC_TYPE_ACTE
                 + " WHERE ecm:name = 'Acte-38' ORDER BY dc:created");
         acte = docs.get(0);
         assertEquals("/2013/02/07/WebDelibSeance-1/Acte-38",
                 acte.getPathAsString());
         assertEquals("Changement des horaires d'ouverture de la mairie",
                 acte.getPropertyValue("dc:title"));
         assertEquals("Projet chambre des notaires",
                 acte.getPropertyValue("dc:description"));
          assertEqualsDate("2013-02-07 14:00:00",
                 acte.getPropertyValue("webdelibacte:date"));
         assertNull(acte.getPropertyValue("webdelibacte:numero"));
         assertEquals("Administration Generale",
                 acte.getPropertyValue("webdelibacte:theme"));
         assertEquals("Direction Informatique",
                 acte.getPropertyValue("webdelibacte:emetteur"));
         assertEquals("Marc Marchal",
                 acte.getPropertyValue("webdelibacte:redacteur"));
         assertEquals("Pascal PERTUSA",
                 acte.getPropertyValue("webdelibacte:rapporteur"));
         assertEquals("Basse ville",
                 acte.getPropertyValue("webdelibacte:canton"));
         assertEquals("Valence", acte.getPropertyValue("webdelibacte:commune"));
         assertEquals("Commission Ressources",
                 acte.getPropertyValue("webdelibacte:type"));
         assertEquals(
                 "Commission Ressources : 2013-02-07 14:00:00, Commission Ressources"
                         + " : 2013-03-29 16:00:00, test FD : 2013-04-05 17:17:00, ",
                 acte.getPropertyValue("webdelibacte:commissions"));
 
         // Acte sans seance
         docs = session.query("select * from " + DOC_TYPE_ACTE
                 + " WHERE ecm:name = 'Acte-149' ORDER BY dc:created");
         acte = docs.get(0);
         assertEquals("Tu le verras celui-là ?",
                 acte.getPropertyValue("dc:title"));
         assertNull(acte.getPropertyValue("dc:description"));
         assertEqualsDate("2013-02-09 00:00:00", acte.getPropertyValue("webdelibacte:date"));
         assertNull(acte.getPropertyValue("webdelibacte:numero"));
         assertNull(acte.getPropertyValue("webdelibacte:theme"));
         assertEquals("DGS", acte.getPropertyValue("webdelibacte:emetteur"));
         assertEquals("Brigitte Liège",
                 acte.getPropertyValue("webdelibacte:redacteur"));
         assertEquals(" ", acte.getPropertyValue("webdelibacte:rapporteur"));
         assertNull(acte.getPropertyValue("webdelibacte:canton"));
         assertEquals("Valence", acte.getPropertyValue("webdelibacte:commune"));
         assertNull(acte.getPropertyValue("webdelibacte:type"));
        assertEquals("/2013/02/09/Autres/Acte-149", acte.getPathAsString());
 
 
         // ****** DOCUMENTS *******
         docs = session.query("select * from " + DOC_TYPE_SIGNATURE
                 + " ORDER BY dc:created");
         assertEquals("we should have 2 files", 2, docs.size());
         DocumentModel signature = docs.get(0);
         assertEquals("1", signature.getPropertyValue("dc:source"));
         signature = docs.get(1);
         assertEquals("2", signature.getPropertyValue("dc:source"));
     }
 
     private void assertEqualsFile(String filename, String mimetype,
             String encoding, Serializable serializable) {
         assertNotNull(serializable);
         assertTrue("Waiting value as Blob , but was "
                 + serializable.getClass().toString(),
                 serializable instanceof Blob);
         Blob blob = (Blob) serializable;
         assertNotNull(blob);
         assertEquals(filename, blob.getFilename());
         assertEquals(mimetype, blob.getMimeType());
         // assertEquals(encoding, blob.getEncoding());
 
     }
 
     private void assertEqualsDate(String expected, Serializable serializable) {
         assertNotNull(serializable);
         assertTrue("Waiting value as Calendar , but was "
                 + serializable.getClass().toString(),
                 serializable instanceof Calendar);
         Calendar date = (Calendar) serializable;
         String current = String.format(DATE_FORMAT, date.get(Calendar.YEAR),
                 date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH),
                 date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
                 date.get(Calendar.SECOND));
         assertEquals(expected, current);
         // assertEquals(year, date.get(Calendar.YEAR));
         // assertEquals(month, date.get(Calendar.MONTH));
         // assertEquals(day, date.get(Calendar.DAY_OF_MONTH));
         // assertEquals(hour, date.get(Calendar.HOUR_OF_DAY));
         // assertEquals(minute, date.get(Calendar.MINUTE));
         // assertEquals(seconde, date.get(Calendar.SECOND));
 
     }
 }
