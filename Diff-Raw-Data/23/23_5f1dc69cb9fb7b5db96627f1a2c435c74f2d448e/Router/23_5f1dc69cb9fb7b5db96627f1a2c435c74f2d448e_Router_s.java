 /*
  *  Copyright 2011 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.routing;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import cx.ath.mancel01.webframework.WebFramework;
 import cx.ath.mancel01.webframework.annotation.Controller;
 import cx.ath.mancel01.webframework.http.Request;
 import cx.ath.mancel01.webframework.routing.Param.TypeMapper;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 /**
  *
  * @author mathieuancelin
  */
 public class Router {
 
     private Map<String, WebMethod> registeredControllers = new HashMap<String, WebMethod>();
     private List<String> uselessMethods = new ArrayList<String>();
 
     public Router() {
         List<Method> methods = Arrays.asList(Object.class.getMethods());
         for (Method m : methods) {
             uselessMethods.add(m.getName());
         }
     }
 
     public void reset() {
         this.registeredControllers.clear();
     }
 
     public WebMethod route(Request request, String contextRoot) {
         String path = request.path;
         if (!"/".equals(contextRoot)) {
             path = path.replace(contextRoot, "");
         }
         for (String url : registeredControllers.keySet()) {
             if (path.matches(url)) {
                 return registeredControllers.get(url);
             }
         }
         throw new RuntimeException("Can't find an applicable controller method for " + path);
     }
 
     public void registrerController(Class<?> clazz) {
         if (clazz.isAnnotationPresent(Controller.class)) {
             String prefix = "";
             if (clazz.isAnnotationPresent(Path.class)) {
                 Path path = clazz.getAnnotation(Path.class);
                 prefix = path.value().trim();
                 if (!prefix.startsWith("/")) {
                     prefix = "/" + prefix;
                 }
             } else {
                 prefix = "/" + clazz.getSimpleName().toLowerCase();
             }
 
             for (Method method : clazz.getMethods()) {
                 if (!uselessMethods.contains(method.getName())) {
                     if (method.isAnnotationPresent(Path.class)) {
                         Path methodPath = method.getAnnotation(Path.class);
                         String value = methodPath.value().trim();
                         if (value.startsWith("/")) {
                             registerRoute(value, clazz, method);
                         } else {
                             String url = "";
                             if (prefix.endsWith("/")) {
                                 url = prefix + value;
                             } else {
                                 url = prefix + "/" + value;
                             }
                             registerRoute(url, clazz, method);
                         }
                     }
                     if (WebFramework.keepDefaultRoutes) {
                         String url = "";
                         if (prefix.endsWith("/")) {
                             url = prefix + method.getName();
                         } else {
                             url = prefix + "/" + method.getName();
                         }
                         registerRoute(url, clazz, method);
                         // TODO : register without @Path prefix
                     }
                 }
             }
         } else {
             throw new RuntimeException("You can't register a controller without @Controller annotation");
         }
     }
 
     private void registerRoute(String url, Class clazz, Method method) {
         WebMethod webMethod = new WebMethod();
         webMethod.setClazz(clazz);
         webMethod.setFullUrl(url);
         webMethod.setMethod(method);
         if (url.startsWith("/")) {
             webMethod.setComparisonUrl(Param.replaceParamsWithWildcard(url));
         } else {
             webMethod.setComparisonUrl("/" + Param.replaceParamsWithWildcard(url));
         }
         if (method.isAnnotationPresent(Consumes.class)) {
             final Class<?>[] paramTypes = method.getParameterTypes();
             if (paramTypes.length > 1) {
                 throw new RuntimeException("can't register an @Consumes method with more than one parameter");
             }
             Consumes consumes = method.getAnnotation(Consumes.class);
             String[] types = consumes.value();
             if (types.length > 1) {
                 throw new RuntimeException("can't register an @Consumes method with more than consume type");
             }
             String type = types[0];
             if (MediaType.APPLICATION_JSON.equals(type)) {
                 Param param = new Param(paramTypes[0], url, new TypeMapper() {
                     @Override
                     public Object map(String value) {
                         Gson gson = new GsonBuilder()
                             .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                             .create();
                         return gson.fromJson(value, paramTypes[0]);
                     }
                 });
                webMethod.getParams().put(param.name(), param);
             } else if (MediaType.APPLICATION_OCTET_STREAM.equals(type)) {
                 Param param = new Param(paramTypes[0], url, new TypeMapper() {
                     @Override
                     public Object map(String value) {
                         FileOutputStream out = null;
                         try {
                             String path = WebFramework.TARGET.getAbsolutePath()
                                     + "/file" + System.currentTimeMillis();
                             out = new FileOutputStream(path);
                             out.write(value.getBytes());
                             out.close();
                             return new File(path);
                         } catch (Exception ex) {
                             try {
                                 out.close();
                             } catch (IOException e) {
                                 throw new RuntimeException(e);
                             }
                             throw new RuntimeException(ex);
                         }
                     }
                 });
                webMethod.getParams().put(param.name(), param);
             } else if (MediaType.APPLICATION_XML.equals(type)) {
                 Param param = new Param(paramTypes[0], url, new TypeMapper() {
                     @Override
                     public Object map(String value) {
                         try {
                             JAXBContext jc = JAXBContext.newInstance(paramTypes[0]);
                             Unmarshaller unmarshaller = jc.createUnmarshaller();
                             ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes());
                             return unmarshaller.unmarshal(input);
                         } catch (JAXBException ex) {
                             throw new RuntimeException(ex);
                         }
                     }
                 });
                webMethod.getParams().put(param.name(), param);
             } else if (MediaType.TEXT_HTML.equals(type)) {
                 Param param = new Param(paramTypes[0], url, new TypeMapper() {
                     @Override
                     public Object map(String value) {
                         return value;
                     }
                 });
                webMethod.getParams().put(param.name(), param);
             } else if (MediaType.TEXT_PLAIN.equals(type)) {
                 Param param = new Param(paramTypes[0], url, new TypeMapper() {
                     @Override
                     public Object map(String value) {
                         return value;
                     }
                 });
                webMethod.getParams().put(param.name(), param);
             } else if (MediaType.TEXT_XML.equals(type)) {
                 Param param = new Param(paramTypes[0], url, new TypeMapper() {
                     @Override
                     public Object map(String value) {
                         return value;
                     }
                 });
                webMethod.getParams().put(param.name(), param);
             } else {
                 throw new RuntimeException("unsupported @Consumes type");
             }
         } else {
             Annotation[][] annotations = method.getParameterAnnotations();
             Class<?>[] types = method.getParameterTypes();
             if (annotations != null) {
                 for (int i = 0; i < annotations.length; i++) {
                     for (int j = 0; j < annotations[i].length; j++) {
                         Annotation annotation = annotations[i][j];
                         Param param = new Param(annotation, url, types[i]);
                         //webMethod.getParams().put(param.name(), param);
                         webMethod.addParam(param);
                     }
                 }
                 Matcher matcher = Param.PATH_PARAM_DECLARATION.matcher(url);
                 while (matcher.find()) {
                     String name = matcher.group().replaceAll("\\{|\\}", "");
                     if (webMethod.getParams().containsKey(name)) {
                         webMethod.getParams().get(name).setPathParamName(matcher);
                     } else {
                         throw new RuntimeException("the @Path on method " + method.getName()
                                 + " is missing path param : " + name);
                     }
                 }
             }
         }
         String niceUrl = null;
         if (url.startsWith("/")) {
             niceUrl = Param.replaceParamsWithWildcard(url);
         } else {
             niceUrl = "/" + Param.replaceParamsWithWildcard(url);
         }
         if (!registeredControllers.containsKey(niceUrl)) {
             //WebFramework.logger.trace("route registered @ " + niceUrl);
             registeredControllers.put(niceUrl, webMethod);
         } else {
             throw new RuntimeException("the url " + url + " is already registered.");
         }
     }
 
     public Set<String> getRoutes() {
         return registeredControllers.keySet();
     }
 
     public String getHtmlRoutesTable() {
         StringBuilder routes = new StringBuilder();
         routes.append("<table>");
         int row = 0;
         for (Entry<String, WebMethod> route : this.registeredControllers.entrySet()) {
             row++;
             if (row % 2 == 0) {
                 routes.append("<tr>");
             } else {
                 routes.append("<tr bgcolor=\"EEEEEE\">");
             }
             routes.append("<td>");
             routes.append(route.getKey());
             routes.append("</td><td width=\"100px\" align=\"center\">");
             routes.append("  =>  ");
             routes.append("</td><td>");
             routes.append(route.getValue().getClazz().getSimpleName());
             routes.append(".");
             routes.append(route.getValue().getMethod().getName());
             routes.append("(");
             Set<String> params = route.getValue().getParams().keySet();
             int nbrParam = params.size();
             int i = 0;
             for (String param : params) {
                 i++;
                 routes.append(param);
                 if (nbrParam > 1) {
                     if (i != nbrParam)
                         routes.append(", ");
                 }
             }
             routes.append(")");
             routes.append("</td>");
             routes.append("</tr>");
         }
         routes.append("</table>");
         return routes.toString();
     }
     
 }
