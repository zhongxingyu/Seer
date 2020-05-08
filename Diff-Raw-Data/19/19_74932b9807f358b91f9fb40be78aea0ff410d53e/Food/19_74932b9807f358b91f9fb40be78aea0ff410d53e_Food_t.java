 package com.saltlakegreekfestival.utahgreekfestival;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class Food extends SherlockFragment implements OnItemClickListener {
 	
 	ArrayList<FoodItem> foodlist = null;
 
 	// references to our images
 	/*
 	private Integer[] mThumbIds = { R.drawable.gyro,
 			R.drawable.souvlaki, R.drawable.keftethes, R.drawable.pilafi,
 			R.drawable.fasolakia, R.drawable.pastitsio, R.drawable.spanokopites,
 			R.drawable.tiropita, R.drawable.dolmathes, R.drawable.salad, R.drawable.feta, R.drawable.olives
 			};
 	*/
 	
 			
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		try {
 			foodlist = buildfoodinfo();
 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		View v = inflater.inflate(R.layout.foodgrid,container,false);
 		GridView gridview = (GridView) v.findViewById(R.id.gridview);
 		gridview.setAdapter(new ImageAdapter(getSherlockActivity()));
 		
 		gridview.setOnItemClickListener(this);
 		return v;
 	}
 	
 	private ArrayList<FoodItem> buildfoodinfo()
 			throws XmlPullParserException, IOException {
 		ArrayList<FoodItem> foodinfo = new ArrayList<FoodItem>();
 		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		XmlPullParser xpp = factory.newPullParser();
 		InputStream is = getResources().openRawResource(R.raw.food);
 		xpp.setInput(is, "utf-8");
 		// check state
 		int eventType;
 		int arrayNum = 0;
 		String tagName = "";
 		eventType = xpp.getEventType();
 		while (eventType != XmlPullParser.END_DOCUMENT) {
 			// instead of the following if/else if lines
 			// you should custom parse your xml
 			if (eventType == XmlPullParser.START_DOCUMENT) {
 				Log.d("SCHED", "Start document");
 			} else if (eventType == XmlPullParser.START_TAG) {
 				tagName = xpp.getName();
 				Log.d("SCHED", "Start tag " + tagName);
 				if (tagName.equals("event")) {
 					FoodItem fi = new FoodItem();
 					fi.setName(xpp.getAttributeValue(null, "name"));
 					fi.setImg(this.getSherlockActivity().getResources().getIdentifier(fi.getName().toLowerCase(), "drawable", getSherlockActivity().getPackageName()));
 					fi.setPrice(xpp.getAttributeValue(null, "price"));
 					fi.setLocation(xpp.getAttributeValue(null, "location"));
 					fi.setDescription(xpp.getAttributeValue(null, "description"));
 					foodinfo.add(fi);
 				}
 			} else if (eventType == XmlPullParser.END_TAG) {
 				Log.d("SCHED", "End tag " + xpp.getName());
 			} else if (eventType == XmlPullParser.TEXT) {
 				Log.d("SCHED", "Text " + xpp.getText());
 			}
 			eventType = xpp.next();
 		}
 		return foodinfo;
 	}
 	
 	
 
 	public class ImageAdapter extends BaseAdapter {
 		private Context mContext;
 
 		public ImageAdapter(Context c) {
 			mContext = c;
 		}
 
 		public int getCount() {
 			return foodlist.size();
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		// create a new ImageView for each item referenced by the Adapter
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ImageView imageView;
 			if (convertView == null) { // if it's not recycled, initialize some
 										// attributes
 				
 				final float scale = getSherlockActivity().getResources().getDisplayMetrics().density;
 				int pixels = (int) (95 * scale + 0.5f);
 				imageView = new ImageView(mContext);
 				imageView.setLayoutParams(new GridView.LayoutParams(pixels, pixels));
 				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 				imageView.setPadding(8, 8, 8, 8);
 			} else {
 				imageView = (ImageView) convertView;
 			}
 
 			imageView.setImageResource(foodlist.get(position).getImg());
 			return imageView;
 			
 			
 		
 			
 		}
 
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		AlertDialog.Builder ad = new AlertDialog.Builder(getSherlockActivity());
 		LayoutInflater i = getSherlockActivity().getLayoutInflater();
 		View vw = i.inflate(R.layout.foodinfo,null);
 		TextView name = (TextView)vw.findViewById(R.id.foodname);
 		TextView price = (TextView)vw.findViewById(R.id.price);
 		TextView loc = (TextView)vw.findViewById(R.id.location);
 		TextView desc = (TextView)vw.findViewById(R.id.description);
 		ImageView img = (ImageView)vw.findViewById(R.id.foodimage);
 		name.setText(foodlist.get(position).getName());
 		price.setText(foodlist.get(position).getprice());
 		loc.setText(foodlist.get(position).getLocation());
 		img.setImageResource(foodlist.get(position).getImg());
 		ad.setView(vw);
 		
 		ad.setNegativeButton("CLOSE ME",
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog,
                             int which) {
                         dialog.cancel();
                     }
                 });
		//Button close = (Button)vw.findViewById(R.id.foodclose);
 		
 		ad.show();
 		
		//close.setOnClickListener(new OnClickListener()
        //{
          //  @Override
            //public void onClick(View v)
            //{
              // Toast.makeText(getActivity(), "Go Away,  I don't work",Toast.LENGTH_LONG);
            //}
       // });
 		
 	}
 	
 	
 	private class FoodItem {
 
 		String name;
 		String price;
 		String location;
 		String description;
 		int img;
 
 		public FoodItem(){
 			
 		}
 		
 		public FoodItem(String name) {
 			this.name = name;
 		}
 
 		public void setName(String name){
 			this.name = name;
 		}
 		public String getName() {
 			return name;
 		}
 		
 		public void setPrice(String price){
 			this.price = price;
 		}
 		
 		public String getprice() {
 			return price;
 		}
 		
 		public void setLocation(String location){
 			this.location = location;
 		}
 		
 		public String getLocation(){
 			return location;
 		}
 		
 		public void setDescription(String description){
 			this.description = description;
 					
 		}
 		
 		public String getDescription(){
 			return description;
 		}
 		
 		public void setImg(int img){
 			this.img = img;
 		}
 		
 		public int getImg(){
 			return img;
 		}
 	}
 }
