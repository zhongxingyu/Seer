 package il.ac.huji.todolist;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class TodoListArrayAdapter extends ArrayAdapter<String> {
 	public TodoListArrayAdapter(Activity activity,int textViewResourceId , List<String> arr) 
 	{
 		super(activity, textViewResourceId,arr);
 		
 		
 	}
 
 
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		View view = inflater.inflate(R.layout.row,null);
 		TextView text = (TextView)view.findViewById(R.id.todoText);
 		text.setText(getItem(position));
		text.setBackgroundColor( position%2 == 0 ?Color.RED : Color.BLUE);
 		return view;
 	}
 	
 }
