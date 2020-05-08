 package org.glite.authz.pap.repository.dao;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.glite.authz.pap.common.xacml.TypeStringUtils;
 import org.glite.authz.pap.common.xacml.utils.PolicySetHelper;
 import org.glite.authz.pap.common.xacml.wizard.PolicyWizard;
 import org.glite.authz.pap.distribution.DistributionModule;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.glite.authz.pap.repository.RepositoryManager;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.opensaml.xacml.XACMLObject;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ProvisioningServiceDAO {
 
     private static ProvisioningServiceDAO instance = null;
     private static final Logger log = LoggerFactory.getLogger(ProvisioningServiceDAO.class);
 
     public static ProvisioningServiceDAO getInstance() {
         if (instance == null)
             instance = new ProvisioningServiceDAO();
         return instance;
     }
 
     private final PAPManager papManager;
 
     private ProvisioningServiceDAO() {
         papManager = PAPManager.getInstance();
     }
 
     public List<XACMLObject> papQuery() {
 
         log.debug("Executing PAP query...");
 
         List<XACMLObject> resultList = new LinkedList<XACMLObject>();
 
         List<PAPContainer> papContainerList = new LinkedList<PAPContainer>();
         papContainerList.add(papManager.getDefaultPAPContainer());
 
         List<PAPContainer> publicPAPContainerList = papManager.getPublicRemotePAPsContainers();
 
         PolicySetType rootPolicySet = makeRootPolicySet();
         resultList.add(rootPolicySet);
 
         if (!(publicPAPContainerList.isEmpty())) {
             papContainerList.addAll(publicPAPContainerList);
         }
 
         for (PAPContainer papContainer : papContainerList) {
 
             List<XACMLObject> papXACMLObjectList = getPublic(papContainer);
             
             if (papXACMLObjectList.size() > 0) {
                 PolicySetType papRootPolicySet = (PolicySetType) papXACMLObjectList.get(0);
                 
                 List<String> policySetIdList = PolicySetHelper.getPolicySetIdReferencesValues(papRootPolicySet);
                 List<String> policyIdList = PolicySetHelper.getPolicyIdReferencesValues(papRootPolicySet);
                 
                 TypeStringUtils.releaseUnneededMemory(papRootPolicySet);
                 for (String id : policySetIdList) {
                     PolicySetHelper.addPolicySetReference(rootPolicySet, id);
                 }
                 for (String id : policyIdList) {
                     PolicySetHelper.addPolicySetReference(rootPolicySet, id);
                 }
                 
                 papXACMLObjectList.remove(papRootPolicySet);
                 
                 resultList.addAll(papXACMLObjectList);
             }
         }
 
         log.debug("PAP query executed: retrieved " + resultList.size() + " elements (Policy/PolicySet)");
 
         return resultList;
     }
 
     public List<XACMLObject> pdpQuery() {
 
         log.debug("Executing PDP query...");
 
         List<XACMLObject> resultList = new LinkedList<XACMLObject>();
 
         PolicySetType rootPolicySet = makeRootPolicySet();
 
         resultList.add(rootPolicySet);
 
         PAPContainer[] papContainerList = papManager.getOrderedPAPContainerArray();
 
         // Add references to the remote PAPs
         for (PAPContainer papContainer : papContainerList) {
             log.info("Adding PAP: " + papContainer.getPAP().getAlias());
 
             try {
                 PolicySetType papPolicySetNoReferences;
 
                 synchronized (DistributionModule.storePoliciesLock) {
                     papPolicySetNoReferences = getPolicySetNoReferences(papContainer, papContainer.getPAPRootPolicySetId());
                 }
 
                 PolicySetHelper.addPolicySet(rootPolicySet, papPolicySetNoReferences);
 
                 TypeStringUtils.releaseUnneededMemory(papPolicySetNoReferences);
 
             } catch (NotFoundException e) {
                 continue;
             }
         }
 
         TypeStringUtils.releaseUnneededMemory(rootPolicySet);
 
         log.debug("PDP query executed: retrieved " + resultList.size() + " elements (Policy/PolicySet)");
 
         return resultList;
     }
 
     private PolicySetType getPolicySetNoReferences(PAPContainer papContainer, String policySetId) {
 
         PolicySetType policySetNoRef = papContainer.getPolicySet(policySetId);
 
         // replace policy set references with policy sets
         List<String> idReferenceList = PolicySetHelper.getPolicySetIdReferencesValues(policySetNoRef);
         for (String childPolicySetId : idReferenceList) {
 
        	try {
             PolicySetType childPolicySetNoRef = getPolicySetNoReferences(papContainer, childPolicySetId);
 
             PolicySetHelper.addPolicySet(policySetNoRef, childPolicySetNoRef);
 
             TypeStringUtils.releaseUnneededMemory(childPolicySetNoRef);
        	} catch (NotFoundException e) {
        		// this exception might occur in case of concurrent remove/add policy operations
        		// nothing to do, go on and remove the reference
        	}
 
             PolicySetHelper.deletePolicySetReference(policySetNoRef, childPolicySetId);
         }
 
         // replace policy references with policies
         idReferenceList = PolicySetHelper.getPolicyIdReferencesValues(policySetNoRef);
         for (String policyIdReference : idReferenceList) {
 
        	try {
             PolicyType policy = papContainer.getPolicy(policyIdReference);
 
             PolicySetHelper.addPolicy(policySetNoRef, policy);
 
             TypeStringUtils.releaseUnneededMemory(policy);
        	} catch (NotFoundException e) {
        		// this exception might occur in case of concurrent remove/add policy operations
        		// nothing to do, go on and remove the reference
        	}
 
             PolicySetHelper.deletePolicyReference(policySetNoRef, policyIdReference);
         }
 
         return policySetNoRef;
     }
 
     private List<XACMLObject> getPublic(PAPContainer papContainer) {
 
         List<XACMLObject> resultList = new LinkedList<XACMLObject>();
 
         List<PolicySetType> resultPolicySetList = new LinkedList<PolicySetType>();
         
         for (PolicySetType policySet : papContainer.getAllPolicySets()) {
             resultPolicySetList.add(policySet);
         }
         
         List<PolicyType> policyList = papContainer.getAllPolicies();
         List<PolicyType> resultPolicyList = new LinkedList<PolicyType>();
 
         boolean removedAtLeastOnePrivatePolicy = false;
 
         // Remove all private policies from the list (together with its references in the policy sets)
         for (PolicyType policy : policyList) {
             String policyId = policy.getPolicyId();
 
             TypeStringUtils.releaseUnneededMemory(policy);
 
             if (PolicyWizard.isPublic(policyId)) {
                 resultPolicyList.add(policy);
                 continue;
             }
 
             // Remove references of the policy
             for (PolicySetType policySet : resultPolicySetList) {
                 if (PolicySetHelper.deletePolicyReference(policySet, policyId)) {
                     removedAtLeastOnePrivatePolicy = true;
                 }
             }
         }
 
         if (removedAtLeastOnePrivatePolicy) {
             for (PolicySetType policySet : resultPolicySetList) {
                 TypeStringUtils.releaseUnneededMemory(policySet);
             }
         }
 
         log.debug("Adding " + resultPolicySetList.size() + " PolicySet elements from PAP \"" + papContainer.getPAP().getPapId()
                 + "\"");
         resultList.addAll(resultPolicySetList);
 
         log.debug("Adding " + policyList.size() + " Policy elements from PAP \"" + papContainer.getPAP().getPapId() + "\"");
         resultList.addAll(resultPolicyList);
 
         return resultList;
     }
 
     private PolicySetType makeRootPolicySet() {
     	String rootPolicySetId = "root-" + PAPManager.getInstance().getDefaultPAP().getPapId();
         PolicySetType rootPolicySet = PolicySetHelper.buildWithAnyTarget(rootPolicySetId,
                                                                          PolicySetHelper.COMB_ALG_FIRST_APPLICABLE);
         rootPolicySet.setVersion(RepositoryManager.getVersion());
         return rootPolicySet;
     }
 }
