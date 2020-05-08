 package il.ac.huji.todolist;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class TodoListArrayAdapter extends ArrayAdapter<TodoItem> {
 	
 	public TodoListArrayAdapter(Activity activity,int textViewResourceId , List<TodoItem> arr) 
 	{
 		super(activity, textViewResourceId,arr);
 		
 		
 	}
 
 
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		View view = inflater.inflate(R.layout.row,null);
 		TextView text = (TextView)view.findViewById(R.id.txtTodoTitle);
 		text.setText(getItem(position).title);
 		text.setTextColor( position%2 == 0 ?Color.RED : Color.BLUE);
 		TextView dueDate = (TextView)view.findViewById(R.id.txtTodoDueDate);
 		if (null == getItem(position).dueDate)
 		{
 			dueDate.setText("No due date");
 		}
 		else
 		{
			dueDate.setText(getItem(position).dueDate.toString()); //TODO: make sure it's the right format
 		}
 		//TODO: format color according to dueDate status
 		return view;
 	}
 	
 }
