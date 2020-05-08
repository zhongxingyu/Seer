 package wa.android.inquire.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import wa.android.common.App;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 import nc.vo.wa.component.available.AvailableInfoVO;
 import nc.vo.wa.component.available.MaterialVO;
 
 public class SQLiteUtilHistoryHelper extends SQLiteOpenHelper {
 
 	public SQLiteUtilHistoryHelper(Context context, String name,
 			CursorFactory factory, int version) {
 		super(context, name, factory, version);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * 
 	 * @param MaterialVO
 	 *            点击的MaterialVO
 	 * 
 	 * @param act
 	 *            查询时的Activity
 	 * 
 	 */
 	public static void updateHistory(MaterialVO MaterialVO, Activity atc) {
 		SQLiteUtilHistoryHelper helper = new SQLiteUtilHistoryHelper(atc,
 				"InquireHistory", null, 1);
 		SQLiteDatabase db = helper.getWritableDatabase();
 		String name = (String) MaterialVO.getInvname();
 		String id = (String) MaterialVO.getInvid();
 
 		ContentValues values = new ContentValues();
 		values.put("id", id);
 		values.put("name", name);
 		values.put("rank", 0);
 		db.insert("InquireHistory", null, values);
 		try{
 		db.execSQL("UPDATE InquireHistory SET rank=rank+1 WHERE 1");
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		db.delete("InquireHistory", "rank=?", new String[] { "11" });
 		db.close();
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param atc
 	 *            查询时的Activity
 	 * @return List<MaterialVO> 收到返回值后须调getInfo(*,*)加在详细信息；
 	 */
 	public static List<MaterialVO> getHistory(Activity atc) {
 		SQLiteUtilHistoryHelper helper = new SQLiteUtilHistoryHelper(atc,
 				"InquireHistory", null, 1);
 		SQLiteDatabase db = helper.getReadableDatabase();
 		Cursor cursor = db.query("InquireHistory",
 				new String[] { "id", "name" }, null, null, null,
 				null, "rank");
 		List<MaterialVO> materialVOs = new ArrayList<MaterialVO>();
 		if(cursor.moveToFirst() == false)
 			return materialVOs;
 			
 		while(cursor!= null && cursor.moveToNext()){
 			MaterialVO materialVO = new MaterialVO();	
 			materialVO.setInvcode(cursor.getString(cursor.getColumnIndex("id")));
 			materialVO.setInvname(cursor.getString(cursor.getColumnIndex("name")));
 			materialVOs.add(materialVO);
 		}
 		cursor.close();
 		db.close();
 		return materialVOs;
 
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// TODO Auto-generated method stub
 		App.Log('d', SQLiteUtilHistoryHelper.class,
 				"Create Table InquireHistory");
 		db.execSQL("CREATE table InquireHistory(id varchar(20), name varchar(20), rank int)");
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		App.Log('d', SQLiteUtilHistoryHelper.class,
 				"Update Table InquireHistory");
 
 	}
 
 }
