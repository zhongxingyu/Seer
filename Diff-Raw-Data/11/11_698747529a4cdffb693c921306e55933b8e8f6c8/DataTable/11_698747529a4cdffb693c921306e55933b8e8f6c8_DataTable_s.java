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
 package org.richfaces.tests.metamer.ftest.model;
 
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.locator.reference.ReferencedLocator.ref;
 
 import org.jboss.test.selenium.framework.AjaxSelenium;
 import org.jboss.test.selenium.framework.AjaxSeleniumProxy;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.jboss.test.selenium.locator.reference.ReferencedLocator;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class DataTable extends AbstractModel<JQueryLocator> implements
     org.richfaces.tests.metamer.ftest.abstractions.DataTable {
 
     AjaxSelenium selenium = AjaxSeleniumProxy.getInstance();
 
    ReferencedLocator<JQueryLocator> tbody = ref(root, "> tbody.rf-dt");
     ReferencedLocator<JQueryLocator> firstColumnRows = ref(tbody, "> tr > td.rf-dt-c:nth-child(1)");
     ReferencedLocator<JQueryLocator> firstRowColumns = ref(tbody, "> tr:eq(0) > td.rf-dt-c");
    ReferencedLocator<JQueryLocator> noData = ref(tbody, "> tr > td.rf-dt-ndt");
     ReferencedLocator<JQueryLocator> columns = ref(tbody, "> tr");
     JQueryLocator columnToElement = jq("td.rf-dt-c");
 
    ReferencedLocator<JQueryLocator> thead = ref(root, "> thead.rf-dt-thead");
     ReferencedLocator<JQueryLocator> tableHeader = ref(thead, "> tr.rf-dt-hdr > th.rf-dt-hdr-c");
     ReferencedLocator<JQueryLocator> columnHeader = ref(thead, "> tr.rf-dt-shdr > th.rf-dt-shdr-c");
 
    ReferencedLocator<JQueryLocator> columnFooter = ref(root, "> tfoot.rd-dt-tfoot > tr.rf-dt-sftr > td.rf-dt-sftr-c");
 
     public DataTable(JQueryLocator root) {
         super(root);
     }
 
     public DataTable(String name, JQueryLocator root) {
         super(name, root);
     }
 
     @Override
     public int getRows() {
         return selenium.getCount(firstColumnRows);
     }
 
     @Override
     public int getColumns() {
         return selenium.getCount(firstRowColumns);
     }
 
     @Override
     public boolean isVisible() {
         return selenium.isElementPresent(root.getLocator()) && selenium.isVisible(root.getLocator());
     }
 
     @Override
     public boolean isNoData() {
         return selenium.isElementPresent(noData) && selenium.isVisible(noData);
     }
 
     @Override
     public JQueryLocator getNoData() {
         return noData.getReferenced();
     }
 
     @Override
     public JQueryLocator getElement(int column, int row) {
         return columns.getNthOccurence(row).getChild(columnToElement).getNthChildElement(column);
     }
 
     @Override
     public JQueryLocator getColumnHeader(int column) {
         return columnHeader.getNthOccurence(column);
     }
 
     @Override
     public JQueryLocator getColumnFooter(int column) {
         return columnFooter.getNthOccurence(column);
     }
 
     @Override
     public JQueryLocator getHeader() {
         return tableHeader.getReferenced();
     }
 
 }
