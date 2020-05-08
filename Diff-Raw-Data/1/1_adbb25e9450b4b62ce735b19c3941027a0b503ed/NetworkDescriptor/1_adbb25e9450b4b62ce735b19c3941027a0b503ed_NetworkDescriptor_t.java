 /*
  * Copyright (c) 2006-2007, AIOTrade Computing Co. and Contributors
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  *  o Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer. 
  *    
  *  o Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution. 
  *    
  *  o Neither the name of AIOTrade Computing Co. nor the names of 
  *    its contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  *    
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.aiotrade.lib.neuralnetwork.core.descriptor;
 
 import org.aiotrade.lib.neuralnetwork.core.model.Network;
 import org.aiotrade.lib.neuralnetwork.datasource.DataSource;
 import java.util.List;
 import org.aiotrade.lib.util.Argument;
 import org.aiotrade.lib.util.Descriptor;
 
 /**
  * @author Caoyuan Deng
  */
 
 public abstract class NetworkDescriptor implements Descriptor {
     
     protected DataSource dataSource;
     
     
     private Argument arg;
     
     protected NetworkDescriptor() {
     }
     
     public abstract int getNLayers();
     
     public abstract List<? extends LayerDescriptor> getLayerDescriptors();
     
     public Argument getArg() {
         return arg;
     }
     
     public void setArg(Argument arg) {
         this.arg = arg;
     }
     
     /**
      * A factory of configured and ready to train neural networks.
      *
      * @return configured network.
      */
     public Network createServiceInstance() {
         Network networkInstance = null;
         
         try {
             networkInstance = (Network)getServiceClass().newInstance();
             
             if (networkInstance != null) {
                 networkInstance.init(this);
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         
         
         return networkInstance;
     }
     
     public abstract Class getServiceClass();
     
     
     public void setServiceClass(Class clazz) {
     }
     
     public DataSource getDataSource() {
         return this.dataSource;
     }
     
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
     
    @Override
     public NetworkDescriptor clone() throws CloneNotSupportedException {
         /** 
          * @TODO 
          */
         return (NetworkDescriptor)super.clone();
     }
     
 }
