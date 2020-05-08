 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2009, Red Hat, Inc. and individual contributors
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
 
 package org.jboss.richfaces.integrationTest.contextMenu;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import org.jboss.richfaces.integrationTest.AbstractSeleniumRichfacesTestCase;
import org.jboss.test.selenium.waiting.Condition;
import org.jboss.test.selenium.waiting.Wait;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Tests context menu in the example with table.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class ContextMenuTableTestCase extends AbstractSeleniumRichfacesTestCase {
 
     private final String LOC_SECOND_EXAMPLE_HEADER = getLoc("SECOND_EXAMPLE_HEADER");
     private final String LOC_SECOND_CONTEXT_MENU = getLoc("SECOND_CONTEXT_MENU");
     private final String LOC_SECOND_LAST_MENU_ACTION = getLoc("SECOND_LAST_MENU_ACTION");
 
     private final String LOC_SECOND_CAR_DETAILS = getLoc("SECOND_CAR_DETAILS");
     private final String LOC_SECOND_ACTIONS = getLoc("SECOND_ACTIONS");
     private final String LOC_SECOND_ACTION_PREFORMATTED = getLoc("SECOND_ACTION_PREFORMATTED");
 
     private final String LOC_SECOND_LINE_3_COLUMN_1 = getLoc("SECOND_LINE_3_COLUMN_1");
     private final String LOC_SECOND_LINE_3_COLUMN_2 = getLoc("SECOND_LINE_3_COLUMN_2");
     private final String LOC_SECOND_LINE_6_COLUMN_2 = getLoc("SECOND_LINE_6_COLUMN_2");
     private final String LOC_SECOND_LINE_1_COLUMN_3 = getLoc("SECOND_LINE_1_COLUMN_3");
 
     private final String MSG_SECOND_CAR_DETAILS_PREFORMATTED = getMsg("SECOND_CAR_DETAILS_PREFORMATTED");
     private final String MSG_SECOND_PUT_PRODUCER_MODEL_TO_BASKET_PREFORMATTED = getMsg("SECOND_PUT_PRODUCER_MODEL_TO_BASKET_PREFORMATTED");
     private final String MSG_SECOND_READ_COMMENTS_PREFORMATTED = getMsg("SECOND_READ_COMMENTS_PREFORMATTED");
     private final String MSG_SECOND_GO_TO_PRODUCER_SITE_PREFORMATTED = getMsg("SECOND_GO_TO_PRODUCER_SITE_PREFORMATTED");
 
     /**
      * Tests context menu. It checks that context menu is not visible at start,
      * clicks into various places in table and checks that menu appeared.
      */
     @Test
     public void testTableContextMenu() {
         // check that the context menu is not visible
         boolean present = selenium.isElementPresent(LOC_SECOND_CONTEXT_MENU);
         assertFalse(present, "Context menu should be invisible at start.");
 
         // check that the 'Last Menu Action' is empty
         String text = selenium.getText(LOC_SECOND_LAST_MENU_ACTION);
         assertEquals(text, "", "Last menu action should be empty.");
 
         // open context menu on third line, first column
         selenium.click(LOC_SECOND_LINE_3_COLUMN_1);
         waitForElement(LOC_SECOND_CONTEXT_MENU);
         assertTrue(isDisplayed(LOC_SECOND_CONTEXT_MENU),
                 "Context menu should be visible after clicking on first column.");
 
         // open context menu on sixth line, second column
         selenium.click(LOC_SECOND_LINE_6_COLUMN_2);
         waitForElement(LOC_SECOND_CONTEXT_MENU);
         assertTrue(isDisplayed(LOC_SECOND_CONTEXT_MENU),
                 "Context menu should be visible after clicking on second column.");
 
         // open context menu on first line, third column
         selenium.click(LOC_SECOND_LINE_1_COLUMN_3);
         waitForElement(LOC_SECOND_CONTEXT_MENU);
         assertTrue(isDisplayed(LOC_SECOND_CONTEXT_MENU),
                 "Context menu should be visible after clicking on third column.");
     }
 
     /**
      * Tests clicking on "Car Details" in context menu".
      */
     @Test
     public void testClickOnCarDetails() {
         String producer = selenium.getText(LOC_SECOND_LINE_3_COLUMN_1);
         String model = selenium.getText(LOC_SECOND_LINE_3_COLUMN_2);
 
         // open context menu on third line, first column
         selenium.click(LOC_SECOND_LINE_3_COLUMN_1);
         waitForElement(LOC_SECOND_CONTEXT_MENU);
         assertTrue(isDisplayed(LOC_SECOND_CONTEXT_MENU),
                 "Context menu should be visible after clicking on first column.");
 
         // click '<car> details'
         selenium.click(LOC_SECOND_CAR_DETAILS);
         waitFor(400);
         String text = selenium.getText(LOC_SECOND_LAST_MENU_ACTION);
         assertEquals(text, format(MSG_SECOND_CAR_DETAILS_PREFORMATTED, producer, model), "Details of car.");
     }
 
     /**
      * Tests clicking on "Put Car to Basket" in context menu.
      */
     @Test
     public void testClickOnPutCarToBasket() {
         clickAction(1);
     }
 
     /**
      * Tests clicking on "Read Comments" in context menu.
      */
     @Test
     public void testClickOnReadComments() {
         clickAction(2);
     }
 
     /**
      * Tests clicking on "Go to Producer Site" in context menu.
      */
     @Test
     public void testClickOnGoToProducerSite() {
         clickAction(3);
     }
 
     /**
      * Tests the "View Source". It checks that the source code is not visible,
      * clicks on the link, and checks 15 lines of source code.
      */
     @Test
     public void testContextMenuTableSource() {
         String[] strings = new String[] { "<f:subview xmlns=\"http://www.w3.org/1999/xhtml\"", "<h:form id=\"form\">",
                 "<rich:contextMenu attached=\"false\" id=\"menu\" submitMode=\"ajax\">",
                 "<rich:menuItem ajaxSingle=\"true\">",
                 "<a4j:actionparam name=\"det\" assignTo=\"#{ddmenu.current}\" value=\"{car} {model} details\"/>",
                 "<rich:menuGroup value=\"Actions\">  ", "<rich:menuItem value=\"Read Comments\" ajaxSingle=\"true\">",
                 "<rich:dataTable value=\"#{dataTableScrollerBean.tenRandomCars}\" var=\"car\" id=\"table\"",
                 "onRowMouseOver=\"this.style.backgroundColor='#F8F8F8'\"",
                 "onRowMouseOut=\"this.style.backgroundColor='#{a4jSkin.tableBackgroundColor}'\" rowClasses=\"cur\">",
                 "<rich:column>", "<rich:componentControl event=\"onRowClick\" for=\"menu\" operation=\"show\">",
                 "<f:param value=\"#{car.model}\" name=\"model\"/>", "<a4j:outputPanel ajaxRendered=\"true\">",
                 "<f:facet name=\"header\">Last Menu Action</f:facet>", };
 
         abstractTestSource(2, "View Source", strings);
     }
 
     /**
      * Helper method that performs clicks on items in submenu of context menu.
      * 
      * @param index
      *            which action should be performed
      */
     private void clickAction(int index) {
         String producer = selenium.getText(LOC_SECOND_LINE_3_COLUMN_1);
         String model = selenium.getText(LOC_SECOND_LINE_3_COLUMN_2);
 
         // open context menu on third line, first column
         selenium.click(LOC_SECOND_LINE_3_COLUMN_1);
         waitForElement(LOC_SECOND_CONTEXT_MENU);
         assertTrue(isDisplayed(LOC_SECOND_CONTEXT_MENU),
                 "Context menu should be visible after clicking on first column.");
 
         // click "Put <car> To Basket", "Read Comments" or
         // "Go to <producer> site"
         selenium.click(format(LOC_SECOND_ACTION_PREFORMATTED, index));
        
        Wait.timeout(15000).failWith("Action was not performed.").until(new Condition() {
            public boolean isTrue() {
                return !selenium.getText(LOC_SECOND_LAST_MENU_ACTION).equals("");
            }
        });
        
         String text = selenium.getText(LOC_SECOND_LAST_MENU_ACTION);
 
         switch (index) {
         case 1:
             assertEquals(text, format(MSG_SECOND_PUT_PRODUCER_MODEL_TO_BASKET_PREFORMATTED, producer, model),
                     "Action put to basket:");
             break;
         case 2:
             assertEquals(text, MSG_SECOND_READ_COMMENTS_PREFORMATTED, "Action read comments:");
             break;
         case 3:
             assertEquals(text, format(MSG_SECOND_GO_TO_PRODUCER_SITE_PREFORMATTED, producer), "Action go to site:");
             break;
         default:
             fail("Wrong index.");
         }
     }
 
     /**
      * Loads the needed page.
      */
     @SuppressWarnings("unused")
     @BeforeMethod
     private void loadPage() {
         openComponent("Context Menu");
         scrollIntoView(LOC_SECOND_EXAMPLE_HEADER, true);
     }
 }
