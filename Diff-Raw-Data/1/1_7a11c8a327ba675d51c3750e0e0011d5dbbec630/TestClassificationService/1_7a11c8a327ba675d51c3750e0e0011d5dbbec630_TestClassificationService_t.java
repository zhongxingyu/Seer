 /*
  * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
  *     ldoguin
  *
  * $Id$
  */
 
 package org.nuxeo.ecm.classification.core.test;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.nuxeo.ecm.classification.api.ClassificationService;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
 import org.nuxeo.runtime.api.Framework;
 
 public class TestClassificationService extends SQLRepositoryTestCase {
 
     protected ClassificationService cs;
 
     @Before
     public void setUp() throws Exception {
         super.setUp();
         deployBundle("org.nuxeo.ecm.platform.classification.api");
         deployContrib("org.nuxeo.ecm.platform.classification.core",
                 "OSGI-INF/classification-classifiable-types-framework.xml");
         deployContrib("org.nuxeo.ecm.platform.classification.test.core",
                 "OSGI-INF/classification-classifiable-types-test-contrib.xml");
        fireFrameworkStarted();
         cs = Framework.getLocalService(ClassificationService.class);
         assertNotNull(cs);
     }
 
     @Test
     public void testRegistration() {
         assertNotNull(cs);
         assertNotNull(cs.getClassifiableDocumentTypes());
         assertTrue(cs.isClassifiable("File"));
         assertTrue(cs.isClassifiable("Note"));
         assertTrue(cs.isClassifiable("Picture"));
         assertEquals(3, cs.getClassifiableDocumentTypes().size());
     }
 
     @Test
     public void testOverriding() throws Exception {
         assertNotNull(cs);
         deployContrib("org.nuxeo.ecm.platform.classification.test.core",
                 "OSGI-INF/classification-classifiable-types-test-override-contrib.xml");
         assertFalse(cs.isClassifiable("File"));
         assertEquals(2, cs.getClassifiableDocumentTypes().size());
     }
 
     @Test
     public void testClassifiable() throws ClientException {
         openSession();
         DocumentModel folder = session.createDocumentModel("/", "foo", "Folder");
         folder = session.createDocument(folder);
         assertFalse(cs.isClassifiable(folder));
         DocumentModel classifiableDoc = session.createDocumentModel("/", "bar", "ClassifiableDoc");
         classifiableDoc = session.createDocument(folder);
         assertFalse(cs.isClassifiable(classifiableDoc));
 
     }
 }
