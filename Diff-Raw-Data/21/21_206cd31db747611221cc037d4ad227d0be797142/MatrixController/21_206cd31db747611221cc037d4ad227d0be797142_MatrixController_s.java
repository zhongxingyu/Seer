 package com.radianapps.linalg;
 
 import com.radianapps.linalg.views.Matrix;
 import com.radianapps.linalg.views.MatrixIME;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tushar
  * Date: 9/7/13
  * Time: 10:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MatrixController {
 
     private Position focus = new Position();
 
     private List<List<Integer>> data;
     private List<Matrix> views;
 
     private MatrixIME ime;
 
     public MatrixController() {
         data = new ArrayList<List<Integer>>();
         views = new ArrayList<Matrix>();
     }
 
     public void resizeMatrix(int newRows, int newCols) {
         int oldRows = numRows();
 
        if (oldRows < newRows) {
             for (int i = oldRows; i < newRows; ++i) {
                 data.add(new ArrayList<Integer>());
             }
         } else {
            data.subList(oldRows - newRows, oldRows).clear();
         }
 
         for (List<Integer> entries : data) {
             int oldCols = entries.size();
            if (oldCols < newCols) {
                 for (int j = entries.size(); j < newCols; ++j) {
                     entries.add(0);
                 }
             } else {
                entries.subList(oldCols - newCols, oldCols).clear();
             }
         }
 
        boolean focusChanged = !focus.inLimits(newRows, newCols);
         if (focusChanged) {
             focus.x = 0;
             focus.y = 0;
             focus.setValid(true);
         }
 
         for (Matrix view : views) {
             view.resize(newRows, newCols);
             if (focusChanged) {
                 view.setFocus(focus);
             }
         }
     }
 
     public void registerIME(MatrixIME ime) {
         this.ime = ime;
         if (ime != null) {
             ime.registerController(this);
         }
     }
 
     public void navInput(MatrixIME.Nav nav) {
         int numCols = numCols();
         int numRows = numRows();
 
         switch (nav) {
             case UP:
                 if (focus.y < numCols) {
                     ++focus.y;
                 } else {
                     focus.y = 0;
                 }
                 break;
             case DOWN:
                 if (focus.y > 0) {
                     --focus.y;
                 } else {
                     focus.y = numCols;
                 }
                 break;
             case RIGHT:
                 if (focus.x < numCols) {
                     ++focus.x;
                 } else {
                     focus.x = 0;
                 }
                 break;
             case LEFT:
                 if (focus.x > 0) {
                     --focus.x;
                 } else {
                     focus.x = numRows;
                 }
                 break;
         }
 
         for (Matrix view : views) {
             view.setFocus(focus);
         }
     }
 
     private int numCols() {
         return (!data.isEmpty()) ? data.get(0).size() : 0;
     }
 
     private int numRows() {
         return data.size();
     }
 
     public void registerView(Matrix matrix) {
         views.add(matrix);
 
         matrix.resize(numRows(), numCols());
         matrix.registerController(this);
 
         for (int rowIndex = 0; rowIndex < data.size(); ++rowIndex) {
             List<Integer> row = data.get(rowIndex);
             for (int colIndex = 0; colIndex < row.size(); ++colIndex) {
                 matrix.setCellAt(rowIndex, colIndex, row.get(colIndex));
             }
         }
 
        if (focus.inLimits(numRows(), numCols())) {
             matrix.setFocus(focus);
         }
     }
 
     public void unregisterView(Matrix matrix) {
         views.remove(matrix);
         matrix.unregisterController();
     }
 
 }
