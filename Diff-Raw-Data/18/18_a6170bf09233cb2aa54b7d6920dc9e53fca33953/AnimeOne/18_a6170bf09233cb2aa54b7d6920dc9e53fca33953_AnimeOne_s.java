 package net.hisme.masaki.kyoani.schedule_service;
 
 import net.hisme.masaki.kyoani.App;
 import net.hisme.masaki.kyoani.models.Account;
 import net.hisme.masaki.kyoani.models.AnimeCalendar;
 import net.hisme.masaki.kyoani.models.Schedule;
 import net.hisme.masaki.kyoani.models.Schedules;
 import net.hisme.masaki.kyoani.schedule_service.exception.LoginFailureException;
 import net.hisme.masaki.kyoani.schedule_service.exception.NetworkUnavailableException;
 import net.hisme.masaki.kyoani.schedule_service.exception.SessionExpiredException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.HttpHost;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.cookie.Cookie;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.net.UnknownHostException;
 import java.io.OutputStreamWriter;
 import java.io.BufferedWriter;
 
 import java.util.regex.*;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 /**
  * adapter for AnimeOne
  * 
  * @author masarakki
  */
 public class AnimeOne extends Base {
   public static final String HOST = "anime.biglobe.ne.jp";
   public static final String REGISTER_URI = "https://anime.biglobe.ne.jp/regist/regist_user";
   private static final String MYPAGE_URI = "http://anime.biglobe.ne.jp/program/myprogram";
   private static final String LOGIN_URI = "/login/login_ajax";
   private static final String LOGOUT_URI = "/login/logout_ajax";
   private static final String SESSION_FILE_NAME = "_session";
   private static final String SESSION_KEY_NAME = "PHPSESSID";
 
   public static final int LOGIN_OK = 0;
   public static final int LOGIN_NG = 1;
   public static final int NETWORK_ERROR = 2;
   public HttpHost http_host, https_host = null;
 
   public AnimeOne() {
     this.http = getClient();
     this.http_host = new HttpHost(HOST, 80, "http");
     this.https_host = new HttpHost(HOST, 443, "https");
     loadSessionID();
   }
 
   @Override
   protected String getSessionFileName() {
     return SESSION_FILE_NAME;
   }
 
   @Override
   protected String getSessionKeyName() {
     return SESSION_KEY_NAME;
   }
 
   public Schedules reloadSchedules() throws LoginFailureException, NetworkUnavailableException {
     log("Reload Schedule");
     if (hasSessionID() || login()) {
       try {
         Schedules schedules = mypage();
         if (schedules.save()) {
           log("Update cached date");
           AnimeCalendar today = new AnimeCalendar();
           try {
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(App.li.openFileOutput(DATE_FILE, 0)));
             writer.write(String.format("%04d-%02d-%02d", today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH)));
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
   public Schedules fetchSchedules() {
     try {
       return reloadSchedules();
     } catch (LoginFailureException e) {
       e.printStackTrace();
     } catch (NetworkUnavailableException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public Schedules parseMypage(String html) throws LoginFailureException {
     AnimeCalendar today = new AnimeCalendar();
     String date_string = String.format("%d月%d日", today.month(), today.day());
     return parseMypage(html, date_string);
   }
 
   public Schedules parseMypage(String html, String date_string) throws LoginFailureException {
     html = replace_whitespaces(html);
     Schedules schedules = new Schedules();
     org.jsoup.nodes.Document document = Jsoup.parse(html);
     if (document.select("#userName a").isEmpty()) {
       log("not logined, retry");
       throw new LoginFailureException();
     }
     Elements daily_lists = document.select(".program");
     for (Element daily_list : daily_lists) {
       if (daily_list.select(".fc_mm").text().contains(date_string)) {
         Elements programs = daily_list.select("table");
         for (Element program : programs) {
           Elements informations = program.select("td");
           if (informations.get(1).select("img").get(0).attr("alt").compareTo("ネット配信") != 0) {
             Matcher date_and_channel = Pattern.compile("([0-9:]+) +(.+)").matcher(informations.get(0).text());
             date_and_channel.find();
             String start = date_and_channel.group(1).trim();
             String channel = date_and_channel.group(2).trim();
             String title = informations.get(2).select("a").text();
             schedules.add(new Schedule(channel, title, start));
           }
         }
         return schedules;
       }
     }
     return schedules;
   }
 
   /**
    * access to MyPage
    * 
    * @return schedules
    * @throws SessionExpiredException
    */
   public Schedules mypage() throws SessionExpiredException {
     return mypage(3);
   }
 
   /**
    * access to MyPage with retry
    * 
    * @param retry_count
    * @return ArrayList of Schedule
    * @throws SessionExpiredException
    */
   public Schedules mypage(int retry_count) throws SessionExpiredException {
     try {
       String html = httpGet(MYPAGE_URI);
       return parseMypage(html);
     } catch (LoginFailureException e) {
       if (retry_count > 0) {
         try {
           if (login()) {
             return mypage(retry_count - 1);
           }
         } catch (NetworkUnavailableException ex) {
 
         }
         log("MyPage: Giveup; relogin failure");
         throw new SessionExpiredException();
       } else {
         log("MyPage: Giveup;");
         throw new SessionExpiredException();
       }
     }
   }
 
   public void logout() {
     try {
       HttpPost post = new HttpPost(LOGOUT_URI);
       http.execute(https_host, post);
       post.abort();
     } catch (Exception e) {
       log(e.toString());
     }
   }
 
   @Override
   public boolean login() throws NetworkUnavailableException {
     return login(App.li.getAccount());
   }
 
   /**
    * execution of login
    * 
    * @param account
    * @return success or failure
    * @throws NetworkUnavailableException
    */
   public boolean login(Account account) throws NetworkUnavailableException {
     log("Login Start");
     boolean result = false;
     try {
       http = getClient();
       HttpPost post = new HttpPost(LOGIN_URI);
 
       ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
       params.add(new BasicNameValuePair("mail", account.getUserName()));
       params.add(new BasicNameValuePair("password", account.getPassword()));
       post.setEntity(new UrlEncodedFormEntity(params));
       http.execute(https_host, post);
 
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
 
   private String replace_whitespaces(String src) {
     return src.replaceAll("[ 　\t\n\r]+", " ");
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
     App.Log.d("[AnimeOne] " + str);
   }
 
   @Override
   protected boolean isAccountPresent() {
     return !App.li.getAccount().isBlank();
   }
 
 }
