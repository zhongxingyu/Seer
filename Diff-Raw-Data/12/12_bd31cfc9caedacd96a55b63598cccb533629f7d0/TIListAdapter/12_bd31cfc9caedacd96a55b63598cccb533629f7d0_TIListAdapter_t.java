 package ifm9.utils;
 
 import java.util.List;
 
 import ifm9.items.TI;
 import ifm9.main.R;
 import ifm9.main.TNActv;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class TIListAdapter extends ArrayAdapter<TI> {
 
 	/*--------------------------------------------------------
 	 * Class fields
 		--------------------------------------------------------*/
 	// Context
 	Context con;
 
 	// Inflater
 	LayoutInflater inflater;
 
 	//
 	Methods.MoveMode moveMode = null;
 //	Methods.MoveMode moveMode = Methods.MoveMode.OFF;
 	
 	/*--------------------------------------------------------
 	 * Constructor
 		--------------------------------------------------------*/
 	//
 	public TIListAdapter(Context con, int resourceId, List<TI> items) {
 		// Super
 		super(con, resourceId, items);
 
 		// Context
 		this.con = con;
 
 		// Inflater
 		inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 
 	}//public TIListAdapter(Context con, int resourceId, List<TI> items)
 
 
 	public TIListAdapter(Context con, int resourceId, List<TI> items, 
 											Methods.MoveMode moveMode) {
 		// Super
 		super(con, resourceId, items);
 
 		// Context
 		this.con = con;
 		this.moveMode = moveMode;
 
 		// Inflater
 		inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 
 	}//public TIListAdapter(Context con, int resourceId, List<TI> items, Methods.MoveMode moveMode)
 
 	/*--------------------------------------------------------
 	 * Methods
 		--------------------------------------------------------*/
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
     	/*----------------------------
 		 * Steps
 		 * 0. View
 		 * 1. Set layout
 		 * 2. Get view
 		 * 3. Get item
 		 * 4. Get bitmap
 		 * 5. Get memo, or, file name
 			----------------------------*/
     	// Log
 		Log.d("TIListAdapter.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "getView");
 		
     	
     	/*----------------------------
 		 * 0. View
 			----------------------------*/
     	View v;
 
     	/*----------------------------
 		 * 1. Set layout
 			----------------------------*/
 		
     	if (convertView != null) {
 			v = convertView;
 		} else {//if (convertView != null)
 //			v = inflater.inflate(R.layout.list_row, null);
 			v = inflater.inflate(R.layout.list_row, null);
 		}//if (convertView != null)
 
     	/*----------------------------
 		 * 2. Get view
 			----------------------------*/
     	ImageView iv = (ImageView) v.findViewById(R.id.iv_thumbnail);
 
     	/*----------------------------
 		 * 3. Get item
 			----------------------------*/
     	TI ti = (TI) getItem(position);
 
     	/*----------------------------
 		 * 4. Get bitmap
 			----------------------------*/
     	// ContentResolver
     	ContentResolver cr = con.getContentResolver();
     	
     	// Bitmap
     	Bitmap bmp = 
 				MediaStore.Images.Thumbnails.getThumbnail(
 							cr, ti.getFileId(), MediaStore.Images.Thumbnails.MICRO_KIND, null);
     	
     	// Set bitmap
     	iv.setImageBitmap(bmp);
 
     	/*----------------------------
 		 * 5. Get memo, or, file name
 		 * 		1. File name
 		 * 		2. Memo
 			----------------------------*/
 		TextView tv = (TextView) v.findViewById(R.id.textView1);
 		
 		tv.setText(ti.getFile_name());
 
 		/*----------------------------
 		 * 1.5.2. Memo
 			----------------------------*/
 		TextView tv_memo = (TextView) v.findViewById(R.id.textView2);
 		
 		tv_memo.setTextColor(Color.BLACK);
 		tv_memo.setBackgroundColor(Color.WHITE);
 		
 		String memo = ti.getMemo();
 		
 		if (memo != null) {
 			tv_memo.setText(memo);
 			
 		} else {//if (memo)
 			
 			tv_memo.setText("");
 			
 		}//if (memo)
 		
 		// Log
 		Log.d("TIListAdapter.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "File name, memo => Set");
 		
 		
//    	return null;
		return v;
     }//public View getView(int position, View convertView, ViewGroup parent)
 
 }//public class TIListAdapter extends ArrayAdapter<TI>
