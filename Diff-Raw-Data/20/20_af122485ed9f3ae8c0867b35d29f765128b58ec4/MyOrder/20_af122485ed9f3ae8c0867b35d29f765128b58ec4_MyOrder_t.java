 package com.htb.cnk.data;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.DecimalFormat;
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
 import android.util.Log;
 
 import com.htb.cnk.lib.ErrorPHP;
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 public class MyOrder {
 	protected final static String TAG = "MyOrder";
 	public final static int ERR_GET_PHONE_ORDER_FAILED = -10;
 	public final static int RET_NULL_PHONE_ORDER = 1;
 	public final static int RET_MINUS_SUCC = -2;
 	public final static int DEL_ALL_ORDER = -1;
	public final static int UPDATE_ORDER = 1;
	public final static int DEL_ITEM_ORDER = 2;
 	final static int MODE_PAD = 0;
 	protected final static int MODE_PHONE = 1;
 	protected final static int TIME_OUT = -1;
 	private CnkDbHelper mCnkDbHelper;
 	protected SQLiteDatabase mDb;
 	private Context mDelDlgActivity;
 	protected static List<OrderedDish> mOrder = new ArrayList<OrderedDish>();
 	protected int persons;
 	public static String[] mFlavorName;
 	public static String comment = "";
 
 	public MyOrder(Context context) {
 		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME,
 				null, 1);
 		mDb = mCnkDbHelper.getReadableDatabase();
 		mDelDlgActivity = context;
 	}
 
 	public void setPersons(int persons) {
 		this.persons = persons;
 	}
 
 	public int getPersons() {
 		return persons;
 	}
 
 	public int addOrder(Dish dish, float quantity, int tableId, int status,
 			int type) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dish.getId()) {
 				if (type == MODE_PAD) {
 					item.padQuantity += quantity;
 					item.status += status;
 				} else if (type == MODE_PHONE) {
 					item.phoneQuantity += quantity;
 				}
 				return 0;
 			}
 		}
 		mOrder.add(new OrderedDish(this, dish, quantity, tableId, status, type));
 		return 0;
 	}
 
 	public int add(Dish dish, float quantity, int tableId, int type) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dish.getId()) {
 				item.padQuantity += quantity;
 				return 0;
 			}
 		}
 
 		mOrder.add(new OrderedDish(this, dish, quantity, tableId, 1, type));
 		return 0;
 	}
 
 	public int add(int position, float quantity) {
 		mOrder.get(position).padQuantity += quantity;
 		return 0;
 	}
 
 	public int count() {
 		return mOrder.size();
 	}
 
 	public int getTotalQuantity() {
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
 
 	public float getTotalPrice() {
 		float totalPrice = 0;
 
 		for (OrderedDish item : mOrder) {
 			totalPrice += (item.padQuantity + item.phoneQuantity)
 					* item.dish.getPrice();
 		}
 
 		return totalPrice;
 	}
 
 	public int getTableId() {
 		return mOrder.get(0).getTableId();
 	}
 
 	public int getDishStatus(int index) {
 		return mOrder.get(index).getStatus();
 	}
 
 	public String getName(int index) {
 		return mOrder.get(index).getName();
 	}
 
 	public float getQuantity(int index) {
 		return mOrder.get(index).getQuantity();
 	}
 
 	public double getPrice(int index) {
 		return mOrder.get(index).getPrice();
 	}
 
 	public int getId(int index) {
 		return mOrder.get(index).getTableId();
 	}
 
 	public void setComment(String str) {
 		comment = str;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public int setDishStatus(int index) {
 		int status = 0;
 		String quanStr = convertFloat(mOrder.get(index).getQuantity());
 		String quantity[] = quanStr.split("\\.");
 		if (quantity.length > 1) {
 			status = Integer.parseInt(quantity[0]) + 1;
 		}
 		int ret = updateServerServedDish(Info.getTableId(), mOrder.get(index)
 				.getDishId(), status);
 		if (ret < 0) {
 			return ret;
 		} else {
 			mOrder.get(index).addStatus(status == 0 ? 1 : status);
 		}
 		return 0;
 	}
 
 	public int setFlavor(boolean[] flavor, int index) {
 		if (mOrder.size() < index) {
 			return -1;
 		}
 		mOrder.get(index).setFlavor(flavor);
 		return 0;
 	}
 
 	public boolean[] slectedFlavor(int index) {
 		boolean[] slected = new boolean[mFlavorName.length];
 		if (mOrder.get(index).getFlavor() == null) {
 			return slected;
 		}
 		slected = mOrder.get(index).getFlavor();
 		return slected;
 	}
 
 	public OrderedDish getOrderedDish(int position) {
 		return mOrder.get(position);
 	}
 
 	public void removeItem(int position) {
 		mOrder.remove(position);
 	}
 
 	public int remove(int dishId) {
 		for (OrderedDish item : mOrder) {
 			if (item.dish.getId() == dishId) {
 				mOrder.remove(item);
 				return 0;
 			}
 		}
 		return -1;
 	}
 
 	public void clear() {
 		mOrder.clear();
 		comment = "";
 	}
 
 	public void talbeClear() {
 		if (count() > 0 && getTableId() != Info.getTableId()) {
 			mOrder.clear();
 		}
 	}
 
 	public float getOrderedCount(int did) {
 		for (OrderedDish dish : mOrder) {
 			if (dish.getDishId() == did) {
 				return (dish.padQuantity + dish.phoneQuantity);
 			}
 		}
 		return 0;
 	}
 
 	public int submit() {
 		JSONObject order = new JSONObject();
 		Date date = new Date();
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String time = df.format(date);
 
 		if (mOrder.size() <= 0) {
 			return -1;
 		}
 
 		int ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
 		if (ret < 0) {
 			return ret;
 		}
 
 		try {
 			order.put("waiter", UserData.getUserName());
 			order.put("waiterId", UserData.getUID());
 			order.put("tableId", Info.getTableId());
 			order.put("persons", persons);
 			order.put("tableName", Info.getTableName());
 			if (!"".equals(comment.trim())) {
 				order.put("comment", comment);
 			}
 			order.put("timestamp", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		JSONArray dishes = new JSONArray();
 		try {
 			for (int i = 0; i < mOrder.size(); i++) {
 				String flavorStr = null;
 				flavorStr = getFlavorNameToSubmit(i, flavorStr);
 				JSONObject dish = new JSONObject();
 				dish.put("dishId", mOrder.get(i).dish.getId());
 				dish.put("name", mOrder.get(i).dish.getName());
 				dish.put("price", mOrder.get(i).dish.getPrice());
 				dish.put("quan", mOrder.get(i).getQuantity());
 				dish.put("flavor", flavorStr);
 				dishes.put(dish);
 			}
 			order.put("order", dishes);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return -1;
 		}
 		String response = Http.post(Server.SUBMIT_ORDER, order.toString());
 		if (!ErrorPHP.isSucc(response, TAG)) {
 			return -1;
 		}
 		return 0;
 
 	}
 
 	private String getFlavorNameToSubmit(int i, String flavorStr) {
 		if (mOrder.get(i).flavor != null) {
 			StringBuffer flavorStrBuf = new StringBuffer();
 			for (int k = 0; k < mOrder.get(i).flavor.length; k++) {
 				if (mOrder.get(i).flavor[k] == true) {
 					flavorStrBuf.append(MyOrder.mFlavorName[k] + ",");
 				}
 			}
 			if (!flavorStrBuf.toString().equals("")) {
 				flavorStr = flavorStrBuf.toString().substring(0,
 						flavorStrBuf.length() - 1);
 			}
 		}
 		return flavorStr;
 	}
 
 	public static int loodPersons(int tableId) {
 		String response = Http.get(Server.GET_PERSONS, "TID=" + tableId);
 		if ("null".equals(response)) {
 			Log.w(TAG, "getPersonsFromServer.null");
 			return -2;
 		} else if (response == null) {
 			Log.e(TAG, "getPersonsFromServer.timeOut");
 			return TIME_OUT;
 		}
 		try {
 			int start = response.indexOf('[');
 			int end = response.indexOf(']');
 			if (start < 0 || end < 0 || (end - start) > 4) {
 				Log.e(TAG, "getPersons failed:" + response);
 				return -1;
 			} else {
 				String persons = response.substring(start + 1, end);
 				return Integer.valueOf(persons);
 			}
 
 		} catch (Exception e) {
 			Log.e(TAG, response);
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public int getFLavorFromServer() {
 		String response = Http.get(Server.GET_FLAVOR, "");
 		if ("null".equals(response)) {
 			Log.w(TAG, "getFLavorFromServer.null");
 			return -2;
 		} else if (response == null) {
 			Log.e(TAG, "getFLavorFromServer.timeOut");
 			return TIME_OUT;
 		}
 		try {
 			JSONArray flavor = new JSONArray(response);
 			mFlavorName = new String[flavor.length()];
 			int length = flavor.length();
 			for (int i = 0; i < length; i++) {
 				mFlavorName[i] = flavor.getString(i);
 			}
 			return 0;
 		} catch (Exception e) {
 			Log.e(TAG, response);
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public int getOrderFromServer(int tableId) {
 		String response = Http.get(Server.GET_MYORDER, "TID=" + tableId);
 		if ("null".equals(response)) {
 			Log.w(TAG, "getOrderFromServer.null");
 			return -2;
 		} else if (response == null) {
 			Log.e(TAG, "getOrderFromServer.timeOut");
 			return TIME_OUT;
 		}
 		try {
 			JSONArray tableList = new JSONArray(response);
 			int length = tableList.length();
 			clear();
 			for (int i = 0; i < length; i++) {
 				JSONObject item = tableList.getJSONObject(i);
 				float quantity = (float) item.getDouble("quantity");
 				int dishId = item.getInt("dish_id");
 				int status = item.getInt("status");
 				Float dishPrice = (float) item.getDouble("price");
 				String name = getDishName(dishId);
 				Dish mDish = new Dish(dishId, name, dishPrice, null);
 				addOrder(mDish, quantity, tableId, status, MODE_PAD);
 			}
 			return 0;
 		} catch (Exception e) {
 			Log.e(TAG, response);
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public void updateQuantity(int index, float quantity) {
 		mOrder.get(index).phoneQuantity = 0;
 		mOrder.get(index).padQuantity = quantity;
 	}
 
 	public static String convertFloat(float quantity) {
 		DecimalFormat format = new DecimalFormat("0.00");
 		String quantityStr[] = format.format(quantity).split("\\.");
 		if (quantityStr[1].equals("00")) {
 			return quantityStr[0];
 		} else {
 			return quantityStr[0] + "." + quantityStr[1];
 		}
 	}
 
 	public String getDishName(int index) {
 		String name = getDishNameFromDB(index);
 		if (name == null) {
 			return "菜名为空";
 		}
 		return name;
 	}
 
 	public int submitDelDish(int position, int type) {
 		String response;
 		JSONObject order = new JSONObject();
 		Date date = new Date();
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String time = df.format(date);
 		if (mOrder.size() <= 0) {
 			return -1;
 		}
 		int ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
 		if (ret < 0) {
 			return ret;
 		}
 		try {
 			order.put("waiter", UserData.getUserName());
 			order.put("tableId", Info.getTableId());
 			order.put("tableName", Info.getTableName());
 			order.put("timestamp", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		JSONArray dishes = new JSONArray();
 		try {
 
 			if (type == DEL_ALL_ORDER) {
 				for (int i = 0; i < mOrder.size(); i++) {
 					JSONObject dish = new JSONObject();
 					dish.put("dishId", mOrder.get(i).dish.getId());
 					dish.put("name", mOrder.get(i).dish.getName());
 					dish.put("price", mOrder.get(i).dish.getPrice());
 					dish.put(
 							"quan",
 							(mOrder.get(i).padQuantity + mOrder.get(i).phoneQuantity));
 					dishes.put(dish);
 				}
 			} else {
 				JSONObject dish = new JSONObject();
 				dish.put("dishId", mOrder.get(position).dish.getId());
 				dish.put("name", mOrder.get(position).dish.getName());
 				dish.put("price", mOrder.get(position).dish.getPrice());
				if (type == DEL_ITEM_ORDER) {
					dish.put("quan", (mOrder.get(position).padQuantity + mOrder
							.get(position).phoneQuantity));
				} else {
					dish.put("quan", 1);
				}

 				dishes.put(dish);
 			}
 			order.put("order", dishes);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		if (type == DEL_ALL_ORDER) {
 			response = Http.post(Server.DEL_ORDER, order.toString());
 		} else {
 			response = Http.post(
 					Server.UPDATE_TABLE_ORDER + "?TID=" + Info.getTableId()
 							+ "&DID=" + mOrder.get(position).dish.getId()
 							+ "&TYPE=" + type, order.toString());
 		}
 		if (!ErrorPHP.isSucc(response, TAG)) {
 			return -2;
 		}
 		return 0;
 
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
 
 	protected Cursor getDishNameAndPriceFromDB(int id) {
 		Cursor cur = mDb.query(CnkDbHelper.DISH_TABLE_NAME, new String[] {
 				CnkDbHelper.DISH_NAME, CnkDbHelper.DISH_PRICE },
 				CnkDbHelper.DISH_ID + "=" + id, null, null, null, null);
 
 		if (cur.moveToNext()) {
 			return cur;
 		}
 		return null;
 	}
 
 	private int updateServerServedDish(int tableId, int dishId, int status) {
 		String dishStatusPkg = Http.get(Server.SERVE_ORDER, "TID=" + tableId
 				+ "&DID=" + dishId + "&STATUS=" + status);
 		if (ErrorPHP.isSucc(dishStatusPkg, TAG)) {
 			return 0;
 		} else {
 			return -1;
 		}
 	}
 
 	public void showServerDelProgress() {
 		Method showDelProcessDlg;
 		try {
 			showDelProcessDlg = mDelDlgActivity.getClass().getMethod(
 					"showDeletePhoneOrderProcessDlg", new Class[0]);
 			showDelProcessDlg.invoke(mDelDlgActivity, new Object[0]);
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		if (mDb != null) {
 			mDb.close();
 		}
 		super.finalize();
 	}
 
 	public void removeServedDishes() {
 		for (int i = 0; i < mOrder.size(); i++) {
 			OrderedDish item = (OrderedDish) mOrder.get(i);
 			mOrder.get(i).padQuantity = item.getQuantity() - item.status;
 			if (mOrder.get(i).padQuantity <= 0) {
 				mOrder.remove(i);
 				i--;
 			}
 		}
 	}
 
 }
