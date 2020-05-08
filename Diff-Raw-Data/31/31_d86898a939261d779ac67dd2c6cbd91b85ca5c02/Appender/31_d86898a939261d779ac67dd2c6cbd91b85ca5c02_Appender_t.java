 package cz.muni.pb138.log4j.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dom4j.Element;
 
 import cz.muni.pb138.log4j.AppUtils;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import org.apache.log4j.Level;
 
 public class Appender {
     private String name;
     private String className;
     private String layoutClassName = null;
     private boolean hasLayoutAlready = false;
     private String threshold;
     private Map<String, String> params = new HashMap<String, String>();
     private Map<String, String> layoutParams = new HashMap<String, String>();
     private ErrorHandler errorHandler;
     private List<String> appenderRefs = new ArrayList<String>();
     
     // nutne kôli (podla mna nepotrebnemu) logovaniu vo verify, martin by mohol povedat ci to nezmazeme
     private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Appender.class);
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getClassName() {
         return className;
     }
 
     public void setClassName(String className) {
         this.className = className;
     }
     
     public String getLayoutClassName() {
         return layoutClassName;
     }
 
     public void setLayoutClassName(String layoutClassName) {
         this.layoutClassName = layoutClassName;
     }
 
     public String getThreshold() {
         return threshold;
     }
 
     public Map<String, String> getLayoutParams() {
         return layoutParams;
     }
 
     public void addLayoutParam(String key, String value){
         if(!checkLayoutParamSuported(key)) AppUtils.crash("Unsuported layout param: " + key + " in layout: " + layoutClassName);
         
         if(layoutParams.get(key) == null){
              checkLayoutParamSuported(key);
              layoutParams.put(key, value);
         }else{
             AppUtils.crash("Appender: '" + name + "' with two same layout params: " + key);
         }
     }
     
     public Map<String, String> getParams() {
         return params;
     }
     
     public void addParam(String key, String value){
         
         if(!checkParamSuported(key)) AppUtils.crash("Unsuported param: " + key + " in appender: " + name);
         
         if(params.get(key) == null){
            params.put(key, value);
         }else{
             AppUtils.crash("Appender: '" + name + "' with two same params: " + key);
         }
     }
 
     public ErrorHandler getErrorHandler() {
         return errorHandler;
     }
 
     public void setErrorHandler(ErrorHandler errorHandler) {
         this.errorHandler = errorHandler;
     }
     
     public List<String> getAppenderRefs() {
         return appenderRefs;
     }
     
     public void addAppenderRef(String ref){
         if(!appenderRefs.contains(ref)){
             appenderRefs.add(ref);
         }else{
             AppUtils.crash("Appender: '" + name + "' with two same appender-ref: " + ref);
         }
     }
 
     public void addConfig(String key, String value) {
         if (key.equals(name)) {
             className = value;
         } else {
             String newKey = key.substring(name.length() + 1); // removing appenderName. from key
             if(newKey.startsWith("layout")) {               
                 // each appender has max. one "private" layout
                 String layout = newKey.substring(6);      // removing "layout" part without "."
                 if(layout.contains(".")) {
                     layoutParams.put(layout.substring(1), value);
                 } else {
                     if(hasLayoutAlready) {
                         AppUtils.crash("This appender already has a layout defined.");
                     }
                     layoutClassName = value;
                     hasLayoutAlready = true;
                 }              
             } else {
                 params.put(newKey, value);
                 // those values are Bean-type based, they are
                 // params: file, append, buffersize, target... it also
                 // depends on appender class :((
             }
         }
     }
 
     public Element addThisToElement(Element rootElement) {
         Element appenderElement = rootElement.addElement("appender");
         appenderElement.addAttribute("name", name);
         appenderElement.addAttribute("class", className);
         for (String paramKey : params.keySet()) {
             Element paramElement = appenderElement.addElement("param");
             paramElement.addAttribute("name", paramKey);
             paramElement.addAttribute("value", params.get(paramKey));
         }
         
         if(layoutClassName != null) {
             Element layoutElement = appenderElement.addElement("layout");
             layoutElement.addAttribute("class", layoutClassName);
             for (String layoutParamKey : layoutParams.keySet()) {
                 Element layoutParamElement = layoutElement.addElement("param");
                 layoutParamElement.addAttribute("name", layoutParamKey);
                 layoutParamElement.addAttribute("value", layoutParams.get(layoutParamKey));
             }
         }
         return rootElement;
     }
     
     public void setUpFromElement(Element element){
         name = element.attributeValue("name");
         className = element.attributeValue("class");
         
         if(element.element("errorHandler") != null){
             errorHandler = new ErrorHandler();
             errorHandler.setUpFromElement(element.element("errorHandler"));
         }
         
         for(Element e : (List<Element>) element.elements("param")){
             addParam(e.attributeValue("name"), e.attributeValue("value"));
         }
         
         if(element.element("layout") != null) {
             layoutClassName = element.element("layout").attributeValue("class");
             for(Element e : (List<Element>) element.element("layout").elements("param")){
                 addLayoutParam(e.attributeValue("name"), e.attributeValue("value"));
             }
         }
         
         for(Element e : (List<Element>) element.elements("appender-ref")){
             addAppenderRef(e.attributeValue("ref"));
         }
     }
     
     private boolean checkParamSuported(String param){
         if(className.startsWith("org.apache.log4j.")) 
         {            
             String[] classNameArr = className.split("\\.");
 
             if(classNameArr.length == 4){
                 try{
                     AppenderParams params = AppenderParams.valueOf(classNameArr[3].toLowerCase(Locale.ENGLISH));
 
                     for(String par : params.getParams()){
                         if(param.equalsIgnoreCase(par)) return true;
                     }
                     return false;
                 }catch(Exception e){
                     // unnknow appender, we can't check validity
                 }
             }
         }
         
         return true;
     }
     
         
     private boolean checkLayoutParamSuported(String param){
         for (Layout layout : Layout.values())
         {
             if (getLayoutClassName().equalsIgnoreCase(layout.toString())) 
             {
                 if(!layout.getParam1().equalsIgnoreCase(param)
                   && (layout.getParam2() != null && !layout.getParam2().equalsIgnoreCase(param)))
                 {
                     return false;
                 }
             }
         }
         
         return true;
     }
     
     public List<String> toProperty(List<String> prop) {
         
         prop.add("");
         prop.add(AppUtils.prefix("appender." + name + " = " + className));
         
         if(threshold != null && !threshold.isEmpty()) {
             prop.add(AppUtils.prefix("appender.threshold = " + threshold));
         }
         
         //params 
         AppUtils.addParams(prop, "appender." + name, params);
         
         if(layoutClassName != null) {
             prop.add(AppUtils.prefix("appender." + name + ".layout = " + layoutClassName));
                         
             AppUtils.addParams(prop, "appender." + name + ".layout", layoutParams);
         }
         
         if(errorHandler != null ){
             errorHandler.toProperty(prop, "appender." + name);
         }
         
         if(appenderRefs != null && !appenderRefs.isEmpty()) {
             //logger refs
             String appenderRefsString = AppUtils.join( appenderRefs, ", ");
             prop.add(AppUtils.prefix("appender." + name + ".appender-ref = " + appenderRefsString));
         }
         
         return prop;
     }
     
     private enum AppenderParams {
 
         consoleappender("Encoding", "ImmediateFlush", "Target", "Threshold"),
         fileappender("Append", "Encoding", "BufferedIO", "BufferSize", "File", "ImmediateFlush", "Threshold"),
         rollingfileappender("Append", "Encoding", "BufferedIO", "BufferSize", "File", "ImmediateFlush", 
                 "MaxBackupIndex", "MaxFileSize", "Threshold"),
         dailyrollingfileappender("Append", "Encoding", "BufferedIO", "BufferSize", "File", "ImmediateFlush", 
                 "DatePattern", "Threshold"),
         writerappender("Encoding", "ImmediateFlush", "Threshold"),
         asyncappender("BufferSize","Threshold");
 
         private List<String> params = new ArrayList<String>();
 
         AppenderParams(String... params) {
             this.params.addAll(Arrays.asList(params));
         }
 
         public List<String> getParams() {
             return params;
         }
     }
     
     private enum Layout {
         SimpleLayout("org.apache.log4j.SimpleLayout", null, null),
         PatternLayout("org.apache.log4j.PatternLayout", "ConversionPattern", null),
         EnhancedPatternLayout("org.apache.log4j.EnhancedPatternLayout", "ConversionPattern", null),
         DateLayout("org.apache.log4j.DateLayout", "DateFormat", null),
         HTMLLayout("org.apache.log4j.HTMLLayout", "LocationInfo", "Title"),
         XMLLayout("org.apache.log4j.XMLLayout", "LocationInfo", null);
 
         private String fullName;
         private String param1;
         private String param2;
 
         Layout(String fullName, String param1, String param2) {
             this.fullName = fullName;
             this.param1 = param1;
             this.param2 = param2;
         }
 
         public String getParam1() {
             return param1;
         }
 
         public String getParam2() {
             return param2;
         }
 
         @Override
         public String toString() {
             return fullName;
         }
     }
     
     public void verify() {
         boolean loggedAlready = false;
         // verify layouts
         if (getLayoutClassName() != null) {     // there is a layout present
             for (Layout layout : Layout.values()) {
                 if (getLayoutClassName().equalsIgnoreCase(layout.toString())) {
                     for (String layoutParam : getLayoutParams().keySet()) {
                         if (!layoutParam.equalsIgnoreCase(layout.getParam1())
                                 && !layoutParam.equalsIgnoreCase(layout.getParam2())) {
                             AppUtils.crash("You have entered wrong layout parameter.");
                         }
                     }
                 }
             }
         }
         Iterator<Map.Entry<String, String>> iter = getParams().entrySet().iterator();
         while (iter.hasNext()) {
             Map.Entry<String, String> paramCouple = iter.next();
             if (paramCouple.getKey().equals("threshold")) {
                 try {
                     Level.toLevel(paramCouple.getValue()); // FOR MARTIN: Nahrada za Threshold.valueOf(paramCouple.getValue())
                 } catch (IllegalArgumentException ex) {
                     AppUtils.crash("You have entered wrong threshold for appender" + getName());
                 }
             } else {
                 try {
                     boolean paramFound = false;
                     for (String param : AppenderParams.valueOf(getName()).getParams()) {
                         if (param.equalsIgnoreCase(paramCouple.getKey())) {
                             paramFound = true;
                             break;
                         } 
                     }
                     if(!paramFound) {
                         AppUtils.crash("You have entered wrong parameter \""
                                 + paramCouple.getKey() + "\" for appender: " + getName());
                     }
                 } catch (IllegalArgumentException ex) {
                     // custom defined appender: possible & it can have any parameter
                     if(!loggedAlready) {
                         log.info("Custom appender: " + getName());
                     }
                     loggedAlready = true;
                 }
             }
         }
         // + verify correctness of some values for defined parameters
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 31 * hash + (this.className != null ? this.className.hashCode() : 0);
         hash = 31 * hash + (this.layoutClassName != null ? this.layoutClassName.hashCode() : 0);
         hash = 31 * hash + (this.hasLayoutAlready ? 1 : 0);
         hash = 31 * hash + (this.threshold != null ? this.threshold.hashCode() : 0);
         hash = 31 * hash + (this.params != null ? this.params.hashCode() : 0);
         hash = 31 * hash + (this.layoutParams != null ? this.layoutParams.hashCode() : 0);
         hash = 31 * hash + (this.errorHandler != null ? this.errorHandler.hashCode() : 0);
         hash = 31 * hash + (this.appenderRefs != null ? this.appenderRefs.hashCode() : 0);
         return hash;
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Appender other = (Appender) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
             return false;
         }
         if ((this.layoutClassName == null) ? (other.layoutClassName != null) : !this.layoutClassName.equals(other.layoutClassName)) {
             return false;
         }
         if (this.params != other.params && (this.params == null || !this.params.equals(other.params))) {
             return false;
         }
         if (this.layoutParams != other.layoutParams && (this.layoutParams == null || !this.layoutParams.equals(other.layoutParams))) {
             return false;
         }
         if (this.errorHandler != other.errorHandler && (this.errorHandler == null || !this.errorHandler.equals(other.errorHandler))) {
             return false;
         }
         if (this.appenderRefs != other.appenderRefs && (this.appenderRefs == null || !this.appenderRefs.equals(other.appenderRefs))) {
             return false;
         }
         return true;
     }
     
 }
