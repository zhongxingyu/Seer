 package com.zhangwei.stock.gson;
 
 import java.util.ArrayList;
 
 public class StockList {
 	//////////////////////////////////////////
 	public ArrayList<String> shangzheng_list; //"sh600000", 上证
 	public ArrayList<String>  shenzheng_list;  //"sz000858", 深证
 	public ArrayList<String>  chuangye_list;   //"sz300005", 创业板
 	
 	public int status; //1 when ok
 	public long last_modify;  //列表上次更新时间
 	
 	/**
 	 *  {@literal 上次扫描的stockid， sh600031}
 	 * */
 	String lastScanID;
 	
 	/**
 	 *  {@literal 上次完成列表扫描的时间}
 	 * */
 	long lastScanTime;
 	////////////////////////////////////////////
 
 	public transient static String ID = "_StockList_";
 	private transient static int index = -1;
 	public transient static String TAIL = "tail";
 /*	public StockList(){
 		shangzheng_list = new ArrayList<String>();
 		shenzheng_list = new ArrayList<String>();
 		chuangye_list = new ArrayList<String>();
 		index = -1;
 		lastScanTime = 0;
 		lastScanID = null;
 	}*/
 	
 	private boolean seekTo(String stockID){
 		boolean found = false;
 		if(stockID.equals(TAIL)){
 			found = true;
 			index = shangzheng_list.size() + shenzheng_list.size() + chuangye_list.size();
 		}else{
 			if(shangzheng_list.contains(stockID)){
 				found = true;
 				index = shangzheng_list.indexOf(stockID);
 			}else if(shenzheng_list.contains(stockID)){
 				found = true;
				index = shangzheng_list.indexOf(stockID) + shangzheng_list.size();
 			}else if(chuangye_list.contains(stockID)){
 				found = true;
				index = shangzheng_list.indexOf(stockID) + shangzheng_list.size() + shenzheng_list.size();
 			}
 		}
 		
 		return found;
 	}
 	
 	public void rewind(){
 		index=0;
 		lastScanID = null;
 	}
 	
 	public void next(){
 		index++;
 		
 		int sh_size = shangzheng_list.size();
 		int sz_size = shenzheng_list.size();
 		int cyb_size = chuangye_list.size();
 		
 		if(index<sh_size){
 			lastScanID =  shangzheng_list.get(index);
 			//index++;
 		}else if(index<sh_size+sz_size){			
 			lastScanID =  shenzheng_list.get(index-sh_size);
 			//index++;
 		}else if(index<sh_size+sz_size+cyb_size){
 			lastScanID =  chuangye_list.get(index-sh_size-sz_size);
 			//index++;
 		}else{
 			//no-op
 			lastScanID = TAIL;
 		}
 	}
 	
 	public String getCurStockID(){		
 		return lastScanID;
 	}
 	
 	
 	public String getlastScanID(){
 		return lastScanID;
 	}
 	
 	public void setlastScanID(String id){
 		lastScanID = id;
 	}
 	
 	public long getlastScanTime(){
 		return lastScanTime;
 	}
 	
 	public void setlastScanTime(long time){
 		lastScanTime = time;
 	}
 	
 
 }
