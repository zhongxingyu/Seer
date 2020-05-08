 package org.smartly.packages.http.impl.handlers.rest.impl.wrapper;
 
 
 import org.json.JSONObject;
 import org.smartly.IConstants;
 import org.smartly.Smartly;
 import org.smartly.commons.cryptograph.MD5;
 import org.smartly.commons.util.ByteUtils;
 import org.smartly.commons.util.JsonWrapper;
 import org.smartly.commons.util.PathUtils;
 import org.smartly.commons.util.StringUtils;
 import org.smartly.packages.http.impl.handlers.rest.impl.IRESTCons;
 import org.smartly.packages.http.impl.handlers.rest.impl.annotations.*;
 import org.smartly.packages.http.impl.serialization.json.JsonBean;
 import org.smartly.packages.http.impl.util.BinaryData;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 
 public class MethodWrapper {
 
     private static final String CHARSET = Smartly.getCharset();
 
     private final Object _instance;
     private final Method _method;
     // fields
     private String _id;
     private String _http_method;
     private String _path;
     private String _path_id;
     private String[] _path_params; // array of {param} in path
     private String _type_output;
 
     public MethodWrapper(final Object instance, final Method m) {
         _instance = instance;
         _method = m;
         _type_output = IRESTCons.TYPE_JSON;
         this.init(_method);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (obj instanceof MethodWrapper) {
             final MethodWrapper mobj = (MethodWrapper) obj;
             return super.equals(obj) ||
                     (this.getId().equalsIgnoreCase(mobj.getId()));
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return 9 + this.getId().hashCode();
         //(null!=_http_method?_http_method.hashCode():0) +
         //(null!=_path?_path.hashCode():0);
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append(this.getClass().getName()).append("{");
         sb.append("id=").append(this.getId());
         sb.append(", ");
         sb.append("path=").append(_path);
         sb.append(", ");
         sb.append("pathId=").append(_path_id);
         sb.append(", ");
         sb.append("httpMethod=").append(_http_method);
         sb.append(", ");
         sb.append("type=").append(_type_output);
         sb.append(", ");
         sb.append("name=").append(null != _method ? _method.getName() : "");
         sb.append(", ");
         sb.append("class=").append(null != _instance ? _instance.getClass().getSimpleName() : "");
         sb.append("}");
 
         return sb.toString();
     }
 
     public String getId() {
         return _id;
     }
 
     public String getPath() {
         return _path;
     }
 
     public String getPathId() {
         return _path_id;
     }
 
     public String[] getPathParams() {
         return _path_params;
     }
 
     public String getHttpMethod() {
         return _http_method;
     }
 
     public String getTypeOutput() {
         return _type_output;
     }
 
     public boolean match(final String httpMethod, final String url) {
         if (_http_method.equalsIgnoreCase(httpMethod)) {
             // remove service name, the root
             final String path = removeRoot(url);
             return this.match(path);
         }
         return false;
     }
 
 
     public byte[] execute(final String url, final Map<String, Object> formParams) throws IOException {
         return this.execute(url, new JSONObject(formParams));
     }
 
     public byte[] execute(final String url, final JSONObject formParams) throws IOException {
         Object result = null;
         try {
             // remove service name, the root
             final String path = removeRoot(url);
             final Map<String, String> urlParams = pluckParams(path, this.getPathParams());
             result = this.execute(urlParams, formParams);
         } catch (Throwable t) {
             result = t;
         }
         return serialize(_type_output, result);
     }
 
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
 
     private void init(final Method m) {
         final Annotation[] annotations = m.getDeclaredAnnotations();
         for (final Annotation a : annotations) {
             if (a instanceof GET) {
                 _http_method = "GET";
             } else if (a instanceof POST) {
                 _http_method = "POST";
             } else if (a instanceof PUT) {
                 _http_method = "PUT";
             } else if (a instanceof DELETE) {
                 _http_method = "DELETE";
             } else if (a instanceof Path) {
                 final String value = ((Path) a).value();
                 _path = StringUtils.hasText(value) ? value : IRESTCons.DEFAULT_PATH;
             } else if (a instanceof Produces) {
                 final String value = ((Produces) a).value();
                 if (null != value && value.length() > 0) {
                     _type_output = value;
                 } else {
                     _type_output = IRESTCons.TYPE_JSON;
                 }
             }
         }
 
         // set path id
         _path_id = getPathId(_path);
         // count parameters in path
         _path_params = getPathParams(_path);
         // set method id
         _id = MD5.encode((null != _path_id ? _path_id : "") + (null != _http_method ? _http_method : ""));
     }
 
     private boolean match(final String check) {
         final String[] check_tokens = StringUtils.split(check, "/");
         final String[] path_tokens = StringUtils.split(_path, "/");
         if (check_tokens.length != path_tokens.length) {
             return false;
         }
         for (int i = 0; i < check_tokens.length; i++) {
             if (!check_tokens[i].equalsIgnoreCase(path_tokens[i]) && !isParam(path_tokens[i])) {
                 return false;
             }
         }
         return true;
     }
 
     private Object execute(final Map<String, String> urlParams, final JSONObject formParams) throws IOException {
         Object result = null;
         try {
             if (urlParams.isEmpty() && formParams.length() == 0) {
                 result = this.execute();
             } else {
                 final Annotation[][] aparams = _method.getParameterAnnotations();
                 if (null != aparams && aparams.length > 0) {
                     final Object[] params = new Object[aparams.length];
                     Arrays.fill(params, "");
                     for (int i = 0; i < aparams.length; i++) {
                         final Annotation[] ap = aparams[i];
                         for (final Annotation a : ap) {
                             if (a instanceof PathParam) {
                                 final String key = ((PathParam) a).value(); // getValue(a);
                                 if (urlParams.containsKey(key)) {
                                     params[i] = urlParams.get(key);
                                 }
                             } else if (a instanceof FormParam) {
                                 final String key = ((FormParam) a).value(); // getValue(a);
                                 if (formParams.has(key)) {
                                     params[i] = formParams.get(key).toString();
                                 }
                             }
                         }
                     }
                     // now I should have an ordered array of parameters
                     result = this.execute(params);
                 } else {
                     result = this.execute();
                 }
             }
         } catch (Throwable t) {
             result = t;
         }
         return result;
     }
 
     private Object execute(final Object... args) throws IOException {
         Object result = null;
         try {
             result = _method.invoke(_instance, args);
         } catch (InvocationTargetException ite) {
             result = ite.getTargetException();
         } catch (Throwable t) {
             result = t;
         }
         return result;
     }
     // --------------------------------------------------------------------
     //               S T A T I C
     // --------------------------------------------------------------------
 
     private static byte[] serialize(final String type, final Object data) throws IOException {
         if (!StringUtils.isNULL(data)) {
             if (IRESTCons.TYPE_JSON.equalsIgnoreCase(type)) {
                final JsonBean json = new JsonBean(data);
                return json.asJSONObject().toString().getBytes(CHARSET);
             } else {
                 if (data instanceof BinaryData) {
                     final BinaryData bin_data = (BinaryData) data;
                     return bin_data.getBytes();
                 } else if (ByteUtils.isByteArray(data)) {
                     return (byte[]) data;
                 } else if (data instanceof String) {
                     return ((String) data).getBytes(Smartly.getCharset());
                 } else if (data instanceof InputStream) {
                     return ByteUtils.getBytes((InputStream) data);
                 } else {
                     return data.toString().getBytes(CHARSET);
                 }
             }
         } else {
             if (IRESTCons.TYPE_JSON.equalsIgnoreCase(type)) {
                final JSONObject json = new JSONObject();
                JsonWrapper.put(json, "response", IConstants.NULL);
                return json.toString().getBytes(CHARSET);
             }
         }
         return IConstants.NULL.getBytes(CHARSET);
     }
 
     private static String getPathId(final String path) {
         final StringBuilder sb = new StringBuilder();
         final String[] tokens = StringUtils.split(path, "/");
         for (final String t : tokens) {
             if (sb.length() > 0) {
                 sb.append(".");
             }
             if (isParam(t)) {
                 sb.append("*");
             } else {
                 sb.append(t);
             }
         }
         return sb.toString();
     }
 
     private static String removeRoot(final String url) {
         return PathUtils.splitPathRoot(url);
     }
 
     private static boolean isParam(final String path_token) {
         return (path_token.trim().startsWith("{"));
     }
 
     private static String[] getPathParams(final String path) {
         final List<String> result = new LinkedList<String>();
         final String[] tokens = StringUtils.split(path, "/");
         for (final String t : tokens) {
             if (isParam(t)) {
                 result.add(t.substring(t.indexOf("{") + 1, t.indexOf("}")));
             } else {
                 result.add(""); // empty space
             }
         }
         return result.toArray(new String[result.size()]);
     }
 
     private static Map<String, String> pluckParams(final String path, final String[] path_params) {
         final String[] tokens = StringUtils.split(path, "/");
         final Map<String, String> result = new HashMap<String, String>();
         if (tokens.length > 0 && tokens.length == path_params.length) {
             for (int i = 0; i < path_params.length; i++) {
                 if (StringUtils.hasText(path_params[i])) {
                     result.put(path_params[i], tokens[i]);
                 }
             }
         }
         return result;
     }
 
     private static String getValue(final Annotation annotation) {
         try {
             return (String) annotation.annotationType().getMethod("value").invoke(annotation);
         } catch (Throwable ignored) {
         }
         return "";
     }
 }
