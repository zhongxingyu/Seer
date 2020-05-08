 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package com.cloudfoundry.bae.cloudpush;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 /**
  * Channel
  * 
  * <p>Channel类提供百度云消息通道服务的Java版本SDK，用户首先实例化这个类，设置自己的access_token，
  * 即可使用百度云消息通道服务</p>
  * 
  * @author zhuanght(zhuang.hai.tao@163.com)
  * @version  0.0.1
  */
 public class Channel extends BaeBase {
     private static final Log logger = LogFactory.getLog(Channel.class);
     /*
 	 * 可选参数的KEY
 	 * 
 	 * 用户关注：是
 	 * 在调用Channel类的SDK方法时，根据用户的个性化需要，可能需要传入可选参数，而可选参数需要放在关联数组$optional中传入，
 	 * 这里定义了$optional数组可用的KEY
 	 */
     
     /**
      * 发起请求时的时间戳
      */
     public static final String TIMESTAMP = "timestamp";
     
     /**
      * 请求过期的时间
      * 如果不填写，默认为10分钟
      */
     public static final String EXPIRES = "expires";
     
     /**
      * API版本号
      * 用户一般不需要关注此项
      */
     public static final String VERSION = "v";
     
     /**
      * 消息通道ID号
      */
     public static final String CHANNEL_ID = "channel_id";
     
     /**
      * 用户ID的类型
      * 0：百度用户标识对称加密；1：百度用户标识明文
      */
     public static final String USER_TYPE = "user_type";
     
     /**
 	 * 用户ID的类型
 	 * 
 	 * 0：百度用户标识对称加密串；1：百度用户标识明文
 	 * 
 	 */
     public static final String DEVICE_TYPE = "device_type";
     
     /**
 	 * 第几页
 	 * 
 	 * 批量查询时，需要指定start，默认为第0页
 	 * 
 	 */
 	public static final String START = "start";
 	/**
 	 * 每页多少条记录
 	 * 
 	 * 批量查询时，需要指定limit，默认为100条
 	 * 
 	 */
 	public static final String LIMIT = "limit";
     
     
     /**
 	 * 消息ID json字符串
 	 * 
 	 */
 	public static final String MSG_IDS = "sg_ids";
 	public static final String MSG_KEYS = "msg_keys";
 	public static final String IOS_MESSAGES = "ios_messages";
 	public static final String WP_MESSAGES = "wp_messages";
     
     /**
 	 * 消息类型
 	 * 
 	 * 扩展类型字段，0：默认类型
 	 * 
 	 */
 	public static final String MESSAGE_TYPE = "message_type";
 	/**
 	 * 消息超时时间
 	 * 
 	 */
 	public static final String MESSAGE_EXPIRES = "message_expires";
     
     /**
      * 消息标签名称
      * 
      */
     public static final String TAG_NAME = "tag";
     
     /**
      * 消息标签描述
      * 
      */
     public static final String TAG_INFO = "info";
     
     /**
      * 消息标签id
      * 
      */
     public static final String TAG_ID = "tid";
     
     /**
      * 封禁时间
      * 
      */
     public static final String BANNED_TIME = "banned_time";
     
     /**
      * 回调域名
      * 
      */
     public static final String CALLBACK_DOMAIN = "domain";
     
     /**
      * 回调uri
      * 
      */
     public static final String CALLBACK_URI = "uri";
     
     
     /**
 	 * Channel常量
 	 * 
 	 * 用户关注：否
 	 */
 	public static final String APPID = "appid";
 	public static final String ACCESS_TOKEN = "access_token";
 	public static final String API_KEY = "apikey";
 	public static final String SECRET_KEY = "secret_key";
 	public static final String SIGN = "sign";
 	public static final String METHOD = "method";
 	public static final String HOST = "host";
 	public static final String USER_ID = "user_id";
 	public static final String MESSAGES = "messages";
 	public static final String PRODUCT = "channel";
 	
 	public static final String DEFAULT_HOST = "channel.api.duapp.com";
 	public static final String NAME = "name";
 	public static final String DESCRIPTION = "description";
 	public static final String CERT = "cert"; 
 	public static final String RELEASE_CERT = "release_cert";
 	public static final String DEV_CERT = "dev_cert";
 	public static final String PUSH_TYPE = "push_type";
     
     
     
     /**
 	 * Channel私有变量
 	 * 
 	 * 用户关注：否
 	 */
 	private String apiKey = null;
 	private String secretKey = null;
 	private int requestId = 0;
     private int CONN_TIMEOUT = 30;
     private ConnectionOption connOption = ConnectionOption.DEFAULT;
 
 	public static final int PUSH_TO_USER = 1;
 	public static final int PUSH_TO_TAG = 2;
 	public static final int PUSH_TO_ALL = 3;
 	public static final int PUSH_TO_DEVICE = 4;
     
     
     /**
 	 * Channel 错误常量
 	 * 
 	 * 用户关注：否
 	 */
 	public static final int CHANNEL_SDK_SYS = 1;
 	public static final int CHANNEL_SDK_INIT_FAIL = 2;
 	public static final int CHANNEL_SDK_PARAM = 3;
 	public static final int CHANNEL_SDK_HTTP_STATUS_ERROR_AND_RESULT_ERROR = 4;
 	public static final int CHANNEL_SDK_HTTP_STATUS_OK_BUT_RESULT_ERROR = 5;
     
     /**
 	 * 错误常量与错误字符串的映射
 	 * 
 	 * 用户关注：否
 	 */
 	private String[] arrayErrorMap = {"php sdk error", "php sdk error", "lack param",
             "http status is error, and the body returned is not a json string",
             "http status is ok, but the body returned is not a json string"
         };
 
     /**
 	 * 用户关注：是
 	 * 对象构造方法，用户传入apiKey与secretKey进行初始化
      * @param string apiKey
 	 * @param string secretKey
 	 * @param array arr_curlOpts 可选参数
      * @throws ChannelException 如果出错，则抛出异常，异常号是CHANNEL_SDK_INIT_FAIL
 	*/
     public void initialize(String apiKey, String secretKey, ConnectionOption option) {
         if (checkString(apiKey, 1, 64)) {
             this.apiKey = apiKey;
         }
         else {
             throw new ChannelException("invalid param - apiKey["+apiKey+"],"
                     + "which must be a 1 - 64 length string",
                     CHANNEL_SDK_INIT_FAIL );
         }
         
         if (checkString(secretKey, 1, 64)) {
             this.secretKey = secretKey;
         }
         else {
             throw new ChannelException("invalid param - secretKey["+secretKey+"],"
                     + "which must be a 1 - 64 length string",
                     CHANNEL_SDK_INIT_FAIL );
         }
         
         if (option != null) {
             this.connOption = option;
         }
         
         resetErrorStatus();
     }
     
     /**
 	 * setApiKey
 	 * 
 	 * 用户关注：是
 	 * 服务类方法， 设置Channel对象的apiKey属性，如果用户在创建Channel对象时已经通过参数设置了apiKey，这里的设置将会覆盖以前的设置
 	 * 
 	 * @param string apiKey
 	 * @return 成功：true，失败：false
 	 * 
 	 * @version 
 	 */
     public boolean setApiKey(String apiKey) {
         this.resetErrorStatus();
         
         try {
             if (this.checkString(apiKey, 1, 64)) {
                 this.apiKey = apiKey;
             }
             else {
                 throw new ChannelException("invaid apiKey ( "+ apiKey + " ), which must be a 1 - 64 length string", CHANNEL_SDK_INIT_FAIL);
             }
         } 
         catch (ChannelException ex) {
             this.channelExceptionHandler(ex);
             return false;
         }
         return true;
     }
     /**
 	 * setSecretKey
 	 * 
 	 * 用户关注：是
 	 * 服务类方法， 设置Channel对象的secretKey属性，如果用户在创建Channel对象时已经通过参数设置了secretKey，这里的设置将会覆盖以前的设置
 	 * 
 	 * @access public
 	 * @param string $secretKey
 	 * @return 成功：true，失败：false
 	 * 
 	 * @version 
 	 */
     public boolean setSecretKey(String secretKey) {
         this.resetErrorStatus();
         
         try {
             if (this.checkString(secretKey, 1, 64)) {
                 this.secretKey = secretKey;
             }
             else {
                 throw new ChannelException("invaid secretKey ( "+ secretKey + " ), which must be a 1 - 64 length string", CHANNEL_SDK_INIT_FAIL);
             }
         } 
         catch (ChannelException ex) {
             this.channelExceptionHandler(ex);
             return false;
         }
         return true;
     }
     /**
 	 * getRequestId
 	 * 
 	 * 用户关注：是
 	 * 服务类方法，获取上次调用的request_id，如果SDK本身错误，则直接返回0
 	 * 
 	 * @return 上次调用服务器返回的request_id
 	 * 
 	 * @version 1.0.0.0
 	 */
 	public int getRequestId (  )
 	{
 		return this.requestId;
 	}
     
     /**
 	 * queryBindList
 	 * 
 	 * 用户关注：是
 	 * 
 	 * 供服务器端根据userId[、channelId]查询绑定信息
 	 * 
 	 * @access public
 	 * @param string userId 用户ID号
 	 * @param array optional 可选参数，支持的可选参数包括：CHANNEL_ID、DEVICE_TYPE、START、LIMIT
 	 * @return 成功：JSONObject；失败：null
 	 * 
 	 * @version 1.0.0.0
 	 */
 	public JSONObject queryBindList (String userId, Map<String, String> optional) 
 	{
 		this.resetErrorStatus( );
 		try 
 		{
             Map<String, String> args = new HashMap<String, String>(optional);
             args.put(USER_ID, userId);
             String[] needArray = {USER_ID};
             args = prepareArgs(needArray, args);
             args.put(METHOD, "query_bindlist");
 			return this.commonProcess(args);
 		} 
 		catch (ChannelException ex ) 
 		{
 			this.channelExceptionHandler (ex);
 			return null; 
 		}
 	}
     
     /**
 	 * pushMessage
 	 * 用户关注： 是
 	 * 根据pushType, messages, message_type, [optinal] 推送消息
 	 * @access public
 	 * @param int pushType 推送类型 取值范围 1-4, 1:单人，2：一群人tag， 3：所有人， 4：设备
 	 * @param string messages 要发送的消息，如果是数组格式，则会自动做json_encode;如果是json格式给出，必须与msgKeys对应起来;
      * @param array optional 可选参数,如果pushType为单人，必须指定USER_ID(例:optional[USER_ID] = "xxx"),
 	 *		如果pushType为tag，必须指定TAG,
 	 * 		其他可选参数：MSG_KEYS 发送的消息key，如果是数组格式，则会自动做json_encode，必须与messages对应起来;
 	 *		MESSAGE_TYPE 消息类型，取值范围 0-1, 0:消息（透传），1：通知，默认为0
 	 *		还可指定MESSAGE_EXPIRES, MESSAGE_EXPIRES, CHANNLE_ID等
 	 *
 	 * @return 成功：JSONObject；失败:null
 	 * @version 2.0.0.0
 	*/
     public JSONObject pushMessage(int pushType, String messages, String msgKeys, Map<String, String> optional) {
         this.resetErrorStatus();
         
         try {
             Map<String, String> args = new HashMap<String, String>(optional);
             args.put(PUSH_TYPE, String.valueOf(pushType));
             args.put(MESSAGES, messages);
             args.put(MSG_KEYS, msgKeys);
 
             String[] needArray = {PUSH_TYPE, MESSAGES, MSG_KEYS};
             args = prepareArgs(needArray, args);
             args.put(METHOD, "push_msg");
 
             switch (pushType) {
                 case PUSH_TO_USER: 
                     if (!args.containsKey(USER_ID) || args.get(USER_ID) == null || args.get(USER_ID).isEmpty()) {
                         throw new ChannelException("userId should be specified in optional when pushType is PUSH_TO_USER", CHANNEL_SDK_PARAM);
                     }
                     break;
                 case PUSH_TO_TAG:
                     if (!args.containsKey(TAG_NAME) || args.get(TAG_NAME) == null || args.get(TAG_NAME).isEmpty()) {
                         throw new ChannelException("tag should be specified in optional[] when pushType is PUSH_TO_TAG", CHANNEL_SDK_PARAM);
                     }
                     break;
                 case  PUSH_TO_ALL:
                     break;
                 case PUSH_TO_DEVICE:
                     if (!args.containsKey(CHANNEL_ID)) {
                         throw new ChannelException("channelId should be specified in optional[] when pushType is PUSH_TO_DEVICE", CHANNEL_SDK_PARAM);
                     }
                     break;
                 default:
                     throw new ChannelException("pushType value is not supported or not specified", CHANNEL_SDK_PARAM);
             }
             
             return this.commonProcess(args);
         }
         catch (ChannelException ex) {
             this.channelExceptionHandler (ex);
 			return null; 
         }
     }
     
     /**
 	 * checkString
 	 *  
 	 * 用户关注：否
 	 * 
 	 * 检查参数是否是一个大于等于min且小于等于max的字符串
 	 * 
 	 * @param string str 要检查的字符串
 	 * @param int min 字符串最小长度
 	 * @param int max 字符串最大长度
 	 * @return 成功：true；失败：false
 	 * 
 	 * @version 1.0.0.0
 	 */
 	private boolean checkString(String str, int min, int max)
 	{
 		if (str!=null && str.length() >= min && str.length() <= max) {
 			return true;
 		}
 		return false;
 	}
     
     
     /**
 	 *   
 	 * 用户关注：否
 	 * 
 	 * 异常处理方法
 	 * 
 	 * @access protected
 	 * @param Excetpion $ex 异常处理函数，主要是填充Channel对象的错误状态信息
 	 * 
 	 * @version 1.0.0.0
 	 */
 	private void channelExceptionHandler(ChannelException ex) {
         int tmpCode = ex.code;
         if (0 == tmpCode) {
             tmpCode = CHANNEL_SDK_SYS;
         }
         
         this.errcode = tmpCode;
         if (this.errcode >= 30000) {
             this.errmsg = ex.getMessage();
         }
         else {
             this.errmsg = arrayErrorMap[this.errcode] ;
         }
 	}
     
     /**
 	 * resetErrorStatus
 	 *   
 	 * 用户关注：否
 	 * 
 	 * 恢复对象的错误状态，每次调用服务类方法时，由服务类方法自动调用该方法
 	 * 
 	 * @version 1.0.0.0
 	 */
 	private void resetErrorStatus()
 	{
 		this.errcode = 0;
 		this.errmsg = this.arrayErrorMap[errcode];
 		this.requestId = 0;
 	}
     
     /**
 	 * commonProcess
 	 *   
 	 * 用户关注：否
 	 * 
 	 * 所有服务类SDK方法的通用过程
 	 * 
 	 * @access protected
 	 * @param map paramOpt 参数数组
 	 * @throws ChannelException 如果出错，则抛出异常
 	 * 
 	 * @version 1.0.0.0
 	 */
     private JSONObject commonProcess(Map<String, String> paramOpt) {
         adjustOpt(paramOpt);
         ResponseCore ret = baseControl(paramOpt);
         
         if (ret == null) {
             throw new ChannelException("base control returned empty object", CHANNEL_SDK_SYS);
         }
         
         if (ret.isOK()) {
             try {
                 JSONObject result = new JSONObject(ret.body);
                 this.requestId = result.getInt("request_id");
                 return result;
             } catch (JSONException ex) {
                 throw new ChannelException(ret.body, CHANNEL_SDK_HTTP_STATUS_OK_BUT_RESULT_ERROR);
             }
         }
         else {
             try {
                 JSONObject result = new JSONObject(ret.body);
                 this.requestId = result.getInt("request_id");
                 throw new ChannelException(result.getString("error_msg"), result.getInt("error_code"));
             } catch (JSONException ex) {
                 throw new ChannelException("ret body:" + ret.body, CHANNEL_SDK_HTTP_STATUS_ERROR_AND_RESULT_ERROR);
             }
         }
     }
 
     
     /**
 	 * prepareArgs
 	 *   
 	 * 用户关注：否
 	 * 
 	 * 合并传入的参数到一个数组中，便于后续处理
 	 * 
 	 * @access protected
 	 * @param array arrNeed 必须的参数KEY
 	 * @param map tmpArgs 参数数组
 	 * @throws ChannelException 如果出错，则抛出异常，异常号为self::Channel_SDK_PARAM 
 	 * 
 	 * @version 1.0.0.0
 	 */
     public Map<String, String> prepareArgs(String[] arrNeed, Map<String, String> tmpArgs) {
         Map<String, String> args;
         
         if (null == tmpArgs || tmpArgs.isEmpty()) {
             args = new HashMap<String, String>();
             return args;
         }
         
         if (null != arrNeed && arrNeed.length > 0 && tmpArgs.isEmpty()) {
             String keys = "(";
             for (String key : arrNeed) {
                 keys += key + ",";
             }
             keys += ")";
             throw new ChannelException("invalid sdk params, params" + keys + "are needed",CHANNEL_SDK_PARAM);
         }
         if (null != arrNeed) {
             for (String key : arrNeed) {
                 if (!tmpArgs.containsKey(key)) {
                     throw new ChannelException("lack param (" + key + ")", CHANNEL_SDK_PARAM);
                 }
             }
         }
         
         args = new HashMap<String, String>(tmpArgs);
         if (args.containsKey(CHANNEL_ID)) {
             String channelIDValue = (String)args.get(CHANNEL_ID);
             args.put(CHANNEL_ID, channelIDValue);
 //            URLCodec codec = new URLCodec("utf8");
 //            String channelIDValue = (String)args.get(CHANNEL_ID);
 //            try {
 //                args.put(CHANNEL_ID, codec.encode(channelIDValue));
 //            } catch (EncoderException ex) {
 //                throw new ChannelException("bad channel_id (" + channelIDValue + ")", CHANNEL_SDK_PARAM);
 //            }
         }
         
         return args;
     }
     
     /**
 	 * adjustOpt
 	 *   
 	 * 用户关注：否
 	 * 
 	 * 参数调整方法
 	 * 
 	 * @param Map opt 参数map
 	 * @throws ChannelException 如果出错，则抛出异常，异常号为 CHANNEL_SDK_PARAM
 	 * 
 	 * @version 1.0.0.0
 	 */
 	private void adjustOpt(Map<String, String> opt) {
 		if (null == opt || opt.isEmpty()) {
             throw new ChannelException("no params are set", CHANNEL_SDK_PARAM);
         }
         
         if (!opt.containsKey(TIMESTAMP)) {
             String timestamp = String.valueOf(System.currentTimeMillis()/1000);
             opt.put(TIMESTAMP, timestamp);
         }
         
         opt.put(HOST, DEFAULT_HOST);
         opt.put(API_KEY, apiKey);
         
         if (opt.containsKey(SECRET_KEY)) {
             opt.remove(SECRET_KEY);
         }
 	}
     
     /**
 	 * _baseControl
 	 *   
 	 * 用户关注：否
 	 * 
 	 * 网络交互方法
 	 * 
 	 * @param Map opt 参数数组
 	 * @throws ChannelException 如果出错，则抛出异常，错误号为CHANNEL_SDK_SYS
 	 * 
 	 * @version 1.0.0.0
 	 */
 	private ResponseCore baseControl(Map<String, String> opt) {
         StringBuilder content = new StringBuilder();
         String resource = "channel";
         if (opt.containsKey(CHANNEL_ID) && opt.get(CHANNEL_ID)!=null) {
             resource = opt.get(CHANNEL_ID);
             opt.remove(CHANNEL_ID);
         }
         
         String host = opt.get(HOST);
         opt.remove(HOST);
         
         String url = "http://" + host + "/rest/2.0/" + PRODUCT + "/";
         url += resource;
         HttpMethod httpMethod = HttpMethod.HTTP_POST;
         String sign = genSign(httpMethod.toString(), url, opt);
         opt.put(SIGN, sign);
         
         Set<String> keys  = opt.keySet();
         for (String key : keys) {
             try {
                 //            try {
                 ////                key = new URLCodec("utf8").encode(key);
                 ////                String v = new URLCodec("utf8").encode(opt.get(key));
                 //                content.append(key).append("=").append(opt.get(key)).append("&");
                 //            } catch (EncoderException ex) {
                 //                Logger.getLogger(Channel.class.getName()).log(Level.SEVERE, null, ex);
                 //            }
                 String v = URLEncoder.encode(opt.get(key), "utf8");
                 content.append(key).append("=").append(v).append("&");
             } catch (UnsupportedEncodingException ex) {
                 Logger.getLogger(Channel.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         String postContent = content.toString();
         postContent = postContent.substring(0, postContent.length()-1);
         logger.info("content = " + postContent);
         logger.info("url = " + url);
         
         RequestCore request = new RequestCore(url);
         Map<String, String> headers = new HashMap<String, String>();
         headers.put("Content-Type", "application/x-www-form-urlencoded");
         headers.put("User-Agent", "Baidu Channel Service Javasdk Client");
         Set<String> headerKeySet = headers.keySet();
         for (String headerKey : headerKeySet) {
             String headerValue = headers.get(headerKey);
             request.addHeader(headerKey, headerValue);
         }
         request.setMethod(httpMethod);
         request.setBody(postContent);
         request.setConnectionOption(connOption);
         request.sendRequest();
         
         return new ResponseCore(request.getResponseHeader(), 
                 request.getResponseBody(),
                 request.getResponseCode());
     }
 
     /**
 	 * genSign
 	 *
 	 *用户关注： 否
 	 *
 	 * 根据method, url, 参数内容 生成签名
 	*/
 	private String genSign(String method, String url, Map<String, String> opt) {
         String gather = method + url;
         TreeMap<String, String> sortOpt = new TreeMap<String, String>(opt);
         NavigableSet<String> keySet = sortOpt.navigableKeySet();
         Iterator<String> it = keySet.iterator();
         while (it.hasNext()) {
             String key = it.next();
             String value = sortOpt.get(key);
             gather += key + "=" + value;
         }
         
         gather += secretKey;
         
         logger.info("sign source content: " + gather);
         
         String encodedGather;
         try {
 //            encodedGather = new URLCodec("utf8").encode(gather);
             encodedGather = URLEncoder.encode(gather, "utf8");
         } catch (UnsupportedEncodingException ex) {
            throw new ChannelException("wront params are seted: " + gather, CHANNEL_SDK_PARAM);
         }
         String sign = DigestUtils.md5Hex(encodedGather);
         
         return sign;
 	}
 }
