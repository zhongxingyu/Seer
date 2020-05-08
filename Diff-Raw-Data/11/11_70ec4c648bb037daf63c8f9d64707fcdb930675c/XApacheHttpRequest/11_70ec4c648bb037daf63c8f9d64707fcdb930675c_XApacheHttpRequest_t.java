 package com.xengine.android.session.http.apache;
 
 import android.text.TextUtils;
 import com.xengine.android.session.http.XBaseHttpRequest;
 import org.apache.http.HttpRequest;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tujun
  * Date: 13-9-3
  * Time: 下午6:13
  * To change this template use File | Settings | File Templates.
  */
 class XApacheHttpRequest extends XBaseHttpRequest {
 
     private XHttpTransferListener mListener;
 
     protected XApacheHttpRequest() {
         super();
     }
 
     protected XApacheHttpRequest setListener(XHttpTransferListener listener) {
         this.mListener = listener;
         return this;
     }
 
     public HttpRequest toApacheHttpRequest() {
         HttpUriRequest request = null;
         switch (getMethod()) {
             case GET:
                 request = createHttpGet();
                 break;
             case POST:
                 request = createHttpPost();
                 break;
             case PUT:
                 request = createHttpPut();
                 break;
             case DELETE:
                 request = createHttpDelete();
                 break;
         }
         return request;
     }
 
     private HttpGet createHttpGet() {
         if (TextUtils.isEmpty(getUrl()))
             return null;
 
         HttpGet httpGet = new HttpGet(getUrl());
         if (getHeaders() != null) {
             for (Map.Entry<String, String> header : getHeaders().entrySet())
                httpGet.setHeader(header.getKey(), header.getValue());
         }
         return httpGet;
     }
 
     private HttpPost createHttpPost() {
         if (TextUtils.isEmpty(getUrl()))
             return null;
 
         HttpPost httpPost = new HttpPost(getUrl());
         if (getHeaders() != null) {
             for (Map.Entry<String, String> header : getHeaders().entrySet())
                httpPost.setHeader(header.getKey(), header.getValue());
         }
         // 含有上传文件
         if (getFileParams() != null) {
             XMultipartEntity reqEntity = new XMultipartEntity
                     (HttpMultipartMode.BROWSER_COMPATIBLE, mListener);
             try {
                 for (Map.Entry<String, File> fileParam : getFileParams().entrySet())
                     reqEntity.addPart(fileParam.getKey(), new FileBody(fileParam.getValue()));
                 if (getStringParams() != null) {
                     for (Map.Entry<String, String> strParam : getStringParams().entrySet()) {
                         if (getCharset() != null)
                             reqEntity.addPart(strParam.getKey(),
                                     new StringBody(strParam.getValue(), Charset.forName(getCharset())));
                         else
                             reqEntity.addPart(strParam.getKey(),
                                     new StringBody(strParam.getValue()));
                     }
                 }
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
             httpPost.setEntity(reqEntity);
         }
         // 不含上传文件，只有字符串参数
         else {
             if (getStringParams() != null) {
                 List<NameValuePair> params = new ArrayList<NameValuePair>();
                 for (Map.Entry<String, String> strParam : getStringParams().entrySet())
                     params.add(new BasicNameValuePair(strParam.getKey(), strParam.getValue()));
                 try {
                     if (getCharset() != null)
                         httpPost.setEntity(new UrlEncodedFormEntity(params, getCharset()));
                     else
                         httpPost.setEntity(new UrlEncodedFormEntity(params));
                 } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();
                 }
             }
         }
         return httpPost;
     }
 
     private HttpPut createHttpPut() {
         if (TextUtils.isEmpty(getUrl()))
             return null;
 
         HttpPut httpPut = new HttpPut(getUrl());
         if (getHeaders() != null) {
             for (Map.Entry<String, String> header : getHeaders().entrySet())
                httpPut.setHeader(header.getKey(), header.getValue());
         }
         // 含有上传文件
         if (getFileParams() != null) {
             MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
             try {
                 for (Map.Entry<String, File> fileParam : getFileParams().entrySet())
                     reqEntity.addPart(fileParam.getKey(), new FileBody(fileParam.getValue()));
                 if (getStringParams() != null) {
                     for (Map.Entry<String, String> strParam : getStringParams().entrySet()) {
                         if (getCharset() != null)
                             reqEntity.addPart(strParam.getKey(),
                                     new StringBody(strParam.getValue(), Charset.forName(getCharset())));
                         else
                             reqEntity.addPart(strParam.getKey(),
                                     new StringBody(strParam.getValue()));
                     }
                 }
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
             httpPut.setEntity(reqEntity);
         }
         // 不含上传文件，只有字符串参数
         else {
             if (getStringParams() != null) {
                 List<NameValuePair> params = new ArrayList<NameValuePair>();
                 for (Map.Entry<String, String> strParam : getStringParams().entrySet())
                     params.add(new BasicNameValuePair(strParam.getKey(), strParam.getValue()));
                 try {
                     if (getCharset() != null)
                         httpPut.setEntity(new UrlEncodedFormEntity(params, getCharset()));
                     else
                         httpPut.setEntity(new UrlEncodedFormEntity(params));
                 } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();
                 }
             }
         }
         return httpPut;
     }
 
     private HttpDelete createHttpDelete() {
         if (TextUtils.isEmpty(getUrl()))
             return null;
 
         HttpDelete httpDelete = new HttpDelete(getUrl());
         if (getHeaders() != null) {
             for (Map.Entry<String, String> header : getHeaders().entrySet())
                httpDelete.setHeader(header.getKey(), header.getValue());
         }
         return httpDelete;
     }
 }
