 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.kikaineko.ssu.db;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  *
  * @author Masayuki Ioki
  *
  */
 class TimeF {
 
 	public static String toSFromTime(String time) {
 		if (time == null || time.length() == 0) {
 			return "";
 		}
 
 		if (Mapper.getDbmsType().equals(Mapper.DB2)) {
 			String strTime = time.toUpperCase();
 			strTime = strTime.replaceAll(":", ".");
 			if (strTime.endsWith("AM")) {
 				System.out.println(strTime.substring(0, 5) + ".00");
 				return strTime.substring(0, 5) + ".00";
 			}
 			else if (strTime.endsWith("PM")) {
 				int hh = Integer.parseInt(strTime.substring(0, 2)) + 12;
 				System.out.println(hh + "." + strTime.substring(3, 5) + ".00");
 				return hh + "." + strTime.substring(3, 5) + ".00";
 			}
 			else {
 				System.out.println(strTime);
 				return strTime;
 			}
 		} else {
 			String[] timeArray = null;
 			String mil = "000";
 			if (time.indexOf(".") != -1) {
 				String[] tmp = time.split("\\.");
 				time = tmp[0];
 				mil = tmp[1];
 			}
 			if (time.indexOf("-") != -1) {
 				timeArray = time.split("-");
 			} else if (time.indexOf("/") != -1) {
 				timeArray = time.split("/");
 			} else if (time.indexOf(":") != -1) {
 				timeArray = time.split(":");
 			} else {
 				timeArray = new String[3];
 				timeArray[0] = time.substring(0, 2);
 				timeArray[1] = time.substring(2, 4);
 				timeArray[2] = time.substring(4, 6);
 				if (time.length() == 9) {
 					mil = time.substring(6, 9);
 				}
 			}
 			return timeArray[0] + ":" + timeArray[1] + ":" + timeArray[2] + "."
 					+ mil;
 		}
 	}
 
 	public static String toSFromTimestamp(String s) {
 		if (s == null || s.length() == 0) {
 			return "";
 		}
 
 		if (Mapper.getDbmsType().equals(Mapper.DB2)) {
 			String ymd = s.substring(0, 10);
 			String hms = s.substring(11, 19).replace(':', '.');
 			String micros = (s.length() > 20) ? s.substring(20) : "000000";
 			int microsLength = micros.length();
 			for (int i = 0; i < 6 - microsLength; i++) {
 				micros += "0";
 			}
 
 			return ymd + "-" + hms + "." + micros;
		} else if (Mapper.getDbmsType().equals(Mapper.ORACLE)) {
 			String[] dateArray = null;
 			String[] timeArray = null;
 			String mil = "000";
 			if (s.indexOf(" ") != -1) {
 				String[] temp = s.split(" ");
 				String date = temp[0];
 				String time = temp[1];
 				if (date.indexOf("-") != -1) {
 					dateArray = date.split("-");
 				} else if (date.indexOf("/") != -1) {
 					dateArray = date.split("/");
 				} else {
 					dateArray = new String[3];
 					dateArray[0] = date.substring(0, 4);
 					dateArray[1] = date.substring(4, 6);
 					dateArray[2] = date.substring(6, 8);
 				}
 
 				if (time.indexOf(".") != -1) {
 					String[] tmp = time.split("\\.");
 					time = tmp[0];
 					mil = tmp[1];
 				}
 				if (time.indexOf("-") != -1) {
 					timeArray = time.split("-");
 				} else if (time.indexOf("/") != -1) {
 					timeArray = time.split("/");
 				} else if (time.indexOf(":") != -1) {
 					timeArray = time.split(":");
 				} else {
 					timeArray = new String[3];
 					timeArray[0] = time.substring(0, 2);
 					timeArray[1] = time.substring(2, 4);
 					timeArray[2] = time.substring(4, 6);
 				}
 			} else {
 				dateArray = new String[3];
 				timeArray = new String[3];
 				dateArray[0] = s.substring(0, 4);
 				dateArray[1] = s.substring(4, 6);
 				dateArray[2] = s.substring(6, 8);
 				timeArray[0] = s.substring(8, 10);
 				timeArray[1] = s.substring(10, 12);
 				timeArray[2] = s.substring(12, 14);
 
 				if (s.length() == 17) {
 					mil = s.substring(14, 17);
 				}
 			}
 			return dateArray[0] + "-" + dateArray[1] + "-" + dateArray[2] + " "
 					+ timeArray[0] + ":" + timeArray[1] + ":" + timeArray[2]
 					+ "." + mil;
 		}
 
 		return s;
 	}
 
 	public static String toSFromDate(String s) {
 		String dbmsType = Mapper.getDbmsType();
 		if (dbmsType.equals(Mapper.DB2) || dbmsType.equals(Mapper.NETEZZA)) {
 			// YYYY-MM-DD
 			if (s.indexOf("-") != -1) {
 				return s;
 			}
 			// MM/DD/YYYY
 			else if (s.indexOf("/") != -1) {
 				String[] str = s.split("/");
 				return str[2] + "-" + str[0] + "-" + str[1];
 			}
 			// DD.MM.YYYY
 			else if (s.indexOf(".") != -1) {
 				String[] str = s.split("\\.");
 				return str[2] + "-" + str[1] + "-" + str[0];
 			}
		} else if (dbmsType.equals(Mapper.ORACLE)) {
 			if (s == null || s.length() == 0) {
 				return "";
 			}
 			String[] ss = null;
 			if (s.indexOf("-") != -1) {
 				ss = s.split("-");
 			} else if (s.indexOf("/") != -1) {
 				ss = s.split("/");
 			} else {
 				ss = new String[3];
 				ss[0] = s.substring(0, 4);
 				ss[1] = s.substring(4, 6);
 				ss[2] = s.substring(6, 8);
 			}
 			return ss[0] + "-" + ss[1] + "-" + ss[2];
 		}
 
 		return s;
 	}
 
 	public static String toS(Timestamp ts) {
 		if (ts == null) {
 			return "";
 		}
 
 		if (Mapper.getDbmsType().equals(Mapper.DB2)) {
 			SimpleDateFormat formatter = new SimpleDateFormat(
 					"yyyy-MM-dd-HH.mm.ss");
 			int micros = ts.getNanos();
 			String strmicros = String.valueOf(micros);
 			int microsLength = strmicros.length();
 			for (int i = 0; i < 6 - microsLength; i++) {
 				strmicros += "0";
 			}
 
 			return formatter.format(ts) + "." + strmicros.substring(0, 6);
 		} else if (Mapper.getDbmsType().equals(Mapper.ORACLE)) {
 			// TODO old code
 			long l = ts.getTime();
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(l);
 			String y = f4keta(cal.get(Calendar.YEAR));
 			String m = f2keta(cal.get(Calendar.MONTH) + 1);
 			String day = f2keta(cal.get(Calendar.DATE));
 			String hh = f2keta(cal.get(Calendar.HOUR_OF_DAY));
 			String mm = f2keta(cal.get(Calendar.MINUTE));
 			String ss = f2keta(cal.get(Calendar.SECOND));
 			String cc = f3keta(cal.get(Calendar.MILLISECOND));
 			return y + "-" + m + "-" + day + " " + hh + ":" + mm + ":" + ss
 					+ "." + cc;
 		}
 
 		return ts.toString();
 	}
 
 	public static String toS(java.sql.Date d) {
 		if (d == null) {
 			return "";
 		}
 		long l = d.getTime();
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(l);
 		String y = f4keta(cal.get(Calendar.YEAR));
 		String m = f2keta(cal.get(Calendar.MONTH) + 1);
 		String day = f2keta(cal.get(Calendar.DATE));
 		return y + "-" + m + "-" + day;
 	}
 
 	public static String toS(java.sql.Time t) {
 		if (t == null) {
 			return "";
 		}
 
 		if (Mapper.getDbmsType().equals(Mapper.DB2)) {
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(t.getTime());
 			String hh = f2keta(cal.get(Calendar.HOUR_OF_DAY));
 			String mm = f2keta(cal.get(Calendar.MINUTE));
 			String ss = f2keta(cal.get(Calendar.SECOND));
 			return hh + "." + mm + "." + ss;
 		}
 		else {
 			long l = t.getTime();
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(l);
 			String hh = f2keta(cal.get(Calendar.HOUR_OF_DAY));
 			String mm = f2keta(cal.get(Calendar.MINUTE));
 			String ss = f2keta(cal.get(Calendar.SECOND));
 			String cc = f3keta(cal.get(Calendar.MILLISECOND));
 			return hh + ":" + mm + ":" + ss + "." + cc;
 		}
 	}
 
 	private static String f2keta(int i) {
 		if (i < 10) {
 			return "0" + i;
 		}
 		return String.valueOf(i);
 	}
 
 	private static String f3keta(int i) {
 		if (i < 10) {
 			return "00" + i;
 		} else if (i < 100) {
 			return "0" + i;
 		}
 		return String.valueOf(i);
 	}
 
 	private static String f4keta(int i) {
 		if (i < 10) {
 			return "000" + i;
 		} else if (i < 100) {
 			return "00" + i;
 		} else if (i < 1000) {
 			return "0" + i;
 		}
 		return String.valueOf(i);
 	}
 }
