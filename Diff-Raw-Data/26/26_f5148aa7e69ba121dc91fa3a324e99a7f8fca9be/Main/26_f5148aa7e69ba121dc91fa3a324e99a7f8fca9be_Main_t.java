 package soofw.trk;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.GestureDetectorCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.Transformation;
 import android.view.GestureDetector;
 import android.view.HapticFeedbackConstants;
 import android.view.inputmethod.EditorInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.util.Calendar;
 import soofw.util.FlowLayout;
 import soofw.util.SpaceTokenizer;
 
 public class Main extends FragmentActivity {
 	Trk app = null;
 
 	DrawerLayout drawerLayout = null;
 	LayoutInflater inflater = null;
 	FlowLayout filterLayout = null;
 	ListView drawer = null;
 	ListView taskView = null;
 	MultiAutoCompleteTextView omnibar = null;
 	GestureDetectorCompat detector = null;
 
 	final static int CONSUMED_IGNORE = 0;
 	final static int CONSUMED_NOTHING = 1;
 	final static int CONSUMED_LONGPRESS = 2;
 	final static int CONSUMED_SWIPE = 3;
 	final static int CONSUMED_RESTORE = 4;
 	View consumedView = null;
 	int consumedPosition = -1;
 	int consumedAction = CONSUMED_IGNORE;
 
 	ArrayAdapter<String> autoCompleteAdapter = null;
 	TagAdapter tagAdapter = null;
 	TaskAdapter taskAdapter = null;
 	TaskList list = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		app = (Trk)getApplicationContext();
 		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
 		drawer = (ListView)findViewById(R.id.drawer);
 		filterLayout = (FlowLayout)findViewById(R.id.filter_layout);
 		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		omnibar = (MultiAutoCompleteTextView)findViewById(R.id.omnibar);
 		taskView = (ListView)findViewById(R.id.task_view);
 		detector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
 			final int UNDEFINED = 0;
 			final int SWIPE = 1;
 			final int SCROLL = 2;
 
 			float SLOP_DELTA = ViewConfiguration.get(Main.this).getScaledTouchSlop() * 2;
 			float SWIPE_DELTA = 360;
 			int mode = UNDEFINED;
 			View item = null;
 			int position = -1;
 
 			@Override
 			public boolean onDown(MotionEvent event) {
 				this.mode = UNDEFINED;
 				this.item = null;
 				Main.this.consumedAction = Main.CONSUMED_IGNORE;
 				Main.this.consumedPosition = -1;
 				Main.this.consumedView = null;
 
 				this.position = Main.this.taskView.pointToPosition((int)event.getX(), (int)event.getY());
 				if(this.position > -1) {
 					Log.d("TRK","Down " + this.position);
 					this.item = (View)(Main.this.taskView.getChildAt(this.position - Main.this.taskView.getFirstVisiblePosition()));
 					Main.this.consumedPosition = this.position;
 					Main.this.consumedView = this.item;

					this.item.setBackgroundColor(Main.this.getResources().getColor(R.color.list_item_bg_focus));
 				} else {
 					Log.d("TRK", "Down");
 				}
 				return true;
 			}
 
 			@Override
 			public boolean onScroll(MotionEvent event1, MotionEvent event2, float dxp, float dyp) {
 				float dx = event2.getX() - event1.getX();
 				float dy = event2.getY() - event1.getY();
 				switch(this.mode) {
 					case UNDEFINED:
 						Log.d("TRK", "Deciding");
 						Main.this.consumedAction = Main.CONSUMED_NOTHING;
 						if(Math.sqrt(dx * dx + dy * dy) >= SLOP_DELTA) {
 							if(Math.abs(dx) >= Math.abs(dy)) {
 								Log.d("TRK", "Enter SWIPE");
 								this.mode = SWIPE;
 							} else {
 								Log.d("TRK", "Enter SCROLL");
 								this.mode = SCROLL;
 								Main.this.consumedAction = Main.CONSUMED_IGNORE;
 							}
							if(this.item != null) {
								this.item.setBackgroundColor(Main.this.getResources().getColor(R.color.list_item_bg));
							}
 						}
 						break;
 					case SWIPE:
 						if(this.item != null) {
 							Log.d("TRK", "Swipe " + this.position + ": " + dx);
 							this.item.setTranslationX(dx);
 
 							float newAlpha = 1 - Math.abs(dx) / SWIPE_DELTA;
 							this.item.setAlpha(Math.max(newAlpha, 0.3f));
 
 							if(Math.abs(dx) >= SWIPE_DELTA) {
 								Main.this.consumedAction = Main.CONSUMED_SWIPE;
 							} else {
 								Main.this.consumedAction = Main.CONSUMED_RESTORE;
 							}
 						}
 						return true;
 					case SCROLL:
 						Log.d("TRK", "Scroll");
 						break;
 				}
 				return false;
 			}
 
 			@Override
 			public boolean onSingleTapUp(MotionEvent event) {
 				if(this.position > -1) {
 					Log.d("TRK", "Tap " + this.position);
 					boolean checked = Main.this.taskView.isItemChecked(this.position);
 					Main.this.list.filterList.get(this.position).setDone(!checked);
 					Main.this.taskAdapter.notifyDataSetChanged();
 					Main.this.list.write();
 				}
				if(this.item != null) {
					this.item.setBackgroundColor(Main.this.getResources().getColor(R.color.list_item_bg));
				}
 				return true;
 			}
 			@Override
 			public boolean onDoubleTap(MotionEvent event) {
 				if(this.position > -1) {
 					Log.d("TRK", "DTap " + this.position);
 					boolean checked = Main.this.taskView.isItemChecked(this.position);
 					Main.this.list.filterList.get(this.position).setDone(!checked);
 					Main.this.taskAdapter.notifyDataSetChanged();
 					Main.this.list.write();
 				}
 				return true;
 			}
 			@Override
 			public boolean onDoubleTapEvent(MotionEvent event) {
 				return true;
 			}
 
 			@Override
 			public void onLongPress(MotionEvent event) {
 				Main.this.consumedAction = Main.CONSUMED_LONGPRESS;
 				if(this.position > -1) {
 					Log.d("TRK", "Long press " + this.position);
 					Main.this.taskView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
 					new ActionDialogFragment(Main.this.list.filterList.get(this.position))
 						.show(Main.this.getSupportFragmentManager(), "tag?");
 				}
				if(this.item != null) {
					this.item.setBackgroundColor(Main.this.getResources().getColor(R.color.list_item_bg));
				}
 			}
 		});
 
 
 		filterLayout.setVisibility(View.GONE);
 		taskView.setItemsCanFocus(false);
 
 		this.list = new TaskList(this.app.listFile);
 		this.list.read();
 
 		taskAdapter = new TaskAdapter(this, this.list.filterList);
 		taskView.setAdapter(taskAdapter);
 		taskView.setLongClickable(false);
 		taskView.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View view, MotionEvent event) {
 				if(event.getActionMasked() == MotionEvent.ACTION_UP) {
					if(Main.this.consumedView != null) {
						Main.this.consumedView.setBackgroundColor(Main.this.getResources().getColor(R.color.list_item_bg));
					}
 					switch(Main.this.consumedAction) {
 						case Main.CONSUMED_NOTHING:
 						case Main.CONSUMED_LONGPRESS:
 							return true;
 						case Main.CONSUMED_SWIPE:
 							Log.d("TRK", "Dismiss " + Main.this.consumedPosition);
 							Main.this.deleteItem(Main.this.consumedView, Main.this.consumedPosition);
 							return true;
 						case Main.CONSUMED_RESTORE:
 							Log.d("TRK", "Restore " + Main.this.consumedPosition);
 							Main.this.restoreItem(Main.this.consumedView, Main.this.consumedPosition);
 							return true;
 					}
 				}
 				return Main.this.detector.onTouchEvent(event);
 			}
 		});
 
 		tagAdapter = new TagAdapter(this, this.list);
 		drawer.setAdapter(tagAdapter);
 		drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				boolean checked = ((ListView)parent).isItemChecked(position);
 				if(checked) {
 					Main.this.addFilter(view);
 				} else {
 					Main.this.removeFilter(view);
 				}
 			}
 		});
 
 
 		autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, this.list.complexTagList);
 		omnibar.setAdapter(autoCompleteAdapter);
 		omnibar.setTokenizer(new SpaceTokenizer());
 		omnibar.setThreshold(1);
 		omnibar.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				notifyAdapters();
 			}
 			@Override public void afterTextChanged(Editable s) {}
 			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 		});
 		omnibar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
 				if(actionId == EditorInfo.IME_ACTION_SEND) {
 					addItem();
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 	void updateFilters() {
 		if(this.list.tagFilters.size() == 0) {
 			filterLayout.setVisibility(View.GONE);
 			return;
 		}
 		filterLayout.setVisibility(View.VISIBLE);
 
 		Calendar now = Calendar.getInstance();
 		Calendar tomorrow = Calendar.getInstance();
 		tomorrow.add(Calendar.DATE, 1);
 
 		for(int i = 0; i < this.list.tagFilters.size() || i < filterLayout.getChildCount(); i++) {
 			if(i < this.list.tagFilters.size()) {
 				TextView tag;
 				if(i < filterLayout.getChildCount()) {
 					tag = (TextView)(filterLayout.getChildAt(i));
 					tag.setVisibility(View.VISIBLE);
 				} else {
 					tag = (TextView)this.inflater.inflate(R.layout.filter_item, filterLayout, false);
 					filterLayout.addView(tag);
 				}
 
 				tag.setText(this.list.tagFilters.get(i));
 
 				int bg_id = 0;
 				switch(this.list.tagFilters.get(i).charAt(0)) {
 					case '+':
 						bg_id = R.color.plus_bg;
 						break;
 					case '@':
 						bg_id = R.color.at_bg;
 						break;
 					case '#':
 						bg_id = R.color.hash_bg;
 						break;
 					case '!':
 						if(this.list.tagFilters.get(i).equals("!0")) {
 							bg_id = R.color.lowpriority_bg;
 						} else {
 							bg_id = R.color.priority_bg;
 						}
 						break;
 					default:
 						Calendar c = Task.matcherToCalendar(Task.re_date.matcher(this.list.tagFilters.get(i)));
 						if(c.before(now)) {
 							bg_id = R.color.date_overdue_bg;
 						} else if(c.before(tomorrow)) {
 							bg_id = R.color.date_soon_bg;
 						} else {
 							bg_id = R.color.date_bg;
 						}
 				}
 
 				tag.setBackgroundColor(this.getResources().getColor(bg_id));
 			} else {
 				filterLayout.getChildAt(i).setVisibility(View.GONE);
 			}
 		}
 	}
 	void notifyAdapters() {
 		this.list.filter(omnibar.getText().toString());
 		this.taskAdapter.notifyDataSetChanged();
 		this.tagAdapter.notifyDataSetChanged();
 
 		// apparently autoCompleteAdapter.notifyDataSetChanged()
 		// won't update a MultiAutoCompleteTextView list
 		this.autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, this.list.complexTagList);
 		this.omnibar.setAdapter(autoCompleteAdapter);
 	}
 
 	void addFilter(String filter) {
 		this.list.addTagFilter(filter);
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 	public void addFilter(View view) {
 		this.list.addTagFilter(((TextView)view).getText().toString());
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 	void removeFilter(String filter) {
 		this.list.removeTagFilter(filter);
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 	public void removeFilter(View view) {
 		this.list.removeTagFilter(((TextView)view).getText().toString());
 		this.updateFilters();
 		this.notifyAdapters();
 	}
 
 
 	public void addItem(View view) {
 		addItem();
 	}
 	void addItem() {
 		String source = omnibar.getText().toString();
 		if(!source.isEmpty()) {
 			this.list.add(source);
 			this.omnibar.setText("");
 			this.notifyAdapters();
 			this.list.write();
 		}
 	}
 
 	void editItem(Task source, String newSource) {
 		if(!newSource.isEmpty()) {
 			this.list.set(list.indexOf(source), newSource);
 			this.notifyAdapters();
 			this.list.write();
 		}
 	}
 
 	// Many thanks to http://stackoverflow.com/a/14306588
 	// and https://github.com/paraches/ListViewCellDeleteAnimation
 	void deleteItem(final View view, final int index) {
 		final int initialHeight = view.getMeasuredHeight();
 
 		AnimationListener al = new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation arg) {
 				Main.this.list.remove(list.filterList.get(index));
 				Main.this.notifyAdapters();
 				Main.this.list.write();
 			}
 			@Override public void onAnimationRepeat(Animation anim) {}
 			@Override public void onAnimationStart(Animation anim) {}
 		};
 
 		Animation anim = new Animation() {
 			@Override
 			protected void applyTransformation(float time, Transformation t) {
 				if(time == 1) {
 					view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
 					view.requestLayout();
 				} else {
 					view.getLayoutParams().height = initialHeight - (int)(initialHeight * time);
 					view.requestLayout();
 				}
 			}
 			@Override
 			public boolean willChangeBounds() {
 				return true;
 			}
 		};
 
 		anim.setAnimationListener(al);
 		anim.setDuration(200);
 		view.startAnimation(anim);
 	}
 	void restoreItem(final View view, final int index) {
 		final float initialX = view.getTranslationX();
 		final float initialAlpha = view.getAlpha();
 
 		Animation anim = new Animation() {
 			@Override
 			protected void applyTransformation(float time, Transformation t) {
 				if(time == 1) {
 					view.setAlpha(1);
 					view.setTranslationX(0);
 				} else {
 					view.setAlpha(initialAlpha + (1 - initialAlpha) * time);
 					view.setTranslationX(initialX * (1 - time));
 				}
 			}
 			@Override
 			public boolean willChangeBounds() {
 				return true;
 			}
 		};
 
 		anim.setDuration(200);
 		view.startAnimation(anim);
 	}
 }
