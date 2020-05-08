 package com.example.ucrinstagram;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.example.ucrinstagram.Models.Photo;
 import com.example.ucrinstagram.Models.User;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class Updates extends ListActivity {
 
 	private List<String> updatesArray;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_updates);
 		updatesArray = new ArrayList<String>();
 
 		buildUpdateContent();
 
 	}
 
 	private void buildUpdateContent() {
 		String username = Login.username;
 		User user = new User(username);
 		Photo[] yourPhotos = user.getPhotos();
 		Photo[] favorites = user.getFavorites();
 		User[] friends = user.getFriends();
 		Integer[] photoIds = new Integer[yourPhotos.length];
 		for( int i = 0; i < yourPhotos.length; i++){
 			int numComments = yourPhotos[i].getComments().length;
 			if(numComments > 0){
 				String updates = "Your photo: <font color=#3333FF>"
 						+ yourPhotos[i].caption + "</font> has "
 						+ Integer.toString(numComments) + " new comments.";
 				updatesArray.add(updates);
 			}
 			photoIds[i]	= Integer.valueOf(yourPhotos[i].getId());
 		}
 		User[] allUsers = new WebAPI().getAllUsers();
 		for(int i = 0; i < allUsers.length; i++){
			Photo[] photos = allUsers[i].getPhotos();
 			for(int j = 0; j < photos.length; j++){
 				if(Arrays.asList(photoIds).contains(photos[j].getId())){
 					String friendlikes = "Friend: <font color=#3333FF>"
 							+ allUsers[i].username + "</font> favorited your photo <font color=#3333FF>"
 							+ photos[j].caption + "</font>.";
 					updatesArray.add(friendlikes);
 				}
 			}
 		}
 		for (int i = 0; i < friends.length; i++) {
 			Photo[] photos = friends[i].getPhotos();
 			Photo[] favPhotos = friends[i].getFavorites();
 			if(favPhotos.length > 0){
 				//String updates = "Friend: <font color=#3333FF>"
 				//		+ friends[i].username + "</font> favorited "
 				//		+ Integer.toString(favPhotos.length) + " photos.";
 				//updatesArray.add(updates);
 				/*
 				for(int j = 0; j < favPhotos.length; j++){
 					if( Arrays.asList(photoIds).contains(favPhotos[j].getId())){
 						String friendlikes = "Friend: <font color=#3333FF>"
 								+ friends[i].username + "</font> favorited your photo <font color=#3333FF>"
 								+ favPhotos[j].caption + "</font>.";
 						updatesArray.add(friendlikes);
 					}
 				}
 				*/
 				;
 			}
 			if (photos.length > 0) {
 				String updates = "Friend: <font color=#3333FF>"
 						+ friends[i].username + "</font> uploaded "
 						+ Integer.toString(photos.length) + " new photos.";
 				updatesArray.add(updates);
 			}
 		}
 		for (int i = 0; i < favorites.length; i++) {
 			String tmpString = "";
 			if (favorites[i].getComments().length > 0) {
 
 				tmpString = "Favorite photo: <font color=#3333FF>"
 						+ favorites[i].caption + "</font> has "
 						+ Integer.toString(favorites[i].getComments().length)
 						+ " new comments.";
 				updatesArray.add(tmpString);
 			}
 		}
 		String[] aUpdate = new String[updatesArray.size()];
 		updatesArray.toArray(aUpdate);
 		setUpdateArray(aUpdate);
 	}
 
 	public void setUpdateArray(String[] array) {
 		setListAdapter(new UpdateListAdapter(this,
 				android.R.layout.simple_list_item_1, array));
 	}
 
 	private class UpdateListAdapter extends ArrayAdapter<String> {
 		private Context context;
 		private int layoutResourceId;
 		private String[] data = null;
 		private LayoutInflater mLayoutInflater;
 
 		public UpdateListAdapter(Context context, int layoutResourceId,
 				String[] data) {
 			super(context, layoutResourceId, data);
 			this.context = context;
 			this.layoutResourceId = layoutResourceId;
 			this.data = data;
 			this.mLayoutInflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		@Override
 		public View getView(int position, View convertview, ViewGroup parent) {
 
 			View row = mLayoutInflater.inflate(
 					android.R.layout.simple_list_item_1, parent, false);
 
 			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 			row = inflater.inflate(layoutResourceId, parent, false);
 
 			String mElement = data[position];
 
 			TextView textview = (TextView) row.findViewById(android.R.id.text1);
 			textview.setText(Html.fromHtml(mElement));
 
 			return row;
 		}
 	}
 
 	public void home(View view) {
 		Intent intent = new Intent(this, HomeScreen.class);
 		startActivity(intent);
 	}
 
 	public void explore(View view) {
 		Intent intent = new Intent(this, Explore.class);
 		startActivity(intent);
 	}
 
 	public void camera(View view) {
 		Intent intent = new Intent(this, Camera.class);
 		startActivity(intent);
 	}
 
 	public void profile(View view) {
 		Intent intent = new Intent(this, Profile.class);
 		startActivity(intent);
 	}
 }
