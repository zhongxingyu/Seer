 package com.tassadar.lorrismobile;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.util.SparseArray;
 import android.util.SparseIntArray;
 
 import com.tassadar.lorrismobile.connections.Connection;
 import com.tassadar.lorrismobile.modules.Tab;
 
 public class Session extends SQLiteOpenHelper {
     
     private static final int DB_VERSION = 1;
     
     private static final int CHANGED_NAME      = 0x01;
     private static final int CHANGED_DESC      = 0x02;
     private static final int CHANGED_IMG       = 0x04;
     private static final int CHANGED_LAST_OPEN = 0x08;
     private static final int CHANGED_CURR_TAB  = 0x10;
 
     private static final int NEW     = 0;
     private static final int REMOVED = 1;
 
     public static final int CHANGED_ALL       = 0xFF;
 
     public Session(Context ctx, String name, String path) {
         super(ctx, path, null, DB_VERSION);
 
         m_name = name;
         m_desc = "";
         m_changed = 0;
         m_maxTabId = -1;
         m_maxConnId = -1;
         m_currTabId = -1;
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         // General info
         db.execSQL("CREATE TABLE session_info (" +
                 "name TEXT NOT NULL," +
                 "desc TEXT, " +
                 "image BLOB," +
                 "creation_time INTEGER NOT NULL DEFAULT '0'," +
                 "last_open_time INTEGER NOT NULL DEFAULT '0'," +
                 "current_tab_id INTEGER NOT NULL DEFAULT '-1');");
         
         ByteArrayOutputStream str = new ByteArrayOutputStream();
         if(m_image != null)
             m_image.compress(Bitmap.CompressFormat.JPEG, 100, str);
 
         try {
             str.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         ContentValues vals = new ContentValues();
         vals.put("name", m_name);
         vals.put("desc", m_desc);
         vals.put("image", str.toByteArray());
         vals.put("creation_time", Calendar.getInstance().getTimeInMillis()/1000);
         vals.put("last_open_time", 0);
         vals.put("current_tab_id", -1);
         db.insert("session_info", null, vals);
 
         // tabs
         db.execSQL("CREATE TABLE tabs (" +
                 "id INTEGER UNIQUE NOT NULL," +
                 "type INTEGER NOT NULL," +
                 "name TEXT NOT NULL," +
                 "conn_id INTEGER NOT NULL DEFAULT '-1');");
 
         // Connections
         db.execSQL("CREATE TABLE connections (" +
                 "id INTEGER UNIQUE NOT NULL," +
                 "type INTEGER NOT NULL," +
                 "data BLOB);");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // TODO Auto-generated method stub
         
     }
 
     public synchronized void acquireDBRef() {
         if(m_db == null)
             m_db = getWritableDatabase(); 
         ++m_dbRefCounter;
     }
 
     public synchronized void releaseDBRef() {
         if(--m_dbRefCounter == 0) {
             m_db.close();
             m_db = null;
         }
     }
 
     public synchronized void saveBase() {
         if(m_changed == 0)
             return;
 
         acquireDBRef();
 
         ContentValues vals = new ContentValues();
         
         if((m_changed & CHANGED_NAME) != 0)
             vals.put("name", m_name);
         
         if((m_changed & CHANGED_DESC) != 0)
             vals.put("desc", m_desc);
         
         if((m_changed & CHANGED_IMG) != 0) {
             ByteArrayOutputStream str = new ByteArrayOutputStream();
             if(m_image != null)
                 m_image.compress(Bitmap.CompressFormat.JPEG, 100, str);
 
             try {
                 str.flush();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             vals.put("image", str.toByteArray());
         }
         
         if((m_changed & CHANGED_LAST_OPEN) != 0)
             vals.put("last_open_time", m_last_open);
 
         if((m_changed & CHANGED_CURR_TAB) != 0)
             vals.put("current_tab_id", m_currTabId);
 
         m_db.update("session_info", vals, null, null);
 
         releaseDBRef();
         m_changed = 0;
     }
 
     public void saveTabs(SparseArray<Tab> tabs) {
         acquireDBRef();
 
         int size = m_tabChanges.size();
         for(int i = 0; i < size; ++i) {
             int id = m_tabChanges.keyAt(i);
             switch(m_tabChanges.get(id)) {
                 case NEW:
                 {
                     Tab t = tabs.get(id);
                     if(t == null) {
                         Log.e("Lorris", "DB: failed to save tab " + id + ", tab not found\n");
                         break;
                     }
                     dbCreateTab(t);
                     createTabDataFile(t);
                     break;
                 }
                 case REMOVED:
                     dbRemoveTab(id);
                     rmTabDataFile(id);
                     break;
             }
         }
         m_tabChanges.clear();
 
         dbSaveTabs(tabs);
 
         releaseDBRef();
     }
 
     public void saveConns(SparseArray<Connection> conns) {
         acquireDBRef();
 
         int size = m_connChanges.size();
         for(int i = 0; i < size; ++i) {
             int id = m_connChanges.keyAt(i);
             switch(m_connChanges.get(id)) {
                 case NEW:
                 {
                     Connection c = conns.get(id);
                     if(c == null) {
                         Log.e("Lorris", "DB: failed to save conn " + id + ", conn not found\n");
                         break;
                     }
                     dbCreateConn(c);
                     break;
                 }
                 case REMOVED:
                     dbRemoveConn(id);
                     break;
             }
         }
         m_connChanges.clear();
 
         dbSaveConns(conns);
 
         releaseDBRef();
     }
 
     private void dbCreateTab(Tab t) {
         ContentValues vals = new ContentValues();
         vals.put("id", t.getTabId());
         vals.put("type", t.getType());
         vals.put("name", t.getName());
         vals.put("conn_id", t.getLastConnId());
         m_db.insert("tabs", null, vals);
     }
 
     private void dbRemoveTab(int id) {
         m_db.delete("tabs", "id=" + String.valueOf(id), null);
     }
 
     private void dbCreateConn(Connection c) {
         ContentValues vals = new ContentValues();
         Log.e("Lorris", "Create conn " + c.getId() + "\n");
         vals.put("id", c.getId());
         vals.put("type", c.getType());
         vals.put("data", c.saveData());
         m_db.insert("connections", null, vals);
     }
 
     private void dbRemoveConn(int id) {
         m_db.delete("connections", "id=" + String.valueOf(id), null);
     }
 
     private void dbSaveTabs(SparseArray<Tab> tabs) {
         m_maxTabId = -1;
 
         int size = tabs.size();
         ContentValues vals = new ContentValues();
         for(int i = 0; i < size; ++i) {
             Tab t = tabs.valueAt(i);
 
             if(t.getTabId() > m_maxTabId)
                 m_maxTabId = t.getTabId();
 
             vals.put("name", t.getName());
             vals.put("conn_id", t.getConnId());
             Log.e("Lorris", "Tab connection id " + t.getConnId() + "\n");
             m_db.update("tabs", vals, "id="+ String.valueOf(t.getTabId()), null);
 
             File f = getTabDataFile(t.getTabId(), true);
             if(f != null) {
                 byte[] save = t.saveData();
                 try {
                     FileOutputStream str = new FileOutputStream(f);
                     str.write(save);
                     str.close();
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private void dbSaveConns(SparseArray<Connection> conns) {
         m_maxConnId = -1;
 
         int size = conns.size();
         ContentValues vals = new ContentValues();
         for(int i = 0; i < size; ++i) {
             Connection c = conns.valueAt(i);
 
             if(c.getId() > m_maxConnId)
                 m_maxConnId = c.getId();
 
             vals.put("data", c.saveData());
             m_db.update("connections", vals, "id="+ String.valueOf(c.getId()), null);
         }
     }
 
     private void createTabDataFile(Tab t) {
         File dataFile = getTabDataFile(t.getTabId(), false);
         if(dataFile.exists())
             dataFile.delete();
 
         try {
             if(!dataFile.createNewFile())
                 Log.e("Lorris", "Failed to craete tab data file.");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private void rmTabDataFile(int id) {
         File f = getTabDataFile(id, true);
         if(f == null) {
             Log.e("Lorris", "Can't remove data file, it does not exist\n");
             return;
         }
         f.delete();
     }
 
     public File getTabDataFile(int id, boolean mustExist) {
         File extPath = Utils.getDataFolder(null);
         if(extPath == null) {
             Log.e("Lorris", "Failed to get data folde!\n");
             return null;
         }
 
         File path = new File(extPath.getAbsolutePath() + "/" + m_name + "/");
         if(!path.exists()) {
             Log.e("Lorris", "Failed to get data file, session folder does not exist!\n");
             return null;
         }
 
         File f = new File(path, "tab_data_" + id);
         if(mustExist && !f.exists())
             return null;
         return f;
     }
 
     public boolean loadBase() {
         SQLiteDatabase db = getReadableDatabase();
 
         Cursor c = db.query("session_info",
                 //             0       1        2                 3       4
                 new String[] { "desc", "image", "last_open_time", "name", "current_tab_id" },
                 null, null, null, null, null, null);
 
         if(c.moveToFirst()) {
             m_desc = c.getString(0);
             m_last_open = c.getLong(2);
             m_currTabId = c.getInt(4);
     
             byte[] imgData = c.getBlob(1);
             if(imgData != null && imgData.length != 0) {
                 m_image = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
             }
         } else {
             Log.e("Lorris", "Failed to move to first record from session_info table!\n");
         }
     
         c.close();
 
         c = db.rawQuery("SELECT MAX(id) FROM tabs", null);
         if(c.moveToFirst())
             m_maxTabId = c.getInt(0);
         c.close();
 
         c = db.rawQuery("SELECT MAX(id) FROM connections", null);
         if(c.moveToFirst())
             m_maxConnId = c.getInt(0);
         c.close();
 
         db.close();
         return true;
     }
 
     public ArrayList<ContentValues> loadConnections() {
         ArrayList<ContentValues> res = new ArrayList<ContentValues>();
 
         SQLiteDatabase db = getReadableDatabase();
         Cursor c = db.rawQuery("SELECT id, type, data FROM connections ORDER BY id;", null);
         while(c.moveToNext()) {
             ContentValues vals = new ContentValues();
             vals.put("id", c.getInt(0));
             vals.put("type", c.getInt(1));
             vals.put("data", c.getBlob(2));
             res.add(vals);
         }
         c.close();
         db.close();
         return res;
     }
 
     public ArrayList<ContentValues> loadTabs() {
         ArrayList<ContentValues> res = new ArrayList<ContentValues>();
 
         SQLiteDatabase db = getReadableDatabase();
         //                             0   1     2     3
         Cursor c = db.rawQuery("SELECT id, type, name, conn_id FROM tabs ORDER BY id;", null);
         while(c.moveToNext()) {
             ContentValues vals = new ContentValues();
             vals.put("id", c.getInt(0));
             vals.put("type", c.getInt(1));
             vals.put("name", c.getString(2));
             vals.put("conn_id", c.getInt(3));
 
             byte[] data = new byte[0];
             File f = getTabDataFile(c.getInt(0), true);
             if(f != null) {
                 try {
                     FileInputStream str = new FileInputStream(f);
                     data = new byte[(int) f.length()];
                     str.read(data);
                     str.close();
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
 
             vals.put("data", data);
             res.add(vals);
         }
         c.close();
         db.close();
         return res;
     }
 
     public String getName() {
         return m_name;
     }
 
     public String getDesc() {
         return m_desc;
     }
 
     public Bitmap getImage() {
         return m_image;
     }
 
     public void setName(String name) {
         if(name == m_name)
             return;
         m_changed |= CHANGED_NAME;
         m_name = name;
     }
 
     public void setDesc(String desc) {
         if(desc == m_desc)
             return;
         m_changed |= CHANGED_DESC;
         m_desc = desc;
     }
    
     public void setImage(Bitmap img) {
        if(m_image != null && !Utils.compareBitmap(m_image, img))
             return;
         m_changed |= CHANGED_IMG;
         m_image = img;
     }
    
     public void setLastOpenTime() {
         m_last_open = (int) (Calendar.getInstance().getTimeInMillis()/1000);
         m_changed |= CHANGED_LAST_OPEN;
     }
 
     public void setChanged(int changed) {
         m_changed = changed;
     }
 
     public int getMaxTabId() {
         return m_maxTabId;
     }
 
     public int getMaxConnId() {
         return m_maxConnId;
     }
 
     public synchronized void addTab(int id) {
         m_tabChanges.put(id, NEW);
     }
 
     public synchronized void rmTab(int id) {
         m_tabChanges.put(id, REMOVED);
     }
 
     public synchronized void addConn(int id) {
         m_connChanges.put(id, NEW);
     }
 
     public synchronized void rmConn(int id) {
         m_connChanges.put(id, REMOVED);
     }
 
     public synchronized void setCurrTab(int id) {
         m_currTabId = id;
         m_changed |= CHANGED_CURR_TAB;
     }
 
     public int getCurrTabId() {
         return m_currTabId;
     }
 
     public synchronized void clearChanges() {
         m_tabChanges.clear();
         m_connChanges.clear();
         m_changed = 0;
     }
 
     private String m_name;
     private String m_desc;
     private Bitmap m_image;
     private int m_changed;
     private long m_last_open;
     private int m_maxTabId;
     private int m_maxConnId;
     private int m_currTabId;
     private SparseIntArray m_tabChanges = new SparseIntArray();
     private SparseIntArray m_connChanges = new SparseIntArray();
     private SQLiteDatabase m_db;
     private int m_dbRefCounter;
 }
