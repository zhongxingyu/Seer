 package uk.ac.prospects.w4.webapp;
 
 import org.springframework.util.StringUtils;
 
 /**
  * Constructs the string containing the parameters that need to be preserved accross the widget
  */
 public class UrlBuilder {
 
 	/**
 	 * Builds the parameter string for the links to calendar page from other pages
 	 *
 	 * @param provId
 	 * @param provTitle
 	 * @param keyword
 	 * @param fromStartDate
 	 * @param toStartDate
 	 * @param courseTitle
 	 * @return The parameter string that needs to be preserved when going to the calendar page from any other page.
 	 */
 	public static String buildCalendarURL(String provId, String provTitle, String keyword, String fromStartDate, String toStartDate, String courseTitle){
 		StringBuilder sb = buildBasicURL(provId, provTitle, keyword, courseTitle);
 
 		StringBuilder calendarSb = new StringBuilder();
 		if (StringUtils.hasText(fromStartDate) || StringUtils.hasText(toStartDate) || StringUtils.hasText(courseTitle)) {
 			//calendarSb.append("preserve=");
 			if (StringUtils.hasText(fromStartDate)){
 				calendarSb.append("fromStartDate:" + fromStartDate);
 			}
 			if (StringUtils.hasText(toStartDate)){
 				if (calendarSb.length() > 0)
 					calendarSb.append(",");
 				calendarSb.append("toStartDate:" + toStartDate);
 			}
 		}
 
 		if ((sb.length() > 0) && (calendarSb.length() > 0)){
 			sb.append("&");
 			sb.append("preserve=");
 			sb.append(calendarSb);
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * Builds the parameter string to all pages apart from calendar
 	 *
 	 * @param provId
 	 * @param provTitle
 	 * @param keyword
 	 * @param fromStartDate
 	 * @param toStartDate
 	 * @param courseTitle
 	 * @return The parameter string that needs to be preserved when going to any page in the widget apart from calendar
 	 */
 	public static String buildGeneralURL(String provId, String provTitle, String keyword, String fromStartDate, String toStartDate, String courseTitle){
 		StringBuilder sb = buildBasicURL(provId, provTitle, keyword, courseTitle);
 		if (StringUtils.hasText(fromStartDate)){
 			if (sb.length() > 0)
 				sb.append("&");
 			sb.append("fromStartDate=" + fromStartDate);
 		}
 		if (StringUtils.hasText(toStartDate)){
 			if (sb.length() > 0)
 				sb.append("&");
 			sb.append("toStartDate=" + toStartDate);
 		}
 
 		return sb.toString();
 	}
 
 	public static String buildCalendarURLForCalendarPage(String provId, String provTitle, String keyword, String courseTitle, String preserve){
 		StringBuilder sb = new StringBuilder(buildBasicURL(provId, provTitle, keyword, courseTitle));
 
 		if (StringUtils.hasText(preserve)){
 			if (sb.length() > 0)
 				sb.append("&");
 			sb.append("preserve=" + preserve);
 		}
 
 		return sb.toString();
 	}
 
 	public static String buildGeneralURLForCalendarPage(String provId, String provTitle, String keyword, String courseTitle, String preserve){
 		StringBuilder sb = new StringBuilder(buildBasicURL(provId, provTitle, keyword, courseTitle));
 
 
 		if (StringUtils.hasText(preserve)){
 			StringBuilder calendarSb = new StringBuilder();
 			String[] parameterPairs = preserve.split(",");
 			for (String pair : parameterPairs){
 				if (calendarSb.length() > 0){
 					calendarSb.append("&");
 				}
 				calendarSb.append(pair.split(":")[0]);
 				calendarSb.append("=");
 				calendarSb.append(pair.split(":")[1]);
 			}
 			if (sb.length() > 0){
 				sb.append("&");
				sb.append(calendarSb);
 			}
 		}
 
 
 		return sb.toString();
 	}
 
 	private static StringBuilder buildBasicURL(String provId, String provTitle, String keyword, String courseTitle) {
 			StringBuilder sb = new StringBuilder();
 			if (StringUtils.hasText(provId)){
 				sb.append("provid=" + provId);
 			}
 			if (StringUtils.hasText(provTitle)){
 				if (sb.length() > 0)
 					sb.append("&");
 				sb.append("provTitle=" + provTitle);
 			}
 			if (StringUtils.hasText(keyword)){
 				if (sb.length() > 0)
 					sb.append("&");
 				sb.append("keyword=" + keyword);
 			}
 			if (StringUtils.hasText(courseTitle)){
 				if (sb.length() > 0)
 					sb.append("&");
 				sb.append("courseTitle=" + courseTitle);
 			}
 			return sb;
 		}
 }
