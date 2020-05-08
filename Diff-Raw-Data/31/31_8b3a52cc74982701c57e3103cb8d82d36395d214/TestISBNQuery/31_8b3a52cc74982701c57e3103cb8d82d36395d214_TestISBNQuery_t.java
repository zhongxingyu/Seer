/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     ldoguin
 */

 package org.nuxeo.isbn.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.nuxeo.ecm.automation.AutomationService;
 import org.nuxeo.ecm.automation.OperationContext;
 import org.nuxeo.ecm.automation.OperationType;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.test.CoreFeature;
 import org.nuxeo.isbn.ISBNQuery;
 import org.nuxeo.runtime.test.runner.Deploy;
 import org.nuxeo.runtime.test.runner.Features;
 import org.nuxeo.runtime.test.runner.FeaturesRunner;
 
 import com.google.inject.Inject;
 
 @RunWith(FeaturesRunner.class)
 @Features(CoreFeature.class)
 @Deploy({ "nuxeo-book-isbn", "org.nuxeo.ecm.automation.core" })
 public class TestISBNQuery {
 
     @Inject
     AutomationService automationService;
 
     @Inject
     CoreSession coreSession;
 
     public static final String ISBN_TEST = "0201558025";
     @Test
     public void testISBNQuery() throws Exception {
         assertNotNull(automationService);
         OperationType opType = automationService.getOperation(ISBNQuery.ID);
         assertNotNull(opType);
 
         assertNotNull(coreSession);
         DocumentModel bookDocument = coreSession.createDocumentModel("/", ISBN_TEST, "Book");
         bookDocument.setPropertyValue("isbn:isbn", ISBN_TEST);
         bookDocument = coreSession.createDocument(bookDocument);
         OperationContext ctx = new OperationContext();
         ctx.setInput(bookDocument);
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         Object result = automationService.run(ctx, ISBNQuery.ID, parameters);
         assertNotNull(result);
         if (result instanceof DocumentModel) {
             bookDocument = (DocumentModel) result;
             String bib_key = bookDocument.getProperty("isbn:bib_key").getValue(String.class);
             assertEquals("ISBN:0201558025",bib_key);
         } else {
             fail("result should be a DocumentModel instead of" + result.getClass());
         }
 
         
     }
 
 
 }
