 /**
  * es.upm.dit.gsi.shanks
  * 02/04/2012
  */
 package es.upm.dit.gsi.shanks.agent.capability.reasoning.bayes;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import unbbayes.io.BaseIO;
 import unbbayes.io.NetIO;
 import unbbayes.io.exception.LoadException;
 import unbbayes.prs.Edge;
 import unbbayes.prs.Node;
 import unbbayes.prs.bn.PotentialTable;
 import unbbayes.prs.bn.ProbabilisticNetwork;
 import unbbayes.prs.bn.ProbabilisticNode;
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
     public static ProbabilisticNetwork loadNetwork(String networkPath)
             throws ShanksException {
         return ShanksAgentBayesianReasoningCapability.loadNetwork(new File(
                 networkPath));
     }
 
     /**
      * Load a Bayesian network and return the probabilistic network object
      * 
      * @param netFile
      * @return
      * @throws Exception
      */
     @SuppressWarnings("deprecation")
     public static ProbabilisticNetwork loadNetwork(File netFile)
             throws ShanksException {
         ProbabilisticNetwork net = null;
         BaseIO io = new NetIO();
         try {
             net = (ProbabilisticNetwork) io.load(netFile);
             net.compile();
         } catch (LoadException e) {
             throw new ShanksException(e);
         } catch (IOException e) {
             throw new ShanksException(e);
         } catch (Exception e) {
             throw new ShanksException(e);
         }
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
         ProbabilisticNetwork bn = ShanksAgentBayesianReasoningCapability
                 .loadNetwork(agent.getBayesianNetworkFilePath());
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
     public static void addEvidence(ProbabilisticNetwork bn, String nodeName,
             String status) throws ShanksException {
         ProbabilisticNode node = ShanksAgentBayesianReasoningCapability
                 .getNode(bn, nodeName);
         if (node.hasEvidence()) {
             ShanksAgentBayesianReasoningCapability.clearEvidence(bn, node);
         }
         int states = node.getStatesSize();
         for (int i = 0; i < states; i++) {
             if (status.equals(node.getStateAt(i))) {
                 node.addFinding(i);
                 try {
                     bn.updateEvidences();
                 } catch (Exception e) {
                     throw new ShanksException(e);
                 }
                 return;
             }
         }
         throw new UnknowkNodeStateException(bn, nodeName, status);
     }
 
     /**
      * Add soft-evidence to the Bayesian network to reason with it.
      * 
      * @param bn
      * @param nodeName
      * @param softEvidence
      * @throws ShanksException
      */
     public static void addSoftEvidence(ProbabilisticNetwork bn,
             String nodeName, HashMap<String, Double> softEvidence)
             throws ShanksException {
         ProbabilisticNode targetNode = ShanksAgentBayesianReasoningCapability
                 .getNode(bn, nodeName);
         boolean found = false;
         for (Node child : targetNode.getChildren()) {
             if (child.getName().equals(softEvidenceNodePrefix + nodeName)) {
                 if (child.getStatesSize() == 2
                         && child.getStateAt(0).equals(triggerState)) {
                     found = true;
                     break;
                 }
             }
         }
         if (!found) {
             // Create soft-evidence node
             ProbabilisticNode auxNode = new ProbabilisticNode();
             auxNode.setName(softEvidenceNodePrefix + nodeName);
             auxNode.setLabel(softEvidenceNodePrefix + nodeName);
             auxNode.appendState(triggerState);
             auxNode.appendState("NON" + triggerState);
             PotentialTable cpt = auxNode.getProbabilityFunction();
             cpt.addVariable(auxNode);
             for (int i = 0; i < cpt.tableSize(); i++) {
                 cpt.setValue(i, (float) 0.5);
             }
             auxNode.initMarginalList();
             bn.addNode(auxNode);
 
             Edge edge = new Edge(targetNode, auxNode);
             try {
                 bn.addEdge(edge);
                 cpt = auxNode.getProbabilityFunction();
                 for (int i = 0; i < cpt.tableSize(); i++) {
                     cpt.setValue(i, (float) 0.5);
                 }
                 auxNode.initMarginalList();
             } catch (Exception e) {
                 throw new ShanksException(e);
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
     private static void updateSoftEvidenceAuxiliaryNodeCPT(
             ProbabilisticNetwork bn, String targetNodeName,
             HashMap<String, Double> softEvidence) throws ShanksException {
 
         ProbabilisticNode targetNode = (ProbabilisticNode) bn
                 .getNode(targetNodeName);
         ProbabilisticNode auxNode = (ProbabilisticNode) bn
                 .getNode(softEvidenceNodePrefix + targetNodeName);
 
         // Check if new beliefs join 1
         double total = 0;
         for (Entry<String, Double> entry : softEvidence.entrySet()) {
             total += entry.getValue();
         }
         double aux = 1 - total;
         if (aux < (-0.05) || aux > 0.05) {
             throw new ShanksException(
                     "Impossible to set soft-evidence in node: "
                             + targetNode.getName()
                             + " Target probabilistic distribution is not consistent. All states joint: "
                             + total);
         }
 
         // Check if believes are consistent
         if (targetNode.getStatesSize() != softEvidence.size()) {
             throw new ShanksException(
                     "Old belief and new belief are incompatible. Different number of states of hypothesis");
         }
 
         for (String status : softEvidence.keySet()) {
             boolean found = false;
             for (int i = 0; i < targetNode.getStatesSize(); i++) {
                 if (targetNode.getStateAt(i).equals(status)) {
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
         ShanksAgentBayesianReasoningCapability.clearEvidence(bn, auxNode);
 
         // Obtain required data
         double[] currentProbabilities = new double[targetNode.getStatesSize()];
         double[] newProbabilities = new double[targetNode.getStatesSize()];
         for (int i = 0; i < targetNode.getStatesSize(); i++) {
             for (String status : softEvidence.keySet()) {
                 if (targetNode.getStateAt(i).equals(status)) {
                     currentProbabilities[i] = targetNode.getMarginalAt(i);
                     newProbabilities[i] = softEvidence.get(status);
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
         PotentialTable cpt = auxNode.getProbabilityFunction();
         for (int i = 0; i < cptProbabilitiesPrime.length; i++) {
             cpt.setValue(2 * i, (float) cptProbabilitiesPrime[i]);
             cpt.setValue(1 + (2 * i), (float) (1 - cptProbabilitiesPrime[i]));
         }
 
         // Compiling new network
         Map<String, String> currentEvidences = ShanksAgentBayesianReasoningCapability
                 .getEvidences(bn);
         ShanksAgentBayesianReasoningCapability.clearEvidences(bn);
         ShanksAgentBayesianReasoningCapability.addEvidences(bn,
                 currentEvidences);
 
         // Testing CPT
 
         ShanksAgentBayesianReasoningCapability.addEvidence(bn,
                 softEvidenceNodePrefix + targetNodeName, triggerState);
         double conf = ShanksAgentBayesianReasoningCapability.getHypothesis(bn,
                 softEvidenceNodePrefix + targetNodeName, triggerState);
        if (Math.abs(conf - 1) > 0.01) {
             throw new ShanksException(
                     "Error adding finding to soft-evidence node for node: "
                             + targetNodeName
                             + " It should be equals to 1, but it is: " + conf);
         }
         ShanksAgentBayesianReasoningCapability.clearEvidence(bn, auxNode);
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
     public static void clearEvidence(ProbabilisticNetwork bn, String nodeName)
             throws ShanksException {
         ProbabilisticNode node = (ProbabilisticNode) bn.getNode(nodeName);
         ShanksAgentBayesianReasoningCapability.clearEvidence(bn, node);
     }
 
     /**
      * Clear a hard evidence fixed in a given node
      * 
      * @param node
      * @throws ShanksException
      */
     public static void clearEvidence(ProbabilisticNetwork bn,
             ProbabilisticNode node) throws ShanksException {
         if (node.hasEvidence()) {
             Map<String, String> evidences = ShanksAgentBayesianReasoningCapability
                     .getEvidences(bn);
             ShanksAgentBayesianReasoningCapability.clearEvidences(bn);
             evidences.remove(node.getName());
             ShanksAgentBayesianReasoningCapability.addEvidences(bn, evidences);
         }
     }
 
     /**
      * Return all current evidences in the BN
      * 
      * @param bn
      * @return
      */
     public static Map<String, String> getEvidences(ProbabilisticNetwork bn) {
 
         HashMap<String, String> evidences = new HashMap<String, String>();
         for (Node n : bn.getNodes()) {
             ProbabilisticNode pn = (ProbabilisticNode) n;
             if (pn.hasEvidence()) {
                 evidences.put(pn.getName(), pn.getStateAt(pn.getEvidence()));
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
     public static float getHypothesis(ProbabilisticNetwork bn, String nodeName,
             String status) throws ShanksException {
         ProbabilisticNode node = ShanksAgentBayesianReasoningCapability
                 .getNode(bn, nodeName);
         int states = node.getStatesSize();
         for (int i = 0; i < states; i++) {
             if (status.equals(node.getStateAt(i))) {
                 return node.getMarginalAt(i);
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
     public static ProbabilisticNode getNode(ProbabilisticNetwork bn,
             String nodeName) throws ShanksException {
         ProbabilisticNode node = (ProbabilisticNode) bn.getNode(nodeName);
         if (node == null) {
             throw new UnknownNodeException(bn, nodeName);
         }
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
     public static void addEvidences(ProbabilisticNetwork bn,
             Map<String, String> evidences) throws ShanksException {
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
     public static void addSoftEvidences(ProbabilisticNetwork bn,
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
     public static HashMap<String, Float> getNodeHypotheses(
             ProbabilisticNetwork bn, String nodeName, List<String> states)
             throws ShanksException {
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
             ProbabilisticNetwork bn, HashMap<String, List<String>> queries)
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
     public static HashMap<String, Float> getNodeStatesHypotheses(
             ProbabilisticNetwork bn, String nodeName) throws ShanksException {
         ProbabilisticNode node = ShanksAgentBayesianReasoningCapability
                 .getNode(bn, nodeName);
         HashMap<String, Float> result = new HashMap<String, Float>();
         int statesNum = node.getStatesSize();
         for (int i = 0; i < statesNum; i++) {
             String status = node.getStateAt(i);
             Float hypothesis = node.getMarginalAt(i);
             result.put(status, hypothesis);
         }
         return result;
     }
 
     /**
      * To know all values of all nodes of the Bayesian network
      * 
      * @param bn
      * @return hashmap in format <node, <status, hypothesis>>
      * @throws UnknownNodeException
      */
     public static HashMap<String, HashMap<String, Float>> getAllHypotheses(
             ProbabilisticNetwork bn) throws ShanksException {
         List<Node> nodes = bn.getNodes();
         HashMap<String, HashMap<String, Float>> result = new HashMap<String, HashMap<String, Float>>();
         for (Node node : nodes) {
             String nodeName = node.getName();
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
     @SuppressWarnings("deprecation")
     public static void clearEvidences(ProbabilisticNetwork bn)
             throws ShanksException {
         try {
             bn.compile();
             // bn.initialize();
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
     public static ProbabilisticNode getNode(BayesianReasonerShanksAgent agent,
             String nodeName) throws ShanksException {
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
