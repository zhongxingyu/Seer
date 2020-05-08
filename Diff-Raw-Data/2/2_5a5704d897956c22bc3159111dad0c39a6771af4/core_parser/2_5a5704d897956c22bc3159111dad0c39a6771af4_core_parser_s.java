 package core_dos.server;
 
 import java.awt.im.InputContext;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 
 import java.util.HashMap;
 
 import core_dos.shared.JsEvent;
 
 public class core_parser {
 	private static final String BASE_SITE = "https://core.meditech.com"; 
 	static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
 	/**
 	 * 
 	 * @param input 123
 	 * @param type must be "day" or "cal" otherwise return null
 	 * @return array list of div elements on the page
 	 * <table border="1">
 	 * <tr><td>div 0 </td><td colspan=3>empty	</td></tr>
 	 * <tr><td>div 1 </td><td colspan=3>schedule,worklist,inquiries</td></tr>
 	 * <tr><td>div 2 </td><td colspan=3>left arrow, date, right arrow</td></tr>
 	 * <tr><td></td><td>Day View</td><td>calendar view</td><td>event view</td></tr>
 	 * <tr><td>div 3</td><td>event table</td><td>month picker</td><td>event name</td></tr>  
 	 * <tr><td>div 4</td><td>events/no events</td><td>calendar</td><td>when/what/where</td></tr>
 	 * <tr><td>div 5</td><td>empty?</td><td>top</td><td>who</td></tr>
 	 * <tr><td>div 6</td><td colspan=2>schedule,worklist,inquiries</td><td>blank</td></tr>
 	 * <tr><td>div 7</td><td colspan=2>logoff</td><td>notes</td></tr>
 	 * <tr><td>div 8</td><td></td><td></td><td>top</td></tr>
 	 * <tr><td>div 9</td><td></td><td></td><td>schedule,worklist,inquiries</td></tr>
 	 * <tr><td>div 10</td><td></td><td></td><td>Logoff</td></tr>
 	 * </table>
 	 * 
 	 */
 	public static ArrayList<String> parseCoreViewHTML(String input,String type){
 		ArrayList<String> ret = new ArrayList<String>();
 		String cal_start = "<div id=\"bodyregion\"";
 		String cal_end = "</body>";
 		String day_start = "<form name=\"inputform\"";
 		String day_end = "</form>";
 		
 		int start_form;
 		int end_form;
 		
 		if(type=="day"){
 			start_form = input.indexOf(day_start);
 			if(start_form==-1){return null;}
 			start_form += day_start.length();
 			end_form = input.indexOf(day_end,start_form);
 			
 		}else if (type == "cal") {
 			start_form = input.indexOf(cal_start);
 			if(start_form==-1){return null;}
 			start_form += +cal_start.length();
 			end_form = input.indexOf(cal_end,start_form);
 		}else{
 			return null;
 		}
 		
 		
 		
 		int x = 0;
 		if(start_form!=-1){
 			int next = input.indexOf("<div",start_form);
 			while(next!=-1&&next<end_form){
 				next = input.indexOf(">",next)+1;
 				int end = input.indexOf("</div>", next);
 				if(end!=-1&&next!=-1){
 					ret.add(input.substring(next, end));
 					//System.out.println((x++)+": "+ret.get(x-1));
 				}
 				next = input.indexOf("<div",end);
 			}
 			
 		}
 		
 		return ret;
 	}
 	/**
 	 * @param input - div 2 element from any view
 	 * @return remote link to the calendar page
 	 */
 	public static String getCalendarLink(String input){
 		int date_view = input.indexOf("<td class=\"style6\"");
 		if (date_view!=-1){
 			int href_start = input.indexOf("<a href=\".",date_view)+10; 
 			int href_end   = input.indexOf("\"", href_start);
 			String cal_link = BASE_SITE + input.substring(href_start,href_end);
 			return cal_link;
 		}
 		return null;
 	}
 	/**
 	 * @param input - div 4 from calendar view
 	 * @return ArrayList of day view links
 	 */
 	public static ArrayList<String> parseEventFromCal(String input) {
 		ArrayList<String> ret = new ArrayList<String>();
 		int start_form = input.indexOf("<form name=\"inputform\"");
 		int end_form = input.indexOf("</form>",start_form+22);
 		/*
 		 * <a href="./0005vkt9gz.mthd"><img src="./Images\HHIcon_DownArrow.png" alt="*" width="20" height="13"></a>
 		 */
 		String pat_link = "<a href=\".";
 		String pat_event = "<img src=\"./Images\\HHIcon_DownArrow.png\"";
 		int x = 0;
 		int start_link;
 		int end_link;
 		int next = input.indexOf(pat_link);
 		int next_event = input.indexOf(pat_event);
 		while(next!=-1&&next_event!=-1){
 			start_link = next += pat_link.length();
 			end_link = input.indexOf("\">",next);
 			String link = input.substring(start_link,end_link);
 			end_link = input.indexOf("</a>",end_link);
 			next_event = input.indexOf(pat_event,start_link);
 			if(next_event<end_link){
 				//found link for event
 				ret.add(BASE_SITE+link);
 			}
 			next = input.indexOf(pat_link,end_link);
 			
 		}
 		
 		return ret;
 	}
 	/**
 	 * 
 	 * @param input - div 3 from day view
 	 * @return ArrayList of event view links
 	 */
 	public static ArrayList<String> parseEventFromDay(String input) {
 		//System.out.println("parseEventFromDay:-----------------------------\n"+input);
 		ArrayList<String> ret = new ArrayList<String>();
 		/*
 		 * <tr>
 			<td class="style12">1:30p</td>
 			<td class="style12"><a href="./0007eovr73.mthd">Quick Meeting<br></a></td>
 			<td class="style12">Faneuil (Framingham)</td>
 			</tr>
 		 */
 		String pat_start_day = "<td class=\"style9\">";
 		String pat_link_start = "<a href=\".";
 		int start_day_one = input.indexOf(pat_start_day);
 		
 		if(start_day_one==-1){
 			return null;
 		}
 		start_day_one+=pat_start_day.length();
 		int start_day_two = input.indexOf(pat_start_day,start_day_one);
 		int end = start_day_two==-1?input.length():start_day_two;
 		
 		int next = input.indexOf(pat_link_start,start_day_one);
 		System.out.println("--------------345-------------"+next+":"+end);
 		while (next!=-1 && next<end){
 			next+=pat_link_start.length();
 			int end_link = input.indexOf("\"",next);
 			String link = input.substring(next,end_link);
 			ret.add(BASE_SITE+link);
 			next = input.indexOf(pat_link_start,end_link);
 		}
 		
 		return ret;
 	}
 
 	/**
 	 * 
 	 * @param input
 	 * @return ArrayList of event
 	 * <table border="1">
 	 * <tr><td colspan=2>required</td></tr>
 	 * <tr><td> summary (event name)</td></tr>
 	 * <tr><td> event start day (if all day)</td><td rowspan=4>date/times will be created in pairs of 2</td></tr>
 	 * <tr><td> event end day (if all day)</td></tr>
 	 * <tr><td> start time</td></tr>
 	 * <tr><td> end time</td></tr>
 	 * <tr><td colspan=2>optional</td></tr>
 	 * <tr><td>location</td></tr>
 	 * <tr><td>description (notes)</td><td>not html enabled :(</td></tr>
 	 * <tr><td>participants <br>(can't do this without emails)</td></tr>
 	 * 
 	 * </table>
 	 */
 	public static ArrayList<JsEvent> parseEventFromEventView(ArrayList<String> event_view){
 		ArrayList<JsEvent> events = new ArrayList<JsEvent>();
 		
 		HashMap<String,Object> ret = new HashMap<String,Object>();
 		String yyyy = getYYYY(event_view.get(2));
 		String summary = getSummary(event_view.get(3));
 		String location = getLocation(event_view.get(4));
 		System.out.println(summary);
 		
 		ArrayList<Date> dates = getDates(event_view.get(4));
 		
		SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US);
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
 		SimpleDateFormat human = new SimpleDateFormat("M/d H:mm",Locale.US);
 		if(dates.size()>1){
 			
 			for(int x=0;x<dates.size(); x+=2){
 				String start_date = rfc3339.format(dates.get(x));
 				String end_date =  rfc3339.format(dates.get(x+1));
 				String start_date_human = human.format(dates.get(x));
 				String end_date_human = human.format(dates.get(x+1));
 				
 				JsEvent ev = new JsEvent(summary,location,
 						"",start_date,start_date_human,
 						"",end_date,end_date_human);
 				events.add(ev);
 			}
 		}else if(dates.size()==1){
 			Date s = dates.get(0);
 			
 			String start_date = sdf.format(dates.get(0));
 			String end_date = sdf.format(dates.get(0).getTime()+MILLIS_IN_DAY);
 			String start_date_human = human.format(dates.get(0));
 			String end_date_human = human.format(dates.get(0).getTime()+MILLIS_IN_DAY);
 			
 			JsEvent allDayEvent = new JsEvent(summary,location,
 					start_date,"",start_date_human,
 					end_date,"",end_date_human);
 			events.add(allDayEvent);
 			
 		}else {
 			System.out.println("ERROR-------------\n"+event_view.get(3));
 		}
 		
 		//descriptions can contain html from core, but google calendar doesn't support that
 		return events;
 	}
 	/**
 	 * getDates
 	 * @param input
 	 * @return ArrayList of all dates for event
 	 */
 	private static ArrayList<Date> getDates(String input){
 		ArrayList<Date> ret = new ArrayList<Date>();
 		String pat_tag_start_old = "<td class=\"style9\">";
 		String pat_tag_start_new = "<td class=\"style10\">";
 		String pat_tag_end   = "</td>";
 		DateFormat df = DateFormat.getDateInstance();
 		int next_old = input.indexOf(pat_tag_start_old);
 		int next_new = input.indexOf(pat_tag_start_new);
 		
 		System.out.println(input);
 		while (next_old!=-1||next_new!=-1){
 			String pat_tag_start = (next_old!=-1&&next_new==-1)||
 					(next_old!=-1&&next_old<next_new)?
 					pat_tag_start_old:
 					pat_tag_start_new;
 			
 			int next = 	(next_old!=-1&&next_new==-1)||
 					(next_old!=-1&&next_old<next_new)?
 					next_old:
 					next_new;
 			
 			next += pat_tag_start.length();
 			int end = input.indexOf(pat_tag_end,next);
 			//get when
 			String date = input.substring(next, end);
 			System.out.println(date);
 			
 			try {
 				SimpleDateFormat sdf;
 				int br = date.indexOf("<br>");
 				String d1 = date.substring(0, br);
 				String d2 = date.substring(br+"<br>".length());
 				if(d1.charAt(d1.length()-1)=='a'||
 						d1.charAt(d1.length()-1)=='p'){
 					sdf = new SimpleDateFormat("MMM dd yy h:mmaa");
 
 					d1+="m";
 					d2+="m";
 				}else{
 					
 					//could be all day event?
 					//could be event on just one day
 					//	d2 = 2:00p - 4:30p
 					int split = d2.indexOf(" - ");
 					if(split!=-1){
 						sdf = new SimpleDateFormat("MMM dd yy h:mmaa");
 						String t1 = d2.substring(0,split);
 						String t2 = d2.substring(split+3);
 						t1 = d1 +" " + t1+"m";
 						t2 = d1 + " " + t2+"m";
 						d1 = t1;
 						d2 = t2;
 						
 						
 					} else {
 						sdf = new SimpleDateFormat("MMM dd yy");
 					}
 				}
 				Date dt1 = sdf.parse(d1.toUpperCase());
 				Date dt2 = sdf.parse(d2.toUpperCase());
 				ret.add(dt1);
 				ret.add(dt2);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			
 			next = input.indexOf(pat_tag_start,end);
 			next += pat_tag_start.length();
 			end = input.indexOf(pat_tag_end,next);
 			//get what
 			//input.subSequence(next, end);
 			
 			next = input.indexOf(pat_tag_start,end);
 			next += pat_tag_start.length();
 			end = input.indexOf(pat_tag_end,next);
 			//input.subSequence(next, end);
 			
 			next_old = input.indexOf(pat_tag_start_old,end);
 			next_new = input.indexOf(pat_tag_start_new,end);
 			
 			
 		}
 		return ret;
 	}
 	
 	private static String getLocation(String input){
 		String pat_tag_start = "<td class=\"style9\">";
 		String pat_tag_end   = "</td>";
 		String ret = null;
 		int next = input.indexOf(pat_tag_start);
 		while (next!=-1){
 			next += pat_tag_start.length();
 			int end = input.indexOf(pat_tag_end,next);
 			//get when
 			//String date = input.substring(next, end);
 			
 			next = input.indexOf(pat_tag_start,end);
 			next += pat_tag_start.length();
 			end = input.indexOf(pat_tag_end,next);
 			//get what
 			//input.subSequence(next, end);
 			
 			next = input.indexOf(pat_tag_start,end);
 			next += pat_tag_start.length();
 			end = input.indexOf(pat_tag_end,next);
 			//get where
 			ret = input.substring(next, end);
 			
 			next = input.indexOf(pat_tag_start,end);
 		}
 		return ret;
 	}
 	/**
 	 * getSummary
 	 * @param input
 	 * @return summary from div 3 (event view only)
 	 */
 	private static String getSummary(String input){
 		/*
 		 * <div class="style2" align="center">
 			<span class="style3">BAR MONTH END Coverage (Business)</span>
 			<br></div>
 		 */
 		String pat_tag_start = "<span class=\"style3\">";
 		String pat_tag_end = "</span>";
 		int start = input.indexOf(pat_tag_start);
 		if(start==-1){
 			return null;
 		}
 		start += pat_tag_start.length();
 		int end = input.indexOf(pat_tag_end,start);
 		return input.substring(start,end);
 	}
 	/**
 	 * 
 	 * @param input - div 2 from any view
 	 * @return the 4 digit year as displayed on page
 	 */
 	private static String getYYYY(String input){
 		//input has: <td class="style6" align="center">Sun Sep 02 2012</td>
 		String pat_start_date = "<td class=\"style6\" align=\"center\">";
 		String pat_end_date = "</td>";
 		int date_start = input.indexOf(pat_start_date);
 		if (date_start==-1){
 			return null;
 		}
 		date_start += pat_start_date.length();
 		int date_end = input.indexOf(pat_end_date,date_start);
 		String date = input.substring(date_start,date_end);
 		//date is: Sun Sep 02 2012
 		String[] date_tokens = date.split(" ");
 		//possible array out of bounds exception
 		return date_tokens[3];
 	}
 }
