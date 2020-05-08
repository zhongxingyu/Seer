 package de.uni.stuttgart.informatik.ToureNPlaner.UI.ConstraintViews;
 
 import android.content.Context;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.FloatConstraint;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 
 public class FloatConstraintView extends ConstraintView {
 	private Context context;
 
	protected final int SEEKBAR_MAX = 10000;
 	private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener;
 
 	public FloatConstraintView(Context context, Constraint constraint) {
 		super(context, constraint);
 		this.context = context;
 		FloatConstraint type = (FloatConstraint) constraint.getType();
 		this.min = type.getMinimum();
 		this.max = type.getMaximum();
 	}
 
 	protected final float min;
 	protected final float max;
 
 	@Override
 	public int getLayout() {
 		return R.layout.float_constraint_layout;
 	}
 
 	/**
 	 * Range 0.0 - 1.0
 	 */
 	protected float barToValueNormalized(float bar) {
 		return bar;
 	}
 
 	/**
 	 * Range 0.0 - 1.0
 	 *
 	 * @return
 	 */
 	protected float valueToBarNormalized(float value) {
 		return value;
 	}
 
 	protected float barToValue(int bar) {
 		return barToValueNormalized((float) bar / SEEKBAR_MAX) * (max - min) + min;
 	}
 
 	protected int valueToBar(float value) {
 		return (int) (valueToBarNormalized((value - min) / (max - min)) * SEEKBAR_MAX);
 	}
 
 	@Override
 	protected void setup() {
 		TextView lblMin = (TextView) view.findViewById(R.id.lblconstMin);
 		TextView lblMax = (TextView) view.findViewById(R.id.lblconstMax);
 		final EditText etValue = (EditText) view.findViewById(R.id.txtconstValue);
 
 		final FloatConstraint type = (FloatConstraint) constraint.getType();
 
 		lblMin.setText(String.valueOf(type.getMinimum()));
 		lblMax.setText(String.valueOf(type.getMaximum()));
 
 
 		etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
 
 		//------------------get seekBar -------------------
 		final SeekBar seekbar = (SeekBar) view.findViewById(R.id.editconstraintseekBar);
 		seekbar.setMax(SEEKBAR_MAX);
 
 		if (constraint.getValue() != null) {
 			float val = (Float) constraint.getValue();
 			seekbar.setProgress(valueToBar(val));
 			etValue.setText(Float.toString(val));
 		} else {
 			etValue.setHint(context.getResources().getString(R.string.select_a_value));
 		}
 		etValue.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				String val = etValue.getEditableText().toString();
 				if (val != null && !val.equals("")) {
 					try {
 						float valfloat = Float.valueOf(val);
 						if (valfloat > type.getMaximum()) {
 							valfloat = type.getMaximum();
 						}
 						if (valfloat < type.getMinimum()) {
 							valfloat = type.getMinimum();
 						}
 						constraint.setValue(valfloat);
 						// prevent infinite loop
 						seekbar.setOnSeekBarChangeListener(null);
 						seekbar.setProgress(valueToBar(valfloat));
 						seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
 					} catch (NumberFormatException e) {
 						// ignore
 					}
 				}
 			}
 		});
 		onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
 			@Override
 			public void onProgressChanged(SeekBar arg0, int arg1,
 			                              boolean arg2) {
 				etValue.setText(Float.toString(barToValue(arg1)));
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar arg0) {
 
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar arg0) {
 
 			}
 		};
 		seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
 	}
 }
