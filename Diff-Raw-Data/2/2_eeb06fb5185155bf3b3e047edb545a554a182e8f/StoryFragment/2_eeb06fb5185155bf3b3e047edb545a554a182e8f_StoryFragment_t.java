 package th.in.llun.thorfun;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import th.in.llun.thorfun.api.CategoryStory;
 import th.in.llun.thorfun.api.RemoteCollection;
 import th.in.llun.thorfun.api.Thorfun;
 import th.in.llun.thorfun.api.ThorfunResult;
 import th.in.llun.thorfun.utils.ImageLoader;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class StoryFragment extends Fragment {
 
 	private Thorfun thorfun;
 	private StoryAdapter adapter;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	    Bundle savedInstanceState) {
 		thorfun = Thorfun.getInstance();
 		adapter = new StoryAdapter(getActivity(),
 		    getLayoutInflater(savedInstanceState));
 
 		View rootView = inflater.inflate(R.layout.fragment_story, container, false);
 		GridView grid = (GridView) rootView.findViewById(R.id.story_grid);
 		grid.setAdapter(adapter);
 
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		final Activity activity = getActivity();
 		final RelativeLayout layout = (RelativeLayout) activity
 		    .findViewById(R.id.story_loading);
 
 		Log.d(Thorfun.LOG_TAG, "Load story");
 		thorfun.loadStory(null,
 		    new ThorfunResult<RemoteCollection<CategoryStory>>() {
 
 			    @Override
 			    public void onResponse(RemoteCollection<CategoryStory> response) {
 				    final List<CategoryStory> stories = response.collection();
 				    activity.runOnUiThread(new Runnable() {
 
 					    @Override
 					    public void run() {
 						    layout.setVisibility(View.GONE);
 						    adapter.setStories(stories);
 					    }
 				    });
 
 			    }
 
 		    });
 	}
 
 	private static class StoryAdapter extends BaseAdapter {
 
 		private List<CategoryStory> stories = new ArrayList<CategoryStory>(0);
 		private LayoutInflater inflater = null;
 		private Activity activity = null;
 
 		private boolean isLoading = false;
 
 		public StoryAdapter(Activity activity, LayoutInflater inflater) {
 			this.activity = activity;
 			this.inflater = inflater;
 		}
 
 		public void setStories(List<CategoryStory> stories) {
 			this.stories = stories;
 			this.notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			if (stories.size() > 0) {
 				return stories.size() + 2;
 			}
 			return 0;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return stories.get(position - 1);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, final ViewGroup parent) {
 			if (position == 0) {
 				View view = convertView;
 				if (view == null) {
 					view = new View(parent.getContext());
 				}
 				return view;
 			}
 			// Load next page
 			else if (position == getCount() - 1) {
 				RelativeLayout row = (RelativeLayout) convertView;
 				if (row == null) {
 					row = (RelativeLayout) inflater.inflate(
 					    R.layout.fragment_loading_row, parent, false);
 				}
 
 				if (!isLoading) {
 					CategoryStory story = stories.get(stories.size() - 1);
 					Log.d(Thorfun.LOG_TAG, story.getID());
 				}
 
 				return row;
 			}
 			// Normal row
 			else {
 				Log.d(Thorfun.LOG_TAG, "Position: " + position);
 				RelativeLayout row = (RelativeLayout) convertView;
 				if (row == null) {
 					row = (RelativeLayout) inflater.inflate(R.layout.fragment_story_row,
 					    parent, false);
 				}
 
 				CategoryStory story = stories.get(position - 1);
 
 				ImageView icon = (ImageView) row.findViewById(R.id.story_row_icon);
 				ViewGroup loading = (ViewGroup) row
 				    .findViewById(R.id.story_row_icon_progress);
 				loading.setVisibility(View.VISIBLE);
 				new ImageLoader(icon, loading).execute(story.getImageURL());
 
 				TextView title = (TextView) row.findViewById(R.id.story_row_title);
 				TextView description = (TextView) row
 				    .findViewById(R.id.story_row_description);
 
 				title.setText(Html.fromHtml(story.getTitle()));
 				description.setText(Html.fromHtml(story.getDescription()));
 				row.setTag(position - 1);
 
 				row.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View view) {
 						int position = (Integer) view.getTag();
 						CategoryStory story = stories.get(position);
 
 						Intent intent = new Intent(activity, StoryView.class);
 						intent.putExtra(StoryView.KEY_STORY, story.rawString());
 						activity.startActivity(intent);
 					}
 				});
 
 				return row;
 			}
 		}
 
 		public int getViewTypeCount() {
 			return 3;
 		}
 
 		public int getItemViewType(int position) {
			if (position == 0) {
 				return 0;
 			} else if (position == getCount() - 1) {
 				return 1;
 			}
 			return 2;
 		}
 	}
 }
