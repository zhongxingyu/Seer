 package com.your.worth.controller;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import com.your.worth.R;
 import com.your.worth.model.AppModel;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Catalin BORA
  * Date: 10/18/13
  * Time: 10:35 AM
  * An abstract class that holds the general methods for the income and spending activities.
  */
 public abstract class AbstractIncomeSendingControllerActivity extends Activity {
 
     // The ListView
     protected ListView mListView = null;
 
     // The tag to determent if it's income or spending window
     protected int mTag = 0;
 
     /** Called when the user clicks the Options tab */
     public void openOptions(View view) {
 
         Intent intent = new Intent(this, OprionsActivity.class);
         startActivity(intent);
     }
 
     /** Called when the user clicks the Home tab */
     public void openHome(View view) {
 
         Intent intent = new Intent(this, MainActivity.class);
         startActivity(intent);
     }
 
     /**
      * Method that loads or reloads the LisView from data from the AppModel
      */
     public void reloadListView() {
 
         // get the values from the AppModel
         // exclude 0 value
         final ArrayList<String> list = new ArrayList<String>();
 
        for (int i=0; i< AppModel.getInstance().getIncomeListSize(); i++) {
             if (AppModel.getInstance().getRecordValue(i,mTag) != 0 ) {
                 list.add(
                         AppModel.getInstance().getRecordValue(i,mTag)+" - "+
                                 AppModel.getInstance().getRecordDescription(i,mTag));
             }
         }
 
         ArrayAdapter<String> adapter = new CustomAdapter(this,list,mTag);
         // do i need to erase the content of the List View first ?
         // Nop.. this seams to be the default way to work with adapters ListView
 
         // Assign adapter to ListView
         mListView.setAdapter(adapter);
     }
 
     /** Called when the user clicks the Add button*/
     public void executeAdd(View view) {
 
         int value=0;
         String description;
 
         // get the values from the fields
         EditText valueTextField   = (EditText)findViewById(R.id.value);
         EditText descriptionTextField   = (EditText)findViewById(R.id.description);
 
         // convert the values
         description = descriptionTextField.getText().toString();
         if (!valueTextField.getText().toString().isEmpty()) {
             value = Integer.parseInt(valueTextField.getText().toString());
         }
 
         // externalize this code so it can be tested automatically
         addRecord(value,description);
 
         // clear the fields
         valueTextField.getText().clear();
         descriptionTextField.getText().clear();
 
         // reload the List View
         reloadListView();
     }
 
     /**
      * Method that adds an record to the AppModel and updates the ListView
      * @param value the value from the Amount field
      * @param description the description from the Description field
      */
     public void addRecord(int value, String description) {
 
         AppModel.getInstance().addRecordValueAndDescriptionByTag(value,description,mTag);
 
     }
 }
