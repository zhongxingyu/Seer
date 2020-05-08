 package com.eyougo.unlockword;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.R.integer;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class WordDatabaseHelper extends SQLiteOpenHelper {
 	private static String TAG = "WordDatabase";
 	public static final int TOP_PROCESS_VALUE = 5;
 	public static final String DEFAULT_WORD_FILE = "kaoyan.xmf";
 	public static final String DEFAULT_WORD_TABLE = "word_kaoyan";
 	public static final String DATABASE_NAME = "unlockword.db";
 	public static final int DATABASE_VERSION = 1;
 	public static final String CREATE_WORDPROCESS_TABLE_SQL = "create table if not exists word_process (" +
 			" word TEXT PRIMARY KEY, process INTEGER)";
 	
 	private SQLiteDatabase wordDataBase;
 	private final Context context;
 	
 	public WordDatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 		wordDataBase = this.getWritableDatabase();
 	}
 	
 	public void close(){
 		wordDataBase.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		//创建进度表
 		db.execSQL(CREATE_WORDPROCESS_TABLE_SQL);
 		Log.i(TAG,"create word process table");
 		//默认导入单词表
 		importWordTable(DEFAULT_WORD_FILE, DEFAULT_WORD_TABLE, db);
 		Log.i(TAG,"create defaul word table");
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 	}
 	
 	public int getWordCount(String tableName){
 		Cursor cursor = wordDataBase.rawQuery("select count(*) from " + tableName, null);
 		cursor.moveToFirst();
 		int count = cursor.getInt(0);
 		cursor.close();
 		return count;
 		
 	}
 	
 	public List<WordItem> getRandomOtherWordItemList(int number, String table, String exceptWord){
 		List<WordItem> list = new ArrayList<WordItem>(number);
 		int count = getWordCount(table);
 		Random random = new Random();
 		int offset = random.nextInt(count);
 		String sql = "select t.word, t.trans, t.phonetic, t.tags, p.process from " + table +
 				" t left join word_process p on t.word = p.word" +
 				" where t.word <> ? and (process < ? or process is null) limit ? offset ?";
 		Cursor cursor = wordDataBase.rawQuery(sql, 
 				new String[]{exceptWord, String.valueOf(TOP_PROCESS_VALUE), String.valueOf(number), String.valueOf(offset)});
 		while (cursor.moveToNext()) {
 			WordItem wordItem = new WordItem();
 			wordItem.setWord(cursor.getString(cursor.getColumnIndex("word")));
 			wordItem.setTrans(cursor.getString(cursor.getColumnIndex("trans")));
 			wordItem.setPhonetic(cursor.getString(cursor.getColumnIndex("phonetic")));
 			wordItem.setTags(cursor.getString(cursor.getColumnIndex("tags")));
 			wordItem.setProcess(cursor.getInt(cursor.getColumnIndex("process")));
 			list.add(wordItem);
 		}
 		cursor.close();
 		return list;
 	}
 	
 	public WordItem getRandomWordItem(String table, String exceptWord){
 		int count = getWordCount(table);
 		Random random = new Random();
 		int offset = random.nextInt(count);
 		StringBuffer sqlBuffer = new StringBuffer();
 		List<String> argList = new ArrayList<String>();
 		sqlBuffer.append("select t.word, t.trans, t.phonetic, t.tags, p.process from " )
 			.append(table).append(" t left join word_process p on t.word = p.word")
 			.append(" where (process < ? or process is null) ");
 		argList.add(String.valueOf(TOP_PROCESS_VALUE));
 		if (exceptWord != null) {
 			sqlBuffer.append("and t.word != ? ");
 			argList.add(exceptWord);
 		}
 		sqlBuffer.append(" limit 1 offset ?");
 		argList.add(String.valueOf(offset));
 		Cursor cursor = wordDataBase.rawQuery(sqlBuffer.toString(), argList.toArray(new String[]{}));
 		cursor.moveToFirst();
 		WordItem wordItem = new WordItem();
 		wordItem.setWord(cursor.getString(cursor.getColumnIndex("word")));
 		wordItem.setTrans(cursor.getString(cursor.getColumnIndex("trans")));
 		wordItem.setPhonetic(cursor.getString(cursor.getColumnIndex("phonetic")));
 		wordItem.setTags(cursor.getString(cursor.getColumnIndex("tags")));
 		wordItem.setProcess(cursor.getInt(cursor.getColumnIndex("process")));
 		cursor.close();
 		return wordItem;
 	}
 	
 	public String getRandomOtherTrans(String table, String exceptWord){
 		int count = getWordCount(table);
 		Random random = new Random();
 		int offset = random.nextInt(count);
 		StringBuffer sqlBuffer = new StringBuffer();
 		List<String> argList = new ArrayList<String>();
 		sqlBuffer.append("select t.trans from " )
 			.append(table).append(" t ");
		argList.add(String.valueOf(TOP_PROCESS_VALUE));
 		if (exceptWord != null) {
 			sqlBuffer.append("where t.word != ? ");
 			argList.add(exceptWord);
 		}
 		sqlBuffer.append(" limit 1 offset ?");
 		argList.add(String.valueOf(offset));
 		Cursor cursor = wordDataBase.rawQuery(sqlBuffer.toString(), argList.toArray(new String[]{}));
 		cursor.moveToFirst();
 		String trans = cursor.getString(cursor.getColumnIndex("trans"));
 		cursor.close();
 		return trans;
 	}
 	
 	public int processBackward(String word, int process){
 		if (process > 0) {
 			wordDataBase.beginTransaction();
 			ContentValues values = new ContentValues();
 			process --;
 			values.put("process", process);
 			wordDataBase.update("word_process", values, " word = ? ", new String[]{word});
 		
 			wordDataBase.setTransactionSuccessful();
 			wordDataBase.endTransaction();
 		}
 		return process;
 	}
 	
 	public void processForward(String word, int process, boolean top){
 		wordDataBase.beginTransaction();
 		if (process > 0) {
 			ContentValues values = new ContentValues();
 			if (top) {
 				process = TOP_PROCESS_VALUE;
 			}else {
 				process ++;
 			}
 			values.put("process", process);
 			wordDataBase.update("word_process", values, " word = ? ", new String[]{word});
 		}else {
 			ContentValues values = new ContentValues();
 			values.put("word", word);
 			if (top) {
 				process = TOP_PROCESS_VALUE;
 			}else {
 				process = 1;
 			}
 			values.put("process", process);
 			wordDataBase.insert("word_process", null, values);
 		}
 		wordDataBase.setTransactionSuccessful();
 		wordDataBase.endTransaction();
 	}
 	
 	public void importWordTable(String fileName, String tableName, SQLiteDatabase db){
 		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
 		InputStream inputStream = null;
 		XMLReader xmlReader;
 		try {
 			xmlReader = saxParserFactory.newSAXParser().getXMLReader();
 			if (db == null) {
 				db = wordDataBase;
 			}
 			xmlReader.setContentHandler(new WordXmlHandler(tableName, db));
 			inputStream = context.getAssets().open(fileName);
 			InputSource inputSource = new InputSource(inputStream);
 			inputSource.setEncoding("UTF-8");
 			xmlReader.parse(inputSource);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally{
 			if (inputStream != null) {
 				try {
 					inputStream.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 	
 	private class WordXmlHandler extends DefaultHandler{
 		private String tableName;
 		private WordItem wordItem;
 		private String tagName;
 		private SQLiteDatabase db;
 		
 		public WordXmlHandler(String tableName, SQLiteDatabase db) {
 			super();
 			this.db = db;
 			this.tableName = tableName;
 		}
 
 		@Override
 		public void endDocument() throws SAXException {
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		}
 
 		@Override
 		public void startDocument() throws SAXException {
 			// 开启数据库事务，创建表
 			db.beginTransaction();
 			String sql = "create table if not exists " + tableName + 
 					" ( word TEXT PRIMARY KEY, " +
 					" trans TEXT, " +
 					" phonetic TEXT, " +
 					" tags TEXT)";
 			db.execSQL(sql);
 		}
 
 		@Override
 		public void startElement(String uri, String localName, String qName,
 				Attributes attributes) throws SAXException {
 			tagName = localName;
 			if (localName.equals("item")) {
 				wordItem = new WordItem();
 			}
 		}
 
 		@Override
 		public void characters(char[] ch, int start, int length)
 				throws SAXException {
 			//解析一个词的内容
 			String text = new String(ch, start, length);
 			if (text != null) {
 				text = text.trim();
 			}
 			if (tagName.equals("word")) {
 				if (wordItem.getWord() != null) {
 					wordItem.setWord(wordItem.getWord().trim()+text);
 				}else {
 					wordItem.setWord(text);
 				}
 			}else if (tagName.equals("trans")) {
 				if (wordItem.getTrans() != null) {
 					wordItem.setTrans(wordItem.getTrans().trim()+text);
 				}else {
 					wordItem.setTrans(text);
 				}
 			}else if (tagName.equals("phonetic")) {
 				if (wordItem.getPhonetic() != null) {
 					wordItem.setPhonetic(wordItem.getPhonetic().trim()+text);
 				}else {
 					wordItem.setPhonetic(text);
 				}
 			}else if (tagName.equals("tags")) {
 				if (wordItem.getTags() != null) {
 					wordItem.setTags(wordItem.getTags().trim()+text);
 				}else {
 					wordItem.setTags(text);
 				}
 			}
 		}
 
 		@Override
 		public void endElement(String uri, String localName, String qName)
 				throws SAXException {
 			if (localName.equals("item")) {
 				//System.out.println("end item" + wordItem.getWord());
 				//将该词写入数据库
 				ContentValues values = new ContentValues();
 				values.put("word", wordItem.getWord());
 				values.put("trans", wordItem.getTrans());
 				values.put("phonetic", wordItem.getPhonetic());
 				values.put("tags", wordItem.getTags());
 				db.insert(tableName, null, values);
 			}
 		}
 		
 		
 		
 	}
 }
