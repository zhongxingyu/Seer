 package org.eweb4j.config;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.eweb4j.cache.ActionConfigBeanCache;
 import org.eweb4j.cache.DBInfoConfigBeanCache;
 import org.eweb4j.cache.IOCConfigBeanCache;
 import org.eweb4j.cache.ORMConfigBeanCache;
 import org.eweb4j.cache.Props;
 import org.eweb4j.cache.SingleBeanCache;
 import org.eweb4j.config.bean.ConfigBean;
 import org.eweb4j.config.bean.ListenerBean;
 import org.eweb4j.config.bean.Listeners;
 import org.eweb4j.ioc.config.IOCConfig;
 import org.eweb4j.mvc.config.ActionAnnotationConfig;
 import org.eweb4j.mvc.config.ActionConfig;
 import org.eweb4j.mvc.config.InterceptorAnnotationConfig;
 import org.eweb4j.mvc.config.InterceptorConfig;
 import org.eweb4j.orm.DBType;
 import org.eweb4j.orm.config.ORMConfig;
 import org.eweb4j.orm.config.PojoAnnotationConfig;
 import org.eweb4j.orm.dao.DAOFactory;
 import org.eweb4j.orm.dao.config.DAOConfig;
 import org.eweb4j.orm.dao.config.bean.DBInfoConfigBean;
 import org.eweb4j.orm.dao.update.UpdateDAO;
 import org.eweb4j.orm.jdbc.transaction.Trans;
 import org.eweb4j.orm.jdbc.transaction.Transaction;
 import org.eweb4j.orm.sql.Model2Table;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.FileUtil;
 import org.eweb4j.util.xml.BeanXMLUtil;
 import org.eweb4j.util.xml.XMLReader;
 import org.eweb4j.util.xml.XMLWriter;
 
 /**
  * EWeb4J配置
  * 
  * @author cfuture.aw
  * @since v1.a.0
  * 
  */
 public class EWeb4JConfig {
 
 	private static Log log = LogFactory.getLogger(EWeb4JConfig.class, true);
 
 	public synchronized static String start() {
 		return start(ConfigConstant.START_FILE_NAME);
 	}
 
 	public synchronized static String start(String fileName) {
 		setSTART_FILE_NAME(fileName);
 
 		return startByAbFile(ConfigConstant.START_FILE_PATH());
 	}
 
 	/**
 	 * 通过ConfigBean对象启动
 	 * @date 2013-1-7 下午12:31:26
 	 * @param config
 	 * @return
 	 */
 	public synchronized static String start(ConfigBean config) {
 		String error = _start(null, config);
 		_done(error);
 		return error;
 	}
 	
 	private synchronized static String startByAbFile(String aStartXmlPath) {
 		String startXmlPath = aStartXmlPath;
 		String error = null;
 		File file = null;
 		boolean readXml = true;
 		if (ConfigConstant.SUCCESS_START.equals(String.valueOf(SingleBeanCache.get(ConfigConstant.SUCCESS_START)))) {
 			ConfigBean cb = (ConfigBean) SingleBeanCache.get(ConfigBean.class.getName());
 
 			String reload = (cb == null ? "true" : cb.getReload());
 			if ("true".equals(reload) || "1".equals(reload)) {
 				// 如果开了DEBUG,清空缓存，重新读取配置文件
 				SingleBeanCache.clear();
 				ORMConfigBeanCache.clear();
 				IOCConfigBeanCache.clear();
 				ActionConfigBeanCache.clear();
 				log.debug("EWeb4J clear cache");
 				readXml = true;
 			} else {
 				// 否则，不需要读取配置文件
 				readXml = false;
 			}
 		}
 
 		if (readXml) {
 			// 1.读取配置文件
 			try {
 				file = new File(startXmlPath);
 				ConfigBean cb = null;
 				boolean readFile = true;
 				final String check = ConfigConstant.CHECK_START_FILE_EXIST;
 				if (!file.exists()){
 					if (!"true".equals(check) && !"1".equals(check)){
 						log.warn("Skip the Start Configuation file !!!");
 						cb = ConfigBeanCreator.create();
 						readFile = false;
 					}
 				}
 				
 				if (readFile){
 					XMLReader reader = BeanXMLUtil.getBeanXMLReader(file);
 					reader.setBeanName("eweb4j");
 					reader.setClass("eweb4j", ConfigBean.class);
 					cb = reader.readOne();
 				}
 				
 				error = _start(error, cb);
 			} catch (Throwable e) {
 				// 重写配置文件
 				try {
 					// 保存为备份文件
 					FileUtil.copy(file, new File(startXmlPath + ".back" + "_" + CommonUtil.getNowTime("MMddHHmmss")));
 					XMLWriter writer = BeanXMLUtil.getBeanXMLWriter(file,ConfigBeanCreator.create());
 					writer.setBeanName("eweb4j");
 					writer.setClass("eweb4j", ConfigBean.class);
 					writer.write();
 					String info = "configuration error, now it has repaired.";
 					error = CommonUtil.getNowTime() + "EWeb4JConfig : " + info + "exception：" + CommonUtil.getExceptionString(e);
 
 					log.error(info, e);
 				} catch (Throwable e1) {
 					String info = "can not write any configuration";
 					error = CommonUtil.getNowTime() + "EWeb4JConfig : " + info + "exception：" + CommonUtil.getExceptionString(e1);
 					log.fatal(info, e);
 				}
 			}
 			
 			_done(error);
 		}// end if (readXml)
 
 		return error;
 	}
 
 	private static void _done(String error) {
 		if (error != null) {
 			SingleBeanCache.clear();
 			ORMConfigBeanCache.clear();
 			IOCConfigBeanCache.clear();
 			ActionConfigBeanCache.clear();
 
 			log.error(error);
 		} else {
 			SingleBeanCache.add(ConfigConstant.SUCCESS_START, ConfigConstant.SUCCESS_START);
 		}
 	}
 
 	private static String _start(String error, ConfigBean cb) {
 		if (cb == null) {
 			error = " can not read any configuration info! But now have bean repaired, please restart.";
 		} else {
 			StringBuilder infos = new StringBuilder("EWeb4JConfig.start \n");
 			infos.append("start-config-xml-path --> ").append(ConfigConstant.START_FILE_PATH()).append("\n");
 			infos.append("${RootPath} --> ").append(ConfigConstant.ROOT_PATH).append("\n");
 			infos.append(cb).append("\n");
 
 			log.debug(infos.toString());
 
 			// 检查配置信息格式是否填写正确
 			String error1 = CheckConfigBean.checkEWeb4JConfigBean(cb);
 			if (error1 != null)
 				error = error1;
 
 			String error2 = CheckConfigBean.checkEWeb4JIOCPart(cb.getIoc());
 			if (error2 != null)
 				if (error == null)
 					error = error2;
 				else
 					error += error2;
 
 			String error3 = CheckConfigBean.checkIOCXml(cb.getIoc().getIocXmlFiles());
 			if (error3 != null)
 				if (error == null)
 					error = error3;
 				else
 					error += error3;
 
 			String error4 = CheckConfigBean.checkEWeb4JORMPart(cb.getOrm());
 			if (error4 != null)
 				if (error == null)
 					error = error4;
 				else
 					error += error4;
 
 			String error5 = CheckConfigBean.checkORMXml(cb.getOrm().getOrmXmlFiles());
 			if (error5 != null)
 				if (error == null)
 					error = error5;
 				else
 					error += error5;
 
 			String error6 = CheckConfigBean.checkEWeb4JMVCPart(cb.getMvc());
 			if (error6 != null)
 				if (error == null)
 					error = error6;
 				else
 					error += error6;
 
 			String error7 = CheckConfigBean.checkMVCActionXmlFile(cb.getMvc().getActionXmlFiles());
 			if (error7 != null)
 				if (error == null)
 					error = error7;
 				else
 					error += error7;
 
 			String error8 = CheckConfigBean.checkInter(cb.getMvc().getInterXmlFiles());
 			if (error8 != null)
 				if (error == null)
 					error = error8;
 				else
 					error += error8;
 
 			if (error == null) {
 				// 验证通过，将读取到的信息放入缓存池中
 				SingleBeanCache.add(ConfigBean.class.getName(), cb);
 				// ------log-------
 				String info = "EWeb4J start configuration info have bean validated and pushed to the cache. ";
 				log.debug(info);
 				// ------log-------
 				// 继续验证其他组件配置信息
 				// properties
 				String error13 = null;
 				try {
 					for (org.eweb4j.config.bean.Prop f : cb.getProperties().getFile()) {
 						error13 = Props.readProperties(f, true);
 						if (error13 != null)
 							if (error == null)
 								error = error13;
 							else
 								error += error13;
 					}
 				} catch (Throwable e) {
 					log.warn(e.toString(), e);
 					if (error == null)
 						error = e.toString();
 					else
 						error += e.toString();
 				}
 				
 				if (error == null)
 					log.debug("properties module -> ok");
 				
 				if (error == null && ("true".equals(cb.getIoc().getOpen()) || "1".equals(cb.getIoc().getOpen()))) {
 					String error10 = IOCConfig.check();
 					if (error10 != null)
 						error = error10;
 					
 					if (error == null)
 						log.debug("ioc module -> ok");
 				}// end ioc module
 				
 				if (error == null && ("true".equals(cb.getOrm().getOpen()) || "1".equals(cb.getOrm().getOpen()))) {
 					// check the db connection
 					String error14 = DAOConfig.check();
 					if (error14 != null)
 						error = error14;
 					
 					if (error == null){
 						log.debug("orm.dao module -> ok");
 					
 						// read jpa annotation
 						String error10 = new PojoAnnotationConfig().readAnnotation(cb.getOrm().getScanPojoPackage().getPath());
 						if (error10 != null)
 							error = error10;
 						
 						if (error == null){
 							log.debug("orm.pojo.annotation module -> ok");
 							
 							// read orm xml file
 							String error11 = ORMConfig.check();
 							if (error11 != null)
 								error = error11;
 							
 							if (error == null)
 								log.debug("orm.pojo.xml module -> ok");
 								
 							// generate ddl
 							if (error == null && ("true".equals(cb.getOrm().getDdl().getGenerate()) || "1".equals(cb.getOrm().getDdl().getGenerate()))) {
 								log.debug("ddl.generate -> true");
 								DBInfoConfigBean dcb = DBInfoConfigBeanCache.get(cb.getOrm().getDdl().getDs());
 								if (DBType.MYSQL_DB.equals(dcb.getDataBaseType())) {
									File sqlFile = new File(ConfigConstant.CONFIG_BASE_PATH() + cb.getOrm().getDdl().getDs()+ "-create.sql");
 									if ("1".equals(cb.getOrm().getDdl().getOverride()) 
 											|| "true".equals(cb.getOrm().getDdl().getOverride()) 
 											|| !sqlFile.exists()) {
 										
 										String sql = Model2Table.generate();
 										if (sql == null) {
 											error = "can not generate ddl file";
 										} else
 											log.debug("ddl.generate execute success -> " + sqlFile.getAbsolutePath());
 									} else {
 										log.warn("ddl.generate do not need to execute ->" + sqlFile.getAbsolutePath() + " is exists !");
 									}
 								} else {
 									log.warn("sorry only mysql db can use the ddl feature !");
 								}
 								
 								if (error == null)
 									log.debug("orm.ddl.generate module -> ok");
 							}// end orm.ddl.generate
 							
 							// run ddl
 							if (error == null && ("true".equals(cb.getOrm().getDdl().getRun()) || "1".equals(cb.getOrm().getDdl().getRun()))) {
 								log.debug("ddl.run -> true");
 								DBInfoConfigBean dcb = DBInfoConfigBeanCache.get(cb.getOrm().getDdl().getDs());
 								if (dcb != null) {
 									if (DBType.MYSQL_DB.equals(dcb.getDataBaseType())) {
 										File sqlFile = new File(ConfigConstant.CONFIG_BASE_PATH() + cb.getOrm().getDdl().getDs() + "-create.sql");
 										StringBuilder builder = new StringBuilder();
 
 										BufferedReader sql_reader = null;
 										try {
 											sql_reader = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFile)));
 											String line = null;
 											while ((line = sql_reader.readLine()) != null) {
 												builder.append(line);
 											}
 											final String[] sqls = builder.toString().split(";");
 
 											final UpdateDAO dao = DAOFactory.getUpdateDAO(cb.getOrm().getDdl().getDs());
 											Transaction.execute(new Trans() {
 												public void run(Object... args) throws Exception {
 													for (String sql : sqls) {
 														log.debug("ddl run -> "+ sql);
 														dao.updateBySQL(sql);
 													}
 												}
 											});
 
 										} catch (Exception e) {
 											String _error13 = CommonUtil.getExceptionString(e);
 											if (_error13 != null)
 												error = _error13;
 										} finally {
 											if (sql_reader != null) {
 												try {
 													sql_reader.close();
 												} catch (IOException e) {
 													e.printStackTrace();
 												}
 											}
 										}
 									} else {
 										log.warn("sorry only mysql db can use the ddl feature !");
 									}
 								} else {
 									log.error("ddl.ds -> " + cb.getOrm().getDdl().getDs() + " not found !");
 								}
 								
 								if (error == null)
 									log.debug("orm.ddl.run module -> ok");
 								
 							}//end orm.ddl.run
 						}// end if error == null -> pojo.annotation
 						
 					}// end dao ok
 					
 					if (error == null)
 						log.debug("orm module -> ok ");
 				}// end orm module
 				
 				if (error == null && ("true".equals(cb.getMvc().getOpen())|| "1".equals(cb.getMvc().getOpen()))) {
 					String error20 = new ActionAnnotationConfig().readAnnotation(cb.getMvc().getScanActionPackage().getPath());
 					if (error20 != null)
 						error += error20;
 					
 					if (error == null){
 						log.debug("mvc.action.annotation module -> ok");
 					
 						String error11 = ActionConfig.check();
 						if (error11 != null)
 							error = error11;
 						if (error == null){
 							log.debug("mvc.action.xml module -> ok");
 							
 							String error12 = new InterceptorAnnotationConfig().readAnnotation(cb.getMvc().getScanInterceptorPackage().getPath());
 							if (error12 != null)
 								error = error12;
 							if (error == null){
 								log.debug("mvc.action.interceptor.annotation module -> ok");
 								
 								String error21 = InterceptorConfig.check();
 								if (error21 != null)
 									error = error21;
 								if (error == null)
 									log.debug("mvc.action.interceptor.xml module -> ok");
 							}// end if interceptor annotation ok
 						}// end if action xml ok
 					}// end if action annotation ok
 					
 					if (error == null)
 						log.debug("mvc module -> ok");
 				}// end mvc module
 			
 				//CallBack after startup
 				Listeners listeners = cb.getListeners();
 				if (listeners != null && listeners.getListener() != null && !listeners.getListener().isEmpty()) {
 					for (ListenerBean lb : listeners.getListener()){
 						String clazz = lb.getClazz();
 						try {
 							EWeb4JListener listener = (EWeb4JListener)CommonUtil.loadClass(clazz).newInstance();
 							listener.onStartup();
 							log.debug("listener->"+listener+".onStartup execute...");
 						} catch (Throwable e) {
 							e.printStackTrace();
 						} 
 					}
 				}
 				
 			}// end if error == null
 			
 		}// end else cb != null
 		
 		return error;
 	}
 
 	public static void createStartXml(String path, ConfigBean cb) throws Exception {
 		XMLWriter writer = BeanXMLUtil.getBeanXMLWriter(new File(ConfigConstant.CONFIG_BASE_PATH() + path), cb);
 		writer.setBeanName("eweb4j");
 		writer.setClass("eweb4j", ConfigBean.class);
 		writer.write();
 	}
 
 	public static String about() {
 		return "EWeb4J Framework 1.10.1-SNAPSHOT";
 	}
 
 	public static void setROOT_PATH(String path){
 		ConfigConstant.setROOT_PATH(path);
 	}
 	
 	public static void setSTART_FILE_NAME(String START_FILE_NAME) {
 		if (START_FILE_NAME == null || START_FILE_NAME.trim().length() == 0)
 			return;
 		ConfigConstant.START_FILE_NAME = START_FILE_NAME;
 	}
 
 	public static void setCONFIG_BASE_PATH(String CONFIG_BASE_PATH) {
 		if (CONFIG_BASE_PATH == null || CONFIG_BASE_PATH.trim().length() == 0)
 			return;
 		ConfigConstant.CONFIG_BASE_PATH = CONFIG_BASE_PATH;
 	}
 	
 	public static void setCHECK_START_FILE_EXIST(String CHECK_START_FILE_EXIST){
 		if (CHECK_START_FILE_EXIST == null || CHECK_START_FILE_EXIST.trim().length() == 0)
 			return;
 		ConfigConstant.CHECK_START_FILE_EXIST = CHECK_START_FILE_EXIST;
 	}
 }
