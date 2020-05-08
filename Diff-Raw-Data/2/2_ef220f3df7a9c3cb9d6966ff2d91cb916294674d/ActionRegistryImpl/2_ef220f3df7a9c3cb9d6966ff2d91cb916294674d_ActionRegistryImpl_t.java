 package net.cyklotron.cms.accesslimits;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.event.ResourceChangeListener;
 import org.objectledge.coral.event.ResourceCreationListener;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 
 import com.google.common.base.Optional;
 
 public class ActionRegistryImpl
     implements ResourceChangeListener, ResourceCreationListener, ResourceDeletionListener,
     ActionRegistry
 {
     private static final String ACTIONS_ROOT = "/cms/accesslimits/actions";
 
     private final Map<String, Action> actions = new ConcurrentHashMap<>();
 
     public ActionRegistryImpl(CoralSessionFactory coralSessionFactory)
         throws EntityDoesNotExistException
     {
         try(CoralSession coralSession = coralSessionFactory.getRootSession())
         {
             Resource[] children = coralSession.getStore().getResourceByPath(ACTIONS_ROOT + "/*");
             for(Resource child : children)
             {
                 if(child instanceof ActionResource)
                 {
                     ActionResource childAction = (ActionResource)child;
                     actions.put(childAction.getName(), new Action(childAction));
                 }
             }
             ResourceClass<?> actionRc = coralSession.getSchema().getResourceClass(
               ActionResource.CLASS_NAME);
             coralSession.getEvent().addResourceChangeListener(this, actionRc);
             coralSession.getEvent().addResourceCreationListener(this, actionRc);
             coralSession.getEvent().addResourceDeletionListener(this, actionRc);
         }
     }
 
     @Override
     public void resourceCreated(Resource resource)
     {
         ActionResource actionResource = (ActionResource)resource;
         actions.put(actionResource.getName(), new Action(actionResource));
     }
 
     @Override
     public void resourceDeleted(Resource resource)
         throws Exception
     {
         actions.remove(resource.getName());
     }
 
     @Override
     public void resourceChanged(Resource resource, Subject subject)
     {
         ActionResource actionResource = (ActionResource)resource;
         actions.put(actionResource.getName(), new Action(actionResource));
     }
 
     @Override
     public Optional<Action> getAction(String name)
     {
         return Optional.fromNullable(actions.get(name));
     }
 }
