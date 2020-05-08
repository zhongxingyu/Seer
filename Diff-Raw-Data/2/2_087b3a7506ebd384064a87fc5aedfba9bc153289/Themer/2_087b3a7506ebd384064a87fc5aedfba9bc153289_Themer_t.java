 package com.art.themer;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class Themer {
 	
 	private static Connection getConnection() throws URISyntaxException, SQLException {
 	    URI dbUri = new URI(System.getenv("DATABASE_URL"));
 
 	    String[] loginCredentials = dbUri.getUserInfo().split(":");
 	    String username = loginCredentials[0];
 	    String password = loginCredentials[1];
 	    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + ":" + dbUri.getPort();
 
 	    return DriverManager.getConnection(dbUrl, username, password);
 	}
 
 	public static HashMap<String, Integer> sortTopWords(HashMap<String, Integer> map) {
 		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());
 
 		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
 			public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
 				return (m2.getValue()).compareTo(m1.getValue());
 			}
 		});
 
 		HashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
 		for (Map.Entry<String, Integer> entry : list) {
 			result.put(entry.getKey(), entry.getValue());
 		}
 		return result;
 	}
 
 	public static HashMap<String, Double> sortTopThemes(HashMap<String, Double> map) {
 		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());
 
 		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
 			public int compare(Map.Entry<String, Double> m1, Map.Entry<String, Double> m2) {
 				return (m2.getValue()).compareTo(m1.getValue());
 			}
 		});
 
 		HashMap<String, Double> result = new LinkedHashMap<String, Double>();
 		for (Map.Entry<String, Double> entry : list) {
 			result.put(entry.getKey(), entry.getValue());
 		}
 		return result;
 	}
 
 	public static HashMap<String, Double> theme(String text) {
 		Connection conn = null;
 		try {
 			conn = getConnection();
 
 			HashMap<String, Double> result = new HashMap<String, Double>();
 
 			HashMap<String, Integer> hm = filter(text);
 			Set<?> set = hm.entrySet();
 			Iterator<?> i = set.iterator();
 			while(i.hasNext()) {
 				Map.Entry me = (Map.Entry)i.next();
 				String k = (String) me.getKey();
 				int v = Integer.valueOf(me.getValue().toString());
 
 				PreparedStatement st = conn.prepareStatement("SELECT t_id, p FROM dict WHERE word = ?");
 				st.setString(1, k);
 				ResultSet rs = st.executeQuery();
 				while (rs.next()) {
 					PreparedStatement innerSt = conn.prepareStatement("SELECT theme FROM themes WHERE t_id = ?");
 					innerSt.setInt(1, Integer.valueOf(rs.getString(1)));
 					ResultSet innerRs = innerSt.executeQuery();
 					while (innerRs.next()) {
 						result.put(innerRs.getString(1), Double.valueOf(rs.getString(2)) * v);
 					}
 					innerRs.close();
 					innerSt.close();
 				}
 				rs.close();
 				st.close();
 			}
 
 			Set<?> rset = sortTopThemes(result).entrySet();
 			Iterator<?> ri = rset.iterator();
 
 			int cutter = 0;
 			while(ri.hasNext()) {
 				cutter++;
 				Map.Entry me = (Map.Entry) ri.next();
 				if (cutter > 10) result.remove(me.getKey());
 			}
 
 			double max = 0;
 			Collection<Double> col = result.values();
 			for (Object val : col) {
 				Double num = Double.valueOf(val.toString());
 				if (num > max) max = num;
 			}
 			double min = max;
 			for (Object val : col) {
 				Double num = Double.valueOf(val.toString());
 				if (num < min) min = num;
 			}
 			Set<?> pset = result.entrySet();
 			Iterator<?> pi = pset.iterator();
 			while(pi.hasNext()) {
 				Map.Entry me = (Map.Entry) pi.next();
 				double p = Double.valueOf(me.getValue().toString()) / (max/10 + min + max);
 				result.put((String) me.getKey(), p);
 			}
 
 			return result;
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.exit(1);
 		} catch (URISyntaxException e) {			
 			e.printStackTrace();
 			System.exit(2);
 		}
 		return null;
 	}
 
 	public static HashMap<String, Integer> filter(String text) {
 		String[] words = text.replaceAll("[^а-яА-Я\\s]", "").split("\\s+");
 
 		HashMap<String, Integer> hm = new HashMap<String, Integer>();
 
 		for (String kword : words) {
 			kword = Stemmer.stem(kword.toLowerCase());
 			if (!hm.containsKey(kword))	hm.put(kword, Integer.valueOf(1));
 			else hm.put(kword, Integer.valueOf(hm.get(kword).toString()) + 1);
 		}
 
 		Connection conn = null;
 		try {
 			conn = getConnection();
 
 			List<String> stoplist = new ArrayList<String>();
 			
 			PreparedStatement st = conn.prepareStatement("SELECT word FROM stopwords");
 			ResultSet rs = st.executeQuery();
 			while (rs.next()) {
				stoplist.add(rs.getString(1));
 			}
 			rs.close();
 			st.close();
 
 			for (String slword : stoplist) {
 				slword = Stemmer.stem(slword);
 				hm.remove(slword);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.exit(1);
 		} catch (URISyntaxException e) {			
 			e.printStackTrace();
 			System.exit(2);
 		}
 
 		Set<?> set = sortTopWords(hm).entrySet();
 		Iterator<?> i = set.iterator();
 		int cutter = 0;
 		while(i.hasNext()) {
 			cutter++;
 			Map.Entry me = (Map.Entry) i.next();
 			if (cutter > 20) hm.remove(me.getKey());
 		}
 
 		return hm;
 	}
 
 	public static Set<?> getThemes(String text) {
 		HashMap<String, Double> themes = theme(text);
 		Set<?> set = sortTopThemes(themes).entrySet();		
 		return set;
 	}
 }
