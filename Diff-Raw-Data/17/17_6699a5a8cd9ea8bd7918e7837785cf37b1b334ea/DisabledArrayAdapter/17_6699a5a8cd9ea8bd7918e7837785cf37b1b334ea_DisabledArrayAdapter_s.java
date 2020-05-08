 package com.jep.learning.widgets;
 
 import java.util.List;
 
 import com.jep.learning.models.User;
 import com.jep.learning.services.SessionService;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public class DisabledArrayAdapter<T> extends ArrayAdapter<T>{
 	SessionService service;
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = super.getView(position, convertView, parent);
         view.setEnabled(isEnabled(position));
         return view;
 	}
 	public DisabledArrayAdapter(Context context, int textViewResourceId,
 			List<T> objects, SharedPreferences prefs) {
 		super(context, textViewResourceId, objects);
 		service = new SessionService(prefs);
 	}
 	@Override
 	public boolean areAllItemsEnabled() {
 		return false;
 	}
 	@Override
 	public boolean isEnabled(int position) {
 		if(position == 0) {
 			return true;
 		}
 		User user = service.getCurrentUser();
 		User data = service.getUserSession(user);
		Log.e("app", "User is at chapter" +  data.getCurrentChapter() + "postion is" + position);
 		if(data.getCurrentChapter() >= position) {
 			return true;
 		}
 		return false;
 	}
 
 }
