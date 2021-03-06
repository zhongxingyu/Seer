 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.webservices.rest.web.resource;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import org.openmrs.Concept;
 import org.openmrs.ConceptSearchResult;
 import org.openmrs.annotation.Handler;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.webservices.rest.SimpleObject;
 import org.openmrs.module.webservices.rest.web.ConversionUtil;
 import org.openmrs.module.webservices.rest.web.RequestContext;
 import org.openmrs.module.webservices.rest.web.RestConstants;
 import org.openmrs.module.webservices.rest.web.annotation.Resource;
 import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
 import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
 import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
 import org.openmrs.module.webservices.rest.web.representation.Representation;
 import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
 import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
 import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
 import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
 import org.openmrs.module.webservices.rest.web.response.ResponseException;
 import org.openmrs.util.LocaleUtility;
 
 /**
  * {@link Resource} for {@link Concept}, supporting standard CRUD operations
  */
 @Resource("concept")
 @Handler(supports = Concept.class, order = 0)
 public class ConceptResource extends DelegatingCrudResource<Concept> {
 	
 	/**
 	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
 	 */
 	@Override
 	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
 		if (rep instanceof RefRepresentation) {
 			DelegatingResourceDescription description = new DelegatingResourceDescription();
 			description.addProperty("uuid");
 			description.addProperty("display", "displayString", Representation.DEFAULT);
 			description.addSelfLink();
 			return description;
 		} else if (rep instanceof DefaultRepresentation) {
 			DelegatingResourceDescription description = new DelegatingResourceDescription();
 			description.addProperty("uuid");
 			description.addProperty("name", Representation.DEFAULT);
 			description.addProperty("description", Representation.DEFAULT);
 			description.addProperty("datatype", Representation.REF);
 			description.addProperty("conceptClass", Representation.REF);
 			description.addProperty("set");
 			description.addProperty("version");
 			description.addProperty("retired");
 			
 			description.addProperty("names", Representation.REF);
 			description.addProperty("descriptions", Representation.REF);
 			
 			//description.addProperty("answers", Representation.REF);  add as subresource
 			//description.addProperty("conceptSets", Representation.REF);  add as subresource
 			//description.addProperty("conceptMappings", Representation.REF);  add as subresource
 			
 			description.addSelfLink();
 			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
 			return description;
 		} else if (rep instanceof FullRepresentation) {
 			DelegatingResourceDescription description = new DelegatingResourceDescription();
 			description.addProperty("uuid");
 			description.addProperty("name", Representation.DEFAULT);
 			description.addProperty("description", Representation.DEFAULT);
 			description.addProperty("datatype", Representation.REF);
 			description.addProperty("conceptClass", Representation.REF);
 			description.addProperty("set");
 			description.addProperty("version");
 			
 			description.addProperty("names", Representation.DEFAULT);
 			description.addProperty("descriptions", Representation.DEFAULT);
 			
 			//description.addProperty("answers", Representation.DEFAULT);  add as subresource
 			//description.addProperty("conceptSets", Representation.DEFAULT);  add as subresource
 			//description.addProperty("conceptMappings", Representation.DEFAULT);  add as subresource
 			description.addProperty("auditInfo", findMethod("getAuditInfo"));
 			description.addSelfLink();
 			return description;
 		}
 		return null;
 	}
 	
 	/**
 	 * Must put this here because we cannot extend {@link MetadataDelegatingCrudResource} 
 	 * 
 	 * @param concept the delegate concept
 	 * @return audit information
 	 * @throws Exception
 	 */
 	public SimpleObject getAuditInfo(Concept concept) throws Exception {
 		SimpleObject ret = new SimpleObject();
 		ret.put("creator", ConversionUtil.getPropertyWithRepresentation(concept, "creator", Representation.REF));
 		ret.put("dateCreated", ConversionUtil.convertToRepresentation(concept.getDateCreated(), Representation.DEFAULT));
 		ret.put("retiredBy", ConversionUtil.getPropertyWithRepresentation(concept, "retiredBy", Representation.REF));
 		ret.put("dateRetired", ConversionUtil.convertToRepresentation(concept.getDateRetired(), Representation.DEFAULT));
 		ret.put("retireReason", ConversionUtil.convertToRepresentation(concept.getRetireReason(), Representation.DEFAULT));
 		ret.put("changedBy", ConversionUtil.getPropertyWithRepresentation(concept, "changedBy", Representation.REF));
 		ret.put("dateChanged", ConversionUtil.convertToRepresentation(concept.getDateChanged(), Representation.DEFAULT));
 		return ret;
 	}
 	
 	/**
 	 * @see DelegatingCrudResource#newDelegate()
 	 */
 	@Override
 	public Concept newDelegate() {
 		return new Concept();
 	}
 	
 	/**
 	 * @see DelegatingCrudResource#save(java.lang.Object)
 	 */
 	@Override
 	public Concept save(Concept c) {
 		return Context.getConceptService().saveConcept(c);
 	}
 	
 	/**
 	 * Fetches a concept by uuid, if no match is found, it tries to look up one with a matching name
 	 * with the assumption that the passed parameter is a concept name
 	 * 
 	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
 	 */
 	@Override
 	public Concept getByUniqueId(String uuidOrName) {
 		Concept concept = Context.getConceptService().getConceptByUuid(uuidOrName);
 		//We assume the caller was fetching by name
 		if (concept == null)
 			concept = Context.getConceptService().getConceptByName(uuidOrName); // NOT using getConcept here because that also searches on conceptId
 			
 		return concept;
 	}
 	
 	/**
 	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
 	 *      org.openmrs.module.webservices.rest.web.RequestContext)
 	 */
 	@Override
 	public void purge(Concept concept, RequestContext context) throws ResponseException {
 		if (concept == null)
 			return;
 		Context.getConceptService().purgeConcept(concept);
 	}
 	
 	/**
 	 * This does not include retired concepts
 	 * 
 	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
 	 */
 	@Override
 	protected List<Concept> doGetAll(RequestContext context) {
 		return Context.getConceptService().getAllConcepts(null, true, false);
 	}
 	
 	/**
 	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(java.lang.String,
 	 *      org.openmrs.module.webservices.rest.web.RequestContext)
 	 */
 	@Override
 	protected AlreadyPaged<Concept> doSearch(String query, RequestContext context) {
 		ConceptService service = Context.getConceptService();
 		
 		List<ConceptSearchResult> searchResults;
 		
 		// get the user's locales...and then convert that from a set to a list
 		List<Locale> locales = new ArrayList<Locale>(LocaleUtility.getLocalesInOrder());
 		
		searchResults = service.getConcepts(query, locales, false, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
		    Collections.EMPTY_LIST, Collections.EMPTY_LIST, null, context.getStartIndex(), context.getLimit());
 		
 		// convert search results into list of concepts
 		List<Concept> results = new ArrayList<Concept>(searchResults.size());
 		for (ConceptSearchResult csr : searchResults) {
 			results.add(csr.getConcept());
 		}
 		
		Integer count = service.getCountOfConcepts(query, locales, false, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
		    Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
 		boolean hasMore = count > context.getStartIndex() + context.getLimit();
 		
 		return new AlreadyPaged<Concept>(context, results, hasMore);
 	}
 	
 	@Override
 	protected void delete(Concept c, String reason, RequestContext context) throws ResponseException {
 		if (c.isRetired()) {
 			// since DELETE should be idempotent, we return success here
 			return;
 		}
 		Context.getConceptService().retireConcept(c, reason);
 	}
 }
