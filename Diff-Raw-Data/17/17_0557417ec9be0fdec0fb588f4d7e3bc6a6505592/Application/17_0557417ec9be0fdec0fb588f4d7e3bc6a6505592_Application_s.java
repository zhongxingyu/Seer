 package com.ormy;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
import android.util.Log;
 import dalvik.system.DexFile;
 
 public class Application extends android.app.Application {
 	protected static Database database = null;
 	public static Context dummyContext = null;
 
 	public void onCreate() {
 		super.onCreate();
 		dummyContext = this;
 		database = new Database(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Class<? extends Model<?>>> getModels(Context context) {
 		List<Class<? extends Model<?>>> res =
 		        new ArrayList<Class<? extends Model<?>>>();
 		try {
 			String path =
 			        context.getPackageManager().getApplicationInfo(
 			            context.getPackageName(), 0).sourceDir;
 			DexFile dexfile = new DexFile(path);
 			Enumeration<String> entries = dexfile.entries();
 			while (entries.hasMoreElements()) {
 				String name = entries.nextElement();
 				Class cls = null;
 				Class sc = null;
 				try {
 					cls =
 					        Class.forName(name, true, context.getClass()
 					            .getClassLoader());
 					sc = cls.getSuperclass();
 				} catch (ClassNotFoundException e) {
 					Util.Log(e);
 				}
 
 				if ((cls == null) || (sc == null)
 				        || (!cls.getSuperclass().equals(Model.class)))
 					continue;
 				res.add(cls);
 			}
 
 		} catch (Exception e) {
 			Util.Log(e);
 		}
 		return res;
 	}
 
 	public static Bundle getMetaData(Context context) {
 		PackageManager pm = context.getPackageManager();
 		try {
 			ApplicationInfo ai =
 			        pm.getApplicationInfo(context.getPackageName(), 128);
 			return ai.metaData;
 		} catch (Exception e) {
 			Util.Log(e);
 		}
 		return null;
 	}
 }
