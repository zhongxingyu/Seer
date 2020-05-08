 package watsons.wine;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 //import watsons.wine.WineListProduct.JsonTask;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.text.SpannableString;
 import android.text.style.ImageSpan;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import java.net.URL;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class WineAdapter extends ArrayAdapter<Wine> {
 
 	Context context;
 	int layoutResourceId;
 	ArrayList<Wine> data = null;
 	ImageSpan is;
 	LinearLayout ll;
 	ArrayList<LoadImageTask> tasks = null;
 
 	public WineAdapter(Context context, int layoutResourceId,
 			ArrayList<Wine> data) {
 		super(context, layoutResourceId, data);
 		this.layoutResourceId = layoutResourceId;
 		this.context = context;
 		this.data = data;
 		
 		this.tasks = new ArrayList<LoadImageTask>();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 		WineHolder holder = null;
 		
 		if (row == null) {
 			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 			row = inflater.inflate(layoutResourceId, parent, false);
 			holder = new WineHolder();
 			holder.llOri = (LinearLayout) row
 					.findViewById(R.id.list_product_item_ori_price_layout);
 			holder.llPro = (LinearLayout) row
 					.findViewById(R.id.list_product_item_pro_price_layout);
 			holder.llRat = (LinearLayout) row
 					.findViewById(R.id.list_product_item_rating_layout);
 			holder.llSize = (LinearLayout) row
 					.findViewById(R.id.list_product_item_size_layout);
 			holder.imgIcon = (ImageView) row
 					.findViewById(R.id.list_product_item_image);
 			holder.txtName = (TextView) row
 					.findViewById(R.id.list_product_item_name);
 			holder.txtSize = (TextView) row
 					.findViewById(R.id.list_product_item_size_value);
 			holder.txtOriPrice = (TextView) row
 					.findViewById(R.id.list_product_item_ori_price_value);
 			holder.txtProPrice = (TextView) row
 					.findViewById(R.id.list_product_item_pro_price_value);
 			holder.txtRatingRP = (TextView) row
 					.findViewById(R.id.list_product_item_rating_rp_value);
 			holder.txtRatingWS = (TextView) row
 					.findViewById(R.id.list_product_item_rating_ws_value);
 			holder.txtRatingJH = (TextView) row
 					.findViewById(R.id.list_product_item_rating_jh_value);
 			
 			row.setTag(holder);
 			
 		} else {
 			holder = (WineHolder) row.getTag();
 		}
 
 		Wine wine = data.get(position);
 		
 		//Edit By Stark //
 		if(wine.photo == null)
 		{
 			LoadImageTask task = (LoadImageTask) new LoadImageTask().execute(holder.imgIcon, wine);
 			tasks.add(task);
 			
			Log.d("Stark", "loading image task --- "+ tasks.size());
			
			if (tasks.size() > 20) {
				Log.d("Stark", "too much loading, remove the older one"); 
 				LoadImageTask tasktoquit = tasks.get(0);
 				tasktoquit.cancel(true);
				tasks.remove(0);
 			}
 		}
 		else {
 			holder.imgIcon.setImageBitmap(wine.photo);
 		}
 		//End //
 		
 		holder.txtName.setText(wine.name);
 		if (wine.size != null && wine.size != "")
 		{
 			holder.txtSize.setText(wine.size);
 			holder.txtSize.setVisibility(View.VISIBLE);
 			holder.llSize.setVisibility(View.VISIBLE);
 		}
 		else {
 			holder.txtSize.setVisibility(View.GONE);
 			holder.llSize.setVisibility(View.GONE);
 		}
 
 		if ((wine.original_price != null) && (wine.original_price != "null")) {
 			holder.llOri.setVisibility(View.VISIBLE);
 			holder.txtOriPrice.setVisibility(View.VISIBLE);
 			holder.txtOriPrice.setText("HK$" + wine.original_price);
 		}
 		else {
 			holder.llOri.setVisibility(View.GONE);
 			holder.txtOriPrice.setVisibility(View.GONE);
 		}
 		if ((wine.promote_price != null)
 				&& !(wine.promote_price.equals("null"))) {
 			holder.txtProPrice.setText("HK$" + wine.promote_price);
 			holder.llPro.setVisibility(View.VISIBLE);
 			holder.txtProPrice.setVisibility(View.VISIBLE);
 		} else {
 			holder.llPro.setVisibility(View.GONE);
 			holder.txtProPrice.setVisibility(View.GONE);
 		}
 		if ((wine.rating_rp == null && wine.rating_ws == null && wine.rating_jh == null)
 				|| (wine.rating_rp == "null" && wine.rating_ws == "null" && wine.rating_jh == "null")) {
 			holder.llRat.setVisibility(View.GONE);
 		} else {
 			holder.llRat.setVisibility(View.VISIBLE);
 			//Log.d("vk", wine.name + "; jh=" + wine.rating_jh + "; rp="
 			//		+ wine.rating_rp + "; ws=" + wine.rating_ws);
 			if (wine.rating_rp != null && wine.rating_rp != "null") {
 				SpannableString tmp = new SpannableString("tmp "
 						+ wine.rating_rp);
 				is = new ImageSpan(context, R.drawable.rating_rp);
 				tmp.setSpan(is, 0, 3, 0);
 				holder.txtRatingRP.setText(tmp);
 				holder.txtRatingRP.setVisibility(View.VISIBLE);
 			} else {
 				holder.txtRatingRP.setVisibility(View.GONE);
 			}
 			if (wine.rating_ws != null && wine.rating_ws != "null") {
 				SpannableString tmp = new SpannableString("tmp "
 						+ wine.rating_ws);
 				is = new ImageSpan(context, R.drawable.rating_ws);
 				tmp.setSpan(is, 0, 3, 0);
 				holder.txtRatingWS.setText(tmp);
 				holder.txtRatingWS.setVisibility(View.VISIBLE);
 			} else {
 				holder.txtRatingWS.setVisibility(View.GONE);
 			}
 			if (wine.rating_jh != null && wine.rating_jh != "null") {
 				SpannableString tmp = new SpannableString("tmp "
 						+ wine.rating_jh);
 				is = new ImageSpan(context, R.drawable.rating_jh);
 				tmp.setSpan(is, 0, 3, 0);
 				holder.txtRatingJH.setText(tmp);
 				holder.txtRatingJH.setVisibility(View.VISIBLE);
 			} else {
 				holder.txtRatingJH.setVisibility(View.GONE);
 			}
 		}
 		return row;
 	}
 	
 	private class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {
 	
 		private ImageView imv;
         private Wine wine;
         public Boolean quit;
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();  
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap result) {
 			super.onPostExecute(result);
 			
			Log.d("Stark", "removed = "+ tasks.remove(LoadImageTask.this));
			
 			if(result != null && imv != null){
                 //imv.setVisibility(View.VISIBLE);
                 imv.setImageBitmap(result);
             }else{
            	imv.setImageBitmap(null);
                 //imv.setVisibility(View.GONE);
             }
 		}
 
 		protected Bitmap doInBackground(Object... params) {
 			imv = (ImageView)   params[0];
 			wine = (Wine) params[1];
 			try {
 				String url = wine.url;
 				Bitmap photo = Wine.loadBitmap(url);
				//wine.photo = photo; //Don't cache the image inside the app ram, as it may be OOM
 				return photo;
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return null;
 		}
 
 	}
 
 	static class WineHolder {
 		LinearLayout llOri;
 		LinearLayout llPro;
 		LinearLayout llRat;
 		LinearLayout llSize;
 		ImageView imgIcon;
 		TextView txtName;
 		TextView txtSize;
 		TextView txtOriPrice;
 		TextView txtProPrice;
 		TextView txtRatingRP;
 		TextView txtRatingWS;
 		TextView txtRatingJH;
 	}
 }
