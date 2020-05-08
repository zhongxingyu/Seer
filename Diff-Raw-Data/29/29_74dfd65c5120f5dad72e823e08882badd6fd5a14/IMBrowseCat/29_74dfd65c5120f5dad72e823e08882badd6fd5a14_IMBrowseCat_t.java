 package nz.ac.otago.android;
 
 import java.util.ArrayList;
 
 import nz.ac.otago.util.IMConstants;
 import nz.ac.otago.util.IMDatabase;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Intent for Category browsing - Show all categories from data file
  * @author ngocminh
  */
 public class IMBrowseCat extends ListActivity {
 	CategoryAdapter myCategory;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.imbrowselist);
 		
 		myCategory = new CategoryAdapter(this);
 		if (myCategory.getCount() != 0) {
 			this.setListAdapter(myCategory);
 		} else {	/* Show a message that there's no category */
 			Toast toast = Toast.makeText(this, IMConstants.TOAST_NO_CAT, 
 													Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 40);
 			toast.show();
 		}
 	}
 	
 	@Override
     protected void onListItemClick(ListView parent, View v, int position, long id) {
 		String cat = myCategory.getItem(position);
 		
 		/* On list item click, change to Location Browsing with category name */ 
 		Intent locList = new Intent(this, IMBrowseLoc.class);
 		locList.putExtra("categoryName", cat);
 		startActivity(locList);
 	}
 	
 	/**
 	 * Extend BaseAdapter to create a list of category
 	 * @author ngocminh
 	 *
 	 */
 	private class CategoryAdapter extends BaseAdapter { 
 		private LayoutInflater mInflater;
 		private ArrayList<String> categories;
 		
 		public CategoryAdapter(Context context) {
 			mInflater = LayoutInflater.from(context); 
 			categories = new ArrayList<String>(); 
 			
 			IMDatabase db = new IMDatabase(context);
 			db.open();
 			categories = db.getCategories();
			if (!categories.isEmpty()) {
				categories.add(0, IMConstants.ALL_LOCATION);
			}
 			db.close();
 		}
 		
 		@Override
 		public int getCount() {
 			return categories.size();
 		} 
 		
 		public String getItem(int i) {
 			return categories.get(i);
 		} 
 		
 		public long getItemId(int i) {
 			return i;
 		} 
 		
 		public View getView(int index, View convertView, ViewGroup parent) {
 			final ViewHolder holder; 
 			View view = convertView;
 			
 			if ((view == null) || (view.getTag() == null)) {
 				view = mInflater.inflate(R.layout.imbrowsecatrow, null); 
 				holder = new ViewHolder(); 
 				holder.mName = (TextView)view.findViewById(R.id.name); 
 				view.setTag(holder);
 			} else {
 				holder = (ViewHolder) view.getTag();
 			}
 			
 			holder.mName.setText(getItem(index)); 
 			view.setTag(holder); 
 			return view;
 		}
 		
 		public class ViewHolder {
 			TextView mName;
 		}
 	}
 }
