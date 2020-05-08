 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Random;
 
 import edu.kgu.QrCode.*;
 import edu.kgu.aeon.bean.loginFormBean;
 import edu.kgu.aeon.bean.registerFormBean;
 import edu.kgu.aeon.Action.*;
 import edu.kgu.aeon.logic.*;
 import edu.kgu.util.*;
 import edu.kgu.aeon.agent.*;
 
 public class test {
 	public static void main(String[] args) {
		updateUserInfo();
 	}
 	
 	public static void deletedl() {
 		delDownloadAction action = new delDownloadAction();
 		action.setUserID("1315492432");
 		action.setDLNo("00001");
 		action.execute();
 	}
 	public static void updateUserInfo() {
		userInfoUpdateAction action = new userInfoUpdateAction();
 		
 		registerFormBean bean = new registerFormBean();
 		//bean.setUserName("shensean08");
 		
 		bean.setPassword("1234567");
 		bean.setPasswordConfirm("1234567");
 		bean.setMailAddress("sdfsdf@ggg.com");
		bean.setAddress1Name("家");
 		bean.setAddress1Postalcode("p1");
		bean.setAddress1Address("宝塚市");
 		
 		action.setUserID("1315492432");
 		action.setUserName("shensean08");
		action.setInfoParten("address1");
 		
 		action.setUserInfoBean(bean);
 		
 		action.execute();
 		
 	}
 	
 	public static void HttpUrlConnection () {
 		try {    
 	           URL url = new URL("http://127.0.0.1:8080/aeonsite/downloadAction.action?userID=111&fileName=test.zip");  
 	           HttpURLConnection conn = (HttpURLConnection)url.openConnection();   
 	  
 	           conn.setDoOutput(true);  
 	           conn.setUseCaches(false);  
 	           conn.setRequestMethod("POST");  
 	           conn.setRequestProperty("Content-Type","text/html");   
 //	           conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
 //	           conn.setRequestProperty("Accept","*/*");
 	           
 	           conn.connect();  
 	           conn.setConnectTimeout(10000);  
 	           OutputStream out =conn.getOutputStream();  
 	  
 	           File file = new File("/Users/seanshen/Documents/0510.pptx");  
 	   
 	           DataInputStream in = new DataInputStream(new FileInputStream(file));  
 	  
 	           int bytes = 0;  
 	           byte[] buffer = new byte[1024];  
 	           while ((bytes = in.read(buffer)) != -1) {  
 	               out.write(buffer, 0, bytes);  
 	           }  
 	           in.close();  
 	           out.flush();  
 	           out.close();   
 	      
 	           System.out.println(conn.getResponseCode());
 	           conn.getInputStream();  
 	           
 //	           conn.getResponseCode();
 	           conn.disconnect();  
 	       } catch (Exception e) {  
 	            System.out.println(e);
 
 	            e.printStackTrace();  
 	       }  
 	}
 	
 	public static void showDownloadLogic() {
 		showDownloadLogic logic = new showDownloadLogic();
 		logic.execute("000002");
 	}
 	public static void downloadAction () {
 		DownloadApplet a = new DownloadApplet();
 		downloadAction action = new downloadAction();
 		action.setUserID("000002");
 //		action.setStartName("sName");
 		action.setStartLat("10");
 		action.setStartLng("20");
 		action.setType("0");
 //		action.setDestinationName("dName");
 		action.setDestinationLat("11");
 		action.setDestinationLng("22");
 		try {
 //			action.execute(new ByteArrayInputStream(DownloadApplet.CaptureScreen()));
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void downloadpic() {
 		DownloadApplet a = new DownloadApplet();
 		a.test();
 	}
 	public static void agentJSON() {
 		agentJSON agent = new agentJSON(300,330,10,4);
 		agent.addAnimation("Show", 0, 0, 35);
 		agent.addAnimation("Idle", 0, 0, 35);
 		System.out.println(agent.getResult());
 	}
 	
 	public static void loginQrAction() {
 		loginQrAction action = new loginQrAction();
 		action.execute();
 	}
 	public static void QrcodeDecode() {
 		QrcodeDecode a = new QrcodeDecode();
 		String b = a.QrcodeDecode("/Users/seanshen/Documents/Cloud/Dropbox/workspace/aeon/aeonsite/QRtmp/ontologyhotmailcom.png");
 		System.out.println(b);
 	}
 	
 	public static void qrcode() {
 		QrcodeEncode qr = new QrcodeEncode('B','M',139,139);
     	qr.setQRVer(2);
     	try {
 			qr.OutQRImage("sean@hotmail.com", "/Users/seanshen/Documents/Cloud/Dropbox/workspace/aeon/qrtest.png");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void mailAddress() {
 		System.out.println(StringProcess.chkMailAddress("sdfsadf@co.com"));
 	}
 	public static void login() {
 		loginFormBean bean = new loginFormBean();
 		bean.setUserName("shensean08");
 		bean.setPassword("1234567");
 		
 		loginAction action = new loginAction();
 		action.setLoginBean(bean);
 		action.execute();
 	}
 	public static void register() {
 		registerFormBean bean = new registerFormBean();
 
 		bean.setUserName("sean");
 		bean.setPassword("1234567");
 		bean.setPasswordConfirm("1234567");
 		bean.setMailAddress("sdfsdf@ggg.com");
 		bean.setAddress1Name("address1");
 		bean.setAddress1Postalcode("p1");
 		bean.setAddress1Address("ad1");
 		registerAction action = new registerAction();
 		action.setRegisterBean(bean);
 		
 		action.execute();
 	}
 	
 	public static void loginQr() {
 		loginQrAction action = new loginQrAction();
 		action.setQrdecode("1315492432");
 		action.execute();
 	}
 }
