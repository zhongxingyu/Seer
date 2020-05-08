 package coupling.app;
 
 import java.util.Date;
 
 import com.nit.coupling.R;
 
 import coupling.app.BL.BLShopListOverview;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 
 public class AdapterShopListOverview extends CursorAdapter {
 
 	 private LayoutInflater mLayoutInflater;
 	 private Context mContext;
 	
 	 private BLShopListOverview blShopListOverview;
 	 
 	public AdapterShopListOverview(Context context, BLShopListOverview blShopListOverview) {
 		super(context, blShopListOverview.getSource(), true);
 		 mContext = context;
 	     mLayoutInflater = LayoutInflater.from(context);
 	     this.blShopListOverview = blShopListOverview;
 	}
 
 	@Override
 	public void bindView(View row, Context context, Cursor cursor) {
 		//Retrieve data from database
 		long id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
 		Long Uid = cursor.getLong(cursor.getColumnIndex("UId"));
 		String title =  cursor.getString(cursor.getColumnIndexOrThrow("Title"));
 		int totalItems = cursor.getInt(cursor.getColumnIndexOrThrow("TotalItems"));
 		Date createdAt = Utils.toDate(cursor.getString(cursor.getColumnIndexOrThrow("CreatedAt")));
 		boolean isMine = (cursor.getInt(cursor.getColumnIndexOrThrow("IsMine")) == 1);
 		
		if(!isMine)
			row.setBackgroundColor(Color.YELLOW);
 		
 		TextView tvTitle = (TextView) row.findViewById(R.id.tvTitle);
 		tvTitle.setText(title);
 		
 		Button removeList = (Button) row.findViewById(R.id.btnRemoveList);
 		Ids ids = new Ids(id, Uid);
 		removeList.setTag(ids);
 		removeList.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				boolean isSucceed =blShopListOverview.deleteItem((Ids)v.getTag());
 				if(!isSucceed)
 					Log.e("adaptor_shoplistOverview", "failed to delete List: " + ((Ids)v.getTag()).getDBId());
 				refreshList();
 			}
 		});
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		 View v = mLayoutInflater.inflate(R.layout.shoplist_row, parent, false);
 	     return v;
 	}
 	
 	public void refreshList(){
 		swapCursor(blShopListOverview.getSource());
 		notifyDataSetChanged();
 	}
 }
