 package com.pk.addits.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import com.pk.addits.model.Article;
 
 public class DatabaseHelper extends SQLiteOpenHelper
 {
 	private static final int DATABASE_VERSION = 1;
 	private static final String DATABASE_NAME = "db.addits.article";
 	private static final String TABLE_ARTICLES = "articles";
 	
 	private static final String KEY_ID = "id";
 	private static final String KEY_TITLE = "title";
 	private static final String KEY_DESCRIPTION = "description";
 	private static final String KEY_CONTENT = "content";
 	private static final String KEY_COMMENT_FEED = "comments";
 	private static final String KEY_AUTHOR = "creator";
 	private static final String KEY_DATE = "pubDate";
 	private static final String KEY_CATEGORY = "category";
 	private static final String KEY_IMG_URL = "imgLink";
 	private static final String KEY_URL = "url";
 	private static final String KEY_IS_FAV = "isFav";
 	private static final String KEY_IS_READ = "isRead";
 	
 	public DatabaseHelper(Context context)
 	{
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 	
 	// Creating Tables
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
		String CREATE_ARTICLES_TABLE = "CREATE TABLE " + TABLE_ARTICLES + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL  UNIQUE, " + KEY_TITLE + " TEXT UNIQUE ," + KEY_DESCRIPTION + " TEXT UNIQUE , " + KEY_CONTENT + " TEXT UNIQUE , " + KEY_COMMENT_FEED + " TEXT UNIQUE , " + KEY_AUTHOR + " TEXT , " + KEY_DATE + " TEXT , " + KEY_CATEGORY + " TEXT , " + KEY_IMG_URL + " TEXT UNIQUE ," + KEY_URL + " TEXT, " + KEY_IS_FAV + " BOOL, " + KEY_IS_READ + " BOOL " + ")";
 		
 		db.execSQL(CREATE_ARTICLES_TABLE);
 	}
 	
 	// Upgrading database
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 	{
 		// Drop older table if existed
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTICLES);
 		
 		// Create tables again
 		onCreate(db);
 	}
 	
 	// Adding new article
 	public void addArticle(Article article)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_TITLE, article.getTitle());
 		values.put(KEY_DESCRIPTION, article.getDescription());
 		values.put(KEY_CONTENT, article.getContent());
 		values.put(KEY_COMMENT_FEED, article.getCommentFeed());
 		values.put(KEY_AUTHOR, article.getAuthor());
 		values.put(KEY_DATE, article.getDate());
 		values.put(KEY_CATEGORY, article.getCategory());
 		values.put(KEY_IMG_URL, article.getImage());
 		values.put(KEY_URL, article.getURL());
 		values.put(KEY_IS_FAV, article.isFavorite());
 		values.put(KEY_IS_READ, article.isRead());
 		
 		// Inserting Row
 		db.insert(TABLE_ARTICLES, null, values);
 		db.close(); // Closing database connection
 	}
 	
 	// Getting single article
 	public Article getArticle(int id)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(TABLE_ARTICLES, new String[] { KEY_ID, KEY_TITLE, KEY_DESCRIPTION, KEY_CONTENT, KEY_COMMENT_FEED, KEY_AUTHOR, KEY_DATE, KEY_CATEGORY, KEY_IMG_URL, KEY_URL, KEY_IS_FAV, KEY_IS_READ }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
 		Article article = new Article();
 		
 		try
 		{
 			if (cursor != null)
 				cursor.moveToFirst();
 			
			article = new Article(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9), Boolean.parseBoolean(cursor.getString(10)), Boolean.parseBoolean(cursor.getString(11)));
 			
 		}
 		finally
 		{
 			cursor.close();
 		}
 		
 		db.close();
 		return article;
 	}
 	
 	// Getting All Articles
 	public List<Article> getAllArticles()
 	{
 		List<Article> articleList = new ArrayList<Article>();
 		// Select All Query
 		String selectQuery = "SELECT  * FROM " + TABLE_ARTICLES;
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		Cursor cursor = db.rawQuery(selectQuery, null);
 		
 		// looping through all rows and adding to list
 		if (cursor.moveToFirst())
 		{
 			do
 			{
 				Article article = new Article();
 				article.setID(Integer.parseInt(cursor.getString(0)));
 				article.setTitle(cursor.getString(1));
 				article.setDescription(cursor.getString(2));
 				article.setContent(cursor.getString(3));
 				article.setCommentFeed(cursor.getString(4));
 				article.setAuthor(cursor.getString(5));
 				article.setDate(cursor.getString(6));
 				article.setCategory(cursor.getString(7));
 				article.setImage(cursor.getString(8));
 				article.setURL(cursor.getString(9));
				article.setFavorite(Boolean.parseBoolean(cursor.getString(10)));
				article.setRead(Boolean.parseBoolean(cursor.getString(11)));
 				articleList.add(article);
 			} while (cursor.moveToNext());
 		}
 		db.close();
 		
 		// return article list
 		return articleList;
 	}
 	
 	// Updating single article
 	public int updateArticle(Article article)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_IS_FAV, article.isFavorite());
 		values.put(KEY_IS_READ, article.isRead());
 		
 		// updating row
 		db.close();
 		return db.update(TABLE_ARTICLES, values, KEY_ID + " = ?", new String[] { String.valueOf(article.getID()) });
 	}
 	
 	// Deleting single Article
 	public void deleteArticle(Article article)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(TABLE_ARTICLES, KEY_ID + " = ?", new String[] { String.valueOf(article.getID()) });
 		db.close();
 	}
 	
 	public void removeAll()
 	{
		// EXPERIMENTAL!!!
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete(DatabaseHelper.TABLE_ARTICLES, null, null);
 		db.close();
 	}
 	
 	// Getting Article Count
 	public int getArticleCount()
 	{
 		int count = 0;
 		String countQuery = "SELECT  * FROM " + TABLE_ARTICLES;
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.rawQuery(countQuery, null);
 		
 		if (cursor != null && !cursor.isClosed())
 		{
 			count = cursor.getCount();
 			cursor.close();
 		}
 		
 		db.close();
 		// return count
 		return count;
 	}
 }
