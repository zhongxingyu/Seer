 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Application;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.CharArrayBuffer;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.database.DataSetObserver;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import cm.aptoide.pt.Server.State;
 import cm.aptoide.pt.sharing.DialogBaseShareListener;
 import cm.aptoide.pt.views.ViewApk;
 import cm.aptoide.pt.views.ViewApkFeaturedEditorsChoice;
 import cm.aptoide.pt.views.ViewApkFeaturedTop;
 import cm.aptoide.pt.views.ViewApkInfoXml;
 import cm.aptoide.pt.views.ViewApkItemBased;
 import cm.aptoide.pt.views.ViewApkLatest;
 import cm.aptoide.pt.views.ViewApkTop;
 import cm.aptoide.pt.views.ViewApkUserBased;
 import cm.aptoide.pt.views.ViewLogin;
 import cm.aptoide.pt.DbStructure;
 
 public class Database {
 	public static SQLiteDatabase database = null;
 	private DbStructure dbhandler;
 	private int i = 0;
 	static Context context = ApplicationAptoide.getContext();
 
 	private static final String COLUMN_A = "A.";
 	private static final String COLUMN_A_NAME = "A";
 
 	private static final String COLUMN_B = "B.";
 	private static final String COLUMN_B_NAME = "B";
 
 	private static final String COLUMN_C = "C.";
 	private static final String COLUMN_C_NAME = "C";
 
 	public static final int TOP_ID = 0;
 	public static final int LATEST_ID = 1;
 
 	private HashMap<String, Integer> categories1 = new HashMap<String, Integer>();
 	private HashMap<String, Integer> categories2 = new HashMap<String, Integer>();
 	private Object lock = new Object();
 	private Object lock2 = new Object();
 
 	private Database() {
 		dbhandler = new DbStructure(context);
 		database = dbhandler.getWritableDatabase();
 	}
 
 	private static class SingletonHolder {
 		public static final Database INSTANCE = new Database();
 	}
 
 	public static Database getInstance() {
 		return SingletonHolder.INSTANCE;
 	}
 
 	public void insert(ViewApkFeaturedTop apk) {
 		// database.beginTransaction();
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_REPO_ID, apk.getRepo_id());
 		values.put(DbStructure.COLUMN_CATEGORY_2ND, apk.getCategory2());
 		putCommonValues(apk, values);
 		long id = database.insert(DbStructure.TABLE_FEATURED_TOP_APK, null,
 				values);
 		apk.setId(id);
 		insertScreenshots(apk, Category.TOPFEATURED);
 		// database.yieldIfContendedSafely(1000);
 		// database.setTransactionSuccessful();
 		// database.endTransaction();
 	}
 
 	public void insert(ViewApkFeaturedEditorsChoice apk) {
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_ICONS_PATH, apk.getServer().iconsPath);
 		values.put(DbStructure.COLUMN_SCREENS_PATH, apk.getServer().screenspath);
 		values.put(DbStructure.COLUMN_FEATURED_GRAPHICS_PATH,
 				apk.getServer().featuredgraphicPath);
 		values.put(DbStructure.COLUMN_NAME, apk.getServer().url);
 		values.put(DbStructure.COLUMN_BASE_PATH, apk.getServer().basePath);
 		long i = database.insert(DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 				null, values);
 		values.clear();
 		values.put(DbStructure.COLUMN_REPO_ID, i);
 		values.put(DbStructure.COLUMN_FEATURED_HIGHLIGHTED, apk.isHighlighted());
 		values.put(DbStructure.COLUMN_FEATURED_GRAPHIC, apk.getFeatureGraphic());
 		putCommonValues(apk, values);
 		long id = database.insert(DbStructure.TABLE_FEATURED_EDITORSCHOICE_APK,
 				null, values);
 		apk.setId(id);
 		insertScreenshots(apk, Category.EDITORSCHOICE);
 	}
 
 	public void insert(ViewApkLatest apk) {
 		try {
 			ContentValues values = new ContentValues();
 
 			values.put(DbStructure.COLUMN_REPO_ID, apk.getRepo_id());
 			values.put(DbStructure.COLUMN_CATEGORY_2ND, apk.getCategory2());
 			putCommonValues(apk, values);
 			yield();
 			long id = database.insert(DbStructure.TABLE_LATEST_APK, null,
 					values);
 			yield();
 			insertDynamicCategories(apk, Category.LATEST);
 			apk.setId(id);
 			insertScreenshots(apk, Category.LATEST);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void insert(ViewApkTop apk) {
 		try {
 
 			ContentValues values = new ContentValues();
 			values.put(DbStructure.COLUMN_REPO_ID, apk.getRepo_id());
 			values.put(DbStructure.COLUMN_CATEGORY_2ND, apk.getCategory2());
 			putCommonValues(apk, values);
 			yield();
 			long id = database.insert(DbStructure.TABLE_TOP_APK, null, values);
 			insertDynamicCategories(apk, Category.TOP);
 			apk.setId(id);
 			insertScreenshots(apk, Category.TOP);
 			i++;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@SuppressLint("NewApi")
 	public void insert(ViewApkInfoXml apk) {
 		
 			ContentValues values = new ContentValues();
 			insertCategories(apk);
 			values.put(DbStructure.COLUMN_REPO_ID, apk.getRepo_id());
 			values.put(DbStructure.COLUMN_CATEGORY_2ND,
 					categories2.get(apk.getCategory2()));
 			putCommonValues(apk, values);
 			database.insert(DbStructure.TABLE_APK, null, values);
 			yield();
 			i++;
 			if (i % 300 == 0) {
 				Intent i = new Intent("update");
 				i.putExtra("server", apk.getRepo_id());
 				context.sendBroadcast(i);
 			}
 	}
 
 	public void insert(ViewApkUserBased apk) {
 		ContentValues values = new ContentValues();
 		putCommonValues(apk, values);
 		database.insert(DbStructure.TABLE_APK, null, values);
 		yield();
 	}
 
 	public void insert(ViewApkItemBased apk) {
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_ICONS_PATH, apk.getServer().iconsPath);
 		values.put(DbStructure.COLUMN_SCREENS_PATH, apk.getServer().screenspath);
 		values.put(DbStructure.COLUMN_NAME, apk.getServer().url);
 		values.put(DbStructure.COLUMN_BASE_PATH, apk.getServer().basePath);
 		long i = database.insert(DbStructure.TABLE_ITEMBASED_REPO, null, values);
 		yield();
 		values.clear();
 		values.put("repo_id", i);
 		values.put("parent_apkid", apk.getParentApk().getApkid());
 		putCommonValues(apk, values);
 		long id = database.insert(DbStructure.TABLE_ITEMBASED_APK, null, values);
 		yield();
 		apk.setRepo_id(i);
 		apk.setId(id);
 		insertScreenshots(apk, Category.ITEMBASED);
 		yield();
 	}
 
 	// public void insert(ViewApk apk) {
 	//
 	// try{
 	//
 	//
 	//
 	// ContentValues values = new ContentValues();
 	// insertCategories(apk);
 	// values.put(DbStructure.COLUMN_APKID, apk.getApkid());
 	// values.put(DbStructure.COLUMN_ICON, apk.getIcon());
 	// values.put(DbStructure.COLUMN_NAME, apk.getName());
 	// values.put(DbStructure.COLUMN_SIZE, apk.getSize());
 	// values.put(DbStructure.COLUMN_DOWNLOADS, apk.getDownloads());
 	// values.put(DbStructure.COLUMN_VERNAME, apk.getVername());
 	// values.put(DbStructure.COLUMN_VERCODE, apk.getVercode());
 	// values.put(DbStructure.COLUMN_MD5, apk.getMd5());
 	// values.put("repo_id", apk.getRepo_id());
 	// values.put("date",apk.getDate());
 	// values.put(DbStructure.COLUMN_RATING, apk.getRating());
 	// values.put(DbStructure.COLUMN_REMOTE_PATH, apk.getPath());
 	// values.put(DbStructure.COLUMN_MIN_SCREEN, apk.getMinScreen());
 	// values.put(DbStructure.COLUMN_MIN_SDK, apk.getMinSdk());
 	// values.put(DbStructure.COLUMN_MIN_GLES, apk.getMinGlEs());
 	// values.put(DbStructure.COLUMN_MATURE, apk.getAge());
 	// values.put(DbStructure.COLUMN_CATEGORY_2ND,
 	// categories2.get(apk.getCategory2()));
 	//
 	// database.insert(DbStructure.TABLE_APK, null, values);
 	//
 	//
 	// i++;
 	//
 	// if(i%300==0){
 	// Intent i = new Intent("update");
 	// i.putExtra("server", apk.getRepo_id());
 	// context.sendBroadcast(i);
 	// }
 	//
 	//
 	// database.yieldIfContendedSafely(1000);
 	//
 	// } catch (Exception e){
 	// e.printStackTrace();
 	// }finally{
 	//
 	// }
 
 	// }
 
 	private void insertCategories(ViewApk apk) {
 			ContentValues values = new ContentValues();
 			values.put(DbStructure.COLUMN_NAME, apk.getCategory1());
 			database.insert("category_1st", null, values);
 			if (categories1.get(apk.getCategory1()) == null) {
 				Cursor c = database.query("category_1st",
 						new String[] { "_id" }, "name = ?",
 						new String[] { apk.getCategory1() }, null, null, null);
 				c.moveToFirst();
 				categories1.put(apk.getCategory1(), c.getInt(0));
 				c.close();
 			}
 			if (apk.getCategory1().equals("Other")) {
 				System.out.println(apk.getApkid());
 			}
 
 			values.clear();
 			values.put("repo_id", apk.getRepo_id());
 			values.put("category_1st_id", categories1.get(apk.getCategory1()));
 			database.insert("repo_category_1st", null, values);
 			values.clear();
 
 			values.put("category_1st_id", categories1.get(apk.getCategory1()));
 			values.put(DbStructure.COLUMN_NAME, apk.getCategory2());
 			database.insert(DbStructure.TABLE_CATEGORY_2ND, null, values);
 			if (categories2.get(apk.getCategory2()) == null) {
 				Cursor c = database.query(DbStructure.TABLE_CATEGORY_2ND,
 						new String[] { "_id" }, "name = ?",
 						new String[] { apk.getCategory2() }, null, null, null);
 				c.moveToFirst();
 				categories2.put(apk.getCategory2(), c.getInt(0));
 				c.close();
 			}
 			values.clear();
 			values.put("repo_id", apk.getRepo_id());
 			values.put("category_2nd_id", categories2.get(apk.getCategory2()));
 			database.insert(DbStructure.TABLE_REPO_CATEGORY_2ND, null, values);
 	}
 
 	private void insertDynamicCategories(ViewApk apk, Category category) {
 		ContentValues values = new ContentValues();
 		values.put("repo_id", apk.getRepo_id());
 		switch (category) {
 		case TOP:
 			values.put(DbStructure.COLUMN_CATEGORY_1ST_ID, TOP_ID);
 			break;
 		case LATEST:
 			values.put(DbStructure.COLUMN_CATEGORY_1ST_ID, LATEST_ID);
 			break;
 		default:
 			break;
 		}
 		database.insert(DbStructure.TABLE_REPO_CATEGORY_1ST, null, values);
 	}
 
 	@SuppressLint("NewApi")
 	public void startTransation() {
 		// context.sendBroadcast(new Intent("status"));
 		System.out.println("Starting transaction");
 		database.beginTransaction();
 	}
 
 	public void endTransation(Server server) {
 		Intent intent = new Intent("status");
 		intent.putExtra("server", server.url);
 
 		if (i != 0 && server.id > 0) {
 			context.sendBroadcast(intent);
 			intent.setAction("update");
 			context.sendBroadcast(intent);
 		}
 		intent.setAction("complete");
 		context.sendBroadcast(intent);
 		
 		System.out.println("Endind transaction");
 		database.setTransactionSuccessful();
 		database.endTransaction();
 	}
 
 	public Cursor getApps(long category2_id, long store, boolean mergeStores,
 			Order order, boolean allApps) {
 		Cursor c = null;
 		yield();
 		SharedPreferences sPref = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		try {
 
 			String order_string = "";
 			switch (order) {
 
 			case NAME:
 				order_string = "order by a.name collate nocase";
 				break;
 			case DATE:
 				order_string = "order by date desc";
 				break;
 			case DOWNLOADS:
 				order_string = "order by a.downloads desc";
 				break;
 			case RATING:
 				order_string = "order by rating desc";
 				break;
 			default:
 				break;
 			}
 
 			String filter = "";
 			if (sPref.getBoolean("hwspecsChkBox", true)) {
 				filter = filter + " and minscreen <= "
 						+ HWSpecifications.getScreenSize(context)
 						+ " and minsdk <=  " + HWSpecifications.getSdkVer()
 						+ " and mingles <= "
 						+ HWSpecifications.getEsglVer(context);
 			}
 
 			if (sPref.getBoolean("matureChkBox", false)) {
 				filter = filter + " and mature <= 0";
 			}
 
 			if (allApps) {
 				if (mergeStores) {
 
 					c = database
 							.rawQuery(
 									"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from apk as a, repo as c where vercode = (select max(vercode) from apk as b where a.apkid=b.apkid) and a.repo_id = c._id "
 											+ filter
 											+ " group by apkid "
 											+ order_string, null);
 
 				} else {
 
 					c = database
 							.rawQuery(
 									"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from apk as a, repo as c where repo_id = ? and vercode in (select vercode from apk as b where a.apkid=b.apkid order by vercode asc) and a.repo_id = c._id "
 											+ filter
 											+ " group by apkid "
 											+ order_string,
 									new String[] { store + "" });
 				}
 
 			} else {
 				if (mergeStores) {
 
 					c = database
 							.rawQuery(
 									"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads,a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from apk as a, repo as c where category_2nd = ? and vercode = (select max(vercode) from apk as b where a.apkid=b.apkid) and a.repo_id = c._id "
 											+ filter
 											+ " group by apkid "
 											+ order_string,
 									new String[] { category2_id + "" });
 
 				} else {
 
 					c = database
 							.rawQuery(
 									"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from apk as a, repo as c where repo_id = ? and category_2nd = ? and vercode in (select vercode from apk as b where a.apkid=b.apkid order by vercode asc) and a.repo_id = c._id "
 											+ filter
 											+ " group by apkid "
 											+ order_string, new String[] {
 											store + "", category2_id + "" });
 
 				}
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return c;
 	}
 
 	public Cursor getCategory1(long store, boolean mergeStores, boolean allApps) {
 		Cursor d = null;
 		yield();
 		if (mergeStores) {
 			d = database
 					.rawQuery(
 							"select a._id,a.name from category_1st as a order by a._id",
 							null);
 		} else {
 			d = database
 					.rawQuery(
 							"select a._id,a.name from category_1st as a , repo_category_1st as b where a._id=b.category_1st_id and b.repo_id = ? order by a._id",
 							new String[] { store + "" });
 		}
 
 		System.out.println("Getting category1 " + store);
 
 		MatrixCursor c = new MatrixCursor(new String[] { "_id",
 				DbStructure.COLUMN_NAME });
 		ArrayList<Holder> a = new ArrayList<Database.Holder>();
 
 		for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
 			if (d.getString(1).equals("Top Apps")
 					|| d.getString(1).equals("Latest Apps")) {
 				Holder holder = new Holder();
 				holder.id = d.getLong(0);
 				holder.name = d.getString(1);
 				a.add(holder);
 			} else if (!allApps) {
 				c.addRow(new Object[] { d.getString(0), d.getString(1) });
 			}
 		}
 		boolean hasCategory = false;
 
 		if (!mergeStores && !allApps
 				&& !getServer(store, false).state.equals(State.PARSED)
 				&& !getServer(store, false).state.equals(State.FAILED)) {
 			for (d.moveToFirst(); !d.isAfterLast(); d.moveToNext()) {
 				if (d.getString(1).equals("Applications")
 						|| d.getString(1).equals("Games")) {
 					hasCategory = true;
 					break;
 				}
 			}
 		} else {
 			hasCategory = true;
 		}
 		if (!hasCategory) {
 			c.newRow().add(-10).add("Applications");
 			c.newRow().add(-10).add("Games");
 		}
 		Collections.sort(a);
 
 		if (allApps) {
 			c.addRow(new Object[] { -4, "All Applications" });
 		}
 
 		for (Holder holder : a) {
 			c.addRow(new Object[] { holder.id, holder.name });
 		}
 
 		if (!mergeStores) {
 			c.addRow(new Object[] { -2, "Latest Comments" });
 			c.addRow(new Object[] { -1, "Latest Likes" });
 		}
 
 		c.addRow(new Object[] { -3, "Recommended for you" });
 
 		d.close();
 
 		return c;
 	}
 
 	static class Holder implements Comparable<Holder> {
 		long id;
 		String name;
 
 		@Override
 		public int compareTo(Holder another) {
 			return another.name.compareTo(name);
 		}
 	}
 
 	public void insertUserBasedApk(ViewApk apk) {
 		ContentValues values = new ContentValues();
 
 		values.put(DbStructure.COLUMN_APKID, apk.getApkid());
 		values.put(DbStructure.COLUMN_VERCODE, apk.getVercode());
 
 		database.insert("userbasedapk", null, values);
 	}
 
 	public Cursor getUserBasedApk(long repo_id, boolean joinStores) {
 
 		
 		yield();
 		SharedPreferences sPref = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		String filter = "";
 		if (sPref.getBoolean("hwspecsChkBox", true)) {
 			filter = filter + " and b.minscreen <= "
 					+ HWSpecifications.getScreenSize(context)
 					+ " and b.minsdk <=  " + HWSpecifications.getSdkVer()
 					+ " and b.mingles <= " + HWSpecifications.getEsglVer(context);
 		}
 
 		if (sPref.getBoolean("matureChkBox", false)) {
 			filter = filter + " and b.mature <= 0 ";
 		}
 		
 		Cursor c = null;
 
 		try {
 			if(joinStores){
 			c = database
 					.rawQuery(
 							"select a._id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid, a.vercode, c.iconspath as iconspath from apk a,itembased_apk b, itembased_repo as c where b.parent_apkid = 'recommended' and a.apkid=b.apkid and a.vercode=b.vercode and b.repo_id=c._id " +filter,null);
 			}else{
 			c = database
 					.rawQuery(
 							"select a._id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid, a.vercode, c.iconspath as iconspath from apk a,itembased_apk b, itembased_repo as c where a.repo_id = ? and b.parent_apkid = 'recommended' and a.apkid=b.apkid and a.vercode=b.vercode and b.repo_id=c._id " +filter,
 							new String[] { repo_id + "" });
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return c;
 	}
 
 	public Cursor getCategory2(long l, long store, boolean mergeStores) {
 		Cursor c = null;
 		yield();
 		if (mergeStores) {
 			c = database
 					.rawQuery(
 							"select a._id,a.name from category_2nd as a where a.category_1st_id = ? order by a.name",
 							new String[] { l + "" });
 		} else {
 			c = database
 					.rawQuery(
 							"select a._id,a.name from category_2nd as a , repo_category_2nd as b where a._id=b.category_2nd_id and a.category_1st_id = ? and b.repo_id = ? order by a.name",
 							new String[] { l + "", store + "" });
 		}
 		System.out.println("Getting category2: " + l + " store: " + store);
 		return c;
 	}
 
 	public Server getServer(String uri) {
 		Server server = null;
 		Cursor c = null;
 		yield();
 		try {
 			c = database.query("repo",
 					new String[] { "_id, url, hash , username, password" },
 					"url = ?", new String[] { uri }, null, null, null);
 			if (c.moveToFirst()) {
 				server = new Server(c.getString(1), c.getString(2),
 						c.getLong(0));
 				ViewLogin login = new ViewLogin(c.getString(3), c.getString(4));
 				server.setLogin(login);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return server;
 	}
 
 	public Server getServer(long id, boolean top) {
 		Server server = null;
 		Cursor c = null;
 		yield();
 		try {
 			if (top) {
 				c = database
 						.query("toprepo_extra",
 								new String[] { "_id, url, top_delta, status, username, password" },
 								"_id = ?", new String[] { id + "" }, null,
 								null, null);
 			} else {
 				c = database
 						.query("repo",
 								new String[] { "_id, url, hash, status, username, password" },
 								"_id = ?", new String[] { id + "" }, null,
 								null, null);
 			}
 
 			if (c.moveToFirst()) {
 				server = new Server(c.getString(1), c.getString(2),
 						c.getLong(0));
 				server.state = State.valueOf(c.getString(3));
 				ViewLogin login = new ViewLogin(c.getString(3), c.getString(4));
 				server.setLogin(login);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return server;
 	}
 
 	public void deleteTopOrLatest(long id, Category category) {
 		try {
 			switch (category) {
 			case TOP:
 				database.delete(DbStructure.TABLE_TOP_APK, "repo_id = ?",
 						new String[] { id + "" });
 				database.delete(DbStructure.TABLE_TOP_SCREENSHOTS,
 						"repo_id = ?", new String[] { id + "" });
 				database.delete(DbStructure.TABLE_TOP_REPO, "_id = ?",
 						new String[] { id + "" });
 				break;
 			case LATEST:
 				database.delete(DbStructure.TABLE_LATEST_APK, "repo_id = ?",
 						new String[] { id + "" });
 				database.delete(DbStructure.TABLE_LATEST_SCREENSHOTS,
 						"repo_id = ?", new String[] { id + "" });
 				database.delete(DbStructure.TABLE_LATEST_REPO, "_id = ?",
 						new String[] { id + "" });
 				break;
 			default:
 				break;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void deleteFeatured(long id, Category category) {
 		switch (category) {
 		case TOPFEATURED:
 			database.delete(DbStructure.TABLE_FEATURED_TOP_APK, "repo_id = ?",
 					new String[] { id + "" });
 			database.delete(DbStructure.TABLE_FEATURED_TOP_SCREENSHOTS,
 					"repo_id = ?", new String[] { id + "" });
 			database.delete(DbStructure.TABLE_FEATURED_TOP_REPO, "_id = ?",
 					new String[] { id + "" });
 			break;
 		case EDITORSCHOICE:
 			database.delete(DbStructure.TABLE_FEATURED_EDITORSCHOICE_APK,
 					"repo_id = ?", new String[] { id + "" });
 			database.delete(
 					DbStructure.TABLE_FEATURED_EDITORSCHOICE_SCREENSHOTS,
 					"repo_id = ?", new String[] { id + "" });
 			database.delete(DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 					"_id = ?", new String[] { id + "" });
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void deleteServer(long id, boolean fromRepoTable) {
 		// ArrayList<String> catg1_deletes = new ArrayList<String>();
 		// ArrayList<String> catg2_deletes = new ArrayList<String>();
 		if (fromRepoTable) {
 			database.delete("repo", "_id = ?", new String[] { id + "" });
 			deleteTopOrLatest(id, Category.TOP);
 			deleteTopOrLatest(id, Category.LATEST);
 			database.delete("repo_category_1st", "repo_id = ?",
 					new String[] { id + "" });
 			database.delete("repo_category_2nd", "repo_id = ?",
 					new String[] { id + "" });
 		}
 		database.delete(DbStructure.TABLE_APK, "repo_id = ?", new String[] { id
 				+ "" });
 
 		// Cursor c = database.query("category_1st", new String[]{"_id"}, null,
 		// null, null, null, null);
 		// Cursor d = null;
 		// for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
 		//
 		// d = database.query("repo_category_1st", new String[]{"repo_id"},
 		// "category_1st_id=?", new String[]{c.getString(0)}, null, null, null);
 		//
 		// if(d.getCount()==0){
 		// catg1_deletes.add(c.getString(0));
 		// }
 		//
 		// }
 		// c = database.query(DbStructure.COLUMN_CATEGORY_2ND, new
 		// String[]{"_id"}, null, null, null, null, null);
 		// for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
 		// d = database.query("repo_category_2nd", new String[]{"repo_id"},
 		// "category_2nd_id=?", new String[]{c.getString(0)}, null, null, null);
 		// if(d.getCount()==0){
 		// catg2_deletes.add(c.getString(0));
 		// }
 		//
 		// }
 		//
 		// for(String s : catg1_deletes){
 		// if(!s.equals("Top Apps")&&!s.equals("Latest Apps")){
 		// database.delete("category_1st", "_id = ?", new String[]{s});
 		// }
 		// }
 		//
 		// for(String s : catg2_deletes){
 		// database.delete(DbStructure.COLUMN_CATEGORY_2ND, "_id = ?", new
 		// String[]{s});
 		// }
 		// c.close();
 		// if(d!=null){
 		// d.close();
 		// }
 
 	}
 
 	public void addStore(String string, String username, String password) {
 		try {
 			ContentValues values = new ContentValues();
 			values.put("url", string);
 			values.put("username", username);
 			values.put("password", password);
 			database.insert("repo", null, values);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public Cursor getStores(boolean mergeStores) {
 		yield();
 		Cursor c = null;
 		try {
 			c = database.query("repo", null, "_id > 0", null, null, null, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 
 		}
 		if (mergeStores && c.getCount() > 0) {
 			MatrixCursor mc = new MatrixCursor(new String[] { "_id",
 					DbStructure.COLUMN_NAME, DbStructure.COLUMN_AVATAR_URL,
 					DbStructure.COLUMN_DOWNLOADS, "status" });
 			mc.newRow()
 					.add(-1)
 					.add("All Stores")
 					.add("http://imgs.aptoide.com/includes/themes/default/images/repo_default_icon.png")
 					.add("").add("");
 			c.close();
 			return mc;
 		} else {
 			return c;
 		}
 
 	}
 
 	public String getStoreName(long repo_id) {
 		String return_string = null;
 		Cursor c = null;
 		yield();
 		try {
 			c = database.query("repo",
 					new String[] { DbStructure.COLUMN_NAME }, "_id = ?",
 					new String[] { repo_id + "" }, null, null, null);
 
 			if (c.moveToFirst()) {
 				return_string = c.getString(c
 						.getColumnIndex(DbStructure.COLUMN_NAME));
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return return_string;
 	}
 
 	public long getServerId(String server) {
 		System.out.println(server);
 		yield();
 		Cursor c = database.query("repo", new String[] { "_id" }, "url = ?",
 				new String[] { server }, null, null, null);
 		c.moveToFirst();
 		long return_long = c.getLong(0);
 		c.close();
 
 		return return_long;
 	}
 
 	public String getBasePath(long repo_id, Category category) {
 		yield();
 		Cursor c = null;
 		String path = null;
 		try {
 
 			switch (category) {
 			case EDITORSCHOICE:
 				c = database.query(
 						DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 						new String[] { DbStructure.COLUMN_BASE_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case INFOXML:
 				c = database.query(DbStructure.TABLE_REPO,
 						new String[] { DbStructure.COLUMN_BASE_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case ITEMBASED:
 			case USERBASED:
 				c = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 						new String[] { DbStructure.COLUMN_BASE_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case LATEST:
 				c = database.query(DbStructure.TABLE_LATEST_REPO,
 						new String[] { DbStructure.COLUMN_BASE_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case TOP:
 				c = database.query(DbStructure.TABLE_TOP_REPO,
 						new String[] { DbStructure.COLUMN_BASE_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case TOPFEATURED:
 				c = database.query(DbStructure.TABLE_FEATURED_TOP_REPO,
 						new String[] { DbStructure.COLUMN_BASE_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			default:
 				break;
 			}
 			if (c.moveToFirst()) {
 				path = c.getString(0);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return path;
 	}
 
 	public void insertServerInfo(Server server) {
 
 		ContentValues values = new ContentValues();
 		if (server.iconsPath != null) {
 			values.put(DbStructure.COLUMN_ICONS_PATH, server.iconsPath);
 		}
 		if (server.basePath != null) {
 			values.put(DbStructure.COLUMN_BASE_PATH, server.basePath);
 		}
 		if (server.webservicesPath != null) {
 			values.put("webservicespath", server.webservicesPath);
 		}
 		if (server.hash != null) {
 			values.put("hash", server.hash);
 		}
 		if (server.apkPath != null) {
 			values.put("apkpath", server.apkPath);
 		}
 		if (values.size() > 0) {
 			database.update("repo", values, "_id = ?", new String[] { server.id
 					+ "" });
 		}
 
 	}
 
 	public void insertInstalled(ViewApk apk) {
 		ContentValues values = new ContentValues();
 
 		values.put(DbStructure.COLUMN_APKID, apk.getApkid());
 		values.put(DbStructure.COLUMN_VERCODE, apk.getVercode());
 		values.put(DbStructure.COLUMN_VERNAME, apk.getVername());
 		values.put(DbStructure.COLUMN_NAME, apk.getName());
 
 		database.insert(DbStructure.TABLE_INSTALLED, null, values);
 
 	}
 
 	public List<String> getStartupInstalled() {
 
 		Cursor c = null;
 		List<String> apkids = new ArrayList<String>();
 		try {
 			c = database.query(DbStructure.TABLE_INSTALLED,
 					new String[] { DbStructure.COLUMN_APKID }, null, null,
 					null, null, null);
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				apkids.add(c.getString(0));
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return apkids;
 	}
 
 	public Cursor getInstalledApps(Order order) {
 		yield();
 		Cursor c = null;
 		try {
 
 			String query = "select b._id as _id, a.name,a.vername,b.repo_id,b.icon,b.rating,b.downloads,b.apkid as apkid,b.vercode as vercode, c.iconspath from installed as a, apk as b, repo as c where a.apkid=b.apkid and b.repo_id=c._id group by a.apkid";
 
 			query = orderBy(order, query);
 
 			c = database.rawQuery(query, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return c;
 	}
 
 	public Cursor getUpdates(Order order) {
 		yield();
 		Cursor c = null;
 		try {
 
 			String filter = filters();
 
 			String query = "select b._id as _id, b.name,b.vername,b.repo_id,b.icon as imagepath,b.rating,b.downloads,b.apkid as apkid,b.vercode as vercode, c.iconspath as iconspath, b.md5, c.apkpath, b.remote_path from installed as a, apk as b, repo as c "
 					+ "where a.apkid=b.apkid and b.vercode > a.vercode and b.vercode = (select max(vercode) from apk as b where a.apkid=b.apkid) and b.repo_id = c._id and not exists (select 1 from excluded_apkid as d where b.apkid = d.apkid and b.vercode = d.vercode ) "
 					+ filter + " group by a.apkid";
 
 			query = orderBy(order, query);
 
 			c = database.rawQuery(query, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return c;
 	}
 
 	/**
 	 * @return
 	 */
 	private String filters() {
 		SharedPreferences sPref = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		String filter = "";
 		if (sPref.getBoolean("hwspecsChkBox", true)) {
 			filter = filter + " and minscreen <= "
 					+ HWSpecifications.getScreenSize(context)
 					+ " and minsdk <=  " + HWSpecifications.getSdkVer()
 					+ " and mingles <= " + HWSpecifications.getEsglVer(context);
 		}
 
 		if (sPref.getBoolean("matureChkBox", false)) {
 			filter = filter + " and mature <= 0 ";
 		}
 		return filter;
 	}
 
 	/**
 	 * @param order
 	 * @param query
 	 * @return
 	 */
 	private String orderBy(Order order, String query) {
 		switch (order) {
 		case NAME:
 			query = query + " order by b.name collate nocase";
 			break;
 		case DATE:
 			query = query + " order by b.date desc";
 			break;
 		case DOWNLOADS:
 			query = query + " order by b.downloads desc";
 			break;
 		case RATING:
 			query = query + " order by b.rating desc";
 			break;
 		default:
 			break;
 		}
 		return query;
 	}
 
 	public void updateStatus(Server server) {
 		ContentValues values = new ContentValues();
 		if (server.hash != null) {
 			values.put("hash", server.hash);
 		}
 		values.put("status", server.state.name());
 		database.update("repo", values, "url =?", new String[] { server.url });
 		context.sendBroadcast(new Intent("status"));
 	}
 
 	long insertTopApk(ViewApk apk) {
 		long return_long = 0;
 		try {
 			ContentValues values = new ContentValues();
 
 			values.put(DbStructure.COLUMN_REPO_ID, apk.getRepo_id());
 			values.put(DbStructure.COLUMN_CATEGORY_2ND, apk.getCategory2());
 
 			putCommonValues(apk, values);
 
 			return_long = database.insert(DbStructure.TABLE_TOP_APK, null,
 					values);
 			i++;
 			// database.yieldIfContendedSafely(1000);
 			// if(i%300==0){
 			// Intent i = new Intent("update");
 			// i.putExtra("server", apk.getRepo_id());
 			// context.sendBroadcast(i);
 			// }
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return return_long;
 
 	}
 
 	/**
 	 * @param apk
 	 * @param values
 	 */
 	private void putCommonValues(ViewApk apk, ContentValues values) {
 		values.put(DbStructure.COLUMN_NAME, apk.getName());
 		values.put(DbStructure.COLUMN_VERNAME, apk.getVername());
 		values.put(DbStructure.COLUMN_APKID, apk.getApkid());
 		values.put(DbStructure.COLUMN_MD5, apk.getMd5());
 		values.put(DbStructure.COLUMN_VERCODE, apk.getVercode());
 		values.put(DbStructure.COLUMN_ICON, apk.getIcon());
 		values.put(DbStructure.COLUMN_DOWNLOADS, apk.getDownloads());
 		values.put(DbStructure.COLUMN_SIZE, apk.getSize());
 		values.put(DbStructure.COLUMN_RATING, apk.getRating());
 		values.put(DbStructure.COLUMN_REMOTE_PATH, apk.getPath());
 		values.put(DbStructure.COLUMN_MIN_SCREEN, apk.getMinScreen());
 		values.put(DbStructure.COLUMN_MIN_SDK, apk.getMinSdk());
 		values.put(DbStructure.COLUMN_MIN_GLES, apk.getMinGlEs());
 		values.put(DbStructure.COLUMN_MATURE, apk.getAge());
 	}
 
 	public Cursor getTopApps(long store_id, boolean joinStores_boolean) {
 		yield();
 		Cursor c = null;
 		try {
 			String filter = "";
 			SharedPreferences sPref = PreferenceManager
 					.getDefaultSharedPreferences(context);
 
 			if (sPref.getBoolean("hwspecsChkBox", true)) {
 				filter = filter + " and minscreen <= "
 						+ HWSpecifications.getScreenSize(context)
 						+ " and minsdk <=  " + HWSpecifications.getSdkVer()
 						+ " and mingles <= "
 						+ HWSpecifications.getEsglVer(context);
 			}
 
 			if (sPref.getBoolean("matureChkBox", false)) {
 				filter = filter + " and mature <= 0";
 			}
 
 			if (joinStores_boolean) {
 				c = database
 						.rawQuery(
 								"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from top_apk as a, top_repo as c where a.repo_id = c._id "
 										+ filter + " group by a._id", null);
 
 				// c = database.query(DbStructure.TABLE_FEATURED_TOP_APK +
 				// " as " + COLUMN_A_NAME + " ,  " +
 				// DbStructure.TABLE_FEATURED_TOP_REPO + " as " + COLUMN_B_NAME,
 				//
 				// new String[]{COLUMN_A + DbStructure.COLUMN__ID, },
 				// DbStructure.COLUMN_CATEGORY_1ST + "=? and " + COLUMN_A +
 				// DbStructure.COLUMN_REPO_ID + "=" +
 				// COLUMN_B+"."+DbStructure.COLUMN__ID, new
 				// String[]{category_id+""}, COLUMN_A+DbStructure.COLUMN__ID,
 				// null, null);
 			} else {
 				c = database
 						.rawQuery(
 								"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from top_apk as a, top_repo as c where repo_id = ? and a.repo_id = c._id "
 										+ filter + " group by a._id",
 								new String[] { store_id + "" });
 			}
 
 			System.out.println("getapps " + "repo_id =" + store_id);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return c;
 	}
 
 	public Cursor getLatestApps(long store_id, boolean joinStores_boolean) {
 		Cursor c = null;
 		yield();
 		try {
 			String filter = "";
 			SharedPreferences sPref = PreferenceManager
 					.getDefaultSharedPreferences(context);
 
 			if (sPref.getBoolean("hwspecsChkBox", true)) {
 				filter = filter + " and minscreen <= "
 						+ HWSpecifications.getScreenSize(context)
 						+ " and minsdk <=  " + HWSpecifications.getSdkVer()
 						+ " and mingles <= "
 						+ HWSpecifications.getEsglVer(context);
 			}
 
 			if (sPref.getBoolean("matureChkBox", false)) {
 				filter = filter + " and mature <= 0";
 			}
 
 			if (joinStores_boolean) {
 				c = database
 						.rawQuery(
 								"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from latest_apk as a, latest_repo as c where a.repo_id = c._id "
 										+ filter + " group by a._id", null);
 
 				// c = database.query(DbStructure.TABLE_FEATURED_TOP_APK +
 				// " as " + COLUMN_A_NAME + " ,  " +
 				// DbStructure.TABLE_FEATURED_TOP_REPO + " as " + COLUMN_B_NAME,
 				//
 				// new String[]{COLUMN_A + DbStructure.COLUMN__ID, },
 				// DbStructure.COLUMN_CATEGORY_1ST + "=? and " + COLUMN_A +
 				// DbStructure.COLUMN_REPO_ID + "=" +
 				// COLUMN_B+"."+DbStructure.COLUMN__ID, new
 				// String[]{category_id+""}, COLUMN_A+DbStructure.COLUMN__ID,
 				// null, null);
 			} else {
 				c = database
 						.rawQuery(
 								"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from latest_apk as a, latest_repo as c where repo_id = ? and a.repo_id = c._id "
 										+ filter + " group by a._id",
 								new String[] { store_id + "" });
 			}
 
 			System.out.println("getapps " + "repo_id =" + store_id);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return c;
 	}
 
 	public void remove(ViewApk apk, Server server) {
 		database.delete(DbStructure.TABLE_APK, "repo_id = ? and apkid=?",
 				new String[] { server.id + "", apk.getApkid() });
 	}
 
 	public String getIconsPath(long repo_id, Category category) {
 		Cursor c = null;
 		String path = null;
 		yield();
 		try {
 			switch (category) {
 			case EDITORSCHOICE:
 				c = database.query(
 						DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH }, null,
 						null, null, null, null);
 				break;
 			case INFOXML:
 				c = database.query(DbStructure.TABLE_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case ITEMBASED:
 			case USERBASED:
 				c = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case LATEST:
 				c = database.query(DbStructure.TABLE_LATEST_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case TOP:
 				c = database.query(DbStructure.TABLE_TOP_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case TOPFEATURED:
 				c = database.query(DbStructure.TABLE_FEATURED_TOP_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH }, null,
 						null, null, null, null);
 				break;
 			default:
 				break;
 			}
 			c.moveToFirst();
 			if (c.isNull(0)) {
 				path = getBasePath(repo_id, category);
 			} else {
 				path = c.getString(0);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return path;
 	}
 
 	public void insertServerInfo(Server server, Category category) {
 		ContentValues values = new ContentValues();
 		try {
 			if (server.screenspath != null) {
 				values.put(DbStructure.COLUMN_SCREENS_PATH, server.screenspath);
 			}
 			if (!category.equals(Category.INFOXML)) {
 				values.put(DbStructure.COLUMN__ID, server.id);
 			}
 			if (server.hash != null) {
 				values.put(DbStructure.COLUMN_HASH, server.hash);
 			}
 			if (server.url != null) {
 				values.put("url", server.url);
 			}
 			if (server.iconsPath != null) {
 				values.put(DbStructure.COLUMN_ICONS_PATH, server.iconsPath);
 			}
 			if (server.basePath != null) {
 				values.put(DbStructure.COLUMN_BASE_PATH, server.basePath);
 			}
 			if (server.name != null) {
 				values.put(DbStructure.COLUMN_NAME, server.name);
 			}
 			switch (category) {
 			case EDITORSCHOICE:
 				database.insert(DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 						null, values);
 				break;
 			case INFOXML:
 				if (server.apkPath != null) {
 					values.put(DbStructure.COLUMN_APKPATH, server.apkPath);
 				}
 				if (server.webservicesPath != null) {
 					values.put(DbStructure.COLUMN_WEBSERVICESPATH,
 							server.webservicesPath);
 				}
 
 				database.update(DbStructure.TABLE_REPO, values, "_id = ?",
 						new String[] { server.id + "" });
 				break;
 			case ITEMBASED:
 			case USERBASED:
 				database.insert(DbStructure.TABLE_ITEMBASED_REPO, null, values);
 				break;
 			case LATEST:
 				database.insert(DbStructure.TABLE_LATEST_REPO, null, values);
 				break;
 			case TOP:
 				database.insert(DbStructure.TABLE_TOP_REPO, null, values);
 				break;
 			case TOPFEATURED:
 				database.insert(DbStructure.TABLE_FEATURED_TOP_REPO, null,
 						values);
 				break;
 
 			default:
 				break;
 			}
 
 			Log.d("Database", "Updated repo " + category.name() + " with: "
 					+ values);
 
 			// values.put("_id", server.id);
 			// values.put("top_delta", server.top_hash);
 			// values.put("category", category.name().hashCode());
 			//
 			// if(featured){
 			// values.put("url", "http://apps.store.aptoide.com/");
 			// }else{
 			// values.put("url", server.url);
 			// }
 			// values.put(DbStructure.COLUMN_ICONS_PATH, server.iconsPath);
 			// values.put(DbStructure.COLUMN_BASE_PATH,server.basePath);
 			// values.put(DbStructure.COLUMN_NAME,server.name);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public String getRepoHash(long repo_id, Category category) {
 		Cursor c = null;
 		String return_string = "";
 		yield();
 		try {
 			switch (category) {
 			case EDITORSCHOICE:
 				c = database.query(
 						DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 						new String[] { DbStructure.COLUMN_HASH }, "_id = ?",
 						new String[] { repo_id + "" }, null, null, null);
 				break;
 			case INFOXML:
 				c = database.query(DbStructure.TABLE_REPO,
 						new String[] { DbStructure.COLUMN_HASH }, "_id = ?",
 						new String[] { repo_id + "" }, null, null, null);
 				break;
 			case ITEMBASED:
 			case USERBASED:
 				c = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 						new String[] { DbStructure.COLUMN_HASH }, "_id = ?",
 						new String[] { repo_id + "" }, null, null, null);
 				break;
 			case LATEST:
 				c = database.query(DbStructure.TABLE_LATEST_REPO,
 						new String[] { DbStructure.COLUMN_HASH }, "_id = ?",
 						new String[] { repo_id + "" }, null, null, null);
 				break;
 			case TOP:
 				c = database.query(DbStructure.TABLE_TOP_REPO,
 						new String[] { DbStructure.COLUMN_HASH }, "_id = ?",
 						new String[] { repo_id + "" }, null, null, null);
 				break;
 			case TOPFEATURED:
 				c = database.query(DbStructure.TABLE_FEATURED_TOP_REPO,
 						new String[] { DbStructure.COLUMN_HASH }, "_id = ?",
 						new String[] { repo_id + "" }, null, null, null);
 				break;
 			default:
 				break;
 			}
 
 			if (c.moveToFirst()) {
 				return_string = c.getString(0);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return return_string;
 	}
 
 	public void prepare() {
 		i = 0;
 		categories1.clear();
 		categories2.clear();
 	}
 
 	public ViewApk getApk(long id, Category category) {
 		Cursor c = null;
 		ViewApk apk = new ViewApk();
 		Log.d("Aptoide-Database", "Get APK id: " + category.name() + " " + id);
 		yield();
 		switch (category) {
 		case INFOXML:
 			c = database.query("apk as a, repo as c", new String[] { "a.apkid",
 					"a.vername", "a.repo_id", "a.downloads", "a.size",
 					"a.icon", "a.name", "a.rating", "a.remote_path", "a.md5",
 					"c.iconspath", "c.name", "c.apkpath", "a.vercode" },
 					"a._id = ? and a.repo_id = c._id",
 					new String[] { id + "" }, null, null, null);
 			break;
 		case USERBASED:
 		case ITEMBASED:
 			c = database
 					.query("itembased_apk as a, itembased_repo as c",
 							new String[] { "a.apkid", "a.vername", "a.repo_id",
 									"a.downloads", "a.size", "a.icon",
 									"a.name", "a.rating", "a.remote_path",
 									"a.md5", "c.iconspath", "c.name",
 									"c.basepath", "a.vercode" },
 							"a._id = ? and a.repo_id = c._id",
 							new String[] { id + "" }, null, null, null);
 			break;
 		case EDITORSCHOICE:
 			c = database
 					.query("featured_editorschoice_apk as a, featured_editorschoice_repo as c",
 							new String[] { "a.apkid", "a.vername", "a.repo_id",
 									"a.downloads", "a.size", "a.icon",
 									"a.name", "a.rating", "a.remote_path",
 									"a.md5", "c.iconspath", "c.name",
 									"c.basepath", "a.vercode" },
 							"a._id = ? and a.repo_id = c._id",
 							new String[] { id + "" }, null, null, null);
 			break;
 		case TOP:
 			c = database
 					.query("top_apk as a, top_repo as c", new String[] {
 							"a.apkid", "a.vername", "a.repo_id", "a.downloads",
 							"a.size", "a.icon", "a.name", "a.rating",
 							"a.remote_path", "a.md5", "c.iconspath", "c.name",
 							"c.basepath", "a.vercode" },
 							"a._id = ? and a.repo_id = c._id",
 							new String[] { id + "" }, null, null, null);
 			break;
 		case LATEST:
 			c = database
 					.query("latest_apk as a, latest_repo as c", new String[] {
 							"a.apkid", "a.vername", "a.repo_id", "a.downloads",
 							"a.size", "a.icon", "a.name", "a.rating",
 							"a.remote_path", "a.md5", "c.iconspath", "c.name",
 							"c.basepath", "a.vercode" },
 							"a._id = ? and a.repo_id = c._id",
 							new String[] { id + "" }, null, null, null);
 			break;
 		case TOPFEATURED:
 			c = database
 					.query("featured_top_apk as a, featured_top_repo as c",
 							new String[] { "a.apkid", "a.vername", "a.repo_id",
 									"a.downloads", "a.size", "a.icon",
 									"a.name", "a.rating", "a.remote_path",
 									"a.md5", "c.iconspath", "c.name",
 									"c.basepath", "a.vercode" },
 							"a._id = ? and a.repo_id = c._id",
 							new String[] { id + "" }, null, null, null);
 		default:
 			break;
 		}
 
 		try {
 
 			c.moveToFirst();
 			apk.setApkid(c.getString(0));
 			apk.setVername(c.getString(1));
 			apk.setVercode(c.getInt(13));
 			apk.setRepo_id(c.getLong(2));
 			apk.setDownloads(c.getString(3));
 			apk.setSize(c.getString(4));
 			apk.setIconPath(c.getString(10) + c.getString(5));
 			apk.setName(c.getString(6));
 			apk.setRating(c.getString(7));
 			apk.setPath(c.getString(12) + c.getString(8));
 			apk.setMd5(c.getString(9));
 			apk.setRepoName(c.getString(11));
 			apk.setId(id);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return apk;
 	}
 
 	public Cursor getAllApkVersions(String apkid, long id, String vername,
 			boolean b, long repo_id) {
 		Cursor c = null;
 		MatrixCursor mc = new MatrixCursor(
 				new String[] { "_id", DbStructure.COLUMN_APKID,
 						DbStructure.COLUMN_VERNAME, "repo_id" });
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_APK, new String[] {
 					DbStructure.COLUMN__ID, DbStructure.COLUMN_APKID,
 					DbStructure.COLUMN_VERNAME, DbStructure.COLUMN_REPO_ID },
 					"apkid = ? and repo_id != ?", new String[] { apkid,
 							repo_id + "" }, null, null, "vercode desc");
 
 			mc.newRow().add(id).add(apkid).add(vername).add(repo_id);
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				mc.newRow().add(c.getString(0)).add(c.getString(1))
 						.add(c.getString(2)).add(c.getString(3));
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return mc;
 	}
 
 	public String getWebServicesPath(long repo_id, Category category) {
 		Cursor c = null;
 		String return_string = null;
 		yield();
 		
 		try {
 			switch (category) {
 			case EDITORSCHOICE:
 				c = database.query(
 						DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case INFOXML:
 				c = database.query(DbStructure.TABLE_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case ITEMBASED:
 			case USERBASED:
 				c = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case LATEST:
 				c = database.query(DbStructure.TABLE_LATEST_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case TOP:
 				c = database.query(DbStructure.TABLE_TOP_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case TOPFEATURED:
 				c = database.query(DbStructure.TABLE_FEATURED_TOP_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			default:
 				break;
 			}
 			if (c.moveToFirst()) {
 				return_string = c.getString(0);
 			} else {
 				return_string = "http://webservices.aptoide.com/";
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return return_string;
 	}
 
 	/**
 	 * 
 	 */
 	private void yield() {
 		try{
 			if(database.yieldIfContendedSafely(1000)){
 				System.out.println("Yelded at id: " + i);
 			}
 		} catch (Exception e){};
 	}
 
 	public String getWebServicesPath(long repo_id) {
 		Cursor c = null;
 		String return_string = null;
 		yield();
 		try {
 			c = database.query("repo", new String[] { "webservicespath" },
 					"_id = ?", new String[] { repo_id + "" }, null, null, null);
 			c.moveToFirst();
 			if (c.getCount() > 0) {
 				return_string = c.getString(0);
 			} else {
 				return_string = "http://webservices.aptoide.com/";
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return return_string;
 	}
 
 	public void insertItemBasedApk(Server server, ViewApk apk, String hashCode,
 			Category category) {
 		// insert itembasedapkrepo
 		database.beginTransaction();
 		try {
 			ContentValues values = new ContentValues();
 			values.put(DbStructure.COLUMN_ICONS_PATH, apk.getServer().iconsPath);
 			values.put(DbStructure.COLUMN_SCREENS_PATH, server.screenspath);
 			values.put("featuredgraphicpath", server.featuredgraphicPath);
 			values.put(DbStructure.COLUMN_NAME, server.url);
 			values.put(DbStructure.COLUMN_BASE_PATH, server.basePath);
 			// insert itembasedapk
 			long i = database.insert(DbStructure.TABLE_ITEMBASED_REPO, null,
 					values);
 			values.clear();
 
 			values.put(DbStructure.COLUMN_APKID, apk.getApkid());
 			values.put(DbStructure.COLUMN_VERCODE, apk.getVercode());
 			values.put(DbStructure.COLUMN_VERNAME, apk.getVername());
 			values.put(DbStructure.COLUMN_CATEGORY_2ND, apk.getCategory2());
 			values.put(DbStructure.COLUMN_DOWNLOADS, apk.getDownloads());
 			values.put(DbStructure.COLUMN_RATING, apk.getRating());
 			values.put(DbStructure.COLUMN_REMOTE_PATH, apk.getPath());
 			values.put(DbStructure.COLUMN_SIZE, apk.getSize());
 			values.put(DbStructure.COLUMN_MD5, apk.getMd5());
 			values.put(DbStructure.COLUMN_NAME, apk.getName());
 			values.put(DbStructure.COLUMN_MIN_SCREEN, apk.getMinScreen());
 			values.put(DbStructure.COLUMN_MIN_SDK, apk.getMinSdk());
 			values.put(DbStructure.COLUMN_MIN_GLES, apk.getMinGlEs());
 			values.put(DbStructure.COLUMN_MATURE, apk.getAge());
 			values.put(DbStructure.COLUMN_ICON, apk.getIcon());
 
 			// if(apk.isHighlighted()){
 			// values.put("highlight", "true");
 			// }
 			// values.put("featuredgraphic", apk.getFeatureGraphic());
 			values.put("itembasedapkrepo_id", i);
 			values.put("parent_apkid", hashCode);
 			apk.setId(i);
 
 			insertScreenshots(apk, category);
 			database.insert(DbStructure.TABLE_ITEMBASED_APK, null, values);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		database.setTransactionSuccessful();
 		database.endTransaction();
 	}
 
 	public ArrayList<HashMap<String, String>> getItemBasedApks(String apkid) {
 		Cursor c = null;
 		Cursor d = null;
 		yield();
 		ArrayList<HashMap<String, String>> values = new ArrayList<HashMap<String, String>>();
 		SharedPreferences sPref = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		String filter = "";
 
 		if (sPref.getBoolean("hwspecsChkBox", true)) {
 			filter = filter + " and minscreen <= "
 					+ HWSpecifications.getScreenSize(context)
 					+ " and minsdk <=  " + HWSpecifications.getSdkVer()
 					+ " and mingles <= " + HWSpecifications.getEsglVer(context);
 		}
 
 		if (sPref.getBoolean("matureChkBox", false)) {
 			filter = filter + " and mature <= 0";
 		}
 
 		try {
 			c = database.query(DbStructure.TABLE_ITEMBASED_APK, new String[] {
 					DbStructure.COLUMN_NAME, DbStructure.COLUMN_ICON,
 					"repo_id", "_id", DbStructure.COLUMN_RATING,
 					DbStructure.COLUMN_APKID, DbStructure.COLUMN_VERCODE },
 					"parent_apkid = ? " + filter, new String[] { apkid }, null,
 					null, null);
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				HashMap<String, String> value = new HashMap<String, String>();
 				value.put(DbStructure.COLUMN_NAME, c.getString(0));
 				d = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH },
 						"_id = ?", new String[] { c.getString(2) }, null, null,
 						null);
 				d.moveToFirst();
 				value.put(DbStructure.COLUMN_ICON,
 						d.getString(0) + c.getString(1));
 				value.put("_id", c.getString(3));
 				value.put(DbStructure.COLUMN_RATING, c.getString(4));
 				value.put("hashCode",
 						(c.getString(5) + "|" + c.getString(6)));
 				values.add(value);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 			if (c != null) {
 				c.close();
 			}
 		}
 
 		return values;
 	}
 
 	public ArrayList<HashMap<String, String>> getItemBasedApksRecommended(
 			String apkid) {
 		Cursor c = null;
 		Cursor d = null;
 		yield();
 		ArrayList<HashMap<String, String>> values = new ArrayList<HashMap<String, String>>();
 		SharedPreferences sPref = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		String filter = "";
 
 		if (sPref.getBoolean("hwspecsChkBox", true)) {
 			filter = filter + " and a.minscreen <= "
 					+ HWSpecifications.getScreenSize(context)
 					+ " and a.minsdk <=  " + HWSpecifications.getSdkVer()
 					+ " and a.mingles <= "
 					+ HWSpecifications.getEsglVer(context);
 		}
 
 		if (sPref.getBoolean("matureChkBox", false)) {
 			filter = filter + " and a.mature <= 0";
 		}
 
 		try {
 			c = database
 					.rawQuery(
 							"select a.name as name, a.icon as icon, a.repo_id as itembasedapkrepo_id, a._id as _id, a.rating as _id, a.apkid as apkid, a.vercode as vercode, a.vername as vername, a.downloads as downloads from itembased_apk as a where a.parent_apkid = ? "
 									+ filter, new String[] { apkid });
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				HashMap<String, String> value = new HashMap<String, String>();
 				value.put(DbStructure.COLUMN_NAME, c.getString(0));
 				d = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 						new String[] { DbStructure.COLUMN_ICONS_PATH },
 						"_id = ?", new String[] { c.getString(2) }, null, null,
 						null);
 				d.moveToFirst();
 				value.put(DbStructure.COLUMN_ICON,
 						d.getString(0) + c.getString(1));
 				value.put("_id", c.getString(3));
 				value.put("vername", c.getString(7));
 				value.put("downloads", c.getString(8));
 				value.put(DbStructure.COLUMN_RATING, c.getString(4));
 				value.put("hashCode",
 						(c.getString(5) + "|" + c.getString(6)).hashCode() + "");
 				values.add(value);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (d != null) {
 				d.close();
 			}
 			c.close();
 		}
 
 		return values;
 	}
 
 	public String getItemBasedApksHash(String string) {
 		Cursor c = null;
 		String return_string = "";
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_HASHES,
 					new String[] { DbStructure.COLUMN_HASH }, "apkid = ?",
 					new String[] { string }, null, null, null);
 			if (c.moveToFirst()) {
 				return_string = c.getString(0);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return return_string;
 	}
 
 	public void insertItemBasedApkHash(String md5hash, String apkid) {
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_HASH, md5hash);
 		values.put(DbStructure.COLUMN_APKID, apkid);
 		database.insert(DbStructure.TABLE_HASHES, null, values);
 	}
 
 	public void addStoreInfo(String avatar, String name, String downloads,
 			long id) {
 		ContentValues values = new ContentValues();
 		if (avatar == null) {
 			avatar = "";
 		}
 		if (name == null) {
 			name = "Unnamed";
 		}
 		if (downloads == null) {
 			downloads = "0";
 		}
 		values.put(DbStructure.COLUMN_AVATAR_URL, avatar);
 		values.put(DbStructure.COLUMN_NAME, name);
 		values.put(DbStructure.COLUMN_DOWNLOADS, downloads);
 		database.update("repo", values, "_id=?", new String[] { id + "" });
 
 	}
 
 	public void insertScreenshots(ViewApk apk, Category category) {
 		ContentValues values = new ContentValues();
 
 		for (String screenshot : apk.getScreenshots()) {
 			values.clear();
 			values.put(DbStructure.COLUMN_REMOTE_PATH, screenshot);
 			values.put("_id", apk.getId());
 
 			switch (category) {
 			case EDITORSCHOICE:
 				database.insert(
 						DbStructure.TABLE_FEATURED_EDITORSCHOICE_SCREENSHOTS,
 						null, values);
 				break;
 			case ITEMBASED:
 				values.put("repo_id", apk.getRepo_id());
 				database.insert(DbStructure.TABLE_ITEMBASED_SCREENSHOTS, null,
 						values);
 				break;
 			case LATEST:
 				values.put("repo_id", apk.getRepo_id());
 				database.insert(DbStructure.TABLE_LATEST_SCREENSHOTS, null,
 						values);
 				break;
 			case TOP:
 				values.put("repo_id", apk.getRepo_id());
 				database.insert(DbStructure.TABLE_TOP_SCREENSHOTS, null, values);
 				break;
 			case TOPFEATURED:
 				database.insert(DbStructure.TABLE_FEATURED_TOP_SCREENSHOTS,
 						null, values);
 				break;
 
 			default:
 				break;
 			}
 			yield();
 		}
 
 	}
 
 	public void getScreenshots(ArrayList<String> originalList, ViewApk viewApk,
 			Category category) {
 		Cursor c = null;
 		yield();
 		String screenspath = getScreenshotsPath(viewApk.getRepo_id(), category);
 		try {
 
 			switch (category) {
 			case EDITORSCHOICE:
 				c = database.query(
 						DbStructure.TABLE_FEATURED_EDITORSCHOICE_SCREENSHOTS,
 						new String[] { DbStructure.COLUMN_REMOTE_PATH },
 						"_id = ?", new String[] { viewApk.getId() + "" }, null,
 						null, null);
 				break;
 			case TOPFEATURED:
 				c = database.query(DbStructure.TABLE_FEATURED_TOP_SCREENSHOTS,
 						new String[] { DbStructure.COLUMN_REMOTE_PATH },
 						"_id = ?", new String[] { viewApk.getId() + "" }, null,
 						null, null);
 				break;
 			case ITEMBASED:
 				c = database.query(DbStructure.TABLE_ITEMBASED_SCREENSHOTS,
 						new String[] { DbStructure.COLUMN_REMOTE_PATH },
 						"_id = ?", new String[] { viewApk.getId() + "" }, null,
 						null, null);
 				break;
 			case LATEST:
 				c = database.query(DbStructure.TABLE_LATEST_SCREENSHOTS,
 						new String[] { DbStructure.COLUMN_REMOTE_PATH },
 						"_id = ?", new String[] { viewApk.getId() + "" }, null,
 						null, null);
 				break;
 			case TOP:
 				c = database.query(DbStructure.TABLE_TOP_SCREENSHOTS,
 						new String[] { DbStructure.COLUMN_REMOTE_PATH },
 						"_id = ?", new String[] { viewApk.getId() + "" }, null,
 						null, null);
 				break;
 			case USERBASED:
 				break;
 			default:
 				break;
 			}
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				originalList.add(screenspath + c.getString(0));
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 	}
 
 	private String getScreenshotsPath(long repo_id, Category category) {
 		String return_string = "";
 		Cursor c = null;
 		yield();
 		try {
 			switch (category) {
 			case TOPFEATURED:
 				c = database.query(DbStructure.TABLE_FEATURED_TOP_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case EDITORSCHOICE:
 				c = database.query(
 						DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case LATEST:
 				c = database.query(DbStructure.TABLE_LATEST_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case TOP:
 				c = database.query(DbStructure.TABLE_TOP_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 			case ITEMBASED:
 				c = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 						new String[] { DbStructure.COLUMN_SCREENS_PATH },
 						"_id = ?", new String[] { repo_id + "" }, null, null,
 						null);
 				break;
 
 			default:
 				break;
 			}
 			if (c.moveToFirst()) {
 				return_string = c.getString(0);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return return_string;
 	}
 
 	public ViewApk getItemBasedApk(long id) {
 		Cursor c = null;
 		ViewApk apk = new ViewApk();
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_ITEMBASED_APK, new String[] {
 					DbStructure.COLUMN_APKID, DbStructure.COLUMN_VERNAME,
 					"itembasedapkrepo_id", DbStructure.COLUMN_DOWNLOADS,
 					DbStructure.COLUMN_SIZE, DbStructure.COLUMN_ICON,
 					DbStructure.COLUMN_NAME, DbStructure.COLUMN_RATING,
 					DbStructure.COLUMN_REMOTE_PATH, DbStructure.COLUMN_MD5 },
 					"_id = ?", new String[] { id + "" }, null, null, null);
 
 			c.moveToFirst();
 			apk.setApkid(c.getString(0));
 			apk.setVername(c.getString(1));
 			apk.setRepo_id(c.getLong(2));
 			apk.setMd5(c.getString(9));
 			apk.setDownloads(c.getString(3));
 			apk.setSize(c.getString(4));
 			apk.setIconPath(c.getString(5));
 			apk.setName(c.getString(6));
 			apk.setRating(c.getString(7));
 			apk.setPath(c.getString(8));
 			apk.setId(id);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return apk;
 	}
 
 	public String getItemBasedServer(long repo_id) {
 		Cursor c = null;
 		String return_string = null;
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 					new String[] { DbStructure.COLUMN_NAME }, "_id=?",
 					new String[] { repo_id + "" }, null, null, null);
 
 			if (c.moveToFirst()) {
 				return_string = c.getString(0);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return return_string;
 	}
 
 	public String getItemBasedBasePath(long repo_id) {
 		Cursor c = null;
 		String return_string = null;
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_ITEMBASED_REPO,
 					new String[] { DbStructure.COLUMN_BASE_PATH }, "_id=?",
 					new String[] { repo_id + "" }, null, null, null);
 
 			if (c.moveToFirst()) {
 				return_string = c.getString(0);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return return_string;
 	}
 
 	public void deleteItemBasedApks(ViewApk apk) {
 
 		Cursor c = null;
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_ITEMBASED_APK,
 					new String[] { "repo_id" }, "parent_apkid = ?",
 					new String[] { apk.getApkid() }, null, null, null);
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				database.delete(DbStructure.TABLE_ITEMBASED_REPO, "_id = ?",
 						new String[] { c.getString(0) });
 				database.delete(DbStructure.TABLE_ITEMBASED_SCREENSHOTS,
 						"_id = ?", new String[] { c.getString(0) });
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		database.delete(DbStructure.TABLE_ITEMBASED_APK, "parent_apkid = ?",
 				new String[] { apk.getApkid() });
 	}
 
 	public long getApkId(String apkid, long store_id) {
 		Cursor c = null;
 		long return_int = -1;
 		System.out.println(apkid + " " + store_id);
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_APK, new String[] { "_id" },
 					"apkid = ? and repo_id = ?", new String[] { apkid,
 							store_id + "" }, null, null, null);
 
 			if (c.moveToFirst()) {
 				return_int = c.getLong(0);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return return_int;
 	}
 
 	public long getApkId(String apkid) {
 		Cursor c = null;
 		long return_int = -1;
 		System.out.println(apkid);
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_APK, new String[] { "_id" },
 					"apkid = ?", new String[] { apkid }, null, null, null);
 
 			if (c.moveToFirst()) {
 				return_int = c.getLong(0);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return return_int;
 	}
 
 	public ArrayList<HashMap<String, String>> getFeaturedGraphics() {
 		ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> value;
 
 		Cursor c = null;
 		yield();
 		try {
 			c = database
 					.rawQuery(
 							"select a._id, b.featuredgraphicspath, a.featuredgraphic ,a.apkid, a.vercode from featured_editorschoice_apk a, featured_editorschoice_repo b where b._id = a.repo_id and  a.highlighted = 0 ",
 							null);
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				value = new HashMap<String, String>();
 				value.put("url", c.getString(1) + c.getString(2));
 				value.put("id", c.getString(0));
 				value.put("hashCode",
 						(c.getString(3) + "|" + c.getString(4)).hashCode()
 								+ "featured");
 				arrayList.add(value);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 
 		return arrayList;
 	}
 
 	public HashMap<String, String> getHighLightFeature() {
 		HashMap<String, String> value = null;
 
 		Cursor c = null;
 		yield();
 		try {
 			c = database
 					.rawQuery(
 							"select a._id, b.featuredgraphicspath, a.featuredgraphic ,a.apkid, a.vercode from featured_editorschoice_apk a, featured_editorschoice_repo b where a.highlighted = 1 ",
 							null);
 
 			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
 				value = new HashMap<String, String>();
 				value.put("url", c.getString(1) + c.getString(2));
 				value.put("id", c.getString(0));
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			c.close();
 		}
 		return value;
 	}
 
 	public Cursor getScheduledDownloads() {
 
 		Cursor c = null;
 		yield();
 		try {
 			c = database.query(DbStructure.TABLE_SCHEDULED, null, null, null,
 					null, null, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return c;
 	}
 
 	public void insertScheduledDownload(String apkid, int vercode,
 			String vername, String remotePath, String name, String md5, String icon) {
 
 		Cursor c = database.query(DbStructure.TABLE_SCHEDULED, null,
 				"apkid = ? and vercode = ?",
 				new String[] { apkid, vercode + "" }, null, null, null);
 
 		if (c.moveToFirst()) {
 			c.close();
 			return;
 		}
 		c.close();
 
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_NAME, name);
 		values.put(DbStructure.COLUMN_APKID, apkid);
 		values.put(DbStructure.COLUMN_MD5, md5);
 		values.put(DbStructure.COLUMN_VERCODE, vercode);
 		values.put(DbStructure.COLUMN_VERNAME, vername);
 		values.put(DbStructure.COLUMN_REMOTE_PATH, remotePath);
 		values.put(DbStructure.COLUMN_ICON, icon);
 
 		database.insert(DbStructure.TABLE_SCHEDULED, null, values);
 
 	}
 
 	public void deleteScheduledDownload(String planet) {
 		database.delete(DbStructure.TABLE_SCHEDULED, "_id = ?",
 				new String[] { planet });
 	}
 
 	public void deleteScheduledDownload(String apkid, String versionName) {
 		database.delete(DbStructure.TABLE_SCHEDULED,
 				"apkid = ? and vername = ?",
 				new String[] { apkid, versionName });
 	}
 
 	public void insertInstalled(String apkid, int versionCode,
 			String versionName, String appName) {
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_APKID, apkid);
 		values.put(DbStructure.COLUMN_VERCODE, versionCode);
 		values.put(DbStructure.COLUMN_VERNAME, versionName);
 		values.put(DbStructure.COLUMN_NAME, appName);
 		database.insert(DbStructure.TABLE_INSTALLED, null, values);
 	}
 
 	public void deleteInstalled(String apkid) {
 		database.delete(DbStructure.TABLE_INSTALLED, "apkid = ?",
 				new String[] { apkid });
 	}
 
 	public Cursor getSearch(String searchQuery) {
 		yield();
 		String query = "select b._id as _id, b.name,b.vername,b.repo_id,b.icon as imagepath,b.rating,b.downloads,b.apkid as apkid ,b.vercode as vercode, c.iconspath as iconspath from apk as b, repo as c where (b.name LIKE '%"
 				+ searchQuery
 				+ "%' OR b.apkid LIKE '%"
 				+ searchQuery
 				+ "%') and b.repo_id = c._id";
 
 		Cursor c = null;
 		try {
 			c = database.rawQuery(query, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return c;
 	}
 
 	/**
 	 * 
 	 */
 	public int getInstalledAppVercode(String apkid) {
 		yield();
 		Cursor c = null;
 		int return_int = 0;
 		try {
 			c = database.query(DbStructure.TABLE_INSTALLED,
 					new String[] { DbStructure.COLUMN_VERCODE }, "apkid = ?",
 					new String[] { apkid }, null, null, null);
 			if (c.moveToFirst()) {
 				return_int = c.getInt(0);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (c != null) {
 				c.close();
 			}
 		}
 
 		return return_int;
 
 	}
 
 	public String getInstalledAppVername(String apkid) {
 		yield();
 		Cursor c = null;
 		String return_string = "";
 		try {
 			c = database.query(DbStructure.TABLE_INSTALLED,
 					new String[] { DbStructure.COLUMN_VERNAME }, "apkid = ?",
 					new String[] { apkid }, null, null, null);
 			if (c.moveToFirst()) {
 				return_string = c.getString(0);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (c != null) {
 				c.close();
 			}
 		}
 
 		return return_string;
 
 	}
 
 	public String getEditorsChoiceHash() {
 		String hash = "";
 		yield();
 		Cursor c = database.query(DbStructure.TABLE_HASHES, new String[]{DbStructure.COLUMN_HASH}, DbStructure.COLUMN_APKID + "=?", new String[]{"editorschoice"}, null, null, null);
 		
 		if(c.moveToFirst()){
 			hash = c.getString(0);
 			c.close();
 		}
 		
 		return hash;
 	}
 
 	public Cursor getFeaturedTopApps() {
 		yield();
 		Cursor c = database
 				.rawQuery(
 						"select a._id as _id, a.name, a.vername, a.repo_id, a.icon as imagepath, a.rating, a.downloads, a.apkid as apkid, a.vercode as vercode, c.iconspath as iconspath from featured_top_apk as a, featured_top_repo as c where c._id = a.repo_id "
 								+ filters() + " group by a._id", null);
 		return c;
 	}
 
 	public void deleteFeaturedTopApps() {
 
 		database.delete(DbStructure.TABLE_FEATURED_TOP_APK, null, null);
 		database.delete(DbStructure.TABLE_FEATURED_TOP_REPO, null, null);
 		database.delete(DbStructure.TABLE_FEATURED_TOP_SCREENSHOTS, null, null);
 
 	}
 
 	public void deleteEditorsChoice() {
 		database.delete(DbStructure.TABLE_FEATURED_EDITORSCHOICE_APK, null,
 				null);
 		database.delete(DbStructure.TABLE_FEATURED_EDITORSCHOICE_REPO, null,
 				null);
 		database.delete(DbStructure.TABLE_FEATURED_EDITORSCHOICE_SCREENSHOTS,
 				null, null);
 		database.delete(DbStructure.TABLE_HASHES, DbStructure.COLUMN_APKID + "=?", new String[]{"editorschoice"});
 	}
 
 	public void insertEditorsChoiceHash(String hash) {
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_APKID, "editorschoice");
 		values.put(DbStructure.COLUMN_HASH, hash);
 		database.insert(DbStructure.TABLE_HASHES, null, values);
 	}
 
 	public void addToExcludeUpdate(int itemId) {
 		ContentValues values = new ContentValues();
 		ViewApk apk = getApk(itemId, Category.INFOXML);
 
 		String apkid = apk.getApkid();
 		int vercode = apk.getVercode();
 		String name = apk.getName();
 		values.put(DbStructure.COLUMN_APKID, apkid);
 		values.put(DbStructure.COLUMN_VERCODE, vercode);
 		values.put(DbStructure.COLUMN_NAME, name);
 		database.insert(DbStructure.TABLE_EXCLUDED_APKID, null, values);
 	}
 
 	public void deleteFromExcludeUpdate(String apkid, int vercode) {
 		database.delete(DbStructure.TABLE_EXCLUDED_APKID,
 				"apkid = ? and vercode = ?",
 				new String[] { apkid, vercode + "" });
 	}
 
 	public Cursor getExcludedApks() {
 		return database.query(DbStructure.TABLE_EXCLUDED_APKID, null, null,
 				null, null, null, null);
 	}
 
 	public void updateServerCredentials(String url, String username,
 			String password) {
 
 		ContentValues values = new ContentValues();
 		values.put(DbStructure.COLUMN_USERNAME, username);
 		values.put(DbStructure.COLUMN_PASSWORD, password);
 
 		database.update(DbStructure.TABLE_REPO, values, DbStructure.COLUMN_URL
 				+ "=?", new String[] { url });
 	}
 
 }
