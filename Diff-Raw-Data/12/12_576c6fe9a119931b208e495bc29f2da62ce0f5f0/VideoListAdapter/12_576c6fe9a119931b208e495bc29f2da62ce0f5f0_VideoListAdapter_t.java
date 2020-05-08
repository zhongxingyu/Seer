 package harris.GiantBomb;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OptionalDataException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Custom adapter
  * 
  */
 public class VideoListAdapter extends ArrayAdapter<String> {
 
 	ArrayList<Video> videos;
	private Map<String, Drawable> drawableMap;
 	private LayoutInflater mInflater;
     
 	@SuppressWarnings("unchecked")
 	public VideoListAdapter(Context context, int textViewResourceId,
 			ArrayList<Video> videos) throws OptionalDataException,
 			ClassNotFoundException, IOException {
 		super(context, textViewResourceId, (List) videos);
 		mInflater = LayoutInflater.from(context);
 		this.videos = videos;
 		drawableMap = new HashMap();
 	}
 
 	static class ViewHolder {
 		ImageView thumb;
 		TextView title;
 		TextView desc;
 	}
 
 	// Sets up title, thumbnail, and description.
 	// Thumbnails are loaded in a new thread for performance
 	@Override
 	public View getView(final int i, View convertView, ViewGroup parent) {
 		final ViewHolder holder;
 		
		//if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.videorow, null);
 			holder = new ViewHolder();
 			holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
 			holder.title = (TextView) convertView.findViewById(R.id.videotitle);
 			holder.desc = (TextView) convertView.findViewById(R.id.desc);
 			convertView.setTag(holder);
		//} else {
		//	holder = (ViewHolder) convertView.getTag();
		//}
 		holder.thumb.setImageResource(R.drawable.loading);
 		if (videos.get(i) != null) {
 			if (videos.get(i).getId() == -1) {
 				holder.title.setText(videos.get(i).getTitle());
 				holder.desc.setText("");
 				holder.thumb.setImageDrawable(null);
 			} else {
 				holder.title.setText(videos.get(i).getTitle());
 				holder.desc.setText(videos.get(i).getDesc());
 				
 				//url + id is combined so we get both of them passed as parameters to the async task
 				holder.thumb.setTag(videos.get(i).getThumbLink() + ";" + videos.get(i).getId());
 /*
 				final Handler handler = new Handler() {
 					public void handleMessage(Message message) {
 						holder.thumb.setImageDrawable((Drawable) message.obj);
 					}
 				};
 				
 				new Thread() {
 					@Override
 					public void run() {
 						Drawable drawable = null;
 						try {
 							drawable = fetchDrawable(videos.get(i).getThumbLink(), videos.get(i).getId());
 						} catch (MalformedURLException e) {
 						} catch (IOException e) {
 						}
 						handler.sendMessage(handler.obtainMessage(0, drawable));
 					}
 				}.start();
 	*/			
 				new DownloadImageTask().execute(holder);
 			}
 		}
 		return convertView;
 	}
 
 	public Drawable fetchDrawable(String urlString, int id)
 			throws MalformedURLException, IOException {
 		if(drawableMap.containsKey(urlString)) {
 			return drawableMap.get(urlString);
 		}
 		drawableMap.put(urlString, Drawable.createFromStream(fetch(urlString), "src"));
 		return drawableMap.get(urlString);
 	}
 	
 	private InputStream fetch(String urlString) throws MalformedURLException,
 			IOException {
 		return new DefaultHttpClient().
 			execute(new HttpGet(urlString)).
 			getEntity().
 			getContent();
 	}
 	
 	private class DownloadImageTask extends AsyncTask<ViewHolder, Void, ViewHolder> {
 		Drawable drawable;
 		String tagid;
 
 		@Override
 		protected ViewHolder doInBackground(ViewHolder... params) {
 			tagid = (String)params[0].thumb.getTag();
 			//Split the string on our delimiter ";" so we can pass the url and id to fetchDrawable
 			String[] test = tagid.split(";");
 			try {
 				drawable = fetchDrawable(test[0], Integer.parseInt(test[1]));
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				System.out.println(e.getStackTrace());
 				e.printStackTrace();
 			}
 			return params[0];
 		}
 
 		protected void onPostExecute(ViewHolder holder) {
 			holder.thumb.setImageDrawable(drawable);
 		}
 	}
 }
