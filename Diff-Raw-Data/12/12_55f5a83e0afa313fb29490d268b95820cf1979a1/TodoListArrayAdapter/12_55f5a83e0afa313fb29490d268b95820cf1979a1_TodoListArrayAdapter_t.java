 package il.ac.huji.todolist;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.annotation.SuppressLint;
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
 
 
 	
 	@SuppressLint("SimpleDateFormat")
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		View view = inflater.inflate(R.layout.row,null);
 		TodoItem currentItem = getItem(position);
 		TextView text = (TextView)view.findViewById(R.id.txtTodoTitle);
 		text.setText(currentItem.title);
 		TextView dueDate = (TextView)view.findViewById(R.id.txtTodoDueDate);
 		if (null == currentItem.dueDate)
 		{
 			dueDate.setText("No due date");
 		}
 		else
 		{
 			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 			dueDate.setText(df.format(currentItem.dueDate));
 		}
 		
 		Calendar clNow = Calendar.getInstance();
 		Calendar clCurrent = Calendar.getInstance();
 		
 		clNow.setTime(new Date());
 		clNow.add(Calendar.DATE, -1);
 		
 		clCurrent.setTime(currentItem.dueDate);
 		
 		if (clNow.after(clCurrent))
 		{
 			text.setTextColor(Color.RED);
 			dueDate.setTextColor(Color.RED);
 		}
 		
 		return view;
 	}
 	
 }
