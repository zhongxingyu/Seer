 package uk.org.downesward.dirtside;
 
 import uk.org.downesward.dirtside.domain.CombatResolutionResult;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class CombatResolutionResultFragment extends Fragment {
 
 	public static final String ARG_RESULT = "combat_result";
 
 	private CombatResolutionResult result;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Bundle args = getArguments();
 		
 		if (args != null && args.containsKey(ARG_RESULT)) {
 			result = (CombatResolutionResult) getArguments().getSerializable(
 					ARG_RESULT);
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		// Inflate the layout for this fragment
 
 		View view = inflater.inflate(R.layout.combatresolutionresult,
 				container, false);
 
 		displayResult(view);
 
 		return view;
 
 	}
 
 	public void displayResult(View view) {
 		if (result != null && view != null) {
 			((TextView) view.findViewById(R.id.txtMessages)).setText(result
 					.getChits());
 			((TextView) view.findViewById(R.id.txtResults)).setText(result
 					.getOutcome());
 			((TextView) view.findViewById(R.id.txtRolls)).setText(result
 					.getDieRolls());
 		}
 	}
 	
 	public void setResult(CombatResolutionResult result) {
 		this.result = result;
		View view = this.getActivity().findViewById(R.id.panCombatResult);
 		displayResult(view);
 	}
 }
