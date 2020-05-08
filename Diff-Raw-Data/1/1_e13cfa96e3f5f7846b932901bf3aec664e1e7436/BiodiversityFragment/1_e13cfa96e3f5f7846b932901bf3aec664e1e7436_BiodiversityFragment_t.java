 package com.example.cubicmetercommunity.app;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import com.example.cubicmetercommunityapp.R;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 //This fragment allows a user to select a role
 
 public class BiodiversityFragment extends Fragment implements OnClickListener {
 
 	String[] bugs, bugPics;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		TableLayout table = (TableLayout) getActivity().findViewById(
 				R.id.TableLayout1);
 		bugs = getResources().getStringArray(R.array.BugNames);
 		bugPics = getResources().getStringArray(R.array.BugPics);
 		Iterator<String> b = Arrays.asList(bugs).iterator();
 		Iterator<String> bPics = Arrays.asList(bugPics).iterator();
 
 		while (b.hasNext()) {
 			TableRow tr = new TableRow(getActivity());
 			for (int i = 0; i < 3; i++) {
 				View v = LayoutInflater.from(getActivity()).inflate(
 						R.layout.bugs_layout, tr, false);
 				((TextView) v.findViewById(R.id.title)).setText(b.next());
 				ImageView image = (ImageView) v.findViewById(R.id.imageView1);
 				String resource = bPics.next();
 				int resID = getResources().getIdentifier(resource,
 					    "drawable", getActivity().getPackageName());
 				image.setImageDrawable(getResources().getDrawable(resID));
 				
 				
 				Button minus = (Button) v.findViewById(R.id.button1);
 				minus.setEnabled(false);
 				minus.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View arg) {
 						View p = (View) arg.getParent();
 						TextView num = (TextView) p.findViewById(R.id.textView1);
 						Button minus = (Button) p.findViewById(R.id.button1);
 						int count = Integer.parseInt(num.getText().toString());
 
 						num.setText(Integer.toString(--count));
 						if (count == 0)
 							minus.setEnabled(false);
 					}
 				});
 				((Button) v.findViewById(R.id.button2))
 						.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View arg) {
 								View p = (View) arg.getParent();
 								TextView num = (TextView) p.findViewById(R.id.textView1);
 								int count = Integer.parseInt(num.getText()
 										.toString());
 								num.setText(Integer.toString(++count));
 								Button minus = (Button) p.findViewById(R.id.button1);
 								minus.setEnabled(true);
 							}
 						});
 				tr.addView(v);
 				if (!b.hasNext()) {
 					break;
 				}
 			}
 
 			table.addView(tr);
 		}
 		Button finish = (Button) getActivity().findViewById(R.id.act_finish);
 		finish.setOnClickListener(this);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.biodiversity_activity_2, container,
 				false);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.act_finish:
 			TableLayout table = (TableLayout) getActivity().findViewById(
 					R.id.TableLayout1);
 
 			for (int i = 0; i < table.getChildCount(); i++) {
 				TableRow row = (TableRow) table.getChildAt(i);
 				for (int j = 0; j < row.getChildCount(); j++) {
 					View view = row.getChildAt(j);
 					String label = ((TextView) view.findViewById(R.id.title))
 							.getText().toString();
 					int count = Integer
 							.parseInt(((TextView) view
 									.findViewById(R.id.textView1)).getText()
 									.toString());
 					Log.d("debug", label + ": " + count);
 
 				}
 			}
 
 		}
 
 	}
 }
