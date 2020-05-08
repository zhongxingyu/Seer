 package ml.core;
 
 import java.lang.annotation.Annotation;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 
 import cpw.mods.fml.common.FMLLog;
 
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 
 public abstract class Config {
 
 	@Retention(RetentionPolicy.RUNTIME)
 	protected static @interface prop {
 		public String category() default Configuration.CATEGORY_GENERAL;
 		public String comment() default "";
 		public String inFileName() default "";
 	}
 	
 	public abstract String getFailMsg();
 	
 	public void load(Configuration cfg) {
 		try {
 			for (Field fld : this.getClass().getFields()){
 				prop ann = fld.getAnnotation(prop.class);
 				if (ann != null){
 					Class type = fld.getType();
 					String propName = ann.inFileName().isEmpty() ? fld.getName() : ann.inFileName();
 					Property cProp = null;
 					if (type == boolean.class){
 						boolean def = fld.getBoolean(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.getBoolean(def));
 						
 					} else if (type == Integer.class){
 						Integer def = (Integer)fld.get(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.getInt(def));
 						
 					} else if (type == String.class){
 						String def = (String)fld.get(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.value);
 						
 					} else if (type == double.class){
 						double def = fld.getDouble(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.getDouble(def));
 						
 					// Lists
 					} else if (type == boolean[].class){
 						boolean[] def = (boolean[])fld.get(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.getBooleanList());
 						
 					} else if (type == int[].class){
 						int[] def = (int[])fld.get(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.getIntList());
 						
 					} else if (type == String[].class){
 						String[] def = (String[])fld.get(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.valueList);
 						
 					} else if (type == double[].class){
 						double[] def = (double[])fld.get(this);
 						cProp = cfg.get(ann.category(), propName, def);
 						fld.set(this, cProp.getDoubleList());
 					}
 					
 					if (cProp != null && !ann.comment().isEmpty())
 						cProp.comment = ann.comment();
 				}
 			}
 		} catch(Exception e) {
 			FMLLog.log(Level.SEVERE, e, getFailMsg());
 		} finally {
 			cfg.save();
 		}
 	}
 }
