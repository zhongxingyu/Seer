 package com.dephi.bosojowo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Supports the ListActivity to displays the ListViews
  */
 public class AdapterList extends BaseAdapter {
 	public enum AIM {
 		Category, Post, Detail,
 	}
 
 	private Activity mActivity;
 	private ArrayList<HashMap<String, String>> mData;
 	private LayoutInflater mInflater = null;
 	private AIM mAim;
 
 	public AdapterList(Activity activity, ArrayList<HashMap<String, String>> data, AIM aim) {
 		mActivity = activity;
 		mData = data;
 		mInflater = (LayoutInflater) mActivity
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		mAim = aim;
 	}
 
 	public int getCount() {
 		return mData.size();
 	}
 
 	public Object getItem(int position) {
 		return position;
 	}
	
	@Override
 	public long getItemId(int position) {
		return 0;
	}
	
	public int getItemDBid(int position) {
		String id = mData.get(position).get("_id");
		return Integer.parseInt(id);
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View vi = convertView;
 		HashMap<String, String> current = new HashMap<String, String>();
 		current = mData.get(position);
 
 		switch (mAim) {
 		case Category:
 			vi = mInflater.inflate(R.layout.list_category, null);
 			TextView categoryName = (TextView) vi
 					.findViewById(R.id.categoryName);
 
 			categoryName.setText(current.get("seekThis"));
 			break;
 		case Post:
 			vi = mInflater.inflate(R.layout.list_post, null);
 			arrangeItems(vi, current);
 			break;
 		case Detail:
 			vi = mInflater.inflate(R.layout.list_detail, null);
 			arrangeItems(vi, current);
 			break;
 		default:
 			break;
 		}
 		return vi;
 	}
 
 	// Filter Class
 	public void filter(ArrayList<HashMap<String, String>> resultDB) {
 		mData.clear();
 		Utilities.copyToNewList(resultDB, mData);
 		mAim = AIM.Post;
 		notifyDataSetChanged();
 	}
 
 	private static void arrangeItems(View vi, HashMap<String, String> current) {
 		ImageView img = (ImageView) vi.findViewById(R.id.picture);
 		TextView postOne = (TextView) vi.findViewById(R.id.postOne);
 		TextView postTwo = (TextView) vi.findViewById(R.id.postTwo);
 
 		postOne.setText(current.get("postOne"));
 		postTwo.setText(current.get("postTwo"));
 		String picture = current.get("picture");
 
 		if (picture.equals("dangu"))
 			img.setImageResource(R.drawable.dangu);
 		else if (picture.equals("ontong"))
 			img.setImageResource(R.drawable.ontong);
 		else if (picture.equals("hanacaraka"))
 			img.setImageResource(R.drawable.hanan);
 		else if (picture.equals("abimanyu"))
 			img.setImageResource(R.drawable.abimanyu);
 		else if (picture.equals("anoman"))
 			img.setImageResource(R.drawable.anoman);
 		else if (picture.equals("aswotomo"))
 			img.setImageResource(R.drawable.aswotomo);
 		else if (picture.equals("dursasana"))
 			img.setImageResource(R.drawable.dursasana);
 		else if (picture.equals("gatotkaca"))
 			img.setImageResource(R.drawable.gatotkaca);
 		else if (picture.equals("irawan"))
 			img.setImageResource(R.drawable.irawan);
 		else if (picture.equals("janaka"))
 			img.setImageResource(R.drawable.janaka);
 		else if (picture.equals("lesmana"))
 			img.setImageResource(R.drawable.lesmana);
 		else if (picture.equals("nakula"))
 			img.setImageResource(R.drawable.nakula);
 		else if (picture.equals("ontorejo"))
 			img.setImageResource(R.drawable.antareja);
 		else if (picture.equals("ontoseno"))
 			img.setImageResource(R.drawable.antasena);
 		else if (picture.equals("kartamarma"))
 			img.setImageResource(R.drawable.kartamarma);
 		else if (picture.equals("sadewa"))
 			img.setImageResource(R.drawable.sadewa);
 		else if (picture.equals("setyaka"))
 			img.setImageResource(R.drawable.setyaka);
 		else if (picture.equals("setyaki"))
 			img.setImageResource(R.drawable.setyaki);
 		else if (picture.equals("samba"))
 			img.setImageResource(R.drawable.samba);
 		else if (picture.equals("sengkuni"))
 			img.setImageResource(R.drawable.sengkuni);
 		else if (picture.equals("udawa"))
 			img.setImageResource(R.drawable.udawa);
 		else if (picture.equals("werkudara"))
 			img.setImageResource(R.drawable.werkudara);
 		else if (picture.equals("asem"))
 			img.setImageResource(R.drawable.asem);
 		else if (picture.equals("jambu"))
 			img.setImageResource(R.drawable.jambu);
 		else
 			img.setImageResource(R.drawable.default_pic);
 	}
 }
