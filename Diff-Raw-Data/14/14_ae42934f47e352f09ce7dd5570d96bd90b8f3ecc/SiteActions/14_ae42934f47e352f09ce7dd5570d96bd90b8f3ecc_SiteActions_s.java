 package org.droidstack;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class SiteActions extends Activity {
 	
 	private static final int POS_ALL = 0;
 	private static final int POS_UNANSWERED = 1;
 	private static final int POS_SEARCH = 2;
 	private static final int POS_MY_QUESTIONS = 3;
 	private static final int POS_FAVORITES = 4;
 	private static final int POS_MY_ANSWERS = 5;
 	
 	private Context mContext;
 	private String mEndpoint;
 	private String mSiteName;
 	private String mUserName;
 	private int mUserID;
 	private ListView mSiteActionsList;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.site);
 		
 		mContext = this;
 		
 		Uri data = getIntent().getData();
 		
 		mEndpoint = data.getQueryParameter("endpoint");
 		mSiteName = data.getQueryParameter("name");
 		mUserName = data.getQueryParameter("uname");
		mUserID = Integer.parseInt(data.getQueryParameter("uid"));
 		
 		setTitle(mSiteName);
 		
 		mSiteActionsList = (ListView) findViewById(R.id.SiteActionsList);
 		
 		mSiteActionsList.setOnItemClickListener(onItemClicked);
 	}
 	
 	@Override
 	public boolean onSearchRequested() {
 		View dialogView = getLayoutInflater().inflate(R.layout.search_dialog, null);
 		final EditText intitleEdit = (EditText) dialogView.findViewById(R.id.intitle);
 		final EditText taggedEdit = (EditText) dialogView.findViewById(R.id.tagged);
 		final EditText nottaggedEdit = (EditText) dialogView.findViewById(R.id.nottagged);
 		new AlertDialog.Builder(mContext)
 			.setTitle(R.string.search_title)
 			.setView(dialogView)
 			.setNegativeButton(android.R.string.cancel, null)
 			.setPositiveButton(android.R.string.ok, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					String intitle = intitleEdit.getText().toString();
 					String tagged = taggedEdit.getText().toString();
 					String nottagged = nottaggedEdit.getText().toString();
 					if (intitle.length() > 0 || tagged.length() > 0 || nottagged.length() > 0) {
 						Intent i = new Intent(mContext, Questions.class);
 						String uri = "droidstack://questions/search" +
 							"?endpoint=" + Uri.encode(mEndpoint) +
 							"&name=" + Uri.encode(mSiteName);
 						if (intitle.length() > 0) uri += "&intitle=" + Uri.encode(intitle);
 						if (tagged.length() > 0) uri += "&tagged=" + Uri.encode(tagged);
 						if (nottagged.length() > 0) uri += "&nottagged=" + Uri.encode(nottagged);
 						i.setData(Uri.parse(uri));
 						startActivity(i);
 					}
 				}
 			}).create().show();
 		return false;
 	}
 
 	private OnItemClickListener onItemClicked = new OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			Class activity = null;
 			String uri = null;
 			switch(position) {
 			case POS_ALL:
 				activity = Questions.class;
 				uri = "droidstack://questions/all?";
 				break;
 			case POS_UNANSWERED:
 				activity = Questions.class;
 				uri = "droidstack://questions/unanswered?";
 				break;
 			case POS_SEARCH:
 				onSearchRequested();
 				break;
 			case POS_MY_QUESTIONS:
 				if (mUserID == 0) {
 					Toast.makeText(mContext,
 							R.string.no_userid,
 							Toast.LENGTH_LONG).show();
 					break;
 				}
 				activity = Questions.class;
 				uri = "droidstack://questions/user" +
					"&uid=" + mUserID +
 					"&uname=" + Uri.encode(mUserName) + "&";
 				break;
 			case POS_FAVORITES:
 				if (mUserID == 0) {
 					Toast.makeText(mContext,
 							R.string.no_userid,
 							Toast.LENGTH_LONG).show();
 					break;
 				}
 				activity = Questions.class;
 				uri = "droidstack://questions/favorites" +
					"&uid=" + mUserID +
 					"&uname=" + Uri.encode(mUserName) + "&";
 				break;
 			case POS_MY_ANSWERS:
 				if (mUserID == 0) {
 					Toast.makeText(mContext,
 							R.string.no_userid,
 							Toast.LENGTH_LONG).show();
 					break;
 				}
 				activity = Answers.class;
 				uri = "droidstack://answers/user" +
					"&uid=" + mUserID +
 					"&uname=" + Uri.encode(mUserName) + "&";
 				break;
 			}
 			if (activity != null && uri != null) {
 				uri += "endpoint=" + Uri.encode(mEndpoint) + "&name=" + Uri.encode(mSiteName);
 				Intent i = new Intent(mContext, activity);
 				i.setData(Uri.parse(uri));
 				startActivity(i);
 			}
 		}
 	};
 	
 }
