 package com.louishong.ibg.weixin.command;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import com.louishong.database.WeixinShiftWrapper;
 
 public class WeixinShiftCommand {
 
 	public static String execute(String command) throws SQLException,
 			InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 
 		// Check if the command is "Today"
 		if (command.equals("Today")) {
 			WeixinShiftWrapper shiftWrapper = new WeixinShiftWrapper();
 			ResultSet results = shiftWrapper.getWeixinShift();
 			ArrayList<String> names = new ArrayList<String>();
 
 			while (results.next()) {
 				String name = results.getString("ChineseName");
 
 				boolean isToday = shiftWrapper.isShiftDaysAfter(name, 0);
				if (isToday && !names.contains(name)) {
 					names.add(name);
 				}
 			}
 
 			String stringResult = "";
 			for (String name : names) {
 				stringResult += name;
 				stringResult += ", ";
 			}
 			if (!names.isEmpty()) {
 				stringResult = stringResult.substring(0,
 						stringResult.length() - 2);
 				stringResult += "΢~";
 			} else {
 				stringResult += "û΢~";
 			}
 			return stringResult;
 
 		}
 		return null;
 	}
 }
