 /*
 * Copyright (c) 2010 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package org.ovirt.engine.api.restapi.rsdl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 
 import org.ovirt.engine.api.common.util.FileUtils;
 import org.ovirt.engine.api.common.util.ReflectionHelper;
 import org.ovirt.engine.api.model.Actionable;
 import org.ovirt.engine.api.model.Body;
 import org.ovirt.engine.api.model.DetailedLink;
 import org.ovirt.engine.api.model.DetailedLinks;
 import org.ovirt.engine.api.model.Header;
 import org.ovirt.engine.api.model.Headers;
 import org.ovirt.engine.api.model.HttpMethod;
 import org.ovirt.engine.api.model.Parameter;
 import org.ovirt.engine.api.model.ParametersSet;
 import org.ovirt.engine.api.model.RSDL;
 import org.ovirt.engine.api.model.Request;
 import org.ovirt.engine.api.model.Response;
 import org.ovirt.engine.api.model.Schema;
 import org.ovirt.engine.api.model.Url;
 import org.ovirt.engine.api.resource.CreationResource;
 import org.ovirt.engine.api.resource.RsdlIgnore;
 import org.ovirt.engine.api.restapi.resource.BackendApiResource;
 import org.ovirt.engine.core.utils.log.Log;
 import org.ovirt.engine.core.utils.log.LogFactory;
 import org.yaml.snakeyaml.Yaml;
 
 public class RsdlBuilder {
 
     private static final String COLLECTION_PARAMETER_RSDL = "collection";
     private static final String COLLECTION_PARAMETER_YAML = "--COLLECTION";
     private RSDL rsdl;
     private String entryPoint;
     private BackendApiResource apiResource;
     private Map<String, Action> parametersMetaData;
     private String rel;
     private String href;
     private Schema schema;
     private String description;
 
     private static final String ACTION = "Action";
     private static final String DELETE = "delete";
     private static final String UPDATE = "update";
     private static final String GET = "get";
     private static final String ADD = "add";
 
     protected static final Log LOG = LogFactory.getLog(RsdlBuilder.class);
 
     private static final String RESOURCES_PACKAGE = "org.ovirt.engine.api.resource";
     private static final String PARAMS_METADATA = "rsdl_metadata_v-3.1.yaml";
 
     public RsdlBuilder(BackendApiResource apiResource) {
         this.apiResource = apiResource;
         this.entryPoint = apiResource.getUriInfo().getBaseUri().getPath();
         this.parametersMetaData = loadParametersMetaData();
     }
 
     public Map<String, Action> loadParametersMetaData() {
         parametersMetaData = new HashMap<String, Action>();
         try {
              InputStream stream = FileUtils.get(RESOURCES_PACKAGE, PARAMS_METADATA);
              if (stream != null) {
                  Object result = new Yaml().load(stream);
                  for (Action action : ((MetaData)result).getActions()) {
                      parametersMetaData.put(action.getName(), action);
                  }
              }
              LOG.error("Parameters metatdata file not found.");
         } catch (Exception e) {
              LOG.error("Loading parameters metatdata failed.", e);
         }
         return parametersMetaData;
     }
 
     private RSDL construct() throws ClassNotFoundException, IOException {
         RSDL rsdl = new RSDL();
         rsdl.setLinks(new DetailedLinks());
         for (DetailedLink link : getLinks()) {
             rsdl.getLinks().getLinks().add(link);
         }
         return rsdl;
     }
 
     public RSDL build() {
         try {
             rsdl = construct();
             rsdl.setRel(getRel());
             rsdl.setHref(getHref());
             rsdl.setDescription(getDescription());
             rsdl.setSchema(getSchema());
         } catch (Exception e) {
             e.printStackTrace();
             LOG.error("RSDL generation failure.", e);
         }
         return rsdl;
     }
 
     public RsdlBuilder rel(String rel) {
         this.rel = rel;
         return this;
     }
 
     public RsdlBuilder href(String href) {
         this.href = href;
         return this;
     }
 
     public RsdlBuilder schema(Schema schema) {
         this.schema = schema;
         return this;
     }
 
     public RsdlBuilder description(String description) {
         this.description = description;
         return this;
     }
 
     public String getHref() {
         return this.href;
     }
 
     public String getRel() {
         return this.rel;
     }
 
     public Schema getSchema() {
         return schema;
     }
 
     public String getDescription() {
         return this.description;
     }
 
     @Override
     public String toString() {
             return "RSDL Href: " + getHref() +
                                ", Description:" + getDescription() +
                                ", Links: " + (rsdl != null ? (rsdl.isSetLinks() ? rsdl.getLinks().getLinks().size() : "0") : "0") + ".";
     }
 
     public class LinkBuilder {
         private DetailedLink link = new DetailedLink();;
         public LinkBuilder url(String url) {
             link.setHref(url);
             return this;
         }
         public LinkBuilder rel(String rel) {
             link.setRel(rel);
             return this;
         }
         public LinkBuilder requestParameter(final String requestParameter) {
             link.setRequest(new Request());
             link.getRequest().setBody(new Body(){{setType(requestParameter);}});
             return this;
         }
         public LinkBuilder responseType(final String responseType) {
             link.setResponse(new Response(){{setType(responseType);}});
             return this;
         }
         public LinkBuilder httpMethod(HttpMethod httpMethod) {
             if(!link.isSetRequest()) {
                 link.setRequest(new Request());
             }
             link.getRequest().setHttpMethod(httpMethod);
             return this;
         }
         public DetailedLink build() {
             if (!link.getRequest().isSetBody()) {
                 link.getRequest().setBody(new Body());
             }
             return addParametersMetadata(link);
         }
     }
 
     public Collection<DetailedLink> getLinks() throws ClassNotFoundException, IOException {
         //SortedSet<Link> results = new TreeSet<Link>();
         List<DetailedLink> results = new ArrayList<DetailedLink>();
         List<Class<?>> classes = ReflectionHelper.getClasses(RESOURCES_PACKAGE);
         for (String path : apiResource.getRels()) {
             Class<?> resource = findResource(path, classes);
             results.addAll(describe(resource, entryPoint + "/" +  path, new HashMap<String, Type>()));
         }
         return results;
     }
 
     private Class<?> findResource(String path, List<Class<?>> classes) throws ClassNotFoundException, IOException {
         path = "/" + path;
         for (Class<?> clazz : classes) {
             if (path.equals(getPath(clazz))) {
                 return clazz;
             }
         }
         return null;
     }
 
     private String getPath(Class<?> clazz) {
         Path pathAnnotation = clazz.getAnnotation(Path.class);
         return pathAnnotation==null ? null : pathAnnotation.value();
     }
 
     public List<DetailedLink> describe(Class<?> resource, String prefix, Map<String, Type> parametersMap) throws ClassNotFoundException {
         //SortedSet<Link> results = new TreeSet<Link>();
         List<DetailedLink> results = new ArrayList<DetailedLink>();
         if (resource!=null) {
             for (Method m : resource.getMethods()) {
                 if (isConcreteReturnType(m, resource)) {
                     handleMethod(prefix, results, m, resource, parametersMap);
                 }
             }
         }
         return results;
     }
 
     private boolean isConcreteReturnType(Method method, Class<?> resource) {
         for (Method m : resource.getMethods()) {
             if (!m.equals(method)
                     && m.getName().equals(method.getName())
                     && parameterTypesEqual(m.getParameterTypes(), method.getParameterTypes())
                     && method.getReturnType().isAssignableFrom(m.getReturnType())) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean parameterTypesEqual(Class<?>[] types1, Class<?>[] types2) {
         if (types1.length!=types2.length) {
             return false;
         } else {
             for (int i=0; i<types1.length; i++) {
                 if (!(types1[i].isAssignableFrom(types2[i]) || types2[i].isAssignableFrom(types1[i]))) {
                     return false;
                 }
             }
             return true;
         }
     }
 
     private void addToGenericParamsMap (Class<?> resource, Type[] paramTypes, Type[] genericParamTypes, Map<String, Type> parametersMap) {
         for (int i=0; i<genericParamTypes.length; i++) {
             if (paramTypes[i].toString().length() == 1) {
                 //if the parameter type is generic - don't add to map, as it might override a more meaningful value:
                 //for example, without this check we could replace <"R", "Template"> with <"R", "R">, and lose information.
             } else {
                 //if the length is greater than 1, we have an actual type (e.g: "CdRoms"), and we want to add it to the
                 //map, even if it overrides an existing value.
                 parametersMap.put(genericParamTypes[i].toString(), paramTypes[i]);
             }
         }
     }
 
     private void handleMethod(String prefix, Collection<DetailedLink> results, Method m, Class<?> resource, Map<String, Type> parametersMap) throws ClassNotFoundException {
         if (isRequiresDescription(m)) {
             Class<?> returnType = findReturnType(m, resource, parametersMap);
             String returnTypeStr = getReturnTypeStr(returnType);
             if (m.isAnnotationPresent(javax.ws.rs.GET.class)) {
                 handleGet(prefix, results, returnTypeStr);
             } else if (m.isAnnotationPresent(PUT.class)) {
                 handlePut(prefix, results, returnTypeStr);
             } else if (m.isAnnotationPresent(javax.ws.rs.DELETE.class)) {
                 handleDelete(prefix, results, m);
             } else if (m.isAnnotationPresent(Path.class)) {
                 String path = m.getAnnotation(Path.class).value();
                 if (isAction(m)) {
                     handleAction(prefix, results, returnTypeStr, path);
                 } else {
                     if (isSingleEntityResource(m)) {
                         path = "{" + getSingleForm(prefix) + ":id}";
                     }
                     if (m.getGenericReturnType() instanceof ParameterizedType) {
                         ParameterizedType parameterizedType = (ParameterizedType)m.getGenericReturnType();
                         addToGenericParamsMap(resource, parameterizedType.getActualTypeArguments(), m.getReturnType().getTypeParameters(), parametersMap);
                     }
                     results.addAll(describe(returnType, prefix + "/" + path, new HashMap<String, Type>(parametersMap)));
                 }
             } else {
                 if (m.getName().equals(ADD)) {
                     handleAdd(prefix, results, m);
                 }
             }
         }
     }
 
     private void handleAction(String prefix, Collection<DetailedLink> results, String returnValueStr, String path) {
         results.add(new RsdlBuilder.LinkBuilder().url(prefix + "/" + path).rel(path).requestParameter(ACTION).responseType(returnValueStr).httpMethod(HttpMethod.POST).build());
     }
 
     private void handleDelete(String prefix, Collection<DetailedLink> results, Method m) {
         if (m.getParameterTypes().length>1) {
             Class<?>[] parameterTypes = m.getParameterTypes();
             Annotation[][] parameterAnnotations = m.getParameterAnnotations();
             for (int i=0; i<parameterTypes.length; i++) {
                 //ignore the id parameter (string), that's annotated with @PathParam
                 if (!( parameterTypes[i].equals(String.class) && (!(parameterAnnotations[i].length==0)))) {
                     results.add(new RsdlBuilder.LinkBuilder().url(prefix + "/{" + getSingleForm(prefix) + ":id}").rel(DELETE).requestParameter(parameterTypes[i].getSimpleName()).httpMethod(HttpMethod.DELETE).build());
                     return; //we can break, because we excpect only one parameter.
                 }
             }
         } else {
             results.add(new RsdlBuilder.LinkBuilder().url(prefix + "/{" + getSingleForm(prefix) + ":id}").rel(DELETE).httpMethod(HttpMethod.DELETE).build());
         }
     }
 
     private void handlePut(String prefix, Collection<DetailedLink> results, String returnValueStr) {
         results.add(new RsdlBuilder.LinkBuilder().url(prefix).rel(UPDATE).requestParameter(returnValueStr).responseType(returnValueStr).httpMethod(HttpMethod.PUT).build());
     }
 
     private void handleGet(String prefix, Collection<DetailedLink> results, String returnValueStr) {
         DetailedLink link = new RsdlBuilder.LinkBuilder().url(prefix).rel(GET).responseType(returnValueStr).httpMethod(HttpMethod.GET).build();
         results.add(link);
     }
 
     private DetailedLink addParametersMetadata(DetailedLink link) {
         String link_name = link.getHref() + "|rel=" + link.getRel();
         if (this.parametersMetaData.containsKey(link_name)) {
             Action action = this.parametersMetaData.get(link_name);
             if (action.getRequest() != null) {
                 addUrlParams(link, action);
                 addHeaderParams(link, action);
                 addBodyParams(link, action);
             }
         }
         return link;
     }
 
     private void addBodyParams(DetailedLink link, Action action) {
         if (action.getRequest().getBody() != null) {
             if (action.getRequest().getBody().getSignatures() != null) {
                 for (Signature signature : action.getRequest().getBody().getSignatures()) {
                     ParametersSet ps = new ParametersSet();
                     addBodyParams(ps, signature.getMandatoryArguments().entrySet(), true);
                     addBodyParams(ps, signature.getOptionalArguments().entrySet(), false);
                     link.getRequest().getBody().getParametersSets().add(ps);
                 }
             }
         }
     }
 
     private void addBodyParams(ParametersSet ps, Set<Entry<Object, Object>> entrySet, boolean required) {
         for (Entry<Object, Object> paramData : entrySet) {
             Parameter param = createBodyParam(paramData, required);
             ps.getParameters().add(param);
         }
     }
 
     private Parameter createBodyParam(Entry<Object, Object> mandatoryKeyValuePair, boolean required) {
         Parameter param = new Parameter();
         param.setRequired(required);
         String paramName = mandatoryKeyValuePair.getKey().toString();
         if (paramName.endsWith(COLLECTION_PARAMETER_YAML)) {
             param.setName(paramName.substring(0, paramName.length()-(COLLECTION_PARAMETER_YAML.length())));
             param.setType(COLLECTION_PARAMETER_RSDL);
             @SuppressWarnings("unchecked")
             Map<Object, Object> listParams = (Map<Object, Object>)mandatoryKeyValuePair.getValue();
             param.setParametersSet(new ParametersSet());
             for (Entry<Object, Object> listParamData : listParams.entrySet()) {
                 Parameter listParam = createBodyParam(listParamData, required);
                 param.getParametersSet().getParameters().add(listParam);
             }
         } else {
             param.setName(paramName);
             param.setType(mandatoryKeyValuePair.getValue().toString());
         }
         return param;
     }
 
     private void addHeaderParams(DetailedLink link, Action action) {
         if (action.getRequest().getHeaders() != null && !action.getRequest().getHeaders().isEmpty()) {
             link.getRequest().setHeaders(new Headers());
             for (Object key :  action.getRequest().getHeaders().keySet()) {
                 Header header = new Header();
                 header.setName(key.toString());
                 Object value = action.getRequest().getHeaders().get(key);
                 if (value != null) {
                     header.setValue(value.toString());
                 }
                 link.getRequest().getHeaders().getHeaders().add(header);
             }
         }
     }
 
     private void addUrlParams(DetailedLink link, Action action) {
         if (action.getRequest().getUrlparams() != null && !action.getRequest().getUrlparams().isEmpty()) {
             link.getRequest().setUrl(new Url());
             ParametersSet ps = new ParametersSet();
         for (Object key :  action.getRequest().getUrlparams().keySet()) {
                 Parameter param = new Parameter();
                 param.setName(key.toString());
                 Object value = action.getRequest().getUrlparams().get(key);
                 if (value != null) {
                     UrlParamData urlParamData = (UrlParamData)value;
                     param.setType(urlParamData.getType());
                     param.setContext(urlParamData.getContext());
                     param.setValue(urlParamData.getValue());
                     param.setRequired(urlParamData.getRequired()==null ? false : urlParamData.getRequired());
                 }
                 ps.getParameters().add(param);
             }
             link.getRequest().getUrl().getParametersSets().add(ps);
         }
     }
 
     private void handleAdd(String prefix, Collection<DetailedLink> results, Method m) {
         Class<?>[] parameterTypes = m.getParameterTypes();
         assert(parameterTypes.length==1);
         String s = parameterTypes[0].getSimpleName();
         s = handleExcpetionalCases(s, prefix); //TODO: refactor to a more generic solution
 
         results.add(new RsdlBuilder.LinkBuilder().url(prefix).rel(ADD).requestParameter(s).responseType(s).httpMethod(HttpMethod.POST).build());
     }
 
     private String handleExcpetionalCases(String s, String prefix) {
         if (s.equals("BaseDevice")) {
             if (prefix.contains("cdroms")) {
                 return "CdRom";
             }
             if (prefix.contains("nics")) {
                 return "NIC";
             }
             if (prefix.contains("disks")) {
                 return "Disk";
             }
         }
         return s;
     }
 
     /**
      * get the class name, without package prefix
      * @param returnValue
      * @return
      */
     private String getReturnTypeStr(Class<?> returnValue) {
         int lastIndexOf = returnValue.getSimpleName().lastIndexOf(".");
         String entityType = lastIndexOf==-1 ? returnValue.getSimpleName() : returnValue.getSimpleName().substring(lastIndexOf);
         return entityType;
     }
 
     private Class<?> findReturnType(Method m, Class<?> resource, Map<String, Type> parametersMap) throws ClassNotFoundException {
         for (Type superInterface : resource.getGenericInterfaces()) {
             if (superInterface instanceof ParameterizedType) {
                 ParameterizedType p = (ParameterizedType)superInterface;
                 Class<?> clazz = Class.forName(p.getRawType().toString().substring(p.getRawType().toString().lastIndexOf(' ')+1));
                 Map<String, Type> map = new HashMap<String, Type>();
                 for (int i=0; i<p.getActualTypeArguments().length; i++) {
                    if (!map.containsKey(clazz.getTypeParameters()[i].toString())) {
                         map.put(clazz.getTypeParameters()[i].toString(), p.getActualTypeArguments()[i]);
                     }
                 }
                 if (map.containsKey(m.getGenericReturnType().toString())) {
                     String type = map.get(m.getGenericReturnType().toString()).toString();
                     try {
                         Class<?> returnClass = Class.forName(type.substring(type.lastIndexOf(' ')+1));
                         return returnClass;
                     } catch (ClassNotFoundException e) {
                         break;
                     }
                 }
             }
         }
         if (parametersMap.containsKey(m.getGenericReturnType().toString())) {
             try {
                 Type type = parametersMap.get(m.getGenericReturnType().toString());
                 Class<?> returnClass = Class.forName(type.toString().substring(type.toString().indexOf(' ') +1));
                 return returnClass;
             } catch (ClassNotFoundException e) {
                 return m.getReturnType();
             }
         } else {
             return m.getReturnType();
         }
     }
 
     private boolean isSingleEntityResource(Method m) {
         Annotation[][] parameterAnnotations = m.getParameterAnnotations();
         for (int i=0; i<parameterAnnotations.length; i++) {
             for (int j=0; j<parameterAnnotations[j].length; j++) {
                 if (parameterAnnotations[i][j].annotationType().equals(PathParam.class)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean isAction(Method m) {
         return m.isAnnotationPresent(Actionable.class);
     }
 
 
     private boolean isRequiresDescription(Method m) {
         if (m.isAnnotationPresent(RsdlIgnore.class)) {
             return false;
         }
         boolean pathRelevant = !(m.isAnnotationPresent(Path.class) && m.getAnnotation(Path.class).value().contains(":"));
         boolean returnValueRelevant = !m.getReturnType().equals(CreationResource.class);
         return pathRelevant && returnValueRelevant;
     }
 
     //might need to truncate the plural 's', for example:
     //for "{api}/hosts/{host:id}/nics" return "nic"
     //but for "{api}/hosts/{host:id}/storage" return "storage" (don't truncate last character)
     private String getSingleForm(String prefix) {
         int startIndex = prefix.lastIndexOf('/')+1;
         int endPos = prefix.endsWith("s") ?  prefix.length() -1 : prefix.length();
         return prefix.substring(startIndex, endPos);
     }
 }
