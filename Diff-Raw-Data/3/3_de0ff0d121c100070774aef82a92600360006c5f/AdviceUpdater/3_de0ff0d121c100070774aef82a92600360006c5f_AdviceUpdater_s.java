 /*
  * Copyright 2012 Oxygen Development
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.devoxy.fgawidget.web;
 
 import android.content.Context;
 import android.util.Log;
 import com.devoxy.fgawidget.R;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by Dmitriy Tarasov.
  * <p/>
  * Email: dm.vl.tarasov@gmail.com
  * Date: 18.01.12
  * Time: 21:31
  */
 public class AdviceUpdater {
 
     private static final String TAG = AdviceUpdater.class.getName();
 
     private static final String MAIN_PAGE_URL = "http://fucking-great-advice.ru/";
 
     private static final String RSS_FEED_URL = "http://feeds.feedburner.com/365advices?format=xml";
 
     private static final String LINK_REGEX = ".*<a id=\"next\" href=\"(http://fucking-great-advice.ru/advice/\\d*/)\" .*";
 
     private static final Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX);
 
     private static final String ADVICE_REGEX = "<p id=\"advice\">(.*)</p>";
 
     private static final Pattern ADVICE_PATTERN = Pattern.compile(ADVICE_REGEX);
 
     private static String nextAdvice;
 
     public static String getTodayAdvice(Context context) {
         try {
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             documentBuilderFactory.setValidating(false);
             DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
             Document doc = builder.parse(RSS_FEED_URL);
 
             NodeList items = doc.getElementsByTagName("item");
             Node todayAdvice = items.item(0);
             NodeList itemParts = todayAdvice.getChildNodes();
             for (int i = 0; i < itemParts.getLength(); i++) {
                 if ("title".equals(itemParts.item(i).getNodeName())) {
                     Node node = itemParts.item(i);
                     return node.getFirstChild().getNodeValue();
                 }
             }
         } catch (Exception e) {
             Log.e(TAG, "cannot obtain advice", e);
         }
         return context.getString(R.string.connection_problem);
     }
 
     public static String getRandomAdvice(Context context) {
         HttpURLConnection connection = null;
         try {
             URL serverAddress = new URL(nextAdvice == null ? MAIN_PAGE_URL : nextAdvice);
             connection = (HttpURLConnection) serverAddress.openConnection();
             connection.connect();
 
             BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "windows-1251"));
             StringBuilder sb = new StringBuilder();
 
             String line;
             while ((line = rd.readLine()) != null) {
                 sb.append(line).append('\n');
             }
 
             String page = sb.toString();
             nextAdvice = parseNextAdviceUrl(page);
             return parseAdvice(context, page);
         } catch (MalformedURLException e) {
             Log.e(TAG, "Incorrect URL exception", e);
         } catch (ProtocolException e) {
             Log.e(TAG, "Protocol exception", e);
         } catch (IOException e) {
             Log.e(TAG, "IO exception", e);
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
         return context.getString(R.string.connection_problem);
     }
 
     private static String parseAdvice(Context context, String page) {
         Matcher matcher = ADVICE_PATTERN.matcher(page);
         if (matcher.find()) {
            return matcher.group(1).replaceAll("&.*;", "");
         } else {
             return context.getString(R.string.connection_problem);
         }
     }
 
     private static String parseNextAdviceUrl(String page) {
         Matcher matcher = LINK_PATTERN.matcher(page);
         if (matcher.find()) {
             return matcher.group(1);
         } else {
             return MAIN_PAGE_URL;
         }
     }
 }
