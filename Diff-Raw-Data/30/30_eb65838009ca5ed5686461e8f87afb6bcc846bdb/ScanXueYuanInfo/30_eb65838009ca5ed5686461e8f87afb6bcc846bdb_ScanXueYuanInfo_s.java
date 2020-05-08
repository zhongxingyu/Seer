 /**
  *@version:2012-5-8-下午03:03:50
  *@author:jianjunwei
  *@date:下午03:03:50
  *
  */
 package com.sohu.wap;
 
 import com.sohu.wap.core.Constants;
 import com.sohu.wap.http.HttpUtil4Exposer;
 import com.sohu.wap.util.DateUtil;
 import com.sohu.wap.util.NetChangedReloadingStrategy;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 
 /**
  * 用户约车(*^__^*) 嘻嘻……
  * 
  * @author jianjunwei
  *
  */
 public class ScanXueYuanInfo {
     
     
     
     private static Logger  log = LoggerFactory.getLogger(ScanXueYuanInfo.class);
     
     private    ConcurrentHashMap <String , XueYuanAccount> xueYuanAccountMap = new ConcurrentHashMap<String, XueYuanAccount> ();
     
     private  static NetChangedReloadingStrategy strategy  = new NetChangedReloadingStrategy();
 
     public ScanXueYuanInfo(){
     	reload();
     }
     
     
   public ConcurrentHashMap  <String , XueYuanAccount> getXuanYuanInfo(){
	  		reload();
             return xueYuanAccountMap;
     }
  
   
     /**
      * 
      * 初始化 account 对象 
      *
      */
     private  synchronized void reload (){
 //    	 if (strategy.reloadingRequired()){
              loadScanYueCheInfos();
 
 //    	 }
     }
     
     private  synchronized  void loadScanYueCheInfos(){
     	try {
     	 JSONArray ycArray = new JSONArray();
 
          String  url = String.format(Constants.YUECHE_SCAN_URL )   ;
     	 String  ycInfo  = HttpUtil4Exposer.getInstance().getContent(url);
     	 if(ycInfo != null){
     		 	ycArray  = new JSONArray(ycInfo);
 			
     	 }else{
     		 log.error("get yuche info from server error");
     		 return;
     	 }
     	 
     	 for (int index =0; index < ycArray.length(); index ++){
     		
     		JSONObject yc =  ycArray.getJSONObject(index);
     		
     		 YueCheItem yueCheItem = YueCheItem.jsonToYueCheItem(yc);
              String  xueYuanId = yueCheItem.getXueYuanId();
 
              if(xueYuanAccountMap.containsKey( xueYuanId ) ){
 
                  XueYuanAccount old = xueYuanAccountMap.get(xueYuanId);
                  yueCheItem.addXueYuanDetailInfo(old);
                  old.addYueCheItem(yueCheItem);
 
 
              }else{
                  String xueYuanDetailUrl =   String.format(Constants.XUEYUAN_DETAIL_URL, Integer.valueOf( xueYuanId ) );
 
                  String  xueYuanDetailInfo  = HttpUtil4Exposer.getInstance().getContent(xueYuanDetailUrl);
 
                  if(xueYuanDetailInfo != null){
                      JSONArray  xueYuanArray  = new JSONArray(xueYuanDetailInfo);
                      XueYuanAccount xueYuanAccount = XueYuanAccount.jsonToXueYuanAccount(xueYuanArray.getJSONObject(0)) ;
                      yueCheItem.addXueYuanDetailInfo( xueYuanAccount);
                      xueYuanAccount.addYueCheItem(yueCheItem);
                      xueYuanAccountMap.put(xueYuanId , xueYuanAccount);
                  }
 
              }
 
     	 }
     	} catch (JSONException e) {
 			log.error("init yueche account from net error", e);
 		}
     	 log.info("initScanYueCheInfos over! size=" + xueYuanAccountMap.size());
     }
     
     
     
     public static void main(String [] args) {
 
         ScanXueYuanInfo scanXueYuanInfo = new ScanXueYuanInfo();
     }
 
     
    
     
 }
