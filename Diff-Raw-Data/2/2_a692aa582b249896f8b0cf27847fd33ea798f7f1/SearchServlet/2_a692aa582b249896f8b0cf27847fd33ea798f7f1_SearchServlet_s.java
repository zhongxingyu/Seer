 package com.itranswarp.shici;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.gson.stream.JsonWriter;
 
 public class SearchServlet extends HttpServlet {
 
 	static int MAX_RESULTS = 20;
 	static int INDEX_BATCH = 50;
 	static final long TIMER_PERIOD = 60000L * 10; // 10 min
 
 	static final Set<String> DYNASTY_SET = new HashSet<String>(Arrays.asList("100", "200", "300", "400", "500", "600", "700", "800", "900", "1000", "100000"));
 	static final Set<String> FORM_SET = new HashSet<String>(Arrays.asList("58", "54", "78", "74", "9", "8", "15"));
 	static final Set<String> CATEGORY_SET = new HashSet<String>(Arrays.asList("1000", "2000", "5000"));
 
 	static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
 	static final String MYSQL_JDBC_URL = "jdbc:mysql://localhost:3306/shici";
 	static final String MYSQL_JDBC_USER = "www-data";
 	static final String MYSQL_JDBC_PASSWD = "www-data";
 
 	Log log = LogFactory.getLog(getClass());
 	PoemSearcher searcher = null;
 	Timer timer = null;
 
 	class ScanTask extends TimerTask {
 		static final String LAST_INDEX_POINT = "/srv/search.shi-ci.com/lucene_search_from";
 
 		public ScanTask() {
 			try {
 				Class.forName(MYSQL_JDBC_DRIVER);
 			}
 			catch (ClassNotFoundException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		public void run() {
 			log.info("BEGIN timer for ScanTask...");
 			Connection conn = null;
 			try {
 				conn = DriverManager.getConnection(MYSQL_JDBC_URL, MYSQL_JDBC_USER, MYSQL_JDBC_PASSWD);
 				conn.setAutoCommit(false);
 				runInTransaction(conn);
 				conn.commit();
 			}
 			catch (SQLException e) {
 				log.warn("SQL exception", e);
 				if (conn!=null)
 					try {
 						conn.rollback();
 					} catch (SQLException e1) {}
 			}
 			finally {
 				if (conn!=null)
 					try {
 						conn.close();
 					} catch (SQLException e) {}
 			}
 			log.info("END timer for ScanTask.");
 		}
 
 		void runInTransaction(Connection conn) throws SQLException {
 			long lastSearchFrom = read();
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery("select * from poem where version>" + lastSearchFrom + " order by version limit " + INDEX_BATCH);
 			List<Poem> poems = new LinkedList<Poem>();
 			while (rs.next()) {
 				poems.add(readPoem(rs));
 			}
 			if (poems.isEmpty()) {
 				log.info("No modified poems found since " + lastSearchFrom + ", " + toDate(lastSearchFrom));
 				return;
 			}
 			log.info(poems.size() + " poems found since last modified: " + lastSearchFrom + ", " + toDate(lastSearchFrom));
 			index(poems);
 			long nextFrom = Long.parseLong(poems.get(poems.size()-1).version);
 			log.info("Next search time: " + nextFrom + ", " + toDate(nextFrom));
 			write(nextFrom);
 		}
 
 		void index(List<Poem> poems) {
 			log.info("Index poem...");
 			for (Poem p : poems) {
 				searcher.unindex(p.id);
 				searcher.index(p);
 			}
 			log.info("Index done.");
 		}
 
 		String toDate(long date) {
 			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
 		}
 
 		Poem readPoem(ResultSet rs) throws SQLException {
 			Poem p = new Poem();
 			p.content = rs.getString("content");
 			p.content_cht = rs.getString("content_cht");
 			p.content_pinyin = rs.getString("content_pinyin");
 			p.dynasty_id = rs.getString("dynasty_id");
 			p.dynasty_name = rs.getString("dynasty_name");
 			p.dynasty_name_cht = rs.getString("dynasty_name_cht");
 			p.form = rs.getString("form");
 			p.id = rs.getString("id");
 			p.name = rs.getString("name");
 			p.name_cht = rs.getString("name_cht");
 			p.name_pinyin = rs.getString("name_pinyin");
 			p.poet_id = rs.getString("poet_id");
 			p.poet_name = rs.getString("poet_name");
 			p.poet_name_cht = rs.getString("poet_name_cht");
 			p.poet_name_pinyin = rs.getString("poet_name_pinyin");
 			p.version = String.valueOf(rs.getLong("version"));
 			return p;
 		}
 
 		void write(long search_from) {
 			File f = new File(LAST_INDEX_POINT);
 			BufferedWriter writer = null;
 			try {
 				writer = new BufferedWriter(new FileWriter(f));
 				writer.write(String.valueOf(search_from));
 				log.info("write last search from = " + search_from);
 			}
 			catch (Exception e) {
 				log.warn("write last search from failed.", e);
 			}
 			finally {
 				Utils.close(writer);
 			}
 		}
 
 		long read() {
 			long search_from = 0L;
 			File f = new File(LAST_INDEX_POINT);
 			if (f.isFile()) {
 				BufferedReader reader = null;
 				try {
 					reader = new BufferedReader(new FileReader(f));
 					String line = reader.readLine();
 					search_from = Long.parseLong(line);
 				}
 				catch (Exception e) {
 					log.warn("read last search from failed.", e);
 				}
 				finally {
 					Utils.close(reader);
 				}
 			}
 			log.info("read last search from = " + search_from);
 			return search_from;
 		}
 	}
 
 	public void destroy() {
 		searcher.shutdown();
 		timer.cancel();
 	}
 
 	public void init() throws ServletException {
 		log.info("Init SearchServlet...");
 		searcher = new PoemSearcher("/srv/search.shi-ci.com/search_db/");
 		timer = new Timer();
 		timer.schedule(new ScanTask(), TIMER_PERIOD, TIMER_PERIOD);
 	}
 
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		long current = Long.parseLong(getRequiredParam(req, "time"));
 		if (Math.abs(System.currentTimeMillis() - current) > 30000L)
 			throw new ServletException("Bad time");
 		String action = getRequiredParam(req, "action");
 		String sig = getRequiredParam(req, "sig");
 		if ( ! sig.equals(Utils.md5(current + ":" + action + ":" + Config.KEY)))
 			throw new ServletException("Bad sig");
 		if ("index".equals(action)) {
 			Poem p = new Poem();
 			p.id = getRequiredParam(req, "id");
 
 			p.poet_id = getRequiredParam(req, "poet_id");
 			p.poet_name = getRequiredParam(req, "poet_name");
 			p.poet_name_pinyin = getRequiredParam(req, "poet_name_pinyin");
 
 			p.dynasty_id = getRequiredParam(req, "dynasty_id");
 			p.dynasty_name = getRequiredParam(req, "dynasty_name");
 			p.dynasty_name_cht = getRequiredParam(req, "dynasty_name_cht");
 
 			p.form = getRequiredParam(req, "form");
 
 			p.name = getRequiredParam(req, "name");
 			p.name_cht = getRequiredParam(req, "name_cht");
 			p.name_pinyin = getRequiredParam(req, "name_pinyin");
 
 			p.content = getRequiredParam(req, "content");
 			p.content_cht = getRequiredParam(req, "content_cht");
 			p.content_pinyin = getRequiredParam(req, "content_pinyin");
 			p.version = getRequiredParam(req, "version");
 
 			searcher.unindex(p.id);
 			searcher.index(p);
 		}
 		else if ("unindex".equals(action)) {
 			String id = getRequiredParam(req, "id");
 			searcher.unindex(id);
 		}
 		else {
 			throw new ServletException("Bad action");
 		}
 	}
 
 	boolean isEmpty(String s) {
		return s!=null && !"".equals(s);
 	}
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		try {
     		String q = getRequiredParam(req, "q");
     		log.info("Search for " + q);
     		if (q.length()>20)
     			throw new SearchException("QUERY_STRING_TOO_LONG", "Keywords too long.");
     		String next = req.getParameter("next");
     		String dynasty_id = req.getParameter("dynasty_id");
     		String poet_id = req.getParameter("poet_id");
     		String form = req.getParameter("form");
     		String category = req.getParameter("category");
     		Map<String, String> filter = new HashMap<String, String>();
     		if (isEmpty(dynasty_id)) {
     			if ( ! isEmpty(poet_id)) {
     				filter.put("poet_id", poet_id);
     			}
     		}
     		else {
         		if (DYNASTY_SET.contains(dynasty_id)) {
         			filter.put("dynasty_id", dynasty_id);
     	    	}
     		}
     		if (FORM_SET.contains(form))
     			filter.put("form", form);
     		if (CATEGORY_SET.contains(category))
     			filter.put("category", category);
     		String[] qs = q.split(" ");
     		int len = qs.length;
     		if (len > 5)
     			len = 5;
     		SearchResult sr = searcher.search(qs, len, filter, next, MAX_RESULTS);
     		resp.setContentType("application/json");
 			PrintWriter pw = resp.getWriter();
     		JsonWriter jwriter = new JsonWriter(pw);
     		jwriter.beginObject();
     		jwriter.name("total").value(sr.total);
     		jwriter.name("next").value(sr.next);
     		jwriter.name("poems").beginArray();
     		if (sr.poems!=null) {
         		for (Poem p : sr.poems) {
         			jwriter.beginObject();
         			jwriter.name("id").value(p.id);
 
         			jwriter.name("poet_id").value(p.poet_id);
         			jwriter.name("poet_name").value(p.poet_name);
         			jwriter.name("poet_name_cht").value(p.poet_name_cht);
         			jwriter.name("poet_name_pinyin").value(p.poet_name_pinyin);
 
         			jwriter.name("dynasty_id").value(p.dynasty_id);
         			jwriter.name("dynasty_name").value(p.dynasty_name);
         			jwriter.name("dynasty_name_cht").value(p.dynasty_name_cht);
 
         			jwriter.name("form").value(p.form);
 
         			jwriter.name("name").value(p.name);
         			jwriter.name("name_cht").value(p.name_cht);
         			jwriter.name("name_pinyin").value(p.name_pinyin);
 
         			jwriter.name("content").value(p.content);
         			jwriter.name("content_cht").value(p.content_cht);
         			jwriter.name("content_pinyin").value(p.content_pinyin);
         			jwriter.name("version").value(p.version);
         			jwriter.endObject();
         		}
     		}
     		jwriter.endArray();
     		jwriter.endObject();
 			jwriter.close();
 			pw.flush();
 		}
 		catch (SearchException e) {
 			log.warn("Search Exception.", e);
 			PrintWriter pw = resp.getWriter();
 			pw.print(e.toJsonString());
 			pw.flush();
 		}
 		catch (Exception e) {
 			log.warn("Servlet Error.", e);
 			PrintWriter pw = resp.getWriter();
 			pw.print("{\"error\":\"");
 			pw.print(e.getClass().getName());
 			pw.print("\"}");
 			pw.flush();
 		}
 	}
 
 	String getRequiredParam(HttpServletRequest req, String name) {
 		String s = req.getParameter(name);
 		if (s==null)
 			throw new IllegalArgumentException("Missing parameter.");
 		s = s.trim();
 		if (s.isEmpty())
 			throw new IllegalArgumentException("Missing parameter.");
 		return s;
 	}
 }
