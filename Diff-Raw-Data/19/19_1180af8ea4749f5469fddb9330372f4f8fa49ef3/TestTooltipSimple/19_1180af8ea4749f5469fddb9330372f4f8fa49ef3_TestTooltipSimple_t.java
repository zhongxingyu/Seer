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
 package org.richfaces.tests.metamer.ftest.richTooltip;
 
 import static javax.faces.event.PhaseId.APPLY_REQUEST_VALUES;
 import static javax.faces.event.PhaseId.RENDER_RESPONSE;
 import static javax.faces.event.PhaseId.RESTORE_VIEW;
 import static org.jboss.test.selenium.dom.Event.CLICK;
 import static org.jboss.test.selenium.dom.Event.DBLCLICK;
 import static org.jboss.test.selenium.dom.Event.MOUSEDOWN;
 import static org.jboss.test.selenium.dom.Event.MOUSEMOVE;
 import static org.jboss.test.selenium.dom.Event.MOUSEOUT;
 import static org.jboss.test.selenium.dom.Event.MOUSEOVER;
 import static org.jboss.test.selenium.dom.Event.MOUSEUP;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.css.CssProperty;
 import org.jboss.test.selenium.dom.Event;
 import org.jboss.test.selenium.geometry.Dimension;
 import org.jboss.test.selenium.geometry.Point;
 import org.jboss.test.selenium.javascript.JQueryScript;
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.richfaces.TooltipLayout;
 import org.richfaces.TooltipMode;
 import org.richfaces.component.Positioning;
 import org.richfaces.tests.metamer.ftest.AbstractMetamerTest;
 import org.richfaces.tests.metamer.ftest.DelayTester;
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
import org.richfaces.tests.metamer.ftest.annotations.RegressionTest;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.richfaces.tests.metamer.ftest.annotations.Uses;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestTooltipSimple extends AbstractMetamerTest {
 
     private static final int EVENT_OFFSET = 10;
     private static final int PRESET_OFFSET = 5;
 
     TooltipAttributes attributes = new TooltipAttributes();
     JQueryLocator panel = pjq("div[id$=panel]");
     TooltipModel tooltip = new TooltipModel(jq(".rf-tt"), panel);
 
     Point eventPosition;
 
     @Inject
     @Use(empty = true)
     Positioning direction;
 
     Integer[] offsets = new Integer[] { 0, PRESET_OFFSET, -PRESET_OFFSET };
 
     @Inject
     @Use(ints = 0)
     Integer verticalOffset;
 
     @Inject
     @Use(ints = 0)
     Integer horizontalOffset;
 
     @Inject
     @Use(empty = true)
     Event domEvent;
     Event[] domEvents = { CLICK, DBLCLICK, MOUSEDOWN, MOUSEMOVE, MOUSEOUT, MOUSEOVER, MOUSEUP };
 
     @Inject
     @Use(empty = true)
     Boolean followMouse = true;
 
     @Inject
     @Use(empty = true)
     Integer presetDelay;
 
     @Inject
     @Use(empty = true)
     TooltipLayout layout;
 
     @Inject
     @Use(empty = true)
     TooltipMode mode;
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/richTooltip/simple.xhtml");
     }
 
     @Test
     public void testLifecycle() {
         attributes.setMode(TooltipMode.ajax);
         retrieveRequestTime.initializeValue();
         tooltip.recall();
         waitGui.waitForChange(retrieveRequestTime);
         phaseInfo.assertPhases(RESTORE_VIEW, APPLY_REQUEST_VALUES, RENDER_RESPONSE);
     }
 
     @Test
     public void testData() {
         attributes.setData("RichFaces 4");
         attributes.setOncomplete("data = event.data");
         attributes.setMode(TooltipMode.ajax);
 
         retrieveRequestTime.initializeValue();
         tooltip.recall();
         waitGui.waitForChange(retrieveRequestTime);
 
         assertEquals(retrieveWindowData.retrieve(), "RichFaces 4");
     }
 
     @Test
     public void testRequestEventHandlers() {
         attributes.setMode(TooltipMode.ajax);
 
         super.testRequestEventsBefore("begin", "beforedomupdate", "complete");
         retrieveRequestTime.initializeValue();
         tooltip.recall();
         waitGui.waitForChange(retrieveRequestTime);
         super.testRequestEventsAfter("begin", "beforedomupdate", "complete");
     }
 
     @Test
     public void testDir() {
         super.testDir(tooltip);
     }
 
     @Test
     @Uses({ @Use(field = "direction", enumeration = true), @Use(field = "verticalOffset", value = "offsets"),
         @Use(field = "horizontalOffset", value = "offsets") })
     public void testPositioning() {
         attributes.setDirection(direction);
         attributes.setHorizontalOffset(horizontalOffset);
         attributes.setVerticalOffset(verticalOffset);
 
         if (direction == Positioning.auto) {
             direction = Positioning.topRight;
         }
 
         recallTooltipInRightBottomCornerOfPanel(0, 0);
 
         final Point tooltipPosition = selenium.getElementPosition(tooltip);
         final Dimension tooltipDimension = selenium.getElementDimension(tooltip);
 
         if (getHorizontalAlignment() != null) {
             switch (getHorizontalAlignment()) {
                 case RIGHT:
                     assertEquals(tooltipPosition.getX(), eventPosition.getX() + horizontalOffset);
                     break;
                 case LEFT:
                     assertEquals(tooltipPosition.getX() + tooltipDimension.getWidth(), eventPosition.getX()
                         - horizontalOffset);
                 default:
             }
         }
 
         if (getVerticalAlignment() != null) {
             switch (getVerticalAlignment()) {
                 case BOTTOM:
                     assertEquals(tooltipPosition.getY(), eventPosition.getY() + verticalOffset);
                     break;
                 case TOP:
                     assertEquals(tooltipPosition.getY() + tooltipDimension.getHeight(), eventPosition.getY()
                         - verticalOffset);
                 default:
             }
         }
     }
 
     @Test
     @Use(field = "followMouse", booleans = { true, false })
     public void testFollowMouse() {
         attributes.setFollowMouse(followMouse);
 
         recallTooltipInRightBottomCornerOfPanel(0, 0);
 
         Point initialPosition = selenium.getElementPosition(tooltip);
 
         recallTooltipInRightBottomCornerOfPanel(-5, -5);
 
         Point nextPosition = selenium.getElementPosition(tooltip);
 
         int expectedOffset = (followMouse) ? -5 : 0;
 
         assertEquals(nextPosition.getX() - initialPosition.getX(), expectedOffset);
         assertEquals(nextPosition.getY() - initialPosition.getY(), expectedOffset);
     }
 
     @Test
     @Use(field = "presetDelay", ints = { 0, 1000, 5000 })
     public void testHideDelay() {
 
         attributes.setMode(TooltipMode.ajax);
         attributes.setHideDelay(presetDelay);
 
         new DelayTester(presetDelay) {
             public void beforeAction() {
                 tooltip.recall();
             }
 
             public void action() {
                 tooltip.hide();
                 waitGui.timeout(presetDelay + 2000).until(isNotDisplayed.locator(tooltip));
             }
         }.run();
     }
 
     @Test
     public void testHideEvent() {
         attributes.setHideEvent("mouseup");
 
         tooltip.recall();
 
         selenium.mouseUpAt(panel, new Point(5, 5));
         waitGui.until(isNotDisplayed.locator(tooltip));
     }
 
     @Test
     public void testLang() {
         super.testLang(tooltip);
     }
 
     @Test
     @Use(field = "layout", enumeration = true)
     public void testLayout() {
         attributes.setLayout(layout);
 
         String expectedTagName = (layout == TooltipLayout.block) ? "div" : "span";
 
         String actualTagName = selenium.getEval(JQueryScript.jqScript(tooltip, "get(0).tagName"));
         actualTagName = actualTagName.toLowerCase();
 
         assertEquals(actualTagName, expectedTagName);
     }
 
     @Test
     public void testLimitRender() {
         attributes.setLimitRender(true);
         attributes.setRender("@this renderChecker");
         attributes.setMode(TooltipMode.ajax);
 
         retrieveRenderChecker.initializeValue();
         retrieveRequestTime.initializeValue();
 
         tooltip.recall();
         waitAjax.waitForChange(retrieveRenderChecker);
         assertFalse(retrieveRequestTime.isValueChanged());
     }
 
     @Test
     @Use(field = "mode", enumeration = true)
     public void testMode() {
         attributes.setMode(mode);
 
         retrieveRequestTime.initializeValue();
 
         tooltip.recall();
         assertEquals(retrieveRequestTime.isValueChanged(), mode == TooltipMode.ajax);
 
         retrieveRequestTime.initializeValue();
         tooltip.hide();
         waitGui.until(isNotDisplayed.locator(tooltip));
         assertFalse(retrieveRequestTime.isValueChanged());
     }
 
     @Test
     @Use(field = "domEvent", value = "domEvents")
     public void testDomEvents() {
         tooltip.recall();
 
         testFireEvent(domEvent, tooltip);
     }
 
     @Test
     public void testRendered() {
         attributes.setRendered(false);
 
         assertFalse(selenium.isElementPresent(tooltip));
     }
 
     @Test
     @Use(field = "presetDelay", ints = { 0, 1000, 5000 })
    @RegressionTest("https://issues.jboss.org/browse/RF-10522")
     public void testShowDelay() {
 
         attributes.setMode(TooltipMode.client);
         attributes.setShowDelay(presetDelay);
 
         new DelayTester(presetDelay) {
             public void action() {
                 tooltip.recall();
                 waitGui.timeout(presetDelay + 2000).until(isDisplayed.locator(tooltip));
             }
 
             public void afterAction() {
                 tooltip.hide();
             }
         }.run();
     }
 
     @Test
     public void testShowEvent() {
         attributes.setShowEvent("mouseup");
 
         selenium.mouseUpAt(panel, new Point(5, 5));
         waitGui.until(isDisplayed.locator(tooltip));
     }
 
     @Test
     public void testStatus() {
         attributes.setStatus("statusChecker");
         attributes.setMode(TooltipMode.ajax);
 
         retrieveStatusChecker.initializeValue();
         tooltip.recall();
         waitAjax.waitForChange(retrieveStatusChecker);
     }
 
     @Test
     public void testStyle() {
         super.testStyle(tooltip, "style");
     }
 
     @Test
     public void testStyleClass() {
         super.testStyleClass(tooltip, "styleClass");
     }
 
     @Test
     public void testTitle() {
         super.testTitle(tooltip);
     }
 
     @Test
     public void testZindex() {
         attributes.setZindex(10);
 
         String zindex = selenium.getStyle(tooltip, CssProperty.Z_INDEX);
         assertEquals(zindex, "10");
     }
 
     private void recallTooltipInRightBottomCornerOfPanel(int offsetX, int offsetY) {
         final Point panelPosition = selenium.getElementPosition(panel);
         final Dimension panelDimension = selenium.getElementDimension(panel);
 
         eventPosition = new Point(panelPosition.getX() + panelDimension.getWidth() - EVENT_OFFSET, panelPosition.getY()
             + panelDimension.getHeight() - EVENT_OFFSET);
 
         tooltip.recall(panelDimension.getWidth() - EVENT_OFFSET + offsetX, panelDimension.getHeight() - EVENT_OFFSET
             + offsetY);
     }
 
     private HorizontalAlignment getHorizontalAlignment() {
         if (direction != null) {
             if (direction.toString().toLowerCase().contains("left")) {
                 return HorizontalAlignment.LEFT;
             }
             if (direction.toString().toLowerCase().contains("right")) {
                 return HorizontalAlignment.RIGHT;
             }
         }
         return null;
     }
 
     private VerticalAlignment getVerticalAlignment() {
         if (direction != null) {
             if (direction.toString().contains("top")) {
                 return VerticalAlignment.TOP;
             }
             if (direction.toString().contains("bottom")) {
                 return VerticalAlignment.BOTTOM;
             }
         }
         return null;
     }
 
     private enum HorizontalAlignment {
         LEFT, RIGHT
     }
 
     private enum VerticalAlignment {
         TOP, BOTTOM
     }
 
 }
