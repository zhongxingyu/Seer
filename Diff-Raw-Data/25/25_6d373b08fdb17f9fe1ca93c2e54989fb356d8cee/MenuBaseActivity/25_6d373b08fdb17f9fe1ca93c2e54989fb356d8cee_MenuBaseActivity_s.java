 package com.htb.cnk.lib;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.MyOrderActivity;
 import com.htb.cnk.R;
 import com.htb.cnk.adapter.DishListAdapter;
 import com.htb.cnk.data.Categories;
 import com.htb.cnk.data.Dish;
 import com.htb.cnk.data.Dishes;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.constant.ErrorNum;
 
 public class MenuBaseActivity extends BaseActivity {
 
 	protected final int DO_NOTHING = 0;
 	protected final int FINISH_ACTIVITY = 1;
 	protected final int SHOW_PROGRESS_DLG = 2;
 	protected ListView mCategoriesLst;
 	protected ListView mDishesLst;
 	protected Button mSettingsBtn;
 	protected Button mMyOrderBtn;
 	protected Button mBackBtn;
 	protected TextView mDishLstTitle;
 	protected TextView mOrderedDishCount;
 	protected Categories mCategories;
 	protected Dishes mDishes;
 	protected DishListAdapter mDishLstAdapter;
 	protected MyOrder mMyOrder;
 	protected ProgressDialog mpDialog;
 	protected int layout;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(layout);
 		mMyOrder = new MyOrder(MenuBaseActivity.this);
 		mDishes = new Dishes(this);
 		mCategories = new Categories(this);
 		findViews();
 		setClickListener();
 
 		mpDialog = new ProgressDialog(MenuBaseActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 	}
 
 	private OnClickListener myOrderBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (Info.getMode() == Info.WORK_MODE_PHONE) {
 				finish();
 			} else {
 				Intent intent = new Intent();
 				intent.setClass(MenuBaseActivity.this, MyOrderActivity.class);
 				startActivity(intent);
 				finish();
 			}
 		}
 	};
 
 	protected void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back_btn);
 		mSettingsBtn = (Button) findViewById(R.id.settings_btn);
 		mMyOrderBtn = (Button) findViewById(R.id.myOrder);
 		mCategoriesLst = (ListView) findViewById(R.id.categories);
 		mDishesLst = (ListView) findViewById(R.id.dishes);
 		mDishLstTitle = (TextView) findViewById(R.id.category);
 		mOrderedDishCount = (TextView) findViewById(R.id.orderedCount);
 	}
 
 	public View getMenuView(int position, View convertView) {
 		ItemViewHolder viewHolder;
 		Dish dishDetail = mDishes.getDish(position);
 
 		if (convertView == null) {
 			convertView = LayoutInflater.from(MenuBaseActivity.this).inflate(
 					R.layout.item_dish, null);
 			viewHolder = new ItemViewHolder();
 			viewHolder.findViews(convertView, ItemViewHolder.ITEM_ORDER_VIEW);
 			viewHolder.setOnClickListener();
 			convertView.setTag(viewHolder);
 		} else {
 			viewHolder = (ItemViewHolder) convertView.getTag();
 		}
 
 		viewHolder.setPic(position, dishDetail.getPic());
 		viewHolder.setData(dishDetail);
 		viewHolder.setTag(position);
 		return convertView;
 	}
 
 	public View getFastOrderMenu(int position, View convertView) {
		mSettingsBtn.setText("快捷");
 		ItemViewHolder viewHolder;
 		Dish dishDetail = mDishes.getDish(position);
 
 		if (convertView == null) {
 			viewHolder = new ItemViewHolder();
 			convertView = LayoutInflater.from(MenuBaseActivity.this).inflate(
 					R.layout.item_fastorder, null);
 			viewHolder.findViews(convertView,
 					ItemViewHolder.ITEM_FASTORDER_VIEW);
 			convertView.setTag(viewHolder);
 		} else {
 			viewHolder = (ItemViewHolder) convertView.getTag();
 		}
 
 		viewHolder.setData(dishDetail);
 		viewHolder.setOnClickListener();
 		viewHolder.setTag(position);
 		return convertView;
 	}
 
 	public FileInputStream getPic(String name) {
 		FileInputStream isBigPic = null;
 		try {
 			isBigPic = openFileInput(name);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return isBigPic;
 	}
 
 	public FileInputStream getThumbnail(String name) {
 		FileInputStream inStream = null;
 		try {
 			if (name == null || "".equals(name) || "null".equals(name)) {
 				return null;
 			}
 			inStream = openFileInput(name);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return inStream;
 	}
 
 	public void setThumbnail(int position, ImageButton dishPic,
 			FileInputStream inStream) {
 		Bitmap photo = BitmapFactory.decodeStream(inStream);
 		Drawable drawable = new BitmapDrawable(photo);
 		dishPic.setTag(position);
 		dishPic.setBackgroundDrawable(drawable);
 		dishPic.setOnClickListener(thumbnailClicked);
 	}
 
 	protected void setClickListener() {
 		mMyOrderBtn.setOnClickListener(myOrderBtnClicked);
 		mDishesLst.setOnItemClickListener(itemClicked);
 	}
 
 	public void updateOrderedDishCount() {
 		mOrderedDishCount.setText(Integer.toString(mMyOrder.totalQuantity()));
 	}
 
 	
 
 	public void updateDishQuantity(final int position, final int quantity) {
 		if (quantity < 0) {
 			minusOrder(position, quantity);
 		} else {
 			mMyOrder.add(position, quantity);
 		}
 		updateOrderedDishCount();
 		mDishLstAdapter.notifyDataSetChanged();
 	}
 
 	protected void minusOrder(final int position, final int quantity) {
 		new Thread() {
 			public void run() {
 				int ret = mMyOrder.minus(mDishes.getDish(position),
 						-quantity);
 				minushandler.sendEmptyMessage(ret);
 			}
 		}.start();
 	}
 
 	public void showDeletePhoneOrderProcessDlg() {
 		minushandler.sendEmptyMessage(SHOW_PROGRESS_DLG);
 	}
 
 	public void errorAccurDlg(String msg, final int action) {
 		new AlertDialog.Builder(MenuBaseActivity.this).setTitle("错误")
 				.setMessage(msg)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (action == FINISH_ACTIVITY) {
 							finish();
 						}
 					}
 				}).show();
 	}
 
 	private OnClickListener thumbnailClicked = new Button.OnClickListener() {
 		public void onClick(View view) {
 			final int position = Integer.parseInt(view.getTag().toString());
 			final Dialog dialog = new Dialog(MenuBaseActivity.this,
 					R.style.TANCStyle);
 
 			ImageView dishPicView;
 
 			dialog.setContentView(R.layout.dish_detail);
 			dishPicView = (ImageView) dialog.findViewById(R.id.dishBigPic);
 			dialog.setCancelable(true);
 
 			FileInputStream isBigPic = getPic(mDishes.getDish(position)
 					.getPic());
 			if (isBigPic != null) {
 				Bitmap photo = BitmapFactory.decodeStream(isBigPic);
 				Drawable drawable = new BitmapDrawable(photo);
 				dishPicView.setBackgroundDrawable(drawable);
 			} else {
 				dishPicView.setBackgroundResource(R.drawable.no_pic_bigl);
 			}
 
 			Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
 			btnCancel.setOnClickListener(new Button.OnClickListener() {
 				public void onClick(View view) {
 					dialog.cancel();
 				}
 			});
 			dialog.show();
 		}
 	};
 	
 	protected OnClickListener minusClicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			updateDishQuantity(position, -1);
 		}
 	};
 	
 	protected OnClickListener plusClicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			mMyOrder.add(mDishes.getDish(position), 1, Info.getTableId(), 0);
 			updateOrderedDishCount();
 			mDishLstAdapter.notifyDataSetChanged();
 		}
 	};
 	
 	protected OnItemClickListener itemClicked = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 				long arg3) {
 			mMyOrder.add(mDishes.getDish(position), 1, Info.getTableId(), 0);
 			updateOrderedDishCount();
 			mDishLstAdapter.notifyDataSetChanged();
 		}
 		
 	};
 	private Handler minushandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				errorAccurDlg("删除失败", DO_NOTHING);
 			} else {
 				switch (msg.what) {
 				case 0:
 					updateOrderedDishCount();
 					mDishLstAdapter.notifyDataSetChanged();
 					break;
 				default:
 					mpDialog.setMessage("正在删除...");
 					mpDialog.show();
 				}
 			}
 		}
 	};
 	
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				switch (msg.what) {
 				case ErrorNum.DB_BROKEN:
 					errorAccurDlg("菜谱数据损坏,请更新菜谱!", FINISH_ACTIVITY);
 					break;
 				default:
 					Toast.makeText(MenuBaseActivity.this, "服务器开小差了,系统将显示全部菜单.",
 							Toast.LENGTH_SHORT).show();
 				}
 
 			}
 
 			mDishLstAdapter.notifyDataSetChanged();
 		}
 	};
 
 	public class ItemViewHolder {
 		public final static int ITEM_FASTORDER_VIEW = 1;
 		public final static int ITEM_ORDER_VIEW = 2;
 		ImageButton dishPic;
 		TextView orderedCount;
 		TextView dishName;
 		TextView dishPrice;
 		Button plusBtn;
 		Button minusBtn;
 
 		void findViews(View convertView, int itemViewType) {
 			dishName = (TextView) convertView.findViewById(R.id.dishName);
 			dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
 			plusBtn = (Button) convertView.findViewById(R.id.dishPlus);
 			orderedCount = (TextView) convertView
 					.findViewById(R.id.orderedCount);
 			minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
 			if (itemViewType == ITEM_ORDER_VIEW) {
 				dishPic = (ImageButton) convertView.findViewById(R.id.pic);
 			}
 		}
 
 		void setPic(int position, String picPath) {
 			FileInputStream inStream = getThumbnail(picPath);
 			if (inStream != null) {
 				setThumbnail(position, dishPic, inStream);
 			} else {
 				Resources resources = MenuBaseActivity.this.getResources();
 				dishPic.setBackgroundDrawable(resources
 						.getDrawable(R.drawable.no_pic_bigl));
 				dishPic.setOnClickListener(null);
 			}
 		}
 
 		void setData(Dish dishDetail) {
 			dishName.setText(dishDetail.getName());
 			dishPrice.setText(Double.toString(dishDetail.getPrice()) + " 元/份");
 			float orderCount = mMyOrder.getOrderedCount(dishDetail.getId());
 			if (orderCount > 0) {
 				orderedCount.setText(mMyOrder.convertFloat(orderCount));
 			} else {
 				orderedCount.setText(" ");
 			}
 		}
 
 		void setOnClickListener() {
 			plusBtn.setOnClickListener(plusClicked);
 			minusBtn.setOnClickListener(minusClicked);
 		}
 
 		void setTag(int position) {
 			minusBtn.setTag(position);
 			plusBtn.setTag(position);
 		}
 	}
 
 }
