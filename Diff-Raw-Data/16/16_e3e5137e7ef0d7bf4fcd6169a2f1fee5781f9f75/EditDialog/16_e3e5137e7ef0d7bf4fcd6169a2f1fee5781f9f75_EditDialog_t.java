 package uk.org.smithfamily.mslogger.dialog;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.ecuDef.Constant;
 import uk.org.smithfamily.mslogger.ecuDef.DialogField;
 import uk.org.smithfamily.mslogger.ecuDef.DialogPanel;
 import uk.org.smithfamily.mslogger.ecuDef.MSDialog;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 public class EditDialog extends Dialog implements android.view.View.OnClickListener
 {
     private MSDialog dialog;
     private RelativeLayout content;
     private Megasquirt ecu;
     private int nbPanels = 0;
     
     /**
      * Constructor for dialog which set the current dialog and ECU object
      * 
      * @param context
      * @param dialog
      */
     public EditDialog(Context context, MSDialog dialog)
     {
         super(context);
         
         this.ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         this.dialog = dialog;
     }
     
     /**
      * @param savedInstanceState
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.editdialog);
         
         content = (RelativeLayout) findViewById(R.id.content);
         
         setTitle(dialog.getLabel());
         
         Button buttonBurn = (Button) findViewById(R.id.burn);
         buttonBurn.setOnClickListener(this);
         
         Button buttonCancel = (Button) findViewById(R.id.cancel);
         buttonCancel.setOnClickListener(this);
         
         drawDialogFields(dialog, false, "");
         
         // Hide keyboard
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     }
     
     /**
      * Recursive function that will draw fields and panels of a dialog.
      * It's called recursively until all fields and panels were drawn.
      * 
      * @param dialog
      * @param isPanel
      * @param orientation
      */
     private void drawDialogFields(MSDialog dialog, boolean isPanel, String orientation)
     {
         TableLayout tl = new TableLayout(getContext());
         LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 
         // Used on label in table row with field beside label, add a margin right
         // so the label and field are separated
         LayoutParams lpSpan = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         lpSpan.setMargins(0, 0, 8, 0);
 
         // Used on label in table row with no field beside
         // Those are usually used as separator so add top and bottom margins
         LayoutParams lpSpanWithMargins = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         lpSpanWithMargins.setMargins(0, 10, 0, 15);
         lpSpanWithMargins.span = 2;
         
         tl.setId(nbPanels);
         
         // This is not the first panel we add on this dialog
         if (nbPanels > 0)
         {
             RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
             
             /*
             if (orientation.equals("North"))
             {
                 tlp.addRule(RelativeLayout.ABOVE, nbPanels - 1);
                 tl.setPadding(0, 0, 0, 10);
             }
             else if (orientation.equals("South"))
             {
                 tlp.addRule(RelativeLayout.BELOW, nbPanels - 1);
                 tl.setPadding(0, 10, 0, 0);
             }
             else if (orientation.equals("West"))
             {
                 tlp.addRule(RelativeLayout.LEFT_OF, nbPanels - 1);
                 tl.setPadding(0, 0, 10, 0);
             }
             else
             {
                 tlp.addRule(RelativeLayout.RIGHT_OF, nbPanels - 1);
                 tl.setPadding(10, 0, 0, 0);
             }*/
             
             tlp.addRule(RelativeLayout.RIGHT_OF, nbPanels - 1);
             tl.setPadding(10, 0, 0, 0);
             
             tl.setLayoutParams(tlp);
         }
         
         content.addView(tl);
         nbPanels++;
         
         TableRow tableRow;
         
         // If it's a panel or at least a dialog with one field or more, we can display its label
         if (isPanel || dialog.getFieldsList().size() > 0)
         {
             showPanelLabel(dialog.getLabel(), tl);
         }
         
         // For each dialog field, add a row in the table layout
         for (DialogField df : dialog.getFieldsList())
         {
             Constant constant = ecu.getConstantByName(df.getName());
 
             if (constant == null && !df.getName().equals("null"))
             {
                 showConstantDoesntExists(df.getName());
             }
             else 
             {
                 tableRow = new TableRow(getContext());
                 
                 String labelText = df.getLabel();
                 
                 // Add units to label
                 if (constant != null && !constant.getUnits().equals(""))
                 {
                     labelText += " (" + constant.getUnits() + ")";
                 }
                 
                 TextView label = new TextView(getContext());
                 label.setText(labelText);
                 
                 // If the first character of the label is a #, we need to highlight that label
                 // It means it is used as a section separator
                 if (labelText.length() > 0 && labelText.substring(0,1).equals("#"))
                 {
                     label.setText(" " + label.getText().toString().substring(1)); // Replace the # by a space
                     label.setBackgroundColor(Color.rgb(110, 110, 110));
                 }
                 
                 label.setLayoutParams(lpSpan);
                 
                 tableRow.addView(label);
                 
                 // For empty label or empty field name, we just insert an empty text view as second column of the row
                 if (df.getLabel().equals("") || df.getName().equals("null"))
                 {
                     // No second column so label is used to separate so make it bold and merge columns
                     label.setTypeface(null, Typeface.BOLD);
                     
                     // If it's not an empty label and not , add some top and bottom margins
                     if (!df.getLabel().equals(""))
                     {
                         label.setLayoutParams(lpSpanWithMargins);
                     }
                 }
                 else 
                 {
                     tableRow.setLayoutParams(lp);
                     
                     // Multi-choice constant
                     if (constant.getClassType().equals("bits"))
                     {
                         Spinner spin = new Spinner(getContext());
                         
                         // Field is ready only or disabled
                         if (df.isDisplayOnly() || !ecu.getVisibilityFlagsByName(df.getExpression()))
                         {
                             spin.setEnabled(false);
                         }
                         
                         /*
                         // Remove INVALID from values
                         List<String> valuesWithoutInvalid = new ArrayList<String>();
                         for (int i = 0; i < constant.getValues().length; i++)
                         {
                             String value = constant.getValues()[i];
                             if (!value.equals("INVALID"))
                             {
                                 valuesWithoutInvalid.add(value);
                             }
                         }
                         
                         valuesWithoutInvalid.toArray(new String[valuesWithoutInvalid.size()])
                         */
                         
                         ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, constant.getValues());
                         
                         // Specify the layout to use when the list of choices appears
                         spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                         
                         spin.setAdapter(spinAdapter);
                         
                         int selectedValue = (int) ecu.getField(df.getName());                        
                         spin.setSelection(selectedValue);
                         spin.setTag(df.getName());
                         
                         spin.setOnItemSelectedListener(new OnItemSelectedListener()
                         {
                             @Override
                             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
                             {
                                 refreshFieldsVisibility();
                             }
 
                             @Override
                             public void onNothingSelected(AdapterView<?> parentView){}
                         });
                         
                         tableRow.addView(spin);
                     }
                     // Single value constant
                     else
                     {
                         double constantValue = ecu.roundDouble(ecu.getField(df.getName()),constant.getDigits());
                         String displayedValue = "";
                         
                         if (constant.getDigits() == 0)
                         {
                             displayedValue = String.valueOf((int) constantValue);
                         }
                         else
                         {
                             displayedValue = String.valueOf(constantValue);
                         }
 
                         EditText edit = new EditText(getContext());
                         edit.setText(displayedValue);
                         edit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                         edit.setSingleLine(true);
                         edit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                         edit.setPadding(8, 5, 8, 5);
                         edit.setTag(df.getName());
                         
                         // Field is ready only or disabled
                         if (df.isDisplayOnly() || !ecu.getVisibilityFlagsByName(df.getExpression()))
                         {
                             edit.setEnabled(false);
                         }
                         
                         tableRow.addView(edit);
                     }
                 }
                 
                 tl.addView(tableRow);
             }           
         }        
         
         // For each dialog panel, add a layout to the dialog
         for (DialogPanel dp : dialog.getPanelsList())
         {
             MSDialog dialogPanel = ecu.getDialogByName(dp.getName());
             
             if (dialogPanel != null)
             {
               drawDialogFields(dialogPanel, true, dp.getOrientation());
             }
             else
             {
               // Maybe it's a curve panel
               //
               // CurveEditor curvePanel = ecu.getCurveEditorByName(dp.getName());
               // if (curvePanel)
               // {
               // 
               // }
               //
             }
         }
     }
     
     /**
      * Add a label at the top of a panel
      * 
      * @param title
      * @param tl
      */
     private void showPanelLabel(String title, TableLayout tl)
     {
         LayoutParams lpSpan = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         lpSpan.span = 2;     
         
         TableRow tableRow = new TableRow(getContext());
         tableRow.setLayoutParams(lpSpan);
         
         TextView label = new TextView(getContext());
         label.setText(title);
         label.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
         label.setPadding(0, 0, 0, 10);
         label.setLayoutParams(lpSpan);
         
         tableRow.addView(label);
         
         tl.addView(tableRow);
     }
     
     /**
      * Show the constant doesn't exists alert dialog
      * 
      * @param constantName The name of the constant that doesn't exists
      */
     private void showConstantDoesntExists(String constantName)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
         builder.setMessage("Uh oh, It looks like constant \"" + constantName + "\" is missing!")
                 .setIcon(android.R.drawable.ic_dialog_info)
                 .setTitle("Missing constant")
                 .setCancelable(true)
                 .setPositiveButton("OK", new DialogInterface.OnClickListener()
                 {
                     public void onClick(DialogInterface dialog, int id){}
                 });
         
         AlertDialog alert = builder.create();
         alert.show();  
     }
     
     /**
      * When value are changed, it's possible dialog fields change state
      * so we need to refresh fields visibility and reply them
      */
     private void refreshFieldsVisibility()
     {
        
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
