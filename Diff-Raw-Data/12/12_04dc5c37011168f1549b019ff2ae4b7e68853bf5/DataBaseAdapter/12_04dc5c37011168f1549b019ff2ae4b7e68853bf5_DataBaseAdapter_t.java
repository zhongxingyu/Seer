 package com.baiboly.katolika;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class DataBaseAdapter {
 	private Context context;
 	private SQLiteDatabase database;
 	private DataBaseHelper dbHelper;
 
 	public DataBaseAdapter(Context context) {
 		this.context = context;
 	}
 
 	public DataBaseAdapter open() throws SQLException {
 		dbHelper = new DataBaseHelper(context);
 		database = dbHelper.getDatabase();
 		return this;
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 	
 	public Cursor fetchAllBooks() {
 		StringBuilder q = new StringBuilder("SELECT b._id as book_id, b.b_sname as MalayalamShortName, count(t._id) as num_chptr, '' as EnglishShortName FROM b_boky as b LEFT JOIN b_toko as t ON t.t_b_id=b._id GROUP BY b._id, b.b_sname ORDER BY b.b_order");        
         return database.rawQuery(q.toString(), null);
 
 	}
 	
 	public Cursor fetchChapter(int bookId, int chapterId, String table) {
 		StringBuilder q = new StringBuilder("SELECT a.b_and as verse_id, a.b_text as verse_text FROM b_and as a LEFT JOIN b_toko as t ON t._id=a.b_t_id WHERE t.t_b_id = ? AND a.b_toko = ? ORDER BY a.b_and");        
         return database.rawQuery(q.toString(), new String[]{bookId + "", chapterId + ""});
 	}
 	
 	public Cursor fetchVerses(int bookId, int chapterId, String table, ArrayList<String> verses, char lang) {
 		StringBuilder q = new StringBuilder("SELECT a.b_and as verse_id, a.b_text as verse_text FROM b_and as a LEFT JOIN b_toko as t ON t._id=a.b_t_id WHERE t.t_b_id = ? AND a.b_toko = ? ");
 		q.append(" AND a.b_and IN (");
 		if(verses != null) {
 			int count = 0;
 			for(String v : verses) {
 				if(v.charAt(0) == lang) {
 					if(count++ > 0) {
 						q.append(",");
 					}
 					q.append(v.substring(1));
 				}
 			}
 		}
 		q.append(") order by a.b_and");
 		
 		return database.rawQuery(q.toString(), new String[]{bookId + "", chapterId + ""});
 	}
 	
 	public Cursor fetchVerses(int bookId, int chapterId, String table, ArrayList<Integer> verses) {
 		StringBuilder q = new StringBuilder("SELECT a.b_and as verse_id, a.b_text as verse_text FROM b_and as a LEFT JOIN b_toko as t ON t._id=a.b_t_id WHERE t.t_b_id = ? AND a.b_toko = ? ");
 		q.append(" AND a.b_and IN (");
 		if(verses != null) {
 			int count = 0;
 			for(Integer v : verses) {
 				if(count++ > 0) {
 					q.append(",");
 				}
 				q.append(v);
 			}
 		}
 		q.append(") order by a.b_and");
 		
 		return database.rawQuery(q.toString(), new String[]{bookId + "", chapterId + ""});
 	}
     
     public Cursor fetchVerses(String search, int bookId, int limit, int offset)
     {
 		
 		return database.rawQuery("SELECT a.b_and , a.b_text , a.b_toko, b.b_sname,b._id as b_id FROM b_and as a LEFT JOIN b_toko as t ON t._id=a.b_t_id LEFT JOIN b_boky AS b ON b._id=t.t_b_id WHERE a.b_text LIKE '%" + search + "%' AND b._id = ? ORDER BY b._id, a.b_toko, a.b_and  LIMIT ? OFFSET ? " , new String[]{bookId + "", limit + "", offset + ""});
     
     }
     public Cursor fetchVerses(String search, int limit, int offset)
     {
         
 		
 		return database.rawQuery("SELECT a.b_and , a.b_text , a.b_toko, b.b_sname,b._id as b_id FROM b_and as a LEFT JOIN b_toko as t ON t._id=a.b_t_id LEFT JOIN b_boky AS b ON b._id=t.t_b_id WHERE a.b_text LIKE '%" + search + "%'  order by b._id, a.b_toko, a.b_and  LIMIT ? OFFSET ?", new String[]{limit + "", offset + ""});
    
     }
     
     public Cursor fetchVerses(ArrayList<String> selectedVerses)
     {
 		StringBuilder q = new StringBuilder("SELECT a.b_and , a.b_text , a.b_toko, b.b_sname,b._id as b_id FROM b_and as a LEFT JOIN b_toko as t ON t._id=a.b_t_id LEFT JOIN b_boky AS b ON b._id=t.t_b_id WHERE b._id = ? AND a.b_toko = ? AND a.b_and = ? ");
 		
         String sid = selectedVerses.get(0);
        String[] tokens = sid.split(":");
         
 		return database.rawQuery(q.toString(), new String[]{tokens[1], tokens[2], tokens[3]});
         
 
     }
     
     public int countVerses(String search)
     {
         Cursor c = database.rawQuery("SELECT count(a.b_and) as cnt FROM b_and AS a WHERE a.b_text LIKE '%" + search + "%'", null);
         c.moveToFirst();
         return c.getInt(0);
     }
     
     public int countVerses(String search, int bookId)
     {
 		StringBuilder q = new StringBuilder("SELECT count(a.b_and) as cnt FROM b_and as a LEFT JOIN b_toko as t ON t._id=a.b_t_id WHERE t.t_b_id = ? AND a.b_text LIKE '%" + search + "%'");
 		
 		Cursor c = database.rawQuery(q.toString(), new String[]{bookId + ""});
         if(c != null)
         {
             c.moveToFirst();
             
             
             return c.getInt(0);
         }
         else
         {
             return 0;
         }
     }    
 }
