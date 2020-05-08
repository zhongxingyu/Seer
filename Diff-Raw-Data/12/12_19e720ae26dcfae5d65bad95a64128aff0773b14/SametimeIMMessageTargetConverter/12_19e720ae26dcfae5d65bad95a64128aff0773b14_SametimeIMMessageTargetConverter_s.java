 /**
  * Hudson Sametime Plugin
  */
 package hudson.plugins.sametime.im.transport;
 
 import hudson.plugins.sametime.im.IMMessageTarget;
 import hudson.plugins.sametime.im.IMMessageTargetConversionException;
 import hudson.plugins.sametime.im.IMMessageTargetConverter;
 import hudson.plugins.sametime.tools.Assert;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.lotus.sametime.core.comparch.STSession;
 import com.lotus.sametime.core.types.STUser;
 import com.lotus.sametime.lookup.LookupService;
 import com.lotus.sametime.lookup.ResolveEvent;
 import com.lotus.sametime.lookup.ResolveListener;
 import com.lotus.sametime.lookup.Resolver;
 
 /**
  * The Sametime IM Message Target Converter resolves String representations of userIds to Sametime addresses, using the lookup service.
  * @author Jamie Burrell
  * @since 18 Jan 2008
  * @version 1.0
  */
 public class SametimeIMMessageTargetConverter implements IMMessageTargetConverter
 {
     private final LookupService lookupService;
     private final Resolver resolver;
     private static final Logger log = Logger.getLogger(SametimeIMMessageTargetConverter.class.getName());
 
     private final CyclicBarrier listenBarrier;
 
    private final Map<String, SametimeIMMessageTarget> resolutionMap;
     private final ResolveListenerImpl resolveListener;
 
     /**
      * Constructor.
      * @param stsession The Sametime session
      */
     public SametimeIMMessageTargetConverter(STSession stsession)
     {
         // Get a handle to the Lookup Service and add a resolve listener
         log.info("Registering for Lookup Service.");
         lookupService = (LookupService) stsession.getCompApi(LookupService.COMP_NAME);
         resolver = lookupService.createResolver(true, false, true, false);
         resolveListener = new ResolveListenerImpl();
         resolver.addResolveListener(resolveListener);
 
         listenBarrier = new CyclicBarrier(2);
        resolutionMap = new HashMap<String, SametimeIMMessageTarget>();
     }
 
     /* (non-Javadoc)
      * @see hudson.plugins.sametime.im.IMMessageTargetConverter#fromString(java.lang.String)
      */
     public IMMessageTarget fromString(final String targetAsString) throws IMMessageTargetConversionException
     {
         if(StringUtils.isEmpty(targetAsString))
             return null;
 
         listenBarrier.reset();
 
         resolutionMap.put(targetAsString, null);
         resolveListener.setTarget(targetAsString);
         resolver.resolve(targetAsString);
 
         try
         {
             listenBarrier.await(10, TimeUnit.SECONDS);
         }
         catch (InterruptedException e)
         {
         	log.log(Level.SEVERE, "InterruptedException caught!", e);
         }
         catch (BrokenBarrierException e)
         {
         	log.log(Level.SEVERE, "BrokenBarrierException caught!", e);
         }
         catch (TimeoutException e)
         {
         	log.log(Level.SEVERE, "TimeoutException caught!", e);
         }
 
         return resolutionMap.get(targetAsString);
     }
 
     /* (non-Javadoc)
      * @see hudson.plugins.sametime.im.IMMessageTargetConverter#toString(hudson.plugins.sametime.im.IMMessageTarget)
      */
     public String toString(final IMMessageTarget target)
     {
         Assert.isNotNull(target, "Parameter 'target' must not be null.");
         return target.toString();
     }
 
     /**
      * A subscriber that listens for the results of ID resolution operations.
      * @author Jamie Burrell
      * @since 16 Jan 2008
      * @version 1.0
      */
     private class ResolveListenerImpl implements ResolveListener
     {
         private String target;
 
         /**
          * Getter method for the target field.
          *
          * @return The target property.
          * @see #setTarget(String)
          */
         public String getTarget()
         {
             return target;
         }
 
         /**
          * Setter method for the target field.
          *
          * @param target The new value of the target property.
          * @see #getTarget()
          */
         public void setTarget(String target)
         {
             this.target = target;
         }
 
         /* (non-Javadoc)
          * @see com.lotus.sametime.lookup.ResolveListener#resolveConflict(com.lotus.sametime.lookup.ResolveEvent)
          */
         public void resolveConflict(ResolveEvent arg0)
         {
             log.info("Resolution of " + target + " caused a conflict.");
 
             try
             {
                 // now wait at the barrier to say we've finished
                 listenBarrier.await(10, TimeUnit.SECONDS);
             }
             catch (InterruptedException e)
             {
             	log.log(Level.SEVERE, "InterruptedException caught!", e);
             }
             catch (BrokenBarrierException e)
             {
             	log.log(Level.SEVERE, "BrokenBarrierException caught!", e);
             }
             catch (TimeoutException e)
             {
             	log.log(Level.SEVERE, "TimeoutException caught!", e);
             }
         }
 
         /* (non-Javadoc)
          * @see com.lotus.sametime.lookup.ResolveListener#resolveFailed(com.lotus.sametime.lookup.ResolveEvent)
          */
         public void resolveFailed(ResolveEvent re)
         {
             log.info("Resolution of " + target + " failed.");
 
             try
             {
                 // now wait at the barrier to say we've finished
                 listenBarrier.await(10, TimeUnit.SECONDS);
             }
             catch (InterruptedException e)
             {
             	log.log(Level.SEVERE, "InterruptedException caught!", e);
             }
             catch (BrokenBarrierException e)
             {
             	log.log(Level.SEVERE, "BrokenBarrierException caught!", e);
             }
             catch (TimeoutException e)
             {
             	log.log(Level.SEVERE, "TimeoutException caught!", e);
             }
         }
 
         /* (non-Javadoc)
          * @see com.lotus.sametime.lookup.ResolveListener#resolved(com.lotus.sametime.lookup.ResolveEvent)
          */
         public void resolved(ResolveEvent re)
         {
             // we've managed to look up the supplied user in the directory, and now have an object for them
             if (re.getResolved() instanceof STUser)
             {
                 STUser user = (STUser) re.getResolved();
                 String userName = user.getName();
                 log.info("Resolved to '" + userName + "'.");
 
                 // create our representation of them as a target
                 SametimeIMMessageTarget imTarget = new SametimeIMMessageTarget(user, target);
                 resolutionMap.put(target, imTarget);
             }
             try
             {
                 // now wait at the barrier to say we've finished
                 listenBarrier.await(10, TimeUnit.SECONDS);
             }
             catch (InterruptedException e)
             {
             	log.log(Level.SEVERE, "InterruptedException caught!", e);
             }
             catch (BrokenBarrierException e)
             {
             	log.log(Level.SEVERE, "BrokenBarrierException caught!", e);
             }
             catch (TimeoutException e)
             {
             	log.log(Level.SEVERE, "TimeoutException caught!", e);
             }
         }
     }
 
 }
