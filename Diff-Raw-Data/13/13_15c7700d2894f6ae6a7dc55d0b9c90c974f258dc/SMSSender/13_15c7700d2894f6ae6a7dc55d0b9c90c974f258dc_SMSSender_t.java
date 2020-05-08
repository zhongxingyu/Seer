 package ilunch.http;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SMSSender {
 	
 	//return values
 	public static final int INVALID_PARAMS = 100;
 	public static final int CONTENT_TOO_LONG = 200;
 	public static final int SUCCESS = 0;
 	public static final int INVALID_PHONE_NUMBER = 300;
 	public static final int INSUFFICIENT_FUNDS  = 400;
 	public static final int NO_SUCH_USER  = 500;
 	public static final int INCORRECT_PASSWORD  = 600;
 	public static final int INCORRECT_PARAM_NUM_OR_PARAM_VALUE  = 700;
 	public static final int INCORRECT_SMS_ENCODING  = 800;
 	public static final int CONTENT_CONTAINS_ILLEGAL_WORD  = 900;
 	public static final int UNKNOW_ERROR  = 1000;
 	public static final int TOO_MANY_PHONE_NUMBERS  = 1100;
 	
 
 	private static final String API_URL = "http://service.winic.org/sys_port/gateway/?";
	private static final String UID = "idealllee";
	private static final String PWD = "ilunchTest123";
 	
 	private static final String ALERT_TO = "18621181017";
 	private static final String ALSERT_CONTENT = "[iLunch.cn]短信平台余额剩余即将用尽，请及时充值";
 	private static final int ALERT_BOTTOM_LINE = 500;
 	
 	private static final String TMONEY_ID = "Tmoney:";
 
 	/**
 	 * To send SMSs
 	 * @param phoneNumbers - an array of phone number string. a phone number string should contains digits only.
 	 * @param content - Content of the SMS. Should be less than 70 characters.
 	 * @return return value
 	 */
 	public static int sendSMS(String[] phoneNumbers, String content) {
 		if(phoneNumbers == null || phoneNumbers.length <= 0 || content == null || content.isEmpty()) {
 			return SMSSender.INVALID_PARAMS;
 		}
 		
 		if(content.length() > 70) {
 			return SMSSender.CONTENT_TOO_LONG;
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		for(int i = 0; i < phoneNumbers.length; i++) {
 			if(!phoneNumbers[i].matches("^\\d+$"))
 				return SMSSender.INVALID_PHONE_NUMBER;
 			sb.append(phoneNumbers[i]);
 			if(i != (phoneNumbers.length-1))
 				sb.append(',');
 		}
 		
 		String result = SMSSender.doSend(sb.toString(), content);
 		try {
 			Pattern p = Pattern.compile(".*"+SMSSender.TMONEY_ID+".*(\\d+).*");
 			Matcher m = p.matcher(result);
 			if(m.matches())
 				if(Integer.parseInt(m.group(1)) < SMSSender.ALERT_BOTTOM_LINE)
 					SMSSender.sendAlert();
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 		return SMSSender.parseResult(result);
 	}
 
 	public static int sendAlert() {
 		return SMSSender.parseResult(SMSSender.doSend(SMSSender.ALERT_TO, SMSSender.ALSERT_CONTENT));
 	}
 
 	private static int parseResult(String result) {
 		if(result.startsWith("-")) {
 			if(result.startsWith("-01"))
 				return SMSSender.INSUFFICIENT_FUNDS;
 			else if(result.startsWith("-02"))
 				return SMSSender.NO_SUCH_USER;
 			else if(result.startsWith("-03"))
 				return SMSSender.INCORRECT_PASSWORD;
 			else if(result.startsWith("-04"))
 				return SMSSender.INCORRECT_PARAM_NUM_OR_PARAM_VALUE;
 			else if(result.startsWith("-05"))
 				return SMSSender.INVALID_PHONE_NUMBER;
 			else if(result.startsWith("-06"))
 				return SMSSender.INCORRECT_SMS_ENCODING;
 			else if(result.startsWith("-07"))
 				return SMSSender.CONTENT_CONTAINS_ILLEGAL_WORD;
 			else if(result.startsWith("-08"))
 				return SMSSender.UNKNOW_ERROR;
 			else if(result.startsWith("-09"))
 				return SMSSender.UNKNOW_ERROR;
 			else if(result.startsWith("-10"))
 				return SMSSender.TOO_MANY_PHONE_NUMBERS;
 			else if(result.startsWith("-11"))
 				return SMSSender.CONTENT_TOO_LONG;
 			else
 				return SMSSender.UNKNOW_ERROR;
 		}
 		else
 			return SMSSender.SUCCESS;
 	}
 	
 	private static String doSend(String phoneNumberParam, String content) {
 		HttpURLConnection httpconn = null;
 		String result="-20";
 		StringBuilder sb = new StringBuilder();
 		
 		try {
 			sb.append(SMSSender.API_URL);
 			sb.append("id=").append(SMSSender.UID);
 			sb.append("&pwd=").append(SMSSender.PWD);
 			sb.append("&to=").append(phoneNumberParam);
			sb.append("&content=").append(URLEncoder.encode(content, "GB2312"));
 			URL url = new URL(sb.toString());
 			httpconn = (HttpURLConnection) url.openConnection();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
 			result = rd.readLine();
 			rd.close();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally{
 			if(httpconn!=null){
 				httpconn.disconnect();
 				httpconn=null;
 			}
 
 		}
 		return result;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String[] ps = {
 				"18621181017",
				"18621077586"
 		};
		System.out.println(SMSSender.sendSMS(ps, "[ilunch测试]Hi,你妈喊你回家吃饭啦ABCD1234~!@#$%^&*()"));
 	}
 
 }
 
