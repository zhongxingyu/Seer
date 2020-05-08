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
 package org.jboss.richfaces.integrationTest.extendedDataTable;
 
 import org.jboss.richfaces.integrationTest.AbstractDataIterationTestCase;
 import org.jboss.test.selenium.waiting.Condition;
 import org.jboss.test.selenium.waiting.Wait;
 import org.testng.annotations.BeforeMethod;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class AbstractExtendedDataTableTestCase extends AbstractDataIterationTestCase {
 
 	protected final String LOC_TH_STATE = getLoc("TH_STATE");
 	protected final String LOC_TH_CAPITAL = getLoc("TH_CAPITAL");
 	protected final String LOC_TH_TIME_ZONE = getLoc("TH_TIME_ZONE");
 	protected final String LOC_TH_FLAG = getLoc("TH_FLAG");
 	protected final String LOC_TH_RELATIVE = getLoc("TH_RELATIVE");
 	protected final String LOC_TD_PREFORMATTED = getLoc("TD_PREFORMATTED");
 	private final String LOC_TABLE_EXTENDED = getLoc("TABLE_EXTENDED");
 	private final String LOC_DIV_SPLASH_SCREEN = getLoc("DIV_SPLASH_SCREEN");
 	private final String LOC_INPUT_COLUMN_FILTER = getLoc("INPUT_COLUMN_FILTER");
 
 	private final String MSG_OPTION_SELECTION_MODE_PREFORMATTED = getMsg("OPTION_SELECTION_MODE_PREFORMATTED");
 
 	@BeforeMethod
 	protected void loadPage() {
 		openComponent("Extended Data Table");
 		openTab("Usage");
 		scrollIntoView(LOC_TABLE_EXTENDED, true);
 		selenium.allowNativeXpath("true");
 	}
 
 	/**
 	 * Wait for splash screen indicating request of table rerendering disappears
 	 */
 	protected void waitForSplash() {
		waitModelUpdate.dontFail().interval(5).until(new Condition() {
 			public boolean isTrue() {
 				return selenium.isElementPresent(LOC_DIV_SPLASH_SCREEN);
 			}
 		});
 		waitModelUpdate.until(new Condition() {
 			public boolean isTrue() {
 				return !selenium.isElementPresent(LOC_DIV_SPLASH_SCREEN);
 			}
 		});
 	}
 
 	/**
 	 * Selects given mode of selection (resp. sorting) and wait for table
 	 * rerenders
 	 * 
 	 * @param selectLocator
 	 *            locator of select input
 	 * @param selectionMode
 	 *            value of selection mode, which should be selected
 	 */
 	protected void selectMode(String selectLocator, String selectionMode) {
 		if (!selectionMode.equals(selenium.getValue(selectLocator))) {
 			selenium.select(selectLocator, format(MSG_OPTION_SELECTION_MODE_PREFORMATTED, selectionMode));
 			waitForSplash();
 		}
 	}
 
 	/**
 	 * Get a actual position of column given by columnHeader (TH)
 	 * 
 	 * @param columnHeader
 	 *            the locator of given TH
 	 * @return the actual position (sequence) of column in table
 	 */
 	protected int getColumnIndex(String columnHeader) {
 		return 1 + selenium.getElementIndex(format(LOC_TH_RELATIVE, columnHeader)).intValue();
 	}
 
 	/**
 	 * Preformat column's rows for column given by columnHeader (TH)
 	 * 
 	 * Uses {@link getColumnIndex(String)}
 	 * 
 	 * @param columnHeader
 	 *            the locator of given TH
 	 * @return preformatted column rows' locators ready for use with format()
 	 *         method
 	 */
 	protected String preformatColumn(String columnHeader) {
 		int columnIndex = getColumnIndex(columnHeader);
 		String columnPreformatted = format(LOC_TD_PREFORMATTED, Integer.MAX_VALUE, columnIndex);
 		return columnPreformatted.replaceFirst(format("\\[{0,number}\\]", Integer.MAX_VALUE), "{0,choice,0#|1#[{0}]}");
 	}
 
 	/**
 	 * Preformat filtering input's locator for column given by its columnHeader
 	 * (TH)
 	 * 
 	 * @param columnHeader
 	 *            the locator of given TH
 	 * @return locator of filter input for given columnHeader
 	 */
 	protected String preformatFilterInput(String columnHeader) {
 		int columnIndex = getColumnIndex(columnHeader);
 		return format(LOC_INPUT_COLUMN_FILTER, columnIndex);
 	}
 }
