 package uk.co.blackpepper.penguin.android;
 
 import java.util.List;
 
 import uk.co.blackpepper.penguin.client.Story;
 import android.content.Context;
 import android.graphics.Paint;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class QueueAdapter extends ArrayAdapter<Story>
 {
 	public QueueAdapter(Context context, int textViewResourceId)
 	{
 		super(context, textViewResourceId);
 	}
 
 	public void setData(List<Story> data)
 	{
 		clear();
 		if (data != null)
 		{
 			addAll(data);
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		View view = super.getView(position, convertView, parent);
 		TextView textView = (TextView) view;
 		
 		Story story = getItem(position);
 		String text = String.format("%s - %s", story.getReference(), story.getAuthor());
 		textView.setText(text);
 		
		// TODO This should be story.isMerged()
		if (story.getMerged()) {
 			textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
 		}
 
 		return view;
 	}
 }
