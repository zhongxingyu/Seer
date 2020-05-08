 package org.manalith.ircbot.plugin.twitreader;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONTokener;
 import org.json.JSONObject;
 
 import org.manalith.ircbot.plugin.twitreader.exceptions.StrDoesntSpecifiedException;
 import org.manalith.ircbot.plugin.twitreader.exceptions.UnknownTypeOfStringException;
 
 public class TwitReaderRunner {
 	private String str;
 
 	private enum StrType {
 		TwitURL, UserURL, ScrName, Unknown
 	}
 
 	public TwitReaderRunner() {
 		this.setStr("");
 	}
 
 	public TwitReaderRunner(String newStr) {
 		this.setStr(newStr);
 	}
 
 	private void setStr(String newStr) {
 		this.str = newStr;
 	}
 
 	private String getStr() {
 		return this.str;
 	}
 
 	public String/* [] */run() {
 
 		String /* [] */result = "";
 
 		try {
 			result = getText(this.validateTwitterStr());
 		} catch (StrDoesntSpecifiedException e) {
 			// result = new String[1];
 			// result[0]
 			result = "문자열이 없습니다";
 			return result;
 		} catch (UnknownTypeOfStringException ue) {
 			result = "알 수 없는 형식의 문자열입니다";
 		}
 		// */
 
		// System.out.println(result);
 		return result;
 	}
 
 	private String getText(StrType type) {
 		String result = "";
 
 		if (type == StrType.TwitURL) {
 			try {
 				JSONObject obj = new JSONObject(new JSONTokener(
 						(new StreamDownloader(this.getJSONURL(type)))
 								.downloadDataStream()));
 
 				String written_by = obj.getJSONObject("user").getString("name");
 				/*
 				 * String written_datetime = obj.getString("created_at"); String
 				 * Creating_Source = obj.getString("source");
 				 */
 				String body = obj.getString("text");
 
 				// result = new String[2];
 				// result[0] = "작성자 : " + written_by + ", 작성시각 : " +
 				// getDateTimeinKoreanFormat(written_datetime) + ", 작성 클라이언트 : "
 				// + Creating_Source.replaceAll(
 				// "\\<(\\/)?[a-zA-Z]+((\\s)[a-zA-Z]+\\=\\\"(\\s|\\S)+\\\")*\\>",
 				// "");
 				result/* [1] */= "작성자 : " + written_by + ", 본문 : " + body;
 			} catch (Exception e) {
 				// result = new String[1];
 				result/* [0] */= e.getMessage();
 				return result;
 			}
 		} else if (type == StrType.UserURL || type == StrType.ScrName) {
 			try {
 				JSONArray arr = new JSONArray(new JSONTokener(
 						(new StreamDownloader(this.getJSONURL(type)))
 								.downloadDataStream()));
				if (arr.length() == 0) {
					result = "게시물이 존재하지 않습니다";
				} else {
					JSONObject obj = arr.getJSONObject(0);

					String written_datetime = obj.getString("created_at");
					String body = obj.getString("text");

					result = "작성시각 : "
							+ getDateTimeinKoreanFormat(written_datetime)
							+ ", 본문 : " + body;
				}
 			} catch (NullPointerException e) {
 				result = "페이지가 존재하지 않습니다";
 			} catch (IOException ie) {
 				result = ie.getMessage();
 			} catch (JSONException je) {
 				result = je.getMessage();
 			}
 		}
 		return result;
 	}
 
 	private StrType validateTwitterStr() throws StrDoesntSpecifiedException,
 			UnknownTypeOfStringException {
 		StrType result = StrType.Unknown;
 
 		if (this.getStr().equals(""))
 			throw new StrDoesntSpecifiedException();
 
 		Pattern twit_url_pattern = Pattern
 				.compile("http(s)?\\:\\/\\/twitter\\.com\\/\\#\\!\\/[a-zA-Z0-9\\_]{1,15}\\/status\\/[0-9]+");
 		Matcher twit_url_pattern_matcher = twit_url_pattern.matcher(this
 				.getStr());
 
 		Pattern user_url_pattern = Pattern
 				.compile("http(s)?\\:\\/\\/twitter\\.com\\/(\\#\\!\\/)?([a-zA-Z0-9\\_]{1,15}(\\/)?){1}");
 		Matcher user_url_pattern_matcher = user_url_pattern.matcher(this
 				.getStr());
 
 		Pattern user_scrname_pattern = Pattern.compile("[a-zA-Z0-9\\_]{1,15}");
 		Matcher user_scrname_pattern_matcher = user_scrname_pattern
 				.matcher(this.getStr());
 
 		if (twit_url_pattern_matcher.matches())
 			result = StrType.TwitURL;
 		else if (user_url_pattern_matcher.matches())
 			result = StrType.UserURL;
 		else if (user_scrname_pattern_matcher.matches())
 			result = StrType.ScrName;
 
 		if (result == StrType.Unknown)
 			throw new UnknownTypeOfStringException();
 
 		return result;
 	}
 
 	private String getJSONURL(StrType type) {
 		String json_requrl = "";
 
 		if (type == StrType.TwitURL) {
 			String[] split_url = this.getStr().split("\\/");
 			String twit_id = split_url[split_url.length - 1];
 			json_requrl = "https://api.twitter.com/1/statuses/show.json?id="
 					+ twit_id + "&include_entities=false";
 		} else if (type == StrType.ScrName)
 			json_requrl = "https://api.twitter.com/1/statuses/user_timeline.json?include_entities=false&include_rts=true&screen_name="
 					+ this.getStr() + "&count=1";
 		else if (type == StrType.UserURL) {
 			String[] userurl = this.getStr().split("\\/");
 			String scrname = userurl[userurl.length - 1];
 			json_requrl = "https://api.twitter.com/1/statuses/user_timeline.json?include_entities=false&include_rts=true&screen_name="
 					+ scrname + "&count=1";
 		}
 
 		return json_requrl;
 	}
 
 	private String getDateTimeinKoreanFormat(String datetime) {
 		String result = "";
 		String[] dayOfWeekKorean = { "일", "월", "화", "수", "목", "금", "토" };
 		String[] month = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
 				"Aug", "Sep", "Oct", "Nov", "Dec" };
 
 		String[] split_datetime_by_space = datetime.split("\\s");
 
 		int yyyy = 0;
 		int MM = 0;
 		int dd = 0;
 		int hh = 0;
 		int mm = 0;
 		int ss = 0;
 		String[] hhmmss;
 
 		GregorianCalendar gdt = null;
 
 		try {
 			yyyy = Integer.parseInt(split_datetime_by_space[5]);
 			MM = this.indexOf(month, split_datetime_by_space[1]);
 			dd = Integer.parseInt(split_datetime_by_space[2]);
 
 			hhmmss = split_datetime_by_space[3].split("\\:");
 
 			hh = Integer.parseInt(hhmmss[0]);
 			mm = Integer.parseInt(hhmmss[1]);
 			ss = Integer.parseInt(hhmmss[2]);
 		} catch (Exception e) {
 			;
 		}
 
 		if (split_datetime_by_space[4].equals("+0000")) {
 			try {
 
 				gdt = new GregorianCalendar(yyyy, MM, dd);
 				gdt.set(Calendar.ERA, GregorianCalendar.AD);
 				gdt.setTimeZone(TimeZone.getTimeZone("UTC"));
 				gdt.set(Calendar.HOUR, hh);
 				gdt.set(Calendar.MINUTE, mm);
 				gdt.set(Calendar.SECOND, ss);
 
 				gdt.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
 				gdt.add(Calendar.HOUR, 9);
 
 			} catch (Exception e) {
 				;
 			}
 		} else {
 			try {
 				gdt = new GregorianCalendar(yyyy, MM, dd);
 				gdt.set(Calendar.ERA, GregorianCalendar.AD);
 				gdt.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
 				gdt.set(Calendar.HOUR, hh);
 				gdt.set(Calendar.MINUTE, mm);
 				gdt.set(Calendar.SECOND, ss);
 
 				result += gdt.get(Calendar.YEAR) + "년 "
 						+ (gdt.get(Calendar.MONTH) + 1) + "월 "
 						+ gdt.get(Calendar.DAY_OF_MONTH) + "일 ";
 				result += dayOfWeekKorean[gdt.get(Calendar.DAY_OF_WEEK) - 1]
 						+ "요일 ";
 				result += String.format("%02d", gdt.get(Calendar.HOUR_OF_DAY))
 						+ ":" + String.format("%02d", gdt.get(Calendar.MINUTE))
 						+ ":" + String.format("%02d", gdt.get(Calendar.SECOND));
 
 			} catch (Exception e) {
 				;
 			}
 		}
 
 		result += gdt.get(Calendar.YEAR) + "년 " + (gdt.get(Calendar.MONTH) + 1)
 				+ "월 " + gdt.get(Calendar.DAY_OF_MONTH) + "일 ";
 		result += dayOfWeekKorean[gdt.get(Calendar.DAY_OF_WEEK) - 1] + "요일 ";
 		result += String.format("%02d", gdt.get(Calendar.HOUR_OF_DAY)) + ":"
 				+ String.format("%02d", gdt.get(Calendar.MINUTE)) + ":"
 				+ String.format("%02d", gdt.get(Calendar.SECOND));
 
 		return result;
 	}
 
 	private int indexOf(String[] str, String value) {
 		int result = -1;
 		int length = str.length;
 
 		for (int i = 0; i < length; i++) {
 			if (str[i].equals(value)) {
 				result = i;
 				break;
 			}
 		}
 
 		return result;
 	}
}
