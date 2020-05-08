 /*
  *  Copyright (C) 2008 Farrukh Ijaz, OpenKoncept Inc.
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.openkoncept.vroom;
 
 import net.openkoncept.vroom.config.*;
 
 import java.util.*;
 
 /**
  * <p>
  * This class is one of the core classes of Vroom framework. This class generates java script file for a request based
  * on the configuration in vroom's config file.
  * </p>
  *
  * @author Farrukh Ijaz Muhammad Riaz (ijazfx@yahoo.com)
  */
 public class VroomScriptGenerator {
 
     String uri;
     VroomConfig vc;
     String contextPath;
     String servletPath;
     String sessionId;
     String requestPath;
     List lstInitFunctionCalls = new ArrayList();
     List lstFunctions = new ArrayList();
     Map functionCountMap = new LinkedHashMap();
 
     /**
      * <p>
      * Use this constructor to create the script generator object and then call generateScript() method of the instance.
      * </p>
      *
      * @param vc          - VroomConfig object
      * @param contextPath - Context path of the web application
      * @param servletPath - VroomServlet path (must be /vroom)
      * @param sessionId   - Session ID for the request
      * @param uri         - Requested URI
      */
     public VroomScriptGenerator(VroomConfig vc, String contextPath, String servletPath, String sessionId, String uri) {
         this.uri = uri;
         this.vc = vc;
         this.contextPath = contextPath;
         this.servletPath = servletPath;
         this.sessionId = sessionId;
         requestPath = contextPath + servletPath + ";jsessionid=" + sessionId;
     }
 
     /**
      * <p>
      * This method generates the script for the requested URI which was passed as paramter to the construtor.
      * </p>
      *
      * @return - Generated Script (javascript)
      */
     public String generateScript() {
         StringBuffer sbScript = new StringBuffer();
         String wpBeanClass = "";
         String wpVar = "";
         ScopeType wpScope = null;
         for (Webpage wp : vc.getWebpage()) {
             wpBeanClass = (wp.getBeanClass() != null) ? wp.getBeanClass() : wpBeanClass;
             wpVar = (wp.getVar() != null) ? wp.getVar() : wpVar;
             wpScope = wp.getScope();
             // proceed only if the request uri matches with the webpage uri defined in vroom's config file.
             if (uri.matches(wp.getUri())) {
                 // Process all objects...
                 Iterator<net.openkoncept.vroom.config.Object> iobj = wp.getObject().iterator();
                 while (iobj.hasNext()) {
                     net.openkoncept.vroom.config.Object o = iobj.next();
                     String oBeanClass = (o.getBeanClass() != null) ? o.getBeanClass() : wpBeanClass;
                     String oVar = (o.getVar() != null) ? o.getVar() : wpVar;
                     ScopeType oScope = (o.getBeanClass() != null) ? o.getScope() : wpScope;
                     // Process events of the object...
                     Iterator<Event> ievent = o.getEvent().iterator();
                     while (ievent.hasNext()) {
                         Event ev = ievent.next();
                         String evBeanClass = (ev.getBeanClass() != null) ? ev.getBeanClass() : oBeanClass;
                         String evVar = (ev.getVar() != null) ? ev.getVar() : oVar;
                         ScopeType evScope = (ev.getBeanClass() != null) ? ev.getScope() : oScope;
                         lstFunctions.add(jsFunction(o.getName(), null, ev.getType(), ev.getMethod(), ev.isSync(), evBeanClass, evVar, evScope, ev.getCall()));
                     }
                 }
                 // Process all the forms...
                 Iterator<Form> ifrm = wp.getForm().iterator();
                 while (ifrm.hasNext()) {
                     Form f = ifrm.next();
                     String fBeanClass = (f.getBeanClass() != null) ? f.getBeanClass() : wpBeanClass;
                     String fVar = (f.getVar() != null) ? f.getVar() : wpVar;
                     ScopeType fScope = (f.getBeanClass() != null) ? f.getScope() : wpScope;
                     if (f.getMethod() != null) {
                         lstInitFunctionCalls.add(jsSetForm(f.getId(), f.getMethod(), fBeanClass, fVar, fScope, uri));
                     }
                     // Process all the elements...
                     Iterator<Element> ielem = f.getElement().iterator();
                     while (ielem.hasNext()) {
                         Element e = ielem.next();
                         String eBeanClass = (e.getBeanClass() != null) ? e.getBeanClass() : fBeanClass;
                         String eVar = (e.getVar() != null) ? e.getVar() : fVar;
                         ScopeType eScope = (e.getBeanClass() != null) ? e.getScope() : fScope;
                         // Process events for the element...
                         Iterator<Event> ievent = e.getEvent().iterator();
                         while (ievent.hasNext()) {
                             Event ev = ievent.next();
                             String evBeanClass = (ev.getBeanClass() != null) ? ev.getBeanClass() : eBeanClass;
                             String evVar = (ev.getVar() != null) ? ev.getVar() : eVar;
                             ScopeType evScope = (ev.getBeanClass() != null) ? ev.getScope() : eScope;
                             lstFunctions.add(jsFunction(null, e.getId(), ev.getType(), ev.getMethod(), ev.isSync(), evBeanClass, evVar, evScope, ev.getCall()));
                         }
                     }
                     // Process events for the element...
                     Iterator<Event> ievent = f.getEvent().iterator();
                     while (ievent.hasNext()) {
                         Event ev = ievent.next();
                         String evBeanClass = (ev.getBeanClass() != null) ? ev.getBeanClass() : fBeanClass;
                         String evVar = (ev.getVar() != null) ? ev.getVar() : fVar;
                         ScopeType evScope = (ev.getBeanClass() != null) ? ev.getScope() : fScope;
                         lstFunctions.add(jsFunction(null, f.getId(), ev.getType(), ev.getMethod(), ev.isSync(), evBeanClass, evVar, evScope, ev.getCall()));
                     }
 
                 }
                 // Process all the elements...
                 Iterator<Element> ielem = wp.getElement().iterator();
                 while (ielem.hasNext()) {
                     Element e = ielem.next();
                     String eBeanClass = (e.getBeanClass() != null) ? e.getBeanClass() : wpBeanClass;
                     String eVar = (e.getVar() != null) ? e.getVar() : wpVar;
                     ScopeType eScope = (e.getBeanClass() != null) ? e.getScope() : wpScope;
                     // Process events for the element...
                     Iterator<Event> ievent = e.getEvent().iterator();
                     while (ievent.hasNext()) {
                         Event ev = ievent.next();
                         String evBeanClass = (ev.getBeanClass() != null) ? ev.getBeanClass() : eBeanClass;
                         String evVar = (ev.getVar() != null) ? ev.getVar() : eVar;
                         ScopeType evScope = (ev.getBeanClass() != null) ? ev.getScope() : eScope;
                         lstFunctions.add(jsFunction(null, e.getId(), ev.getType(), ev.getMethod(), ev.isSync(), evBeanClass, evVar, evScope, ev.getCall()));
                     }
                 }
                 // Process all the events...
                 Iterator<Event> ievent = wp.getEvent().iterator();
                 while (ievent.hasNext()) {
                     Event ev = ievent.next();
                     String evBeanClass = (ev.getBeanClass() != null) ? ev.getBeanClass() : wpBeanClass;
                     String evVar = (ev.getVar() != null) ? ev.getVar() : wpVar;
                     ScopeType evScope = (ev.getBeanClass() != null) ? ev.getScope() : wpScope;
                     lstFunctions.add(jsFunction(null, null, ev.getType(), ev.getMethod(), ev.isSync(), evBeanClass, evVar, evScope, ev.getCall()));
                 }
             }
         }
         lstFunctions.add(jsFunctionCallAll());
         Iterator iter = lstFunctions.iterator();
         while (iter.hasNext()) {
             sbScript.append(iter.next());
         }
         sbScript.append(jsInitFunction());
         return sbScript.toString();
     }
 
     /**
      * <p>
      * This method generate a javascript function corresponding the event type and calls all the functions having the
      * same function name suffixed with numbers (0, 1, 2, ...) in sequenctial order.
      * </p>
      *
      * @return - javascript function
      */
     private String jsFunctionCallAll() {
         StringBuffer sb = new StringBuffer();
         Iterator iter = functionCountMap.keySet().iterator();
         while (iter.hasNext()) {
             String funcName = (String) iter.next();
             int count = ((Integer) functionCountMap.get(funcName)).intValue();
             sb.append("function ");
             sb.append(funcName);
             sb.append("() {\n");
             for (int i = 0; i < count; i++) {
                 sb.append(funcName);
                 sb.append(i);
                 sb.append("();\n");
             }
             sb.append("}\n");
         }
         return sb.toString();
     }
 
     private String jsSetForm(String id, String method, String fBeanClass, String fVar, ScopeType fScope, String uri) {
         return jsSetForm(id, method, fBeanClass, fVar, fScope.value(), uri);
     }
     
     /**
      * <p>
      * This method generates a java script function that is called in the init() java script function of the uri. This
      * method sets the form values (changes the action, enctype).
      * </p>
      *
      * @param id        - HTML id of the form
      * @param method    - The method to be called
      * @param beanClass - The java class which contains the method
      * @param var       - The variable name to look for the java object in the scope
      * @param scope     - The scope of the java object
      * @return - String of javascript function
      */
     private String jsSetForm(String id, String method, String beanClass, String var, String scope, String callerUri) {
         StringBuffer sb = new StringBuffer();
         String action = requestPath;
         // action = action + "v-rt=submit&v-id=" + id + "&v-method=" + method + "&v-bean-class=" + beanClass + "&v-var=" + var + "&v-scope=" + scope + "&v-caller-uri=" + uri;
         // sb.append("vroomSetForm(").append("'").append(id).append("',").append("'").append(encType).append("',").append("'").append(action).append("')");
         sb.append("VroomUtils.setupForm('").append(id).append("','").append(action).append("','")
                 .append(method).append("','").append(beanClass).append("','").append(var).append("','").append(scope).append("','")
                 .append(callerUri).append("')");
         return sb.toString();
     }
 
     private String jsFunction(String name, String id, EventTypeList type, String method, boolean sync, String evBeanClass, String evVar, ScopeType evScope, List<Call> call) {
         return jsFunction(name, id, type.value(), method, sync, evBeanClass, evVar, evScope.value(), call);
     }
     
     /**
      * <p>
      * This method generates a java script function based on the event type.
      * </p>
      *
      * @param name      - Name of the object e.g. browser, window or a javascript variable
      * @param id        - Html control ID
      * @param type      - javascript event name such as "onclick", "onblur" etc.
      * @param method    - The method name
      * @param sync      - Synchronous flag (default is false)
      * @param beanClass - The class that contains the method method
      * @param var       - The variable name to locate object in of the beanClass in a specified scope.
      * @param scope     - The scope of the object ("application" | "session" | "request")
      * @param calls     - List of calls to process
      * @return - String of javascript function
      */
     private String jsFunction(String name, String id, String type, String method, Boolean sync, String beanClass, String var, String scope, List<Call> calls) {
         String functionName = (id != null) ? jsFunctionName(id, type) : jsFunctionName("window", type);//jsFunctionName(type);
         Integer count = (Integer) functionCountMap.get(functionName);
         if (count == null) {
             count = new Integer(1);
             functionCountMap.put(functionName, count);
         } else {
             count = new Integer(count.intValue() + 1);
             functionCountMap.put(functionName, count);
         }
         StringBuffer sb = new StringBuffer();
         sb.append("function ").append(functionName).append((count.intValue() - 1)).append("() {\n");
         sb.append("var _uri = '" + uri + "';\n");
         // sb.append("var _obj = " + ((name != null) ? name : (id != null) ? "document.getElementById('" + id + "')" : "document.getElementsByTagName('body')[0]") + ";\n");
         sb.append("var _obj = " + ((name != null) ? name : (id != null) ? "VroomUtils.getElement('" + id + "')" : "VroomUtils.getBodyObject()") + ";\n");
         sb.append("var _method = '" + method + "';\n");
         sb.append("var _sync = " + sync + ";\n");
         sb.append("var _beanClass = '" + beanClass + "';\n");
         sb.append("var _var = '" + var + "';\n");
         sb.append("var _scope = '" + scope + "';\n");
         Iterator iter = calls.iterator();
         StringBuffer sbCalls = new StringBuffer();
         while (iter.hasNext()) {
             Call call = (Call) iter.next();
            sbCalls.append(call.getType().value() + "|");
             if (CallType.UPDATE.equals(call.getType())) {
                 sbCalls.append(call.getId() + "|");
                 sbCalls.append(call.getTag() + "|");
                 sbCalls.append(call.getName() + "|");
                 sbCalls.append(call.getAttribute() + "|");
             }
             String url = call.getUrl();
             if (url != null && url.trim().length() > 0) {
                 url = url.replaceAll("#\\{contextPath}", contextPath);
                 sbCalls.append("url[").append(url).append("]");
             } else {
                String value = call.getValueAttribute().replaceAll("\\\\'", "&#x5C;&#x27;");
                 value = value.replaceAll("'", "&#x27;");
                 sbCalls.append(value);
             }
             sbCalls.append("VRMB");
         }
         sb.append("var _calls = '" + sbCalls.toString() + "';\n");
         sb.append("VroomUtils.ajaxCall(_uri, _obj, _method, _sync, _beanClass, _var, _scope, _calls);\n");
         sb.append("}\n");
         return sb.toString();
     }
 
     /**
      * <p>
      * This method generates the init() java script function for the request.
      * </p>
      *
      * @return - javascript function.
      */
     private String jsInitFunction() {
         StringBuffer sb = new StringBuffer();
         String function = "function " + jsInitFunctionName() + "() {\n";
         sb.append(function);
         Iterator iter = lstInitFunctionCalls.iterator();
         while (iter.hasNext()) {
             sb.append(iter.next() + ";\n");
         }
         iter = functionCountMap.keySet().iterator();
         while (iter.hasNext()) {
             String funcName = (String) iter.next();
             String[] temp = funcName.split("VRMB");
 //            String id = safeIdToId(temp[temp.length - 2]);
 //            String event = temp[temp.length - 1];
             String id = safeIdToId(temp[1]);
             String event = temp[2];
             String stmt = "VroomUtils.registerEvent(':1', ':2', :3);\n";
             stmt = stmt.replaceFirst(":1", id);
             stmt = stmt.replaceFirst(":2", event);
             stmt = stmt.replaceFirst(":3", funcName);
             sb.append(stmt);
         }
         sb.append(jsPostbackFunction());
         sb.append("}\n");
         return sb.toString();
     }
 
     /**
      * <p>
      * This method generates the init function name.
      * </p>
      *
      * @return - javascript function name
      */
     private String jsInitFunctionName() {
         return uri.replace('/', '_').replace('.', '_').replace('-', '_') + "_init";
     }
 
     /**
      * <p>
      * This method generates the init function name for the request.
      * </p>
      *
      * @param uri - The request URI
      * @return - javascript init() function for the uri
      */
     public static String jsInitFunctionName(String uri) {
         return uri.replace('/', '_').replace('.', '_').replace('-', '_') + "_init";
     }
 
     private String idToSafeId(String id) {
         return id.replaceAll("[/]", "FoRwArDsLaSh").replaceAll("\\\\", "BaCkSlAsH").replaceAll(":", "CoLoN").replaceAll("[$]", "DoLlOr").replaceAll("#", "HaSh").replaceAll("-", "HiPhEn").replaceAll("[.]", "DoT");
     }
 
     private String safeIdToId(String id) {
         return id.replaceAll("FoRwArDsLaSh", "\\/").replaceAll("BaCkSlAsH", "\\\\").replaceAll("CoLoN", ":").replaceAll("DoLlOr", "\\$").replaceAll("HaSh", "#").replaceAll("HiPhEn", "-").replaceAll("DoT", ".");
     }
 
     private String jsFunctionName(String id, String type) {
         String _id = idToSafeId(id);
         // return uri.replace('/', '_').replace('.', '_').replace('-', '_') + "_" + id + "_" + type;
         return uri.replace('/', '_').replace('.', '_').replace('-', '_') + "VRMB" + _id + "VRMB" + type;
     }
 
     private String jsPostbackFunction() {
         StringBuffer sb = new StringBuffer();
         String funcName = uri.replace('/', '_').replace('.', '_').replace('-', '_') + "_postback";
         sb.append("VroomUtils.callPostBack('").append(funcName).append("');\n");
         return sb.toString();
     }
 
 //    private String jsPostbackFunction() {
 //        StringBuffer sb = new StringBuffer();
 //        String funcName = uri.replace('/', '_').replace('.', '_').replace('-', '_') + "_postback";
 //        sb.append("try {\n");
 //        sb.append(funcName).append("();\n");
 //        sb.append("} catch(e) { alert(e); }\n");
 //        return sb.toString();
 //    }
 
 }
