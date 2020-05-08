 /*******************************************************************************
  * Copyright (c) 2012 MASConsult Ltd
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.masconsult.bgbanking.banks.dskbank;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.net.UrlQuerySanitizer;
 import android.util.Log;
 import eu.masconsult.bgbanking.BankingApplication;
 import eu.masconsult.bgbanking.banks.BankClient;
 import eu.masconsult.bgbanking.banks.CaptchaException;
 import eu.masconsult.bgbanking.banks.RawBankAccount;
 import eu.masconsult.bgbanking.utils.Convert;
 
 public class DskClient implements BankClient {
 
     /** The tag used to log to adb console. */
     private static final String TAG = BankingApplication.TAG + "DskClient";
 
     /** Timeout (in ms) we specify for each http request */
     private static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
 
     /** Domain for DSK Direct website */
     private static final String DOMAIN = "www.dskdirect.bg";
     /** Base URL for DSK Direct website */
     private static final String BASE_URL = "https://" + DOMAIN + "/page/default.aspx";
     private static final String XML_ID_PREFIX = "/en-US/";
     /** URI for authentication service */
     private static final String AUTH_XML_ID = XML_ID_PREFIX + ".processlogin";
     /** URI for retrieving bank account */
     private static final String LIST_ACCOUNTS_XML_ID = XML_ID_PREFIX + "01Individuals/02Accounts/";
     /** URI for retrieving captcha */
     private static final String CAPTCHA_XML_ID = XML_ID_PREFIX + ".CaptchaImage";
 
     /** POST parameter name for the user's account name */
     private static final String PARAM_USERNAME = "userName";
     /** POST parameter name for the user's password */
     private static final String PARAM_PASSWORD = "pwd";
 
     private static final String XML_ID = "xml_id";
 
     private static final String ENCODING = "utf8";
 
     private static final String PARAM_USER_ID = "user_id";
 
     private static final String PARAM_SESSION_ID = "session_id";
 
     private static final Pattern PATTERN_MATCH_BANK_ACCOUNT_ID =
             Pattern.compile(".*document\\.forms\\[0\\]\\.BankAccountID\\.value='(\\d+)';.*",
                     Pattern.MULTILINE | Pattern.DOTALL);
 
     /**
      * Configures the httpClient to connect to the URL provided.
      * 
      * @param authToken
      */
     private static DefaultHttpClient getHttpClient() {
         DefaultHttpClient httpClient = new DefaultHttpClient();
         final HttpParams params = httpClient.getParams();
         HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
         return httpClient;
     }
 
     @Override
     public String authenticate(String username, String password) throws IOException,
             ParseException, CaptchaException {
         final HttpResponse resp;
         final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(PARAM_USERNAME, username));
         params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
         final HttpEntity entity;
         try {
             entity = new UrlEncodedFormEntity(params);
         } catch (final UnsupportedEncodingException e) {
             // this should never happen.
             throw new IllegalStateException(e);
         }
         String uri = BASE_URL + "?"
                 + URLEncodedUtils.format(
                         Arrays.asList(new BasicNameValuePair(XML_ID, AUTH_XML_ID)), ENCODING);
         Log.i(TAG, "Authenticating to: " + uri);
         final HttpPost post = new HttpPost(uri);
         post.addHeader(entity.getContentType());
         post.setHeader("Accept", "*/*");
         post.setEntity(entity);
         try {
             resp = getHttpClient().execute(post);
 
             if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                 throw new ParseException("login: unhandled http status "
                         + resp.getStatusLine().getStatusCode() + " "
                         + resp.getStatusLine().getReasonPhrase());
             }
 
             String response = EntityUtils.toString(resp.getEntity());
             Log.v(TAG, "response = " + response);
 
             Document doc = Jsoup.parse(response, BASE_URL);
             Element mainForm = doc.getElementById("mainForm");
             if (mainForm == null) {
                 throw new ParseException("login: missing mainForm");
             }
 
             String action = BASE_URL + mainForm.attr("action");
             Log.v(TAG, "action=" + action);
             UrlQuerySanitizer sanitizer = new UrlQuerySanitizer(action);
             String user_id = sanitizer.getValue(PARAM_USER_ID);
             String session_id = sanitizer.getValue(PARAM_SESSION_ID);
 
             if (user_id == null || "".equals(user_id) || session_id == null
                     || "".equals(session_id)) {
                 if (doc.getElementsByClass("redtext").size() > 0) {
                     // bad authentication
                     return null;
                 } else {
                     // TODO handle captcha
                     Elements captcha = doc.select("input[name=captcha_hkey]");
                     if (captcha != null && captcha.size() == 1) {
                         String captchaHash = captcha.first().attr("value");
                         String captchaUri = BASE_URL + "?" + URLEncodedUtils.format(Arrays.asList(
                                 new BasicNameValuePair(XML_ID, CAPTCHA_XML_ID),
                                 new BasicNameValuePair("captcha_key", captchaHash)
                                 ),
                                 ENCODING);
                         throw new CaptchaException(captchaUri);
                     }
                     throw new ParseException("no user_id or session_id: " + action);
                 }
             }
 
             return URLEncodedUtils.format(Arrays.asList(
                     new BasicNameValuePair(PARAM_USER_ID, user_id),
                     new BasicNameValuePair(PARAM_SESSION_ID, session_id)),
                     ENCODING);
         } catch (ClientProtocolException e) {
            throw new IOException(e.getMessage());
         }
     }
 
     @Override
     public List<RawBankAccount> getBankAccounts(String authToken) throws IOException,
             ParseException, AuthenticationException {
         String uri = BASE_URL + "?" + URLEncodedUtils.format(
                 Arrays.asList(new BasicNameValuePair(XML_ID, LIST_ACCOUNTS_XML_ID)), ENCODING)
                 + "&" + authToken;
 
         // Get the accounts list
         Log.i(TAG, "Getting from: " + uri);
         final HttpGet get = new HttpGet(uri);
         get.setHeader("Accept", "*/*");
 
         DefaultHttpClient httpClient = getHttpClient();
 
         Log.v(TAG, "sending " + get.toString());
         final HttpResponse resp = httpClient.execute(get);
 
         if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
             throw new ParseException("getBankAccounts: unhandled http status "
                     + resp.getStatusLine().getStatusCode() + " "
                     + resp.getStatusLine().getReasonPhrase());
         }
 
         HttpEntity entity = resp.getEntity();
         Document doc = Jsoup.parse(entity.getContent(), "utf-8", BASE_URL);
 
         if (!checkLoggedIn(doc)) {
             throw new AuthenticationException("session expired!");
         }
 
         Element content = doc.getElementById("PageContent");
         if (content == null) {
             throw new ParseException("getBankAccounts: can't find PageContent");
         }
 
         Elements tables = content.getElementsByTag("table");
         if (tables == null || tables.size() == 0) {
             throw new ParseException("getBankAccounts: can't find table in PageContent");
         }
 
         Elements rows = tables.first().getElementsByTag("tr");
         if (rows == null || rows.size() == 0) {
             throw new ParseException("getBankAccounts: first table is empty in PageContent");
         }
 
         ArrayList<RawBankAccount> bankAccounts = new ArrayList<RawBankAccount>(rows.size());
 
         String lastCurrency = null;
         for (Element row : rows) {
             RawBankAccount bankAccount = obtainBankAccountFromHtmlTableRow(row);
             if (bankAccount != null) {
                 if (bankAccount.getCurrency() == null) {
                     bankAccount.setCurrency(lastCurrency);
                 } else {
                     lastCurrency = bankAccount.getCurrency();
                 }
                 bankAccounts.add(bankAccount);
             }
         }
 
         return bankAccounts;
     }
 
     private RawBankAccount obtainBankAccountFromHtmlTableRow(Element row) {
         // skip title rows
         if (row.children().size() != 4) {
             return null;
         }
         // skip header
         if (row.hasClass("td-header")) {
             return null;
         }
 
         String onclick = row.child(0).child(0).attr("onclick");
         Matcher matcher = PATTERN_MATCH_BANK_ACCOUNT_ID.matcher(onclick);
         if (!matcher.find()) {
             throw new ParseException("can't find bank account id in " + onclick);
         }
 
         return new RawBankAccount()
                 .setServerId(matcher.group(1))
                 .setName(row.child(0).text())
                 .setIBAN(row.child(1).text())
                 .setCurrency(row.child(2).text())
                 .setBalance(Convert.strToFloat(row.child(3).text()))
                 .setAvailableBalance(Convert.strToFloat(row.child(3).text()));
     }
 
     private boolean checkLoggedIn(Document doc) {
         Elements sup_links = doc.getElementsByClass("supplemental_links");
         if (sup_links == null || sup_links.size() == 0) {
             throw new ParseException("getBankAccounts: can't find .supplemental_links");
         }
         for (Element sup_link : sup_links) {
             Elements exits = sup_link.getElementsContainingText("Log Out");
             if (exits != null && exits.size() > 0) {
                 return true;
             }
         }
         return false;
     }
 
 }
