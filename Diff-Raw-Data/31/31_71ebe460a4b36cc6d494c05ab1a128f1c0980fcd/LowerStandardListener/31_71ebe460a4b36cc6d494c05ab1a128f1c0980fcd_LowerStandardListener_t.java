 package kamkowarrier.collab.resistorreader.Listeners;
 
 import kamkowarrier.collab.resistorreader.R;
 import kamkowarrier.collab.resistorreader.ResistorView;
 import kamkowarrier.collab.resistorreader.ColorBand;
 import kamkowarrier.collab.resistorreader.ResistorAct;
 import kamkowarrier.collab.resistorreader.TextReader;
 import kamkowarrier.collab.resistorreader.ColorBand.MultBand;
 import kamkowarrier.collab.resistorreader.ColorBand.ValBand;
 import android.util.TypedValue;
 import android.view.*;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class LowerStandardListener implements View.OnClickListener {
 
 	private boolean[] pressed;
 	private ResistorView resistorView;
 	private ResistorAct resistorAct;
 
 	public LowerStandardListener(boolean[] pressed, ResistorView resistorView,
 			ResistorAct resistorAct) {
 
 		this.pressed = pressed;
 		this.resistorView = resistorView;
 		this.resistorAct = resistorAct;
 	}
 	@Override
 	public void onClick(View v) {
 		if (TextReader.allowStandards[0]) { 
 			if (!pressed[0]) {
 				
 				pressed[1] = false;
 				pressed[0] = true;
 				TextReader.lower.setBackgroundResource(R.drawable.btn_default_pressed);
 				TextReader.upper.setBackgroundResource(R.drawable.btn_default_normal);
 				TextReader.valueOut.setText(TextReader.standards[0]);
								
 				updateBands();
 				
 
 			}
 			else {
 				pressed[0] = false;
 				TextReader.lower.setBackgroundResource(R.drawable.btn_default_normal);
 				TextReader.valueOut.setText(TextReader.standards[1]);
 				
 				updateBands();
 			}
 		}
 	}
 	
 	public void updateBands() {
 		
 		
 		int numBands = resistorView.bandColors.size();
 		TextReader.setTolerance(new Double(TextReader.tolOut.getText().toString()).doubleValue(),true);
 		TextReader.setBandNum(numBands);
 		
 		TextReader.read(TextReader.valueOut.getText().toString(),false); //also changes lower & upper textviews
 		
 
 		
 		int original = resistorView.activeBandNum;
 		for (int i = 0; i < numBands-1; i++) {
 			resistorView.activeBandNum = i;
 			ColorBand c = new ColorBand(resistorView.getContext());
 			int val = 0;
 			if (i < numBands-2) {
 				ColorBand.ValBand valB = c.new ValBand(resistorView.getContext());
 				val = valB.valueToColor(TextReader.band[i]);
 			}
 			else {
 				ColorBand.MultBand multB = c.new MultBand(resistorView.getContext());
 				val = multB.valueToColor(TextReader.band[i]);
 			}
 			resistorView.updateWithoutCalc(val);
 			
 		}
 		resistorView.activeBandNum = original;
 		if (!TextReader.isStandardVal) {
 			TextReader.ohm.setText("\u26A0");
 			TextReader.ohm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
 		}
 		else {
 			TextReader.ohm.setText(resistorAct.getString(R.string.ohm));
 			TextReader.ohm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
 		}
 	}
 }
