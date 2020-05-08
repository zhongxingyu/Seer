 /**
  * 
  */
 package org.chencc98.etmslog.main;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.chencc98.etmslog.entity.DoctorProperty;
 import org.chencc98.etmslog.entity.EventProperty;
 import org.chencc98.etmslog.entity.UserProperty;
 import org.chencc98.etmslog.utils.Constants;
 import org.chencc98.etmslog.utils.ETMSUtil;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.UncheckedJDOMFactory;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.input.sax.XMLReaders;
 
 //http://jwebunit.sourceforge.net/quickstart.html
 
 
 /**
  * @author chencarl
  *
  */
 public class Main {
 	private DefaultHttpClient httpclient = null;
 	private UserProperty pro = null;
 	private Vector<DoctorProperty> vall = null;
 	private Vector<DoctorProperty> vused = null;
 	private boolean isdebug = false;
 	
 	private Document xmldoc;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//#1 login first
 		Main m = new Main();
 		m.login();
 		
 		
 		//#2 insert event log and verify
 		
 		
 		
 //		Date dt = new Date();
 		//default we will handle current day. but sometimes, we will handle another day
 		int today = 0;  //only handle current day.
 		Calendar c = Calendar.getInstance();
 		c.add(Calendar.DAY_OF_YEAR, today);
 		Date dt = c.getTime();
 		
 		
 		
 //		m.InsertEvent(dt);
 		try{
 			m.verifyEvent(dt);
 		}catch(ETMSException e){
 			m.InsertEvent(dt);
 			try{
 				m.verifyEvent(dt);
 			}catch(ETMSException e1){
 				System.err.println("Error: something is wrong "+e1.getMessage());
 				m.shutdownConnection();
 				throw new ETMSRuntimeException(e1.getMessage(), e1);
 			}
 		}
 		
 		
 		
 		 m.getTotalDone(dt);    //get the doctor info and put into vall
 		 
 		 
 //		if( m.getIsdebug() ){
 		System.out.println("Below is the doctor list:");
 		Vector<DoctorProperty> v = m.getVall();
 		Iterator<DoctorProperty> it = v.iterator();
 		while( it.hasNext()){
 			System.out.println(it.next().toString());
 		}
 //			Enumeration<String> e = ht.keys();
 //			while( e.hasMoreElements()){
 //				String key = e.nextElement();
 //				System.out.println("Total at "+key+" is "+ht.get(key));
 //			}
 //		}
 				
 		//#3 add visit log 
 		
 		m.pickup(dt);
 		System.out.println("Picked doctor list:");
 		v = m.getVused();
 		 it = v.iterator();
 		while( it.hasNext()){
 			System.out.println(it.next().toString());
 		}
 		
 		
 		
 		
 			for( DoctorProperty cdp : v ){
 //				if( i == 1 && j == 0){
 					m.addVisitLog(cdp, dt);
 //				}
 			}
 			
 		m.getVall().clear();
 		m.getTotalDone(dt);
 		System.out.println("result:");
 		it = v.iterator();
 		while( it.hasNext() ){
 			DoctorProperty dp = it.next();
 			for( DoctorProperty used : m.getVall() ){
 				if( used.equals(dp)){
					System.out.println(used.toString());
 					break;
 				}
 			}
 		}
 
 		
 		m.logout();
 		m.shutdownConnection();
 	}
 	
 	public Main(){
 		httpclient = new DefaultHttpClient();
 		pro = new UserProperty();
 		vall = new Vector<DoctorProperty>();
 		vused = new Vector<DoctorProperty>();
 		//DoctorHandler.fillAllDoctors(vall); //after login, run this. we will load all doctors dynamically
 		String temp = System.getProperty("DEBUG","false");
 		if( temp.equals("0") || temp.equals("false") ){
 			isdebug = false;
 		}else{
 			isdebug = true;
 		}
 	}
 	public Vector<DoctorProperty> getVall(){
 		return vall;
 	}
 	public Vector<DoctorProperty> getVused(){
 		return vused;
 	}
 	public boolean getIsdebug(){
 		return isdebug;
 	}
 	
 	public void shutdownConnection(){
 		httpclient.getConnectionManager().shutdown();
 	}
 	
 	public void login(){
 		try {
             HttpGet httpget = new HttpGet(Constants.ETMS_BASE_URL + Constants.ETMS_LOGIN_PAGE);
 
             //System.out.println("executing request " + httpget.getURI());
 
             // Create a response handler
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String responseBody = httpclient.execute(httpget, responseHandler);
             int index = responseBody.indexOf("TestValid.asp");
             String code = responseBody.substring(index+19, index+19+4);
             if( this.isdebug ){
             	System.out.println("Login Debug info:");
             	System.out.println(responseBody);
             }
             System.out.println("----------------------------------------");
             System.out.println("code="+code);
             System.out.println("----------------------------------------");
             
             HttpPost httpost = new HttpPost(Constants.ETMS_BASE_URL + Constants.ETMS_LOGIN_PAGE);
             List <NameValuePair> nvps = new ArrayList <NameValuePair>();
             nvps.add(new BasicNameValuePair("multi_lan", "chn"));
             nvps.add(new BasicNameValuePair("usr_code", pro.getUsername()));
             nvps.add(new BasicNameValuePair("usr_passwd", pro.getPassword()));
             nvps.add(new BasicNameValuePair("valid_code", code));
             nvps.add(new BasicNameValuePair("retry_num", "0"));
             httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.ASCII));
             HttpResponse response = httpclient.execute(httpost);
             HttpEntity entity = response.getEntity();
             EntityUtils.consume(entity);
             if( this.isdebug ) {System.out.println("Post logon cookies:");}
             List<Cookie> cookies = httpclient.getCookieStore().getCookies();
             if (cookies.isEmpty()) {
 //                System.out.println("None");
                 throw new ETMSException("Login failed. no cookie");
             } else {
                 for (int i = 0; i < cookies.size(); i++) {
                     if( this.isdebug ){System.out.println("- " + cookies.get(i).toString());}
                 }
                 if( cookies.size() != 2 ){
 //                	throw new ETMSException("Login failed. cookie number is "+cookies.size());
                 }
             }
 
         } catch (Exception e) {
 			System.err.println("error happen when try to login");
 			if( this.isdebug) { e.printStackTrace();}
 			httpclient.getConnectionManager().shutdown();
 			//System.exit(1);
 			
 			throw new ETMSRuntimeException(e.getMessage(), e);
 		} 
         
         System.out.println("Info: Login Successfully");
 	}
 	
 	public void logout(){
 		try {
             HttpGet httpget = new HttpGet("https://www.etms1.astrazeneca.cn/logout.asp");
 
             //System.out.println("executing request " + httpget.getURI());
 
             // Create a response handler
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
              httpclient.execute(httpget, responseHandler);
             
 //            System.out.println("----------------------------------------");
 //            System.out.println(responseBody);
 //            System.out.println("----------------------------------------");
             
            
             
 
         } catch (Exception e) {
 			System.err.println("error happen when try to logout");
 			if( this.isdebug ){e.printStackTrace();}
 			httpclient.getConnectionManager().shutdown();
 			System.exit(1);
 		} 
         
         System.out.println("Info: Logout Successfully");
 	}
 
 	public void InsertEvent(java.util.Date dt){
 		try{
 //			HttpGet httpget = new HttpGet("https://www.etms1.astrazeneca.cn/Call/EventEditDialog-body.asp?usr_code="+pro.getUsername()+"&start_date="+ETMSUtil.getDateFormat());
 //			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 //            String responseBody = httpclient.execute(httpget, responseHandler);
 //            System.out.println("----------------------------------------");
 //            System.out.println(responseBody);
 //            System.out.println("----------------------------------------");
 			HttpPost httpost = new HttpPost(Constants.ETMS_BASE_URL + Constants.ETMS_EVENTINSERT_PAGE);
             List <NameValuePair> nvps = new ArrayList <NameValuePair>();
             nvps.add(new BasicNameValuePair("start_date", ETMSUtil.getDateFirst(dt)));
             nvps.add(new BasicNameValuePair("usr_code", pro.getUsername()));
             nvps.add(new BasicNameValuePair("events", pro.getDefaultEvents()));
 //            System.out.println(pro.getDefaultEvents());
             nvps.add(new BasicNameValuePair("flag", "0"));
             httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.ASCII));
             HttpResponse response = httpclient.execute(httpost);
             HttpEntity entity = response.getEntity();
             EntityUtils.consume(entity);
 //            entity.writeTo(System.out);
 			
 		}catch(Exception e){
 			System.err.println("error happen when try to insert event");
 			if( this.isdebug) {e.printStackTrace();}
 			httpclient.getConnectionManager().shutdown();
 			//System.exit(1);
 			
 			throw new ETMSRuntimeException( e.getMessage(), e);
 		}
 	}
 	
 	public void verifyEvent(Date dt)throws ETMSException{
 		try{
 			HttpGet httpget = new HttpGet(Constants.ETMS_BASE_URL + Constants.ETMS_EVENTEDIT_PAGE 
 					+ "?usr_code="+pro.getUsername()+"&start_date="+ETMSUtil.getDateFirst(dt));
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String responseBody = httpclient.execute(httpget, responseHandler);
             if( this.isdebug ){
             	System.out.println("VerifyEvent Debug info:");
             	System.out.println(responseBody);
             }
             System.out.println("----------------------------------------");
 //            System.out.println(responseBody);
             EventProperty[] eps = ETMSUtil.getEventLists(responseBody, dt);
             int empty_found = 0;
             for( int i=1; i<=7; i++ ){
             	System.out.println("Date:"+eps[i-1].getDate()+"\tAM:"+eps[i-1].getAMEvt()+"\tPM:"+eps[i-1].getPMEvt());
             	if( eps[i-1].getAMEvt().equals("") || eps[i-1].getPMEvt().equals("")){
             		empty_found++;
             	}
             }
             
             System.out.println("----------------------------------------");
             if( empty_found != 0){
             	throw new ETMSException("Verify error, some Event is empty");
             }
 		}catch(ETMSException e){
 			throw e;
 		}
 		catch(Exception e){
 			System.err.println("error happen when try to verify event");
 			if( this.isdebug ){e.printStackTrace();}
 			httpclient.getConnectionManager().shutdown();
 			//System.exit(1);
 			
 			throw new ETMSRuntimeException(e.getMessage(), e);
 		}
 		System.out.println("Event verified successfully");
 	}
 
 	public void addVisitLog(DoctorProperty doc, Date dt){
 		if( this.isdebug ){
 			System.out.println("Info: Add doc visit "+doc+" at "+dt);
 		}
 		try{
 			// below code is the first click
 			 HttpPost httpost = new HttpPost("https://www.etms1.astrazeneca.cn/Call/CPDocAction-updt.asp");
 	            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
 	            nvps.add(new BasicNameValuePair("cur_date", ETMSUtil.getDateFirst(dt)));
 	            nvps.add(new BasicNameValuePair("m_Date", ETMSUtil.getDateFirst(dt)));
 	            nvps.add(new BasicNameValuePair("half", "0"));
 	            nvps.add(new BasicNameValuePair("sysdate", ETMSUtil.getDateFormat(new Date())));
 	            nvps.add(new BasicNameValuePair("frmAsp", "bulk"));
 	            nvps.add(new BasicNameValuePair("txtBtnKind", "rbAllDoc"));
 	            nvps.add(new BasicNameValuePair("updt_cnt", "1"));
 	            nvps.add(new BasicNameValuePair("arg", "&c_"+doc.getHospital()+"_"+doc.getDoctorID()+"_"+doc.getDept()+"_"+ETMSUtil.getDateFormat(dt)+"=add"));
 	            nvps.add(new BasicNameValuePair("mr_code", pro.getUsername()));
 	            nvps.add(new BasicNameValuePair("mr_name", pro.getFullname()));
 	            
 	           
 	            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.ISO_8859_1));
 	            HttpResponse response = httpclient.execute(httpost);
 	            HttpEntity entity = response.getEntity();
 	            EntityUtils.consume(entity);
 			
 			
 //			return;
 			
 			//get the necessary info and open inout dialg
 			
 			HttpGet httpget = new HttpGet("https://www.etms1.astrazeneca.cn/Call/CPDocDetailModal.asp?call_date="+ETMSUtil.getDateFormat(dt)+"&ins_code="+doc.getHospital()+"&doc_code="+doc.getDoctorID()+"&usr_code="+pro.getUsername()+"&frmASP=bulk");
 //			HttpGet httpget = new HttpGet("https://www.etms1.astrazeneca.cn/Call/CPDocDetailModal.asp?call_date="+ETMSUtil.getDateFormat(dt)+"&ins_code=ZJHZ036H&doc_code=ZJHZ036114&usr_code="+pro.getUsername()+"&frmASP=bulk");
 
             
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String responseBody = httpclient.execute(httpget, responseHandler);
             String system_id = ETMSUtil.searchSystem_id(responseBody);
             String call_code = ETMSUtil.searchCall_code(responseBody);
             String type = ETMSUtil.searchType(responseBody);
             if( this.isdebug ){
 				System.out.println("-----------------");
 				// System.out.println(responseBody);
 				System.out.println("system_id=" + system_id);
 				System.out.println("call_code=" + call_code);
 				System.out.println("type=" + type);
 				System.out.println("-----------------");
             }
             
             HttpGet httpget2 = new HttpGet("https://www.etms1.astrazeneca.cn/Call/CPDocDetail-input.asp?usr_code="+pro.getUsername()+"&call_date="+ETMSUtil.getDateFormat(dt)+"&ins_code="+doc.getHospital()+"&doc_code="+doc.getDoctorID()+
             		"&system_id="+system_id+"&call_code="+call_code+"&mr_code="+pro.getUsername()+"&mr_name="+pro.getFullname()+"&frmAsp=bulk&type="+type);
 //            HttpGet httpget2 = new HttpGet("https://www.etms1.astrazeneca.cn/Call/CPDocDetail-input.asp?usr_code="+pro.getUsername()+"&call_date="+ETMSUtil.getDateFormat(dt)+"&ins_code=ZJHZ036Hdoc_code=ZJHZ036114&system_id="+system_id+"&call_code="+call_code+"&mr_code="+pro.getUsername()+"&frmAsp=bulk&type="+type);
 
             
             ResponseHandler<String> responseHandler2 = new BasicResponseHandler();
             String responseBody2 = httpclient.execute(httpget2, responseHandler2);
             
 //            System.out.println("-----------------");
             if( this.isdebug ) {
             	System.out.println(responseBody2);
             }
             String time_stamp = ETMSUtil.searchTimestamp(responseBody2);
             if( time_stamp.equals("") ){
             	System.err.println("Error: can't get timestamp for this doctor "+ doc.getDoctorID());
             	return;
             }
 //            
 //            System.out.println("-----------------");
 //            
             
 
             //start to post to add this visit
             HttpPost httpost2 = new HttpPost("https://www.etms1.astrazeneca.cn/Call/CPDocDetail-Updt.asp");
 //            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
             nvps.clear();
 //            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
             nvps.add(new BasicNameValuePair("system_id", system_id));
             nvps.add(new BasicNameValuePair("time_stamp", time_stamp));
             nvps.add(new BasicNameValuePair("usr_code", pro.getUsername()));
             nvps.add(new BasicNameValuePair("login_usr_code", pro.getUsername()));
             nvps.add(new BasicNameValuePair("call_code", call_code));
             String call_type_code = "11";
             Date nowdt = new Date();
             if( nowdt.before(dt) ){
             	call_type_code = "02";
             }
             nvps.add(new BasicNameValuePair("call_type_code", call_type_code));
             nvps.add(new BasicNameValuePair("doc_code", String.format("%-15s",doc.getDoctorID())));
             nvps.add(new BasicNameValuePair("ins_code", String.format("%-10s",doc.getHospital())));
 //            nvps.add(new BasicNameValuePair("doc_code","ZJHZ036114" ));
 //            nvps.add(new BasicNameValuePair("ins_code", "ZJHZ036H"));
             nvps.add(new BasicNameValuePair("call_date", ETMSUtil.getDateFormat2(dt)));
             String start = ETMSUtil.getStartTime();
             nvps.add(new BasicNameValuePair("start_time", start));
             nvps.add(new BasicNameValuePair("end_time", ETMSUtil.getEndTime(start)));
             nvps.add(new BasicNameValuePair("call_sub_type_code", "D"));
             nvps.add(new BasicNameValuePair("prod_code1", "RE09"));
             nvps.add(new BasicNameValuePair("det_msg1", ETMSUtil.getMsg()));
             nvps.add(new BasicNameValuePair("feedback1", "01"));
             nvps.add(new BasicNameValuePair("btnUpdt", "ݷ"));
             
            
             httpost2.setEntity(new UrlEncodedFormEntity(nvps, HTTP.ISO_8859_1));
             HttpResponse response2 = httpclient.execute(httpost2);
             HttpEntity entity2 = response2.getEntity();
             EntityUtils.consume(entity2);
             
             
 		}catch (Exception e) {
 			System.err.println("error happen when try to add visit log");
 //			if( this.isdebug ){
 				e.printStackTrace();
 //			}
 			httpclient.getConnectionManager().shutdown();
 			System.exit(1);
 		} 
 //		System.out.println("Info: Add doc visit successfully");
 	}
 	
 	
 	public void getTotalDone(Date dt){
 //		Hashtable<String, String> ht = new Hashtable<String, String>();
 		try{
 			HttpGet httpget = new HttpGet( Constants.ETMS_BASE_URL + Constants.ETMS_DOCTOR_LOG_PAGE
 					+"?half=1&txtBtnKind=rbAllDoc&mr_code="+pro.getUsername()+"&mr_name="+pro.getFullname()
 					+"&cur_date="+ ETMSUtil.getDateFormat(dt) + "&m_Date="+ ETMSUtil.getDateFormat(dt)
 					+"&sysdate="+ETMSUtil.getDateFormat(new Date()) + "&frmAsp=bulk");
 //			
 //			System.out.println(httpget.getParams().toString());
             
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String responseBody = httpclient.execute(httpget, responseHandler);
             if( this.isdebug ){ 
             	System.out.println("GetTotalDone Debug info:");
             	System.out.println(responseBody);
             }
            
             
             //remove unused string before <html>
             int i = responseBody.indexOf("<html>");
             if( i >= 0 ){
             	responseBody = responseBody.substring(i);
             }
             
             
             int i1 = responseBody.indexOf("<head>");
             int i2 = responseBody.indexOf("</head>", i1);
             responseBody = responseBody.substring(0, i1) + responseBody.substring(i2 + 7);  //remove first link
             while(( i1=responseBody.indexOf("<colgroup>") ) >=0 ){
             	i2 = responseBody.indexOf("</colgroup>", i1);
             	responseBody = responseBody.substring(0, i1) + responseBody.substring(i2 + 11); 
             }
             while(( i1=responseBody.indexOf("<input ") ) >=0 ){
             	i2 = responseBody.indexOf(">", i1);
             	responseBody = responseBody.substring(0, i1) + responseBody.substring(i2 + 1); 
             }
             while(( i1=responseBody.indexOf("<label ") ) >=0 ){
             	i2 = responseBody.indexOf("</label>", i1);
             	responseBody = responseBody.substring(0, i1) + responseBody.substring(i2 + 8); 
             }
             while(( i1=responseBody.indexOf("<br>") ) >=0 ){
             	
             	responseBody = responseBody.substring(0, i1) + responseBody.substring(i1 + 4); 
             }
             while(( i1=responseBody.indexOf("nowrap") ) >=0 ){
             	
             	responseBody = responseBody.substring(0, i1) + responseBody.substring(i1 + 6); 
             }
             while(( i1=responseBody.indexOf("value=") ) >=0 ){
             	i2 = responseBody.indexOf(">", i1);
             	responseBody = responseBody.substring(0, i1) + responseBody.substring(i2 ); 
             }
             while(( i1=responseBody.indexOf("id=total") ) >=0 ){
             	i2 = responseBody.indexOf(">", i1);
             	responseBody = responseBody.substring(0, i1) + " id=\"" + responseBody.substring(i1 +3 , i2)
             			+ "\" " + responseBody.substring(i2 ); 
             }
             
             
 //            System.out.println(responseBody);
             
             SAXBuilder sbuild = new SAXBuilder(null,null, new UncheckedJDOMFactory());
             sbuild.setExpandEntities(false);
             sbuild.setXMLReaderFactory(XMLReaders.NONVALIDATING);
             //sbuild.setErrorHandler(new MyErrorHandler());
             xmldoc = sbuild.build(new StringReader( responseBody));
             
             Element root = xmldoc.getRootElement();   //root element <html>
             List<Element> divlist = root.getChild("body").getChildren("div");   //the div list
             Element wantedDiv = null;
             for( Element e : divlist ){
             	if( "callactionbody_area".equals(e.getAttributeValue("id"))){
             		wantedDiv = e;
             		break;
             	}
             }
             if( wantedDiv == null ){
             	throw new ETMSException("body div can't found");
             }
             
             List<Element> trlist = wantedDiv.getChild("table").getChild("tbody").getChildren("tr");
             //above is the doctor tr list, start to parse
             for( Element tr : trlist ){
             	List<Element> tdlist = tr.getChildren("td");
             	String level = tdlist.get(3).getText();          //level, A B C V
             	String access = tdlist.get(5).getText();          //visit number, 3 or 4
             	String line = tdlist.get(6).getAttributeValue("id");  //c_ZJHZ036H_ZJHZ036137_D007_2012/04/22
             	String [] token = line.split("_");
             	String hospital_id = token[1];
             	String doctor_id = token[2];
             	String depart_id = token[3];
             	boolean ispicked = false;
             	for( int j = 6; j<= 19 ; j++){
             		line = tdlist.get(j).getAttributeValue("id");
             		String mark = "c_"+hospital_id+"_"+doctor_id+"_"+depart_id+"_"+ETMSUtil.getDateFormat(dt);
             		if( mark.equals(line) ){
             			if( ! "".equals(tdlist.get(j).getText()) && "cell_5".equals(tdlist.get(j).getAttributeValue("class")) ){
             				ispicked = true;
             			}else{
             				ispicked = false;
             			}
             			break;
             		}
             	}
             	
             	DoctorProperty dp = new DoctorProperty(hospital_id, depart_id, doctor_id,
             			level, Integer.parseInt(access));
             	dp.setIspicked(ispicked);
             	vall.add(dp);
             	
             }
             
             
            
             
 		}catch(Exception e){
 			System.err.println("error happen when try to get total done");
 			if( this.isdebug) { e.printStackTrace();}
 //			httpclient.getConnectionManager().shutdown();
 //			System.exit(1);
 			
 			throw new ETMSRuntimeException(e.getMessage(), e);
 		}
 		
 	}
 	
 	
 	//pickup the doctor to log
 	public void pickup(Date dt){
 		Calendar c = Calendar.getInstance();
 		c.setTime(dt);
 		int day = c.get(Calendar.DAY_OF_MONTH);
 		if( day >= 1 && day <= 15 ){  //pick up the same hospital and same depart
 			pickupHospitalDepart();
 			if( vused.size() < Constants.MINIMUM_LOG){
 				pickupHospital(Constants.MINIMUM_LOG);
 				if( vused.size() < Constants.MINIMUM_LOG ){
 					pickupAnyHospital(Constants.MINIMUM_LOG);
 				}
 			}
 		}else{
 			pickupAsLevel(Constants.MINIMUM_LOG);
 			if( vused.size() < Constants.MINIMUM_LOG ){
 				pickupAnyHospital(Constants.MINIMUM_LOG);
 			}
 		}
 	}
 	
 	private void pickupHospitalDepart(){
 		Random r = new Random();
 		DoctorProperty dp = null;
 		
 		while( dp == null){
 			int num = r.nextInt(vall.size());
 			dp =  vall.get(num);
 			if( dp.getIspicked() ){
 				dp = null;
 			}else{
 				dp.setIspicked(true);
 				vused.add(dp);
 			}
 		}
 		
 		Iterator<DoctorProperty> it = vall.iterator();
 		while( it.hasNext()){
 			DoctorProperty tmp = it.next();
 			if( tmp.getHospital().equals(dp.getHospital()) && tmp.getDept().equals(dp.getDept())
 					&& !tmp.getIspicked()){
 				tmp.setIspicked(true);
 				vused.add(tmp);
 			}
 		} 		
 		
 		
 		
 	}
 	
 	private void pickupHospital( int max){
 		Random r = new Random();
 		DoctorProperty dp = null;
 		
 		if( vused.size() != 0 ){
 			dp = vused.get(r.nextInt(vused.size()));
 		}
 		
 		
 		while( dp == null){
 			int num = r.nextInt(vall.size());
 			dp =  vall.get(num);
 			if( dp.getIspicked() ){
 				dp = null;
 			}else{
 				dp.setIspicked(true);
 				vused.add(dp);
 			}
 		}
 		
 		Iterator<DoctorProperty> it = vall.iterator();
 		while( it.hasNext() && vused.size() <= max){
 			DoctorProperty tmp = it.next();
 			if( tmp.getHospital().equals(dp.getHospital()) 
 					&& !tmp.getIspicked()){
 				tmp.setIspicked(true);
 				vused.add(tmp);
 			}
 		} 
 		
 	
 		
 	}
 	
 	
 	private void pickupAnyHospital(int max){
 		Random r = new Random();
 		
 		
 		while( vused.size() <= max ){
 			int num = r.nextInt(vall.size());
 			DoctorProperty dp =  vall.get(num);
 			if( dp.getIspicked() ){
 				//do nothing
 			}else{
 				dp.setIspicked(true);
 				vused.add(dp);
 			}
 		}
 		
 				
 	}
 	
 	private void pickupAsLevel(int max ){
 		
 		
 		Iterator<DoctorProperty> it = vall.iterator();
 		while( it.hasNext() && vused.size() <= max ){
 			DoctorProperty tmp = it.next();
 			if( tmp.getLevel().equals("A") && tmp.getAccess()< Constants.A_LEVEL_REQUIRED_LOG  
 					&& !tmp.getIspicked()){
 				tmp.setIspicked(true);
 				vused.add(tmp);
 			}else if( tmp.getLevel().equals("B") && tmp.getAccess()< Constants.B_LEVEL_REQUIRED_LOG  
 					&& !tmp.getIspicked()){
 				tmp.setIspicked(true);
 				vused.add(tmp);
 			}else if(  !tmp.getLevel().equals("A") && !tmp.getLevel().equals("B")
 					&& tmp.getAccess()< Constants.O_LEVEL_REQUIRED_LOG  
 					&& !tmp.getIspicked()){
 				tmp.setIspicked(true);
 				vused.add(tmp);
 			}
 		} 
 		
 	
 		
 	}
 	
 	
 }
