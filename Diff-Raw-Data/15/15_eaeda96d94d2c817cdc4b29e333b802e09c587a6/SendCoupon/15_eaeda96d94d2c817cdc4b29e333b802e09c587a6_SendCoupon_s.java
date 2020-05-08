 package lab.cgcl.requestClient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import lab.cgcl.dao.DaoFactory;
 import lab.cgcl.dao.SqlDao;
 import lab.cgcl.myOCR.util.JsonUtil;
 
 import org.apache.http.Consts;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class SendCoupon {
 	Logger log = Logger.getLogger(ClientFormLogin.class);
 	
 	private String username = "";
 	private String password = "";
 	private String moblie = "";
 	private String Rid = "";
 	private BasicCookieStore cookieStore;
 	private CloseableHttpClient httpclient ;
 	private SqlDao dao;
 
 	public String getMoblie() {
 		return moblie;
 	}
 
 
 	public void setMoblie(String moblie) {
 		this.moblie = moblie;
 	}
 
 
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
 
 
 	public BasicCookieStore getCookieStore() {
 		return cookieStore;
 	}
 
 
 	public void setCookieStore(BasicCookieStore cookieStore) {
 		this.cookieStore = cookieStore;
 	}
 
 
 	public SqlDao getDao() {
 		return dao;
 	}
 
 
 	public void setDao(SqlDao dao) {
 		this.dao = dao;
 	}
 
 	public SendCoupon (SqlDao d , String m) {
 		cookieStore = new BasicCookieStore();
 		httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
 		this.dao = d;
 		this.moblie = m;
 	}
 	
 	public void getAccount() {
		String sql = "select username , pwd from user where used = 0 limit 0 , 1;";
 		try {
 			@SuppressWarnings("unchecked")
 			Map<String , String> map = dao.retrieve(sql);
 			this.setUsername(map.get("username"));
 			this.setPassword(map.get("pwd"));
 			log.info("username : " + username + " ; password : " + password);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	protected int updateDB() {
 		String sql = "update user set usedDate = (select now()) , Rid = '" + Rid +
 				"' , used = 1 , couponMoblie = '" + moblie +
 				"' where username = '" + username +"';";
 		int ret = 0 ;
 		try {
 			ret =dao.update(sql);
 			log.info("saved in db # username : " + username + " ; couponMobile : " + moblie);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	protected boolean doLogin() throws ClientProtocolException, IOException {
 		ClientFormLogin client = new ClientFormLogin (getUsername() , getPassword() , dao) ;
 		int failureCount = 0 ; 
         final int MAXFAILURE = 20;
         
     	while (!client.getLoginPage(httpclient ,cookieStore)) {
     		if ( ++ failureCount == MAXFAILURE )  {
     			log.error("try too many times in login.give up login");
     			return false;
     		}
     	}
     	while (!client.loginAction(httpclient , cookieStore)){
     		if ( ++ failureCount == MAXFAILURE )  {
     			log.error("try too many times in login.give up login");
     			return false;
     		}
     	}
         	
         return true;
 	}
 	
     public boolean exchangeAction (CloseableHttpClient httpclient) throws ClientProtocolException, IOException {
     	boolean flag = false;
     	HttpPost earnPoingPost = new HttpPost("http://www.canda.cn/Utility/CAndAHandler.ashx");
         List <NameValuePair> nvps1 = new ArrayList <NameValuePair>();
         nvps1.add(new BasicNameValuePair("action", "ExchangeProduct"));
        nvps1.add(new BasicNameValuePair("mobile", "13667258772"));
         nvps1.add(new BasicNameValuePair("productId", "3"));
        nvps1.add(new BasicNameValuePair("userName", ""));
         earnPoingPost.setEntity(new UrlEncodedFormEntity(nvps1, Consts.UTF_8));
         CloseableHttpResponse response3 = httpclient.execute(earnPoingPost);
       
         try {
             InputStream is = response3.getEntity().getContent();
             byte[] buffer = new byte[512];
             is.read(buffer);
             String s = new String(buffer, "UTF-8").trim();
             if ( "1".equals(JsonUtil.jsonUtil(s, "ID"))) {
             	log.info( "exchange coupon action response : " +
             			JsonUtil.jsonUtil(s , "DES"));
             	Rid = JsonUtil.jsonUtil(s , "Rid");
             	flag = true;
             }	else if ("-1".equals(JsonUtil.jsonUtil(s, "ID"))){
             	log.error( "no enough points : " +
             			JsonUtil.jsonUtil(s , "DES"));
             }
             else {
             	log.error("exchange coupon action response: " +new String(buffer, "UTF-8") );
             }
             
         } catch (Exception e) {
         	log.error("exchange coupon action error." , e);
         } finally {
             response3.close();
         	try {
         		httpclient.close();
         	} catch (Exception e) {
         		
         	}
         }
         return flag;
     }
 	
     /**
      * һŻȯ
      * @throws ClientProtocolException
      * @throws IOException
      */
 	public boolean exchange () throws ClientProtocolException, IOException {
 		getAccount();
 		if (doLogin()) {
 			if (exchangeAction(httpclient) ) {
 				updateDB();
 				log.info("exchange for " + moblie + " success.");
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void main(String[] args) {
 		PropertyConfigurator.configure("properties/log4j.property");
 		SendCoupon sc = new SendCoupon(DaoFactory.byDefault() , "13667258772");
 		try {
 			sc.exchange();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
