 package com.daodao.tools;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.daodao.dao.DictionaryDAO;
 import com.daodao.model.DictionaryDO;
 
 public class TranslateWord {
 
 	private static final String YOUDAO_URL = "http://fanyi.youdao.com/openapi.do?keyfrom=englishtester&key=578714415&type=data&doctype=json&version=1.1&q=";
 
 	private static DictionaryDAO dictionaryDAO;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
 				"applicationContext.xml");
 		dictionaryDAO = (DictionaryDAO) context.getBean("dictionaryDAO");
 		youdaoAPI("sample_IELTS.txt");
 	}
 
 	public static void youdaoAPI(String fileName) {
 		HttpClient client = null;
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new InputStreamReader(
 					TranslateWord.class.getClassLoader().getResourceAsStream(
 							fileName)));
 			String word = null;
 			client = new DefaultHttpClient();
 			while ((word = reader.readLine()) != null) {
				if (word.indexOf(" ") != -1) {
					word = word.replaceAll(" ", "%20");
				}
 				HttpGet request = new HttpGet(YOUDAO_URL + word);
 				HttpResponse response = client.execute(request);
 				try {
 					String content = EntityUtils.toString(response.getEntity());
 					JSONObject jsonAll = JSONObject.fromObject(content);
 					// String phonetic = basicObj.getString("phonetic");
 					StringBuilder explainStr = new StringBuilder();
					if (jsonAll.containsKey("basic")) {
						JSONObject basicObj = jsonAll.getJSONObject("basic");
						JSONArray explains = basicObj.getJSONArray("explains");
						for (int index = 0; index < explains.size(); index++) {
							explainStr.append(explains.get(index));
						}
					} else {
						explainStr.append(jsonAll.getString("translation"));
 					}
 					System.out.println(word + "\t" + explainStr.toString());
 					DictionaryDO dictionaryDO = new DictionaryDO();
 					dictionaryDO.setAccurate(0);
 					dictionaryDO.setEn(word);
 					dictionaryDO.setSource(fileName);
 					dictionaryDO.setZh(explainStr.toString());
 					System.out.println(dictionaryDAO.saveEntity(dictionaryDO));
 					// api request limit under 1000 times
					Thread.sleep(4000);
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					System.out.println("Error in " + word);
 					continue;
 				}
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (client != null) {
 				client.getConnectionManager().shutdown();
 			}
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
