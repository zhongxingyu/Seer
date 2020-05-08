 package com.cs301w01.meatload.adapters;
 
 import java.util.List;
 
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.model.Tag;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * Adapter for populating a ListView with tag names.
  * @author Blake Bouchard
  * @author Jose C. Gomez
  * @see <a href="http://www.josecgomez.com/2010/05/03/android-putting-custom-objects-in-listview/">
  http://www.josecgomez.com/2010/05/03/android-putting-custom-objects-in-listview/</a>
  */
 public class TagAdapter extends ArrayAdapter<Tag> {
 
 	int resource;
 	String response;
 	Context context;
 	public TagAdapter(Context context, int textViewResourceId, List<Tag> tags) {
 		super(context, textViewResourceId, tags);
 		this.resource = textViewResourceId;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
         LinearLayout tagListItem;
         // Get current tag object
         Tag tag = getItem(position);
  
         // Inflate the view
         if (convertView == null) {
             tagListItem = new LinearLayout(getContext());
             String inflater = Context.LAYOUT_INFLATER_SERVICE;
             LayoutInflater vi;
             vi = (LayoutInflater)getContext().getSystemService(inflater);
             vi.inflate(resource, tagListItem, true);
         } else {
             tagListItem = (LinearLayout) convertView;
         }
         
         // Get the text boxes from the list_item.xml file
        TextView tagName = (TextView) tagListItem.findViewById(R.id.tag_list_item);
  
         // Assign the appropriate data from tag object
         tagName.setText(tag.getName());
  
         return tagListItem;
     }
 }
