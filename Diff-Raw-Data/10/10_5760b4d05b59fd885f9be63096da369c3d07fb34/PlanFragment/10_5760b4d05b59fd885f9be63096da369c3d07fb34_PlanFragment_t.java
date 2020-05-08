 package de.gymbuetz.gsgbapp;
 
 import java.io.IOException;
 
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import de.gymbuetz.gsgbapp.classes.Constants;
 import de.gymbuetz.gsgbapp.classes.DownloadXmlTask;
 import de.gymbuetz.gsgbapp.classes.FileFunctions;
 import de.gymbuetz.gsgbapp.classes.HelpFunctions;
 import de.gymbuetz.gsgbapp.classes.RepPlan;
 import de.gymbuetz.gsgbapp.classes.RepPlanDay;
 
 public class PlanFragment extends Fragment {
 
 	String repPlanString = null;
 	RepPlan repPlan = null;
 	LinearLayout rootView;
 
 	public PlanFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Start displaying
 		rootView = (LinearLayout) inflater.inflate(R.layout.plan_fragment, container, false);
 
 		Log.i("App-state", "Start, try to load repPlan from Application");
 		repPlanString = ((Application) this.getActivity().getApplication()).getRepPlanString();
 		repPlan = ((Application) this.getActivity().getApplication()).getRepPlan();
 
 		if (repPlanString == null | repPlan == null) {
 			Log.i("App-state", "Start, loading repPlan");
 			if (HelpFunctions.inetAvailable(getActivity())) {
 				if (HelpFunctions.inetAllowed(getActivity())) {
 					loadReplacementData();
 				} else {
 					try {
 						Log.i("App-state", "offline mode");
 
 						((Application) this.getActivity().getApplication()).setRepPlanString(FileFunctions.loadXmlfromFile(
 								Constants.REPPLAN_FILE_NAME, getActivity()));
 						repPlanString = ((Application) this.getActivity().getApplication()).getRepPlanString();
 						((Application) this.getActivity().getApplication()).setRepPlan(RepPlan.parseXml(repPlanString));
 						repPlan = ((Application) this.getActivity().getApplication()).getRepPlan();
 
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			} else {
 				try {
 					Log.i("App-state", "not connected mode");
 					repPlanString = FileFunctions.loadXmlfromFile(Constants.REPPLAN_FILE_NAME, getActivity());
 					repPlan = RepPlan.parseXml(repPlanString);
 					((Application) this.getActivity().getApplication()).setRepPlanString(repPlanString);
 					((Application) this.getActivity().getApplication()).setRepPlan(repPlan);
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return rootView;
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (repPlan != null)
 			fillView();
 	}
 
 	public void fillView() {
 		rootView.removeAllViews();
 		for (RepPlanDay rpd : repPlan.getDays()) {
 			if (rpd.getRepList().size() > 0) {
 				final String date = rpd.getDate();
 
 				Button btn = new Button(getActivity());
 				btn.setText(date);
 				btn.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						Intent i = new Intent(getActivity().getApplicationContext(), DayActivity.class);
 						i.putExtra("result", repPlanString);
 						i.putExtra("date", date);
 						startActivity(i);
 					}
 
 				});
 				rootView.addView(btn);
 			}
 		}
 	}
 
	
	
 	private void loadReplacementData() {
 		Log.i("Internet", "Loading repPlan from Server");
 
 		ProgressDialog pd = new ProgressDialog(getActivity());
 		pd.setProgress(0);
 		pd.setTitle(R.string.loading);
 
 		pd.setOnCancelListener(new OnCancelListener() {
 
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				try {
 					Log.i("App-state", "after download of repPlan");
 					repPlanString = FileFunctions.loadXmlfromFile(Constants.REPPLAN_FILE_NAME, getActivity());
 					repPlan = RepPlan.parseXml(repPlanString);
					saveData();
 					fillView();
 				} catch (IOException e) {
 					Log.w("App-state", "Failed to load downloaded repPlan file");
 					e.printStackTrace();
 				}
 			}
 		});
 		pd.show();
 		new DownloadXmlTask(getActivity(), Constants.REPPLAN_URL, Constants.REPPLAN_FILE_NAME, pd).execute();
 	}
 
	protected void saveData() {
		((Application) this.getActivity().getApplication()).setRepPlanString(repPlanString);
		((Application) this.getActivity().getApplication()).setRepPlan(repPlan);
	}

 }
