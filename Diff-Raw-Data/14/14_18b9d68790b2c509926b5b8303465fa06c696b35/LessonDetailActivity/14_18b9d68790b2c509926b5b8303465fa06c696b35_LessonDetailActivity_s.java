 package com.ahutlesson.android;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.ahutlesson.android.model.Lesson;
 import com.ahutlesson.android.model.LessonManager;
 import com.ahutlesson.android.model.Timetable;
 import com.umeng.analytics.MobclickAgent;
 
 public class LessonDetailActivity extends BaseActivity {
 	
 	private int week, time;
 	private Lesson lesson;
 	private TextView tvLessonName, tvLessonPlace, tvTeacherName, tvLessonWeek,
 			tvHomework, tvLessonTime, tvCurrentTime;
 	private Button btnEditHomework, btnDeleteHomework;
 	
 	private LessonManager lessonManager;
 	private Timetable timetable;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		week = getIntent().getExtras().getInt("week");
 		time = getIntent().getExtras().getInt("time");
 
 		if(!Timetable.isValidWeekTime(week, time)) {
 			this.finish();
 			return;
 		}
         
 		setContentView(R.layout.lessondetail);
 
 		tvLessonName = (TextView) findViewById(R.id.tvLessonName);
 		tvLessonPlace = (TextView) findViewById(R.id.tvLessonPlace);
 		tvTeacherName = (TextView) findViewById(R.id.tvTeacherName);
 		tvLessonWeek = (TextView) findViewById(R.id.tvLessonWeek);
 		tvHomework = (TextView) findViewById(R.id.tvHomework);
 		tvLessonTime = (TextView) findViewById(R.id.tvLessonTime);
 
 		tvCurrentTime = (TextView) findViewById(R.id.tvCurrentTime);
 
 		btnEditHomework = (Button) findViewById(R.id.btnEditHomework);
 		btnDeleteHomework = (Button) findViewById(R.id.btnDeleteHomework);
 
 		btnEditHomework.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				editHomework();
 			}
 		});
 		btnDeleteHomework.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				deleteHomework();
 			}
 		});
 
 	}
 
 	@Override
 	protected void onResume() {
 		// ɾ޸ĺ
 		super.onResume();
 		
 		lessonManager = LessonManager.getInstance(this);
 		timetable = Timetable.getInstance(this);
 
 		tvCurrentTime.setText(timetable.weekname[week]
 				+ timetable.lessontime_name[time]);
 		tvLessonTime.setText(timetable.begintime[time] + " ~ "
 				+ timetable.endtime[time]);
 		
 		
 		lesson = lessonManager.getLessonAt(week, time);
 		if (lesson != null) {
 			tvLessonName.setText(lesson.name);
 			tvLessonPlace.setText(lesson.place);
 			tvLessonWeek.setText(lesson.getDuration());
 			tvTeacherName.setText(lesson.teacher);
 			if (lesson.homework != null && !lesson.homework.contentEquals("")) {
 				tvHomework.setText(lesson.homework);
 			}
 			btnEditHomework.setVisibility(View.VISIBLE);
 			btnDeleteHomework.setVisibility(View.VISIBLE);
 		} else {
 			btnEditHomework.setVisibility(View.GONE);
 			btnDeleteHomework.setVisibility(View.GONE);
 		}
 
 		MobclickAgent.onResume(this);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		MobclickAgent.onPause(this);
 	}
 
 	protected void editHomework() {
 		// ҵ
 		if (lesson == null)
 			return;
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("༭ҵ");
 		alert.setMessage("뱾γ̵ҵ");
 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 		if (lesson.hasHomework && lesson.homework != null
 				&& !lesson.homework.contentEquals("")) {
 			input.setText(lesson.homework);
 		}
 		alert.setPositiveButton("ȷ", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString();
 				if (!value.contentEquals("")) {
 					lesson.homework = value;
 					lesson.hasHomework = true;
 					lessonManager.editHomework(week, time, lesson.homework);
 					tvHomework.setText(lesson.homework);
 				}
 			}
 		});
 
 		alert.setNegativeButton("ȡ", null);
 		alert.show();
 	}
 
 	protected void deleteHomework() {
 		// ҵ
 		if (lesson == null)
 			return;
 		if (!lesson.hasHomework)
 			return;
 		new AlertDialog.Builder(LessonDetailActivity.this).setTitle("ҵ")
 				.setMessage("ȷձγҵ")
 				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						lesson.homework = null;
 						lesson.hasHomework = false;
 						lessonManager.deleteHomework(week, time);
 						tvHomework.setText("");
 					}
 
 				}).setNegativeButton(R.string.cancel, null).show();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 0, Menu.NONE, R.string.edit)
 			.setIcon(R.drawable.edit)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		menu.add(0, 1, Menu.NONE, R.string.delete)
 			.setIcon(R.drawable.delete)
 			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		return true;
 	}
 
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case 0:
 			Intent i = new Intent(this, EditLessonActivity.class);
 			i.putExtra("week", week);
 			i.putExtra("time", time);
 			startActivity(i);
 			return true;
 		case 1:
			if (lesson == null)
				break;
 			new AlertDialog.Builder(LessonDetailActivity.this)
 					.setTitle("ɾγ")
 					.setMessage("ȷɾγ̣")
 					.setPositiveButton(R.string.ok,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int which) {
 									if (lesson != null) {
 										LessonManager.getInstance(
 												LessonDetailActivity.this)
 												.deleteLesson(lesson);
 									}
 									LessonDetailActivity.this.finish();
 								}
 
 							}).setNegativeButton(R.string.cancel, null).show();
 			return true;
 		default:
 			return super.onMenuItemSelected(featureId, item);
 		}
		return false;
 	}
 	
 	
 }
