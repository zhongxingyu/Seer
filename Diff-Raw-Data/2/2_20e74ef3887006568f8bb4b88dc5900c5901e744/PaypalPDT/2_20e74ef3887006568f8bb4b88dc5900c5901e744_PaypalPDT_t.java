 package delivery.model;
 
 import java.io.*;
 import java.util.*;
 import java.net.*;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import javax.net.ssl.*;
 import co.aceteq.util.UpdateDB;
 public class PaypalPDT
 {
 
 	private String tx;
 	private String st;
 	private String amt;
 	private String cc;
 	private String item_name;
 	private boolean verified;
 	private String paypalResp;
 	private final static String at = "zW23TEL-sHErudZAvhyX5fythMDM8zmA_nNcbmgucFrE6QfSOjVpY4AOxh4";
 	public PaypalPDT()
 	{
 		verified = false;
 	}
 
 	
 	public static HttpsURLConnection getSSLByPassedConnection(String url) throws Exception {
 		X509TrustManager tm = new X509TrustManager() {
 			@Override
 			public X509Certificate[] getAcceptedIssuers() {
 			return null;
 			}
 			@Override
 			public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
 			}
 			@Override
 			public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
 			}
 		};
 		SSLContext ctx = SSLContext.getInstance("TLS");
 		ctx.init(null, new TrustManager[] { tm }, null);
 		SSLContext.setDefault(ctx); 
 		HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
 		conn.setHostnameVerifier(new HostnameVerifier() {
 			@Override
 			public boolean verify(String paramString, SSLSession paramSSLSession) {
 				return true;
 			}
 		});
 		return conn;
 	}
 
 	public boolean verifyTransaction(){
 		boolean verified = false;
 		try{
 			String str = "cmd=_notify-synch&tx="+tx+"&at="+at;
 			HttpsURLConnection uc = getSSLByPassedConnection("https://www.sandbox.paypal.com/cgi-bin/webscr"); 
 			uc.setDoOutput(true);
 			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			PrintWriter pw = new PrintWriter(uc.getOutputStream());
 			pw.println(str);
 			pw.close();
 		
 			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
 			String res = in.readLine();
 			str = "";
 			while((str = in.readLine()) != null){
				res += "\n" +  str;
 			}
 			this.paypalResp = res;
 			System.out.println(res);
 			if (res.substring(0,4).equals("SUCC"))
 			{ 
 				System.out.println("Success matched.");
 				verified = true;
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		this.verified = verified;
 		return verified;
 	}
 
 	public void save()
 	{
 		Map<String,String> strData = new HashMap<String,String>();
 		strData.put("tx",this.tx);
 		strData.put("st",this.st);
 		strData.put("amt",this.amt);
 		strData.put("verified",new Boolean(this.verified).toString());
 		strData.put("paypalResponse",this.paypalResp);
 		UpdateDB.insert("transactions",strData,null);	
 	}
 	public void setTx(String x)
 	{
 		this.tx = x;
 	}
 	
 	public String getTx()
 	{
 		return this.tx;
 	}
 
 	public void setSt(String x)
 	{
 		this.st = x;
 	}
 
 	public String getSt()
 	{
 		return this.st;
 	}
 
 	public void setAmt(String x)
 	{
 		this.amt = x;
 	}
 
 	public String getAmt()
 	{
 		return this.amt;
 	}
 
 	public void setCc(String x)
 	{
 		this.cc = x;
 	}
 
 	public void setItem_name(String x)
 	{	
 		this.item_name = x;
 	}
 
 	public String getCc()
 	{
 		return this.cc;
 	}
 
 	public String getItem_name()
 	{
 		return this.item_name;
 	}
 }
