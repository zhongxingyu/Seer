 package myauction.view_controller;
 
 import java.awt.Point;
 import java.sql.*;
 import myauction.CLIObject;
 import myauction.Session;
 import myauction.QueryLoader;
 import myauction.helpers.validators.*;
 
 
 
 public class UpdateTimeScreen extends Screen {
 	private CLIObject headerBox;
 	private CLIObject updateTimeBox;
 	private PreparedStatement updateTimeStatement;
 	private String curTime;
 	private PreparedStatement curTimeStatement;
 
 	private static DateValidator dateValidator = new DateValidator("date");
 	private static SpecialCharDetector prevDetector = new SpecialCharDetector("<");
 
 
 	public UpdateTimeScreen(Session session) {
 		super(session);
 
 		headerBox = new CLIObject(WIDTH, 2);
 		headerBox.setLine(0, "Previous (<)                  Update System Time                            ");
 		headerBox.setLine(1, "----------------------------------------------------------------------------");
 
 		updateTimeBox = new CLIObject(WIDTH, 11);
 		updateTimeBox.setLine(0,  "---Update Date---------------------");
 		updateTimeBox.setLine(1,  "|                                 |");
 		updateTimeBox.setLine(2,  "| Month (MM) : |_                 |");
 		updateTimeBox.setLine(3,  "| Day (DD) : __                   |");
 		updateTimeBox.setLine(4,  "| Year (YYYY) : ____              |");
 		updateTimeBox.setLine(5,  "|                                 |");
 		updateTimeBox.setLine(6,  "| Hour (hh) : __                  |");
 		updateTimeBox.setLine(7,  "| Minute (mm) : __                |");
 		updateTimeBox.setLine(8,  "| Seconds (ss) : __               |");
 		updateTimeBox.setLine(9,  "|                                 |");
 		updateTimeBox.setLine(10, "-----------------------------------");
 
 		addScreenObject(updateTimeBox, new Point(originX + 2, originY + 3));
 
 	}
 	public void reset(){
 		setMonth("|_");
 		setDay("__");
 		setYear("____");
 		setHour("__");
 		setMinute("__");
 		setSecond("__");
 		curTime = getCurrentTime();
 		updateStatus("Current Time: " + curTime);
 	}
 
 	public int run() {
 		reset();
 		draw();
 		try{
 			String month = getInput();
 			prevDetector.validate(month);
 			setMonth(month);
 			
 			updateTimeBox.setLine(3, "| Day (DD) : |_                   |");
 			draw();
 			String day = getInput();
 			prevDetector.validate(day);
 			setDay(day);
 
 			updateTimeBox.setLine(4, "| Year (YYYY) : |___              |");
 			draw();
 			String year = getInput();
 			prevDetector.validate(year);
 			setYear(year);
 
 			updateTimeBox.setLine(6, "| Hour (hh): |_                   |");
 			draw();
 			String hour = getInput();
 			prevDetector.validate(hour);
 			setHour(hour);
 			
 
 			updateTimeBox.setLine(7, "| Minute (mm): |_                 |");
 			draw();
 			String minute = getInput();
 			prevDetector.validate(minute);
 			setMinute(minute);
 
 			updateStatus("");
 			updateTimeBox.setLine(8, "| Second (ss): |_                 |");
 			draw();
 			String second = getInput();
 			prevDetector.validate(second);
 			setSecond(second);
 			dateValidator.validate(month, day, year, hour, minute, second);
			Date time = new Date(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
 			int rowUpdated = updateTime(month, day, year, hour, minute, second);
 			if (rowUpdated > 0) {
 				curTime = time.toString();
 				updateStatus("You have changed the system time to: " + time);
 			}
 		} catch (SpecialCharException e) {
 			if (e.getMessage().equals("<")) {
 				return ADMIN;
 			}
 		} catch (ValidationException e) {
 			updateStatus(e.getMessage());
 			updateStatus("");
 			return UPDATE_TIME;
 		 }// catch (Exception e) {
 		// 	debug.println(e.getMessage());
 		// 	debug.flush();
 		// 	updateStatus("Unable to update time!");
 		// }
 
 		return UPDATE_TIME;
 	}
 
 	public String formatDate(String month, String day, String year, String hour, String minute, String second) {
 		if (month.length() == 1) {
 			month = "0" + month;
 		}
 
 		if (day.length() == 1) {
 			day = "0" + day;
 		}
 
 		if (hour.length() == 1) {
 			hour = "0" + hour;
 		}
 
 		if (minute.length() == 1) {
 			minute = "0" + minute;
 		}
 
 		if (second.length() == 1) {
 			second = "0" + second;
 		}
 
 		return month + "/" + day + "/" + year + " " + hour + ":" + minute + ":" + second;
 	}
 
 	public int updateTime(String month, String day, String year, String hour, String minute, String second) {
 		try{
 			if (updateTimeStatement == null) {
 				updateTimeStatement = session.getDb().prepareStatement("update system_time set current_time = to_date(?, 'MM/DD/YYYY HH24:MI:SS')");
 			}
 			updateTimeStatement.setString(1, formatDate(month, day, year, hour, minute, second));
 			return updateTimeStatement.executeUpdate();
 		} catch (SQLException e) {
             while (e != null) {
                 debug.println(e.toString());
                 debug.flush();
                 e = e.getNextException();
             }
 		}
 		return -1;
 	}
 	public String getCurrentTime(){
 		try {
 			if (curTimeStatement == null) {
 				curTimeStatement = session.getDb().prepareStatement("select to_char(current_time, 'MM/DD/YYYY HH24:MI:SS') as cur_time from system_time");
 			}
 			ResultSet results = curTimeStatement.executeQuery();
 			results.next();
 			curTime = results.getString("cur_time");
 		} catch (SQLException e) {
 			debug.println(e.toString());
           	debug.flush();
             e = e.getNextException();
 		}
 		return curTime;
 	}
 
 	public void setMonth(String month) {
 		String line = "| Month (MM): " + month;
 		for (int i = month.length(); i < 20; i++) {
 			line += " ";
 		}
 		line += "|";
 		updateTimeBox.setLine(2, line);
 	}
 
 	public void setDay(String day) {
 		String line = "| Day (DD): " + day;
 		for (int i = day.length(); i < 22; i++) {
 			line += " ";
 		}
 		line += "|";
 		updateTimeBox.setLine(3, line);
 	}
 
 	public void setYear(String year) {
 		String line = "| Year (YYYY): " + year;
 		for (int i = year.length(); i < 19; i++) {
 			line += " ";
 		}
 		line += "|";
 		updateTimeBox.setLine(4, line);
 	}
 
 	public void setHour(String hour) {
 		String line = "| Hour (hh): " + hour;
 		for (int i = hour.length(); i < 21; i++) {
 			line += " ";
 		}
 		line += "|";
 		updateTimeBox.setLine(6, line);
 	}
 
 	public void setMinute(String minute) {
 		String line = "| Minute (mm) : " + minute;
 		for (int i = minute.length(); i < 18; i++) {
 			line += " ";
 		}
 		line += "|";
 		updateTimeBox.setLine(7, line);
 	}
 
 	public void setSecond(String second) {
 		String line = "| Second (ss) : " + second;
 		for (int i = second.length(); i < 18; i++) {
 			line += " ";
 		}
 		line += "|";
 		updateTimeBox.setLine(8, line);
 	}
 }
