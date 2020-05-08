 /**
  * 
  */
 package me.yumin.android.common.domain.enumtype.impl;
 
 import me.yumin.android.common.domain.enumtype.IEnvEnum;
 
 /**
  * @author yumin
  * 
  */
 public enum ApiEnum implements IEnvEnum {
 
 	// 自动登录
 	TOP_AUTO_LOGIN_V2(
 		"http://api.m.taobao.com/rest/api2.do?api=com.taobao.client.sys.autoLogin&v=v2",
		"http://api.waptest.taobao.com/rest/api2.do?api=com.taobao.client.sys.autoLogin&v=v2"
 	),
 	// 获取令牌
 	TOP_GET_APP_TOKEN(
 		"http://api.m.taobao.com/rest/api2.do?api=com.taobao.client.sys.getAppToken&v=*",
		"http://api.waptest.taobao.com/rest/api2.do?api=com.taobao.client.sys.getAppToken&v=*"
 	),
 	// 登录淘宝
 	TOP_LOGIN_V2(
 		"http://api.m.taobao.com/rest/api2.do?api=com.taobao.client.sys.login&v=v2", 
		"http://api.waptest.taobao.com/rest/api2.do?api=com.taobao.client.sys.login&v=v2"
 	),
 	// 核销校验
 	TOP_BEFORE_CONSUME(
 		"http://api.m.taobao.com/rest/api2.do?api=mtop.eticket.wirelessbeforeconsume&v=1.0", 
		"http://api.waptest.taobao.com/rest/api2.do?api=mtop.eticket.wirelessbeforeconsume&v=1.0"
 	),
 	// 执行核销
 	TOP_CONSUME(
 		"http://api.m.taobao.com/rest/api2.do?api=mtop.eticket.wirelessConsume&v=1.0", 
		"http://api.waptest.taobao.com/rest/api2.do?api=mtop.eticket.wirelessConsume&v=1.0"
 	);
 
 	/**
      * 
      */
 	private String prodUrl;
 	private String testUrl;
 
 	/**
 	 * 
 	 * @param prodUrl
 	 * @param testUrl
 	 */
 	ApiEnum(String prodUrl, String testUrl) {
 
 		this.prodUrl = prodUrl;
 		this.testUrl = testUrl;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	@Override
 	public String prodValue() {
 		return this.prodUrl;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	@Override
 	public String testValue() {
 		return this.testUrl;
 	}
 }
