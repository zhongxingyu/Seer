 package com.htb.cnk;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ClipData.Item;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.adapter.CategoryListAdapter;
 import com.htb.cnk.adapter.DishListAdapter;
 import com.htb.cnk.data.Categories;
 import com.htb.cnk.data.Dish;
 import com.htb.cnk.data.Dishes;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.constant.ErrorNum;
 
 /**
  * @author josh
  * 
  */
 public class MenuActivity extends BaseActivity {
 
 	private final int DO_NOTHING = 0;
 	private final int FINISH_ACTIVITY = 1;
 	private ListView mCategoriesLst;
 	private ListView mDishesLst;
 	private Button mBackBtn;
 	private Button mSettingsBtn;
 	private Button mMyOrderBtn;
 	private TextView mDishLstTitle;
 	private TextView mOrderedDishCount;
 	private Categories mCategories;
 	private Dishes mDishes;
 	private DishListAdapter mDishLstAdapter;
 	private MyOrder mMyOrder;
 	private ProgressDialog mpDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.menu_activity);
 		mMyOrder = new MyOrder(MenuActivity.this);
 		mDishes = new Dishes(this);
 		mCategories = new Categories(this);
 		findViews();
 		setListData();
 		setClickListener();
 
 		mpDialog = new ProgressDialog(MenuActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		//mpDialog.setTitle("请稍等");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 
 		if (Info.isNewCustomer() && Info.getMode() == Info.WORK_MODE_CUSTOMER) {
 			showGuide();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updateOrderedDishCount();
 		if (mDishLstAdapter != null) {
 			mDishLstAdapter.notifyDataSetChanged();
 		}
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back_btn);
 		mSettingsBtn = (Button) findViewById(R.id.settings_btn);
 		mMyOrderBtn = (Button) findViewById(R.id.myOrder);
 		mCategoriesLst = (ListView) findViewById(R.id.categories);
 		mDishesLst = (ListView) findViewById(R.id.dishes);
 		mDishLstTitle = (TextView) findViewById(R.id.category);
 		mOrderedDishCount = (TextView) findViewById(R.id.orderedCount);
 		mSettingsBtn.setVisibility(View.INVISIBLE);
 	}
 
 	private View getMenuView(int position, View convertView) {
 		ItemViewHolder viewHolder;
 		Dish dishDetail = mDishes.getDish(position);
 		
 		if (convertView == null) {
 			convertView = LayoutInflater.from(MenuActivity.this).inflate(
 					R.layout.item_dish, null);
 			viewHolder = new ItemViewHolder();
 			viewHolder.findViews(convertView, ItemViewHolder.ITEM_ORDER_VIEW);
 			viewHolder.setOnClickListener();
 			convertView.setTag(viewHolder);
 		} else {
 			viewHolder = (ItemViewHolder)convertView.getTag();
 		}
 	
 		viewHolder.setPic(position, dishDetail.getPic());
 		viewHolder.setData(dishDetail);
 		viewHolder.setTag(position);
 		return convertView;
 	}
 
 	private View getFastOrderMenu(int position, View convertView) {
 		ItemViewHolder viewHolder;
 		Dish dishDetail = mDishes.getDish(position);
 		
 		if (convertView == null) {
 			viewHolder = new ItemViewHolder();
 			convertView = LayoutInflater.from(MenuActivity.this).inflate(
 					R.layout.item_fastorder, null);
 			viewHolder.findViews(convertView, ItemViewHolder.ITEM_FASTORDER_VIEW);
 			convertView.setTag(viewHolder);
 		} else {
 			viewHolder = (ItemViewHolder)convertView.getTag();
 		}
 		
 		viewHolder.setData(dishDetail);
 		viewHolder.setOnClickListener();
 		viewHolder.setTag(position);
 		return convertView;
 	}
 
 	private FileInputStream getPic(String name) {
 		FileInputStream isBigPic = null;
 		try {
 			isBigPic = openFileInput("hdpi_" + name);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return isBigPic;
 	}
 
 	private FileInputStream getThumbnail(String name) {
 		FileInputStream inStream = null;
 		try {
 			// TODO usehdpi cause no ldpi pic available
 			if (name == null || "".equals(name) || "null".equals(name)) {
 				return null;
 			}
 			Log.d("fileName", "hdpi_" + name);
 			inStream = openFileInput("hdpi_" + name);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return inStream;
 	}
 
 	private void setListData() {
 		if (mCategories.count() <= 0) {
 			errorAccurDlg("菜谱数据损坏,请更新菜谱!", FINISH_ACTIVITY);
 			return;
 		}
 		setCategories();
 		setDishes();
 		updateOrderedDishCount();
 	}
 
 	private void setCategories() {
 		mCategoriesLst.setAdapter(new CategoryListAdapter(this, mCategories));
 	}
 
 	private void setDishes() {
 		mDishLstAdapter = new DishListAdapter(this, mDishes) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup arg2) {
 				if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
 					return getMenuView(position, convertView);
 				} else {
 					return getFastOrderMenu(position, convertView);
 				}
 			}
 		};
 		if (mCategories.count() <= 0) {
 			return;
 		}
 		mDishLstTitle.setText(mCategories.getName(0));
 		mDishesLst.setAdapter(mDishLstAdapter);
 		View emptyView = getLayoutInflater().inflate(R.layout.empty_list, null);
 
 		((ViewGroup) mDishesLst.getParent()).addView(emptyView,
 				new LayoutParams(LayoutParams.WRAP_CONTENT,
 						LayoutParams.WRAP_CONTENT));
 		mDishesLst.setEmptyView(emptyView);
 		updateDishes(0);
 	}
 
 	private void setThumbnail(int position, ImageButton dishPic,
 			FileInputStream inStream) {
 		Bitmap photo = BitmapFactory.decodeStream(inStream);
 		Drawable drawable = new BitmapDrawable(photo);
 		dishPic.setTag(position);
 		dishPic.setBackgroundDrawable(drawable);
 		dishPic.setOnClickListener(thumbnailClicked);
 	}
 
 	private void setClickListener() {
 		mBackBtn.setOnClickListener(backBtnClicked);
 		mSettingsBtn.setOnClickListener(settingBtnClicked);
 		mMyOrderBtn.setOnClickListener(myOrderBtnClicked);
 		mCategoriesLst.setOnItemClickListener(CategoryListClicked);
 	}
 
 	private void updateOrderedDishCount() {
 		mOrderedDishCount.setText(Integer.toString(mMyOrder.totalQuantity()));
 	}
 
 	private void updateDishes(final int position) {
 		mDishes.clear();
 		mDishLstAdapter.notifyDataSetChanged();
		new Thread() {
			public void run() {
				int ret = mDishes.setCategory(
						mCategories.getCategoryId(position),
						mCategories.getTableName(position));
				handler.sendEmptyMessage(ret);
			}
		}.start();
 	}
 
 	private void updateDishQuantity(final int position, final int quantity) {
 		if (quantity < 0) {
 			new Thread() {
 				public void run() {
 					int ret = mMyOrder.minus(mDishes.getDish(position), -quantity);
 					minushandler.sendEmptyMessage(ret);
 				}
 			}.start();
 			
 		} else {
 			mMyOrder.add(position, quantity);
 		}
 		updateOrderedDishCount();
 		mDishLstAdapter.notifyDataSetChanged();
 	
 	}
 
 	//TODO Define
 	public void showDeletePhoneOrderProcessDlg() {
 		minushandler.sendEmptyMessage(2);
 	}
 
 	private void showGuide() {
 		final Dialog dialog = new Dialog(MenuActivity.this,
 				R.style.FULLTANCStyle);
 
 		dialog.setContentView(R.layout.guide);
 		dialog.setCancelable(true);
 
 		ImageButton btnCancel = (ImageButton) dialog.findViewById(R.id.guide);
 		btnCancel.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View view) {
 				dialog.cancel();
 			}
 		});
 		Info.setNewCustomer(false);
 		dialog.show();
 	}
 
 	private void errorAccurDlg(String msg, final int action) {
 		new AlertDialog.Builder(MenuActivity.this).setTitle("错误")
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
 
 
 	private OnItemClickListener CategoryListClicked = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View view, int position,
 				long arg3) {
 			if (!mCategories.getName(position).equals(mDishLstTitle.getText())) {
 				mDishLstTitle.setText(mCategories.getName(position));
 				updateDishes(position);
 			}
 		}
 	};
 
 	private OnClickListener backBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			MenuActivity.this.finish();
 		}
 	};
 
 	private OnClickListener settingBtnClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(MenuActivity.this, TableActivity.class);
 			MenuActivity.this.startActivity(intent);
 			finish();
 		}
 
 	};
 
 	private OnClickListener myOrderBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (Info.getMode() == Info.WORK_MODE_PHONE) {
 				finish();
 			} else {
 				Intent intent = new Intent();
 				intent.setClass(MenuActivity.this, MyOrderActivity.class);
 				startActivity(intent);
 				finish();
 			}
 		}
 	};
 
 	private OnClickListener thumbnailClicked = new Button.OnClickListener() {
 		public void onClick(View view) {
 			final int position = Integer.parseInt(view.getTag().toString());
 			final Dialog dialog = new Dialog(MenuActivity.this,
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
 
 	private OnClickListener minusClicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			updateDishQuantity(position, -1);
 		}
 	};
 	
 	private OnClickListener plusClicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
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
 					Toast.makeText(MenuActivity.this, "服务器开小差了,系统将显示全部菜单.",
 							Toast.LENGTH_SHORT).show();
 				}
 				
 			}
 
 			mDishLstAdapter.notifyDataSetChanged();
 		}
 	};
 
 	class ItemViewHolder {
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
 			orderedCount = (TextView) convertView.findViewById(R.id.orderedCount);
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
 				Resources resources = MenuActivity.this.getResources();
 				dishPic.setBackgroundDrawable(resources
 						.getDrawable(R.drawable.no_pic_bigl));
 				dishPic.setOnClickListener(null);
 			}
 		}
 		void setData(Dish dishDetail) {
 			dishName.setText(dishDetail.getName());
 			dishPrice.setText(Double.toString(dishDetail.getPrice()) + " 元/份");
 			int orderCount = mMyOrder.getOrderedCount(dishDetail.getId());
 			if (orderCount > 0) {
 				orderedCount.setText(Integer.toString(orderCount));
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
