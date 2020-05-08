 package com.sohu.wap;
 
 
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.ParseException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sohu.wap.core.Constants;
 import com.sohu.wap.http.HttpUtil4Exposer;
 import com.sohu.wap.util.DateUtil;
 import com.sohu.wap.util.NetSystemConfigurations;
 import com.sohu.wap.util.ThreadUtil;
 
 
 
 /**
  * 程序启动
  *
  */
 public class YueCheHelper 
 {
     private static Logger log = LoggerFactory.getLogger(YueCheHelper.class);
     
     
     
     public static  String  getImageCodeInputMethod(){
           
         String  IMAGE_CODE_INPUT_METHOD =NetSystemConfigurations.getSystemStringProperty("system.imagecode.inputmethod","auto") ;
         return IMAGE_CODE_INPUT_METHOD;
         
     }
     
     //遗留的，删除
     public static  boolean    IS_ENTER_CREAKER_MODEL = false;
     
     public static  boolean  isImageCodeInputMethodAuto(){
         boolean IMAGE_CODE_INPUT_METHOD_IS_AUTO = true;
         if(! getImageCodeInputMethod().equals("auto")){
             IMAGE_CODE_INPUT_METHOD_IS_AUTO = false;
         }
         return IMAGE_CODE_INPUT_METHOD_IS_AUTO;
     }
     
    
     public static boolean isEnterCreakerModel(){
         boolean    IS_ENTER_CREAKER_MODEL = NetSystemConfigurations.getSystemBooleanProperty("system.open.creak.model",false) ;; 
         return IS_ENTER_CREAKER_MODEL;
     }
     
     //遗留的，删除
     public static boolean IS_USE_PROXY= false;
     
     
     public static boolean isScanUseProxy(){
         
         boolean   IS_SCAN_USE_PROXY = NetSystemConfigurations.getSystemBooleanProperty("system.scan.use.proxy",false) ;; 
         
         return IS_SCAN_USE_PROXY;
     }
     
     
     public static boolean isUseProxy(){
         
         boolean   IS_USE_PROXY = NetSystemConfigurations.getSystemBooleanProperty("system.use.proxy",false) ;; 
         
         return IS_USE_PROXY;
     }
     
     
     public static int  getProxyNumPreUser(){
         
         int   PROXY_NUM_PER_USER = NetSystemConfigurations.getSystemIntProperty("system.proxy.num.per.user", 2);
         
         return PROXY_NUM_PER_USER;
     }
     
     public static int getThreadPerUser(){
     	
     	return NetSystemConfigurations.getSystemIntProperty("system.noproxy.num.per.user", 2);
         
     }
     
     
    
     private static Map<String, String>  IS_EXECUTE_TASK = new HashMap<String, String>();
     
     
     private static String[]  AM_PM_NUM ={"812","15","58"};
     private static String[]  AM_PM_STR={"上午","下午","晚上"};
     private static String[]  AM_PM_STR1={"sw","xw","ws"};
     private static String[]  AM_PM_STR2={"am","pm","ni"};
     public static Map<String, String> AMPM = new HashMap<String, String>();
     static  {
     	//初始化 ampm信息
     	for(int i =0; i< AM_PM_NUM.length; i++){
     		AMPM.put(AM_PM_NUM[i], AM_PM_STR[i]);
     		AMPM.put(AM_PM_STR[i], AM_PM_NUM[i]);
     		AMPM.put(AM_PM_STR1[i], AM_PM_NUM[i]);
     		AMPM.put(AM_PM_STR2[i], AM_PM_NUM[i]);
     	}
     	
     
     	
     	
     }
     
     public static  int   SCAN_MIN_INTEVAL = NetSystemConfigurations.getSystemIntProperty("system.scan.min.interval", 60);
     
     public static  int   SCAN_MAX_INTEVAL = NetSystemConfigurations.getSystemIntProperty("system.scan.max.interval", 180);
     
     public static  int   SCAN_MAX_SLEEP_TIME = NetSystemConfigurations.getSystemIntProperty("system.scan.maxsleeptime", 30);
 
     
     public static  int   MIN_SCAN_INTEVAL = NetSystemConfigurations.getSystemIntProperty("system.scan.min.interval", 60);
     
     public static  int   MAX_SCAN_INTEVAL = NetSystemConfigurations.getSystemIntProperty("system.scan.max.interval", 180);
     
     
     public static String YUCHE_TIME = NetSystemConfigurations.getSystemStringProperty("system.yueche.time","am,pm") ;
     
     public static  int   MAX_SLEEP_TIME = NetSystemConfigurations.getSystemIntProperty("system.maxsleeptime", 2);
  
    
     private static String    SERVICE_BEGIN_TIME ="07:34";
     private static String    SERVICE_END_TIME ="20:00";
 //
 //    private static String    SERVICE_BEGIN_TIME ="00:01";
 //    private static String   SERVICE_END_TIME ="23:59";
 //    
     
     public  static int  WAITTING_SCAN_INTERVAL = 5;
     
     public static  int LOGIN_SESSION_TIMEOUT_MILLISECOND  =  30 * 60 *1000;
     
     public static  int IMAGE_CODE_TIMEOUT_MILLISECOND  =  10 * 60 *1000;
     
     //网络重试次数
     public static int NET_RETRY_LIMIT = 100;

    public static int LOGIN_RETRY_LIMIT = 1000;
     
     public static  String   PROXY_IP = NetSystemConfigurations.getSystemStringProperty("system.proxy.ip", "127.0.0.1");
     
     public static  int   PROXY_PORT = NetSystemConfigurations.getSystemIntProperty("system.proxy.port", 8087);
     
    public static boolean   IS_USE_PROXY_BOOK_CAR = NetSystemConfigurations.getSystemBooleanProperty("system.use.proxy.bookcar",false) ;; 
    
    
    /**
     * 是否再约车的服务时间内
     * @return
     */
    public static   boolean isInServiceTime(){
     
 	   return  DateUtil.isCurrTimeInTimeInterval(SERVICE_BEGIN_TIME,SERVICE_END_TIME);
       
     } 
     
     /**
      *设置今天任务已经完成 
      * 
      */
     public static void setTodayTaskExecuteOver(){
         IS_EXECUTE_TASK.put(DateUtil.getNowDay(), "over");
     }
     
     
     
     /**
      * 
      *得到今日任务完成状态
      * 
      */
     public static boolean  isTodayTaskExecuteOver(){
         
         
         if (IS_EXECUTE_TASK.containsKey(DateUtil.getNowDay())){  //今日已经执行完毕
             return true;
         }
         
         return false;
         
     }
     
     
     
     public static  void waitForService() throws InterruptedException{
     	  
         do {
             //在服务时间内
             if (!YueCheHelper.isInServiceTime()){
             	System.out.println("waitting task begin!");
                 ThreadUtil.sleep(YueCheHelper.WAITTING_SCAN_INTERVAL);
             }else{
             	break;
             }
         }while (true);
     }
     
     
     
 //    public static void waiting(String carType) throws InterruptedException{
 //    	Date beginDate = DateUtil.getTodayTime(SERVICE_BEGIN_TIME);
 ////    	
 ////    	if ( Constants.CAR_TYPE_FK.equalsIgnoreCase(carType)  || Constants.CAR_TYPE_STN.equalsIgnoreCase(carType)){
 ////    		beginDate = DateUtil.getTodayTime(FK_YUECHE_BEGIN_TIME);
 ////    	}
 //    	long beginTime = beginDate.getTime();
 //    	
 //    	do {
 //    		long distance = beginTime - System.currentTimeMillis();
 //            //在服务时间内
 //    		System.out.println("等待...");
 //            if ( distance  >  YueCheHelper.MAX_SCAN_INTEVAL*1000 ){
 //            	ThreadUtil.sleep (YueCheHelper.MIN_SCAN_INTEVAL +RandomUtil.getRandomInt(YueCheHelper.MAX_SCAN_INTEVAL-YueCheHelper.MIN_SCAN_INTEVAL));
 //            	
 //            }else if ( distance > 10000 ){
 //            	ThreadUtil.sleep(MAX_SLEEP_TIME);
 //            }else{
 //            	break;
 //            }
 //        }while (true);
 //    }
     
     /**
      * 
      *得到用户的约车信息 
      * 
      */
     public static int  getYueCheBookInfo(int ycId){
     	int result = BookCarUtil.UNKNOWN_ERROR;
     	
     	try {
        	 JSONArray ycArray = new JSONArray();
        	 String ycURL =String.format(Constants.YUECHE_DETAIL_URL, ycId);
        	 
        	 String  ycInfo  = HttpUtil4Exposer.getInstance().getContent(ycURL);
        	 
        	 if(ycInfo != null){
        		 	ycArray  = new JSONArray(ycInfo);
    			
        	 }else{
        		 log.error("get yucheinfo from server error");
        		 result = BookCarUtil.SERVER_ERROR;
        		 return result;
        	 }
        	 
        	 for (int index =0; index < ycArray.length(); index ++){
        		
        		JSONObject yc =  ycArray.getJSONObject(index);
 
             YueCheItem xyAccount = YueCheItem.jsonToYueCheItem(yc);
        		log.debug(xyAccount.toString());
        		result = xyAccount.getYcResult();
        		
        	 }
        	} catch (JSONException e) {
    			log.error("load yueche account from net error", e);
    		}
        	
        	return result;
     }
     
     
     public static void updateYueCheBookInfo(int ycId, int ycResult, String ycResultInfo){
     	try {
     		
           	 JSONObject  param = new JSONObject();
           	 
           	 param.put("yc_result", ycResult);
           	 param.put("yc_info", ycResultInfo);
           	 
           	 
           	 String ycURL =String.format(Constants.YUECHE_UPDATE_URL, ycId);
           	
           	 String  ycInfo  = HttpUtil4Exposer.getInstance().getContent(ycURL,param);
 		  
           	 System.out.println(ycInfo);
           	 
           	} catch (JSONException e) {
       			log.error("error", e);
       		} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
           	
     }
     
     public static void updateYueCheBookInfo(String idNum,String yueCheDate, int ycResult, String ycResultInfo){
     	try {
     		
           	 JSONObject  param = new JSONObject();
           	 param.put("id_num", idNum);
           	 
           	 param.put("yc_date", yueCheDate);
           	 param.put("yc_result", ycResult);
           	 param.put("yc_info", ycResultInfo);
           	 
           	
           	 String  ycInfo  = HttpUtil4Exposer.getInstance().getContent(Constants.YUECHE_UPDATE_URL2,param);
 		  
           	 System.out.println(ycInfo);
           	 
           	} catch (JSONException e) {
       			log.error("error", e);
       		} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
           	
     }
     
     public static void addPrxoyHost(String proxyIp, String proxyPort){
         try {
             
              JSONObject  param = new JSONObject();
              
              param.put("ip", proxyIp);
              param.put("port", proxyPort);
              
              
              String  ycInfo  = HttpUtil4Exposer.getInstance().getContent(Constants.PROXY_ADD_URL,param);
           
              System.out.println(ycInfo);
              
             } catch (JSONException e) {
                 log.error("error", e);
             } catch (ParseException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
     }
     
     
     public static void main (String[] args){
 //    	waiting("fk");
     	System.out.println(getYueCheBookInfo(2));
 //    	updateYueCheBookInfo(2,0,"371312198511084844:Info:20121222:15-02015:20121222下午约车成功");
        
     }
 }
