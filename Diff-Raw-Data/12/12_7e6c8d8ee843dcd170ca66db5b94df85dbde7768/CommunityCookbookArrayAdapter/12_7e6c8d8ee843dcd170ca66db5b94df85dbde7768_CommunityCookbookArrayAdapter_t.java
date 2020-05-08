 package com.csci5115.group2.planmymeal;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class CommunityCookbookArrayAdapter extends ArrayAdapter<Meal> {
 	
 	private final Context context;
 	private final Meal[] values;
 
 	public CommunityCookbookArrayAdapter(Context context, Meal[] values) {
 		super(context, R.layout.row_community_cookbook_meal, values);
 		this.context = context;
 		this.values = values;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 	    LayoutInflater inflater = (LayoutInflater) context
 	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 	    View rowView = inflater.inflate(R.layout.row_community_cookbook_meal, parent, false);
 	    TextView mealName = (TextView) rowView.findViewById(R.id.row_communityCookbookMeal_listMealName);
 	    TextView mealTime = (TextView) rowView.findViewById(R.id.row_communityCookbookMeal_listMealTime);
 	    
 	    Meal meal = values[position];
 	    mealName.setText(meal.getName());
	    mealTime.setText(Double.toString(meal.getTime()));
 	    
 	    return rowView;
 	}
 }
