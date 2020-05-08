 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.pap.papmanagement;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.glite.authz.pap.common.Pap;
 import org.glite.authz.pap.common.xacml.impl.TypeStringUtils;
 import org.glite.authz.pap.common.xacml.utils.PolicySetHelper;
 import org.glite.authz.pap.common.xacml.wizard.PolicySetWizard;
 import org.glite.authz.pap.monitoring.MonitoredProperties;
 import org.glite.authz.pap.repository.dao.DAOFactory;
 import org.glite.authz.pap.repository.dao.PapDAO;
 import org.glite.authz.pap.repository.dao.PolicyDAO;
 import org.glite.authz.pap.repository.dao.PolicySetDAO;
 import org.glite.authz.pap.repository.exceptions.AlreadyExistsException;
 import org.glite.authz.pap.repository.exceptions.InvalidVersionException;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.glite.authz.pap.repository.exceptions.RepositoryException;
 import org.joda.time.DateTime;
 import org.joda.time.chrono.ISOChronology;
 import org.opensaml.xacml.policy.PolicySetType;
 import org.opensaml.xacml.policy.PolicyType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class manages the content (the policies) of a <code>Pap</code>.
  * <p>
  * A pap has a root policy set which <i>id</i> is the same as the <i>pap id</i> (retrieved with
  * {@link Pap#getId()}). This policy set is the root of the tree of policies of the pap. There are
  * specific methods to create and retrieve this policy set (
  * {@link PapContainer#createRootPolicySet(), {@link PapContainer#getRootPolicySet()}).
  * 
  * @see PapManager
  * @see Pap
  * @see PapDAO
  */
 public class PapContainer {
 
     private static final Logger log = LoggerFactory.getLogger(PapContainer.class);
 
     private static final Object notificationLock = new Object();
     private final Pap pap;
     private final String papId;
     private final PolicyDAO policyDAO;
     private final PolicySetDAO policySetDAO;
     private final String rootPolicySetId;
 
     /**
      * Constructor.
      * 
      * @param pap the <code>Pap</code> to be used as container.
      */
 
     public PapContainer(Pap pap) {
         this.pap = pap;
         papId = pap.getId();
         rootPolicySetId = papId;
         policySetDAO = DAOFactory.getDAOFactory().getPolicySetDAO();
         policyDAO = DAOFactory.getDAOFactory().getPolicyDAO();
     }
 
     /**
      * Convenience method to get a List of containers from a List of paps.
      * 
      * @param papList List of paps.
      * @return the List of containers of the given paps.
      */
     public static List<PapContainer> getContainers(List<Pap> papList) {
 
         List<PapContainer> papContainerList = new ArrayList<PapContainer>(papList.size());
 
         for (Pap pap : papList) {
             papContainerList.add(new PapContainer(pap));
         }
         return papContainerList;
     }
 
     /**
      * Adds a policy into a policy set.
      * 
      * @param index position of the policy to added inside the list of policies already present in
      *            the policy set.
      * @param policySetId the policy set id.
      * @param policy the policy to be added.
      * 
      * @throws NotFoundException if the policy set id was not found.
      * @throws AlreadyExistsException if there's already a policy with the same id of the given one.
      * @throws InvalidVersionException if there was a concurrent modification of the policy set.
      *             Getting this exception means that no policy has been added and the repository
      *             hasn't been modified nor corrupted.
      */
     public void addPolicy(int index, String policySetId, PolicyType policy) {
 
         if (!policySetDAO.exists(papId, policySetId)) {
             throw new NotFoundException("PolicySetId \"" + policySetId + "\" not found");
         }
 
         String policyId = policy.getPolicyId();
 
         policyDAO.store(papId, policy);
 
         int numberOfRules = policy.getRules().size();
 
         TypeStringUtils.releaseUnneededMemory(policy);
 
         PolicySetType policySet = policySetDAO.getById(papId, policySetId);
 
         if (PolicySetHelper.referenceIdExists(policySet, policyId)) {
             throw new AlreadyExistsException("Reference id \"" + policyId + "\" alredy exists");
         }
 
         if (index < 0) {
             PolicySetHelper.addPolicyReference(policySet, policyId);
         } else {
             PolicySetHelper.addPolicyReference(policySet, index, policyId);
         }
 
         String oldVersion = policySet.getVersion();
         PolicySetWizard.increaseVersion(policySet);
 
         try {
             policySetDAO.update(papId, oldVersion, policySet);
         } catch (RepositoryException e) {
             policyDAO.delete(papId, policyId);
             throw e;
         }
 
         TypeStringUtils.releaseUnneededMemory(policySet);
 
         updatePapPolicyLastModificationTime();
 
         notifyPoliciesAdded(numberOfRules);
     }
 
     /**
      * Adds a policy set into the root policy set of the pap.
      * 
      * @param index
      * @param policySet
      * 
      * @throws InvalidVersionException if there was a concurrent modification of the root policy
      *             set. Getting this exception means that no policy has been added and the
      *             repository hasn't been modified nor corrupted.
      */
     public void addPolicySet(int index, PolicySetType policySet) {
 
         String policySetId = policySet.getPolicySetId();
 
         policySetDAO.store(papId, policySet);
 
         PolicySetType rootPolicySet = policySetDAO.getById(papId, rootPolicySetId);
 
         if (PolicySetHelper.referenceIdExists(rootPolicySet, policySetId)) {
             throw new AlreadyExistsException("Reference id \"" + policySetId + "\" alredy exists");
         }
 
         if (index < 0) {
             PolicySetHelper.addPolicySetReference(rootPolicySet, policySetId);
         } else {
             PolicySetHelper.addPolicySetReference(rootPolicySet, index, policySetId);
         }
 
         String oldVersion = rootPolicySet.getVersion();
         PolicySetWizard.increaseVersion(rootPolicySet);
 
         try {
             policySetDAO.update(papId, oldVersion, rootPolicySet);
         } catch (RepositoryException e) {
             policySetDAO.delete(papId, policySetId);
             throw e;
         }
 
         TypeStringUtils.releaseUnneededMemory(rootPolicySet);
 
         updatePapPolicyLastModificationTime();
     }
 
     /**
      * Creates the pap root policy set.
      * 
      * @throws AlreadyExistsException if the pap root policy set already exists.
      */
     public void createRootPolicySet() {
 
         PolicySetType rootPolicySet = PolicySetHelper.buildWithAnyTarget(pap.getId(),
                                                                          PolicySetHelper.COMB_ALG_FIRST_APPLICABLE);
         rootPolicySet.setVersion("0");
 
         policySetDAO.store(papId, rootPolicySet);
     }
 
     /**
      * Delete all the policies of the pap.
      * <p>
      * Important: references of the deleted policies in policy sets are not deleted. See method
      * {@link PapContainer#removePolicyAndReferences(String)}.
      */
     public void deleteAllPolicies() {
         // get the number of rules
         List<PolicyType> policyList = policyDAO.getAll(papId);
         int numberOfRules = 0;
         for (PolicyType policy : policyList) {
             numberOfRules += policy.getRules().size();
             TypeStringUtils.releaseUnneededMemory(policy);
         }
 
         policyDAO.deleteAll(papId);
 
         updatePapPolicyLastModificationTime();
         notifyPoliciesDeleted(numberOfRules);
     }
 
     /**
      * Delete all the policy sets of the pap.
      * <p>
      * Important: references of the deleted policy sets in policy sets (i.e. the root policy set)
      * are not deleted. See method {@link PapContainer#removePolicySetAndReferences(String)}.
      */
     public void deleteAllPolicySets() {
         policySetDAO.deleteAll(papId);
     }
 
     public void deletePolicy(String id) throws NotFoundException, RepositoryException {
         PolicyType policy = policyDAO.getById(papId, id);
 
         int numberOfRules = policy.getRules().size();
 
         policyDAO.delete(papId, id);
 
         updatePapPolicyLastModificationTime();
         notifyPoliciesDeleted(numberOfRules);
     }
 
     /**
      * Delete a policy of the pap.
      * <p>
      * Important: references of the deleted policy in policy sets are not deleted. See method
      * {@link PapContainer#removePolicyAndReferences(String)}.
      * 
      * @param id policy id of the policy to be deleted.
      * 
      * @throws NotFoundException if the given policy id was not found.
      */
     public void deletePolicySet(String id) throws NotFoundException, RepositoryException {
         policySetDAO.delete(papId, id);
     }
 
     /**
      * Returns a List of all the policies of the pap.
      * 
      * @return a List of all the policies of the pap.
      * 
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy file).
      */
     public List<PolicyType> getAllPolicies() {
         return policyDAO.getAll(papId);
     }
 
     /**
      * Return a list of all the policy sets of the pap. The root policy set is the first element of
      * the list.
      * 
      * @return a list of all the policy sets of the pap where thr first element is the root policy
      *         set.
      * 
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy file).
      */
     public List<PolicySetType> getAllPolicySets() {
         List<PolicySetType> policySetList = policySetDAO.getAll(papId);
 
         // place the root policy set as the first element
         for (PolicySetType policySet : policySetList) {
 
             if (policySet.getPolicySetId().equals(rootPolicySetId)) {
 
                 int rootPolicySetIndex = policySetList.indexOf(policySet);
 
                 if (rootPolicySetIndex != 0) {
                     Collections.swap(policySetList, 0, rootPolicySetIndex);
                 }
                 break;
             }
         }
         return policySetList;
     }
 
     /**
      * Returns the number of policies (i.e. number of rules) in the pap.
      * 
      * @return the number of policies (i.e. number of rules) in the pap.
      */
     public int getNumberOfPolicies() {
 
         List<PolicyType> policyList = policyDAO.getAll(papId);
 
         int numberOfRules = 0;
 
         for (PolicyType policy : policyList) {
             numberOfRules += policy.getRules().size();
             TypeStringUtils.releaseUnneededMemory(policy);
         }
 
         return numberOfRules;
     }
 
     /**
      * Return the Pap object this container was build with.
      * 
      * @return the Pap associated to this container.
      */
     public Pap getPap() {
         return this.pap;
     }
 
     /**
      * Returns the root policy set of this pap.
      * 
      * @return the root policy set of this pap.
      */
     public PolicySetType getRootPolicySet() {
         return policySetDAO.getById(papId, rootPolicySetId);
     }
 
     /**
      * Returns the <i>id</i> of the root policy set of this pap.
      * 
      * @return the <i>id</i> of the root policy set.
      */
     public String getRootPolicySetId() {
         return rootPolicySetId;
     }
 
     /**
      * Get a policy by <i>id</i>.
      * 
      * @param id policy id to search for.
      * @return the policy with the given <i>id</i>.
      * 
      * @throws NotFoundException if no policy with the given id was found.
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy file).
      */
     public PolicyType getPolicy(String id) {
         return policyDAO.getById(papId, id);
     }
 
     /**
      * Get a policy set by <i>id</i>.
      * 
      * @param id policy set id to search for.
      * @return the policy set with the given <i>id</i>.
      * 
      * @throws NotFoundException if no policy set with the given id was found.
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy set file).
      */
     public PolicySetType getPolicySet(String id) {
         return policySetDAO.getById(papId, id);
     }
 
     /**
      * Checks for the existence of a policy with the given id.
      * 
      * @param id policy id to search for.
      * @return <code>true</code> if a policy with the given id was found, <code>false</code>
      *         otherwise.
      */
     public boolean hasPolicy(String id) {
         return policyDAO.exists(papId, id);
     }
 
     /**
      * Checks for the existence of a policy set with the given id.
      * 
      * @param id policy set id to search for.
      * @return <code>true</code> if a policy set with the given id was found, <code>false</code>
      *         otherwise.
      */
     public boolean hasPolicySet(String id) {
         return policySetDAO.exists(papId, id);
     }
 
     /**
      * Delete all the policies with no rules. References to that policies are deleted too.
      * 
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy file).
      */
     public void purgePoliciesWithNoRules() {
         List<PolicyType> policyList = policyDAO.getAll(papId);
         for (PolicyType policy : policyList) {
             if (policy.getRules().size() == 0) {
                 removePolicyAndReferences(policy.getPolicyId());
             }
         }
     }
 
     /**
      * Delete all the policy sets with no policies. References to that policy sets are deleted too.
      * 
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy file).
      */
     public void purgePolicySetWithNoPolicies() {
         List<PolicySetType> policySetList = policySetDAO.getAll(papId);
         for (PolicySetType policySet : policySetList) {
 
             String policySetId = policySet.getPolicySetId();
 
             if (rootPolicySetId.equals(policySetId)) {
                 continue;
             }
 
             if (policySet.getPolicyIdReferences().size() == 0) {
                 removePolicySetAndReferences(policySetId);
             }
         }
     }
 
     /**
      * Delete all the policy sets found to be unreferenced (obviously the root policy set is not
      * deleted).
      */
     public void purgeUnreferencedPolicySets() {
 
         Set<String> idSet = new HashSet<String>();
 
         PolicySetType rootPS = policySetDAO.getById(papId, rootPolicySetId);
 
         idSet.add(rootPS.getPolicySetId());
 
         for (String id : PolicySetHelper.getPolicySetIdReferencesValues(rootPS)) {
             idSet.add(id);
         }
 
         TypeStringUtils.releaseUnneededMemory(rootPS);
 
         for (PolicySetType policySet : policySetDAO.getAll(papId)) {
 
             String policySetId = policySet.getPolicySetId();
 
             if (!idSet.contains(policySetId)) {
                 log.info("Purging policy set " + policySetId);
                 policySetDAO.delete(papId, policySetId);
             }
         }
     }
 
     /**
      * Delete all the policies found to be unreferenced.
      */
     public void purgeUnreferencesPolicies() {
 
         Set<String> idSet = new HashSet<String>();
 
         for (PolicySetType policySet : policySetDAO.getAll(papId)) {
 
             List<String> idList = PolicySetHelper.getPolicyIdReferencesValues(policySet);
 
             TypeStringUtils.releaseUnneededMemory(policySet);
 
             for (String id : idList) {
                 idSet.add(id);
             }
         }
 
         for (PolicyType policy : policyDAO.getAll(papId)) {
             String policyId = policy.getPolicyId();
 
             if (!idSet.contains(policyId)) {
                 log.info("Purging policy " + policyId);
                 policyDAO.delete(papId, policyId);
             }
         }
     }
 
     /**
      * Delete a policy and all its references.
      * 
      * @param policyId the policy id of the policy to remove.
      * 
      * @throws NotFoundException if no policy with the given id was found.
      * @throws InvalidVersionException if there was a concurrent modification of the policy set.
      *             Getting this exception means that no policy has been added and the repository
      *             hasn't been modified nor corrupted.
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy file).
      */
     public void removePolicyAndReferences(String policyId) {
 
         if (!policyDAO.exists(papId, policyId)) {
             throw new NotFoundException("PolicyId \"" + policyId + "\" does not exists");
         }
 
         boolean policyAlreadyRemoved = false;
         
         List<PolicySetType> policySetList = policySetDAO.getAll(papId);
 
         for (PolicySetType policySet : policySetList) {
 
             if (PolicySetHelper.deletePolicyReference(policySet, policyId)) {
 
                 if (policySet.getPolicyIdReferences().size() == 0) {
                     
                     removePolicySetAndReferences(policySet.getPolicySetId());
                     
                     policyAlreadyRemoved = true;
                     
                 } else {
 
                     String oldVersion = policySet.getVersion();
                     PolicySetWizard.increaseVersion(policySet);
 
                     policySetDAO.update(papId, oldVersion, policySet);
                 }
 
                 TypeStringUtils.releaseUnneededMemory(policySet);
             }
         }
         
         if (policyAlreadyRemoved) {
             return;
         }
 
         PolicyType policy = policyDAO.getById(papId, policyId);
 
         int numberOfRules = policy.getRules().size();
 
         policyDAO.delete(papId, policyId);
 
         updatePapPolicyLastModificationTime();
         notifyPoliciesDeleted(numberOfRules);
     }
 
     /**
      * Delete a policy set and all its references.
      * 
      * @param policySetId the policy set id of the policy set to remove.
      * 
      * @throws NotFoundException if no policy set with the given id was found.
      * @throws InvalidVersionException if there was a concurrent modification of the root policy
      *             set. Getting this exception means that no policy has been added and the
      *             repository hasn't been modified nor corrupted.
      * @throws RepositoryException if an error occurred (e.g. a corrupted policy set file).
      */
     public void removePolicySetAndReferences(String policySetId) {
 
         if (!policySetDAO.exists(papId, policySetId)) {
             throw new NotFoundException("PolicySetId \"" + policySetId + "\" does not exists");
         }
 
         PolicySetType rootPolicySet = policySetDAO.getById(papId, rootPolicySetId);
 
         if (PolicySetHelper.deletePolicySetReference(rootPolicySet, policySetId)) {
 
             String oldVersion = rootPolicySet.getVersion();
             PolicySetWizard.increaseVersion(rootPolicySet);
 
             policySetDAO.update(papId, oldVersion, rootPolicySet);
 
             TypeStringUtils.releaseUnneededMemory(rootPolicySet);
         }
 
         PolicySetType policySet = policySetDAO.getById(papId, policySetId);
         policySetDAO.delete(papId, policySetId);
 
         List<String> idList = PolicySetHelper.getPolicyIdReferencesValues(policySet);
 
         TypeStringUtils.releaseUnneededMemory(policySet);
 
         for (String policyId : idList) {
 
             PolicyType policy = policyDAO.getById(papId, policyId);
             int numberOfRules = policy.getRules().size();
             TypeStringUtils.releaseUnneededMemory(policy);
 
             policyDAO.delete(papId, policyId);
             notifyPoliciesDeleted(numberOfRules);
         }
 
         updatePapPolicyLastModificationTime();
     }
 
     /**
      * Store a policy.
      * 
      * @param policy the policy to be stored.
      * 
      * @throws AlreadyExistsException if a policy with the same id was found.
      */
     public void storePolicy(PolicyType policy) {
         policyDAO.store(papId, policy);
 
         int numberOfRules = policy.getRules().size();
 
         TypeStringUtils.releaseUnneededMemory(policy);
 
         updatePapPolicyLastModificationTime();
         notifyPoliciesAdded(numberOfRules);
     }
 
     /**
      * Store a policy set.
      * 
      * @param policySet the policy set to be stored.
      * 
      * @throws AlreadyExistsException if a policy set with the same id was found.
      */
     public void storePolicySet(PolicySetType policySet) {
         policySetDAO.store(papId, policySet);
     }
 
     /**
      * Update a policy.
      * 
      * @param version version of the policy in the repository to be updated.
      * @param policy new policy replacing the one with the same id.
      * 
      * @throws NotFoundException if a policy with the same id was not found.
      * @throws InvalidVersionException if there was a concurrent modification of the policy. Getting
      *             this exception means that no policy has been updated and the repository hasn't
      *             been modified nor corrupted.
      */
     public void updatePolicy(String version, PolicyType policy) {
         PolicyType oldPolicy = policyDAO.getById(papId, policy.getPolicyId());
         int numberOfRemovedRules = oldPolicy.getRules().size();
         TypeStringUtils.releaseUnneededMemory(oldPolicy);
 
         int numberOfAddedRules = policy.getRules().size();
 
         policyDAO.update(papId, version, policy);
 
         updatePapPolicyLastModificationTime();
         notifyPoliciesDeleted(numberOfRemovedRules);
         notifyPoliciesAdded(numberOfAddedRules);
     }
 
     /**
      * Update a policy set.
      * 
      * @param version version of the policy set in the repository to be updated.
      * @param newPolicySet new policy set replacing the one with the same id.
      * 
      * @throws NotFoundException if a policy set with the same id was not found.
      * @throws InvalidVersionException if there was a concurrent modification of the policy set.
      *             Getting this exception means that no policy set has been updated and the
      *             repository hasn't been modified nor corrupted.
      */
     public void updatePolicySet(String version, PolicySetType newPolicySet) {
         policySetDAO.update(papId, version, newPolicySet);
         updatePapPolicyLastModificationTime();
     }
 
     /**
      * Notifies that some policies have been added.
      * 
      * @param numOfAddedPolicies number of added policies.
      */
     private void notifyPoliciesAdded(int numOfAddedPolicies) {
 
         String propName;
 
         if (pap.isLocal()) {
             propName = MonitoredProperties.NUM_OF_LOCAL_POLICIES_PROP_NAME;
         } else {
             propName = MonitoredProperties.NUM_OF_REMOTE_POLICIES_PROP_NAME;
         }
 
         synchronized (notificationLock) {
             Integer numOfPoliciesInteger = (Integer) PAPConfiguration.instance()
                                                                      .getMonitoringProperty(propName);
 
             if (numOfPoliciesInteger == null) {
                 return;
             }
 
             int numOfPolicies = numOfPoliciesInteger.intValue() + numOfAddedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
 
             propName = MonitoredProperties.NUM_OF_POLICIES_PROP_NAME;
 
             numOfPoliciesInteger = (Integer) PAPConfiguration.instance().getMonitoringProperty(propName);
             numOfPolicies = numOfPoliciesInteger.intValue() + numOfAddedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
         }
     }
 
     /**
      * Notifies that some policies have been deleted.
      * 
      * @param numOfDeletedPolicies number of deleted policies.
      */
     private void notifyPoliciesDeleted(int numOfDeletedPolicies) {
 
         String propName;
 
         if (pap.isLocal()) {
             propName = MonitoredProperties.NUM_OF_LOCAL_POLICIES_PROP_NAME;
         } else {
             propName = MonitoredProperties.NUM_OF_REMOTE_POLICIES_PROP_NAME;
         }
 
         synchronized (notificationLock) {
             Integer numOfPoliciesInteger = (Integer) PAPConfiguration.instance()
                                                                      .getMonitoringProperty(propName);
 
             if (numOfPoliciesInteger == null) {
                 return;
             }
 
             int numOfPolicies = numOfPoliciesInteger.intValue() - numOfDeletedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
 
             propName = MonitoredProperties.NUM_OF_POLICIES_PROP_NAME;
 
             numOfPoliciesInteger = (Integer) PAPConfiguration.instance().getMonitoringProperty(propName);
             numOfPolicies = numOfPoliciesInteger.intValue() - numOfDeletedPolicies;
             numOfPoliciesInteger = new Integer(numOfPolicies);
             PAPConfiguration.instance().setMonitoringProperty(propName, numOfPoliciesInteger);
         }
     }
 
     /**
      * Notifies the time of last policy modification.
      */
     private void notifyPolicyLastModificationTimeUpdate() {
 
         if (pap.isRemote()) {
             return;
         }
 
         synchronized (notificationLock) {
 	        String lastModificationTimeString = pap.getPolicyLastModificationTimeInMilliseconds();
         	
             PAPConfiguration.instance()
                             .setMonitoringProperty(MonitoredProperties.POLICY_LAST_MODIFICATION_TIME_MILLIS_PROP_NAME,
                                                    lastModificationTimeString);
             
            DateTime lastModificationTime = new DateTime(Long.parseLong(lastModificationTimeString))
            	.withChronology(ISOChronology.getInstanceUTC());
             
             PAPConfiguration.instance()
             				.setMonitoringProperty(MonitoredProperties.POLICY_LAST_MODIFICATION_TIME_PROP_NAME,
                                    lastModificationTime);
         }
     }
 
     /**
      * Update the last policy modification time of the pap in the repository.
      */
     private void updatePapPolicyLastModificationTime() {
         pap.setPolicyLastModificationTime((new GregorianCalendar()).getTimeInMillis());
         synchronized (notificationLock) {
             DAOFactory.getDAOFactory().getPapDAO().update(pap);
         }
         notifyPolicyLastModificationTimeUpdate();
     }
 }
