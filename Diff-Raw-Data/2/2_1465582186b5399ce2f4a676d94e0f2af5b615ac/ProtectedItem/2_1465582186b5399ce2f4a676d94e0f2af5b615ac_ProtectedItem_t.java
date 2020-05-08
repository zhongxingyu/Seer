 package net.cyklotron.cms.accesslimits;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.event.ResourceChangeListener;
 import org.objectledge.coral.event.ResourceCreationListener;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.web.ratelimit.impl.RequestInfo;
 import org.objectledge.web.ratelimit.impl.Rule;
 import org.objectledge.web.ratelimit.impl.RuleFactory;
 import org.objectledge.web.ratelimit.rules.ParseException;
 
 public class ProtectedItem
     implements ResourceChangeListener, ResourceCreationListener, ResourceDeletionListener
 {
     private final ProtectedItemResource res;
 
     private final Logger log;
 
     private volatile Pattern urlPattern;
 
     private volatile List<Rule> rules;
 
     public ProtectedItem(ProtectedItemResource res, CoralSession coralSession, Logger log)
         throws EntityDoesNotExistException
     {
         this.res = res;
         this.log = log;
         initialize(res);
         coralSession.getEvent().addResourceChangeListener(this, res);
         coralSession.getEvent().addResourceChangeListener(this,
             coralSession.getSchema().getResourceClass(RuleResource.CLASS_NAME));
     }
 
     @Override
     public void resourceChanged(Resource resource, Subject subject)
     {
         if(resource instanceof ProtectedItemResource || resource instanceof RuleResource
             && resource.getParent().equals(res))
         {
             initialize(res);
         }
     }
 
     @Override
     public void resourceCreated(Resource resource)
     {
         if(resource instanceof RuleResource && resource.getParent().equals(res))
         {
             initialize(res);
         }
     }
 
     @Override
     public void resourceDeleted(Resource resource)
         throws Exception
     {
         if(resource instanceof RuleResource && resource.getParent().equals(res))
         {
             initialize(res);
         }
     }
 
     private void initialize(ProtectedItemResource res)
     {
         try
         {
             urlPattern = Pattern.compile(res.getUrlPattern());
             Resource[] children = res.getChildren();
             List<RuleResource> ruleDefs = new ArrayList<>();
             for(Resource child : children)
             {
                 if(child instanceof RuleResource)
                 {
                     ruleDefs.add((RuleResource)child);
                 }
             }
             Collections.sort(ruleDefs, RuleResource.BY_PRIORITY);
             List<Rule> newRules = new ArrayList<>(ruleDefs.size());
             for(RuleResource def : ruleDefs)
             {
                newRules.add(RuleFactory.getInstance().newRule(def.getId(), def.getRuleDefinition()));
             }
             rules = Collections.unmodifiableList(newRules);
         }
         catch(ParseException | PatternSyntaxException e)
         {
             log.error("invalid protected item definition #" + res.getId(), e);
         }
     }
 
     public boolean matches(RequestInfo request)
     {
         return urlPattern.matcher(request.getPath()).matches();
     }
 
     public List<Rule> getRules()
     {
         return rules;
     }
 }
