 /* Copyright (C) 2010-2011 Christoph Giesel
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hisqisnoten;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.CookiePolicy;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 
 /**
  * 
  * @author Christoph Giesel
  *
  */
 public class HisqisGrabber {
 
 	protected String user;
 	protected String password;
 
 	protected String keystoreFile = "/resources/keystore";
 	protected char[] keystorePassword = "password".toCharArray();
 
 	static final String PREDECESSORLOGINPAGEURL = "https://vhisqis.uni-magdeburg.de/qisserver/rds?state=user&type=1";
 	static final String LOGINPAGEURL = "https://idp.uni-magdeburg.de/idp/Authn/UserPassword";
 	static final String LOGINPAGEDATAGETTERURL = "https://vhisqis.uni-magdeburg.de/Shibboleth.sso/SAML2/POST";
 	static final String STARTPAGEURL = "https://vhisqis.uni-magdeburg.de/qisserver/rds?state=user&type=1";
 	static final String STUDYCOURSESURL = "https://vhisqis.uni-magdeburg.de/qisserver/rds?state=notenspiegelStudent&next=tree.vm&nextdir=qispos/notenspiegel/student&menuid=notenspiegelStudent&breadcrumb=notenspiegel&breadCrumbSource=menu&asi=";
 
 	static final Pattern relaystatePattern = Pattern.compile("<input type=\"hidden\" name=\"RelayState\" value=\"(.+)\"/>");
 	static final Pattern samlresponsePattern = Pattern.compile("<input type=\"hidden\" name=\"SAMLResponse\" value=\"(.+)\"/>");
 	static final Pattern asiPattern = Pattern.compile("asi=(.+?)\"");
 	static final Pattern studyCoursePattern = Pattern.compile("<a href=\"(.+?)\" title=\"Leistungen f");
 	static final Pattern tablePattern = Pattern.compile("<table border=\"0\">(.+?)<\\/table>", Pattern.MULTILINE | Pattern.DOTALL);
 	static final Pattern marksPattern = Pattern.compile("<tr>(.+?)<\\/tr>", Pattern.MULTILINE | Pattern.DOTALL);
 	static final Pattern tdPattern = Pattern.compile("<td (.+?)>(.+?)<\\/td>", Pattern.MULTILINE | Pattern.DOTALL);
 	static final Pattern htmlCommentPattern = Pattern.compile("<!--(.+?)-->", Pattern.MULTILINE | Pattern.DOTALL);
 	static final Pattern commaZeroPattern = Pattern.compile(",0");
 	
 	protected String relaystate = "";
 	protected String samlresponse = "";
 	protected String asi = "";
 	protected String studyCourseURL = "";
 	
 	protected String averageGrade = "";
 	protected String totalCreditPoints = "";
 	
 	protected ArrayList<HQNContainer> marks;
 
 	public HisqisGrabber() {
 		this(null, null);
 	}
 
 	public HisqisGrabber(String user, String password) {
 		this.user = user;
 		this.password = password;
 
 		prepareCookies();
 
 		prepareSSL();
 		
 		init();
 	}
 	
 	public void init() {
 		relaystate = "";
 		samlresponse = "";
 		asi = "";
 		studyCourseURL = "";
 		averageGrade = "";
 		totalCreditPoints = "";
 		marks = null;
 	}
 
 	/**
 	 * Cookiemanager - keine Ahnung ob noetig
 	 */
 	protected void prepareCookies() {
 		CookieManager manager = new CookieManager();
 		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
 		CookieHandler.setDefault(manager);
 	}
 
 	/**
 	 * SSL Zeug - hoffentlich hilft es
 	 * 
	 * @return false on error
 	 */
 	protected boolean prepareSSL() {
 		try {
 			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
 
 			InputStream is = Class.class.getResourceAsStream(keystoreFile);
 
 			if (is == null) {
 				return false;
 			}
 
 			ks.load(is, keystorePassword);
 
 			KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
 			km.init(ks, keystorePassword);
 
 			SSLContext sc = SSLContext.getInstance("SSLv3");
 			sc.init(km.getKeyManagers(), null, null);
 
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
 	public HisqisGrabberResults process() {
 		try {
 			init();
 			
 			URL url1 = doStep1();
 			URL url2 = doStep2(url1);
 			URL url3 = doStep3(url2);
 			URL url4 = doStep4(url3);
 			URL url5 = doStep5(url4);
 			doStep6(url5);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return new HisqisGrabberResults(averageGrade, totalCreditPoints, marks);
 	}
 
 	/**
 	 * forwards to login page
 	 * 
 	 * @return URL to first page
 	 * @throws IOException
 	 *             invalid URL or I/O Error
 	 */
 	protected URL doStep1() throws IOException {
 		URL url = new URL(PREDECESSORLOGINPAGEURL);
 		url.openConnection().getInputStream();
 
 		return url;
 	}
 
 	/**
 	 * shows login page
 	 * 
 	 * @param referer
 	 *            URL to predecessor page
	 * @return URL to second page
 	 * @throws IOException
 	 *             invalid URL or I/O Error
 	 */
 	protected URL doStep2(URL referer) throws IOException {
 		URL url = new URL(LOGINPAGEURL);
 		URLConnection inputConnection = url.openConnection();
 
 		inputConnection.setRequestProperty("Referer", referer.toExternalForm());
 
 		inputConnection.setDoOutput(true);
 		OutputStream outputStream = inputConnection.getOutputStream();
 		outputStream.write(("j_username=" + URLEncoder.encode(user, "UTF-8")
 				+ "&j_password=" + URLEncoder.encode(password, "UTF-8"))
 				.getBytes());
 		outputStream.flush();
 
 		InputStream inputResponseStream = inputConnection.getInputStream();
 
 		String responseContent = new Scanner(inputResponseStream).useDelimiter("\\Z").next();
 
 		Matcher relaystateMatcher = relaystatePattern.matcher(responseContent);
 		relaystateMatcher.find();
 		relaystate = relaystateMatcher.group(1);
 
 		Matcher samlresponseMatcher = samlresponsePattern.matcher(responseContent);
 		samlresponseMatcher.find();
 		samlresponse = samlresponseMatcher.group(1);
 
 		return url;
 	}
 
 	/**
 	 * gets login page data
 	 * 
 	 * @param referer
	 * @return URL to third page
 	 * @throws UnsupportedEncodingException
 	 * @throws IOException
 	 */
 	protected URL doStep3(URL referer) throws UnsupportedEncodingException, IOException {
 		URL url3 = new URL(LOGINPAGEDATAGETTERURL);
 		URLConnection inputConnection = url3.openConnection();
 
 		inputConnection.setRequestProperty("Referer", referer.toExternalForm());
 
 		inputConnection.setDoOutput(true);
 		OutputStream outStream = inputConnection.getOutputStream();
 		outStream.write(("RelayState=" + URLEncoder.encode(relaystate, "UTF-8")
 				+ "&SAMLResponse=" + URLEncoder.encode(samlresponse, "UTF-8"))
 				.getBytes());
 		outStream.flush();
 
 		inputConnection.getInputStream();
 
 		return url3;
 	}
 
 	/**
 	 * startpage
 	 * 
 	 * @param referer
	 * @return URL to fourth page
 	 * @throws IOException
 	 */
 	protected URL doStep4(URL referer) throws IOException {
         URL url = new URL(STARTPAGEURL);
         URLConnection inputStream = url.openConnection();
         
         inputStream.setRequestProperty("Referer", referer.toExternalForm());
         
         InputStream responseStream = inputStream.getInputStream();
 
         String responseContent = new Scanner(responseStream).useDelimiter("\\Z").next();
 
         Matcher asiMatcher = asiPattern.matcher(responseContent);
         asiMatcher.find();
         asi = asiMatcher.group(1);
         
         return url;
 	}
 	
 	/**
 	 * overview of study courses
 	 * 
 	 * @param referer
	 * @return URL to fifth page
 	 * @throws IOException
 	 */
 	protected URL doStep5(URL referer) throws IOException {
         URL url = new URL(STUDYCOURSESURL + URLEncoder.encode(asi, "UTF-8"));
         URLConnection inputStream = url.openConnection();
         
         inputStream.setRequestProperty("Referer", referer.toExternalForm());
         
         InputStream responseStream = inputStream.getInputStream();
 
         String responseContent = new Scanner(responseStream).useDelimiter("\\Z").next();
 
         Matcher studeCourseMatcher = studyCoursePattern.matcher(responseContent);
         studeCourseMatcher.find();
         studyCourseURL = studeCourseMatcher.group(1).replaceAll("&amp;", "&");
         
         return url;
 	}
 	
 	/**
 	 * marks overview
 	 * 
 	 * @param referer
	 * @return URL to sixth page
 	 * @throws IOException
 	 */
 	protected URL doStep6(URL referer) throws IOException {
         URL url = new URL(studyCourseURL);
         URLConnection inputStream = url.openConnection();
         
         inputStream.setRequestProperty("Referer", referer.toExternalForm());
         
         InputStream responseStream = inputStream.getInputStream();
 
         String responseContent = new Scanner(responseStream).useDelimiter("\\Z").next();
 
         Matcher tableMatcher = tablePattern.matcher(responseContent);
         tableMatcher.find();
         final String table = tableMatcher.group(1);
 
         Matcher marksMatcher = marksPattern.matcher(table);
 
         marks = new ArrayList<HQNContainer>();
 
         marksMatcher.find();
         marksMatcher.find();
 
         while (marksMatcher.find()) {
             Matcher tdMatcher = tdPattern.matcher(htmlCommentPattern.matcher(marksMatcher.group(1)).replaceAll(""));
             tdMatcher.find();
 
             if (tdMatcher.group().contains("<b>")) {
             	tdMatcher.find();
             	if (tdMatcher.group(2).contains("Durchschnittsnote")) {
             		tdMatcher.find();
             		averageGrade = tdMatcher.group(2).trim();
             		
             		tdMatcher.find();
             		tdMatcher.find();
             		totalCreditPoints = commaZeroPattern.matcher(tdMatcher.group(2)).replaceAll("").trim();
             	}
             	
                 continue;
             }
 
             tdMatcher.find();
             String subject = tdMatcher.group(2).trim();
 
             tdMatcher.find();
             String term = tdMatcher.group(2).trim();
 
             tdMatcher.find();
             String mark = htmlCommentPattern.matcher(tdMatcher.group(2)).replaceAll("").trim();
             
             tdMatcher.find();
             String passed = tdMatcher.group(2).trim();
             
             tdMatcher.find();
             String creditpoints = commaZeroPattern.matcher(tdMatcher.group(2)).replaceAll("").trim();
 
             marks.add(new HQNContainer(subject, term, mark, passed, creditpoints));
         }
         
         return url;
 	}
 	
 	/**
 	 * @return the username
 	 */
 	public String getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user the username to set
 	 */
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	/**
 	 * @return the password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * @param password the password to set
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * @return the path to key store
 	 */
 	public String getKeystoreFile() {
 		return keystoreFile;
 	}
 
 	/**
 	 * @param keystoreFile the path to key store to set
 	 */
 	public void setKeystoreFile(String keystoreFile) {
 		this.keystoreFile = keystoreFile;
 	}
 
 	/**
 	 * @return the key store password
 	 */
 	public char[] getKeystorePassword() {
 		return keystorePassword;
 	}
 
 	/**
 	 * @param keystorePassword the key store password to set
 	 */
 	public void setKeystorePassword(char[] keystorePassword) {
 		this.keystorePassword = keystorePassword;
 	}
 
 	/**
 	 * @return the marks
 	 */
 	public ArrayList<HQNContainer> getMarks() {
 		return marks;
 	}
 
 	/**
 	 * @return the average grade
 	 */
 	public String getAverageGrade() {
 		return averageGrade;
 	}
 
 	/**
 	 * @return total credit points
 	 */
 	public String getTotalCreditPoints() {
 		return totalCreditPoints;
 	}
 }
