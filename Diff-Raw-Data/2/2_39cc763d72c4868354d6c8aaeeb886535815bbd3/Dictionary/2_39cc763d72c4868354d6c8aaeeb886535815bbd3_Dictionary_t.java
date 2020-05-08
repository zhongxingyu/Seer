 package com.voc4u.activity.dictionary;
 
 import java.util.ArrayList;
 
 import junit.framework.Assert;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.voc4u.R;
 import com.voc4u.activity.BaseActivity;
 import com.voc4u.activity.BaseWordActivity;
 import com.voc4u.activity.dashboard.Dashboard;
 import com.voc4u.activity.train.LastItem;
 import com.voc4u.controller.EPoliticy;
 import com.voc4u.controller.PublicWord;
 import com.voc4u.controller.Word;
 import com.voc4u.controller.WordController;
 import com.voc4u.setting.CommonSetting;
 import com.voc4u.setting.LangSetting;
 import com.voc4u.widget.CommonDialogs;
 
 public class Dictionary extends BaseWordActivity implements OnClickListener, OnItemClickListener
 {
 
 	private WordController		mWordCtrl;
 	private ListView			mList;
 	private View				btnStoreSetting;
 	private Adapter				mAdapter;
 	private MenuItem	menuReset;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		mWordCtrl = WordController.getInstance(this);
 
 		mAdapter = new Adapter();
 
 		mList = (ListView) findViewById(R.id.list);
 		mList.setAdapter(mAdapter);
 		mList.setOnItemClickListener(this);
 		
 		
 		//mList.setClickable(true);
 		
 		//if (CommonSetting.DEBUG)
 		{
 			// findViewById(R.id.addword).setVisibility(View.VISIBLE);
 			btnStoreSetting = findViewById(R.id.btnStoreSetting);
 			btnStoreSetting.setOnClickListener(this);
 		}
 
 		btnStoreSetting = findViewById(R.id.btnStoreSetting);
 		btnStoreSetting.setOnClickListener(this);
 	}
 
 	@Override
 	protected void onResume()
 	{
 		// FIXME: close dialog when mWordCtrl is finish
 		// if still runing the async task
 		// isn't posible changing anything
 		if (mWordCtrl.isAsyncRunning())
 		{
 			showDialog(BaseActivity.DIALOG_PROGRESS);
 		}
 
 		super.onResume();
 	}
 
 	@Override
 	public void onBackPressed()
 	{
 		// TODO: move this to special button for save
 		// TODO: show information about changes and leaving without this changes
 
 		// super is called in store() -> showDialogAboutDurationOfOperation() ->
 		// "YES"
 		if(!CommingFromInit())
 			super.onBackPressed();
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 	}
 
 	private void store()
 	{
 		boolean anyChanges = false;
 		boolean anyChecked = false;
 
 		for (int i = 0; i != mAdapter.getLessonCount(); i++)
 		{
 			ItemView item = mAdapter.getLessonItem(i);
 
 			Assert.assertNotNull(item);
 			if (item != null)
 			{
 				ItemStatus is = item.getStatus();
 				if (is != ItemStatus.NONE)
 				{
 					// because 0 is for user owned words
 					mWordCtrl.enableLessonAsync(item.getLesson(), is == ItemStatus.ADD);
 					anyChanges = true;
 				}
 
 				if (item.isChecked())
 					anyChecked = true;
 			}
 		}
 
 		if (!anyChecked)
 		{
 			showDialog(BaseActivity.DIALOG_MUST_CHECK_AT_LEAST_ONE);
 			// showDialogAboutMustCheckAtleasOneItem();
 		}
 		else if (!CommingFromInit() && anyChanges)
 		{
 			showDialog(BaseActivity.DIALOG_CONFIRM_CONTINUE_SAVE_SETTING);
 			// showDialogAboutDurationOfOperation();
 		}
 		else
 		{
			superOnBackPresed();
 		}
 	}
 
 	private void ShowDashboardOrFinish()
 	{
 		// show dashboard because comming from init
 		// and before init was the dashboard finished
 		if(CommingFromInit())
 		{
 			Intent dashboard = new Intent(this, Dashboard.class);
 			startActivity(dashboard);
 		}
 		
 		finish();
 	}
 
 	private boolean CommingFromInit()
 	{
 		Intent intent = getIntent();
 		
 		return intent.hasExtra(FROM_INIT);
 	}
 
 	protected void superOnBackPresed()
 	{
 		mWordCtrl.runAsyncTask();
 		// without sleep is the word setting returned back
 		// because isn't load any word between 
 		// finish() and resume() new activity
 		try
 		{
 			Thread.sleep(500);
 		}
 		catch (InterruptedException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		ShowDashboardOrFinish();
 	}
 
 	public class Adapter implements ListAdapter
 	{
 		private static final int	NUM_ADAPTING	= 50;
 		private static final int	VOCABULARY_TYPE	= 0;
 		private static final int	SETTING_TYPE	= 1;
 		private static final int 	CUSTOM_WORDS_TYPE = 2;
 		final private int			mLessonNum;
 		final ItemView[]			mLessons;
 		private int					mLastItem		= 0;
 
 		public ArrayList<Word>	mCustomWords;
 
 		public Adapter()
 		{
 			mLessonNum = LangSetting.LESSON_SIZES.length;
 			mLessons = new ItemView[mLessonNum];
 			mCustomWords = mWordCtrl.getWordsInLesson(WordController.CUSTOM_WORD_LESSON);
 			
 			if(mCustomWords == null)
 				mCustomWords = new ArrayList<Word>();
 			
 			mLastItem = 0;
 		}
 
 		@Override
 		public boolean areAllItemsEnabled()
 		{
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isEnabled(int position)
 		{
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public int getCount()
 		{
 			
 			// in list is lessons
 			// setting
 			// custom words
 			return mLessonNum + 1 + mCustomWords.size();
 		}
 
 		public int getLessonCount()
 		{
 			return mLessonNum;
 		}
 
 		ItemView getLessonItem(int position)
 		{
 			if (mLessonNum > position)
 				return mLessons[position];
 
 			return null;
 		}
 
 		@Override
 		public ItemView getItem(int position)
 		{
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position)
 		{
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getItemViewType(int position)
 		{
 			if (position == mLessonNum)
 				return SETTING_TYPE;
 			else if(position < mLessonNum)
 				return VOCABULARY_TYPE;
 			else
 				return CUSTOM_WORDS_TYPE;	
 		}
 
 		@Override
 		public View getView(int position, View convertView, final ViewGroup parent)
 		{
 			switch(getItemViewType(position))
 			{
 				case VOCABULARY_TYPE:
 					return createWordView(position, convertView);
 				case SETTING_TYPE:
 					return createSettingView(convertView);
 				case CUSTOM_WORDS_TYPE:
 				{
 					return createCustomWord(convertView, position);
 				}
 			}
 			return convertView;
 		}
 
 		private View createCustomWord(View convertView, int position)
 		{
 			int numItemsBeforeCW = mLessonNum + 1;
 			int absolutePosition = position - numItemsBeforeCW;
 			
 			
 			//if(convertView == null)
 			PublicWord pw = new PublicWord(mCustomWords.get(absolutePosition), EPoliticy.PRIMAR);
 			convertView = new LastItem(Dictionary.this, pw);
 			convertView.setOnCreateContextMenuListener(Dictionary.this);
 			//convertView.setClickable(true);
 			//convertView.setOnClickListener(WordSetting.this);
 			return convertView;
 		}
 
 		private View createSettingView(View convertView)
 		{
 			// SettingItemView setting;
 
 			if (convertView == null)
 			{
 				convertView = new SettingItemView(Dictionary.this);
 				((SettingItemView) convertView).setup();
 			}
 
 			return convertView;
 		}
 
 		public View createWordView(int position, View convertView)
 		{
 			ItemView item;
 
 			// it suppose the lesson is the first in list
 			final int lesson = position + 1;
 
 			if (convertView == null)
 			{
 				item = new ItemView(Dictionary.this, mWordCtrl);
 				if(position < mLessons.length)
 					mLessons[position] = item;
 			}
 			else
 				item = (ItemView) convertView;
 
 			item.setup(lesson);
 
 			return item;
 		}
 
 		@Override
 		public int getViewTypeCount()
 		{
 			// TODO Auto-generated method stub
 			return 3;
 		}
 
 		@Override
 		public boolean hasStableIds()
 		{
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isEmpty()
 		{
 			return false;
 		}
 
 		@Override
 		public void registerDataSetObserver(DataSetObserver observer)
 		{
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void unregisterDataSetObserver(DataSetObserver observer)
 		{
 			// TODO Auto-generated method stub
 
 		}
 
 		public void addCustomWord(Word word)
 		{
 			mCustomWords.add(word);
 		}
 
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		if (v == btnStoreSetting)
 		{
 			// showDialog(101);
 			CommonSetting.store(this);
 			store();
 		}
 	}
 
 	public void onBtnAddWord(View v)
 	{
 		showDialog(BaseActivity.DIALOG_ADD_WORD);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		final Dialog dialog;
 		switch (id)
 		{
 			case BaseActivity.DIALOG_CONFIRM_CONTINUE_SAVE_SETTING:
 			{
 				dialog = CommonDialogs.confirmDictionarySetting(this, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						superOnBackPresed();
 					}
 				});
 //				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 //				builder.setMessage(R.string.vocabulary_you_are_make_some_changes).setCancelable(false).setPositiveButton(
 //				this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
 //				{
 //					public void onClick(DialogInterface dialog, int id)
 //					{
 //						superOnBackPresed();
 //					}
 //				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 //				{
 //					public void onClick(DialogInterface dialog, int id)
 //					{
 //						dialog.cancel();
 //					}
 //				});
 //				dialog = builder.create();
 				break;
 			}
 			case BaseActivity.DIALOG_MUST_CHECK_AT_LEAST_ONE:
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setIcon(android.R.drawable.ic_dialog_info);
 				builder.setTitle(R.string.btnStoreSettings);
 				builder.setMessage(R.string.vocabulary_you_must_enable_at_least_one_lesson_);
 				builder.setCancelable(true);
 				builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
 				{
 					@Override
 					public void onClick(DialogInterface dialog, int which)
 					{
 						dialog.dismiss();
 					}
 				});
 				// builder.set
 				dialog = builder.create();
 
 				break;
 			}
 			case BaseActivity.DIALOG_PROGRESS:
 			{
 				dialog = ProgressDialog.show(this, "", getString(R.string.database_still_initializing_please_wait), false, true);
 				dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
 				{
 					@Override
 					public void onCancel(DialogInterface dialog)
 					{
 						finish();
 					}
 				});
 				break;
 			}
 			case DIALOG_RESET_DB:
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle(R.string.menu_reset_db);
 				builder.setIcon(android.R.drawable.ic_dialog_alert);
 				builder.setMessage(R.string.dialog_text_confirm_reset_db);
 				builder.setCancelable(true);
 				builder.setPositiveButton(
 				this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						resetDB();
 					}
 				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						dialog.cancel();
 					}
 				});
 				dialog = builder.create();
 				break;
 			}
 			default:
 			{
 				return super.onCreateDialog(id);
 			}
 		}
 
 		return dialog;
 	}
 
 	protected void resetDB()
 	{
 		mWordCtrl.unloadAllLesson();
 		CommonSetting.lernCode = null;
 		CommonSetting.nativeCode = null;
 		CommonSetting.store(this);
 		finish();
 	}
 
 	@Override
 	protected void onAddCustomWord(Word word)
 	{
 		mAdapter.addCustomWord(word);
 		mList.invalidateViews();
 		super.onAddCustomWord(word);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
 	{
 		menu.setHeaderTitle("ahoj");
 		menu.add("delete");
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	protected int getContentView()
 	{
 		return R.layout.word_setting;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		boolean result = super.onCreateOptionsMenu(menu);
 		menuReset = menu.add(R.string.menu_reset_db);
 		menuReset.setOnMenuItemClickListener(this);
 		return result;
 	}
 	
 	@Override
 	public boolean onMenuItemClick(MenuItem item)
 	{
 		if(menuReset == item)
 		{
 			showDialog(BaseActivity.DIALOG_RESET_DB);
 			return true;
 		}
 		else
 			return super.onMenuItemClick(item);
 	}
 	
 }
