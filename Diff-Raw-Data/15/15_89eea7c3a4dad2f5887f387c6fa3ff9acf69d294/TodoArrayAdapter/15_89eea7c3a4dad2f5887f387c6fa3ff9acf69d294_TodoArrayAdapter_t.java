 package com.example.tabactionbar;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 
 import com.parse.ParseObject;
 
 public class TodoArrayAdapter extends ArrayAdapter<TodoModel>{
 
 	private final List<TodoModel> list;
 	private final Activity context;
 //	private final TodoArrayAdapter self = this;
 	
 	public TodoArrayAdapter(Activity context, List<TodoModel> list) {
 		super(context, R.layout.todo_list_view, list);
 		this.list = list;
 		this.context = context;
 	}
 
 	static class ViewHolder {
         protected TextView name;
         protected CheckBox delete;
     }
 	
 	@Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         View view = null;
         if (convertView == null) {
             LayoutInflater inflator = context.getLayoutInflater();
             view = inflator.inflate(R.layout.todo_list_view, null);
             final ViewHolder viewHolder = new ViewHolder();
             viewHolder.name = (TextView) view.findViewById(R.id.list_name);
             viewHolder.name.setTextColor(Color.BLACK);
             viewHolder.delete = (CheckBox) view.findViewById(R.id.checkbox_list);
             viewHolder.delete.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				
 				@Override
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					if (isChecked) {
 						new AlertDialog.Builder(context)
 					    .setTitle("Delete entry")
 					    .setMessage("Are you sure you want to delete this entry?")
 					    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					        public void onClick(DialogInterface dialog, int which) { 
 					            // continue with delete
 					        	Log.e("InitList", "Pressed yes and now trying to delete");
					        	InitListTodoFragment.deleteTask(list.get(position));
 					        	ParseObject deleteTask = new ParseObject("Todo_List");
 					        	deleteTask.put("action", "deleted task");
 					        	deleteTask.saveInBackground();
 					        	
 					        }
 					     })
 					    .setNegativeButton("No", new DialogInterface.OnClickListener() {
 					        public void onClick(DialogInterface dialog, int which) { 
 					            // simply close the alert dialog
 					        	viewHolder.delete.setChecked(false);
 					        	dialog.dismiss();
 					        }
 					     })
 					     .show();
 					}
 					
 				}
 			});
             view.setTag(viewHolder);
         } else {
             view = convertView;
         }
         ViewHolder holder = (ViewHolder) view.getTag();
         Log.e("todoArray",list.get(position).getName());
         holder.name.setText(list.get(position).getName());
         return view;
     }
 
 	@Override
 	public boolean areAllItemsEnabled() {
 	    return true;
 	}
 	
 	@Override
 	public boolean isEnabled(int position) {
 	    return true;
 	}
 	
 }
