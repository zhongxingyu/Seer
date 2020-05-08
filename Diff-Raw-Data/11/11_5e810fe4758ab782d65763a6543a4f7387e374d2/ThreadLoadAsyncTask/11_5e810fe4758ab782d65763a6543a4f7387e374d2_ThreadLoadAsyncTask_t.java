 package ru.terrach.tasks;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.terrach.R;
 import ru.terrach.activity.component.PostsArrayAdapter;
 import ru.terrach.core.AsyncTaskEx;
 import ru.terrach.network.dto.PostDTO;
 import ru.terrach.network.dto.SingleThreadDTO;
 import android.content.Context;
 import android.widget.ListView;
 import flexjson.JSONDeserializer;
 import flexjson.JSONException;
 
 public class ThreadLoadAsyncTask extends AsyncTaskEx<String, Integer, SingleThreadDTO> {
 	private String server, wakaba;
 	private ListView lvPosts;
 	private String board;
 	private ArrayList<String> pics;
 
 	public ThreadLoadAsyncTask(Context a, ListView lvPosts, ArrayList<String> pics) {
 		super(a);
 		this.lvPosts = lvPosts;
 		this.pics = pics;
 		server = a.getString(R.string.server);
 		wakaba = a.getString(R.string.wakaba);
 	}
 
 	@Override
 	protected void onCancelled() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected SingleThreadDTO doInBackground(String... params) {
 		try {
 			board = params[0];
 			URLConnection conn = new URL(server + board + "/res/" + params[1] + ".json").openConnection();
 			return new JSONDeserializer<SingleThreadDTO>().deserialize(new InputStreamReader(conn.getInputStream(), "UTF-8"), SingleThreadDTO.class);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(SingleThreadDTO result) {
 		if (result != null) {
 			for (List<PostDTO> msgsInThread : result.thread)
				if (msgsInThread.get(0).thumbnail != null)
					pics.add(server + board + msgsInThread.get(0).thumbnail);
 
 			PostsArrayAdapter adapter = (PostsArrayAdapter) lvPosts.getAdapter();
 			if (adapter == null)
 				lvPosts.setAdapter(new PostsArrayAdapter(context, result.thread, board));
 			else
 				adapter.update(result.thread);
 		}
 	}
 
 }
