 package ua.maker.gbible.fragment;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import ua.maker.gbible.R;
 import ua.maker.gbible.activity.ComparePoemActivity;
 import ua.maker.gbible.adapter.ItemDialogAdapter;
 import ua.maker.gbible.adapter.ItemListPoemAdapter;
 import ua.maker.gbible.constant.App;
 import ua.maker.gbible.listeners.onDialogClickListener;
 import ua.maker.gbible.utils.DataBase;
 import ua.maker.gbible.utils.Tools;
 import android.app.AlertDialog;
 import android.content.ClipData;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class ListPoemsFragment extends SherlockFragment implements OnGestureListener{
 	
 	private static final String TAG = "ListPoemsFragment";
 	
 	private View view = null;
 	private ListView lvShowPoem = null;
 	private EditText etContent = null;
 	private LinearLayout llMenuLink = null;
 	private DataBase dataBase = null;
 	private AlertDialog dialog = null;
 	private GestureDetector gestureDetector = null;
 	private Thread thread = null;
 	private List<String> listPoems = null;
 	
 	private int bookId = 1;
 	private int chapterNumber = 1;
 	private int poemPos = 1;
 	private int selectPoem = 1;
 	private int maxChapter = 0;
 	private int click = 0;
 	
 	private int posPoemForCompare = 0;
 	
 	private String nameTranslate = "";
 	
 	private Button btnBook = null;
 	private Button btnChapter = null;
 	private Button btnPoem = null;
 	
 	private SharedPreferences sp = null, defPref = null;
 	private boolean dayNight = false; //false - day / true - night
 	
 	//link for bookmark
 	private int bookIdBM = 1;
 	private int poemBM = 1;
 	private int chapterBM = 1;
 	private String contentBM = "";
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		Log.d(TAG, "Start onCreateView");
 		view = inflater.inflate(R.layout.activity_list_poems, null);
 		lvShowPoem = (ListView)view.findViewById(R.id.lv_show_poems);
 		llMenuLink = (LinearLayout)view.findViewById(R.id.menu_select_link);
 
 		btnBook = (Button)view.findViewById(R.id.btn_book);
 		btnChapter = (Button)view.findViewById(R.id.btn_chapter);
 		btnPoem = (Button)view.findViewById(R.id.btn_poem);
 		
 		dataBase = new DataBase(getSherlockActivity());
 		try {
 			dataBase.createDataBase();
 		} catch (IOException e) {e.printStackTrace();}
 		dataBase.openDataBase();
 		sp = getSherlockActivity().getSharedPreferences(App.PREF_SEND_DATA, 0);
 		
 		nameTranslate = getTranslateWitchPreferences();
 		bookId = sp.getInt(App.BOOK_ID, 1);
 		chapterNumber = sp.getInt(App.CHAPTER, 1);
 		selectPoem = sp.getInt(App.POEM_SET_FOCUS, 1);
 		maxChapter = dataBase.getNumberOfChapterInBook(bookId, nameTranslate);
 		
 		getSherlockActivity().setTitle(""
 				+ Tools.getBookNameByBookId(bookId, getSherlockActivity()) + " " 
 				+ chapterNumber);
 		Log.d(TAG, "END onCreateView");
 		gestureDetector = new GestureDetector(this);
 		gestureDetector.setOnDoubleTapListener(doubleTapListener);
 		
 		return view;
 	}
 	
 	private OnDoubleTapListener doubleTapListener = new OnDoubleTapListener() {
 		
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			Log.d(TAG, "onSingleTapConfirmed");
 			
 			return false;
 		}
 		
 		@Override
 		public boolean onDoubleTapEvent(MotionEvent e) {
 			Log.d(TAG, "onDoubleTapEvent");
 			return false;
 		}
 		
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			Log.d(TAG, "onDoubleTap");
 			if(getSherlockActivity().getActionBar().isShowing()) 
 				getSherlockActivity().getActionBar().hide();
 			else
 				getSherlockActivity().getActionBar().show();
 			return false;
 		}
 	};
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setHasOptionsMenu(true);
 		
 		defPref = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
 		dayNight = (defPref.getString(getString(R.string.pref_mode_read), "0").equals("0"))?false:true;
 		if(dayNight)
 		{
 			lvShowPoem.setBackgroundColor(Color.BLACK);
 			llMenuLink.setBackgroundColor(Color.BLACK);
 		}
 		else
 		{
 			lvShowPoem.setBackgroundColor(Color.WHITE);
 			llMenuLink.setBackgroundColor(Color.WHITE);
 		}
 		
 		btnPoem.setBackgroundResource(R.drawable.btn_active_select);
 		btnBook.setText(Tools.getBookNameByBookId(bookId, getSherlockActivity()));
 		btnChapter.setText(""+chapterNumber);
 		
 		btnBook.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(getFragmentManager().findFragmentByTag(App.TAG_FRAGMENT_BOOKS) != null){
 					FragmentTransaction ft = getFragmentManager().
 							 beginTransaction();
 					ft.remove(getFragmentManager().findFragmentByTag(App.TAG_FRAGMENT_BOOKS));
 					ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 					ft.addToBackStack(null);
 					ft.commit();
 				}
 				
 				FragmentTransaction ft = getFragmentManager().
 						 beginTransaction();
 				ft.replace(R.id.flRoot, new StartFragment(), App.TAG_FRAGMENT_BOOKS);
 				ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 				ft.addToBackStack(null);
 				ft.commit();
 				
 				Editor e = sp.edit();
 				e.remove(App.CHAPTER);
 				e.remove(App.BOOK_ID);
 				e.commit();
 			}
 		});
 		
 		btnChapter.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(sp.contains(App.BOOK_ID)){
 					FragmentTransaction ft = getFragmentManager().
 							 beginTransaction();
 					ft.replace(R.id.flRoot, new ListChaptersFragment(), App.TAG_FRAGMENT_CHAPTERS);
 					ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 					ft.addToBackStack(null);
 					ft.commit();
 				}
 				else
 					Tools.showToast(getSherlockActivity(), getString(R.string.no_select_book));
 				
 				Editor e = sp.edit();
 				e.remove(App.CHAPTER);
 				e.commit();
 			}
 		});
 		
 		Log.d(TAG, "Start onActivityCreated");
 		listPoems = dataBase.getPoemsInChapter(bookId, chapterNumber, nameTranslate);
 		
 		ItemListPoemAdapter adapterListPoem = new ItemListPoemAdapter(getSherlockActivity(), R.layout.item_list_poems, listPoems);
 		lvShowPoem.setAdapter(adapterListPoem);
 		
 		Log.d(TAG, "END onActivityCreated");
 		
 		lvShowPoem.setOnScrollListener(scrollChangeListener);		
 		lvShowPoem.setOnItemClickListener(itemClickListener);		
 		lvShowPoem.setOnItemLongClickListener(itemLongClickListener);
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
 		String[] listActions = {
 				""+getString(R.string.dialog_add_to_bookmarks),
 				""+getString(R.string.dialog_copy_to_clicpboard),
 				""+getString(R.string.dialog_share),
 				""+getString(R.string.dialog_compare)};
 		List<Integer> idPicture = new ArrayList<Integer>();
 		idPicture.add(R.drawable.add_bookmark_small);
 		idPicture.add(R.drawable.copy_ico_small);
 		idPicture.add(R.drawable.ico_share_smal_v2);
 		idPicture.add(R.drawable.ico_compare_poem);
 		
 		ItemDialogAdapter adapterDialog = new ItemDialogAdapter(getSherlockActivity(), listActions, idPicture);
 		
 		builder.setAdapter(adapterDialog, dialogItemClickListener);
 		dialog = builder.create();
 		
 		thread = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				click++;
 				if(click%2 == 0){
 					if(getSherlockActivity().getActionBar().isShowing()) 
 						getSherlockActivity().getActionBar().hide();
 					else
 						getSherlockActivity().getActionBar().show();
 					if(llMenuLink.getVisibility() == LinearLayout.VISIBLE) 
 						llMenuLink.setVisibility(LinearLayout.GONE);
 					else
 						llMenuLink.setVisibility(LinearLayout.VISIBLE);
 					click = 0;
 				}
				if(click%2 == 0)
					new Handler()
						.postDelayed(new Runnable() {
 					
 					@Override
 					public void run() {
 						click = 0;
 					}
				}, 500);
 			}
 		});
 	}
 	
 	private void selectPrefPoem(){
 		Log.d(TAG, "START - select poem: " + poemPos);
 		lvShowPoem.setSelection(selectPoem);
 		lvShowPoem.smoothScrollToPosition(selectPoem);
 	}
 	
	@SuppressWarnings("deprecation")
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
		thread.destroy();
 	}
 	
 	private OnScrollListener scrollChangeListener = new OnScrollListener() {
 		
 		@Override
 		public void onScrollStateChanged(AbsListView view, int scrollState) {}
 		
 		@Override
 		public void onScroll(AbsListView view, int firstVisibleItem,
 				int visibleItemCount, int totalItemCount) {
 			btnPoem.setText(""+(firstVisibleItem+1));
 			poemPos = firstVisibleItem;				
 		}
 	};
 	
 	private OnItemClickListener itemClickListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View v, int position,
 				long id) {
 			posPoemForCompare = position;
 			thread.run();
 		}
 	};
 	
 	private DialogInterface.OnClickListener dialogItemClickListener = new onDialogClickListener() {
 		
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch (which) {
 			case 0: //add to bookmarks
 				dataBase.insertBookMark(getTranslateWitchPreferences(), bookIdBM, chapterBM, poemBM, contentBM);
 				Tools.showToast(getSherlockActivity(), getString(R.string.dialog_added_to_bookmarks));
 				dialog.cancel();
 				break;
 			case 1://Copy to clipboard
 				AlertDialog.Builder builderSelect = new AlertDialog.Builder(getSherlockActivity());
 				builderSelect.setTitle(getString(R.string.dialog_title_select_text));
 				
 				LayoutInflater inflater = getSherlockActivity().getLayoutInflater();
 				View view = inflater.inflate(R.layout.dialog_select_poem, null);
 				etContent = (EditText)view.findViewById(R.id.et_content_for_select);
 				etContent.setText(Tools.getBookNameByBookId(bookId, getSherlockActivity())
 	        		+" "+chapterNumber+":"+poemBM+"\n"
 	        		+listPoems.get(poemBM-1));
 				
 				builderSelect.setNegativeButton(getString(R.string.dialog_cancel), clickCancelCopyListener);
 				builderSelect.setPositiveButton(getString(R.string.dialog_dtn_copy_all), clickCopyAllListener);
 				builderSelect.setView(view);
 				
 				AlertDialog dialogSelect = builderSelect.create();
 				dialogSelect.show();
 				
 				break;
 			case 2://Share
 				Intent intent = new Intent(Intent.ACTION_SEND);
 				intent.setType("text/*");
 				intent.putExtra(Intent.EXTRA_TEXT, Tools.getBookNameByBookId(bookId, getSherlockActivity())
 	        		+" "+chapterNumber+":"+poemBM+"\n"
 	        		+listPoems.get(poemBM-1));
 				getSherlockActivity().startActivity(intent);
 				
 				break;
 			case 3://Compare
 				Intent intentCompare = new Intent(getSherlockActivity(), ComparePoemActivity.class);
 				intentCompare.putExtra(App.BOOK_ID, bookId);
 				intentCompare.putExtra(App.CHAPTER, chapterNumber);
 				intentCompare.putExtra(App.POEM, (posPoemForCompare+1));
 				startActivity(intentCompare);
 				Log.d(TAG, "Click on list: pos = " + posPoemForCompare);
 				
 				break;
 			default:
 				break;
 			}			
 		}
 	};
 	
 	private DialogInterface.OnClickListener clickCopyAllListener = new onDialogClickListener() {
 		
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			copyToClipBoard(Tools.getBookNameByBookId(bookId, getSherlockActivity())
 	        		+" "+chapterNumber+":"+poemBM+"\n"
 	        		+listPoems.get(poemBM-1));
 			dialog.cancel();			
 		}
 	};
 	
 	private DialogInterface.OnClickListener clickCopySelectedListener = new onDialogClickListener() {
 		
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			int start = 0, end = 0;
 			start = etContent.getSelectionStart();
 			end = etContent.getSelectionEnd();
 			copyToClipBoard(""+etContent.getText().toString().substring(start, end).toString());
 			
 			dialog.cancel();			
 		}
 	};
 	
 	private DialogInterface.OnClickListener clickCancelCopyListener = new onDialogClickListener() {
 		
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			dialog.cancel();			
 		}
 	};
 	
 	@SuppressWarnings("deprecation")
 	private void copyToClipBoard(String textSetClip) {
 		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
 		if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
 		     getSherlockActivity();
 			android.content.ClipboardManager clipboard =  (android.content.ClipboardManager) getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE); 
 		        ClipData clip = ClipData.newPlainText("label", textSetClip);
 		        clipboard.setPrimaryClip(clip); 
 		} else{
 		    getSherlockActivity();
 			android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE); 
 		    clipboard.setText(textSetClip);
 		}
 		 Tools.showToast(getSherlockActivity(), getString(R.string.copyed_poem));
 	}
 	
 	private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
 
 		@Override
 		public boolean onItemLongClick(AdapterView<?> parent, View v,
 				int position, long id) {
 			Log.d(TAG, "Ling click listener - pos: " + position + " id: " + id);
 			poemBM = (int)id+1;
 			chapterBM = chapterNumber;
 			contentBM = listPoems.get((int)id);
 			dialog.show();
 			return false;
 		}
 	};
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.menu_main_in_list_poem, menu);
 		menu.setQwertyMode(true);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.action_exit:
 			getSherlockActivity().finish();
 			return true;
 		case R.id.action_next_chapter:
 			chapterNumber++;
 			if(dataBase.getPoemsInChapter(bookId, chapterNumber, nameTranslate) != null 
 					&& chapterNumber > 0 && chapterNumber <= maxChapter){
 				chengeChapter(chapterNumber);
 			}
 			else
 				chapterNumber--;
 			return true;
 		case R.id.action_previous_chapter:
 			chapterNumber--;
 			if(dataBase.getPoemsInChapter(bookId, chapterNumber, nameTranslate) != null 
 					&& chapterNumber > 0 && chapterNumber <= maxChapter){
 				chengeChapter(chapterNumber);
 			}
 			else
 				chapterNumber++;
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void chengeChapter(int count){
 		listPoems = dataBase.getPoemsInChapter(bookId, chapterNumber, nameTranslate);
 		ItemListPoemAdapter adapterListPoem = new ItemListPoemAdapter(getSherlockActivity(), R.layout.item_list_poems, listPoems);
 		lvShowPoem.setAdapter(adapterListPoem);
 		getSherlockActivity().setTitle(""+
 				Tools.getBookNameByBookId(bookId, getSherlockActivity()) + " " + chapterNumber);
 		btnChapter.setText(""+chapterNumber);
 		sp = getSherlockActivity().getSharedPreferences(App.PREF_SEND_DATA, 0);
 		Editor editor = sp.edit();
 		editor.putInt(App.BOOK_ID, bookId);
 		editor.putInt(App.CHAPTER, chapterNumber);
 		editor.commit();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		List<String> listPoems = dataBase.getPoemsInChapter(bookId, chapterNumber, getTranslateWitchPreferences());
 		
 		ItemListPoemAdapter adapterListPoem = new ItemListPoemAdapter(getSherlockActivity(), R.layout.item_list_poems, listPoems);
 		lvShowPoem.setAdapter(adapterListPoem);
 		
 		dayNight = (defPref.getString(getString(R.string.pref_mode_read), "0").equals("0"))?false:true;
 		if(dayNight){
 			lvShowPoem.setBackgroundColor(Color.BLACK);
 			llMenuLink.setBackgroundColor(Color.BLACK);
 		}
 		else
 		{
 			lvShowPoem.setBackgroundColor(Color.WHITE);
 			llMenuLink.setBackgroundColor(Color.WHITE);
 		}
 		
 		selectPrefPoem();
 		
 		Log.d(TAG, "OnResume TESTING");
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.d(TAG, "onPause");
 		Editor e = sp.edit();
 		e.putInt(App.POEM_SET_FOCUS, poemPos);
 		e.commit();
 	}
 	
 	public String getTranslateWitchPreferences(){
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
 		String translateName = DataBase.TABLE_NAME_RST;
 		if(prefs.contains(getString(R.string.pref_default_translaters))){
 			
 			switch(Integer.parseInt(prefs.getString(getString(R.string.pref_default_translaters), ""))){
 				case 0:
 					translateName = DataBase.TABLE_NAME_RST;
 					break;
 				case 1:
 					translateName = DataBase.TABLE_NAME_MT;
 					break;
 			}
 		}
 		return translateName;
 	}
 
 	@Override
 	public boolean onDown(MotionEvent e) {
 		return false;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 		
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		return false;
 	}
 }
