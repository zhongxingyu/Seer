 /**
  * JBoss, Home of Professional Open Source
  * Copyright Red Hat, Inc., and individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jboss.aerogear.aerodoc;
 
 import java.util.List;
 
 import org.jboss.aerogear.aerodoc.config.CustomMediaTypeResponder;
 import org.jboss.aerogear.aerodoc.exceptions.HttpSecurityException;
 import org.jboss.aerogear.aerodoc.model.Lead;
 import org.jboss.aerogear.aerodoc.model.PushConfig;
 import org.jboss.aerogear.aerodoc.model.SaleAgent;
 import org.jboss.aerogear.aerodoc.rest.Error;
 import org.jboss.aerogear.aerodoc.rest.LeadEndpoint;
 import org.jboss.aerogear.aerodoc.rest.Login;
 import org.jboss.aerogear.aerodoc.rest.PushConfigEndpoint;
 import org.jboss.aerogear.aerodoc.rest.Register;
 import org.jboss.aerogear.aerodoc.rest.SaleAgentEndpoint;
 import org.jboss.aerogear.controller.router.AbstractRoutingModule;
 import org.jboss.aerogear.controller.router.RequestMethod;
 import org.jboss.aerogear.security.exception.AeroGearSecurityException;
 
 public class Routes extends AbstractRoutingModule {
 
 	private static final String[] JSON_MEDIA_TYPES = new String[] {
 			"application/json; charset=utf-8", "application/json" };
 
 	@Override
 	public void configuration() throws Exception {
 		route().on(AeroGearSecurityException.class).produces(JSON)
 				.to(Error.class).index(param(HttpSecurityException.class));
 		route().from("/login").on(RequestMethod.POST)
 				.consumes(JSON_MEDIA_TYPES)
 				.produces(CustomMediaTypeResponder.CUSTOM_MEDIA_TYPE)
 				.to(Login.class).login(param(SaleAgent.class));
 
 		route().from("/logout").on(RequestMethod.POST)
 				.consumes(JSON_MEDIA_TYPES)
 				.produces(CustomMediaTypeResponder.CUSTOM_MEDIA_TYPE)
 				.to(Login.class).logout();
 
 		route().from("/register").on(RequestMethod.POST)
 				.consumes(JSON_MEDIA_TYPES)
 				.produces(CustomMediaTypeResponder.CUSTOM_MEDIA_TYPE)
 				.to(Register.class).register(param(SaleAgent.class));
 
 		route().from("/leads").roles("simple").on(RequestMethod.GET)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(LeadEndpoint.class).listAll();
 
 		route().from("/leads").roles("simple").on(RequestMethod.POST)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(LeadEndpoint.class).create(param(Lead.class));
 
 		route().from("/leads/{id}").roles("simple").on(RequestMethod.DELETE)
 				.consumes(JSON).produces(JSON).to(LeadEndpoint.class)
 				.deleteById(param("id", Long.class));
 
 		route().from("/leads/{id}").roles("simple").on(RequestMethod.GET)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(LeadEndpoint.class).findById(param("id", Long.class));
 
 		route().from("/leads/{id}").roles("simple").on(RequestMethod.PUT)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(LeadEndpoint.class)
 				.update(param("id", Long.class), param(Lead.class));
 
 		route().from("/saleagents").roles("simple").on(RequestMethod.GET)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(SaleAgentEndpoint.class).listAll();
 
 		route().from("/saleagents").roles("simple").on(RequestMethod.POST)
 				.consumes(JSON).produces(JSON).to(SaleAgentEndpoint.class)
 				.create(param(SaleAgent.class));
 
 		route().from("/sendleads/{id}").on(RequestMethod.POST)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(LeadEndpoint.class)
 				.sendLead(param("id", Long.class), param(List.class));
 
 		route().from("/searchAgents").roles("simple").on(RequestMethod.GET)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(SaleAgentEndpoint.class)
 				.listByCriteria(param("status", ""), param("location", ""));
 
 		route().from("/saleagents/{id}").roles("simple")
 				.on(RequestMethod.DELETE).consumes(JSON).produces(JSON)
 				.to(SaleAgentEndpoint.class)
 				.deleteById(param("id", Long.class));
 
 		route().from("/saleagents/{id}").roles("simple").on(RequestMethod.GET)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(SaleAgentEndpoint.class).findById(param("id", Long.class));
 
 		route().from("/saleagents/{id}").roles("simple").on(RequestMethod.PUT)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(SaleAgentEndpoint.class)
 				.update(param("id"), param(SaleAgent.class));
 
 		route().from("/pushconfig/{id}").roles("admin")
 				.on(RequestMethod.DELETE).consumes(JSON).produces(JSON)
 				.to(PushConfigEndpoint.class)
 				.deleteById(param("id", Long.class));
 
 		route().from("/pushconfig/{id}").roles("admin").on(RequestMethod.GET)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(PushConfigEndpoint.class).findById(param("id", Long.class));
 
 		route().from("/pushconfig/{id}").roles("admin").on(RequestMethod.PUT)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(PushConfigEndpoint.class)
 				.update(param("id", Long.class), param(PushConfig.class));
 
 		route().from("/pushconfig").roles("admin").on(RequestMethod.GET)
 				.consumes(JSON_MEDIA_TYPES).produces(JSON)
 				.to(PushConfigEndpoint.class).listAll();
 
 		route().from("/pushconfig").roles("admin").on(RequestMethod.POST)
 				.consumes(JSON).produces(JSON).to(PushConfigEndpoint.class)
 				.create(param(PushConfig.class));
 
 	}
 
 }
