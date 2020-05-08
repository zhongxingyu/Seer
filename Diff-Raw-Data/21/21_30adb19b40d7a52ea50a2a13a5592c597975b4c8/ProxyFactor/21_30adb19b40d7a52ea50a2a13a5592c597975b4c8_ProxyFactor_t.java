 /*
  * Software License Agreement (BSD License)
  *
  * Copyright 2014 Marc Pujol <mpujol@iiia.csic.es>.
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
 package es.csic.iiia.maxsum.factors;
 
 import java.util.List;
 import java.util.Map;
 
 import es.csic.iiia.maxsum.CommunicationAdapter;
 import es.csic.iiia.maxsum.Factor;
 import es.csic.iiia.maxsum.MaxOperator;
 
 /**
  * Skeletal implementation of a factor that relays (possibly modified) messages into/out of
  * another wrapped factor.
  *
  * @author Marc Pujol <mpujol@iiia.csic.es>
  * @param <T> Type of the factor's identity.
  */
 public abstract class ProxyFactor<T> implements Factor<T> {
 
     private final CommunicationAdapter<T> proxyAdapter = new ProxyAdapter();
 
     private Factor<T> innerFactor;
 
     private CommunicationAdapter<T> communicationAdapter;
 
     /**
      * Build a new proxy factor that relays messages to/from the given inner factor.
      *
      * @param innerFactor
      */
     public ProxyFactor(Factor<T> innerFactor) {
         this.innerFactor = innerFactor;
 
         // Inherit the inner factor's communication adapter if it has one
         CommunicationAdapter<T> adapter = innerFactor.getCommunicationAdapter();
         if (adapter != null) {
             communicationAdapter = adapter;
         }
 
         innerFactor.setCommunicationAdapter(proxyAdapter);
     }
 
     /**
      * Get the internal factor we are proxying to/from.
      *
      * @return the factor we are proxying to/from.
      */
     public Factor<T> getInnerFactor() {
         return innerFactor;
     }
 
     @Override
     public void setIdentity(T identity) {
         innerFactor.setIdentity(identity);
     }
 
     @Override
     public T getIdentity() {
         return innerFactor.getIdentity();
     }
 
     @Override
     public MaxOperator getMaxOperator() {
         return innerFactor.getMaxOperator();
     }
 
     @Override
     public double getMessage(T neighbor) {
         return innerFactor.getMessage(neighbor);
     }
 
     @Override
     public void setMaxOperator(MaxOperator maxOperator) {
         innerFactor.setMaxOperator(maxOperator);
     }
 
     @Override
     public void setCommunicationAdapter(CommunicationAdapter<T> manager) {
         communicationAdapter = manager;
     }
 
     @Override
     public CommunicationAdapter<T> getCommunicationAdapter() {
         return communicationAdapter;
     }
 
     @Override
     public void addNeighbor(T factor) {
         innerFactor.addNeighbor(factor);
        receive(0, factor);
     }
 
     @Override
     public boolean removeNeighbor(T factor) {
         return innerFactor.removeNeighbor(factor);
     }
 
     @Override
     public List<T> getNeighbors() {
         return innerFactor.getNeighbors();
     }
 
     @Override
     public void clearNeighbors() {
         innerFactor.clearNeighbors();
     }
 
     @Override
     public void receive(double message, T sender) {
         innerFactor.receive(message, sender);
     }
 
     @Override
     public void send(double message, T recipient) {
         communicationAdapter.send(message, innerFactor.getIdentity(), recipient);
     }
 
     @Override
     public double evaluate(Map<T, Boolean> values) {
         return innerFactor.evaluate(values);
     }
 
     @Override
     public long run() {
         return innerFactor.run();
     }
 
     private class ProxyAdapter implements CommunicationAdapter<T> {
 
         @Override
         public void send(double message, T sender, T recipient) {
             ProxyFactor.this.send(message, recipient);
         }
 
     }
 
 }
