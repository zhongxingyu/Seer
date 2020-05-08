 package com.amee.restlet.environment;
 
 import com.amee.domain.algorithm.AlgorithmContext;
 import com.amee.restlet.BaseResource;
 import com.amee.service.data.DataConstants;
 import com.amee.service.definition.DefinitionService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.Method;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.Serializable;
 import java.util.Map;
 
 @Component
 @Scope("prototype")
 public class AlgorithmContextsResource extends BaseResource implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private DefinitionService definitionService;
 
     @Autowired
     private DefinitionBrowser definitionBrowser;
 
     private AlgorithmContext newAlgorithmContext;
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         definitionBrowser.setEnvironmentUid(request.getAttributes().get("environmentUid").toString());
     }
 
     @Override
     public boolean isValid() {
         return super.isValid() &&
                 (definitionBrowser.getEnvironment() != null);
     }
 
     @Override
     public String getTemplatePath() {
         return DataConstants.VIEW_ALGORITHM_CONTEXTS;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.put("browser", definitionBrowser);
         values.put("environment", definitionBrowser.getEnvironment());
         values.put("algorithmContexts", definitionBrowser.getAlgorithmContexts());
         return values;
     }
 
     @Override
     public JSONObject getJSONObject() throws JSONException {
         JSONObject obj = new JSONObject();
         if (isGet()) {
             obj.put("environment", definitionBrowser.getEnvironment().getIdentityJSONObject());
             JSONArray algorithmContexts = new JSONArray();
             for (AlgorithmContext algorithmContext : definitionBrowser.getAlgorithmContexts()) {
                 algorithmContexts.put(algorithmContext.getJSONObject(false));
             }
             obj.put("algorithms", algorithmContexts);
         } else if (getRequest().getMethod().equals(Method.POST)) {
             obj.put("algorithm", newAlgorithmContext.getJSONObject());
         }
         return obj;
     }
 
     @Override
     public Element getElement(Document document) {
         Element element = document.createElement("AlgorithmContextsResource");
         if (isGet()) {
             element.appendChild(definitionBrowser.getEnvironment().getIdentityElement(document));
             Element algorithmContextsElement = document.createElement("AlgorithmContexts");
             for (AlgorithmContext algorithmContext : definitionBrowser.getAlgorithmContexts()) {
                 algorithmContextsElement.appendChild(algorithmContext.getElement(document, false));
             }
             element.appendChild(algorithmContextsElement);
         } else if (getRequest().getMethod().equals(Method.POST)) {
             element.appendChild(newAlgorithmContext.getElement(document));
         }
         return element;
     }
 
     @Override
     public void handleGet() {
         log.debug("handleGet");
         if (definitionBrowser.getAlgorithmActions().isAllowList()) {
             super.handleGet();
         } else {
             notAuthorized();
         }
     }
 
     @Override
     public boolean allowPost() {
         return true;
     }
 
     @Override
     public void acceptRepresentation(Representation entity) {
         log.debug("post");
         if (definitionBrowser.getAlgorithmActions().isAllowCreate()) {
             Form form = getForm();
             if (form.getFirstValue("name") != null) {
                 newAlgorithmContext = new AlgorithmContext(definitionBrowser.getEnvironment());
                 newAlgorithmContext.setName(form.getFirstValue("name"));
                 newAlgorithmContext.setContent(form.getFirstValue("content"));
             }
             if (newAlgorithmContext != null) {
                 if (isStandardWebBrowser()) {
                     definitionService.save(newAlgorithmContext);
                     success();
                 } else {
                     // return a response for API calls
                     super.handleGet();
                 }
             } else {
                 badRequest();
             }
         } else {
             notAuthorized();
         }
     }
 }
