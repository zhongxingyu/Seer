 /*
  *	Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * 	Evan DeGraff
  *
  * 	Permission is hereby granted, free of charge, to any person obtaining a copy of
  * 	this software and associated documentation files (the "Software"), to deal in
  * 	the Software without restriction, including without limitation the rights to
  * 	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * 	the Software, and to permit persons to whom the Software is furnished to do so,
  * 	subject to the following conditions:
  *
  * 	The above copyright notice and this permission notice shall be included in all
  * 	copies or substantial portions of the Software.
  *
  * 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * 	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * 	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * 	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * 	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.view;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.Bookmark;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IBookmarkListListener;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IStoryListListener;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * View holding list of bookmarks. Accessed from MainView
  * 
  * TODO: Load from model
  * 
  * @author James Finlay
  *
  */
 public class ContinueView extends Activity implements IBookmarkListListener,
 														IStoryListListener {
 	private static final String TAG = "ContinueView";
 
 	private ListView _listView;
 	private RowArrayAdapter _adapter;
 	
 	private List<Bookmark> _bookmarks;
 	private List<Story> _stories;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_view);
 
 		_listView = (ListView) findViewById(R.id.list_view);
 		_listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 				// Get selected item
 				ListView listView = (ListView) parent;
 				Story item = (Story) listView.getItemAtPosition(position);
 				
 				// TODO: Send fragment info to controller
 				
 				Intent intent = new Intent(ContinueView.this, FragmentView.class);
 				startActivity(intent);
 			}
 		});
 	}
 	public void OnBookmarkListChange(Collection<Bookmark> newBookmarks) {
 		_bookmarks = (List<Bookmark>) newBookmarks;
 		setUpView();
 	}
 	public void OnCurrentStoryListChange(Collection<Story> newStories) {
 		_stories = (List<Story>) newStories;
 		setUpView();
 	}
 	private void setUpView() {
 		if (_bookmarks == null) return;
 		if (_stories == null) return;
 		
 		HashMap<String, Story> hStories = new HashMap<String, Story>();
 		for (Story story : _stories)
 			hStories.put(story.getId(), story);
 		
 		List<Story> relevants = new ArrayList<Story>();
 		for (Bookmark bookmark : _bookmarks) {
 			if (hStories.containsKey(bookmark.getStoryID()))
 				relevants.add(hStories.get(bookmark.getStoryID()));
 		}
		
 		_adapter = new RowArrayAdapter(this, R.layout.listviewitem, 
 				relevants.toArray(new Story[relevants.size()]));
 		_listView.setAdapter(_adapter);
 	}
 	@Override
 	public void onResume() {
 		Locator.getPresenter().Subscribe((IBookmarkListListener)this);
 		Locator.getPresenter().Subscribe((IStoryListListener)this);
 		super.onResume();
 	}
 	@Override
 	public void onPause() {
 		Locator.getPresenter().Unsubscribe((IBookmarkListListener)this);
 		Locator.getPresenter().Unsubscribe((IStoryListListener)this);
 		super.onPause();
 	}
 
 	private class RowArrayAdapter extends ArrayAdapter<Story> {
 
 		private Context context;
 		private int layoutResourceID;
 		private Story[] values;
 
 		public RowArrayAdapter(Context context, int layoutResourceID, Story[] values) {
 			super(context, layoutResourceID, values);
 
 			this.context = context;
 			this.layoutResourceID = layoutResourceID;
 			this.values = values;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			View rowView = inflater.inflate(R.layout.listviewitem, parent, false);
 
 			ImageView thumbnail = (ImageView) rowView.findViewById(R.id.thumbnail);
 			TextView title = (TextView) rowView.findViewById(R.id.title);
 			TextView author = (TextView) rowView.findViewById(R.id.author);
 			TextView lastPlayed = (TextView) rowView.findViewById(R.id.datetime);
 
 			// TODO: fill out views from values[position]
 
 			
 			return rowView;
 		}
 	}
 }
