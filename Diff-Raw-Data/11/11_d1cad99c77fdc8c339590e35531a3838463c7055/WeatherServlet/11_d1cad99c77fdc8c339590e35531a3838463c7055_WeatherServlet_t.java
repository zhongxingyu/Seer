 package com.nbcedu.function.schoolmaster2.servlet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 @SuppressWarnings("serial")
 public class WeatherServlet extends HttpServlet{
 	
 	private static final Logger logger = Logger.getLogger(WeatherServlet.class);
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		this.doPost(req, resp);
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String cityId = req.getParameter("cityId");
 		String callBack = req.getParameter("jsoncallback");
 		if (isNotBlank(cityId)) {
 			String result = formatJSONP(callBack,getWeather(cityId));
 			if(isNotBlank(result)){
 				resp.setCharacterEncoding("utf-8");
 				resp.getWriter().write(result);
 				resp.getWriter().flush();
 			}
 		}
 	
 	}
 	
 	private static String formatJSONP(String callBack,String origin){
 		if(isNotBlank(origin)){
 			String function = "weatherCallBack";//default callbackName;
 			if(isNotBlank(callBack)){
 				function = callBack;
 			}
 			return function + "(" + origin + ")";
 		}
 		return "";
 	}
 	
 	private static String getWeather(String cityId) {
 		String result = "";
 
 		HttpURLConnection connection = null;
 		try {
 			
 			connection = (HttpURLConnection) new URL(
 					"http://m.weather.com.cn/data/${cityId}.html".replace(
 							"${cityId}", cityId)).openConnection();
 			connection.setDoOutput(true);
 			connection.setRequestMethod("GET");
 			connection.setRequestProperty("accept", "text/xml;text/html");
 			connection.setUseCaches(false);
 			connection.connect();
 			if (connection.getResponseCode() == 200) {
 				StringBuilder responseStr = new StringBuilder("");
 				BufferedReader reader = null;
 				try {
 					reader = new BufferedReader(new InputStreamReader(
 							connection.getInputStream()));
 					for (String line = reader.readLine(); line != null; line = reader
 							.readLine()) {
 						responseStr.append(line);
 					}
 				} catch (Exception e) {
					logger.error(e);
 				} finally {
 					if (reader != null)
 						reader.close();
 				}
 				connection.disconnect();
 				result = responseStr.toString();
 			}
		} catch (MalformedURLException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
 		} finally {
 			if (connection != null) {
 				connection.disconnect();
 			}
 		}
 		return result;
 	}
 	
 	private static final boolean isNotBlank(String str){
 		return str!=null&&!str.trim().isEmpty();
 	}
 }
