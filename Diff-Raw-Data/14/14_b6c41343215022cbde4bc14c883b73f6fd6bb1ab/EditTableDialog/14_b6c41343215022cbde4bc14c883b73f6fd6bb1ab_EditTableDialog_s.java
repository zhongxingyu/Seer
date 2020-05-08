 package uk.org.smithfamily.mslogger.dialog;
 
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.ecuDef.TableEditor;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.FloatMath;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.view.WindowManager;
import android.view.animation.AnimationUtils;
 import android.view.animation.RotateAnimation;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 /**
  *
  */
 public class EditTableDialog extends Dialog implements android.view.View.OnClickListener
 {    
     private TableEditor table;
     private int tableNbX = 0;
     private int tableNbY = 0;
     
     // An array of 5 colors:  (blue, cyan, green, yellow, red) using {R,G,B} for each
     final float COLORS[][] = {{0, 0, 255}, {0, 255, 255}, {0, 255, 0}, {255, 255, 0}, {255, 0, 0}};
     final int NUM_COLORS = 5;
     
     /**
      * 
      * @param context
      * @param table
      */
     public EditTableDialog(Context context, TableEditor table)
     {
         super(context);
         
         this.table = table;
     }
     
     /**
      * @param savedInstanceState
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.edittable);
      
         setTitle("Edit " + table.getLabel());
         
         drawTable();
     }
     
     /**
      * 
      */
     private void drawTable()
     {
         this.tableNbX = table.getzBins().length;
         this.tableNbY = table.getzBins()[0].length;
         
         TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
         
         LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                 
         LayoutParams rowHeaderLayout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         rowHeaderLayout.setMargins(0, 0, 10, 0);
         
         TableRow tableRow;     
         
         double xBins[] = table.getxBins();
         double yBins[] = table.getyBins();
         
         double zBins[][] = table.getzBins();
          
         for (int x = 1; x <= tableNbX; x++)
         {
             tableRow = new TableRow(getContext());
             tableRow.setLayoutParams(lp);
             
             // Row header
             TextView rowHeader = new TextView(getContext());
             rowHeader.setText(Double.toString(yBins[tableNbX - x]));
             rowHeader.setLayoutParams(rowHeaderLayout);
             tableRow.addView(rowHeader);
             
             for (int y = 1; y <= tableNbY; y++)
             {
                 EditText cell = new EditText(getContext());
                 cell.setText(Double.toString(zBins[y - 1][tableNbX - x]));
                 cell.setId(getCellId(x,y));
                 cell.setLayoutParams(lp);
                 cell.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                 cell.setSingleLine(true);
                 cell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                 cell.setPadding(8, 5, 8, 5);
                 
                 // Refresh background color when one edit text lost focus
                 cell.addTextChangedListener(new TextWatcher()
                 {
                     @Override
                     public void afterTextChanged(Editable s)
                     {
                         refreshCellsBackgroundColor();
                     }
 
                     @Override
                     public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
                     @Override
                     public void onTextChanged(CharSequence s, int start, int before, int count) {}                     
                 });
                 
                 // Add EditText to row
                 tableRow.addView(cell);
             }
             
             tableLayout.addView(tableRow,lp);
         }
         
         // Column headers
         tableRow = new TableRow(getContext());
         tableRow.setLayoutParams(lp);
         
         for (int x = 1; x <= tableNbX + 1; x++)
         {
             TextView columnHeader = new TextView(getContext());
             
             if (x > 1)
             {
                 columnHeader.setText(Double.toString(xBins[x - 2]));
                 columnHeader.setGravity(Gravity.CENTER);
             }          
                         
             columnHeader.setLayoutParams(lp);
             
             tableRow.addView(columnHeader);   
         }       
         
         tableLayout.addView(tableRow,lp);
                 
         final TextView xBinsLabel = (TextView) findViewById(R.id.xBinsLabel);
         xBinsLabel.setText(table.getxLabel());
         
         final TextView yBinsLabel = (TextView) findViewById(R.id.yBinsLabel);
         yBinsLabel.setText(table.getyLabel());
         
        // Apply animation on text view so it's vertical (90 degree rotation with 0ms duration animation)
        RotateAnimation ranim = (RotateAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.verticaltextview);
        ranim.setFillAfter(true);
        yBinsLabel.setAnimation(ranim);
         
         refreshCellsBackgroundColor();
         
         Button buttonBurn = (Button) findViewById(R.id.burn);
         buttonBurn.setOnClickListener(this);
         
         Button buttonCancel = (Button) findViewById(R.id.cancel);
         buttonCancel.setOnClickListener(this);
         
         // Hide keyboard
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     }
     
     /**
      * Generate on the fly ID based on the cell position
      * 
      * @param x
      * @param y
      * @return
      */
     private int getCellId(int x, int y)
     {
         return x + (y * 1000);
     }
 
     /**
      * This method goes over all the table cells and apply the right background color
      * so all the cells together looks like  a heat map
      */
     private void refreshCellsBackgroundColor()
     {
         EditText firstCell = (EditText) findViewById(getCellId(1,1));
         
         float min = Float.parseFloat(firstCell.getText().toString());
         float max = min;
         
         // Find min/max
         for (int y = 1; y <= tableNbX; y++)
         {
             for (int x = 1; x <= tableNbY; x++)
             {
                 EditText currentCell = (EditText) findViewById(getCellId(x,y));
                 float currentValue = 0;
                 
                 try
                 {
                     currentValue = Float.parseFloat(currentCell.getText().toString());
                 }
                 catch (NumberFormatException e) {}
 
                 if (min > currentValue)
                 {
                     min = currentValue;
                 }
                 
                 if (max < currentValue)
                 {
                     max = currentValue;
                 }
             }
         }
         
         // Change background colors
         for (int y = 1; y <= tableNbX; y++)
         {
             for (int x = 1; x <= tableNbY; x++)
             {
                 EditText currentCell = (EditText) findViewById(getCellId(x,y));
                 
                 float currentValue = 0;
                 try
                 {
                     currentValue = Float.parseFloat(currentCell.getText().toString());
                 }
                 catch (NumberFormatException e) {}
 
                 float currentPercent = (currentValue - min) / (max - min);
                 
                 currentCell.setBackgroundColor(getHeatMapColor(currentPercent));
             }
         }
     }
     
     /**
      * 
      * @param value
      * @return
      */
     private int getHeatMapColor(float value)
     {
         int idx1, idx2;
         
         // Fraction between "idx1" and "idx2" where our value is
         float fractBetween = 0;
        
         if (value <= 0)
         {
             idx1 = idx2 = 0;
         } 
         else if (value >= 1)
         {
             idx1 = idx2 = NUM_COLORS - 1;
         }
         else
         {
           value = value * (NUM_COLORS - 1);
           idx1  = (int) FloatMath.floor(value);
           idx2  = idx1 + 1;
           
           // Distance between the two indexes (0-1)
           fractBetween = value - (float) idx1;
         }
         
         int red = (int) ((COLORS[idx2][0] - COLORS[idx1][0]) * fractBetween + COLORS[idx1][0]);
         int green = (int) ((COLORS[idx2][1] - COLORS[idx1][1]) * fractBetween + COLORS[idx1][1]);
         int blue = (int) ((COLORS[idx2][2] - COLORS[idx1][2]) * fractBetween + COLORS[idx1][2]);
         
         return Color.rgb(red, green, blue);
     }
     
     /**
      * Triggered when one of the two bottoms button are clicked ("Burn" and "Cancel")
      * 
      * @param v The view that was clicked on
      */
     @Override
     public void onClick(View v)
     {
         int which = v.getId();
         
         if (which == R.id.burn)
         {
             
         }
         else if (which == R.id.cancel)
         {
             cancel();
         }
     }
 }
