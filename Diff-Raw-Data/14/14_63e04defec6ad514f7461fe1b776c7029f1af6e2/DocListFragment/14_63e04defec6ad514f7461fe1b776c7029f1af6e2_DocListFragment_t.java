 package com.wehuibao;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.gson.Gson;
 import com.wehuibao.json.Doc;
 import com.wehuibao.json.DocList;
 
public class DocListFragment extends SherlockListFragment implements OnClickListener {
 
 	private static final String HOT_URL = "http://wehuibao.com/api/hot/";
 	private static final String DOC_LIST_URL = "http://wehuibao.com/api/doclist/";
 	private List<Doc> docs = null;
 	private DocAdapter adapter;
 	private int start = 0;
 	private Boolean hasMore = true;
 	private TextView loadMore;
 	private ProgressBar loadMorePB;
 	private MenuItem refresh;
 	private View footer;
 	private String listUrl;
 	private ListType lt = null;
 	private String userId = null;
 	private String cookie;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setRetainInstance(true);
 		setHasOptionsMenu(true);
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getActivity()
 						.getApplicationContext());
 		cookie = prefs.getString("cookie", null);
 
 		Intent intent = this.getActivity().getIntent();
 		String listType = intent.getStringExtra(DocListActivity.LIST_TYPE);
 		if (listType != null) {
 			lt = ListType.getListType(listType);
 		} else {
 			if (cookie != null) {
 				lt = ListType.ME;
 			} else {
 				lt = ListType.HOT;
 			}
 		}
 		
 		switch (lt) {
 		case ME:
 			userId = "@me";
 			listUrl = DOC_LIST_URL + userId;
 			break;
 		case OTHER:
 			userId = listType;
 			listUrl = DOC_LIST_URL + userId;
 			break;
 		default:
 			listUrl = HOT_URL;
 		}
 
 		if (docs == null) {
 			docs = new ArrayList<Doc>();
 			new DocFetchTask().execute(listUrl);
 			adapter = new DocAdapter();
 		}
 		footer = this.getActivity().getLayoutInflater()
 				.inflate(R.layout.load_more, null);
 		this.getListView().addFooterView(footer);
 		loadMore = (TextView) this.getActivity().findViewById(R.id.load_more);
 		loadMorePB = (ProgressBar) this.getActivity().findViewById(
 				R.id.load_more_pb);
 		loadMore.setOnClickListener(this);
 		this.setListAdapter(adapter);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		switch (lt) {
 		case ME:
 			inflater.inflate(R.menu.me, menu);
 			break;
 		case OTHER:
 			inflater.inflate(R.menu.doc_list, menu);
 			break;
 		default:
 			inflater.inflate(R.menu.hot, menu);
 		}
 
 		//inflater.inflate(R.menu.doc_list, menu);
 		refresh = menu.findItem(R.id.menu_refresh);
 		refresh.setActionView(R.layout.refresh);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_refresh) {
 			adapter.clear();
 			start = 0;
 			refresh.setActionView(R.layout.refresh);
 			new DocFetchTask().execute(HOT_URL);
 		}
 		if (item.getItemId() == R.id.menu_home) {
 			if (cookie != null) {
 				Intent homeIntent = new Intent(getActivity(), DocListActivity.class);
 				homeIntent.putExtra(DocListActivity.LIST_TYPE, ListType.ME.toString());
 				startActivity(homeIntent);
 			} else {
 				Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
 				startActivity(profileIntent);
 			}
 		}
 		if (item.getItemId() == R.id.menu_hot) {
 			Intent hotIntent = new Intent(getActivity(), DocListActivity.class);
 			hotIntent.putExtra(DocListActivity.LIST_TYPE, ListType.HOT.toString());
 			startActivity(hotIntent);
 		}
 		if (item.getItemId() == R.id.menu_profile) {
 			Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
 			profileIntent.putExtra(ProfileActivity.USERID, userId);
 			startActivity(profileIntent);
 		}
 		return super.onOptionsItemSelected(item);
 
 	}
 
 	class DocAdapter extends ArrayAdapter<Doc> {
 
 		public DocAdapter() {
 			super(DocListFragment.this.getActivity(), R.layout.doc_row,
 					R.id.doc_title, docs);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = super.getView(position, convertView, parent);
 			Doc doc = docs.get(position);
 			TextView title = (TextView) row.findViewById(R.id.doc_title);
 			title.setText(doc.title);
 			TextView abbrev = (TextView) row.findViewById(R.id.doc_abbrev);
 			abbrev.setText(doc.abbrev_text);
 			ImageView thumb = (ImageView) row.findViewById(R.id.doc_thumb);
 			if (doc.thumb != null && doc.thumb.image_path != null) {
 				Bitmap bm = BitmapFactory.decodeFile(doc.thumb.image_path);
 				thumb.setImageBitmap(bm);
 				thumb.setVisibility(View.VISIBLE);
 			} else {
 				thumb.setVisibility(View.GONE);
 			}
 			return row;
 		}
 	}
 
 	class DocFetchTask extends AsyncTask<String, Doc, Void> {
 
 		@Override
 		protected Void doInBackground(String... urls) {
 			try {
 				String urlStr = urls[0];
 				if (start != 0) {
 					urlStr += "?start=" + String.valueOf(start);
 				}
 				URL url = new URL(urlStr);
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				connection.setReadTimeout(5000);
 				connection.setRequestMethod("GET");
 				if (cookie != null) {
 					connection.setRequestProperty("Cookie", cookie);
 				}
 				connection.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(connection.getInputStream()));
 				Gson gson = new Gson();
 				DocList docList = gson.fromJson(reader, DocList.class);
 				reader.close();
 				for (Doc doc : docList.items) {
 					if (doc.title == null || doc.title.length() == 0) {
 						continue;
 					}
 					if (doc.thumb != null && doc.thumb.image_src != null) {
 						doc.thumb.image_path = downloadDocThumbnail(
 								doc.thumb.image_src, doc.docId);
 						Log.d("doc.image:", doc.thumb.image_path);
 					}
 					this.publishProgress(doc);
 				}
 				hasMore = docList.has_more;
 				if (docList.has_more) {
 					start += 20;
 				}
 				Log.d("start: ", String.valueOf(start));
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Doc... docs) {
 			for (Doc doc : docs) {
 				adapter.add(doc);
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Void unused) {
 			refresh.setActionView(null);
 			if (loadMorePB.getVisibility() == View.VISIBLE) {
 				loadMorePB.setVisibility(View.GONE);
 				loadMore.setVisibility(View.VISIBLE);
 			}
 			if (!hasMore) {
 				DocListFragment.this.getListView().removeFooterView(footer);
 			} else {
 				if (DocListFragment.this.getListView().getFooterViewsCount() == 0) {
 					DocListFragment.this.getListView().addFooterView(footer);
 				}
 			}
 		}
 
 		private String downloadDocThumbnail(String image_url, String doc_id) {
 			String root = DocListFragment.this.getActivity()
 					.getExternalFilesDir(null).toString();
 			File avatarDir = new File(root + "/docs/" + doc_id);
 			String image_name = image_url
 					.substring(image_url.lastIndexOf('/') + 1);
 			if (image_name.indexOf('?') != -1) {
 				image_name = image_name.substring(0,
 						image_name.lastIndexOf('?'));
 			}
 			File avatar = new File(avatarDir.toString() + '/' + image_name);
 			if (avatar.exists()) {
 				return avatar.getAbsolutePath();
 			}
 			if (!avatarDir.exists()) {
 				avatarDir.mkdirs();
 			}
 
 			try {
 				URL url = new URL(image_url);
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				connection.setReadTimeout(50000);
 				connection.connect();
 				InputStream in = connection.getInputStream();
 				FileOutputStream fos = new FileOutputStream(avatar.getPath());
 				BufferedOutputStream bos = new BufferedOutputStream(fos);
 				byte[] buffer = new byte[1024];
 				int len = 0;
 				try {
 					while ((len = in.read(buffer)) > 0) {
 						bos.write(buffer, 0, len);
 					}
 					bos.flush();
 				} finally {
 					fos.getFD().sync();
 					bos.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				return "";
 			}
 			return avatar.getAbsolutePath();
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.load_more) {
 			v.setVisibility(View.GONE);
 			loadMorePB.setVisibility(View.VISIBLE);
 			new DocFetchTask().execute(HOT_URL);
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Doc doc = docs.get(position);
 		Intent intent = new Intent(this.getActivity(), DocDetailActivity.class);
 		intent.putExtra(DocDetailActivity.DOC_ID, doc.docId);
 		this.startActivity(intent);
 	}
 }
