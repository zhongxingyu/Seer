 /*
  * Created on 25 Jul 2006
  */
 package uk.org.ponder.rsf.state.guards;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Validator;
 
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.PathUtil;
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.errorutil.TargettedMessage;
 import uk.org.ponder.errorutil.TargettedMessageList;
 import uk.org.ponder.mapping.BeanInvalidationModel;
 import uk.org.ponder.springutil.errors.SpringErrorConverter;
 
 public class BeanGuardProcessor implements ApplicationContextAware {
 
   private BeanGuard[] guards;
   private BeanModelAlterer darapplier;
   
   public void setApplicationContext(ApplicationContext applicationContext) {
     String[] guardnames = applicationContext.getBeanNamesForType(BeanGuard.class, false, false);
     guards = new BeanGuard[guardnames.length];
     for (int i = 0; i < guardnames.length; ++ i) {
       guards[i] = (BeanGuard) applicationContext.getBean(guardnames[i]);
     }  
   }
   
   public void setBeanModelAlterer(BeanModelAlterer darapplier) {
     this.darapplier = darapplier;
   }
   
   public void processPostGuards(BeanInvalidationModel bim, TargettedMessageList errors, 
       WriteableBeanLocator rbl) {
     BindException springerrors = null;
     for (int i = 0; i < guards.length; ++ i) {
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
       if (mode.equals(BeanGuard.WRITE) && 
           timing == null || timing.equals(BeanGuard.POST)) {
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
             if (guardmethod != null) {
              darapplier.invokeBeanMethod(guardmethod, guard);
             }
             else if (guardproperty != null) {
               darapplier.setBeanValue(guardproperty, guard, guarded, errors);
             }
             else if (guard instanceof Validator) {
               Validator guardv = (Validator) guard;
               springerrors = new BindException(guarded, guardedpath);
               // TODO: We could try to store this excess info somewhere.
               guardv.validate(guarded, springerrors);
               SpringErrorConverter.appendErrors(errors, springerrors);
             }
           }
           catch (Exception e) {
             TargettedMessage message = new TargettedMessage(e.getMessage(), e, match);
             errors.addMessage(message);
           }
         }
       }
     }
 //    if (springerrors != null) {
 //      throw UniversalRuntimeException.accumulate(springerrors);
 //    }
   }
 
   
 }
