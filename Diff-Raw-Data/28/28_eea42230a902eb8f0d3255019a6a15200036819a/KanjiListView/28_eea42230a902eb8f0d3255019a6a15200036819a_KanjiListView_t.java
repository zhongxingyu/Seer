 package ch.shibastudio.kanjinotepad.lists;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import ch.shibastudio.kanjinotepad.KanjiDBHelper;
 import ch.shibastudio.kanjinotepad.KanjiInfo;
 import ch.shibastudio.kanjinotepad.KanjiManager;
 import ch.shibastudio.kanjinotepad.R;
 import ch.shibastudio.kanjinotepad.R.id;
 import ch.shibastudio.kanjinotepad.R.layout;
 import ch.shibastudio.kanjinotepad.lists.KanjiListFancyAdapter.ViewHolder;
 import ch.shibastudio.kanjinotepad.utils.TextTools;
 import ch.shibastudio.kanjinotepad.utils.ZipTools;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class KanjiListView extends LinearLayout {
 	// -- Members ------------------------------------------------------------------
 	private KanjiDBHelper mKDBHelper;
 	private Spinner mFilterSpinner;
 	private EditText mFilterText;
 	private ImageView mSearchButton;
 	private ListView mKanjiListView;
 	private Context mCtx;
 	
 	// -- Action Listeners ---------------------------------------------------------
 	private OnItemSelectedListener mFilterSelected = new OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 			int category = KanjiDBHelper.KANJI_FILTER_ALL;
 			if(0 < pos){
 				if(1 == pos){
 					// Favorites
 					category = KanjiDBHelper.KANJI_FILTER_FAVORITES;
 				}else{
 					category = pos - 1;
 				}
 			}else{
 				// All kanji
 				category = KanjiDBHelper.KANJI_FILTER_ALL;
 			}
 			getKanjis(category);
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {}
 	};
 	
 	private OnClickListener mCustomFilterSearch = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			String expression = mFilterText.getText().toString();
 
 			try{
 	    		mKDBHelper.openDatabase();
 	        	Cursor c = mKDBHelper.fetchKanjiFromExpression(expression);
 	        	mKDBHelper.close();
 	        	
 	    		refreshList(c);
 	 
 	    	}catch(SQLException e){
 	    		throw new Error(e.getMessage());
 	    	}
 		}
 	};
 	
 	private OnItemClickListener onCharacterClicked = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			if(null != view){
 				try {
 					mKDBHelper.openDatabase();
 					ViewHolder vh = (ViewHolder)view.getTag();
 					if(null != vh){
 						String character = vh.tvCharacter.getText().toString();
						
						boolean bFav = mKDBHelper.isInFavorites(TextTools.kanjiToCode(character));
						
						KanjiInfo currentKanji = new KanjiInfo(character, bFav);
 						
 						int cp = TextTools.kanjiToCode(character);
 						Cursor c = mKDBHelper.getKanjiInfos(cp);
 						
 						// Get the JLPT level
 						int jlpt = c.getInt(c.getColumnIndex(KanjiDBHelper.KEY_JLPT));
 						// Get the kanjiVG data
 						String kvg = "";
 						byte[] paths = c.getBlob(c.getColumnIndex(KanjiDBHelper.KEY_PATH));
 						if(null != paths){
 							kvg = ZipTools.decompress(paths);
 						}
 						// Get the stroke count
 						int strokeCount = c.getInt(c.getColumnIndex(KanjiDBHelper.KEY_STROKE_COUNT));
 						// Get the frequency
 						int frequency = c.getInt(c.getColumnIndex(KanjiDBHelper.KEY_FREQUENCY));				
 						// Get the grade
 						int grade = c.getInt(c.getColumnIndex(KanjiDBHelper.KEY_GRADE));
 						
 						Cursor cOn = mKDBHelper.getKanjiOnYomi(cp);
 						cOn.moveToFirst();
 						for(int i=0; i<cOn.getCount(); i++){
 							String yomi = cOn.getString(cOn.getColumnIndex(KanjiDBHelper.KEY_YOMI));
 							currentKanji.oNYomi.add(yomi);
 							cOn.moveToNext();
 						}
 						
 						Cursor cKun = mKDBHelper.getKanjiKunYomi(cp);
 						cKun.moveToFirst();
 						for(int i=0; i<cKun.getCount(); i++){
 							String yomi = cKun.getString(cKun.getColumnIndex(KanjiDBHelper.KEY_YOMI));
 							currentKanji.kUNYomi.add(yomi);
 							cKun.moveToNext();
 						}
 
 						Cursor cMeaning = mKDBHelper.getKanjiMeaning(cp);
 						cMeaning.moveToFirst();
 						for(int i=0; i<cMeaning.getCount(); i++){
 							String meaning = cMeaning.getString(cMeaning.getColumnIndex(KanjiDBHelper.KEY_MEANING));
 							currentKanji.meaning.add(meaning);
 							cMeaning.moveToNext();
 						}
 						
 						currentKanji.frequency = frequency;
 						currentKanji.grade = grade;
 						currentKanji.kvg = kvg;
 						currentKanji.strokeCount = strokeCount;
 						currentKanji.jlpt = jlpt;
						currentKanji.favorite = bFav;
 						KanjiManager.setCurrentKanji(currentKanji);
 						mKDBHelper.close();
 					}
 				}catch (SQLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
     };
 	
 	public KanjiListView(Context c){
 		super(c);
 		mCtx = c;
 		init();
 	}
 	
 	public KanjiListView(Context c, AttributeSet attrs){
 		super(c, attrs);
 		mCtx = c;
 		LayoutInflater inflater = LayoutInflater.from(c);
 		inflater.inflate(R.layout.kanji_list, this);
 		init();
 	}
 	
 	private void init(){
 		mKanjiListView = (ListView)findViewById(R.id.kanjiList);
         mKanjiListView.setEmptyView(findViewById(R.id.emptyKanjiView));
         mFilterSpinner = (Spinner)findViewById(R.id.filterCategory);
         mFilterText = (EditText)findViewById(R.id.searchField);
         mSearchButton = (ImageView)findViewById(R.id.searchButton);
         mKDBHelper = new KanjiDBHelper(mCtx);
         mFilterSpinner.setOnItemSelectedListener(mFilterSelected);
         mSearchButton.setOnClickListener(mCustomFilterSearch);
 	}
     
     public void getKanjis(int category){
     	try{
     		mKDBHelper.openDatabase();
         	Cursor c = mKDBHelper.fetchKanji(category);
         	mKDBHelper.close();
         	
     		refreshList(c);
  
     	}catch(SQLException e){
     		throw new Error(e.getMessage());
     	}
     }
     
     private void refreshList(Cursor c) throws SQLException{	
     	KanjiListFancyAdapter fAdapter = new KanjiListFancyAdapter(mCtx);
 		c.moveToFirst();
 		for(int i=0; i<c.getCount(); i++){
 			KanjiListInfos kli = new KanjiListInfos();
 			
 			// Get the value at the given position
 			int index = c.getColumnIndex(KanjiDBHelper.KEY_ID);
 			String value = c.getString(index);
 			int iVal = Integer.parseInt(value);
 			kli.character = TextTools.codeToKanji(iVal);
 			
 			// Now we will add the favorite star
 			int favIndex = c.getColumnIndex(KanjiDBHelper.KEY_STATE);		
 			int iFavVal = c.getInt(favIndex);		
 			kli.favorite = (0 < iFavVal);
 		
 			// NOTE: Add some extra infos to be displayed on the rows
 			int onIndex = c.getColumnIndex(KanjiDBHelper.KEY_ENTRY_ONYOMI);
 			String onValue = c.getString(onIndex);
 			kli.info1 = onValue;
 			
 			int kunIndex = c.getColumnIndex(KanjiDBHelper.KEY_ENTRY_KUNYOMI);
 			String kunValue = c.getString(kunIndex);
 			kli.info2 = kunValue;
 			
 			fAdapter.addItem(kli);
 			
 			c.moveToNext();
 		}
 		mKanjiListView.setAdapter(fAdapter);
         mKanjiListView.setOnItemClickListener(onCharacterClicked);
     }
 }
