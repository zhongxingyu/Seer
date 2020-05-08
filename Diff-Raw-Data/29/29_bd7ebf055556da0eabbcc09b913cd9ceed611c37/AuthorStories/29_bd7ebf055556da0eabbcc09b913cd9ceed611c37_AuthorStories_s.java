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
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.*;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.AccountService;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ILocalStoriesListener;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * 
  * View containing list of stories created by the author
  * 
  * TODO: Link with Model
  * 
  * @author James Finlay
  *
  */
 public class AuthorStories extends FragmentActivity implements ILocalStoriesListener {
 
 	private ListView _listView;
 	private RowArrayAdapter _adapter;
 	private Collection<Story> _stories;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.browse_authored);
 
 		_listView = (ListView) findViewById(R.id.list_view);
 		_listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 				// Get selected item
 				ListView listView = (ListView) parent;
 				Story item = (Story) listView.getItemAtPosition(position);
 
 				Locator.getAuthorController().selectStory(item.getId());
 
 				Intent intent = new Intent(AuthorStories.this, AuthorStoryDescription.class);
 				startActivity(intent);	
 			}
 		});
 		setUpView();
 	}
 	@Override
 	public void OnLocalStoriesChange(Map<UUID, Story> stories) {
 		_stories = Locator.getAuthorController().checkIfAuthored(stories.values());
 		setUpView();
 	}
 	@Override
 	public void onResume() {
 		Locator.getPresenter().Subscribe(this);
 		super.onResume();
 	}
 	@Override
 	public void onPause() {
 		Locator.getPresenter().Unsubscribe(this);
 		super.onPause();
 	}
 	private void setUpView() {
 		if (_listView == null) return;
 		if (_stories == null) return;
 
 
 		_adapter = new RowArrayAdapter(this, R.layout.listviewitem,
 				_stories.toArray(new Story[_stories.size()]));
 
 		_listView.setAdapter(_adapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 
 		getMenuInflater().inflate(R.menu.authorlist, menu);
 		return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.action_new:
 			Story story = Locator.getAuthorController().CreateStory();
 			Locator.getAuthorController().selectStory(story.getId());
 			story.setAuthor(AccountService.getUserName(getContentResolver()));
 			Locator.getAuthorController().saveStory();
 			Locator.getAuthorController().selectStory(story.getId());
 			
 			Intent intent = new Intent(AuthorStories.this, AuthorStoryDescription.class);
 			startActivityForResult(intent, 0);
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
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
 
 			Story item = values[position];
 			
 			/** Layout Items **/
 			ImageView thumbnail = (ImageView) rowView.findViewById(R.id.thumbnail);
 			TextView title = (TextView) rowView.findViewById(R.id.title);
 			TextView fragments = (TextView) rowView.findViewById(R.id.author);
 			TextView lastModified = (TextView) rowView.findViewById(R.id.datetime);
 			ImageView status = (ImageView) rowView.findViewById(R.id.status_icon);
 
 			thumbnail.setImageBitmap(item.decodeThumbnail());
 			title.setText(item.getTitle());
 			fragments.setText("Fragments: " + item.getFragmentIds().size());
 			lastModified.setText("Last Modified: " + item.getFormattedTimestamp());
 			
 			/*
 			 *  TODO: set status icon depending on server sync
 			 *  status.setImageResource(R.drawable.ic_action_cloud);
 			 *	status.setImageResource(R.drawable.ic_action_sync);
 			 */
 
 			return rowView;
 		}
 	}
 
 }
