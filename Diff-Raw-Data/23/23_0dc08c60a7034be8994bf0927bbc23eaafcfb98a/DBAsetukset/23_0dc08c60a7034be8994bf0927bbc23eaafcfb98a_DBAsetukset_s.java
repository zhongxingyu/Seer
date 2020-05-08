 package com.example.frisbeegolfscores;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class DBAsetukset {
 
 	private SQLiteDatabase database;
 	//private DBAsetukset dbAsetukset;
 	private DBAvaus dbAvaus;
 
 	public DBAsetukset(Context context) {
 		dbAvaus = new DBAvaus(context);
 	}
 	
 	private String[] allAsetuksetColumns = { 
 			DBAlustus.COLUMN_Asetukset_ID,
 			DBAlustus.COLUMN_Asetukset_nimi,
 			DBAlustus.COLUMN_Asetukset_versio,
 			DBAlustus.COLUMN_Asetukset_db_versio,
 			DBAlustus.COLUMN_Asetukset_vuoronvaihto,
 			DBAlustus.COLUMN_Asetukset_pelijarjestys,
 			DBAlustus.COLUMN_Asetukset_kieli,
 			DBAlustus.COLUMN_Asetukset_metric,
 			DBAlustus.COLUMN_Asetukset_raportinmuoto,
 			DBAlustus.COLUMN_Asetukset_useGPS
 			};
 
 	public void setDBInstance(SQLiteDatabase database) {
 		this.database = database;
 	}
 
 	// Adding new
     public void addAsetus(Asetukset asetukset) {
         ContentValues values = new ContentValues();
         Log.d("DBAsetukset: ", "Listn asetus alkaa..");
         //values.put(DBAlustus.COLUMN_Asetukset_ID,
 		values.put(DBAlustus.COLUMN_Asetukset_nimi, asetukset.getNimi());
 		values.put(DBAlustus.COLUMN_Asetukset_versio, asetukset.getVersio());
 		values.put(DBAlustus.COLUMN_Asetukset_db_versio, asetukset.getDb_versio());
 		values.put(DBAlustus.COLUMN_Asetukset_vuoronvaihto, asetukset.getVuoronvaihto());
 		values.put(DBAlustus.COLUMN_Asetukset_pelijarjestys, asetukset.getPelijarjestys());
 		values.put(DBAlustus.COLUMN_Asetukset_kieli, asetukset.getKieli());
 		values.put(DBAlustus.COLUMN_Asetukset_metric, asetukset.getMetric());
 		values.put(DBAlustus.COLUMN_Asetukset_raportinmuoto, asetukset.getRaportinmuoto());
 		values.put(DBAlustus.COLUMN_Asetukset_useGPS, asetukset.getUseGPS());
 		
         // Inserting Row
 		long insertId = database.insert(DBAlustus.TABLE_Asetukset, null, values);
         //database.close(); // Closing database connection
 		Log.d("DBAsetukset: ", "Listn asetus loppuu..");
     }
 
     // Getting single
     public Asetukset getAsetus(int id) { 
         Cursor cursor = database.query(DBAlustus.TABLE_Asetukset, new String[] { DBAlustus.COLUMN_Asetukset_ID, DBAlustus.COLUMN_Asetukset_nimi, DBAlustus.COLUMN_Asetukset_versio, DBAlustus.COLUMN_Asetukset_db_versio, DBAlustus.COLUMN_Asetukset_vuoronvaihto, DBAlustus.COLUMN_Asetukset_pelijarjestys, DBAlustus.COLUMN_Asetukset_kieli, DBAlustus.COLUMN_Asetukset_metric, DBAlustus.COLUMN_Asetukset_raportinmuoto, DBAlustus.COLUMN_Asetukset_useGPS }, DBAlustus.COLUMN_Asetukset_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
         if (cursor != null)
             cursor.moveToFirst();
  
         Asetukset asetukset = new Asetukset(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(0)>0, cursor.getInt(5)>0, cursor.getInt(6), cursor.getInt(7)>0, cursor.getInt(8), cursor.getInt(9)>0);
         return asetukset;
     }
    
     // Updating single
 	public int updateAsetukset(Asetukset asetukset) {
 	    ContentValues values = new ContentValues();	 
 	  //values.put(DBAlustus.COLUMN_Asetukset_ID,
 		values.put(DBAlustus.COLUMN_Asetukset_nimi, asetukset.getNimi());
 		values.put(DBAlustus.COLUMN_Asetukset_versio, asetukset.getVersio());
 		values.put(DBAlustus.COLUMN_Asetukset_db_versio, asetukset.getDb_versio());
 		values.put(DBAlustus.COLUMN_Asetukset_vuoronvaihto, asetukset.getVuoronvaihto());
 		values.put(DBAlustus.COLUMN_Asetukset_pelijarjestys, asetukset.getPelijarjestys());
 		values.put(DBAlustus.COLUMN_Asetukset_kieli, asetukset.getKieli());
 		values.put(DBAlustus.COLUMN_Asetukset_metric, asetukset.getMetric());
 		values.put(DBAlustus.COLUMN_Asetukset_raportinmuoto, asetukset.getRaportinmuoto());
 		values.put(DBAlustus.COLUMN_Asetukset_useGPS, asetukset.getUseGPS());
 	 
 	    // updating row
 	    return database.update(DBAlustus.TABLE_Asetukset, values, DBAlustus.COLUMN_Asetukset_ID + " = ?", new String[] { String.valueOf(asetukset.getId()) });
 	}
     
     public void deleteAsetus(Asetukset asetukset) {
 		int id = asetukset.getId();
 
 		database.delete(DBAlustus.TABLE_Asetukset, DBAlustus.COLUMN_Asetukset_ID + " = " + id, null);
 	}
 	
  // Getting All Asetukset
     public List<Asetukset> haeAsetukset() {
         List<Asetukset> listAsetukset = new ArrayList<Asetukset>();
 
         Cursor cursor = database.query(DBAlustus.TABLE_Asetukset, allAsetuksetColumns, null, null, null, null, null);
  
         // looping through all rows and adding to list
         if (cursor.moveToFirst()) {
             do {
             	Asetukset asetukset = new Asetukset();
             	asetukset.setId(cursor.getInt(0));
             	asetukset.setNimi(cursor.getString(1));
             	asetukset.setVersio(cursor.getString(2));
             	asetukset.setDb_versio(cursor.getInt(3));
             	asetukset.setVuoronvaihto(cursor.getInt(4)>0);
             	asetukset.setPelijarjestys(cursor.getInt(5)>0);
             	asetukset.setKieli(cursor.getInt(6));
             	asetukset.setMetric(cursor.getInt(7)>0);
             	asetukset.setRaportinmuoto(cursor.getInt(8));
             	asetukset.setUseGPS(cursor.getInt(9)>0);
                 // Adding contact to list
             	listAsetukset.add(asetukset);
             } while (cursor.moveToNext());
         }
  
 		// Make sure to close the cursor
 		cursor.close();
         // return asetukset list        
         return listAsetukset;
     }
 
 }
