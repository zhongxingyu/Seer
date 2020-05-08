 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.splash.job;
 
 import org.amanzi.neo.core.database.nodes.CellID;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.splash.swing.Cell;
 import org.amanzi.splash.swing.SplashTableModel;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.amanzi.splash.utilities.NeoSplashUtil;
 import org.neo4j.api.core.Transaction;
 
 import com.eteks.openjeks.format.CellFormat;
 
 /**
  * The task that interprets new formula of Cell
  *
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class InterpretTask implements SplashJobTask {
     
 	//table model
     private SplashTableModel model;
 
     //row of Cell
     private int row;
     
     //column of Cell
     private int column;
     
     //formula for interpreting
     private String formula;
     
     //resulted Cell
     private Cell resultCell;
 
     /**
      * Construcotr.
      * 
      * @param model table model
      * @param row row of Cell
      * @param column column of Cell
      * @param formula formula to interpret
      */
     public InterpretTask(SplashTableModel model, int row, int column, String formula) {
         this.model = model;
         this.row = row;
         this.column = column;
         this.formula = formula;
     }
     
     @Override
     public SplashJobTaskResult execute() {
     	//create a real Transaction in this thread    	
         Transaction transaction = NeoServiceProvider.getProvider().getService().beginTx();
         
         try {
         	//was copied from SplashTableMode.interpret
             String cellID = new CellID(row, column).getFullID();
             NeoSplashUtil.logn("<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>");
             NeoSplashUtil.logn("Start interpreting a cell...");
             NeoSplashUtil.logn("CellID = " + cellID);
 
             resultCell = model.getCellByID(cellID);
 
             if (resultCell == null) {
                 NeoSplashUtil.logn("WARNING: se = null");
                 resultCell = new Cell(row, column, Cell.DEFAULT_VALUE, Cell.DEFAULT_DEFINITION, new CellFormat());
             }
             Object s1;
             try {
                 s1 = model.interpret_erb(cellID, formula);
             } catch (Exception e) {
                 s1 = e.getLocalizedMessage();
                SplashPlugin.error(null, e);
             }
 
             NeoSplashUtil.logn("Setting cell definition: " + formula);
             resultCell.setDefinition(formula);
 
             NeoSplashUtil.logn("Setting cell value:" + (String) s1);
         
             resultCell.setValue(s1.toString());        
 
             model.setValueAt(resultCell, row, column, formula);
             
             transaction.success();
         }
         catch (Exception e) {
         	//if there was exception than changes will be rollbacked
             SplashPlugin.error(null, e);
         	transaction.failure();
         }
         finally {            
             transaction.finish();
         }
         
         return SplashJobTaskResult.CONTINUE;
     }
     
     /**
      * Returns cell with calculated value 
      * 
      * @return Cell with result
      */
     public Cell getResultCell() {
         return resultCell;
     }
 }
