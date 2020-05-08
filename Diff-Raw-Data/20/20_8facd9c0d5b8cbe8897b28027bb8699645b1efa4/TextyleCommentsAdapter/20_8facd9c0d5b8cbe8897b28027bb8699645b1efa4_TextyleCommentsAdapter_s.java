 package com.arnia.karybu.textyle.comments;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.text.Html;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.arnia.karybu.KarybuFragment;
 import com.arnia.karybu.R;
 import com.arnia.karybu.classes.KarybuComment;
 import com.arnia.karybu.classes.KarybuHost;
 import com.arnia.karybu.classes.KarybuResponse;
 import com.arnia.karybu.controls.KarybuDialog;
 
 //Adapter for the listView with KarybuComments
 public class TextyleCommentsAdapter extends BaseAdapter {
 	private Context context;
 	private ArrayList<KarybuComment> comments;
 
 	private ArrayList<KarybuComment> arrayWithReplies;
 	private ArrayList<KarybuComment> arrayWithComments;
 
 	// getter
 	public ArrayList<KarybuComment> getArrayWithComments() {
 		return arrayWithComments;
 	}
 
 	// constructor
 	public TextyleCommentsAdapter(Context context) {
 		this.context = context;
 		this.comments = new ArrayList<KarybuComment>();
 
 		arrayWithReplies = new ArrayList<KarybuComment>();
 		arrayWithComments = new ArrayList<KarybuComment>();
 	}
 
 	// prepare the KarybuComments array for the indentation of reply comments
 	private void prepareArrayWithCommentsAndArrayWithReplies() {
 		// sort the array
 		Collections.sort(comments);
 
 		// put the comments in two arrays: one with replies and one with simple
 		// comments
 		for (KarybuComment comment : this.comments) {
 			if (comment.parent_srl.equals("0"))
 				arrayWithComments.add(comment);
 			else
 				arrayWithReplies.add(comment);
 		}
 
 		// add the reply comments after parent comment
 		for (KarybuComment reply : this.arrayWithReplies) {
 			int index = indexInArrayForCommentWithModuleSRL(reply.parent_srl);
 			arrayWithComments.add(index, reply);
 		}
 	}
 
 	// returns the index where the reply comment should be introduced
 	private int indexInArrayForCommentWithModuleSRL(String document_srl) {
 		for (int i = 0; i < arrayWithComments.size(); i++) {
 			KarybuComment comment = arrayWithComments.get(i);
 
 			if (comment.comment_srl.equals(document_srl))
 				return i + 1;
 
 		}
 
 		return 0;
 	}
 
 	// setter
 	public void setComments(ArrayList<KarybuComment> comments) {
 		arrayWithComments.clear();
 		arrayWithReplies.clear();
 		if (comments != null)
 			this.comments = comments;
 		prepareArrayWithCommentsAndArrayWithReplies();
 	}
 
 	@Override
 	public int getCount() {
 		return arrayWithComments.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		return null;
 	}
 
 	@Override
 	public long getItemId(int arg0) {
 		return 0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup arg2) {
 		final KarybuComment comment = arrayWithComments.get(position);
 
 		if (convertView == null) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = inflater.inflate(R.layout.cellview_commentitem, null);
 		}
 
 		TextView nickname = (TextView) convertView
 				.findViewById(R.id.TEXTYLE_COMMENTS_NICKNAMETEXTVIEW);
 		nickname.setText(comment.nickname);
 
 		// reply comments indentation
 		if (!comment.parent_srl.equals("0"))
 			nickname.setPadding(25, 0, 0, 0);
 		else
 			nickname.setPadding(0, 0, 0, 0);
 
 		TextView txtComment = (TextView) convertView
 				.findViewById(R.id.TEXTYLE_COMMENTS_CONTENT);
 		txtComment.setText(Html.fromHtml(comment.content));
 
 		Button btnPublish = (Button) convertView
 				.findViewById(R.id.TEXTYLE_COMMENTS_PUBLISH);
 		if (comment.status.equals("1"))
 			btnPublish.setText(context.getString(R.string.unpublish_comment));
 		else
 			btnPublish.setText(context.getString(R.string.publish_comment));
 
 		btnPublish.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				publishComment(comment);
 			}
 		});
 
 		Button delete = (Button) convertView
 				.findViewById(R.id.TEXTYLE_COMMENTS_DELETE);
 		delete.setTag(position);
 		delete.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final KarybuDialog dialog = new KarybuDialog(context);
 				dialog.setTitle(R.string.delete_comment);
 				dialog.setMessage(R.string.delete_comment_msg);
 				dialog.setPositiveButton(R.string.yes, new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						DeleteCommentAsyncTask task = new DeleteCommentAsyncTask(
 								comment);
 						task.execute();
 						dialog.dismiss();
 					}
 				});
 				dialog.setNegativeButton(R.string.no);
 				dialog.show();
 			}
 		});
 
 		return convertView;
 	}
 
 	public void clearData() {
 		comments = new ArrayList<KarybuComment>();
 		arrayWithReplies = new ArrayList<KarybuComment>();
 		arrayWithComments = new ArrayList<KarybuComment>();
 		notifyDataSetChanged();
 	}
 
	private void publishComment(KarybuComment comment) {
		boolean newPublishStatus = comment.status.equals("0");
 		ChangeCommentPublishStatus task = new ChangeCommentPublishStatus(
 				comment);
 		task.execute(newPublishStatus);
 	}
 
 	private class ChangeCommentPublishStatus extends
 			AsyncTask<Boolean, Void, Boolean> {
 
 		private KarybuComment comment;
 
 		public ChangeCommentPublishStatus(KarybuComment comment) {
 			this.comment = comment;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			KarybuFragment.startProgress(context,
 					context.getString(R.string.processing));
 		}
 
 		@Override
 		protected Boolean doInBackground(Boolean... params) {
			boolean newStatus = params[0];
 
 			HashMap<String, String> ps = new HashMap<String, String>();
 			ps.put("module", "mobile_communication");
 			ps.put("act", "procmobile_communicationManagePublishCommentStatus");
 			ps.put("will_publish", newStatus ? "1" : "0");
 			ps.put("cart[]", comment.comment_srl);
 
 			try {
 				String strResponse = KarybuHost.getINSTANCE().postMultipart(ps,
 						"/");
 				Log.i("leapkh", "Resp: " + strResponse);
 
 				Serializer serializer = new Persister();
 				Reader reader = new StringReader(strResponse);
 				KarybuResponse response = serializer.read(KarybuResponse.class,
 						reader);
 				if (response.error == 0)
 					return true;
 				else
 					return false;
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				return false;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			super.onPostExecute(result);
 			KarybuFragment.dismissProgress();
 			if (result) {
				String newStatus = comment.equals("0") ? "1" : "0";
				Log.i("leapkh", "New: " + newStatus);
				arrayWithComments.get(arrayWithComments.indexOf(comment)).status = newStatus;
 				notifyDataSetChanged();
 			} else {
 				Toast.makeText(context, "Change published status fail.",
 						Toast.LENGTH_LONG).show();
 			}
 
 		}
 
 	}
 
 	private class DeleteCommentAsyncTask extends AsyncTask<Void, Void, Boolean> {
 
 		private KarybuComment comment;
 
 		public DeleteCommentAsyncTask(KarybuComment comment) {
 			this.comment = comment;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			KarybuFragment.startProgress(context,
 					context.getString(R.string.processing));
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 
 			HashMap<String, String> ps = new HashMap<String, String>();
 			ps.put("module", "mobile_communication");
 			ps.put("act", "procmobile_communicationDeleteComment");
 			ps.put("cart[]", comment.comment_srl);
 
 			try {
 				String strResponse = KarybuHost.getINSTANCE().postMultipart(ps,
 						"/");
 				Log.i("leapkh", "Resp: " + strResponse);
 
 				Serializer serializer = new Persister();
 				Reader reader = new StringReader(strResponse);
 				KarybuResponse response = serializer.read(KarybuResponse.class,
 						reader);
 				if (response.error == 0)
 					return true;
 				else
 					return false;
 
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				return false;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			super.onPostExecute(result);
 			KarybuFragment.dismissProgress();
 			if (result) {
 				arrayWithComments.remove(comment);
 				notifyDataSetChanged();
 			} else {
 				Toast.makeText(context, "Delete comment fail.",
 						Toast.LENGTH_LONG).show();
 			}
 
 		}
 
 	}
 
 }
