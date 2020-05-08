 /*
  * Software License Agreement (BSD License)
  * 
  * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
  * All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  * 
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  * 
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  * 
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute 
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package es.csic.iiia.dcop.jt;
 
 import es.csic.iiia.dcop.Variable;
 import es.csic.iiia.dcop.mp.DefaultGraph;
 import es.csic.iiia.dcop.up.UPEdge;
 import es.csic.iiia.dcop.up.UPGraph;
 import es.csic.iiia.dcop.up.UPNode;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public class JunctionTree extends DefaultGraph<JTNode, JTEdge, JTResults> {
 
     private static Logger log = LoggerFactory.getLogger(JunctionTree.class);
 
     /**
      * Creates a JT Message passing graph to propagate the variables
      * of the given clique graph.
      *
      * @param cg
      */
     public JunctionTree(UPGraph cg) {
         HashMap<UPNode, JTNode> nodes = new HashMap<UPNode, JTNode>();
 
         // Java generics bug:
         // UPGraph.getNodes() returns an ArrayList<N extends UPNode>, but the
         // current compiler fails to notice it.
         ArrayList<UPNode> ns = cg.getNodes();
         for(UPNode cn : ns) {
             JTNode jn = new JTNode(cn);
             addNode(jn);
             nodes.put(cn, jn);
         }
 
         ArrayList<UPEdge> es = cg.getEdges();
         for(UPEdge e : es) {
             addEdge(new JTEdge(nodes.get(e.getNode1()), nodes.get(e.getNode2()), e));
         }
     }
 
     @Override
     protected JTResults buildResults() {
         return new JTResults();
     }
 
     @Override
     protected void end() {
         super.end();
     }
 
     @Override
     public void reportIteration(int i) {
         log.trace("------- Iter " + i);
     }
 
     @Override
     public void reportStart() {
         log.trace("\n======= ENSURING RIP PROPERTY");
     }
 
     @Override
     public void reportResults(JTResults results) {
         if (log.isTraceEnabled()) {
             log.trace("\n======= CLIQUE TREE/GRAPH");
             log.trace(this.toString());
         }
     }
 
     public int getLowestDecisionRoot() {
         int node = -1;
         int minDecisionVariables = Integer.MAX_VALUE;
 
         ArrayList<JTNode> ns = getNodes();
         for (int i=0, len=ns.size(); i<len; i++) {
             final JTNode n = ns.get(i);
             int dv = getNumberOfDecisionVariables(n);
 
             log.info("Root " + i + ": " + dv + " decision variables.");
             if (dv < minDecisionVariables) {
                 node = i;
                 minDecisionVariables = dv;
             }
         }
 
         return node;
     }
 
     private int getNumberOfDecisionVariables(JTNode n) {
         return dfs(null, n, -1, new HashSet<Variable>());
     }
 
     private int dfs(JTNode root, JTNode node, int maxVars, HashSet<Variable> assignedVars) {
         HashSet<Variable> vars = new HashSet<Variable>(node.getNode().getVariables());
         vars.removeAll(assignedVars);
         if (vars.size() > maxVars) {
             maxVars = vars.size();
         }
 
         vars = new HashSet<Variable>(node.getNode().getVariables());
         for (JTEdge edge : node.getEdges()) {
             // Skip the parent
             JTNode child = edge.getDestination(node);
             if (child == root)
                 continue;
 
             // Recursion call
             final int dv = dfs(node, child, maxVars, vars);
             if (dv > maxVars) {
                 maxVars = dv;
             }
         }
 
         return maxVars;
     }
 
 }
