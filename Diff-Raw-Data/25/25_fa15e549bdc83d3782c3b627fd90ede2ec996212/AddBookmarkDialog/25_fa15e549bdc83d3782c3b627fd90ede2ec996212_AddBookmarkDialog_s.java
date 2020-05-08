 package com.github.alexesprit.chatlogs.dialog;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 import com.github.alexesprit.chatlogs.R;
 import com.github.alexesprit.chatlogs.item.Bookmark;
 import com.github.alexesprit.chatlogs.loader.LogLoaderFactory;
 
 public final class AddBookmarkDialog {
     private AlertDialog dialog;
 
     public AddBookmarkDialog(final Context context) {
         String data[] = LogLoaderFactory.getSupportedLoaderNames();
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, data);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         View dialogView = LayoutInflater.from(context).inflate(R.layout.add_bookmark_dialog, null);
         final EditText bmAddress = (EditText)dialogView.findViewById(R.id.dialog_add_bm_name);
         final Spinner bmType = (Spinner)dialogView.findViewById(R.id.dialog_add_bm_type);
         bmAddress.requestFocus();
         bmType.setAdapter(adapter);
 
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 String address = bmAddress.getText().toString();
                 if (!address.isEmpty()) {
                     AddBookmarkListener listener;
                     try {
                         listener = (AddBookmarkListener)context;
                         int type = bmType.getSelectedItemPosition();
                         Bookmark bm = new Bookmark(address, type);
                         listener.onBookmarkAdded(bm);
                     } catch (ClassCastException e) {
                     }
                 } else {
                     Toast.makeText(context, R.string.enter_address, Toast.LENGTH_SHORT).show();
                 }
             }
         });
         builder.setNegativeButton(android.R.string.cancel, null);
         builder.setTitle(R.string.dialog_bm_title);
         builder.setView(dialogView);
 
         dialog = builder.create();
     }
 
     public void show() {
         dialog.show();
     }
 
     public interface AddBookmarkListener {
         void onBookmarkAdded(Bookmark bm);
     }
 }
