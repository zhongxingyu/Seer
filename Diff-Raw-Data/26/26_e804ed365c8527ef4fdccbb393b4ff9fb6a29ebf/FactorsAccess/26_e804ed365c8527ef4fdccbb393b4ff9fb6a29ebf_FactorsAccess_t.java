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
 
 package edu.nrao.dss.client;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 
 public class FactorsAccess {
 	private String banner;
 	private String headers[];
 	private String factors[][];
 	MessageBox box;
 
 	public void request(final FactorsDisplay display, Integer sessionId,
 			final String label, final Date start, Integer minutes,
 			final String timezone) {
 		JSONRequest.get("/factors", formKeys(sessionId, label, start, minutes,
 				timezone), new JSONCallbackAdapter() {
 			public void onSuccess(JSONObject json) {
 				populateHeadersFactors(json, start, timezone);
 				populateBanner(json, label);
 				display.show(label, banner, headers, factors);
 				box.close();
 			}
 		});
 	}
 
 	private HashMap<String, Object> formKeys(Integer index, String label,
 			Date start, Integer minutes, String timezone) {
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		keys.put("id", index);
 		keys.put("tz", timezone);
 		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss")
 				.format(start);
 		keys.put("start", startStr);
 		keys.put("duration", minutes);
 		String msg = "Generating scheduling factors for " + label;
 		box = MessageBox.wait("Getting factors", msg, "Be Patient ...");
 		return keys;
 	}
 
 	private void populateBanner(JSONObject json, String label) {
 		String ra   = json.get("ra").isString().stringValue();
 		String dec  = json.get("dec").isString().stringValue();
 		String freq = json.get("freq").isString().stringValue();
 		String xi = json.get("xi").isString().stringValue();
 		boolean type = json.get("type").isBoolean().booleanValue();
 		boolean time = json.get("time").isBoolean().booleanValue();
 		boolean not_complete = json.get("not_complete").isBoolean().booleanValue();
 		boolean enabled = json.get("enabled").isBoolean().booleanValue();
 		boolean authorized = json.get("authorized").isBoolean().booleanValue();
 		boolean observers = json.get("observers").isBoolean().booleanValue();
 		banner = label + ": (RA, Dec) = (" +
 		         ra + " Hr, " +
 		         dec + " Deg); Freq = " +
 		         freq + " GHz";
 		if (!xi.equals("1.00")) {
 			banner += "; <font color=gold>Xi = " + xi + "</font>";
 		}
 		boolean schedulable = type && time && not_complete && enabled && authorized && observers;
 		if (!schedulable) {
 			StringBuilder buf = new StringBuilder();
 			buf.append(";    <font color=red>Not schedulable:</font> ");
 			if (!type) {
 			    buf.append("Not open or no viable windows.  ");
 		    }
 			if (!time) {
 				buf.append("Time exhausted.  ");
 			}
 			if (!not_complete) {
 				buf.append("Marked completed.  ");
 			}
 			if (!enabled) {
 				buf.append("Not enabled.  ");
 			}
 			if (!authorized) {
 				buf.append("Not authorized.  ");
 			}
 			if (!observers) {
 				buf.append("No observers.  ");
 			}
 			if (!type && !time && !not_complete && !enabled && !authorized) {
 				buf.append("Do you need any other reasons this cannot be scheduled?");
 			}
 			banner += buf.toString();
 		}
 	}
 
 	private void populateHeadersFactors(JSONObject json, Date start, String tz) {
 		JSONArray fs = json.get("factors").isArray();
 		JSONArray fs0 = fs.get(0).isArray();
 		int length = fs0.size();
 		// Grid size including times, but not headers
 		int rows = fs.size();
 		int cols = length + 1;
 		// Extract column header names
 		headers = new String[cols];
 		headers[0] = "Date [" + tz + "]";
 		for (int i = 0; i < length; ++i) {
 			String str = fs0.get(i).isArray().get(0).toString();
 			headers[i + 1] = str.substring(1, str.indexOf('"', 1));
 		}
 		// Extract factor values
 		factors = new String[rows][cols];
 		long msecs = start.getTime();
 		Date quarter = new Date();
 		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
 		for (int t = 0; t < rows; ++t) {
 			JSONArray row = fs.get(t).isArray();
 			quarter.setTime(msecs);
 			msecs += 15 * 60 * 1000;
 			factors[t][0] = dtf.format(quarter);
 			for (int f = 0; f < length; ++f) {
 				JSONObject obj = row.get(f).isArray().get(1).isObject();
 				String repr;
 				if (obj.containsKey("Nothing")) {
 					repr = "?";
 				} else {
 					double value = obj.get("Just").isNumber().doubleValue();
					repr = NumberFormat.getFormat("#0.0000000").format(value);
 				}
 				factors[t][f + 1] = repr;
 			}
 		}
 	}
 
 }
