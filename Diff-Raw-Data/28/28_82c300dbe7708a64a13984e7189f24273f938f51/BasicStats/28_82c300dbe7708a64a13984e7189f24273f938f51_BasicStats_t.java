 package com.aeidesign.statscalc;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.aeidesign.statscalc.stats.Descriptive;
 import com.aeidesign.statscalc.stats.Functions;
 
 public class BasicStats extends Activity {
 	private String dataValues = "";
 	
 	private TextView tNumSamples;
 	private TextView tMin;
 	private TextView tMax;
 	private TextView tRange;
 	private TextView tSum;
 	private TextView tMean;
 	private TextView tGeometricMean;
 	private TextView tLowerQuartile;
 	private TextView tMedian;
 	private TextView tUpperQuartile;
 	private TextView tIQR;
 	private TextView tMode;
 	private TextView tStandardDeviation;
 	private TextView tStandardError;
 	private TextView tVariance;
 	private TextView tSkewness;
 	private TextView tKurtosis;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.basic);
 
         tNumSamples = (TextView) findViewById(R.id.tBasicNumSamples);
     	tMin = (TextView) findViewById(R.id.tBasicMin);
     	tMax = (TextView) findViewById(R.id.tBasicMax);
     	tRange = (TextView) findViewById(R.id.tBasicRange);
     	tSum = (TextView) findViewById(R.id.tBasicSum);
     	tMean = (TextView) findViewById(R.id.tBasicMean);
     	tGeometricMean = (TextView) findViewById(R.id.tBasicGeometricMean);
     	tLowerQuartile = (TextView) findViewById(R.id.tBasicLowerQuartile);
         tMedian = (TextView) findViewById(R.id.tBasicMedian);
         tUpperQuartile = (TextView) findViewById(R.id.tBasicUpperQuartile);
         tIQR = (TextView) findViewById(R.id.tBasicIQR);
         tMode = (TextView) findViewById(R.id.tBasicMode);
     	tStandardDeviation = (TextView) findViewById(R.id.tBasicStandardDeviation);
     	tStandardError = (TextView) findViewById(R.id.tBasicStandardError);
     	tVariance = (TextView) findViewById(R.id.tBasicVariance);
     	tKurtosis = (TextView) findViewById(R.id.tBasicKurtosis);
     	tSkewness = (TextView) findViewById(R.id.tBasicSkewness);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch ( item.getItemId() ) {
         	case R.id.mManageData:
         		Intent activityIntent = new Intent(this, DataManagement.class);
             	activityIntent.putExtra("resultRequired", true);
             	activityIntent.putExtra("unidimData", true);
         		startActivityForResult(activityIntent,1);
         		break;
         }
 		return true;
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data){
    		super.onActivityResult(requestCode, resultCode, data);
    		
    		if ( resultCode != RESULT_OK)
    			return;
    		
 		dataValues = data.getStringExtra("dataValues");
 
    		analyzeData();
     }
     
     private void analyzeData(){    	
     	String[] dataSet = dataValues.split(";");
     	int setSize = dataSet.length;
     	
     	Descriptive stats = new Descriptive();
     	
     	for (int i = 0; i < setSize; i++) {
     		String[] dataPoint = dataSet[i].split(",");
     		stats.addValue( Double.parseDouble( dataPoint[1] ) );
 		}
     	
     	// Compute some statistics & format the result to 4 decimal places
     	// Display the results
     	tNumSamples.setText( String.valueOf( stats.getSampleSize() ) );
     	tMin.setText( String.valueOf( Functions.format(stats.getMin()) ));
     	tMax.setText( String.valueOf( Functions.format(stats.getMax()) ) );
    	tRange.setText( String.valueOf( Functions.format(stats.getRange()) ) );
     	tSum.setText( String.valueOf( Functions.format(stats.getSum()) ) );	
     	tMean.setText( String.valueOf( Functions.format(stats.getMean()) ) );
     	tGeometricMean.setText( String.valueOf( Functions.format(stats.getMeanGeometric()) ) );
     	tLowerQuartile.setText( String.valueOf( Functions.format(stats.getLowerQuartile()) ) );
     	tMedian.setText( String.valueOf( Functions.format(stats.getMedian()) ) );
     	tUpperQuartile.setText( String.valueOf( Functions.format(stats.getUpperQuartile()) ) );
     	tIQR.setText( String.valueOf( Functions.format(stats.getIQR()) ) );
     	tMode.setText( String.valueOf( Functions.format(stats.getMode()) ) );
     	tStandardDeviation.setText( String.valueOf( Functions.format(stats.getStandardDeviation()) ) );
     	tStandardError.setText( String.valueOf( Functions.format(stats.getStandardError() )) );
     	tVariance.setText( String.valueOf( Functions.format(stats.getVariance() )) );
     	tKurtosis.setText( String.valueOf( Functions.format(stats.getKurtosis() )) );
     	tSkewness.setText( String.valueOf( Functions.format(stats.getSkewness() )) );
     }
 }
