 package com.htb.cnk.lib;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 import com.htb.cnk.R;
 import com.htb.cnk.adapter.StatisticsAdapter;
 import com.htb.cnk.data.CnkDbHelper;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.Statistics;
 import com.htb.cnk.ui.base.BaseActivity;
 import com.htb.constant.ErrorNum;
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
 	protected ProgressDialog mpDialog;
 	protected Calendar mStart = Calendar.getInstance();
 	protected Calendar mEnd = Calendar.getInstance();
 	protected Calendar mStartSet = Calendar.getInstance();
 	protected Calendar mEndSet = Calendar.getInstance();
 	protected String mLatestStatistics;
 	protected int mQueryMode;
 	protected Statistics mStatistics;
 	protected StatisticsAdapter mStatisticsAdapter;
 
 	private OnClickListener backClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			StatisticsBaseActivity.this.finish();
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.statistics_activity);
 		mStatistics = new Statistics(StatisticsBaseActivity.this);
 		findViews();
 		setClickListener();
 		downloadDB();
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
 
 	private void downloadDB() {
 		mpDialog = new ProgressDialog(StatisticsBaseActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		showProgressDlg("正在加载销售数据...");
 		new Thread() {
 			public void run() {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
				int ret = mStatistics.downloadDB(Server.SERVER_DB_SALES, CnkDbHelper.SALES_DATA);
 				if (ret < 0) {
 					handler.sendEmptyMessage(ret);
 					Log.e(TAG, "Download sales db failed:" + ret);
 					return;
 				}
 
 				String respond = Http.get(Server.LATEST_STATISTICS, "do=get");
 				if (respond == null) {
 					handler.sendEmptyMessage(ErrorNum.GET_LATEST_STATISTICS_FAILED);
 					Log.e(TAG, "get latest statistics time failed");
 					return;
 				}
 				int start = respond.indexOf("[") + 1;
 				if (start != 1) {
 					handler.sendEmptyMessage(ErrorNum.GET_LATEST_STATISTICS_FAILED);
 					return;
 				}
 				int end = respond.indexOf("]");
 				mLatestStatistics = respond.substring(start, end);
 				handler.sendEmptyMessage(0);
 			}
 		}.start();
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
 
 	private void DateTimeNotSetAlert(String err) {
 		popUpDlg("请注意", err, false);
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
 		mStatisticsAdapter.notifyDataSetChanged();
 	}
 
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
 
 	public void showProgressDlg(String msg) {
 		mpDialog.setMessage(msg);
 		mpDialog.show();
 	}
 
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
 	
 	protected Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				switch (msg.what) {
 				case ErrorNum.DOWNLOAD_DB_FAILED:
 					popUpDlg("错误", "下载销售数据失败,错误码:" + msg.what, true);
 					break;
 				case ErrorNum.GET_LATEST_STATISTICS_FAILED:
 					popUpDlg("错误", "获取上次统计时间失败！", true);
 					break;
 				default:
 					popUpDlg("错误", "下载销售数据失败,错误码:" + msg.what, true);
 					Log.e(TAG, "unknow error num");
 				}
 			}
 		}
 	};
 
 	protected void popUpDlg(String title, String msg,
 			final boolean finishActivity) {
 		new AlertDialog.Builder(StatisticsBaseActivity.this).setTitle(title)
 				.setMessage(msg)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (finishActivity) {
 							finish();
 						}
 					}
 				}).show();
 	}
 
 }
