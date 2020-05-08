 package com.randallma.shukudai;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AssignmentAdapter extends BaseAdapter {
 	private static ArrayList<Assignment> assignmentArrayList;
 
 	private final LayoutInflater mInflater;
 
 	public AssignmentAdapter(Context context, ArrayList<Assignment> entries) {
 		assignmentArrayList = entries;
 		mInflater = LayoutInflater.from(context);
 	}
 
 	@Override
 	public int getCount() {
 		return assignmentArrayList.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return assignmentArrayList.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	private static class ThumbnailTask extends AsyncTask {
 		private final int position;
 		private final ViewHolder holder;
 
 		public ThumbnailTask(int position, ViewHolder holder) {
 			this.position = position;
 			this.holder = holder;
 		}
 
 		@Override
 		protected Cursor doInBackground(Object... params) {
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Object result) {
 			if (holder.position == position) {
 				boolean imageExists = (assignmentArrayList.get(position)
 						.getImageUri() == null) ? false : true;
 				if (imageExists == true) {
 					holder.txtThumbnail.setVisibility(View.VISIBLE);
 					holder.txtThumbnail.setImageURI(Uri
 							.parse(assignmentArrayList.get(position)
 									.getImageUri()));
 					ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) holder.txtSchoolClass
 							.getLayoutParams();
 					mlp.setMargins(24, 0, 24, 0);
 					mlp = (ViewGroup.MarginLayoutParams) holder.txtDescription
 							.getLayoutParams();
 					mlp.setMargins(24, 0, 24, 0);
 				} else {
 					holder.txtThumbnail.setVisibility(View.GONE);
 				}
 			}
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.assignment_list_row_view,
 					null);
 			holder = new ViewHolder();
 
 			holder.txtSchoolClass = (TextView) convertView
 					.findViewById(R.id.schoolClass);
 			holder.txtDateDue = (TextView) convertView
 					.findViewById(R.id.dateDue);
 			holder.txtDescription = (TextView) convertView
 					.findViewById(R.id.description);
 			holder.txtThumbnail = (ImageView) convertView
 					.findViewById(R.id.thumbnail);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		// Set SchoolClass
 		holder.txtSchoolClass.setText(assignmentArrayList.get(position)
 				.getSchoolClass());
 
 		// Set DateDue
 		Calendar dateDue = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
 		try {
 			dateDue.setTime(sdf.parse(assignmentArrayList.get(position)
 					.getDateDue()));
 		} catch (ParseException e) {
 			Log.i(PostAssignmentActivity.class.getName(), "Blank date set");
 		}
 		Calendar today = Calendar.getInstance();
		long daysDiff = ((dateDue.getTimeInMillis() - today.getTimeInMillis()) / 86400000) + 1;

		if ((daysDiff <= 0)
				|| (dateDue.getTime().getDate() == today.getTime().getDate()
						&& dateDue.getTime().getMonth() == today.getTime()
								.getMonth() && dateDue.getTime().getYear() == today
						.getTime().getYear())) {
 			holder.txtDateDue.setText("Due");
		} else {
			holder.txtDateDue.setText(Long.toString(daysDiff) + "d");
 		}
 
 		// Set Description
 		holder.txtDescription.setText(assignmentArrayList.get(position)
 				.getDescription());
 
 		// Set Thumbnail
 		holder.position = position;
 		new ThumbnailTask(position, holder).executeOnExecutor(
 				ThumbnailTask.THREAD_POOL_EXECUTOR, null);
 
 		return convertView;
 	}
 
 	static class ViewHolder {
 		TextView txtSchoolClass;
 		ImageView txtThumbnail;
 		TextView txtDateDue;
 		TextView txtDateAssigned;
 		TextView txtDescription;
 		int position;
 	}
 
 }
