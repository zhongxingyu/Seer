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
 package org.richfaces.tests.metamer.ftest.richTreeNode;
 
 import static org.jboss.test.selenium.JQuerySelectors.append;
 import static org.jboss.test.selenium.JQuerySelectors.not;
 import static org.jboss.test.selenium.dom.Event.CLICK;
 import static org.jboss.test.selenium.dom.Event.DBLCLICK;
 import static org.jboss.test.selenium.dom.Event.KEYDOWN;
 import static org.jboss.test.selenium.dom.Event.KEYPRESS;
 import static org.jboss.test.selenium.dom.Event.KEYUP;
 import static org.jboss.test.selenium.dom.Event.MOUSEDOWN;
 import static org.jboss.test.selenium.dom.Event.MOUSEMOVE;
 import static org.jboss.test.selenium.dom.Event.MOUSEOUT;
 import static org.jboss.test.selenium.dom.Event.MOUSEOVER;
 import static org.jboss.test.selenium.dom.Event.MOUSEUP;
 import static org.jboss.test.selenium.locator.Attribute.SRC;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.net.URL;
 
 import javax.faces.event.PhaseId;
 
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.geometry.Point;
 import org.jboss.test.selenium.locator.AttributeLocator;
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.jboss.test.selenium.waiting.EventFiredCondition;
 import org.jboss.test.selenium.waiting.retrievers.AttributeRetriever;
 import org.richfaces.component.SwitchType;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.richfaces.tests.metamer.ftest.richTree.TreeAttributes;
 import org.richfaces.tests.metamer.ftest.richTree.TreeModel;
 import org.richfaces.tests.metamer.ftest.richTree.TreeNodeModel;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestTreeNodeSimple extends AbstractMetamerTest {
 
     private static final String SAMPLE_CLASS = "sample-class";
     private static final String JQ_SAMPLE_CLASS = "[sample-class]";
     private static final String IMAGE_URL = "/resources/images/loading.gif";
 
     TreeAttributes attributesTree = new TreeAttributes(jq("span[id$=attributes:panel]"));
     TreeNodeAttributes attributes = new TreeNodeAttributes(jq("span[id$=treeNode1Attributes:panel]"));
     TreeNodeAttributes attributesLeaf = new TreeNodeAttributes(jq("span[id$=treeNode3Attributes:panel]"));
 
     TreeModel tree = new TreeModel(pjq("div.rf-tr[id$=richTree]"));
     TreeNodeModel treeNode = tree.getNode(1);
     TreeNodeModel subTreeNode = treeNode.getNode(1);
     TreeNodeModel leaf = subTreeNode.getNode(1);
 
     @Inject
     @Use(empty = true)
     Event event = CLICK;
     Event[] events = new Event[] { CLICK, DBLCLICK, KEYDOWN, KEYPRESS, KEYUP, MOUSEDOWN, MOUSEMOVE, MOUSEOUT,
         MOUSEOVER, MOUSEUP };
 
     @Inject
     @Use(empty = true)
     SwitchType toggleType;
     SwitchType[] toggleTypes = new SwitchType[] { SwitchType.ajax, SwitchType.server };
 
     @BeforeMethod
     public void init() {
         attributesRoot.setLocator(jq("span[id$=treeNode1Attributes:panel]"));
         tree.setToggleType(SwitchType.ajax);
     }
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "http://localhost:8080/metamer/faces/components/richTree/simple.xhtml");
     }
 
     @Test
     public void testDir() {
         super.testDir(treeNode.getTreeNode());
     }
 
     @Test
     public void testHandleClass() {
         assertEquals(selenium.getCount(not(tree.getAnyNode().getHandle(), JQ_SAMPLE_CLASS)), 4);
 
         attributes.setHandleClass(SAMPLE_CLASS);
 
         assertEquals(selenium.getCount(append(tree.getAnyNode().getHandle(), JQ_SAMPLE_CLASS)), 0);
         assertEquals(selenium.getCount(not(tree.getAnyNode().getHandle(), JQ_SAMPLE_CLASS)), 4);
     }
 
     @Test
     public void testIconClass() {
         assertEquals(selenium.getCount(not(tree.getAnyNode().getIcon(), JQ_SAMPLE_CLASS)), 4);
 
         attributes.setIconClass(SAMPLE_CLASS);
 
         assertEquals(selenium.getCount(append(tree.getAnyNode().getIcon(), JQ_SAMPLE_CLASS)), 0);
         assertEquals(selenium.getCount(not(tree.getAnyNode().getIcon(), JQ_SAMPLE_CLASS)), 4);
     }
 
     ElementLocator<?> iconImage = treeNode.getIcon();
     AttributeLocator<?> imageSrc = iconImage.getAttribute(SRC);
     AttributeRetriever retrieveImageSrc = retrieveAttribute.attributeLocator(imageSrc);
 
     @Test
     public void testIconCollapsed() {
         assertTrue(selenium.isElementPresent(iconImage));
         assertFalse(selenium.isAttributePresent(imageSrc));
 
         attributes.setIconCollapsed(IMAGE_URL);
 
         assertTrue(selenium.isElementPresent(iconImage));
         assertTrue(selenium.isAttributePresent(imageSrc));
 
         String url = retrieveImageSrc.retrieve();
         assertTrue(url.endsWith(IMAGE_URL));
     }
 
     @Test
     public void testIconExpanded() {
         assertTrue(selenium.isElementPresent(iconImage));
         assertFalse(selenium.isAttributePresent(imageSrc));
 
         attributes.setIconExpanded(IMAGE_URL);
 
         assertTrue(selenium.isElementPresent(iconImage));
         assertFalse(selenium.isAttributePresent(imageSrc));
 
         treeNode.expand();
 
         assertTrue(selenium.isElementPresent(iconImage));
         assertTrue(selenium.isAttributePresent(imageSrc));
 
         String url = retrieveImageSrc.retrieve();
         assertTrue(url.endsWith(IMAGE_URL));
     }
 
     @Test
     public void testIconLeaf() {
         treeNode.expand();
         subTreeNode.expand();
 
         ElementLocator<?> leafIcon = leaf.getIcon();
         AttributeLocator<?> leafIconImageSrc = leaf.getIcon().getAttribute(SRC);
 
         assertTrue(selenium.isElementPresent(leafIcon));
         assertFalse(selenium.isAttributePresent(leafIconImageSrc));
 
         attributesLeaf.setIconLeaf(IMAGE_URL);
 
         assertTrue(selenium.isElementPresent(leafIcon));
         assertTrue(selenium.isAttributePresent(leafIconImageSrc));
 
         String url = selenium.getAttribute(leafIconImageSrc);
         assertTrue(url.endsWith(IMAGE_URL));
     }
 
     @Test
     @Use(field = "toggleType", value = "toggleTypes")
     public void testImmediate() {
         tree.setToggleType(toggleType);
         attributesTree.setToggleType(toggleType);
 
         treeNode.expand();
 
         phaseInfo.assertPhases(PhaseId.ANY_PHASE);
         phaseInfo.assertListener(PhaseId.PROCESS_VALIDATIONS, "tree toggle listener invoked");
 
         attributes.setImmediate(true);
 
         treeNode.expand();
 
         phaseInfo.assertPhases(PhaseId.ANY_PHASE);
         phaseInfo.assertListener(PhaseId.APPLY_REQUEST_VALUES, "tree toggle listener invoked");
     }
 
     @Test
     public void testLabelClass() {
         assertEquals(selenium.getCount(not(tree.getAnyNode().getLabel(), JQ_SAMPLE_CLASS)), 4);
 
         attributes.setLabelClass(SAMPLE_CLASS);
 
         assertEquals(selenium.getCount(append(tree.getAnyNode().getLabel(), JQ_SAMPLE_CLASS)), 0);
         assertEquals(selenium.getCount(not(tree.getAnyNode().getLabel(), JQ_SAMPLE_CLASS)), 4);
     }
 
     @Test
     public void testLang() {
         assertEquals(selenium.getCount(not(tree.getAnyNode(), "[lang=cs]")), 4);
 
         attributes.setLang("cs");
 
         assertEquals(selenium.getCount(append(tree.getAnyNode(), "[lang=cs]")), 0);
         assertEquals(selenium.getCount(not(tree.getAnyNode(), "[lang=cs]")), 4);
     }
 
     @Test
     @Use(field = "event", value = "events")
    @IssueTracking("https://issues.jboss.org/browse/RF-10483")
     public void testClientEvents() {
         String attributeName = event.getEventName();
         ElementLocator<?> eventInput = pjq("span[id$=treeNode1Attributes:panel] input[id$=on" + attributeName
             + "Input]");
         String value = "metamerEvents += \"" + event.getEventName() + " \"";
 
         selenium.type(eventInput, value);
         selenium.waitForPageToLoad(TIMEOUT);
 
         fireEventAt(treeNode.getLabel(), event);
 
         waitGui.failWith("Attribute on" + attributeName + " does not work correctly").until(
             new EventFiredCondition(event));
     }
 
     @Test
     public void testOnbeforetoggle() {
         super.testRequestEventsBefore("beforetoggle", "toggle");
         treeNode.expand();
         super.testRequestEventsAfter("beforetoggle", "toggle");
     }
 
     @Test
     public void testRendered() {
         treeNode.expand();
         subTreeNode.expand();
 
         assertTrue(selenium.isElementPresent(leaf));
 
         attributesLeaf.setRendered(false);
 
         assertFalse(selenium.isElementPresent(leaf));
         assertTrue(selenium.isElementPresent(subTreeNode));
     }
 
     @Test
     public void testStyle() {
         super.testStyle(treeNode.getTreeNode(), "style");
     }
 
     @Test
     public void testStyleClass() {
         super.testStyleClass(treeNode.getTreeNode(), "styleClass");
     }
 
     @Test
     public void testTitle() {
         super.testTitle(treeNode.getTreeNode());
     }
 
     private void fireEventAt(ElementLocator<?> element, Event event) {
         Point coords = new Point(0, 0);
         if (event == CLICK) {
             selenium.clickAt(element, coords);
         } else if (event == DBLCLICK) {
             selenium.doubleClickAt(element, coords);
         } else if (event == MOUSEDOWN) {
             selenium.mouseDownAt(element, coords);
         } else if (event == MOUSEMOVE) {
             selenium.mouseMoveAt(element, coords);
         } else if (event == MOUSEOUT) {
             selenium.mouseOutAt(element, coords);
         } else if (event == MOUSEOVER) {
             selenium.mouseOverAt(element, coords);
         } else if (event == MOUSEUP) {
             selenium.mouseUpAt(element, coords);
         } else {
             selenium.fireEvent(element, event);
         }
     }
 }
