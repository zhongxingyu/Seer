 package com.randallma.whatsthehomework;
 
 import java.util.ArrayList;
 
 import android.content.Context;
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
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 
 		if (convertView == null) {
 			convertView = mInflater.inflate(R.layout.assignment_list_row_view,
 					null);
 			holder = new ViewHolder();
 
 			holder.txtSchoolClass = (TextView) convertView
 					.findViewById(R.id.schoolClass);
 			holder.txtThumbnail = (ImageView) convertView
 					.findViewById(R.id.thumbnail);
 			holder.txtDateDue = (TextView) convertView
 					.findViewById(R.id.dateDue);
 			holder.txtDateAssigned = (TextView) convertView
 					.findViewById(R.id.dateAssigned);
 			holder.txtDescription = (TextView) convertView
 					.findViewById(R.id.description);
 
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		holder.txtSchoolClass.setText(assignmentArrayList.get(position)
 				.getSchoolClass());
 		boolean imageExists = (assignmentArrayList.get(position).getImageUri() == null) ? false
 				: true;
 		if (imageExists == true) {
 			holder.txtThumbnail.setImageResource(R.drawable.ic_content_picture);
 		} else {
 			holder.txtThumbnail.setVisibility(View.GONE);
 		}
 		holder.txtDateDue.setText(assignmentArrayList.get(position)
 				.getDateDue());
 		holder.txtDateAssigned.setText(assignmentArrayList.get(position)
 				.getDateAssigned());
 		holder.txtDescription.setText(assignmentArrayList.get(position)
 				.getDescription());
 
 		return convertView;
 	}
 
 	static class ViewHolder {
 		TextView txtSchoolClass;
 		ImageView txtThumbnail;
 		TextView txtDateDue;
 		TextView txtDateAssigned;
 		TextView txtDescription;
 	}
 
 }
