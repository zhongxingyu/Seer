 package com.nari.somnium.fragment;
 
 import java.util.ArrayList;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ExpandableListView;
 
 import com.nari.somnium.R;
 import com.nari.somnium.adapter.CLExpandableListAdapter;
 import com.nari.somnium.helper.DatabaseHelper;
 import com.nari.somnium.pojo.CLDataPojo;
 import com.nari.somnium.pojo.CLSubDataPojo;
 
 /**
  * @author Nari Kim Shin (wassupnari@gmail.com)
  */
 
 public class FragmentCL extends Fragment {
 
 	private static final int NUM_OF_CL_PJT = 10;
 
 	private CLExpandableListAdapter mAdapter;
 	private ExpandableListView expandableListView;
 
 	private ArrayList<String> mGroupItem = new ArrayList<String>();
 	private ArrayList<Object> mChildItem = new ArrayList<Object>();
 	
 	private DatabaseHelper mDB;
 	
 	private int mWidth;
 	
 	private int[] subCount = new int[NUM_OF_CL_PJT];
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.fragment_meeting_list, null);
 
 		expandableListView = (ExpandableListView) view
 				.findViewById(R.id.expandable_list_meeting);
 		
 		mDB = new DatabaseHelper(getActivity());
 		
 		setGroupData();
 		setChildData();
 		
 		for(int i=0; i<NUM_OF_CL_PJT; i++) {
 			CLDataPojo data = new CLDataPojo();
 			ArrayList<CLSubDataPojo> subList = getList(subCount[i]);
 			data.setSubData(subList);
 			mDB.addCLData(i, data);
 		}
 
 		DisplayMetrics metrics = new DisplayMetrics();
 		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		mWidth = metrics.widthPixels;
 
 		mAdapter = new CLExpandableListAdapter(mGroupItem, mChildItem);
 		mAdapter.setInflater(inflater, getActivity());
 
 		return view;
 	}
 	
 	
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		expandableListView.setAdapter(mAdapter);
 		expandableListView.setIndicatorBounds(mWidth - 70, mWidth - 20);
 	}
 
 	public ArrayList<CLSubDataPojo> getList(int count) {
 		ArrayList<CLSubDataPojo> list = new ArrayList<CLSubDataPojo>();
 		
 		for(int i=0; i<count; i++) {
 			CLSubDataPojo subData = new CLSubDataPojo();
 			list.add(subData);
 		}
 		return list;
 	}
 
 	public void setGroupData() {
 
 		mGroupItem.add("1. Listening");
 		mGroupItem.add("2. Critical Thinking");
 		mGroupItem.add("3. Giving Feedback");
 		mGroupItem.add("4. Time Management");
 		mGroupItem.add("5. Planning and Implementation");
 		mGroupItem.add("6. Organization and Delegation");
 		mGroupItem.add("7. Facilitation");
 		mGroupItem.add("8. Motivation");
 		mGroupItem.add("9. Mentoring");
 		mGroupItem.add("10. Team Building");
 	}
 
 	public void setChildData() {
 
 		ArrayList<String> child;
 
 		// PJT #1
 		child = new ArrayList<String>();
 		child.add("Ah-Counter");
 		child.add("Speech Evaluator");
 		child.add("Grammarian");
 		child.add("Table Topics Speaker");
 		subCount[0] = 4;
 		mChildItem.add(child);
 
 		// PJT #2
 		child = new ArrayList<String>();
 		child.add("Speech Evaluator");
 		child.add("Grammarian");
 		child.add("General Evaluator");
 		subCount[1] = 3;
 		mChildItem.add(child);
 
 		// PJT #3
 		child = new ArrayList<String>();
 		child.add("Speech Evaluator");
 		child.add("Grammarian");
 		child.add("General Evaluator");
 		subCount[2] = 3;
 		mChildItem.add(child);
 
 		// PJT #4
 		child = new ArrayList<String>();
 		child.add("Timer");
 		child.add("Toastmaster");
 		child.add("Speaker");
 		child.add("Topicmaster");
 		child.add("Grammarian");
 		subCount[3] = 5;
 		mChildItem.add(child);
 
 		// PJT #5
 		child = new ArrayList<String>();
 		child.add("Speaker");
 		child.add("General Evaluator");
 		child.add("Toastmaster");
 		child.add("Topicsmaster");
 		subCount[4] = 4;
 		mChildItem.add(child);
 
 		// PJT #6
 		child = new ArrayList<String>();
 		child.add("Help Organize a Club Speech Contest");
 		child.add("Help Organize a Clup Special Event");
 		child.add("Help Organize a Membership Campaign or Contest");
 		child.add("Help Organize a Club PR Campaign");
 		child.add("Help Produce a Club Newsletter");
 		child.add("Assist the Club's Webmaster");
 		subCount[5] = 6;
 		mChildItem.add(child);
 
 		// PJT #7
 		child = new ArrayList<String>();
 		child.add("Toastmaster");
 		child.add("General Evaluator");
 		child.add("Topicsmaster");
 		child.add("Befreind a guest");
 		subCount[6] = 4;
 		mChildItem.add(child);
 
 		// PJT #8
 		child = new ArrayList<String>();
 		child.add("Membership Compaign or Contest Chair");
 		child.add("PR Campaign Chair");
 		child.add("Toastmaster");
 		child.add("Speech Evaluator");
 		child.add("General Evaluator");
 		subCount[7] = 5;
 		mChildItem.add(child);
 
 		// PJT #9
 		child = new ArrayList<String>();
 		child.add("Mentor for a New Member");
 		child.add("Mentor for an Existing Member");
 		child.add("HPL Guidance Committee Member");
 		subCount[8] = 3;
 		mChildItem.add(child);
 
 		// PJT #10
 		child = new ArrayList<String>();
 		child.add("Toastmaster");
 		child.add("General Evaluator");
 		child.add("Membership Campaign Chair");
 		child.add("Club PR Campaign Chair");
 		child.add("Club Speech Contest Chair");
 		child.add("Club Special Event Chair");
 		child.add("Club Newsletter Chair");
 		child.add("Club Webmaster");
 		subCount[9] = 8;
 		mChildItem.add(child);
 
 	}
 }
