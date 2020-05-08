 package com.nekokittygames.modjam.UnDeath;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 
 import net.minecraftforge.common.Configuration;
 
 public class Configs {
 
 	@Retention(RetentionPolicy.RUNTIME)
 	private static @interface CfgId {
 		public boolean block() default false;
 	}
 	@Retention(RetentionPolicy.RUNTIME)
 	private static @interface CfgBool {}
 	
 	@Retention(RetentionPolicy.RUNTIME)
 	private static @interface CfgString {}
 	
 	public static void  load(Configuration config) {
 		try {
 			config.load();
 			Field[] fields = Configs.class.getFields();
 			for(Field field : fields) {
 				CfgId annotation = field.getAnnotation(CfgId.class);
 				if(annotation != null) {
 					int id = field.getInt(null);
 					if(annotation.block()){
 						id = config.getBlock(field.getName(), id).getInt();
 					}else{
 						id = config.getItem(field.getName(), id).getInt();
 					}
 					field.setInt(null, id);
 				} else {
 					if(field.isAnnotationPresent(CfgBool.class)){
 						boolean bool = field.getBoolean(null);
 						bool = config.get(Configuration.CATEGORY_GENERAL,
 								field.getName(), bool).getBoolean(bool);
 						field.setBoolean(null, bool);
 					}
 				}
 			}
 		} catch(Exception e) {
 			//failed to load configs log
 		} finally {
 			config.save();
 		}
 	}
 }
