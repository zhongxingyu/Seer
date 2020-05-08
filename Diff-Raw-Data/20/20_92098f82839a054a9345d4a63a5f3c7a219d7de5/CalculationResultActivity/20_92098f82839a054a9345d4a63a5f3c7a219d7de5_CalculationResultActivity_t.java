 package com.qut.spc;
 
 import java.util.Arrays;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.hardware.SensorEvent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.TextView;
 
 import com.qut.spc.service.CompassService;
 import com.qut.spc.task.XmlRequestTask;
 import com.qut.spc.view.CompassView;
 
 public class CalculationResultActivity extends Activity {
 	private TextView tvElectricityProduction, tvReturnOnInvestment,
 		tvRebates, tvAngle;
 	
 	private static final String[] DURATIONS = {
 			"week",
 			"month",
 			"year",
 	};
 	
 	private int latitude;
 	
 	private CompassView compassView;
 	private CompassService compassService;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calculation_result);
 
 		tvElectricityProduction = (TextView) findViewById(R.id.electricity_production);
 		tvReturnOnInvestment = (TextView) findViewById(R.id.return_on_investment);
 		tvRebates = (TextView) findViewById(R.id.rebates);
 		tvAngle = (TextView) findViewById(R.id.angle);
 		compassView = (CompassView) findViewById(R.id.compass);
 
 		compassService = new CompassService(this) {
 			@Override
 			public void onSensorChanged(SensorEvent e) {
 				super.onSensorChanged(e);
 				updateOrientation(this.getOrientation());
 			}
 		};
 		
 		latitude = getIntent().getIntExtra("latitude", -27);
 		
 		String url = getIntent().getStringExtra("url");
 		new CalculationResultTask().execute(url);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_calculation_result, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		compassService.registerListener();
 	}
 
 	@Override
	protected void onPause() {
		compassService.unregisterListener();
		super.onPause();
	}
	
	@Override
 	protected void onStop() {
 		compassService.unregisterListener();
 		super.onStop();
 	}
 
 	private void updateOrientation(float[] orientation) {
 		compassView.setBearing(orientation[0]);
 		compassView.setPitch(orientation[1]);
 		compassView.setRoll(-orientation[2]);
 		compassView.invalidate();
 	}
 	
 	/**
 	 * @see http://exploringgreentechnology.com/solar-energy/s/best-angle-for-solar-panels/
 	 */
 	public int getOptimalAngle() {
 		if (latitude < 0) {
 			latitude = -latitude;
 		}
 		if (latitude > 40) {
 			return latitude + 20;
 		}
 		if (latitude > 35) {
 			return latitude + 15;
 		}
 		if (latitude > 30) {
 			return latitude + 10;
 		}
 		if (latitude > 25) {
 			return latitude + 5;
 		}
 		return 15;
 	}
 
 	class CalculationResultTask extends XmlRequestTask {
 		private int depth;
 		private String category;
 		private String type;
 		
 		private double[] electricityProduction;
 		private double[] returnOnInvestment;
 		private double[] rebates;
 		
 		private ProgressDialog progressDlg;
 		
 		public CalculationResultTask() {
 			electricityProduction = new double[DURATIONS.length];
 			returnOnInvestment = new double[DURATIONS.length];
 			rebates = new double[DURATIONS.length];
 		}
 
 		@Override
 		protected boolean onXmlTag(XmlPullParser parser, int eventType) {
 			String name;
 			
 			switch (eventType) {
 			case XmlPullParser.START_DOCUMENT:
 				depth = 0;
 				break;
 				
 			case XmlPullParser.START_TAG:
 				++depth;
 				name = parser.getName();
 				if (depth == 2) {
 					category = name;
 					type = null;
 				} else if (depth == 3) {
 					type = name;
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				if (depth == 2) {
 					category = null;
 					type = null;
 				} else if (depth == 3) {
 					type = null;
 				}
 				--depth;
 				break;
 			case XmlPullParser.TEXT:
 				if (depth == 3) {
 					setValue(category, type, parser.getText());
 				}
 				break;
 			}
 			return true;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			progressDlg = ProgressDialog.show(CalculationResultActivity.this,
 					null, "Loading...");
 		}
 		
 		@Override
 		protected void onPostExecute(XmlPullParser parser) {
 			if (progressDlg != null) {
 				progressDlg.dismiss();
 				progressDlg = null;
 			}
 			// Display in years only
 			tvElectricityProduction.setText(String.valueOf(electricityProduction[2]));
 			tvReturnOnInvestment.setText(String.valueOf(returnOnInvestment[2]));
 			tvRebates.setText(String.valueOf(rebates[2]));
 			tvAngle.setText(String.valueOf(CalculationResultActivity.this.getOptimalAngle()));
 		}
 		
 		private void setValue(String category, String type, String value) {
 			if (category == null || type == null) {
 				return;
 			}
 			int durationIdx = Arrays.asList(DURATIONS).indexOf(type);
 			
 			if (durationIdx >= 0 && durationIdx < DURATIONS.length) {
 				double calValue;
 				
 				try {
 					calValue = Double.parseDouble(value);
 				} catch (NumberFormatException ex) {
 					ex.printStackTrace();
 					calValue = 0.0;
 				}
 				if (category.equals("electricityProduction")) {
 					electricityProduction[durationIdx] = calValue;
 				} else if (category.equals("returnOnInvestment")) {
 					returnOnInvestment[durationIdx] = calValue;
 				} else if (category.equals("governmentRebates")) {
 					rebates[durationIdx] = calValue;
 				}
 			}
 		}
 	}
 }
