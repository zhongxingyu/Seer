 package de.zib.gndms.gndmc.test.gorfx;
 
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
 
 import com.sun.jndi.toolkit.url.Uri;
 import de.zib.gndms.common.kit.application.AbstractApplication;
 import de.zib.gndms.common.logic.action.ActionMeta;
 import de.zib.gndms.common.logic.config.ConfigMeta;
 import de.zib.gndms.common.model.gorfx.types.*;
 import de.zib.gndms.common.rest.Facet;
 import de.zib.gndms.common.rest.Facets;
 import de.zib.gndms.common.rest.GNDMSResponseHeader;
 import de.zib.gndms.common.rest.Specifier;
 
 import de.zib.gndms.gndmc.gorfx.FullGORFXClient;
 import de.zib.gndms.gndmc.gorfx.TaskFlowClient;
 import de.zib.gndms.common.kit.security.SetupSSL;
 import de.zib.gndms.gndmc.test.KeyStoreTest;
 import org.kohsuke.args4j.Option;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 
 import java.util.List;
 import java.util.UUID;
 
 /**
  * @author try ma ik jo rr a zib
  * @date 10.02.11 15:57
  * @brief A test run for the gorfx client.
  */
 public class GORFXClientMain extends AbstractApplication {
 
 	@Option(name = "-uri", required = true, usage = "URL of gndms-Endpoint", metaVar = "URI")
 	protected String gndmsEpUrl;
 	@Option(name = "-dn", required = true, usage = "DN")
 	protected String dn;
     @Option(name = "-ksp", required = false, usage = "key store passwd" )
     protected String ksp = null;
     @Option(name = "-tsp", required = false, usage = "trust store passwd" )
     protected String tsp = null;
 
 
 	protected final String wid = UUID.randomUUID().toString();
 
 	public static void main(String[] args) throws Exception {
 
 		GORFXClientMain cnt = new GORFXClientMain();
 		cnt.run(args);
 		System.exit(0);
 	}
 
 	@Override
     public void run() throws Exception {
 
 		ApplicationContext context = new ClassPathXmlApplicationContext(
 				"classpath:META-INF/client-context.xml");
 		FullGORFXClient gorfxClient = (FullGORFXClient) context
 				.getAutowireCapableBeanFactory().createBean(
 						FullGORFXClient.class,
 						AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
 		gorfxClient.setServiceURL( gndmsEpUrl );
 
 		if (gorfxClient.getRestTemplate() == null)
 			throw new IllegalStateException("restTemplate is null");
 
 		System.out.println("connecting to: \"" + gndmsEpUrl + "\"");
         Uri gorfxUrl = new Uri( gndmsEpUrl );
         if( "https".equals( gorfxUrl.getScheme() ) ) {
             System.out.println( "https specified. enabling SSL" );
             SetupSSL setupSSL = KeyStoreTest.initSSL( ksp, ksp, tsp );
             setupSSL.setupDefaultSSLContext( ksp );
         }
 
 		System.out.println("requesting facets");
 		ResponseEntity<Facets> res = gorfxClient.listAvailableFacets(dn);
 		System.out.println("StatusCode: " + res.getStatusCode());
 		showHeader(res.getHeaders());
 		System.out.println("Body: ");
 		Facets f = res.getBody();
 		for (Facet fa : f.getFacets()) {
 			System.out.println(fa.getName() + " " + fa.getUrl());
 		}
 		
 
 	}
 
 	private void executeTaskFlow(FullGORFXClient gorfxClient, String type, Order order) {
 		System.out.println("Testing workflow: Execute Task Flow of type " + type);
 		System.out.println("Step 1: requesting facets and find facet config");
 		ResponseEntity<Facets> res = gorfxClient.listAvailableFacets(dn);
 		Facets facets = res.getBody();
 		Facet f = facets.findFacet("taskflows");
 		if (f == null) {
 			System.out.println("Taskflows facet not found");
 			return;
 		}
 		System.out.println(f.getName() + " " + f.getUrl());
 
 		System.out.println("Step 2: requesting task flow types");
 		ResponseEntity<List<String>> res1 = gorfxClient.listTaskFlows(dn);
 		if (!res1.getBody().contains(type)) {
 			System.out.println("Taskflow type " + type + " not found");
 			return;
 		}
 		System.out.println("Taskflow type " + type + " exists");
 
 		System.out.println("Step 3: creating task flow");
 		ResponseEntity<Specifier<Facets>> res2 = gorfxClient.createTaskFlow( type, order, dn, wid, new GNDMSResponseHeader() );
 		
 		// optionally: change order, select quote?
 		
 		// define with res2.getBody().getUriMap() ??
 		String id = null;
 		getTaskFlowStatus(id, type, wid);
 
 	}
 
 	private void executeConfigAction(FullGORFXClient gorfxClient, String action) {
 		System.out.println("Testing workflow: Execute Config Action" + action);
 
 		System.out.println("Step 1: requesting facets and find facet config");
 		ResponseEntity<Facets> res = gorfxClient.listAvailableFacets(dn);
 		Facets facets = res.getBody();
 		Facet f = facets.findFacet("config");
 		if (f == null) {
 			System.out.println("Config facet not found");
 			return;
 		}
 		System.out.println(f.getName() + " " + f.getUrl());
 		
 		System.out.println("Step 2: requesting actions");
 		ResponseEntity<List<String>> res2 = gorfxClient.listConfigActions(dn);
 		if (!res2.getBody().contains(action)) {
 			System.out.println("Action" + action + "unknown");
 			return;
 		}
 		
 		System.out.println("Step 3: requesting description of action" + action);
 		ResponseEntity<ConfigMeta> res3 = gorfxClient.getConfigActionInfo(
 				action, dn);
 		System.out.println(res3.getBody().getDescription());
 		
 		System.out.println("Step 4: executing action" + action);
 		// String args = res3.getBody.???
 		String args = null;
 		ResponseEntity<String> res4 = gorfxClient.callConfigAction(action,
 				args, dn);
 		System.out.println(res4.getBody());
 	}
 
 	private void executeBatchAction(FullGORFXClient gorfxClient, String action) {
 		System.out.println("Testing workflow: Execute Batch Action" + action);
 		
 		System.out.println("Step 1: requesting facets and find facet batch");
 		ResponseEntity<Facets> res = gorfxClient.listAvailableFacets(dn);
 		Facets facets = res.getBody();
 		Facet f = facets.findFacet("batch");
 		if (f == null) {
 			System.out.println("Batch facet not found");
 			return;
 		}
 		System.out.println(f.getName() + " " + f.getUrl());
 		
 		System.out.println("Step 2: requesting actions");
 		ResponseEntity<List<String>> res2 = gorfxClient.listBatchActions(dn);
 		if (!res2.getBody().contains(action)) {
 			System.out.println("Action" + action + "unknown");
 			return;
 		}
 		
 		System.out.println("Step 3: requesting description of action" + action);
 		ResponseEntity<ActionMeta> res3 = gorfxClient.getBatchActionInfo(
 				action, dn);
 		System.out.println(res3.getBody().getDescription());
 		
 		System.out.println("Step 4: executing action" + action);
 		// String args = res3.getBody().???
 		String args = null;
 		ResponseEntity<Specifier> res4 = gorfxClient.callBatchAction(action,
 				args, dn);
 		System.out.println(res4.getBody().toString());
 		
 		System.out.println("Step 5: requesting task specifier");
 		// String id = res4.getBody().???
 		String batchId = null;
 		ResponseEntity<Specifier<Facets>> res5 = gorfxClient.getBatchAction(
 				action, batchId, dn);
 		System.out.println("Task URL" + res5.getBody().getUrl());
 		// define with res5.getBody().getUriMap() ??
 		String type = null;
 		String wid = null;
 		String id = null;
 
 		getTaskFlowStatus(id, type, wid);
 	}
 
 	private void getTaskFlowStatus(String id, String type, String wid) {
 		Facets facets;
 		Facet f;
 		ApplicationContext context = new ClassPathXmlApplicationContext(
 				"classpath:META-INF/client-context.xml");
 		TaskFlowClient tfClient = (TaskFlowClient) context
 				.getAutowireCapableBeanFactory().createBean(
 						TaskFlowClient.class,
 						AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
 		tfClient.setServiceURL( gndmsEpUrl );
 
 		System.out
 				.println("Step 6: requesting facets of task flow, retrieving status results");
 		ResponseEntity<Facets> res6 = tfClient.getFacets(type, id, dn);
 		// should define facets status, result, errors
 		facets = res6.getBody();
 		f = facets.findFacet("status");
 		if (f == null) {
 			System.out.println("Status facet not found");
 		} else {
 			System.out.println(f.getName() + " " + f.getUrl());
 			ResponseEntity<TaskFlowStatus> res7 = tfClient.getStatus(type, id,
 					dn, wid);
 			System.out.println(res7.getBody().toString());
 		}
 
 		f = facets.findFacet("result");
 		if (f == null) {
 			System.out.println("Result facet not found");
 		} else {
 			System.out.println(f.getName() + " " + f.getUrl());
 			ResponseEntity<Specifier<TaskResult>> res8 = tfClient.getResult(
 					type, id, dn, wid);
 			System.out.println(res8.getBody().toString());
 		}
 
 		f = facets.findFacet("errors");
 		if (f == null) {
 			System.out.println("Errors facet not found");
 		} else {
 			System.out.println(f.getName() + " " + f.getUrl());
			ResponseEntity<Specifier<TaskFailure>> res9 = tfClient.getErrors(type, id,
 					dn, wid);
 			if (res9.getStatusCode().equals( HttpStatus.OK )) {
 				System.out.println("Failure"
						+ res9.getBody().getPayload().getMessage());
 			} else {
 				System.out.println("No errors");
 
 			}
 		}
 	}
 
 	public static void showHeader(HttpHeaders head) {
 
 		GNDMSResponseHeader h = new GNDMSResponseHeader(head);
 
 		showList("parentURL", h.getParentURL());
 		showList("facetURL", h.getFacetURL());
 		showList("DN", h.getDN());
 		showList("WId", h.getWId());
 	}
 
 	public static void showList(String name, List<String> list) {
 
         StringBuilder sb = new StringBuilder();
 
         sb.append( name ).append( ": " );
 		if (list == null || list.size() == 0) {
 			sb.append("NIL\n");
 			return;
 		}
 
 		for (String s : list) {
 			sb.append(s);
 			sb.append(",");
 		}
 
 		sb.replace(sb.length() - 1, sb.length() - 1, "");
 
 		System.out.println(sb.toString());
 	}
 
 }
