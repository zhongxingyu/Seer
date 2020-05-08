 package helper;
 
 import java.util.ArrayList;
 
 import my.catalog.R;
 import resource.Category;
 import resource.Item;
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class LazyAdapter extends BaseAdapter {
 
 	private Activity activity;
 	private ArrayList<Object> list;
 	private static LayoutInflater inflater = null;
 
 	public LazyAdapter(Activity a, ArrayList<Object> list) {
 		activity = a;
 		this.list = list;
 		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public int getCount() {
 		return list.size();
 	}
 
 	public Object getItem(int position) {
 		return list.get(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View vi = convertView;
 		if (convertView == null)
 			vi = inflater.inflate(R.layout.item_row, null);
 
 		TextView title = (TextView) vi.findViewById(R.id.title);
 		TextView subtitle = (TextView) vi.findViewById(R.id.subtitle);
 		ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);
 
 		Object obj = list.get(position);
 
 		if (obj instanceof Item) {
 			Item item = (Item) obj;
 			
 			title.setText(item.get_name());
 			subtitle.setText(item.getShortDescription());
 			thumb_image.setImageBitmap(item.getThumbPhoto(activity.getApplicationContext()));
 		} else if (obj instanceof Category) {
 			Category cat = (Category) obj;
 			
 			title.setText(cat.get_name());
			subtitle.setText(cat.getAllItems().size() +" produto(s).");
 			thumb_image.setImageBitmap(cat.getPhoto(activity.getApplicationContext()));
 		}
 		return vi;
 	}
 }
