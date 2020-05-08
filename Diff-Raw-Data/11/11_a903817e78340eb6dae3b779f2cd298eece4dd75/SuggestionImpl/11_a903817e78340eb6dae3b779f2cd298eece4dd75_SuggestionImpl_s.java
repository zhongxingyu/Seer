 package org.wrowclif.recipebox.impl;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.content.ContentValues;
 
 import org.wrowclif.recipebox.AppData;
 import org.wrowclif.recipebox.AppData.Transaction;
 import org.wrowclif.recipebox.Recipe;
 import org.wrowclif.recipebox.Suggestion;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class SuggestionImpl implements Suggestion {
 
 	protected static final SuggestionFactory factory;
 
 	static {
 		factory = new SuggestionFactory();
 	}
 
 	private Recipe r1;
 	private Recipe r2;
 	private long lowId;
 	private long hiId;
 	private String comments;
 
 	private SuggestionImpl(Recipe r1, Recipe r2) {
 		this.r1 = r1;
 		this.r2 = r2;
 		if(r1.getId() < r2.getId()) {
 			this.lowId = r1.getId();
 			this.hiId = r2.getId();
 		} else {
 			this.lowId = r2.getId();
			this.hiId = r2.getId();
 		}
 		this.comments = "";
 	}
 
 	public Recipe getOriginalRecipe() {
 		return r1;
 	}
 
 	public Recipe getSuggestedRecipe() {
 		return r2;
 	}
 
 	public String getComments() {
 		return comments;
 	}
 
 	public void setComments(String text) {
 		this.comments = text;
 
 		ContentValues cv = new ContentValues();
 		cv.put("comments", text);
 		factory.data.itemUpdate(cv, "SuggestedWith", "rid1 = ? and rid2 = ?",
 							new String[] {lowId + "", hiId + ""}, "setComments");
 	}
 
 	protected static class SuggestionFactory {
 
 		protected AppData data;
 
 		private SuggestionFactory() {
 			data = AppData.getSingleton();
 		}
 
 		protected List<Suggestion> createListFromCursor(Recipe r, Cursor c) {
 			// r.rid, sw.comments, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid
 			List<Suggestion> list = new ArrayList<Suggestion>(c.getCount());
 			ContentValues values = new ContentValues();
 			while(c.moveToNext()) {
 				values.clear();
 				values.put("rid", c.getLong(0));
 				values.put("name", c.getString(2));
 				values.put("description", c.getString(3));
 				values.put("preptime", c.getInt(4));
 				values.put("cooktime", c.getInt(5));
				values.put("vid", c.getLong(6));
 				Recipe r2 = RecipeImpl.factory.createRecipeFromData(values);
 				SuggestionImpl si = new SuggestionImpl(r, r2);
 				si.comments = c.getString(1);
 
 				list.add(si);
 			}
 
 			return list;
 		}
 
 		protected List<Suggestion> getSuggestedWith(final Recipe r) {
 			final String stmt =
 				"SELECT r.rid, sw.comments, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
 				"FROM Recipe r, SuggestedWith sw " +
 				"WHERE sw.rid1 = ? " +
 					"and sw.rid2 = r.rid " +
 				"UNION " +
 				"SELECT r.rid, sw.comments, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
 				"FROM Recipe r, SuggestedWith sw " +
 				"WHERE sw.rid1 = r.rid " +
 					"and sw.rid2 = ?; ";
 
 			return data.sqlTransaction(new Transaction<List<Suggestion>>() {
 				public List<Suggestion> exec(SQLiteDatabase db) {
 					Cursor c1 = db.rawQuery(stmt.replaceAll("\\?",r.getId() + ""), null);
 					List<Suggestion> list = createListFromCursor(r, c1);
 					c1.close();
 					return list;
 				}
 			});
 		}
 
 		protected Suggestion addSuggestion(Recipe r1, Recipe r2) {
 			if(r1.getId() == r2.getId()) {
 				throw new IllegalArgumentException("Cannot suggest a recipe with itself");
 			}
 
 			final String selectStmt =
 				"SELECT sw.comments " +
 				"FROM SuggestedWith sw " +
 				"WHERE sw.rid1 = ? " +
 					"and sw.rid2 = ?;";
 			final String stmt =
 				"INSERT OR IGNORE INTO SuggestedWith(rid1, rid2) " +
 					"VALUES (?, ?); ";
 			final SuggestionImpl si = new SuggestionImpl(r1, r2);
 			data.sqlTransaction(new Transaction<Void>() {
 				public Void exec(SQLiteDatabase db) {
 					Cursor c = db.rawQuery(selectStmt, new String[] {si.lowId + "", si.hiId + ""});
					if(c.getCount() > 0) {
 						si.comments = c.getString(0);
 					}
 					db.execSQL(stmt, new Object[] {si.lowId, si.hiId});
 					return null;
 				}
 			});
 			return si;
 		}
 
 		protected void removeSuggestion(Recipe r1, Recipe r2) {
 			final String stmt =
 				"DELETE FROM SuggestedWith " +
 					"WHERE rid1 = ? " +
 						"and rid2 = ?; ";
 			final SuggestionImpl si = new SuggestionImpl(r1, r2);
 			data.sqlTransaction(new Transaction<Void>() {
 				public Void exec(SQLiteDatabase db) {
 					db.execSQL(stmt, new Object[] {si.lowId, si.hiId});
 					return null;
 				}
 			});
 		}
 	}
 }
 
 
