 package mtu.notes;
 
 import java.util.List;
 
import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Environment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ExpandableListAdapter extends BaseExpandableListAdapter{
 
 	private Activity context;
 	private Map<String, List<String>> journals;
 	private List<String> notes;
 	
 	public ExpandableListAdapter(Activity context, List<String> notes,
 			Map<String, List<String>> journals){
 		this.context = context;
 		this.notes = notes;
 		this.journals = journals;
 	}
 	
 	@Override
 	public Object getChild(int groupPosition, int childPosition) {
 		return journals.get(notes.get(groupPosition)).get(childPosition);
 	}
 
 	@Override
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 
 	@Override
 	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
 			ViewGroup parent) {
 		final String noteName = (String) getChild(groupPosition, childPosition);
 		LayoutInflater inflater = context.getLayoutInflater();
 		
 		if(convertView == null)
 			convertView = inflater.inflate(R.layout.child_item, null);
 		
 		TextView note = (TextView) convertView.findViewById(R.id.noteName);
 		
 		String journalPath = "";
 		int i = 0;
 		for(String jN : journals.keySet())
 		{
 			if (i == groupPosition)
 				journalPath = jN;
 		}
 		final String jN = journalPath;
 		Button edit = (Button) convertView.findViewById(R.id.editButton);
 		edit.setOnClickListener(new OnClickListener(){
 			public void onClick(View view){
 				Intent intent = new Intent(context, EditNoteActivity.class);
 				intent.putExtra("path", Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + jN + "/" + noteName);
 				intent.putExtra("name", noteName);
 				context.startActivity(intent);
 			}
 		});
 		
 		Button view = (Button) convertView.findViewById(R.id.viewButton);
 		view.setOnClickListener(new OnClickListener(){
 			public void onClick(View view){
 				Intent intent = new Intent(context, ViewNoteActivity.class);
 				intent.putExtra("path", Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + jN + "/" + noteName);
 				intent.putExtra("name", noteName);
 				context.startActivity(intent);
 			}
 		});
 		
 		Button delete = (Button) convertView.findViewById(R.id.deleteButton);
 		delete.setOnClickListener(new OnClickListener(){
 			public void onClick(View view){
				//TODO Delete note here
 			}
 		});
 		
 		note.setText(noteName);
 		return convertView;
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition) {
 		return journals.get(notes.get(groupPosition)).size();
 	}
 
 	@Override
 	public Object getGroup(int groupPosition) {
 		return notes.get(groupPosition);
 	}
 
 	@Override
 	public int getGroupCount() {
 		return notes.size();
 	}
 
 	@Override
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 		final String journalName = (String) getGroup(groupPosition);
 		if(convertView == null){
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = inflater.inflate(R.layout.group_item, null);
 		}
 		TextView journal = (TextView) convertView.findViewById(R.id.journalName);
 		journal.setTypeface(null, Typeface.BOLD);
 		journal.setText(journalName);
 		return convertView;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public boolean isChildSelectable(int arg0, int arg1) {
 		return true;
 	}
 
 }
