 package com.tuit.ar.databases;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 public abstract class Model {
 	protected long id;
 	static public Database db;
 
 //	TODO: Is there any way to use reflection to call the setters?
 	/*
 	public Model(Cursor query) {
 		if (query == null) return;
 		int c = query.getColumnCount();
 		for (int i = 0; i < c; i++) {
 			String columnName = query.getColumnName(0);
 			try {
 				this.getClass().getMethod("set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1), this.getClass().getField(columnName).getType());
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 			} catch (NoSuchFieldException e) {
 				e.printStackTrace();
 			}
 		}
 	}*/
 
 	public void save() {
 		if (id > 0) update();
 		else insert();
 	}
 
 	abstract protected ContentValues getValues();
 
 	@SuppressWarnings("unchecked")
 	static protected ArrayList<? extends Model> select(Class<? extends Model> modelClass, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
 		ArrayList<Model> models = new ArrayList<Model>();
 		Cursor cursor = db.query(modelClass.getSimpleName().toLowerCase(), columns, selection, selectionArgs, groupBy, having, orderBy, limit);
 		try {
 			for (int i = 0; i < cursor.getCount(); i++) {
 				cursor.moveToPosition(i);
 				Model model;
 				Class<Cursor> partypes[] = new Class[1];
 				partypes[0] = Cursor.class;
 				Constructor<? extends Model> ct;
 				ct = modelClass.getConstructor(partypes);
 				Object arglist[] = new Object[1];
 				arglist[0] = cursor;
 				model = ct.newInstance(arglist);
 				models.add(model);
 			}
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		cursor.close();
 		return models;
 	}
 
 	public long replace() {
 		long result = db.replace(this.getClass().getSimpleName().toLowerCase(), "id", getValues());
 		if (result > 0)
 			id = result;
 		return result;
 	}
 
 	public long insert() {
 		long result = db.insert(this.getClass().getSimpleName().toLowerCase(), "id", getValues());
 		if (result > 0)
 			id = result;
 		return result;
 	}
 
 	public int update() {
 		return db.update(this.getClass().getSimpleName().toLowerCase(), getValues(), "id = ?", new String[] {String.valueOf(id)});
 	}
 
 	public int delete() {
 		return db.delete(this.getClass().getSimpleName().toLowerCase(), "id = ?", new String[] {String.valueOf(id)});
 	}
 
 	static protected String sanitize(String str) {
 		if (str == null) return null;
		return str.replaceAll("", "\r").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
 	}
 
 }
