 package jtan325.contactmanager;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class EditContact extends Activity {
 	
 	
 	private ContactsList contactsList = ContactsList.getInstance();
 	
 	private EditText firstName;
 	private EditText lastName;
 	private EditText mobile;
 	private EditText home;
 	private EditText work;
 	private EditText email;
 	private EditText address;
 	private EditText DOB;
 	private ImageView image;
 	private static int RESULT_LOAD_IMAGE = 1;
	private String photoPath = ContactDetails.contactClicked.getPhotoPath();
 	
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_contact);
 		setTitle("Edit");
 		
 		setupContactInfo();
 		
 		
 		
 		image.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				
 				//new action taking you to gallery to select a picture
 				Intent i = new Intent(
 						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 						 
 						startActivityForResult(i, RESULT_LOAD_IMAGE);
 			}
 		});
 		
 		Log.d(NewContact.TAG, "Able to load image");
 		
 		
 		//customizing the home button on the actionbar
 		getActionBar().setHomeButtonEnabled(true);
 		getActionBar().setIcon(R.drawable.arrow3);
 		
 		
 		
 		firstName =(EditText) findViewById(R.id.first_name);
 		firstName.setFocusable(false);
 		
 		firstName.setOnClickListener( new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				firstName.setFocusableInTouchMode(true);
 				
 			}
 		});
 		
 		
 		
 		
 	}
 	
 	
 	private void setupContactInfo(){
 		
 		
 		//matching the edittext fields with XML
 		firstName =(EditText) findViewById(R.id.first_name);
 		lastName =(EditText) findViewById(R.id.last_name);
 		mobile = (EditText)findViewById(R.id.mobile_edit);
 		home = (EditText)findViewById(R.id.home_edit);
 		work = (EditText)findViewById(R.id.work_edit);
 		email = (EditText)findViewById(R.id.email_edit);
 		address = (EditText)findViewById(R.id.address_edit);
 		DOB = (EditText)findViewById(R.id.DOB_edit);
 		image= (ImageView)findViewById(R.id.edit_image);
 
 		//loading current contact data into respective the edittext fields
 		firstName.setText(ContactDetails.contactClicked.getFirstName());
 		lastName.setText(ContactDetails.contactClicked.getLastName());
 		mobile.setText(ContactDetails.contactClicked.getMobile());
 		home.setText(ContactDetails.contactClicked.getHome());
 		work.setText(ContactDetails.contactClicked.getWork());
 		email.setText(ContactDetails.contactClicked.getEmail());
 		address.setText(ContactDetails.contactClicked.getAddress());
 		DOB.setText(ContactDetails.contactClicked.getDOB());
 		image.setImageBitmap(BitmapFactory.decodeFile(ContactDetails.contactClicked.getPhotoPath()));
 		
 	}
 	
 	
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
          
         if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
             Uri selectedImage = data.getData();
             String[] filePathColumn = { MediaStore.Images.Media.DATA };
  
             Cursor cursor = getContentResolver().query(selectedImage,
                     filePathColumn, null, null, null);
             cursor.moveToFirst();
  
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             photoPath = cursor.getString(columnIndex);
             cursor.close();
             
             image.setImageBitmap(BitmapFactory.decodeFile(photoPath));
         }
         
  }
 	
 	
 	
 	
 	
 	 //this method allows buttons from the actionbar to have functionality
 	 public boolean onOptionsItemSelected(MenuItem item) { 
 		 switch (item.getItemId()) { 
 		 
 		//clicking on the "save" button saves all edited details and redirects to the contact details screen.
 		 case R.id.save: 
 			 
 			 
 			 //saving the new values of the edittext 
 			 String saveFirstName = firstName.getText().toString();
 			 String saveLastName = lastName.getText().toString();
 			 String saveMobile = mobile.getText().toString();
 			 String saveHome = home.getText().toString();
 			 String saveWork = work.getText().toString();
 			 String saveEmail = email.getText().toString();
 			 String saveAddress = address.getText().toString();
 			 String saveDOB = DOB.getText().toString();
 			 
 			
 			 
 		
 			 
 			 Log.d(NewContact.TAG, "Attempting to save editted details: " + saveFirstName);
 			 
 			 //updating and changing the database values for the editted values
 			 MainActivity.db = openOrCreateDatabase(dbconstants.DATABASE_NAME,MODE_PRIVATE, null);
 			 ContentValues values = new ContentValues();
 			 values.put(dbconstants.CONTACT_FIRST, saveFirstName);
 			 values.put(dbconstants.CONTACT_LAST, saveLastName);
 			 values.put(dbconstants.CONTACT_MOBILE, saveMobile);
 			 values.put(dbconstants.CONTACT_HOME, saveHome);
 			 values.put(dbconstants.CONTACT_WORK, saveWork);
 			 values.put(dbconstants.CONTACT_EMAIL, saveEmail);
 			 values.put(dbconstants.CONTACT_ADDRESS, saveAddress);
 			 values.put(dbconstants.CONTACT_DOB, saveDOB);
 			 values.put(dbconstants.CONTACT_IMAGE, photoPath);
 			
 			 Log.d(NewContact.TAG, "Successfully created contents value for "+ ContactDetails.contactClicked.getFirstName());
 				
 			 MainActivity.db.update(dbconstants.TABLE_NAME, values, dbconstants.CONTACT_FIRST+ " = ?", new String[] {ContactDetails.contactClicked.getFirstName()});
 			 MainActivity.db.close();
 			 
 			 //updating the contact object in the contactList to be consistent with the current changes
 			 ContactDetails.contactClicked.setFirstName(saveFirstName);
 			 ContactDetails.contactClicked.setLastName(saveLastName);
 			 ContactDetails.contactClicked.setMobile(saveMobile);
 			 ContactDetails.contactClicked.setHome(saveHome);
 			 ContactDetails.contactClicked.setWork(saveWork);
 			 ContactDetails.contactClicked.setEmail(saveEmail);
 			 ContactDetails.contactClicked.setAddress(saveAddress);
 			 ContactDetails.contactClicked.setDOB(saveDOB);
			 ContactDetails.contactClicked.setPhotoPath(photoPath);
 			 
 			  
 			 finish();
 			 break;
 			 
 			 
 		 //clicking on the "back" button redirects to the contact details screen 
 		 case android.R.id.home: 
 			 
 			
 			 onBackPressed();
 			 break;
 		 
 		 //clicking on the "delete" button opens up a dialogue box prompting the user and then deleting the contact if the accept.
 		 //delete functionality has been correctly implemented 
 		 case R.id.delete:
 			 
 			
 					 
 			 AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditContact.this);
 						
 			 dialogBuilder.setTitle("Are you sure you want to delete this contact?");
 			 dialogBuilder.setMessage("This action will permanently remove the contact");
 						
 			 dialogBuilder.setNegativeButton("Cancel", null);
 			 dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 							
 						
 				 public void onClick(DialogInterface dialog, int which) {
 								
 					//removing the value from the database 
 					MainActivity.db = openOrCreateDatabase(dbconstants.DATABASE_NAME,MODE_PRIVATE, null);
 					Log.d(NewContact.TAG, dbconstants.CONTACT_ID + " ='" + ContactDetails.contactClicked.getId()+ "'");
 					boolean deleted = MainActivity.db.delete(dbconstants.TABLE_NAME, dbconstants.CONTACT_ID + " ='" + ContactDetails.contactClicked.getId()+ "'", null) >0; 
 					
 					//removing the contact from the contactList 
 					String name = ContactDetails.contactClicked.toString();
 					Log.d(NewContact.TAG, "Successfully deleted " +name+" from database");
 				
 					contactsList.delete(ContactDetails.contactClicked);
 					MainActivity.db.close();
 					 
 					//going 2 screens back to the main activity			
 					Intent a = new Intent(EditContact.this,MainActivity.class);
 					a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(a);
 								
 					}
 				 
 				});
 			 
 			dialogBuilder.setCancelable(true);
 			dialogBuilder.create().show();
 			break;
 							
 		 }
 		 return true; 
 	}
 	
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.edit_contact, menu);
 		return true;
 	}
 
 }
