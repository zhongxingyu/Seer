 package com.ted.xplatform.web;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.BooleanUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.context.annotation.ScannedGenericBeanDefinition;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
 import org.springframework.core.io.support.ResourcePatternResolver;
 import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
 import org.springframework.core.type.classreading.MetadataReader;
 import org.springframework.core.type.classreading.MetadataReaderFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.util.ClassUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.JsonRawValue;
 import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
 import com.google.common.collect.Maps;
 import com.ted.common.Constants;
 import com.ted.common.exception.BusinessException;
 import com.ted.common.util.ConfigUtils;
 import com.ted.common.util.JsonUtils;
 import com.ted.common.util.ResourceUtils;
 import com.ted.common.util.ServletUtils;
 
 /**
  * Ext Model 的java，根据反射得到Ext Model 需要配置packageScan,这个是用来 扫面pojo and vo
  * 需要用到CacheBuilder来增加缓存，类似动态sql ReloadableTemplateQLServiceImpl, 先把这个写好
  * 
  * <pre>
  *  要不要加缓存，如果是每次都是要反射，那不是要慢死,结果是：加。
  *  还有，如果model的js文件改了要能reload。CacaheBuilder要加expiredAfterWrite() //不管了，直接读取就是，返回js文件内容。
  *  关于反射，可以用PropertyValues.java，不用原生的javabean，因为scan出来的就是Resource,这个转为BeanDefinition容易些，并且可以获得PropertyValues.good
  * </pre>
  * <b>要求必须有module/SYS/model/XXX/XXX.js or model/SYS/store/XXX/XXX.js
  * 
  * 20130420 新增特性：
  * 访问并返回/controller/的js文件，PageResource就不用配置hasController了。咱不缓存。
  */
 
 @Controller
 @SuppressWarnings("all")
 public class ExtController {
     public static final String                               PREFIX_MODULE            = "module.";                                                     //要求的前缀
     public static final String                               STORE                    = "store";                                                       //要求的前缀
     public static final String                               MODEL                    = "model";                                                       //要求的前缀
 
     private static final Logger                              logger                   = LoggerFactory.getLogger(ExtController.class);
 
     private static final Map<Class<?>, String>               JSTYPE_BY_JAVACLASS_MAP  = new HashMap();
 
     private static Map<String, Js>                           jsCache                  = Maps.newConcurrentMap();
 
     private Boolean                                          prettyPrint              = null;
 
     private static final Map<String, AbstractBeanDefinition> beanMap                  = Maps.newConcurrentMap();                                       // 根据文件名，返回Java Bean的属性定义
 
     private boolean                                          loaded                   = false;                                                         // beanMap是否初始化
 
     static final String                                      DEFAULT_RESOURCE_PATTERN = "**/*.class";
 
     private ResourcePatternResolver                          resourcePatternResolver  = new PathMatchingResourcePatternResolver();
 
     private MetadataReaderFactory                            metadataReaderFactory    = new CachingMetadataReaderFactory(this.resourcePatternResolver);
 
     private static final String RESOURCE_INCLASSPATH_PREFIX                           = "META-INF/resources"; //发布的时候资源存放的路径
     
     static {
         JSTYPE_BY_JAVACLASS_MAP.put(String.class, "string");
         JSTYPE_BY_JAVACLASS_MAP.put(Character.TYPE, "string");
         JSTYPE_BY_JAVACLASS_MAP.put(Character.class, "string");
         JSTYPE_BY_JAVACLASS_MAP.put(Date.class, "date"); // 这种就默认YYYY-MM-DD, HH24:MM:SS,可以采用json的annotation,good
         JSTYPE_BY_JAVACLASS_MAP.put(DateTime.class, "date"); // 这种就默认YYYY-MM-DD, HH24:MM:SS,可以采用json的annotation,good
         JSTYPE_BY_JAVACLASS_MAP.put(LocalDateTime.class, "date"); // 这种就默认YYYY-MM-DD, HH24:MM:SS,可以采用json的annotation,good
         JSTYPE_BY_JAVACLASS_MAP.put(LocalDate.class, "date"); // 这种就默认YYYY-MM-DD, HH24:MM:SS,可以采用json的annotation,good
         JSTYPE_BY_JAVACLASS_MAP.put(Integer.class, "number");
         JSTYPE_BY_JAVACLASS_MAP.put(int.class, "int");
         JSTYPE_BY_JAVACLASS_MAP.put(Long.class, "number");
         JSTYPE_BY_JAVACLASS_MAP.put(long.class, "int");
         JSTYPE_BY_JAVACLASS_MAP.put(Float.class, "number");
         JSTYPE_BY_JAVACLASS_MAP.put(float.class, "number");
         JSTYPE_BY_JAVACLASS_MAP.put(Double.class, "number");
         JSTYPE_BY_JAVACLASS_MAP.put(double.class, "number");
         JSTYPE_BY_JAVACLASS_MAP.put(BigDecimal.class, "number");
         JSTYPE_BY_JAVACLASS_MAP.put(Boolean.class, "boolean");
         JSTYPE_BY_JAVACLASS_MAP.put(boolean.class, "boolean");
     }
 
     private void refreshBeanMap() {
         if (!isLoaded()) {
             String[] packageScans = ConfigUtils.getPackageScan();
             try {
                 for (String packageScan : packageScans) {
                     String packageScanPath = ResourcePatternResolver.CLASSPATH_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(packageScan) + "/" + DEFAULT_RESOURCE_PATTERN;
                     Resource[] resources = this.resourcePatternResolver.getResources(packageScanPath);
                     for (Resource resource : resources) {
                         MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                         ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                         sbd.setResource(resource);
                         sbd.setSource(resource);
                         String fileName = resource.getFilename();
                         fileName = fileName.substring(0, fileName.indexOf("."));
                         beanMap.put(fileName, sbd);
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         setLoaded(true);
     }
 
     public Boolean getPrettyPrint() {
         return prettyPrint;
     }
 
     public void setPrettyPrint(Boolean prettyPrint) {
         this.prettyPrint = prettyPrint;
     }
 
     public boolean isLoaded() {
         return loaded;
     }
 
     public void setLoaded(boolean loaded) {
         this.loaded = loaded;
     }
 
     private boolean isPrettyPrint() {
         if (this.prettyPrint == null)
             this.prettyPrint = BooleanUtils.toBooleanDefaultIfNull(ConfigUtils.isPrettyPrint(), true);
         return this.prettyPrint.booleanValue();
     }
 
     private String tab() {
         return isPrettyPrint() ? "\t" : "";
     }
 
     private String n() {
         return isPrettyPrint() ? "\r\n" : " ";
     }
 
     //tool method,先看磁盘文件，如果没有，则看classpath
     private Resource getResource(HttpServletRequest request, String path) throws Exception{
         Resource resource = ResourceUtils.getResource(request, path);
         if(null == resource){
            logger.debug("resource: " + path + " does not exist in disc.");
            resource = ResourceUtils.getResourceByClasspath(RESOURCE_INCLASSPATH_PREFIX + path);
            if(null == resource){
                logger.debug("resource: " + RESOURCE_INCLASSPATH_PREFIX + path + " does not exist in classpath.");
            }else{
                logger.debug("resource: " + RESOURCE_INCLASSPATH_PREFIX + path + " exist in classpath.");
            }
        }else{
            logger.debug("resource: " + path + " exist in disc.");
         }
         return resource;
     }
     
     @RequestMapping(value = { "/module/**/model/**/*.js" })
     @ResponseBody
     public String getModelJs(HttpServletRequest request, HttpServletResponse response) throws Exception {
         // 1 看看有没有js文件在磁盘，比较lastModified, 如果磁盘的新，则push to jsCache, then return
         // js string
         // 2 如果不在，看beanMap中存在根据FileName的定义否。不存在就报错.这个要refreshBeanMap() call
         // 3 然后看jsCache中是否有，有就返回，没有就根据beanMap中的BeanDefinition生成,放到缓存中。
         String path = getPath(request);
         String defineName = getDefineName(path);//defineName like 'SYS.model.WorkDay'
         Resource resource = getResource(request, path);
         if (resource != null && resource.exists()) {
             Js jsFromCache = jsCache.get(defineName);
             if (jsFromCache == null || resource.lastModified() > jsFromCache.lastModified) {
                logger.debug("model: " + path +" to load from resource.");
                 Js newJs = makeJsByResource(resource);
                 jsCache.put(defineName, newJs);
                 return newJs.text;
             } else {
                logger.debug("model: " + path +" to load from cache.");
                 return jsFromCache.text;
             }
         } else {
             Js jsFromCache = jsCache.get(defineName);
             if (jsFromCache != null) {
                logger.debug("model: " + path +" to load from cache, for the path does not exist in resource, cache from bean.");
                 return jsFromCache.text;
             } else {
                logger.debug("model: " + path +" does not exist in resource, need to reflect from bean");
                 refreshBeanMap();
                 AbstractBeanDefinition beanDef = beanMap.get(getShortName(defineName));
                 if (null == beanDef) {
                     throw new BusinessException(path + " could not be found!");
                 }
                 Js js = makeModelJsByBeanDef(beanDef, path);
                 jsCache.put(defineName, js);
                 return js.text;
             }
         }
     };
 
     /**
      * 注意：这个store中的api:{read需要在js端修改tab.store.getProxy().url = 'task/myMethod.json'; //<--Saki magic :)}
      */
     @RequestMapping(value = { "/module/**/store/**/*.js" })
     @ResponseBody
     public String getStoreJs(HttpServletRequest request, HttpServletResponse response) throws Exception {
         // 1 看看有没有js文件在磁盘，比较lastModified, 如果磁盘的新，则push to jsCache, then return
         // js string
         // 2 如果不在，看beanMap中存在根据FileName的定义否。不存在就报错.这个要refreshBeanMap() call
         // 3 然后看jsCache中是否有，有就返回，没有就根据beanMap中的BeanDefinition生成,放到缓存中。
         String path = getPath(request);
         String defineName = getDefineName(path);//defineName like 'SYS.model.WorkDay'
         Resource resource = getResource(request, path);
         if (resource != null && resource.exists()) {
             Js jsFromCache = jsCache.get(defineName);
             if (jsFromCache == null || resource.lastModified() > jsFromCache.lastModified) {
                logger.debug("store: " + path +" to load from resource.");
                 Js newJs = makeJsByResource(resource);
                 jsCache.put(defineName, newJs);
                 return newJs.text;
             } else {
                logger.debug("store: " + path +" to load from cache.");
                 return jsFromCache.text;
             }
         } else {
             Js jsFromCache = jsCache.get(defineName);
             if (jsFromCache != null) {
                logger.debug("store: " + path +" to load from cache, for the path does not exist in resource, cache from bean.");
                 return jsFromCache.text;
             } else {
                logger.debug("store: " + path +" does not exist in resource, need to reflect from bean");
                 refreshBeanMap();
                 AbstractBeanDefinition beanDef = beanMap.get(getShortName(defineName));
                 if (null == beanDef) {
                     throw new BusinessException(path + " could not be found!");
                 }
                 Js js = makeStoreJsByBeanDef(beanDef, path);
                 jsCache.put(defineName, js);
                 return js.text;
             }
         }
     };
 
     /**
      * 返回module 下的controller
      * 先load classpath，如果没有则load 磁盘文件
      */
     @RequestMapping(value = { "/module/**/controller/**/*.js" })
     @ResponseBody
     public void getControllerJs(HttpServletRequest request, HttpServletResponse response) throws Exception {
         // 1 看看有没有js文件在磁盘，有就返回，没有就返回空
         String path = getPath(request);
         
         //先看classpath里面有没有，对于路径有个要求：META-INF/resources/resources/module/**/controller/**/MESController.js
         Resource resource = getResource(request, path);
         if (resource != null) {
             //IOUtils.toString(resource.getInputStream());
             IOUtils.copy(resource.getInputStream(),response.getOutputStream());
         }
     };
 
     /**
      * defineName is FullName,like SYS.model.WorkDay
      * shortName like WorkDay
      */
     private String getShortName(String defineName) {
         Assert.notNull(defineName);
         return defineName.substring(defineName.lastIndexOf(".") + 1);
     }
 
     /**
      * path = /context/model/admin/User.js
      */
     private Js makeStoreJsByBeanDef(AbstractBeanDefinition beanDef, String path) throws ClassNotFoundException, LinkageError {
         String defineName = getDefineName(path); // SYS.model.WorkDay
         StringBuffer buf = new StringBuffer("Ext.define('").append(defineName).append("', {");
         buf.append(n()).append(tab()).append("extend : 'Ext.data.Store',");
         buf.append(n()).append(tab()).append("model : '").append(defineName.replaceAll(".store.", ".model.")).append("',");
         buf.append(n()).append(tab()).append("proxy : {");
         buf.append(n()).append(tab()).append(tab()).append("type : 'ajax',");
         //insert api:{read,update, delete,create}
         buf.append(n()).append(tab()).append(tab()).append("actionMethods : {");
         buf.append(n()).append(tab()).append(tab()).append(tab()).append("read : 'POST'");
         buf.append(n()).append(tab()).append(tab()).append("},");
         buf.append(n()).append(tab()).append(tab()).append("reader : {");
         buf.append(n()).append(tab()).append(tab()).append(tab()).append("type : 'json'");
         buf.append(",").append(n()).append(tab()).append(tab()).append(tab()).append("root : '").append(Constants.CONTENT).append("'");
         buf.append(",").append(n()).append(tab()).append(tab()).append(tab()).append("successProperty : '").append("success").append("'");
         buf.append(",").append(n()).append(tab()).append(tab()).append(tab()).append("totalProperty : '").append("totalElements").append("'");//妥协于spring-data PageImpl
         buf.append(n()).append(tab()).append(tab()).append("}");
         buf.append(n()).append(tab()).append("}");
         buf.append(n()).append("});");
 
         Js js = new Js();
         js.text = buf.toString();
         js.lastModified = new Date().getTime();
         return js;
     };
 
     /**
      * path = /context/model/admin/User.js
      */
     private Js makeModelJsByBeanDef(AbstractBeanDefinition beanDef, String path) throws ClassNotFoundException, LinkageError {
         String defineName = getDefineName(path); // SYS.model.WorkDay
         StringBuffer buf = new StringBuffer("Ext.define('").append(defineName).append("', {");
         buf.append(n()).append(tab()).append("extend : 'Ext.data.Model',");
         String className = beanDef.getBeanClassName();
         Class clazz = ClassUtils.forName(className);// beanDef.getBeanClass();
         //Field[] fields = clazz.getFields();// getFields(); getDeclaredFields
         //Collection<Field> fields = ReflectUtils.getFields(clazz);beanDef
         List<BeanPropertyDefinition> propertyDefinitions = JsonUtils.getClassPropertyDefinitions(clazz);
 
         if (propertyDefinitions != null && propertyDefinitions.size() > 0) {
             buf.append(n()).append(tab()).append("fields : [ ");
             int i = 0;
             for (BeanPropertyDefinition propertyDefinition : propertyDefinitions) {
                 Class returnType = propertyDefinition.getGetter().getRawType();
                 JsonProperty jsonProperty = propertyDefinition.getGetter().getAnnotated().getAnnotation(JsonProperty.class);
                 //JsonProperty jsonField = (JsonProperty) field.getAnnotation(JsonProperty.class);
                 String name = (jsonProperty == null) || (StringUtils.isEmpty(jsonProperty.value())) ? propertyDefinition.getName() : jsonProperty.value();
                 buf.append(i++ == 0 ? "{" : ", {");
                 buf.append(n()).append(tab()).append(tab()).append("name : '").append(name).append("',");
                 String type = toJsType(propertyDefinition);
                 String format = null;
                 if (type.equals("date")) {
                     format = "Y-m-d H:i:s";
                 }
                 buf.append(n()).append(tab()).append(tab()).append("type : '").append(type).append("'");
                 if ((!StringUtils.isEmpty(format)) && (!"none".equals(format)))
                     buf.append(",").append(n()).append(tab()).append(tab()).append("dateFormat : '").append(format).append("'");
                 buf.append(n()).append(tab()).append("}");
             }
             buf.append(" ]");
         }
         buf.append(n()).append("});");
         Js js = new Js();
         js.text = buf.toString();
         js.lastModified = new Date().getTime();
         return js;
     };
 
     /**
      * make model and store
      */
     private Js makeJsByResource(Resource resource) throws IOException {
         String jsText = IOUtils.toString(resource.getInputStream());
         return new Js().text(jsText).lastModified(resource.lastModified());
     }
 
     private static String toJsType(BeanPropertyDefinition propertyDefinition) {
         Class type = propertyDefinition.getGetter().getRawType();
         JsonRawValue jsonRawValue = propertyDefinition.getGetter().getAnnotated().getAnnotation(JsonRawValue.class);
 
         //JsonRawValue jsonRawValue = (JsonRawValue) field.getAnnotation(JsonRawValue.class);
         if ((jsonRawValue != null) && (jsonRawValue.value())) {
             return "auto";
         }
         //Class type = returnType.getType();
         if (JSTYPE_BY_JAVACLASS_MAP.containsKey(type)) {
             return (String) JSTYPE_BY_JAVACLASS_MAP.get(type);
         }
         if ((List.class.isAssignableFrom(type)) || (Map.class.isAssignableFrom(type)) || (Set.class.isAssignableFrom(type))) {
             return "auto";
         }
         return "string";
     }
 
     private String getPath(HttpServletRequest request) {
         //return "/" + ServletUtils.getUrlPath(request); //有2个/
         return ServletUtils.getUrlPath(request);
     }
 
     /**
      * GET  http://localhost:8080/xp/module/SYS/model/WorkDay.js?_dc=1361015478790 404 (Not Found)
      * Ext.define('..module.SYS.model.WorkDay', { 
      * result sample: SYS.model.WorkDay
      * <pre>
      * and result like admin.User.js
      * @param String type is model or store
      */
     private String getDefineName(String path) {
         String splitPath = StringUtils.replace(path, "/", ".");
         splitPath = splitPath.substring(0, splitPath.lastIndexOf("."));
         int index = splitPath.indexOf(PREFIX_MODULE);
         return splitPath.substring(index + 7, splitPath.length());
     }
 
     class Js {
         String text;
         long   lastModified;
 
         protected Js text(String t) {
             this.text = t;
             return this;
         }
 
         protected Js lastModified(long lm) {
             this.lastModified = lm;
             return this;
         }
     }
 
 }
