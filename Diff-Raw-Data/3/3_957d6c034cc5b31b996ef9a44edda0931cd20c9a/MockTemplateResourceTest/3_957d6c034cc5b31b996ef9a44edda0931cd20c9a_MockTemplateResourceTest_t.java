 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.mock.resource;
 
 import org.junit.Test;
 
 import com.redhat.rhevm.api.model.Template;
 import com.redhat.rhevm.api.model.Templates;
 
 public class MockTemplateResourceTest extends MockTestBase {
     private MockTestBase.TemplatesResource getService() {
         return createTemplatesResource(getEntryPoint("templates").getHref());
     }
 
     private void checkTemplate(Template template) {
         assertNotNull(template.getName());
         assertNotNull(template.getId());
         assertNotNull(template.getHref());
         assertTrue(template.getHref().endsWith("templates/" + template.getId()));
         assertNotNull(template.getActions());
        assertEquals(1, template.getActions().getLinks().size());
        assertEquals("export", template.getActions().getLinks().get(0).getRel());
     }
 
     @Test
     public void testGetTemplatesList() throws Exception {
         MockTestBase.TemplatesResource service = getService();
         assertNotNull(service);
 
         Templates templates = service.list();
         assertNotNull(templates);
         assertTrue(templates.getTemplates().size() > 0);
 
         for (Template template : templates.getTemplates()) {
             checkTemplate(template);
 
             Template t = service.get(template.getId());
             checkTemplate(t);
             assertEquals(template.getId(), t.getId());
         }
     }
 }
