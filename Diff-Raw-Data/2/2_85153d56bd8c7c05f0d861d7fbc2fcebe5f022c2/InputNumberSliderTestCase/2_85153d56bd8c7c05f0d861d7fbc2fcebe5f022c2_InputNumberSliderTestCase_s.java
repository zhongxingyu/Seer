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
 
 package org.jboss.richfaces.integrationTest.inputNumberSlider;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import org.jboss.richfaces.integrationTest.AbstractSeleniumRichfacesTestCase;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Test case that tests the input number slider.
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class InputNumberSliderTestCase extends
 		AbstractSeleniumRichfacesTestCase {
 
 	// messages
 	private final String MSG_HANDLE_N_PX = getMsg("HANDLE_N_PX");
 	private final String MSG_TIP_N_PX = getMsg("TIP_N_PX");
 	private final String MSG_TIP_SHOULD_BE_VISIBLE = getMsg("TIP_SHOULD_BE_VISIBLE");
 	private final String MSG_TIP_SHOULD_NOT_BE_VISIBLE = getMsg("TIP_SHOULD_NOT_BE_VISIBLE");
 	private final String MSG_OFFSETS_SHOULD_BE_THE_SAME = getMsg("OFFSETS_SHOULD_BE_THE_SAME");
 	private final String MSG_TIP_FIRST_HALF = getMsg("TIP_FIRST_HALF");
 	private final String MSG_TIP_SECOND_HALF = getMsg("TIP_SECOND_HALF");
 	private final String MSG_HANDLE_FIRST_HALF = getMsg("HANDLE_FIRST_HALF");
 	private final String MSG_HANDLE_SECOND_HALF = getMsg("HANDLE_SECOND_HALF");
 	private final String MSG_VALUE_IN_INPUT = getMsg("VALUE_IN_INPUT");
 
 	// locators
 	private final String LOC_EXAMPLE_HEADER = getLoc("EXAMPLE_HEADER");
 	private final String LOC_FIRST = format(getLoc("SLIDER_N"), 0);
 	private final String LOC_FIRST_HANDLE = format(getLoc("SLIDER_N_HANDLE"), 0);
 	private final String LOC_FIRST_TIP = format(getLoc("SLIDER_N_TIP"), 0);
 	private final String LOC_FIRST_INPUT = format(getLoc("SLIDER_N_INPUT"), 0);
 
 	private final String LOC_SECOND = format(getLoc("SLIDER_N"), 1);
 	private final String LOC_SECOND_HANDLE = format(getLoc("SLIDER_N_HANDLE"), 1);
 	private final String LOC_SECOND_TIP = format(getLoc("SLIDER_N_TIP"), 1);
 	private final String LOC_SECOND_INPUT = format(getLoc("SLIDER_N_INPUT"), 1);
 
 	private final String LOC_THIRD = format(getLoc("SLIDER_N"), 2);
 	private final String LOC_THIRD_HANDLE = format(getLoc("SLIDER_N_HANDLE"), 2);
 	private final String LOC_THIRD_TIP = format(getLoc("SLIDER_N_TIP"), 2);
 	private final String LOC_THIRD_INPUT = format(getLoc("SLIDER_N_INPUT"), 2);
 	
 	// tolerance (in pixels)
	private final int DELTA = 10;
 
 	/**
 	 * Tests clicking on the first slider. First, it checks the offset of the handler and
 	 * tip box and checks that tip is hidden. Then it clics to the first half of the slider
 	 * and verifies that the tip is visible while the mouse button is pressed.
 	 * In the end it verifies that the handler and tip box moved.
 	 */
 	@Test
 	public void testFirstSliderMouse() {
 		assertTrue(Math.abs(getOffset(LOC_FIRST_HANDLE + "@style") - 75) < DELTA, format(MSG_HANDLE_N_PX, 75));
 		assertTrue(Math.abs(getOffset(LOC_FIRST_TIP + "@style") - 75) < DELTA, format(MSG_TIP_N_PX, 75));
 		assertFalse(isDisplayed(LOC_FIRST_TIP), MSG_TIP_SHOULD_NOT_BE_VISIBLE);
 
 		selenium.mouseDownAt(LOC_FIRST, "20,3");
 		assertTrue(isDisplayed(LOC_FIRST_TIP), MSG_TIP_SHOULD_BE_VISIBLE);
 		selenium.mouseUp(LOC_FIRST);
 
 		int tipOffset = getOffset(LOC_FIRST_TIP + "@style");
 		int handleOffset = getOffset(LOC_FIRST_HANDLE + "@style");
 		int value = Integer.parseInt(selenium.getValue(LOC_FIRST_INPUT));
 
 		assertEquals(tipOffset, handleOffset, MSG_OFFSETS_SHOULD_BE_THE_SAME);
 		assertTrue(Math.abs(tipOffset - value * 1.5) < DELTA, MSG_TIP_FIRST_HALF);
 		assertTrue(handleOffset < 75, MSG_HANDLE_FIRST_HALF);
 	}
 
 	/**
 	 * Tests typing into first slider's input box. First it checks the position of the tip box and 
 	 * handle. Then it types a number from the first and second half of slider and checks 
 	 * the position of tip and
 	 * handle. Then it clicks min value, max value, negative integer, an integer bigger than 
 	 * maximum, and decimal number and checks that the value of the input box changes to the
 	 * right value.
 	 */
 	@Test
 	public void testFirstSliderKeyboard() {
 		selenium.type(LOC_FIRST_INPUT, "10");
 		int tipOffset = getOffset(LOC_FIRST_TIP + "@style");
 		int handleOffset = getOffset(LOC_FIRST_HANDLE + "@style");
 
 		assertEquals(tipOffset, handleOffset, MSG_OFFSETS_SHOULD_BE_THE_SAME);
 		assertTrue(Math.abs(tipOffset - 10 * 1.5) < DELTA, MSG_TIP_FIRST_HALF);
 		assertTrue(handleOffset < 75, MSG_HANDLE_FIRST_HALF);
 
 		selenium.type(LOC_FIRST_INPUT, "90");
 		tipOffset = getOffset(LOC_FIRST_TIP + "@style");
 		handleOffset = getOffset(LOC_FIRST_HANDLE + "@style");
 
 		assertEquals(tipOffset, handleOffset, MSG_OFFSETS_SHOULD_BE_THE_SAME);
 		assertTrue(Math.abs(tipOffset - 90 * 1.5) < DELTA, MSG_TIP_SECOND_HALF);
 		assertTrue(handleOffset > 75, MSG_HANDLE_SECOND_HALF);
 
 		selenium.type(LOC_FIRST_INPUT, "0");
 		int value = Integer.parseInt(selenium.getValue(LOC_FIRST_INPUT));
 		assertEquals(value, 0, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_FIRST_INPUT, "100");
 		value = Integer.parseInt(selenium.getValue(LOC_FIRST_INPUT));
 		assertEquals(value, 100, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_FIRST_INPUT, "-10");
 		value = Integer.parseInt(selenium.getValue(LOC_FIRST_INPUT));
 		assertEquals(value, 0, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_FIRST_INPUT, "130");
 		value = Integer.parseInt(selenium.getValue(LOC_FIRST_INPUT));
 		assertEquals(value, 100, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_FIRST_INPUT, "1.30");
 		value = Integer.parseInt(selenium.getValue(LOC_FIRST_INPUT));
 		assertEquals(value, 1, MSG_VALUE_IN_INPUT);
 	}
 
 	/**
 	 * Tests clicking on the second slider. First, it checks the offset of the handler and
 	 * tip box and checks that tip is hidden. Then it clics to the first half of the slider
 	 * and verifies that the tip is invisible while the mouse button is pressed.
 	 * In the end it verifies that the handler and tip box moved.
 	 */
 	@Test
 	public void testSecondSlider() {
 		assertTrue(Math.abs(getOffset(LOC_SECOND_HANDLE + "@style") - 96) < DELTA, format(MSG_HANDLE_N_PX, 96));
 		assertTrue(Math.abs(getOffset(LOC_SECOND_TIP + "@style") - 96) < DELTA, format(MSG_TIP_N_PX, 96));
 		assertFalse(isDisplayed(LOC_SECOND_TIP), MSG_TIP_SHOULD_NOT_BE_VISIBLE);
 
 		selenium.mouseDownAt(LOC_SECOND, "20,3");
 		// it is a slider without tip so it cannot be visible
 		assertFalse(isDisplayed(LOC_SECOND_TIP), MSG_TIP_SHOULD_NOT_BE_VISIBLE);
 		selenium.mouseUp(LOC_SECOND);
 
 		int tipOffset = getOffset(LOC_SECOND_TIP + "@style");
 		int handleOffset = getOffset(LOC_SECOND_HANDLE + "@style");
 		int value = Integer.parseInt(selenium.getValue(LOC_SECOND_INPUT));
 
 		assertEquals(tipOffset, handleOffset, MSG_OFFSETS_SHOULD_BE_THE_SAME);
 		assertTrue(Math.abs(tipOffset - value * 1.5) < DELTA, MSG_TIP_FIRST_HALF);
 		assertTrue(handleOffset < 75, MSG_HANDLE_FIRST_HALF);
 	}
 
 	/**
 	 * Tests clicking on the third slider. First, it checks the offset of the handler and
 	 * tip box and checks that tip is hidden. Then it clics to the first half of the slider
 	 * and verifies that the tip is invisible while the mouse button is pressed.
 	 * In the end it verifies that the handler and tip box moved.
 	 */
 	@Test
 	public void testThirdSliderMouse() {
 		assertTrue(Math.abs(getOffset(LOC_THIRD_HANDLE + "@style") - 225) < DELTA, format(MSG_HANDLE_N_PX, 225));
 		assertTrue(Math.abs(getOffset(LOC_THIRD_TIP + "@style") - 225) < DELTA, format(MSG_TIP_N_PX, 225));
 		assertFalse(isDisplayed(LOC_THIRD_TIP), MSG_TIP_SHOULD_NOT_BE_VISIBLE);
 
 		selenium.mouseDownAt(LOC_THIRD, "20,3");
 		// slider does not use the tip so it has to be invisible
 		assertFalse(isDisplayed(LOC_THIRD_TIP), MSG_TIP_SHOULD_NOT_BE_VISIBLE);
 		selenium.mouseUp(LOC_THIRD);
 
 		int tipOffset = getOffset(LOC_THIRD_TIP + "@style");
 		int handleOffset = getOffset(LOC_THIRD_HANDLE + "@style");
 		int value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 
 		assertEquals(tipOffset, handleOffset, MSG_OFFSETS_SHOULD_BE_THE_SAME);
 		assertTrue(Math.abs(tipOffset - value * 0.46) < DELTA, MSG_TIP_FIRST_HALF);
 		assertTrue(handleOffset < 75, MSG_HANDLE_FIRST_HALF);
 	}
 
 	/**
 	 * Tests typing into third slider's input box. First it checks the position of the tip box and 
 	 * handle. Then it types a number from the first and second half of the slider
 	 * and checks the position of tip and
 	 * handle. Then it clicks min value, max value, negative integer, an integer bigger than 
 	 * maximum, and decimal number and checks that the value of the input box changes to the
 	 * right value. In the end it tries a number that should be rounded up and a number
 	 * that should be rounded down.
 	 */
 	@Test
 	public void testThirdSliderKeyboard() {
 		selenium.type(LOC_THIRD_INPUT, "10"); // 10 -> 0
 		int tipOffset = getOffset(LOC_THIRD_TIP + "@style");
 		int handleOffset = getOffset(LOC_THIRD_HANDLE + "@style");
 
 		assertEquals(tipOffset, handleOffset, MSG_OFFSETS_SHOULD_BE_THE_SAME);
 		assertTrue(tipOffset < 5, MSG_TIP_FIRST_HALF);
 		assertTrue(handleOffset < 75, MSG_HANDLE_FIRST_HALF);
 
 		selenium.type(LOC_THIRD_INPUT, "690"); // 690 -> 700
 		tipOffset = getOffset(LOC_THIRD_TIP + "@style");
 		handleOffset = getOffset(LOC_THIRD_HANDLE + "@style");
 
 		assertEquals(tipOffset, handleOffset, MSG_OFFSETS_SHOULD_BE_THE_SAME);
 		assertTrue(Math.abs(tipOffset - 0.69 * 450) < DELTA, MSG_TIP_SECOND_HALF);
 		assertTrue(handleOffset > 75, MSG_HANDLE_SECOND_HALF);
 
 		selenium.type(LOC_THIRD_INPUT, "0");
 		int value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 		assertEquals(value, 0, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_THIRD_INPUT, "1000");
 		value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 		assertEquals(value, 1000, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_THIRD_INPUT, "-10"); // -10 -> 0
 		value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 		assertEquals(value, 0, MSG_VALUE_IN_INPUT);
 		
 		selenium.type(LOC_THIRD_INPUT, "1200"); // 1200 -> 1000
 		value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 		assertEquals(value, 1000, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_THIRD_INPUT, "1.30"); // 1.30 -> 0
 		value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 		assertEquals(value, 0, MSG_VALUE_IN_INPUT);
 		
 		selenium.type(LOC_THIRD_INPUT, "524"); // 524 -> 500
 		value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 		assertEquals(value, 500, MSG_VALUE_IN_INPUT);
 
 		selenium.type(LOC_THIRD_INPUT, "525"); // 525 -> 500
 		value = Integer.parseInt(selenium.getValue(LOC_THIRD_INPUT));
 		assertEquals(value, 550, MSG_VALUE_IN_INPUT);
 	}
 
     /**
      * Tests the "View Source". It checks that the source code is not visible,
      * clicks on the link, and checks 9 lines of source code.
      */
     @Test
     public void testExampleSource() {
         String[] strings = new String[] { "<ui:composition xmlns=\"http://www.w3.org/1999/xhtml\"",
                 "<p>Here is an example of default inputNumberSlider:</p>",
                 "<rich:inputNumberSlider value=\"50\" />",
                 "<p>Here is \"minimalistic\" input:</p>",
                 "<rich:inputNumberSlider value=\"50\" showInput=\"false\"",
                 "enableManualInput=\"false\" showBoundaryValues=\"false\"",
                 "showToolTip=\"false\" />",
                 "<rich:inputNumberSlider value=\"500\" width=\"500\" maxValue=\"1000\"",
                 "step=\"50\" showToolTip=\"false\" />",
         };
 
         abstractTestSource(1, "View Source", strings);
     }
     
 	/**
 	 * Returns the offset of the element. It requires a locator for an
 	 * attribute, e.g. //div@style. It returns the 'left' attribute, e.g. for
 	 * style="visibility: visible; left: 51px;" would return 51.
 	 */
 	private int getOffset(String locator) {
 		StringBuilder attr = new StringBuilder(selenium.getAttribute(locator));
 		attr = attr.delete(0, attr.indexOf("left: "));
 		attr = attr.delete(0, 6);
 		attr = attr.delete(attr.indexOf("px;"), attr.length());
 		return Integer.parseInt(attr.toString());
 	}
 
 	/**
      * Loads the page containing needed component.
      */
     @SuppressWarnings("unused")
     @BeforeMethod
     private void loadPage() {
         openComponent("Input Number Slider");
         scrollIntoView(LOC_EXAMPLE_HEADER, true);
     }
 }
