 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.richfaces.component;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.html.HtmlForm;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.PhaseId;
 
 import org.ajax4jsf.event.EventsQueue;
 import org.ajax4jsf.tests.AbstractAjax4JsfTestCase;
 import org.ajax4jsf.tests.MockViewRoot;
 import org.apache.commons.lang.StringUtils;
 import org.richfaces.event.SwitchablePanelSwitchEvent;
 
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlScript;
 
 /**
  * Unit test for Datascroller component.
  */
 public class TogglePanelComponentTest extends AbstractAjax4JsfTestCase {
     private static Set javaScripts = new HashSet();
     private static final boolean IS_PAGE_AVAILABILITY_CHECK = true;
 
     static {
         javaScripts.add("org.ajax4jsf.javascript.AjaxScript");
         javaScripts.add("org.ajax4jsf.javascript.PrototypeScript");
         javaScripts.add("org/ajax4jsf/javascript/scripts/form.js");
         javaScripts.add("scripts/togglePanel.js");
     }
 
     private UITogglePanel togglePanel;
 
     private UIToggleControl toggleControl;
 
     private UIComponent form;
 
     private UIOutput a;
 
     private UIOutput b;
 
     private UIOutput c;
 
     /**
      * Create the test case
      *
      * @param testName
      *            name of the test case
      */
     public TogglePanelComponentTest(String testName) {
         super(testName);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.ajax4jsf.tests.AbstractAjax4JsfTestCase#setUp()
      */
     public void setUp() throws Exception {
         super.setUp();
 
         a = (UIOutput) application.createComponent(UIOutput.COMPONENT_TYPE);
         a.setId("a_output");
         a.setValue("a");
 
         b = (UIOutput) application.createComponent(UIOutput.COMPONENT_TYPE);
         b.setId("b_output");
         b.setValue("b");
 
         c = (UIOutput) application.createComponent(UIOutput.COMPONENT_TYPE);
         c.setId("c_output");
         c.setValue("c");
 
         form = new HtmlForm();
         form.setId("form");
         facesContext.getViewRoot().getChildren().add(form);
 
         togglePanel = (UITogglePanel) application
                 .createComponent("org.richfaces.TogglePanel");
         togglePanel.setId("TogglePanel");
         togglePanel.getFacets().put("a", a);
         togglePanel.getFacets().put("b", b);
         togglePanel.getFacets().put("c", c);
         togglePanel.setStateOrder("a,b,c");
         togglePanel.setInitialState("a");
 
         form.getChildren().add(togglePanel);
 
         toggleControl = (UIToggleControl) application
                 .createComponent("org.richfaces.ToggleControl");
         toggleControl.setId("ToggleControl");
         toggleControl.setFor(togglePanel.getId());
         form.getChildren().add(toggleControl);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.ajax4jsf.tests.AbstractAjax4JsfTestCase#tearDown()
      */
     public void tearDown() throws Exception {
         super.tearDown();
         togglePanel = null;
         toggleControl = null;
         form = null;
     }
 
     /**
      * Test component rendering
      *
      * @throws Exception
      */
 
     public void testRender() throws Exception {
         HtmlPage page = renderView();
         assertNotNull(page);
         // System.out.println(page.asXml());
 
         HtmlElement div = page.getHtmlElementById(togglePanel
                 .getClientId(facesContext));
         String classAttr0 = div.getAttributeValue("class");
        assertTrue(classAttr0.contains("rich-toggle-panel"));
         assertNotNull(div);
         assertEquals("div", div.getNodeName());
 
         HtmlElement div_control = page.getHtmlElementById(toggleControl
                 .getClientId(facesContext));
         String classAttr = div_control.getAttributeValue("class");
         assertTrue(classAttr.contains("rich-tglctrl"));
     }
 
     /**
      * Test style rendering
      *
      * @throws Exception
      */
     public void testRenderStyle() throws Exception {
         HtmlPage page = renderView();
         assertNotNull(page);
         List links = page.getDocumentElement().getHtmlElementsByTagName("link");
 
         assertEquals(1, links.size());
         HtmlElement link = (HtmlElement) links.get(0);
         assertTrue(link.getAttributeValue("href").contains(
                 "css/toggleControl.xcss"));
     }
 
     /**
      * Test script rendering
      *
      * @throws Exception
      */
     public void testRenderScript() throws Exception {
         HtmlPage page = renderView();
         assertNotNull(page);
         // System.out.println(page.asXml());
 
         assertEquals(getCountValidScripts(page, javaScripts, IS_PAGE_AVAILABILITY_CHECK).intValue(), javaScripts.size());
     }
 
     public void testSwitch() throws Exception {
 
         HtmlPage page = renderView();
         // System.out.println(page.asXml());
         togglePanel.setValue("a");
         togglePanel.broadcast(new SwitchablePanelSwitchEvent(togglePanel, null,
                 toggleControl));
         assertFalse(((String) togglePanel.getValue()).equals("a"));
         assertTrue(((String) togglePanel.getValue()).equals("b"));
 
         toggleControl.setSwitchToState("a");
         togglePanel.broadcast(new SwitchablePanelSwitchEvent(togglePanel,
                 "null", toggleControl));
         assertFalse(((String) togglePanel.getValue()).equals("b"));
         assertTrue(((String) togglePanel.getValue()).equals("a"));
 
         toggleControl.setSwitchToState(null);
         togglePanel.setStateOrder("c,b,a");
         togglePanel.broadcast(new SwitchablePanelSwitchEvent(togglePanel,
                 "null", toggleControl));
         assertFalse(((String) togglePanel.getValue()).equals("a"));
         assertTrue(((String) togglePanel.getValue()).equals("c"));
 
         togglePanel.setValue(null);
         togglePanel.setStateOrder("c,b,a");
         togglePanel.broadcast(new SwitchablePanelSwitchEvent(togglePanel,
                 "null", toggleControl));
         assertTrue(((String) togglePanel.getValue()).equals("c"));
         
         togglePanel.setStateOrder("");
         toggleControl.setSwitchToState("d");
         try {
             togglePanel.broadcast(new SwitchablePanelSwitchEvent(togglePanel,
                     "null", toggleControl));
             //fail();
         } catch (Exception ex) {
         }
     }
 
     /**
      * Test for UITogglePanel & UIToggleControl classes methods.
      *
      * @throws Exception
      */
     public void testUIComponents() throws Exception {
 
         togglePanel.setStateOrder("A,B,Y,B,C");
         List stateOrderList = togglePanel.getStateOrderList();
         assertNotNull(stateOrderList);
         assertEquals(5, stateOrderList.size());
 
         togglePanel.setStateOrder(null);
         stateOrderList = togglePanel.getStateOrderList();
         assertNotNull(stateOrderList);
         assertEquals(0, stateOrderList.size());
 
         Object switchValue = togglePanel.convertSwitchValue(new UIOutput(),
                 "ABYBC");
         assertEquals("ABYBC", (String) switchValue);
 
         toggleControl.setFor(togglePanel.getClientId(facesContext));
         toggleControl.setReRender(togglePanel.getClientId(facesContext));
         assertEquals(togglePanel.getClientId(facesContext), toggleControl
                 .getReRender());
 
         toggleControl.setFor(null);
         try {
             toggleControl.getPanel();
             assertTrue(false);
         } catch (FacesException e) {
         }
     }
 
     /**
      * Test for TogglePanel in "client" mode.
      *
      * @throws Exception
      */
     public void testTogglePanelInClientSwichMode() throws Exception {
 
         togglePanel.setSwitchType(UITogglePanel.CLIENT_METHOD);
 
         toggleControl.getAttributes().put("onclick", "someOnClick");
 
         HtmlPage page = renderView();
         assertNotNull(page);
         // System.out.println(page.asXml());
 
         HtmlElement div = page.getHtmlElementById(togglePanel
                 .getClientId(facesContext));
         assertNotNull(div);
         assertEquals("div", div.getNodeName());
 
         HtmlElement anchor = (HtmlElement) div.getHtmlElementById(toggleControl
                 .getClientId(facesContext));
         assertNotNull(anchor);
         String classAttr = anchor.getAttributeValue("onclick");
         assertTrue(classAttr.contains("someOnClick;"));
 
         List divs = div.getHtmlElementsByTagName("div");
         assertTrue(divs.size() > 0);
         assertEquals(togglePanel.getStateOrderList().size() + 1, 4);
         div = (HtmlElement) divs.get(0);
         assertNotNull(div);
         classAttr = div.getAttributeValue("style");
         assertTrue(classAttr.contains("display:"));
         div = (HtmlElement) divs.get(togglePanel.getStateOrderList().size());
         assertNotNull(div);
         classAttr = div.getAttributeValue("style");
         assertTrue(classAttr.contains("display: none;"));
     }
 
     /**
      * Test for TogglePanel components "doDecode" method.
      */
     public void testDoDecodeClientMode() throws Exception {
 
         togglePanel.setImmediate(true);
         togglePanel.setSwitchType(UITogglePanel.CLIENT_METHOD);
 
         externalContext.getRequestParameterMap().put(
                 togglePanel.getClientId(facesContext), "ABYBC");
         togglePanel.decode(facesContext);
 
         externalContext.getRequestParameterMap().put(
                 toggleControl.getClientId(facesContext), "ABYBC");
         toggleControl.decode(facesContext);
 
         MockViewRoot mockViewRoot = (MockViewRoot) facesContext.getViewRoot();
         EventsQueue events = mockViewRoot.getEventsQueue(PhaseId.APPLY_REQUEST_VALUES);
         assertNotNull(events);
         assertEquals(2, events.size());
 
         FacesEvent event = (FacesEvent) events.remove();
         assertTrue(event instanceof SwitchablePanelSwitchEvent);
         SwitchablePanelSwitchEvent switchEvent = (SwitchablePanelSwitchEvent) event;
         assertEquals(switchEvent.getValue(), "ABYBC");
 
         events = mockViewRoot.getEventsQueue(PhaseId.INVOKE_APPLICATION);
         assertNotNull(events);
         assertEquals(1, events.size());
 
         event = (FacesEvent) events.remove();
 
         assertTrue(event instanceof ActionEvent);
         ActionEvent actionEvent = (ActionEvent) event;
         assertEquals(actionEvent.getSource(), toggleControl);
 
     }
 
     /**
      * Test "doDecode" method. No events must be generated.
      */
     public void testDoDecodeNoEvents() throws Exception {
     	//FIXME: Placeholder
     	if (true) {
     		return;
     	}
         togglePanel.setSwitchType(UITogglePanel.CLIENT_METHOD);
 
         externalContext.getRequestParameterMap().put("ABYBC", "ABYBC");
         toggleControl.decode(facesContext);
         togglePanel.decode(facesContext);
 
         MockViewRoot mockViewRoot = (MockViewRoot) facesContext.getViewRoot();
         EventsQueue events = mockViewRoot.getEventsQueue(PhaseId.INVOKE_APPLICATION);
         assertNotNull(events);
         assertEquals(0, events.size());
 
         togglePanel.setSwitchType(UITogglePanel.AJAX_METHOD);
         externalContext.getRequestParameterMap().put(
                 togglePanel.getClientId(facesContext), "ABYBC");
         externalContext.getRequestParameterMap().put(
                 toggleControl.getClientId(facesContext), "ABYBC");
         toggleControl.decode(facesContext);
         togglePanel.decode(facesContext);
 
         mockViewRoot = (MockViewRoot) facesContext.getViewRoot();
         events = mockViewRoot.getEventsQueue(PhaseId.INVOKE_APPLICATION);
         assertNotNull(events);
         assertEquals(0, events.size());
     }
 
     /**
      * Test for UIToggleControl getPanel method.
      */
     public void testUIToggleControlGetPanelMethod() throws Exception {
 
         assertEquals(toggleControl.getPanel(), togglePanel);
 
         toggleControl.setFor("ABYBC");
         try {
             UIComponent targetComponent = toggleControl.getPanel();
             assertTrue(false);
         } catch (Exception e) {
             assertTrue(e instanceof FacesException);
         }
 
         toggleControl.setFor(null);
         togglePanel.getChildren().add(a);
         a.getChildren().add(b);
         b.getChildren().add(toggleControl);
         assertEquals(toggleControl.getPanel(), togglePanel);
 
         togglePanel.getChildren().remove(a);
         try {
             UIComponent targetComponent = toggleControl.getPanel();
             assertTrue(false);
         } catch (Exception e) {
             assertTrue(e instanceof FacesException);
         }
     }
 
 }
