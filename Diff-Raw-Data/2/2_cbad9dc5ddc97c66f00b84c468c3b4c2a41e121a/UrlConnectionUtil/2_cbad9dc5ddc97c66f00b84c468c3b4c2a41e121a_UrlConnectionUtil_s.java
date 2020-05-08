 package com.xuechong.utils.http;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Collection;
 
 import com.xuechong.utils.http.mapping.Parameter;
 
 
 public class UrlConnectionUtil {
 	
 	/**
 	 * 发送POST请求并返回结果(如果请求失败则返回NULL)
 	 * @param url 请求地址
 	 * @param parameters 参数集合
 	 * @return 服务器响应数据 (如果请求失败则返回NULL)
 	 * @throws IOException
 	 * @author xuechong
 	 */
 	public static String postReuqest(String url,Collection<Parameter> parameters) 
 		throws IOException {
 		
 		URL requestUrl = new URL(url);
 		HttpURLConnection connection =(HttpURLConnection) requestUrl.openConnection();
 		connection.setDoOutput(true);
 		connection.setDoInput(true);   
 		
 		connection.setRequestMethod("POST"); 
 		connection.setRequestProperty("accept", "text/xml;text/html");  
 		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 		//connection.setConnectTimeout(1000*5);
 		connection.setRequestProperty("Accept-Charset", "UTF-8");
 		connection.setUseCaches(false);
 		connection.connect();
 		StringBuffer responseStr = new StringBuffer(""); 
 		try {
			getResponseStr(parameters, connection);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}finally{
 			connection.disconnect();
 		}
 		return responseStr.toString();
 	}
 	private static StringBuffer getResponseStr(
 			Collection<Parameter> parameters, HttpURLConnection connection)
 			throws IOException {
 		StringBuffer responseStr = new StringBuffer("");
 		OutputStream out = connection.getOutputStream();
 		try {
 			out.write(buildParameterStr(parameters).getBytes("UTF-8"));
 			out.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}finally{
 			if(out!=null)out.close();
 		}
 		
 		if(connection.getResponseCode()==200){
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					connection.getInputStream()));
 			try {
 				for (String line = reader.readLine();line!=null;line = reader.readLine()) {
 					responseStr.append(line);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}finally{if (reader!=null) reader.close();}
 		}
 		return responseStr;
 	}
 	/**
 	 * 发送POST请求 (启动新线程)
 	 * @param url 请求地址
 	 * @param parameters 参数集合
 	 * @author xuechong
 	 */
 	public static void postNewThreadReuqest
 		(final String url,final Collection<Parameter> parameters) {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					postReuqest(url, parameters);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 	/**
 	 * 封装参数，需要URL编码
 	 * @param parameters
 	 * @return
 	 * @author xuechong
 	 */
 	private static String buildParameterStr(Collection<Parameter> parameters){
 		StringBuilder sb = new StringBuilder("");
 		if(parameters!=null&&parameters.size()>0){
 			for (Parameter param : parameters) {
 				sb.append(param.getKey());
 				sb.append("=");
 				try {
 					sb.append(param.getValue()!=null&&!param.getValue().isEmpty() ?
 							URLEncoder.encode(param.getValue(), "UTF-8") : "");
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 					sb.append("");
 				}
 				sb.append("&");
 			}
 			sb.deleteCharAt(sb.length()-1);
 		}
 		return sb.toString();
 	}
 	
 }
