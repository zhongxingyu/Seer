 package net.cyklotron.cms.rewrite;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.event.ResourceChangeListener;
 import org.objectledge.coral.event.ResourceCreationListener;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.schema.AttributeDefinition;
 import org.objectledge.coral.schema.AttributeFlags;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.schema.UnknownAttributeException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.ProtectedResource;
 import net.cyklotron.cms.site.SiteResource;
 
 import bak.pcj.map.LongKeyMap;
 import bak.pcj.map.LongKeyOpenHashMap;
 
 public abstract class ResourceBasedUrlRewriteParticipant<T extends Resource>
     implements ResourceCreationListener, ResourceDeletionListener, ResourceChangeListener,
     Startable, UrlRewriteParticipant
 {
     private final ResourceClass<T> rc;
 
     private final Map<SitePath, ResourceRef<T>> cache = new HashMap<>();
 
     private final LongKeyMap invCache = new LongKeyOpenHashMap();
 
     private final Lock r;
 
     private final Lock w;
 
     private AttributeDefinition<String> pathAttr;
 
     private final CoralSessionFactory coralSessionFactory;
 
     private void preloadCache(CoralSession coralSession)
         throws MalformedQueryException
     {
         QueryResults results = coralSession.getQuery().executeQuery(
             "FIND RESOURCE FROM " + rc.getName() + " WHERE DEFINED " + pathAttribute());
         for(T res : (List<T>)results.getList(1))
         {
             if(res.isDefined(pathAttr))
             {
                 String path = res.get(pathAttr);
                final SitePath sitePath = new SitePath(getSite(res), path);
                cache.put(sitePath, new ResourceRef<T>(res));
                invCache.put(res.getId(), sitePath);
             }
         }
     }
 
     public ResourceBasedUrlRewriteParticipant(String rcName, Class<T> rcl,
         CoralSessionFactory coralSessionFactory)
     {
         this.coralSessionFactory = coralSessionFactory;
         ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
         r = rwl.readLock();
         w = rwl.writeLock();
 
         try(CoralSession coralSession = coralSessionFactory.getRootSession())
         {
             ResourceClass<T> rc = coralSession.getSchema().getResourceClass(rcName, rcl);
             this.rc = rc;
             pathAttr = rc.getAttribute(pathAttribute(), String.class);
             if((pathAttr.getFlags() & (AttributeFlags.REQUIRED | AttributeFlags.READONLY)) != 0)
             {
                 throw new Error(pathAttribute() + " must not be REQUIRED or READONLY");
             }
             coralSession.getEvent().addResourceCreationListener(this, rc);
             coralSession.getEvent().addResourceDeletionListener(this, rc);
             coralSession.getEvent().addResourceChangeListener(this, rc);
         }
         catch(Exception e)
         {
             throw new ComponentInitializationError("initialization failed", e);
         }
     }
 
     public void start()
     {
         try(CoralSession coralSession = coralSessionFactory.getRootSession())
         {
             preloadCache(coralSession);
         }
         catch(MalformedQueryException e)
         {
             throw new RuntimeException("unexpected", e);
         }
     }
 
     public void stop()
     {
     }
 
     @Override
     public void resourceChanged(Resource resource, Subject subject)
     {
         w.lock();
         try
         {
             final long id = resource.getId();
            final SitePath oldSitePath = (SitePath)invCache.get(id);
            if(oldSitePath != null)
             {
                cache.remove(oldSitePath);
             }
             if(resource.isDefined(pathAttr))
             {
                 final String path = resource.get(pathAttr);
                 @SuppressWarnings("unchecked")
                 final T res = (T)resource;
                final SitePath sitePath = new SitePath(getSite(res), path);
                cache.put(sitePath, new ResourceRef<T>(res));
                invCache.put(id, sitePath);
             }
         }
         finally
         {
             w.unlock();
         }
     }
 
     @Override
     public void resourceDeleted(Resource resource)
         throws Exception
     {
         w.lock();
         try
         {
            final SitePath sitePath = (SitePath)invCache.remove(resource.getId());
             if(cache != null)
             {
                cache.remove(sitePath);
             }
         }
         finally
         {
             w.unlock();
         }
     }
 
     @Override
     public void resourceCreated(Resource resource)
     {
         w.lock();
         try
         {
             if(resource.isDefined(pathAttr))
             {
                 final String path = resource.get(pathAttr);
                 @SuppressWarnings("unchecked")
                 final T res = (T)resource;
                final SitePath sitePath = new SitePath(getSite(res), path);
                cache.put(sitePath, new ResourceRef<T>(res));
                invCache.put(resource.getId(), sitePath);
             }
         }
         finally
         {
             w.unlock();
         }
     }
 
     @Override
     public boolean matches(SitePath path)
     {
         r.lock();
         try
         {
             return cache.containsKey(path);
         }
         finally
         {
             r.unlock();
         }
     }
 
     @Override
     public Set<SitePath> getPaths()
     {
         r.lock();
         try
         {
             return new HashSet<>(cache.keySet());
         }
         finally
         {
             r.unlock();
         }
     }
 
     @Override
     public Collection<RewriteEntry> getRewriteInfo()
     {
         r.lock();
         try(CoralSession coralSession = coralSessionFactory.getRootSession())
         {
             Collection<RewriteEntry> infos = new ArrayList<>(cache.size());
             for(Map.Entry<SitePath, ResourceRef<T>> entry : cache.entrySet())
             {
                 try
                 {
                     final SitePath sitePath = entry.getKey();
                     final T resource = entry.getValue().get(coralSession);
                     infos.add(new RewriteEntry(getName(), sitePath.getSite().getName(), sitePath
                         .getPath(), formatRewrite(getTarget(resource)), getDescription(resource)));
                 }
                 catch(EntityDoesNotExistException e)
                 {
                     // resource deleted concurrently - ignore
                 }
             }
             return infos;
         }
         finally
         {
             r.unlock();
         }
     }
 
     @Override
     public void drop(SitePath path)
     {
         w.lock();
         try
         {
             if(cache.containsKey(path))
             {
                 ResourceRef<T> ref = cache.remove(path);
                 invCache.remove(ref.getId());
                 try(CoralSession coralSession = coralSessionFactory.getRootSession())
                 {
                     T resource = ref.get(coralSession);
                     resource.unset(pathAttr);
                     resource.update();
                 }
                 catch(EntityDoesNotExistException e)
                 {
                     // resource disappeared - ignore
                 }
                 catch(UnknownAttributeException | ValueRequiredException e)
                 {
                     throw new RuntimeException("unexpected", e);
                 }
             }
         }
         finally
         {
             w.unlock();
         }
     }
 
     @Override
     public RewriteTarget rewrite(SitePath path)
     {
         r.lock();
         try
         {
             if(cache.containsKey(path))
             {
                 try(CoralSession coralSession = coralSessionFactory.getRootSession())
                 {
                     ResourceRef<T> ref = cache.get(path);
                     T resource = ref.get(coralSession);
                     return getTarget(resource);
                 }
                 catch(EntityDoesNotExistException e)
                 {
                     // resource disappeared - can't rewrite
                     return null;
                 }
             }
             return null;
         }
         finally
         {
             r.unlock();
         }
     }
 
     @Override
     public SitePath path(Object object)
     {
         if(rc.getJavaClass().isAssignableFrom(object.getClass()))
         {
             @SuppressWarnings("unchecked")
             long id = ((T)object).getId();
             return (SitePath)invCache.get(id);
         }
         return null;
     }
 
     public ProtectedResource guard(SitePath path)
     {
         r.lock();
         try
         {
             if(cache.containsKey(path))
             {
                 try(CoralSession coralSession = coralSessionFactory.getRootSession())
                 {
                     ResourceRef<T> ref = cache.get(path);
                     T resource = ref.get(coralSession);
                     if(resource instanceof ProtectedResource)
                     {
                         return (ProtectedResource)resource;
                     }
                 }
                 catch(EntityDoesNotExistException e)
                 {
                     // resource disappeared - can't rewrite
                     return null;
                 }
             }
             return null;
         }
         finally
         {
             r.unlock();
         }
     }
 
     protected abstract String pathAttribute();
 
     protected abstract RewriteTarget getTarget(T resource);
 
     protected abstract String getDescription(T resource);
 
     protected abstract SiteResource getSite(T resource);
 
     private String formatRewrite(RewriteTarget rewrite)
     {
         StringBuilder b = new StringBuilder();
         b.append("/ledge/x/").append(rewrite.getNode().getIdString());
         Map<String, List<String>> params = rewrite.getParameters();
         if(params.size() > 0)
         {
             b.append('?');
             for(Map.Entry<String, List<String>> entry : params.entrySet())
             {
                 for(String value : entry.getValue())
                 {
                     b.append(entry.getKey()).append('=').append(value);
                 }
             }
         }
         return b.toString();
     }
 
     private class ResourceRef<R extends Resource>
     {
         private final long id;
 
         private WeakReference<R> ref;
 
         public ResourceRef(R resource)
         {
             id = resource.getId();
             ref = new WeakReference<>(resource);
         }
 
         public long getId()
         {
             return id;
         }
 
         public R get(CoralSession coralSession)
             throws EntityDoesNotExistException
         {
             R r = ref.get();
             if(r == null)
             {
                 r = (R)coralSession.getStore().getResource(id);
                 ref = new WeakReference<>(r);
             }
             return r;
         }
     }
 }
