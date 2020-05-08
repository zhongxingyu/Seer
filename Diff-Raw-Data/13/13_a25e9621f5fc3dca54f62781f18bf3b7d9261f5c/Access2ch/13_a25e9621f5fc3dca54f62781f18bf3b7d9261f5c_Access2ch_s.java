 package net.hisme.masaki;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.zip.GZIPInputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class Access2ch {
 	public static ArrayList<String[]> threads(String host, String board) {
 		ArrayList<String[]> res = new ArrayList<String[]>();
 		try {
 			URL uri = new URL("http://" + host + "/" + board + "/subject.txt");
 			HttpURLConnection http = (HttpURLConnection) uri.openConnection();
 			http.setRequestMethod("GET");
 			http.addRequestProperty("Accept-Encoding", "gzip");
 			http.addRequestProperty("User-Agent", "Monazilla/1.00");
 			http.addRequestProperty("Connection", "close");
 			http.connect();
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					new GZIPInputStream(http.getInputStream()), "SHIFT_JIS"));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
				String[] thread = line.split(".dat<>");
				res.add(thread);
 			}
 			Collections.sort(res, new Comparator<String[]>() {
 				public int compare(String[] a, String[] b) {
 					double c = Double.parseDouble(b[0]) - Double.parseDouble(a[0]);
 					if (c > 0)
 						return 1;
 					if (c == 0)
 						return 0;
 					return -1;
 				}
 			});
 
 			reader.close();
 			http.disconnect();
 		} catch (java.net.MalformedURLException e) {
 			// return e.toString();
 		} catch (java.io.IOException e) {
 			// return e.toString();
 		} catch (java.lang.IllegalStateException e) {
 			// return e.toString();
 		} catch (NullPointerException e) {
 			// return e.toString();
 		}
 		return res;
 	}
 }
