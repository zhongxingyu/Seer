 package com.htb.cnk.data;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.util.Log;
 
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 public class MyOrder {
 	private int MODE_PAD = 0;
 	private int MODE_PHONE = 1;
 
 	public class OrderedDish {
 		Dish dish;
 		int padQuantity;
 		int phoneQuantity;
 		int orderDishId;
 		int tableId;
 
 		public OrderedDish(Dish dish, int quantity, int tableId, int type) {
 			this.dish = dish;
 			this.tableId = tableId;
 			if (type == MODE_PAD) {
 				this.padQuantity = quantity;
 				this.phoneQuantity = 0;
 			} else if (type == MODE_PHONE) {
 				this.phoneQuantity = quantity;
 				this.padQuantity = 0;
 			}
 
 		}
 
 		public OrderedDish(Dish dish, int quantity, int id, int tableId,
 				int type) {
 			this.dish = dish;
 			this.orderDishId = id;
 			this.tableId = tableId;
 			if (type == MODE_PAD) {
 				this.padQuantity = quantity;
 				this.phoneQuantity = 0;
 			} else if (type == MODE_PHONE) {
 				this.phoneQuantity = quantity;
 				this.padQuantity = 0;
 			}
 		}
 
 		public String getName() {
 			return dish.getName();
 		}
 
 		public int getQuantity() {
 			return padQuantity + phoneQuantity;
 		}
 
 		public double getPrice() {
 			return dish.getPrice();
 		}
 
 		public int getDishId() {
 			return this.orderDishId;
 		}
 
 		public int getId() {
 			return dish.getId();
 		}
 
 		public int getTableId() {
 			return this.tableId;
 		}
 	}
 
 	private CnkDbHelper mCnkDbHelper;
 	protected SQLiteDatabase mDb;
 	protected static List<OrderedDish> mOrder = new ArrayList<OrderedDish>();
 
 	public MyOrder(Context context) {
 		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME,
 				null, 1);
 		mDb = mCnkDbHelper.getReadableDatabase();
 	}
 
 	public int addOrder(Dish dish, int quantity, int tableId, int type) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dish.getId()) {
 				if (type == MODE_PAD) {
 					item.padQuantity += quantity;
 				} else if (type == MODE_PHONE) {
 					item.phoneQuantity += quantity;
 				}
 				return 0;
 			}
 		}
 		mOrder.add(new OrderedDish(dish, quantity, tableId, type));
 		return 0;
 	}
 
 	public int add(Dish dish, int quantity, int tableId, int type) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dish.getId()) {
 				item.padQuantity += quantity;
 				return 0;
 			}
 		}
 
 		mOrder.add(new OrderedDish(dish, quantity, tableId, type));
 		return 0;
 	}
 
 	public int addItem(Dish dish, int quantity, int id, int tableId) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dish.getId()) {
 				item.padQuantity += quantity;
 				return 0;
 			}
 		}
 
 		mOrder.add(new OrderedDish(dish, quantity, id, tableId));
 		return 0;
 	}
 
 	public int add(int position, int quantity) {
 		mOrder.get(position).padQuantity += quantity;
 		return 0;
 	}
 
 	public int minus(Dish dish, int quantity) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dish.getId()) {
 				if (item.padQuantity > 0) {
 					item.padQuantity -= quantity;
 				} else if (item.padQuantity == 0 && item.phoneQuantity > 0) {
 					item.phoneQuantity -= quantity;
 				} else {
 					mOrder.remove(item);
 				}
 				return 0;
 			}
 		}
 
 		return 0;
 	}
 
 	public int minus(int position, int quantity) {
 		if ((mOrder.get(position).padQuantity + mOrder.get(position).phoneQuantity) > quantity) {
 			if (mOrder.get(position).padQuantity > quantity) {
 				mOrder.get(position).padQuantity -= quantity;
 			} else {
 				quantity -= mOrder.get(position).padQuantity;
 				mOrder.get(position).padQuantity = 0;
 				mOrder.get(position).phoneQuantity -= quantity;
 				return mOrder.get(position).phoneQuantity;
 			}
 		} else {
 			mOrder.remove(position);
 		}
 		return 0;
 	}
 
 	public int count() {
 		return mOrder.size();
 	}
 
 	public int totalQuantity() {
 		int count = 0;
 
 		for (OrderedDish item : mOrder) {
 			count += (item.padQuantity + item.phoneQuantity);
 		}
 		return count;
 	}
 
 	public int getDishId(int position) {
 		if (position < mOrder.size()) {
 			return mOrder.get(position).dish.getId();
 		}
 		return -1;
 	}
 
 	public double getTotalPrice() {
 		double totalPrice = 0;
 
 		for (OrderedDish item : mOrder) {
 			totalPrice += (item.padQuantity + item.phoneQuantity)
 					* item.dish.getPrice();
 		}
 
 		return totalPrice;
 	}
 
 	public int getTableId() {
 		return mOrder.get(0).getTableId();
 	}
 
 	public OrderedDish getOrderedDish(int position) {
 		return mOrder.get(position);
 	}
 
 	public void removeItem(int did) {
 		mOrder.remove(did);
 	}
 
 	public void clear() {
 		mOrder.clear();
 	}
 	
 	public void phoneClear(){
 		for (int i = 0; i < mOrder.size(); i++) {
 			OrderedDish item = (OrderedDish) mOrder.get(i);
 			if (item.padQuantity == 0) {
 				mOrder.remove(item);
 				i--;
 			} else {
 				item.phoneQuantity = 0;
 			}
 
 		}
 	}
 	
 	public void talbeClear() {
 		if (count() > 0 && getTableId() != Info.getTableId()) {
 			mOrder.clear();
 			Log.d("talbeClear", "clear");
 		}
 	}
 
 	public int getOrderedCount(int did) {
 		for (OrderedDish dish : mOrder) {
 			if (dish.getId() == did) {
 				return (dish.padQuantity + dish.phoneQuantity);
 			}
 		}
 		return 0;
 	}
 
 	public String submit() {
 		JSONObject order = new JSONObject();
 		Date date = new Date();
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String time = df.format(date);
 
 		if (mOrder.size() <= 0) {
 			return null;
 		}
 
 		try {
 			order.put("tableId", Info.getTableId());
 			order.put("tableName", Info.getTableName());
 			order.put("timestamp", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		JSONArray dishes = new JSONArray();
 		try {
 			for (int i = 0; i < mOrder.size(); i++) {
 				JSONObject dish = new JSONObject();
 				dish.put("id", mOrder.get(i).dish.getId());
 				dish.put("name", mOrder.get(i).dish.getName());
 				dish.put("price", mOrder.get(i).dish.getPrice());
 				dish.put(
 						"quan",
 						(mOrder.get(i).padQuantity + mOrder.get(i).phoneQuantity));
 				dishes.put(dish);
 			}
 			order.put("order", dishes);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		Log.d("JSON", order.toString());
 		String response = Http.post(Server.SUBMIT_ORDER, order.toString());
 		if (response == null) {
 			Log.d("Respond", "ok");
 		} else {
 			Log.d("Respond", response);
 		}
 		return response;
 	}
 
 	public int getTableFromDB(int tableId) {
 		String response = Http.get(Server.GET_MYORDER, "TID=" + tableId);
 		Log.d("resp", response);
 		try {
 			JSONArray tableList = new JSONArray(response);
 			int length = tableList.length();
 			clear();
 			for (int i = 0; i < length; i++) {
 				JSONObject item = tableList.getJSONObject(i);
 				int quantity = item.getInt("quantity");
 				int dishId = item.getInt("dish_id");
 				double dishPrice = item.getInt("price");
 				Log.d("tableFromDB", "quantity" + quantity + "dishId" + dishId
 						+ "dishPrice" + dishPrice);
 				String name = getDishName(dishId);
 				Dish mDish = new Dish(dishId, name, dishPrice, null);
 				addOrder(mDish, quantity, tableId, MODE_PAD);
 			}
 			return 0;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public int getTablePhoneFromDB(int tableId) {
 		talbeClear();
 		String response = Http.get(Server.GET_GETPHONEORDER, "TID=" + tableId);
 		Log.d("resp", "Phone:" + response);
 		if (response == null) {
 			return -1;
 		}
 		try {
 			JSONArray tableList = new JSONArray(response);
 			int length = tableList.length();
 			for (int i = 0; i < mOrder.size(); i++) {
 				OrderedDish item = (OrderedDish) mOrder.get(i);
 				if (item.padQuantity == 0) {
 					mOrder.remove(item);
 					i--;
 				} else {
 					item.phoneQuantity = 0;
 				}
 
 			}
 
 			for (int i = 0; i < length; i++) {
 				JSONObject item = tableList.getJSONObject(i);
 				int quantity = item.getInt("quantity");
 				int dishId = item.getInt("dish_id");
 				Cursor cur = getDishNameAndPriceFromDB(dishId);
 				String name = cur.getString(0);
 				double dishPrice = cur.getDouble(1);
 				Dish mDish = new Dish(dishId, name, dishPrice, null);
 				addOrder(mDish, quantity, tableId, MODE_PHONE);
 				Log.d("phone", "phoneNum :" + i);
 			}
 			return 0;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return -1;
 	}
 
 	public String getDishName(int index) {
 		String name = getDishNameFromDB(index);
 		if (name == null) {
 			return "菜名为空";
 		}
 		return name;
 	}
 
 	private String getDishNameFromDB(int id) {
 		Cursor cur = mDb.query(CnkDbHelper.DISH_TABLE_NAME,
 				new String[] { CnkDbHelper.DISH_NAME }, CnkDbHelper.DISH_ID
 						+ "=" + id, null, null, null, null);
 
 		if (cur.moveToNext()) {
 			return cur.getString(0);
 		}
 		return null;
 	}
 
 	private Cursor getDishNameAndPriceFromDB(int id) {
 		Cursor cur = mDb.query(CnkDbHelper.DISH_TABLE_NAME, new String[] {
 				CnkDbHelper.DISH_NAME, CnkDbHelper.DISH_PRICE },
 				CnkDbHelper.DISH_ID + "=" + id, null, null, null, null);
 
 		if (cur.moveToNext()) {
 			return cur;
 		}
 		return null;
 	}
 
 	public int delPhoneTable(int tableId, int dishId) {
 		String tableStatusPkg;
 		if (dishId == 0) {
 			tableStatusPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
 					+ tableId);
 		} else {
 			tableStatusPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
 					+ tableId + "&DID=" + dishId);
 		}
 		Log.d("Respond", "tableStatusPkg: " + tableStatusPkg);
 		if (tableStatusPkg == null) {
 			return -1;
 		}
 
 		return 0;
 	}
 
 	public int updatePhoneOrder(int tableId, int quantity, int dishId) {
 		String phoneOrderPkg = Http.get(Server.UPDATE_PHONE_ORDER, "DID="
 				+ dishId + "&DNUM=" + quantity + "&TID=" + tableId);
 		Log.d("resp", "resp:" + phoneOrderPkg);
 		if (phoneOrderPkg == null) {
 			return -1;
 		}
 		return 0;
 	}
 
 	public int delDish(int dishId) {
 		Log.d("DID", "" + dishId);
 		JSONObject order = new JSONObject();
 		Date date = new Date();
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String time = df.format(date);
 		if (mOrder.size() <= 0) {
 			return -1;
 		}
 		try {
 			order.put("tableId", Info.getTableId());
 			order.put("tableName", Info.getTableName());
 			order.put("timestamp", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		JSONArray dishes = new JSONArray();
 		try {
 
 			if (dishId == -1) {
 				for (int i = 0; i < mOrder.size(); i++) {
 					JSONObject dish = new JSONObject();
					dish.put("dishId", mOrder.get(i).dish.getId());
 					dish.put("name", mOrder.get(i).dish.getName());
 					dish.put("price", mOrder.get(i).dish.getPrice());
 					dish.put(
 							"quan",
 							(mOrder.get(i).padQuantity + mOrder.get(i).phoneQuantity));
 					dish.put("id", mOrder.get(i).getDishId());
 					dishes.put(dish);
 				}
 			} else {
 				JSONObject dish = new JSONObject();
 				dish.put("dishId", mOrder.get(dishId).dish.getId());
 				dish.put("name", mOrder.get(dishId).dish.getName());
 				dish.put("price", mOrder.get(dishId).dish.getPrice());
 				dish.put(
 						"quan",
 						(mOrder.get(dishId).padQuantity + mOrder.get(dishId).phoneQuantity));
 				dish.put("id", mOrder.get(dishId).getDishId());
 				dishes.put(dish);
 			}
 			order.put("order", dishes);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		Log.d("JSON", order.toString());
 
 		String response = Http.post(Server.DEL_ORDER, order.toString());
 		Log.d("response", "response:"+response);
 		if (response == null) {
 			return -1;
 		}
 		return 0;
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		if (mDb != null) {
 			mDb.close();
 		}
 		super.finalize();
 	}
 
 }
