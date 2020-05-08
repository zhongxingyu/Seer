 package app.xzone.storyline;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import app.xzone.storyline.adapter.DBAdapter;
 import app.xzone.storyline.component.Sliding;
 import app.xzone.storyline.helper.AdapterHelper;
 import app.xzone.storyline.helper.EventHelper;
 import app.xzone.storyline.helper.Helper;
 import app.xzone.storyline.helper.ImageAdapter;
 import app.xzone.storyline.model.Event;
 import app.xzone.storyline.model.Story;
 import app.xzone.storyline.util.StringManipulation;
 import app.xzone.storyline.util.TimeUtil;
 
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
 
 public class HomeActivity extends SlidingActivity implements OnClickListener {
 
 	private ImageButton listButton;
 	private ImageButton storyButton;
 	private LinearLayout newStoryButton;
 	private ViewGroup viewGroup;
 
 	private int noBubble = 0; // index last saved bubble
 	private int countBubble = 0; // count of bubble ready to save
 
 	int key1 = 0;
 	int key2 = 0;
 	private Sliding popup;
 	private Button submitEventButton;
 	private Button cancelEventButton;
 	private LinearLayout pickDateEvent;
 	private LinearLayout pickTimeEvent;
 
 	private Story story;
 	private Event event;
 	private ArrayList<Event> events = new ArrayList<Event>();
 	private ArrayList<Event> prevEvents = new ArrayList<Event>();
 
 	final Context context = this;
 
 	// accessing model storage
 	DBAdapter db = null;
 	
 
 	/** Called when the activity is first created. Testing */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
 		// getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
 
 		setContentView(R.layout.main);
 		setBehindContentView(R.layout.sliding_menu);
 		getSlidingMenu().setSecondaryMenu(R.layout.sliding_recomendation);
 
 		getSlidingMenu().setBehindOffset(80);
 
 		popup = (Sliding) findViewById(R.id.sliding1);
 		popup.setVisibility(View.GONE);
 
 		listButton = (ImageButton) findViewById(R.id.menuListButton);
 		storyButton = (ImageButton) findViewById(R.id.storyButton);
 		newStoryButton = (LinearLayout) findViewById(R.id.newStorySliding);
 		submitEventButton = (Button) findViewById(R.id.submitEventButton);
 		cancelEventButton = (Button) findViewById(R.id.cancelEventButton);
 		pickDateEvent = (LinearLayout) findViewById(R.id.pickDateEvent);
 		pickTimeEvent = (LinearLayout) findViewById(R.id.pickTimeEvent);
 		
 
 		listButton.setOnClickListener(this);
 		storyButton.setOnClickListener(this);
 		newStoryButton.setOnClickListener(this);
 		submitEventButton.setOnClickListener(this);
 		cancelEventButton.setOnClickListener(this);
 		pickDateEvent.setOnClickListener(this);
 		pickTimeEvent.setOnClickListener(this);
 
 		
 		db = new DBAdapter(context);
 
 		// read parameters passing to activity
 		getStoryWithBundle(getIntent().getExtras());
 
 		Helper.buildUIMain(HomeActivity.this, story);
 		AdapterHelper.buildListViewAdapter(HomeActivity.this);
 
 		if (story != null && story.getEvents() != null) {
 			renderTimeline(story.getEvents());
 		} else {
 			Helper.modeEdit(this, viewGroup);
 		}
 
 	}
 	
 	
 	
 
 	private void getStoryWithBundle(Bundle bundle) {
 		if (bundle == null) {
 
 			// calling when start application
 			if (story == null) {
 				story = db.getLastStory();
 				
 				// store all event from this story into prevEvent before change
 				if(story != null){	prevEvents = story.getEvents(); }
 												
 			} else {
 				Helper.modeEdit(this, viewGroup);
 			}
 		} else {
 
 			// calling from menu list story
 			story = (Story) bundle.getSerializable("app.story");
 			
 			// store all event from this story
 			if(story != null){ prevEvents = story.getEvents(); }
 		}
 	}
 
 	public void renderTimeline(ArrayList<Event> items) {
 		countBubble = 0;
 		for (int i = 0; i < items.size(); i++) {
 			viewGroup = AdapterHelper.buildBubbleEventAdapter(this,
 					items.get(i));
 			countBubble++;
 		}
 		Helper.modeNormal(this, viewGroup);
 
 		
 		// reset viewGroup into null value
 		noBubble = Helper.getBubbleIndex(0, viewGroup);
 
 		// default -> add list of events contains from story.getEvents()
 		events = EventHelper.getSingletonArray(events);
 		events.addAll(story.getEvents());
 		 
 
 		View bubble = findViewById(R.id.body_content);
 		bubble.setVisibility(View.GONE);
 	}
 	
 	public void renderEmptyTimeline(){
 		countBubble = 0;
 		Helper.modeNormal(this, viewGroup);
 		
 		View bubble = findViewById(R.id.body_content);
 		bubble.setVisibility(View.GONE);
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuItem item01 = menu.add("Map").setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						Intent intent = new Intent(HomeActivity.this,
 								MapPlaceActivity.class);
 						startActivity(intent);
 
 						return true;
 					}
 				});
 
 		MenuItem item02 = menu.add("Edit").setOnMenuItemClickListener(
 				new OnMenuItemClickListener() {
 
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						Helper.modeEdit(HomeActivity.this, viewGroup);
 
 						return true;
 					}
 				});
 		item01.setIcon(R.drawable.map);
 		item02.setIcon(R.drawable.list);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.menuListButton:
 			// put your code here
 			toggle();
 			break;
 
 		case R.id.storyButton:
 			showSecondaryMenu();
 			break;
 
 		case R.id.newStorySliding:
			ProgressDialog.show(HomeActivity.this, null, "Loading");
			
 			// routes to main activity
 			finish();
 			Intent intent = getIntent();
 			intent.putExtra("app.story", (Story) null);
 			startActivity(intent);
 
 			break;
 
 		case R.id.pickDateEvent:
 			Helper.showDatePicker(context, findViewById(R.id.valueDateEvent));
 			break;
 
 		case R.id.pickTimeEvent:
 			Helper.showTimePicker(context, findViewById(R.id.valueTimeEvent));
 			break;
 
 		case R.id.submitEventButton:
 			key1 = 0;
 			popup.setVisibility(View.GONE);
 
 			Event e = EventHelper.getEventFromTag(this);
 
 			if (e != null) {
 				// update / modify event data
 				event = EventHelper.buildEvent(this, e, story);
 				viewGroup = AdapterHelper.updateBubbleEvent(this, event);
 
 				// replace with modified event
 				int index = events.indexOf(e);
 				
 				events.set(index, event); 
 
 			} else {
 				// event handler for save event to storage
 				event = EventHelper.buildEvent(this, (e == null) ? new Event() : e, story);
 				viewGroup = AdapterHelper.buildBubbleEventAdapter(this, event);
 				countBubble++;
 				events.add(event);
 			}
 
 			// reset object event
 			event = null;
 			break;
 
 		case R.id.cancelEventButton:
 
 			key1 = 0;
 			popup.setVisibility(View.GONE);
 
 			break;
 		
 		}
 
 	}
 
 	// Pick location from map view
 	public void pickLocation(View v) {
 		Intent intent = new Intent(this, LocationPickerActivity.class);
 		startActivityForResult(intent, Helper.REQUEST_CODE_PICK_LOCATION);
 	}
 
 	// Pick image from camera event
 	public void pickCamera(View v) {
 		ImageAdapter.takePhoto(HomeActivity.this);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case Helper.REQUEST_CODE_IMAGE_CAMERA:
 			if (resultCode == RESULT_OK) {
 
 				Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
 				// preview.setImageBitmap(thumbnail);
 			}
 			break;
 
 		case Helper.REQUEST_CODE_IMAGE_GALLERY:
 			if (resultCode == RESULT_OK && null != data) {
 				Uri selectedImage = data.getData();
 				String[] filePathColumn = { MediaStore.Images.Media.DATA };
 				Cursor cursor = getContentResolver().query(selectedImage,
 						filePathColumn, null, null, null);
 				cursor.moveToFirst();
 				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
 				String picturePath = cursor.getString(columnIndex);
 				cursor.close();
 
 				ImageView image = (ImageView) findViewById(R.id.pic01);
 				image.setImageBitmap(BitmapFactory.decodeFile(picturePath));
 			}
 		case Helper.REQUEST_CODE_PICK_LOCATION:
 			if (resultCode == RESULT_OK && data != null) {
 				if (data.getExtras().containsKey("location")) {
 					String locname = data.getStringExtra("location");
 
 					double[] coordinates = { data.getDoubleExtra("lat", 0d),
 							data.getDoubleExtra("lng", 0d) };
 					TextView tv = (TextView) findViewById(R.id.locationEvent);
 					tv.setText(StringManipulation.ellipsis(locname, 30));
 					tv.setTag(coordinates);
 				}
 			}
 		}
 	}
 
 	// event handler when cancel edit button clicked
 	public void saveEdit(View v) {
 		View title = (View) findViewById(R.id.titleStory);
 		story = (Story) title.getTag();
 
 		// enter code show alert => your story not valid
 		if (story == null)
 			return;
 
 		// save your story
 		if (!story.isExist())
 			story.setId((int) db.insertStoryRecord(story));
 		else
 			db.updateStoryRecord(story);
 
 		
 		// make empty collection for ready to fill
 		if(prevEvents != null) { prevEvents.clear(); }
 		
 		// insert / update event to db
 		for (int i = 0; i < events.size(); i++) {
 			Event e = (Event) events.get(i);
 
 			if (e.getId() > 0) {
 				db.updateEventRecord(e);
 			} else {
 				e.setId((int) db.insertEventRecord(e, story.getId()));
 			}
 			
 			prevEvents.add(e);
 		}
 		
 		Helper.modeNormal(this, viewGroup);
 		AdapterHelper.buildListViewAdapter(HomeActivity.this);
 
 		if (viewGroup != null) {
 			
 			// reset viewGroup into null value
 			noBubble = Helper.getBubbleIndex(0, viewGroup);
 			viewGroup = null;
 		}
 
 	}
 
 	public void cancelEdit(View v) {
 		TextView title = (TextView) findViewById(R.id.titleStory);
 
 		story = (Story) title.getTag();
 
 		// revert into last event data
 		rollbackTimeline(this, prevEvents);
 	}
 	
 	public void showNewEvent(View v) {
 
 		Sliding popup = (Sliding) findViewById(R.id.sliding1);
 		popup.setVisibility(View.VISIBLE);
 		EventHelper.buildUISliding(this, null);
 	}
 
 	// rollback configuration before modified, if cancel
 	public void rollbackTimeline(Activity a, ArrayList<Event> prevs) {
 		if(prevs.size() > 0){
 			viewGroup.removeViews(1, countBubble);
 			renderTimeline(prevEvents);
 		} else {
 			renderEmptyTimeline();
 		}
 	}
 
 	
 	
 	
 	// event handler when notif icon clicked
 	public void addStoryPopup(View v) {
 		TextView title = (TextView) findViewById(R.id.titleStory);
 		story = (Story) title.getTag();
 
 		LinearLayout ll = null;
 		final Dialog dialog = new Dialog(HomeActivity.this);
 
 		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
 		dialog.setContentView(R.layout.dialog_pick_story);
 
 		dialog.setTitle(R.string.pick_story);
 
 		dialog.show();
 		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
 				R.drawable.paper_plane);
 
 		if (story != null) {
 			EditText t = (EditText) dialog.findViewById(R.id.valueNameStory);
 			t.setText(story.getName());
 			t = (EditText) dialog.findViewById(R.id.valueDescriptionStory);
 			t.setText(story.getDescription());
 
 			TextView tv = (TextView) dialog.findViewById(R.id.valueStartDate);
 			tv.setText(TimeUtil.dateFormat(
 					TimeUtil.fromEpochFormat(story.getStartDate()),
 					"EEE MMM d, yyyy"));
 			tv = (TextView) dialog.findViewById(R.id.valueStartTime);
 			tv.setText(TimeUtil.dateFormat(
 					TimeUtil.fromEpochFormat(story.getStartDate()), "k:mm a"));
 
 			tv = (TextView) dialog.findViewById(R.id.valueEndDate);
 			tv.setText(TimeUtil.dateFormat(
 					TimeUtil.fromEpochFormat(story.getEndDate()),
 					"EEE MMM d, yyyy"));
 			tv = (TextView) dialog.findViewById(R.id.valueEndTime);
 			tv.setText(TimeUtil.dateFormat(
 					TimeUtil.fromEpochFormat(story.getEndDate()), "k:mm a"));
 
 		}
 
 		// pick date
 		ll = (LinearLayout) dialog.findViewById(R.id.pickDate);
 		ll.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Helper.showDatePicker(dialog.getContext(),
 						dialog.findViewById(R.id.valueStartDate));
 			}
 		});
 
 		// pick time
 		ll = (LinearLayout) dialog.findViewById(R.id.pickTime);
 		ll.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Helper.showTimePicker(dialog.getContext(),
 						dialog.findViewById(R.id.valueStartTime));
 			}
 		});
 
 		// pick date end
 		ll = (LinearLayout) dialog.findViewById(R.id.pickDateEnd);
 		ll.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Helper.showDatePicker(dialog.getContext(),
 						dialog.findViewById(R.id.valueEndDate));
 			}
 		});
 
 		// pick time end
 		ll = (LinearLayout) dialog.findViewById(R.id.pickTimeEnd);
 		ll.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Helper.showTimePicker(dialog.getContext(),
 						dialog.findViewById(R.id.valueEndTime));
 			}
 		});
 
 		// submit button
 		Button okButton = (Button) dialog.findViewById(R.id.submitDateButton);
 		okButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Story s = null;
 				try {
 					s = Helper.buildFromDateTimeStory(story, dialog);
 
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				if (s != null)
 					Helper.buildUIMain(HomeActivity.this, s);
 
 				dialog.dismiss();
 			}
 		});
 	}
 
 }
