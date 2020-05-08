 package com.yinheli.sxtcm;
 
 /**
  * 常量
  * 
  * @author yinheli <yinheli@gmail.com>
  *
  */
 public interface Constants {
 
 	/**
 	 * 版本号
 	 * 公布给用户查看的版本名称
 	 */
 	String VER = "1.0 beta";
 
 	/**
 	 * 版本id
 	 * 
 	 * 程序内部版本序号，值越大表示版本越高
 	 * 常用于比对版本号
 	 */
	int VER_ID = 0;
 
 	/**
 	 * 最新的app下载地址
 	 */
 	String LAST_APP_URL = "http://droidapi.sinaapp.com/sxtcm_news.apk";
 
 	/**
 	 * 检查最新版本号地址
 	 */
 	String CHECK_UPDATE_URL = "http://droidapi.sinaapp.com/sxtcm_news_update_check.php";
 
 	/**
 	 * 缓存目录名称
 	 */
 	String CACHE_FILE_DIR = "sxtcm-news";
 	
 	String TAG = "sxtcm";
 
 }
