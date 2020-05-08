 package regalowl.databukkit;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 
 public class CommonFunctions {
 
 	
 	public String fM(String message) {
 		message = message.replace("&0", ChatColor.BLACK+"");
 		message = message.replace("&1", ChatColor.DARK_BLUE+"");
 		message = message.replace("&2", ChatColor.DARK_GREEN+"");
 		message = message.replace("&3", ChatColor.DARK_AQUA+"");
 		message = message.replace("&4", ChatColor.DARK_RED+"");
 		message = message.replace("&5", ChatColor.DARK_PURPLE+"");
 		message = message.replace("&6", ChatColor.GOLD+"");
 		message = message.replace("&7", ChatColor.GRAY+"");
 		message = message.replace("&8", ChatColor.DARK_GRAY+"");
 		message = message.replace("&9", ChatColor.BLUE+"");
 		message = message.replace("&a", ChatColor.GREEN+"");
 		message = message.replace("&b", ChatColor.AQUA+"");
 		message = message.replace("&c", ChatColor.RED+"");
 		message = message.replace("&d", ChatColor.LIGHT_PURPLE+"");
 		message = message.replace("&e", ChatColor.YELLOW+"");
 		message = message.replace("&f", ChatColor.WHITE+"");
 		message = message.replace("&k", ChatColor.MAGIC+"");
 		message = message.replace("&l", ChatColor.BOLD+"");
 		message = message.replace("&m", ChatColor.STRIKETHROUGH+"");
 		message = message.replace("&n", ChatColor.UNDERLINE+"");
 		message = message.replace("&o", ChatColor.ITALIC+"");
 		message = message.replace("&r", ChatColor.RESET+"");
 		return message;
 	}
 	
 	public double twoDecimals(double input) {
 		return round(input, 2);
 	}
 	public double round(double input, int decimals) {
		BigDecimal result = round(new BigDecimal(String.valueOf(input)), decimals);
		return result.doubleValue();
 	}
 	public BigDecimal round(BigDecimal input, int decimals) {
		BigDecimal factor = new BigDecimal(String.valueOf(Math.pow(10, decimals)));
		return new BigDecimal(input.multiply(factor).toBigInteger()).divide(factor);
 	}
 	public String roundString(BigDecimal input, int decimals) {
 		return round(input, decimals).toPlainString();
 	}
 	public String roundString(double input, int decimals) {
		return round(new BigDecimal(String.valueOf(input)), decimals).toPlainString();
 	}
 	/*
 	public double round(double input, int decimals) {
 		Double factor = Math.pow(10, decimals);
 		long changedecimals = (long) Math.ceil((input * factor) - .5);
 		return (double) changedecimals / factor;
 	}
 	*/
 	
 	public String getTimeStamp() {
 		 Date date = new Date();
 		return new Timestamp(date.getTime()).toString();
 	}
 	
 	
 	
 	public String getErrorString(Exception e) {
 		if (e == null) {return null;}
 		StringWriter error = new StringWriter();
 		e.printStackTrace(new PrintWriter(error));
 		return error.toString();
 	}
 	
 	
 	
 
 	public ArrayList<Double> doubleToArray(String commalist) {
 		try {
 			ArrayList<Double> array = new ArrayList<Double>();
 			if (commalist.indexOf(",") == 0) {
 				commalist = commalist.substring(1, commalist.length());
 			}
 			if (!commalist.substring(commalist.length() - 1, commalist.length()).equalsIgnoreCase(",")) {
 				commalist = commalist + ",";
 			}
 			while (commalist.contains(",")) {
 				array.add(Double.parseDouble(commalist.substring(0, commalist.indexOf(","))));
 				if (commalist.indexOf(",") == commalist.lastIndexOf(",")) {
 					break;
 				}
 				commalist = commalist.substring(commalist.indexOf(",") + 1, commalist.length());
 			}
 			return array;
 		} catch (Exception e) {
 			ArrayList<Double> array = new ArrayList<Double>();
 			return array;
 		}
 	}
 	public ArrayList<Integer> intToArray(String commalist) {
 		try {
 			ArrayList<Integer> array = new ArrayList<Integer>();
 			if (commalist.indexOf(",") == 0) {
 				commalist = commalist.substring(1, commalist.length());
 			}
 			if (!commalist.substring(commalist.length() - 1, commalist.length()).equalsIgnoreCase(",")) {
 				commalist = commalist + ",";
 			}
 			while (commalist.contains(",")) {
 				array.add(Integer.parseInt(commalist.substring(0, commalist.indexOf(","))));
 				if (commalist.indexOf(",") == commalist.lastIndexOf(",")) {
 					break;
 				}
 				commalist = commalist.substring(commalist.indexOf(",") + 1, commalist.length());
 			}
 			return array;
 		} catch (Exception e) {
 			ArrayList<Integer> array = new ArrayList<Integer>();
 			return array;
 		}
 	}
 	public String doubleArrayToString(ArrayList<Double> array) {
 		String string = "";
 		int c = 0;
 		while (c < array.size()) {
 			string = string + array.get(c) + ",";
 			c++;
 		}
 		return string;
 	}
 	public String intArrayToString(ArrayList<Integer> array) {
 		String string = "";
 		int c = 0;
 		while (c < array.size()) {
 			string = string + array.get(c) + ",";
 			c++;
 		}
 		return string;
 	}
 	
 	
 	
 	
 	public ArrayList<String> explode(String string, String delimiter) {
 		ArrayList<String> array = new ArrayList<String>();
 		if (string == null || delimiter == null || !string.contains(delimiter)) {return array;}
 		if (string.indexOf(delimiter) == 0) {string = string.substring(1, string.length());}
 		if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(delimiter)) {string += delimiter;}
 		while (string.contains(delimiter)) {
 			array.add(string.substring(0, string.indexOf(delimiter)));
 			if (string.indexOf(delimiter) == string.lastIndexOf(delimiter)) {break;}
 			string = string.substring(string.indexOf(delimiter) + 1, string.length());
 		}
 		return array;
 	}
 	public String implode(List<String> array, String delimiter) {
 		if (array == null || delimiter == null) {return "";}
 		String string = "";
 		for (String cs:array) {
 			string += cs + delimiter;
 		}
 		return string;
 	}
 	
 	
 	
 
 	
 	
 	
 	
 	public HashMap<String,String> explodeMap(String string) {
 		HashMap<String,String> map = new HashMap<String,String>();
 		if (string == null || !string.contains(",")) {return map;}
 		if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(";")) {string += ";";}
 		while (string.contains(";")) {
 			String mapEntry = string.substring(0, string.indexOf(";"));
 			String mapKey = mapEntry.substring(0, mapEntry.indexOf(","));
 			String mapValue = mapEntry.substring(mapEntry.indexOf(",") + 1, mapEntry.length());
 			map.put(mapKey, mapValue);
 			if (string.indexOf(";") == string.lastIndexOf(";")) {break;}
 			string = string.substring(string.indexOf(";") + 1, string.length());
 		}
 		return map;
 	}
 	public String implodeMap(HashMap<String,String> map) {
 		if (map == null) {return "";}
 		String string = "";
 		for (Map.Entry<String,String> entry : map.entrySet()) {
 		    String key = entry.getKey();
 		    String value = entry.getValue();
 		    string += (key + "," + value + ";");
 		}
 		return string;
 	}
 	
 	
 	public HashMap<String,Integer> explodeIntMap(String string) {
 		HashMap<String,Integer> map = new HashMap<String,Integer>();
 		try {
 			if (string == null || !string.contains(",")) {return map;}
 			if (!string.substring(string.length() - 1, string.length()).equalsIgnoreCase(";")) {string += ";";}
 			while (string.contains(";")) {
 				String mapEntry = string.substring(0, string.indexOf(";"));
 				String mapKey = mapEntry.substring(0, mapEntry.indexOf(","));
 				Integer mapValue = Integer.parseInt(mapEntry.substring(mapEntry.indexOf(",") + 1, mapEntry.length()));
 				map.put(mapKey, mapValue);
 				if (string.indexOf(";") == string.lastIndexOf(";")) {break;}
 				string = string.substring(string.indexOf(";") + 1, string.length());
 			}
 			return map;
 		} catch (Exception e) {
 			return new HashMap<String,Integer>();
 		}
 		
 	}
 	public String implodeIntMap(HashMap<String,Integer> map) {
 		if (map == null) {return "";}
 		String string = "";
 		for (Map.Entry<String,Integer> entry : map.entrySet()) {
 		    String key = entry.getKey();
 		    Integer value = entry.getValue();
 		    string += (key + "," + value + ";");
 		}
 		return string;
 	}
 
 	
 	
 }
