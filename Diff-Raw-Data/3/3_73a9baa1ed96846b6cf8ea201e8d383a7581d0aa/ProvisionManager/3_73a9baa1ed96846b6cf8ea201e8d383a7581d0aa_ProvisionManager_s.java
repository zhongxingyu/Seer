 /*
  * Copyright 2012 the original author or authors.
  * Copyright 2012 SorcerSoft.org.
  *  
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core.dispatch;
 
 import net.jini.core.lease.Lease;
 import net.jini.space.JavaSpace05;
 import sorcer.core.exertion.ExertionEnvelop;
 import sorcer.core.provider.Spacer;
 import sorcer.core.signature.NetSignature;
 import sorcer.ext.Provisioner;
 import sorcer.ext.ProvisioningException;
 import sorcer.service.*;
 import sorcer.service.space.SpaceAccessor;
 
 import java.rmi.RemoteException;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * @author Pawel Rubach
  */
 public class ProvisionManager {
 	private static final Logger logger = Logger.getLogger(ProvisionManager.class.getName());
 	protected final Set<SignatureElement> servicesToProvision = new LinkedHashSet<SignatureElement>();
     private static ProvisionManager instance = null;
     private static final int MAX_ATTEMPTS = 2;
 
 
     public static ProvisionManager getInstance() {
         if (instance==null)
             instance = new ProvisionManager();
         return instance;
     }
 
 	
 	protected ProvisionManager() {
         ThreadGroup provGroup = new ThreadGroup("spacer-provisioning");
         provGroup.setDaemon(true);
         provGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);
         Thread pThread = new Thread(provGroup, new ProvisionThread(), "Provisioner");
         pThread.start();
 	}
 
     public void add(Exertion exertion, SpaceExertDispatcher spaceExertDispatcher) {
         NetSignature sig = (NetSignature) exertion.getProcessSignature();
         Service service = (Service) Accessor.getService(sig);
         // A hack to disable provisioning spacer itself
         if (service==null && !sig.getServiceType().getName().equals(Spacer.class.getName())) {
             synchronized (servicesToProvision) {
                 servicesToProvision.add(
                         new SignatureElement(sig.getServiceType().getName(), sig.getProviderName(),
                                 sig.getVersion(), sig, exertion, spaceExertDispatcher));
             }
         }
     }
 
 
 
     protected class ProvisionThread implements Runnable {
 
         public void run() {
             Provisioner provisioner = Accessor.getService(Provisioner.class);
             while (true) {
                 if (!servicesToProvision.isEmpty()) {
                     LinkedHashSet<SignatureElement> copy ;
                     synchronized (servicesToProvision){
                         copy = new LinkedHashSet<SignatureElement>(servicesToProvision);
                     }
                     Iterator<SignatureElement> it = copy.iterator();
                     Set<SignatureElement> sigsToRemove = new LinkedHashSet<SignatureElement>();
                     logger.fine("Services to provision from Spacer/Jobber: "+ servicesToProvision.size());
 
                     while (it.hasNext()) {
                         SignatureElement sigEl = it.next();
 
                         // Catalog lookup or use Lookup Service for the particular
                         // service
                         Service service = (Service) Accessor.getService(sigEl.getSignature());
                         if (service == null ) {
                             if (provisioner != null) {
                                 try {
                                     logger.info("Provisioning: "+ sigEl.getSignature());
                                    sigEl.incrementProvisionAttempts();
                                     service = provisioner.provision(sigEl.getServiceType(), sigEl.getProviderName(), sigEl.getVersion());
                                     if (service!=null) sigsToRemove.add(sigEl);
                                 } catch (ProvisioningException pe) {
                                     logger.severe("Problem provisioning: " +pe.getMessage());
                                 } catch (RemoteException re) {
                                     provisioner = Accessor.getService(Provisioner.class);
                                     String msg = "Problem provisioning "+sigEl.getSignature().getServiceType()
                                             + " (" + sigEl.getSignature().getProviderName() + ")"
                                             + " " +re.getMessage();
                                     logger.severe(msg);
                                 }
                             } else
                                 provisioner = Accessor.getService(Provisioner.class);
 
                             if (service == null && sigEl.getProvisionAttempts() > MAX_ATTEMPTS) {
                                 String logMsg = "Provisioning for " + sigEl.getServiceType() + "(" + sigEl.getProviderName()
                                         + ") tried: " + sigEl.getProvisionAttempts() +" times, provisioning will not be reattempted";
                                 logger.severe(logMsg);
                                 try {
                                     failExertionInSpace(sigEl, new ProvisioningException(logMsg));
                                     sigsToRemove.add(sigEl);
                                 } catch (ExertionException ile) {
                                     logger.severe("Problem trying to remove exception after reattempting to provision");
                                 }
                             }
                         } else
                             sigsToRemove.add(sigEl);
                     }
                     if (!sigsToRemove.isEmpty()) {
                         synchronized (servicesToProvision) {
                             servicesToProvision.removeAll(sigsToRemove);
                         }
                     }
                 }
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                 }
             }
         }
     }
 
 
     private void failExertionInSpace(SignatureElement sigEl, Exception exc) throws ExertionException {
         logger.info("Setting Failed state for service type: " + sigEl.getServiceType() + " exertion ID: " +
                 "" + sigEl.getExertion().getId());
         ExertionEnvelop ee = ExertionEnvelop.getTemplate(sigEl.getExertion());
 
         ExertionEnvelop result = null;
         result = sigEl.getSpaceExertDispatcher().takeEnvelop(ee);
         if (result!=null) {
             result.state = ExecState.FAILED;
             ((ServiceExertion)result.exertion).setStatus(ExecState.FAILED);
             ((ServiceExertion)result.exertion).reportException(exc);
             try {
 
                 JavaSpace05 space = SpaceAccessor.getSpace();
                 if (space == null) {
                     throw new ExertionException("NO exertion space available!");
                 }
                 space.write(result, null, Lease.FOREVER);
                 logger.finer("===========================> written failure envelop: "
                         + ee.describe() + "\n to: " + space);
             } catch (Exception e) {
                 e.printStackTrace();
                 logger.throwing(this.getClass().getName(), "faileExertionInSpace", e);
                 throw new ExertionException("Problem writing exertion back to space");
             }
         }
     }
 
     private class SignatureElement {
         String serviceType;
         String providerName;
         String version;
         Signature signature;
         int provisionAttempts=0;
         Exertion exertion;
         SpaceExertDispatcher spaceExertDispatcher;
 
         private String getServiceType() {
             return serviceType;
         }
 
         private void setServiceType(String serviceType) {
             this.serviceType = serviceType;
         }
 
         private String getProviderName() {
             return providerName;
         }
 
         private void setProviderName(String providerName) {
             this.providerName = providerName;
         }
 
         private String getVersion() {
             return version;
         }
 
         private void setVersion(String version) {
             this.version = version;
         }
 
         private Signature getSignature() {
             return signature;
         }
 
         private void setSignature(Signature signature) {
             this.signature = signature;
         }
 
 
         public int getProvisionAttempts() {
             return provisionAttempts;
         }
 
         public void incrementProvisionAttempts() {
             this.provisionAttempts++;
         }
 
         public Exertion getExertion() {
             return exertion;
         }
 
         public SpaceExertDispatcher getSpaceExertDispatcher() {
             return spaceExertDispatcher;
         }
 
         private SignatureElement(String serviceType, String providerName, String version, Signature signature,
                                  Exertion exertion, SpaceExertDispatcher spaceExertDispatcher) {
             this.serviceType = serviceType;
             this.providerName = providerName;
             this.version = version;
             this.signature = signature;
             this.exertion = exertion;
             this.spaceExertDispatcher = spaceExertDispatcher;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             SignatureElement that = (SignatureElement) o;
             if (!providerName.equals(that.providerName)) return false;
             if (!serviceType.equals(that.serviceType)) return false;
             if (!exertion.equals(that.exertion)) return false;
             if (version != null ? !version.equals(that.version) : that.version != null) return false;
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = serviceType.hashCode();
             result = 31 * result + providerName.hashCode();
             result = 31 * result + (version != null ? version.hashCode() : 0);
             return result;
         }
     }
 
 }
