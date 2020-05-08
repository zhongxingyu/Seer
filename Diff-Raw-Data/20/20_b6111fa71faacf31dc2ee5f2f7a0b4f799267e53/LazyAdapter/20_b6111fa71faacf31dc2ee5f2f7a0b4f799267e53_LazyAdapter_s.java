 package net.redlinesoft.app.iampetdo;
 
 import java.util.ArrayList;
 
 import java.util.HashMap;
  
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public  class LazyAdapter extends BaseAdapter {
 	
 	private static ArrayList<HashMap<String, String>> MyArrList = null;
 	private Activity activity;	 
     private static LayoutInflater inflater=null;
     
     
     public ImageLoader imageLoader; 
     
     public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> myArrList) {
         activity = a;
         MyArrList=myArrList;
         inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         imageLoader=new ImageLoader(activity.getApplicationContext()); 
     }
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return MyArrList.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 		View vi=convertView;
         if(convertView==null)
             vi = inflater.inflate(R.layout.activity_column, null);
         
         HashMap<String, String> item = new HashMap<String, String>();
         item = MyArrList.get(position);
         
         TextView title = (TextView)vi.findViewById(R.id.ColTitle);
         ImageView thumb_image=(ImageView)vi.findViewById(R.id.ColImage);
         
         title.setText(item.get("ImageTitle"));       
         String strPath = item.get("ImagePath");
         String[] strThumb= strPath.split(",");
         
         
         imageLoader.DisplayImage(strThumb[0],position,thumb_image);
         
  
         return vi;
 	}
 	
 }	
 	
