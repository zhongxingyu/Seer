 package com.horeca;
 
 import java.util.Date;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import java.util.ArrayList;
 
 public class Commande {
 	/*
 	 * @pre the user is signed in !
 	 */
 	public static Cursor getAllCommandeForPlat(SQLiteDatabase db, Plat plat) {
 		return getCursor(db, HorecaContract.Commande.PLAT_ID + " = ? AND " +
 				HorecaContract.Commande.USER_ID + " = ?", // TODO sort it
 				new String[]{String.valueOf(plat.getId()), String.valueOf(User.getCurrentUser().getId())});
 	}
 	private static Cursor getCursor(SQLiteDatabase db, String selection, String[] selectionArgs) {
 		return db.query(HorecaContract.Commande.TABLE_NAME,
 				HorecaContract.Commande.COLUMN_NAMES, selection, selectionArgs, null, null, null);
 	}
 	public static Commande createCommande(SQLiteDatabase db, Plat plat, Date temps, long nombre) {
 		User user = User.getCurrentUser();
 		ContentValues cv = new ContentValues();
 		cv.put(HorecaContract.Commande.USER_ID, user.getId());
 		cv.put(HorecaContract.Commande.PLAT_ID, plat.getId());
 		cv.put(HorecaContract.Commande.TEMPS, temps.getTime());
 		cv.put(HorecaContract.Commande.NOMBRE, nombre);
 		long id = db.insert(HorecaContract.Commande.TABLE_NAME, null, cv);
		if (id == -1) { // The command already exists for this date
			return null;
		}
 		if (plat.hasStock()) {
 			plat.setStock(db, plat.getStock() - nombre);
 		}
 		return new Commande(id, user, plat, temps, nombre);
 	}
 	
 	
 	private long id;
 	private User user;
 	private Plat plat;
 	private Date temps;
 	private long nombre;
 	
 	public Commande (Cursor cursor) {
 		
 		this.temps = new Date(cursor.getLong(HorecaContract.Commande.TEMPS_INDEX));
 		this.nombre = cursor.getLong(HorecaContract.Commande.NOMBRE_INDEX);
 		/*
 		name = cursor.getString(HorecaContract.Horeca.NAME_INDEX);
 		minPrice = cursor.getLong(HorecaContract.Horeca.MIN_PRICE_INDEX) / 100.;
 		*/
 	}
 	
 	public Commande(SQLiteDatabase db, long id) {
 		Cursor cursor = getCursor(db, HorecaContract.Commande._ID + " = ?",
 				new String[]{String.valueOf(id)});
 		cursor.moveToFirst();
 		this.id = id;
 		long user_id = cursor.getLong(HorecaContract.Commande.USER_ID_INDEX);
 		long plat_id = cursor.getLong(HorecaContract.Commande.PLAT_ID_INDEX);
 		this.temps = new Date(cursor.getLong(HorecaContract.Commande.TEMPS_INDEX));
 		this.nombre = cursor.getLong(HorecaContract.Commande.NOMBRE_INDEX);
 		cursor.close();
 		this.user = new User(user_id, db);
 		this.plat = new Plat(plat_id, db);
 	}
 	private Commande(long id, User user, Plat plat, Date temps, long nombre) {
 		this.id = id;
 		this.user = user;
 		this.plat = plat;
 		this.temps = temps;
 		this.nombre = nombre;
 	}
 	public long getId() {
 		return id;
 	}
 	public User getUser() {
 		return user;
 	}
 	public Plat getOuverture() {
 		return plat;
 	}
 	public Date getTemps() {
 		return temps;
 	}
 	public long getNombre() {
 		return nombre;
 	}
 	public void destroy(SQLiteDatabase db) {
 		if (plat.hasStock()) {
 			plat.setStock(db, plat.getStock() + nombre);
 		}
 		db.delete(HorecaContract.Commande.TABLE_NAME, HorecaContract.Commande._ID + " = ?",
 				new String[]{String.valueOf(id)});
 	}
 	public static ArrayList<String> getAllCommandsTime(SQLiteDatabase db, Plat plat) //renvoie une liste contenant l'heure de chaque commande
 	    {
 	        ArrayList<String> times = new ArrayList<String>(); //nouvelle instance
 	        Cursor c = getAllCommandeForPlat(db, plat);
 	        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) 
 	        {
 	        	String s = "Commande n. " + c.getString(0) + "   Date : " + c.getString(3) + "   Nombre : " + c.getString(4);
 	            times.add(s); // on rajoute le temps a la liste
 	        }
 	        return times; 
 	    }
 }
