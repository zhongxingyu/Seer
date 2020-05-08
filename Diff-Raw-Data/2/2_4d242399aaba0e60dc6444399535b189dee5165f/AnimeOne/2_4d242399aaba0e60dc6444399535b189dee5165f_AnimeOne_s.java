 package net.hisme.masaki.kyoani.models;
 
 import net.hisme.masaki.kyoani.models.ScheduleService.LoginFailureException;
 import net.hisme.masaki.kyoani.models.ScheduleService.NetworkUnavailableException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.cookie.Cookie;
 import java.lang.StringBuffer;
 import java.net.UnknownHostException;
 import java.io.OutputStreamWriter;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.io.BufferedReader;
 import javax.xml.parsers.*;
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 import java.util.regex.*;
 import java.util.ArrayList;
 import android.util.Log;
 import android.content.Context;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 public class AnimeOne extends AbstractScheduleService {
     private Account account = null;
 
     public static final String REGISTER_URI = "https://anime.biglobe.ne.jp/regist/regist_user";
     private static final String MYPAGE_URI = "http://anime.biglobe.ne.jp/program/myprogram";
     private static final String LOGIN_FORM = "https://anime.biglobe.ne.jp/login/index";
     private static final String LOGIN_URI = "https://anime.biglobe.ne.jp/login/login_ajax";
     private static final String LOGOUT_URI = "https://anime.biglobe.ne.jp/login/logout_ajax";
     private static final String SESSION_FILE_NAME = "_session";
     private static final String SESSION_KEY_NAME = "PHPSESSID";
 
     private static final int BUFFSIZE = 1024;
 
     public static final int LOGIN_OK = 0;
     public static final int LOGIN_NG = 1;
     public static final int NETWORK_ERROR = 2;
 
     /**
      * create new AnimeOne
      */
     public AnimeOne() {
     }
 
     /**
      * @param context
      * @throws Account.BlankException
      */
     public AnimeOne(Context context) throws Account.BlankException {
         setContext(context);
         initAccount();
         initHttpClient();
 
     }
 
     /**
      * @deprecated
      * @param account
      */
     public AnimeOne(Account account) {
         setAccount(account);
         initHttpClient();
     }
 
     private void initAccount() throws Account.BlankException {
         setAccount(new Account(getContext()));
     }
 
     public void setAccount(Account account) {
         this.account = account;
     }
 
     public Account getAccount() {
         return this.account;
     }
 
     @Override
     protected boolean isAccountPresent() {
         return getAccount() != null;
     }
 
     @Override
     protected String getSessionFileName() {
         return SESSION_FILE_NAME;
     }
 
     @Override
     protected String getSessionKeyName() {
         return SESSION_KEY_NAME;
     }
 
     public ArrayList<Schedule> getSchedules() throws LoginFailureException,
             NetworkUnavailableException {
         log("Get Schedules");
         if (needUpdate()) {
             log("Need Update");
             return reloadSchedules();
         } else {
             log("Cached");
             return Schedule.loadSchedules(context);
         }
     }
 
     public Schedule getNextSchedule() throws LoginFailureException,
             NetworkUnavailableException {
         AnimeCalendar now = new AnimeCalendar();
         for (Schedule schedule : getSchedules()) {
             if (now.compareTo(schedule.getStart()) == -1) {
                 return schedule;
             }
         }
         return null;
     }
 
     public ArrayList<Schedule> reloadSchedules() throws LoginFailureException,
             NetworkUnavailableException {
         log("Reload Schedule");
         if (hasSessionID() || login()) {
             log("Use Session");
             try {
                 ArrayList<Schedule> schedules = mypage();
                 if (Schedule.saveSchedules(context, schedules)) {
                     log("Update cached date");
                     AnimeCalendar today = new AnimeCalendar();
                     try {
                         BufferedWriter writer = new BufferedWriter(
                                 new OutputStreamWriter(context.openFileOutput(
                                         DATE_FILE, 0)));
                         writer.write(String.format("%04d-%02d-%02d", today
                                 .get(Calendar.YEAR),
                                 today.get(Calendar.MONTH) + 1, today
                                         .get(Calendar.DAY_OF_MONTH)));
                         writer.flush();
                         writer.close();
                     } catch (FileNotFoundException e) {
                         log("FileNotFound in write updated date");
                     } catch (IOException e) {
                         log("IOException in write updated date");
                     }
                 }
                 return schedules;
             } catch (SessionExpiredException e) {
                 if (login())
                     return reloadSchedules();
             }
         }
         return null;
     }
 
     @Override
     public ArrayList<Schedule> fetchSchedules() {
         log("fetchSchedule");
         try {
             return reloadSchedules();
         } catch (LoginFailureException e) {
             e.printStackTrace();
         } catch (NetworkUnavailableException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public ArrayList<Schedule> mypage() throws SessionExpiredException {
         return mypage(3);
     }
 
     public ArrayList<Schedule> mypage(int retry_count)
             throws SessionExpiredException {
         log("MyPage Start");
         ArrayList<Schedule> result = new ArrayList<Schedule>();
         boolean retry = true;
         try {
             HttpGet get = new HttpGet(MYPAGE_URI);
             HttpResponse response = http.execute(get);
             HttpEntity entity = response.getEntity();
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     entity.getContent()));
             StringBuffer responseText = new StringBuffer();
 
             char[] buf = new char[BUFFSIZE];
             int read_size = 0;
             while ((read_size = reader.read(buf, 0, BUFFSIZE)) != -1) {
                 responseText = responseText.append(buf, 0, read_size);
             }
             get.abort();
 
             Pattern pattern = Pattern
                     .compile(
                             "(<div class=\"w220Box program program2 marginLeft10px\">.*</div>).*<div class=\"w220Box program marginLeft10px\">",
                             Pattern.DOTALL | Pattern.MULTILINE
                                     | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
             Matcher match = pattern.matcher(new String(responseText));
             if (match.find()) {
                 retry = false;
                 NodeList tmp;
                 String body = match.group(1);
                 body = body.replace("&", "&amp;");
                 DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                         .newDocumentBuilder();
                 Document doc = builder.parse(new InputSource(new StringReader(
                         body)));
                 Matcher date_matcher = Pattern.compile("([0-9]+)月([0-9]+)")
                         .matcher(
                                 nodeMapString(
                                         doc.getElementsByTagName("span")
                                                 .item(0)).get(0));
                 date_matcher.find();
 
                 NodeList td_list = doc.getElementsByTagName("td");
                 final int TDNUMS = 4;
                 for (int i = 0; i < td_list.getLength() / TDNUMS; i++) {
                     tmp = td_list.item(i * TDNUMS + 1).getChildNodes();
                     for (int j = 0; j < tmp.getLength(); j++) {
                         ArrayList<String> values = null;
                         if (tmp.item(j).getNodeName().compareTo("img") == 0
                                 && tmp.item(j).getAttributes().getNamedItem(
                                         "alt").getNodeValue()
                                         .compareTo("ネット配信") != 0) {
                             String channel = "";
                             String name = "";
                             String start = "";
 
                             values = nodeMapString(td_list.item(i * TDNUMS + 0));
                             if (values.size() == 1) {
                                 Matcher m = Pattern.compile("([0-9:]+) +(.+)")
                                         .matcher(values.get(0));
                                 m.find();
                                 start = m.group(1);
                                 channel = m.group(2);
                             }
                             values = nodeMapString(td_list.item(i * TDNUMS + 2));
                             name = values.get(0);
                             Schedule schedule = new Schedule(channel, name,
                                     start);
                             log(schedule.toString());
                             result.add(schedule);
 
                             break;
                         }
                     }
                 }
             } else {
                 throw new SessionExpiredException();
             }
             log("Parse Finish: " + result.size() + " items found.");
         } catch (org.apache.http.client.ClientProtocolException e) {
             log(e.toString());
         } catch (java.io.IOException e) {
             log(e.toString());
         } catch (ParserConfigurationException e) {
             log(e.toString());
         } catch (org.xml.sax.SAXException e) {
             org.xml.sax.SAXParseException ex = (org.xml.sax.SAXParseException) e;
             log("row:" + ex.getLineNumber() + "   col: " + ex.getColumnNumber());
         }
         log("MyPage Finish");
         if (retry && retry_count > 0) {
             return mypage(retry_count - 1);
         }
         return result;
     }
 
     public void logout() {
         try {
             HttpPost post = new HttpPost(LOGOUT_URI);
             http.execute(post);
             post.abort();
         } catch (Exception e) {
             log(e.toString());
         }
     }
 
     private String getSessionID() {
         for (Cookie cookie : this.http.getCookieStore().getCookies()) {
             if (cookie.getName().equals("PHPSESSID")) {
                 return cookie.getValue();
             }
         }
         return "";
     }
 
     public boolean login() throws NetworkUnavailableException {
         log("Login Start");
         boolean result = false;
         try {
             HttpPost post = new HttpPost(LOGIN_URI);
             post.addHeader("Referer", LOGIN_FORM);
             post.addHeader("X-Requested-With", "XMLHttpRequest");
            post.addHeader("User-Agent", "Mozilla/5.0(AnimeOneBrowser)");
             post.addHeader("Content-Type",
                     "application/x-www-form-urlencoded; charset=UTF-8");
             post.setEntity(new StringEntity("mail=" + account.getUser()
                     + "&password=" + account.getPassword()));
 
             http.execute(post);
 
             for (Cookie cookie : http.getCookieStore().getCookies()) {
                 if (cookie.getName().equals(getSessionKeyName()))
                     saveSessionID(cookie.getValue());
                 if (cookie.getName().equals("user[id_nick]"))
                     result = true;
             }
             post.abort();
 
             if (result)
                 return result;
 
         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (UnknownHostException e) {
             throw new NetworkUnavailableException();
         } catch (IOException e) {
             throw new NetworkUnavailableException();
         }
         return false;
     }
 
     private ArrayList<String> nodeMapString(Node node) {
         ArrayList<String> res = new ArrayList<String>();
         _nodeToString(res, node);
         return res;
     }
 
     private void _nodeToString(ArrayList<String> list, Node node) {
         Pattern p = Pattern
                 .compile("[ 　\t\n\r]+", Pattern.DOTALL | Pattern.MULTILINE
                         | Pattern.UNICODE_CASE | Pattern.UNIX_LINES);
         switch (node.getNodeType()) {
         case Node.TEXT_NODE:
             Matcher match = p.matcher(node.getNodeValue());
             if (match.replaceAll("").compareTo("") != 0) {
                 list.add(match.replaceAll(" "));
             }
             break;
         default:
             NodeList nl = node.getChildNodes();
             for (int i = 0; i < nl.getLength(); i++) {
                 _nodeToString(list, nl.item(i));
             }
             break;
         }
     }
 
     @SuppressWarnings("unused")
     private void log(boolean b) {
         log(b ? "true" : "false");
     }
 
     @SuppressWarnings("unused")
     private void log(int n) {
         log(new Integer(n).toString());
     }
 
     private void log(String str) {
         Log.d("KyoAni", "[AnimeOne] " + str);
     }
 
     class SessionExpiredException extends Exception {
         private static final long serialVersionUID = 1L;
     }
 
 }
