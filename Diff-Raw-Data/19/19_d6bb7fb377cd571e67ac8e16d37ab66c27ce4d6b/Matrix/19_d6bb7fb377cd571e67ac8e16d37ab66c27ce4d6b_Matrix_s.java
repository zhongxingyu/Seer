 package com.radianapps.linalg.views;
 
 import android.content.Context;
import android.graphics.Color;
 import android.util.AttributeSet;
 import android.view.ViewGroup;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import com.radianapps.linalg.MatrixController;
 import com.radianapps.linalg.Position;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tushar
  * Date: 9/7/13
  * Time: 10:34 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Matrix extends TableLayout {
     private int rows = 0, cols = 0;
     private MatrixController controller;
 
     private Position focus;
 
     private final TableRow.LayoutParams rowLp =
             new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
 
     public Matrix(Context context) {
         this(context, null);
     }
 
     public Matrix(Context context, AttributeSet attributeSet) {
         super(context, attributeSet);
 
         LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
         setLayoutParams(lp);
     }
 
     public void resize(int newRows, int newCols) {
         if (rows < newRows) {
             for (int i = rows; i < newRows; ++i) {
                 ++rows;
                 addView(new TableRow(getContext()), rowLp);
             }
         } else {
             for (int i = rows - newRows; i > 0; --i) {
                 --rows;
                 removeViewAt(rows);
             }
         }
 
         for (int i = 0; i < rows; ++i) {
             TableRow tableRow = (TableRow) getChildAt(i);
             int oldCols = tableRow.getChildCount();
             if (oldCols < newCols) {
                 for (int j = oldCols; j < newCols; ++j) {
                     ++oldCols;
                     TextView cell = new TextView(getContext());
                     cell.setText(Integer.toString(oldCols));
                     tableRow.addView(cell);
                 }
             } else {
                 for (int j = oldCols - newCols; j > 0; --j) {
                     --oldCols;
                     tableRow.removeViewAt(oldCols);
                 }
             }
         }
         cols = newCols;
     }
 
     public void setFocus(Position focus) {
        if (this.focus != null) {
             unfocusOnCell(this.focus);
         }
         if (focus != null) {
             focusOnCell(focus);
         }
 
        this.focus = focus;
     }
 
     public void setCellAt(int row, int col, int value) {
         ((TextView)((TableRow)getChildAt(row)).getChildAt(col)).setText(String.valueOf(value));
     }
 
     private void focusOnCell(Position cellToFocus) {
        // TODO: Make focus colour variable
        ((TableRow)getChildAt(cellToFocus.x)).getChildAt(cellToFocus.y).setBackgroundColor(Color.YELLOW);
     }
 
     private void unfocusOnCell(Position cellToUnFocus) {
        // TODO: Make background colour variable
        ((TableRow)getChildAt(cellToUnFocus.x)).getChildAt(cellToUnFocus.y).setBackgroundColor(Color.TRANSPARENT);
     }
 
     public int rowCount() {
         return rows;
     }
 
     public int colCount() {
         return cols;
     }
 
     public void registerController(MatrixController controller) {
         this.controller = controller;
     }
 
     public void unregisterController() {
         this.controller = null;
     }
 
 }
