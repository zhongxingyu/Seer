 package org.glite.authz.pap.services;
 
 import java.rmi.RemoteException;
 
 import org.glite.authz.pap.authz.policymanagement.GetPolicyOperation;
 import org.glite.authz.pap.authz.policymanagement.GetPolicySetOperation;
 import org.glite.authz.pap.authz.policymanagement.HasPolicyOperation;
 import org.glite.authz.pap.authz.policymanagement.HasPolicySetOperation;
 import org.glite.authz.pap.authz.policymanagement.ListPoliciesForPAPOperation;
 import org.glite.authz.pap.authz.policymanagement.ListPoliciesOperation;
 import org.glite.authz.pap.authz.policymanagement.ListPolicySetOperation;
 import org.glite.authz.pap.authz.policymanagement.ListPolicySetsForPAPOperation;
 import org.glite.authz.pap.authz.policymanagement.RemovePolicyOperation;
 import org.glite.authz.pap.authz.policymanagement.RemovePolicySetOperation;
 import org.glite.authz.pap.authz.policymanagement.StorePolicyOperation;
 import org.glite.authz.pap.authz.policymanagement.StorePolicySetOperation;
 import org.glite.authz.pap.authz.policymanagement.UpdatePolicyOperation;
 import org.glite.authz.pap.authz.policymanagement.UpdatePolicySetOperation;
 import org.glite.authz.pap.distribution.PAPManager;
 import org.glite.authz.pap.repository.PAPContainer;
 import org.glite.authz.pap.services.xacml_policy_management.axis_skeletons.XACMLPolicyManagement;
 import org.glite.authz.pap.ui.wizard.PolicyWizard;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class XACMLPolicyManagementService implements XACMLPolicyManagement {
 
 	private static final Logger log = LoggerFactory.getLogger(XACMLPolicyManagementService.class);
 
 	public String addPolicy(String policySetId, String policyIdPrefix, PolicyType policy)
 			throws RemoteException {
 		
 		log.info("addPolicy();");
 
 		try {
 			
 			String policyId = null;
 
 			String emptyString = "";
 
 			PAPContainer localPAP = PAPManager.getInstance().getLocalPAPContainer();
 
 			if (!localPAP.hasPolicySet(policySetId))
 				return emptyString;
 
 			policyId = PolicyWizard.generateId(policyIdPrefix);
 			policy.setPolicyId(policyId);
 
 			localPAP.addPolicy(policySetId, policy);
 
 			return policyId;
 			
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 
 	}
 
 	public PolicyType getPAPPolicy(String papAlias, String policyId) throws RemoteException {
 		log.info("getPAPPolicy(\"" + papAlias + "\", \"" + policyId + "\");");
 		
 		try {
 			PAPContainer pap = PAPManager.getInstance().getTrustedPAPContainer(papAlias);
 
 			PolicyType policy = pap.getPolicy(policyId);
 
 			return policy;
 			
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public PolicySetType getPAPPolicySet(String papAlias, String policySetId) throws RemoteException {
 		log.info("getPAPPolicySet(\"" + papAlias + "\", \"" + policySetId + "\");");
 
 		try {
 
 			PAPContainer pap = PAPManager.getInstance().getTrustedPAPContainer(papAlias);
 			PolicySetType policySet = pap.getPolicySet(policySetId);
 			return policySet;
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public PolicyType getPolicy(String policyId) throws RemoteException {
 		log.info("getPolicy(\"" + policyId + "\");");
 		
 		try {
 
 			PolicyType policy = GetPolicyOperation.instance(policyId).execute();
 			return policy;
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public PolicySetType getPolicySet(String policySetId) throws RemoteException {
 		log.info("getPolicySet(\"" + policySetId + "\");");
 		
 		try {
 
 			PolicySetType policySet = GetPolicySetOperation.instance(policySetId).execute();
 			return policySet;
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public boolean hasPolicy(String policyId) throws RemoteException {
 		log.info("hasPolicy(\"" + policyId + "\");");
 		try {
 
 			return HasPolicyOperation.instance(policyId).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public boolean hasPolicySet(String policySetId) throws RemoteException {
 		log.info("hasPolicySet(\"" + policySetId + "\");");
 		try {
 
 			return HasPolicySetOperation.instance(policySetId).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public PolicyType[] listPAPPolicies(String papAlias) throws RemoteException {
		log.info("listPolicies(\"" + papAlias + "\");");
 		try {
 
 			return ListPoliciesForPAPOperation.instance(papAlias).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public PolicySetType[] listPAPPolicySets(String papAlias) throws RemoteException {
 		log.info("listPolicySets(\"" + papAlias + "\");");
 		try {
 
 			return ListPolicySetsForPAPOperation.instance(papAlias).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public PolicyType[] listPolicies() throws RemoteException {
 		log.info("listPolicies();");
 		try {
 
 			PolicyType[] policyArray = ListPoliciesOperation.instance().execute();
 			log.info("Returning " + policyArray.length + " policies");
 			return policyArray;
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public PolicySetType[] listPolicySets() throws RemoteException {
 		log.info("listPolicySets();");
 
 		try {
 
 			PolicySetType[] policySetArray = null;
 			policySetArray = ListPolicySetOperation.instance().execute();
 			log.info("Returning " + policySetArray.length + " policy sets");
 			return policySetArray;
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public boolean removePolicy(String policyId) throws RemoteException {
 		log.info("removePolicy(\"" + policyId + "\");");
 		try {
 
 			return RemovePolicyOperation.instance(policyId).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public boolean removePolicyAndReferences(String policyId) throws RemoteException {
 		log.info("removePolicyAndReferences(\"" + policyId + "\");");
 
 			try {
 
 				PAPContainer localPAP = PAPManager.getInstance().getLocalPAPContainer();
 				if (!localPAP.hasPolicy(policyId))
 					return false;
 				localPAP.removePolicyAndReferences(policyId);
 				return true;
 
 			} catch (RuntimeException e) {
 				ServiceClassExceptionManager.log(log, e);
 				throw e;
 			}
 	}
 
 	public boolean removePolicySet(String policySetId) throws RemoteException {
 		log.info("removePolicySet(\"" + policySetId + "\");");
 		return RemovePolicySetOperation.instance(policySetId).execute();
 	}
 
 	public String storePolicy(String idPrefix, PolicyType policy) throws RemoteException {
 		log.info("storePolicy();");
 		try {
 
 			return StorePolicyOperation.instance(idPrefix, policy).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public String storePolicySet(String idPrefix, PolicySetType policySet) throws RemoteException {
 		log.info("storePolicySet();");
 		try {
 
 			return StorePolicySetOperation.instance(idPrefix, policySet).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public boolean updatePolicy(PolicyType policy) throws RemoteException {
 		log.info("updatePolicy();");
 		try {
 
 			return UpdatePolicyOperation.instance(policy).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 	public boolean updatePolicySet(PolicySetType policySet) throws RemoteException {
 		log.info("updatePolicySet();");
 		try {
 
 			return UpdatePolicySetOperation.instance(policySet).execute();
 
 		} catch (RuntimeException e) {
 			ServiceClassExceptionManager.log(log, e);
 			throw e;
 		}
 	}
 
 }
