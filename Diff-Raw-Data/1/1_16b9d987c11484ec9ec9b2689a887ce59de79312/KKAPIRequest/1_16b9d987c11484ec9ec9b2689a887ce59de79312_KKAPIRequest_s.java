 /* Copyright (C) 2013 KKBOX Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * ​http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  * KKAPIRequest
  */
 package com.kkbox.toolkit.api;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.os.SystemClock;
 
 import com.kkbox.toolkit.internal.api.KKAPIRequestListener;
 import com.kkbox.toolkit.utils.KKDebug;
 import com.kkbox.toolkit.utils.StringUtils;
 import com.kkbox.toolkit.utils.UserTask;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.crypto.Cipher;
 
 public class KKAPIRequest extends UserTask<Object, Void, Void> {
 	private KKAPIRequestListener listener;
 	private String getParams = "";
 	private final String url;
 	private HttpClient httpclient;
 	private boolean isNetworkError = false;
 	private boolean isHttpStatusError = false;
 	private int httpStatusCode = 0;
 	private ArrayList<NameValuePair> postParams;
 	private ArrayList<NameValuePair> headerParams;
 	private MultipartEntity multipartEntity;
 	private StringEntity stringEntity;
 	private FileEntity fileEntity;
 	private ByteArrayEntity byteArrayEntity;
 	private InputStreamEntity gzipStreamEntity;
 	private Cipher cipher = null;
 
 	private Context context = null;
 	private long reloadPeriod = -1;
 
 	public KKAPIRequest(String url, Cipher cipher, long reloadPeriod, Context context) {
 		this(url, cipher, 10000);
 		this.reloadPeriod = reloadPeriod;
 		this.context = context;
 	}
 
 	public KKAPIRequest(String url, Cipher cipher) {
 		this(url, cipher, 10000);
 	}
 
 	public KKAPIRequest(String url, Cipher cipher, int socketTimeout) {
 		BasicHttpParams params = new BasicHttpParams();
 		params.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
 		params.setIntParameter(HttpConnectionParams.SO_TIMEOUT, socketTimeout);
 		httpclient = new DefaultHttpClient(params);
 		this.url = url;
 		this.cipher = cipher;
 	}
 
 	public void addGetParam(String key, String value) {
 		if (getParams == "") {
 			getParams = "?";
 		} else {
 			getParams += "&";
 		}
 		getParams += key + "=" + value;
 	}
 
 	public void addGetParam(String parameter) {
 		if (getParams == "") {
 			getParams = "?";
 		} else {
 			getParams += "&";
 		}
 		getParams += parameter;
 	}
 
 	public void addPostParam(String key, String value) {
 		if (postParams == null) {
 			postParams = new ArrayList<NameValuePair>();
 		}
 		postParams.add((new BasicNameValuePair(key, value)));
 	}
 
 	public void addHeaderParam(String key, String value) {
 		if (headerParams == null) {
 			headerParams = new ArrayList<NameValuePair>();
 		}
 		headerParams.add((new BasicNameValuePair(key, value)));
 	}
 
 	public void addMultiPartPostParam(String key, ContentBody contentBody) {
 		if (multipartEntity == null) {
 			multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 		}
 		multipartEntity.addPart(key, contentBody);
 	}
 
 	public void addStringPostParam(String data) {
 		try {
 			stringEntity = new StringEntity(data, HTTP.UTF_8);
 		} catch (Exception e) {};
 	}
 
 	public void addFilePostParam(String path) {
 		fileEntity = new FileEntity(new File(path), URLEncodedUtils.CONTENT_TYPE + HTTP.CHARSET_PARAM + HTTP.UTF_8);
 	}
 
 	public void addByteArrayPostParam(byte[] data) {
 		byteArrayEntity = new ByteArrayEntity(data);
 		byteArrayEntity.setContentType("application/octet-stream");
 	}
 
 	public void addGZIPPostParam(String key, String value) {
 		try {
 			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 			ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
 			postParams.add((new BasicNameValuePair(key, value)));
 			GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
 			gZIPOutputStream.write(EntityUtils.toByteArray(new UrlEncodedFormEntity(postParams, HTTP.UTF_8)));
 			gZIPOutputStream.close();
 			byte[] byteDataForGZIP = byteArrayOutputStream.toByteArray();
 			byteArrayOutputStream.close();
 			gzipStreamEntity = new InputStreamEntity(new ByteArrayInputStream(byteDataForGZIP), byteDataForGZIP.length);
 			gzipStreamEntity.setContentType("application/x-www-form-urlencoded");
 			gzipStreamEntity.setContentEncoding("gzip");
 		} catch (Exception e) {}
 	}
 
 	public void cancel() {
 		listener = null;
 		this.cancel(true);
 	}
 
 	@Override
 	public Void doInBackground(Object... params) {
 		int readLength;
 		final ByteArrayOutputStream data = new ByteArrayOutputStream();
 		final byte[] buffer = new byte[128];
 		listener = (KKAPIRequestListener) params[0];
 		int retryTimes = 0;
 		File cacheFile = null;
 		ConnectivityManager connectivityManager = null;
 		if (context != null) {
 			final File cacheDir = new File(context.getCacheDir().getAbsolutePath() + File.separator + "api");
 			if (!cacheDir.exists()) {
 				cacheDir.mkdir();
 			}
 			cacheFile = new File(cacheDir.getAbsolutePath() + File.separator + StringUtils.getMd5Hash(url + getParams));
 			connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
 		}
 
 		if (context != null && reloadPeriod > 0 && cacheFile.exists()
 				&& ((System.currentTimeMillis() - cacheFile.lastModified() < reloadPeriod)
 				|| connectivityManager == null)) {
 			try {
 				InputStream inputStream = new FileInputStream(cacheFile);
 				while ((readLength = inputStream.read(buffer, 0, buffer.length)) != -1) {
 					data.write(buffer, 0, readLength);
 				}
 				data.flush();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			do {
 				try {
 					HttpResponse response;
 					if (postParams != null || multipartEntity != null || stringEntity != null || fileEntity != null
 							|| byteArrayEntity != null
 							|| gzipStreamEntity != null || (headerParams != null && postParams != null)) {
 						final HttpPost httppost = new HttpPost(url + getParams);
 						if (postParams != null) {
 							httppost.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
 						}
 						if (multipartEntity != null) {
 							httppost.setEntity(multipartEntity);
 						}
 						if (stringEntity != null) {
 							httppost.setEntity(stringEntity);
 						}
 						if (fileEntity != null) {
 							httppost.setEntity(fileEntity);
 						}
 						if (byteArrayEntity != null) {
 							httppost.setEntity(byteArrayEntity);
 						}
 						if (gzipStreamEntity != null) {
 							httppost.setHeader("Accept-Encoding", "gzip");
 							httppost.setEntity(gzipStreamEntity);
 						}
 						if (headerParams != null) {
 							for (NameValuePair header : headerParams) {
 								httppost.setHeader(header.getName(), header.getValue());
 							}
 						}
 						response = httpclient.execute(httppost);
 					} else {
 						final HttpGet httpGet = new HttpGet(url + getParams);
 						if (headerParams != null) {
 							for (NameValuePair header : headerParams) {
 								httpGet.setHeader(header.getName(), header.getValue());
 							}
 						}
 						response = httpclient.execute(httpGet);
 					}
 					httpStatusCode = response.getStatusLine().getStatusCode();
 					switch (httpStatusCode) {
 						case 200:
 						case 201:
 						case 202:
 						case 203:
 						case 204:
 						case 205:
 						case 206:
 						case 207:
 							final InputStream is;
 							Header contentEncoding = response.getFirstHeader("Content-Encoding");
 							if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
 								byte[] inputStreamBuffer = new byte[8192];
 								int length;
 								ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 								GZIPInputStream gZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(
 										EntityUtils.toByteArray(response.getEntity())));
 								while ((length = gZIPInputStream.read(inputStreamBuffer)) >= 0) {
 									byteArrayOutputStream.write(inputStreamBuffer, 0, length);
 								}
 								is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
 								gZIPInputStream.close();
 								byteArrayOutputStream.close();
 							} else {
 								is = response.getEntity().getContent();
 							}
 							while ((readLength = is.read(buffer, 0, buffer.length)) != -1) {
 								data.write(buffer, 0, readLength);
 							}
 							data.flush();
 							isNetworkError = false;
 							break;
 						case 404:
 						case 403:
 						case 400:
 							isHttpStatusError = true;
 							isNetworkError = false;
 							break;
 						default:
 							KKDebug.w("connetion to " + url + getParams + " returns " + httpStatusCode);
 							retryTimes++;
 							isNetworkError = true;
 							SystemClock.sleep(1000);
 							break;
 					}
 					response.getEntity().consumeContent();
 				} catch (final Exception e) {
 					KKDebug.w("connetion to " + url + getParams + " failed!");
 					retryTimes++;
 					isNetworkError = true;
 					SystemClock.sleep(1000);
 				}
 			} while (isNetworkError && retryTimes < 3);
 		}
 		try {
 			if (!isNetworkError && !isHttpStatusError) {
 				if (listener != null) {
 					String jsonData;
 					if (cipher != null) {
 						jsonData = new String(cipher.doFinal(data.toByteArray()));
 					} else {
 						jsonData = data.toString();
 					}
 					listener.onPreComplete(jsonData);
 					if (context != null) {
 						try {
 							FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
 							OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
 							outputStreamWriter.write(jsonData);
 							outputStreamWriter.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		} catch (Exception e) {}
 		return null;
 	}
 
 	@Override
 	public void onPostExecute(Void v) {
 		if (listener == null) {
 			return;
 		}
 		if (isHttpStatusError) {
 			listener.onHttpStatusError(httpStatusCode);
 		} else if (isNetworkError) {
 			listener.onNetworkError();
 		} else {
 			listener.onComplete();
 		}
 	}
 }
