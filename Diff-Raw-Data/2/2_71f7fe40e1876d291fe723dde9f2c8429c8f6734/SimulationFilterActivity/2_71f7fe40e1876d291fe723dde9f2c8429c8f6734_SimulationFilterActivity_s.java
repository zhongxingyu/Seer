 package br.usp.ime.tcc.activities.filter;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import br.usp.ime.tcc.activities.ContinousFilterActivity;
 import br.usp.ime.tcc.activities.R;
 import br.usp.ime.tcc.activities.components.ComponentUtils;
 import br.usp.ime.tcc.utils.Constants;
 
 public class SimulationFilterActivity extends FilterActivity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	protected void loadSpecificComponents() {
 		ComponentUtils componentUtils = new ComponentUtils(this);
 
 		componentUtils.loadButton(R.id.liveModeButton, new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent continousModeIntent = new Intent(
 						SimulationFilterActivity.this,
 						ContinousFilterActivity.class);
				continousModeIntent.putExtra(Constants.FILTER_TYPE, Constants.VISOCOR_FILTER);
 				startActivity(continousModeIntent);
 			}
 		});
 
 		componentUtils.loadTextViewWithText(R.id.filter_title, getString(R.string.simulation_filter));
 		componentUtils.showSpinner(R.id.filter_type_spinner);
 	}
 	
 	@Override
 	protected void putFilterTypeExtra(Intent intent) {
 		int filterType;
 		
 		ComponentUtils cu = new ComponentUtils(this);
 		filterType = cu.getSpinnerPosition(R.id.filter_type_spinner) + 2;
 		intent.putExtra(Constants.FILTER_TYPE, filterType);		
 	}
 }
