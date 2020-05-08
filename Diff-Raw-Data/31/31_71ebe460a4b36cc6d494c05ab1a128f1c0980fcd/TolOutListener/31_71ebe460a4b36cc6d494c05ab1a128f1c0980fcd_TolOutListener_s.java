 package kamkowarrier.collab.resistorreader.Listeners;
 
 import kamkowarrier.collab.resistorreader.ColorBand;
 import kamkowarrier.collab.resistorreader.R;
 import kamkowarrier.collab.resistorreader.ResistorAct;
 import kamkowarrier.collab.resistorreader.ResistorView;
 import kamkowarrier.collab.resistorreader.TextReader;
 import kamkowarrier.collab.resistorreader.ColorBand.TolBand;
 import android.content.res.Resources;
 import android.text.SpannableString;
 import android.view.*;
 import android.view.View.OnKeyListener;
 import android.widget.TextView;
 
 public class TolOutListener implements OnKeyListener {
 
 	private ResistorAct resistorAct;
 	private ResistorView resistorView;
 	private TextView percent;
 	private SpannableString redX;
 	private double[] storedTols;
 	private String[] boxVals;
 	private Resources r;
 
 	public TolOutListener(ResistorAct resistorAct, ResistorView resistorView,
 			TextView percent, SpannableString redX, double[] storedTols,
 			String[] boxVals, Resources r) {
 		this.r = r;
 		this.resistorAct = resistorAct;
 		this.resistorView = resistorView;
 		this.percent = percent;
 		this.redX = redX;
 		this.storedTols = storedTols;
 		this.boxVals = boxVals;
 
 	}
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if (event.getAction() == KeyEvent.ACTION_DOWN) {
 			switch(keyCode) {
 			case KeyEvent.KEYCODE_ENTER:
 				if (TextReader.tolOut.getText().toString().length() == 0) {
 					TextReader.tolOut.setText(boxVals[1]);
 					break;
 				}
 				int original = resistorView.activeBandNum;
 				int originalColor = resistorView.bandColors.get(original);
 				if (!TextReader.isValidString(TextReader.tolOut.getText().toString(),true)) {
 					percent.setText(redX);
 					break;
 				}
 				double val = Double.valueOf(TextReader.tolOut.getText().toString());
				System.out.println("In listener: " + val);
 				if (resistorView.bandColors.size() == 4) {
 					resistorView.activeBandNum = 3;
 					TextReader.setBandNum(4);
 					val = TextReader.findClosestVal(val,TextReader.validTols);
 					storedTols[0] = val;
 				}
 				else {
 					resistorView.activeBandNum = 4;
 					TextReader.setBandNum(5);
 					val = TextReader.findClosestVal(val,TextReader.validTols);
 					storedTols[1] = val;
 				}
 				// change calc upper and lower to textreader buttons
 
 				TextReader.setTolerance(val,false);
 				//this code enabled the view to update when tol changed
 				/*if (reader.bandNum != resistorView.bandColors.size()) {
 						if (reader.bandNum == 4) {
 							resistorView.setBandMode(4,false,true);
 \						                fourBandButton.setTextColor(0xFF000000);
 			                fiveBandButton.setTextColor(r.getColor(R.color.gray4));
 						}
 						else if (reader.bandNum == 5) {
 							resistorView.setBandMode(5,false,true);
 			                fiveBandButton.setTextColor(0xFF000000);
 			                fourBandButton.setTextColor(r.getColor(R.color.gray4));
 						}
 					}*/
 				TextReader.read(TextReader.standards[1],true); //also changes lower & upper textviews
 				TextReader.tolOut.setText(Double.valueOf(val).toString());
 				boxVals[1] = Double.valueOf(val).toString();
 
 				ColorBand c = new ColorBand(resistorView.getContext());
 				ColorBand.TolBand tolB = c.new TolBand(resistorView.getContext());
 
 				int color = tolB.valueToColor(val);
				System.out.println("UPDATING");
 				resistorView.updateWithoutCalc(color);
 				if (original != resistorView.activeBandNum) {
 					resistorView.activeBandNum = original;
 					resistorView.updateWithoutCalc(originalColor);
 				}
 				percent.setText(resistorAct.getString(R.string.percent));
 				return true;
 			default:
 				break;
 			}
 		}
 		return false;
 	}
 }
 
