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
 package org.richfaces.tests.metamer.ftest.richExtendedDataTable;
 
 import static org.jboss.test.selenium.locator.LocatorFactory.*;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.locator.IdLocator;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.model.AssertingDataScroller;
 import org.richfaces.tests.metamer.ftest.model.DataScroller;
 import org.richfaces.tests.metamer.ftest.model.DataTable;
 import org.testng.annotations.Test;
 
 /**
  * Test of DataScroller tied to Extended Data Table
  * 
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestScroller extends AbstractMetamerTest {
 
     private static final int TOTAL_ROW_COUNT = 50;
     private static final Integer[] ROW_COUNT_VALUES = new Integer[] { null, 10, 1, TOTAL_ROW_COUNT, 13, 9, 17,
         TOTAL_ROW_COUNT + 1, 2 * TOTAL_ROW_COUNT };
     DataScroller dataScroller1 = new AssertingDataScroller("outside-table", pjq("span.rf-ds[id$=scroller1]"));
     DataScroller dataScroller2 = new AssertingDataScroller("inside-table-footer", pjq("span.rf-ds[id$=scroller2]"));
     DataTable table = new DataTable(pjq("div.rf-edt[id$=richEDT]"));
     IdLocator attributeRowsInput = id("form:attributes:rowsInput");
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richExtendedDataTable/scroller.xhtml");
     }
 
     /**
      * Tests row count for scroller in footer of the EDT.
      * 
      * @see {@link #testRowCount(DataScroller)}
      */
     @Test(groups = "client-side-perf")
     public void testRowCountFooterScroller() {
         testRowCount(dataScroller2);
     }
 
     /**
      * Tests row count for scroller outside of the EDT.
      * 
      * @see {@link #testRowCount(DataScroller)}
      */
     @Test
     public void testRowCountOutsideTable() {
         testRowCount(dataScroller1);
     }
 
     /**
      * <p>
      * Test the data scroller functionality for different values of row count
      * </p>
      * 
      * <p>
      * If desired, sets the value of 'rows' first for each iteration.
      * </p>
      * 
      * <p>
      * Then goes to first page and verifies that there is valid number of rows.
      * </p>
      * 
      * <p>
      * Verifies that on the first page is less than total number of rows if the scroller has several pages.
      * </p>
      * 
      * <p>
      * If the scroller has pages enabled, goes to last page and verifies that this is valid number of pages visible in
      * scroller and the number of rows is valid.
      * </p>
      * 
      * <p>
      */
     private void testRowCount(DataScroller dataScroller) {
         for (Integer rowsPerPage : ROW_COUNT_VALUES) {
             if (rowsPerPage != null) {
                 selenium.type(attributeRowsInput, String.valueOf(rowsPerPage));
                 selenium.waitForPageToLoad();
             }
 
             dataScroller.gotoFirstPage();
             int rowCountPreset = Integer.valueOf(selenium.getValue(attributeRowsInput));
             int rowCountActual = table.getCountOfTableRows();
             assertEquals(rowCountActual, Math.min(TOTAL_ROW_COUNT, rowCountPreset));
 
             assertEquals(dataScroller.hasPages(), rowCountActual < TOTAL_ROW_COUNT);
             if (dataScroller.hasPages()) {
                 dataScroller.gotoLastPage();
 
                 int pagesExpected = pageCountActualExpected(rowCountActual);
                 int countOfVisiblePages = dataScroller.getCountOfVisiblePages();
 
                 if (countOfVisiblePages < pagesExpected) {
                     int lastVisiblePage = dataScroller.getLastVisiblePage();
                     assertEquals(lastVisiblePage, pageCountActualExpected(rowCountActual));
                 } else {
                     assertEquals(countOfVisiblePages, pagesExpected);
                 }
 
                 int rowCountExpected = rowCountLastPageExpected(rowCountPreset);
                 rowCountActual = table.getCountOfTableRows();
                 assertEquals(rowCountActual, rowCountExpected);
             }
         }
     }
 
     private int pageCountActualExpected(int rowCountActual) {
         return Double.valueOf(Math.ceil((double) TOTAL_ROW_COUNT / rowCountActual)).intValue();
     }
 
     private int rowCountLastPageExpected(int rowCountPreset) {
         int result = TOTAL_ROW_COUNT % rowCountPreset;
         return (result == 0) ? rowCountPreset : result;
     }
 }
