 /*
  * Created on 25 Jul 2006
  */
 package uk.org.ponder.rsf.state.guards;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Validator;
 
 import uk.org.ponder.beanutil.BeanLocator;
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.PathUtil;
 import uk.org.ponder.mapping.BeanInvalidationModel;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 import uk.org.ponder.springutil.errors.SpringErrorConverter;
 import uk.org.ponder.util.CollectingRunnableInvoker;
 import uk.org.ponder.util.RunnableInvoker;
 
 /**
  * Collects all BeanGuard definitions from the context, and supervises their
  * application.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 // TODO: This implementation currently scales very badly - cost of every
 // bean model write is proportional to number of guards.
 public class BeanGuardProcessor implements ApplicationContextAware {
 
   private BeanGuard[] guards;
   private BeanModelAlterer darapplier;
 
   public void setApplicationContext(ApplicationContext applicationContext) {
     String[] guardnames = applicationContext.getBeanNamesForType(
         BeanGuard.class, false, false);
     guards = new BeanGuard[guardnames.length];
     for (int i = 0; i < guardnames.length; ++i) {
       guards[i] = (BeanGuard) applicationContext.getBean(guardnames[i]);
     }
   }
 
   public void setBeanModelAlterer(BeanModelAlterer darapplier) {
     this.darapplier = darapplier;
   }
 
   public RunnableInvoker getGuardProcessor(final BeanInvalidationModel bim,
       final TargettedMessageList errors, final BeanLocator rbl) {
     return new RunnableInvoker() {
       public void invokeRunnable(Runnable torun) {
         processGuards(bim, errors, rbl, torun);
       }
     };
   }
 
   public void processPostGuards(BeanInvalidationModel bim,
       TargettedMessageList errors, BeanLocator rbl) {
     processGuards(bim, errors, rbl, null);
   }
 
   private List appendWrapper(List toappend, RunnableInvoker invoker) {
     if (toappend == null) {
       toappend = new ArrayList();
     }
     toappend.add(invoker);
     return toappend;
   }
   
   private void processGuards(BeanInvalidationModel bim,
       TargettedMessageList errors, BeanLocator rbl, Runnable toinvoke) {
     BindException springerrors = null;
     List wrappers = null;
     for (int i = 0; i < guards.length; ++i) {
       BeanGuard guarddef = guards[i];
       String mode = guarddef.getGuardMode();
       String timing = guarddef.getGuardTiming();
       String guardedpath = guarddef.getGuardedPath();
       String guardmethod = guarddef.getGuardMethod();
       String guardEL = guarddef.getGuardEL();
       String guardproperty = guarddef.getGuardProperty();
       if (guardEL != null && guardmethod != null) {
         guardmethod = PathUtil.composePath(guardEL, guardmethod);
       }
       if (mode.equals(BeanGuard.WRITE) && timing == null
           || timing.equals(BeanGuard.POST)) {
         // for each POST-WRITE guard for an invalidated path, execute it.
         String match = bim.invalidPathMatch(guardedpath);
         if (match != null) {
           Object guard = guarddef.getGuard();
           if (guard == null && guardEL == null) {
             if (guardmethod != null) {
               guardEL = PathUtil.getToTailPath(guardmethod);
               guardmethod = PathUtil.getTailPath(guardmethod);
             }
             else if (guardproperty != null) {
               guardEL = PathUtil.getToTailPath(guardproperty);
               guardproperty = PathUtil.getTailPath(guardproperty);
             }
           }
           if (guardEL != null) {
             guard = darapplier.getBeanValue(guardEL, rbl);
           }
           Object guarded = darapplier.getBeanValue(match, rbl);
           try {
             if (guard instanceof RunnableInvoker) {
               if (toinvoke == null) {
                 throw new IllegalArgumentException(
                     "Configuration error: Bean Guard "
                         + guard
                         + " at "
                         + guardEL
                         + " was required in AROUND mode but does not implement RunnableInvoker");
               }
               else {
                 wrappers = appendWrapper(wrappers, (RunnableInvoker) guard);
               }
             }
             else {
               // now invoking postguards
               if (toinvoke == null) {
                 if (guardmethod != null) {
                   darapplier.invokeBeanMethod(guardmethod, guard);
                 }
                 else if (guardproperty != null) {
                   darapplier
                       .setBeanValue(guardproperty, guard, guarded, errors, false);
                 }
                 else if (guard instanceof Validator) {
                   if (guarded == null) {
                     throw new IllegalArgumentException(
                         "Error: Spring Validator may not be used to validate a null object");
                   }
                   Validator guardv = (Validator) guard;
                   // NB, a Spring validator may not be applied to a null object!
                   springerrors = new BindException(guarded, guardedpath);
                   guardv.validate(guarded, springerrors);
                  SpringErrorConverter.appendErrors(guardedpath, errors, springerrors);
                 }
               }
             }
           }
           catch (Exception e) {
             TargettedMessage message = new TargettedMessage(e.getMessage(), e,
                 match);
             errors.addMessage(message);
           }
         }
       }
     }
     if (toinvoke != null) {
       CollectingRunnableInvoker.invokeWrappers(wrappers, toinvoke);
     }
     // if (springerrors != null) {
     // throw UniversalRuntimeException.accumulate(springerrors);
     // }
   }
 }
