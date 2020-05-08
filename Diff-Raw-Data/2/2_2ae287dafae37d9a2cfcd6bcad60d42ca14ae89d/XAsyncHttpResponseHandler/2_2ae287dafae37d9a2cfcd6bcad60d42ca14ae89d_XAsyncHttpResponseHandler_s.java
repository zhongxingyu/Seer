 
 /*
  Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
  This program is distributed under the terms of the GNU General Public License.
 
  This file is part of xFace.
 
  xFace is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  xFace is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with xFace.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.polyvi.xface.http;
 
 import java.io.IOException;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.util.EntityUtils;
 
 import com.polyvi.xface.util.XLog;
 
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 
 /**
  * http响应体处理器
  *
  */
 public class XAsyncHttpResponseHandler {
 
     protected static final int SUCCESS_MESSAGE = 0;
     protected static final int FAILURE_MESSAGE = 1;
 
    private   static Handler mHandler;
 
     public XAsyncHttpResponseHandler() {
         if (Looper.myLooper() != null) {
             mHandler = new Handler() {
                 @Override
                 public void handleMessage(Message msg) {
                     XAsyncHttpResponseHandler.this.handleMessage(msg);
                 }
             };
         }
     }
 
     /**
      * 消息处理
      *
      * @param msg
      */
     protected void handleMessage(Message msg) {
         Object[] response;
         switch (msg.what) {
         case SUCCESS_MESSAGE:
             response = (Object[]) msg.obj;
             onSuccess(((Integer) response[0]).intValue(),
                     (Header[]) response[1], (String) response[2]);
             break;
         case FAILURE_MESSAGE:
             response = (Object[]) msg.obj;
             onFailure((Throwable) response[0], (String) response[1]);
         }
     }
 
     /**
      * 发送http response消息
      *
      * @param response
      */
     public void sendResponseMessage(HttpResponse response) {
         StatusLine status = response.getStatusLine();
         String responseBody = null;
         HttpEntity entity = null;
         HttpEntity temp = response.getEntity();
         if (temp != null) {
             try {
                 entity = new BufferedHttpEntity(temp);
                 responseBody = EntityUtils.toString(entity, "UTF-8");
             } catch (IOException e) {
                 sendFailureMessage(e, null);
             }
         }
         // 小于300的请求 都代表请求成功被服务器接收 理解
         if (status.getStatusCode() >= 300) {
             sendFailureMessage(new HttpResponseException(
                     status.getStatusCode(), status.getReasonPhrase()),
                     responseBody);
         } else {
             sendSuccessMessage(status.getStatusCode(),
                     response.getAllHeaders(), responseBody);
         }
 
     }
 
     /**
      * 成功回调 子类可以重载
      *
      * @param statusCode
      * @param headers
      * @param content
      */
     public void onSuccess(int statusCode, Header[] headers, String content) {
 
     }
 
     /**
      * 失败回调 子类可以重载
      *
      * @param error
      * @param content
      */
     public void onFailure(Throwable error, String content) {
 
     }
 
     /**
      * 发送成功消息
      *
      * @param statusCode
      *            http状态码
      * @param headers
      *            http响应头部
      * @param body
      *            http响应体
      */
     protected void sendSuccessMessage(int statusCode, Header[] headers,
             String body) {
         sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] {
                 new Integer(statusCode), headers, body }));
     }
 
     /**
      * 发送失败消息
      *
      * @param e
      *            失败异常对象
      * @param body
      *            失败的文本信息
      */
     protected void sendFailureMessage(Throwable e, String body) {
         sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { e, body }));
     }
 
     /**
      * 工具函数 用于发送消息
      *
      * @param msg
      */
     protected void sendMessage(Message msg) {
         if (mHandler != null) {
             mHandler.sendMessage(msg);
         } else {
             handleMessage(msg);
         }
     }
 
     /**
      * 工具函数 用于获得消息对象
      *
      * @param responseMessage
      * @param response
      * @return
      */
     protected Message obtainMessage(int responseMessage, Object response) {
         Message msg = null;
         if (mHandler != null) {
             msg = this.mHandler.obtainMessage(responseMessage, response);
         } else {
             msg = Message.obtain();
             msg.what = responseMessage;
             msg.obj = response;
         }
         return msg;
     }
 
     /**
      * 标示是否是异步响应
      *
      * @return
      */
     public boolean isAsync() {
         return true;
     }
 }
