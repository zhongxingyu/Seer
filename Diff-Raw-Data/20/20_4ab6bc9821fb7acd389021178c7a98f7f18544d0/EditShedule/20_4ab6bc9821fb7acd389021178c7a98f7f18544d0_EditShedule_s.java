 package com.mh.activity;
 
 import com.mh.R;
 import com.mh.util.MyDBHelper;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.TimePickerDialog;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class EditShedule extends Activity implements OnClickListener {
 	private static final String TAG = "EditClass";
 	private Button saveButton;
 	private Button discardButton;
 	private EditText subjectET;
 	private EditText placeET;
 	private EditText teacherET;
 	private EditText timeET;
 	private EditText infoET;
 	private TextView weekNameTV;
 	private TextView sequenceTV;
 	private boolean editFlag;
 
 	MyDBHelper dbHelper;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.v(TAG, "onCreate()");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.eidt_class);
 		dbHelper = new MyDBHelper(this);
 		editFlag = false; // 编辑标志，默认为假，说明是新建记录，为真，说明是编辑记录
 		initView();
 
 	}
 
 	private void initView() {
 		String weekName = getIntent().getExtras().getString("weekName");
 		int position = getIntent().getExtras().getInt("position");
 		String positionString = "";
 		switch (position) {
 		case 0:
 			positionString = "第一节";
 			break;
 		case 1:
 			positionString = "第二节";
 			break;
 		case 2:
 			positionString = "第三节";
 			break;
 		case 3:
 			positionString = "第四节";
 			break;
 		case 4:
 			positionString = "第五节";
 			break;
 		case 5:
 			positionString = "第六节";
 			break;
 		case 6:
 			positionString = "第七节";
 			break;
 		case 7:
 			positionString = "第八节";
 			break;
 		case 8:
 			positionString = "第九节";
 			break;
 		case 9:
 			positionString = "第十节";
 			break;
 		}
 		weekNameTV = (TextView) findViewById(R.id.weekNameTV);
 		weekNameTV.setText(weekName);
 		sequenceTV = (TextView) findViewById(R.id.sequenceTV);
 		sequenceTV.setText(positionString);
 
 		saveButton = (Button) findViewById(R.id.saveButton);
 		saveButton.setOnClickListener(this);
 		discardButton = (Button) findViewById(R.id.discardButton);
 		discardButton.setOnClickListener(this);
 
 		subjectET = (EditText) findViewById(R.id.subjectET);
 		placeET = (EditText) findViewById(R.id.placeET);
 		teacherET = (EditText) findViewById(R.id.teacherET);
 		timeET = (EditText) findViewById(R.id.timeET);
 		timeET.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				timeET.setInputType(InputType.TYPE_NULL); // 关闭软键盘
 
 				return false;
 			}
 		});
 		timeET.setOnClickListener(this);
 		infoET = (EditText) findViewById(R.id.infoET);
 
 		Cursor cursor = dbHelper.open().query(weekName, positionString);
 		dbHelper.close();
 		if (cursor != null && cursor.getCount() > 0) {
 			String subject = cursor.getString(3);
 			String place = cursor.getString(4);
 			String teacher = cursor.getString(5);
 			String time = cursor.getString(6);
 			String info = cursor.getString(7);
 			subjectET.setText(subject);
 			placeET.setText(place);
 			teacherET.setText(teacher);
 			timeET.setText(time);
 			infoET.setText(info);
 
 			editFlag = true;
 		}
 	}
 
 	private void showTimePickerDialog() {
 		TimePickerDialog dlg = new TimePickerDialog(this, timeSetListener, 00,
 				00, true);
 		dlg.setTitle("请输入时间");
 		dlg.show();
 	}
 
 	@Override
 	public void onClick(View v) {
		if (v == timeET) {
 			showTimePickerDialog();
		}
		if (v == saveButton) {
 			String weekName = weekNameTV.getText().toString();
 			String sequence = sequenceTV.getText().toString();
 			String subject = subjectET.getText().toString();
 			String place = placeET.getText().toString();
 			String teacher = teacherET.getText().toString();
 			String time = timeET.getText().toString();
 			String info = infoET.getText().toString();
 			if ("".equals(subject) || subject == null) {
 				new AlertDialog.Builder(this).setTitle("提示")
 						.setMessage("课程名不能为空").create().show();
 				return;
 			}
 
 			if (editFlag) {
 				boolean result = dbHelper.open().update(weekName, sequence,
 						subject, place, teacher, time, info);
 				dbHelper.close();
 				if (result) {
 					Toast.makeText(this, "保存成功", Toast.LENGTH_LONG).show();
 					this.finish();
 				}
 			} else {
 				long result = dbHelper.open().insert(weekName, sequence,
 						subject, place, teacher, time, info);
 				dbHelper.close();
 				if (result > 0) {
 					Toast.makeText(this, "创建成功", Toast.LENGTH_LONG).show();
 					this.finish();
 				}
 			}
 
		}
		if (v == discardButton) {
 			this.finish();
 		}
 	}
 
 	private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
 
 		@Override
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			String timeStr = "";
 			String minuteStr = "";
 			String hourStr = "";
 			if (hourOfDay < 10) {
 				hourStr = "0" + hourOfDay;
 			} else {
 				hourStr = "" + hourOfDay;
 			}
 			if (minute < 10) {
 				minuteStr = "0" + minute;
 			} else {
 				minuteStr = "" + minute;
 			}
 			timeStr = hourStr + ":" + minuteStr;
 			timeET.setText(timeStr);
 		}
 	};
 }
