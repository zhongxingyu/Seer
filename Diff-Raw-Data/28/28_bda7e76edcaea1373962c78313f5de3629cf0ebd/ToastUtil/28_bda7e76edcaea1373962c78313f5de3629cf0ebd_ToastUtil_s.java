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
		if (toast == null)
 			toast = Toast.makeText(paramContext, paramInt, 0);
		while (true) {
			toast.show();
			return;
 		}
 	}
 
 	public static void show(Context paramContext, String paramString) {
		if (toast == null)
 			toast = Toast.makeText(paramContext, paramString, 0);
		while (true) {
			toast.show();
			return;
 		}
 	}
 }
