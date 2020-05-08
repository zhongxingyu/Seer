 package cz.kinst.jakub.coursemanager;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 public class Course extends CMActivity implements Serializable{
 
 	private static final String TAB_LESSONS = "lessons";
 	private int cid;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//addTab(TAB_LESSONS,R.layout.course,getText(R.string.lesson));
 		//switchTab(TAB_LESSONS);
 		setContentView(R.layout.course);
 		this.cid = getIntent().getExtras().getInt("cid");
 		reload();
 	}
 
 	@Override
 	protected JSONObject reloadWork() {
 		JSONObject course = new JSONObject();
 		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 		args.add(new BasicNameValuePair("cid", String.valueOf(this.cid)));
 		course = cm.getAction("course", "homepage", args,
 				new ArrayList<NameValuePair>());
 		return course;
 	}
 
 	@Override
 	public void gotData(JSONObject data) throws JSONException {
 		JSONObject course = data.getJSONObject("activeCourse");
 		String name = course.getString("name");
 		String description = course.getString("description");
 		((TextView) findViewById(R.id.name)).setText(name);
 		((TextView) findViewById(R.id.description)).setText(description);
 
 		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
 		JSONArray lessons = data.getJSONArray("lessons");
 		for (int i = 0; i < lessons.length(); i++) {
 			list.add(lessons.getJSONObject(i));
 		}
 		ExpandableListView lessonView = (ExpandableListView) findViewById(R.id.lessons);
 		lessonView.setAdapter(new LessonListAdapter(list));
		lessonView.expandGroup(0);
 	}
 
 	public class LessonListAdapter extends BaseExpandableListAdapter {
 
 		public ArrayList<JSONObject> lessons;
 
 		public LessonListAdapter(ArrayList<JSONObject> lessons) {
 			this.lessons = lessons;
 		}
 
 		@Override
 		public Object getChild(int groupPosition, int childPosition) {
 			return lessons.get(childPosition);
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.lesson_content_row, null);
 			}
 
 			WebView content = (WebView) v.findViewById(R.id.content);
 			Button detail = (Button) v.findViewById(R.id.show_more_button);
 			
 
 			content.setVerticalScrollBarEnabled(true);
 			content.setHorizontalScrollBarEnabled(true);
 			final JSONObject lesson;
 			lesson = lessons.get(groupPosition);
 			String html = "";
 			try {
 				html = "<html><body>" + lesson.getString("description_html")
 						+ "</body></html>";
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			detail.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 				Intent i = new Intent(Course.this,Lesson.class);
 				try {
 					i.putExtra("lid",lesson.getInt("id"));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				i.putExtra("cm", cm);
 				startActivity(i);
 
 				}
 			});
 			
 			content.loadData(html, "text/html", null);
 			return v;
 		}
 	
 
 		@Override
 		public int getChildrenCount(int groupPosition) {
 			return 1;
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getGroupCount() {
 			return lessons.size();
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.lesson_header_row, null);
 			}
 
 			TextView topic = (TextView) v.findViewById(R.id.topic);
 			TextView date = (TextView) v.findViewById(R.id.date);
 
 			final JSONObject lesson;
 			lesson = lessons.get(groupPosition);
 			try {
 				topic.setText(lesson.getString("topic"));
 				date.setText(lesson.getString("date"));
 			} catch (JSONException e) {
 			}
 			return v;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 	}
 
 }
