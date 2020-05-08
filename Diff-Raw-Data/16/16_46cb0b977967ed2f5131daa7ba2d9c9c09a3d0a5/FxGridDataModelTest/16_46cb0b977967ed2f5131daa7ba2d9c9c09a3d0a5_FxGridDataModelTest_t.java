 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded.jsf;
 
 import com.flexive.faces.model.FxGridDataModel;
 import com.flexive.faces.model.FxResultSetDataModel;
 import com.flexive.shared.exceptions.FxAccountInUseException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.exceptions.FxLogoutFailedException;
 import com.flexive.shared.search.FxResultSet;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.testng.Assert;
 
 import javax.faces.model.DataModel;
 
 /**
  * Basic FxGridDataModel tests.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @Test(groups = "jsf")
 public class FxGridDataModelTest extends AbstractSqlQueryTest {
     @Override
     @BeforeClass
     public void beforeClass() throws FxLoginFailedException, FxAccountInUseException, FxApplicationException {
         super.beforeClass();
     }
 
     @Override
     @AfterClass
     public void afterClass() throws FxLogoutFailedException, FxApplicationException {
         super.afterClass();
     }
 
     @Test
     public void basicGridResult() throws FxApplicationException {
         FxResultSet result = getRows(0, 25);
         FxResultSetDataModel resultModel = new FxResultSetDataModel(result);
         FxGridDataModel gridModel = new FxGridDataModel(resultModel, 5);    // 5x5 grid
         Assert.assertEquals(gridModel.getRowCount(), 5);
         for (int i = 0; i < gridModel.getRowCount(); i++) {
             gridModel.setRowIndex(i);
             Assert.assertTrue(gridModel.isRowAvailable());
             Object[] row = (Object[]) gridModel.getRowData();
             Assert.assertEquals(row.length, 5);
             for (int j = 0; j < row.length; j++) {
                 final Object[] refRow = result.getRows().get(i * 5 + j);
                 Assert.assertTrue(row[j] != null && row[j] == refRow, "Expected grid column to be equal to reference row.");
             }
         }
         gridModel.setRowIndex(6);
         Assert.assertFalse(gridModel.isRowAvailable());
     }
 
     @Test
     public void emptyGridResult() throws FxApplicationException {
         FxResultSet result = getRows(0, 1);
         result.getRows().clear();
         FxResultSetDataModel resultModel = new FxResultSetDataModel(result);
         FxGridDataModel gridModel = new FxGridDataModel(resultModel, 5);    // 5x5 grid
         assertAvailableRows(gridModel, 0, 0);
     }
 
     @Test
     public void oneHalfFullRowResult() throws FxApplicationException {
         FxResultSet result = getRows(0, 3);
         FxResultSetDataModel resultModel = new FxResultSetDataModel(result);
         FxGridDataModel gridModel = new FxGridDataModel(resultModel, 5);    // 5x5 grid
         assertAvailableRows(gridModel, 1, 0);
         gridModel.setRowIndex(0);
         Assert.assertTrue(gridModel.isRowAvailable());
         Object[] row = (Object[]) gridModel.getRowData();
         Assert.assertTrue(gridModel.isRowAvailable());  // assert that the backing datamodel index has not changed
         Assert.assertEquals(row.length, 5);
         for (int i = 0; i < 3; i++) {
             Assert.assertTrue(row[i] != null && row[i] == result.getRows().get(i));
         }
         Assert.assertNull(row[3]);
         Assert.assertNull(row[4]);
     }
 
     @Test
     public void lazyGridResult() throws FxApplicationException {
         testLazyGridResult(0);
         testLazyGridResult(10);
     }
 
     private void testLazyGridResult(int startRow) throws FxApplicationException {
         FxResultSetDataModel model = new FxResultSetDataModel(getSelectAllBuilder(),
                 0, 10);
         FxGridDataModel gridModel = new FxGridDataModel(model, 5);    // 5x2 grid (with lazy loading)
         assertAvailableRows(gridModel, 2, startRow / 5);
     }
 
     private void assertAvailableRows(DataModel model, int rows, int startRow) {
         for (int i = startRow; i < rows; i++) {
             model.setRowIndex(startRow + i);
             Assert.assertTrue(model.isRowAvailable(), "Row " + i + " should be available in " + model);
         }
         //model.setRowIndex(startRow + rows);
         //Assert.assertTrue(!model.isRowAvailable(), "No more than " + rows + " rows should be available in " + model);
     }
 }
