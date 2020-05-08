 package de.questmaster.tudmensa;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Vector;
 
 public class DataExtractor implements Runnable {
 
 	private MensaMeals mActivity = null;
 	private MealsDbAdapter mDbHelper = null;
 	private String mFirstDate = null;
 	private String mLocation = null;
 	private boolean mWork_done = false;
 	private MensaMealsSettings.Settings mSettings = new MensaMealsSettings.Settings();
 
 	public DataExtractor(MensaMeals c, String location) {
 		this.mActivity = c;
 		this.mDbHelper = c.mDbHelper;
 		this.mLocation = location;

		// Read settings
		mSettings.ReadSettings(c);
 	}
 
 	public void run() {
 		mWork_done = false;
 
 		parseTable(getWebPage(mLocation, "week"));
 		parseTable(getWebPage(mLocation, "nextweek"));
 		if (mSettings.m_bDeleteOldData)
 			mDbHelper.deleteOldMeal(mFirstDate);
 
 		mWork_done = true;
 		mActivity.mHandler.sendEmptyMessage(0);
 	}
 
 	public boolean isAlive() {
 		return !mWork_done;
 	}
 
 	/* parse Website and store in database */
 	private Vector<String> getWebPage(String task, String view) {
 		Vector<String> webTable = new Vector<String>();
 
 		try {
 			URL uTest = new URL("http://www.studentenwerkdarmstadt.de/index.php?option=com_spk&task=" + task + "&view="
 					+ view);
 			BufferedReader br = new BufferedReader(new InputStreamReader(uTest.openStream()), 2048);
 
 			boolean store = false;
 			String s;
 			while ((s = br.readLine()) != null) {
 				// find first line of meal tables
 				if (s.indexOf("class=\"spk_table\">") >= 0) {
 					// remove before table
 					s = s.substring(s.indexOf("<tr><td"));
 					store = true;
 				}
 				if (store) {
 					if (s.indexOf("</table>") >= 0) {
 						// remove after table
 						s = "</table>";
 						store = false;
 					}
 
 					// append line
 					webTable.add(s);
 
 					if (!store) {
 						break; // fertig
 					}
 				}
 			}
 
 			br.close();
 		} catch (MalformedURLException e) {
 			// Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return webTable;
 	}
 
 	private void parseTable(Vector<String> tbl) {
 		Vector<String> days = new Vector<String>();
 		int day_index = 0;
 		int meal_num = 0;
 		String curCounter = "";
 
 		for (String s : tbl) {
 			// new Row -> maybe new counter
 			if (s.startsWith("<tr>")) {
 				if (s.indexOf("&nbsp;") < 0) {
 					// get new Counter name
 					curCounter = extractData(s);
 					meal_num = 0;
 				} else if (!curCounter.equals("")) {
 					// more meals at one counter
 					meal_num++;
 				}
 			}
 
 			if (s.startsWith("<td")) {
 				String tmp = extractData(s);
 
 				// date line
 				if (curCounter.compareTo("") == 0 && tmp.length() == 10) {
 					tmp = tmp.substring(6, 10) + tmp.substring(3, 5) + tmp.substring(0, 2);
 					days.add(tmp);
 
 					if (mFirstDate == null) {
 						mFirstDate = tmp;
 					}
 
 					// €-sign unfortunately not encoded, so checking price-tag
 				} else if (tmp.lastIndexOf(",") > 0 && day_index < days.size()
 						&& Character.isDigit(tmp.charAt(tmp.lastIndexOf(",") - 1))
 						&& Character.isDigit(tmp.charAt(tmp.lastIndexOf(",") + 1))
 						&& Character.isDigit(tmp.charAt(tmp.lastIndexOf(",") + 2))) {
 
 					// price
 					tmp = tmp.substring(0, tmp.lastIndexOf(",") + 3);
 					String price = tmp.substring(tmp.lastIndexOf(" ") + 1);
 
 					// cut price from string
 					tmp = tmp.substring(0, tmp.length() - price.length()).trim();
 
 					// add EUR sign to price string
 					price += " €";
 
 					// type
 					String type = tmp.substring(tmp.lastIndexOf(" ") + 1);
 
 					// cut type from string
 					String meal = tmp.substring(0, tmp.length() - type.length()).trim();
 					meal = htmlDecode(meal);
 
 					// create detail information
 					String info;
 					if (type.equals("F")) {
 						info = mActivity.getResources().getString(R.string.fish);
 					} else if (type.equals("G")) {
 						info = mActivity.getResources().getString(R.string.poultry);
 					} else if (type.equals("K")) {
 						info = mActivity.getResources().getString(R.string.calf);
 					} else if (type.equals("R")) {
 						info = mActivity.getResources().getString(R.string.beef);
 					} else if (type.equals("RS")) {
 						info = mActivity.getResources().getString(R.string.beefpig);
 					} else if (type.equals("S")) {
 						info = mActivity.getResources().getString(R.string.pig);
 					} else if (type.equals("V")) {
 						info = mActivity.getResources().getString(R.string.vegie);
 					} else {
 						info = "";
 					}
 
 					// get additional information (extract from meal name)
 					String mealInspect = meal;
 					while (mealInspect.contains("(") && mealInspect.contains(")")) {
 						String additions = mealInspect.substring(mealInspect.indexOf("(") + 1, mealInspect.indexOf(")"));
 						mealInspect = mealInspect.substring(mealInspect.indexOf(")") + 1); // skip current (...)
 						String[] splitAdditions = additions.split(",");
 						try {
 							for (String s1 : splitAdditions) {
 								switch (Integer.parseInt(s1)) {
 								case 1:
 									info += "\n(1) " + mActivity.getResources().getString(R.string.colorant);
 									break;
 								case 2:
 									info += "\n(2) " + mActivity.getResources().getString(R.string.preservative);
 									break;
 								case 3:
 									info += "\n(3) " + mActivity.getResources().getString(R.string.antioxidant);
 									break;
 								case 4:
 									info += "\n(4) " + mActivity.getResources().getString(R.string.flavor_enhancer);
 									break;
 								case 5:
 									info += "\n(5) " + mActivity.getResources().getString(R.string.sulphur_treated);
 									break;
 								case 6:
 									info += "\n(6) " + mActivity.getResources().getString(R.string.blackened);
 									break;
 								case 7:
 									info += "\n(7) " + mActivity.getResources().getString(R.string.waxed);
 									break;
 								case 8:
 									info += "\n(8) " + mActivity.getResources().getString(R.string.phosphate);
 									break;
 								case 9:
 									info += "\n(9) " + mActivity.getResources().getString(R.string.sweetening);
 									break;
 								case 11:
 									info += "\n(11) "
 											+ mActivity.getResources().getString(R.string.phenylalanine_source);
 									break;
 								}
 							}
 
 						} catch (NumberFormatException e) {
 							// No number, so its nothing we care about
 						}
 					}
 					
 					// Add table entry
 					String date = days.get(day_index);
 					long rowId = 0;
 					if ((rowId = mDbHelper.fetchMealId(mLocation, date, curCounter, meal_num)) >= 0) {
 						mDbHelper.updateMeal(rowId, mLocation, date, meal_num, curCounter, meal, type, price, info);
 					} else
 						mDbHelper.createMeal(mLocation, date, meal_num, curCounter, meal, type, price, info);
 				}
 
 				day_index++;
 			}
 
 			if (s.startsWith("</tr>")) {
 				day_index = 0;
 			}
 
 			// </table>: end -> clean old entries
 			if (s.equals("</table>")) {
 				break;
 			}
 		}
 	}
 
 	private String htmlDecode(String in) {
 		String out = in;
 
 		out = out.replaceAll("&auml;", "ä");
 		out = out.replaceAll("&Auml;", "Ä");
 		out = out.replaceAll("&ouml;", "ö");
 		out = out.replaceAll("&Ouml;", "Ö");
 		out = out.replaceAll("&uuml;", "ü");
 		out = out.replaceAll("&Uuml;", "Ü");
 		out = out.replaceAll("&szlig;", "ß");
 		out = out.replaceAll("&amp;", "&");
 		out = out.replaceAll("&quot;", "\"");
 		out = out.replaceAll("&acute;", "\'");
 		out = out.replaceAll("&egrave;", "é");
 
 		return out;
 	}
 
 	private String extractData(String s) {
 		// cut end "</td>"
 		s = s.substring(0, s.length() - 5);
 
 		// cut begining
 		return s.substring(s.lastIndexOf(">") + 1, s.length()).trim();
 	}
 
 }
