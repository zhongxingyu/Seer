 /*
  * Copyright (C) 2013 Tim Vaughan <tgvaughan@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package argbeast;
 
 import beast.core.Description;
 import beast.core.Input;
 import beast.core.Input.Validate;
 import beast.core.StateNode;
 import beast.core.StateNodeInitialiser;
 import beast.evolution.alignment.Alignment;
 import beast.evolution.alignment.Sequence;
 import beast.evolution.tree.Node;
 import beast.evolution.tree.coalescent.PopulationFunction;
 import beast.util.Randomizer;
 import com.google.common.collect.Lists;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * @author Tim Vaughan <tgvaughan@gmail.com>
  */
 @Description("Simulates an ARG - can be used for chain initialization or for "
         + "sampler validation.")
 public class RecombinationGraphSimulator extends RecombinationGraph implements StateNodeInitialiser {
 
     public Input<Double> rhoInput = new Input<Double>("rho",
             "Recombination rate parameter.", Validate.REQUIRED);
     
     public Input<Double> deltaInput = new Input<Double>("delta",
             "Tract length parameter.", Validate.REQUIRED);
     
     public Input<PopulationFunction> popFuncInput = new Input<PopulationFunction>(
             "populationModel", "Demographic model to use.", Validate.REQUIRED);
     
     public Input<Integer> sequenceLengthInput = new Input<Integer>(
             "sequenceLength", "Length of sequence to use in simulation."
                     + " (Only use when alignment is not available.)");
     
     public Input<Integer> nTaxaInput = new Input<Integer>(
             "nTaxa", "Number of taxa to use in simulation. "
                     + "(Only use when alignment is unavailable.)");
 
     private double rho, delta;
     private PopulationFunction popFunc;
     
     private class Event {
         double tau, t;
         int lineages;
         
         Event (double t, double tau, int lineages) {
             this.t = t;
             this.tau = tau;
             this.lineages = lineages;
         }

        @Override
        public String toString() {
            return "t: " + t + ", k: " + lineages;
        }
     }
     private List<Event> eventList;
     
     public RecombinationGraphSimulator() {
 
         alignmentInput.setRule(Validate.OPTIONAL);
     };
     
     @Override
     public void initAndValidate() throws Exception {
 
         rho = rhoInput.get();
         delta = deltaInput.get();
         popFunc = popFuncInput.get();
         
         if (sequenceLengthInput.get() != null && nTaxaInput.get() != null) {
 
             // Generate random alignment of specified length
             List<Sequence> seqList = Lists.newArrayList();
             for (int i=0; i<nTaxaInput.get(); i++)
                 seqList.add(new Sequence("taxon" + i, getSeq(sequenceLengthInput.get())));
 
             alignmentInput.setValue(new Alignment(seqList, 4, "nucleotide"), this);
         }
         else {
             if (alignmentInput.get() == null)
                 throw new IllegalArgumentException("Need to specify either "
                         + "alignment or both sequenceLength and nTaxa.");
         }
         
         eventList = Lists.newArrayList();
         simulateClonalFrame();
         
         initArrays();
         super.initAndValidate();
         
         generateRecombinations();
 
        System.out.println(getExtendedNewick());
     }
     
     /**
      * Generate a random nucleotide sequence of specified length.
      * 
      * @param length
      * @return String representation of DNA sequence
      */
     private String getSeq(int length) {
         StringBuilder seq = new StringBuilder();
         String alphabet = "GTCA";
         for (int i=0; i<length; i++)
             seq.append(alphabet.charAt((Randomizer.nextInt(4))));
         
         return seq.toString();
     }
 
     /**
      * Use coalescent model to simulate clonal frame.
      */
     private void simulateClonalFrame() {
 
         // Initialize leaf nodes
         List<Node> leafNodes = Lists.newArrayList();
         for (int i=0; i<alignmentInput.get().getNrTaxa(); i++) {
             Node leaf = new Node();
             leaf.setNr(i);
             if (timeTraitSet != null)
                 leaf.setHeight(timeTraitSet.getValue(i));
             else
                 leaf.setHeight(0.0);
             
             leafNodes.add(leaf);
         }
         
         // Create and sort list of inactive nodes
         List<Node> inactiveNodes = Lists.newArrayList(leafNodes);
         Collections.sort(inactiveNodes, new Comparator<Node>() {
             @Override
             public int compare(Node n1, Node n2) {
                 if (n1.getHeight()<n2.getHeight())
                     return -1;
                 
                 if (n2.getHeight()>n1.getHeight())
                     return 1;
                 
                 return 0;
             }
         });
         
         List<Node> activeNodes = Lists.newArrayList();
         eventList.clear();
         
         double tau = 0.0;
         int nextNr = leafNodes.size();
         while (true) {
             
             // Calculate coalescence propensity
             int k = activeNodes.size();
             double chi = 0.5*k*(k-1);
             
             // Draw scaled coalescent time
             if (chi>0.0)
                 tau += Randomizer.nextExponential(chi);
             else
                 tau = Double.POSITIVE_INFINITY;
             
             // Convert to real time
             double t = popFunc.getInverseIntensity(tau);
             
             // If new time takes us past next sample time, insert that sample
             if (!inactiveNodes.isEmpty() && t>inactiveNodes.get(0).getHeight()) {
                 Node nextActive = inactiveNodes.remove(0);
                 activeNodes.add(nextActive);
                 tau = popFunc.getIntensity(nextActive.getHeight());
                 eventList.add(new Event(nextActive.getHeight(),
                         tau, activeNodes.size()));
                 continue;
             }
             
             // Coalesce random pair of active nodes.
             Node node1 = activeNodes.remove(Randomizer.nextInt(k));
             Node node2 = activeNodes.remove(Randomizer.nextInt(k-1));
             
             Node parent = new Node();
             parent.addChild(node1);
             parent.addChild(node2);
             parent.setHeight(t);
             parent.setNr(nextNr++);
             
             activeNodes.add(parent);
             eventList.add(new Event(t, tau, activeNodes.size()));
             
             if (inactiveNodes.isEmpty() && activeNodes.size()<2)
                 break;
         }
         
         // Remaining active node is root
         setRoot(activeNodes.get(0));
     }
     
     /**
      * Uses a discrete two-state Markov process to generate the list of
      * converted segments along the sequence.
      */
     private void generateRecombinations() {
         
         double pRec = 0.5*rho*getClonalFrameLength()/getSequenceLength();
         double pTractEnd = 1.0/delta;
         double p0cf = 1.0/(1.0 + pRec*delta);
         
         long l; // next available convertible locus
         if (Randomizer.nextDouble()>p0cf) {
             Recombination recomb = new Recombination();
             recomb.setStartLocus(0);
             long tractLength = Randomizer.nextGeometric(pTractEnd);
             recomb.setEndLocus(Math.min(tractLength, getSequenceLength()-1));
             associateRecombinationWithCF(recomb);
             addRecombination(recomb);
             
             l = tractLength+1;
         } else
             l = 1;
         
         if (l>=getSequenceLength())
             return;
 
         while (true) {
             l += Randomizer.nextGeometric(pRec);
 
             if (l>=getSequenceLength())
                 break;
             
             Recombination recomb = new Recombination();
 
             recomb.setStartLocus(l);
             l += Randomizer.nextGeometric(pTractEnd);
             recomb.setEndLocus(Math.min(l,getSequenceLength()-1));
 
             associateRecombinationWithCF(recomb);
             addRecombination(recomb);
             
             if (l>=getSequenceLength())
                 break;
         }
     }
     
     /**
      * Associates recombination with the clonal frame, selecting points of
      * departure and coalescence.
      * 
      * @param recomb recombination to associate
      */
     private void associateRecombinationWithCF(Recombination recomb) {
     
         // Select departure point            
         double u = Randomizer.nextDouble()*getClonalFrameLength();
         double tauStart = 0.0;
         
         boolean started = false;
         for (int eidx=0; eidx<eventList.size(); eidx++) {
             Event event = eventList.get(eidx);
 
             if (!started) {
                 
                double interval = eventList.get(eidx+1).t - event.t;
                 
                 if (u<interval*event.lineages) {
                     for (Node node : getNodesAsArray()) {
                         if (!node.isRoot()
                                 && node.getHeight()<=event.t
                                && node.getParent().getHeight()>event.t) {
                             
                             if (u<interval) {
                                 recomb.setNode1(node);
                                 recomb.setHeight1(event.t + u);
                                 tauStart = popFunc.getIntensity(event.t + u);
                                 break;
                             } else
                                 u -= interval;
                             
                         }
                     }
                     started = true;
                     u = Randomizer.nextExponential(1.0);
                 } else
                     u -= interval*event.lineages;
             }
             
             if (started) {
                 double intervalArea;
                 if (eidx<eventList.size()-1)
                     intervalArea = (eventList.get(eidx+1).tau - Math.max(event.tau, tauStart));
                 else
                     intervalArea = Double.POSITIVE_INFINITY;
                 
                 if (u<intervalArea*event.lineages) {
                     for (Node node : getNodesAsArray()) {
                         if (node.getHeight()<=event.t
                                && (node.isRoot() || node.getParent().getHeight()>event.t)) {
                             
                             if (u<intervalArea) {
                                 recomb.setNode2(node);
                                 double tauEnd = Math.max(event.tau, tauStart) + u;
                                 recomb.setHeight2(popFunc.getInverseIntensity(tauEnd));
                                 break;
                             } else
                                 u -= intervalArea;
                         }
                     }
                     break;
                 } else
                     u -= intervalArea*event.lineages;
             }
         }
 
     }
     
     @Override
     public void initStateNodes() throws Exception { }
 
     @Override
     public void getInitialisedStateNodes(List<StateNode> stateNodes) {
         stateNodes.add(this);
     }
     
 }
