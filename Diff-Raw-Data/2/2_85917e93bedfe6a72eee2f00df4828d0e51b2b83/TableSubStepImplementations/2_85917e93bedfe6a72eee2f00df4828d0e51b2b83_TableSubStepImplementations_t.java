 /*
  *	Copyright Technophobia Ltd 2012
  *
  *   This file is part of Substeps.
  *
  *    Substeps is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    Substeps is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public License
  *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.technophobia.webdriver.substeps.impl;
 
 import static com.technophobia.webdriver.substeps.runner.DefaultExecutionSetupTearDown.getThreadLocalWebDriverContext;
 import static org.hamcrest.CoreMatchers.is;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import com.google.common.base.Supplier;
 import com.technophobia.substeps.model.SubSteps.Step;
 import com.technophobia.substeps.model.SubSteps.StepImplementations;
 import com.technophobia.substeps.model.SubSteps.StepParameter;
 import com.technophobia.substeps.model.parameter.IntegerConverter;
 import com.technophobia.webdriver.substeps.runner.DefaultExecutionSetupTearDown;
 import com.technophobia.webdriver.util.WebDriverContext;
 
 @StepImplementations(requiredInitialisationClasses = DefaultExecutionSetupTearDown.class)
 public class TableSubStepImplementations extends
         AbstractWebDriverSubStepImplementations {
 
     public TableSubStepImplementations() {
         super();
     }
 
 
     public TableSubStepImplementations(
             final Supplier<WebDriverContext> webDriverContextSupplier) {
         super(webDriverContextSupplier);
     }
 
 
     /*
      * TODO
      * 
      * a method that takes a table with column names - stash in the context for
      * reference and look up the index could combine this - not sure what
      * purpose the column names serves... see nhs proxy user feature (low) and
      * Then the current contracts list will contain the following
      * ContractSearchSteps.thenTheContractsListContains
      * 
      * Assert table contains row - a method that takes a map of values, use the
      * stash for the index
      * 
      * find a table - findby id etc..
      * 
      * assert row x has values <array> - can't pass a single row of values..
      * 
      * combinations with passing in the table id
      */
 
     /**
      * Locates the table body row, assuming that the table has already been
      * located Row 1 is the first
      * <tr>
      * beneath a <tbody>
      * 
      * @example FindTableBodyRow row 3
      * @section Table
      * 
      * @param row
      *            1 based row index
      */
     @Step("FindTableBodyRow row ([^\"]*)")
     public WebElement findTableBodyRow(
             @StepParameter(converter = IntegerConverter.class) final Integer row) {
 
         // assumes current element is already set
         final WebElement currentElem = getThreadLocalWebDriverContext()
                 .getCurrentElement();
 
         AssertionWebDriverSubStepImplementations.assertElementIs(currentElem,
                 "table");
 
         final WebElement tbody = getResultsTableBodyElement(currentElem);
 
         final WebElement rowElement = getTableRow(tbody, row);
 
         Assert.assertNotNull("expecting a table row element", rowElement);
         getThreadLocalWebDriverContext().setCurrentElement(rowElement);
 
         return rowElement;
     }
 
 
     // TODO can't implement this until worked out how to pass single rows
     // through
 
     // @Step("AssertTable row ([^\"]*) contains values")
     // public void assertTableRowContainsValues(@StepParameter(converter =
     // IntegerConverter.class)final Integer row,
     // final List<Map<String, String>> expectedResults){
     //
     // Assert.assertNotNull("expecting a table of results to be specified",
     // expectedResults);
     // Assert.assertThat("only expecting 1 row in table data",
     // expectedResults.size(), is(1));
     // //
     //
     // final WebElement tableRow = findTableBodyRow(row);
     //
     //
     // final List<WebElement> columnElements =
     // tableRow.findElements(By.tagName("td"));
     //
     // if (columnElements != null) {
     //
     // if (table == null) {
     // table = new ArrayList<Map<String, String>>();
     // }
     // final Map<String, String> rowMap = new HashMap<String, String>();
     // table.add(rowMap);
     //
     // for (int i = 0; i < columnElements.size(); i++) {
     // rowMap.put(columnHeadings[i], columnElements.get(i).getText());
     // }
     // }
     // }
 
     /**
      * Check that a table cell contains the specified text using a 1 based
      * index. Row 0 is the first
      * <tr>
      * beneath a <tbody>
      * 
      * @example AssertTableValue column 2, row 3 contains text "Hello Bob"
      * @section Table
      * 
      * @param column
      *            1 based column index
      * @param row
      *            1 based row index
      * @param text
      *            the expected text
      */
     @Step("AssertTableValue column ([^\"]*), row ([^\"]*) contains text \"([^\"]*)\"")
     public void assertTableValue(
             @StepParameter(converter = IntegerConverter.class) final Integer column,
             @StepParameter(converter = IntegerConverter.class) final Integer row,
             final String text) {
 
         // assumes current element is already set
         final WebElement currentElem = getThreadLocalWebDriverContext()
                 .getCurrentElement();
 
         AssertionWebDriverSubStepImplementations.assertElementIs(currentElem,
                 "table");
 
         final WebElement tbody = getResultsTableBodyElement(currentElem);
 
         final String cellText = getValueInResultsTable(tbody,
                 column.intValue(), row.intValue());
         Assert.assertNotNull("expecting some cell text", cellText);
 
         Assert.assertTrue("expecting cell text to contain: " + text
                 + " actual: " + cellText, cellText.contains(text));
 
     }
 
 
     private WebElement getResultsTableBodyElement(final WebElement tableElement) {
 
         Assert.assertNotNull("expecting a tableElement", tableElement);
 
         /* 
          * Note that we are expecting that the table has a "tbody" element, which is not in fact mandatory.
          * This will find a "tbody" child of tableElement, there should be only one.
          * 
          * @TODO. Should we return tableElement itself if it has no tbody child?
          */
         final WebElement tbodyElem = tableElement.findElement(By
                 .xpath("./tbody"));
 
        Assert.assertNotNull("expecting tbody elem", tbodyElem);
 
        return tbodyElem;
     }
 
 
     private WebElement getTableRow(final WebElement tbody, final int row) {
         final List<WebElement> rowElements = tbody.findElements(By
                 .tagName("tr"));
         Assert.assertNotNull("expecting th row elems", rowElements);
 
         Assert.assertTrue("expecting more than " + row + " row in the table",
                 rowElements.size() >= row);
 
         // row parameter will be 1 based, but we need to discard the header row
         final WebElement rowElement = rowElements.get(row - 1);
         Assert.assertNotNull("expecting a tr at tbody idx: " + (row - 1),
                 rowElement);
         return rowElement;
     }
 
 
     public String getValueInResultsTable(final WebElement tbody, final int col,
             final int row) {
 
         final WebElement rowElement = getTableRow(tbody, row);
 
         final List<WebElement> columnElements = rowElement.findElements(By
                 .tagName("td"));
         Assert.assertNotNull("expecting columnElements", columnElements);
 
         Assert.assertTrue("expecting more than " + col
                 + " columns in the table, got: " + columnElements.size(),
                 columnElements.size() >= col);
         final WebElement tdElem = columnElements.get(col - 1);
 
         Assert.assertNotNull("expecting a td at column: " + col, tdElem);
 
         return tdElem.getText();
     }
 
 }
