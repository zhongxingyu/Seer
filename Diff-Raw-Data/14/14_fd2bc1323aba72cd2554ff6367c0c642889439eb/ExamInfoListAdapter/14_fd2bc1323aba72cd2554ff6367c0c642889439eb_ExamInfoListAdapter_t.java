 package com.cromiumapps.uwhub;
 
 import java.util.ArrayList;
 
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class ExamInfoListAdapter extends BaseAdapter {
 
     // context
     private Context context;
 
     // views
     private LayoutInflater inflater;
 
     // data
     private ArrayList<ExamInfoData> exams;
 
     public ExamInfoListAdapter(Context context, ArrayList<ExamInfoData> exams) {
 		this.context = context;
 		this.exams = exams;
 		inflater = LayoutInflater.from(context);
     }
 
     @Override
 	public int getCount() {
 		return exams.size();
     }
 
     @Override
     public Object getItem(int position) {
 		return exams.get(position);
     }
 
     @Override
     public long getItemId(int position) {
 		return position;
     }
 
     public void setData(ArrayList<ExamInfoData> exams) {
 		this.exams = exams;
     }
     
     public ExamInfoData getData(int position) {
 		return this.exams.get(position);
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 		View v = null;
 		if (convertView != null) {
 			v = convertView;
 		} else {
 		    v = inflater.inflate(R.layout.layout_examinfo, parent, false);
 		}
 
 		ExamInfoData exams = (ExamInfoData) getItem(position);
 
 		TextView tvLAYOUTExamInfoCode = (TextView) v.findViewById(R.id.tvLAYOUTExamInfoCode);
 		TextView tvLAYOUTExamInfoLocation = (TextView) v.findViewById(R.id.tvLAYOUTExamInfoLocation);
 		TextView tvLAYOUTExamInfoDate = (TextView) v.findViewById(R.id.tvLAYOUTExamInfoDate);
 	
		//tvLAYOUTExamInfoCode.setText(exams.getCourseID() + " (" + exams.getSection() + ")");
		tvLAYOUTExamInfoCode.setText(exams.getCourseID());
 		tvLAYOUTExamInfoLocation.setText(exams.getLocation());
 		tvLAYOUTExamInfoDate.setText(exams.getDate());
 	
 		return v;
     }
 
 }
