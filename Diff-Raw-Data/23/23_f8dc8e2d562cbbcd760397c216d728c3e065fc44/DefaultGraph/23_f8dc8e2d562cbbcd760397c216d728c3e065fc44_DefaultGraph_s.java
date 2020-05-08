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
 package es.csic.iiia.dcop.mp;
 
 import es.csic.iiia.dcop.mp.AbstractNode.Modes;
 import es.csic.iiia.dcop.util.BytesSent;
 import es.csic.iiia.dcop.util.ConstraintChecks;
 import es.csic.iiia.dcop.util.MemoryTracker;
 
 /**
  *
  * @param <N>
  * @param <E>
  * @param <R>
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public abstract class DefaultGraph<N extends Node, E extends Edge, R extends Results>
         extends AbstractGraph<N, E, R> {
 
     private Modes mode = Modes.GRAPH;
 
     public void reportIteration(int i) {};
     public void reportStart() {};
     public void reportResults(R results) {};
 
     public R run(int maxIterations) {
         reportStart();
 
         int iter = 0;
         reportIteration(iter++);
         initialize();
         R results = getResults();
 
         if (getMode() == Modes.TREE_DOWN) {
             getNodes().get(getRoot()).setRoot();
             getNodes().get(getRoot()).run();
         }
         if (getMode() == Modes.TREE_UP) {
             getNodes().get(getRoot()).setRoot();
         }
 
         // Now for the "real meat":
         boolean converged = false;
         for (; iter < maxIterations && !converged; iter++) {
 
             // Tick for synchronous graphs
             reportIteration(iter);
             for (Edge e : getEdges()) {
                 e.tick();
             }
 
             // Clique operation
             long mcc = 0, tcc = 0, mbytes = 0, tbytes = 0, mmem = 0;
             for (Node n : getNodes()) {
                 if (n.isUpdated()) {
                     ConstraintChecks.addTracker(n);
                     BytesSent.addTracker(n);
                     MemoryTracker.addTracker(n);
                     n.run();
                     long cc = ConstraintChecks.removeTracker(n);
                     long bytes = BytesSent.removeTracker(n);
                     long mem = MemoryTracker.removeTracker(n);
                     tcc += cc;
                     tbytes += bytes;
                     mcc = Math.max(mcc, cc);
                     mbytes = Math.max(mbytes, bytes);
                     mmem = Math.max(mmem, mem);
                 }
             }
             results.addCycle(mcc, tcc, mbytes, tbytes, mmem);
 
             // Check for convergence
             converged = true;
             for (Node n : getNodes()) {
                 if (!n.isFinished()) {
                     converged = false;
                     break;
                 }
             }

         }
 
         // Just in case...
         if (iter == maxIterations) {
            System.err.println("WARNING: Maximum number of iterations reached (i=" + iter + ")");
         }
 
         // Result collection
         end();
 
         reportResults(results);
         return results;
     }
 
     /**
      * @return the mode
      */
     public Modes getMode() {
         return mode;
     }
 
     /**
      * @param mode the mode to set
      */
     public void setMode(Modes mode) {
         this.mode = mode;
     }
 }
