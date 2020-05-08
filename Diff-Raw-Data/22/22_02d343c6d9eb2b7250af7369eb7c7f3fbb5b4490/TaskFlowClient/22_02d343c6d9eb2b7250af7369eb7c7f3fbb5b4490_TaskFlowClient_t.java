 package de.zib.gndms.gndmc.gorfx;
 
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
 
 import de.zib.gndms.common.GORFX.service.TaskFlowService;
 import de.zib.gndms.common.model.gorfx.types.*;
 import de.zib.gndms.common.rest.Facets;
 import de.zib.gndms.common.rest.Specifier;
 import de.zib.gndms.gndmc.AbstractClient;
 import org.springframework.http.ResponseEntity;
 
 import java.util.List;
 
 /**
  * The task flow client implementing the task flow service.
  * 
  * @author Ulrike Golas
  *
  * @see de.zib.gndms.common.GORFX.service.TaskFlowService for details.
  */
 public class TaskFlowClient extends AbstractClient implements TaskFlowService {
 
 	/**
 	 * The constructor.
 	 */
 	public TaskFlowClient() {
 	}
 
     /**
 	 * The constructor.
 	 * 
 	 * @param serviceURL The base url of the grid.
 	 */
 	public TaskFlowClient(final String serviceURL) {
 		this.setServiceURL( serviceURL );
 	}
 
 	@Override
 	public final ResponseEntity<Facets> getFacets(final String type, final String id, final String dn) {
         logger.debug( "calling getFacets to: " + getServiceURL() + "/gorfx/_" + type + "/_" + id );
 		return unifiedGet(Facets.class, getServiceURL() + "/gorfx/_" + type + "/_"
 				+ id, dn);
 	}
 
 	@Override
 	public final ResponseEntity< Integer > deleteTaskflow(final String type, final String id, final String dn, final String wid) {
 		return unifiedDelete( getServiceURL() + "/gorfx/_" + type + "/_" + id, dn, wid);
 	}
 
 
 	@Override
 	public final ResponseEntity<Order> getOrder(final String type, final String id, final String dn, final String wid) {
 		return unifiedGet(Order.class, getServiceURL() + "/gorfx/_" + type + "/_"
 				+ id + "/order", dn, wid);
 	}
 
 	@Override
	public final ResponseEntity< Integer > setOrder(final String type, final String id,
 			final Order orq, final String dn, final String wid) {
		return unifiedPost( Integer.class, orq, getServiceURL() + "/gorfx/_" + type + "/_"
 				+ id + "/order", dn, wid);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public final ResponseEntity<List<Specifier<Quote>>> getQuotes(final String type, final String id,
 			final String dn, final String wid) {
 		return ( ResponseEntity<List<Specifier<Quote>>> ) (Object) unifiedGet(List.class,
 				getServiceURL() + "/gorfx/_" + type + "/_" + id + "/quote", dn, wid);
 	}
 
 	@Override
 	public final ResponseEntity<Integer> setQuote( final String type, final String id,
                                                    final Quote cont,
                                                    final String dn, final String wid ) {
 
 		return unifiedPost(Integer.class, cont, getServiceURL() + "/gorfx/_" + type + "/_"
 				+ id + "/quote", dn, wid);
 	}
 
 
 	public final ResponseEntity<Quote> getQuote(final String type, final String id, final int idx,
 			final String dn, final String wid) {
 		return unifiedGet(Quote.class, getServiceURL() + "/gorfx/_" + type + "/_" + id
 				+ "/quote/_" + idx, dn, wid);
 	}
 
 
 	public final ResponseEntity< Integer > deleteQuotes(final String type, final String id, final int idx,
 			final String dn, final String wid) {
 		return unifiedDelete( getServiceURL() + "/gorfx/_" + type + "/_" + id
 				+ "/quote/_" + idx, dn, wid);
 	}
 
 
     @SuppressWarnings("unchecked")
 	public final ResponseEntity<Specifier<Facets>> getTask( final String type, final String id, final String dn, final String wid ) {
 		return ( ResponseEntity<Specifier<Facets>> ) (Object) unifiedGet(Specifier.class, getServiceURL() + "/gorfx/_" + type + "/_"
 				+ id + "/task", dn, wid);
 	}
 
 
     @SuppressWarnings("unchecked")
 	public final ResponseEntity<Specifier<Facets>> createTask( final String type, final String id,
                                                                final Integer quoteId,
                                                                final String dn, final String wid ) {
 		return ( ResponseEntity<Specifier<Facets>> ) (Object) unifiedPut(Specifier.class, quoteId, getServiceURL() + "/gorfx/_" + type
 				+ "/_" + id + "/task", dn, wid);
 	}
 
 
 	public final ResponseEntity<TaskFlowStatus> getStatus(final String type, final String id,
 			final String dn, final String wid) {
 		return unifiedGet(TaskFlowStatus.class, getServiceURL() + "/gorfx/_" + type
 				+ "/_" + id + "/status", dn, wid);
 	}
 
 
     @SuppressWarnings("unchecked")
 	public final ResponseEntity<Specifier<TaskResult>> getResult( final String type, final String id,
                                                                   final String dn, final String wid ) {
 		return ( ResponseEntity<Specifier<TaskResult>> ) (Object) unifiedGet(Specifier.class, getServiceURL() + "/gorfx/_" + type
 				+ "/_" + id + "/result", dn, wid);
 	}
 
 
     @SuppressWarnings("unchecked")
 	public final ResponseEntity<TaskFlowFailure> getErrors( final String type, final String id,
                                                             final String dn, final String wid ) {
 		return ( ResponseEntity<TaskFlowFailure> ) (Object) unifiedGet(TaskFlowFailure.class,
                 getServiceURL() + "/gorfx/_" + type
 				+ "/_" + id + "/errors", dn, wid);
 	}
 
 }
