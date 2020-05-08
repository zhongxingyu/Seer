 package com.voc4u.activity;
 
 import java.util.List;
 import java.util.Locale;
 
 import yuku.iconcontextmenu.IconContextMenu;
 import yuku.iconcontextmenu.IconContextMenuOnClickListener;
 
 import junit.framework.Assert;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Toast;
 
 import com.voc4u.R;
 import com.voc4u.activity.words.WordsItem;
 import com.voc4u.controller.PublicWord;
 import com.voc4u.controller.Word;
 import com.voc4u.controller.WordController;
 import com.voc4u.setting.CommonSetting;
 import com.voc4u.setting.LangSetting;
 import com.voc4u.ws.AddWord;
 import com.voc4u.ws.DeleteWord;
 
 public abstract class BaseWordActivity extends BaseActivity implements OnInitListener
 {
 	private static final String	TAG	= "VOC4UBaseWordActivity";
 	protected WordController	mWCtrl;
 	protected TextToSpeech mTts = null;
 	private MenuItem			mMenuHomeId;
 	private Word mSelectedWord;
 	private IconContextMenu iconContextMenu;
 	private WordsItem mSelectedWordItem;
 
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(getContentView());
 
 		mWCtrl = WordController.getInstance(this);
 		
 		
 		Resources res = getResources();
 		iconContextMenu = new IconContextMenu(this);
         iconContextMenu.addItem(res, res.getString(R.string.ctx_menu_play), R.drawable.ic_menu_play_clip, WordsItem.PLAY);
         iconContextMenu.addItem(res, res.getString(R.string.ctx_menu_edit), R.drawable.ic_menu_edit, WordsItem.EDIT);
         iconContextMenu.addItem(res, res.getString(R.string.ctx_menu_delete), R.drawable.ic_menu_delete, WordsItem.DELETE);
  
         //set onclick listener for context menu
         iconContextMenu.setOnClickListener(new IconContextMenuOnClickListener() {
             @Override
             public void onClick(int menuId) 
             {
             	if(mSelectedWordItem != null)
             	{
             		mSelectedWord = mSelectedWordItem.getWord();
             		switch(menuId)
             		{
             		case WordsItem.PLAY:
             			onPlay(mSelectedWord.getLern());
             			break;
             		case WordsItem.EDIT:
             			showDialog(BaseActivity.DIALOG_EDIT_WORD);
             			break;
             		case WordsItem.DELETE:
             		{
             			 new AlertDialog.Builder(BaseWordActivity.this)
             		        .setIcon(android.R.drawable.ic_dialog_alert)
             		        .setTitle(R.string.ctx_menu_delete)
             		        .setMessage(R.string.confirm_you_want_realy_delete)
             		        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 
             		            @Override
             		            public void onClick(DialogInterface dialog, int which) {
             		            	removeFromList(mSelectedWord);
                         			mWCtrl.removeWord(mSelectedWord);
                         			doRedrawList();
             		                   
             		            }
 
             		        })
             		        .setNegativeButton(android.R.string.no, null)
             		        .show();
             			
             		}
             			break;
             		}
             		mSelectedWordItem = null;
             	}
                 
             }
         });
         
        
         
 	}
 
 	protected abstract int getContentView();
 
 	public boolean isConnected()
 	{
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		TelephonyManager mTelephony = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
         NetworkInfo info = cm.getActiveNetworkInfo();
         if(info != null)
         {
         	int netType = info.getType();
         	int netSubtype = info.getSubtype();
 			 if (netType == ConnectivityManager.TYPE_WIFI || (netType == ConnectivityManager.TYPE_MOBILE
 			    && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
 			    && !mTelephony.isNetworkRoaming())) 
 			{
 			        return info.isConnected();
 			} 
 		}
         
 		return false;
 		
 	}
 
 	@Override
 	public void onResumeSuccess() 
 	{
 		super.onResumeSuccess();
 		if(mTts == null)
 		{
 			mTts = new TextToSpeech(this, this);
 		}
 		
 		if(isConnected())
 		{
 			new AddWord(mWCtrl);
 			new DeleteWord(mWCtrl);
 		}
 	}
 
 
 	@Override
 	public void onInit(int status)
 	{
 		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
 		if (status == TextToSpeech.SUCCESS)
 		{
 			// Set preferred language to US english.
 			// Note that a language may not be available, and the result will
 			// indicate this.
 			Locale loc = CommonSetting.lernCode.toLocale();
 			int result = mTts.setLanguage(loc);
 			
 			if(result == TextToSpeech.LANG_NOT_SUPPORTED)
 			{
 				Log.e(TAG, "Language is not available. code: " + loc.getLanguage());
 				//showDialog(BaseActivity.DIALOG_TTS_LANGUAGE_MISSING);
 				//result = mTts.setLanguage(Locale.ENGLISH);
 				
 			}
 			
 			if (result == TextToSpeech.LANG_MISSING_DATA)
 			{
 				// Lanuage data is missing or the language is not supported.
 				Log.e(TAG, "Language is not available.");
 				//showDialog(BaseActivity.DIALOG_TTS_DATA_MISSING);
 			} 
 			else
 			{
 				// Check the documentation for other possible result codes.
 				// For example, the language may be available for the locale,
 				// but not for the specified country and variant.
 
 				// The TTS engine has been successfully initialized.
 				// Allow the user to press the button for the app to speak
 				// again.
 				// mAgainButton.setEnabled(true);
 				// Greet the user.
 				// sayHello();
 			}
 		}
 		else
 		{
 			// Initialization failed.
 			Log.e(TAG, "Could not initialize TextToSpeech.");
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		mMenuHomeId = menu.add(R.string.btn_menu_home).setOnMenuItemClickListener(this);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public void onDestroy()
 	{
 		// Don't forget to shutdown!
 		if (mTts != null)
 		{
 			mTts.stop();
 			mTts.shutdown();
 		}
 
 		super.onDestroy();
 	}
 
 	public void onPlay(String text)
 	{
 		Assert.assertTrue(mTts != null && text != null && text.length() > 0);
 		if (mTts != null && text != null && text.length() > 0)
 		{
 			mTts.speak(text, TextToSpeech.QUEUE_FLUSH, // Drop all pending
 														// entries in the
 														// playback queue.
 			null);
 		}
 	}
 
 	@Override
 	public boolean onMenuItemClick(MenuItem item)
 	{
 		if (item == mMenuHomeId)
 		{
 			finish();
 			return true;
 		}
 		else
 			return super.onMenuItemClick(item);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) 
 	{
 		Dialog dialog = null;
 		switch(id)
 		{
 			case BaseActivity.DIALOG_TTS_DATA_MISSING:
 			{
 				dialog = ShowDialogForTtsSetting(R.string.msg_tts_data_missing);
 				break;
 			}
 			case BaseActivity.DIALOG_EDIT_WORD:
 			{
 				dialog = createDialogAddWord(new OnWordAdd() {
 					
 					@Override
 					public long onWordAdd(WordController wc, String learn, String nativ) {
 						doRedrawList();
 						if(mSelectedWord != null)
 						{
 							mSelectedWord.setLearn(learn);
 							mSelectedWord.setNative(nativ);
 							wc.updateWord(mSelectedWord.getId(), mSelectedWord.getLern(), mSelectedWord.getNative());
 							
 							return mSelectedWord.getId();
 						}
 						return 0;
 					}
 
 					
 				});
 				break;
 			}
			case 1564:
 				if(mSelectedWordItem != null)
 				{
 					dialog = iconContextMenu.createMenu(mSelectedWordItem.getWord().getLern());
 				}
 				break;
 			default:
 				return super.onCreateDialog(id);
 		}
 		
 		return dialog;
 	}
 	
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 //		if (id == DIALOG_ADD_WORD) {
 //			final EditText edtNative = (EditText) dialog
 //					.findViewById(R.id.edtNative);
 //			final EditText edtLern = (EditText) dialog
 //					.findViewById(R.id.edtLearn);
 //			edtLern.setText("hello");
 //			edtNative.setText("ahoj");
 //		}
 //		else 
 			if(id == DIALOG_EDIT_WORD)
 		{
 			final EditText edtNative = (EditText) dialog
 					.findViewById(R.id.edtNative);
 			final EditText edtLern = (EditText) dialog
 					.findViewById(R.id.edtLearn);
 			if(mSelectedWord != null)
 			{
 				edtLern.setText(mSelectedWord.getLern());
 				edtNative.setText(mSelectedWord.getNative());
 			}
 		}
 		else if (id == DIALOG_SHOW_INFO) {
 			DialogInfo.setup(this, GetShowInfoType(), dialog);
 		} else
 			super.onPrepareDialog(id, dialog);
 	}
 
 	public void doRedrawList() 
 	{
 		
 	}
 	
 	public Dialog ShowDialogForTtsSetting(int message) 
 	{
 		Dialog dialog;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.form_dashboard);
 		builder.setIcon(android.R.drawable.ic_dialog_alert);
 		builder.setMessage(message);
 		builder.setCancelable(true);
 		builder.setPositiveButton(
 		this.getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener()
 		{
 			public void onClick(DialogInterface dialog, int id)
 			{
 				showTtsSetting();
 			}
 		}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
 		{
 			public void onClick(DialogInterface dialog, int id)
 			{
 				dialog.cancel();
 			}
 		});
 		dialog = builder.create();
 		return dialog;
 	}
 	
 	private void showTtsSetting() 
 	{
 		getIntent().putExtra("onShowSpeechMenu", true);
 		if(mTts != null)
 		{
 			mTts.shutdown();
 			mTts = null;
 		}
 		onShowSpeechMenu();
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// TODO Auto-generated method stub
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		mSelectedWordItem = ((WordsItem)((AdapterContextMenuInfo)menuInfo).targetView);
 		//((WordsItem)((AdapterContextMenuInfo)menuInfo).targetView).createMenu(menu);
 		//IconContextMenu icm = new IconContextMenu(this.getApplicationContext(), menu);
 		
 		 showDialog(DIALOG_CONTEXT_MENU);
 	}
 	
 	
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item)
 	{
 		WordsItem iw = (WordsItem)((AdapterContextMenuInfo)item.getMenuInfo()).targetView;
 		
 		
 		// for on dialog prepare
 		mSelectedWord = iw.getWord();
 		
 		int i = item.getItemId();
 		switch(i)
 		{
 		case WordsItem.PLAY:
 			onPlay(mSelectedWord.getLern());
 			break;
 		case WordsItem.EDIT:
 			showDialog(BaseActivity.DIALOG_EDIT_WORD);
 			break;
 		case WordsItem.DELETE:
 			removeFromList(mSelectedWord);
 			mWCtrl.removeWord(mSelectedWord);
 			doRedrawList();
 			break;
 		default:
 			return super.onContextItemSelected(item);
 		}
 		return true;
 	}
 	
 	/**
 	 * remove from last list
 	 * @param w
 	 */
 	protected void removeFromList(Word w)
 	{
 		List<PublicWord>lastlist = mWCtrl.getLastList();
 		
 		if(lastlist.size() < 1)
 		{
 			return ;
 		}
 		
 		int i = 0;
 		long[] ids = mWCtrl.getLastListIds();
 		
 		
 		
 		for(long id : ids)
 		{
 			if(id == w.getId())
 			{
 				
 				lastlist.remove(i);
 				break;
 			}
 			i++;
 		}
 		
 	}
 	
 	
 }
