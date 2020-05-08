 package com.papagiannis.tuberun.binders;
 
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.SimpleAdapter.ViewBinder;
 import android.widget.TextView;
 
 import com.ericharlow.DragNDrop.DragNDropListView;
 import com.papagiannis.tuberun.FavoritesActivity;
 import com.papagiannis.tuberun.LinePresentation;
 import com.papagiannis.tuberun.LineType;
 import com.papagiannis.tuberun.R;
 import com.papagiannis.tuberun.favorites.Favorite;
 import com.papagiannis.tuberun.fetchers.DeparturesDLRFetcher;
 
 public class FavoritesBinder implements ViewBinder, OnClickListener {
 
 	private final FavoritesActivity activity;
 	private final DragNDropListView listView;
 	private final int red;
 	private final int grey;
 	
 	public FavoritesBinder (FavoritesActivity activity, DragNDropListView listView) {
 		super();
 		grey=activity.getResources().getColor(R.drawable.tuberun_grey_darker);
 		red=activity.getResources().getColor(R.drawable.tuberun_red_bright);
 		this.activity=activity;
 		this.listView=listView;
 	}
 	
 	LineType last_lt;
 	
 	@Override
 	public boolean setViewValue(View view, Object o, String s) {
 		final int id=view.getId();
 		Class c= view.getClass();
 		String sss=c.toString();
 		switch (id) {
		case R.id.linee_favorites:
 			last_lt=LinePresentation.getLineTypeRespresentation(s);
 			view.setBackgroundColor(LinePresentation.getBackgroundColor(last_lt));
 			return true;
 		case R.id.icon_favorites:
 			ImageView iv=(ImageView) view;
 			int icon=(Integer) o;
			if (icon==R.drawable.buses) icon=R.drawable.buses_inverted;
 			Bitmap bmp = BitmapFactory.decodeResource(
 					activity.getResources(), icon);
 			Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp,
 					bmp.getWidth() / 2, bmp.getHeight() / 2, true);
 			iv.setImageBitmap(resizedbitmap);
 			view.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					listView.setIsMoving(true);
 				}
 			});
 			return true;
 		case R.id.remove_favorite:
 			view.setOnClickListener(this);
 			view_favorite_indexes.put(view, Integer.parseInt(s));
 			return true;
 		}
 		
 		TextView tv = (TextView) view;
 		if (id==R.id.platform_favorites) {
 			tv.setVisibility(View.VISIBLE);
 			tv.setTextColor(red);
 		}
 		else if (s.equals("") || s.equals(DeparturesDLRFetcher.none_msg)) {
 			tv.setVisibility(View.GONE);
 		} else {
 			tv.setVisibility(View.VISIBLE);
 		}
 		return false; // continue with the text
 	}
 
 	private HashMap<View,Integer> view_favorite_indexes=new HashMap<View, Integer>();
 	int delete_index;
 	
 	@Override
 	public void onClick(View v) {
 		delete_index=view_favorite_indexes.get(v);
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		builder.setMessage("Are you sure you want to delete this favorite?")
 		       .setCancelable(false)
 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	    view_favorite_indexes.clear();
 		       			Favorite.removeIndex(delete_index, activity);
 		       			activity.create();
 		           }
 		       })
 		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       });
 		AlertDialog alert = builder.create();
 		alert.show();
 		
 	}
 }
