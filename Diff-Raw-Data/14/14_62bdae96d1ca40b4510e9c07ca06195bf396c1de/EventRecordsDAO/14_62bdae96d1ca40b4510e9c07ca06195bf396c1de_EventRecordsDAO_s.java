 package com.yvelabs.timerecording.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.yvelabs.timerecording.EventRecordModel;
 import com.yvelabs.timerecording.utils.LogUtils;
 import com.yvelabs.timerecording.utils.MyDBHelper;
 
 public class EventRecordsDAO {
 	
 	private static final String INSERT = " insert into t_event_records(event_name, event_category_name, event_date, useing_time, summary, create_time) values(?, ?, ?, ?, ?, ?) ";
 	private static final String DELETE = " delete from t_event_records where 1 = 1 ";
 	private static final String SELECT = " select _id, event_name, event_category_name, event_date, useing_time, summary, create_time from t_event_records where 1 = 1 ";
 	private static final String UPDATE_BY_CATEGORYNAME = "update t_event_records set event_category_name = ? where lower(event_category_name) = ?";
 
 	private Context context;
 
 	public EventRecordsDAO(Context context) {
 		this.context = context;
 	}
 	
 	public void insert (EventRecordModel model) {
 		List<EventRecordModel> modelList = new ArrayList<EventRecordModel>();
 		modelList.add(model);
 		
 		insert(modelList);
 	}
 
 	public void insert (List<EventRecordModel> modelList) {
 		SQLiteDatabase db = new MyDBHelper(context).getWritableDatabase();
 		try {
 			db.beginTransaction();
 			
 			for (EventRecordModel model : modelList) {
 				db.execSQL(INSERT, new Object[]{model.getEventName(), model.getEventCategoryName(), model.getEventDate().getTime(), model.getUseingTime(), model.getSummary(), model.getCreateTime().getTime()});
 			}
 			db.setTransactionSuccessful();
 		} finally {
 			if (db != null) {
 				db.endTransaction();
 				db.close();
 			}
 		}
 	}
 	
 	public void delete (EventRecordModel recordModel) {
 		List<EventRecordModel> modelList = new ArrayList<EventRecordModel>();
 		modelList.add(recordModel);
 		delete(modelList);
 	}
 	
 	public int delete (List<EventRecordModel> modelList) {
 		int count = 0;
 		SQLiteDatabase db = new MyDBHelper(context).getWritableDatabase();
 		try {
 			db.beginTransaction();
 			
 			for (EventRecordModel model : modelList) {
 				StringBuilder sql = new StringBuilder(DELETE);
 				List<String> paraList = new ArrayList<String>();
 				
 				if (model.getRecordId() >= 0) {
 					sql.append(" and _id = ? ");
 					paraList.add(String.valueOf(model.getRecordId()));
 				} 
 				if (model.getEventName() != null && model.getEventName().length() > 0) {
 					sql.append(" and lower(event_name) = ? ");
 					paraList.add(model.getEventName().toLowerCase());
 				}
 				if (model.getEventCategoryName() != null && model.getEventCategoryName().length() > 0) {
 					sql.append(" and lower(event_category_name) = ? ");	
 					paraList.add(model.getEventCategoryName().toLowerCase());
 				}
 				if (model.getStartEventDate() != null) {
 					sql.append(" and event_date > ? ");
 					paraList.add(model.getStartEventDate().getTime() + "");
 				}
 				if (model.getEndEventDate() != null) {
 					sql.append(" and event_date < ? ");
 					paraList.add(model.getEndEventDate().getTime() + "");
 				}
 				if (model.getStartCreateTime() != null) {
 					sql.append(" and create_time > ? ");
 					paraList.add(model.getStartCreateTime().getTime() + "");
 				}
 				if (model.getEndCreateTime() != null) {
 					sql.append(" and create_time < ? ");
 					paraList.add(model.getEndCreateTime().getTime() + "");
 				}
 				
 				if (paraList.size() <= 0) {
 					db.execSQL(sql.toString(), null);
 				} else {
 					String [] paraArray = new String[paraList.size()];
 					for (int i = 0 ; i < paraArray.length ; i ++) {
 						paraArray[i] = paraList.get(i);
 					}
 					db.execSQL(sql.toString(), paraArray);
 				}
 				
 				count++;
 			}
 			
 			db.setTransactionSuccessful();
 		} finally {
 			if (db != null) {
 				db.endTransaction();
 				db.close();
 			}
 		}
 		
 		return count;
 	}
 	
 	public void updateByCategoryName (SQLiteDatabase db, EventRecordModel oldModel, EventRecordModel newModel) {
 		db.execSQL(UPDATE_BY_CATEGORYNAME, new Object[]{newModel.getEventCategoryName(), oldModel.getEventCategoryName().toLowerCase()});
 	}
 	
 	public void update (SQLiteDatabase db, EventRecordModel parameterModel, EventRecordModel newModel) {
 		StringBuilder sql = new StringBuilder(" update t_event_records set _id = _id ");
 		
 		List<String> paraList = new ArrayList<String>();
 		if (newModel.getEventName() != null && newModel.getEventName().length() > 0) {
 			sql.append(" , event_name = ? ");
 			paraList.add( newModel.getEventName());
 		}
 		if (newModel.getEventCategoryName() != null && newModel.getEventCategoryName().length() > 0) {
 			sql.append(" , event_category_name = ? ");
 			paraList.add(newModel.getEventCategoryName());
 		}
 		sql.append(" where 1 = 1 ");
 		
 		if (parameterModel.getEventName() != null && parameterModel.getEventName().length() > 0) {
 			sql.append(" and lower(event_name) = ? ");
 			paraList.add(parameterModel.getEventName().toLowerCase());
 		}
 		if (parameterModel.getEventCategoryName() != null && parameterModel.getEventName().length() > 0) {
 			sql.append(" and lower(event_category_name) = ? ");
 			paraList.add(parameterModel.getEventCategoryName().toLowerCase());
 		}
 		
 		if (paraList.size() <= 0) {
 			db.execSQL(sql.toString(), null);
 		} else {
 			String [] paraArray = new String[paraList.size()];
 			for (int i = 0 ; i < paraArray.length ; i ++) {
 				paraArray[i] = paraList.get(i);
 			}
 			db.execSQL(sql.toString(), paraArray);
 		}
 	}
 	
 	public List<EventRecordModel> query4Diary () {
 		List<EventRecordModel> resultList = new ArrayList<EventRecordModel>(); 
 		Cursor c = null;
 		SQLiteDatabase db = new MyDBHelper(context).getWritableDatabase();
 		
 		try {
 			StringBuilder sql = new StringBuilder(SELECT);
 			sql.append(" order by event_date desc, create_time desc ");
 			
 			c = db.rawQuery(sql.toString(), null);
 			
 			while (c.moveToNext()) {
 				EventRecordModel model = new EventRecordModel();
 				model.setRecordId(c.getInt(c.getColumnIndex("_id")));
 				model.setEventName(c.getString(c.getColumnIndex("event_name")));
 				model.setEventCategoryName(c.getString(c.getColumnIndex("event_category_name")));
 				model.setEventDate(new Date(c.getLong(c.getColumnIndex("event_date"))));
 				model.setUseingTime(c.getLong(c.getColumnIndex("useing_time")));
 				model.setSummary(c.getString(c.getColumnIndex("summary")));
 				model.setCreateTime(new Date(c.getLong(c.getColumnIndex("create_time"))));
 				
 				resultList.add(model);
 			}
 			
 		} finally {
 			if (c != null) c.close();
 			if (db != null)	db.close();
 		}
 		
 		return resultList;
 	}
 	
 	public List<List<EventRecordModel>> getDiaryList () {
 		List<List<EventRecordModel>> resultList = new ArrayList<List<EventRecordModel>>();
 		List<EventRecordModel> originalList = query4Diary();
 		
 		List<EventRecordModel> subList = null;
 		Date tempEventDate = new Date();
 		for (EventRecordModel model : originalList) {
 			
 			if (model.getEventDate().compareTo(tempEventDate) != 0) {
 				if (subList != null)
 					resultList.add(subList);
 				subList = new ArrayList<EventRecordModel>();
 				tempEventDate = model.getEventDate();
 			} 
 
 			subList.add(model);
 		}
 		
 		return resultList;
 	}
 	
 	public List<EventRecordModel> query (EventRecordModel parameter) {
 		List<EventRecordModel> resultList = new ArrayList<EventRecordModel>(); 
 		Cursor c = null;
 		SQLiteDatabase db = new MyDBHelper(context).getWritableDatabase();
 		
 		try {
 			StringBuilder sql = new StringBuilder(SELECT);
 			List<String> paraList = new ArrayList<String>();
 			
 			if (parameter.getRecordId() >= 0) {
 				sql.append(" and _id = ? ");
 				paraList.add(String.valueOf(parameter.getRecordId()));
 			} 
 			if (parameter.getEventName() != null && parameter.getEventName().length() > 0) {
 				sql.append(" and lower(event_name) = ? ");
 				paraList.add(parameter.getEventName().toLowerCase());
 			}
 			if (parameter.getEventCategoryName() != null && parameter.getEventCategoryName().length() > 0) {
 				sql.append(" and lower(event_category_name) = ? ");	
 				paraList.add(parameter.getEventCategoryName().toLowerCase());
 			}
 			if (parameter.getStartEventDate() != null) {
 				sql.append(" and event_date > ? ");
 				paraList.add(parameter.getStartEventDate().getTime() + "");
 				LogUtils.d(getClass(), "parameter.getStartEventDate().getTime():" + parameter.getStartEventDate().getTime());
 			}
 			if (parameter.getEndEventDate() != null) {
 				sql.append(" and event_date < ? ");
 				paraList.add(parameter.getEndEventDate().getTime() + "");
 				LogUtils.d(getClass(), "parameter.getEndEventDate().getTime():" + parameter.getEndEventDate().getTime());
 			}
 			if (parameter.getStartCreateTime() != null) {
 				sql.append(" and create_time > ? ");
 				paraList.add(parameter.getStartCreateTime().getTime() + "");
 			}
 			if (parameter.getEndCreateTime() != null) {
 				sql.append(" and create_time < ? ");
 				paraList.add(parameter.getEndCreateTime().getTime() + "");
 			}
 			
 			sql.append(" order by create_time desc ");
 			
 			LogUtils.d(this.getClass(), "sql:" + sql);
 			
 			if (paraList.size() <= 0) {
 				c = db.rawQuery(sql.toString(), null);
 			} else {
 				String [] paraArray = new String[paraList.size()];
 				for (int i = 0 ; i < paraArray.length ; i ++) {
 					paraArray[i] = paraList.get(i);
 				}
 				c = db.rawQuery(sql.toString(), paraArray);
 			}
 			
 			while (c.moveToNext()) {
 				EventRecordModel model = new EventRecordModel();
 				model.setRecordId(c.getInt(c.getColumnIndex("_id")));
 				model.setEventName(c.getString(c.getColumnIndex("event_name")));
 				model.setEventCategoryName(c.getString(c.getColumnIndex("event_category_name")));
 				model.setEventDate(new Date(c.getLong(c.getColumnIndex("event_date"))));
 				model.setUseingTime(c.getLong(c.getColumnIndex("useing_time")));
 				model.setSummary(c.getString(c.getColumnIndex("summary")));
 				model.setCreateTime(new Date(c.getLong(c.getColumnIndex("create_time"))));
 				
 				resultList.add(model);
 			}
 			
 		} finally {
 			if (c != null) c.close();
 			if (db != null)	db.close();
 		}
 		
 		return resultList;
 	}
 
 }
