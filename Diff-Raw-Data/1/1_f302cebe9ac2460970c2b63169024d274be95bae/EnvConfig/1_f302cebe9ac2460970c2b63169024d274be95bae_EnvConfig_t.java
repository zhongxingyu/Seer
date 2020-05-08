 
 package topshelf.utils.common.foreman;
 
 import java.io.File;
 import java.io.FileReader;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.beanutils.ConvertUtils;
 import org.apache.commons.beanutils.Converter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EnvConfig {
 	
 	static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
 	
 	public static <T> T init(Class<T> type) {
 
 		/*
 		 * create config pojo
 		 */
 		T config = null;
 		try {
 			config = type.newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException("Could not instantiate config type: " + type);
 		}
 
 		/*
 		 * register any custom converters
 		 */
 		Map<Class<?>,Converter> converterMap = new HashMap<Class<?>,Converter>();
 		EnvParamConverter epc = type.getAnnotation(EnvParamConverter.class);
 		addConverter(converterMap, epc);
 		EnvParamConverter.List epcList = type.getAnnotation(EnvParamConverter.List.class);
 		if (null != epcList) {
 			for (EnvParamConverter epci : epcList.value()) {
 				addConverter(converterMap, epci);
 			}
 		}
 		
 		/*
 		 * get values for each field and set to config pojo
 		 */
 		for (Field field : type.getDeclaredFields()) {
 			EnvParam envParam = field.getAnnotation(EnvParam.class);
 			if (null == envParam) continue;
 			String rawVal = System.getenv(envParam.value());
 			if (null == rawVal) {
 				initEnvFile();
 				rawVal = envFile.getProperty(envParam.value());
 			}
 			if (null == rawVal && !"".equals(envParam.defaultValue())) {
 				logger.debug("Using default value: " + envParam.defaultValue() +
 						" for param: " + envParam.value());
 				rawVal = envParam.defaultValue();
 			}
 
 			if (null == rawVal) {
 				if (envParam.required()) {
 					dumpRequiredParams(type);
 					throw new RuntimeException("Could not configure env param: " +
 							envParam.value());
 				} else{
 					rawVal = envParam.defaultValue();
 				}
 			}
 			
 			try {
 				if (null == ConvertUtils.lookup(field.getType())) {
 					Converter c = converterMap.get(field.getType());
 					if (null == c) {
 						throw new RuntimeException("Could not find Converter for type: " +
 								field.getType());
 					}
 					Object convertedVal = c.convert(field.getType(), rawVal);
 					BeanUtils.setProperty(config, field.getName(), convertedVal);
 					
 				} else {
 					BeanUtils.setProperty(config, field.getName(), rawVal);
 				}
 			} catch (Exception e) {
 				throw new RuntimeException("Could not set field [" + field.getName() +
 						"] to value: [" + rawVal + "]", e);
 			}
 		}
 
 		return config;
 	}
 
 	private static Properties envFile;
     private static String home = System.getProperty("user.home");
 	
 	private synchronized static void initEnvFile() {
 
 		if (null != envFile) return;
         envFile = new Properties();
 
         /**
          * if our project is at /Users/bob/workspace/projectgroup/project-app,
          * and we launch our JVM from there, our projectName will resolve to
          * 'project-app'
          */
         String cwd = System.getProperty("user.dir");
         File cwdDir = new File(cwd);
         File env = getEnvFromCwd(cwdDir);
        if (!env.canRead()) return;
         
         try {
         	envFile.load(new FileReader(env));
         	logger.debug("Loaded file [" + env + "]: " + envFile.toString());
         } catch (Exception e) {
         	throw new RuntimeException("Could not open file: " + env, e);
         }
 	}
 	
 	private static File getEnvFromCwd(File cwdDir) {
 
         /**
          * with our projectName, look for a local env file at:
          * ~/.foreman-envs/project-app.env
          */
         String projectName = cwdDir.getName();
         File env = new File(home + "/.foreman-envs/" + projectName + ".env");
         return env;
 	}
 	
 	private static void addConverter(Map<Class<?>,Converter> converterMap, EnvParamConverter epc) {
 		
 		if (null != epc) {
 			try {
 				Converter c = epc.converter().newInstance();
 				converterMap.put(epc.type(), c);
 				
 			} catch (Exception e) {
 				throw new RuntimeException("Could not create converter: " +
 						epc.converter(), e);
 			}
 		}
 	}
 	
 	protected static <T> void dumpRequiredParams(Class<T> type) {
 		for (Field field : type.getDeclaredFields()) {
 			EnvParam envParam = field.getAnnotation(EnvParam.class);
 			if (null != envParam && envParam.required() && "".equals(envParam.defaultValue())) {
 				logger.error("Required EnvParam: " + field.getName()
 						+ " : " + field.getType());
 			}
 		}
 	}
 }
