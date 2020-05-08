 package br.uff.utils;
 
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.json.JSONObject;
 
 public class HTTPUtils {
 
 	public static String acessar(String endereco) {
 
 		try {
 			URL url = new URL(endereco);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			InputStream is = conn.getInputStream();
 			Scanner in = new Scanner(is);
 			String content = in.useDelimiter("\\A").next();
 			in.close();
 			return content;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static String postar(String endereco, Map<String, String> dados) {
 		try {
 			JSONObject jsdata = new JSONObject();
 			for (Map.Entry<String, String> entry : dados.entrySet()) {
 				jsdata.put(entry.getKey(), entry.getValue());
 			}
 			URL url = new URL(endereco);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("POST");
 			conn.setRequestProperty("Charset", "utf-8");
 			conn.setRequestProperty("Content-Type",
 					"application/json;charset=utf-8");
 			conn.setRequestProperty("Connection", "Keep-Alive");
 			conn.setConnectTimeout(5000);
 			conn.setDoOutput(true);
 			conn.setDoInput(true);
 			OutputStreamWriter wr = new OutputStreamWriter(
 					conn.getOutputStream());
 			wr.write(jsdata.toString());
 			wr.flush();
 			wr.close();
 
 			InputStream is = conn.getInputStream();
 			Scanner in = new Scanner(is);
 			String content = in.useDelimiter("\\A").next();
 			in.close();
 			return content;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static boolean atualizar(String endereco, Map<String, String> dados) {
 		try {
 			JSONObject jsdata = new JSONObject();
 			for (Map.Entry<String, String> entry : dados.entrySet()) {
 				jsdata.put(entry.getKey(), entry.getValue());
 			}
 			URL url = new URL(endereco);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("PUT");
 			conn.setDoOutput(true);
 			conn.setDoInput(true);
 			conn.setRequestProperty("Charset", "utf-8");
 			conn.setRequestProperty("Content-Type",
 					"application/json;charset=utf-8");
 			conn.setRequestProperty("Connection", "Keep-Alive");
 			conn.setConnectTimeout(5000);
 			OutputStreamWriter wr = new OutputStreamWriter(
 					conn.getOutputStream());
 			wr.write(jsdata.toString());
 			wr.flush();
 			wr.close();
			return (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) ? true : false;
 		} catch (Exception e) {
 			return false;
 		}
 
 	}
 
 	public static boolean deletar(String endereco) {
 		try {
 			URL url = new URL(endereco);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("DELETE");
			return (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) ? true : false;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 }
