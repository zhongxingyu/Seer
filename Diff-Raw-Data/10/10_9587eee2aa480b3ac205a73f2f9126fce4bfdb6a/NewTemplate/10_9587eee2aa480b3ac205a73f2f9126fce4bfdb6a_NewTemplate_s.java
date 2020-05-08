 package com.krl109.scheduler.template;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.krl109.scheduler.R;
 import com.krl109.scheduler.db.TemplateDatabaseHelper;
 import com.krl109.scheduler.main.MainActivity;
 
 
 public class NewTemplate extends Activity implements OnClickListener 
 {
 	protected static final int CONTACT_PICKER_RESULT = 1001;
 	//	MyCustomAdapter dataAdapter = null;
 	private TemplateDatabaseHelper databaseHelper;
 	Button add_count, btn_save, btn_cancel;
 	public String def_character, category;
 	AlertDialog categoryDialog;
 	TextView typ;
 	EditText tmp_message, tmp_date, tmp_name;
 
 	RadioButton radioDataButton;
 	RadioGroup radioDataGroup;
 
 	private String[] data;
 	public String name, message;
 	int categoryID;
 	String[] def_ch={" %%AGE%%", " %%DATE%%", " %%MONTH%%",
 	" %%YEAR%%"};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		databaseHelper = new TemplateDatabaseHelper(this);
 		setContentView(R.layout.new_template);
 		//tmp_message = (EditText) findViewById(R.id.tmp_message);
 
 		/*category = (Spinner) findViewById(R.id.freq_spinner);
 		ArrayAdapter<String> list = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, cat);
 		list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		category.setAdapter(list);*/
 
 		tmp_name = (EditText) findViewById(R.id.tmp_name_txt);
 		tmp_message = (EditText) findViewById(R.id.tmp_message);
 		tmp_message.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				if(s.toString().contains("%%")){
 					typ.setText(R.string.typical_type);
 				}else{typ.setText(R.string.normal_type);}
 			}
 		});
 		typ = (TextView) findViewById(R.id.categoryTxt);
 		typ.setText(R.string.normal_type);
 
 		add_count = (Button) findViewById(R.id.btn_data_count);
 		registerForContextMenu(add_count);
 		/*add_count.setOnClickListener(new Button.OnClickListener() 
 		{
 			public void onClick(View v) 
 			{
 				//untuk buka count dialog
 				addCountDialog(tmp_message);
 			}
 		});*/
 
 		btn_save = (Button) findViewById(R.id.btn_save);
 		btn_save.setOnClickListener(this);
 		btn_cancel = (Button) findViewById(R.id.btn_cancel);
 		btn_cancel.setOnClickListener(this);
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// TODO Auto-generated method stub
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Choose The define Character");
 		menu.add(0, v.getId(), 0, "AGE");
 		menu.add(0, v.getId(), 0, "DATE");
 		menu.add(0, v.getId(), 0, "MONTH");
 		menu.add(0, v.getId(), 0, "YEAR");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		typ.setText(R.string.typical_type);
 		if(item.getTitle().equals("AGE"))
 		{
 			tmp_message.append(def_ch[0]);
			//			tmp_message.getText().replace(Math.min(start, end), Math.max(start, end), def_ch[0], 0, def_ch[0].length());
 
 		}else if(item.getTitle().equals("DATE"))
 		{
 			tmp_message.append(def_ch[1]);
 		}else if(item.getTitle().equals("MONTH"))
 		{
 			tmp_message.append(def_ch[2]);
 		}else if(item.getTitle().equals("YEAR"))
 		{
 			tmp_message.append(def_ch[3]);
 
 		}else {return false;}
 		return super.onContextItemSelected(item);
 	}
 
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		message = tmp_message.getText().toString();
 		data = new String[3];
 		if(v.equals(btn_save))
 		{
 			if(databaseHelper.checkTemplate(tmp_name.getText().toString())){
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				String message1 = "Change Your template's name";
 				String message2 = "Your template's name already exist.";
 				builder.setTitle("Alert");
 				builder.setMessage(message1 + "\n" + message2);
 				builder.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,int id) {
 						// if this button is clicked, close
 						// current activity
 						tmp_message.setText("");
 					}
 				  });
 				// create alert dialog
 				AlertDialog alertDialog = builder.create();
  
 				// show it
 				alertDialog.show();
 			}
 			
 			//cek message content
 			if(message.contains("%%"))
 			{
 				openDialogCategory();
 
 
 			}else{category= "Other";
 			if(databaseHelper.checkCategory(category)){
 				categoryID = databaseHelper.getCategoryId(category);
 				Log.d("category", ""+categoryID);
 			}else{
 				databaseHelper.saveTemplatetoCategory(category);
 				Log.d("category", category);
 			}
 			}
 			//save
 			// addto list, to database template
 						data[0] = tmp_message.getText().toString();
 						data[1] = typ.getText().toString();
 						data[2] = tmp_name.getText().toString();
 						databaseHelper.saveTemplatetoTemplate(categoryID, data);
 			for (int i=0;i<data.length;i++)
 			{
 				Log.d("data", data[i]);
 			}
 			Toast.makeText(this, "saved", Toast.LENGTH_LONG).show();
 		}
 		/*else if(v.equals(btn_datetime))
 		{
 			//retrieve from contact
 
 		 * if cat equals birthday
 		 * then find data.birthday contact 
 		 * 		if null then
 		 * 			show alert
 		 * 		endif
 		 * endif
 
 			if (category.getSelectedItem().toString() == "Birthday")
 			{
 				// iterate through all Contact's Birthdays and print in log
 				Cursor cursor = getContactsBirthdays();
 				int bDayColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
 				while (cursor.moveToNext()) {
 					String bDay = cursor.getString(bDayColumn);
 					Log.d("BD", "Birthday: " + bDay);
 				}
 			}else if (category.getSelectedItem().toString() == "Anniversary")
 			{
 				// iterate through all Contact's Birthdays and print in log
 				Cursor cursor = getContactsAnniversary();
 				int bDayColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
 				while (cursor.moveToNext()) {
 					String annivDay = cursor.getString(bDayColumn);
 					Log.d("BD", "Annivday: " + annivDay);
 				}
 			}
 		}*/
 		else if(v.equals(btn_cancel))
 		{
 			Intent temp = new Intent(NewTemplate.this, MainActivity.class);
 			startActivity(temp);
 		}
 	}
 







 	private void openDialogCategory()
 	{
 		// Strings to Show In Dialog with Radio Buttons
 		final CharSequence[] seq_category = {" ANNIVERSARY "," BIRTHDAY "};
 
 		// Creating and Building the Dialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Select The Category");
 		builder.setItems(seq_category, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				switch(which)
 				{
 				case 0:
 					category = "anniversary";
 					break;
 				case 1:
 					category = "birthday";
 					break;
 				}
 				if(databaseHelper.checkCategory(category)){
 					categoryID = databaseHelper.getCategoryId(category);
 					Log.d("category", ""+categoryID);
 				}else{
 					databaseHelper.saveTemplatetoCategory(category);
 					Log.d("category", category);
 				}
 				categoryDialog.dismiss();
 			}
 		});
 		categoryDialog = builder.create();
 		categoryDialog.show();
 	}
 
 }
