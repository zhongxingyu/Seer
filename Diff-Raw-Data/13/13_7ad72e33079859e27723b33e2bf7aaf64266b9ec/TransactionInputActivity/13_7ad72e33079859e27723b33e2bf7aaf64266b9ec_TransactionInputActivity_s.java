 package pl.edu.uj.portfel.transaction;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import pl.edu.uj.portfel.ErrorReporter;
 import pl.edu.uj.portfel.R;
 import pl.edu.uj.portfel.WPAsyncTask;
 import pl.edu.uj.portfel.camera.CameraPhotoActivity;
 import pl.edu.uj.portfel.camera.PhotoPreviewActivity;
 import pl.edu.uj.portfel.db.AccountDao;
 import pl.edu.uj.portfel.db.AttributeDao;
 import pl.edu.uj.portfel.db.Database;
 import pl.edu.uj.portfel.db.TransactionDao;
 import pl.edu.uj.portfel.microphone.AudioPlayerActivity;
 import pl.edu.uj.portfel.microphone.AudioRecorder;
 import pl.edu.uj.portfel.microphone.AudioRecorderActivity;
 import pl.edu.uj.portfel.server.Server;
 import pl.edu.uj.portfel.transaction.attributes.audio.AudioTransactionAttribute;
 import pl.edu.uj.portfel.transaction.attributes.image.ImageTransactionAttribute;
 import pl.edu.uj.portfel.transaction.attributes.text.InputActivity;
 import pl.edu.uj.portfel.transaction.attributes.text.TextTransactionAttribute;
 import pl.edu.uj.portfel.utils.ChoiceActivatedClosure;
 import pl.edu.uj.portfel.utils.ChoiceList;
 import pl.edu.uj.portfel.utils.Currency;
 import pl.edu.uj.portfel.utils.StringUtils;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TransactionInputActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, ErrorReporter {
 	private AudioRecorder recorder;
 	private Server server;
 	private ViewHolder holder;
 	private Database db;
 	private String serverToken;
 	private long accId;
 	private long prevTid;
 	private long timestamp;
 	private long rid;
 	private TransactionType type;
 	private List<TransactionAttribute> attributes;
 	private TransactionAttributeListViewAdapter listAdapter;
 	private boolean update;
 	private PopupWindow menuPopup;
 	
 	private TextTransactionAttribute textAttributeUnderEdition;
 	
 	public enum TransactionType {
 		EXPENSE, EARNING
 	}
 	
 	long amount;
 	
 	class ViewHolder {
 		public TextView plusBtn;
 		public TextView minusBtn;
 		public TextView cashValue;
 	}
 	
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.transaction);
 		
 		server = new Server();
 		db = new Database(this, this);
 		
 		attributes = new ArrayList<TransactionAttribute>();
 		Currency currency = new Currency();
 		
 		Bundle args = getIntent().getExtras();
 		if(args.containsKey("LOAD_TRANSACTION_ID")) {
 			update = true;
 			loadAttributes(db, args);
 		} else
 			update = false;
 		
 		serverToken = args.getString("SERVER_TOKEN");
 		
 		holder = new ViewHolder();
 		holder.cashValue = (TextView) findViewById(R.id.cashValue);
 		holder.minusBtn = (TextView) findViewById(R.id.transactionMinusBtn);
 		holder.plusBtn = (TextView) findViewById(R.id.transactionPlusBtn);
 
 		long cash = args.getLong("CASH");
 		amount = cash;
 
 		accId = args.getLong("ACCOUNT_ID");
 		holder.cashValue.setText(StringUtils.getCanonicalCashValue(cash, currency));
 		
 		ListView list = (ListView) findViewById(R.id.transactionAttributeList);
 		listAdapter = new TransactionAttributeListViewAdapter(this, R.layout.transaction_item, attributes);
 		list.setAdapter(listAdapter);
 		list.setOnItemClickListener(this);
 		list.setOnItemLongClickListener(this);
 		
 		updateTransactionType();
 	}
 	
 	public void loadAttributes(Database db, Bundle args) {
 		long tid = args.getLong("LOAD_TRANSACTION_ID");
 		prevTid = tid;
 		
 		long[] ids = db.getAttributeIds(tid);
 		for(long aid: ids) {
 			AttributeDao adao = db.getAttributeById(aid);
 			TransactionAttribute attr = adao.createTransactionAttribute();
 			
 			attributes.add(attr);
 		}
 		
 		TransactionDao tdao = db.getTransactionById(tid);
 		type = tdao.getTypeObj();
 		timestamp = tdao.getTimestamp();
 		rid = tdao.getRid();
 		Log.d("ts", "loaded id=" + tid + ", timestamp=" + timestamp);
 		
 		// can't update gui now, because Holder doesn't exist yet
 		// updateTransactionTypeGui();
 	}
 
 	public void recordClicked(View v) {
 //		try {
 //			if (recorder == null) {
 //				recorder = new AudioRecorder("/w-portfel/test.3gp");
 //				recorder.start();
 //				
 //				Toast.makeText(this, "recording start", Toast.LENGTH_SHORT).show();
 //			} else {
 //				recorder.stop();
 //				Toast.makeText(this, "recording stop", Toast.LENGTH_SHORT).show();
 //			}
 //		} catch (IOException e) {
 //			try {
 //				if(recorder != null)
 //					recorder.stop();
 //			} catch(IOException ex) {
 //				
 //			}
 //		}
 		
 		Intent i = new Intent(this, AudioRecorderActivity.class);
 		startActivityForResult(i, 4);
 	}
 	
 	public void shotClicked(View v) {
 		Intent i = new Intent(this, CameraPhotoActivity.class);
 		startActivityForResult(i, 3);
 	}
 	
 	public void addTextAttribute(View v) {
 		Intent transactionIntent = new Intent(this, InputActivity.class);
 		startActivityForResult(transactionIntent, 1);
 	}
 	
 	public void editTextAttribute(TextTransactionAttribute attr) {
 		Intent transactionIntent = new Intent(this, InputActivity.class);
 		transactionIntent.putExtra("INITIAL_CAPTION", attr.getCaption());
 		transactionIntent.putExtra("INITIAL_DESCRIPTION", attr.getDescription());
 		textAttributeUnderEdition = attr;
 		startActivityForResult(transactionIntent, 2);
 	}
 	
 	public void previewImageAttribute(ImageTransactionAttribute attr) {
 		Intent intent = new Intent(this, PhotoPreviewActivity.class);
 		intent.putExtra("FILENAME", attr.getFilename());
 		startActivityForResult(intent, 99);
 	}
 	
 	public void playAudioAttribute(AudioTransactionAttribute attr) {
 		Intent intent = new Intent(this, AudioPlayerActivity.class);
 		intent.putExtra("FILENAME", attr.getFilename());
 		startActivityForResult(intent, 99);
 	}
 	
 	private int countTextAttributes() {
 		int count = 0;
 		
 		for(TransactionAttribute ta: attributes) {
 			if(ta instanceof TextTransactionAttribute)
 				count++;
 		}
 		
 		return count;
 	}
 	
 	private TextTransactionAttribute getFirstTextTransactionAttribute() {
 		for(TransactionAttribute ta: attributes) {
 			if(ta instanceof TextTransactionAttribute)
 				return (TextTransactionAttribute) ta;
 		}
 		
 		return null;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(data == null)
 			return;
 		
 		if(requestCode == 1 && resultCode == RESULT_OK) {
 			// Added a new attribute
 			String caption = data.getCharSequenceExtra("CAPTION").toString();
 			String description = data.getCharSequenceExtra("DESCRIPTION").toString();
 			
 			TextTransactionAttribute attr = new TextTransactionAttribute(caption, description);
 			
 			attributes.add(attr);
 			listAdapter.notifyDataSetChanged();
 			
 			if(countTextAttributes() == 1)
 				attr.setTitle(true);
 			
 			saveTransaction();
 		} else if(requestCode == 2 && resultCode == RESULT_OK) {
 			// Edited an existing text attribute
 			
 			textAttributeUnderEdition.setCaption(data.getCharSequenceExtra("CAPTION").toString());
 			textAttributeUnderEdition.setDescription(data.getCharSequenceExtra("DESCRIPTION").toString());
 
 			listAdapter.notifyDataSetChanged();
 
 			if(countTextAttributes() == 1)
 				getFirstTextTransactionAttribute().setTitle(true);
 			
 			saveTransaction();
 		} else if(requestCode == 3 && resultCode == RESULT_OK) {
 			String filename = data.getCharSequenceExtra("FILENAME").toString();
 			ImageTransactionAttribute attr = new ImageTransactionAttribute(filename);
 			
 			attributes.add(attr);
 			listAdapter.notifyDataSetChanged();
 			
 			saveTransaction();
 		} else if(requestCode ==4 && resultCode == RESULT_OK) {
 			String filename = data.getCharSequenceExtra("FILENAME").toString();
 			AudioTransactionAttribute attr = new AudioTransactionAttribute(filename);
 			
 			attributes.add(attr);
 			
 			listAdapter.notifyDataSetChanged();
 			saveTransaction();
 		}
 	}
 
 	public void updateTransactionType() {
 		if(type == TransactionType.EARNING) {
 			holder.plusBtn.setBackgroundColor(Color.GREEN);
 			holder.minusBtn.setBackgroundColor(Color.BLACK);
 		} else if(type == TransactionType.EXPENSE) {
 			holder.minusBtn.setBackgroundColor(Color.RED);
 			holder.plusBtn.setBackgroundColor(Color.BLACK);
 		}
 		
 		saveTransactionType();
 	}
 	
 	public void plusBtnClick(View v) {
 		type = TransactionType.EARNING;
 		updateTransactionType();
 	}
 	
 	public void minusBtnClick(View v) {
 		type = TransactionType.EXPENSE;
 		updateTransactionType();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int idx, long arg3) {
 		attributeClicked(attributes.get(idx));
 	}
 	
 	private void attributeClicked(TransactionAttribute attr) {
 		if(attr instanceof TextTransactionAttribute) {
 			editTextAttribute((TextTransactionAttribute) attr);
 		} else if(attr instanceof ImageTransactionAttribute) {
 			previewImageAttribute((ImageTransactionAttribute) attr);
 		} else if(attr instanceof AudioTransactionAttribute) {
 			playAudioAttribute((AudioTransactionAttribute) attr);
 		}
 	}
 	
 	private void clearTextMarks() {
 		// db.clearTextMarksForTid(prevTid);
 		for(TransactionAttribute attr: attributes) {
 			if(attr instanceof TextTransactionAttribute) {
 				TextTransactionAttribute tattr = (TextTransactionAttribute) attr;
 				tattr.setTitle(false);
 			}
 		}
 	}
 	
 	private boolean markTextAttribute(TextTransactionAttribute attr) {
 		boolean ret = false;
 
 		clearTextMarks();
 		attr.setTitle(true);
 		saveTransaction();
 		ret = true;
 		
 		return ret;
 	}
 	
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		ChoiceList clist = new ChoiceList(this, getString(R.string.select_action));
 		final TransactionAttribute attr = attributes.get(arg2);
 		final int cIdx = arg2;
 		
 		if(attr instanceof TextTransactionAttribute) {
 			clist.add(getString(R.string.transaction_attribute_popup_edit), new ChoiceActivatedClosure() {
 				@Override
 				public void actionPerformed(DialogInterface dialog, int which) {
 					attributeClicked(attr);
 				}
 			});
 		}
 		
 		if(attr instanceof ImageTransactionAttribute) {
 			clist.add(getString(R.string.transaction_attribute_popup_preview), new ChoiceActivatedClosure() {
 				@Override
 				public void actionPerformed(DialogInterface dialog, int which) {
 					attributeClicked(attr);
 				}
 			});
 		}
 		
 		if(attr instanceof AudioTransactionAttribute) {
 			clist.add(getString(R.string.transaction_attribute_popup_play), new ChoiceActivatedClosure() {
 				@Override
 				public void actionPerformed(DialogInterface dialog, int which) {
 					attributeClicked(attr);
 				}
 			});
 		}
 		
 		if(attr instanceof TextTransactionAttribute) {
 			clist.add(getString(R.string.transaction_attribute_popup_mark), new ChoiceActivatedClosure() {
 				@Override
 				public void actionPerformed(DialogInterface dialog, int which) {
 					if(markTextAttribute((TextTransactionAttribute) attr))
 						reportInfo(getString(R.string.transaction_attribute_selected_as_title));
 				}
 			});
 		}
 		
 		clist.add(getString(R.string.transaction_attribute_popup_delete), new ChoiceActivatedClosure() {
 			@Override
 			public void actionPerformed(DialogInterface dialog, int which) {
 				attributes.remove(cIdx);
 				listAdapter.notifyDataSetChanged();
 				saveTransaction();
 			}
 		});
 		
 		clist.run();
 		return false;
 	}
 	
 	@Override
 	public void onBackPressed() {
 		finishAndSaveClickedLogic();
 	}
 	
 	public void finishAndSaveClickedLogic() {
 		if(saveTransaction()) {
 			Intent map = new Intent();
 			setResult(RESULT_OK, map);
 			finish();
 		}
 	}
 	
 	public void finishAndSaveClicked(View v) {
 		reportInfo("Zapisywanie transakcji");
 		finishAndSaveClickedLogic();
 	}
 	
 	public boolean saveTransaction() {
 		if(type == null) {
 			reportInfo("Wybierz typ transakcji!");
 			return false;
 		}
 		
 		TransactionDao dao = new TransactionDao();
 		dao.setType(type);
 		dao.setAmount(amount);
 		dao.setAccId(accId);
 		dao.setRid(rid);
 		
 		long tid = 0;
 		
 		if(update) {
 			db.removeTransaction(prevTid);
 			dao.setTimestamp(timestamp);
 		} else {
 			Date now = new Date();
 			dao.setTimestamp(now.getTime());
 		}
 		
 		db.insertTransaction(dao);
 		tid = dao.getId();
 		
 		ArrayList<AttributeDao> attributesToStore = new ArrayList<AttributeDao>();
 		
 		for(TransactionAttribute attr: attributes) {
 			AttributeDao adao = AttributeDao.fromAttributeObj(tid, attr);
 			attributesToStore.add(adao);
 		}
 		
 		db.removeAttributesForTid(tid);
 		for(AttributeDao a: attributesToStore) {
 			db.writeAttribute(a);
 		}
 		
 		prevTid = tid;
 		rid = dao.getRid();
 		
 		if(! update)
 			update = true;
 		
 		syncToServer();
 		return true;
 	}
 	
 	public String createAttributeBlob(TransactionDao dao) {
 		try {
 			long[] ids = db.getAttributeIds(dao.getId());
 			JSONArray arr = new JSONArray();
 			
 			for(long id: ids) {
 				AttributeDao attrDao = db.getAttributeById(id);
 				if(attrDao.getTypeObj() == AttributeDao.AttributeType.TEXT) { 
 					JSONObject obj = new JSONObject();
 					
 					obj.put("aux1", attrDao.getAux1());
 					obj.put("aux2", attrDao.getAux2());
 					obj.put("aux3", attrDao.getAux3());
 					obj.put("type", attrDao.getType());
 					obj.put("title", attrDao.getTitle());
 					
 				arr.put(obj);
 				}
 			}
 			
 			return arr.toString();
 		} catch(JSONException e) {
 			return "";
 		}
 	}
 	
 	public void syncToServer() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Czekaj");
        progress.setCancelable(false);
        progress.setMessage("Zapisywanie...");
        progress.show();
         
 		WPAsyncTask task = new WPAsyncTask(this, server) {
 			@Override
 			public void onFinished() {
				runOnUiThread(new Runnable() { public void run() { progress.dismiss(); } });
 			}
 
 			@Override
 			public void run() {
 				TransactionDao tdao = db.getTransactionById(prevTid);
 				if(tdao.getRid() == 0) {
 					AccountDao adao = db.getAccountById(accId);
 					
 					String blob = createAttributeBlob(tdao);
 					if(blob.length() > 0) {
 						tdao.setRid(server.createTransaction(serverToken, tdao, adao.getRemoteId(), blob));
 						db.updateTransaction(tdao);
 						rid = tdao.getRid();
 					}
 				}
 				
 				onFinished();
 			}
 		};
 		
 		new Thread(task).start();
 	}
 	
 	public void saveTransactionType() {
 		if(prevTid <= 0)
 			return;
 		
 		TransactionDao tdao = db.getTransactionById(prevTid);
 		tdao.setType(type);
 		db.updateTransaction(tdao);
 		
 		syncToServer();
 	}
 
 	@Override
 	public void reportError(String str) {
 		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void reportInfo(String str) {
 		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
 	}
 }
