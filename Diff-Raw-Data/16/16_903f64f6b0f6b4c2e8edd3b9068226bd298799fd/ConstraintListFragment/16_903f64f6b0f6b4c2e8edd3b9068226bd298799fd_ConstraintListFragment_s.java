 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Fragments;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import com.actionbarsherlock.app.SherlockFragment;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.ConstraintFragments.ConstraintFragment;
 
 import java.util.ArrayList;
 
 public class ConstraintListFragment extends SherlockFragment {
 	public static String IDENTIFIER = "constraints";
 
 	private ArrayList<Constraint> constraints;
 
 	public boolean isDirty() {
 		for (int i = 0; i < constraints.size(); i++) {
 			if (((ConstraintFragment) getFragmentManager().findFragmentById(i + 1)).isDirty()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable(IDENTIFIER, constraints);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// If we get created for the first time we get our data from the intent
 		Bundle data = savedInstanceState != null ? savedInstanceState : getArguments();
 		constraints = (ArrayList<Constraint>) data.getSerializable(IDENTIFIER);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		setupList();
 
 		if (savedInstanceState == null)
 			setupFragments();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.constraint_list, null);
 	}
 
 	private void setupFragments() {
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		for (int i = 0; i < constraints.size(); i++) {
 			ft.add(i + 1, constraints.get(i).createFragment(i));
 		}
 		ft.commit();
 	}
 
 	private void setupList() {
 		LinearLayout list = (LinearLayout) getView().findViewById(android.R.id.list);
 		for (int i = 0; i < constraints.size(); i++) {
 			FrameLayout frameLayout = new FrameLayout(getActivity());
 			frameLayout.setId(i + 1);
 			list.addView(frameLayout);
 		}
 	}
 
 	public static ConstraintListFragment newInstance(ArrayList<Constraint> constraints) {
 		ConstraintListFragment fragment = new ConstraintListFragment();
 		Bundle args = new Bundle();
 		args.putSerializable(IDENTIFIER, constraints);
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	public ArrayList<Constraint> getConstraints() {
 		return constraints;
 	}
 }
