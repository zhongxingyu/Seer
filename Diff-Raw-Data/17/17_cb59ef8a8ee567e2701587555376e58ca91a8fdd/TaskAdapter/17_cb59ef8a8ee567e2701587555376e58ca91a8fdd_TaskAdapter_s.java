 package com.whatstodo.activities;
 
 import android.content.Context;
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 import com.whatstodo.R;
 import com.whatstodo.models.Task;
 
 public class TaskAdapter extends ArrayAdapter<Task> {
 
 	private Context context;
 	private java.util.List<Task> tasks;
 
 	public TaskAdapter(Context ctx, int textViewResourceId,
 			java.util.List<Task> tasks) {
 		super(ctx, textViewResourceId, tasks);
 		context = ctx;
 		this.tasks = tasks;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View element = convertView;
 		if (element == null) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			element = inflater.inflate(R.layout.taskitem, null);
 		}
 		Task task = tasks.get(position);
 
 		TextView taskName = (TextView) element.findViewById(R.id.taskName);
 		TextView taskDate = (TextView) element.findViewById(R.id.taskDate);
 		TextView taskPriority = (TextView) element
 				.findViewById(R.id.taskPriority);
 		TextView taskDone = (TextView) element.findViewById(R.id.taskDone);
 		
 		FrameLayout button = (FrameLayout) element
 				.findViewById(R.id.buttonLayout);
 
 		taskName.setText(task.getName());
 		taskName.getInputExtras(true).putLong("id", task.getId());
 
 		switch (task.getPriority()) {
 		case HIGH:
 			taskPriority.setBackgroundResource(R.drawable.rating_important);
 			break;
 		case NORMAL:
 			taskPriority.setBackgroundResource(R.drawable.rating_half_important);
 			break;
 		case LOW:
 			taskPriority.setBackgroundResource(R.drawable.rating_not_important);
 		}
 
 		if (task.isDone()) {
 			taskDone.setBackgroundResource(R.drawable.check);
 			button.setBackgroundResource(R.drawable.buttontransparent);
 //			taskName.setTextColor(R.color.White);
 //			taskDate.setTextColor(R.color.White);
 		} else {
 			taskDone.setBackgroundResource(R.drawable.checkbox3);
 			button.setBackgroundResource(R.drawable.button3);
 //			taskName.setTextColor(R.color.Black);
 //			taskDate.setTextColor(R.color.Black);
 		}
 		
		if (task.getDate() != null) {
			taskDate.setText(DateFormat.format("dd.MM.yyyy", task.getDate()));
		}

 		return element;
 	}
 }
