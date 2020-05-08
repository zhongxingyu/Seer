 /**
  * es.upm.dit.gsi.shanks
  * 02/04/2012
  */
 package es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.smile;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import smile.Network;
 import smile.Network.BayesianAlgorithmType;
 import smile.Network.NodeType;
 import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.exception.UnknowkNodeStateException;
 import es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes.exception.UnknownNodeException;
 import es.upm.dit.gsi.shanks.exception.ShanksException;
 
 /**
  * This class defines the behavior of an agent that inherits Bayesian Reasoning
  * Capability.
  * 
  * @author a.carrera
  * 
  */
 public class ShanksAgentBayesianReasoningCapability {
 
     private static final String softEvidenceNodePrefix = "softEvidenceNode";
     private static final String triggerState = "setSoftEvidence";
 
     /**
      * Load a Bayesian network and return the probabilistic network object
      * 
      * @param networkPath
      * @return
      * @throws Exception
      */
     public static Network loadNetwork(String networkPath)
             throws ShanksException {
         Network net = new Network();
         net.readFile(networkPath);
         net.setBayesianAlgorithm(BayesianAlgorithmType.Lauritzen);
         net.updateBeliefs();
         return net;
     }
 
     /**
      * Load the Bayesian network of the agent
      * 
      * @param agent
      * @throws Exception
      */
     public static void loadNetwork(BayesianReasonerShanksAgent agent)
             throws ShanksException {
         Network bn = ShanksAgentBayesianReasoningCapability.loadNetwork(agent
                 .getBayesianNetworkFilePath());
         agent.setBayesianNetwork(bn);
     }
 
     /**
      * Add information to the Bayesian network to reason with it.
      * 
      * @param bn
      * @param nodeName
      * @param status
      * @throws Exception
      */
     public static void addEvidence(Network bn, String nodeName, String status)
             throws ShanksException {
 
         if (bn == null || nodeName == null || status == null) {
             throw new ShanksException("Null parameter in addEvidence method.");
         }
         try {
             if (!bn.isPropagatedEvidence(nodeName)) {
                 if (bn.isRealEvidence(nodeName)) {
                     bn.clearEvidence(nodeName);
                 }
                 bn.setEvidence(nodeName, status);
                 bn.updateBeliefs();
             }
         } catch (Exception e) {
             bn.updateBeliefs();
             HashMap<String, Float> belief = ShanksAgentBayesianReasoningCapability
                     .getNodeStatesHypotheses(bn, nodeName);
 
             String msg = e.getMessage() + " -> values for node: " + nodeName
                     + " -> ";
             for (Entry<String, Float> entry : belief.entrySet()) {
                 msg = msg + entry.getKey() + "-" + entry.getValue() + " ";
             }
            throw new ShanksException(msg);
         }
     }
 
     /**
      * Add soft-evidence to the Bayesian network to reason with it.
      * 
      * @param bn
      * @param nodeName
      * @param softEvidence
      * @throws ShanksException
      */
     public static void addSoftEvidence(Network bn, String nodeName,
             HashMap<String, Double> softEvidence) throws ShanksException {
         String auxNodeName = softEvidenceNodePrefix + nodeName;
         int targetNode = bn.getNode(nodeName);
         boolean found = false;
         int[] children = bn.getChildren(targetNode);
         for (int child : children) {
             if (bn.getNodeName(child).equals(auxNodeName)) {
                 if (bn.getOutcomeCount(child) == 2
                         && bn.getOutcomeId(child, 0).equals(triggerState)) {
                     found = true;
                     break;
                 }
             }
         }
         if (!found) {
             // Create soft-evidence node
             bn.addNode(NodeType.Cpt, auxNodeName);
             bn.setNodeName(auxNodeName, auxNodeName);
             int node = bn.getNode(auxNodeName);
             bn.setOutcomeId(node, 0, triggerState);
             bn.setOutcomeId(node, 1, "NON" + triggerState);
             // bn.insertOutcome(auxNode, 0, triggerState);
             // bn.insertOutcome(auxNode, 1, "NON" + triggerState);
             double[] cpt = bn.getNodeDefinition(auxNodeName);
             for (int i = 0; i < cpt.length; i++) {
                 cpt[i] = (float) 0.5;
             }
 
             bn.addArc(targetNode, bn.getNode(auxNodeName));
             cpt = bn.getNodeDefinition(auxNodeName);
             for (int i = 0; i < cpt.length; i++) {
                 cpt[i] = (float) 0.5;
             }
         }
         ShanksAgentBayesianReasoningCapability
                 .updateSoftEvidenceAuxiliaryNodeCPT(bn, nodeName, softEvidence);
         ShanksAgentBayesianReasoningCapability.addEvidence(bn,
                 softEvidenceNodePrefix + nodeName, triggerState);
 
     }
 
     /**
      * Update the CPT of the aux node to get the soft-evidence in the target
      * node
      * 
      * @param bn
      * @param targetNode
      * @param auxNode
      * @param softEvidence
      * @throws ShanksException
      */
     private static void updateSoftEvidenceAuxiliaryNodeCPT(Network bn,
             String targetNodeName, HashMap<String, Double> softEvidence)
             throws ShanksException {
 
         int targetNode = bn.getNode(targetNodeName);
         int auxNode = bn.getNode(softEvidenceNodePrefix + targetNodeName);
 
         // Check if new beliefs join 1
         double total = 0;
         for (Entry<String, Double> entry : softEvidence.entrySet()) {
             total += entry.getValue();
         }
         double aux = 1 - total;
         if (aux < (-0.05) || aux > 0.05) {
             throw new ShanksException(
                     "Impossible to set soft-evidence in node: "
                             + targetNodeName
                             + " Target probabilistic distribution is not consistent. All states joint: "
                             + total);
         }
 
         // Check if believes are consistent
         if (bn.getOutcomeCount(targetNodeName) != softEvidence.size()) {
             throw new ShanksException(
                     "Old belief and new belief are incompatible. Different number of states of hypothesis");
         }
 
         for (String status : softEvidence.keySet()) {
             boolean found = false;
             String[] outcomes = bn.getOutcomeIds(targetNode);
             for (int i = 0; i < outcomes.length; i++) {
                 if (outcomes[i].equals(status)) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 throw new ShanksException(
                         "Not valid Belief, exist unconsistent between current and old believes.");
             }
         }
 
         // Now, when the consistent has been checked
         // Update the belief
 
         // Reset evidence.
         ShanksAgentBayesianReasoningCapability.clearEvidence(bn,
                 bn.getNodeName(auxNode));
 
         // Obtain required data
         int size = bn.getOutcomeCount(targetNodeName);
         double[] currentProbabilities = new double[size];
         double[] newProbabilities = new double[size];
         String[] states = bn.getOutcomeIds(targetNodeName);
         bn.updateBeliefs();
         double[] values = bn.getNodeValue(targetNodeName);
         for (int i = 0; i < size; i++) {
             for (String status : softEvidence.keySet()) {
                 if (states[i].equals(status)) {
                     currentProbabilities[i] = values[i];
                     newProbabilities[i] = softEvidence.get(status);
                     break;
                 }
             }
         }
 
         // Build the new CPT
         double auxNumber = new Float(0.1);
         double[] cptProbabilitiesPrime = new double[currentProbabilities.length];
         for (int i = 0; i < currentProbabilities.length; i++) {
             cptProbabilitiesPrime[i] = 0.5;
             if (currentProbabilities[i] != 0.0) {
                 cptProbabilitiesPrime[i] = newProbabilities[i] * auxNumber
                         / currentProbabilities[i];
             } else if (newProbabilities[i] != 0.0) {
                 // If there is a full confidence (i.e. 0.0 probability, you must
                 // not request a belief
                 // It is impossible that a change in a full confidence (100% or
                 // 0%)
                 // If this occurs, the BN is wrong fixed (CPT's are wrong)
                 throw new ShanksException(
                         "Incoherence! Belief does not update. Probability for status: "
                                 + targetNodeName + " is ficked as evidence!!");
             }
             if (cptProbabilitiesPrime[i] > 1) {
                 // In this case, reduce auxNumber and rebuild CPT probabilities.
                 auxNumber = auxNumber / 2;
                 i = -1;
             }
         }
 
         // Update the CPT and the Inference Engine
         // TODO check this
         double[] cpt = new double[bn.getNodeDefinition(auxNode).length];
         for (int i = 0; i < currentProbabilities.length; i++) {
             cpt[2 * i] = cptProbabilitiesPrime[i];
             cpt[1 + (2 * i)] = (1 - cptProbabilitiesPrime[i]);
         }
         bn.setNodeDefinition(softEvidenceNodePrefix + targetNodeName, cpt);
 
         // Compiling new network
         // Map<String, String> currentEvidences =
         // ShanksAgentBayesianReasoningCapability
         // .getEvidences(bn);
         // ShanksAgentBayesianReasoningCapability.clearEvidences(bn);
         // ShanksAgentBayesianReasoningCapability.addEvidences(bn,
         // currentEvidences);
 
         // Testing CPT
 
         // ShanksAgentBayesianReasoningCapability.addEvidence(bn,
         // softEvidenceNodePrefix + targetNodeName, triggerState);
         // double conf =
         // ShanksAgentBayesianReasoningCapability.getHypothesis(bn,
         // softEvidenceNodePrefix + targetNodeName, triggerState);
         // if (Math.abs(conf - 1) > 0.01) {
         // throw new ShanksException(
         // "Error adding finding to soft-evidence node for node: "
         // + targetNodeName
         // + " It should be equals to 1, but it is: " + conf);
         // }
         // ShanksAgentBayesianReasoningCapability.clearEvidence(bn, auxNode);
     }
 
     /**
      * Clear a hard evidence fixed in a given node
      * 
      * /**
      * 
      * @param bn
      * @param nodeName
      * @throws ShanksException
      */
     public static void clearEvidence(Network bn, String nodeName)
             throws ShanksException {
         int node = bn.getNode(nodeName);
         if (bn.isEvidence(node)) {
             bn.clearEvidence(nodeName);
             bn.updateBeliefs();
         }
     }
 
     /**
      * Clear a hard evidence fixed in a given node
      * 
      * @param node
      * @throws ShanksException
      */
     public static void clearEvidence(Network bn, int node) {
         if (bn.isEvidence(node)) {
             bn.clearEvidence(node);
             bn.updateBeliefs();
         }
     }
 
     /**
      * Return all current evidences in the BN
      * 
      * @param bn
      * @return
      */
     public static Map<String, String> getEvidences(Network bn) {
 
         HashMap<String, String> evidences = new HashMap<String, String>();
         for (int n : bn.getAllNodes()) {
             if (bn.isEvidence(n)) {
                 evidences.put(bn.getNodeName(n), bn.getEvidenceId(n));
             }
         }
         return evidences;
     }
 
     /**
      * Get the value of a status in a node
      * 
      * @param bn
      * @param nodeName
      * @param status
      * @return a float with the probability
      * @throws UnknownNodeException
      * @throws UnknowkNodeStateException
      */
     public static float getHypothesis(Network bn, String nodeName, String status)
             throws ShanksException {
         int node = ShanksAgentBayesianReasoningCapability.getNode(bn, nodeName);
         String[] states = bn.getOutcomeIds(node);
         for (int i = 0; i < states.length; i++) {
             if (status.equals(states[i])) {
                 return (float) bn.getNodeValue(node)[i];
             }
         }
         throw new UnknowkNodeStateException(bn, nodeName, status);
     }
 
     /**
      * Return the complete node
      * 
      * @param bn
      * @param nodeName
      * @return the ProbabilisticNode object
      * @throws UnknownNodeException
      */
     public static int getNode(Network bn, String nodeName)
             throws ShanksException {
         int node = bn.getNode(nodeName);
         return node;
     }
 
     /**
      * Add a set of evidences to the Bayesian network to reason with it.
      * 
      * @param bn
      * @param evidences
      *            map in format <nodeName, status> to set evidences in the
      *            bayesian network
      * @throws ShanksException
      */
     public static void addEvidences(Network bn, Map<String, String> evidences)
             throws ShanksException {
         if (bn == null || evidences.isEmpty()) {
             throw new ShanksException("Null parameter in addEvidences method.");
         }
         for (Entry<String, String> evidence : evidences.entrySet()) {
             ShanksAgentBayesianReasoningCapability.addEvidence(bn,
                     evidence.getKey(), evidence.getValue());
         }
     }
 
     /**
      * Add a set of soft-evidences to the Bayesian network to reason with it. It
      * creates automatically the auxiliary nodes.
      * 
      * @param bn
      * @param evidences
      *            hashmap in format <nodeName, hashmap> to set evidences in the
      *            bayesian network. The second hashmap in format <nodeStatus,
      *            confidence>
      * @throws ShanksException
      */
     public static void addSoftEvidences(Network bn,
             HashMap<String, HashMap<String, Double>> softEvidences)
             throws ShanksException {
         for (Entry<String, HashMap<String, Double>> softEvidence : softEvidences
                 .entrySet()) {
             ShanksAgentBayesianReasoningCapability.addSoftEvidence(bn,
                     softEvidence.getKey(), softEvidence.getValue());
         }
     }
 
     /**
      * Query several states of a node
      * 
      * @param bn
      * @param nodeName
      * @param states
      * @return
      * @throws UnknownNodeException
      * @throws UnknowkNodeStateException
      */
     public static HashMap<String, Float> getNodeHypotheses(Network bn,
             String nodeName, List<String> states) throws ShanksException {
         HashMap<String, Float> result = new HashMap<String, Float>();
         for (String status : states) {
             result.put(status, ShanksAgentBayesianReasoningCapability
                     .getHypothesis(bn, nodeName, status));
         }
         return result;
     }
 
     /**
      * Query several states of a set of nodes
      * 
      * @param bn
      * @param queries
      *            in format hashmap of <node, List of states>
      * @return results in format hashmap of <node, hashmap>. The second hashmap
      *         is <state, probability of the hypothesis>
      * @throws UnknownNodeException
      * @throws UnknowkNodeStateException
      */
     public static HashMap<String, HashMap<String, Float>> getHypotheses(
             Network bn, HashMap<String, List<String>> queries)
             throws ShanksException {
         HashMap<String, HashMap<String, Float>> result = new HashMap<String, HashMap<String, Float>>();
         for (Entry<String, List<String>> query : queries.entrySet()) {
             HashMap<String, Float> partialResult = ShanksAgentBayesianReasoningCapability
                     .getNodeHypotheses(bn, query.getKey(), query.getValue());
             result.put(query.getKey(), partialResult);
         }
         return result;
     }
 
     /**
      * To know the full status of a node
      * 
      * @param bn
      * @param nodeName
      * @return hashmap in format <status, hypothesis>
      * @throws UnknownNodeException
      */
     public static HashMap<String, Float> getNodeStatesHypotheses(Network bn,
             String nodeName) throws ShanksException {
         try {
             int node = ShanksAgentBayesianReasoningCapability.getNode(bn,
                     nodeName);
             String[] states = bn.getOutcomeIds(node);
             HashMap<String, Float> result = new HashMap<String, Float>();
             for (int i = 0; i < states.length; i++) {
                 String status = states[i];
                 Float hypothesis = (float) bn.getNodeValue(node)[i];
                 result.put(status, hypothesis);
             }
             return result;
         } catch (Exception e) {
             throw new ShanksException("Problem getting hypotheses from: "
                     + nodeName + " -> Exception message: " + e.getMessage());
         }
     }
 
     /**
      * To know all values of all nodes of the Bayesian network
      * 
      * @param bn
      * @return hashmap in format <node, <status, hypothesis>>
      * @throws UnknownNodeException
      */
     public static HashMap<String, HashMap<String, Float>> getAllHypotheses(
             Network bn) throws ShanksException {
         HashMap<String, HashMap<String, Float>> result = new HashMap<String, HashMap<String, Float>>();
         for (int node : bn.getAllNodes()) {
             String nodeName = bn.getNodeName(node);
             HashMap<String, Float> hypotheses = ShanksAgentBayesianReasoningCapability
                     .getNodeStatesHypotheses(bn, nodeName);
             result.put(nodeName, hypotheses);
         }
         return result;
     }
 
     /**
      * Clear all evidences in the network
      * 
      * @param bn
      * @throws Exception
      */
     public static void clearEvidences(Network bn) throws ShanksException {
         try {
             bn.clearAllEvidence();
             bn.updateBeliefs();
         } catch (Exception e) {
             throw new ShanksException(e);
         }
     }
 
     /**
      * Add information to the Bayesian network to reason with it.
      * 
      * @param agent
      * @param nodeName
      * @param status
      * @throws Exception
      */
     public static void addEvidence(BayesianReasonerShanksAgent agent,
             String nodeName, String status) throws ShanksException {
         ShanksAgentBayesianReasoningCapability.addEvidence(
                 agent.getBayesianNetwork(), nodeName, status);
     }
 
     /**
      * Add soft-evidence to the Bayesian network to reason with it.
      * 
      * @param agent
      * @param nodeName
      * @param softEvidence
      * @throws ShanksException
      */
     public static void addSoftEvidence(BayesianReasonerShanksAgent agent,
             String nodeName, HashMap<String, Double> softEvidence)
             throws ShanksException {
         ShanksAgentBayesianReasoningCapability.addSoftEvidence(
                 agent.getBayesianNetwork(), nodeName, softEvidence);
     }
 
     /**
      * Get the value of a status in a node
      * 
      * @param agent
      * @param nodeName
      * @param status
      * @return a float with the probability
      * @throws UnknownNodeException
      * @throws UnknowkNodeStateException
      */
     public static float getHypothesis(BayesianReasonerShanksAgent agent,
             String nodeName, String status) throws ShanksException {
         return ShanksAgentBayesianReasoningCapability.getHypothesis(
                 agent.getBayesianNetwork(), nodeName, status);
     }
 
     /**
      * Return the complete node
      * 
      * @param agent
      * @param nodeName
      * @return the ProbabilisticNode object
      * @throws UnknownNodeException
      */
     public static int getNode(BayesianReasonerShanksAgent agent, String nodeName)
             throws ShanksException {
         return ShanksAgentBayesianReasoningCapability.getNode(
                 agent.getBayesianNetwork(), nodeName);
     }
 
     /**
      * Add a set of evidences to the Bayesian network to reason with it.
      * 
      * @param agent
      * @param evidences
      *            hashmap in format <nodeName, status> to set evidences in the
      *            bayesian network
      * @throws Exception
      */
     public static void addEvidences(BayesianReasonerShanksAgent agent,
             HashMap<String, String> evidences) throws ShanksException {
         ShanksAgentBayesianReasoningCapability.addEvidences(
                 agent.getBayesianNetwork(), evidences);
     }
 
     /**
      * Add a set of soft-evidences to the Bayesian network to reason with it.
      * 
      * @param agent
      * @param softEvidences
      * @throws ShanksException
      */
     public static void addSoftEvidences(BayesianReasonerShanksAgent agent,
             HashMap<String, HashMap<String, Double>> softEvidences)
             throws ShanksException {
         ShanksAgentBayesianReasoningCapability.addSoftEvidences(
                 agent.getBayesianNetwork(), softEvidences);
     }
 
     /**
      * Query several states of a node
      * 
      * @param agent
      * @param nodeName
      * @param states
      * @return
      * @throws UnknownNodeException
      * @throws UnknowkNodeStateException
      */
     public static HashMap<String, Float> getNodeHypotheses(
             BayesianReasonerShanksAgent agent, String nodeName,
             List<String> states) throws ShanksException {
         return ShanksAgentBayesianReasoningCapability.getNodeHypotheses(
                 agent.getBayesianNetwork(), nodeName, states);
     }
 
     /**
      * Query several states of a set of nodes
      * 
      * @param agent
      * @param queries
      *            in format hashmap of <node, List of states>
      * @return results in format hashmap of <node, hashmap>. The second hashmap
      *         is <state, probability of the hypothesis>
      * @throws UnknownNodeException
      * @throws UnknowkNodeStateException
      */
     public static HashMap<String, HashMap<String, Float>> getHypotheses(
             BayesianReasonerShanksAgent agent,
             HashMap<String, List<String>> queries) throws ShanksException {
         return ShanksAgentBayesianReasoningCapability.getHypotheses(
                 agent.getBayesianNetwork(), queries);
     }
 
     /**
      * 
      * To know the full status of a node
      * 
      * @param agent
      * @param nodeName
      * @return hashmap in format <status, hypothesis>
      * @throws UnknownNodeException
      */
     public static HashMap<String, Float> getNodeStatesHypotheses(
             BayesianReasonerShanksAgent agent, String nodeName)
             throws ShanksException {
         return ShanksAgentBayesianReasoningCapability.getNodeStatesHypotheses(
                 agent.getBayesianNetwork(), nodeName);
     }
 
     /**
      * To know all values of all nodes of the Bayesian network
      * 
      * @param agent
      * @return hashmap in format <node, <status, hypothesis>>
      * @throws UnknownNodeException
      */
 
     public static HashMap<String, HashMap<String, Float>> getAllHypotheses(
             BayesianReasonerShanksAgent agent) throws ShanksException {
         return ShanksAgentBayesianReasoningCapability.getAllHypotheses(agent
                 .getBayesianNetwork());
     }
 
     /**
      * Clear all evidences in the Bayesian network of the agent
      * 
      * @param agent
      * @throws Exception
      */
     public static void clearEvidences(BayesianReasonerShanksAgent agent)
             throws ShanksException {
         ShanksAgentBayesianReasoningCapability.clearEvidences(agent
                 .getBayesianNetwork());
     }
 
 }
