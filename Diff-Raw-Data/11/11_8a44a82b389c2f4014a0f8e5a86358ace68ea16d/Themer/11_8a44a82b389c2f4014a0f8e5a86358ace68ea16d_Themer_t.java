 package com.art;
 
 import java.util.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.*;
 
 public class Themer {
 	
 	private static Connection getConnection() throws URISyntaxException, SQLException {
 	    URI dbUri = new URI(System.getenv("DATABASE_URL"));
 
 	    String username = dbUri.getUserInfo().split(":")[0];
 	    String password = dbUri.getUserInfo().split(":")[1];
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
 
 	public static HashMap theme(String text) {
 		Connection conn = null;
 		try {
 			//Class.forName("org.postgresql.Driver");
 			conn = getConnection();
 
 			HashMap result = new HashMap();
 
 			HashMap hm = filter(text);
 			Set set = hm.entrySet();
 			Iterator i = set.iterator();
 			while(i.hasNext()) {
 				Map.Entry me = (Map.Entry)i.next();
 				String k = (String) me.getKey();
 				int v = Integer.valueOf(me.getValue().toString());
 
 				PreparedStatement st = conn.prepareStatement("SELECT t_id, p FROM dict WHERE word = ?");
 				st.setString(1,k);
 				ResultSet rs = st.executeQuery();
 				while (rs.next()) {
 					PreparedStatement st2 = conn.prepareStatement("SELECT theme FROM themes WHERE t_id = ?");
 					st2.setInt(1,Integer.valueOf(rs.getString(1)));
 					ResultSet rs2 = st2.executeQuery();
 					while (rs2.next()) {
 						result.put(rs2.getString(1),Double.valueOf(rs.getString(2))*v);
 					}
 					rs2.close();
 					st2.close();
 				}
 				rs.close();
 				st.close();
 			}
 
 			Set rset = sortTopThemes(result).entrySet();
 			Iterator ri = rset.iterator();
 			int cutter = 0;
 			while(ri.hasNext()) {
 				cutter++;
 				Map.Entry me = (Map.Entry)ri.next();
 				if (cutter > 10) result.remove(me.getKey());
 			}
 
 			double max = 0;
 			Collection col = result.values();
 			for (Object val : col) {
 				Double num = Double.valueOf(val.toString());
 				if (num > max) max = num;
 			}
 			double min = max;
 			for (Object val : col) {
 				Double num = Double.valueOf(val.toString());
 				if (num < min) min = num;
 			}
 			double quarter = 0.25;
 			Set pset = result.entrySet();
 			Iterator pi = pset.iterator();
 			while(pi.hasNext()) {
 				Map.Entry me = (Map.Entry)pi.next();
 				double p = Double.valueOf(me.getValue().toString()) / (max*quarter + min + max);
 				result.put(me.getKey(),p);
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
 
 	public static HashMap filter(String text) {
 		String[] words = text.replaceAll("[^а-яА-Я\\s]", "").split("\\s+");
 
 		HashMap hm = new HashMap();
 
 		for (String kword : words) {
 			kword = Stemmer.stem(kword.toLowerCase());
 			if (!hm.containsKey(kword))	hm.put(kword,Integer.valueOf(1));
 			else hm.put(kword, Integer.valueOf(hm.get(kword).toString()) + 1);
 		}
 
 		// Stoplist of meaningless words
 		// I wanted to read this from file, but it's only 150 words, so I decided to make this HUGE string
 		String stop = "и в во не что он на я с со как а то все она так его но да ты к у же вы за бы по " +
 				"только ее мне было вот от меня еще нет о из ему теперь когда даже ну вдруг ли если уже " +
 				"или ни быть был него до вас нибудь опять уж вам сказал ведь там потом себя ничего ей " +
 				"может они тут где есть надо ней для мы тебя их чем была сам чтоб без будто человек чего " +
 				"раз тоже себе под жизнь будет ж тогда кто этот говорил того потому этого какой совсем " +
 				"ним здесь этом один почти мой тем чтобы нее кажется сейчас были куда зачем сказать " +
 				"всех никогда сегодня можно при наконец два об другой хоть после над больше тот через " +
 				"эти нас про всего них какая много разве сказала три эту моя впрочем хорошо свою этой " +
 				"перед иногда лучше чуть том нельзя такой им более всегда конечно всю между";
 
 		String[] sl = stop.split(" ");
 
 		for (String slword : sl) {
 			slword = Stemmer.stem(slword);
 			hm.remove(slword);
 		}
 
 		Set set = sortTopWords(hm).entrySet();
 		Iterator i = set.iterator();
 		int cutter = 0;
 		while(i.hasNext()) {
 			cutter++;
 			Map.Entry me = (Map.Entry)i.next();
 			if (cutter > 20) hm.remove(me.getKey());
 		}
 
 		return hm;
 	}
 
 	public static HashMap getThemes(String text) {
 		text = "Быть энтузиасткой сделалось ее общественным положением, и иногда, когда\n" +
 				"  ей даже того не хотелось, она, чтобы не обмануть ожиданий людей, знавших ее,\n" +
 				"  делалась энтузиасткой.  Сдержанная  улыбка,  игравшая постоянно на лице Анны\n" +
 				"  Павловны, хотя и не шла к  ее  отжившим чертам, выражала, как у избалованных\n" +
 				"  детей, постоянное  сознание своего  милого недостатка,  от  которого она  не\n" +
 				"  хочет, не может и не находит нужным исправляться.\n" +
 				"       В   середине  разговора   про  политические   действия  Анна   Павловна\n" +
 				"  разгорячилась.\n" +
 				"       -- Ах, не говорите мне про Австрию! Я ничего не понимаю, может быть, но\n" +
 				"  Австрия никогда не хотела  и  не  хочет войны. Она предает нас. Россия  одна\n" +
 				"  должна быть  спасительницей  Европы.  Наш  благодетель  знает  свое  высокое\n" +
 				"  призвание  и будет  верен  ему.  Вот одно, во  что я  верю. Нашему доброму и\n" +
 				"  чудному государю предстоит величайшая  роль  в мире, и он так добродетелен и\n" +
 				"  хорош, что Бог не оставит его, и он  исполнит  свое призвание задавить гидру\n" +
 				"  революции, которая теперь еще ужаснее в  лице этого убийцы и злодея. Мы одни\n" +
 				"  должны  искупить  кровь  праведника...  На   кого  нам   надеяться,   я  вас\n" +
 				"  спрашиваю?... Англия с  своим коммерческим духом не поймет и не может понять\n" +
 				"  всю высоту  души императора  Александра. Она отказалась очистить Мальту. Она\n" +
 				"  хочет   видеть,   ищет  заднюю  мысль   наших  действий.  Что  они   сказали\n" +
 				"  Новосильцову?... Ничего. Они не поняли,  они  не могут понять самоотвержения\n" +
 				"  нашего  императора, который ничего не хочет для себя  и  все хочет для блага\n" +
 				"  мира. И что они обещали? Ничего. И что обещали, и  того не будет! Пруссия уж\n" +
 				"  объявила, что Бонапарте непобедим и  что  вся  Европа ничего не может против\n" +
 				"  него... И я  не  верю ни  в одном  слове ни  Гарденбергу, ни Гаугвицу. Cette\n" +
 				"  fameuse neutralité prussienne, ce n'est qu'un  piège.  [9] Я верю в\n" +
 				"  одного  Бога  и  в  высокую  судьбу  нашего  милого  императора.  Он  спасет\n" +
 				"  Европу!...   --  Она  вдруг  остановилась  с  улыбкою   насмешки  над  своею\n" +
 				"  горячностью.";
 
 		HashMap themes = theme(text);
 		Set set = sortTopThemes(themes).entrySet();
 		Iterator i = set.iterator();
 		while(i.hasNext()) {
 			Map.Entry me = (Map.Entry)i.next();
 			System.out.print(me.getKey().toString().trim() + ": ");
 			System.out.println(me.getValue());
 		}
 		return themes;
 	}
 }
