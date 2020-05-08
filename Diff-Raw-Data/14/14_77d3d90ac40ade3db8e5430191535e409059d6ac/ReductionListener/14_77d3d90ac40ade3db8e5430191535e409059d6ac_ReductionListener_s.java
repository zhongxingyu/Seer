 package woodsie.avalanche.listener;
 
 import static android.view.View.GONE;
 import static android.view.View.VISIBLE;
 import static woodsie.avalanche.data.GroupSize.LARGE;
 import static woodsie.avalanche.data.GroupSize.LARGE_SPACED;
 import static woodsie.avalanche.data.GroupSize.SMALL;
 import static woodsie.avalanche.data.GroupSize.SMALL_SPACED;
 import static woodsie.avalanche.data.Hazard.CONSIDERABLE;
 import static woodsie.avalanche.data.Hazard.HIGH;
 import static woodsie.avalanche.data.Hazard.LOW;
 import static woodsie.avalanche.data.Hazard.MODERATE;
 import static woodsie.avalanche.data.Hazard.VERY_HIGH;
 import static woodsie.avalanche.data.Steepness.MODERATELY_STEEP;
 import static woodsie.avalanche.data.Steepness.NOT_STEEP;
 import static woodsie.avalanche.data.Steepness.STEEP;
 import static woodsie.avalanche.data.Steepness.VERY_STEEP;
 import static woodsie.avalanche.data.Steepness.VERY_VERY_STEEP;
 import static woodsie.avalanche.data.Terrain.TRACKED;
 import static woodsie.avalanche.data.Terrain.UNTRACKED;
 import static woodsie.avalanche.data.Where.ALL_ASPECTS;
 import static woodsie.avalanche.data.Where.AVOID_CRITICAL;
 import static woodsie.avalanche.data.Where.AVOID_NORTH_SECTOR;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import woodsie.avalanche.R;
 import woodsie.avalanche.data.ReductionParams;
 import woodsie.avalanche.processor.ReductionCalculator;
 import android.app.Activity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class ReductionListener implements OnClickListener {
 	private final ReductionCalculator processor = new ReductionCalculator();
 
 	private final Activity activity;
 
 	public ReductionListener(Activity activity) {
 		this.activity = activity;
 	}
 
 	public void onClick(View view) {
 		ReductionParams params = getParamsFromScreen();
 
 		showOrHideInputFields(params);
 
		BigDecimal risk = processor.Process(params);
 
 		displayRisk(risk);
 	}
 
 	public ReductionParams getParamsFromScreen() {
 		ReductionParams params = new ReductionParams();
 
 		// Hazard level
 		RadioGroup hazard = (RadioGroup) activity.findViewById(R.id.hazard);
 		switch (hazard.getCheckedRadioButtonId()) {
 		case R.id.low:
 			params.hazardLevel = LOW;
 			break;
 		case R.id.moderate:
 			params.hazardLevel = MODERATE;
 			break;
 		case R.id.considerable:
 			params.hazardLevel = CONSIDERABLE;
 			break;
 		case R.id.high:
 			params.hazardLevel = HIGH;
 			break;
 		case R.id.veryHigh:
 		default:
 			params.hazardLevel = VERY_HIGH;
 			break;
 		}
 
 		CheckBox higherHazard = (CheckBox) activity.findViewById(R.id.higherHazard);
 		params.higherHazard = higherHazard.isChecked();
 
 		// First class
 		RadioGroup steepness = (RadioGroup) activity.findViewById(R.id.steepness);
 		switch (steepness.getCheckedRadioButtonId()) {
 		case R.id.notSteep:
 			params.steepness = NOT_STEEP;
 			break;
 		case R.id.moderatelySteep:
 			params.steepness = MODERATELY_STEEP;
 			break;
 		case R.id.steep:
 			params.steepness = STEEP;
 			break;
 		case R.id.verySteep:
 			params.steepness = VERY_STEEP;
 			break;
 		case R.id.veryVerySteep:
 		default:
 			params.steepness = VERY_VERY_STEEP;
 			break;
 		}
 
 		// Second class
 		CheckBox allAspects = (CheckBox) activity.findViewById(R.id.allAspectsDanger);
 		params.allAspects = allAspects.isChecked();
 
 		CheckBox inverse = (CheckBox) activity.findViewById(R.id.inverse);
 		params.inverse = inverse.isChecked();
 
 		RadioGroup where = (RadioGroup) activity.findViewById(R.id.where);
 		switch (where.getCheckedRadioButtonId()) {
 		case R.id.allAspects:
 			params.where = ALL_ASPECTS;
 			break;
 		case R.id.avoidNorthSector:
 			params.where = AVOID_NORTH_SECTOR;
 			break;
 		case R.id.avoidNorthHalf:
 			params.where = AVOID_NORTH_SECTOR;
 			break;
 		case R.id.avoidCritical:
 		default:
 			params.where = AVOID_CRITICAL;
 			break;
 		}
 
 		CheckBox tracked = (CheckBox) activity.findViewById(R.id.tracked);
 		params.terrain = tracked.isChecked() ? TRACKED : UNTRACKED;
 
 		// Third class
 		RadioGroup groupSize = (RadioGroup) activity.findViewById(R.id.groupSize);
 		switch (groupSize.getCheckedRadioButtonId()) {
 		case R.id.smallGroupSpaced:
 			params.groupSize = SMALL_SPACED;
 			break;
 		case R.id.smallGroup:
 			params.groupSize = SMALL;
 			break;
 		case R.id.largeGroupSpaced:
 			params.groupSize = LARGE_SPACED;
 			break;
 		case R.id.largeGroup:
 		default:
 			params.groupSize = LARGE;
 			break;
 		}
 
 		return params;
 	}
 
 	private void showOrHideInputFields(ReductionParams params) {
 		RadioGroup where = (RadioGroup) activity.findViewById(R.id.where);
 		CheckBox tracked = (CheckBox) activity.findViewById(R.id.tracked);
 		RadioButton avoidNorthSector = (RadioButton) activity.findViewById(R.id.avoidNorthSector);
 		RadioButton avoidNorthHalf = (RadioButton) activity.findViewById(R.id.avoidNorthHalf);
 
 		if (params.allAspects) {
 			avoidNorthSector.setVisibility(VISIBLE);
 			avoidNorthHalf.setVisibility(VISIBLE);
 
 			where.setVisibility(GONE);
 			tracked.setVisibility(GONE);
 
 		} else if (params.inverse) {
 			where.setVisibility(VISIBLE);
 			tracked.setVisibility(VISIBLE);
 
 			avoidNorthSector.setVisibility(GONE);
 			avoidNorthHalf.setVisibility(GONE);
 
 		} else {
 			where.setVisibility(VISIBLE);
 			avoidNorthSector.setVisibility(VISIBLE);
 			avoidNorthHalf.setVisibility(VISIBLE);
 			tracked.setVisibility(VISIBLE);
 		}
 
 	}
 
 	private void displayRisk(BigDecimal risk) {
 		String riskStr = activity.getResources().getString(R.string.dangerLevel);
 		riskStr = riskStr.replace("%", risk.setScale(3, RoundingMode.CEILING).toString());
 
 		TextView text = (TextView) activity.findViewById(R.id.dangerLevelText);
 
 		String message;
 		if (risk.compareTo(BigDecimal.ONE) <= 0) {
 			text.setBackgroundColor(activity.getResources().getColor(R.color.green));
 			message = activity.getResources().getString(R.string.riskSafe);
 		} else {
 			text.setBackgroundColor(activity.getResources().getColor(R.color.red));
 			if (risk.equals(ReductionCalculator.EXTREME)) {
 				message = activity.getResources().getString(R.string.riskExtreme);
 			} else {
 				message = activity.getResources().getString(R.string.riskDangerous);
 			}
 
 		}
 
 		message = riskStr + "\n" + message;
 		text.setText(message);
 
 	}
 }
