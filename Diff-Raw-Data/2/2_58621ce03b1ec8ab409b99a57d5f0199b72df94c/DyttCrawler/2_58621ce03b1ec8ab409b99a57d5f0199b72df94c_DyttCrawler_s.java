 package crawl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import model.Movie_Info;
 
 import util.BasicUtil;
 import util.LogUtil;
 import witer.DBWriter;
 import witer.ImageWriter;
 
 
 public class DyttCrawler extends BaseCrawler{
 	
 	private static final String ROOT_URL = "http://www.dytt8.net/";
 		
 	public static void main(String[] args) {
 		DyttCrawler dc = new DyttCrawler();
 		dc.begin();
 	}
 	
 	public DyttCrawler(){
 		movie_src = "Dytt";
 		CRAWLABLE_URLS.add("http://www.dytt8.net/html/gndy/china/list_4_%d.html");
 		CRAWLABLE_URLS.add("http://www.dytt8.net/html/gndy/rihan/list_6_%d.html");
 		CRAWLABLE_URLS.add("http://www.dytt8.net/html/gndy/oumei/list_7_%d.html");
 	}
 		
 	protected void begin(){
 		super.begin();
 	}
 	
 	
 	/**
 	 * 获取指定网页内容
 	 * @param surl ：网址
 	 * @return 网页源代码
 	 */
 	private String getContent(String surl){
 		StringBuffer sb = new StringBuffer();
 		try {
 			URL url = new URL(surl);
 			URLConnection urlconnection = url.openConnection();
 			urlconnection.addRequestProperty("User-Agent", AGENT);
 			urlconnection.setConnectTimeout(TIME_OUT);
 			InputStream is = url.openStream();
 			BufferedReader bReader = new BufferedReader(new InputStreamReader(is, "GBK"));
 			String rLine = null;
 			while((rLine=bReader.readLine())!=null){
 				sb.append(rLine);
 				sb.append("/r/n");
 			}
 		}catch (IOException e) {
 		}
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * 获取当前最大页
 	 * @return MAX_PAGE
 	 */
 	protected boolean getMaxPage(){
 		
 		for(int i = 0; i < CRAWLABLE_URLS.size() ; i ++){
 			String url = CRAWLABLE_URLS.get(i);
 			String content = getContent(String.format(url, 1));
 			String regex = "共\\d+页/\\d+条记录";
 			Pattern pt = Pattern.compile(regex);
 			Matcher mt = pt.matcher(content);
 			if(mt.find()){
 				String str = mt.group();
 				str = str.substring(1, str.indexOf("/") - 1);
 				try {
 					int last_page = Integer.parseInt(str);
 					CRAWLABLE_MAX_PAGE.add(i, last_page);
 					System.out.println("last page found: " + last_page);
 				} catch (NumberFormatException e) {
 					LogUtil.getInstance().write(this.getClass().getName() + "	[error] getting max page at URL : " + url);
 					e.printStackTrace();
 				}
 			}
 		}
 		return true;
 	}
 	
 	
 	private final static String MOVIE_URL_PATTERN = "<a href=\"/html/gndy/[a-zA-Z]+/[0-9]{1,}/[0-9]{1,}.html\"";
 	private final static String HAIBAO_PATTERN = "<img[^<]*src=\"http.*?(jpg|gif|JPG|GIF)\"";
 	private final static String PIANMING_PATTERN = "(◎译　　名|◎片　　名|◎中 文 名|◎英 文 名|◎译 　　名|◎片 　　名|◎中  文 名|◎英  文 名|◎影片原名|◎中文译名)[^<]*<br />";
 	private final static String XIAZAIMING_PATTERN = "(rmvb|avi|mp4|mkv|RMVB|AVI|PM4|MKV)\">[^<]*(rmvb|avi|mp4|mkv|RMVB|AVI|PM4|MKV)";
 	private final static String[] MOVIE_PATTERNS = {HAIBAO_PATTERN, PIANMING_PATTERN, XIAZAIMING_PATTERN};
 
 	/**
 	 * 获取电影信息
 	 * @param id : 当前线程ID
 	 * @param sUrl : 网页网址
 	 * @return 当前页获取电影数量
 	 */
 	protected int crawlMovies(int id, String sUrl){
 		String s = getContent(sUrl);
 		int movie_counter = 0;
 		ArrayList<Movie_Info> movie_list = new ArrayList<Movie_Info>();
 		Pattern pt = Pattern.compile(MOVIE_URL_PATTERN);
 		Matcher mt = pt.matcher(s);
 		while(mt.find()){
 			String str = mt.group();
 			str =ROOT_URL + str.substring(10, str.length() - 1);
 			Movie_Info movie_info = new Movie_Info();
 			if(DBWriter.getInstance().ifCrawled(str)){
 				continue;
 			}
 			String content = getContent(str);
 			for(int i = 0; i < MOVIE_PATTERNS.length; i ++){
 				parsePattern(movie_info, content, i);
 			}
 			movie_counter ++;
 			if(movie_info.getMovieName() != null){
 				ImageWriter.getInstance().addMovieList(movie_info.clone());
 			}
 			movie_list.add(movie_info);
 		}
 		DBWriter.getInstance().addMovieList(movie_list);
 		return movie_counter;
 	}
 
 	/**
 	 * 按照正则表达式解析电影信息
 	 * @param movie_info : 电影类
 	 * @param s : 网页源代码
 	 * @param n : 当前正则表达式. 0 : HAIBAO_PATTERN; 1 : PIANMING_PATTERN; 2 : XIAZAIMING_PATTERN.
 	 */
 	private void parsePattern(Movie_Info movie_info, String s, int n){
 		Pattern pt = Pattern.compile(MOVIE_PATTERNS[n]);
 		Matcher mt = pt.matcher(s);
 		while (mt.find()) {
 			//先去除一些特殊的符号
 			String str = mt.group();
 			str = BasicUtil.formatString(str);
 			//用正则表达式进行匹配,通过字符串处理找到相应内容
 			switch(n){
 			case 0:
 				str = str.substring(str.indexOf("src=\"") + 5, str.length() - 1);
 				if(!movie_info.hasHaiBaoPath()){
 					str = str.trim();
 					movie_info.setHaiBaoPath(str);
 				}
 				break;
 			case 1:	
 				//蛋疼的电影名匹配
 				if(str.startsWith("◎译　　名")){
 					str = str.substring(6, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎片　　名")){
 					str = str.substring(6, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎中 文 名")){
 					str = str.substring(7, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎英 文 名")){
 					str = str.substring(7, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎译 　　名")){
 					str = str.substring(7, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎片 　　名")){
 					str = str.substring(7, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎中  文 名")){
 					str = str.substring(8, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎英  文 名")){
 					str = str.substring(8, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎影片原名")){
 					str = str.substring(6, str.lastIndexOf("<"));
 				}else if(str.startsWith("◎中文译名")){
 					str = str.substring(6, str.lastIndexOf("<"));
 				}
 				//去除电影名前的“空格”（trim对此种空格无效）
 				while(str.startsWith("　")){
 					str = str.substring(1, str.length());
 				}
 				StringTokenizer st = new StringTokenizer(str, "/");
 				if(st.countTokens() == 0){
 					movie_info.setMovieName(str.trim());
 				}
 				while(st.hasMoreElements()){
 					String tmp = st.nextToken().trim();
 					if(!movie_info.hasName()){
 						movie_info.setMovieName(tmp);
 					}else{
 						movie_info.addName(tmp);
 					}
 				}
 				break;
 			case 2:
 				str = str.substring(str.indexOf("\">") + 2).trim();
 				movie_info.addDownLoadLinks(str, str.substring(str.lastIndexOf("/") + 1));
 				break;
 			default:break;
 			}
 		}
 		//部分电影仍旧无法识别电影名，通过title从中提取。
		if(!movie_info.hasName()){
 			String title = s.substring(s.indexOf("<title>"), s.indexOf("</title>"));
 			String name = title.substring(title.indexOf("《") + 1, title.indexOf("》"));
 			movie_info.setMovieName(name);
 		}
 	}
 	
 }
