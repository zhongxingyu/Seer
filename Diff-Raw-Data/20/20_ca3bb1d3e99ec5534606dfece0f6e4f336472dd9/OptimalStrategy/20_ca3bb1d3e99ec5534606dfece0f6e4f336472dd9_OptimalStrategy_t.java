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
 
 package es.csic.iiia.dcop.vp.strategy;
 
 import es.csic.iiia.dcop.CostFunction;
 import es.csic.iiia.dcop.VariableAssignment;
 import es.csic.iiia.dcop.up.UPNode;
import es.csic.iiia.dcop.vp.VPGraph;
 import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public class OptimalStrategy extends VPStrategy {
 
    private static Logger log = LoggerFactory.getLogger(VPGraph.class);

     public static int nMappings = 1;
 
     @Override
     public ArrayList<VariableAssignment> getExtendedMappings(ArrayList<VariableAssignment> mappings, UPNode upnode) {
         final double ng = upnode.getFactory().getSummarizeOperation().getNoGood();
 
         ArrayList<VariableAssignment> nm;
         if (mappings.isEmpty()) {
             // The root node chooses by himself
             nm = new ArrayList<VariableAssignment>(nMappings);
             CostFunction belief = getCombinedBelief(upnode);
             for (int i=0; i<nMappings; i++) {
                 final VariableAssignment map = belief.getOptimalConfiguration(null);
                 nm.add(map);
                 belief.setValue(belief.getIndex(map), ng);
             }
         } else {
             // Others must reduce the belief first
             nm = new ArrayList<VariableAssignment>(mappings.size());
             VariableAssignment lastMap = null, lastMapResult = null;
             CostFunction reducedBelief = null;
             for (VariableAssignment mapping : mappings) {
                 if (mapping.equals(lastMap)) {
                     try {
                         int idx = reducedBelief.getIndex(lastMapResult);
                        //log.trace("RRB: " + reducedBelief);
                         reducedBelief.setValue(idx, ng);
                     } catch(Exception e) {}
                 } else {
                    //log.trace("Map: " + mapping);
                     ArrayList<CostFunction> rb = mapping.isEmpty()
                         ? upnode.getBelief()
                         : upnode.getReducedBelief(mapping);
                     reducedBelief = rb.remove(rb.size()-1).combine(rb);
                    //log.trace("FRB: " + reducedBelief);
                     lastMap = mapping;
                 }
                 lastMapResult = reducedBelief.getOptimalConfiguration(null);
                //log.trace("Res: " + lastMapResult);
                 if (lastMapResult.isEmpty() && !nm.isEmpty()) {
                    lastMapResult = new VariableAssignment(nm.get(nm.size()-1));
                 }
                 lastMapResult.putAll(lastMap);
                //log.trace("Add: " + lastMapResult);
                 nm.add(lastMapResult);
             }
         }
 
         return nm;
     }
 
     private CostFunction getCombinedBelief(UPNode upnode) {
         ArrayList<CostFunction> belief = upnode.getBelief();
         CostFunction combi = belief.remove(belief.size()-1).combine(belief);
         return combi;
     }
 
     private VariableAssignment getOptimalConfiguration(VariableAssignment map, UPNode upnode) {
         ArrayList<CostFunction> belief = upnode.getReducedBelief(map);
         CostFunction combi = belief.remove(belief.size()-1).combine(belief);
         return combi.getOptimalConfiguration(map);
     }
 
     /*private VariableAssignment getOptimalConfigurationByGDL(ArrayList<CostFunction> costFunctions, UPNode upnode) {
         GdlFactory gdlFactory = new GdlFactory();
         gdlFactory.setMode(Modes.TREE_UP);
         DFS dfs = dfs = new MCN(costFunctions.toArray(new CostFunction[0]));
         UPGraph cg = JunctionTreeAlgo.buildGraph(gdlFactory, dfs.getFactorDistribution(), dfs.getAdjacency());
         cg.setRoot(dfs.getRoot());
         JunctionTree jt = new JunctionTree(cg);
         cg.setFactory(upnode.getFactory());
         cg.run(1000);
         VPGraph st = new VPGraph(cg);
         VPResults res = st.run(10000);
         VariableAssignment map = res.getMappings().get(0);
         return map;
     }*/
 
 }
