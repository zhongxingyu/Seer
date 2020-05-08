 package com.radianapps.linalg.views;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.ViewGroup;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import com.radianapps.linalg.MatrixController;
 import com.radianapps.linalg.Position;
import com.radianapps.linalg.R;
 
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
                    cell.setBackgroundResource(R.drawable.matrix_cell);
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
        if (this.focus != null && this.focus.inLimits(rows - 1, cols - 1)) {
             unfocusOnCell(this.focus);
         }
         if (focus != null) {
             focusOnCell(focus);
         }
 
        this.focus = new Position(focus);
     }
 
     public void setCellAt(int row, int col, int value) {
         ((TextView)((TableRow)getChildAt(row)).getChildAt(col)).setText(String.valueOf(value));
     }
 
     private void focusOnCell(Position cellToFocus) {
        ((TableRow)getChildAt(cellToFocus.x)).getChildAt(cellToFocus.y).setSelected(true);
     }
 
     private void unfocusOnCell(Position cellToUnFocus) {
        ((TableRow)getChildAt(cellToUnFocus.x)).getChildAt(cellToUnFocus.y).setSelected(false);
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
