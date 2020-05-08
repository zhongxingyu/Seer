 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.Reservation;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.ui.base.BaseActivity;
 
 public class ReservationInfoActivity extends BaseActivity {
 
 	public final static String TAG = "ReservationInfoActivity";
 	private Button backBtn;
 	private Button submitBtn;
 	private EditText nameEt;
 	private EditText telEt;
 	private EditText depositEt;
 	private EditText personsEt;
 	private EditText addrEt;
 	private TextView datetimeTv;
 	private TextView tablesTv;
 	private EditText commentEt;
 	
 	private int mYear;
 	private int mMonth;
 	private int mDay;
 	private int mHour;
 	private int mMinute;
 
 	protected TableSetting mSettings;
 	private Reservation reservation;
 	private MyOrder myOrder;
 
 	protected boolean[] mTableSelected;
 	protected List<Integer> multiOrderIds;
 	protected StringBuffer multiOrderNames;
 	protected boolean datetimeSet = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.reservation_info_activity);
 		findViews();
 		setClickLisener();
 		
 		mSettings = new TableSetting(ReservationInfoActivity.this);
 		reservation = new Reservation();
 		myOrder = new MyOrder(ReservationInfoActivity.this);
 		final Calendar c = Calendar.getInstance();
 	    mYear = c.get(Calendar.YEAR);
 	    mMonth = c.get(Calendar.MONTH);
 	    mDay = c.get(Calendar.DAY_OF_MONTH);
 	    mHour = c.get(Calendar.HOUR_OF_DAY);
 	    mMinute = c.get(Calendar.MINUTE);
 	}
 
 	private void findViews() {
 		backBtn = (Button) findViewById(R.id.back);
 		submitBtn = (Button) findViewById(R.id.submit);
 		nameEt = (EditText) findViewById(R.id.name);
 		telEt = (EditText) findViewById(R.id.tel);
 		depositEt = (EditText) findViewById(R.id.deposit);
 		personsEt = (EditText) findViewById(R.id.persons);
 		addrEt = (EditText) findViewById(R.id.addr);
 		datetimeTv = (TextView) findViewById(R.id.datetime);
 		tablesTv = (TextView) findViewById(R.id.tables);
 		commentEt = (EditText) findViewById(R.id.comment);
 	}
 	
 	private void setClickLisener() {
 		backBtn.setOnClickListener(backBtnClicked);
 		submitBtn.setOnClickListener(submitBtnClicked);
 		datetimeTv.setOnClickListener(datetimeClicked);
 		tablesTv.setOnClickListener(tableSelectClicked);
 	}
 	
 	private boolean isSet(String v) {
 		return !("".equals(v) || v == null);
 	}
 	
 	private boolean validateForm() {
 		boolean ret = true;
 		if (!isSet(nameEt.getText().toString())) {
 			nameEt.setBackgroundColor(0xA0FF0000);
 			ret = false;
 		} else {
 			reservation.setName(nameEt.getText().toString().trim());
 			nameEt.setBackgroundColor(0x00FFFFFF);
 		}
 		
 		if (!isSet(telEt.getText().toString())) {
 			telEt.setBackgroundColor(0xA0FF0000);
 			ret = false;
 		} else {
 			reservation.setTel(telEt.getText().toString().trim());
 			telEt.setBackgroundColor(0x00FFFFFF);
 		}
 		
 		if (!datetimeSet) {
 			datetimeTv.setTextColor(0xFFFF0000);
 			ret = false;
 		} else {
 			reservation.setDatetime(getDatetime());
 			datetimeTv.setTextColor(0xFF4D2412);
 		}
 		
 		reservation.setAddr(addrEt.getText().toString().trim());
 		reservation.setComment(commentEt.getText().toString().trim());
 		String tmp = depositEt.getText().toString();
 		if (!"".equals(tmp)) {
 			reservation.setDeposit(Integer.parseInt(tmp));
 		}
 		tmp = personsEt.getText().toString().trim();
 		if (!"".equals(tmp)) {
 			reservation.setPersons(Integer.parseInt(tmp));
 		}
 		if (multiOrderNames != null) {
 			reservation.setTableNames(multiOrderNames.toString());
 		}
 		
		if (multiOrderIds == null) {
			ret = false;
			tablesTv.setTextColor(0xFFFF0000);
 		} else {
			tablesTv.setTextColor(0xFF000000);
			StringBuffer ids = new StringBuffer();
			for (Integer item:multiOrderIds) {
				ids.append(item.toString() + ",");
			}
			if (multiOrderIds.size() > 0) {
				reservation.setTableIds(ids.toString().substring(0, ids.length()-1));
			} else {
				reservation.setTableIds(null);
			}
 		}
 		return ret;
 	}
 	
 	private String getDatetime() {
 		return Integer.toString(mYear) + "-" + 
 				to2BitString(mMonth) + "-" +
 				to2BitString(mDay) + " " +
 				to2BitString(mHour) + ":" + 
 				to2BitString(mMinute) + ":" + "00";
 	}
 	
 	private String to2BitString(int v) {
 		if (v < 10) {
 			return "0" + Integer.toString(v);
 		} else {
 			return Integer.toString(v);
 		}
 	}
 	protected OnClickListener backBtnClicked = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(ReservationInfoActivity.this, MyOrderActivity.class);
 			startActivity(intent);
 			finish();		
 		}
 	};
 	
 	protected OnClickListener submitBtnClicked = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			if (validateForm()) {
 				showProgressDlg("正在提交预订信息...");
 				new Thread() {
 					public void run() {
 						int ret = myOrder.submitReservation(reservation);
 						Log.d(TAG, "ret:"+ret);
 						handler.sendEmptyMessage(ret);
 					}
 				}.start();
 				
 			} else {
 				Toast.makeText(ReservationInfoActivity.this, "信息不完整", Toast.LENGTH_LONG).show();
 			}
 		}
 	};
 	
 	private DatePickerDialog.OnDateSetListener mDateSetListener =
 		new DatePickerDialog.OnDateSetListener() {
 	
 	    public void onDateSet(DatePicker view, int year, 
 	                          int monthOfYear, int dayOfMonth) {
 	        mYear = year;
 	        mMonth = monthOfYear + 1;
 	        mDay = dayOfMonth;
 	        TimePickerDialog dlg = new TimePickerDialog(ReservationInfoActivity.this,
                     mTimeSetListener, mHour, mMinute, false);
 	        dlg.show();
 	    }
 	};
 
     private TimePickerDialog.OnTimeSetListener mTimeSetListener =
         new TimePickerDialog.OnTimeSetListener() {
             public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                 mHour = hourOfDay;
                 mMinute = minute;
                 updateDatetime();
             }
     };
     
 	protected OnClickListener datetimeClicked = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			DatePickerDialog datePikerDlg = new DatePickerDialog(ReservationInfoActivity.this,
                     mDateSetListener, mYear, mMonth, mDay);
 			datePikerDlg.show();
 		}
 	};
 	
 	protected OnClickListener tableSelectClicked = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			showTableSelectDialog();
 		}
 	};
 	
 	private Handler handler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if(msg.what < 0) {
 				Toast.makeText(ReservationInfoActivity.this, "提交预订失败！", Toast.LENGTH_LONG).show();
 			} else {
 				myOrder.clear();
 				finish();
 			}
 			super.handleMessage(msg);
 		}
 		
 	};
 	
 	private void updateDatetime() {
 		datetimeSet = true;
 		datetimeTv.setText(getDatetime());
 		datetimeTv.setTextSize(22);
 	}
 	
 	protected void showTableSelectDialog() {
         List<String> tableNames = mSettings.getTableNames();
         if (mTableSelected == null) {
         	mTableSelected = new boolean[tableNames.size()];
         	int tableNum = mSettings.tableSeetingsSize();
             for (int i=0; i<tableNum; i++) {
             	mTableSelected[i] = false;
             }
         }
         
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("请选择");
             DialogInterface.OnMultiChoiceClickListener mutiListener = 
                     new DialogInterface.OnMultiChoiceClickListener() {
                                 
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, 
                                                 int which, boolean isChecked) {
                                 	mTableSelected[which] = isChecked;
                                 }
                         };
             builder.setMultiChoiceItems((CharSequence[])tableNames.toArray(new String[0]), 
             		mTableSelected, mutiListener);
             DialogInterface.OnClickListener btnListener = 
                     new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int which) {
                             		boolean flag = true;
                             		if (multiOrderIds == null) {
                             			multiOrderIds = new ArrayList<Integer>();
                             		} else {
                             			multiOrderIds.clear();
                             		}
                             		if (multiOrderNames == null) {
                             			multiOrderNames = new StringBuffer();
                             		} else {
                             			multiOrderNames.setLength(0);
                             		}
                                     for(int i=0; i<mTableSelected.length; i++) {
                                         if(mTableSelected[i] == true) {
                                         	flag = false;
                                             multiOrderIds.add(mSettings.getIdByAllIndex(i));
                                             multiOrderNames.append(mSettings.getNameByAllIndex(i) + ",");
                                         }
                                     }
                                     if(multiOrderIds.size() > 0) {
                                     	multiOrderNames.deleteCharAt(multiOrderNames.length() - 1);
                                     }
                                     updateTablesInfo(flag);
                                 }
                         };
             builder.setPositiveButton("确定", btnListener);
             builder.setNegativeButton("取消", null);
             builder.create().show();
 	}
 	
 	private void updateTablesInfo(boolean noTableSelected) {
 		if (noTableSelected) {
 			Toast.makeText(ReservationInfoActivity.this, "没有选中任何桌号", Toast.LENGTH_LONG).show();
 			tablesTv.setText("点击设置");
 		} else {
 			tablesTv.setText(multiOrderNames);
 		}
 		
 	}
 }
