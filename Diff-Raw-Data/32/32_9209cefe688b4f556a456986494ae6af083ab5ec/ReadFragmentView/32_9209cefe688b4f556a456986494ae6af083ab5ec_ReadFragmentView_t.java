 /*
 FragmentList Class for CreateYourOwnAdventure App.
 The view for the ReadFragmentActivity. Constructs the required view
 to display the media of the story fragment properly.
     
     License GPLv3: GNU GPL Version 3
     <http://gnu.org/licenses/gpl.html>.
     
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cmput301.f13t01.createyourownadventure;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 /**
  * @author Eddie Tai <eddie@ualberta.ca>
  * 
  *         The view for the ReadFragmentActivity. Constructs the required view
  *         to display the media of the story fragment properly.
  * 
  */
 public class ReadFragmentView extends ScrollView {
 
 	// declaration of variables
 	LinearLayout ll;
 	Context context;
 
 	/**
 	 * Constructor for the view. A LinearLayout is added to the ScrollView since
 	 * ScrollView only accepts 1 possible view, and we need to populate the view
 	 * with multiple views, depending on the story fragment and its content
 	 * media files.
 	 * 
 	 * @param context
 	 *            the activity that the view belongs to
 	 */
 	public ReadFragmentView(Context c) {
 		super(c);
 
 		// add LinearLayout to ScrollView since ScrollView only accepts 1 view
 		ll = new LinearLayout(context);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		this.addView(ll);
 
 		// all views within the ScrollView obviously shares the same context
 		context = c;
 
 	}
 
 	/**
 	 * Add a TextView with content of string s to the current view
 	 * 
 	 * @param s
 	 *            string to be displayed by the TextView
 	 * @param context
 	 *            the context of the view
 	 */
 	public void setTextView(String s) {
 		TextView tv = new TextView(context);
 		tv.setText("s");
 		ll.addView(tv);
 	}
 
 	/**
 	 * Incomplete ImageView handler
 	 */
 	public void setImageView() {
 		ImageView iv = new ImageView(context);
 		ll.addView(iv);
 	}
 
 	/**
 	 * Shows the list of choices using their given flavour text and sets the
 	 * manager for these choice options.
 	 * 
 	 * @param choice_text
 	 *            Arraylist of strings indicating the flavour text
 	 * @param onItemClickListener
 	 *            controller for the views
 	 */
 	public void setChoiceView(ArrayList<String> choice_text,
 			OnItemClickListener onItemClickListener) {
 
 		// populate the list with choices' flavour text
 		ListView lv = new ListView(context);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
 				R.layout.list_choices, choice_text);
 		lv.setAdapter(adapter);
 
 		// set the controller for these list items
 		lv.setOnItemClickListener(onItemClickListener);
 	}
 
 }
