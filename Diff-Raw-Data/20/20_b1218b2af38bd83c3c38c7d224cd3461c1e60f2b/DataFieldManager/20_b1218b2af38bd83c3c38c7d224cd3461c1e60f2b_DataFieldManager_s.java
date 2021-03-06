 package edu.uml.cs.isense.collector.objects;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Application;
 import android.content.Context;
import android.util.Log;
 import edu.uml.cs.isense.collector.R;
 import edu.uml.cs.isense.comm.RestAPI;
 import edu.uml.cs.isense.objects.ExperimentField;
 
 public class DataFieldManager extends Application {
 
 	int eid;
 	RestAPI rapi;
 	Context mContext;
 
 	ArrayList<ExperimentField> expFields;
 	public LinkedList<String> order;
 	JSONArray dataSet;
 	Fields f;
 	SensorCompatibility sc = new SensorCompatibility();
 	public boolean[] enabledFields = {  false, false, false, false, false, false,
 			false, false, false, false, false, false, false, false, false,
 			false, false, false, false };
 
 	public DataFieldManager(int eid, RestAPI rapi, Context mContext, Fields f) {
 		this.eid = eid;
 		this.rapi = rapi;
 		this.order = new LinkedList<String>();
 		this.mContext = mContext;
 		this.dataSet = new JSONArray();
 		this.f = f;
 	}
 
 	public void getOrder() {
 		expFields = rapi.getExperimentFields(eid);
 
 		for (ExperimentField field : expFields) {
 			switch (field.type_id) {
 
 			// Temperature
 			case 1:
 				if (field.unit_name.toLowerCase(Locale.US).contains("f"))
 					order.add(mContext.getString(R.string.temperature_f));
 				else if (field.unit_name.toLowerCase(Locale.US).contains("c")) {
 					order.add(mContext.getString(R.string.temperature_c));
 				} else if (field.unit_name.toLowerCase(Locale.US).contains("k")) {
 					order.add(mContext.getString(R.string.temperature_k));
 				} else {
 					order.add(mContext.getString(R.string.null_string));
 				}
 				break;
 				
 			// Potential Altitude
 			case 2:
 			case 3:
 				if (field.field_name.toLowerCase(Locale.US).contains("altitude")) {
 					order.add(mContext.getString(R.string.altitude));
 				} else {
 					order.add(mContext.getString(R.string.null_string));
 				}
 				break;
 
 			// Time
 			case 7:
 				order.add(mContext.getString(R.string.time));
 				break;
 
 			// Light
 			case 8:
 			case 9:
 			case 29:
 				order.add(mContext.getString(R.string.luminous_flux));
 				break;
 
 			// Angle
 			case 10:
 				if (field.unit_name.toLowerCase(Locale.US).contains("deg")) {
 					order.add(mContext.getString(R.string.heading_deg));
 				} else if (field.unit_name.toLowerCase(Locale.US).contains("rad")) {
 					order.add(mContext.getString(R.string.heading_rad));
 				} else {
 					order.add(mContext.getString(R.string.null_string));
 				}
 				break;
 
 			// Geospacial
 			case 19:
 				if (field.field_name.toLowerCase(Locale.US).contains("lat")) {
 					order.add(mContext.getString(R.string.latitude));
 				} else if (field.field_name.toLowerCase(Locale.US).contains("lon")) {
 					order.add(mContext.getString(R.string.longitude));
 				} else {
 					order.add(mContext.getString(R.string.null_string));
 				}
 				break;
 
 			// Numeric/Custom
 			case 21:
 			case 22:
 				if (field.field_name.toLowerCase(Locale.US).contains("mag")) {
 					if (field.field_name.toLowerCase(Locale.US).contains("x")) {
 						order.add(mContext.getString(R.string.magnetic_x));
 					} else if (field.field_name.toLowerCase(Locale.US).contains("y")) {
 						order.add(mContext.getString(R.string.magnetic_y));
 					} else if (field.field_name.toLowerCase(Locale.US).contains("z")) {
 						order.add(mContext.getString(R.string.magnetic_z));
 					} else if ((field.field_name.toLowerCase(Locale.US)
 							.contains("total"))
 							|| (field.field_name.toLowerCase(Locale.US)
 									.contains("average"))
 							|| (field.field_name.toLowerCase(Locale.US).contains("mean"))) {
 						order.add(mContext.getString(R.string.magnetic_total));
 					}
 				} else if (field.field_name.toLowerCase(Locale.US).contains("altitude")) {
 					order.add(mContext.getString(R.string.altitude));
 				} else
 					order.add(mContext.getString(R.string.null_string));
 				break;
 
 			// Acceleration
 			case 25:
 				if (field.field_name.toLowerCase(Locale.US).contains("x")) {
 					order.add(mContext.getString(R.string.accel_x));
 				} else if (field.field_name.toLowerCase(Locale.US).contains("y")) {
 					order.add(mContext.getString(R.string.accel_y));
 				} else if (field.field_name.toLowerCase(Locale.US).contains("z")) {
 					order.add(mContext.getString(R.string.accel_z));
 				} else if ((field.field_name.toLowerCase(Locale.US).contains("total"))
 						|| (field.field_name.toLowerCase(Locale.US).contains("average"))
 						|| (field.field_name.toLowerCase(Locale.US).contains("mean"))) {
 					order.add(mContext.getString(R.string.accel_total));
 				} else {
 					order.add(mContext.getString(R.string.null_string));
 				}
 				break;
 
 			// Pressure
 			case 27:
 				order.add(mContext.getString(R.string.pressure));
 				break;
 
 			// No match (Just about every other category)
 			default:
 				order.add(mContext.getString(R.string.null_string));
 				break;
 
 			}
 
 		}
 
 	}
 
 	public JSONArray putData() {
 
 		JSONArray dataJSON = new JSONArray();
 
 		for (String s : this.order) {
 			try {
 				if (s.equals(mContext.getString(R.string.accel_x))) {
 					dataJSON.put(f.accel_x);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.accel_y))) {
 					dataJSON.put(f.accel_y);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.accel_z))) {
 					dataJSON.put(f.accel_z);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.accel_total))) {
 					dataJSON.put(f.accel_total);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.temperature_c))) {
 					dataJSON.put(f.temperature_c);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.temperature_f))) {
 					dataJSON.put(f.temperature_f);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.temperature_k))) {
 					dataJSON.put(f.temperature_k);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.time))) {
 					dataJSON.put(f.timeMillis);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.luminous_flux))) {
 					dataJSON.put(f.lux);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.heading_deg))) {
 					dataJSON.put(f.angle_deg);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.heading_rad))) {
 					dataJSON.put(f.angle_rad);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.latitude))) {
 					dataJSON.put(f.latitude);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.longitude))) {
 					dataJSON.put(f.longitude);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.magnetic_x))) {
 					dataJSON.put(f.mag_x);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.magnetic_y))) {
 					dataJSON.put(f.mag_y);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.magnetic_z))) {
 					dataJSON.put(f.mag_z);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.magnetic_total))) {
 					dataJSON.put(f.mag_total);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.altitude))) {
 					dataJSON.put(f.altitude);
 					continue;
 				}
 				if (s.equals(mContext.getString(R.string.pressure))) {
 					dataJSON.put(f.pressure);
 					continue;
 				}
 				dataJSON.put(null);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return dataJSON;
 
 	}
 
 	public String writeSdCardLine() {
 
 		StringBuilder b = new StringBuilder();
 		boolean start = true;
 		boolean firstLineWritten = false;
 
 		for (String s : this.order) {
 			
 			if (s.equals(mContext.getString(R.string.accel_x))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.accel_x);
 				else
 					b.append(", ").append(f.accel_x);
 
 			}
 			else if (s.equals(mContext.getString(R.string.accel_y))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.accel_y);
 				else
 					b.append(", ").append(f.accel_y);
 
 			}
 			else if (s.equals(mContext.getString(R.string.accel_z))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.accel_z);
 				else
 					b.append(", ").append(f.accel_z);
 
 			}
 			else if (s.equals(mContext.getString(R.string.accel_total))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.accel_total);
 				else
 					b.append(", ").append(f.accel_total);
 
 			}
 			else if (s.equals(mContext.getString(R.string.temperature_c))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.temperature_c);
 				else
 					b.append(", ").append(f.temperature_c);
 
 			}
 			else if (s.equals(mContext.getString(R.string.temperature_f))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.temperature_f);
 				else
 					b.append(", ").append(f.temperature_f);
 
 			}
 			else if (s.equals(mContext.getString(R.string.temperature_k))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.temperature_k);
 				else
 					b.append(", ").append(f.temperature_k);
 
 			}
 			else if (s.equals(mContext.getString(R.string.time))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.timeMillis);
 				else
 					b.append(", ").append(f.timeMillis);
 
 			}
 			else if (s.equals(mContext.getString(R.string.luminous_flux))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.lux);
 				else
 					b.append(", ").append(f.lux);
 
 			}
 			else if (s.equals(mContext.getString(R.string.heading_deg))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.angle_deg);
 				else
 					b.append(", ").append(f.angle_deg);
 
 			}
 			else if (s.equals(mContext.getString(R.string.heading_rad))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.angle_rad);
 				else
 					b.append(", ").append(f.angle_rad);
 
 			}
 			else if (s.equals(mContext.getString(R.string.latitude))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.latitude);
 				else
 					b.append(", ").append(f.latitude);
 
 			}
 			else if (s.equals(mContext.getString(R.string.longitude))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.longitude);
 				else
 					b.append(", ").append(f.longitude);
 
 			}
 			else if (s.equals(mContext.getString(R.string.magnetic_x))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.mag_x);
 				else
 					b.append(", ").append(f.mag_x);
 
 			}
 			else if (s.equals(mContext.getString(R.string.magnetic_y))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.mag_y);
 				else
 					b.append(", ").append(f.mag_y);
 
 			}
 			else if (s.equals(mContext.getString(R.string.magnetic_z))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.mag_z);
 				else
 					b.append(", ").append(f.mag_z);
 
 			}
 			else if (s.equals(mContext.getString(R.string.magnetic_total))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.mag_total);
 				else
 					b.append(", ").append(f.mag_total);
 
 			}
 			else if (s.equals(mContext.getString(R.string.altitude))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.altitude);
 				else
 					b.append(", ").append(f.altitude);
 
 			}
 			else if (s.equals(mContext.getString(R.string.pressure))) {
 				firstLineWritten = true;
 				if (start)
 					b.append(f.pressure);
 				else
 					b.append(", ").append(f.pressure);
 
 			} else {
 				firstLineWritten = true;
 				if (start)
 					b.append("");
 				else
 					b.append(", ").append("");
 			}
 			
 			if (firstLineWritten)
 				start = false;
 			
 		}
 
 		b.append("\n");
		
		Log.e("wtf", b.toString());
 
 		return b.toString();
 	}
 
 	public SensorCompatibility checkCompatibility() {
 
 		int apiLevel = android.os.Build.VERSION.SDK_INT;
 		int apiVal = 0;
 		int[][] dispatch = sc.compatDispatch;
 
 		if (apiLevel <= 8)
 			apiVal = 0;
 		if (apiLevel > 8 && apiLevel < 14)
 			apiVal = 1;
 		if (apiLevel > 14)
 			apiVal = 2;
 
 		for (int i = 0; i <= 5; i++) {
 			int temp = dispatch[apiVal][i];
 			if (temp == 1)
 				sc.compatible[i] = true;
 		}
 
 		return sc;
 	}
 	
 	public String writeHeaderLine() {
 		StringBuilder b = new StringBuilder();
 		boolean start = true;
 		
 		for (String unitName : this.order) {
 			if (start)
 				b.append(unitName);			
 			else 
 				b.append(", ").append(unitName);
 			
 			start = false;
 		}
 		
 		b.append("\n");
 		
 		return b.toString();
 	}
 
 }
