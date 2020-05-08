 package com.wehuibao;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.webkit.WebSettings.LayoutAlgorithm;
 import android.webkit.WebView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.gson.Gson;
 import com.wehuibao.json.Doc;
 import com.wehuibao.json.User;
 
 public class DocDetailFragment extends SherlockFragment {
 	private TextView docTitle;
 	private WebView docContent;
 	private TableLayout sharerTable;
 
 	private final static String DOC_URL = "http://wehuibao.com/api/doc/";
 	private Doc doc = null;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		setRetainInstance(true);
 		setHasOptionsMenu(true);
 		View view = inflater.inflate(R.layout.doc_detail, container, false);
 		docTitle = (TextView) view.findViewById(R.id.doc_title);
 		docContent = (WebView) view.findViewById(R.id.doc_content);
 		sharerTable = (TableLayout) view.findViewById(R.id.sharerTable);
 		Intent intent = this.getActivity().getIntent();
 		String docId = intent.getStringExtra(DocDetailActivity.DOC_ID);
 		if (doc == null || doc.docId != docId) {
 			new FetchDocTask().execute(DOC_URL + docId);
 		}
 		return view;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.doc_detail, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_share) {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append(doc.major_title);
 			buffer.append(' ');
 			buffer.append(doc.get_absolute_url());
 			buffer.append(' ');
 			buffer.append(doc.abbrev_text);
 			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 			sharingIntent.setType("text/plain");
 			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 					doc.title);
 			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, buffer
 					.toString().substring(0, 140));
 			this.startActivity(Intent.createChooser(sharingIntent,
 					getString(R.string.share_via)));
 		} else if (item.getItemId() == R.id.menu_browser) {
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW);
 			browserIntent.setData(Uri.parse(doc.url));
 			this.startActivity(browserIntent);
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	class FetchDocTask extends AsyncTask<String, Void, Doc> {
 
 		@Override
 		protected Doc doInBackground(String... urls) {
 			try {
 				URL url = new URL(urls[0]);
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				connection.setReadTimeout(5000);
 				connection.setRequestMethod("GET");
 				connection.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(connection.getInputStream()));
 				Gson gson = new Gson();
 				Doc doc = gson.fromJson(reader, Doc.class);
 				return doc;
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
 		protected void onPostExecute(Doc doc) {
 			if (doc == null) {
 				Toast.makeText(getActivity(),
 						getString(R.string.err_msg_cannot_connet),
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 			DocDetailFragment.this.doc = doc;
 			docTitle.setText(doc.title);
 			docContent.setVerticalFadingEdgeEnabled(false);
 			docContent.getSettings().setDefaultTextEncodingName("UTF-8");
 			docContent.getSettings().setLayoutAlgorithm(
 					LayoutAlgorithm.SINGLE_COLUMN);
 			// docContent.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			StringBuffer buffer = new StringBuffer(
					"<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><body>");
			buffer.append(doc.abbrev);
			buffer.append("</body></html>");
			Log.d("DocDetailFragment", buffer.toString());
			docContent.loadDataWithBaseURL(doc.get_absolute_url(),
					buffer.toString(), "text/html", "UTF-8",
					doc.get_absolute_url());
 			if (doc.sharers.size() > 0) {
 				int number = 0;
 				int total = doc.sharers.size();
 				for (User user : doc.sharers) {
 					number++;
 					TableRow row;
 					if (number < total) {
 						row = (TableRow) DocDetailFragment.this.getActivity()
 								.getLayoutInflater()
 								.inflate(R.layout.sharer, null);
 					} else {
 						row = (TableRow) DocDetailFragment.this.getActivity()
 								.getLayoutInflater()
 								.inflate(R.layout.sharer_last, null);
 					}
 
 					TextView name = (TextView) row
 							.findViewById(R.id.sharer_name);
 					name.setText(user.name);
 					row.setTag(user);
 					row.setOnClickListener(new View.OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							// TODO Auto-generated method stub
 							User user = (User) v.getTag();
 							Intent profileIntent = new Intent(
 									DocDetailFragment.this.getActivity(),
 									ProfileActivity.class);
 							profileIntent.putExtra(ProfileActivity.USERID,
 									user.userId);
 							profileIntent.putExtra(ProfileActivity.USER_NAME,
 									user.name);
 							DocDetailFragment.this.startActivity(profileIntent);
 						}
 					});
 
 					sharerTable.addView(row, new TableLayout.LayoutParams(
 							LayoutParams.MATCH_PARENT,
 							LayoutParams.WRAP_CONTENT));
 				}
 			}
 		}
 	}
 
 }
