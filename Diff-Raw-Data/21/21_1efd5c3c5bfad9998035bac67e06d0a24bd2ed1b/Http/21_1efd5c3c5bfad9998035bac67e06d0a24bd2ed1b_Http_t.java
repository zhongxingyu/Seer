 package com.htb.cnk.lib;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import android.util.Log;
 
 import com.htb.cnk.data.Reservation;
 import com.htb.constant.ErrorNum;
 import com.htb.constant.Limit;
 import com.htb.constant.Server;
 
 /**
  * @author josh
  * 
  */
 public class Http {
 	final static String TAG = "Http";
 	private static int mErrno;
 	
 	public static String get(String page, String param) {
 		String ret;
 		
 		for (int i=0; i<Limit.RETRY; i++) {
 			ret = httpRequestGet(page, param);
 			if (ret != null) {
 				return ret;
 			}
 		}
 		
 		return null;
 	}
 	
 	public static String post(String page, String msg) {
 		String ret;
 		
 		for (int i=0; i<Limit.RETRY; i++) {
 			ret = httpRequestPost(page, msg);
 			if (ret != null) {
 				return ret;
 			}
 		}
 		
 		return null;
 	}
 	
 	public static String submitReservation(Reservation reservation, String json) {
 		HttpParams httpParameters1 = new BasicHttpParams();
 
 		// 超时设置
 		HttpConnectionParams.setConnectionTimeout(httpParameters1, 15 * 1000);
 		HttpConnectionParams.setSoTimeout(httpParameters1, 60 * 1000);
 
 		Map<String, String> params = new HashMap<String, String>();
 		String param = "";
 		
 		try {
 			param = new String(json.getBytes(), "UTF-8");
 			params.put("reservationJson", param);
 			param = new String(reservation.getName().getBytes(), "UTF-8");
 			params.put("customerName", param);
 			param = new String(reservation.getTel().getBytes(), "UTF-8");
 			params.put("phoneNum", param);
 			param = new String(reservation.getDatetime().getBytes(), "UTF-8");
 			params.put("reservationTime", param);
 			param = new String("true".getBytes(), "UTF-8");
 			params.put("cashPledgeYN", param);
 			param = new String(Integer.toString(reservation.getDeposit()).getBytes(), "UTF-8");
 			params.put("cashPledge", param);
 			String tableNames = reservation.getTableNames();
 			
 			int tableCount;
 			if (tableNames ==null || tableNames.length() <= 0) {
 				tableCount = 0;
 			} else {
 				tableCount = tableNames.split(",").length;
 			};
 			param = new String(Integer.toString(tableCount).getBytes(), "UTF-8");
 			params.put("tableNum", param);
 			param = new String(Integer.toString(reservation.getPersons()).getBytes(), "UTF-8");
 			params.put("personNum", param);
 			param = new String((tableCount>0?"true":"false").getBytes(), "UTF-8");
 			params.put("submitTableYN", param);
 			String tableIds = reservation.getTableIds();
 			if (tableIds != null) {
 				String tableId[] = tableIds.split(",");
 				for (int i=0; i<tableId.length; i++) {
 					param = new String(tableId[i].getBytes(), "UTF-8");
 					params.put("arrID["+i+"]", param);
 				}
 			} else {
 				param = new String("null".getBytes(), "UTF-8");
 				params.put("arrID", param);
 			}
 
 			param = new String(reservation.getTableNames(" ").getBytes(), "UTF-8");
 			params.put("arrName", param);
 			param = new String(reservation.getComment().getBytes(), "UTF-8");
 			params.put("remark", param);
 			param = new String(reservation.getAddr().getBytes(), "UTF-8");
 			params.put("Addr", param);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		List<NameValuePair> ls = new ArrayList<NameValuePair>();
 		Set<String> keys = params.keySet();
 		for (String key : keys) {
 			ls.add(new BasicNameValuePair(key, params.get(key).toString()));
 		}
 
 		String url = Server.SERVER_DOMIN + "/" + "managePC/client/addReservationDB.php";
 
 		DefaultHttpClient client;
 		client = new DefaultHttpClient(httpParameters1);
 		
 		if ("".equals(url)) {
 			mErrno = ErrorNum.HTTP_NO_CONECTION;
 			return null;
 		}
 
 		HttpPost post = new HttpPost(url);
 		post.setHeader("User-Agent", "Mozilla/4.5");
 		post.setHeader("connection", "Keep-Alive");
 		post.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
 
 		try {
 			post.setEntity(new UrlEncodedFormEntity(ls, HTTP.UTF_8));
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 			mErrno = ErrorNum.UTF8_NOT_SUPPORTED;
 			return null;
 		}
		
 		HttpResponse httpResponse = null;
 		try {
 			httpResponse = client.execute(post);
 		} catch (IOException e) {
 			e.printStackTrace();
 			mErrno = ErrorNum.HTTP_NO_CONECTION;
 			return null;
 		}
 
 		int statusCode = httpResponse.getStatusLine().getStatusCode();
 		if (statusCode == 200) {
 			try {
 				return EntityUtils.toString(httpResponse.getEntity());
 			} catch (ParseException e) {
 				e.printStackTrace();
 				return null;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		} else {
 	    	mErrno = -statusCode;
 	    	Log.e(TAG, Integer.toString(mErrno) + ",url:" + url);
 			return null;
 		}
 	}
 	
 	public static int getPrinterStatus(int contentType) {
 		int ret;
 		String printers[] = getPrinterList(contentType);
 
 		if (printers == null) {
 			Log.e(TAG, "getPrinterListFailed");
 			return ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT;
 		}
 		if (printers.length == 0) {
 			return 0;
 		}
 		
 		for(String printerIp:printers) {
 			ret = getPrinterStatus(printerIp);
 			if (ret < 0) {
 				return ret;
 			}
 		}
 		
 		return 0;
 	}
 	
 	private static String[] getPrinterList(int contentType) {
 		String response = Http.get(Server.PRINTER_LIST, "for=" + contentType);
 		if (response == null || "".equals(response)) {
 			return null;
 		} else {
 			int start, end;
 			start = response.indexOf("[");
 			end = response.indexOf("]");
 			if (start < 0 || end < 0) {
 				return null;
 			}
 			String[] str = response.substring(start+1, end).split(",");
 			Set<String> set = new TreeSet<String>();
 			for (int i = 0; i < str.length; i++) {
 				if (!"".equals(str[i])) {
 					set.add(str[i]);
 				}
 			}
 			str = (String[]) set.toArray(new String[0]);
 			return str;
 		}
 	}
 	
 	private static int getPrinterStatus(String printerIp) {
 		Socket socket = null;
 		byte buffer[] = new byte[2];
         try {
             socket = new Socket();
             socket.setSoTimeout(2000);
 			SocketAddress socketAddress = new InetSocketAddress(printerIp,9100);
 			socket.connect(socketAddress,2000);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             dos.writeBytes("\020\004\004");
             dis.read(buffer);
             socket.close();
             if (buffer[0] == 18) {
             	return 0;
             } else {
             	Log.e(TAG, "PRINTER_NO_PAPER:" + printerIp);
             	return ErrorNum.PRINTER_ERR_NO_PAPER;
             }
         } catch (UnknownHostException e) {
             e.printStackTrace();
             return ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT;
         } catch (IOException e) {
             e.printStackTrace();
             return ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT;
         }
 	}
 	
 	private static String httpRequestPost(String page, String msg) {
 		HttpParams httpParameters1 = new BasicHttpParams();
 
 		// 超时设置
 		HttpConnectionParams.setConnectionTimeout(httpParameters1, 15 * 1000);
 		HttpConnectionParams.setSoTimeout(httpParameters1, 60 * 1000);
 
 		List<NameValuePair> ls = constructHttpPkg(msg);
 
 		String url = Server.SERVER_DOMIN + "/" + page;
 
 		DefaultHttpClient client;
 		client = new DefaultHttpClient(httpParameters1);
 		
 		if ("".equals(url)) {
 			mErrno = ErrorNum.HTTP_NO_CONECTION;
 			return null;
 		}
 
 		HttpPost post = new HttpPost(url);
 		post.setHeader("User-Agent", "Mozilla/4.5");
 		post.setHeader("connection", "Keep-Alive");
 		post.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
 
 		try {
 			post.setEntity(new UrlEncodedFormEntity(ls, HTTP.UTF_8));
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 			mErrno = ErrorNum.UTF8_NOT_SUPPORTED;
 			return null;
 		}
 
 		HttpResponse httpResponse = null;
 		try {
 			httpResponse = client.execute(post);
 		} catch (IOException e) {
 			e.printStackTrace();
 			mErrno = ErrorNum.HTTP_NO_CONECTION;
 			return null;
 		}
 
 		int statusCode = httpResponse.getStatusLine().getStatusCode();
 		if (statusCode == 200) {
 			try {
 				return EntityUtils.toString(httpResponse.getEntity());
 			} catch (ParseException e) {
 				e.printStackTrace();
 				return null;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		} else {
 	    	mErrno = -statusCode;
 	    	Log.e(TAG, Integer.toString(mErrno) + ",url:" + url);
 			return null;
 		}
 	}
 		
 	private static String httpRequestGet(String page, String param) {
 		/*声明网址字符串*/
 		String uriAPI = Server.SERVER_DOMIN + "/" + page + "?" + param;
 		/*建立HTTP Get联机*/
 		HttpGet httpRequest = new HttpGet(uriAPI); 
 		
 		HttpParams httpParameters1 = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParameters1, 5 * 1000);
 		HttpConnectionParams.setSoTimeout(httpParameters1, 15 * 1000);
 		
 		DefaultHttpClient client;
 		client = new DefaultHttpClient(httpParameters1);
 		
 		try 
 		{
 			HttpResponse httpResponse = client.execute(httpRequest); 
 		
 			if(httpResponse.getStatusLine().getStatusCode() == 200) { 
 				return EntityUtils.toString(httpResponse.getEntity());
 			} else {
 				return null;
 			} 
 		} 
 		catch (ClientProtocolException e) {
 			e.printStackTrace();
 			return null;
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}  
 	}
 
 
 	private static List<NameValuePair> constructHttpPkg(String msg) {
 		Map<String, String> params = new HashMap<String, String>();
 		String param = "";
 		
 		try {
 			param = new String(msg.getBytes(), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		params.put("json", param);
 
 		List<NameValuePair> ls = new ArrayList<NameValuePair>();
 		Set<String> keys = params.keySet();
 		for (String key : keys) {
 			ls.add(new BasicNameValuePair(key, params.get(key).toString()));
 		}
 		return ls;
 	}
 }
