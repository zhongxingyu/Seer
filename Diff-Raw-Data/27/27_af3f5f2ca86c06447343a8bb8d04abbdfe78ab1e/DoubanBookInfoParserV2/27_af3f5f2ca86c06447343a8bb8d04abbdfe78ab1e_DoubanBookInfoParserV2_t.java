 package com.intalker.borrow.isbn.parser;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.zip.GZIPInputStream;
 
import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.intalker.borrow.util.WebUtil;
 
 public class DoubanBookInfoParserV2 extends BookInfoParser {
 	private final static String ISBN_SEARCHURL_DOUBAN_V2 = "http://api.douban.com/v2/book/isbn/";
 
 	public DoubanBookInfoParserV2() {
 	}
 
 	@Override
 	public void parse() {
 		String url = ISBN_SEARCHURL_DOUBAN_V2 + mISBN;
 		try {
 
 			HttpURLConnection conn = (HttpURLConnection) new URL(url)
 					.openConnection();
 			conn.setConnectTimeout(8000);
 			conn.setRequestMethod("GET");
 			if (conn.getResponseCode() == 200) {
 				GZIPInputStream inputStream = (GZIPInputStream) conn
 						.getContent();
 
 				BufferedReader br = new BufferedReader(new InputStreamReader(
 						inputStream, "UTF-8"));
 
 				StringBuilder sb = new StringBuilder();
 				String line;
 				while ((line = br.readLine()) != null) {
 					sb.append(line);
 				}
 
 				br.close();
 				String strResult = sb.toString();
 
 				JSONObject jsonObject = new JSONObject(strResult);
 
 				if (null == mCoverImage) {
 					String imageURL = jsonObject.getString("image");
 					mCoverImage = WebUtil.getImageFromURL(imageURL);
 				}
 				mBookName = jsonObject.getString("title");
				JSONArray jsonAuthorList = jsonObject.getJSONArray("author");
				mAuthor = formatStringArray(jsonAuthorList, ",");
 				mPublisher = jsonObject.getString("publisher");
 				mPageCount = jsonObject.getString("pages");
 				mDescription = jsonObject.getString("summary");
 			}
 			if (null != conn) {
 				conn.disconnect();
 			}
 		} catch (Exception e) {
 			return;
 		}
 	}
	
	public static String formatStringArray(JSONArray jsonArray, String separator) {
		if (null == separator) {
			separator = "";
		}
		StringBuilder sb = new StringBuilder();
		try {
			int length = jsonArray.length();
			for (int i = 0; i < length; ++i) {
				String str = (String) jsonArray.get(i);
				if (str.length() > 0) {
					if (i > 0) {
						sb.append(separator);
					}
					sb.append(str);
				}
			}
		} catch (Exception ex) {
		}
		return sb.toString();
	}
 }
