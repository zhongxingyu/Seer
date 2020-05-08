 package com.soofw.trk;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.Transformation;
 import android.view.inputmethod.EditorInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import java.util.ArrayList;
 
 public class Main extends Activity {
 	private Trk app = null;
 
 	private EditText omnibar = null;
 	private ListView taskView = null;
 	private DrawerLayout drawerLayout = null;
 	private ListView drawer = null;
 
 	private TagAdapter tagAdapter = null;
 	private TaskAdapter taskAdapter = null;
 	private TaskList list = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		app = (Trk)getApplicationContext();
 		omnibar = (EditText)findViewById(R.id.omnibar);
 		taskView = (ListView)findViewById(R.id.task_view);
 		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
 		drawer = (ListView)findViewById(R.id.drawer);
 
 		taskView.setItemsCanFocus(false);
 
 		list = new TaskList(this.app.listFile);
 		list.read();
 
 		taskAdapter = new TaskAdapter(this, list.getFilterList());
 		taskView.setAdapter(taskAdapter);
 		taskView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				boolean checked = ((ListView)parent).isItemChecked(position);
 				if(checked) {
 					((ListView)parent).setItemChecked(position, false);
 					deleteItem(view, position);
 				}
 			}
 		});
 
 		tagAdapter = new TagAdapter(this, list.getTagList());
 		drawer.setAdapter(tagAdapter);
 		drawer.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				boolean checked = ((ListView)parent).isItemChecked(position);
 				String text = ((TextView)view).getText().toString();
 				if(checked) {
 					list.addTagFilter(text);
 				} else {
 					list.removeTagFilter(text);
 				}
 
 				filterItems(omnibar.getText().toString());
 				taskAdapter.notifyDataSetChanged();
 			}
 		});
 
 
 		omnibar.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				filterItems(s.toString());
 			}
 			@Override public void afterTextChanged(Editable s) {}
 			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 		});
 
 		omnibar.setOnEditorActionListener(new OnEditorActionListener() {
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
 
 	public void filterItems(String search) {
 		list.filter(search);
 		taskAdapter.notifyDataSetChanged();
 	}
 
 	public void addItem(View view) {
 		addItem();
 	}
 	public void addItem() {
 		String source = omnibar.getText().toString();
 		if(!source.isEmpty()) {
 			list.add(source);
 			taskAdapter.notifyDataSetChanged();
 			tagAdapter.notifyDataSetChanged();
 			omnibar.setText("");
 			list.write();
 		}
 	}
 
 	// Many thanks to https://github.com/paraches/ListViewCellDeleteAnimation for this code
 	public void deleteItem(final View view, final int index) {
 		AnimationListener al = new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation arg) {
 				list.remove(index);
 				list.filter(omnibar.getText().toString());
 				taskAdapter.notifyDataSetChanged();
 				tagAdapter.notifyDataSetChanged();
 				list.write();
 			}
 			@Override public void onAnimationRepeat(Animation anim) {}
 			@Override public void onAnimationStart(Animation anim) {}
 		};
 
 		collapseView(view, al);
 	}
 
 	public void collapseView(final View view, final AnimationListener al) {
 		final int initialHeight = view.getMeasuredHeight();
 
 		Animation anim = new Animation() {
 			@Override
 			protected void applyTransformation(float interpolatedTime, Transformation t) {
 				if(interpolatedTime == 1) {
					view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
 					view.requestLayout();
 				} else {
 					view.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
 					view.requestLayout();
 				}
 			}
 
 			@Override
 			public boolean willChangeBounds() {
 				return true;
 			}
 		};
 
 		if(al != null) {
 			anim.setAnimationListener(al);
 		}
 		anim.setDuration(200);
 		view.startAnimation(anim);
 	}
 
 	private class TaskAdapter extends ArrayAdapter<Task> {
 		public View view;
 
 		public TaskAdapter(Context context, ArrayList<Task> tasks) {
 			super(context, R.layout.list_item, tasks);
 		}
 
 		@Override
 		public View getView(int pos, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			this.view = convertView;
 			if(this.view == null) {
 				this.view = inflater.inflate(R.layout.list_item, null);
 			}
 
 			Task temp = this.getItem(pos);
 			String label = temp.toString();
 			String[] tags = temp.getTags();
 
 			LinearLayout tags_layout = (LinearLayout)view.findViewById(R.id.tags);
 			CheckedTextView text = (CheckedTextView)view.findViewById(R.id.text);
 
 			text.setText(label);
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
 						switch(tags[i].charAt(0)) {
 							case '+':
 								tag.setBackgroundColor(getResources().getColor(R.color.plus_color_bg));
 								break;
 							case '@':
 								tag.setBackgroundColor(getResources().getColor(R.color.at_color_bg));
 								break;
 							case '#':
 								tag.setBackgroundColor(getResources().getColor(R.color.hash_color_bg));
 								break;
 						}
 						tag.setText(tags[i].substring(1));
 					} else {
 						tags_layout.getChildAt(i).setVisibility(View.GONE);
 					}
 				}
 			}
 
 
 			return view;
 		}
 	}
 
 	private class TagAdapter extends ArrayAdapter<String> {
 		public View view;
 
 		public TagAdapter(Context context, ArrayList<String> tags) {
 			super(context, R.layout.drawer_list_item, tags);
 		}
 
 		@Override
 		public View getView(int pos, View convertView, ViewGroup parent) {
 			this.view = convertView;
 			if(this.view == null) {
 				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				this.view = inflater.inflate(R.layout.drawer_list_item, null);
 			}
 
 			String tag = this.getItem(pos);
 			CheckedTextView text = (CheckedTextView)view.findViewById(R.id.text);
 			text.setText(tag);
 			switch(tag.charAt(0)) {
 				case '+':
 					text.setTextColor(getResources().getColor(R.color.plus_color_fg));
 					break;
 				case '@':
 					text.setTextColor(getResources().getColor(R.color.at_color_fg));
 					break;
 				case '#':
 					text.setTextColor(getResources().getColor(R.color.hash_color_fg));
 					break;
 			}
 			((ListView)parent).setItemChecked(pos, Main.this.list.hasTagFilter(tag));
 
 			return view;
 		}
 	}
 }
