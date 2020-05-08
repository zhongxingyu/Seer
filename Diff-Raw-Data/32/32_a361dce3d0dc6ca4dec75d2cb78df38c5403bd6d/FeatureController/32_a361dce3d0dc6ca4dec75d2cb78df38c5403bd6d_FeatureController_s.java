 /**
  *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.cloudmix.common.controller;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.cloudmix.common.GridController;
 import org.fusesource.cloudmix.common.controller.constraints.agent.AgentLivenessChecker;
 import org.fusesource.cloudmix.common.controller.constraints.agent.AgentMaxFeaturesChecker;
 import org.fusesource.cloudmix.common.controller.constraints.agent.AgentOwnedByFeatureChecker;
 import org.fusesource.cloudmix.common.controller.constraints.agent.AgentPackageSupportChecker;
 import org.fusesource.cloudmix.common.controller.constraints.agent.AgentPreferedHostChecker;
 import org.fusesource.cloudmix.common.controller.constraints.agent.AgentProfileChecker;
 import org.fusesource.cloudmix.common.controller.constraints.agent.IAgentConstraintChecker;
 import org.fusesource.cloudmix.common.dto.Dependency;
 import org.fusesource.cloudmix.common.dto.FeatureDetails;
 import org.fusesource.cloudmix.common.dto.DependencyStatus;
 
 /**
  * @version $Revision: 63441 $
  */
 public class FeatureController {
     private static final transient Log LOG = LogFactory.getLog(FeatureController.class);
 
     private static final List<IAgentConstraintChecker> CHECKERS = 
         new ArrayList<IAgentConstraintChecker>();
 
     static {
         CHECKERS.add(new AgentProfileChecker());
         CHECKERS.add(new AgentLivenessChecker());
         CHECKERS.add(new AgentPackageSupportChecker());
         CHECKERS.add(new AgentOwnedByFeatureChecker());
         CHECKERS.add(new AgentMaxFeaturesChecker());
         CHECKERS.add(new AgentPreferedHostChecker());
     }
 
     private GridController client;
     private FeatureDetails details;
 
     public FeatureController(GridController client, FeatureDetails details) {
         this.client = client;
         this.details = details;
     }
 
     public AgentController selectAgentForDeployment(String profileID,
                                                     Collection<AgentController> candidates) {
         
         for (IAgentConstraintChecker checker : CHECKERS) {
             candidates = checker.applyConstraint(profileID, this, candidates);
            LOG.debug("nb of candidates after running checker "
                       + checker.getClass().getSimpleName()
                       + ": "
                       + candidates.size());
         }
         
         return getAgentWithTheLeastAmountOfFeatures(candidates);
     }
 
     public boolean areDependanciesInstanciated(String profileID) {
         List<Dependency> list = getDependencies();
         
         for (Dependency dependency : list) {
             FeatureController controller = client.getFeatureController(dependency);
             if (controller == null) {
                 LOG.warn("No FeatureController yet for: " + dependency);
                 return false;
             }
             if (!controller.hasAtLeastMinimumInstances(profileID)) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Cannot deploy " + getId() + " due to not enough instances of: " + dependency);
                 }
                 return false;
             }
         }
         return true;
     }
 
     public static AgentController getAgentWithTheLeastAmountOfFeatures(Collection<AgentController> agents) {
         AgentController answer = null;
         for (AgentController agent : agents) {
             if (answer == null) {
                 answer = agent;
             
             } else if (agent.getFeatures().size() < answer.getFeatures().size()) {
                 answer = agent;
             }
         }
         return answer;
     }
 
     /**
      * checks that a given agent is on one of the preferred host of the feature.
      * Note that if the feature doesn't specify any particular host then any host is deemed "preferred"
      * @param agent
      * @return
      */
     public boolean isAgentOnAPreferredMachine(AgentController agent) {
         return getDetails().getPreferredMachines() == null
                 || getDetails().getPreferredMachines().size() == 0
                 || getDetails().getPreferredMachines().contains(agent.getDetails().getHostname());
     }
 
     /**
      * Returns true if the minimum number of instances are currently deployed.
      *
      * @return true if the minimum number of instances are currently deployed
      */
     public boolean hasAtLeastMinimumInstances(String profileID) {
         int actual = instanceCount(profileID, true);
         int expected = Integer.parseInt(getDetails().getMinimumInstances());
         return actual >= expected;
     }
 
     /**
      * Returns true if the maximum number of instances are currently assigned to
      * be deployed (i.e. no more can be deployed)
      *
      * @return true if the maximum number of instances are currently assigned to be deployed
      */
     public boolean canDeployMoreInstances(String profileID) {
         int actual = instanceCount(profileID, false);
         int expected = Integer.parseInt(getDetails().getMaximumInstances());
         
         return actual < expected;
     }
 
     /**
      * Return the number of instances of this feature
      */
     private int instanceCount(String profileID, boolean onlyIfDeployed) {
         return client.getFeatureInstanceCount(getId(), profileID, onlyIfDeployed);
     }
     
     // Properties
     //-------------------------------------------------------------------------
 
     public String getId() {
         return details.getId();
     }
 
     public String getResource() {
         return details.getResource();
     }
 
     public List<Dependency> getDependencies() {
         return details.getDependencies();
     }
 
     public Set<String> getPreferredMachines() {
         return details.getPreferredMachines();
     }
 
     public FeatureDetails getDetails() {
         return details;
     }
 
     public DependencyStatus getStatus(String profileId) {
         DependencyStatus answer = new DependencyStatus(getId());
         answer.setProvisioned(hasAtLeastMinimumInstances(profileId));
         return answer;
     }
 }
