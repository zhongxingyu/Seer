 package lab.cgcl.requestClient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import lab.cgcl.dao.DbcpPool;
 import lab.cgcl.dao.SqlDao;
 import lab.cgcl.dao.SqlDaoImpl;
 import lab.cgcl.myOCR.util.JsonUtil;
 import lab.cgcl.myOCR.util.RandomChar;
 import lab.cgcl.myOCR.util.StringUtil;
 
 import org.apache.http.Consts;
 import org.apache.http.HttpEntity;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 
 public class ClientFormLogin {
 	Logger log = Logger.getLogger(ClientFormLogin.class);
 	
 	private String username = "";
 	private String password = "";
 	private String regMoblie = "";
 	private BasicCookieStore cookieStore;
 	private CloseableHttpClient httpclient ;
 	private SqlDao dao;
 
     public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	public ClientFormLogin (String u , String p , SqlDao d) {
 		cookieStore = new BasicCookieStore();
 		httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
 		this.username = u;
 		this.password = p;
 		this.dao = d;
 	}
 
 	public static void main(String[] args) throws Exception {
 		PropertyConfigurator.configure("properties/log4j.property");
         ClientFormLogin clf = new ClientFormLogin(
        		"uSpGUteF@163.com" , "OnhOSVfI" , new SqlDaoImpl(new DbcpPool().getDataSource()));
         clf.doLogin();
     }
 	
 	public void doLogin() throws Exception{
 		/**
 		 * check username and pwd is null
 		 */
 		if (StringUtil.isEmpty(username) && StringUtil.isEmpty(password)) {
 			log.error("username or password is empty.");
 			return;
 		}
 		int failureCount = 0 ; 
         final int MAXFAILURE = 20;
         try {
         	while (!getLoginPage(httpclient ,cookieStore)) {
         		if ( ++ failureCount == MAXFAILURE )  {
         			log.error("try too many times in login.give up this earn point");
         			return ;
         		}
         	}
         	while (!loginAction(httpclient , cookieStore)){
         		if ( ++ failureCount == MAXFAILURE )  {
         			log.error("try too many times in login.give up this earn point");
         			return ;
         		}
         	}
         	while (!memberImproveAction(httpclient)){
         		if ( ++ failureCount == MAXFAILURE )  {
         			log.error("try too many times in member improve.give up this earn point");
         			return ;
         		}
         			
         	}
         	
         	//earn 400 point
         	int pointCount = 0 ;
         	int tryCount = 0;
         	do {
         		if (tryCount ++ == MAXFAILURE) {
         			log.error("try too many times in member improve.it has earned " + 
         		tryCount *50 + " points for " + this.username );
         			return ;
         		}
         		if (earnPointAction(httpclient)) {
         			pointCount ++ ;
         			log.debug("earned 50 points , now sleep for 1 second");
         			Thread.sleep(1000);
         		}
         	}while (pointCount !=10);
         	update();
         	log.info("earns enough points , now exit the thread.");
         	
         } catch (Exception e) { 
         	log.error("unhandled exception" , e);
         }
         finally {
             httpclient.close();
         }
 	}
     
     public boolean getLoginPage (CloseableHttpClient httpclient , BasicCookieStore cookieStore) throws ClientProtocolException, IOException {
     	boolean flag = false;
     	
     	HttpGet httpget = new HttpGet("http://www.canda.cn/memberlogin.aspx");
 
         CloseableHttpResponse response1 = httpclient.execute(httpget);
         try {
             HttpEntity entity = response1.getEntity();
 
             log.debug("Login form get: " + response1.getStatusLine());
             EntityUtils.consume(entity);
 
 //            System.out.println("Initial set of cookies:");
 //            List<Cookie> cookies = cookieStore.getCookies();
 //            if (cookies.isEmpty()) {
 //                System.out.println("None");
 //            } else {
 //                for (int i = 0; i < cookies.size(); i++) {
 //                    System.out.println("- " + cookies.get(i).toString());
 //                }
 //            }
             flag = true;
         } catch (Exception e) {
         	e.printStackTrace();
         } finally {
             response1.close();
         }
         return flag;
     }
     
     public boolean loginAction (CloseableHttpClient httpclient , BasicCookieStore cookieStore) throws ClientProtocolException, IOException {
     	boolean flag = false;
     	HttpPost httpost = new HttpPost("http://www.canda.cn/Utility/CAndAHandler.ashx");
         List <NameValuePair> nvps = new ArrayList <NameValuePair>();
         nvps.add(new BasicNameValuePair("action", "MEMBERLOGIN"));
         nvps.add(new BasicNameValuePair("password", password));
         nvps.add(new BasicNameValuePair("remember", "1"));
         nvps.add(new BasicNameValuePair("username", username));
 
         httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
 
         CloseableHttpResponse response2 = httpclient.execute(httpost);
         try {
             HttpEntity entity = response2.getEntity();
 
             log.debug("Login action get: " + response2.getStatusLine());
             
             InputStream is = entity.getContent();
             byte[] buffer = new byte[512];
             is.read(buffer);
             //String s = new String(buffer, "UTF-8");
             //System.out.println("Login form response: " +s );
             if ( "1".equals(JsonUtil.jsonUtil(new String(buffer , "UTF-8") , "ID"))) {
             	log.info( "login action response : " +
             			JsonUtil.jsonUtil(new String(buffer , "UTF-8") , "DES"));
             	EntityUtils.consume(entity);
 
 //                System.out.println("Post logon cookies:");
 //                List<Cookie> cookies = cookieStore.getCookies();
 //                if (cookies.isEmpty()) {
 //                    System.out.println("None");
 //                } else {
 //                    for (int i = 0; i < cookies.size(); i++) {
 //                        System.out.println("- " + cookies.get(i).toString());
 //                    }
 //                }
                 flag = true;
             } else {
             	log.debug("Login form response: " +new String(buffer, "UTF-8") );
             }
             
         } catch (Exception e) {
         	log.error("login action error." , e);
         } finally {
             response2.close();
         }
         return flag;
     }
     
     public boolean earnPointAction (CloseableHttpClient httpclient) throws ClientProtocolException, IOException {
     	boolean flag = false;
     	HttpPost earnPoingPost = new HttpPost("http://www.canda.cn/Utility/CAndAHandler.ashx");
         List <NameValuePair> nvps1 = new ArrayList <NameValuePair>();
         nvps1.add(new BasicNameValuePair("action", "EARNPOINT"));
         nvps1.add(new BasicNameValuePair("pid", "QQShare"));
         nvps1.add(new BasicNameValuePair("tid", "9"));
         earnPoingPost.setEntity(new UrlEncodedFormEntity(nvps1, Consts.UTF_8));
         CloseableHttpResponse response3 = httpclient.execute(earnPoingPost);
       
         try {
             InputStream is = response3.getEntity().getContent();
             byte[] buffer = new byte[512];
             is.read(buffer);
             if ( "1".equals(JsonUtil.jsonUtil(new String(buffer , "UTF-8") , "ID"))) {
             	log.debug( "earn point action response : " +
             			JsonUtil.jsonUtil(new String(buffer , "UTF-8") , "DES"));
             	flag = true;
             }	else {
             	log.debug("earn point action response: " +new String(buffer, "UTF-8") );
             }
             
         } catch (Exception e) {
         	log.error("earnPoint action error." , e);
         } finally {
             response3.close();
         }
         return flag;
     }
     
     public boolean memberImproveAction (CloseableHttpClient httpclient) throws Exception {
     	boolean flag = false;
     	HttpPost httpPost = new HttpPost("http://www.canda.cn/Utility/CAndAHandler.ashx");
         List <NameValuePair> nvps1 = new ArrayList <NameValuePair>();
         nvps1.add(new BasicNameValuePair("action", "MEMBERIMPROVE"));
         nvps1.add(new BasicNameValuePair("address", ""));
         nvps1.add(new BasicNameValuePair("avatarUrl", ""));
         nvps1.add(new BasicNameValuePair("birthday", ""));
         nvps1.add(new BasicNameValuePair("city", "63"));
         regMoblie = RandomChar.RandomMoblieNo();
         nvps1.add(new BasicNameValuePair("mobile", regMoblie));
         nvps1.add(new BasicNameValuePair("province", "6"));
         nvps1.add(new BasicNameValuePair("qq", ""));
         nvps1.add(new BasicNameValuePair("sex", "1"));
         nvps1.add(new BasicNameValuePair("username", username));
         nvps1.add(new BasicNameValuePair("weibo", ""));
         httpPost.setEntity(new UrlEncodedFormEntity(nvps1, Consts.UTF_8));
         CloseableHttpResponse response3 = httpclient.execute(httpPost);
       
         try {
             InputStream is = response3.getEntity().getContent();
             byte[] buffer = new byte[512];
             is.read(buffer);
             if ( "1".equals(JsonUtil.jsonUtil(new String(buffer , "UTF-8") , "ID"))) {
             	log.debug( "member improve action response : " +
             			JsonUtil.jsonUtil(new String(buffer , "UTF-8") , "DES"));
             	flag =  true;
             }	else {
             	log.debug("member improve action response: " +new String(buffer, "UTF-8") );
             }
             
         } catch (Exception e) {
         	log.error("member improve action error." , e);
         } finally {
             response3.close();
         }
         return flag;
     }
     
     public void update() throws Exception {
     	String sql = "update CAuser.user set earnedPoint = 1 , moblie = '"
     			+ regMoblie + 
     			"' where username = '" + this.username + "';";
     	dao.update(sql);
     	log.info(this.username + " status saved db success");
     }
 }
