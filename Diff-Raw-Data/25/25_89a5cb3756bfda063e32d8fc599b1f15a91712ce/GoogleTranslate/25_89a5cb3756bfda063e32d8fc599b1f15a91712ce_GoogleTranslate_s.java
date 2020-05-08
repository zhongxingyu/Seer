 /**
  * @Title: GoogleTranslate.java
  * @Package org.mariotaku.twidere.extension.twitlonger
  * @Description: TODO(调用Google translate网页翻译借口python抓取)
  * @author shangjiyu@gmail.com
  * @date 2013-9-21 下午1:13:41
  * @version V1.0
  */
 
 package org.shangjiyu.twidere.extension.translator;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 /**
  * @ClassName: GoogleTranslate
  * @Description: TODO(发送POST请求，返回请求结果)
  * @author shangjiyu
  * @date 2013-9-21 下午1:13:41
  *
  */
 
 public class GoogleTranslate implements Constants {
 
 	private static final String GOOGLETRANSLATEURL_STRING = Constants.GOOGLETRANSLATEONBAE_STRING;
 	private static final Pattern PATTERN_LINK = Pattern.compile(Constants.NONEED2TRANSLAETPORTION, Pattern.CASE_INSENSITIVE);
 	private static final Pattern PATTERN_ALPHA = Pattern.compile("(11111)");
 	private final ArrayList<String> linkStrings = new ArrayList<String>();
 	private int uneed2TranslateElementIndex = 0;
 	
 	public GoogleTranslate() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public GGTranslateResponse postTranslate(String srcContent) throws GoogleTranslateException {
 		try {
 			String queryString = "";
 			final Matcher matcher = PATTERN_LINK.matcher(srcContent);
 			while (matcher.find()) {
 				if (matcher.group(1) != null) {
 					linkStrings.add(matcher.group(1));
 				}else if (matcher.group(5) != null) {
 					linkStrings.add(matcher.group(5));
 				}else if (matcher.group(12) != null) {
 					linkStrings.add(matcher.group(12));
 				}
 			}
 			queryString = PATTERN_LINK.matcher(srcContent).replaceAll("11111");
 			queryString = URLEncoder.encode(queryString,"UTF-8");
			final String getURL = GOOGLETRANSLATEURL_STRING+"?"+"msg="+queryString;
 			final HttpClient httpclient = new DefaultHttpClient();
 			final HttpGet httpGet = new HttpGet();
 			httpGet.setURI(new URI(getURL));
 //			final HttpPost httpPost = new HttpPost(GOOGLETRANSLATEURL_STRING);
 //			final ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
 //			parameters.add(new BasicNameValuePair("msg", queryString));
 			final HttpResponse response = httpclient.execute(httpGet);
 			return parseTranslateResponse(EntityUtils.toString(response.getEntity()));
 		} catch ( Exception e) {
 			throw new GoogleTranslateException(e);
 		}
 	}
 	
 	
 	/**
 	 * @throws IOException 
 	 * @throws XmlPullParserException 
 	 * @Title: parseTranslateResponse
 	 * @Description: TODO(解析巨硬返回的XML数据)
 	 * @param @param response
 	 * @param @return
 	 * @param @throws JSONException    设定文件
 	 * @return TranslateResponse    返回类型
 	 * @throws
 	 */
 	public GGTranslateResponse parseTranslateResponse(String response) {
 		String from = "yangpi", to = "chinese", translateResult = response;
 		translateResult = replaceURL(PATTERN_ALPHA, translateResult);
 		return new GGTranslateResponse(from, to, translateResult);
 	}
 	
 	public String replaceURL(Pattern pattern,String toReplaceString) {
 		final Matcher matcher = pattern.matcher(toReplaceString);
 		if (matcher.find()) {
 			toReplaceString = matcher.replaceFirst(linkStrings.get(uneed2TranslateElementIndex));
 			uneed2TranslateElementIndex++;
 			return replaceURL(pattern, toReplaceString);
 		}else {
 			return toReplaceString;
 		}
 	}
 	
 	public static class GoogleTranslateException extends Exception {
 		
 		private static final long serialVersionUID = 1020016463204999157L;
 		
 		public GoogleTranslateException() {
 			super();
 		}
 		
 		public GoogleTranslateException(String detailMsg) {
 			super(detailMsg);
 		}
 		
 		public GoogleTranslateException(Throwable throwable) {
 			super(throwable);
 		}
 		
 		public GoogleTranslateException(String detailMsg, Throwable throwable) {
 			super(detailMsg, throwable);
 		}
 	}
 	
 	/**
 	 * @ClassName: TranslateResponse
 	 * @Description: TODO(翻译结果对象)
 	 * @author shangjiyu
 	 * @date 2013-9-21 下午4:18:58
 	 *
 	 */
 	public static class GGTranslateResponse {
 		public final String from, to, translateResult;
 
 		private GGTranslateResponse(String from, String to, String translateResult) {
 			this.from = from;
 			this.to = to;
 			this.translateResult = translateResult;
 		}
 	}
 }
