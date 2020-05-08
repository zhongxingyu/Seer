 package gc.carbon;
 
 import com.jellymold.kiwi.Environment;
 import com.jellymold.kiwi.User;
 import com.jellymold.kiwi.environment.EnvironmentService;
 import com.jellymold.utils.HeaderUtils;
 import com.jellymold.utils.ThreadBeanHolder;
 import gc.carbon.domain.path.PathItem;
 import gc.carbon.profile.ProfileService;
 import com.jellymold.utils.BaseResource;
 import gc.carbon.data.DataService;
 import org.restlet.Context;
 import org.restlet.resource.Representation;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.MediaType;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.apache.xerces.dom.DocumentImpl;
 
 import java.util.Map;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 @Component("AMEEResource")
 public class AMEEResource extends BaseResource implements BeanFactoryAware {
 
     protected BeanFactory beanFactory;
     protected Environment environment;
     protected PathItem pathItem;
 
     @Autowired
     protected ProfileService profileService;
 
     @Autowired
     protected DataService dataService;
 
     @Autowired
     protected EnvironmentService environmentService;
 
 
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         environment = EnvironmentService.getEnvironment();
         setPathItem();
     }
 
     protected Representation getDomRepresentation() throws ResourceException {
         Document document = new DocumentImpl();
         Element element = document.createElement("Resources");
         if (!getVersion().isVersionOne()) {
             element.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns","http://schemas.amee.cc/2.0");
         }
         element.appendChild(getElement(document));
         document.appendChild(element);
         return new DomRepresentation(MediaType.APPLICATION_XML, document);
     }
 
     public int getItemsPerPage() {
         int itemsPerPage = EnvironmentService.getEnvironment().getItemsPerPage();
         String itemsPerPageStr = getRequest().getResourceRef().getQueryAsForm().getFirstValue("itemsPerPage");
         if (itemsPerPageStr == null) {
             itemsPerPageStr = HeaderUtils.getHeaderFirstValue("ItemsPerPage", getRequest());
         }
         if (itemsPerPageStr != null) {
             itemsPerPage = Integer.parseInt(itemsPerPageStr);
         }
         return itemsPerPage;
     }
 
     private void setPathItem() {
         pathItem = (PathItem) ThreadBeanHolder.get("pathItem");
     }
 
     public PathItem getPathItem() {
         return pathItem;    
     }
 
     public Environment getEnvironment() {
         return environment;
     }
 
 
     //TODO - Implementing here so that subclasses are not required to. Admin client templates will be phased out in time.
     public String getTemplatePath() {
         return null;
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.put("apiVersions", environmentService.getAPIVersions());
         return values;
     }
 
     public APIVersion getVersion() {
         User user = (User) ThreadBeanHolder.get("user");
         return user.getApiVersion();
     }
 }
