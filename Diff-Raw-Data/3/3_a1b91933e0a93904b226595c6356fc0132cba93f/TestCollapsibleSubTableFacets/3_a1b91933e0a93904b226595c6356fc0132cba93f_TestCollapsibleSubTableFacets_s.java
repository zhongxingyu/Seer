 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
  *******************************************************************************/
 package org.richfaces.tests.metamer.ftest.richCollapsibleSubTable;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestCollapsibleSubTableFacets extends AbstractCollapsibleSubTableTest {
 
     private static final String SAMPLE_STRING = "Abc123!@#ĚščСам";
     private static final String EMPTY_STRING = "";
 
     @Override
     public URL getTestUrl() {
 
         return buildUrl(contextPath, "faces/components/richCollapsibleSubTable/facets.xhtml");
     }
 
     @Test
     public void testNoDataFacet() {
         assertTrue(subtable.hasVisibleRows());
         attributes.setShowData(false);
         assertFalse(subtable.hasVisibleRows());
 
         assertTrue(subtable.isNoData());
        assertEquals(selenium.getText(subtable.getNoData()), EMPTY_STRING);
 
         facets.setNoData(SAMPLE_STRING);
 
         attributes.setShowData(true);
         assertTrue(subtable.hasVisibleRows());
 
         attributes.setShowData(false);
         assertFalse(subtable.hasVisibleRows());
 
         assertTrue(subtable.isNoData());
        assertEquals(selenium.getText(subtable.getNoData()), SAMPLE_STRING);
     }
 
     @Test
     public void testHeaderInstantChange() {
         facets.setHeader(SAMPLE_STRING);
         assertEquals(selenium.getText(subtable.getHeader()), SAMPLE_STRING);
 
         facets.setHeader(EMPTY_STRING);
         if (selenium.isElementPresent(subtable.getHeader())) {
             assertEquals(selenium.getText(subtable.getHeader()), EMPTY_STRING);
         }
 
         facets.setHeader(SAMPLE_STRING);
         assertEquals(selenium.getText(subtable.getHeader()), SAMPLE_STRING);
     }
 
     @Test
     public void testFooterInstantChange() {
         facets.setFooter(SAMPLE_STRING);
         assertEquals(selenium.getText(subtable.getFooter()), SAMPLE_STRING);
 
         facets.setFooter(EMPTY_STRING);
         if (selenium.isElementPresent(subtable.getFooter())) {
             assertEquals(selenium.getText(subtable.getFooter()), EMPTY_STRING);
         }
 
         facets.setFooter(SAMPLE_STRING);
         assertEquals(selenium.getText(subtable.getFooter()), SAMPLE_STRING);
     }
 }
