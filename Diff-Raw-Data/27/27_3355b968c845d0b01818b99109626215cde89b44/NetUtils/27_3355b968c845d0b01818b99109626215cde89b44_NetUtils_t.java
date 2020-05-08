 package neo.java.commons;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * 网络相关的工具类
  * 
  * @author neo
  */
 public class NetUtils {
 
 	/** User-Agent 标识 */
 	private static String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)";
 
 	/**
 	 * 输入流转字符串
 	 * 
 	 * @param inputStream
 	 *            输入流
 	 * @param enc
 	 *            指定要转换的字符集
 	 * @throws IOException
 	 * @return 转换后的字符串
 	 */
 	public static String inputStream2String(InputStream inputStream, String enc)
 			throws IOException {
		StringBuilder sBuilder = new StringBuilder();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				inputStream, enc), 512);
 
 		String tmpString = reader.readLine();
		while (null != tmpString) {
			sBuilder.append(tmpString + "\n");
 			tmpString = reader.readLine();
 		}
 
 		reader.close();
		return sBuilder.toString();
 	}
 
 	/**
 	 * 设置模拟 POST/GET 请求的 UserAgent
 	 * 
 	 * @param userAgent
 	 *            待设置的 User-Agent
 	 */
 	public static void setUserAgent(String userAgent) {
 		if (null != userAgent) {
 			USER_AGENT = userAgent;
 		} else {
 			USER_AGENT = "";
 		}
 	}
 
 	/**
 	 * 通过地址获取输入流
 	 * 
 	 * @param url
 	 *            地址
 	 * @param enc
 	 *            指定要转换的字符集
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @return 获取到输入流
 	 */
 	public static InputStream getInputStreamFromURL(String url, String enc)
 			throws MalformedURLException, IOException {
 		HttpURLConnection connection = (HttpURLConnection) (new URL(url)
 				.openConnection());
 		connection.setUseCaches(false);
 		connection.setRequestMethod("GET");
 		connection.setRequestProperty("Accept", "*/*");
 		connection.setRequestProperty("User-Agent", USER_AGENT);
 		connection.setRequestProperty("Charset", enc);
 		return connection.getInputStream();
 	}
 
 	/**
 	 * 发送 GET 请求
 	 * 
 	 * @param url
 	 *            地址
 	 * @param enc
 	 *            指定要转换的字符集
 	 * @throws IOException
 	 * @return GET 请求的返回数据
 	 */
 	public static String requestGET(String url, String enc) throws IOException {
 		return inputStream2String(getInputStreamFromURL(url, enc), enc);
 	}
 
 	/**
 	 * 发送 POST 请求
 	 * 
 	 * @param url
 	 *            地址
 	 * @param params
 	 *            表单信息
 	 * @param enc
 	 *            指定要转换的字符集
 	 * @throws IOException
 	 * @return POST 请求的返回数据
 	 */
 	public static String requestPOST(String url, String params, String enc)
 			throws IOException {
 		HttpURLConnection connection = (HttpURLConnection) (new URL(url)
 				.openConnection());
 		connection.setUseCaches(false);
 		connection.setRequestMethod("POST");
 		connection.setRequestProperty("Accept", "*/*");
 		connection.setRequestProperty("User-Agent", USER_AGENT);
 		connection.setRequestProperty("Charset", enc);
 
 		connection.setDoOutput(true);
 		connection.getOutputStream().write(params.getBytes());
 		connection.getOutputStream().flush();
 		connection.getOutputStream().close();
 		return inputStream2String(connection.getInputStream(), enc);
 	}
 
 }
