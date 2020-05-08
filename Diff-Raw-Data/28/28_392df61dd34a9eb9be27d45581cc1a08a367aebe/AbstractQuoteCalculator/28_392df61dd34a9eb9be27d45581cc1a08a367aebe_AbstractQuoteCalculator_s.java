 package de.zib.gndms.logic.model.gorfx;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
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
 
 
 import de.zib.gndms.common.model.gorfx.types.Order;
 import de.zib.gndms.common.model.gorfx.types.Quote;
 import de.zib.gndms.kit.security.CredentialProvider;
 import de.zib.gndms.kit.security.RequiresCredentialProvider;
 import de.zib.gndms.kit.configlet.ConfigletProvider;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.neomodel.common.Dao;
 
 import javax.inject.Inject;
 import java.util.List;
 
 
 /**
  * @author try ma ik jo rr a zib
  * @date 14.02.11  16:43
  * @brief A task flow calculator interface.
  *
  * This will be merged with the orq calculator of the old GNDMS.
  *
  * It computes quotes and checks the satisfiability of of a order.
  */
 public abstract class AbstractQuoteCalculator<M extends Order>
     implements RequiresCredentialProvider {
 
     private ConfigletProvider configletProvider;
     private Quote preferredOfferExecution;
     private DelegatingOrder<M> order;
 
     private String offerType;
     private CredentialProvider credentialProvider;
     private Dao dao;
 
 
     public AbstractQuoteCalculator() {
     }
 
 
     /**
      * @brief Computes quotes fo the order.
      * here the computation of the required offer should be performed
      * @return A list of quotes for the order.
      * @throws Exception Thrown exception depends on the implementation.
      */
     public abstract List<Quote> createQuotes() throws Exception;
 
     /**
      * @brief Computes Quotes for an order, pays respect to a
      * preferred taskflow execution.
      *
      * @param order The preferred taskflow execution.
      *
      * @return A list of quotes for the order.
      * @throws Exception see parameterless version for details.
      */
     public List<Quote> createQuotes( Quote order ) throws Exception {
 
         setPreferredQuote( order );
         return createQuotes();
     }
 
 
     public Quote getPreferredQuote() {
         return preferredOfferExecution;
     }
 
     /**
      * @brief Sets the order
      *
      * @param preferredQuote The order of the task flow.
      */
     public void setPreferredQuote( Quote preferredQuote ) {
         preferredOfferExecution = preferredQuote;
     }
 
     /**
      * @brief Validates if a order is satisfiable.
      *
      * @return \c true if that's the case.
      */
     public abstract boolean validate( );
 
     public M getOrderBean() {
         return order.getOrderBean();
     }
 
 
     public DelegatingOrder<M> getOrder() {
         return order;
     }
 
 
     public ConfigletProvider getConfigletProvider() {
         return configletProvider;
     }
 
 
     public void setConfigletProvider( ConfigletProvider configletProvider ) {
         this.configletProvider = configletProvider;
     }
 
     @Inject
     public void setCredentialProvider( CredentialProvider cp ) {
         credentialProvider = cp;
     }
 
 
     public CredentialProvider getCredentialProvider() {
         return credentialProvider;
     }
 
 
     public Dao getDao() {
         return dao;
     }
 
 
     public void setDao( Dao dao ) {
         this.dao = dao;
     }
 
 
     public void setOrder( DelegatingOrder<M> order ) {
         this.order = order;
     }
 }
