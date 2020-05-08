 package org.glite.authz.pap.distribution;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.rpc.ServiceException;
 
 import org.glite.authz.pap.common.Pap;
 import org.glite.authz.pap.common.xacml.impl.TypeStringUtils;
 import org.glite.authz.pap.papmanagement.PapContainer;
 import org.glite.authz.pap.papmanagement.PapManager;
 import org.opensaml.xacml.XACMLObject;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class implements the distribution module, that means running a thread which regularly polls
  * remote paps to fetch policies.
  * <p>
  * Start the thread that polls remote paps by calling the
  * {@link DistributionModule#startDistributionModule()} method and stop it by calling the
  * {@link DistributionModule#stopDistributionModule()} method.
  * <p>
  * The {@link DistributionModule#refreshCache(Pap)} method can be used to force a refresh of the
  * remote paps cache asynchronously.
  */
 public class DistributionModule extends Thread {
 
     /** lock used when the cache of a pap is refreshed. */
     public static final Object storePoliciesLock = new Object();
 
     private static final Logger log = LoggerFactory.getLogger(DistributionModule.class);
     private static DistributionModule instance = null;
 
     private long sleepTime;
 
     private boolean stayRunning = true;
 
     private DistributionModule() {
         initialize();
     }
 
     public static DistributionModule getInstance() {
         if (instance == null)
             instance = new DistributionModule();
         return instance;
     }
 
     /**
      * Refreshes the policies of a pap, that means fetching the policies and storing them in the
      * repository. If the given pap is not remote then nothing happens.
      * 
      * @param pap
      * 
      * @throws RemoteException
      * @throws ServiceException
      */
     public static void refreshCache(Pap pap) throws RemoteException, ServiceException {
         log.info("Refreshing cache of remote PAP " + pap.getAlias());
         List<XACMLObject> papPolicies = getPoliciesFromPap(pap);
         log.info(String.format("Retrieved %d XACML objects from PAP %s (%s)",
                                papPolicies.size(),
                                pap.getAlias(),
                                pap.getEndpoint()));
         storePapPolicies(pap, papPolicies);
     }
 
     /**
      * Fetches policies from a remote pap.
      * 
      * @param remotePap the remote pap to fetch policies from.
      * @return the list of policy sets and policies of the pap. The first element is the root policy
      *         set of the pap. Returns an empty list if the pap wasn't a remote pap.
      * 
      * @throws RemoteException
      * @throws ServiceException
      */
     private static List<XACMLObject> getPoliciesFromPap(Pap remotePap) throws RemoteException,
             ServiceException {
 
         if (!remotePap.isRemote()) {
             log.error("Attempting to fetch policies from the local pap: " + remotePap.getAlias());
             return new ArrayList<XACMLObject>(0);
         }
 
         PAPClient client = new PAPClient(remotePap.getEndpoint());
 
         List<XACMLObject> papPolicies = client.retrievePolicies();
 
         return papPolicies;
     }
 
     /**
      * Replace the policy cache of the given pap with the given policies.
      * 
      * @param pap
      * @param papPolicies
      */
     private static void storePapPolicies(Pap pap, List<XACMLObject> papPolicies) {
 
         if (papPolicies.isEmpty()) {
             return;
         }
 
         log.debug(String.format("Storing policies for PAP %s (id=%s)", pap.getAlias(), pap.getId()));
 
         PapManager papManager = PapManager.getInstance();
         PapContainer papContainer = papManager.getPapContainer(pap.getAlias());
 
         synchronized (storePoliciesLock) {
 
             papContainer.deleteAllPolicies();
             papContainer.deleteAllPolicySets();
 
             XACMLObject papRoot = papPolicies.get(0);
 
             if (papRoot instanceof PolicySetType) {
 
                 ((PolicySetType) papRoot).setPolicySetId(papContainer.getPap().getId());
 
                 for (XACMLObject xacmlObject : papPolicies) {
 
                     if (xacmlObject instanceof PolicySetType) {
                         PolicySetType policySet = (PolicySetType) xacmlObject;
                         papContainer.storePolicySet(policySet);
 
                         log.debug(String.format("Stored PolicySet \"%s\" into pap \"%s\"",
                                                 policySet.getPolicySetId(),
                                                 pap.getAlias()));
                     } else if (xacmlObject instanceof PolicyType) {
 
                         PolicyType policy = (PolicyType) xacmlObject;
                         papContainer.storePolicy(policy);
 
                         log.debug(String.format("Stored Policy \"%s\" into pap \"%s\"",
                                                 policy.getPolicyId(),
                                                 pap.getAlias()));
                     } else {
 
                         log.error(String.format("Invalid object (not a Policy or PolicySet) received from PAP %s (%s)",
                                                 pap.getAlias(),
                                                 pap.getEndpoint()));
                     }
                     TypeStringUtils.releaseUnneededMemory(xacmlObject);
                 }
             } else {
                 log.error(String.format("The root of the policy tree is not a PolicySet (papAlias=%s, endpoint=%s)",
                                         pap.getAlias(),
                                         pap.getEndpoint()));
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Thread#run()
      */
     public void run() {
 
         while (stayRunning) {
             try {
                 while (!this.isInterrupted()) {
 
                     log.info("Starting refreshing cache process...");
 
                     for (Pap pap : PapManager.getInstance().getRemotePaps()) {
 
                         if (this.isInterrupted())
                             break;
 
                         try {
                             refreshCache(pap);
                         } catch (RemoteException e) {
                             log.error(String.format("Error connecting to %s (%s): %s",
                                                     pap.getAlias(),
                                                     pap.getEndpoint(),
                                                     e.getMessage()));
                         } catch (ServiceException e) {
                             log.error(String.format("Cannot connect to: %s (%s)",
                                                     pap.getAlias(),
                                                     pap.getEndpoint(),
                                                     e.getMessage()));
                         }
                     }
 
                     log.info("Refreshing cache process has finished");
 
                     sleep(sleepTime);
                 }
             } catch (InterruptedException e) {}
         }
     }
 
    public void startDistributionModule() {
         this.start();
     }
 
     public void setSleepTime(long seconds) {
         sleepTime = seconds * 1000;
         this.interrupt();
     }
 
    public void stopDistributionModule() {
 
         stayRunning = false;
         
         this.interrupt();
 
         while (this.isAlive());
     }
 
     protected void initialize() {
         log.info("Initilizing distribution module...");
         sleepTime = DistributionConfiguration.getInstance().getPollIntervall() * 1000;
     }
 }
