 package edu.upenn.cis350.mosstalkwords;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 
 
 	public class SetAdapter extends ArrayAdapter<Set>{
 		Context context;
 		int layoutResourceId;
 		ArrayList<Set> data = null;
 		
 		public SetAdapter(Context context, int layoutResourceId, ArrayList<Set> data){
 			super(context, layoutResourceId, data);
 			this.layoutResourceId = layoutResourceId;
 			this.context = context;
 			this.data = data;
 		}
 		
 		@Override
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	        View row = convertView;
 	        SetInfo inf = null;
 	       
 	        if(row == null)
 	        {
 	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
 	            row = inflater.inflate(layoutResourceId, parent, false);
 	            
 	           
 	            inf = new SetInfo();
 	            inf.star1Icon = (ImageView)row.findViewById(R.id.star1Icon);
 	            inf.star2Icon = (ImageView)row.findViewById(R.id.star2Icon);
 	            inf.star3Icon = (ImageView)row.findViewById(R.id.star3Icon);
 	            inf.category = (TextView)row.findViewById(R.id.txtCategory);
 	            inf.difficulty =(TextView)row.findViewById(R.id.txtDifficulty);
 	            row.setTag(inf);
 	        }
 	        else
 	        {
 	            inf = (SetInfo)row.getTag();
 	        }
	       
 	        Set currset = data.get(position);
 	        inf.category.setText(currset.category);
 	        inf.difficulty.setText(currset.difficulty);
 	        inf.star1Icon.setImageResource(currset.star1);
 	        inf.star2Icon.setImageResource(currset.star2);
 	        inf.star3Icon.setImageResource(currset.star3);
 	        row.setBackgroundColor(currset.color);
	       
 	        return row;
 	    }
 	   
 	    static class SetInfo
 	    {
 	        ImageView star1Icon;
 	        ImageView star2Icon;
 	        ImageView star3Icon;
 	        TextView category;
 	        TextView difficulty;
 	    }
 	
 	}
 	
 
