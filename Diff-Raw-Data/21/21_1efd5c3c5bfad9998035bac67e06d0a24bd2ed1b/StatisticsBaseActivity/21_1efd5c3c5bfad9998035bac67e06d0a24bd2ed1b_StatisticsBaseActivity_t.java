 package com.htb.cnk.ui.base;
 
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.htb.cnk.R;
 import com.htb.cnk.adapter.StatisticsAdapter;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.Statistics;
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 public class StatisticsBaseActivity extends BaseActivity {
 	private final static String TAG = "StatisticsBaseActivity";
 	public static final int QUERY_BY_TIME = 0;
 	public static final int QUERY_TODAY = 1;
 	protected Button mBackBtn;
 	protected Button mQueryTodayBtn;
 	protected Button mQueryByTimeBtn;
 	protected Button mPrintBtn;
 	protected Button mStartTimeBtn;
 	protected Button mStartDateBtn;
 	protected Button mEndTimeBtn;
 	protected Button mEndDateBtn;
 	protected TextView mStartDate;
 	protected TextView mStartTime;
 	protected TextView mEndDate;
 	protected TextView mEndTime;
 	protected TextView mTableUsage;
 	protected TextView mPersons;
 	protected TextView mTotalAmount;
 	protected ListView mSalesData;
 	protected Calendar mStart = Calendar.getInstance();
 	protected Calendar mEnd = Calendar.getInstance();
 	protected Calendar mStartSet = Calendar.getInstance();
 	protected Calendar mEndSet = Calendar.getInstance();
 	protected String mLatestStatistics;
 	protected int mQueryMode;
 	protected Statistics mStatistics;
 	protected StatisticsAdapter mStatisticsAdapter;
 	protected boolean[] expanded;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.statistics_activity);
 		mStatistics = new Statistics(StatisticsBaseActivity.this);
 		findViews();
 		setClickListener();
 		setAdapter();
 	}
 
 	protected void updateData(Calendar start, Calendar end) {
 		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
 		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
 		mStartDate.setText(date.format(start.getTime()));
 		mStartTime.setText(time.format(start.getTime()));
 		mEndDate.setText(date.format(end.getTime()));
 		mEndTime.setText(time.format(end.getTime()));
 		mTableUsage.setText(Integer.toString(mStatistics.getTableUsage()));
 		mPersons.setText(Integer.toString(mStatistics.getServedPersons()));
 		mTotalAmount.setText(MyOrder.convertFloat(mStatistics.getTotalAmount()));
 		expanded = new boolean[mStatistics.count()];
 		mStatisticsAdapter.notifyDataSetChanged();
 		mStartDateBtn.setText("设置日期");
 		mStartTimeBtn.setText("设置时间");
 		mEndDateBtn.setText("设置日期");
 		mEndTimeBtn.setText("设置时间");
 	}
 
 	protected void updateLatestStatistics() {
 		new AlertDialog.Builder(StatisticsBaseActivity.this)
 				.setTitle("请注意")
 				.setMessage("是否清零销售记录")
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 	
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						new Thread() {
 							public void run() {
 								SimpleDateFormat time = new SimpleDateFormat(
 										"HH:mm");
 								SimpleDateFormat date = new SimpleDateFormat(
 										"yyyy-MM-dd");
 								mLatestStatistics = date.format(mEnd.getTime())
 										+ " " + time.format(mEnd.getTime());
 								Http.get(
 										Server.LATEST_STATISTICS,
 										"do=set&time="
 												+ time.format(mEnd.getTime())
 												+ "&date="
 												+ date.format(mEnd.getTime()));
 	
 							}
 						}.start();
 					}
 				})
 				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 	
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 	
 					}
 				}).show();
 	}
 
 	protected void popUpDlg(String title, String msg,
 			final boolean finishActivity) {
 		new AlertDialog.Builder(StatisticsBaseActivity.this)
 				.setTitle(title)
 				.setMessage(msg)
 				.setCancelable(false)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 	
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (finishActivity) {
 							finish();
 						}
 					}
 				}).show();
 	}
 
 	protected boolean isDateTimeSet() {
 		if (mStartDateBtn.getText().toString().indexOf("-") <= 0) {
 			DateTimeNotSetAlert("没有设置开始日期");
 			return false;
 		}
 	
 		if (mStartTimeBtn.getText().toString().indexOf(":") <= 0) {
 			DateTimeNotSetAlert("没有设置开始时间");
 			return false;
 		}
 	
 		if (mEndDateBtn.getText().toString().indexOf("-") <= 0) {
 			DateTimeNotSetAlert("没有设置结束日期");
 			return false;
 		}
 	
 		if (mEndTimeBtn.getText().toString().indexOf(":") <= 0) {
 			DateTimeNotSetAlert("没有设置结束时间");
 			return false;
 		}
 	
 		return true;
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mQueryTodayBtn = (Button) findViewById(R.id.queryToday);
 		mQueryByTimeBtn = (Button) findViewById(R.id.queryByTime);
 		mPrintBtn = (Button) findViewById(R.id.print);
 		mStartDateBtn = (Button) findViewById(R.id.setStartDate);
 		mStartTimeBtn = (Button) findViewById(R.id.setStartTime);
 		mEndDateBtn = (Button) findViewById(R.id.setEndDate);
 		mEndTimeBtn = (Button) findViewById(R.id.setEndTime);
 		mStartDate = (TextView) findViewById(R.id.startDate);
 		mStartTime = (TextView) findViewById(R.id.startTime);
 		mEndDate = (TextView) findViewById(R.id.endDate);
 		mEndTime = (TextView) findViewById(R.id.endTime);
 		mTotalAmount = (TextView) findViewById(R.id.totalAmount);
 		mPersons = (TextView) findViewById(R.id.persons);
 		mSalesData = (ListView) findViewById(R.id.salesData);
 		mTableUsage = (TextView) findViewById(R.id.tableUsage);
 	}
 
 	private void setClickListener() {
 		mBackBtn.setOnClickListener(backClicked);
 		mStartDateBtn.setOnClickListener(startDateClicked);
 		mStartTimeBtn.setOnClickListener(startTimeClicked);
 		mEndDateBtn.setOnClickListener(endDateClicked);
 		mEndTimeBtn.setOnClickListener(endTimeClicked);
 	}
 
 	private void setAdapter() {
 		mStatisticsAdapter = new StatisticsAdapter(getApplicationContext(),
 				mStatistics) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 				ViewHolder viewHolder;
 
 				if (convertView == null) {
 					viewHolder = new ViewHolder();
 					convertView = LayoutInflater.from(
 							StatisticsBaseActivity.this).inflate(
 							R.layout.item_statistics, null);
 					viewHolder.dishName = (TextView) convertView
 							.findViewById(R.id.dishName);
 					viewHolder.salesCount = (TextView) convertView
 							.findViewById(R.id.salesCount);
 					viewHolder.totalAmount = (TextView) convertView
 							.findViewById(R.id.totalAmount);
 					viewHolder.percentage = (TextView) convertView
 							.findViewById(R.id.percentage);
 					viewHolder.detail = (LinearLayout) convertView.findViewById(R.id.details);
 					viewHolder.detailList = (ListView) convertView.findViewById(R.id.detailList);
 
					
 					View emptyView = getLayoutInflater().inflate(R.layout.empty_list, null);
 					((ViewGroup) viewHolder.detailList.getParent()).addView(emptyView,
 							new LayoutParams(LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 					viewHolder.detailList.setEmptyView(emptyView);
 					convertView.setTag(viewHolder);
 				} else {
 					viewHolder = (ViewHolder) convertView.getTag();
 				}
 				if (mStatistics.getQuantity(position) > 0) {
 					viewHolder.detail.setVisibility(expanded[position]?View.VISIBLE:View.GONE);
 				} else {
 					viewHolder.detail.setVisibility(View.GONE);
 				}
 				viewHolder.dishName.setText(mStatistics.getName(position));
 				viewHolder.salesCount.setText(MyOrder.convertFloat(mStatistics
 						.getQuantity(position)));
 				viewHolder.totalAmount.setText(MyOrder.convertFloat(mStatistics
 						.getAmount(position)));
 				DecimalFormat df = new DecimalFormat("0.00");
				
				double percent;
				if (mStatistics.getTotalAmount() <= 0) {
					percent = 0;
				} else {
					percent = (mStatistics.getAmount(position) * 100)
							/ mStatistics.getTotalAmount();
				}
 				viewHolder.percentage.setText(df.format(percent) + "%");
 				return convertView;
 			}
 		};
 		mSalesData.setAdapter(mStatisticsAdapter);
 	}
 
     public void setListViewHeight(ListView listView) {   
 
         ListAdapter listAdapter = listView.getAdapter();    
 
         if (listAdapter == null) {   
             return;   
         }   
 
         int totalHeight = 0;   
 
         for (int i = 0; i < listAdapter.getCount(); i++) {   
             View listItem = listAdapter.getView(i, null, listView);   
             listItem.measure(0, 0);   
             totalHeight += listItem.getMeasuredHeight();   
         }   
 
         ViewGroup.LayoutParams params = listView.getLayoutParams();   
         params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));   
         listView.setLayoutParams(params);  
     }  
     
 	private void DateTimeNotSetAlert(String err) {
 		popUpDlg("请注意", err, false);
 	}
 
 	private OnClickListener backClicked = new OnClickListener() {
 	
 		@Override
 		public void onClick(View v) {
 			StatisticsBaseActivity.this.finish();
 		}
 	};
 	private OnClickListener startDateClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			showProgressDlg("请稍等...");
 			DatePickerDialog date = new DatePickerDialog(
 					StatisticsBaseActivity.this, startDateListener,
 					mStart.get(Calendar.YEAR), mStart.get(Calendar.MONTH),
 					mStart.get(Calendar.DAY_OF_MONTH));
 			date.setCancelable(false);
 			date.show();
 			mpDialog.cancel();
 		}
 	};
 	private OnClickListener startTimeClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			showProgressDlg("请稍等...");
 			TimePickerDialog time = new TimePickerDialog(
 					StatisticsBaseActivity.this, startTimeListener,
 					mStart.get(Calendar.HOUR_OF_DAY),
 					mStart.get(Calendar.MINUTE), true);
 			time.setCancelable(false);
 			time.show();
 			mpDialog.cancel();
 		}
 	};
 	private OnClickListener endDateClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			showProgressDlg("请稍等...");
 			DatePickerDialog date = new DatePickerDialog(
 					StatisticsBaseActivity.this, endDateListener,
 					mEnd.get(Calendar.YEAR), mEnd.get(Calendar.MONTH),
 					mEnd.get(Calendar.DAY_OF_MONTH));
 			date.setCancelable(false);
 			date.show();
 			mpDialog.cancel();
 		}
 	};
 	private OnClickListener endTimeClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			showProgressDlg("请稍等...");
 			TimePickerDialog time = new TimePickerDialog(
 					StatisticsBaseActivity.this, endTimeListener,
 					mEnd.get(Calendar.HOUR_OF_DAY), mEnd.get(Calendar.MINUTE),
 					true);
 			time.setCancelable(false);
 			time.show();
 			mpDialog.cancel();
 		}
 	};
 	private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker arg0, int year, int month, int day) {
 			mStartSet.set(year, month, day);
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			mStartDateBtn.setText(df.format(mStartSet.getTime()));
 		}
 	};
 
 	private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker arg0, int year, int month, int day) {
 			mEndSet.set(year, month, day);
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 			mEndDateBtn.setText(df.format(mEndSet.getTime()));
 		}
 	};
 	private TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mStartSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			mStartSet.set(Calendar.MINUTE, minute);
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			mStartTimeBtn.setText(df.format(mStartSet.getTime()));
 		}
 	};
 	private TimePickerDialog.OnTimeSetListener endTimeListener = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mEndSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
 			mEndSet.set(Calendar.MINUTE, minute);
 			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 			mEndTimeBtn.setText(df.format(mEndSet.getTime()));
 		}
 	};
 	
 	protected OnClickListener printClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (mStatistics.count() <= 0) {
 				Toast.makeText(getApplicationContext(), "没有可打印信息",
 						Toast.LENGTH_LONG).show();
 				return;
 			}
 			showProgressDlg("正在上传打印信息...");
 			new Thread() {
 				public void run() {
 					int ret = mStatistics.print(mStartSet, mEndSet);
 					handlerPrint.sendEmptyMessage(ret);
 				}
 			}.start();
 
 //			if (mQueryMode == QUERY_TODAY) {
 //				updateLatestStatistics();
 //			}
 		}
 	};
 	
 	protected Handler handlerPrint = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				popUpDlg("错误", "打印出现错误,请检查打印机" + msg.what, false);
 			} else {
 				popUpDlg("完成", "打印完成", false);
 			}
 		}
 	};
 	
 	protected Handler handleDataLoad = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			int ret = -1;
 			switch (msg.what) {
 			case Statistics.BY_DISH:
 				ret = mStatistics.perpareResult((String)msg.obj, Statistics.BY_DISH);
 				break;
 			case Statistics.BY_CATEGORY:
 				ret = mStatistics.perpareStatisticsByCategory((String)msg.obj);
 				break;
 			case Statistics.BY_PRINTER:
 				ret = mStatistics.perpareStatisticsByPrinter((String)msg.obj);
 				break;
 			case Statistics.BY_STUFF:
 				ret = mStatistics.perparePerformanceResult((String)msg.obj);
 				break;
 			default:
 				ret = -1;
 				break;
 			}
 			if (ret < 0) {
 				popUpDlg("错误", "销售数据出错,需从新下载!", true);
 				return;
 			}
 			updateData(mStartSet, mEndSet);
 		}
 	};
 	
 	
 	protected class ViewHolder {
 		TextView dishName;
 		TextView salesCount;
 		TextView totalAmount;
 		TextView percentage;
 		LinearLayout detail;
 		ListView detailList;
 		StatisticsAdapter detailAdapter;
 		Statistics statisticsDetails;
 		
 		private void showDetails(int index) {
 			if (mStatistics.getQuantity(index) == 0) {
 				Toast.makeText(StatisticsBaseActivity.this, "没有相关数据", Toast.LENGTH_SHORT).show();
 			}
 			if (detailAdapter == null) {
 				statisticsDetails = new Statistics(StatisticsBaseActivity.this);
 				
 				detailAdapter = new StatisticsAdapter(getApplicationContext(),
 						statisticsDetails) {
 						@Override
 						public View getView(int position, View convertView, ViewGroup parent) {
 							DetailViewHolder viewHolder;
 				
 							if (convertView == null) {
 								viewHolder = new DetailViewHolder();
 								convertView = LayoutInflater.from(
 										StatisticsBaseActivity.this).inflate(
 										R.layout.item_statistics, null);
 								viewHolder.dishName = (TextView) convertView
 										.findViewById(R.id.dishName);
 								viewHolder.salesCount = (TextView) convertView
 										.findViewById(R.id.salesCount);
 								viewHolder.totalAmount = (TextView) convertView
 										.findViewById(R.id.totalAmount);
 								viewHolder.percentage = (TextView) convertView
 										.findViewById(R.id.percentage);
 								convertView.setTag(viewHolder);
 							} else {
 								viewHolder = (DetailViewHolder) convertView.getTag();
 							}
 				
 							Log.d(TAG, "name:" + statisticsDetails.getName(position));
 							Log.d(TAG, "viewHolder:" + (viewHolder==null?"null":"not"));
 
 							Log.d(TAG, "dishName:" + (viewHolder.dishName==null?"null":"not"));
 							viewHolder.dishName.setText("  " + statisticsDetails.getName(position));
 							viewHolder.salesCount.setText(MyOrder.convertFloat(statisticsDetails
 									.getQuantity(position)));
 							viewHolder.totalAmount.setText(MyOrder.convertFloat(statisticsDetails
 									.getAmount(position)));
 							DecimalFormat df = new DecimalFormat("0.00");
 							double percent = (statisticsDetails.getAmount(position) * 100)
 									/ mStatistics.getTotalAmount();
 							viewHolder.percentage.setText(df.format(percent) + "%");
 							return convertView;
 						}
 				};
 				detailList.setAdapter(detailAdapter);
 				loadData(mStatistics.getId(index));
 			}
 		}
 		
 		protected Handler handler = new Handler() {
 			public void handleMessage(Message msg) {
 				int ret = -1;
 				if (msg.what >= 0) {
 					ret = statisticsDetails.perpareResult((String)msg.obj, Statistics.BY_CATEGORY);
 				}
 				
 				if (ret < 0) {
 					popUpDlg("错误", "销售数据出错,需从新下载!", true);
 				} else {
 					detailAdapter.notifyDataSetChanged();
 					setListViewHeight(detailList);
 					//mStatisticsAdapter.notifyDataSetChanged();
 				}
 			}
 		};
 		
 		private void loadData(final int cid) {
 			new Thread() {
 				public void run() {
 					String ret = mStatistics.loadStatisticsResultJson(mStartSet, mEndSet, Statistics.CATEGORY_DETAIL, cid);
 					Message msg = new Message();
 					if (ret != null && !"".equals(ret)) {
 						Log.d(TAG, ret);
 						msg.what = 0;
 					} else {
 						msg.what = -1;
 					}
 					msg.obj = ret;
 					handler.sendMessage(msg);
 				}
 			}.start();
 		}
 		
 		public void toggleDetail(int index) {
 			expanded[index] = !expanded[index];
 			if (expanded[index]) {
 				Log.d(TAG, "show");
 				showDetails(index);
 			} 
 			mStatisticsAdapter.notifyDataSetChanged();
 		}
 	}
 	
 	protected class DetailViewHolder {
 		TextView dishName;
 		TextView salesCount;
 		TextView totalAmount;
 		TextView percentage;
 	}
 
 }
