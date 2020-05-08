 /*
  * Copyright 2011 Erlend Hamnaberg
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.hamnaberg.confluence.atompub;
 
 import bucket.core.comparators.LastModificationDateComparator;
 import com.atlassian.confluence.security.SpacePermission;
 import com.atlassian.confluence.spaces.Space;
 import com.atlassian.confluence.spaces.SpaceManager;
 import com.atlassian.confluence.spaces.SpaceType;
 import com.atlassian.confluence.spaces.SpacesQuery;
 import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
 import com.atlassian.core.bean.EntityObject;
 import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
 import com.atlassian.user.User;
 import org.apache.abdera.Abdera;
 import org.apache.abdera.model.Collection;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.model.Link;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 import java.util.*;
 
 @Path("spaces")
 @Produces("application/atom+xml")
 @Consumes("application/atom+xml")
 @AnonymousAllowed
 public class SpaceFeed {
 
     // We just have to define the variables and the setters, then Spring injects the correct objects for us to use. Simple and efficient.
     // You just need to know *what* you want to inject and use.
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private final SpaceManager spaceManager;
     private final Abdera abdera = Abdera.getInstance();
     private Comparator<EntityObject> reverseLastModifiedComporator;
 
     public SpaceFeed(SpaceManager spaceManager) {
         this.spaceManager = spaceManager;
         reverseLastModifiedComporator = Collections.reverseOrder(new LastModificationDateComparator());
     }
 
     @GET
     public Response spaces(@Context UriInfo info) {
         User user = AuthenticatedUserThreadLocal.getUser();
         Feed feed = abdera.newFeed();
         feed.setId(info.getRequestUri().toString());
         feed.setTitle("Confluence Space feed");
         SpacesQuery query = SpacesQuery.newQuery().forUser(user).withSpaceType(SpaceType.GLOBAL).withPermission(SpacePermission.VIEWSPACE_PERMISSION).build();
         List<Space> spaces = new ArrayList<Space>(spaceManager.getAllSpaces(query));
         Collections.sort(spaces, reverseLastModifiedComporator);
         UriBuilder uriBuilder = info.getRequestUriBuilder();
         if (spaces.size() >= 1) {
             feed.setUpdated(spaces.get(0).getLastModificationDate());
         }
         else {
             feed.setUpdated(new Date());
         }
         for (Space space : spaces) {
            Entry entry = createSpaceEntry(uriBuilder, space);
             feed.addEntry(entry);
         }
         return Response.ok(new AbderaResponseOutput(feed)).build();
     }
 
     @Path("{key}")
     @GET
     public Response space(@Context UriInfo info, @PathParam("key") String key) {
         Space space = null;
         try {
             space = spaceManager.getSpace(key);
         } catch (Exception e) {
             logger.warn("Exception while fetching space", e);
         }
         if (space == null) {
             return Response.status(404).build();
         }
        Entry entry = createSpaceEntry(info.getBaseUriBuilder().path(getClass()), space);
         return Response.ok(new AbderaResponseOutput(entry)).build();
     }
 
    private Entry createSpaceEntry(UriBuilder uriBuilder, Space space) {
         Entry entry = abdera.newEntry();
         entry.setId("urn:confluence:space:id:" + space.getId());
         entry.setTitle(space.getName());
         entry.addCategory(ConfluenceUtil.createCategory(ConfluenceUtil.SPACE_TERM));
         entry.addAuthor(space.getCreatorName());
         entry.setUpdated(space.getLastModificationDate());
         entry.setSummary("");
         entry.addLink(uriBuilder.clone().path(space.getKey()).build().toString(), Link.REL_SELF);
        Link link = entry.addLink(space.getHomePage().getUrlPath(), Link.REL_ALTERNATE);
         link.setMimeType("text/html");
         entry.addExtension(createCollection(uriBuilder, space, "pages"));
         entry.addExtension(createCollection(uriBuilder, space, "news"));
         //entry.addLink(entry.addLink(uriBuilder.clone().segment(space.getKey()).build().toString(), "feed"));
         return entry;
     }
 
     private Collection createCollection(UriBuilder uriBuilder, Space space, String name) {
         Collection pageCollection = abdera.getFactory().newCollection();
         pageCollection.acceptsNothing();
         pageCollection.setTitle(name);
         pageCollection.setHref(uriBuilder.clone().segment(space.getKey()).segment(name).build().toString());
         return pageCollection;
     }
 
 }
