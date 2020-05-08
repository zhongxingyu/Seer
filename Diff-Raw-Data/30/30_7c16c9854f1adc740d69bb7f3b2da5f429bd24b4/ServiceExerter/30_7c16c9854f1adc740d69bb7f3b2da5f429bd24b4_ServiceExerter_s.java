 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2010 SorcerSoft.org.
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
 
 package sorcer.util;
 
 import java.rmi.RemoteException;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.logging.Logger;
 
 import net.jini.core.lookup.ServiceID;
 import net.jini.core.transaction.Transaction;
 import net.jini.core.transaction.TransactionException;
 
 import org.dancres.blitz.jini.lockmgr.LockResult;
 import org.dancres.blitz.jini.lockmgr.MutualExclusion;
 
 import sorcer.core.context.ControlContext;
 import sorcer.core.context.ThrowableTrace;
 import sorcer.core.dispatch.ExertionSorter;
 import sorcer.core.dispatch.SortingException;
 import sorcer.core.provider.Provider;
 import sorcer.core.SorcerConstants;
 import sorcer.core.dispatch.ProvisionManager;
 import sorcer.core.exertion.NetJob;
 import sorcer.core.provider.ControlFlowManager;
 import sorcer.core.signature.NetSignature;
 import sorcer.core.signature.ServiceSignature;
 import sorcer.ext.Provisioner;
 import sorcer.ext.ProvisioningException;
 import sorcer.jini.lookup.ProviderID;
 import sorcer.service.Accessor;
 import sorcer.service.Arg;
 import sorcer.service.Context;
 import sorcer.service.ContextException;
 import sorcer.service.EvaluationException;
 import sorcer.service.ExecState;
 import sorcer.service.Exerter;
 import sorcer.service.Exertion;
 import sorcer.service.ExertionException;
 import sorcer.service.Job;
 import sorcer.core.provider.Jobber;
 import sorcer.service.Service;
 import sorcer.service.ServiceExertion;
 import sorcer.service.Setter;
 import sorcer.service.Signature;
 import sorcer.service.SignatureException;
 import sorcer.core.provider.Spacer;
 import sorcer.service.Strategy.Access;
 import sorcer.service.Task;
 import sorcer.service.txmgr.TransactionManagerAccessor;
 
 /**
  * @author Mike Sobolewski
  */
 @SuppressWarnings("rawtypes")
 public class ServiceExerter implements Exerter, Callable {
     protected final static Logger logger = Logger.getLogger(ServiceExerter.class
             .getName());
 
     private ServiceExertion exertion;
     private Transaction transaction;
     private static MutualExclusion locker;
     private ProvisionManager provisionManager;
 
     public ServiceExerter() {
     }
 
     public ServiceExerter(Exertion xrt) {
         exertion = (ServiceExertion) xrt;
     }
 
     public ServiceExerter(Exertion xrt, Transaction txn) {
         exertion = (ServiceExertion) xrt;
         transaction = txn;
 
     }
 
     public Exertion exert(Arg... entries) throws TransactionException,
             ExertionException, RemoteException {
         return exert((Transaction) null, (String) null, entries);
     }
 
     /* (non-Javadoc)
      * @see sorcer.service.Exerter#exert(sorcer.service.Exertion, sorcer.service.Parameter[])
      */
     @Override
     public Exertion exert(Exertion xrt, Arg... entries)
             throws TransactionException, ExertionException, RemoteException {
         try {
             xrt.substitute(entries);
         } catch (EvaluationException e) {
             throw new ExertionException(e);
         }
         return exert(xrt, (Transaction) null, (String) null);
     }
 
     /* (non-Javadoc)
      * @see sorcer.service.Exerter#exert(sorcer.service.Exertion, net.jini.core.transaction.Transaction, sorcer.service.Parameter[])
      */
     @Override
     public Exertion exert(Exertion xrt, Transaction txn, Arg... entries)
             throws TransactionException, ExertionException, RemoteException {
         try {
             xrt.substitute(entries);
         } catch (EvaluationException e) {
             throw new ExertionException(e);
         }
         transaction = txn;
         return exert(xrt, txn, (String) null);
     }
 
     public Exertion exert(String providerName) throws TransactionException,
             ExertionException, RemoteException {
         return exert(null, providerName);
     }
 
     public Exertion exert(Exertion xrt, Transaction txn, String providerName)
             throws TransactionException, ExertionException, RemoteException {
         this.exertion = (ServiceExertion) xrt;
         transaction = txn;
         return exert(txn, providerName);
     }
 
 
     public Exertion exert(Transaction txn, String providerName, Arg... entries)
             throws TransactionException, ExertionException, RemoteException {
         try {
             return postProcessExertion(exert0(txn, providerName, entries));
         } catch (ContextException e) {
             throw new ExertionException(e);
         }
     }
 
     public Exertion exert0(Transaction txn, String providerName, Arg... entries)
             throws TransactionException, ExertionException, RemoteException {
         try {
             if (entries != null && entries.length > 0) {
                 exertion.substitute(entries);
             }
         } catch (Exception ex) {
             ex.printStackTrace();
             throw new ExertionException(ex);
         }
         if (exertion instanceof Job && ((Job) exertion).size() == 1) {
             return processAsTask();
         }
         transaction = txn;
         Context<?> cxt = exertion.getDataContext();
         cxt.setExertion(exertion);
         Signature signature = exertion.getProcessSignature();
         Service provider = null;
         try {
             // If the exertion is a job rearrange the inner exertions to make sure the
             // dependencies are not broken
          /*  if (exertion.isJob()) {
                 ExertionSorter es = new ExertionSorter(exertion);
                 exertion = (ServiceExertion)es.getSortedJob();
            }*/
             //
             if (!(signature instanceof NetSignature)) {
                 if (exertion instanceof Task) {
                     if (exertion.getSignatures().size() == 1) {
                         return ((Task) exertion).doTask(txn);
                     } else {
                         try {
                             return new ControlFlowManager().doTask((Task)exertion);
                         } catch (ContextException e) {
                             e.printStackTrace();
                             throw new ExertionException(e);
                         }
                     }
                 }
                 else if (!(exertion instanceof NetJob))
                     return ((Job) exertion).doJob(txn);
             }
             // check for missing signature of inconsistent PULL/PUSH cases
             signature = correctProcessSignature();
             if (!((ServiceSignature) signature).isSelectable()) {
                 exertion.reportException(new ExertionException(
                         "No such operation in the requested signature: "
                                 + signature));
                 logger.warning("Not selectable exertion operation: " + signature);
                 return exertion;
             }
 
             if (providerName != null && providerName.length() > 0) {
                 signature.setProviderName(providerName);
             }
             logger.finer("* Exertion shell's servicer accessor: "
                     + Accessor.getAccessorType());
             provider = ((NetSignature) signature).getService();
         } catch (SignatureException e) {
             e.printStackTrace();
             throw new ExertionException(e);
        } /*catch (SortingException se) {
             throw new ExertionException(se);
        }   */
 
         if (provider == null) {
             // handle signatures for PULL tasks
             if (!exertion.isJob()
                     && exertion.getControlContext().getAccessType() == Access.PULL) {
                 signature = new NetSignature("service", Spacer.class, Sorcer.getActualSpacerName());
             }
             provider = (Service) Accessor.getService(signature);
             if (provider == null && exertion.isProvisionable() && signature instanceof NetSignature) {
                 try {
                     Provisioner provisioner = Accessor.getService(Provisioner.class);
                     if (provisioner != null) {
                         logger.fine("Provisioning "+signature);
                         provider = provisioner.provision(signature.getServiceType().getName(), signature.getName(), ((NetSignature) signature).getVersion());
                     }
                 } catch (ProvisioningException pe) {
                     logger.warning("* Provider not available and not provisioned: " + pe.getMessage());
                     exertion.setStatus(ExecState.FAILED);
                     exertion.reportException(new RuntimeException(
                             "Cannot find provider and provisioning returned error: " + pe.getMessage()));
                     return exertion;
                 }
             }
         }
         // Provider tasker = ProviderLookup.getProvider(exertion.getProcessSignature());
         // provider = ProviderAccessor.getProvider(null, signature
         // .getServiceType());
         if (provider == null) {
             logger.warning("* Provider not available for: " + signature);
             exertion.setStatus(ExecState.FAILED);
             exertion.reportException(new RuntimeException(
                     "Cannot find provider for: " + signature));
             return exertion;
         }
         exertion.getControlContext().appendTrace(
                 "bootstrapping: " + ((Provider) provider).getProviderName()
                         + ":" + ((Provider) provider).getProviderID());
         ((NetSignature) signature).setProvider(provider);
         logger.info("Provider found for: " + signature + "\n\t" + provider);
         if (((Provider) provider).mutualExclusion()) {
             return serviceMutualExclusion((Provider) provider, exertion,
                     transaction);
         } else {
             // test exertion for serialization
 //			 try {
 //				 logger.info("ServiceExerter.exert0(): going to serialize exertion for testing!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 //				 ObjectLogger.persistMarshalled("exertionfile", exertion);
 //			 } catch (Exception e) {
 //				 e.printStackTrace();
 //			 }
             Exertion result = null;
             try {
                 result = provider.service(exertion, transaction);
             } finally {
                 /*if (provisionManager != null) {
                     provisionManager.undeploy();
                 } */
             }
             if (result != null && result.getExceptions().size() > 0) {
                 for (ThrowableTrace et : result.getExceptions()) {
                     if (et.getThrowable() instanceof Error)
                         ((ServiceExertion) result).setStatus(ExecState.ERROR);
                 }
                 ((ServiceExertion)result).setStatus(ExecState.FAILED);
             }
             return result;
         }
     }
 
     private Exertion processAsTask() throws RemoteException,
             TransactionException, ExertionException {
         Task task = (Task) exertion.getExertions().get(0);
         task = (Task) task.exert();
         exertion.getExertions().set(0, task);
         exertion.setStatus(task.getStatus());
         return exertion;
     }
 
     private Exertion serviceMutualExclusion(Provider provider,
                                             Exertion exertion, Transaction transaction) throws RemoteException,
             TransactionException, ExertionException {
         ServiceID mutexId = provider.getProviderID();
         if (locker == null) {
 			locker = Accessor.getService(MutualExclusion.class);
         }
 		TransactionManagerAccessor.getTransactionManager();
         Transaction txn = null;
 
         LockResult lr = locker.getLock(""
                 + exertion.getProcessSignature().getServiceType(),
                 new ProviderID(mutexId), txn,
                 ((ServiceExertion) exertion).getId());
         if (lr.didSucceed()) {
             ((ControlContext)exertion.getControlContext()).setMutexId(provider.getProviderID());
             Exertion xrt = provider.service(exertion, transaction);
             txn.commit();
             return xrt;
         } else {
             // try continue to get lock, if failed abort the transaction txn
             txn.abort();
         }
         ((ControlContext)exertion.getControlContext()).addException(
                 new ExertionException("no lock avaialable for: "
                         + provider.getProviderName() + ":"
                         + provider.getProviderID()));
         return exertion;
     }
 
     /**
      * Depending on provider access type correct inconsistent signatures for
      * composite exertions only. Tasks go either to its provider directly or
      * Spacer depending on their provider access type (PUSH or PULL).
      *
      * @return the corrected signature
      */
     public Signature correctProcessSignature() {
         if (!exertion.isJob())
             return exertion.getProcessSignature();
         Signature sig = exertion.getProcessSignature();
         if (sig != null) {
             Access access = exertion.getControlContext().getAccessType();
            if ((Access.PULL == access || Access.QOS_PULL == access)
                     && !exertion.getProcessSignature().getServiceType()
                     .isAssignableFrom(Spacer.class)) {
                 sig.setServiceType(Spacer.class);
                 ((NetSignature) sig).setSelector("service");
                 sig.setProviderName(SorcerConstants.ANY);
                 sig.setType(Signature.Type.SRV);
                 exertion.getControlContext().setAccessType(access);
            } else if ((Access.PUSH == access || Access.QOS_PUSH == access)
                     && !exertion.getProcessSignature().getServiceType()
                     .isAssignableFrom(Jobber.class)) {
                 if (sig.getServiceType().isAssignableFrom(Spacer.class)) {
                     sig.setServiceType(Jobber.class);
                     ((NetSignature) sig).setSelector("service");
                     sig.setProviderName(SorcerConstants.ANY);
                     sig.setType(Signature.Type.SRV);
                     exertion.getControlContext().setAccessType(access);
                 }
             }
         } else {
             sig = new NetSignature("service", Jobber.class);
         }
         return sig;
     }
 
     public static Exertion postProcessExertion(Exertion exertion)
             throws ContextException, RemoteException {
         List<Exertion> exertions = exertion.getAllExertions();
         for (Exertion xrt : exertions) {
             List<Setter> ps = ((ServiceExertion) xrt).getPersisters();
             if (ps != null) {
 /*
                 for (Setter p : ps) {
                     if (p != null && (p instanceof Par) && ((Par) p).isMappable()) {
                         String from = (String) ((Par) p).getName();
                         Object obj = null;
                         if (xrt instanceof Job)
                             obj = ((Job) xrt).getJobContext().getValue(from);
                         else {
                             obj = xrt.getContext().getValue(from);
                         }
 
                         if (obj != null)
                             p.setValue(obj);
                     }
                 }
 */
             }
         }
         return exertion;
     }
 
     public Transaction getTransaction() {
         return transaction;
     }
 
     public void setTransaction(Transaction transaction) {
         this.transaction = transaction;
     }
 
     @Override
     public String toString() {
         if (exertion == null)
             return "ExertionShell";
         else
             return "ExertionShell for: " + exertion.getName();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see java.util.concurrent.Callable#call()
      */
     @Override
     public Object call() throws Exception {
         return exertion.exert(transaction);
     }
 
 }
