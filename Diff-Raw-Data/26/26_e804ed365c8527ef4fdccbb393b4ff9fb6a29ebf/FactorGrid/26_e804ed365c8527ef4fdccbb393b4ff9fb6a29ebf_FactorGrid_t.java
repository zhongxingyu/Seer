 // Copyright (C) 2011 Associated Universities, Inc. Washington DC, USA.
 // 
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 // 
 // This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // General Public License for more details.
 // 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 // 
 // Correspondence concerning GBT software should be addressed as follows:
 //       GBT Operations
 //       National Radio Astronomy Observatory
 //       P. O. Box 2
 //       Green Bank, WV 24944-0002 USA
 
 package edu.nrao.dss.client.widget;
 
 import java.util.HashMap;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 
 public class FactorGrid extends Grid {
 
 	private final static HashMap<String, String[]> display = new HashMap<String, String[]>();
 	static
 	{
 		display.put("hourAngle",                  gValue("HA [Hr]",       "position"));
 		display.put("elevation",                  gValue("El [Deg]",      "position"));
 		
 		display.put("wind_mph",                   gValue("Vcleo [mph]",   "weather"));
 		display.put("wind_ms",                    gValue("Vcorr [m/s]",   "weather"));
 		display.put("irradiance",                 gValue("Irr [W/m^2]",   "weather"));
 		display.put("opacity",                    gValue("Opacity",       "weather"));
 		display.put("tsys",                       gValue("Tatm [K]",      "weather"));
 
 		display.put("sysNoiseTemp",               gValue("Tsys [K]",      "subfactor"));
 		display.put("sysNoiseTempPrime",          gValue("Teff [K]",      "subfactor"));
 		display.put("minSysNoiseTempPrime",       gValue("TeffMin [K]",   "subfactor"));
 		
 		display.put("score",                      gValue("Score",         "factor"));
 		display.put("scienceGrade",               gValue("SciGrade Fact", "factor"));
 		display.put("thesisProject",              gValue("ThesProj Fact", "factor"));
 		display.put("projectCompletion",          gValue("ProjComp Fact", "factor"));
 		display.put("stringency",                 gValue("Stringency",    "factor"));
 		display.put("rightAscensionPressure",     gValue("PresRA",        "factor"));
 		display.put("frequencyPressure",          gValue("PresFreq",      "factor"));
 		display.put("observingEfficiencyLimit",   gValue("ObsEff Lim",    "factor"));
 		display.put("hourAngleLimit",             gValue("HA Lim",        "factor"));
 		display.put("zenithAngleLimit",           gValue("ZA Lim",        "factor"));
 		display.put("trackingErrorLimit",         gValue("TrackErr Lim",  "factor"));
 		display.put("atmosphericStabilityLimit",  gValue("AtmStab Lim",   "factor"));
 		display.put("receiver",                   gValue("Rx Flag",       "factor"));
 		display.put("observerOnSite",             gValue("OnSite Fact",   "factor"));
 		display.put("needsLowRFI",                gValue("LowRFI Flag",   "factor"));
 		display.put("lstExcepted",                gValue("LST Flag",      "factor"));
 		display.put("enoughTimeBetween",          gValue("TimeBet Flag",  "factor"));
 		display.put("observerAvailable",          gValue("ObsAvail Flag", "factor"));
 		display.put("projectBlackout",            gValue("Proj Blck",     "factor"));
 		display.put("atmosphericEfficiency",      gValue("AtmEff",        "factor"));
 		display.put("surfaceObservingEfficiency", gValue("SurfEff",       "factor"));
 		display.put("trackingEfficiency",         gValue("TrackEff",      "factor"));
 		display.put("inWindows",                  gValue("Window Flag",   "factor"));
 		display.put("keyholeLimit",               gValue("Keyhole Lim",   "factor"));
 	}
 	
 	private static String[] gValue(String label, String type) {
 		String[] retval = new String[2];
 		retval[0] = label;
 		retval[1] = type;
 		return retval;
 	}
 	
 	private void setHeader(int col, String name) {
 		String entry, type;
 		if (display.containsKey(name)) {
 			entry = display.get(name)[0];
 			type = display.get(name)[1];
 		} else {
 			entry = name;
 			type = "other";
 		}
 		setText(0, col, entry);
 		getCellFormatter().setStyleName(0, col, "gwt-FactorGrid-" + type);
 	}
 	 
 	public FactorGrid(int rows, int cols, String[] headers, String[][] factors) {
 		super(rows+1, cols);
 		setBorderWidth(2);
 		setCellPadding(1);
 		setCellSpacing(1);
 		HashMap<String, Integer> colMap = new HashMap<String, Integer>();
         for (int col = 0; col < cols; col++) {
         	setHeader(col, headers[col]);
             colMap.put(headers[col], col);
             getCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
         }
 	    for (int row = 0; row < rows; ++row){
 	        for (int fac = 0; fac < cols; fac++) {
 	        	int col = colMap.get(headers[fac]);
	        	String text = factors[row][col];
	        	Double value;
	        	if (!text.contains("-") & !text.contains("?")){
	        		value = Double.valueOf(text);
	        		text = NumberFormat.getFormat("#0.0000").format(value);
	        	} else {
	        		value = null;
	        	}
	            setText(row+1, col, text);
 	            getCellFormatter().setHorizontalAlignment(row+1, col, HasHorizontalAlignment.ALIGN_CENTER);
 	            getCellFormatter().setWordWrap(row+1, col, false);
	            if (value != null){
		            if (value == 0.000) {
		            	getCellFormatter().setStyleName(row+1, col, "gwt-FactorGrid-" + "zero");
		            }
 	            }
 	        }
 	    }
 	}
 }
