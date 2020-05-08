 package com.hcmus.dictionaryqlqt;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Stack;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.FuzzyQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 
 import bridge.AndroidBridge;
 import bridge.AndroidBridgeListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import manager.SpeakerImpl;
 import manager.WebviewHelper;
 import model.Vocabulary;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.speech.RecognizerIntent;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Animation.AnimationListener;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.ImageView;
 import dao.DatabaseHelperDAOImpl;
 import dao.FavoriteHistoryImp;
 import dao.FileHelperDAOImpl;
 import dao.FinderDAOImpl;
 import dao.IFavoriteHistory;
 import dao.IOHelperDAOImpl;
 
 /**
  * 
  * @author Minh Khanh 
  * Tab tra tu
  * 
  */
 
 @SuppressWarnings("deprecation")
 public class TabDictionaryActivity extends Activity implements OnClickListener,
 		TextWatcher, OnItemClickListener, AndroidBridgeListener, OnFocusChangeListener {
 
 	/*
 	 * enum trang thai man hinh
 	 */
 	enum Screen{
 		Start, // man hinh luc khoi dong
 		Meaning // man hinh luc hien thi nghia
 	}
 	
 	////////////////////////// CAC BIEN /////////////////////////////
 	
 	/*
 	 * ma Code de nhan ket qua tra ve tu voice search
 	 */
 	private static final int SPEECH_RECOGNIZER_CODE = 1;
 	
 	/*
 	 * status = 0: trang thai cancel search
 	 * status = 1: sau khi click vao search box
 	 */
 	private int statusSearchTab = 0;
 	
 	/*
 	 * strang thai man hinh hien tai
 	 * 
 	 */
 	private Screen currentScreen;
 	
 	/*
 	 * tu hien tai duoc hien thi
 	 */	
 	private Vocabulary currentWord;
 	
 	/*
 	 * doi tuong de goi qua javascript trong wbMean
 	 */
 	private AndroidBridge bridge;
 	
 	/*
 	 * luu lich su tra tu hien tai
 	 */
 	private Stack<Vocabulary> history;
 
 	/*
 	 * trang thai toan man hinh
 	 */
 	private boolean isFullScreen = false;	
 
 	/*
 	 * bo phat am
 	 */
 	private SpeakerImpl speaker;
 
 	/*
 	 * the hien cua TabDictionary
 	 */
 	private static TabDictionaryActivity Instance;
 	
 	///////////////////// CAC THANH PHAN GIAO DIEN /////////////////////
 	
 	/*
 	 * edittext nhap tu 
 	 */
 	private AutoCompleteTextView etWord;
 	
 	/*
 	 * button voice search, cancel search, zoom
 	 */
 	private ImageView btnVoiceSearch, btnCancelSearch,btnZoom;
 	
 	/*
 	 * imageview hien thi logo
 	 */
 	private ImageView imgLogo;
 	
 	/*
 	 * search bar, root layout
 	 */
 	private View searchBar, rootLayout;
 
 	/*
 	 * progressbar load nghia tu
 	 */
 	private ProgressDialog pgbLoading;
 
 	/*
 	 * webview hien thi nghia tu
 	 */
 	private WebView wvMean;	
 	
 	/*
 	 * trang thai drop down autocomplete
 	 */
 	private boolean dropDownEnable;
 	
 	
 	/////////////////////////// DATA  /////////////////////////////////
 	
 	private DatabaseHelperDAOImpl databaseHelper;
 	private FileHelperDAOImpl fileHelper;
 	private FinderDAOImpl finder;
 	private ArrayList<String> words;
 	private ArrayList<String> index;
 	private ArrayList<String> length;
 	private IFavoriteHistory favoriteHistory = new FavoriteHistoryImp(TabDictionaryActivity.this);
 	
 	
 	////////////////////////// KHOI TAO ////////////////////////////////
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tab_dictionary);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		initComponents();
 		initData();
 		Instance = this;
 	}
 
 	private void initData() {
 		IOHelperDAOImpl ioHelper = new IOHelperDAOImpl();
 		// kiem tra thu muc chua du lieu, neu chua co thi tao nhu muc moi
 		ioHelper.createFolderData();
 		databaseHelper = new DatabaseHelperDAOImpl(TabDictionaryActivity.this);
 		try {
 			databaseHelper.openDataBase();// mo databases
 		} catch (Exception e) {
 			// neu mo that bai => chua co data => coppy data tu asset
 			databaseHelper.createDataBase();
 			databaseHelper.openDataBase();
 		}
 		fileHelper = new FileHelperDAOImpl();
 		// Kiem tra file nghia ton tai chua, neu chua thi coppy tu asset
 		if (!fileHelper.checkFileExists())
 			fileHelper.coppyFile(this);
 		if (!ioHelper.checkDataFileExists("History.txt")) {
 			ioHelper.coppyDataFile(this, "History.txt");
 		}
 		if (!ioHelper.checkDataFileExists("Favorite.txt")) {
 			ioHelper.coppyDataFile(this, "Favorite.txt");
 		}
 		finder = new FinderDAOImpl(databaseHelper, fileHelper);
 		words = new ArrayList<String>();
 		index = new ArrayList<String>();
 		length = new ArrayList<String>();
 	}
 
 	/**
 	 * khoi tao cac thanh phan can thiet
 	 */
 	private void initComponents() {
 		// khoi tao cac thanh phan giao dien
 		btnVoiceSearch = (ImageView) findViewById(R.id.btnVoiceSearch);
 		btnCancelSearch = (ImageView) findViewById(R.id.btnCancelSearch);
 		etWord = (AutoCompleteTextView) findViewById(R.id.mactSearchText);
 		searchBar = findViewById(R.id.searchbar);		
 		rootLayout = findViewById(R.id.rootDict);
 		imgLogo = (ImageView) findViewById(R.id.imgLogo);
 		wvMean = (WebView) findViewById(R.id.wvMeaning);
 		btnZoom = (ImageView) findViewById(R.id.btnZoomDict);
 		
 		// thiet lap trang thai va su kien
 		etWord.setThreshold(1);
 		etWord.setOnClickListener(this);
 		etWord.addTextChangedListener(TabDictionaryActivity.this);
 		etWord.setOnItemClickListener(TabDictionaryActivity.this);	
 		etWord.setOnFocusChangeListener(this);
 		btnVoiceSearch.setOnClickListener(this);
 		btnCancelSearch.setOnClickListener(this);
 		btnZoom.setOnClickListener(this);
 		pgbLoading = new ProgressDialog(this);
 		
 		speaker = new SpeakerImpl(getApplicationContext(), Locale.ENGLISH);
 		history = new Stack<Vocabulary>();
 		bridge = new AndroidBridge();
 		bridge.setListener(this);
 
 		changeScreen(Screen.Start);
 		statusSearchTab = 0;
 		setDictionaryTabScreen(statusSearchTab);
 	}
 
 	//////////////////////// XU LY SU KIEN  ////////////////////////////////////
 	
 	@Override
 	public void onClick(View v) {
 
 		int id = v.getId();
 		switch (id) {
 		case R.id.mactSearchText:
 			dropDownEnable = true;
 			if (statusSearchTab == 0) {
 				statusSearchTab = 1;
 				setDictionaryTabScreen(statusSearchTab);
 			}
 			break;
 		case R.id.btnVoiceSearch:
 			startSpeechRecognizer();
 			break;
 		case R.id.btnCancelSearch:
 			statusSearchTab = 0;
 			setDictionaryTabScreen(statusSearchTab);
 			wvMean.requestFocus();
 			break;
 		case R.id.btnResetsearch:
 			etWord.setText("");
 			break;
 		case R.id.btnZoomDict:
 			zoomScreenHandle();
 			break;
 		}
 	}
 	
 	
 	///////////////////////// VOICE SEARCH ///////////////////////////////	
 
 	/**
 	 * mo man hinh nhan dien giong noi
 	 */
 	private void startSpeechRecognizer() {
 		Intent intent = new Intent(this, SpeechRecognizerActivity.class);
 		startActivityForResult(intent, SPEECH_RECOGNIZER_CODE);
 	}
 	
 	/**
 	 * su kien nhan ket qua tra ve tu voice search
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == SPEECH_RECOGNIZER_CODE) {
 			if (resultCode == RESULT_OK && data != null) {
 				// lay danh sach tu nhan dien duoc
 				ArrayList<String> listText = data
 						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 				// neu chi co 1 tu goi ham search
 				if (listText.size() == 1) {
 					Search(listText.get(0));
 				}
 				// neu co nhieu tu hien thi len dialog cho nguoi dung chon
 				else {
 					String[] arrText = convertArrayListToArray(listText);
 					showResultDialog(arrText);
 				}
 			}
 		}
 	}	
 
 	/**
 	 * chuyen tu arraylist String sang mang String
 	 * 
 	 * @param source
 	 *            : arraylist can chuyen
 	 * @return mang String
 	 */
 	private String[] convertArrayListToArray(ArrayList<String> source) {
 		String[] result = new String[source.size()];
 		source.toArray(result);
 
 		return result;
 	}
 
 	/**
 	 * hien thi danh sach ca tu voice search
 	 * 
 	 * @param arrText
 	 *            : mang String
 	 */
 	private void showResultDialog(final String[] arrText) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(arrText.length + " possible words");
 		builder.setItems(arrText, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int pos) {
 				//
 				Search(arrText[pos]);
 			}
 		});
 		builder.show();
 	}
 	
 	/////////////////////////////// PHAT AM //////////////////////////////////
 	
 	/**
 	 * su kien tu webview goi phat am
 	 * 
 	 * @param text
 	 *            : tu can phat am
 	 */
 	@Override
 	public void speakOut(String text) {
 		speaker.speakOut(text);
 		Log.i("Dictionary - Speakout: ", text);
 	}
 	
 	/**
 	 * su kien huy activity
 	 */
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		// giai phong speaker
 		speaker.shutdown();
 	}
 	
 	///////////////////////// TRA TU  ///////////////////////////////////////	
 
 	public void Search(String word) {
 		if (currentWord != null &&
 				currentWord.getWord().equalsIgnoreCase(word)){
 			return;
 		}
 		dropDownEnable = false;
 		etWord.setText(word);
 		Vocabulary vocabulary = finder.find(word.trim());
 		String meaning = "";
 		if (vocabulary != null) {
 		
 			meaning = finder.getMean(vocabulary);
 			saveHistory(new Vocabulary(word, meaning));
 		} else {
 			meaning = "";
 			currentWord = null;
 		}
 		showMeaning(meaning);
 	}
 
 	/**
 	 * hien thi nghia tu len webview
 	 * 
 	 * @param meaning
 	 *            : chuoi nghia cua tu
 	 */
 	private void showMeaning(String meaning) {
 		// show dialog loading
 		if (currentScreen == Screen.Start){
 			changeScreen(Screen.Meaning);
 		}
 		showLoadingDialog();
 		WebviewHelper.ShowMeaning(wvMean, meaning, bridge);
 	}
 	
 	/**
 	 * su kien tu webview goi tim tu: tra cheo
 	 * 
 	 * @param text
 	 *            : tu can tra
 	 */
 	@Override
 	public void lookup(String text) {
 		Search(text);
 	}
 
 	/**
 	 * su kien load xong nghia tu tren webview
 	 */
 	@Override
 	public void onLoadComplete() {
 		if (pgbLoading.isShowing()) {
 			pgbLoading.dismiss();
 			wvMean.requestFocus();
 			checkFavoriteWord();
 		}
 	}
 
 	/**
 	 * hien thi dialog loading khi hien thi nghia tu
 	 */
 	private void showLoadingDialog() {
 		String msg = getResources().getString(
 				R.string.dict_message_loading_dialog);
 		pgbLoading.setMessage(msg);
 		pgbLoading.show();
 	}
 	
 	////////////////////// LICH SU TRA TU HIEN TAI  //////////////////////////
 	
 	/**
 	 * lua lai tu tra cuu hien tai
 	 * 
 	 * @param word
 	 *            : tu hien tai
 	 */
 	private void saveHistory(Vocabulary word) {
 		// neu tu da ton tai trong lich su, xoa bo
 		Iterator<Vocabulary> iterator = history.iterator();
 		while (iterator.hasNext()) {
 			Vocabulary vocab = iterator.next();
 			if (vocab.getWord().equals(word.getWord())) {
 				history.remove(vocab);
 				break;
 			}
 		}
 		// dua tu vua tra truoc do vao lich su
 		if (currentWord != null) {
 			history.push(currentWord);
 		}
 		// gan lai tu hien tai
 		currentWord = word;
 
 		if (favoriteHistory.IsExists(word.getWord(), "Recent")) {
 			favoriteHistory.DeleteItem(word.getWord(), "Recent");
 		} 
 		favoriteHistory.InsertWord(word.getWord(), "Recent");
 	}
 	
 
 	/**
 	 * su kien nhan back
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (handleKeyBack()) {
 				return true;
 			}
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	/**
 	 * xu ly su kien nhan back
 	 * 
 	 * @return false neu lich su tra tu hien tai trong, true neu nguoc lai
 	 */
 	private boolean handleKeyBack() {
 		if (currentScreen == Screen.Start){
 			return false;
 		} 
 		
 		if (history.isEmpty()) {
 			changeScreen(Screen.Start);
 			currentWord = null;
 			if (isFullScreen){
 				zoomScreenHandle();
 			}
 		}
 		else {
 			// lay tu tren cung cua lich su ra va xoa bo
 			Vocabulary word = history.pop();
 			currentWord = word;
 			// hien thi nghia cua tu
 			dropDownEnable = false;
 			etWord.setText(word.getWord());
 			showMeaning(word.getStMean());
 		}
 
 		return true;
 	}
 
 	
 	///////////////////// AUTOCOMPLETE ////////////////////////////////
 
 	@Override
 	public void afterTextChanged(Editable arg0) {}
 
 	@Override
 	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 			int arg3) {}
 
 	@Override
 	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 		if (etWord.isPerformingCompletion() || !dropDownEnable)
 			return;
 		if (etWord.getText().toString().length() > 0) {
 			ArrayList<Vocabulary> arr = finder.getRecommendWord(etWord
 					.getText().toString());
 			if (arr != null) { // kiem tra danh sach goi y co rong hay khong,
 								// neu khac null thi hien sach sach
 								// recommendWord
 				words.clear();
 				index.clear();
 				length.clear();
 				for (Vocabulary vocabulary : arr) {
 					words.add(vocabulary.getWord());
 					index.add(vocabulary.getIndex());
 					length.add(vocabulary.getLength());
 				}
 				etWord
 						.setAdapter(new ArrayAdapter<String>(
 								TabDictionaryActivity.this,
 								android.R.layout.simple_list_item_1,
 								words));
 				etWord.showDropDown();
 			}
 		}
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		hideKeyboard();
 		String word = words.get(arg2);
 		if (currentWord != null &&
 				currentWord.getWord().equalsIgnoreCase(word)){
 			return;
 		}
 		boolean flagSimilar = word.contains("---");
 		// kiem tra xem co phai la lua chon similar hay khong
 		if (flagSimilar == false) {
 			String idx = index.get(arg2);
 			String l = length.get(arg2);
 			String mean = finder.getMean(idx, l);
 			saveHistory(new Vocabulary(word, mean));
 			showMeaning(mean);
 		} else // tien hanh fuzzy search
 		{
 			// tien hanh tach chuoi
 			String[] listTemp = word.split("---");
 			String strText = listTemp[0];
 
 			
 			SQLiteDatabase db = this.openOrCreateDatabase("mydatafuzzy", MODE_PRIVATE, null);
 			String sqlselect = "select * from tbfuzzy";
 			Cursor c1 = db.rawQuery(sqlselect, null);
 			c1.moveToPosition(0);
 			String strdirectory = c1.getString(1);
 			File file = new File(Environment.getExternalStorageDirectory()
 					.getPath() + "/" + strdirectory);
 			Directory fsDirectory;
 			c1.close();
 			db.close();
 			try {
 				fsDirectory = FSDirectory.open(file);
 				IndexReader indexReader = IndexReader.open(fsDirectory);
 				@SuppressWarnings("resource")
 				Searcher indexSearcher = new IndexSearcher(indexReader);
 
 				// tao query
 				Query query = new FuzzyQuery(new Term("word", strText));
 				
 				// tien hanh truy van
 				TopDocs topDocs = indexSearcher.search(query, 5);
 				ScoreDoc[] scoreDosArray = topDocs.scoreDocs;
 				
 				//kiem tra xem ket qua tim duoc
 				if (scoreDosArray.length != 0) {
 					ArrayList<String> listText = new ArrayList<String>();
 					// lay word
 					for (ScoreDoc scoredoc : scoreDosArray) {
 						Document doc = indexSearcher.doc(scoredoc.doc);
 						String strWord = doc.getField("word").stringValue();
 						listText.add(strWord);
 					}
 					
 					String[] arrText = convertArrayListToArray(listText);
 					showResultDialog(arrText);
 				}
 				else
 				{
 					etWord.setText("");
 					String meaning = "";
 					currentWord = null;
 					showMeaning(meaning);
 				}
 			} catch (IOException e) {
 				Log.e("Dictonary - Search", e.getMessage());
 			}
 
 		}
 	}
 
 
     ////////////////////////// FAVORITES //////////////////////////////	
 
 	/**
 	 * su kien tu webview khi nhan them favorite
 	 */
 
 	@Override
 	public void setFavorite(String word) {
 		// them tu yeu thich
 		if (favoriteHistory.IsExists(word, "Favorite")) {
 			favoriteHistory.DeleteItem(word, "Favorite");
 		} 
 		favoriteHistory.InsertWord(word, "Favorite");
 		
 	}
 
 	/**
 	 * su kien tu webview khi nhan xoa favorite
 	 */
 	@Override
 	public void removeFavorite(String word) {
 		// xoa tu yeu thich
 	
 		favoriteHistory.DeleteItem(word, "Favorite");
 		
 	}
 
 	/**
 	 * su kien resume activity
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// kiem tra tu dang hien thi co la favorite hay ko de hien thi
 		checkFavoriteWord();
 	}
 
 	/**
 	 * ham xu ly kiem tra tu favorite
 	 */
 	private void checkFavoriteWord() {
 		if (currentWord != null) {
 				boolean isFavorite = favoriteHistory.IsExists(currentWord
 						.getWord(), "Favorite");
 				wvMean.loadUrl("javascript:setFavorite(" + isFavorite + ")");
 			
 		}
 	}
 	
 	/////////////////////////// XU LY TRANG THAI MAN HINH  ////////////////////////////////////
 
 	private void setDictionaryTabScreen(int status) {
 		if (status == 0) {
 			btnVoiceSearch.setVisibility(View.VISIBLE);
 			btnCancelSearch.setVisibility(View.GONE);
 			etWord.setText("");
 
 		} else if (status == 1) {
 			btnVoiceSearch.setVisibility(View.VISIBLE);
 			btnCancelSearch.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	/**
 	 * thay doi trang thai man hinh
 	 * @param screen trang thai moi
 	 */
 	private void changeScreen(Screen screen){
 		switch (screen) {
 		case Start:
 			wvMean.setVisibility(View.INVISIBLE);
 			imgLogo.setVisibility(View.VISIBLE);
 			rootLayout.setBackgroundColor(getResources().
 					getColor(R.color.bg_content_start));
 			btnZoom.setVisibility(View.INVISIBLE);
 			btnZoom.setFocusable(false);
 			etWord.setText("");
 			imgLogo.requestFocus();
 			break;
 		case Meaning:
 			wvMean.setVisibility(View.VISIBLE);
 			imgLogo.setVisibility(View.INVISIBLE);
 			rootLayout.setBackgroundColor(getResources().
 					getColor(R.color.bg_content_meaning));
 			btnZoom.setVisibility(View.VISIBLE);
 			btnZoom.setFocusable(true);
 			break;
 		}		
 		currentScreen = screen;
 	}
 	
 	/**
 	 * xu ly zoom man hinh
 	 */
 	private void zoomScreenHandle(){
 		if (isFullScreen){
 			btnZoom.setImageResource(R.drawable.ic_zoom_in);			
 			HomeActivity.getInstance().showTabs();
 			showSearchBar();
 		}
 		else{
 			btnZoom.setImageResource(R.drawable.ic_zoom_out);
 			HomeActivity.getInstance().hideTabs();
 			hideSearchBar();
 		}
 		isFullScreen = !isFullScreen;
 	}
 	
 	/**
 	 * an search bar
 	 */
 	private void hideSearchBar(){
 		Animation slideOut = AnimationUtils.loadAnimation(this,
 				R.anim.slide_up_out);
 		slideOut.setAnimationListener(barSlideOut);
 		searchBar.startAnimation(slideOut);
 	}
 	
 	/**
 	 * hien thi lai search bar
 	 */
 	private void showSearchBar(){
 		LayoutParams params = (LayoutParams) searchBar.getLayoutParams();
 		params.height = LayoutParams.WRAP_CONTENT;
 		searchBar.setLayoutParams(params);
 		
 		Animation slideIn = AnimationUtils.loadAnimation(this,
 				R.anim.slide_up_in);
 		searchBar.startAnimation(slideIn);
 	}
 	
 	/**
 	 * lang nghe su kien animation
 	 */
 	private AnimationListener barSlideOut = new AnimationListener() {
 
 		@Override
 		public void onAnimationStart(Animation animation) {}
 
 		@Override
 		public void onAnimationRepeat(Animation animation) {}
 
 		/*
 		 * thiet lap lai height khi ket thuc animation 
 		 */
 		@Override
 		public void onAnimationEnd(Animation animation) {
 			LayoutParams params = (LayoutParams) searchBar.getLayoutParams();
 			params.height = 0;
 
 			searchBar.setLayoutParams(params);
 		}
 	};
 	
 	/**
 	 * an ban phim
 	 */
 	private void hideKeyboard(){
 		InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(etWord.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 
 	/**
 	 * su kien focus tren edittext nhap tu
 	 */
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 		if (hasFocus){
 			dropDownEnable = true;
 		}		
 	}
 	
 	/**
 	 * lay the hien TabDictionary
 	 * @return
 	 */
 	public static TabDictionaryActivity getInstance() {
 		return Instance;
 	}
 }
