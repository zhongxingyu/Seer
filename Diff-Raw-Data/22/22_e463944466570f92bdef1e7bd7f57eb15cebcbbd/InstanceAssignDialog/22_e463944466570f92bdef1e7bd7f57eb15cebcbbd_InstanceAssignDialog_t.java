 package com.radicaldynamic.gcmobile.android.dialogs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.widget.Toast;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.documents.FormInstance;
 import com.radicaldynamic.groupinform.logic.AccountDevice;
 
 public class InstanceAssignDialog extends Builder 
 {
     private List<AccountDevice> deviceList = new ArrayList<AccountDevice>();
         
     private CharSequence [] items;
     boolean [] checkedItems;
 
     public InstanceAssignDialog(final Context context, final FormInstance fi)
     {
         super(context);
         
         deviceList = new ArrayList<AccountDevice>(Collect.getInstance().getInformOnlineState().getAccountDevices().values());
         items = new CharSequence[deviceList.size()];
         checkedItems = new boolean[deviceList.size()];
         
         int i = 0;
         
         for (AccountDevice d : deviceList) {
             if (fi.getAssignedTo() != null) {
                 if (fi.getAssignedTo().contains(d.getId()))
                     checkedItems[i] = true;
                 else 
                     checkedItems[i] = false;
             }
             
             items[i++] = d.getDisplayName();
         }
         
         setTitle(context.getString(R.string.tf_select_devices));
         
         setMultiChoiceItems(items, checkedItems, new OnMultiChoiceClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                 checkedItems[which] = isChecked;
             }
         });
         
         setNegativeButton(context.getString(R.string.cancel), new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();      
             }
         });
         
         setPositiveButton(context.getString(R.string.tf_save), new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                fi.setAssignedTo(new ArrayList<String>());
                 
                 int i = 0;
                 
                 for (AccountDevice d : deviceList) {
                     if (checkedItems[i++] == true)
                         fi.getAssignedTo().add(d.getId());
                 }
                 
                 Toast.makeText(context, "Assignment updated", Toast.LENGTH_LONG).show();
                 dialog.dismiss();               
             }
         });
     }
 }
