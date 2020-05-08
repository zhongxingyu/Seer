 package net.guto.hellow.test;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 import net.guto.hellow.auth.TweenerAuthentication;
 
 public class TweenerTest extends TestCase {
 
 	TweenerAuthentication twn;
 
 	@Override
 	public void setUp() {
 		twn = new TweenerAuthentication();
 	}
 
 	@Override
 	public void tearDown() {
 		twn = null;
 	}
 
 	public static final String EL = "\r\n";
 
 	public void testExtractHttpResponseHeader() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("HTTP/1.1 200 OK" + EL);
 		sb.append("Date: Sun, 11 Jul 2010 18:46:58 GMT" + EL);
 		sb.append("Server: Microsoft-IIS/6.0" + EL);
 		sb.append("PPServer: PPV: 30 H: BAYIDSPRTS1B04 V: 0" + EL);
 		sb.append("PassportURLs: DARealm=Passport.Net,DALogin=login.live.com/login2.srf,DAReg=https://accountservices.passport.net/UIXPWiz.srf,Properties=https://accountservices.msn.com/editprof.srf,Privacy=https://accountservices.passport.net/PPPrivacyStatement.srf,GeneralRedir=http://nexusrdr.passport.com/redir.asp,Help=https://accountservices.passport.net,ConfigVersion=14"
 				+ EL);
 		sb.append("Content-Length: 0" + EL);
 		sb.append("Content-Type: text/html" + EL);
 		sb.append("Cache-control: private" + EL);
 		sb.append(EL); // TODO see this;
 		Map<String, String> props = twn
 				.extractHttpResponseHeader(sb.toString());
 		assertEquals("Date not equals", props.get("Date"),
 				"Sun, 11 Jul 2010 18:46:58 GMT");
 		assertEquals("Server not equals", props.get("Server"),
 				"Microsoft-IIS/6.0");
 		assertEquals("PPServer not equals", props.get("PPServer"),
 				"PPV: 30 H: BAYIDSPRTS1B04 V: 0");
 		assertEquals(
 				"PassportURLs not equals",
 				props.get("PassportURLs"),
 				"DARealm=Passport.Net,DALogin=login.live.com/login2.srf,DAReg=https://accountservices.passport.net/UIXPWiz.srf,Properties=https://accountservices.msn.com/editprof.srf,Privacy=https://accountservices.passport.net/PPPrivacyStatement.srf,GeneralRedir=http://nexusrdr.passport.com/redir.asp,Help=https://accountservices.passport.net,ConfigVersion=14");
 		assertEquals("Content-Length not equals", props.get("Content-Length"),
 				"0");
 		assertEquals("Content-Type not equals", props.get("Content-Type"),
 				"text/html");
 		assertEquals("Cache-control not equals", props.get("Cache-control"),
 				"private");
 	}
 
 	public void testExtractPassportVars() {
 		String passportURls = "ARealm=Passport.Net,DALogin=login.live.com/login2.srf,DAReg=https://accountservices.passport.net/UIXPWiz.srf,Properties=https://accountservices.msn.com/editprof.srf,Privacy=https://accountservices.passport.net/PPPrivacyStatement.srf,GeneralRedir=http://nexusrdr.passport.com/redir.asp,Help=https://accountservices.passport.net,ConfigVersion=14";
 		Map<String, String> props = twn.extractVarParams(passportURls);
 		assertEquals("ARealm not equals", props.get("ARealm"), "Passport.Net");
 		assertEquals("DALogin not equals", props.get("DALogin"),
 				"login.live.com/login2.srf");
 		assertEquals("DAReg not equals", props.get("DAReg"),
 				"https://accountservices.passport.net/UIXPWiz.srf");
 		assertEquals("Properties not equals", props.get("Properties"),
 				"https://accountservices.msn.com/editprof.srf");
 		assertEquals("Privacy not equals", props.get("Privacy"),
 				"https://accountservices.passport.net/PPPrivacyStatement.srf");
 		assertEquals("GeneralRedir not equals", props.get("GeneralRedir"),
 				"http://nexusrdr.passport.com/redir.asp");
 		assertEquals("Help not equals", props.get("Help"),
 				"https://accountservices.passport.net");
 		assertEquals("ConfigVersion not equals", props.get("ConfigVersion"),
 				"14");
 
 	}
 
 	public void testBuildParamVars() {
		String username = "hellowgmn%40hotmail.com";
		String password = "123456";
 		String lc = "ct=1278901179,rver=5.5.4182.0,wp=FS_40SEC_0_COMPACT,lc=1033,id=507,ru=http:%2F%2Fmessenger.msn.com,tw=0,kpp=1,kv=4,ver=2.1.6000.1,rn=1lgjBfIL,tpf=b0735e3a873dfb5e75054465196398e0";
 		String expected = "Passport1.4 OrgVerb=GET,OrgURL=http%3A%2F%2Fmessenger%2Emsn%2Ecom,sign-in="
 				+ username + ",pwd=" + password + "," + lc;
 		Map<String, String> authParams = new LinkedHashMap<String, String>();
 		authParams.put("Passport1.4 OrgVerb", "GET");
 		authParams.put("OrgURL", "http%3A%2F%2Fmessenger%2Emsn%2Ecom");
 		authParams.put("sign-in", username);
 		authParams.put("pwd", password);
 		authParams.put("lc", lc);
 		String actual = twn.buildParamVars(authParams);
 		assertEquals(expected, actual);
 	}
 
 	public void testBuildHttpRequestHeader() {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("GET /login2.srf HTTP/1.1" + EL);
		sb.append("Authorization: Passport1.4 OrgVerb=GET,OrgURL=http%3A%2F%2Fmessenger%2Emsn%2Ecom,sign-in=hellowgmn%40hotmail.com,pwd=123456,ct=1278901179,rver=5.5.4182.0,wp=FS_40SEC_0_COMPACT,lc=1033,id=507,ru=http:%2F%2Fmessenger.msn.com,tw=0,kpp=1,kv=4,ver=2.1.6000.1,rn=1lgjBfIL,tpf=b0735e3a873dfb5e75054465196398e0"
 				+ EL);
 		sb.append("Host: login.live.com" + EL);
 
 		Map<String, String> requestParams = new HashMap<String, String>();
 		requestParams
 				.put("Authorization",
						"Passport1.4 OrgVerb=GET,OrgURL=http%3A%2F%2Fmessenger%2Emsn%2Ecom,sign-in=hellowgmn%40hotmail.com,pwd=123456,ct=1278901179,rver=5.5.4182.0,wp=FS_40SEC_0_COMPACT,lc=1033,id=507,ru=http:%2F%2Fmessenger.msn.com,tw=0,kpp=1,kv=4,ver=2.1.6000.1,rn=1lgjBfIL,tpf=b0735e3a873dfb5e75054465196398e0");
 		requestParams.put("Host", "login.live.com");
 		String requestHeader = twn.buildHttpRequestHeader(
 				"login.live.com/login2.srf", requestParams);
 		assertEquals(sb.toString(), requestHeader);
 
 	}
 }
