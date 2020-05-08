 package harris.GiantBomb;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 /**
  * Lists videos, launches video player when one is clicked
  * 
  */
 public class VideoList extends ListActivity implements api {
 
 	private ArrayList<Video> videos;
 	private int offset = 0;
 	private int searchPasses = 1;
 	private int lastItemIndex = 0;
 	
 	private String searchString;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.videolist);
 		videos = new ArrayList<Video>();
 		
 		Bundle bundle = getIntent().getExtras();
 		if (bundle != null) {
 			searchString = bundle.getString("searchString");
 			searchPasses = 10;
 		}
 	
 		loadFeed();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		if (videos.get(position).getId() == -1) {
 			videos.remove(position);
 			loadFeed();
 		} else {
 			Intent myIntent = new Intent(this, VidPlayer.class);
 			Bundle bundle = new Bundle();
 			bundle.putString("URL", videos.get(position).getLink());
 			bundle.putString("title", videos.get(position).getTitle());
 			bundle.putString("siteDetailURL", videos.get(position)
 					.getSiteDetailURL());
 			myIntent.putExtras(bundle);
 			VideoList.this.startActivity(myIntent);
 		}
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, 1, 0, "Share");
 		menu.add(0, 2, 0, "Download");
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		if (item.getItemId() == 1) {
 			System.out.println(info.id);
 			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
 			shareIntent.setType("text/plain");
 			shareIntent.putExtra(Intent.EXTRA_TEXT, videos.get((int) info.id)
 					.getSiteDetailURL());
 			startActivity(Intent.createChooser(shareIntent,
 					"Share link with..."));
 		} else if (item.getItemId() == 2) {
 			Intent myIntent = new Intent(this, DownloadView.class);
 			Bundle bundle = new Bundle();
 			bundle.putString("URL", videos.get((int) info.id).getLink());
 			bundle.putString("title", videos.get((int) info.id).getTitle());
 			myIntent.putExtras(bundle);
 			VideoList.this.startActivity(myIntent);
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadFeed() {
 		final ListActivity list = this;
 		final ProgressDialog dialog = ProgressDialog.show(VideoList.this, "",
 				"Loading. Please wait...", true);
 		dialog.show();
 
 		final Handler handler = new Handler() {
 			@Override
 			public void handleMessage(Message message) {
 				dialog.dismiss();
 				list.setListAdapter(((ArrayAdapter) message.obj));
 				registerForContextMenu(getListView());
 				list.setSelection(lastItemIndex);
 			}
 		};
 
 		Thread thread = new Thread() {
 			@Override
 			public void run() {
 
 				try {
 					lastItemIndex = videos.size() - 1;
 					for (int i = 0; i < searchPasses; i++) {
 						VideoFeedParser parser = new VideoFeedParser(
 								"http://api.giantbomb.com/videos/?api_key="
 										+ API_KEY
 										+ "&sort=-publish_date&limit=25&field_list=name,deck,id,url,image,site_detail_url&format=xml&offset="
 										+ offset, searchString);
 						offset = offset + 25;
 						ArrayList<Video> add = new ArrayList<Video>(25);
 						add = (ArrayList<Video>) parser.parse();
 						for (Video v : add) {
 							videos.add(v);
 						}
 					}
 					Video loadMore = new Video();
 					
 					if (searchString == null) {
 						loadMore.setTitle("Load 25 More...");
 					} else {
 						loadMore.setTitle("Search items " + (offset + 1) + " to " + (offset + searchPasses * 25) + "...");
 					}
 					loadMore.setId(-1);
 					videos.add(loadMore);
 					Message message;
 					message = handler.obtainMessage(-1, new VideoListAdapter(
 							list, R.layout.videorow, videos));
 					handler.sendMessage(message);
 				} catch (Throwable t) {
 				}
 			}
 		};
 		thread.start();
 	}
 }
