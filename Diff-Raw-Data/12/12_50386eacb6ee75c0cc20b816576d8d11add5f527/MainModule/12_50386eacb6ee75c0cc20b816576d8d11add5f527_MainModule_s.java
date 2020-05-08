 package controllers;
 
 
 import org.nutz.mvc.annotation.IocBy;
 import org.nutz.mvc.annotation.Modules;
 import org.nutz.mvc.ioc.provider.ComboIocProvider;
 
 @Modules(scanPackage=true)
 @IocBy(type= ComboIocProvider.class,args={"*org.nutz.ioc.loader.json.JsonLoader","dao.js","*org.nutz.ioc.loader.annotation.AnnotationIocLoader","services","controllers"})
 public class MainModule {
 	
	public static int max = 20;
 	
 }
