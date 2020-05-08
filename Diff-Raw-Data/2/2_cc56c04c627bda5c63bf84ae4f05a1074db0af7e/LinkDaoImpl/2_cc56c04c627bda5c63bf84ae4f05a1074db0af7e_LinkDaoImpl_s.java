 package org.mule.galaxy.impl.link;
 
 import static org.mule.galaxy.event.DefaultEvents.ENTRY_CREATED;
 import static org.mule.galaxy.event.DefaultEvents.ENTRY_DELETED;
 import static org.mule.galaxy.event.DefaultEvents.ENTRY_MOVED;
 import static org.mule.galaxy.event.DefaultEvents.ENTRY_VERSION_DELETED;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.Entry;
 import org.mule.galaxy.EntryVersion;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.Link;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.event.EntryCreatedEvent;
 import org.mule.galaxy.event.EntryDeletedEvent;
 import org.mule.galaxy.event.EntryMovedEvent;
 import org.mule.galaxy.event.EntryVersionDeletedEvent;
 import org.mule.galaxy.event.annotation.BindToEvents;
 import org.mule.galaxy.event.annotation.OnEvent;
 import org.mule.galaxy.impl.jcr.JcrUtil;
 import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.QueryException;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.util.SecurityUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springmodules.jcr.JcrCallback;
 
 @BindToEvents({ENTRY_CREATED, ENTRY_MOVED, ENTRY_DELETED, ENTRY_VERSION_DELETED})
 public class LinkDaoImpl extends AbstractReflectionDao<Link> implements LinkDao, ApplicationContextAware {
 
     private Registry registry;
     private ApplicationContext context;
     
     public LinkDaoImpl() throws Exception {
         super(Link.class, "links", true);
     }
 
     /**
      * Checks for 1) Links which were not resolved and see if this new entry matches them 
      * 2) Links which are associated with this ID and tries to resolve them
      * 
      * @param id
      * @param session
      * @throws IOException
      * @throws RepositoryException
      */
     protected void addLinks(String id, Session session) throws IOException, RepositoryException {
         try {
             // First, see if this item which was moved matches any outstanding unmatched links 
             Item item = getRegistry().getItemById(id);
             List<String> versionIds = getVersionIds(id);
             
             StringBuilder stmt = new StringBuilder();
             stmt.append("//").append(rootNode)
              .append("/*[not(@linkedTo) and jcr:like(@linkedToPath, '%")
              .append(item.getName())
              .append("%')]");
             
             QueryManager qm = getQueryManager(session);
             Query q = qm.createQuery(stmt.toString(), Query.XPATH);
             
             QueryResult qr = q.execute();
             
             for (NodeIterator nodes = qr.getNodes(); nodes.hasNext();) {
                 Node node = nodes.nextNode();
                 Item linkItem = getRegistry().getItemById(JcrUtil.getStringOrNull(node, "item"));
                 String path = JcrUtil.getStringOrNull(node, "linkedToPath");
                 Item resolve = registry.resolve(linkItem, path);
                 if (resolve != null) {
                     node.setProperty("linkedTo", resolve.getId());
                 }
             }
 
             // Now try to resolve links which are associated with this item
             stmt = new StringBuilder();
             stmt.append("//").append(rootNode)
              .append("/*[not(@linkedTo) and (@item = '")
              .append(id);
             
             // Find the children of this node....
             for (String childId : versionIds) {
                 stmt.append("' or @item = '")
                     .append(childId);
             }
             stmt.append("')]");
 
             q = qm.createQuery(stmt.toString(), Query.XPATH);
             for (NodeIterator nodes = q.execute().getNodes(); nodes.hasNext();) {
                 Node node = nodes.nextNode();
                 Item linkItem = getRegistry().getItemById(JcrUtil.getStringOrNull(node, "item"));
                 String path = JcrUtil.getStringOrNull(node, "linkedToPath");
                 Item resolve = registry.resolve(linkItem, path);
                 
                 if (resolve != null) {
                     node.setProperty("linkedTo", resolve.getId());
                 }
             }
         } catch (NotFoundException e) {
            throw new RuntimeException(e);
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         } catch (AccessException e) {
             throw new RuntimeException(e);
         }
     }
     
     protected void deleteAssociatedLinks(String id, Session session) throws IOException, RepositoryException {
         StringBuilder stmt = new StringBuilder();
         stmt.append("//").append(rootNode)
          .append("/*[@item = '")
          .append(id)
          .append("' or linkedTo = '")
          .append(id)
          .append("']");
         
         QueryManager qm = getQueryManager(session);
         Query q = qm.createQuery(stmt.toString(), Query.XPATH);
         
         QueryResult qr = q.execute();
         
         for (NodeIterator nodes = qr.getNodes(); nodes.hasNext();) {
             Node node = nodes.nextNode();
             
             Boolean auto = JcrUtil.getBooleanOrNull(node, "autoDetected");
             String linkedTo = JcrUtil.getStringOrNull(node, "linkedTo");
             if (auto != null && auto && id.equals(linkedTo)) {
                 // we may want to auto resolve this again in the future
                 node.setProperty("linkedTo", (String) null);
             } else {
                 node.remove();
             }
         }
     }
     
     protected void clearDetectedLinks(final String id, Session session) throws IOException, RepositoryException {
         final StringBuilder stmt = new StringBuilder();
         stmt.append("//").append(rootNode)
             .append("/*[@item = '")
             .append(id)
             .append("' or linkedTo = '")
             .append(id);
         
         // Find the children of this node....
         for (String childId : getVersionIds(id)) {
             stmt.append("' or @item = '")
                 .append(childId)
                 .append("' or linkedTo = '")
                 .append(childId);
         }
         
         stmt.append("']");
         
         QueryManager qm = getQueryManager(session);
         Query q = qm.createQuery(stmt.toString(), Query.XPATH);
         
         QueryResult qr = q.execute();
         
         for (NodeIterator nodes = qr.getNodes(); nodes.hasNext();) {
             Node node = nodes.nextNode();
             
             Boolean auto = JcrUtil.getBooleanOrNull(node, "autoDetected");
             if (auto != null && auto) {
                 // we may want to auto resolve this again in the future
                 node.setProperty("linkedTo", (String) null);
             }
         }
     }
 
     private List<String> getVersionIds(final String id) {
         final List<String> versionIds = new ArrayList<String>();
         try {
             Item item = registry.getItemById(id);
             
             if (item instanceof Entry) {
 
                 for (EntryVersion ev : ((Entry) item).getVersions()) {
                     versionIds.add(ev.getId());
                 }
             }
         } catch (NotFoundException e) {
             throw new RuntimeException(e);
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         } catch (AccessException e) {
             throw new RuntimeException(e);
         }
         return versionIds;
     }
     
     public List<Link> getReciprocalLinks(Item item, final String property) {
         StringBuilder q = new StringBuilder();
         String path;
         String name;
         
         if (item instanceof EntryVersion) {
             EntryVersion ev = (EntryVersion) item;
             path = item.getParent().getPath() + "?version=" + ev.getVersionLabel();
             name = item.getParent().getName() + "?version=" + ev.getVersionLabel();
         } else {
             path = item.getPath();
             name = item.getName();
         }
         
         q.append("//").append(rootNode).append("/*[(@")
          .append("linkedToPath = '").append(name).append("' or @linkedToPath = '")
          .append(path).append("' or @linkedTo = '").append(item.getId());
         
         q.append("') and property = '").append(property);
         q.append("']");
         
         return (List<Link>) doQuery(q.toString());
     }
 
     public List<Link> getLinks(final Item item, final String property) {
         StringBuilder q = new StringBuilder();
         q.append("//").append(rootNode)
          .append("/*[@item = '")
          .append(item.getId())
          .append("' and property = '")
          .append(property)
          .append("']");
         
         return (List<Link>) doQuery(q.toString());
     }
     
     public void deleteLinks(final Item item, final String property) {
         execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 deleteLinks(item, property, session);
                 return null;
             }
         });
     }
 
     protected void deleteLinks(Item item, String property, Session session) throws IOException, RepositoryException {
         StringBuilder stmt = new StringBuilder();
         stmt.append("//").append(rootNode)
          .append("/*[@item = '")
          .append(item.getId())
          .append("' and property = '")
          .append(property)
          .append("']");
         
         QueryManager qm = getQueryManager(session);
         Query q = qm.createQuery(stmt.toString(), Query.XPATH);
         
         QueryResult qr = q.execute();
         
         for (NodeIterator nodes = qr.getNodes(); nodes.hasNext();) {
             Node node = nodes.nextNode();
             node.remove();
         }
     }
 
     public Collection<Item> getReciprocalItems(final String property, 
                                                final boolean like, 
                                                final Object value) {
         Collection<Link> links = getLinks(property, "item", like, value);
 
         Set<Item> items = new HashSet<Item>();
         for (Link l : links) {
             Item linkedTo = l.getLinkedTo();
             
             items.add(linkedTo);
         }
         
         return items;
     }
     
     public List<Link> getLinks(final String property,
                                String linkProperty,
                                final boolean like, 
                                final Object value) {
         // Find the Items which we'll use for the next part of the query
         Set<Item> linkToItems = new HashSet<Item>();
 
         try {
 
             if (value instanceof Collection) {
                 Collection values = (Collection) value;
                 for (Object v : values) {
                     findItemsForPath(linkToItems, v.toString(), like);
                 }
             } else {
                 findItemsForPath(linkToItems, value.toString(), like);
             }
 
         } catch (QueryException e) {
             throw new RuntimeException(e);
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         }
         
         if (linkToItems.size() == 0) return Collections.emptyList();
         
         // Now find all the links where the linkedTo property is equal to one of the above items
         StringBuilder stmt = new StringBuilder();
         stmt.append("//").append(rootNode).append("/*[(@");
         
         boolean first = true;
         for (Item i : linkToItems) {
             if (first) first = false;
             else stmt.append("' or ");
             
             stmt.append(linkProperty).append(" = '").append(i.getId());
         }
 
         stmt.append("') and property = '").append(property);
         stmt.append("']");
         
         return doQuery(stmt.toString());
     }
 
     /**
      * Find all the items which match the given path
      * @param items
      * @param path
      * @param like
      * @throws RegistryException
      * @throws QueryException
      */
     private void findItemsForPath(Set<Item> items, String path, final boolean like)
         throws RegistryException, QueryException {
         org.mule.galaxy.query.Query q = 
             new org.mule.galaxy.query.Query();
         
         int idx = path.lastIndexOf('/');
         if (idx != -1) {
             q.fromPath(path.substring(0, idx));
             path = path.substring(idx+1);
         }
 
         String version = null;
         idx = path.lastIndexOf('?');
         if (idx != -1) {
             int vidx = path.lastIndexOf("version=");
             if (vidx != -1) {
                 version = path.substring(vidx + "version=".length());
             }
             
             path = path.substring(0, idx);
             q.setSelectTypes(EntryVersion.class, ArtifactVersion.class);
         }
         
         if (!like) {
             q.add(OpRestriction.eq("name", path));
         } else {
             q.add(OpRestriction.like("name", path));
         }
         
         if (version != null) {
             q.add(OpRestriction.eq("version", version));
         }
         
         SearchResults results = registry.search(q);
         
         items.addAll(results.getResults());
         
         if (like && version == null) {
             q.setSelectTypes(EntryVersion.class, ArtifactVersion.class);
             results = registry.search(q);
             items.addAll(results.getResults());
         }
     }
     
     public List<Link> getLinks(final String property, final boolean like, final Object path) {
         return getLinks(property, "linkedTo", like, path);
     }
     
     @OnEvent
     public void onEvent(final EntryDeletedEvent deleted) {
         SecurityUtils.doPriveleged(new Runnable() {
             public void run() {
                 execute(new JcrCallback() {
                     public Object doInJcr(final Session session) throws IOException, RepositoryException {
                         deleteAssociatedLinks(deleted.getItemId(), session);
                         return null;
                     }
                 });
             }
         });
     }
 
     @OnEvent
     public void onEvent(final EntryVersionDeletedEvent deleted) {
         SecurityUtils.doPriveleged(new Runnable() {
             public void run() {
                 execute(new JcrCallback() {
                     public Object doInJcr(final Session session) throws IOException, RepositoryException {
                         deleteAssociatedLinks(deleted.getItemId(), session);
                         return null;
                     }
                 });
             }
         });
     }
     
     @OnEvent
     public void onEvent(final EntryCreatedEvent created) {
         SecurityUtils.doPriveleged(new Runnable() {
             public void run() {
                 execute(new JcrCallback() {
                     public Object doInJcr(final Session session) throws IOException, RepositoryException {
                         addLinks(created.getItemId(), session);
                         return null;
                     }
                 });
             }
         });
     }
     
     @OnEvent
     public void onEvent(final EntryMovedEvent created) {
         SecurityUtils.doPriveleged(new Runnable() {
             public void run() {
                 execute(new JcrCallback() {
                     public Object doInJcr(final Session session) throws IOException, RepositoryException {
                         clearDetectedLinks(created.getItemId(), session);
                         addLinks(created.getItemId(), session);
                         return null;
                     }
                 });
             }
         });
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.context = applicationContext;
     }
 
     public Registry getRegistry() {
         if (registry == null) {
             registry = (Registry) context.getBean("registry");
         }
         return registry;
     }
 
     
 }
