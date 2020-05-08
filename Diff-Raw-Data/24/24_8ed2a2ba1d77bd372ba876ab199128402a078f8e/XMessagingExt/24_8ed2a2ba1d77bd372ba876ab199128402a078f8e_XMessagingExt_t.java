 
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
 
 package com.polyvi.xface.extension.messaging;
 
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaInterface;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.PluginResult;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.telephony.SmsManager;
 import android.telephony.SmsMessage;
 
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XSystemEventCenter;
 
 import com.polyvi.xface.util.XUtils;
 
 public class XMessagingExt extends CordovaPlugin {
     private static final Uri mSMSContentUri = Uri.parse("content://sms/");
     private static final String FOLDERTYPE_DRAFT = "draft";
 
     private static final String MESSAGE_TYPE_SMS = "SMS";
     private static final String MESSAGE_TYPE_MMS = "MMS";
     private static final String MESSAGE_TYPE_EMAIL = "Email";
 
     private static final String COMMAND_SENDMESSAGE = "sendMessage";
     private static final String COMMAND_GETQUANTITIES = "getQuantities";
     private static final String COMMAND_GETMESSAGE = "getMessage";
     private static final String COMMAND_GETALLMESSAGES = "getAllMessages";
     private static final String COMMAND_FINDMESSAGES = "findMessages";
 
     private static final String SMS_SENT = "SMS_SENT";
 
     private Context mContext;
 
     private CallbackContext mCallbackContext;
 
     /** < 定义Intent的action，即需要的短信收到的action */
     private static final String INTENT_ACTION = "android.provider.Telephony.SMS_RECEIVED";
 
     private BroadcastReceiver mSendSMSBroadcastReceiver = null;
 
    private BroadcastReceiver mMsgReceiveBroadcaseReceiver = null;
 
     private enum SMS_RESULT_STATUS {
         SEND_SUCCESS, // 发送成功
         ERROR_GENERIC_FAILURE, // 通用错误
         ERROR_NO_SERVICE, // 无服务
         ERROR_NULL_PDU, // 没有PDU提供
         ERROR_RADIO_OFF, // 天线关闭
         NO_STATUS // 没有状态（用于native端的短信发送的默认状态）
     }
 
     @Override
     public void initialize(CordovaInterface cordova, CordovaWebView webView) {
         super.initialize(cordova, webView);
         mContext = cordova.getActivity();
        genMsgReceiveBroadcastReceiver();
         regMsgReceiver();
     }
 
    private void genMsgReceiveBroadcastReceiver() {
        if (null == mMsgReceiveBroadcaseReceiver) {
            mMsgReceiveBroadcaseReceiver = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     if (INTENT_ACTION.equals(intent.getAction())) {
                         Bundle bundle = intent.getExtras();
                         if (null != bundle) {
                             // 通过pdus获得接收到的所有短信消息，获取短信内容；
                             Object[] pdus = (Object[]) bundle.get("pdus");
                             // 构建短信对象数组
                             SmsMessage[] msgs = new SmsMessage[pdus.length];
                             for (int i = 0; i < pdus.length; i++) {
                                 // 获取单条短信内容，以pdu格式存,并生成短信对象；
                                 msgs[i] = SmsMessage
                                         .createFromPdu((byte[]) pdus[i]);
                             }
                             JSONArray receivedMsgs = buildSmsList(msgs);
                             XEvent evt = XEvent.createEvent(
                                     XEventType.MSG_RECEIVED,
                                     receivedMsgs.toString());
                             XSystemEventCenter.getInstance()
                                     .sendEventAsync(evt);
                         }
                     }
                 }
             };
         }
     }
 
     /**
      * 构建接收到的短信列表
      * */
     private JSONArray buildSmsList(SmsMessage[] msgs) {
         JSONArray smsList = new JSONArray();
         for (SmsMessage msg : msgs) {
             try {
                 JSONObject msgJsonObj = buildSmsJsonObj(msg);
                 smsList.put(msgJsonObj);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
         return smsList;
     }
 
     /**
      * 构建一条短信的json对象
      * */
     private JSONObject buildSmsJsonObj(SmsMessage msg) throws JSONException {
 
         JSONObject msgJsonObj = new JSONObject();
         msgJsonObj.put("msgId", "");
         msgJsonObj.put("subject", msg.getPseudoSubject());
         msgJsonObj.put("body", msg.getDisplayMessageBody());
         msgJsonObj.put("destinationAddresses", "");
         msgJsonObj
                 .put("originatingAddress", msg.getDisplayOriginatingAddress());
         msgJsonObj.put("messageType", "SMS");
         boolean isRead = false;
         if (SmsManager.STATUS_ON_ICC_READ == msg.getStatusOnIcc()) {
             isRead = true;
         }
         msgJsonObj.put("isRead", isRead);
         msgJsonObj.put("date", msg.getTimestampMillis());
         return msgJsonObj;
     }
 
     private void regMsgReceiver() {
        mContext.registerReceiver(mMsgReceiveBroadcaseReceiver,
                 new IntentFilter(INTENT_ACTION));
     }
 
     @Override
     public boolean execute(String action, JSONArray args,
             CallbackContext callbackContext) throws JSONException {
         mCallbackContext = callbackContext;
         try {
             if (action.equals(COMMAND_SENDMESSAGE)) {
                 sendMessage(args.getString(0), args.getString(1),
                         args.getString(2), args.getString(3));
             } else if (action.equals(COMMAND_GETQUANTITIES)) {
                 int numbers = getQuantities(args.getString(0),
                         args.getString(1));
                 callbackContext.success(numbers);
             } else if (action.equals(COMMAND_GETALLMESSAGES)) {
                 JSONArray messages = getAllMessages(args.getString(0),
                         args.getString(1));
                 callbackContext.success(messages);
             } else if (action.equals(COMMAND_GETMESSAGE)) {
                 JSONObject message = getMessage(args.getString(0),
                         args.getString(1), args.getInt(2));
                 callbackContext.success(message);
             } else if (action.equals(COMMAND_FINDMESSAGES)) {
                 JSONArray messages = findMessages(args.getJSONObject(0),
                         args.getString(1), args.getInt(2), args.getInt(3));
                 callbackContext.success(messages);
             }
             return true;
         } catch (IllegalArgumentException e) {
         } catch (Exception e) {
         }
         return false;
     }
 
     @Override
     public void onDestroy() {
         if (null != mSendSMSBroadcastReceiver) {
             mContext.unregisterReceiver(mSendSMSBroadcastReceiver);
             mSendSMSBroadcastReceiver = null;
         }
        if (null != mMsgReceiveBroadcaseReceiver) {
            mContext.unregisterReceiver(mMsgReceiveBroadcaseReceiver);
            mMsgReceiveBroadcaseReceiver = null;
         }
     }
 
     /**
      * 根据查询条件返回满足条件记录的条数
      */
     private int getRecordCount(Uri queryUri, String selection,
             String[] selectionArgs) throws InvalidParameterException {
         ContentResolver resolver = mContext.getContentResolver();
         Cursor cursor = resolver.query(queryUri,
                 new String[] { BaseColumns._ID }, selection, selectionArgs,
                 null);
         if (null == cursor) {
             throw new InvalidParameterException();
         }
         final int count = cursor.getCount();
         cursor.close();
         return count;
     }
 
     /**
      * 获取消息数量
      *
      * @param messageType
      *            信息类型（如MMS,SMS,Email）
      * @param folderType
      *            文件夹类型
      * @return 消息数量
      */
     @SuppressLint("DefaultLocale")
     private int getQuantities(String messageType, String folderType) {
         // TODO:这里只处理了短信，以后会有彩信和Email的操作
         if (null == folderType) {// folderType为null，表示草稿箱
             folderType = FOLDERTYPE_DRAFT;
         }
         folderType = folderType.toLowerCase();
         Uri uri = Uri.withAppendedPath(mSMSContentUri, folderType);
         // TODO:获取SIM卡内的信息
         final int count = getRecordCount(uri, null, null);
 
         return count;
     }
 
     /**
      * 发送消息
      *
      * @param webContext
      *            app应用，包含app的描述信息、状态及UI界面
      * @param messageType
      *            信息类型（如MMS,SMS,Email）
      * @param addr
      *            目的地址
      * @param body
      *            消息内容
      * @param subject
      *            消息主题
      * @return 返回状态结果
      */
     private void sendMessage(String messageType, String addr, String body,
             String subject) throws IllegalArgumentException {
         // send SMS
         if (messageType.equals(MESSAGE_TYPE_SMS)) {
             sendSMS(addr, body);
         }
         // send MMS
         else if (messageType.equals(MESSAGE_TYPE_MMS)) {
             mCallbackContext.success();
         }
         // send e-mail
         else if (messageType.equals(MESSAGE_TYPE_EMAIL)) {
             sendEmail(addr, body, subject);
         } else {
             throw new IllegalArgumentException("message type illegal");
         }
     }
 
     /**
      * 发送短信
      *
      * @param app
      *            app应用，包含app的描述信息、状态及UI界面
      * @param addr
      *            目的地址
      * @param body
      *            消息内容
      * @return 返回状态结果
      */
     private void sendSMS(String addr, String body) {
 
         String regularExpression = "[+*#\\d]+";
         if (!addr.matches(regularExpression)) {
             throw new IllegalArgumentException("address must be digit,*,# or +");
         }
 
         IntentFilter smsSendIntentFilter = new IntentFilter(SMS_SENT);
         genSendSMSBroadreceiver();
         // 注册发送短信的广播接收器
         mContext.registerReceiver(mSendSMSBroadcastReceiver,
                 smsSendIntentFilter);
 
         SmsManager manager = SmsManager.getDefault();
         ArrayList<String> textList = manager.divideMessage(body);
         ArrayList<PendingIntent> smsSendPendingIntentList = genSMSPendingIntentList(textList);
         manager.sendMultipartTextMessage(addr, null, textList,
                 smsSendPendingIntentList, null);
         PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
         result.setKeepCallback(true);
         mCallbackContext.sendPluginResult(result);
     }
 
     /**
      * 发送Email
      *
      * @param addr
      *            目的地址
      * @param body
      *            消息内容
      * @param subject
      *            消息主题
      * @return 返回状态结果
      */
     private void sendEmail(String addr, String body, String subject) {
         String aEmailList[] = { addr };
         Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
         emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
         emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
         emailIntent.setType("text/plain");
         emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
         mContext.startActivity(emailIntent);
        mCallbackContext.success();
     }
 
     /**
      * 构建短信发送广播接收器
      * */
     private void genSendSMSBroadreceiver() {
         if (null == mSendSMSBroadcastReceiver) {
             mSendSMSBroadcastReceiver = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context arg0, Intent arg1) {
                     // 作无效状态标记
                     SMS_RESULT_STATUS status = SMS_RESULT_STATUS.NO_STATUS;
                     switch (getResultCode()) {
                     case Activity.RESULT_OK:// 发送成功
                         status = SMS_RESULT_STATUS.SEND_SUCCESS;
                         break;
                     case SmsManager.RESULT_ERROR_GENERIC_FAILURE:// 通用错误
                         status = SMS_RESULT_STATUS.ERROR_GENERIC_FAILURE;
                         break;
                     case SmsManager.RESULT_ERROR_NO_SERVICE:// 无服务
                         status = SMS_RESULT_STATUS.ERROR_NO_SERVICE;
                         break;
                     case SmsManager.RESULT_ERROR_NULL_PDU:// 没有PDU提供
                         status = SMS_RESULT_STATUS.ERROR_NULL_PDU;
                         break;
                     case SmsManager.RESULT_ERROR_RADIO_OFF:// 天线关闭
                         status = SMS_RESULT_STATUS.ERROR_RADIO_OFF;
                         break;
                     }
                     resolveSMSSendResult(status);
                 }
             };
         }
     }
 
     /**
      * 构建短信的PendingIntent列表
      *
      * @param textList
      *            短信内容列表
      * @return 根据短信分段数量构建好的相应的短信pendingIntent列表
      * */
     private ArrayList<PendingIntent> genSMSPendingIntentList(
             ArrayList<String> textList) {
         Intent smsSendIntent = new Intent(SMS_SENT);
 
         int count = (null == textList) ? 0 : textList.size();
         ArrayList<PendingIntent> smsSendPendingIntentList = new ArrayList<PendingIntent>(
                 count);
 
         Iterator<String> itor = textList.iterator();
         while (itor.hasNext()) {
             smsSendPendingIntentList.add(PendingIntent.getBroadcast(mContext,
                     0, smsSendIntent, 0));
             itor.next();
         }
         return smsSendPendingIntentList;
     }
 
     /**
      * 处理短信发送的结果
      *
      * @param status
      *            短信发送结果状态
      * */
     private void resolveSMSSendResult(SMS_RESULT_STATUS status) {
         PluginResult.Status callbackStatus = PluginResult.Status.OK;
         if (status != SMS_RESULT_STATUS.SEND_SUCCESS) {
             callbackStatus = PluginResult.Status.ERROR;
         }
         PluginResult result = new PluginResult(callbackStatus, status.ordinal());
         result.setKeepCallback(false);
         mCallbackContext.sendPluginResult(result);
     }
 
     /**
      * 获取消息
      *
      * @param messageType
      *            信息类型（如MMS,SMS,Email）
      * @param folderType
      *            文件夹类型
      * @param index
      *            位置
      * @return 消息实体对象
      */
     private JSONObject getMessage(String messageType, String folderType,
             int index) throws JSONException {
         // TODO:这里只处理了短信，以后会有彩信和Email的操作
         if (null == folderType) {// folderType为null，表示草稿箱
             folderType = FOLDERTYPE_DRAFT;
         }
 
         JSONObject message = new JSONObject();
         try {
             // 需要获取的信息
             String[] projection = new String[] { "_id", "subject", "address",
                     "body", "date", "read" };
             folderType = folderType.toLowerCase();
             Uri uri = Uri.withAppendedPath(mSMSContentUri, folderType);
             ContentResolver resolver = mContext.getContentResolver();
             Cursor cursor = resolver.query(uri, projection, null, null,
                     "date desc");
 
             if (null == cursor) {
                 return message;
             }
             // 找到存放待读取短信的位置
             if (!cursor.moveToPosition(index)) {
                 cursor.close();
                 return message;
             }
             // TODO:获取SIM卡内的信息
             message = getMessageFromCursor(cursor);
             cursor.close();
 
         } catch (SQLiteException ex) {
             ex.printStackTrace();
         }
         return message;
     }
 
     /**
      * 获取所有消息
      *
      * @param messageType
      *            信息类型（如MMS,SMS,Email）
      * @param folderType
      *            文件夹类型
      * @return 消息实体对象数组
      */
     private JSONArray getAllMessages(String messageType, String folderType)
             throws JSONException {
         // TODO:这里只处理了短信，以后会有彩信和Email的操作
         if (null == folderType) {// folderType为null，表示草稿箱
             folderType = FOLDERTYPE_DRAFT;
         }
         JSONArray messages = new JSONArray();
         try {
             // 需要获取的信息
             String[] projection = new String[] { "_id", "subject", "address",
                     "body", "date", "read" };
             folderType = folderType.toLowerCase();
             Uri uri = Uri.withAppendedPath(mSMSContentUri, folderType);
             ContentResolver resolver = mContext.getContentResolver();
             Cursor cursor = resolver.query(uri, projection, null, null,
                     "date desc");
 
             if (null == cursor) {
                 return messages;
             }
             if (!cursor.moveToFirst()) {
                 cursor.close();
                 return messages;
             }
             do {
                 // TODO:获取SIM卡内的信息
                 JSONObject message = getMessageFromCursor(cursor);
                 messages.put(message);
             } while (cursor.moveToNext());
             cursor.close();
 
         } catch (SQLiteException ex) {
             ex.printStackTrace();
         }
         return messages;
     }
 
     /**
      * 查找指定的信息
      *
      * @param comparisonMsg
      *            要查找的信息
      * @param folderType
      *            文件夹类型
      * @param startIndex
      *            起始索引
      * @param endIndex
      *            结束索引
      * @return 消息实体对象数组
      */
     private JSONArray findMessages(JSONObject comparisonMsg, String folderType,
             int startIndex, int endIndex) throws JSONException {
         // TODO:这里只处理了短信，以后会有彩信和Email的操作
         if (null == folderType) {// folderType为null，表示草稿箱
             folderType = FOLDERTYPE_DRAFT;
         }
 
         ArrayList<String> projections = new ArrayList<String>();
         projections.add("_id");
         projections.add("subject");
         projections.add("address");
         projections.add("body");
         ArrayList<String> projectionsValue = new ArrayList<String>();
         projectionsValue.add(comparisonMsg.optString("messageId"));
         projectionsValue.add(comparisonMsg.optString("subject"));
         projectionsValue.add(comparisonMsg.optString("destinationAddresses"));
         projectionsValue.add(comparisonMsg.optString("body"));
 
         StringBuilder selection = XUtils.constructSelectionStatement(
                 projections, projectionsValue);
 
         int isRead = comparisonMsg.getInt("isRead");
         if (-1 != isRead) {
             if (null == selection) {
                 selection = new StringBuilder();
             } else {
                 selection.append(" AND ");
             }
             selection.append("read");
             selection.append("=");
             selection.append(isRead);
         }
         String selectionStr = null;
         if (null != selection) {
             selectionStr = selection.toString();
         }
 
         folderType = folderType.toLowerCase();
         Uri findUri = Uri.withAppendedPath(mSMSContentUri, folderType);
         JSONArray messages = new JSONArray();
         try {
             ContentResolver resolver = mContext.getContentResolver();
             Cursor cursor = resolver.query(findUri, null, selectionStr, null,
                     null);
             if (null == cursor) {
                 return messages;
             }
             int count = endIndex - startIndex + 1;
             if (cursor.moveToPosition(startIndex)) {
                 do {
                     JSONObject message = getMessageFromCursor(cursor);
                     messages.put(message);
                     count--;
                 } while (cursor.moveToNext() && count > 0);
             }
             cursor.close();
         } catch (SQLiteException ex) {
             ex.printStackTrace();
         }
         return messages;
     }
 
     /**
      * 读取message的数据
      */
     private JSONObject getMessageFromCursor(Cursor cursor) throws JSONException {
         // 通过关键字获取该关键字对应的信息
         String msgId = cursor.getString(cursor.getColumnIndex("_id"));
         String subject = cursor.getString(cursor.getColumnIndex("subject"));
         String smsBody = cursor.getString(cursor.getColumnIndex("body"));
         long date = cursor.getLong(cursor.getColumnIndex("date"));
         boolean isRead = cursor.getInt(cursor.getColumnIndex("read")) == 0 ? false
                 : true;
         String destAddress = cursor.getString(cursor.getColumnIndex("address"));
         JSONObject message = new JSONObject();
         message.put("messageId", msgId);
         message.put("subject", subject);
         message.put("body", smsBody);
         message.put("destinationAddresses", destAddress);
         message.put("messageType", MESSAGE_TYPE_SMS);
         message.put("date", date);
         message.put("isRead", isRead);
         return message;
     }
 }
