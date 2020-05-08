 package soofw.trk;
 
 import android.animation.Animator;
 import android.animation.Animator.AnimatorListener;
 import android.content.Context;
 import android.graphics.Paint;
 import android.view.HapticFeedbackConstants;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 import soofw.util.FlowLayout;
 
 class TaskAdapter extends ArrayAdapter<Task> {
 	View view;
 	Main context;
 	ArrayList<Task> tasks;
 
 	TaskAdapter(Context context, ArrayList<Task> tasks) {
 		super(context, R.layout.task_list_item, tasks);
 		this.context = (Main)context;
 		this.tasks = tasks;
 	}
 
 	@Override
 	public View getView(final int position, final View convertView, final ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		Calendar now = Calendar.getInstance();
 		Calendar tomorrow = Calendar.getInstance();
 		tomorrow.add(Calendar.DATE, 1);
 
 		final Task temp = this.getItem(position);
 		final String label = temp.toString();
 		String[] tags = temp.getTags();
 
 		this.view = convertView;
 		if(this.view == null) {
 			this.view = inflater.inflate(R.layout.task_list_item, null);
 		}
 		this.view.setAlpha(1);
 		this.view.setTranslationX(0);
 		this.view.setVisibility(View.VISIBLE);
 		if(this.view.getLayoutParams() != null) {
 			this.view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
 			this.view.requestLayout();
 		}
 		this.view.setOnTouchListener(new View.OnTouchListener() {
 			// https://www.youtube.com/watch?v=YCHNAi9kJI4&feature=player_embedded
 			float downX;
 			long downTime;
 			int swipeSlop = -1;
 			int tapTime = -1;
 			int longTime = -1;
 			Timer longTimer;
 			boolean swiping = false;
			boolean canSwipe = true;
 
 			private void cancelLongPress() {
 				if(longTimer != null) {
 					longTimer.cancel();
 				}
 			}
			private void setBackground(View view, int id) {
				view.setBackgroundColor(TaskAdapter.this.context.getResources().getColor(id));
			}
 
 			@Override
 			public boolean onTouch(final View view, final MotionEvent event) {
 				if(swipeSlop < 0 || tapTime < 0 || longTime < 0) {
 					ViewConfiguration vc = ViewConfiguration.get(TaskAdapter.this.context);
 					swipeSlop = vc.getScaledTouchSlop();
 					tapTime = vc.getTapTimeout();
 					longTime = vc.getLongPressTimeout();
 				}
 
 				float x, dx, dxa;
 				switch(event.getAction()) {
 					case MotionEvent.ACTION_DOWN:
 						downX = event.getX();
 						downTime = System.currentTimeMillis();
 						cancelLongPress();
						setBackground(view, R.color.task_item_bg_focus);
						canSwipe = true;
 						longTimer = new Timer();
 						longTimer.schedule(new TimerTask() {
 							@Override
 							public void run() {
								canSwipe = false;
 								view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
 								new ActionDialogFragment(temp)
 									.show(TaskAdapter.this.context.getSupportFragmentManager(), "tag?");
 							}
 						}, longTime);
 						break;
 
 					case MotionEvent.ACTION_CANCEL:
						setBackground(view, R.color.task_item_bg);
 						view.setAlpha(1);
 						view.setTranslationX(0);
 						cancelLongPress();
 						break;
 
 					case MotionEvent.ACTION_MOVE:
						setBackground(view, R.color.task_item_bg);
 						x = event.getX() + view.getTranslationX();
 						dx = x - downX;
 						dxa = Math.abs(dx);
 
						if(!swiping && canSwipe) {
 							if(dxa >= swipeSlop) {
 								swiping = true;
 								((ListView)parent).requestDisallowInterceptTouchEvent(true);
 								cancelLongPress();
 							}
 						}
 						if(swiping) {
 							view.setTranslationX(dx);
 							view.setAlpha(1 - dxa/view.getWidth());
 						}
 						break;
 
 					case MotionEvent.ACTION_UP:
						setBackground(view, R.color.task_item_bg);
 						long elapsed = System.currentTimeMillis() - downTime;
 						if(swiping) { // SWIPE
 							x = event.getX() + view.getTranslationX();
 							dx = x - downX;
 							dxa = Math.abs(dx);
 							float fractionCovered, endX, endAlpha;
 							final boolean remove;
 							if(dxa >= view.getWidth() / 4) {
 								remove = true;
 								fractionCovered = dxa / view.getWidth();
 								endX = (dx > 0) ? view.getWidth() : -view.getWidth();
 								endAlpha = 0;
								TaskAdapter.this.context.deletions++;
 							} else {
 								remove = false;
 								fractionCovered = 1 - (dxa / view.getWidth());
 								endX = 0;
 								endAlpha = 1;
 							}
 							long duration = (int)((1 - fractionCovered) * 500);
 							((ListView)parent).setEnabled(false);
 							AnimatorListener al = new AnimatorListener() {
 								@Override
 								public void onAnimationEnd(Animator anim) {
 									if(remove) {
 										TaskAdapter.this.context.deleteItem(view, temp);
 									} else {
 										view.setAlpha(1);
 										view.setTranslationX(0);
 										swiping = false;
 										((ListView)parent).setEnabled(true);
 									}
 								}
 								@Override public void onAnimationRepeat(Animator anim) {}
 								@Override public void onAnimationStart(Animator anim) {}
 								@Override public void onAnimationCancel(Animator anim) {}
 							};
 							view.animate().setDuration(duration)
 								.alpha(endAlpha).translationX(endX)
 								.setListener(al);
 						} else if(elapsed <= tapTime) { // TAP
 							temp.setDone(!temp.done);
 							TaskAdapter.this.notifyDataSetChanged();
 							TaskAdapter.this.context.list.write();
 						}
 						if(elapsed <= longTime) {
 							cancelLongPress();
 						}
 						break;
 					default:
 						return false;
 				}
 				return true;
 			}
 		});
 
 		FlowLayout tags_layout = (FlowLayout)view.findViewById(R.id.tags);
 		CheckedTextView text = (CheckedTextView)view.findViewById(R.id.text);
 
 		text.setText(label);
 		text.setTextColor(this.context.getResources().getColor(temp.done ? R.color.done : R.color.not_done));
 		((ListView)parent).setItemChecked(position, temp.done);
 		text.setChecked(temp.done);
 		if(temp.done) {
 			text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
 		} else {
 			text.setPaintFlags(text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
 		}
 
 		if(tags.length == 0) {
 			tags_layout.setVisibility(View.GONE);
 		} else {
 			tags_layout.setVisibility(View.VISIBLE);
 			for(int i = 0; i < tags.length || i < tags_layout.getChildCount(); i++) {
 				if(i < tags.length) {
 					TextView tag;
 					if(i < tags_layout.getChildCount()) {
 						tag = (TextView)(tags_layout.getChildAt(i));
 						tag.setVisibility(View.VISIBLE);
 					} else {
 						tag = (TextView)inflater.inflate(R.layout.tag_item, tags_layout, false);
 						tags_layout.addView(tag);
 					}
 
 					tag.setText(tags[i]);
 					int bg_id = 0;
 					switch(tags[i].charAt(0)) {
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
 							if(tags[i].equals("!0")) {
 								bg_id = R.color.lowpriority_bg;
 							} else {
 								bg_id = R.color.priority_bg;
 							}
 							break;
 						default:
 							if(temp.calendar.before(now)) {
 								bg_id = R.color.date_overdue_bg;
 							} else if(temp.calendar.before(tomorrow)) {
 								bg_id = R.color.date_soon_bg;
 							} else {
 								bg_id = R.color.date_bg;
 							}
 					}
 
 					tag.setBackgroundColor(this.context.getResources().getColor(bg_id));
 				} else {
 					tags_layout.getChildAt(i).setVisibility(View.GONE);
 				}
 			}
 		}
 
 		return view;
 	}
 }
