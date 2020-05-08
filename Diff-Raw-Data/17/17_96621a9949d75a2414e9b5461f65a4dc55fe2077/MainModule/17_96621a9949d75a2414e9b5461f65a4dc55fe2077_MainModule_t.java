 package me.ixin.prince;
 
 import org.nutz.mvc.annotation.IocBy;
 import org.nutz.mvc.annotation.Modules;
 import org.nutz.mvc.ioc.provider.ComboIocProvider;
 
 @Modules(scanPackage = true)
@IocBy(type = ComboIocProvider.class, args = {	"*org.nutz.ioc.loader.json.JsonLoader",
												"dao.js", 
												"*org.nutz.ioc.loader.annotation.AnnotationIocLoader", 
												"me.ixin.prince.post.dao.impl",
												"me.ixin.prince.post.service.impl",
												"me.ixin.prince.post.web.action"})
 public class MainModule {
 	
 }
