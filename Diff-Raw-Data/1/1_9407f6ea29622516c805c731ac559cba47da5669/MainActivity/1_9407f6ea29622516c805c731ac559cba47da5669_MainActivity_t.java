 package com.bdenney.devicestats;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private TextView mStatsTextView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		mStatsTextView = (TextView) findViewById(R.id.stats);
 		if (mStatsTextView != null) {
 			mStatsTextView.setText(getStats());
 		}
 	}
 
 	private String getStats() {
 		final StringBuilder sb = new StringBuilder();
		
 		sb.append("Manufacturer:    " + Build.MANUFACTURER);
 		sb.append("\n");
 		
 		sb.append("Model:    " + Build.MODEL);
 		sb.append("\n");
 		
 		sb.append("SDK Level:    " + Build.VERSION.SDK_INT);
 		sb.append("\n");
 		
 		sb.append("Density:    ");
 		
 		final DisplayMetrics metrics = getResources().getDisplayMetrics();
 		final float density = metrics.densityDpi;
 		if (density ==  DisplayMetrics.DENSITY_HIGH) {
 			sb.append("HDPI");
 		} else if (density == DisplayMetrics.DENSITY_MEDIUM) {
 			sb.append("MDPI");
 		} else if (density == DisplayMetrics.DENSITY_LOW) {
 			sb.append("LDPI");
 		}else if (density == DisplayMetrics.DENSITY_TV) {
 			sb.append("TVDPI");
 		} else if (density == DisplayMetrics.DENSITY_XHIGH) {
 			sb.append("XHDPI");
 		} else if (density == DisplayMetrics.DENSITY_XXHIGH) {
 			sb.append("XXHDPI");
 		} else {
 			sb.append("UNKNOWN");
 		}
 		
 		sb.append("\n");
 		
 		sb.append("Screen size:    ");
 		
 		int screenSize = getResources().getConfiguration().screenLayout &
 		        Configuration.SCREENLAYOUT_SIZE_MASK;
 
 		switch(screenSize) {
 		    case Configuration.SCREENLAYOUT_SIZE_LARGE:
 		        sb.append("LARGE");
 		        break;
 		    case Configuration.SCREENLAYOUT_SIZE_NORMAL:
 		    	sb.append("NORMAL");
 		        break;
 		    case Configuration.SCREENLAYOUT_SIZE_SMALL:
 		    	sb.append("SMALL");
 		        break;
 		    case Configuration.SCREENLAYOUT_SIZE_XLARGE:
 		    	sb.append("XLARGE");
 	        	break;
 		    default:
 		    	sb.append("");
 		}  
 		sb.append("\n");
 		
 		sb.append("Screen dimensions (dp):    " + metrics.xdpi + " x " + metrics.ydpi);
 		sb.append("\n");
 		
 		sb.append("Screen dimensions (px):    " + metrics.widthPixels + " x " + metrics.heightPixels);
 		sb.append("\n");
 		
 		return sb.toString();
 	}
 }
