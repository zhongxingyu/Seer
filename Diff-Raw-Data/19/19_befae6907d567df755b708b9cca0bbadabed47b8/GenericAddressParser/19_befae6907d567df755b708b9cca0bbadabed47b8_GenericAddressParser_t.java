 package com.orange.groupbuy.addressparser;
 
 import java.io.File;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.orange.common.utils.DateUtil;
 import com.orange.common.utils.FileUtils;
 import com.orange.common.utils.html.HtmlUtils;
 import com.orange.common.utils.http.HttpDownload;
 
 public class GenericAddressParser extends CommonAddressParser {
 
 	
 	
 	private List<String> addList = new LinkedList<String>();
 
 	final static String DEFAULT_TEMP_ADDRESS_PATH = "./data/temp/address/";
 	
 	private synchronized String getAddressTempFilePath(){
 					
 		String dateDir = DateUtil.dateToStringByFormat(new Date(), "yyyyMM");
 		String path = DEFAULT_TEMP_ADDRESS_PATH.concat(dateDir).concat("/");
 		
 		File file = new File(path);
 		if (!file.exists())
 			file.mkdirs();
 		
 		return path;
 	}
 	
 	@Override
 	public List<String> doParseAddress(String url) {
 		try {
 			addList.clear();
 			long fetchTime = System.currentTimeMillis();
 
 
 //			String fileDir = getAddressTempFilePath();
 //			String filePath = fileDir.concat(URLEncoder.encode(url, "UTF-8"));
 //			File file = new File(filePath);
 //			if (!file.exists()){
 //				boolean result = HttpDownload.downloadFile(url, filePath);
 //				if (result == false){
 //					log.severe("<doParseAddress> fail to download file for parsing address, file = "+filePath);
 //					return null;
 //				}
 //				
 //				file = new File(filePath);
 //				if (!file.exists()){						
 //					log.severe("<doParseAddress> download file OK but cannot read file, file = "+filePath);
 //					return null;
 //				}
 //			}
 			
 			
 			
 			
 			Connection connection = Jsoup.connect(url).timeout(20*1000);	
 			Document doc = connection.get();
 //			String htmlString = FileUtils.stringFromFile(file);			
 //			Document doc = Jsoup.parse(htmlString);
 			if (doc != null) {
 				long parseStartTime = System.currentTimeMillis();
 				find_common_add(doc, url);
 				long parseEndTime = System.currentTimeMillis();
 //				log.info("<doParseAddress> parsing addrestrs, network "
 //						+ (parseStartTime - fetchTime)
 //						+ " millseconds, parse "
 //						+ (parseEndTime - parseStartTime) + " millseconds");
 			}
 
 		} catch (Exception e) {
 			log.severe("<doParseAddress> catch exception = "+e.toString());
 			e.printStackTrace();
 			return null;
 		}
 		return addList;
 	}
 
 	/**
 	 * 
 	 */
 	private void find_common_add(Document doc, String url) {
 		try {
 			Elements list = (Elements) doc.getElementsByTag("div");
 			for (Element element : list) {
 				String content = element.text();
 				//TODO remove
 //				System.out.println(content);
 				String[] strs = content.split("\\s");
 				if (strs == null)
 					return;
 				int len = strs.length;
 				for (int i = 0; i < strs.length; i++) {
 					String str = strs[i];
 					int index = str.indexOf("地址：");
 					if (index != -1) {
 						str = str.substring(index + 3);
 						strs[i] = str;
 					}
 					index = str.indexOf("电话");
 					if (index != -1) {
 						str = str.substring(0, index);
 						strs[i] = str;
 					}
 				}
 				int[] scores = new int[len];
 				for (int i = 0; i < len; i++)
 					scores[i] = 0;
 				for (int i = 0; i < len; i++) {
 					if (strs[i].length() > 5) {
 						scores[i] = addScore(strs[i]);
 					}
 				}
 				// bubble sort
 				int temp;
 				String stemp;
 				for (int i = 0; i < scores.length; i++) {
 					for (int j = 0; j < scores.length - i - 1; j++) {
 						if (scores[j] < scores[j + 1]) {
 							temp = scores[j];
 							scores[j] = scores[j + 1];
 							scores[j + 1] = temp;
 							// swap the string at the same time
 							stemp = strs[j];
 							strs[j] = strs[j + 1];
 							strs[j + 1] = stemp;
 						}
 					}
 				}
 				int i = 0;
 				// consider the score
 				for (i = 0; i < len; i++) {
 					if (scores[i] >= 2) {
 						addtoList(strs[i]);
 					}
 				}
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private boolean addtoList(String str) { // return true if a valid address is
 											// found
 		str = str.trim();
 		int len = str.length();
 		if (len <= 0)
 			return false;
		
		final String ADDRESS1 = "地址:";
		final String ADDRESS2 = "地址：";
		final String ADDRESS3 = "地址";
		
		int index1 = str.indexOf(ADDRESS1);
		int index2 = str.indexOf(ADDRESS2);
		int index3 = str.indexOf(ADDRESS3);
		if (index1 != -1) {
			str = str.substring(index1 + ADDRESS1.length());
		}
		else if (index2 != -1){
			str = str.substring(index2 + ADDRESS2.length());
		}
		else if (index3 != -1){
			str = str.substring(index3 + ADDRESS3.length());			
 		}
 		
 		// delete the illegal string
 		str = delString(str);
 
 		// TODO remove
 //		System.out.println("<debug> parse str result=" + str);
 
 		if (str.length() > 5 && str.length() < 50) {
 			if (str.contains("。") || str.contains("，") || (addScore(str) < 2)) {
 //				System.out.println("<debug> have the illegal code= " + str);
 			} else if (addList.indexOf(str) == -1) {
 				addList.add(str);
 //				System.out.println("<debug> final result=" + str);
 			} else {
 //				System.out.println("<debug> have the same address!");
 			}
 			return true;
 		} else {
 //			System.out.println("<debug> parse str, address length "
 //					+ str.length() + " too short or too long, skip");
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private int addScore(String str) {
 		int score = 0;
 		if (str.contains("市"))
 			score++;
 		if (str.contains("区"))
 			score++;
 		if (str.contains("路"))
 			score++;
 		if (str.contains("街"))
 			score++;
 		if (str.contains("道"))
 			score++;
 		if (str.contains("号"))
 			score++;
 		if (str.contains("楼"))
 			score++;
 		if (str.contains("巷"))
 			score++;
 		if (str.contains("层"))
 			score++;
 		if (str.contains("铺"))
 			score++;
 		if (str.contains("广场"))
 			score++;
 
 		return score;
 	}
 
 	/**
 	 * 
 	 */
 	private String delString(String str) {
 		int index = 0;
 		// TODO for gaopang
 		str = str.replace(" ", "");
 		// TODO for meituan
 		index = str.indexOf("店：");
 		if (index != -1) {
 			str = str.substring(index + 2);
 		}
 		index = str.indexOf("（查看地图）");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		// TODO for manzuo
 		index = str.indexOf("公交信息");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("联系");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("联络");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("咨询");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("预订");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("预约");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("公交");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("客服");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("营业");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("交通");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 		index = str.indexOf("乘车");
 		if (index != -1) {
 			str = str.substring(0, index);
 		}
 
 		return str;
 	}
 
 }
