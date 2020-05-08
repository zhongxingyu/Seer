 package cz.kinst.jakub.coursemanager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ForumReplies extends CMActivity {
 
 	/**
 	 * UID for serialization
 	 */
 	private static final long serialVersionUID = 3605505635167753669L;
 	private static final int DIALOG_REPLY = 0;
 	private int tid;
 	public int MENU_REPLY;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.tid = getIntent().getExtras().getInt("tid");
 		setContentView(R.layout.forum_replies);
 		reload();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch (id) {
 		case DIALOG_REPLY:
 			LayoutInflater factory = LayoutInflater.from(this);
 			final View v = factory.inflate(R.layout.dialog_new_reply, null);
 			final EditText inputContent = (EditText) v
 					.findViewById(R.id.content);
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(R.string.reply)
 					.setView(v)
 					.setCancelable(false)
 					.setPositiveButton(R.string.post,
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int id) {
 									addReply(inputContent.getText().toString());
 									inputContent.setText("");
 								}
 							})
 					.setNegativeButton(R.string.cancel,
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			dialog = builder.create();
 			break;
 		default:
 			dialog = null;
 		}
 		return dialog;
 	}
 
 	protected void addReply(String content) {
 		final ArrayList<NameValuePair> postArgs = new ArrayList<NameValuePair>();
 		postArgs.add(new BasicNameValuePair("content", content));
 		final ArrayList<NameValuePair> getArgs = new ArrayList<NameValuePair>();
 		getArgs.add(new BasicNameValuePair("tid", String.valueOf(tid)));
 
 		// post topic in safe thread
 		new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected void onPreExecute() {
 				setProgressBarIndeterminateVisibility(true);
 			};
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				courseManagerCon.sendForm("forum", "show-topic", "addReply",
 						getArgs, postArgs);
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				setProgressBarIndeterminateVisibility(false);
 				courseManagerCon.toastFlashes();
 				reload();
 			};
 		}.execute();
 	}
 
 	@Override
 	protected JSONObject reloadWork() throws JSONException {
 		JSONObject forum = new JSONObject();
 		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 		args.add(new BasicNameValuePair("tid", String.valueOf(this.tid)));
 		args.add(new BasicNameValuePair("pages-page", String.valueOf(this.page)));
 		forum = courseManagerCon.getAction("forum", "show-topic", args,
 				new ArrayList<NameValuePair>());
 		return forum;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
		MenuItem newComment = menu.add(R.string.new_topic);
 		this.MENU_REPLY = newComment.getItemId();
 		newComment.setIcon(android.R.drawable.ic_menu_edit);
 		if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 11) {
 			newComment.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		}
 		return result;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		if (item.getItemId() == MENU_REPLY) {
 			showDialog(DIALOG_REPLY);
 			return true;
 		} else {
 			return super.onMenuItemSelected(featureId, item);
 		}
 
 	}
 
 	@Override
 	public void gotData(JSONObject data) throws JSONException {
 		setPaginator(data);
 		ArrayList<JSONObject> replies = new ArrayList<JSONObject>();
 		JSONArray resourcesJSON = data.getJSONArray("replies");
 		for (int i = 0; i < resourcesJSON.length(); i++) {
 			replies.add(resourcesJSON.getJSONObject(i));
 		}
 		JSONObject topic = data.getJSONObject("topic");
 		JSONObject author = topic.getJSONObject("author");
 		((TextView) (findViewById(R.id.topicAuthor))).setText(author
 				.getString("firstname") + " " + author.getString("lastname"));
 		((TextView) (findViewById(R.id.topicDate))).setText(topic
 				.getString("created"));
 		((TextView) (findViewById(R.id.topicContent))).setText(topic
 				.getString("content"));
 		((ListView) (findViewById(R.id.replies)))
 				.setAdapter(new RepliesAdapter(this,
 						android.R.layout.simple_list_item_1, replies));
 	}
 
 	public class RepliesAdapter extends ArrayAdapter<JSONObject> {
 
 		public RepliesAdapter(Context context, int textViewResourceId,
 				List<JSONObject> objects) {
 			super(context, textViewResourceId, objects);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.forum_reply_row, null);
 			}
 			final JSONObject reply = getItem(position);
 			try {
 				JSONObject author = reply.getJSONObject("author");
 				((TextView) (v.findViewById(R.id.content))).setText(reply
 						.getString("content"));
 				((TextView) (v.findViewById(R.id.added))).setText(reply
 						.getString("created"));
 				((TextView) (v.findViewById(R.id.author))).setText(author
 						.getString("firstname")
 						+ " "
 						+ author.getString("lastname"));
 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return v;
 		}
 	}
 
 }
