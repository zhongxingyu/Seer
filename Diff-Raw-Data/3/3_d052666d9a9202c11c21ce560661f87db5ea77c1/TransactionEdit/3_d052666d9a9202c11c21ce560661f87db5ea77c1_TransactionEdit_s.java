 package net.gumbercules.loot.transaction;
 
import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Date;
 
 import net.gumbercules.loot.R;
 import net.gumbercules.loot.account.Account;
 import net.gumbercules.loot.backend.CurrencyWatcher;
 import net.gumbercules.loot.backend.Database;
 import net.gumbercules.loot.backend.Logger;
 import net.gumbercules.loot.premium.PremiumNotFoundActivity;
 import net.gumbercules.loot.premium.ViewImage;
 import net.gumbercules.loot.repeat.RepeatActivity;
 import net.gumbercules.loot.repeat.RepeatSchedule;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images;
 import android.provider.MediaStore.Images.Media;
 import android.text.method.DigitsKeyListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class TransactionEdit extends Activity
 {
 	public static final String KEY_TRANSFER = "te_transfer";
 	private static final String TAG = "net.gumbercules.loot.TransactionEdit";
 	
 	private static final int REQ_REPEAT		= 0;
 	private static final int REQ_CAMERA		= 1;
 	private static final int REQ_GALLERY	= 2;
 	
 	private Transaction mTrans;
 	private RepeatSchedule mRepeat;
 	private int mTransId;
 	private int mRepeatId;		// used only when editing from repeat manager
 	private int mFinishIntent;
 	private int mRequest;
 	private int mType;
 	private int mAccountId;
 	private boolean mFinished;
 	private int mDefaultRepeatValue;
 	private int mLastRepeatValue;
 	private Date mDate;
 	private int mAccountPos;
 	private CurrencyWatcher mCurrencyWatcher;
 	private Uri mImageUri;
 
 	private RadioButton checkRadio;
 	private RadioButton withdrawRadio;
 	private RadioButton depositRadio;
 	
 	private EditText dateEdit;
 	private ImageButton dateButton;
 	private AutoCompleteTextView partyEdit;
 	private EditText amountEdit;
 	private EditText checkEdit;
 	private MultiAutoCompleteTextView tagsEdit;
 	
 	private Spinner accountSpinner;
 	private Spinner repeatAccountSpinner;
 	private Spinner repeatSpinner;
 	private ArrayAdapter<String> mRepeatAdapter;
 	
 	private RadioButton budgetRadio;
 	private RadioButton actualRadio;
 	
 	private Button saveButton;
 	private Button cancelButton;
 	
 	private boolean restarted;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.trans);
 		
 		mFinishIntent = RESULT_CANCELED;
 		mFinished = false;
 		mDefaultRepeatValue = -1;
 		mDate = null;
 		restarted = false;
 
 		ArrayList<String> repeat =
 			new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.repeat)));
 		mRepeatAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, repeat);
         mRepeatAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
         
 		// get the type code so we know whether to show a transaction or a transfer window
 		if (savedInstanceState != null)
 		{
 			mRequest = savedInstanceState.getInt(TransactionActivity.KEY_REQ);
 			mType = savedInstanceState.getInt(TransactionActivity.KEY_TYPE);
 			mTransId = savedInstanceState.getInt(Transaction.KEY_ID);
 			mAccountId = savedInstanceState.getInt(Account.KEY_ID);
 			long date = savedInstanceState.getLong(Transaction.KEY_DATE);
 			mDate = (date == 0 ? null : new Date(date));
 			
 			mRepeat = new RepeatSchedule();
 			mRepeat.iter = savedInstanceState.getInt(RepeatSchedule.KEY_ITER);
 			mRepeat.freq = savedInstanceState.getInt(RepeatSchedule.KEY_FREQ);
 			mRepeat.custom = savedInstanceState.getInt(RepeatSchedule.KEY_CUSTOM);
 			long end = savedInstanceState.getLong(RepeatSchedule.KEY_DATE);
 			mRepeat.end = (end == 0 ? null : new Date(end));
 			mAccountPos = savedInstanceState.getInt(TransactionEdit.KEY_TRANSFER);
 			String uri_string = savedInstanceState.getString("image_uri");
 			if (uri_string != null)
 			{
 				mImageUri = Uri.parse(uri_string);
 			}
 			
 			restarted = true;
 		}
 		else
 		{
 			Bundle extras = getIntent().getExtras();
 			mRequest = extras.getInt(TransactionActivity.KEY_REQ);
 			mType = extras.getInt(TransactionActivity.KEY_TYPE);
 			mTransId = extras.getInt(Transaction.KEY_ID);
 			mAccountId = extras.getInt(Account.KEY_ID);
 			mRepeatId = extras.getInt(RepeatSchedule.KEY_ID);
 		}
 	}
 	
 	private void populateFields()
 	{
 		depositRadio = (RadioButton)findViewById(R.id.depositRadio);
 		withdrawRadio = (RadioButton)findViewById(R.id.withdrawRadio);
 		checkRadio = (RadioButton)findViewById(R.id.checkRadio);
 		
 		dateEdit = (EditText)findViewById(R.id.dateEdit);
 		dateEdit.setEnabled(false);
 		dateButton = (ImageButton)findViewById(R.id.datePickerButton);
 		
 		amountEdit = (EditText)findViewById(R.id.amountEdit);
 		checkEdit = (EditText)findViewById(R.id.checkEdit);
 		checkEdit.setKeyListener(new DigitsKeyListener());
 		mCurrencyWatcher = new CurrencyWatcher();
 		amountEdit.addTextChangedListener(mCurrencyWatcher);
 		tagsEdit = (MultiAutoCompleteTextView)findViewById(R.id.tagsEdit);
 		String[] tags = Transaction.getAllTags();
 		if (tags == null)
 			tags = new String[0];
 		ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(this, 
 				android.R.layout.simple_dropdown_item_1line, tags);
 		tagsEdit.setAdapter(tagsAdapter);
 		tagsEdit.setTokenizer(new TransactionActivity.SpaceTokenizer());
 		
 		// create the repeat spinner and populate the values
 		repeatSpinner = (Spinner)findViewById(R.id.repeatSpinner);
         repeatSpinner.setAdapter(mRepeatAdapter);
 
         actualRadio = (RadioButton)findViewById(R.id.ActualRadio);
 		budgetRadio = (RadioButton)findViewById(R.id.BudgetRadio);
 		
 		saveButton = (Button)findViewById(R.id.saveButton);
 		cancelButton = (Button)findViewById(R.id.cancelButton);
 		
         dateButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 showDialog(0);
             }
         });
 
 		// set the check radio to enable/disable and automatically populate the check entry field
 		checkRadio.setOnCheckedChangeListener( new RadioButton.OnCheckedChangeListener()
 		{
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				if (isChecked)
 				{
 					if (checkEdit.getText().toString().equals(""))
 					{
 						// autopopulate the edit with the next check number
 						Account acct = Account.getAccountById(mAccountId);
 						int check_num = acct.getNextCheckNum();
 						checkEdit.setText(new Integer(check_num).toString());
 					}
 				}
 				checkEdit.setEnabled(isChecked);
 			}
 		});
 		checkEdit.setEnabled(false);
 
         // load the transaction if mTransId > 0
         Transaction trans = null;
 		if (mTransId == 0 && mRepeatId == 0)
 		{
 			mTrans = new Transaction();
 			if (mRepeat == null)
 				mRepeat = new RepeatSchedule();
 			else
 				setRepeatSpinnerSelection(mRepeat);
 			
 			// set the date edittext to the current date by default
         	setDateEdit(mDate);
 		}
 		else
 		{
 			if (mTransId != 0)
 			{
 				if (mTrans == null)
 				{
 					mTrans = Transaction.getTransactionById(mTransId);
 				}
 			}
 			else
 			{
 				if (mRepeat == null && mTrans == null)
 				{
 					mRepeat = RepeatSchedule.getSchedule(mRepeatId);
 					mTrans = mRepeat.getTransaction();
 					mAccountId = mTrans.account;
 				}
 			}
 			trans = mTrans;
 			
 			if (!restarted)
 			{
 				if (mRepeat == null)
 				{
 					int repeat_id = RepeatSchedule.getRepeatId(mTransId);
 					if (repeat_id != -1)
 						mRepeat = RepeatSchedule.getSchedule(repeat_id);
 					else
 						mRepeat = new RepeatSchedule();
 				}
 				
 				if (trans == null)
 				{
 					Log.e(TAG + ".populateFields()", "trans is null");
 					return;
 				}
 	
 				// figure out if this is a normal transaction or a transfer
 				int transfer_id = mTrans.getTransferId();
 				if (transfer_id != -1)
 				{
 					Transaction transfer = Transaction.getTransactionById(transfer_id, true);
 					if (transfer == null)
 					{
 						transfer = new Transaction();
 						transfer.setId(-1);
 						mTrans.removeTransfer(transfer);
 						mType = TransactionActivity.TRANSACTION;
 					}
 					else
 					{
 						mType = TransactionActivity.TRANSFER;
 					}
 				}
 				else
 				{
 					if (mRepeat.getTransferId() > 0)
 					{
 						mType = TransactionActivity.TRANSFER;
 					}
 					else
 					{
 						mType = TransactionActivity.TRANSACTION;
 					}
 				}
 				
 				if (trans.type == Transaction.WITHDRAW)
 				{
 					withdrawRadio.setChecked(true);
 				}
 				else if (trans.type == Transaction.DEPOSIT)
 				{
 					depositRadio.setChecked(true);
 				}
 				
 				if (trans.budget && !trans.isPosted())
 				{
 					budgetRadio.setChecked(true);
 				}
 				else
 				{
 					actualRadio.setChecked(true);
 				}
 				
 				if (mDate == null)
 				{
 					setDateEdit(trans.date);
 				}
 				else
 				{
 					setDateEdit(mDate);
 				}
 	
 				// replace comma and currency symbol with empty string
 				NumberFormat nf = NumberFormat.getCurrencyInstance();
 				String new_currency = Database.getOptionString("override_locale");
 				if (new_currency != null && !new_currency.equals(""))
 					nf.setCurrency(Currency.getInstance(new_currency));
 				String num = nf.format(trans.amount);
 				StringBuilder sb = new StringBuilder();
 				sb.append(mCurrencyWatcher.getAcceptedChars());
 				String accepted = "[^\\Q" + sb.toString() + "\\E]";
 				num = num.replaceAll(accepted, "");
 
 				amountEdit.setText(num);
 				
 				tagsEdit.setText(trans.tagListToString());
 				
 				for (Uri uri : trans.images)
 				{
 					addImageRow(uri);
 				}
 			}
 
 			setRepeatSpinnerSelection(mRepeat);
 		}
         
 		if (mType == TransactionActivity.TRANSFER)
 		{
 	        ArrayAdapter<CharSequence> accountAdapter = showTransferFields();
 	        if (accountAdapter != null)
 	        {
 	        	if (!restarted)
 	        	{
 	        		Transaction transfer = null;
 	        		if (mTransId != 0)
 	        		{
 	        			transfer = Transaction.getTransactionById(mTrans.getTransferId(), true);
 	        		}
 	        		else if (mRepeatId != 0)
 	        		{
 	        			RepeatSchedule rs = RepeatSchedule.getSchedule(
 	        					RepeatSchedule.getRepeatId(mRepeat.getTransferId()));
 	        			transfer = rs.getTransaction();
 	        		}
 	        		
 	        		if (transfer != null)
 	        		{
 		        		Account acct = Account.getAccountById(transfer.account);
 		        		int pos = accountAdapter.getPosition(acct.name);
 		        		accountSpinner.setSelection(pos);
 	        		}
 	        	}
 		        else
 		        {
 		        	accountSpinner.setSelection(mAccountPos);
 		        }
 	        	
 	        	if (trans != null && trans.type == Transaction.CHECK)
 	        	{
 	        		fillCheckFields(trans.check_num);
 	        	}
 	        }
 		}
 		else
 		{
 			showTransactionFields();
 			if (trans != null)
 			{
 				partyEdit.setText(trans.party);
 
 				if (trans.type == Transaction.CHECK)
 				{
 					fillCheckFields(trans.check_num);
 				}
 			}
 		}
 		
 		amountEdit.setOnFocusChangeListener(new OnFocusChangeListener()
 		{
 			public void onFocusChange(View v, boolean hasFocus)
 			{
 				if (v instanceof EditText)
 				{
 					CurrencyWatcher.setInputType((EditText)v);
 				}
 			}
 		});
 		CurrencyWatcher.setInputType(amountEdit);
 				
 		repeatSpinner.setSelection(mDefaultRepeatValue);
 		repeatSpinner.setOnItemSelectedListener(mRepeatSpinnerListener);
 		
 		// show the second account spinner if we're editing from the repeat manager
 		if (mRepeatId != 0)
 		{
 			TableRow row = (TableRow)findViewById(R.id.repeatAccountRow);
 			row.setVisibility(View.VISIBLE);
 			
 			TextView text = (TextView)findViewById(R.id.repeatAccountLabel);
 			text.setText(R.string.trans_account);
 			text = (TextView)findViewById(R.id.accountLabel);
 			text.setText(R.string.trans);
 			
 			repeatAccountSpinner = (Spinner)findViewById(R.id.repeatAccountSpinner);
 			
 			String[] names = Account.getAccountNames();
 			
 			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
 					android.R.layout.simple_spinner_item, names);
 	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	        repeatAccountSpinner.setAdapter(adapter);
 	        
     		Account acct = Account.getAccountById(mTrans.account);
     		int pos = adapter.getPosition(acct.name);
 	        repeatAccountSpinner.setSelection(pos);
 	        
 	        // hide the image attachments
 	        row = (TableRow)findViewById(R.id.imageHeaderRow);
 	        row.setVisibility(View.GONE);
 	        LinearLayout ll = (LinearLayout)findViewById(R.id.imageLayout);
 	        ll.setVisibility(View.GONE);
 		}
 		
 		ImageView addImage = (ImageView)findViewById(R.id.addImage);
 		addImage.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				Intent i;
 				
 				// bail if premium isn't present
 				if (getContentResolver().getType(Uri.parse(
 						"content://net.gumbercules.loot.premium.settingsprovider/settings")) == null)
 				{
 					i = new Intent(TransactionEdit.this, PremiumNotFoundActivity.class);
 					startActivity(i);
 
 					return;
 				}
 
 				AlertDialog dialog = new AlertDialog.Builder(TransactionEdit.this)
 					.setTitle(R.string.attach_receipt)
 					.setItems(R.array.receipt_capture_name, 
 							new DialogInterface.OnClickListener()
 							{
 								@Override
 								public void onClick(DialogInterface dialog, int which)
 								{
 									Intent i = new Intent();
 									int req = 0;
 									
 									if (which == 0)
 									{
 										i.setAction(Intent.ACTION_GET_CONTENT);
 										i.setType("image/*");
 										req = REQ_GALLERY;
 									}
 									else if (which == 1)
 									{
 										ContentValues values = new ContentValues();
 								        values.put(Images.Media.TITLE, "Loot Receipt");
 								        values.put(Images.Media.BUCKET_ID, "Receipts");
 								        values.put(Images.Media.DESCRIPTION, "Receipt Image for Loot");
 								        values.put(Images.Media.MIME_TYPE, "image/jpeg");
 								        mImageUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
 										
 										i.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
 										i.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
 										req = REQ_CAMERA;
 									}
 									
 									startActivityForResult(i, req);
 								}
 							})
 					.create();
 				dialog.show();
 			}
 		});
 		
 		saveButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				mFinishIntent = RESULT_OK;
 				onPause();
 			}
 		});
 		
 		cancelButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				setResult(mFinishIntent);
 				finish();
 			}
 		});
 	}
 	
 	private void fillCheckFields(int check_num)
 	{
 		checkEdit.setText(new Integer(check_num).toString());
 		checkRadio.setChecked(true);
 		checkEdit.setEnabled(true);
 	}
 	
 	private void addImageRow(final Uri content_uri)
 	{
 		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		final LinearLayout imageLayout = (LinearLayout)findViewById(R.id.imageLayout);
 		
 		// bail if this uri was already added
 		if (imageLayout.findViewWithTag(content_uri) != null)
 		{
 			return;
 		}
 		
 		View row = inflater.inflate(R.layout.receipt_entry, null);
 		
 		row.setTag(content_uri);
 		
 		imageLayout.addView(row);
 		imageLayout.invalidate();
 		
 		Button button = (Button)row.findViewById(R.id.image_button);
 		
 		String[] columns = new String[] { Images.ImageColumns.TITLE };
 		Cursor cur = getContentResolver().query(content_uri, columns, null, null, null);
 		String title = "Receipt";
 		if (cur.moveToFirst())
 		{
 			title = cur.getString(0);
 		}
 		
 		button.setText(title);
 		button.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				Intent i = new Intent(TransactionEdit.this, ViewImage.class);
 				i.setData(content_uri);
 				startActivity(i);
 			}
 		});
 		
 		ImageView remove = (ImageView)row.findViewById(R.id.image_delete);
 		remove.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				imageLayout.removeView(imageLayout.findViewWithTag(content_uri));
 				mTrans.images.remove(content_uri);
 				imageLayout.invalidate();
 			}
 		});
 	}
 	
 	private OnItemSelectedListener mRepeatSpinnerListener = new Spinner.OnItemSelectedListener()
 		{
 			public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id)
 			{
 				// only remove the row if the adapter has more than the 7 default items
 				// and the position isn't the row to be deleted
 				if (mRepeatAdapter.getCount() > 7 && pos != 7)
 					mRepeatAdapter.remove("Custom");
 
 				if (pos == 6)
 				{
 					mLastRepeatValue = mDefaultRepeatValue;
 					
 			    	Intent i = new Intent(view.getContext(), RepeatActivity.class);
 			    	i.putExtra(RepeatSchedule.KEY_ITER, mRepeat.iter);
 			    	i.putExtra(RepeatSchedule.KEY_FREQ, mRepeat.freq);
 			    	i.putExtra(RepeatSchedule.KEY_CUSTOM, mRepeat.custom);
 			    	i.putExtra(RepeatSchedule.KEY_DATE, (mRepeat.end != null) ? mRepeat.end.getTime() : 0);
 			    	startActivityForResult(i, REQ_REPEAT);
 				}
 				
 				mDefaultRepeatValue = pos;
 			}
 	
 			public void onNothingSelected(AdapterView<?> adapter) { }
 		};
 	
 	private void setDateEdit(Date date)
 	{
 		Calendar cal = Calendar.getInstance();
 		if (date != null)
 			cal.setTime(date);
 
 		mDate = date;
 		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
 		dateEdit.setText(df.format(cal.getTime()));
 	}
 	
 	private Date parseDateEdit()
 	{
 		Date date;
 		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
 		
 		try
 		{
 			date = df.parse(dateEdit.getText().toString());
 		}
 		catch (ParseException e)
 		{
 			// set the date to today if there's a parsing error
 			date = new Date();
 		}
 		date.setHours(0);
 		date.setMinutes(0);
 		date.setSeconds(0);
 		
 		return date;
 	}
 	
 	private int[] dateEditToYMD()
 	{
 		int[] ymd = new int[3];
 		Date date = parseDateEdit();
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		ymd[0] = cal.get(Calendar.YEAR);
 		ymd[1] = cal.get(Calendar.MONTH);
 		ymd[2] = cal.get(Calendar.DAY_OF_MONTH);
 		
 		return ymd;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		int[] ymd = dateEditToYMD();
 		return new DatePickerDialog(this, mDateSetListener, ymd[0], ymd[1], ymd[2]);
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog)
 	{
 		int[] ymd = dateEditToYMD();
 		((DatePickerDialog)dialog).updateDate(ymd[0], ymd[1], ymd[2]);
 	}
 
 	private DatePickerDialog.OnDateSetListener mDateSetListener =
         new DatePickerDialog.OnDateSetListener()
 		{
             public void onDateSet(DatePicker view, int year, int month,  int day)
             {
             	Calendar cal = Calendar.getInstance();
             	cal.set(Calendar.YEAR, year);
             	cal.set(Calendar.MONTH, month);
             	cal.set(Calendar.DAY_OF_MONTH, day);
             	setDateEdit(cal.getTime());
             }
         };
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (requestCode == REQ_REPEAT)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Bundle extras = data.getExtras();
 				mRepeat.iter = extras.getInt(RepeatSchedule.KEY_ITER);
 				mRepeat.freq = extras.getInt(RepeatSchedule.KEY_FREQ);
 				mRepeat.custom = extras.getInt(RepeatSchedule.KEY_CUSTOM);
 				mRepeat.end = new Date(extras.getLong(RepeatSchedule.KEY_DATE));
 				
 				setRepeatSpinnerSelection(mRepeat);
 			}
 			else
 			{
 				mDefaultRepeatValue = mLastRepeatValue;
 				if (mDefaultRepeatValue == 7)
 					setRepeatSpinnerSelection(mRepeat);
 			}
 		}
 		else if (requestCode == REQ_CAMERA)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				addImageRow(mImageUri);
 			}
 		}
 		else if (requestCode == REQ_GALLERY)
 		{
 			if (resultCode == RESULT_OK)
 			{
 				Uri uri = data.getData();
 				addImageRow(uri);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void setRepeatSpinnerSelection(RepeatSchedule repeat)
 	{
 		int spinner_num = 7;
 		
 		switch (repeat.iter)
 		{
 		case RepeatSchedule.NO_REPEAT:
 			spinner_num = 0;
 			break;
 		
 		case RepeatSchedule.DAILY:
 			if (repeat.freq == 1 && (repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 1;
 			break;
 		
 		case RepeatSchedule.WEEKLY:
 			if (repeat.freq == 1 && repeat.custom == 0 &&
 					(repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 2;
 			else if (repeat.freq == 2 && repeat.custom == 0 &&
 					(repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 3;
 			break;
 		
 		case RepeatSchedule.MONTHLY:
 			if (repeat.freq == 1 && repeat.custom == RepeatSchedule.DATE &&
 					(repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 4;
 			break;
 
 		case RepeatSchedule.YEARLY:
 			if (repeat.freq == 1 && (repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 5;
 			break;
 		}
 		
 		if (spinner_num == 7)
 		{
 			mRepeatAdapter = (ArrayAdapter<String>)repeatSpinner.getAdapter();
 			if (mRepeatAdapter.getCount() <= 7)
 				mRepeatAdapter.add("Custom");
 		}
 		
 		mDefaultRepeatValue = spinner_num;
 	}
 
 	private void showTransactionFields()
 	{
 		partyEdit = (AutoCompleteTextView)findViewById(R.id.partyEdit);
 		
 		// set the autocompletion values for partyEdit
 		String[] parties = Transaction.getAllParties();
 		if (parties == null)
 			parties = new String[0];
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, parties);
 		partyEdit.setAdapter(adapter);
 	}
 	
 	private ArrayAdapter<CharSequence> showTransferFields()
 	{
 		// if we're showing a transfer window, hide the check button, check field, and party field
 		//checkRadio.setVisibility(RadioButton.GONE);
 		
 		TableRow row = (TableRow)findViewById(R.id.partyRow);
 		row.setVisibility(TableRow.GONE);
 		//row = (TableRow)findViewById(R.id.checkRow);
 		//row.setVisibility(TableRow.GONE);
 		row = (TableRow)findViewById(R.id.accountRow);
 		row.setVisibility(TableRow.VISIBLE);
 		
 		accountSpinner = (Spinner)findViewById(R.id.accountSpinner);
 		String[] names = Account.getAccountNames();
 		
 		// if there is only one account in the database, tell the user they can't transfer and cancel
 		if (names.length == 1)
 		{
 			setResult(mFinishIntent);
 			finish();
 			return null;
 		}
 		
 		String[] acctNames = new String[names.length - 1];
 		
 		Account acct = Account.getAccountById(mAccountId);
 		int i = 0;
 		for ( String name : names )
 		{
 			if (!name.equalsIgnoreCase(acct.name))
 			{
 				acctNames[i++] = name;
 			}
 		}
 		
 		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
 				android.R.layout.simple_spinner_item, acctNames);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         accountSpinner.setAdapter(adapter);
         
         return adapter;
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		try
 		{
 			saveState();
 		}
 		catch (Exception e)
 		{
 			Logger.logStackTrace(e, this);
 		}
 	}
 
 	private void saveState() throws Exception
 	{
 		if (mFinishIntent == RESULT_CANCELED || mFinished)
 			return;
 
 		Object[] fields = parseFields();
 		if (fields != null)
 		{
 			if (mRepeatId == 0)
 			{
 				saveTransaction((Transaction)fields[0], (Account)fields[1]);
 			}
 			else
 			{
 				saveRepeat((Transaction)fields[0], (Account)fields[1]);
 			}
 		}
 
 		finish();
 	}
 	
 	// returns Object[] containing Transaction and Account
 	private Object[] parseFields()
 	{
 		Transaction trans;
 		Account acct2 = null;
 
 		if (mTransId != 0)
 			trans = mTrans;
 		else
 		{
 			trans = new Transaction();
 		}
 
 		if (mType == TransactionActivity.TRANSACTION)
 		{
 			trans.party = partyEdit.getText().toString();
 		}
 		else
 		{
 			acct2 = Account.getAccountByName((String)accountSpinner.getSelectedItem());
 		}
 		
 		if (mRepeatId != 0)
 		{
 			trans.account = Account.getAccountByName(
 					(String)repeatAccountSpinner.getSelectedItem()).id();
 		}
 		
 		// clear the list so we don't write tags leftover from loading the transaction
 		trans.tags.clear();
 		trans.addTags(tagsEdit.getText().toString());
 		
 		// get the date of the transaction and set time values to 0
 		trans.date = parseDateEdit();
 		
 		// get the amount of the transaction
 		try
 		{
 			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
 			char sep = dfs.getMonetaryDecimalSeparator();
 			
 			String str = amountEdit.getText().toString();
 			if (sep != '.')
 				str = str.replaceAll(String.valueOf(sep), ".");
 			trans.amount = new Double(str);
 		}
 		catch (NumberFormatException e)
 		{
 			trans.amount = 0.0;
 		}
 
 		// get the type of transaction
 		if (checkRadio.isChecked())
 		{
 			trans.type = Transaction.CHECK;
 
 			try
 			{
 				trans.check_num = new Integer(checkEdit.getText().toString());
 			}
 			catch (NumberFormatException e)
 			{
 				trans.type = Transaction.WITHDRAW;
 				trans.check_num = 0;
 			}
 		}
 		else if (withdrawRadio.isChecked())
 		{
 			trans.type = Transaction.WITHDRAW;
 		}
 		else
 		{
 			trans.type = Transaction.DEPOSIT;
 		}
 		
 		// get if it's a budget transaction
 		trans.budget = budgetRadio.isChecked();
 		if (trans.budget && trans.id() != -1 && trans.isPosted())
 		{
 			trans.post(false);
 			trans.budget = true; // set to true because trans.post() sets it to false
 		}
 
 		setRepeat();
 		
 		// add the images to the transaction
 		LinearLayout images = (LinearLayout)findViewById(R.id.imageLayout);
 		int count = images.getChildCount();
 		LinearLayout image_row;
 		Uri uri;
 		
 		for (int i = 0; i < count; ++i)
 		{
 			image_row = (LinearLayout)images.getChildAt(i);
 			uri = (Uri)image_row.getTag();
 			trans.addImage(uri);
 		}
 		
 		return new Object[]{trans, acct2};
 	}
 	
 	private void saveTransaction(Transaction trans, Account acct2)
 	{
 		int id = -1;
 		Intent broadcast = new Intent("net.gumbercules.loot.intent.ACCOUNT_UPDATED", null);
 		if (mType == TransactionActivity.TRANSACTION)
 		{
 			id = trans.write(mAccountId);
 		}
 		else
 		{
 			trans.account = mAccountId;
 			id = trans.transfer(acct2);
 			broadcast.putExtra("transfer_account", acct2.id());
 		}
 		
 		mFinished = true;
 		if (id != -1)
 		{
 			// write the repeat schedule if it's not set to NO_REPEAT
 			if (mRepeat.iter != RepeatSchedule.NO_REPEAT || mRepeat.id() > 0)
 			{
 				mRepeat.start = trans.date;
 				mRepeat.write(id);
 			}
 			
 			mTransId = id;
 			Intent i = new Intent();
 			Bundle b = new Bundle();
 			b.putInt(Transaction.KEY_ID, mTransId);
 			b.putInt(TransactionActivity.KEY_REQ, mRequest);
 			i.putExtras(b);
 			setResult(mFinishIntent, i);
 			
 			broadcast.putExtra("account_id", trans.account);
 			sendBroadcast(broadcast);
 		}
 		else
 		{
 			setResult(RESULT_CANCELED);
 		}
 	}
 	
 	private void saveRepeat(Transaction trans, Account acct2)
 	{
 		int id = -1;
 		mRepeat.trans = trans;
 		mRepeat.start = trans.date;
 		if (mType == TransactionActivity.TRANSACTION)
 		{
 			id = mRepeat.updateRepeat(trans.id(), false, false);
 		}
 		else
 		{
 			// update the party text to reflect the transfer if the account changed
 			// and update the transferred repeat
 			String detail1, detail2;
 			RepeatSchedule repeat2 = new RepeatSchedule(mRepeat);
 			repeat2.setId(RepeatSchedule.getRepeatId(mRepeat.getTransferId()));
 			repeat2.trans = new Transaction(mRepeat.trans, false);
 			repeat2.trans.setId(repeat2.getTransactionId());
 			repeat2.trans.account = acct2.id();
 			Account acct1 = Account.getAccountById(mRepeat.trans.account);
 			
 			if ( mRepeat.trans.type == Transaction.DEPOSIT )
 			{
 				detail1 = "from ";
 				detail2 = "to ";
 				repeat2.trans.type = Transaction.WITHDRAW;
 			}
 			else
 			{
 				detail1 = "to ";
 				detail2 = "from ";
 				repeat2.trans.type = Transaction.DEPOSIT;
 			}
 			
 			mRepeat.trans.party = "Transfer " + detail1 + acct2.name;
 			id = mRepeat.updateRepeat(trans.id(), false, false);
 			
 			repeat2.trans.party = "Transfer " + detail2 + acct1.name;
 			repeat2.updateRepeat(repeat2.trans.id(), false, false);
 		}
 		
 		mFinished = true;
 		if (id != -1)
 		{
 			setResult(mFinishIntent);
 		}
 		else
 		{
 			setResult(RESULT_CANCELED);
 		}
 	}
 	
 	private void setRepeat()
 	{
 		// set repeat values
 		switch (repeatSpinner.getSelectedItemPosition())
 		{
 		// No Repeat
 		case 0:
 			mRepeat.iter = RepeatSchedule.NO_REPEAT;
 			break;
 		
 		// Daily
 		case 1:
 			mRepeat.iter = RepeatSchedule.DAILY;
 			mRepeat.freq = 1;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 			
 		// Weekly
 		case 2:
 			mRepeat.iter = RepeatSchedule.WEEKLY;
 			mRepeat.freq = 1;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 			
 		// Bi-weekly
 		case 3:
 			mRepeat.iter = RepeatSchedule.WEEKLY;
 			mRepeat.freq = 2;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 			
 		// Monthly
 		case 4:
 			mRepeat.iter = RepeatSchedule.MONTHLY;
 			mRepeat.freq = 1;
 			mRepeat.custom = RepeatSchedule.DATE;
 			mRepeat.end = null;
 			break;
 			
 		// Yearly
 		case 5:
 			mRepeat.iter = RepeatSchedule.YEARLY;
 			mRepeat.freq = 1;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 		
 		// if it's past position 5, mRepeat has already been set
 		}
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		populateFields();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		outState.putInt(TransactionActivity.KEY_REQ, mRequest);
 		outState.putInt(TransactionActivity.KEY_TYPE, mType);
 		if (mAccountId > 0)
 		{
 			outState.putInt(Account.KEY_ID, mAccountId);
 		}
 		if (mTransId > 0)
 		{
 			outState.putInt(Transaction.KEY_ID, mTransId);
 		}
 		if (mRepeatId > 0)
 		{
 			outState.putInt(RepeatSchedule.KEY_ID, mRepeatId);
 		}
 		
 		setRepeat();
 		outState.putInt(RepeatSchedule.KEY_ITER, mRepeat.iter);
 		outState.putInt(RepeatSchedule.KEY_FREQ, mRepeat.freq);
 		outState.putInt(RepeatSchedule.KEY_CUSTOM, mRepeat.custom);
 		long end = (mRepeat.end == null ? 0 : mRepeat.end.getTime());
 		outState.putLong(RepeatSchedule.KEY_DATE, end);
 		
 		if (mDate != null)
 		{
 			outState.putLong(Transaction.KEY_DATE, mDate.getTime());
 		}
 		if (mType == TransactionActivity.TRANSFER)
 		{
 			outState.putInt(TransactionEdit.KEY_TRANSFER, accountSpinner.getSelectedItemPosition());
 		}
 		
 		if (mImageUri != null)
 		{
 			outState.putString("image_uri", mImageUri.toString());
 		}
 	}
 }
