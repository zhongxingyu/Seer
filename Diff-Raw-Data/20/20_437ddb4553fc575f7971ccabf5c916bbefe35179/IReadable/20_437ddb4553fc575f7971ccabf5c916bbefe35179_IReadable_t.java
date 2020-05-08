 /**
  *
  * @desc: IReadable means iterface to acquire Server Information
  * 
  */
 package com.sohu.wuhan;
 
 import java.util.Hashtable;
 
 import android.os.Handler;
 
 import com.sohu.wuhan.Constant.Error;
 
 /**
 * @author Leon
  * 
  */
 public interface IReadable {
 	boolean init(String __token);
 	Error getError();
 	void cancel();
 	
 	/* ======================================================================= */
 	
 	Error createArticle(String __url); // C
 
 	String probeArticle(); // P
 
 	String readArticle(String key); // R
 
 	Error updateArticle(String key, Hashtable<String, String> args); // U
 
 	Error deleteArticle(String key); // D
 	
 	/* article ======================================================================= */
 
 	boolean asyncCreateArticle(String __url, Handler handler);
 
 	boolean asyncProbeArticle(Handler handler);
 	
 	boolean asyncReadArticle(String key, Handler handler);
 	
 	boolean asyncUpdateArticle(String key, Hashtable<String, String> args, Handler handler); 
 		
 	boolean asyncDeleteArticle(String key, Handler handler); 
 	
 	// articleӿһɲṩ
 	/* category ======================================================================= */
 	
 	
 }
