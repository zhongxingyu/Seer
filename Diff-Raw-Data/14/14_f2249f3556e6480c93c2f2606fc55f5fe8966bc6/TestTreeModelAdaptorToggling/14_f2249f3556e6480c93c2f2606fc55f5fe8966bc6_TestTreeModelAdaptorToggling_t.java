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
 package org.richfaces.tests.metamer.ftest.richTreeModelAdaptor;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 
 import java.net.URL;
 
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.richfaces.tests.metamer.ftest.richTree.TestTreeToggling;
 import org.richfaces.tests.metamer.ftest.richTree.TreeAttributes;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestTreeModelAdaptorToggling extends TestTreeToggling {
 
     @Inject
     @Use(enumeration = true)
     public RecursiveModelRepresentation representation;
 
     @Inject
     @Use(booleans = { true, false })
     public boolean recursiveLeafChildrenNullable;
 
     @BeforeClass
    public void setupTreeModelTesting() {
         treeAttributes = new TreeAttributes(jq("span[id*=treeAttributes]"));
        paths = new int[][] { { 3, 2, 1, 2 }, { 2, 4, 6 } };
     }
 
     @BeforeMethod
     public void initPathsAndModelRepresentation() {
         if (representation == RecursiveModelRepresentation.MAP) {
             guardXhr(selenium).click(pjq(":radio[id*=recursiveModelRepresentation]").getNthOccurence(2));
         }
         if (recursiveLeafChildrenNullable) {
             guardXhr(selenium).click(pjq(":checkbox[id$=recursiveLeafChildrenNullable]"));
         }
     }
 
     public URL getTestUrl() {
         return buildUrl(contextPath, "http://localhost:8080/metamer/faces/components/richTree/treeAdaptors.xhtml");
     }
 
     @Test
     @Override
     public void testTopLevelNodesExpansion() {
         super.testTopLevelNodesExpansion();
     }
 
     @Test
     @Override
     public void testTopLevelNodesCollapsion() {
         super.testTopLevelNodesCollapsion();
     }
 
     @Test
     @Override
     public void testDeepExpansion() {
         super.testDeepExpansion();
     }
 
     @Test
     @Override
     public void testDeepCollapsion() {
         super.testDeepCollapsion();
     }
 }
