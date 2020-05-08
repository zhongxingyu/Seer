 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.dip.komusubi.botter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * 
  * @author jun.ozeki 
  * @since 2009/09/10
  * @version $Id: Configuration.java 1358 2011-01-10 04:16:13Z jun $
  */
 public enum Configuration {
 	// enum field singleton.
 	SINGLETON;
 	public static final String BITLY_API_LOGIN_ID = "bit.ly.login.id";
 	public static final String BITLY_API_KEY = "bit.ly.api.key";
 //	public static final String FEED_LAST_TWEET = "feed.last.tweet";
 //	public static final String FLIGHT_STATUS_LAST_TWEET = "flight.status.last.tweet";
 	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
 	private static final String PROPERTY_FILENAME = "twittee.xml";
 //	private static Configuration instance;
 	private static Properties properties;
 	private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
 	private static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
 	private static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
 
 //	private Configuration() {
 	static {
 		properties = new Properties();
 		InputStream input = null;
 		try {
 			input = Configuration.class.getClassLoader().getResourceAsStream(PROPERTY_FILENAME);
 			if (logger.isDebugEnabled()) {
 				URL url = Configuration.class.getClassLoader().getResource(PROPERTY_FILENAME);
 				logger.debug("path : {}", url.toString());
 			}
 			if (input == null) 
 				input = new FileInputStream(new File(PROPERTY_FILENAME));
 
 //			input = new BufferedInputStream(new FileInputStream(file));
 			properties.loadFromXML(input);
 		} catch (IOException e) {
 			logger.error("プロパティーファイルの読み込みに失敗しました。", e);
 			throw new BotterException(e);
 		} finally {
 			try {
 				if (input != null)
 					input.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 	
 	public String[] getDateFormatPattern() {
 		return new String[]{PATTERN_RFC1123, PATTERN_RFC1036, PATTERN_ASCTIME};
 	}
 	
 	public String getLastModified(URL url) {
 		return getProperty(url.toExternalForm());
 	}
 
 	public String getProperty(String key) {
 		return getProperty(key, null);
 	}
 
 	public String getProperty(String key, String aDefault) {
 		return properties.getProperty(key, aDefault);
 	}
 
 	/**
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public Date getPropertyDate(String key) {
 		Date date;
 		try {
 			String value = getProperty(key, null);
 			if (value == null)
 				date = new Date(0L);
 			else 
 				date = DateUtils.parseDate(value, getDateFormatPattern());
 //			date = DateUtils.parseDate(value,
 //						new String[]{DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()});
 		} catch (ParseException e) {
 			logger.error("設定ファイル日付 構文エラー: ", e);
 			date = new Date(0L);
 		}
 		return date;
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 * @param modified
 	 */
 	public void setLastModified(URL url, Date modified) {
 		// 保持している値よりも新しいか確認
 		if (getPropertyDate(url.toExternalForm()).after(modified)) {
 			logger.warn("最終つぶやき時刻がファイルに保持している値よりも古い時刻です: " +
 					"property値: {}, modified: {}", getPropertyDate(url.toExternalForm()), 
 					modified);
 			return;
 		}
 		setLastModified(url, DateFormatUtils.format(modified, getDateFormatPattern()[0]));
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 * @param value
 	 */
 	public void setLastModified(URL url, String value) {
 		properties.put(url.toString(), value);
 		OutputStream outStream = null;
 		try {
 			URL resourceUrl = Configuration.class.getResource("/" + PROPERTY_FILENAME);
 			outStream = new FileOutputStream(resourceUrl.getPath());
 //			outStream = new FileOutputStream(PROPERTY_FILENAME);
 			
 			properties.storeToXML(outStream, "twittee configuration");
 		} catch (FileNotFoundException e) {
 			throw new BotterException(e);
 		} catch (IOException e) {
 			throw new BotterException(e);
 		} finally {
 			try {
 				if (outStream != null)
 					outStream.close();
 			} catch (IOException e) {
 				// nothing to do
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public static Configuration getInstance() {
 //	public static synchronized Configuration getInstance() {
 //		if (instance == null)
 //			instance = new Configuration();
 //		return instance;
 		return SINGLETON;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 //	public List<URL> getFeedUrls() {
 //		List<URL> urls = new ArrayList<URL>();
 //		try {
 //			for (Object url: properties.keySet()) {
 //				if (FEED_LAST_TWEET.equals(url) || 
 //						FLIGHT_STATUS_LAST_TWEET.equals(url))
 //					continue;
 //				urls.add(new URL((String) url));
 //			}
 //			return urls;
 //		} catch (MalformedURLException e) {
 //			throw new TwitteeException(e);
 //		}
 //	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public List<Site> getFeedSites() {
 		List<Site> sites = new ArrayList<Site>();
 		try {
 			sites.add(new SiteEntity(
 					new URL("http://rss.jal.co.jp/f6049/index.rdf"), 
 					"プレスリリース",
 					"#JAL", "#press"));
 			sites.add(new SiteEntity(
 					new URL("http://rss.jal.co.jp/f4749/index.rdf"), 
 					"JALマイレージバンクのお知らせ",
 					"#JAL", "#JMB"));
 			sites.add(new SiteEntity(
 					new URL("http://rss.jal.co.jp/f4728/index.rdf"), 
 					"JALからのお知らせ",
 					"#JAL", "#info"));
 			sites.add(new SiteEntity(
 					new URL("http://rss.jal.co.jp/f4746/index.rdf"), 
 					"国内線のお知らせ",
 					"#JAL", "#domestic"));
 			sites.add(new SiteEntity(
 					new URL("http://rss.jal.co.jp/f4755/index.rdf"), 
 					"企業情報",
 					"#JAL"));
 			
 			
 //			sites.add(new SiteEntity(
 //					new URL("http://rss.jal.co.jp/f4751/index.rdf"), 
 //					"マイルパートナーからのお知らせ",
 //					"#JAL", "#partner"));
 //			sites.add(new SiteEntity(
 //					new URL("http://rss.jal.co.jp/f6513/index.rdf"),
 //					"国内ツアー新着情報",
 //					"#JAL", "#tour"));
 //			sites.add(new SiteEntity(
 //					new URL("http://rss.jal.co.jp/f5717/index.rdf"), 
 //					"投資家情報",
 //					"#JAL", "#IR"));
 		} catch (MalformedURLException e) {
 			logger.error("feed site 構成時に例外発生:", e);
 			// nothing to do
 		}
 		return sites;
 	}
 	
 	public String getCachePath() {
 		return "./cache";
 	}
 	
 	/**
 	 * feed site.
 	 * Siteは実在するentityとして扱う。
 	 * @author jun.ozeki 
 	 * @since 2009/11/07
 	 * @version $Id: Configuration.java 1358 2011-01-10 04:16:13Z jun $
 	 */
 	private static class SiteEntity implements Site {
 		private static final long serialVersionUID = -4876959741610431883L;
 		private String title;
 		private URL url;
 		private List<String> hashTags;
 
 		/**
 		 * コンストラクタ。
 		 * @param url
 		 * @param title
 		 */
 		private SiteEntity(URL url, String title, String... hashTag) {
 			this.url = url;
 			this.title = title;
 			this.hashTags = Arrays.asList(hashTag); 
 		}
 		
 		@Override
 		public String getTitle() {
 			return title;
 		}
 
 		@Override
 		public URL getUrl() {
 			return url;
 		}
 		@Override
 		public List<String> getHashTags() {
 			return hashTags; 
 		}
 		@Override
 		public String toString() {
 			return ToStringBuilder.reflectionToString(this);
 		}
 	}
 }
