 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TextProcessor {
 
 	String stime, etime, day, month, year;
 	String msg;
 
 
 	public TextProcessor(String file) throws Exception{
 		msg = "";
 
 		if (file==null) {
 			throw new IllegalArgumentException("null input given");
 		}
 
 		FileReader f = new FileReader(file);
 		BufferedReader b = new BufferedReader(f);
 
 		while(b.ready() == true){
 			String c = b.readLine();
 			c = c.trim().toLowerCase();
 			c = c.trim().toLowerCase();
 			msg = msg + " " + c;
 		}
 		msg = msg.toLowerCase();
		System.out.println(msg);
 
 		String[] date = dates(msg).split("\\-|\\/|\\.");
 		if (date.length != 3) throw new Exception();
 		else {
 			month = date[0];
 			day = date[1];
 			year = date[2];
 		}
 
 		String[] tiempo = time(msg);
 		stime = tiempo[0];
 		if (tiempo.length > 1)
 			etime = tiempo[1];
 		
 	}
 
 	public String[] time(String msg){
 		String tiempo = "(((([0]?[1-9]|1[0-2]):[0-5][0-9])|([0-9]))( )?(AM|am|aM|Am|PM|pm|pM|Pm))|(([0]?[0-9]|1[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?)|([0]?[1-9](\\-))";
 		Pattern timeRegex = Pattern.compile(tiempo);
 		Matcher timeMatch = timeRegex.matcher(msg);
 		String[] time = new String[10];
 		int i = 0;
 		while(timeMatch.find()){
 			time[i] = timeMatch.group();
 			if (time[i].charAt(time[i].length() - 1) =='-') 
 				time[i] = time[i].substring(0, time[i].length()-1);
 			i++;
 		}
 		return time;
 	}
 
 	public String dates(String msg){
 		String formatted = "(0[1-9]|1[012]|[1-9])(-|\\/|\\.)(0[1-9]|[1-9]|[12][0-9]|3[01])(-|\\/|\\.)(((19|20)\\d{2})|(\\d{2}))";
 		Pattern timeRegex = Pattern.compile(formatted);
 		Matcher timeMatch = timeRegex.matcher(msg);
 		String date = "";
 		int i = 0;
 		while(timeMatch.find()){
 			date = timeMatch.group();
 			i++;
 		}
 		if (i != 0) return date;
 		//else is for when date is not in format 3/24/12
 		else {
 			//must be jan 27th '12
 			String month = "january|jan|february|feb|march|mar|april|apr|may|june|jun|july|jul|" +
 			"august|aug|september|sep|sept|october|oct|november|nov|december|dec";
			String day = "((0[1-9]|[1-9]|[12][0-9]|3[01])((st)|(nd)|(rd)|(th)))|" +
					"(january|jan|february|feb|march|mar|april|apr|may|june|jun|july|jul|" +
			"august|aug|september|sep|sept|october|oct|november|nov|december|dec)" +
			" (0[1-9]|[12][0-9]|3[01]|[1-9])";
 			String year = "((19|20)\\d{2})|((\\')\\d{2})";
 			Pattern dayReg = Pattern.compile(day);
 			Pattern monthReg = Pattern.compile(month);
 			Pattern yearReg = Pattern.compile(year);
 			Matcher dayMatch = dayReg.matcher(msg);
 			Matcher monthMatch = monthReg.matcher(msg);
 			Matcher yearMatch = yearReg.matcher(msg);
 			while(monthMatch.find()){
 				String match = "";
 				if (monthMatch.group().equals("january") || monthMatch.group().equals("jan")) match = "1";
 				if (monthMatch.group().equals("february") || monthMatch.group().equals("feb")) match = "2";
 				if (monthMatch.group().equals("march") || monthMatch.group().equals("mar")) match = "3";
 				if (monthMatch.group().equals("april") || monthMatch.group().equals("apr")) match = "4";
 				if (monthMatch.group().equals("may")) match = "5";
 				if (monthMatch.group().equals("june") || monthMatch.group().equals("jun")) match = "6";
 				if (monthMatch.group().equals("july") || monthMatch.group().equals("jul")) match = "7";
 				if (monthMatch.group().equals("august") || monthMatch.group().equals("aug")) match = "8";
 				if (monthMatch.group().equals("september") || monthMatch.group().equals("sept") || monthMatch.group().equals("sep")) match = "9";
 				if (monthMatch.group().equals("october") || monthMatch.group().equals("oct")) match = "10";
 				if (monthMatch.group().equals("november") || monthMatch.group().equals("nov")) match = "11";
 				if (monthMatch.group().equals("december") || monthMatch.group().equals("dec")) match = "12";
 				date = date + match;
 				date = date + "/";
 			}
 			while (dayMatch.find()){
				if (dayMatch.group().length() > 4)
					date = date + dayMatch.group().substring(dayMatch.group().length() - 2, dayMatch.group().length());
				else 
					date = date + dayMatch.group().substring(0, dayMatch.group().length() - 2);
 				date = date + "/";
 			}
 			while (yearMatch.find()){
 				String yurr = yearMatch.group();
 				if (yurr.length() > 2)
 					yurr = yurr.substring(2);
 				date = date + yurr;
 			}
 			if (date.charAt(date.length() - 1) == '/')
 				date = date + (Calendar.getInstance().get(Calendar.YEAR) - 2000);
 
 			//output will be of format 3/24/12
 			return date;
 		}
 	}
 
 
 
 	public static void main(String args[]){
 		TextProcessor t = null;
 		try {
			t = new TextProcessor("pirates.txt");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(t.stime + " - " + t.etime + " on " + t.month + "/" + t.day + "/" + t.year);
 	}
 
 }
