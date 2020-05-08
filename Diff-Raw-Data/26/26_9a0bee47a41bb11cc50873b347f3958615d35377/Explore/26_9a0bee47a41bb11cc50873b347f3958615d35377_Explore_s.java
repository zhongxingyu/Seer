 package winning.pwnies.recreativity;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import android.app.Fragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 
 public class Explore extends Fragment {
 	private List<FlowDisplay> views;
 	@SuppressWarnings("unused")
 	private Button viewFlowButton;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Collection<Flow> col = Data.getAllFlows();
 		views = new ArrayList<FlowDisplay>();
 		for (Flow f : col) {
 			views.add(new FlowDisplay(f));
 		}		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View myFragmentView = inflater.inflate(R.layout.explore_layout, container, false);
		RelativeLayout ll = (RelativeLayout)myFragmentView.findViewById(R.id.flowlistview);
 		int i = 0;
 		for (FlowDisplay f : views) {
 			View itemView = inflater.inflate(R.layout.flow_view_layout, ll);
 			
 			ContentView left = (ContentView) itemView.findViewById(R.id.contentViewLeft);
 			ContentView center = (ContentView) itemView.findViewById(R.id.contentViewMiddle);
 			ContentView right = (ContentView) itemView.findViewById(R.id.contentViewRight);
 			Submission[] out = f.view();
 			if (out[0] != null) {
 				left.setContent(out[0].getContent());
 			}
 			center.setContent(out[1].getContent());
 			if (out[2] != null) {
 				right.setContent(out[2].getContent());
 			}
 			Log.e("counter", Integer.toString(++i));
 		}
 		
 		View pic4 = myFragmentView.findViewById(R.id.imageView4);
 		pic4.setOnClickListener(new SubmissionListener(3, 0));
 		return myFragmentView;
 	}
 	
 	public void goToFlow(int flow, int submission) {
 		Intent intent = new Intent(getActivity(), ViewSubmissionActivity.class);
 		intent.putExtra(Data.FLOW, flow);
 		intent.putExtra(Data.SUBMISSION, submission);
 		startActivity(intent);
 	}
 	
 	private class SubmissionListener implements View.OnClickListener {
 		private final int flow;
 		private final int submission;
 		
 		public SubmissionListener(int flow, int submission) {
 			this.flow = flow;
 			this.submission = submission;
 		}
 		
 		public void onClick(View v) {
 			Flow f = Data.getFlow(flow);
 			Log.d("Explore", f.serialNumber() + "");
 			goToFlow(flow, submission);
 		}
 	}
 }
 	
 
 
 
