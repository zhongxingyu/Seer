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
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.gson.Gson;
 import com.wehuibao.json.Doc;
 import com.wehuibao.json.DocList;
 
 public class DocListFragment extends SherlockListFragment implements
 		OnClickListener {
 
 	private final static String DOC_LIST_SUFFIX = "_DOC_LIST";
 
 	private List<Doc> docs = null;
 	private DocAdapter adapter;
 	private int start = 0;
 	private Boolean hasMore = true;
 	private TextView loadMore;
 	private ProgressBar loadMorePB;
 	private MenuItem refresh = null;
 	private View footer;
 	private String listUrl;
 	private DocList docList;
 	private boolean isRefresh = false;
 	private String maxId = null;
 	private ListType lt = null;
 	private boolean needFetch = true;
 
 	private boolean isStart() {
 		return getArguments().getBoolean(DocListActivity.IS_START);
 	}
 
 	private String getCookie() {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getActivity()
 						.getApplicationContext());
 		return prefs.getString("cookie", null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setRetainInstance(true);
 		setHasOptionsMenu(true);
 
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getActivity()
 						.getApplicationContext());
 
 		listUrl = getArguments().getString(DocListActivity.LIST_URL);
 
 		if (lt == null) {
 			lt = getListType();
 		}
 
 		if (docs == null) {
 			docs = new ArrayList<Doc>();
 			if (isStart() && (lt == ListType.ME || lt == ListType.HOT)) {
 				String docListStr = prefs.getString(lt.toString()
 						+ DOC_LIST_SUFFIX, null);
 				if (docListStr != null) {
 					Gson gson = new Gson();
 					docList = gson.fromJson(docListStr, DocList.class);
 					if (docList.items.size() > 0) {
 						docs = docList.items;
 						needFetch = false;
 						if (refresh != null) {
 							refresh.setActionView(null);
 						}
 					}
 				}
 			}
 			if (needFetch) {
 				isRefresh = true;
 				new DocFetchTask().execute(listUrl);
 			}
 		} else {
 			if (refresh != null) {
 				refresh.setActionView(null);
 			}
 		}
 		adapter = new DocAdapter();
 
 		footer = this.getActivity().getLayoutInflater()
 				.inflate(R.layout.load_more, null);
 		if (hasMore) {
 			if (getListView().getFooterViewsCount() == 0) {
 				getListView().addFooterView(footer);
 			}
 		} else {
 			if (getListView().getFooterViewsCount() > 0) {
 				getListView().removeFooterView(footer);
 			}
 		}
 		loadMore = (TextView) footer.findViewById(R.id.load_more);
 		loadMorePB = (ProgressBar) footer.findViewById(R.id.load_more_pb);
 		loadMore.setOnClickListener(this);
 		this.setListAdapter(adapter);
 	}
 
 	private ListType getListType() {
 		String listType = getArguments().getString(DocListActivity.LIST_TYPE);
 		return ListType.getListType(listType);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		if (lt == null) {
 			lt = getListType();
 		}
 		switch (lt) {
 		case ME:
 			inflater.inflate(R.menu.me, menu);
 			break;
 		case HOT:
 			inflater.inflate(R.menu.hot, menu);
 			break;
 		default:
 			inflater.inflate(R.menu.doc_list, menu);
 		}
 
 		refresh = menu.findItem(R.id.menu_refresh);
 		if (needFetch) {
 			refresh.setActionView(R.layout.refresh);
 		}
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	private void loadMore() {
 		loadMore.setVisibility(View.GONE);
 		loadMorePB.setVisibility(View.VISIBLE);
 		new DocFetchTask().execute(listUrl);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_refresh) {
 			isRefresh = true;
 			start = 0;
 			maxId = null;
 			refresh.setActionView(R.layout.refresh);
 			new DocFetchTask().execute(listUrl);
 		}
 		if (item.getItemId() == R.id.menu_home) {
 			String cookie = getCookie();
 			if (cookie != null) {
 				Intent homeIntent = new Intent(getActivity(),
 						DocListActivity.class);
 				homeIntent.putExtra(DocListActivity.LIST_TYPE,
 						ListType.ME.toString());
 				startActivity(homeIntent);
 			} else {
 				Intent profileIntent = new Intent(getActivity(),
 						ProfileActivity.class);
 				startActivity(profileIntent);
 			}
 		}
 		if (item.getItemId() == R.id.menu_hot) {
 			Intent hotIntent = new Intent(getActivity(), DocListActivity.class);
 			hotIntent.putExtra(DocListActivity.LIST_TYPE,
 					ListType.HOT.toString());
 			startActivity(hotIntent);
 		}
 		if (item.getItemId() == R.id.menu_profile) {
 			Intent profileIntent = new Intent(getActivity(),
 					ProfileActivity.class);
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(getActivity()
 							.getApplicationContext());
 			String userId = prefs.getString("userId", "");
 			String userName = prefs.getString("userName", "");
 			profileIntent.putExtra(ProfileActivity.USERID, userId);
 			profileIntent.putExtra(ProfileActivity.USER_NAME, userName);
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
 		public int getCount() {
 			return docs.size();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View row = super.getView(position, convertView, parent);
 			Doc doc = docs.get(position);
 			TextView title = (TextView) row.findViewById(R.id.doc_title);
 			title.setText(doc.major_title);
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
 
 	class DocFetchTask extends AsyncTask<String, Doc, DocList> {
 		private List<Doc> fetchedDocs = new ArrayList<Doc>();
 
 		@Override
 		protected DocList doInBackground(String... urls) {
 			try {
 				String urlStr = urls[0];
 				switch (lt) {
 				case HOT:
 					if (start != 0) {
 						urlStr += "?start=" + String.valueOf(start);
 					}
 					break;
 				default:
 					if (maxId != null) {
 						urlStr += "?max_id=" + maxId;
 					}
 				}
 
 				URL url = new URL(urlStr);
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				connection.setReadTimeout(5000);
 				connection.setRequestMethod("GET");
 				String cookie = getCookie();
 				if (cookie != null) {
 					connection.setRequestProperty("Cookie", cookie);
 				}
 				connection.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(connection.getInputStream()));
 				Gson gson = new Gson();
 				docList = gson.fromJson(reader, DocList.class);
 				reader.close();
 				for (Doc doc : docList.items) {
 					if (doc.title == null || doc.title.length() == 0) {
 						continue;
 					}
 					if (doc.thumb != null && doc.thumb.image_src != null) {
 						doc.thumb.image_path = downloadDocThumbnail(
 								doc.thumb.image_src, doc.docId);
 					}
 					maxId = doc.docId;
 					publishProgress(doc);
 				}
 				hasMore = docList.has_more;
 				if (docList.has_more) {
 					if (isRefresh) {
 						start = 20;
 					} else {
 						start += 20;
 					}
 				}
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return docList;
 		}
 
 		@Override
 		protected void onProgressUpdate(Doc... docs) {
 			for (Doc doc : docs) {
 				if (DocListFragment.this.docs.indexOf(doc) == -1) {
 					fetchedDocs.add(doc);
 				}
 			}
 		}
 
 		@Override
 		protected void onPostExecute(DocList docList) {
 			if (docList == null) {
 				if (isAdded()) {
 					Toast.makeText(getActivity(),
 							getString(R.string.err_msg_cannot_connet),
 							Toast.LENGTH_SHORT).show();
 				}
 				return;
 			}
 
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
 			if (isRefresh) {
 				adapter.clear();
 				for (Doc doc : docList.items) {
 					adapter.add(doc);
 				}
 			} else {
 				for (Doc doc : fetchedDocs) {
 					adapter.add(doc);
 				}
 			}
 			if (fetchedDocs.size() > 0) {
 				adapter.notifyDataSetChanged();
 				if (isAdded()) {
 					Toast.makeText(
 							getActivity(),
 							String.valueOf(fetchedDocs.size())
 									+ getString(R.string.number_of_new_docs),
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 
 			if (isRefresh) {
 				if (refresh != null) {
 					refresh.setActionView(null);
 				}
 				DocListFragment.this.getListView()
 						.setSelectionAfterHeaderView();
 				isRefresh = false;
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
 			loadMore();
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		if (position == docs.size()) {
 			loadMore();
 		} else {
 			Doc doc = docs.get(position);
 			Intent intent = new Intent(this.getActivity(),
 					DocDetailActivity.class);
 			intent.putExtra(DocDetailActivity.DOC_ID, doc.docId);
 			this.startActivity(intent);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		if (docList != null) {
 			docList.items = docs;
 			if (lt != null) {
 				if (lt == ListType.ME || lt == ListType.HOT) {
 					Gson gson = new Gson();
 					SharedPreferences prefs = PreferenceManager
 							.getDefaultSharedPreferences(getActivity()
 									.getApplicationContext());
 					prefs.edit()
 							.putString(lt.toString() + "_DOC_LIST",
 									gson.toJson(docList)).commit();
 				}
 			}
 		}
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (lt == ListType.ME && getCookie() == null) {
 			Intent hotIntent = new Intent(getActivity(), DocListActivity.class);
 			hotIntent.putExtra(DocListActivity.LIST_TYPE,
 					ListType.HOT.toString());
 			startActivity(hotIntent);
 			getActivity().finish();
 		}
 	}
 }
