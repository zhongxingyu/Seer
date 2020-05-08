 package com.apple;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.SAXException;
 
 import com.apple.constants.Goods;
 import com.apple.entity.AppleOrder;
 import com.meterware.httpunit.ClientProperties;
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.HttpUnitOptions;
 import com.meterware.httpunit.PostMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebResponse;
 
 public class User {
 	private final WebConversation wc;
 	private String appleId = "hujiag8@eyou.com";
 	private String password = "pengyan";
 	private String phoneNumber = "13482379270";
 	private String email = "hujiag8@eyou.com";
 	/** 名 */
 	private String firstName = "佳佳";
 	/** 姓 */
 	private String lastName = "胡";
 	private String state = "上海";
 	private String city = "上海";
 	private String district = "徐汇区";
 	private String street = "苍梧路468弄6号602室";
 	private String postalCode = "200000";
 	
 	private AppleOrder order = new AppleOrder();
 	
 	public User(){
 		System.setProperty("javax.net.ssl.trustStore",Config.TRUST_STORE);
 		wc = new WebConversation();
 	}
 	
 	
 	public AppleOrder getOrder() {
 		return order;
 	}
 
 
 	public void setOrder(AppleOrder order) {
 		this.order = order;
 	}
 
 
 	public String getAppleId() {
 		return appleId;
 	}
 
 	public void setAppleId(String appleId) {
 		this.appleId = appleId;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 	public void setPhoneNumber(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 	
 	public String getState() {
 		return state;
 	}
 
 	public void setState(String state) {
 		this.state = state;
 	}
 
 	public String getCity() {
 		return city;
 	}
 
 	public void setCity(String city) {
 		this.city = city;
 	}
 
 	public String getDistrict() {
 		return district;
 	}
 
 	public void setDistrict(String district) {
 		this.district = district;
 	}
 
 	public String getStreet() {
 		return street;
 	}
 
 	public void setStreet(String street) {
 		this.street = street;
 	}
 
 	public String getPostalCode() {
 		return postalCode;
 	}
 
 	public void setPostalCode(String postalCode) {
 		this.postalCode = postalCode;
 	}
 
 	public void buy(int i) throws Exception{
 		switch(i) {
 		case 1:
 			this.getOrder().setGoodsName(Goods.IPHONE_3GS);
 			break;
 		case 2:
 			this.getOrder().setGoodsName(Goods.IPHONE4_16G_BLACK);
 			break;
 		case 3:
 			this.getOrder().setGoodsName(Goods.IPHONE4_32G_BLACK);
 			break;
 		}
 		
 		Logs.getLogger().info("start");
 		openApple(i);
 		Logs.getLogger().info("addToCart");
 		addToCart();
 		Logs.getLogger().info("checkoutx");
 		checkoutx();
 		Logs.getLogger().info("invoice");
 		invoice();
 		Logs.getLogger().info("checkoutxForShip");
 		checkoutxForShip();
 		Logs.getLogger().info("status");
 		status();
 		Logs.getLogger().info("end");
 	}
 	
 	/**
 	 * 打开iphone订购页面
 	 * 1 iphone3 8
      * 2 iphone4 16
 	 * 3 iphone4 32
 	 * @param i
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	public void openApple(int i) throws MalformedURLException, IOException,
 			SAXException {
 		HttpUnitOptions.setDefaultCharacterSet("utf-8");
 		HttpUnitOptions.setScriptingEnabled(false);
 		HttpUnitOptions.setExceptionsThrownOnScriptError(false);
 		//HttpUnitOptions.setLoggingHttpHeaders(true);
 		HttpUnitOptions.setAcceptCookies(true);
 		wc.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.12 (KHTML, like Gecko) Chrome/9.0.587.0 Safari/534.12");
 		ClientProperties.getDefaultProperties().setAcceptCookies(true);
//		PostMethodWebRequest mainRq = new PostMethodWebRequest(
//				IphoneUrl.IPHONE_SELECT);
//		WebResponse mainRs = wc.getResponse(mainRq);
//
//		WebForm webForm = mainRs.getForms()[i];
//		webForm.getSubmitButtons()[0].click();
 		
 	}
 
 	public void addToCart() throws Exception {
 		
 		
 		GetMethodWebRequest request = new GetMethodWebRequest(
 				"http://store.apple.com/cn/configure/MC637CH/A?option.iphone_applecare=none&option.iphone_dock=none&option.world_travel_adaptor_kit=none&option.additional_apple_usb_power_adaptor=none&option.additional_apple_dock_connector_to_usb_cable=none&add-to-cart.x=57&add-to-cart.y=5&add-to-cart=添加到购物车");
 		WebResponse response = wc.getResponse(request);
 		System.out.println(response.getText());
 		
 		String result = wc.getResponse("http://store.apple.com/cn/cart").getText();
 		//System.out.println(result);
 		String url =  parseUrlForCart(result);
 		//System.out.println("url="+url);
 		
 		result = wc.getResponse("http://store.apple.com" +url).getText();
 		//System.out.println(result);
 		
 		response = wc.getResponse("http://store.apple.com/cn/checkout/start");
 		
 		String sslUrl = response.getHeaderField("Location");
 		System.out.println(sslUrl);
 		wc.getResponse(sslUrl);
 		
 		PostMethodWebRequest signinRq = new PostMethodWebRequest("https://store.apple.com/cn/sentryx/sign_in");
 		signinRq.setParameter("login-appleId", this.appleId);
 		signinRq.setParameter("login-password", this.password);
 		String[] params = sslUrl.split("&");
 		String c = params[0].split("=")[1];
 		String r = params[1].split("=")[1];
 		String s = params[2].split("=")[1];
 		String t = params[3].split("=")[1];
 		
 		System.out.println("c="+c);
 		System.out.println("r="+r);
 		System.out.println("s="+s);
 		System.out.println("t="+t);
 		
 		signinRq.setParameter("_a", "login.sign");
 		signinRq.setParameter("c", c);
 		signinRq.setParameter("_fid", "si");
 		signinRq.setParameter("r", r);
 		signinRq.setParameter("s", s);
 		signinRq.setParameter("t", t);
 		
 		WebResponse signinRs = wc.getResponse(signinRq);
 		result = signinRs.getText();
 		//System.out.println("sigin result=" + result);
 		
 		
 		setCookie(signinRs);
 		
 		url = parseUrl(result, "https.*?\"}");
 		
 		url = StringUtils.remove(url, "\"}");
 		
 		//System.out.println("url=" + url);
 		
 		wc.getResponse(url);
 		
 		PostMethodWebRequest startRq = new PostMethodWebRequest("https://store.apple.com/cn/checkout/start");
 		startRq.setParameter("pltn", url.split("=")[1]);
 		startRq.setParameter("v", "on");
 		result = wc.getResponse(startRq).getText();
 		
 		//System.out.println("result=" + result);
 		
 		PostMethodWebRequest checkoutRq = new PostMethodWebRequest("https://store.apple.com/cn/checkout");
 		System.out.println(url.split("=")[1]);
 		checkoutRq.setParameter("pltn", url.split("=")[1]);
 		checkoutRq.setParameter("v", "on");
 		result = wc.getResponse(checkoutRq).getText();
 		//System.out.println("result=" + result);
 		
 //		
 
 	}
 	
 	public void checkoutx() throws MalformedURLException, IOException, SAXException{
 		PostMethodWebRequest checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkoutx");
 		checkoutxRq.setParameter("shipping-user-lastName", this.lastName);
 		checkoutxRq.setParameter("shipping-user-firstName", this.firstName);
 		checkoutxRq.setParameter("shipping-user-daytimePhoneAreaCode", "086");
 		checkoutxRq.setParameter("shipping-user-daytimePhone", this.phoneNumber);
 		checkoutxRq.setParameter("shipping-user-emailAddress", this.email);
 		checkoutxRq.setParameter("shipping-user-state", this.state);
 		checkoutxRq.setParameter("shipping-user-city", this.city);
 		checkoutxRq.setParameter("shipping-user-district", this.district);
 		checkoutxRq.setParameter("shipping-user-street", this.street);
 		checkoutxRq.setParameter("shipping-user-street2", "");
 		checkoutxRq.setParameter("shipping-user-postalCode", this.postalCode);
 		checkoutxRq.setParameter("shipping-save-as-default", "true");
 		checkoutxRq.setParameter("zip", "");
 		checkoutxRq.setParameter("method", "A8");
 		checkoutxRq.setParameter("promo-code", "");
 		checkoutxRq.setParameter("_a", "ship.cont");
 		checkoutxRq.setParameter("_fid", "co");
 		WebResponse checkoutxRs = wc.getResponse(checkoutxRq);
 		String result = checkoutxRs.getText();
 		//System.out.println("result=" + result);
 		setCookie(checkoutxRs);
 		
 		checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkoutx");
 		checkoutxRq.setParameter("bankOption", "Alipay");
 		checkoutxRq.setParameter("zip", "");
 		checkoutxRq.setParameter("method", "A8");
 		checkoutxRq.setParameter("promo-code", "");
 		checkoutxRq.setParameter("_a", "bill.cont");
 		checkoutxRq.setParameter("_fid", "co");
 		 checkoutxRs = wc.getResponse(checkoutxRq);
 		result = checkoutxRs.getText();
 		//System.out.println("result=" + result);
 		setCookie(checkoutxRs);
 	}
 	
 	public void invoice() throws MalformedURLException, IOException, SAXException{
 		PostMethodWebRequest invoiceRq = new PostMethodWebRequest("https://store.apple.com/cn/checkoutx/invoice");
 		invoiceRq.setParameter("zip", "");
 		invoiceRq.setParameter("method", "A8");
 		invoiceRq.setParameter("promo-code", "");
 		invoiceRq.setParameter("_a", "copy-shipping");
 		invoiceRq.setParameter("_fid", "co");
 		WebResponse invoiceRs = wc.getResponse(invoiceRq);
 		String result = invoiceRs.getText();
 		//System.out.println("result=" + result);
 		setCookie(invoiceRs);
 	}
 	
 	public void checkoutxForShip() throws MalformedURLException, IOException, SAXException{
 		PostMethodWebRequest checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkoutx");
 		checkoutxRq.setParameter("invoice-user-address-lastName", this.lastName);
 		checkoutxRq.setParameter("invoice-user-address-firstName", this.firstName);
 		checkoutxRq.setParameter("invoice-user-address-daytimePhoneAreaCode", "086");
 		checkoutxRq.setParameter("invoice-user-address-daytimePhone", this.phoneNumber);
 		checkoutxRq.setParameter("invoice-user-address-emailAddress", this.email);
 		checkoutxRq.setParameter("invoice-user-address-state", this.state);
 		checkoutxRq.setParameter("invoice-user-address-city", this.city);
 		checkoutxRq.setParameter("invoice-user-address-district", this.district);
 		checkoutxRq.setParameter("invoice-user-address-street", this.street);
 		checkoutxRq.setParameter("invoice-user-address-street2", "");
 		checkoutxRq.setParameter("invoice-user-address-postalCode", this.postalCode);
 		checkoutxRq.setParameter("invoice-user-address-countryCode", "CN");
 		checkoutxRq.setParameter("_a", "invoice.cont");
 		checkoutxRq.setParameter("_fid", "co");
 		WebResponse checkoutxRs = wc.getResponse(checkoutxRq);
 		String result = checkoutxRs.getText();
 		//System.out.println("https://store.apple.com/cn/checkoutx result=" + result);
 		setCookie(checkoutxRs);
 		
 		checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkoutx");
 		checkoutxRq.setParameter("accept", "true");
 		checkoutxRq.setParameter("_a", "terms.cont");
 		checkoutxRq.setParameter("_fid", "co");
 		 checkoutxRs = wc.getResponse(checkoutxRq);
 		result = checkoutxRs.getText();
 		//System.out.println("https://store.apple.com/cn/checkoutx result=" + result);
 		setCookie(checkoutxRs);
 		
 		checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkoutx");
 		checkoutxRq.setParameter("deviceID", "TF1;015;;;;;;;;;;;;;;;;;;;;;;Mozilla;Netscape;5.0%20%28Windows%3B%20U%3B%20Windows%20NT%205.1%3B%20en-US%29%20AppleWebKit/534.12%20%28KHTML%2C%20like%20Gecko%29%20Chrome/9.0.587.0%20Safari/534.12;20030107;undefined;true;;true;Win32;undefined;Mozilla/5.0%20%28Windows%3B%20U%3B%20Windows%20NT%205.1%3B%20en-US%29%20AppleWebKit/534.12%20%28KHTML%2C%20like%20Gecko%29%20Chrome/9.0.587.0%20Safari/534.12;zh-CN;GBK;store.apple.com;undefined;undefined;undefined;undefined;false;false;1291663443460;8;Tue%20Jun%2007%202005%2021%3A33%3A44%20GMT+0800%20%28China%20Standard%20Time%29;1280;800;6.00;10.1;;1.6.0_18;11.0;;;-480;-480;Tue%20Dec%2007%202010%2003%3A24%3A03%20GMT+0800%20%28China%20Standard%20Time%29;32;1280;770;0;0;Adobe%20Acrobat%7CAdobe%20Acrobat%20Plug-In%20Version%206.00%20for%20Netscape;;;;Shockwave%20for%20Director%7CAdobe%20Shockwave%20for%20Director%20Netscape%20plug-in%2C%20version%2011.0;Shockwave%20Flash%7CShockwave%20Flash%2010.1%20r103;;;;;;;;;Silverlight%20Plug-In%7C3.0.50106.0;Windows%20Media%20Player%20Plug-in%20Dynamic%20Link%20Library%7CNpdsplay%20dll;;;20;");
 		checkoutxRq.setParameter("zip", "");
 		checkoutxRq.setParameter("method", "A8");
 		checkoutxRq.setParameter("promo-code", "");
 		checkoutxRq.setParameter("_a", "po");
 		checkoutxRq.setParameter("_fid", "co");
 		checkoutxRs = wc.getResponse(checkoutxRq);
 		result = checkoutxRs.getText();
 		//System.out.println("https://store.apple.com/cn/checkoutx result=" + result);
 		setCookie(checkoutxRs);
 	}
 
 	
 	public void status() throws Exception{
 		PostMethodWebRequest checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkout/status");
 		WebResponse checkoutxRs = wc.getResponse(checkoutxRq);
 		String result = checkoutxRs.getText();
 		//System.out.println("https://store.apple.com/cn/checkout/status result=" + result);
 		setCookie(checkoutxRs);
 		
 		Thread.sleep(2000);
 		checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkoutx/status");
 		checkoutxRq.setParameter("_a", "st");
 		checkoutxRq.setParameter("_fid", "co");
 		checkoutxRs = wc.getResponse(checkoutxRq);
 		result = checkoutxRs.getText();
 		//System.out.println("https://store.apple.com/cn/checkoutx/status result=" + result);
 		setCookie(checkoutxRs);
 		
 		checkoutxRq = new PostMethodWebRequest("https://store.apple.com/cn/checkout/thankyou");
 		result = wc.getResponse(checkoutxRq).getText();
 		//System.out.println("https://store.apple.com/cn/checkout/thankyou result=" + result);
 		String orderNo = parseOrderNumber(result);
 		System.out.println("orderNo:" + orderNo);
 		this.getOrder().setOrderNo(orderNo);
 	}
 
 	private String parseOrderNumber(String response) throws Exception {
 		Pattern pattern = Pattern.compile("lpOrderNumber\",\"value\":\".*?\"");
 		Matcher matcher = pattern.matcher(response);
 		if (matcher.find())
 			return matcher.group().replace("lpOrderNumber\",\"value\":\"", "").replace("\"", "");
 		else
 			throw new Exception("Can't find OrderNumber");
 	}
 	
 	private String parseUrlForCart(String response) throws Exception {
 		Pattern pattern = Pattern.compile("/cn/hybrid_cartx.*?=cart");
 		Matcher matcher = pattern.matcher(response);
 		if (matcher.find())
 			return matcher.group();
 		else
 			throw new Exception("Can't find UrlForCart");
 	}
 	
 	private String parseUrl(String response, String regex) throws Exception {
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(response);
 		if (matcher.find())
 			return matcher.group();
 		else
 			throw new Exception("Can't find verify");
 	}
 	
 	private void setCookie(WebResponse rs){
 		String[] cookies =rs.getHeaderFields("set-cookie");
 		for(String cookie : cookies){
 			String firstCookie = cookie.split(";")[0];
 			String[] cookieStr = firstCookie.split("=");
 			wc.putCookie(cookieStr[0], cookieStr[1]);
 		}
 	}
 }
