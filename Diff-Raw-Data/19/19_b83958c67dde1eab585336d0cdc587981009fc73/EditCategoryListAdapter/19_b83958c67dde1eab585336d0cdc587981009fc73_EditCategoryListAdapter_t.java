 package edu.mit.moneyManager.listUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.mit.moneyManager.R;
 import edu.mit.moneyManager.view.ViewEditBudgetActivity;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.text.method.DigitsKeyListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EditCategoryListAdapter extends ArrayAdapter<CategoryItemEntry>{
     public static final String TAG="EditCategoryListAdapter.java";
     private Context context;
    private Context parentContext;
     private ArrayList<CategoryItemEntry> categories;
     private LayoutInflater inflator;
 
    public EditCategoryListAdapter(Context context, ArrayList<CategoryItemEntry> categories, Context parent) {
         super(context, 0, categories);
         this.categories = categories;
         this.context = context;
        this.parentContext = parent;
         inflator = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View view = convertView;
         Log.i(TAG, parent.toString());
         final CategoryItemEntry category = categories.get(position);
         if (category != null) {
             view = inflator.inflate(R.layout.list_entry_category_edit, null);
             
             view.setOnClickListener(null);
             view.setOnLongClickListener(null);
             view.setLongClickable(false);
             
             final TextView categoryName = (TextView) view.findViewById(R.id.category_name);            
             final TextView total = (TextView) view.findViewById(R.id.category_amount);
             
             categoryName.setText(category.getName());
             total.setText(category.getTotalAmount());
 
             Button edit = (Button) view.findViewById(R.id.edit_category_button);
             edit.setOnClickListener(new View.OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
                     // TODO Auto-generated method stub
                    final Dialog dialog = new Dialog(parentContext);
                     dialog.setContentView(R.layout.dialog_edit_category);
                     dialog.setTitle("Editing " + category.getName() + " Category");
                     dialog.setCancelable(false);
                     
                     Button saveBtn = (Button) dialog.findViewById(R.id.save_category_btn);
                     Button cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);
                     
                     //TODO: prepopulate and make user input save
 //                    final EditText newCategoryName = (EditText)dialog.findViewById(R.id.new_category_name);
 //                    final EditText newAmount = (EditText)dialog.findViewById(R.id.new_category_amount);
 //                    newCategoryName.setText(category.getName());
 //                    newAmount.setText(category.getTotalAmount());
                     saveBtn.setOnClickListener(new View.OnClickListener() {
                         
                         @Override
                         public void onClick(View v) {
                             // TODO Auto-generated method stub
                             EditText newNameView = (EditText)dialog.findViewById(R.id.new_category_name);
                             EditText amountInput = (EditText)dialog.findViewById(R.id.new_category_amount);
                             amountInput.setKeyListener(DigitsKeyListener.getInstance());
 
                             newNameView.setText("new name");
                             amountInput.setText("100");
                             category.setName("new name");
                             category.setTotal(100);
                             categoryName.setText("new name");
                             total.setText("100");
                             dialog.dismiss();
 //                            try {
 ////                                category.setTotal(new Integer(amountInput.getText().toString()));
 //                                category.setName(newNameView.getText().toString());
 //                                category.setTotal(Integer.parseInt(amountInput.getText().toString()));
 //                                dialog.dismiss();
 //
 //                            }
 //                            catch (NumberFormatException e) {
 //                                Toast.makeText(context, "please enter a valid amount", Toast.LENGTH_SHORT);
 //                            }
                         }
                     });
                     cancelBtn.setOnClickListener(new View.OnClickListener() {
                         
                         @Override
                         public void onClick(View v) {
                             dialog.dismiss();
                         }
                     });
                     
                     dialog.show();
                 }
             });
             
         }
         return view;
     }
 
 }
