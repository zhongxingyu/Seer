 package de.questmaster.tudmensa;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Vector;
 
 public class DataExtractor implements Runnable {
 
 	private MensaMeals cActivity = null;
 	private MealsDbAdapter mDbHelper = null;
 	private String firstDate = null;
 	private String location = null;
 	private boolean work_done = false;
 	private MensaMealsSettings.Settings mSettings = new MensaMealsSettings.Settings();
 
 	public DataExtractor(MensaMeals c, String location) {
 		this.cActivity = c;
 		this.mDbHelper = c.mDbHelper;
 		this.location = location;
 	}
 
 	public void run() {
 		work_done = false;
 
 		parseTable(getWebPage(location, "week"));
 		parseTable(getWebPage(location, "nextweek"));
 		if (mSettings.m_bDeleteOldData)
 			mDbHelper.deleteOldMeal(firstDate);
 
 		work_done = true;
 		cActivity.handler.sendEmptyMessage(0);
 	}
 
 	public boolean isAlive() {
 		return !work_done;
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
 
 					if (firstDate == null) {
 						firstDate = tmp;
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
 
 					String info;
 					// create type drawable
 					if (type.equals("F")) {
 						type = String.valueOf(R.drawable.meal_f_d);
 						info = cActivity.getResources().getString(R.string.fish);
 					} else if (type.equals("G")) {
 						type = String.valueOf(R.drawable.meal_g_d);
 						info = cActivity.getResources().getString(R.string.poultry);
 					} else if (type.equals("K")) {
 						type = String.valueOf(R.drawable.meal_k_d);
 						info = cActivity.getResources().getString(R.string.calf);
 					} else if (type.equals("R")) {
 						type = String.valueOf(R.drawable.meal_r_d);
 						info = cActivity.getResources().getString(R.string.beef);
 					} else if (type.equals("RS")) {
 						type = String.valueOf(R.drawable.meal_rs_d);
 						info = cActivity.getResources().getString(R.string.beefpig);
 					} else if (type.equals("S")) {
 						type = String.valueOf(R.drawable.meal_s_d);
 						info = cActivity.getResources().getString(R.string.pig);
 					} else if (type.equals("V")) {
 						type = String.valueOf(R.drawable.meal_v_d);
 						info = cActivity.getResources().getString(R.string.vegie);
 					} else {
 						type = String.valueOf(R.drawable.essen_d);
 						info = "";
 					}
 
					// get additional information (extract from meal name) TODO more than one pair () possible
					if (meal.contains("(") && meal.contains(")")) {
						String additions = meal.substring(meal.indexOf("(") + 1, meal.indexOf(")"));
 						String[] splitAdditions = additions.split(",");
 						try {
 							for (String s1 : splitAdditions) {
 								switch (Integer.parseInt(s1)) {
 								case 1:
 									info += "\n(1) " + cActivity.getResources().getString(R.string.colorant);
 									break;
 								case 2:
 									info += "\n(2) " + cActivity.getResources().getString(R.string.preservative);
 									break;
 								case 3:
 									info += "\n(3) " + cActivity.getResources().getString(R.string.antioxidant);
 									break;
 								case 4:
 									info += "\n(4) " + cActivity.getResources().getString(R.string.flavor_enhancer);
 									break;
 								case 5:
 									info += "\n(5) " + cActivity.getResources().getString(R.string.sulphur_treated);
 									break;
 								case 6:
 									info += "\n(6) " + cActivity.getResources().getString(R.string.blackened);
 									break;
 								case 7:
 									info += "\n(7) " + cActivity.getResources().getString(R.string.waxed);
 									break;
 								case 8:
 									info += "\n(8) " + cActivity.getResources().getString(R.string.phosphate);
 									break;
 								case 9:
 									info += "\n(9) " + cActivity.getResources().getString(R.string.sweetening);
 									break;
 								case 11:
 									info += "\n(11) "
 											+ cActivity.getResources().getString(R.string.phenylalanine_source);
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
 					if ((rowId = mDbHelper.fetchMealId(location, date, curCounter, meal_num)) >= 0) {
 						mDbHelper.updateMeal(rowId, location, date, meal_num, curCounter, meal, type, price, info);
 					} else
 						mDbHelper.createMeal(location, date, meal_num, curCounter, meal, type, price, info);
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
 
 		return out;
 	}
 
 	private String extractData(String s) {
 		// cut end "</td>"
 		s = s.substring(0, s.length() - 5);
 
 		// cut begining
 		return s.substring(s.lastIndexOf(">") + 1, s.length()).trim();
 	}
 
 }
