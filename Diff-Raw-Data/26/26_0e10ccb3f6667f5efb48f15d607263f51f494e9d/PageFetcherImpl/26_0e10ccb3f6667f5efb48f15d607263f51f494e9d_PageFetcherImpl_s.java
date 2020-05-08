 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package spiderman.plugin.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParamBean;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.eweb4j.spiderman.fetcher.FetchResult;
 import org.eweb4j.spiderman.fetcher.Page;
 import org.eweb4j.spiderman.fetcher.PageFetcher;
 import org.eweb4j.spiderman.fetcher.Status;
 import org.eweb4j.spiderman.xml.Site;
 
 /**
  * Web 页面内容获取器
  * @author weiwei l.weiwei@163.com
  * @date 2013-1-7 上午11:04:50
  */
 public class PageFetcherImpl implements PageFetcher{
 
 	private ThreadSafeClientConnManager connectionManager;
 	private DefaultHttpClient httpClient;
 	private final Object mutex = new Object();
 	private long lastFetchTime = 0;
 	private SpiderConfig config;
 	private Map<String, String> headers = new Hashtable<String, String>();
 	
 	public PageFetcherImpl(){
 	}
 	
 	/**
 	 * 处理GZIP解压缩
 	 * @author weiwei l.weiwei@163.com
 	 * @date 2013-1-7 上午11:26:24
 	 */
 	private static class GzipDecompressingEntity extends HttpEntityWrapper {
 		public GzipDecompressingEntity(final HttpEntity entity) {
 			super(entity);
 		}
 		public InputStream getContent() throws IOException, IllegalStateException {
 			InputStream wrappedin = wrappedEntity.getContent();
 			return new GZIPInputStream(wrappedin);
 		}
 		public long getContentLength() {
 			return -1;
 		}
 	}
 	
 	public void setConfig(SpiderConfig config){
 		this.config = config;
 	}
 	
 	public void addCookie(String key, String val, String host, String path) {
 		Cookie c = new Cookie(key, val, host, path);
 		//设置Cookie
 		String name = c.name();
 		String value = c.value();
 		BasicClientCookie clientCookie = new BasicClientCookie(name, value);
 		clientCookie.setPath(c.path());
 		clientCookie.setDomain(c.domain());
 		httpClient.getCookieStore().addCookie(clientCookie);
 	}
 
 	public void addHeader(String key, String val) {
 		this.headers.put(key, val);
 	}
 
 	/**
 	 * 构造器，进行client的参数设置，包括Header、Cookie等
 	 * @param aconfig
 	 * @param cookies
 	 */
 	public void init(Site site) {
 		for (org.eweb4j.spiderman.xml.Header header : site.getHeaders().getHeader()){
 			this.addHeader(header.getName(), header.getValue());
 		}
 		for (org.eweb4j.spiderman.xml.Cookie cookie : site.getCookies().getCookie()){
 			this.addCookie(cookie.getName(), cookie.getValue(), cookie.getHost(), cookie.getPath());
 		}
 		
 		//设置HTTP参数
 		HttpParams params = new BasicHttpParams();
 		params.setParameter(CoreProtocolPNames.USER_AGENT, config.getUserAgentString());
 		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, config.getSocketTimeout());
 		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, config.getConnectionTimeout());
 		
 		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
 		paramsBean.setVersion(HttpVersion.HTTP_1_1);
 		paramsBean.setContentCharset("UTF-8");
 		paramsBean.setUseExpectContinue(false);
 		
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
 
 		if (config.isIncludeHttpsPages()) 
 			schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
 
 		connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
 		connectionManager.setMaxTotal(config.getMaxTotalConnections());
 		connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());
 		
 		httpClient = new DefaultHttpClient(connectionManager, params);
 		httpClient.getParams().setIntParameter("http.socket.timeout", 15000);
 		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
 
 		//设置响应拦截器
         httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
             public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                 HttpEntity entity = response.getEntity();
                 Header contentEncoding = entity.getContentEncoding();
                 if (contentEncoding != null) {
                     HeaderElement[] codecs = contentEncoding.getElements();
                     for (HeaderElement codec : codecs) {
                     	//处理GZIP解压缩
                         if (codec.getName().equalsIgnoreCase("gzip")) {
                             response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                             return;
                         }
                     }
                 }
             }
         });
 	}
 
 	/**
 	 * 抓取目标url的内容
 	 * @date 2013-1-7 上午11:08:54
 	 * @param toFetchURL
 	 * @return
 	 */
 	public FetchResult fetch(String toFetchURL) throws Exception{
 		FetchResult fetchResult = new FetchResult();
 		HttpGet get = null;
 		HttpEntity entity = null;
 		try {
 			get = new HttpGet(toFetchURL);
 			//设置请求GZIP压缩，注意，前面必须设置GZIP解压缩处理
 			get.addHeader("Accept-Encoding", "gzip");
 			for (Iterator<Entry<String, String>> it = headers.entrySet().iterator(); it.hasNext();){
 				Entry<String, String> entry = it.next();
 				get.setHeader(entry.getKey(), entry.getValue());
 			}
 			
 			//同步信号量,在真正对服务端进行访问之前进行访问间隔的控制
 			synchronized (mutex) {
 				//获取当前时间
 				long now = (new Date()).getTime();
 				//对同一个Host抓取时间间隔进行控制，若在设置的时限内则进行休眠
 				if (now - lastFetchTime < config.getPolitenessDelay()) 
 					Thread.sleep(config.getPolitenessDelay() - (now - lastFetchTime));
 				//不断更新最后的抓取时间，注意，是针对HOST的，不是针对某个URL的
 				lastFetchTime = (new Date()).getTime();
 			}
 			
 			//执行get访问，获取服务端返回内容
 			HttpResponse response = httpClient.execute(get);
 			//设置已访问URL
 			fetchResult.setFetchedUrl(toFetchURL);
 			String uri = get.getURI().toString();
 			if (!uri.equals(toFetchURL)) 
 				if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) 
 					fetchResult.setFetchedUrl(uri);
 			
 			entity = response.getEntity();
 			//服务端返回的状态码
 			int statusCode = response.getStatusLine().getStatusCode();
 			if (statusCode != HttpStatus.SC_OK) {
 				if (statusCode != HttpStatus.SC_NOT_FOUND) {
 					Header locationHeader = response.getFirstHeader("Location");
 					//如果是301、302跳转，获取跳转URL即可返回
 					if (locationHeader != null && (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY)) 
 						fetchResult.setMovedToUrl(URLCanonicalizer.getCanonicalURL(locationHeader.getValue(), toFetchURL));
 				}
 				//只要不是OK的除了设置跳转URL外设置statusCode即可返回
 				fetchResult.setStatusCode(statusCode);
 				return fetchResult;
 			}
 
 			//处理服务端返回的实体内容
 			if (entity != null) {
 				fetchResult.setStatusCode(HttpStatus.SC_OK);
 				Page page = load(entity);
 				page.setUrl(fetchResult.getFetchedUrl());
 				fetchResult.setPage(page);
 				return fetchResult;
 			}
 		} catch (IOException e) {
 			fetchResult.setStatusCode(Status.INTERNAL_SERVER_ERROR.ordinal());
 			return fetchResult;
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			try {
 				if (entity == null && get != null) 
 					get.abort();
 			} catch (Exception e) {
 				throw e;
 			}
 		}
 		
 		fetchResult.setStatusCode(Status.UNSPECIFIED_ERROR.ordinal());
 		return fetchResult;
 	}
 	
 	/**
 	 * 将Entity的内容载入Page对象
 	 * @date 2013-1-7 上午11:22:06
 	 * @param entity
 	 * @return
 	 * @throws Exception
 	 */
 	private Page load(HttpEntity entity) throws Exception {
 		Page page = new Page();
 		
 		//设置返回内容的ContentType
 		String contentType = null;
 		Header type = entity.getContentType();
 		if (type != null) 
 			contentType = type.getValue();
 		page.setContentType(contentType);
 		
 		//设置返回内容的字符编码
 		String contentEncoding = null;
 		Header encoding = entity.getContentEncoding();
 		if (encoding != null) 
 			contentEncoding = encoding.getValue();
 		page.setEncoding(contentEncoding);
 		
 		//设置返回内容的字符集
 		String contentCharset = EntityUtils.getContentCharSet(entity);
 		page.setCharset(contentCharset);
 		
 		//根据配置文件设置的字符集参数进行内容二进制话
 		String charset = config.getCharset();
 		String content = this.read(entity.getContent(), charset);
 		page.setContent(content);
 		if (charset == null || charset.trim().length() == 0)
 			page.setContentData(content.getBytes());
 		else
 			page.setContentData(content.getBytes(charset));
 		
 		return page;
 	}
 	
 	/**
 	 * 根据字符集从输入流里面读取String内容
 	 * @date 2013-1-7 上午11:25:04
 	 * @param inputStream
 	 * @param charset
 	 * @return
 	 */
 	private String read(final InputStream inputStream, String charset) {
 		StringBuilder sb = new StringBuilder();
 		BufferedReader reader = null;
 		try {
 			if (charset == null || charset.trim().length() == 0)
 				reader = new BufferedReader(new InputStreamReader(inputStream));
 			else
 				reader = new BufferedReader(new InputStreamReader(inputStream, charset));
 			
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line);
 			}
 		} catch (IOException e) {
 		}
 
 		return sb.toString();
 	}
 	
 	/**
 	 * 从输入流里读取二进制数据
 	 * @date 2013-1-7 上午11:25:38
 	 * @param inputStream
 	 * @return
 	 * @throws Exception
 	 */
 	private byte[] read(final InputStream inputStream) throws Exception {
 		byte[] bytes = new byte[1000];
 		int i = 0;
 		int b;
 		try {
 			while ((b = inputStream.read()) != -1) {
 				bytes[i++] = (byte) b;
 				if (bytes.length == i) {
 					byte[] newBytes = new byte[(bytes.length * 3) / 2 + 1];
 					for (int j = 0; j < bytes.length; j++) {
 						newBytes[j] = bytes[j];
 					}
 					bytes = newBytes;
 				}
 			}
 		} catch (IOException e) {
 			throw new Exception("There was a problem reading stream.", e);
 		}
 
 		byte[] copy = Arrays.copyOf(bytes, i);
 
 		return copy;
 	}
 
 
 	public HttpClient getHttpClient() {
 		return httpClient;
 	}
 
 	/**
 	 * Proxy
 	 * if (config.getProxyHost() != null) {
 			if (config.getProxyUsername() != null) {
 				httpClient.getCredentialsProvider().setCredentials(
 						new AuthScope(config.getProxyHost(), config.getProxyPort()),
 						new UsernamePasswordCredentials(config.getProxyUsername(), config.getProxyPassword()));
 			}
 
 			HttpHost proxy = new HttpHost(config.getProxyHost(), config.getProxyPort());
 			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
         }
 	 */
 }
