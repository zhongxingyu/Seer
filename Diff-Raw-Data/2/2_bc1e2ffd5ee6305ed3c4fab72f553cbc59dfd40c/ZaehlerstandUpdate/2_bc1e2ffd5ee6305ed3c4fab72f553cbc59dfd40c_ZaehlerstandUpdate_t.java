 package org.fu.swphcc.wattnglueck;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.fu.swphcc.wattnglueck.utils.Database;
 import org.fu.swphcc.wattnglueck.utils.Zaehlerstand;
 
 import android.app.AlertDialog.Builder;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.NumberPicker;
 import android.widget.TextView;
 
 public class ZaehlerstandUpdate extends WattnActivity {
 
 	private Zaehlerstand zaehlerstand;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		setContentView(R.layout.activity_zaehlerstand_update);
 		// Show the Up button in the action bar.
 		//		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		NumberPicker num1 = (NumberPicker) findViewById(R.id.UpdateNumberPicker1);
 		NumberPicker num2 = (NumberPicker) findViewById(R.id.UpdateNumberPicker2);
 		NumberPicker num3 = (NumberPicker) findViewById(R.id.UpdateNumberPicker3);
 		NumberPicker num4 = (NumberPicker) findViewById(R.id.UpdateNumberPicker4);
 		NumberPicker num5 = (NumberPicker) findViewById(R.id.UpdateNumberPicker5);
 		num1.setMinValue(0);
 		num1.setMaxValue(9);
 		num2.setMinValue(0);
 		num2.setMaxValue(9);
 		num3.setMinValue(0);
 		num3.setMaxValue(9);
 		num4.setMinValue(0);
 		num4.setMaxValue(9);
 		num5.setMinValue(0);
 		num5.setMaxValue(9);
 
 		Intent i = getIntent();
 		zaehlerstand = new Zaehlerstand();
 		zaehlerstand.setId(i.getIntExtra("id", -1));
 		zaehlerstand.setDate((Date)i.getSerializableExtra("datum"));
 		zaehlerstand.setZaehlerstand(i.getFloatExtra("zaehlerstand", 0));
 		if (zaehlerstand != null) {
 			String zaehlerstandString = zaehlerstand.getZaehlerstand().toString();
 			int len = zaehlerstandString.length() - 2;
 			if (len >= 5) {
 				num1.setValue(Integer.parseInt(zaehlerstandString.substring(0, 1)));
 			} else {
 				num1.setValue(0);
 			}
 			num2.setMinValue(0);
 			num2.setMaxValue(9);
 			if (len >= 4) {
 				num2.setValue(Integer.parseInt(zaehlerstandString.substring(len - 4, len - 3)));
 			} else {
 				num2.setValue(0);
 			}
 			num3.setMinValue(0);
 			num3.setMaxValue(9);
 			if (len >= 3) {
 				num3.setValue(Integer.parseInt(zaehlerstandString.substring(len - 3, len - 2)));
 			} else {
 				num3.setValue(0);
 			}
 			num4.setMinValue(0);
 			num4.setMaxValue(9);
 			if (len >= 2) {
 				num4.setValue(Integer.parseInt(zaehlerstandString.substring(len - 2, len - 1)));
 			} else {
 				num4.setValue(0);
 			}
 			num5.setMinValue(0);
 			num5.setMaxValue(9);
 			num5.setValue(Integer.parseInt(zaehlerstandString.substring(len - 1, len)));
 
 		}
 
 		Typeface customFont = Typeface.createFromAsset(getAssets(), getString(R.string.setting_fontfilename));
 		((Button)findViewById(R.id.buttonUpdateOK)).setTypeface(customFont);
 		((Button)findViewById(R.id.buttonUpdateOK)).setTextColor(Color.parseColor("#5e625b"));
 		((Button)findViewById(R.id.buttonUpdateOK)).setText("Fertig");
 
 		initViews();
 	}
 
 	@Override
 	protected List<TextView> getTextViewsForFont() {
 		return Arrays.asList(
				(TextView) findViewById(R.id.textUpdateZaehler)
 				);
 	}
 
 	@Override
 	protected boolean showOptionsMenu() {
 		return false;
 	}
 
 	@Override
 	protected List<TextView> getButtonTextViews() {
 		return Arrays.asList(
 				(TextView) findViewById(R.id.buttonUpdateOK)
 				);
 	}
 
 	@Override
 	public boolean onClick(View arg0, MotionEvent arg1) {
 		if(arg0.getId()==R.id.buttonUpdateOK) {
 			Float zaehlerstand = 0f;
 			NumberPicker num1 = (NumberPicker) findViewById(R.id.UpdateNumberPicker1);
 			NumberPicker num2 = (NumberPicker) findViewById(R.id.UpdateNumberPicker2);
 			NumberPicker num3 = (NumberPicker) findViewById(R.id.UpdateNumberPicker3);
 			NumberPicker num4 = (NumberPicker) findViewById(R.id.UpdateNumberPicker4);
 			NumberPicker num5 = (NumberPicker) findViewById(R.id.UpdateNumberPicker5);
 			zaehlerstand += 10000f * num1.getValue();
 			zaehlerstand += 1000f * num2.getValue(); 
 			zaehlerstand += 100f * num3.getValue(); 
 			zaehlerstand += 10f * num4.getValue(); 
 			zaehlerstand += 1f * num5.getValue(); 
 			if (this.zaehlerstand != null) {
 				if (this.zaehlerstand.getZaehlerstand() > zaehlerstand) {
 					OKMessageDialog zaehlerstandNiedrig = new OKMessageDialog("Du hast einen Zhlerstand eingegeben, der niedriger als dein letzter Zhlerstand ist. Bitte gebe einen hheren Wert ein.") {
 
 						@Override
 						protected void onOKAction() {
 							dismiss();
 						}
 
 						@Override
 						protected void additionalBuilderOperations(
 								Builder builder) {
 							// TODO Auto-generated method stub
 							
 						}
 					};
 					zaehlerstandNiedrig.show(getFragmentManager(), "zaehlerstand_niedrig");
 					return true;
 				}
 			}
 			Database db = new Database(this);
 			this.zaehlerstand.setZaehlerstand(zaehlerstand);
 			db.updateZaehlerstand(this.zaehlerstand);
 			Intent returnIntent = new Intent();
 			returnIntent.putExtra("zaehlerstand", this.zaehlerstand.getZaehlerstand());
 			returnIntent.putExtra("id", this.zaehlerstand.getId());
 			returnIntent.putExtra("datum", this.zaehlerstand.getDate());
 			setResult(RESULT_OK,returnIntent); 
 
 			this.finish();
 			return true;
 		} else {
 			this.finish();
 			return false;
 		}
 	}
 
 }
