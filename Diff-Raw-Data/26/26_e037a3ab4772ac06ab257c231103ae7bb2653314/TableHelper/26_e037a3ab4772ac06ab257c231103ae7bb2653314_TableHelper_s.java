 package uk.org.smithfamily.mslogger.dialog;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.ecuDef.OutputChannel;
 import uk.org.smithfamily.mslogger.ecuDef.TableEditor;
 import android.content.Context;
 import android.graphics.Color;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.text.method.DigitsKeyListener;
 import android.util.FloatMath;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 /**
  *
  */
 public class TableHelper
 {
     private TableEditor table;
     private Context context;
     
     private int tableNbX = 0;
     private int tableNbY = 0;
     
     // An array of 5 colors:  (blue, cyan, green, yellow, red) using {R,G,B} for each
    private final float COLORS[][] = {{0, 0, 255}, {0, 255, 255}, {0, 255, 0}, {255, 255, 0}, {255, 0, 0}};
    private final int NUM_COLORS = 5;
     
     private float currentTableMin = 0;
     private float currentTableMax = 0;
     
     private boolean isModified = false;
     
     private LinearLayout containerLayout;
     private TextView xBinsLabel;
     private TextView yBinsLabel;
     private TextView tableLabel;
     private TableLayout tableLayout;
     
     private boolean isTableDialog;
     
     /**
      * Construtor that trigger the build of the table
      * 
      * @param context
      * @param table
      * @param isTableDialog
      */
     public TableHelper(Context context, TableEditor table, boolean isTableDialog)
     {
         this.table = table;
         this.context = context;
         this.isTableDialog = isTableDialog;
         
         buildLayout();
         drawTable();
     }
     
     /**
      * Get the table editor associated with the table
      * 
      * @return
      */
     public TableEditor getTableEditor()
     {
         return this.table;
     }
     
     /**
      * Build the layout for the table
      */
     private void buildLayout()
     {        
         LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);       
         LinearLayout containerLayout = (LinearLayout) inflater.inflate(R.layout.table, null);
         LinearLayout.LayoutParams containerLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
         
         containerLayout.setBaselineAligned(false);
         containerLayout.setOrientation(LinearLayout.VERTICAL);
         containerLayout.setWeightSum(1.0f);
         containerLayout.setLayoutParams(containerLayoutParams);
         
         this.containerLayout = containerLayout;
         this.xBinsLabel = (TextView) containerLayout.findViewById(R.id.xBinsLabel);
         this.yBinsLabel = (TextView) containerLayout.findViewById(R.id.yBinsLabel);
         this.tableLabel = (TextView) containerLayout.findViewById(R.id.tableLabel);
         this.tableLayout = (TableLayout) containerLayout.findViewById(R.id.tableLayout);
         
         // If the generated table is a table dialog
         if (isTableDialog)
         {
             tableLabel.setVisibility(View.GONE);
         }
     }
     
     /**
      * Return the container layout of the 2D table
      */
     public LinearLayout getLayout()
     {
         return containerLayout;
     }
     
     /**
      * @return true if the table has been modified, false otherwise
      */
     public boolean isModified()
     {
         return isModified;
     }
     
     /**
      * Helper function that take a string as input and add a \n after each letter to use in text view so the text is vertical
      * 
      * @param text String to convert
      * @return Converted string
      */
     private String convertToVerticalTextView(String text)
     {
        String output = "";
         
         for (int i = 0; i < text.length(); i++)
         {
            output += text.substring(i, i + 1) + "\n";
         }
         
        return output;
     }
     
     /**
      * Create all the headers and cells to generate the 2D table into the main table layout
      */
     private void drawTable()
     {
         this.tableNbX = table.getzBins().length;
         this.tableNbY = table.getzBins()[0].length;
         
         // X and Y axis labels
         Megasquirt ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         
         OutputChannel xOutputChannel = ecu.getOutputChannelByName(table.getxOutputChannel());
         OutputChannel yOutputChannel = ecu.getOutputChannelByName(table.getyOutputChannel());
         
         String labelText = table.getxOutputChannel();
         if (xOutputChannel != null)
         {
             labelText += " (" + xOutputChannel.getUnits() + ")";
         }
         xBinsLabel.setText(labelText);
         
         labelText = table.getyOutputChannel();
         if (yOutputChannel != null)
         {
             labelText += " " + yOutputChannel.getUnits();
         }
         yBinsLabel.setLineSpacing(0, 0.9f);
         yBinsLabel.setText(convertToVerticalTextView(labelText));
         
         LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         
         LayoutParams rowHeaderLayout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         rowHeaderLayout.setMargins(0, 0, 10, 0);
         
         // Add label on top of table if this table won't be used as a dialog with a single table in it,
         // otherwise it looks weird.
         if (!isTableDialog)
         {
             tableLabel.setText(table.getLabel());
         }
         
         TableRow tableRow;     
         
         double xBins[] = table.getxBins();
         double yBins[] = table.getyBins();
         
         double zBins[][] = table.getzBins();
         
         int xDigits = 0;
         if (xOutputChannel != null)
         {
             xDigits = getDigitsFromScale(xOutputChannel.getScale());
         }
         
         int yDigits = 0;
         if (yOutputChannel != null)
         {
             yDigits = getDigitsFromScale(yOutputChannel.getScale());
         }
         
         for (int y = 1; y <= tableNbY; y++)
         {
             tableRow = new TableRow(context);
             tableRow.setLayoutParams(lp);
             
             // Row header
             TextView rowHeader = new TextView(context);
             
             String headerLabel = "";
             if (yDigits == 0)
             {
                 headerLabel = String.valueOf((int) yBins[tableNbY - y]);
             }
             else
             {
                 headerLabel = String.valueOf(ecu.roundDouble(yBins[tableNbY - y], yDigits));
             }
             
             rowHeader.setText(headerLabel);
             rowHeader.setLayoutParams(rowHeaderLayout);
             tableRow.addView(rowHeader);
             
             for (int x = 1; x <= tableNbX; x++)
             {
                 EditText cell = new EditText(context);
                 cell.setText(Double.toString(zBins[x - 1][tableNbY - y]));
                 cell.setId(getCellId(x,y));
                 cell.setLayoutParams(lp);
                 cell.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                 cell.setSingleLine(true);
                 cell.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                 cell.setPadding(7, 4, 7, 4);
                 cell.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                 
                 // Refresh background color when one edit text lost focus
                 cell.addTextChangedListener(new CustomTextWatcher(cell));
                 
                 // Add EditText to row
                 tableRow.addView(cell);
             }
             
             tableLayout.addView(tableRow,lp);
         }
         
         // Column headers
         tableRow = new TableRow(context);
         tableRow.setLayoutParams(lp);
         
         for (int x = 1; x <= tableNbX + 1; x++)
         {
             TextView columnHeader = new TextView(context);
             
             if (x > 1)
             {
                 String headerLabel = "";
                 if (xDigits == 0)
                 {
                     headerLabel = String.valueOf((int) xBins[x - 2]);
                 }
                 else
                 {
                     headerLabel = String.valueOf(ecu.roundDouble(xBins[x - 2], xDigits));
                 }
                 
                 columnHeader.setText(headerLabel);
                 columnHeader.setGravity(Gravity.CENTER);
             }          
                         
             columnHeader.setLayoutParams(lp);
             
             tableRow.addView(columnHeader);   
         }       
         
         tableLayout.addView(tableRow,lp);
         
         refreshCellsBackgroundColor(null);
     }
     
     /**
      * The INI define digits as scale in the following annoying format: 1.0, 0.1, etc. We need to extract how many digits that represents
      * 
      * @param scale
      * @return
      */
     private int getDigitsFromScale(double scale)
     {
         String sScale = Double.toString(scale);
         
         int digits = 0, separatorPosition = sScale.indexOf(".");
         
         if (separatorPosition > -1)
         {
             int i = 0;
             for (i = separatorPosition + 1; i < sScale.length(); i++)
             {
                 if (sScale.substring(i, i + 1).equals("0"))
                 {
                     break;
                 }
             }
             
             digits = i - 1 - separatorPosition;
         }
         
         return digits;
     }
     
     /**
      * Private class that implements TextWatcher. This class makes allow us to find which EditText fired the event way less painful
      */
     private class CustomTextWatcher implements TextWatcher
     {
         private EditText mEditText;
 
         public CustomTextWatcher(EditText e)
         {
             mEditText = e;
         }
         
         public void afterTextChanged(Editable s)
         {
             isModified = true;
             refreshCellsBackgroundColor(mEditText);
         }
         
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
         public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
      * 
      * @param cell The cell that triggered the refresh, if any
      */
     private void refreshCellsBackgroundColor(EditText cell)
     {
         EditText firstCell = (EditText) tableLayout.findViewById(getCellId(1,1));
         
         float min = Float.parseFloat(firstCell.getText().toString());
         float max = min;
         
         // Find min/max
         for (int y = 1; y <= tableNbY; y++)
         {
             for (int x = 1; x <= tableNbX; x++)
             {
                 EditText currentCell = (EditText) tableLayout.findViewById(getCellId(x,y));
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
         
         // Min and/or maximum value of the table changed, we need redraw all background colors
         if (cell == null || currentTableMin != min || currentTableMax != max)
         {
             // Change background colors
             for (int y = 1; y <= tableNbY; y++)
             {
                 for (int x = 1; x <= tableNbX; x++)
                 {
                     EditText currentCell = (EditText) tableLayout.findViewById(getCellId(x,y));
                     
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
         // Just redraw the specific cell that the value changed
         else
         {
             float currentValue = 0;
             try
             {
                 currentValue = Float.parseFloat(cell.getText().toString());
             }
             catch (NumberFormatException e) {}
             
             float currentPercent = (currentValue - min) / (max - min);
             cell.setBackgroundColor(getHeatMapColor(currentPercent));
         }
     }
     
     /**
      * Used in table to generate background color based on the cell value
      * 
      * @param value Between 0 and 1 representing the percent (based on the min/max) of the color to return 
      * @return An int of the color
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
      * Set all cells of table to enabled or disabled
      * 
      * @param isPanelEnabled
      */
     public void refreshFieldsVisibility(boolean isPanelEnabled)
     {
         for (int i = 0; i < getCellsCount(); i++)
         {
             EditText cell = getCellAt(i);
             cell.setEnabled(isPanelEnabled);
         }
     }
     
     /**
      * @param index Index of the cell
      * @return The cell (EditText) associated with the index
      */
     private EditText getCellAt(int index)
     {
         int x = index % tableNbX + 1;
         int y = index / tableNbX + 1;
         
         return (EditText) tableLayout.findViewById(getCellId(x,y));
     }
     
     /**
      * @return The number of cells for the table
      */
     private int getCellsCount()
     {
         return tableNbX * tableNbY;
     }
 }
