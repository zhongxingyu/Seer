 package util.webpage;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import util.BitOperate.BitOperateException;
 import util.webpage.Course.CourseException;
 import util.webpage.Course.TimeAndAddress.TimeAndAddressException;
 import util.webpage.Student.StudentException;
 
 public class SchoolWebpageParser {
 	private ParserListener listener;
 	private ReadPageHelper autoReadHelper;
 	private ReadPageHelper readHelper;
 
 	private static final Headings HEADINGS = new Headings();
 	
 	/**
 	 * 无参构造方法
 	 */
 	public SchoolWebpageParser() {
 		super();
 		listener = new ParserListenerAdapter();
 		autoReadHelper = new ReadPageHelper();
 		autoReadHelper.setCharset("GB2312");
 		autoReadHelper.setCharsetForParsePostsFromSCCE("UTF-8");
 		readHelper = null;
 	}
 	/**
 	 * 仅设置监听器
 	 * @param listener
 	 * @throws CloneNotSupportedException
 	 */
 	public SchoolWebpageParser(ParserListener listener) throws CloneNotSupportedException {
 		this();
 		setListener(listener);
 	}
 	/**
 	 * 仅设置监听器以及用户名、密码（设置到自动readPageHelper里）
 	 * @param listener
 	 * @param userName
 	 * @param password
 	 * @throws CloneNotSupportedException
 	 */
 	public SchoolWebpageParser(ParserListener listener, String userName, String password) throws CloneNotSupportedException {
 		this(listener);
 		setUser(userName, password);
 	}
 	/**
 	 * 设置监听器和读取网页帮助类
 	 * @param listener
 	 * @param readHelper
 	 * @throws CloneNotSupportedException
 	 */
 	public SchoolWebpageParser(ParserListener listener, ReadPageHelper readHelper) throws CloneNotSupportedException{
 		this(listener);
 		setReadHelper(readHelper);
 		autoReadHelper.setUser(readHelper.getUserName(), readHelper.getPassword());
 	}
 	
 	/**
 	 * @return the listener
 	 */
 	public ParserListener getListener() {
 		return listener;
 	}
 	/**
 	 * @param listener the listener to set
 	 * @throws CloneNotSupportedException 
 	 */
 	public void setListener(ParserListener listener) throws CloneNotSupportedException {
 		this.listener = listener.clone();
 	}
 	/**
 	 * 设置ReadPageHelper的监听器。总是设置内部自动生成的helper，若已自定义helper，也会同时设置
 	 * @param listener
 	 */
 	public void setOnReadPageListener(ReadPageHelper.OnReadPageListener listener){
 		autoReadHelper.setListener(listener);
 		if(readHelper != null)
 			readHelper.setListener(listener);
 	}
 	/**
 	 * 取得 用户最近通过
 	 * {@link SchoolWebpageParser#setListener(ParserListener) setListener(ParserListener)}
 	 * 设置的ReadPageHelper（用于登录，读取页面等）。对之的修改会应用到本对象。
 	 * @return 用户最近设置的ReadPageHelper，若您之前没有设置过则返回null
 	 */
 	public ReadPageHelper getReadHelper() {
 		return readHelper;
 	}
 	/**
 	 * 取得 本对象自动设置的ReadPageHelper（用于登录，读取页面等），这个helper只在您没有自行通过
 	 * {@link SchoolWebpageParser#setListener(ParserListener) setListener(ParserListener)}
 	 * 设置过ReadHelper时使用。对之的修改会应用到本对象自动生成的ReadHelper上。
 	 * @return 本对象自动设置的ReadPageHelper
 	 */
 	public ReadPageHelper getAutoReadHelper() {
 		return autoReadHelper;
 	}
 	/**
 	 * 返回当前使用的ReadPageHelper
 	 * @return 如果您自定义过ReadPageHelper，返回您上次定义的ReadPageHelper；否则返回本对象自动生成的ReadPageHelper
 	 */
 	public ReadPageHelper getCurrentHelper(){
 		return readHelper!=null?readHelper:autoReadHelper;
 	}
 	/**
 	 * 登录后，再返回当前使用的ReadPageHelper
 	 * @return 如果您自定义过ReadPageHelper，返回您上次定义的ReadPageHelper；否则返回本对象自动生成的ReadPageHelper
 	 * @throws ParserException 登录失败
 	 * @throws IOException doLogin进行post请求时遇到error
 	 */
 	public ReadPageHelper getCurrentHelperAfterLogin() throws ParserException, IOException{
 		ReadPageHelper readHelper = getCurrentHelper();
 		try{
 			if(!readHelper.doLogin()){
 				this.listener.onError(ParserListener.ERROR_CANNOT_LOGIN, "登录失败。");
 				throw new ParserException("Cannot login website.");
 			}
 		}catch(IOException e){
 			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法登录。 "+e.getMessage());
 			throw e;
 		}
 		return readHelper;
 	}
 	/**
 	 * 设置ReadPageHelper，它用于登录，读取页面等工作。
 	 * @param readHelper 用于读取网页的ReadPageHelper，您可以在readHelper中设置timeout、charset等
 	 * @throws CloneNotSupportedException 用{@link ReadPageHelper#clone()}复制readHelper时遇到此异常
 	 */
 	public void setReadHelper(ReadPageHelper readHelper) throws CloneNotSupportedException {
 		this.readHelper = readHelper.clone();
 	}
 	/**
 	 * 设置用户名和密码。
 	 * @param userName 用户名
 	 * @param password 密码
 	 * @throws NullPointerException
 	 */
 	public void setUser(String userName, String password){
 		if(readHelper != null)
 			readHelper.setUser(userName, password);
 		autoReadHelper.setUser(userName, password);
 	}
 	/**
 	 * 设置ReadPageHelper的超时时间，单位milliseconds
 	 * @param timeout 超时时间，单位milliseconds
 	 * @return 参数大于0返回真；参数小于等于0忽略本次调用，忽略此次调用，返回假
 	 */
 	public boolean setTimeout(int timeout) {
 		if(readHelper != null)
 			readHelper.setTimeout(timeout);
 		return autoReadHelper.setTimeout(timeout);
 	}
 	/**
 	 * 从给定来源，在指定的categories类别中，解析通知等文章
 	 * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
 	 * @param categories 指定类别范围，是String[]，内容类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES。空数组或首项为null时不分类别全解析
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @return 符合条件的posts；如果postSource不可识别，返回空ArrayList
 	 * @throws IOException 链接超时等IO异常
 	 * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
 	 */
     public ArrayList<Post> parsePosts(byte postSource, String[] categories, Date start, 
     		Date end, int max) throws UnsupportedEncodingException, IOException{
     	ArrayList<Post> result = new ArrayList<Post>();
     	switch(postSource){
     	case Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS:
     		if(categories==null || categories.length==0 || categories[0]==null)
     			categories = Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE;
     		for(String aCategory:categories){
     			if(max<0 || result.size()<max)
     				result.addAll(parsePostsFromTeachingAffairs(aCategory, start , end, max-result.size()));
     		}
     	break;
     	case Post.SOURCES.WEBSITE_OF_SCCE:
     		if(categories==null || categories.length==0 || categories[0]==null){
 	    		result.addAll(parsePostsFromSCCE(null, start, end, max, Post.SOURCES.NOTICES_IN_SCCE_URL));
 	    		if(max<0 || result.size()<max)
 	    			result.addAll(parsePostsFromSCCE(null, start, end, max-result.size(), Post.SOURCES.NEWS_IN_SCCE_URL));
     		}else{
     			ArrayList<String> categoriesInNotices = new ArrayList<String>();
     			ArrayList<String> categoriesInNews = new ArrayList<String>();
     			for(String aCategory:categories){
     				if(aCategory.matches(".*通知.*"))
     					categoriesInNotices.add(aCategory);
     				else if(aCategory.matches(".*新闻.*"))
     					categoriesInNews.add(aCategory);
     			}
     			if(!categoriesInNotices.isEmpty() && (max<0 || result.size()<max))
     				result.addAll(parsePostsFromSCCE(categoriesInNotices.toArray(new String[0]), 
     						start, end, max, Post.SOURCES.NOTICES_IN_SCCE_URL));
     			if(!categoriesInNews.isEmpty() && (max<0 || result.size()<max))
     				result.addAll(parsePostsFromSCCE(categoriesInNews.toArray(new String[0]), start,
     						end, max-result.size(), Post.SOURCES.NEWS_IN_SCCE_URL));
     		}
     	break;
     	case Post.SOURCES.STUDENT_WEBSITE_OF_SCCE:
     		if(categories==null || categories.length==0 || categories[0]==null)
     			categories = Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE;
     		for(String aCategory:categories){
     			if(max<0 || result.size()<max)
     				result.addAll(parsePostsFromSCCEStudent(aCategory, start, end, max-result.size()));
     		}
     	break;
     	case Post.SOURCES.UNKNOWN_SOURCE:
     		if(categories==null || categories.length==0 || categories[0]==null){
 				if(max>=0 && result.size()>=max)
     				break;
     			result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS,categories, start, end, max));
     			if(max>=0 && result.size()>=max)
     				break;
     			result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, categories , start, end, max-result.size()));
     			if(max>=0 && result.size()>=max)
     				break;
     			result.addAll(parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE, categories, start, end, max-result.size()));
     		}
     		else{
     			for(String tested:categories){
     				if(tested==null) continue;
     				if(max>=0 && result.size()>=max)
         				break;
     				for(String aCategory:Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE)
     					if(aCategory.equals(tested)){
     						result.addAll(parsePostsFromTeachingAffairs(tested, start, end, max));
     						break;
     					}
     				if(max>=0 && result.size()>=max)
         				break;
     				for(String aCategory:Post.CATEGORYS.IN_SCCE)
     					if(aCategory.equals(tested)){
     						result.addAll(parsePostsFromSCCE(new String[]{tested}, start, end, max-result.size(), (String)null));
     						break;
     					}
     				if(max>=0 && result.size()>=max)
         				break;
     				for(String aCategory:Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE)
     					if(aCategory.equals(tested)){
     						result.addAll(parsePostsFromSCCEStudent(tested, start, end, max-result.size()));
     						break;
     					}
     			}
     		}
     	break;
     	default:
     	}
     	return result;
     }
     /**
      * 在指定的categories类别中，解析通知等文章
      * @param categories 指定类别范围，是String[]，内容类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES。空数组或首项为null时不分类别全解析
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
      * @return 符合条件的posts
      * @throws IOException 链接超时等IO异常
      * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
      */
     public ArrayList<Post> parsePosts(String[] categories, Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
     	return parsePosts(Post.SOURCES.UNKNOWN_SOURCE, categories, start, end, max);
     }
     /**
      * 在指定的category类别中，解析通知等文章
      * @param aCategory 某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
      * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
      * @return 符合条件的posts
      * @throws IOException 链接超时等IO异常
      * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
      */
     public ArrayList<Post> parsePosts(String aCategory, Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
     	return parsePosts(Post.SOURCES.UNKNOWN_SOURCE, aCategory, start, end, max);
     }
     /**
      * 从所有可处理来源中，解析通知等文章
      * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
      * @return 符合条件的posts
      * @throws IOException 链接超时等IO异常
      * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
      */
     public ArrayList<Post> parsePosts(Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
     	return parsePosts(Post.SOURCES.UNKNOWN_SOURCE, start, end, max);
     }
     /**
      * 从所有可处理来源的常用类别中，解析通知等文章
      * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
      * @return 符合条件的posts
      * @throws IOException 链接超时等IO异常
      * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
      */
     public ArrayList<Post> parseCommonPosts(Date start, Date end, int max) throws UnsupportedEncodingException, IOException{
     	ArrayList<Post> result = new ArrayList<Post>();
     	if(max>=0 && result.size()>=max)
 			return result;;
 		result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS,Post.CATEGORYS.IN_TEACHING_AFFAIRS_WEBSITE_COMMON, start, end, max));
 		if(max>=0 && result.size()>=max)
 			return result;
 		result.addAll(parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, (String[])null , start, end, max-result.size()));
 		if(max>=0 && result.size()>=max)
 			return result;
 		result.addAll(parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE,Post.CATEGORYS.IN_STUDENT_WEBSITE_OF_SCCE_COMMON, start, end, max-result.size()));
 		return result;
     }
     /**
 	 * 从给定来源，解析通知等文章
 	 * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @return 符合条件的posts；如果postSource不可识别，返回null
      * @throws IOException 
      * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
 	 */
     public ArrayList<Post> parsePosts(byte postSource, Date start, 
     		Date end, int max) throws UnsupportedEncodingException, IOException{
     	String[] categories = null;
     	return parsePosts(postSource, categories, start, end, max);
     }
     /**
      * 从给定来源，根据指定的类别等条件，解析通知等文章
      * @param postSource 来源，类似Post.CATEGORYS.TEACHING_AFFAIRS_WEBSITE
      * @param aCategory 某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @return 符合条件的posts；如果postSource不可识别，返回null
      * @throws IOException 
      * @throws UnsupportedEncodingException 为解析教务处信息设置URL时，GB2312编码异常
      */
 	public ArrayList<Post> parsePosts(byte postSource, String aCategory, Date start, Date end, 
 			int max) throws UnsupportedEncodingException, IOException {
 		String[] categories = new String[]{aCategory};
 		return parsePosts(postSource, categories, start, end, max);
 	}
 	
 	/**
 	 * 根据指定的类别等条件，从教务处网站解析通知等文章
 	 * @param aCategory  某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @return 符合条件的posts
 	 * @throws UnsupportedEncodingException 设置URL时，GB2312编码异常
 	 * @throws IOException
 	 */
 	public ArrayList<Post> parsePostsFromTeachingAffairs(String aCategory, Date start, Date end, int max) throws UnsupportedEncodingException, IOException {
 		ReadPageHelper readHelper = getCurrentHelper();
 		if(aCategory == null)
 			return parsePosts(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS, start, end, max);
 		String url = null;
 		Document doc = null;
 		int page = 0;
 		ArrayList<Post> result = new ArrayList<Post>();
 		if(max == 0) return result;
 		try {
 			url = "http://59.67.148.66:8080/getRecords.jsp?url=list.jsp&pageSize=100&name=" 
 					+ URLEncoder.encode(aCategory, "GB2312") + "&currentPage=";
 		} catch (UnsupportedEncodingException e) {
 			listener.onError(ParserListener.ERROR_UNSUPPORTED_ENCODING, "遇到编码异常，解释教务处信息失败。 "+e.getMessage());
 			throw e;
 		}
 		try {
 			doc = readHelper.getWithDocument(url+"1");
 			result.addAll(parsePostsFromTeachingAffairs(aCategory, start, end, max, doc));
 			page = Integer.parseInt( doc.body().select("table table table table")
 					.get(1).select("tr td form font:eq(1)").text() );
 			
 			for(int i = 2;i<=page;i++){
 				if(max>=0 && result.size()>=max)
 					break;
 				try {
 					if(start!=null && Post.convertToDate(doc.body().select("table table table table")
 							.get(0).getElementsByTag("tr").last().getElementsByTag("a").first()
 							.nextSibling().outerHtml().trim().substring(1, 11)).before(start))
 						break;
 				} catch (ParseException e) {
 					listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析教务处通知/文章的日期失败。 "+e.getMessage());
 				}
 				doc = readHelper.getWithDocument(url+i);
 				result.addAll(parsePostsFromTeachingAffairs(aCategory, start, end, max-result.size(), doc));
 			}
 		} catch (IOException e) {
 			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析教务处信息失败。 "+e.getMessage());
 			throw e;
 		}
 		return result;
 	}
 	/**
 	 * 利用类别等信息，从指定的某教务处网页的Document文档对象，解析通知等文章
 	 * @param aCategory  某具体类别，类似Post.CATEGORYS.TEACHING_AFFAIRS_NOTICES等
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @param doc 要解析的网页的Document文档对象模型
 	 * @return 符合条件的posts
 	 */
 	public ArrayList<Post> parsePostsFromTeachingAffairs(String aCategory, Date start, Date end, 
 			int max, Document doc) {
 		ArrayList<Post> result = new ArrayList<Post>();
 		Elements posts = doc.body().select("table table table table").get(0).getElementsByTag("tr");
 		Element link = null;
 		Post aPost = null;
 		for(int i = 1;i<posts.size();i++){
 			if(max>=0 && result.size()>=max)
 				break;
 			link = posts.get(i).getElementsByTag("a").first();
 			aPost = new Post();
 			try {
 				//解析日期
 				aPost.setDate(link.nextSibling().outerHtml().trim().substring(1, 11));
 				if(end!=null && aPost.getDate().after(end))
 					continue;
 				if(start!=null && aPost.getDate().before(start))
 					break;
 			} catch (ParseException e) {
 				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析教务处通知/文章的日期失败。 "+e.getMessage());
 			}
 			aPost.setCategory(aCategory).setTitle(link.text()).setUrl(link.attr("abs:href"));
 			aPost.setSource(Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS);
 			result.add(aPost);
 		}
 		return result;
 	}
 	
 	/**
 	 * 根据aCategory、start、end、max、baseURL等条件，利用readHelper，从SCCE解析posts
 	 * @param aCategory 类别，例如“学生通知”
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @param baseURL 指定解析的基础URL，类似Post.SOURCES.NOTICES_IN_SCCE_URL
 	 * @return 符合条件的posts
 	 * @throws IOException 
 	 */
 	public ArrayList<Post> parsePostsFromSCCE(String[] categories, Date start, Date end, 
 			int max, String baseURL) throws IOException{
 		ReadPageHelper readHelper = getCurrentHelper();
 		Document doc = null;
 		int page = 0;
 		ArrayList<Post> result = new ArrayList<Post>();
 		if(max == 0) return result;
 		if(baseURL == null){
 			if(categories == null)
 				return parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, start, end, max);
 			boolean hasNew = false, hasNotice = false;
 			for(String aCategory:categories){
 				if(aCategory.matches(".*通知.*"))
 					hasNotice = true;
 				else if(aCategory.matches(".*新闻.*"))
 					hasNew = true;
 			}
 			if(hasNotice && !hasNew)
 				baseURL = Post.SOURCES.NOTICES_IN_SCCE_URL;
 			else if(!hasNotice && hasNew)
 				baseURL = Post.SOURCES.NEWS_IN_SCCE_URL;
 			else if(hasNotice && hasNew)
 				return parsePosts(Post.SOURCES.WEBSITE_OF_SCCE, categories, start, end, max);
 			else
 				return result;
 		}	
 		baseURL += "?page=";
 		try {
 			readHelper.prepareToParsePostsFromSCCE();
 			doc = readHelper.getWithDocumentForParsePostsFromSCCE(baseURL+"1");
 			Matcher matcher = Pattern.compile("\\?page=(\\d+)").matcher(doc.body()
 					.select("a[href*=more.aspx?page=]").last().attr("href"));
 			if(matcher.find())
 				page = Integer.parseInt(matcher.group(1));
 			else
 				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_PAGE, "不能从计算机学院网站解析页数，现仅解析第一页内容。");
 			result.addAll(parsePostsFromSCCE(categories, start, end ,max ,doc));
 
 			for(int i = 2;i<=page;i++){
 				if(max>=0 && result.size()>=max)
 					break;
 				try {
 					if(start!=null && Post.convertToDate(ReadPageHelper.deleteSpace(doc
 							.select("form table table").first().getElementsByTag("tr").last()
 							.getElementsByTag("td").get(3).text())).before(start))
 						break;
 				} catch (ParseException e) {
 					listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院网站通知/文章的日期失败。 "+e.getMessage());
 				}
 				doc = readHelper.getWithDocumentForParsePostsFromSCCE(baseURL+i);
 				result.addAll(parsePostsFromSCCE(categories, start, end, max-result.size(), doc));
 			}
 		} catch (IOException e) {
 			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析计算机学院网站信息失败。 "+e.getMessage());
 			throw e;
 		}
 		return result;
 	}
 	/**
 	 * 以aCategory、start、end、max为限制条件，从计算机学院页面doc中解析posts
 	 * @param aCategory 类别，例如“学生通知”
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @param doc 包含post列表的 某计算机学院网页的 Document
 	 * @return 符合条件的posts
 	 */
 	public ArrayList<Post> parsePostsFromSCCE(
 			String[] categories, Date start, Date end, int max, Document doc) {
 		Post post;
 		Elements cols = null;
 		Pattern pattern = Pattern.compile("openwin\\('(.*)'\\)");
 		Matcher matcher = null;
 		ArrayList<Post> result = new ArrayList<Post>();
 		Elements posts = doc.select("form table table").first().getElementsByTag("tr");
 		posts.remove(0);
 		for(Element postTr:posts){
 			if(max>=0 && result.size()>=max)
 				break;
 			cols = postTr.getElementsByTag("td");
 			//验证通知对象
 			String temp = ReadPageHelper.deleteSpace(cols.get(4).text());
 			if(temp.equals("教师") || temp.equals("全体教师"))
 				continue;
 			
 			post = new Post();
 			//验证Category
 			post.setCategory(ReadPageHelper.deleteSpace(cols.get(1).text()));
 			if(categories!=null){
 				boolean isContained = false;
 				for(String aCategory:categories)
 					if(aCategory.equals(post.getCategory()))
 						isContained = true;
 				if(!isContained)
 					continue;
 			}
 			//验证日期
 			try {
 				post.setDate(ReadPageHelper.deleteSpace(cols.get(3).text()));
 				if(end!=null && post.getDate().after(end))
 					continue;
 				if(start!=null && post.getDate().before(start))
 					break;
 			} catch (ParseException e) {
 				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院网站通知/文章的日期失败。 "+e.getMessage());
 			}
 			//设置 title、url、author、source
 			post.setTitle(cols.get(0).text().trim());
 			matcher = pattern.matcher(cols.get(0).getElementsByTag("a").attr("onclick"));
 			if(matcher.find())
 				post.setUrl("http://59.67.152.3/"+matcher.group(1));
 			else
 				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_URL, "解析计算机学院网站通知/文章的URL失败。 ");
 			post.setAuthor(ReadPageHelper.deleteSpace(cols.get(2).text()));
 			post.setSource(Post.SOURCES.WEBSITE_OF_SCCE);
 			result.add(post);
 		}
 		return result;
 	}
 	
 	/**
 	 * 根据aCategory、start、end、max等条件，利用readHelper，从SCCE学生网站解析posts
 	 * @param aCategory 类别，例如Post.CATEGORYS.SCCE_STUDENT_NOTICES
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @return 符合条件的posts；如果aCategory不可识别，返回空ArrayList
 	 * @throws IOException 
 	 */
 	public ArrayList<Post> parsePostsFromSCCEStudent(String aCategory, Date start, Date end, int max) throws IOException{
 		ReadPageHelper readHelper = getCurrentHelper();
 		if(aCategory == null)
 			return parsePosts(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE, start, end, max);
 		int page = 0;
 		Document doc = null;
 		String url = "http://59.67.152.6/Channels/";
 		ArrayList<Post> result = new ArrayList<Post>();
 		if(max == 0) return result;
 		if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_NEWS))
 			url += "7";
 		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_NOTICES))
 			url += "9";
 		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_UNION))
 			url += "45";
 		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_EMPLOYMENT))
 			url += "43";
 		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_YOUTH_LEAGUE))
 			url += "29";
 		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_DOWNLOADS))
 			url += "16";
 		else if(aCategory.equals(Post.CATEGORYS.SCCE_STUDENT_JOBS))
 			url += "55";
 		else
 			return result;
 		url += "?page=";
 		
 		try {
 			doc = readHelper.getWithDocumentForParsePostsFromSCCE(url+"1");
 			result.addAll(parsePostsFromSCCEStudent(aCategory, start, end, max, doc));
 			Matcher matcher = Pattern.compile("共(\\d+)页").matcher(doc.select(".oright .page").first().text());
 			if(matcher.find())
 				page = Integer.parseInt(matcher.group(1));
 			else
 				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_PAGE, "不能从计算机学院学生网站解析页数，现仅解析第一页内容。");
 			
 			for(int i = 2;i<=page;i++){
 				if(max>=0 && result.size()>=max)
 					break;
 				try {
 					if(start!=null && Post.convertToDate(doc.select(".oright .orbg ul li").last()
 							.getElementsByClass("date").first().text().trim().substring(1, 11)).before(start))
 						break;
 				} catch (ParseException e) {
 					listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院学生网站通知/文章的日期失败。 "+e.getMessage());
 				}
 				doc = readHelper.getWithDocumentForParsePostsFromSCCE(url+i);
 				result.addAll(parsePostsFromSCCEStudent(aCategory, start, end, max-result.size(), doc));
 			}
 		} catch (IOException e) {
 			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析计算机学院学生网站信息失败。 "+e.getMessage());
 			throw e;
 		}
 		
 		return result;
 	}
 	/**
 	 * 以start、end、max为限制条件，从计算机学院学生网站页面doc中解析posts
 	 * @param aCategory 类别，例如Post.CATEGORYS.SCCE_STUDENT_NOTICES
 	 * @param start 用于限制返回的Posts的范围，只返回start之后（包括start）的Post
 	 * @param end 用于限制返回的Posts的范围，只返回end之前（包括end）的Post
 	 * @param max 用于限制返回的Posts的数量，最多返回max条Post
 	 * @param doc 包含post列表的 某计算机学院学生网站网页的 Document
 	 * @return 符合条件的posts
 	 */
 	public ArrayList<Post> parsePostsFromSCCEStudent(
 			String aCategory, Date start, Date end, int max, Document doc){
 		Post post = null;
 		Element link = null;
 		ArrayList<Post> result = new ArrayList<Post>();
 		Elements posts = doc.select(".oright .orbg ul li");
 		for(Element postLi:posts){
 			if(max>=0 && result.size()>=max)
 				break;
 			post = new Post();
 			try {
 				post.setDate(postLi.getElementsByClass("date").first().text().substring(1,11));
 				if(end!=null && post.getDate().after(end))
 					continue;
 				if(start!=null && post.getDate().before(start))
 					break;
 			} catch (ParseException e) {
 				listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_DATE, "解析计算机学院学生网站通知/文章的日期失败。 "+e.getMessage());
 			}
 			link = postLi.getElementsByTag("a").first();
 			post.setTitle(link.text().trim());
 			post.setUrl(link.attr("abs:href"));
 			post.setSource(Post.SOURCES.STUDENT_WEBSITE_OF_SCCE).setCategory(aCategory);
 			result.add(post);
 		}
 		return result;
 	}
 	/**
 	 * 解析通知正文，解析结果为完整的HTML文档，保存在target中。根据target中来源、URL、标题解析。<br />
 	 * <strong>注：</strong>target中的来源和URL必须有效。
 	 * @param target 要解析的Post
 	 * @return 为方便使用，返回target
 	 * @throws ParserException Post的来源或者URL无效
 	 * @throws IOException 可能是网络异常
 	 */
 	public Post parsePostMainBody(Post target) throws ParserException, IOException{
 		if(target.getSource()==Post.SOURCES.UNKNOWN_SOURCE || target.getUrl()==null){
 			this.listener.onError(ParserListener.ERROR_INSUFFICIENT_INFORMATION, "Post的来源或者URL不明，无法解析正文。");
 			throw new ParserException("Post的来源或者URL不明，无法解析正文。\tPost:"+target.toString());
 		}
 		ReadPageHelper helper = getCurrentHelper();
 		Document doc = null;
 		String rawMainBody;
 		final String format = 
 				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
 				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
 				"<html xmlns='http://www.w3.org/1999/xhtml'>\n\t<head>\n" +
 				"\t\t<meta  http-equiv='Content-Type' content='text/html; charset=UTF-16' />\n" +
 				"\t\t<base href='%s' />\n" +
 				"\t\t<title>%s</title>\n" +
 				"\t</head>\n" +
 				"\t<body>\n" +
 				"\t\t%s\n" +
 				"\t</body>\n" +
 				"</html>";
 		try{
 			switch(target.getSource()){
 			case Post.SOURCES.WEBSITE_OF_TEACHING_AFFAIRS:
 				doc = helper.getWithDocument(target.getUrl());
 				rawMainBody = doc.select("table tr:eq(2) table td:eq(1) table table tr:eq(4) td").html();
 				break;
 			case Post.SOURCES.WEBSITE_OF_SCCE:
 				doc = helper.getWithDocumentForParsePostsFromSCCE(target.getUrl());
 				rawMainBody = doc.select("#Label2").html();
 				break;
 			case Post.SOURCES.STUDENT_WEBSITE_OF_SCCE:
 				doc = helper.getWithDocumentForParsePostsFromSCCE(target.getUrl());
 				rawMainBody = doc.select(".content div").html();
 				break;
 			default:
 				rawMainBody = "未知来源。若看到此信息说明程序异常，请联系开发组。";
 				break;
 			}
 		}catch(IOException e){
 			this.listener.onError(ParserListener.ERROR_IO, "遇到IO。请检查网络连接。详情："+e.getMessage());
 			throw e;
 		}
 		rawMainBody = String.format(format, doc.baseUri(), target.getTitle(), rawMainBody);
 		target.setMainBody(rawMainBody);
 		return target;
 	}
 	/**
 	 * 从URL指定的页面，解析课程信息，同时返回对应同学信息（如果studentInfoToReturn!=null）
 	 * @param url 要读取的页面地址
 	 * @param studentInfoToReturn 与课程信息相对应的同学的信息，被保存在这里，会覆盖原有数据
 	 * @return 满足条件的课程信息
 	 * @throws ParserException 不能正确读取课程表表头时
 	 * @throws IOException 网络连接出现异常
 	 */
 	public ArrayList<Course> parseCourse(String url, 
 			Student studentInfoToReturn) throws ParserException, IOException{
 		try{
 			Document doc = this.getCurrentHelperAfterLogin().getWithDocument(url);
 			//student
 			if(studentInfoToReturn != null){
 				Pattern pattern = Pattern.compile
 						("学号(?:：|:)(.*)姓名(?:：|:)(.*)学院(?:：|:)(.*)专业(?:：|:)(.*)班级(?:：|:)(.*\\d+班)");
 				Matcher matcher = pattern.matcher(doc.body().child(1).text());
 				if(matcher.find()){
 					studentInfoToReturn.setNumber( matcher.group(1).replaceAll("[\u3000\u00a0]", " ").trim() );
 					studentInfoToReturn.setName( matcher.group(2).replaceAll("[\u3000\u00a0]", " ").trim() );
 					studentInfoToReturn.setSchoolName( matcher.group(3).replaceAll("[\u3000\u00a0]", " ").trim());
 					studentInfoToReturn.setMajorName( matcher.group(4).replaceAll("[\u3000\u00a0]", " ").trim() );
 					studentInfoToReturn.setClassName( matcher.group(5).replaceAll("[\u3000\u00a0]", " ").trim() );	
 				}else
 					listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STUDENT_INFO, "解析课程信息时，无法匹配学生信息，解析学生信息失败。");
 			}
 			//courses
 			return readCourseTable(doc.getElementsByTag("table").get(0));
 		}catch(IOException e){
 			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析课程信息失败。 "+e.getMessage());
 			throw e;
 		}
 	}
 	/**
 	 * 从URL指定的页面，使用指定的网络连接方法（readPageHelper），解析课程信息
 	 * @param url 要读取的页面地址
 	 * @return 满足条件的课程信息
 	 * @throws ParserException 不能正确读取课程表表头时
 	 * @throws IOException 网络连接出现异常
 	 */
 	public ArrayList<Course> parseCourse(String url) throws ParserException, IOException{
 		return parseCourse(url, null);
 	}
 	/**
 	 * 从URL指定的页面，使用指定的网络连接方法（readPageHelper），解析成绩，同时返回对应同学信息（如果studentInfoToReturn!=null）
 	 * @param url 要读取的页面地址
 	 * @param studentInfoToReturn 与成绩信息相对应的同学的信息，被保存在这里，会覆盖原有数据
 	 * @return 满足条件的包含成绩信息的课程类
 	 * @throws ParserException 不能正确读取课程表表头时
 	 * @throws IOException 网络连接出现异常
 	 */
 	public ArrayList<Course> parseScores(String url, 
 			Student studentInfoToReturn) throws ParserException, IOException{
 		try{
 			Document doc = getCurrentHelperAfterLogin().getWithDocument(url);
 			//student
 			if(studentInfoToReturn != null){
 				if(url.equals(Constant.url.ALL_PERSONAL_GRADES)){
 					setStudentInformation(doc.getElementsByTag("table").first(), studentInfoToReturn);
 				}else{
 					Pattern pattern = Pattern.compile
 							("学号(?:：|:)(.*)姓名(?:：|:)(.*)");
 					Matcher matcher = pattern.matcher(doc.body().getElementsByTag("p").get(2).text());
 					if(matcher.find()){
 						studentInfoToReturn.setNumber( matcher.group(1).replaceAll("[\u3000\u00a0]", " ").trim() );
 						studentInfoToReturn.setName( matcher.group(2).replaceAll("[\u3000\u00a0]", " ").trim() );
 					}else
 						listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STUDENT_INFO, "解析成绩信息时，无法匹配学生信息，解析学生信息失败。");
 				}
 			}
 			//courses
 			if(url.equals(Constant.url.ALL_PERSONAL_GRADES))
 				return readCourseTable(doc.getElementsByTag("table").get(1));
 			else
 				return readCourseTable(doc.getElementsByTag("table").first());
 		}catch(IOException e){
 			listener.onError(ParserListener.ERROR_IO, "遇到IO异常，无法打开页面，解析成绩信息失败。 "+e.getMessage());
 			throw e;
 		}
 	}
 	/**
 	 * 从URL指定的页面，使用指定的网络连接方法（readPageHelper），解析成绩
 	 * @param url 要读取的页面地址
 	 * @return 满足条件的包含成绩信息的课程类
 	 * @throws ParserException 不能正确读取课程表表头时
 	 * @throws IOException 网络连接出现异常
 	 */
 	public ArrayList<Course> parseScores(String url) throws ParserException, IOException{
 		return parseScores(url, null);
 	}
 	private void setStudentInformation(Element studentInfoTable, Student studentInfoToReturn){
 		Elements rows = studentInfoTable.getElementsByTag("tr");
 		studentInfoToReturn.setNumber(rows.get(0).getElementsByTag("td").get(1).text().replaceAll("[\u3000\u00a0]", " ").trim());
 		studentInfoToReturn.setName(rows.get(0).getElementsByTag("td").get(3).text().replaceAll("[\u3000\u00a0]", " ").trim());
 		if(rows.get(0).getElementsByTag("td").get(5).text().replaceAll("[\u3000\u00a0]", " ").trim().equals("男"))
 			studentInfoToReturn.setIsMale(true);
 		else if(rows.get(0).getElementsByTag("td").get(5).text().replaceAll("[\u3000\u00a0]", " ").trim().equals("女"))
 			studentInfoToReturn.setIsMale(false);
 		try {
 			studentInfoToReturn.setBirthday(rows.get(1).getElementsByTag("td").get(1).text().replaceAll("[\u3000\u00a0]", " ").trim());
 		} catch (ParseException e) {
 			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STUDENT_BIRTHDAY, "无法解析日期，解析生日失败。"+e.getMessage());
 		}
 		try {
 			studentInfoToReturn.setAcademicPeriod(Integer.parseInt(rows.get(1).getElementsByTag("td").get(3).text().replaceAll("[\u3000\u00a0]", " ").trim()));
 		} catch (NumberFormatException e) {
 			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STUDENT_ACADEMIC_PERIOD, "无法解析数字，解析学制失败。"+e.getMessage());
 		} catch (StudentException e) {
 			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STUDENT_ACADEMIC_PERIOD, "解析的数字超出合理范围，解析学制失败。"+e.getMessage());
 		}
 		try {
 			studentInfoToReturn.setAdmissionTime(rows.get(1).getElementsByTag("td").get(5).text().replaceAll("[\u3000\u00a0]", " ").trim());
 		} catch (ParseException e) {
 			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STUDENT_ADMISSION_TIME, "无法解析日期，解析入学时间失败。"+e.getMessage());
 		}
 		studentInfoToReturn.setSchoolName(rows.get(2).getElementsByTag("td").get(1).text().replaceAll("[\u3000\u00a0]", " ").trim());
 		studentInfoToReturn.setMajorName(rows.get(2).getElementsByTag("td").get(3).text().replaceAll("[\u3000\u00a0]", " ").trim());
 		studentInfoToReturn.setClassName(rows.get(2).getElementsByTag("td").get(5).text().replaceAll("[\u3000\u00a0]", " ").trim());
 	}
 	private ArrayList<Course> readCourseTable(Element table) throws ParserException {
 	    ArrayList<Course> result = new ArrayList<Course>();
 	    Elements courses = table.getElementsByTag("tr");
 	    
 	    HashMap<Integer, Integer> headingMap = null;
 	    for(Element course:courses){
 	    	if(course.text().trim().length()==0)
 	    		continue;
 	    	if(course.getElementsByTag("td").first().text().trim().matches("\\d+"))
 				result.add(readCourse(course, headingMap));
 	    	else if(course.getElementsByTag("td").size()>1)
 	    	    headingMap = getHeading(course);
 	    	else
 	    		listener.onInformation(ParserListener.INFO_SKIP, "Skip: "+course.text());
 	    }
 	    result.trimToSize();
 		return result;
 	}
 	private Course readCourse(Element course, HashMap<Integer, Integer> headingMap){
 		if(headingMap == null){
 			listener.onError(ParserListener.NULL_POINTER, "headingMap是null，无法解析课程表格。");
 			throw new NullPointerException("headingMap is null.");
 		}
 		String rawTime = null, rawAddress = null;
 		Course result = new Course();
 		Elements cols = course.getElementsByTag("td");
 		int i;
 		Integer fieldCode;
 		for(i = 0;i<cols.size();i++){
 			fieldCode = headingMap.get(i);
 			if(fieldCode == null){
 				listener.onWarn(ParserListener.NULL_POINTER, "headingMap检查字段内容时，返回null。");
 				continue;
 			}
 			switch(fieldCode.intValue()){
 			case Headings.COURSE_CODE: case Headings.COURSE_NAME: case Headings.CLASS_NUMBER: 
 			case Headings.COURSE_TEACHER: case Headings.COURSE_KIND: case Headings.COURSE_SEMESTER:
 				parseAsString(result, fieldCode.intValue(), cols.get(i).text());
 				break;
 			case Headings.COURSE_CREDIT: case Headings.COURSE_TEST_SCORE: case Headings.COURSE_TOTAL_SCORE: 
 			case Headings.COURSE_ACADEMIC_YEAR:
 				parseAsNumber(result, fieldCode.intValue(), cols.get(i).text());
 				break;
 			case Headings.COURSE_TIME:rawTime= cols.get(i).getElementsByTag("font").get(0).html();break;
 			case Headings.COURSE_ADDRESS:rawAddress=cols.get(i).getElementsByTag("font").get(0).html();break;
 			case Headings.SEQUENCE_NUMBER: case Headings.COURSE_TEACHING_MATERIAL: case Headings.COURSE_GRADE_POINT:
 				listener.onInformation(ParserListener.INFO_SKIP, "忽略"+HEADINGS.getString(fieldCode)+"："+cols.get(i).text().trim());
 				break;
 			case Headings.UNKNOWN_COL:
 			default:listener.onWarn(ParserListener.WARNING_UNKNOWN_COLUMN, "未知列: "+cols.get(i).text());break;
 			}
 		}
 		if(rawTime!=null || rawAddress!=null){
 			try{
 				readTimeAndAddress(result, rawTime, rawAddress);
 			}catch(Exception e){
 				listener.onWarn(ParserListener.WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS, "解析时间地点失败。因为：" + e.getMessage());
 			}
 		}
 		return result;
 	}
 	private String pretreatmentString(int colContent, String rawData){
 		String temp = null;
 		if(colContent == Headings.COURSE_NAME || colContent == Headings.COURSE_TEACHER)
 			temp = ReadPageHelper.trim(rawData);
 		else
 			temp = ReadPageHelper.deleteSpace(rawData);
 		if(temp.length() == 0){
 			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_STRING_DATA, "数据"+HEADINGS.getString(colContent)+"为空，解析失败。");
 			return null;
 		}
 		else
 			return temp;
 	}
 	private void parseAsString(Course result, int colContent, String rawData){
 		String temp = pretreatmentString(colContent, rawData);
 		if(temp == null)
 			return;
 		switch(colContent){
 		case Headings.COURSE_CODE:result.setCode(temp);break;
 		case Headings.COURSE_NAME:result.setName(temp);break;
 		case Headings.CLASS_NUMBER:result.setClassNumber(temp);break;
 		case Headings.COURSE_TEACHER:result.addTeacher(temp);break;
 		case Headings.COURSE_KIND:result.setKind(temp);break;
 		case Headings.COURSE_SEMESTER:
 			if(temp.equals("1"))
 				result.setIsFirstSemester(true);
 			else if(temp.equals("2"))
 				result.setIsFirstSemester(false);
 			else{
 				result.setIsFirstSemester(null);
 				listener.onWarn(ParserListener.WARNINT_CANNOT_PARSE_SEMESTER, "未知的学期数据"+temp+"，解析学期失败。");
 			}
 			break;
 		default:listener.onWarn(ParserListener.WARNING_UNKNOWN_COLUMN, 
 				"未知的字符串数据项"+colContent+"("+HEADINGS.getString(colContent)+")。");
 		}
 	}
 	private void parseAsNumber(Course result, int colContent, String rawData){
 		String temp = pretreatmentString(colContent, rawData);
 		if(temp == null)
 			return;
 		try{
 			int data = Integer.parseInt(temp);
 			switch(colContent){
 			case Headings.COURSE_CREDIT:result.setCredit(data);break;
 			case Headings.COURSE_TEST_SCORE:result.setTestScore(data);break;
 			case Headings.COURSE_TOTAL_SCORE:result.setTotalScore(data);break;
 			case Headings.COURSE_ACADEMIC_YEAR:result.setYear(data);break;
 			default:listener.onWarn(ParserListener.WARNING_UNKNOWN_COLUMN, 
 					"未知的数字数据项"+colContent+"("+HEADINGS.getString(colContent)+")。");
 			}
 		}catch(NumberFormatException e){
 			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_NUMBER_DATA, 
 					"不能把字符串数据转换为数字，解析数字数据项"+HEADINGS.getString(colContent)+"失败。详情："+e.getMessage() );
 		}
 		catch (CourseException e) {
 			listener.onWarn(ParserListener.WARNING_CANNOT_PARSE_NUMBER_DATA, 
 					"数据超过合理范围，解析数字数据项"+HEADINGS.getString(colContent)+"失败.详情："+e.getMessage() );
 		}
 	}
 	private static void readTimeAndAddress(Course result, String rawTime, String rawAddress) 
 			throws ParserException, TimeAndAddressException, BitOperateException {
 		if(rawTime==null)
 			throw new ParserException("Error: rawTime == null");
 		if(rawAddress==null)
 			throw new ParserException("Error: rawAddress == null");
 		String[] times, addresses ,timesSplited;
 		times = splitTimeOrAddress(rawTime);
 		addresses = splitTimeOrAddress(rawAddress);
 		if(times.length != addresses.length)
 			throw new ParserException("Error: times.length != addresses.length");
 		for(int index = 0;index<times.length;index++){
 			timesSplited = splitTime(times[index]);
 			result.addTimeAndAddress(timesSplited[0], timesSplited[1], 
 					timesSplited[2], addresses[index]);
 		}
 	}
 	private static String[] splitTime(String time) throws ParserException{
 		int counter = 0;
 		String[] result = new String[3];
 		Pattern pattern = Pattern.compile(
 				"(\\d[\\d\\s\u00a0\u3000;；,，\\-－\u2013\u2014\u2015]*\\d)|" +
 				"((星期|周)[一二三四五六日]([\\s\u00a0\u3000;；,，星期周日一二三四五六至到]*[一二三四五六日])?)");
 		Matcher matcher = pattern.matcher(time);
 		while(matcher.find())
 			if(counter<3){
 				result[counter] = matcher.group();
 				counter++;
 			}
 			else
 				throw new ParserException("Unexpected time String: "+time);
 		if(counter != 3)
 			throw new ParserException("Unexpected time String: "+time);
 		return result;
 	}
 	private static String[] splitTimeOrAddress(String timeOrAddress){
 		String[] first;
 		ArrayList<String> second = new ArrayList<String>();
 		first = timeOrAddress.split("<[^><]*>");
 		for(String s:first){
 			s.trim();
 			if(s.length()>0)
 				second.add(s);
 		}
 		return (String[])second.toArray(new String[0]);
 	}
 	private HashMap<Integer, Integer> getHeading(Element heading) throws ParserException {
 		String colName = null;
 		HashMap<Integer, Integer> headMap = new HashMap<Integer, Integer>(8);
 		Elements cols = null;
 		cols = heading.getElementsByTag("td");
 		if(cols == null || cols.isEmpty()){
 			cols = heading.getElementsByTag("th");
 			if(cols == null || cols.isEmpty()){
 				listener.onError(ParserListener.ERROR_CANNOT_PARSE_TABLE_HEADING, "heading中没有<td>或<th>标签,无法解析表头。");
 				throw new ParserException("Can't getHeading because no td|th tag in first line.");
 			}
 		}
 		for(int i=0;i<cols.size();i++){
 			colName = ReadPageHelper.deleteSpace(cols.get(i).text());
 			//assert colName != null;
 			headMap.put(i, HEADINGS.getCode(colName));
 		}
 		return headMap;
 	}
 	public static class ParserException extends Exception{
 		private static final long serialVersionUID = 3737828070910029299L;
 		public ParserException(String message){
 			super(message + " @SchoolWebpageParser");
 		}
 		public ParserException(){
 			super("encounter Exception when parse school page.");
 		}
 		public ParserException(String message, Throwable cause) {
 			super(message + " @SchoolWebpageParser", cause);
 		}
 		public ParserException(Throwable cause){
 			super("encounter Exception when parse school page.", cause);
 		}
 	}
 	
 	private static final class Headings{
 		private static final int UNKNOWN_COL = -1;
 	    private static final int SEQUENCE_NUMBER = 0;
 	    private static final int COURSE_CODE = 1;
 	    private static final int COURSE_NAME = 2;
 	    private static final int CLASS_NUMBER = 3;
 	    private static final int COURSE_TEACHER = 4;
 	    private static final int COURSE_CREDIT = 5;
 	    private static final int COURSE_TIME = 6;
 	    private static final int COURSE_ADDRESS = 7;
 	    private static final int COURSE_TEACHING_MATERIAL = 8;
 	    //SCORE
 	    private static final int COURSE_TEST_SCORE = 10;
 	    private static final int COURSE_TOTAL_SCORE = 11;
 	    private static final int COURSE_ACADEMIC_YEAR = 12;
 	    private static final int COURSE_SEMESTER = 13;
 	    private static final int COURSE_KIND = 14;
 	    private static final int COURSE_GRADE_POINT = 15;
 		
 	    private static final HashMap<String, Integer> HEADING_STRING2INTEGER = new HashMap<String, Integer>();
 	    public Headings(){
 	    	HEADING_STRING2INTEGER.put("序号",	SEQUENCE_NUMBER);
 	    	HEADING_STRING2INTEGER.put("课程代码",	COURSE_CODE);
 	    	HEADING_STRING2INTEGER.put("课程编码",	COURSE_CODE);
 	    	HEADING_STRING2INTEGER.put("课程名称",	COURSE_NAME);
 	    	HEADING_STRING2INTEGER.put("教学班号",	CLASS_NUMBER);
 	    	HEADING_STRING2INTEGER.put("教师",	COURSE_TEACHER);
 	    	HEADING_STRING2INTEGER.put("学分",	COURSE_CREDIT);
 	    	HEADING_STRING2INTEGER.put("时间",	COURSE_TIME);
 	    	HEADING_STRING2INTEGER.put("地点",	COURSE_ADDRESS);
 	    	HEADING_STRING2INTEGER.put("参考教材",	COURSE_TEACHING_MATERIAL);
 	    	//Scores
 	    	HEADING_STRING2INTEGER.put("结课考核成绩",	COURSE_TEST_SCORE);
 	    	HEADING_STRING2INTEGER.put("期末总评成绩",	COURSE_TOTAL_SCORE);
 	    	HEADING_STRING2INTEGER.put("成绩",		COURSE_TOTAL_SCORE);
 	    	HEADING_STRING2INTEGER.put("学年",		COURSE_ACADEMIC_YEAR);
 	    	HEADING_STRING2INTEGER.put("学期",		COURSE_SEMESTER);
 	    	HEADING_STRING2INTEGER.put("课程性质",		COURSE_KIND);
 	    	HEADING_STRING2INTEGER.put("绩点",		COURSE_GRADE_POINT);
 	    	HEADING_STRING2INTEGER.put("<UNKNOWN_COL>", UNKNOWN_COL);
 	    }
 	    public int getCode(String heading){
 	    	Integer temp = HEADING_STRING2INTEGER.get(heading);
 	    	return temp!=null ? temp:UNKNOWN_COL;
 	    }
 	    public String getString(int headingCode){
 	    	Iterator<Map.Entry<String, Integer>> iter = HEADING_STRING2INTEGER.entrySet().iterator();
 	    	while (iter.hasNext()) {
 	    	    Map.Entry<String, Integer> entry = iter.next();
 	    	    if (entry.getValue().intValue() == headingCode) {
 	    	        return entry.getKey();
 	    	    }
 	    	}
 	    	return "Unkown Code";
 	    }
 	}
 	
 	/**
 	 * 解析监听器，用于返回状体信息，解析进度等。
 	 * @author Bai Jie
 	 */
 	public static interface ParserListener extends Cloneable{
 		public static final int NULL_POINTER = 1;
 		public static final int ERROR_CANNOT_LOGIN = 2;
 		public static final int ERROR_IO = 3;
 		public static final int ERROR_UNSUPPORTED_ENCODING = 4;
 		public static final int ERROR_CANNOT_PARSE_TABLE_HEADING = 5;
 		public static final int ERROR_INSUFFICIENT_INFORMATION = 6;
 		public static final int WARNING_CANNOT_PARSE_PAGE = 10;
 		public static final int WARNING_UNKNOWN_COLUMN = 11;
 		public static final int WARNING_CANNOT_PARSE_DATE = 12;
 		public static final int WARNING_CANNOT_PARSE_URL = 13;
 		public static final int WARNING_CANNOT_PARSE_STRING_DATA = 14;
 		public static final int WARNING_CANNOT_PARSE_NUMBER_DATA = 15;
 		public static final int WARNINT_CANNOT_PARSE_TIME_AND_ADDRESS = 16;
 		public static final int WARNINT_CANNOT_PARSE_SEMESTER = 17;
 		public static final int WARNING_CANNOT_PARSE_STUDENT_INFO = 20;
 		public static final int WARNING_CANNOT_PARSE_STUDENT_BIRTHDAY = 21;
 		public static final int WARNING_CANNOT_PARSE_STUDENT_ADMISSION_TIME = 22;
 		public static final int WARNING_CANNOT_PARSE_STUDENT_ACADEMIC_PERIOD = 23;
 		public static final int INFO_SKIP = 30;
 		/**
 		 * 当遇到错误时，调用此方法。错误意味着解释失败，停止解析。很可能此调用后解析方法抛出异常
 		 * @param code 错误代码
 		 * @param message 错误信息
 		 */
 		public void onError(int code, String message);
 		/**
 		 * 当遇到警告信息时，调用此方法。警告意味着可能有错误的解析结果，需要用户检查正误，解析方法本身会正常返回结果（可能包含错误信息）。
 		 * @param code 警告代码
 		 * @param message 警告信息
 		 */
 		public void onWarn(int code, String message);
 		/**
 		 * 当返回消息信息时，调用此方法。这并不代表程序不正常，只是用于返回一些关于当前状态的消息。
 		 * @param code 消息代码
 		 * @param message 消息信息
 		 */
 		public void onInformation(int code, String message);
 		/**
 		 * 当解析进度改变时。用于报告解析进度。用current/total表示，0<=current<=total，current==total时解析完成。
 		 * @param current 当前进度。
 		 * @param total 总进度。
 		 */
 		public void onProgressChange(float current, float total);
 		/**
 		 * 实现深克隆
 		 * @see java.lang.Object#clone()
 		 */
 		public ParserListener clone() throws CloneNotSupportedException;
 	}
 	/**
 	 * 解析监听器适配器，若无说明，默认方法是空方法。您可以继承此类，只重写需要定义的方法。<br />
 	 * <strong>注：</strong>本适配器的onError调用System.err.println返回错误信息，方便调试。您可以重写此方法（如用空方法替代）<br />
 	 * <strong>注：</strong>本适配器的clone()方法仅调用默认的super.clone()方法，如果您有非安全对象字段，这并不适合您，请重写clone
 	 * @author Bai Jie
 	 */
 	public static class ParserListenerAdapter implements ParserListener{	
 		public ParserListenerAdapter() {
 			super();
 		}
 		@Override
 		public void onError(int code, String message) {
 			System.err.println("Error "+code+": "+message);
 		}
 		@Override
 		public void onWarn(int code, String message) {			
 		}
 		@Override
 		public void onInformation(int code, String message) {
 		}
 		@Override
 		public void onProgressChange(float current, float total) {			
 		}
 		@Override
 		public ParserListener clone() throws CloneNotSupportedException{
 			return (ParserListener) super.clone();
 		}
 	}
 }
