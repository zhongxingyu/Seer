 package app.xzone.storyline.helper;
 
 import java.text.ParseException;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import app.xzone.storyline.HomeActivity;
 import app.xzone.storyline.R;
 import app.xzone.storyline.adapter.DBAdapter;
 import app.xzone.storyline.model.Story;
 import app.xzone.storyline.util.StringManipulation;
 import app.xzone.storyline.util.TimeUtil;
 
 public class Helper {
 
 	public static final int REQUEST_CODE_IMAGE_CAMERA = 0;
 	public static final int REQUEST_CODE_IMAGE_GALLERY = 1;
 	public static final int REQUEST_CODE_PICK_LOCATION = 2;
 
 	public static final int MODE_NORMAL = 0;
 	public static final int MODE_EDIT = 1;
 
 	public static int OFFSET_VIEWGROUP = 3;
 
 	public static Story singletonStory(Story story) {
 		if (story == null) {
 			story = new Story();
 		}
 
 		return story;
 	}
 
 	public static int getBubbleIndex(int index, ViewGroup viewGroup) {
 		if(viewGroup == null) return 0; 
 		return index + viewGroup.getChildCount() - Helper.OFFSET_VIEWGROUP;
 	}
 
 	public static void buildUIMain(Activity activity, Story story) {
 
 		TextView tv = (TextView) activity.findViewById(R.id.titleStory);
 		tv.setTag(story);
 
 		if (story == null)
 			tv.setText("");
 		else
 			tv.setText(StringManipulation.ellipsis(story.getName()
 					.toUpperCase(), 100));
 
 		tv = (TextView) activity.findViewById(R.id.infoStart);
 		if (story == null)
 			tv.setText("");
 		else
 			tv.setText(TimeUtil.dateFormat(
 					TimeUtil.fromEpochFormat(story.getStartDate())).toString());
 
 		tv = (TextView) activity.findViewById(R.id.infoEnd);
 		if (story == null)
 			tv.setText("");
 		else
 			tv.setText(TimeUtil.dateFormat(
 					TimeUtil.fromEpochFormat(story.getEndDate())).toString());
 
 	}
 
 	public static Story buildFromDateTimeStory(Story story, Dialog dialog)
 			throws ParseException {
 
 		story = singletonStory(story);
 
 		EditText t01 = (EditText) dialog.findViewById(R.id.valueNameStory);
 		EditText t02 = (EditText) dialog
 				.findViewById(R.id.valueDescriptionStory);
 
 		TextView date01 = (TextView) dialog.findViewById(R.id.valueStartDate);
 		TextView time01 = (TextView) dialog.findViewById(R.id.valueStartTime);
 		TextView date02 = (TextView) dialog.findViewById(R.id.valueEndDate);
 		TextView time02 = (TextView) dialog.findViewById(R.id.valueEndTime);
 
 		if (t01.getText().toString().equals("")
 				|| t02.getText().toString().equals("")
 				|| date01.getText().toString().equals("")
 				|| time01.getText().toString().equals("")
 				|| date02.getText().toString().equals("")
 				|| time02.getText().toString().equals(""))
 			return null;
 
 		int hour01 = Integer
 				.parseInt(time01.getText().toString().split(":")[0]);
 		int minute01 = Integer
 				.parseInt(time01.getText().toString().split(":")[1].split(" ")[0]);
 
 		int hour02 = Integer
 				.parseInt(time02.getText().toString().split(":")[0]);
 		int minute02 = Integer
 				.parseInt(time02.getText().toString().split(":")[1].split(" ")[0]);
 
 		story.setName(t01.getText().toString());
 		story.setDescription(t02.getText().toString());
 		story.setStartDate(TimeUtil.toEpochFormat(date01.getText().toString(),
 				hour01, minute01));
 		story.setEndDate(TimeUtil.toEpochFormat(date02.getText().toString(),
 				hour02, minute02));
 
 		return story;
 	}
 
 	// Helper for handle date time picker
 	public static void showDatePicker(final Context context,
 			final View resourceTarget) {
 		DateTime dt = new DateTime();
 		DatePickerDialog dp = null;
 
 		dp = new DatePickerDialog(context, new OnDateSetListener() {
 
 			@Override
 			public void onDateSet(DatePicker view, int year, int monthOfYear,
 					int dayOfMonth) {
 				DateTimeFormatter fmt = DateTimeFormat
 						.forPattern("MMM dd, yyyy");
 
 				TextView dateText = (TextView) resourceTarget;
 				dateText.setText((new DateTime(year, monthOfYear + 1,
 						dayOfMonth, 0, 0, 0, 0)).toString(fmt));
 
 			}
 		}, dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
 		dp.show();
 
 	}
 
 	public static void showTimePicker(final Context context,
 			final View resourceTarget) {
 		DateTime dt = new DateTime();
 		TimePickerDialog tp = null;
 
 		tp = new TimePickerDialog(context, new OnTimeSetListener() {
 
 			@Override
 			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 				TextView timeText = (TextView) resourceTarget;
 				timeText.setText(hourOfDay + ":" + minute
 						+ TimeUtil.timeArea(hourOfDay));
 
 			}
 		}, dt.getHourOfDay(), dt.getMinuteOfHour(), true);
 		tp.show();
 	}
 
 	// Handle Mode screen (normal, edit)
 	public static void modeNormal(final Activity ac, ViewGroup viewGroup) {
 		TextView mode = (TextView) ac.findViewById(R.id.mode);
 		mode.setTag(MODE_NORMAL);
 
 		
 		ImageButton sb = (ImageButton) ac.findViewById(R.id.storyButton);
 		sb.setImageDrawable(ac.getResources().getDrawable(
 				R.drawable.paper_plane));
 		sb.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				((HomeActivity) ac).showSecondaryMenu();
 			}
 		});
 
 		View v = (View) ac.findViewById(R.id.footer);
 		v.setVisibility(View.GONE);
 		if (viewGroup != null) {
 			v = (View) ac.findViewById(R.id.bubbleEvent);
 			v.setClickable(false);
 			
 			Button deleteEvent = (Button) ac.findViewById(R.id.delete_event);
 			deleteEvent.setVisibility(View.VISIBLE);
 		}
 		v = ac.findViewById(R.id.addDateStoryButton);
 		v.setVisibility(View.GONE);
 		v = ac.findViewById(R.id.addDateStoryButton02);
 		v.setVisibility(View.GONE);
 		v = ac.findViewById(R.id.addEventButton);
 		v.setVisibility(View.GONE);
 
 		// hide pointer add new event button
 		if (viewGroup != null) {
 			View bubble = ac.findViewById(R.id.body_content);
 			bubble.setVisibility(View.GONE);
 		}
 
 	}
 
 	public static void modeEdit(final Activity ac, ViewGroup viewGroup) {
 		TextView mode = (TextView) ac.findViewById(R.id.mode);
 		mode.setTag(MODE_EDIT);
 		
 		ImageButton sb = (ImageButton) ac.findViewById(R.id.storyButton);
 		sb.setImageDrawable(ac.getResources().getDrawable(R.drawable.trash));
 		sb.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				AlertDialog.Builder builder = new AlertDialog.Builder(ac);
 				builder.setMessage("Are you sure remove this Story?");
 				builder.setCancelable(true);
 				builder.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								TextView tv = (TextView) ac
 										.findViewById(R.id.titleStory);
 								Story story = (Story) tv.getTag();
 
 								if (story != null) {
 
 									DBAdapter db = new DBAdapter(ac
 											.getApplicationContext());
 									db.deleteStoryRecord(story.getId());
 
 									ac.finish();
 									ac.startActivity(ac.getIntent());
 								}
 
 								dialog.cancel();
 
 							}
 						});
 				builder.setNegativeButton("No",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								dialog.cancel();
 							}
 						});
 
 				builder.show();
 			}
 		});
 
 		View v = (View) ac.findViewById(R.id.footer);
 		v.setVisibility(View.VISIBLE);
 		// v = (View) ac.findViewById(R.id.bubbleEvent);
 		// v.setClickable(true);
 		v = ac.findViewById(R.id.addDateStoryButton);
 		v.setVisibility(View.VISIBLE);
 		v = ac.findViewById(R.id.addDateStoryButton02);
 		v.setVisibility(View.VISIBLE);
 		v = ac.findViewById(R.id.addEventButton);
 		v.setVisibility(View.VISIBLE);
 		
 		if(viewGroup != null){
 			Button deleteEvent = (Button) ac.findViewById(R.id.delete_event);
 			deleteEvent.setVisibility(View.INVISIBLE);
 		}
 
 		// show pointer add new event button
 		View bubble = ac.findViewById(R.id.body_content);
 		viewGroup = (ViewGroup) bubble.getParent();
 		bubble.setVisibility(View.VISIBLE);
 
 	}
 
 	public View getBubbleEvent(ViewGroup viewGroup, int position) {
 		int pos = position + OFFSET_VIEWGROUP;
 
 		return viewGroup.getChildAt(position);
 	}
 
 	public static int getCurrentMode(Activity a) {
 		TextView mode = (TextView) a.findViewById(R.id.mode);
 
 		return (Integer) mode.getTag();
 	}
 
 }
