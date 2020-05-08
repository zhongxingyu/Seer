 package com.ag.zhaisujie;
 
 import android.content.Context;
 import android.widget.Toast;
 /**
  *    ToastUtil.java
  *     <p>
  *     ʾʾ
  *     Copyright: Copyright(c) 2013 
  *     @author Gavin_Feng
  *     </p>
  * 
  */
 public class ToastUtil {
 	private static Toast toast;
 
 	public static void show(Context paramContext, int paramInt) {
		if (toast == null){
 			toast = Toast.makeText(paramContext, paramInt, 0);
		}else{
			toast.setText(paramInt);
 		}
		toast.show();
 	}
 
 	public static void show(Context paramContext, String paramString) {
		if (toast == null){
 			toast = Toast.makeText(paramContext, paramString, 0);
		}else{
			toast.setText(paramString);
 		}
		toast.show();
 	}
 }
