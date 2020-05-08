 /**
  *
  */
 package org.AndroidShareApp.gui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.AndroidShareApp.R;
 import org.AndroidShareApp.core.NetworkManager;
 import org.AndroidShareApp.core.Person;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * @author jonaias
  * 
  */
 public class PersonListActivity extends ListActivity {
 
 	private EfficientAdapter adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.shared_with_me_list_activity);
 
 		adapter = new EfficientAdapter(this);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 	}
 
 	public static class EfficientAdapter extends BaseAdapter implements
 			Filterable {
 		private LayoutInflater mInflater;
 		private ArrayList<Person> mPersonList;
 		private PersonListActivity mPersonListActivity;
 
 		public EfficientAdapter(PersonListActivity personListActivity) {
 			// Cache the LayoutInflate to avoid asking for a new one each time.
 			Context context = personListActivity.getBaseContext();
 			mInflater = LayoutInflater.from(context);
 			mPersonListActivity = personListActivity;
 			mPersonList = NetworkManager.getInstance().getPersonList();
			//Collections.sort(mPersonList);
 		}
 
 		/**
 		 * Make a view to hold each row.
 		 * 
 		 * @see android.widget.ListAdapter#getView(int, android.view.View,
 		 *      android.view.ViewGroup)
 		 */
 		public View getView(final int position, View convertView,
 				ViewGroup parent) {
 
 			// A ViewHolder keeps references to children views to avoid
 			// unnecessary calls
 			// to findViewById() on each row.
 			ViewHolder holder;
 
 			// When convertView is not null, we can reuse it directly, there is
 			// no need to reinflate it. We only inflate a new View when the
 			// convertView
 			// supplied by ListView is null.
 			if (convertView == null) {
 				convertView = mInflater.inflate(
 						R.layout.shared_with_me_list_item, null);
 
 				// Creates a ViewHolder and store references to the two children
 				// views
 				// we want to bind data to.
 				holder = new ViewHolder();
 				holder.textLine = (TextView) convertView
 						.findViewById(R.id.personListItemTextView);
 
 				convertView.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						/*Toast.makeText(mContext, "Got click on person "
 								+ mPersonList.get(position).getName(),
 								Toast.LENGTH_SHORT);*/
 						Intent intent = mPersonListActivity.getIntent();
 						intent.putExtra("selectedPerson", mPersonList.get(position).getDeviceID());
 						mPersonListActivity.setResult(Activity.RESULT_OK, intent);
 						mPersonListActivity.finish();
 					}
 				});
 
 				convertView.setTag(holder);
 			} else {
 				// Get the ViewHolder back to get fast access to the TextView
 				// and the ImageView.
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			// Bind the data efficiently with the holder.
 			synchronized (mPersonList) {
 				holder.textLine.setText(mPersonList.get(position).getName());
 			}
 
 			return convertView;
 		}
 
 		static class ViewHolder {
 			TextView textLine;
 		}
 
 		@Override
 		public Filter getFilter() {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public int getCount() {
 			int ret;
 			synchronized (mPersonList) {
 				ret = mPersonList.size();
 			}
 			return ret;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			Object ret;
 			synchronized (mPersonList) {
 				ret = mPersonList.get(position);
 			}
 			return ret;
 		}
 
 	}
 
 }
