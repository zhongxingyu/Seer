 package com.livestrong.myplate.adapters;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.livestrong.myplate.MyPlateApplication;
 import com.livestrong.myplate.R;
 import com.livestrong.myplate.back.DataHelper;
 import com.livestrong.myplate.back.api.ApiHelper;
 import com.livestrong.myplate.back.api.models.ExerciseSearchResponse;
 import com.livestrong.myplate.back.models.Exercise;
 import com.livestrong.myplate.utilities.ImageLoader;
 
 public class ExerciseSelectorAdapter extends AbstractBaseAdapterDataHelperDelegate {
 
 	private List<Exercise> exercises = new ArrayList<Exercise>();
 	private ImageLoader imageLoader;
 	private Boolean isLoading = false;
 	private Boolean showSearchOnlinePromp = false;
 	public Boolean showNoResultsMessage = false;
 	
 	public ExerciseSelectorAdapter(Activity activity, List<Exercise> exercises) {
 		this.activity = activity;
 		this.exercises = exercises;
 		this.imageLoader = new ImageLoader(this.activity);
 		
 		loadRecentlyPerformed();
 	}
 	
 	/**** Load list data functions ****/
 	
 	public void loadRecentlyPerformed(){
 		this.isLoading = false;
 		this.showSearchOnlinePromp = false;
 		this.showNoResultsMessage = false;
 		this.setExercises(DataHelper.getRecentExercises(null));
 	}
 	
 	public void loadFrequentlyPerformed(){
 		this.isLoading = false;
 		this.showSearchOnlinePromp = false;
 		this.showNoResultsMessage = false;
 		this.setExercises(DataHelper.getFavoriteExercises(null));
 	}
 	
 	public void loadCustomExercises(){
 		this.isLoading = false;
 		this.showSearchOnlinePromp = false;
 		this.showNoResultsMessage = false;
 		this.setExercises(DataHelper.getCustomExercises());
 	}
 	
 	public void loadExercisesFromLocalSearch(String searchStr){
 		ExerciseSearchResponse exerciseSearchResponse = DataHelper.searchExercises(searchStr, true, null);
 		this.setExercises((List<Exercise>)exerciseSearchResponse.getExercises());
 		this.showSearchOnlinePromp = true;
 		this.showNoResultsMessage = false;
 	}
 	
 	public void loadExercisesFromServerSearch(String searchStr){
 		this.showSearchOnlinePromp = false;
 		this.showNoResultsMessage = false;
 		DataHelper.searchExercises(searchStr, false, this);
 		this.isLoading = true;
 		this.setExercises(null);
 	}
 
 	/**
 	 * DataHelperDelegate call back
 	 */
 	@Override
 	public void dataReceived(Method methodCalled, Object data) {
 		this.isLoading = false;
 		this.notifyDataSetChanged();
 		Log.d("ExerciseSelectorActivity","dataReceived");
 		if (data instanceof ExerciseSearchResponse) {
 			ExerciseSearchResponse exerciseSearchResponse = (ExerciseSearchResponse) data;
 			this.setExercises((List<Exercise>) exerciseSearchResponse.getExercises());
 			if (exerciseSearchResponse.getExercises().size() == 0){
				this.showNoResultsMessage = true;
 			}
 		}
 	}
 
 	@Override
 	public int getCount() {
 		if (this.isLoading){
 			return 1;
 		}
 		
 		int count = 0;
 		if (this.exercises != null){
 			count = this.exercises.size();
 		}
 		
 		if (this.showSearchOnlinePromp || this.showNoResultsMessage){
 			count++;
 		}
 		return count;
 	}
 
 	@Override
 	public Object getItem(int position) {
 		if (this.isShowingSearchOnlinePrompt() && position >= this.exercises.size()){
 			return null;
 		}
 		
 		return (this.exercises == null) ? 0 : this.exercises.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Context context = MyPlateApplication.getContext();
 		
 		if (this.isLoading){
 			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_loading, null);
 			return convertView;
 		} else if (this.showSearchOnlinePromp && position == this.exercises.size()){
 			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_message, null);
 			TextView message = (TextView) convertView.findViewById(R.id.messageTextView);
 			if (ApiHelper.isOnline()){
 				message.setText("Tap to search online...");	
 			} else {
 				message.setText("Offline - only previously tracked items available.");
 			}
 			return convertView;
 		} else if (this.showNoResultsMessage && position == this.exercises.size()){
 			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_message, null);
 			((TextView)convertView.findViewById(R.id.messageTextView)).setText("No Results Found.");
 			return convertView;
 		}
 		
 		
 		if (convertView == null || convertView.getId() != R.id.listItemFood) {
 			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_food, null);			
 		}
 		
 		// Retrieve exercise at position
 		Exercise exercise = (Exercise) getItem(position);
 		if (exercise != null){
 			TextView exerciseNameTextField = (TextView)convertView.findViewById(R.id.foodNameTextView);
 			exerciseNameTextField.setText(exercise.getTitle());
 			
 			TextView exerciseDescriptionTextField = (TextView)convertView.findViewById(R.id.foodDescriptionTextView);
 			exerciseDescriptionTextField.setText(exercise.getDescription());
 	
 			ImageView imageView = (ImageView)convertView.findViewById(R.id.foodImageView);
 			imageView.setImageResource(R.drawable.icon_fitness);
 			this.imageLoader.DisplayImage(exercise.getSmallImage(), imageView);
 		} 
 		
 		return convertView;
 	}
 	
 	public void setExercises(List<Exercise> exercises){		
 		this.exercises = exercises;
 		notifyDataSetChanged();
 	}
 	
 	public Boolean isShowingSearchOnlinePrompt(){
 		return this.showSearchOnlinePromp;
 	}
 	
 	public Boolean isLoading(){
 		return this.isLoading;
 	}
 }
