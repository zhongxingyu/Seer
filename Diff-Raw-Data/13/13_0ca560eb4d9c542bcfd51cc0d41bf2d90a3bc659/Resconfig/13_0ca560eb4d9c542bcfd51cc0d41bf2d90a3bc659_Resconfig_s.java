 package com.redarc;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.VisitorSupport;
 import org.dom4j.io.*;
 
 public class Resconfig {
 
 	private HashSet<String> weblist = new HashSet<String>();
 	private String user = "root";
 	private String passwd = "root";
 	private String l3PGRmd = "L3_PG_Reminder.html";
 	private String ftreport = "FT_L23_DAILY_REPORT.html";
 	private String mtguide = "LMR_Main_Track_delivery_guidelines.html";
 	private String security = "Security.html";
 	private String mtQAstatus;
 	private String index = "index.html";
 
 	private String localpath = System.getProperty("user.dir");
 	private String localsrv = "10.186.135.173";
 	private Integer delayTime = 5000;
 	private Integer swapTime = 2000;
 	private Integer refreshTime = 10000;
 	private int transtime = 2;
 	private int tr_max_no = 5;
 	private int up_max_no = 3;
 	private String mhweburl = "https://mhweb.ericsson.se/TREditWeb/faces/tredit/tredit.xhtml?eriref=";
 	private String mtQAstatusurl;
	private String hrheadingId = "frm_field_heading_j_id_9v_notetextEditMode";
 	private String hrexceptionId = "compAjaxExceptionDialog_msgDialogPanel";
 	private LinkedHashMap<String,String> trackMap = new LinkedHashMap<String,String>();
 	private Document doc;
 	
 	private static Resconfig self;
 	private Resconfig(){
 		SAXReader saxreader = new SAXReader();
 		try {
 			doc = saxreader.read(new File("config.xml"));
 			Element root = doc.getRootElement();
 			root.accept(new ResVisitor());
 			
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Resconfig getInstance(){
 		if(null != self){
 			return self;
 		}else{
 			return new Resconfig();
 		}
 	}
 	
 	public class ResVisitor extends VisitorSupport{
 		public void visit(Element elem){
 		    switch(elem.getName()){
 		    case "mhweburl" :
 		    {
 		    	setMhweburl(elem.getText());
 		    	break;
 		    }
 		    case "mtQAstatus" :
 		    {
 		    	setMtQAstatusurl(elem.getText());
 		    	break;
 		    }	
 		    case "tracklist" :
 		    {
 		    	@SuppressWarnings("rawtypes")
 				List list = elem.elements();
 		    	@SuppressWarnings("rawtypes")
 				Iterator iter = list.iterator();
 		    	while(iter.hasNext()){
 		    		Element track = (Element)iter.next();
 		    		putTrackMap(track.getName(),track.getText());
 		    	}
 		    	break;
 		    }
 		    case "rollist" :
 		    {
 		    	@SuppressWarnings("rawtypes")
 				List list = elem.elements();
 		    	@SuppressWarnings("rawtypes")
 				Iterator iter = list.iterator();
 		    	while(iter.hasNext()){
 		    		Element track = (Element)iter.next();
 		    		if(!track.getName().equals("Index")){
 		    			weblist.add(track.getText());
 		    		}
 		    	}
 		    	break;
 		    }
 		    case "localserver":
 		    {
 		    	setLocalsrv(elem.getText());
 		    	break;
 		    }
 		    case "localpath":
 		    {
 		    	setLocalpath(elem.getText());
 		    	break;
 		    }
 		    case "user":
 		    {
 		    	setUser(elem.getText());
 		    	break;
 		    }
 		    case "passwd":
 		    {
 		    	setPasswd(elem.getText());
 		    	break;
 		    }
 		    case "hrheadingid":
 		    {	
 		    	setHrheadingId(elem.getText());
 		    	break;
 		    }
 		    case "hrexceptionid":
 		    {
 		    	setHrexceptionId(elem.getText());
 		    	break;
 		    }
 		    case "swaptime":
 		    {
 		    	setSwapTime(Integer.parseInt(elem.getText()));
 		    	break;
 		    }
 		    case "delaytime":
 		    {
 		    	setDelayTime(Integer.parseInt(elem.getText()));
 		    	break;
 		    }
 		    case "refreshtme":
 		    {
 		    	setRefreshTime(Integer.parseInt(elem.getText()));
 		    	break;
 		    }
 		    case "trmaxno":
 		    {
 		    	setTr_max_no(Integer.parseInt(elem.getText()));
 		    	break;
 		    }
 		    case "recmaxno":
 		    {
 		    	setUp_max_no(Integer.parseInt(elem.getText()));
 		    	break;
 		    }
 		    case "L3Reminder":
 		    {
 		    	setL3PGRmd(elem.getText());
 		    	break;
 		    }
 		    case "FTReport":
 		    {
 		    	setFtreport(elem.getText());
 		    	break;
 		    }
 		    case "MTGuide":
 		    {
 		    	setMtguide(elem.getText());
 		    	break;
 		    }
 		    case "Security":
 		    {
 		    	setSecurity(elem.getText());
 		    	break;
 		    }
 		    case "MTQA_Status":
 		    {
 		    	setMtQAstatus(elem.getText());
 		    	break;
 		    }
 		    case "Index":
 		    {
 		    	setIndex(elem.getText());
 		    	break;
 		    }
 		    case "transtime":
 		    {
 		    	setTranstime(elem.getText());
 		    	break;
 		    }
 		    default:
 		    	break;
 		    }
 		}
 		public void visit(Attribute attr){
 			System.out.println("attr: " + attr.getName());
 		}
 	}
 	
 	public String getMtQAstatus() {
 		return mtQAstatus;
 	}
 
 	public void setMtQAstatus(String mtQAstatus) {
 		this.mtQAstatus = mtQAstatus;
 	}
 	
 	public String getMtQAstatusurl() {
 		return mtQAstatusurl;
 	}
 
 	public void setMtQAstatusurl(String mtQAstatusurl) {
 		this.mtQAstatusurl = mtQAstatusurl;
 	}
 	
 	public String getSecurity() {
 		return security;
 	}
 
 	public void setSecurity(String security) {
 		this.security = security;
 	}
 	public String getLocalpath() {
 		return localpath;
 	}
 	public void setLocalpath(String localpath) {
 		this.localpath = localpath;
 	}
 	public String getLocalsrv() {
 		return localsrv;
 	}
 	public void setLocalsrv(String localsrv) {
 		this.localsrv = localsrv;
 	}
 	public Integer getDelayTime() {
 		return delayTime;
 	}
 	public void setDelayTime(Integer delayTime) {
 		this.delayTime = delayTime;
 	}
 	public Integer getSwapTime() {
 		return swapTime;
 	}
 	public void setSwapTime(Integer swapTime) {
 		this.swapTime = swapTime;
 	}
 	public Integer getRefreshTime() {
 		return refreshTime;
 	}
 	public void setRefreshTime(Integer refreshTime) {
 		this.refreshTime = refreshTime;
 	}
 	public int getTr_max_no() {
 		return tr_max_no;
 	}
 	public void setTr_max_no(int tr_max_no) {
 		this.tr_max_no = tr_max_no;
 	}
 	public int getUp_max_no() {
 		return up_max_no;
 	}
 	public void setUp_max_no(int up_max_no) {
 		this.up_max_no = up_max_no;
 	}
 	public String getMhweburl() {
 		return mhweburl;
 	}
 	public void setMhweburl(String mhweburl) {
 		this.mhweburl = mhweburl;
 	}
 	public String getHrheadingId() {
 		return hrheadingId;
 	}
 	public void setHrheadingId(String hrheadingId) {
 		this.hrheadingId = hrheadingId;
 	}
 	public String getHrexceptionId() {
 		return hrexceptionId;
 	}
 	public void setHrexceptionId(String hrexceptionId) {
 		this.hrexceptionId = hrexceptionId;
 	}
 	public void putTrackMap(String key, String value){
 		trackMap.put(key, value);
 	}
 	public LinkedHashMap<String, String> getTrackMap() {
 		return trackMap;
 	}
 	public void setTrackMap(LinkedHashMap<String, String> trackMap) {
 		this.trackMap = trackMap;
 	}
 
 	public String getFtreport() {
 		return ftreport;
 	}
 
 	public void setFtreport(String ftreport) {
 		this.ftreport = ftreport;
 	}
 
 	public String getMtguide() {
 		return mtguide;
 	}
 
 	public void setMtguide(String mtguide) {
 		this.mtguide = mtguide;
 	}
 
 	public HashSet<String> getWeblist() {
 		return weblist;
 	}
 
 	public void setWeblist(HashSet<String> weblist) {
 		this.weblist = weblist;
 	}
 	
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getPasswd() {
 		return passwd;
 	}
 
 	public void setPasswd(String passwd) {
 		this.passwd = passwd;
 	}
 	public String getIndex() {
 		return index;
 	}
 
 	public void setIndex(String index) {
 		this.index = index;
 	}
 	
 	public String getL3PGRmd() {
 		return l3PGRmd;
 	}
 
 	public void setL3PGRmd(String l3pgRmd) {
 		l3PGRmd = l3pgRmd;
 	}
 	public int getTranstime() {
 		return transtime;
 	}
 
 	public void setTranstime(String transtime) {
 		this.transtime = Integer.parseInt(transtime);
 	}
 }
