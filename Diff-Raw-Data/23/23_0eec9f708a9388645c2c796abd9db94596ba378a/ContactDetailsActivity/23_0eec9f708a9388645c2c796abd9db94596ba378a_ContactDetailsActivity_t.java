 package edu.umn.contactviewer;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Darin
  * Date: 2/24/13
  * Time: 10:50 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ContactDetailsActivity extends Activity {
     private ContactRepository storage;
     private int contactID;
     private Contact contact;
     TextView nameView;
     TextView titleView;
     TextView phoneView;
     TextView emailView;
     TextView twitterIdView;
     //private ContactStorage contacts = new LocalContactStorage(ContactDetailsActivity.this);
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.detail);
 
         storage = (ContactRepository)getIntent().getSerializableExtra(ContactListActivity.REPOSITORY);
         contactID = getIntent().getIntExtra(ContactListActivity.CONTACT_ID, -1);
         contact = storage.lookupContact(contactID);
 
         nameView = (TextView)findViewById(R.id.name_value);
         titleView = (TextView)findViewById(R.id.title_value);
         phoneView = (TextView)findViewById(R.id.phone_value);
         emailView = (TextView)findViewById(R.id.email_value);
         twitterIdView = (TextView)findViewById(R.id.twitterId_value);
         nameView.setText(contact.getName());
         titleView.setText(contact.getTitle());
         phoneView.setText(contact.getPhone());
         emailView.setText(contact.getEmail());
         twitterIdView.setText(contact.getTwitterId());
 
         ToolbarConfig toolbar = new ToolbarConfig(this, contact.getName());
 
         Button rightButton = toolbar.getToolbarRightButton();
         rightButton.setText("Edit");
 
         rightButton.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Toast.makeText(ContactDetailsActivity.this, "Edit Button clicked!", Toast.LENGTH_LONG).show();
                 Intent intent = new Intent(ContactDetailsActivity.this, ContactEditActivity.class);
                //intent.putExtra("contact", contact);
                intent.putExtra(ContactListActivity.CONTACT_ID, contact.getID());
 				intent.putExtra(ContactListActivity.REPOSITORY, storage);
                 ContactDetailsActivity.this.startActivityForResult(intent, 77);
             }
 
         });
 
         //***********************************************
         //*** GOT STUMPED HERE, SO I COMMENTED IT OUT ***
         //rightButton.setOnClickListener(new View.OnClickListener() {
         //    @Override
         //    public void onClick(View view) {
         //        //Toast.makeText(ContactDetailsActivity.this, "This would edit contact " + contact.getName(), Toast.LENGTH_LONG).show();
         //        Intent intent = new Intent(this, ContactEditActivity.class);
         //        //intent.putExtra("contact", storage.getContact(position));
         //        //intent.putExtra("contactIndex", position);
         //        //startActivityForResult(intent, 5);
         //    }
         //});
 
         Button backButton = toolbar.getToolbarLeftButton();
         backButton.setText("Back");
         backButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 finish();
             }
         });
 
         Button deleteButton = (Button)findViewById(R.id.bottom_button);
         deleteButton.setText("Delete");
         //deleteButton.setVisibility(deleteButton.GONE);
         deleteButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 boolean deleteContact = false;
                 AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailsActivity.this);
                 builder.setMessage("Are you sure you want to delete " + contact.getName() + "?")
                         .setTitle("Delete " + contact.getName() + "?");
                 builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         ContactDetailsActivity.this.setResult(RESULT_OK);
                         finish();
                     }
                 });
                 builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 });
                 AlertDialog deleteAlert = builder.create();
                 deleteAlert.show();
             }
         });
     }
 
 //    @Override
 //    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 //        //To change body of implemented methods use File | Settings | File Templates.
 //
 //        Intent intent = new Intent(this, ContactEditActivity.class);
 //        intent.putExtra("contact", parent.contact); //*** WE NEED TO ADD HERE  ***
 //        intent.putExtra("contactIndex", position);
 //        startActivityForResult(intent, 5);
 //    }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         //super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
         Contact contact;
         int position;
         switch (requestCode) {
             case 77:
                 if (resultCode == RESULT_OK) {
                    //contact = (Contact)data.getSerializableExtra("contact");
                    //position = data.getIntExtra("contactIndex", -1);
                    
                    contactID = getIntent().getIntExtra(ContactListActivity.CONTACT_ID, -1);
                    contact = storage.lookupContact(contactID);

                     nameView.setText(contact.getName());
                     titleView.setText(contact.getTitle());
                     phoneView.setText(contact.getPhone());
                     emailView.setText(contact.getEmail());
                     twitterIdView.setText(contact.getTwitterId());
                 }
         }
     }
 
     //   @Override
     //   public boolean onCreateOptionsMenu(Menu menu) {
     //       getMenuInflater().inflate(android.R.menu.detail, menu);
     //       return true;
     //   }
 }
