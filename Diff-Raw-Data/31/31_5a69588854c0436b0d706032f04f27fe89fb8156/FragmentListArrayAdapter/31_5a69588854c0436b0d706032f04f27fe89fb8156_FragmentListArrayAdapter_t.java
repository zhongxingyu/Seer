 package ualberta.g12.adventurecreator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.util.List;
 
 
 public class FragmentListArrayAdapter extends ArrayAdapter<Fragment> {
 	  private final Context context;
 
 	    private static final boolean DEBUG_LOG = true;
 	    private static final String TAG = "FragmentListArrayAdapter";
 
 	    public FragmentListArrayAdapter(Context context, int id, List<Fragment> list) {
 	        super(context, id, list);
 	        this.context = context;
 	        if (DEBUG_LOG)
 	            Log.d(TAG, "Yo creating a fragmentList adapter yos?");
 	    }
 
 	    private class ViewHolder {
 	        TextView title;
 	    }
 
 	    @Override
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	        ViewHolder holder = null;
 	        Fragment fragment = getItem(position);
 
 	        LayoutInflater mInflater = (LayoutInflater) context
 	                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
 	        if (convertView == null) {
 	            convertView = mInflater.inflate(R.layout.listview_fragment_list, null);
 	            holder = new ViewHolder();
 	            holder.title = (TextView) convertView.findViewById(R.id.list_fragment_title);
	            Log.d("the title is" , holder.title.getText().toString());
 	            convertView.setTag(holder);
 	        } else {
 	            holder = (ViewHolder) convertView.getTag();
 	        }
 
 	        holder.title.setText(fragment.getTitle());
 
 	        return convertView;
 	    }
 }
