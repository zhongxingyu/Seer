 package ngocnv371.Money;
 
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import ngocnv371.Money.Model.DatabaseHelper;
 import ngocnv371.Money.Model.Tag;
 import ngocnv371.Money.Model.Transaction;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TimePicker;
 
 public class TransactionDetailActivity extends Activity implements OnClickListener{
 	Button mSave;
 	AutoCompleteTextView mName;
 	EditText mSpend;
 	EditText mNotes;
 	MultiAutoCompleteTextView mTags;
 	DatePicker mDate;
 	TimePicker mTime;
 	DatabaseHelper mHelper;
 	
 	Long mPassedId = null;
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.transaction_detail);
 		try {
 			mHelper = new DatabaseHelper(this);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new Error(e.getMessage());
 		}
 		mSave = (Button)findViewById(R.id.transaction_detail_save);
 		mName = (AutoCompleteTextView)findViewById(R.id.transaction_detail_item);
 		mSpend = (EditText)findViewById(R.id.transaction_detail_spend);
 		mNotes = (EditText)findViewById(R.id.transaction_detail_notes);
 		mTags = (MultiAutoCompleteTextView)findViewById(R.id.transaction_detail_tags);
 		mDate = (DatePicker)findViewById(R.id.transaction_detail_date);
 		mTime = (TimePicker)findViewById(R.id.transaction_detail_time);
 		
 		mSave.setOnClickListener(this);
 		try {
 			mName.setAdapter(new ItemsAdapter(this, mHelper.getItems(""), mHelper));
 			mTags.setAdapter(new TagsAdapter(this, mHelper.getTags(""), mHelper));
 			mTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new Error(e.getMessage());
 		}
 		
 		Intent intent = getIntent();
 		Bundle bundle = intent.getExtras();
 		if (bundle != null) {
 			mPassedId = bundle.getLong("Id");
 			if (mPassedId != null) {
 				Transaction transaction = mHelper.getTransaction(mPassedId);
 				mName.append(transaction.ItemName);
 				mNotes.setText(transaction.Notes);
 				mSpend.setText(transaction.Spend + "");
 				mDate.updateDate(transaction.Year, transaction.Month, transaction.Day);
 				mTime.setCurrentHour(transaction.Hour);
 				mTime.setCurrentMinute(transaction.Minute);
 				Cursor cursor = mHelper.getItemTags(transaction.ItemId);
 				if (cursor.moveToFirst()) {
					String tags = new Tag(cursor).Name;
 					while (cursor.moveToNext()) {
 						Tag tag = new Tag(cursor);
 						tags += ", " + tag.Name;
 					}
 					mTags.setText(tags);
 				}
 			}
 		}
 	}
 
 	public void onClick(View v) {
 		if (v.getId() == R.id.transaction_detail_save) {
 			try {
 				
 				if (mPassedId != null) {
 					mHelper.updateTransaction(mPassedId,
 							mName.getText().toString(),
 							Integer.parseInt(mSpend.getText().toString()),
 							mNotes.getText().toString(),
 							mTags.getText().toString(),
 							mDate.getYear(),
 							mDate.getMonth(),
 							mDate.getDayOfMonth(),
 							mTime.getCurrentHour(),
 							mTime.getCurrentMinute());
 				} else {
 					mHelper.createTransaction(
 							mName.getText().toString(),
 							Integer.parseInt(mSpend.getText().toString()),
 							mNotes.getText().toString(),
 							mTags.getText().toString(),
 							mDate.getYear(),
 							mDate.getMonth(),
 							mDate.getDayOfMonth(),
 							mTime.getCurrentHour(),
 							mTime.getCurrentMinute());
 				}
 			} catch (InvalidKeyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			finish();
 		}
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		mHelper.close();
 	}
 }
