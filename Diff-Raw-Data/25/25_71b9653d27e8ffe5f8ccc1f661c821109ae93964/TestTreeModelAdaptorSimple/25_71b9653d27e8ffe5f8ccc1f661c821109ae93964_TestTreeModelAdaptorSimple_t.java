 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2011, Red Hat, Inc. and individual contributors
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
 
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.locator.ExtendedLocator;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.tests.metamer.ftest.AbstractComponentAttributes;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.richTree.TreeAttributes;
 import org.richfaces.tests.metamer.ftest.richTree.TreeModel;
 import org.richfaces.tests.metamer.ftest.richTree.TreeNodeModel;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
@IssueTracking("https://issues.jboss.org/browse/RF-10497")
 public class TestTreeModelAdaptorSimple extends AbstractMetamerTest {
 
     protected TreeAttributes treeAttributes = new TreeAttributes(jq("span[id*=treeAttributes]"));
     protected TreeModel tree = new TreeModel(pjq("div.rf-tr[id$=richTree]"));
     protected TreeNodeModel treeNode;
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richTree/treeAdaptors.xhtml");
     }
 
     @Test
     public void testModelAdaptorRendered() {
         tree.getNode(2).expand();
         tree.getNode(2).getNode(2).expand();
         treeNode = tree.getNode(2).getNode(2).getNode(1);
 
         assertTrue(treeNode.isLeaf());
 
         modelAdaptorAttributes.setRendered(false);
 
         assertFalse(treeNode.isLeaf());
     }
 
     @Test
     public void testRecursiveModelAdaptorRendered() {
         tree.getNode(2).expand();
         tree.getNode(2).getNode(2).expand();
 
         boolean subnodePresent = false;
         for (TreeNodeModel treeNode : tree.getNode(2).getNode(2).getNodes()) {
             if (!treeNode.isLeaf()) {
                 subnodePresent = true;
             }
         }
         assertTrue(subnodePresent, "there should be at least one subnode (not leaf) in expanded branch");
 
         recursiveModelAdaptorAttributes.setRendered(false);
 
         for (TreeNodeModel treeNode : tree.getNode(2).getNode(2).getNodes()) {
             if (!treeNode.isLeaf()) {
                 fail("there should be no subnode (not leaf) in expanded branch");
             }
         }
     }
 
     private ModelAdaptorAttributes modelAdaptorAttributes = new ModelAdaptorAttributes(
         pjq("span[id$=:listAttributes:panel]"));
     private RecursiveModelAdaptorAttributes recursiveModelAdaptorAttributes = new RecursiveModelAdaptorAttributes(
         pjq("span[id$=:recursiveAttributes:panel]"));
 
     private class ModelAdaptorAttributes extends AbstractComponentAttributes {
 
         public <T extends ExtendedLocator<JQueryLocator>> ModelAdaptorAttributes(T root) {
             super(root);
         }
 
         public void setRendered(Boolean rendered) {
             setProperty("rendered", rendered);
         }
     }
 
     private class RecursiveModelAdaptorAttributes extends AbstractComponentAttributes {
 
         public <T extends ExtendedLocator<JQueryLocator>> RecursiveModelAdaptorAttributes(T root) {
             super(root);
         }
 
         public void setRendered(Boolean rendered) {
             setProperty("rendered", rendered);
         }
     }
 }
