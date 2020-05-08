 package de.wak_sh.client.fragments;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import de.wak_sh.client.R;
 import de.wak_sh.client.backend.ProgressDialogTask;
 import de.wak_sh.client.backend.adapters.ModulePagerAdapter;
 import de.wak_sh.client.backend.service.DataService;
 import de.wak_sh.client.backend.service.ModuleService;
 
 public class NotenuebersichtFragment extends Fragment {
 	private ModuleService moduleService;
 	private ModulePagerAdapter adapter;
 	private ViewPager pager;
 	private TextView textAverage;
 	private TextView textCredits;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_notenuebersicht,
 				container, false);
 
 		adapter = new ModulePagerAdapter(getFragmentManager());
 		pager = (ViewPager) rootView.findViewById(R.id.module_pager);
 		pager.setAdapter(adapter);
 
 		textAverage = (TextView) rootView
 				.findViewById(R.id.txt_overall_durchschnitt);
 		textCredits = (TextView) rootView
 				.findViewById(R.id.txt_overall_credits);
 
		if (moduleService != null) {
			populateUi();
		} else {
			new GradesTask(getActivity()).execute();
		}

 		return rootView;
 	}
 
 	protected void populateUi() {
 		String average = String.format(Locale.getDefault(), "%.2f",
 				moduleService.getAverageGrade(0));
 		String credits = Integer.toString(moduleService.getCredits(0));
 		textAverage.setText(average);
 		textCredits.setText(credits);
 	}
 
 	private class GradesTask extends ProgressDialogTask<Void, Void> {
 		private Activity activity;
 
 		public GradesTask(Activity activity) {
 			super(activity, activity.getString(R.string.fetching_grades));
 			this.activity = activity;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			DataService dataService = DataService.getInstance();
 			moduleService = new ModuleService(dataService);
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
 				moduleService.fetchModules();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			int semesters = moduleService.countSemesters();
 			adapter.setModuleService(moduleService);
 			adapter.notifyDataSetChanged();
 			pager.setCurrentItem(semesters - 1);
 			activity.runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					populateUi();
 				}
 			});
 		}
 	}
 }
