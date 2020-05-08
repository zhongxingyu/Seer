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
 import android.text.Editable;
 import android.text.InputType;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import cz.kinst.jakub.coursemanager.utils.DownloadTask;
 
 public class Lesson extends CMActivity {
 
 	private static final int DIALOG_NEW_COMMENT = 0;
 	private int lid;
 	public ArrayList<Integer> pages;
 	public int page = 1;
 	public int MENU_NEW_COMMENT;
 	public static final String TAB_COMMENTS = "comments";
 	public static final String TAB_RESOURCES = "resources";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setHeader(R.layout.lesson_header);
 		addTab(TAB_COMMENTS, R.layout.lesson_comments,
 				getText(R.string.comments));
 		addTab(TAB_RESOURCES, R.layout.lesson_resources,
 				getText(R.string.resources));
 		switchTab("comments");
 
 		this.lid = getIntent().getExtras().getInt("lid");
 		((TextView) (getTab(TAB_COMMENTS).findViewById(R.id.category)
 				.findViewById(R.id.name))).setText(R.string.comments);
 		((TextView) (getTab(TAB_RESOURCES).findViewById(R.id.category)
 				.findViewById(R.id.name))).setText(R.string.resources);
 		reload();
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		switch (id) {
 		case DIALOG_NEW_COMMENT:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_TEXT);
 			builder.setMessage(R.string.new_comment)
 					.setView(input)
 					.setCancelable(false)
 					.setPositiveButton(R.string.post,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									addComment(input.getText());
 									input.setText("");
 								}
 							})
 					.setNegativeButton(R.string.cancel,
 							new DialogInterface.OnClickListener() {
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
 
 	protected void addComment(Editable text) {
 		final ArrayList<NameValuePair> postArgs = new ArrayList<NameValuePair>();
 		postArgs.add(new BasicNameValuePair("content", text.toString()));
 
 		final ArrayList<NameValuePair> getArgs = new ArrayList<NameValuePair>();
 		getArgs.add(new BasicNameValuePair("lid", String.valueOf(lid)));
 
 		// post comment in safe thread
 		new AsyncTask<Void, Void, Void>() {
 			protected void onPreExecute() {
 				setProgressBarIndeterminateVisibility(true);
 			};
 
 			protected Void doInBackground(Void... params) {
 				cm.sendForm("lesson", "homepage", "commentForm", getArgs,
 						postArgs);
 				return null;
 			}
 
 			protected void onPostExecute(Void result) {
 				setProgressBarIndeterminateVisibility(false);
 				cm.toastFlashes();
 				reload();
 			};
 		}.execute();
 	}
 
 	@Override
 	protected JSONObject reloadWork() throws JSONException {
 		JSONObject lesson = new JSONObject();
 		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 		args.add(new BasicNameValuePair("lid", String.valueOf(this.lid)));
 		args.add(new BasicNameValuePair("pages-page", String.valueOf(this.page)));
 		lesson = cm.getAction("lesson", "homepage", args,
 				new ArrayList<NameValuePair>());
 		return lesson;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		boolean result = super.onCreateOptionsMenu(menu);
 		MenuItem newComment = menu.add(R.string.new_comment);
 		this.MENU_NEW_COMMENT = newComment.getItemId();
 		newComment.setIcon(android.R.drawable.ic_menu_edit);
 		if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 11)
 			newComment.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		return result;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		if (item.getItemId() == MENU_NEW_COMMENT) {
 			showDialog(DIALOG_NEW_COMMENT);
 			return true;
 		} else {
 			return super.onMenuItemSelected(featureId, item);
 		}
 
 	}
 
 	@Override
 	public void gotData(JSONObject data) throws JSONException {
 
 		Button prev = (Button) getTab(TAB_COMMENTS).findViewById(
 				R.id.previous_page);
 		Button next = (Button) getTab(TAB_COMMENTS).findViewById(
 				R.id.nextt_page);
 		TextView pageLabel = (TextView) getTab(TAB_COMMENTS).findViewById(
 				R.id.page);
 		if (data.getJSONObject("pages").has("steps")) {
 			JSONArray steps = data.getJSONObject("pages").getJSONArray("steps");
 			pages = new ArrayList<Integer>();
 			for (int i = 0; i < steps.length(); i++) {
 				pages.add(new Integer(steps.getInt(i)));
 			}
 
 			pageLabel.setText(getText(R.string.page) + " "
 					+ String.valueOf(page) + " " + getText(R.string.of) + " "
 					+ pages.size());
 			if (page < 2)
 				prev.setVisibility(View.INVISIBLE);
 			else
 				prev.setVisibility(View.VISIBLE);
 			if (page >= pages.size())
 				next.setVisibility(View.INVISIBLE);
 			else
 				next.setVisibility(View.VISIBLE);
 
 			pageLabel.setVisibility(View.VISIBLE);
 
 			prev.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					page = page - 1;
 					reload();
 				}
 			});
 			next.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					page = page + 1;
 					reload();
 				}
 			});
 		} else {
 			prev.setVisibility(View.GONE);
 			next.setVisibility(View.GONE);
 			pageLabel.setVisibility(View.GONE);
 		}
 
 		JSONObject lesson = data.getJSONObject("lesson");
 		((TextView) (getHeader().findViewById(R.id.topic))).setText(lesson
 				.getString("topic"));
 		((TextView) (getHeader().findViewById(R.id.date))).setText(lesson
 				.getString("date"));
 
 		ArrayList<JSONObject> resources = new ArrayList<JSONObject>();
 		JSONArray resourcesJSON = data.getJSONArray("resources");
 		for (int i = 0; i < resourcesJSON.length(); i++) {
 			resources.add(resourcesJSON.getJSONObject(i));
 		}
 		ArrayList<JSONObject> comments = new ArrayList<JSONObject>();
 		JSONArray commentsJSON = data.getJSONArray("comments");
 		for (int i = 0; i < commentsJSON.length(); i++) {
 			comments.add(commentsJSON.getJSONObject(i));
 		}
 		// ListView list = new ListView(this);
 		((ListView) (getTab(TAB_RESOURCES).findViewById(R.id.resources)))
 				.setAdapter(new ResourceAdapter(this,
 						android.R.layout.simple_list_item_1, resources));
 		((ListView) (getTab(TAB_COMMENTS).findViewById(R.id.comments)))
 				.setAdapter(new CommentsAdapter(this,
 						android.R.layout.simple_list_item_1, comments));
 	}
 
 	public class ResourceAdapter extends ArrayAdapter<JSONObject> {
 
 		public List<JSONObject> resources;
 
 		public ResourceAdapter(Context context, int textViewResourceId,
 				List<JSONObject> objects) {
 			super(context, textViewResourceId, objects);
 			this.resources = objects;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.resource_row, null);
 			}
 			final JSONObject resource = resources.get(position);
 			try {
 				((TextView) (v.findViewById(R.id.filename))).setText(resource
 						.getString("name"));
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			v.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					new DownloadTask(Lesson.this, cm).execute(resource);
 				}
 			});
 
 			return v;
 		}
 	}
 
 	public class CommentsAdapter extends ArrayAdapter<JSONObject> {
 
 		public List<JSONObject> comments;
 
 		public CommentsAdapter(Context context, int textViewResourceId,
 				List<JSONObject> objects) {
 			super(context, textViewResourceId, objects);
 			this.comments = objects;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.comment_row, null);
 			}
 			JSONObject comment = comments.get(position);
 			try {
 
 				((TextView) (v.findViewById(R.id.content))).setText(comment
 						.getString("content"));
 				((TextView) (v.findViewById(R.id.added))).setText(comment
 						.getString("added"));
 				((TextView) (v.findViewById(R.id.author))).setText(comment
 						.getJSONObject("user").getString("firstname")
 						+ " "
 						+ comment.getJSONObject("user").getString("lastname"));
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return v;
 		}
 	}
 }
