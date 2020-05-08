 package eu.trentorise.smartcampus.jp;
 
 import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoadType;
 
 import java.util.Date;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import eu.trentorise.smartcampus.android.feedback.fragment.FeedbackFragment;
 import eu.trentorise.smartcampus.jp.helper.AlertRoadsHelper;
 import eu.trentorise.smartcampus.jp.model.AlertRoadLoc;
 
 public class SmartCheckAlertDetailsFragment extends FeedbackFragment {
 
 	public static final String ARG_ALERT = "alert";
 
 	private AlertRoadLoc alert;
 
 	public SmartCheckAlertDetailsFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.alert = (AlertRoadLoc) getArguments().getSerializable(ARG_ALERT);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.smartcheck_alert_details, container, false);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		TextView title = (TextView) getView().findViewById(R.id.smartcheck_alertdetails_title);
 		LinearLayout typesLayout = (LinearLayout) getView().findViewById(R.id.smartcheck_alertdetails_types);
 		ImageButton goToMapBtn = (ImageButton) getView().findViewById(R.id.smartcheck_alertdetails_gotomap);
 		TextView description = (TextView) getView().findViewById(R.id.smartcheck_alertdetails_description);
 
 		// title
 		title.setText(alert.getRoad().getStreet());
 
 		// types
 		if (alert.getChangeTypes().length > 0) {
			typesLayout.removeAllViews();
 			for (AlertRoadType type : alert.getChangeTypes()) {
 				ImageView typeImageView = new ImageView(getActivity());
 				typeImageView.setImageResource(AlertRoadsHelper.getDrawableResourceByType(type));
 				final float scale = getActivity().getResources().getDisplayMetrics().density;
 				int pixels = (int) (36 * scale + 0.5f);
 				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pixels, pixels);
 				typeImageView.setLayoutParams(params);
 				typesLayout.addView(typeImageView);
 			}
 			typesLayout.setVisibility(View.VISIBLE);
 		} else {
 			typesLayout.setVisibility(View.GONE);
 		}
 
 		// go to map
 		goToMapBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Toast.makeText(getSherlockActivity(), "TODO",
 				// Toast.LENGTH_SHORT).show();
 				goToMap();
 			}
 		});
 
 		// description
 		description.setText(alert.getDescription());
 
 		// effect
 		if (alert.getEffect() != null && alert.getEffect().length() > 0) {
 			TextView effect = (TextView) getView().findViewById(R.id.smartcheck_alertdetails_effect);
 			effect.setText(alert.getEffect());
 			effect.setVisibility(View.VISIBLE);
 		}
 
 		// time
 		if (alert.getFrom() > 0 && alert.getTo() > 0) {
 			TextView time = (TextView) getView().findViewById(R.id.smartcheck_alertdetails_time);
 			String from = Config.FORMAT_DATE_UI_LONG.format(new Date(alert.getFrom()));
 			String to = Config.FORMAT_DATE_UI_LONG.format(new Date(alert.getTo()));
 			time.setText(getString(R.string.smart_check_alerts_time, from, to));
 			time.setVisibility(View.VISIBLE);
 		}
 
 		// numbers
 		if (alert.getRoad().getFromNumber() != null && alert.getRoad().getFromNumber().length() > 0
 				&& alert.getRoad().getToNumber() != null && alert.getRoad().getToNumber().length() > 0) {
 			TextView numbers = (TextView) getView().findViewById(R.id.smartcheck_alertdetails_numbers);
 			numbers.setText(getString(R.string.smart_check_alerts_numbers, alert.getRoad().getFromNumber(), alert.getRoad()
 					.getToNumber()));
 			numbers.setVisibility(View.VISIBLE);
 		}
 
 		// intersections
 		if (alert.getRoad().getFromIntersection() != null && alert.getRoad().getFromIntersection().length() > 0
 				&& alert.getRoad().getToIntersection() != null && alert.getRoad().getToIntersection().length() > 0) {
 			TextView intersections = (TextView) getView().findViewById(R.id.smartcheck_alertdetails_intersections);
 			intersections.setText(getString(R.string.smart_check_alerts_intersections, capitalize(alert.getRoad()
 					.getFromIntersection()), capitalize(alert.getRoad().getToIntersection())));
 			intersections.setVisibility(View.VISIBLE);
 		}
 
 		// note
 		if (alert.getNote() != null && alert.getNote().trim().length() > 0 && !alert.getNote().trim().startsWith("-")) {
 			TextView note = (TextView) getView().findViewById(R.id.smartcheck_alertdetails_note);
 			note.setText(alert.getNote());
 			note.setVisibility(View.VISIBLE);
 		}
 
 	}
 
 	private void goToMap() {
 		if (alert != null) {
 			AlertRoadsHelper.setFocused(alert);
 			// getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(1);
 
 			FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
 			Fragment fragment = new SmartCheckAlertsMapV2Fragment();
 			// Bundle args = new Bundle();
 			// args.putSerializable(SmartCheckAlertDetailsFragment.ARG_ALERT,
 			// alert);
 			// fragment.setArguments(args);
 			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 			fragmentTransaction.replace(Config.mainlayout, fragment, "lines");
 			fragmentTransaction.addToBackStack(fragment.getTag());
 			// fragmentTransaction.commitAllowingStateLoss();
 			fragmentTransaction.commit();
 		}
 	}
 
 	private String capitalize(String s) {
 		String capitalized = "";
 		String[] splitted = s.split(" ");
 		for (int i = 0; i < splitted.length; i++) {
 			if (i > 0) {
 				capitalized += " ";
 			}
 			String word = splitted[i];
 			capitalized += word.substring(0, 1).toUpperCase() + word.substring(1);
 		}
 		return capitalized;
 	}
 }
