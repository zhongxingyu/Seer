 package com.liorginsberg.talktodo;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 class TaskAdapter extends ArrayAdapter<Task> {
 
     private ArrayList<Task> tasks;
     private Context context;
 
     public TaskAdapter(Context context, int textViewResourceId,
 	    ArrayList<Task> tasks) {
 	super(context, textViewResourceId, tasks);
 	this.tasks = tasks;
 	this.context = context;
     }
 
     public View getView(int position, View convertView, ViewGroup parent) {
 	ViewHolder holder;
 	final int pos = position;
 	if (convertView == null) {
 	    LayoutInflater inflater = (LayoutInflater) getContext()
 		    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	    convertView = inflater.inflate(R.layout.tasklist_item, null);
 	    holder = new ViewHolder();
 	    holder.tvTitle = (TextView) convertView
 		    .findViewById(R.id.tvTaskTitle);
 	    holder.btnDone = (Button) convertView.findViewById(R.id.btnDone);
 	    holder.btnDone.setOnClickListener(new OnClickListener() {
 
 		public void onClick(View v) {
 
 		    Toast.makeText(context,
 			    "You Are done with: " + tasks.get(pos).getTitle(),
			    Toast.LENGTH_LONG);
 
 		}
 	    });
 
 	    convertView.setTag(holder);
 
 	} else {
 	    holder = (ViewHolder) convertView.getTag();
 	}
 
 	holder.tvTitle.setText(tasks.get(position).getTitle());
 	return convertView;
     }
 
     static class ViewHolder {
 	TextView tvTitle;
 	Button btnDone;
     }
 }
