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
 package gc.carbon;
 
 import org.apache.abdera.model.*;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.Abdera;
 import org.springframework.stereotype.Component;
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.ApplicationContext;
 import org.restlet.Context;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Variant;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.WriterRepresentation;
 import org.restlet.data.*;
 import org.joda.time.*;
 
 import gc.carbon.domain.path.PathItem;
 import gc.carbon.domain.path.PathItemGroup;
 import gc.carbon.domain.data.DataCategory;
 import gc.carbon.domain.data.DataItem;
 import gc.carbon.domain.profile.StartEndDate;
 import gc.carbon.path.PathItemService;
 import gc.carbon.data.OnlyActiveDataService;
 import gc.carbon.profile.builder.v2.AtomFeed;
 import com.jellymold.utils.ThreadBeanHolder;
 
 import java.util.Date;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 import java.io.Writer;
 import java.io.IOException;
 
 @Component("serviceResource")
 @Scope("prototype")
 public class ServiceResource extends AMEEResource {
 
     public static AtomFeed ATOM_FEED = AtomFeed.getInstance();
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         List<Variant> variants = super.getVariants();
         variants.clear();
         variants.add(new Variant(MediaType.APPLICATION_ATOM_SERVICE_XML));
     }
 
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         Representation representation = null;
         if (variant.getMediaType().equals(MediaType.APPLICATION_ATOM_SERVICE_XML)) {
             representation = getAtomRepresentation();
             representation.setCharacterSet(CharacterSet.UTF_8);
             //TODO - Make sure this is the correct thing to do
             representation.setExpirationDate(new org.joda.time.DateTime().withHourOfDay(23).withSecondOfMinute(59).withMinuteOfHour(59).toDate());
             representation.setModificationDate(new org.joda.time.DateTime().toDate());
         }
         return representation;
     }
 
     @Override
     public org.apache.abdera.model.Element getAtomElement() {
 
         ApplicationContext springContext = (ApplicationContext) ThreadBeanHolder.get("springContext");
         PathItemService pathItemService = (PathItemService) springContext.getBean("pathItemService");
         PathItemGroup pathItemGroup = pathItemService.getProfilePathItemGroup();
 
         final Service service = ATOM_FEED.newService();
 
         Workspace ws = ATOM_FEED.newWorkspace(service);
 
         ws.setBaseUri(getRequest().getResourceRef().getBaseRef().getParentRef().toString());
         ws.setTitle("DataCategories");
 
         String path = getRequest().getResourceRef().getRelativeRef().getPath();
         if (path.isEmpty()) {
             addCollections(pathItemGroup.getRootPathItem(), ws);
         } else {
             PathItem pathItem = pathItemGroup.findByPath(path);
             if (pathItem != null) {
                 addCollections(pathItem, ws);
             }
         }
         return service;
 
     }
 
     protected Representation getAtomRepresentation() {
             return new WriterRepresentation(MediaType.APPLICATION_ATOM_SERVICE_XML) {
                 public void write(Writer writer) throws IOException {
                     getAtomElement().writeTo(writer);
                 }
             };
     }
 
     private void addCollections(PathItem pi, Workspace ws) {
 
         Set<PathItem> pathItems = pi.findChildrenByType("DC");
         if (pathItems.isEmpty()) {
             addCollection(ws, pi, false);
         } else {
             for (PathItem pii : pi.findChildrenByType("DC")) {
                 addCollection(ws, pii, true);
            }
         }
     }
 
     //TODO - Check this is the best was of doing this with Dig
     private void addCollection(Workspace ws, PathItem pii, boolean recurse) {
         DataCategory dc = dataService.getDataCategory(pii.getUid());
         if (dc.getItemDefinition() != null) {
 
             Reference href = new Reference(getRequest().getResourceRef().getParentRef(), pii.getFullPath());
 
             Collection col = ATOM_FEED.newCollection(ws);
             col.setHref(href.toString().substring(1));
             col.setTitle(pii.getFullPath());
             col.setAcceptsEntry();
 
             Categories cats = ATOM_FEED.newItemCategories(col);
             cats.setFixed(true);
            for (DataItem di : new OnlyActiveDataService(dataService).getDataItems(dc, new StartEndDate())) {
                 Category cat = ATOM_FEED.newCategory(cats);
                 cat.setTerm(di.getUid());
                 cat.setLabel(di.getItemDefinition().getName() + " (" + di.getItemValuesString() + ")");
             }
             if (recurse)
                 addCollections(pii, ws);
         } else {
             if (recurse)
                 addCollections(pii, ws);
         }
     }
 
 }
 
