 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.pb138.log4j.model;
 
 import cz.muni.pb138.log4j.AppUtils;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.dom4j.Element;
 
 /**
  *
  * @author jozef
  */
 public class ErrorHandler {
     private String className;
     private Map<String, String> params = new HashMap<String, String>();
     private List<String> loggers = new ArrayList<String>();
     private String root;
     private String appender;
 
     public String getClassName() {
         return className;
     }
 
     public void setClassName(String className) {
         this.className = className;
     }
 
     public Map<String, String> getParams() {
         return params;
     }
 
     public void addParam(String key, String value) {
         if(params.get(key) == null){
            checkErrHandParamSuported(key);
            params.put(key, value);
         }else{
             AppUtils.crash("ErrorHandler: '" + className + "' with two same params: " + key);
         }
     }
 
     public List<String> getLoggers() {
         return loggers;
     }
 
     public void addLogger(String logger) {
         if(!loggers.contains(logger)){
             loggers.add(logger);
         }else{
             AppUtils.crash("Two same logger-ref:'" + logger + "' in ErrorHanfler: " + className);
         }
     }
 
     public String getRoot() {
         return root;
     }
 
     public void setRoot(String root) {
         this.root = root;
     }
 
     public String getAppender() {
         return appender;
     }
 
     public void setAppender(String appender) {
         this.appender = appender;
     }
     
     public void setUpFromElement(Element element){
         className = element.attributeValue("class");
         
         for(Element e : (List<Element>) element.elements("param")){
             addParam(e.attributeValue("name"), e.attributeValue("value"));
         }
         
         if(element.element("root-ref") != null){
                 root = element.element("root-ref").attributeValue("ref");
         }
         
         for(Element e : (List<Element>) element.elements("logger-ref")){
             addLogger(e.attributeValue("ref"));
         }
         
         if(element.element("appender-ref") != null){
                 appender = element.element("appender-ref").attributeValue("ref");
         }
         
     }
     
     private void checkErrHandParamSuported(String name){
         
     }
     
     
     public List<String> toProperty(List<String> prop, String prefix) {
         prop.add(AppUtils.prefix(prefix + ".errorHandler = " + className));
         AppUtils.addParams(prop, prefix + ".errorHandler", params);
 
         //logger refs
        if(loggers.size() != 0) {
            String loggerRefs = AppUtils.join( loggers, ", ");
            prop.add(AppUtils.prefix(prefix + ".errorHandler.logger-ref = " + loggerRefs));
        }
        
        if(appender != null) {
            prop.add(AppUtils.prefix(prefix + ".errorHandler.appender-ref = " + appender));
        }
        
         
         return prop;
         }
 }
