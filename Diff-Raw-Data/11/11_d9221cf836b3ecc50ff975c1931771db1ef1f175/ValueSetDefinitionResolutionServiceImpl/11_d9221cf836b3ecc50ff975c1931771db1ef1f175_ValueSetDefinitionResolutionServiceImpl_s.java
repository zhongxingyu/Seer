 package edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.stereotype.Component;
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import edu.mayo.cts2.framework.core.client.Cts2RestClient;
 import edu.mayo.cts2.framework.core.timeout.Timeout;
 import edu.mayo.cts2.framework.model.association.AssociationDirectory;
 import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
 import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
 import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntrySummary;
 import edu.mayo.cts2.framework.model.command.Page;
 import edu.mayo.cts2.framework.model.command.ResolvedFilter;
 import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
 import edu.mayo.cts2.framework.model.core.CodeSystemReference;
 import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
 import edu.mayo.cts2.framework.model.core.ComponentReference;
 import edu.mayo.cts2.framework.model.core.DescriptionInCodeSystem;
 import edu.mayo.cts2.framework.model.core.EntityReference;
 import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
 import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
 import edu.mayo.cts2.framework.model.core.PredicateReference;
 import edu.mayo.cts2.framework.model.core.ScopedEntityName;
 import edu.mayo.cts2.framework.model.core.SortCriteria;
 import edu.mayo.cts2.framework.model.core.SortCriterion;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference;
 import edu.mayo.cts2.framework.model.core.ValueSetReference;
 import edu.mayo.cts2.framework.model.core.types.AssociationDirection;
 import edu.mayo.cts2.framework.model.core.types.CompleteDirectory;
 import edu.mayo.cts2.framework.model.core.types.SetOperator;
 import edu.mayo.cts2.framework.model.directory.DirectoryResult;
 import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
 import edu.mayo.cts2.framework.model.exception.UnspecifiedCts2Exception;
 import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
 import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
 import edu.mayo.cts2.framework.model.service.core.NameOrURI;
 import edu.mayo.cts2.framework.model.service.core.NameOrURIList;
 import edu.mayo.cts2.framework.model.service.core.Query;
 import edu.mayo.cts2.framework.model.service.exception.UnknownCodeSystemVersion;
 import edu.mayo.cts2.framework.model.service.exception.UnknownEntity;
 import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
 import edu.mayo.cts2.framework.model.valuesetdefinition.AssociatedEntitiesReference;
 import edu.mayo.cts2.framework.model.valuesetdefinition.CompleteCodeSystemReference;
 import edu.mayo.cts2.framework.model.valuesetdefinition.CompleteValueSetReference;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ExternalValueSetDefinition;
 import edu.mayo.cts2.framework.model.valuesetdefinition.PropertyQueryReference;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetHeader;
 import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry;
 import edu.mayo.cts2.framework.model.valuesetdefinition.types.LeafOrAll;
 import edu.mayo.cts2.framework.model.valuesetdefinition.types.TransitiveClosure;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ValueSetDefinitionSharedServiceBase;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility.ExceptionBuilder;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility.SetUtilities;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility.queryBuilders.AssociationQueryBuilder;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.CodeSystemVersionCatalogEntryAndHref;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.CustomURIAndEntityName;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.EntityReferenceAndHref;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.EntityReferenceResolver;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.EntityReferenceResolverComparator;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.ResolveReturn;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.ResultCache;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.SortCriterionComparator;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.ValueSetDefinitionEntryComparator;
 import edu.mayo.cts2.framework.service.command.restriction.ResolvedValueSetResolutionEntityRestrictions;
 import edu.mayo.cts2.framework.service.command.restriction.TaggedCodeSystemRestriction;
 import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
 import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
 import edu.mayo.cts2.framework.service.profile.association.AssociationQuery;
 import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
 
 @Component("valueSetDefinitionResolutionServiceImpl")
 public class ValueSetDefinitionResolutionServiceImpl extends ValueSetDefinitionSharedServiceBase implements ValueSetDefinitionResolutionService
 {
 	// TODO TEST cycle detection
 	// TODO TEST threading
 	// TODO TEST property query reference
 
 	/*
 	 * This is used to cache entire result objects, so that we can instantly answer a request for page 2 of a query,
 	 * as long as they didn't change any aspects of the query. Results time out automatically after 5 minutes.
 	 */
 	private Cache<Long, ResultCache> resultCache_ = CacheBuilder.newBuilder().concurrencyLevel(2).maximumSize(10).expireAfterWrite(5, TimeUnit.MINUTES).build();
 
 	/*
 	 * A thread pool used in the resolution of various entities - as those operations are usually handled by other services that
 	 * may be a slow network hop away.
 	 */
	private ExecutorService threadPool_ = new ThreadPoolExecutor(2, 15, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new ThreadFactory()
 	{
 		@Override
 		public Thread newThread(Runnable r)
 		{
 			Thread thread = new Thread(r);
 			thread.setDaemon(true);
			thread.setName("ValueSetDefinition-ResolveThread");
 			return thread;
 		}
 	});
 
 	@Override
 	public Set<PredicateReference> getKnownProperties()
 	{
 		return new HashSet<PredicateReference>();  //not sure what this is for - don't see any examples - I don't think it applies.
 	}
 
 	@Override
 	public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms()
 	{
 		HashSet<MatchAlgorithmReference> result = new HashSet<MatchAlgorithmReference>();
 		result.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
 		result.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference());
 		result.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference());
 		return result;
 	}
 
 	@Override
 	public Set<? extends ComponentReference> getSupportedSearchReferences()
 	{
 		HashSet<ComponentReference> result = new HashSet<ComponentReference>();
 		result.add(StandardModelAttributeReference.DESIGNATION.getComponentReference());
 		result.add(StandardModelAttributeReference.RESOURCE_NAME.getComponentReference());
 		return result;
 	}
 
 	@Override
 	public Set<? extends ComponentReference> getSupportedSortReferences()
 	{
 		HashSet<ComponentReference> sorts = new HashSet<ComponentReference>();
 		sorts.add(SupportedSorts.ALPHA_NUMERIC.asComponentReference());
 		sorts.add(SupportedSorts.ALPHABETIC.asComponentReference());
 		return sorts;
 	}
 
 	/**
 	 * Resolve definition as complete set.
 	 * 
 	 * @param definitionId the definition id
 	 * @param codeSystemVersions the code system versions to execute against
 	 * @param tag the tag (if any) of the code system versions to use
 	 * @param readContext the read context
 	 * @return the resolved value set
 	 */
 	@Override
 	public ResolvedValueSet resolveDefinitionAsCompleteSet(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions, NameOrURI tag,
 			ResolvedReadContext readContext)
 	{
 		logger_.debug("resolveDefinitionAsCompleteSet {}, {}, {}, {}", definitionId, codeSystemVersions, tag, readContext);
 		ResolvedValueSet resolvedValueSet = new ResolvedValueSet();
 		ResolveReturn rr = resolveHelper(definitionId, codeSystemVersions, tag, null, null, readContext, null);
 		resolvedValueSet.setEntry(rr.getItems());
 		resolvedValueSet.setResolutionInfo(rr.getHeader());
 		return resolvedValueSet;
 	}
 
 	/**
 	 * Resolve a {@link ValueSetDefinition} into a set of {@link EntitySynopsis} entries.
 	 * 
 	 * @param definitionId the definition id
 	 * @param codeSystemVersions the code system versions to execute against
 	 * @param tag the tag (if any) of the code system versions to use
 	 * @param query the query to filter the returned results
 	 * @param readContext the read context
 	 * @param page the page
 	 * @return the resolved value set result
 	 */
 	@Override
 	public ResolvedValueSetResult<URIAndEntityName> resolveDefinition(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions, NameOrURI tag,
 			SortCriteria sortCriteria, ResolvedReadContext readContext, Page page)
 	{
 		logger_.debug("resolveDefinition {}, {}, {}, {}, {}, {}", definitionId, codeSystemVersions, tag, sortCriteria, readContext, page);
 
 		ResolveReturn rr = resolveHelper(definitionId, codeSystemVersions, tag, null, sortCriteria, readContext, page);
 		return new ResolvedValueSetResult<URIAndEntityName>(rr.getHeader(), rr.getItems(), rr.isAtEnd());
 	}
 
 	/**
 	 * Resolve a {@link ValueSetDefinition} into a set of {@link EntityDirectoryEntry} entries.
 	 * 
 	 * @param definitionId the definition id
 	 * @param codeSystemVersions the code system versions to execute against
 	 * @param tag the tag (if any) of the code system versions to use
 	 * @param query the query to filter the returned results
 	 * @param readContext the read context
 	 * @param page the page
 	 * @return the directory result
 	 */
 	@Override
 	public ResolvedValueSetResult<EntityDirectoryEntry> resolveDefinitionAsEntityDirectory(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions,
 			NameOrURI tag, ResolvedValueSetResolutionEntityQuery query, SortCriteria sortCriteria, ResolvedReadContext readContext, Page page)
 	{
 		logger_.debug("resolveDefinitionAsEntityDirectory {}, {}, {}, {}, {}, {}, {}", definitionId, codeSystemVersions, tag, query, sortCriteria, readContext, page);
 		ResolveReturn rr = resolveHelper(definitionId, codeSystemVersions, tag, query, sortCriteria, readContext, page);
 		ResolvedValueSetResult<EntityDirectoryEntry> rvsr = new ResolvedValueSetResult<EntityDirectoryEntry>(rr.getHeader(), rr.getEntityDirectoryEntry(), rr.isAtEnd());
 		return rvsr;
 	}
 
 	private ResolveReturn resolveHelper(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions, final NameOrURI tag,
 			ResolvedValueSetResolutionEntityQuery query, SortCriteria sortCriteria, final ResolvedReadContext readContext, Page page)
 	{
 		logger_.debug("resolveHelper {}, {}, {}, {}, {}, {}, {}", definitionId, codeSystemVersions, tag, query, sortCriteria, readContext, page);
 
 		Long cacheKey = makeCacheKey(definitionId, codeSystemVersions, tag, query, sortCriteria, readContext);
 		ResultCache rc = resultCache_.getIfPresent(cacheKey);
 		if (rc != null)
 		{
 			return new ResolveReturn(rc, page);
 		}
 		
 		ArrayList<SupportedSorts> resolvedSortCriteria = new ArrayList<>();
 
 		if (sortCriteria != null)
 		{
 			Collections.sort(sortCriteria.getEntryAsReference(), new SortCriterionComparator());
 			//verify the sort criteria is valid
 			for (SortCriterion sc : sortCriteria.getEntryAsReference())
 			{
 				boolean found = false;
 				for (SupportedSorts ss : SupportedSorts.values())
 				{
 					if (ss.getNiceName().equals(sc.getSortElement().getSpecialReference()))
 					{
 						resolvedSortCriteria.add(ss);
 						found = true;
 						break;
 					}
 				}
 				if (!found)
 				{
 					throw new UnspecifiedCts2Exception("Unsupported sort algorithm '" + sc.getSortElement().getSpecialReference() + "'");
 				}
 			}
 		}
 		
 		//Lookup all of the codeSystemVersions
 		final HashMap<String, CodeSystemVersionCatalogEntryAndHref> resolvedCodeSystemVersions = new HashMap<>();
 		if (codeSystemVersions != null)
 		{
 			for (NameOrURI nou : codeSystemVersions)
 			{
 				try
 				{
 					CodeSystemVersionCatalogEntryAndHref csvce = utilities_.lookupCodeSystemVersion(nou, null, readContext);
 					resolvedCodeSystemVersions.put(csvce.getCodeSystemVersionCatalogEntry().getAbout(), csvce);
 				}
 				catch (Exception e)
 				{
 					throw ExceptionBuilder.buildUnknownCodeSystemVersion("The requested code system version '" + nou.getName() + ":" + nou.getUri() +
 							"' could not be resolved");
 				}
 			}
 		}
 
 		HashSet<EntityReferenceResolver> pendingResult = new HashSet<>();
 		ArrayList<ResolvedValueSetHeader> includesResolvedValueSets = new ArrayList<>();
 		HashSet<CodeSystemVersionReference> resolvedUsingCodeSystems = new HashSet<>();
 
 		LocalIdValueSetDefinition localIdValueSetDefinition = utilities_.lookupValueSetDefinition(definitionId, readContext);
 		ValueSetDefinition vsd = localIdValueSetDefinition.getResource();
 		String valueSetDefinitionName = localIdValueSetDefinition.getLocalID();
 
 		ArrayList<ArrayList<EntityReferenceResolver>> tempResults = new ArrayList<>();
 
 		Collections.sort(vsd.getEntryAsReference(), new ValueSetDefinitionEntryComparator());
 
 		for (ValueSetDefinitionEntry valueSetDefinitionEntry : vsd.getEntryAsReference())
 		{
 			if (Timeout.isTimeLimitExceeded())
 			{
 				throw new RuntimeException("Notified of timeout");
 			}
 			Object entry = valueSetDefinitionEntry.getChoiceValue();
 			ArrayList<EntityReferenceResolver> itemList = new ArrayList<>();
 
 			if (entry instanceof AssociatedEntitiesReference)
 			{
 				AssociatedEntitiesReference aer = (AssociatedEntitiesReference) entry;
 				List<EntityReferenceResolver> items = resolveAssociations(aer, resolvedCodeSystemVersions, tag, readContext);
 				itemList.addAll(items);
 			}
 			else if (entry instanceof PropertyQueryReference)
 			{
 				PropertyQueryReference pqr = (PropertyQueryReference) entry;
 				
 				CodeSystemVersionCatalogEntry csv = pickBestCodeSystemVersion(pqr.getCodeSystemVersion(), pqr.getCodeSystem(), tag, resolvedCodeSystemVersions, 
 						readContext).getCodeSystemVersionCatalogEntry();
 
 				if (pqr.getFilter() == null)
 				{
 					throw new UnspecifiedCts2Exception("A property filter must be provided within a PropertyQueryReference");
 				}
 				ResolvedFilter resolvedFilter = new ResolvedFilter();
 				resolvedFilter.setMatchAlgorithmReference(pqr.getFilter().getMatchAlgorithm());
 				resolvedFilter.setMatchValue(pqr.getFilter().getMatchValue());
 				resolvedFilter.setComponentReference(pqr.getFilter());
 
 				Iterator<EntityReferenceResolver> entities = utilities_.getEntities(csv.getCodeSystemVersionName(), csv.getVersionOf().getContent(),
 						csv.getEntityDescriptions(), resolvedFilter, readContext);
 				while (entities.hasNext())
 				{
 					itemList.add(entities.next());
 				}
 			}
 			else if (entry instanceof CompleteValueSetReference)
 			{
 				CompleteValueSetReference completeValueSetRef = (CompleteValueSetReference) entry;
 				ValueSetDefinitionReadId nestedValueSetDefinitionReadId;
 				if (completeValueSetRef.getValueSetDefinition() == null)
 				{
 					if (completeValueSetRef.getValueSet() == null)
 					{
 						throw ExceptionBuilder.buildUnknownValueSetDefinition("No valid parameters specified to enable resolving the Complete ValueSet reference");
 					}
 					ValueSetCatalogEntry vsce = utilities_.lookupValueSetByAny(completeValueSetRef.getValueSet().getUri(), completeValueSetRef.getValueSet().getContent(), 
 								completeValueSetRef.getValueSet().getHref(), readContext);
 					
 					ValueSetDefinitionReference vsdr = vsce.getCurrentDefinition();
 					if (vsdr == null)
 					{
 						throw new UnspecifiedCts2Exception("No Valueset Definition set to CURRENT for " + vsce);
 					}
 					nestedValueSetDefinitionReadId = new ValueSetDefinitionReadId(vsdr.getValueSetDefinition().getUri());
 					nestedValueSetDefinitionReadId.setName(vsdr.getValueSetDefinition().getContent());
 				}
 				else
 				{
 					nestedValueSetDefinitionReadId = new ValueSetDefinitionReadId(completeValueSetRef.getValueSetDefinition().getUri());
 					nestedValueSetDefinitionReadId.setName(completeValueSetRef.getValueSetDefinition().getContent());
 				}
 
 				NameOrURI nestedValueSet = new NameOrURI();
 				if (completeValueSetRef.getValueSet() != null)
 				{
 					nestedValueSet.setName(completeValueSetRef.getValueSet().getContent());
 					nestedValueSet.setUri(completeValueSetRef.getValueSet().getUri());
 				}
 				nestedValueSetDefinitionReadId.setValueSet(nestedValueSet);
 
 				if (completeValueSetRef.getReferenceCodeSystemVersion() != null)
 				{
 					for (CodeSystemVersionCatalogEntryAndHref csvce : resolvedCodeSystemVersions.values())
 					{
 						if (csvce.getCodeSystemVersionCatalogEntry().getVersionOf().getUri()
 								.equals(completeValueSetRef.getReferenceCodeSystemVersion().getCodeSystem().getUri()))
 						{
 							//Make sure the versions match
 							if (!csvce.getAsCodeSystemVersionReference().getVersion().getUri()
 									.equals(completeValueSetRef.getReferenceCodeSystemVersion().getVersion().getUri()))
 							{
 								throw new UnspecifiedCts2Exception("The Code System Version specified in a CompleteValueSet Reference '" 
 										+ "'" + completeValueSetRef.getReferenceCodeSystemVersion() + "' conflicts with the version specified in the codeSystemVersions "
 										+ "list '" + csvce.getAsCodeSystemVersionReference());
 							}
 						}
 					}
 					
 				}
 
 				Page subPage = new Page();
 				
 				ResolvedValueSetHeader rvsh = null;
 				while (true)
 				{
 					ResolveReturn rr = resolveHelper(nestedValueSetDefinitionReadId, codeSystemVersions, tag, null, null, readContext, subPage);
 					if (rvsh == null)
 					{
 						rvsh = rr.getHeader();
 					}
 					for (EntityDirectoryEntry item : rr.getEntityDirectoryEntry())
 					{
 						itemList.add(new EntityReferenceResolver(item));
 					}
 					if (rr.isAtEnd())
 					{
 						break;
 					}
 					else
 					{
 						subPage.setPage(subPage.getEnd() + 1);
 					}
 				}				
 				includesResolvedValueSets.add(rvsh);
 			}
 			else if (entry instanceof CompleteCodeSystemReference)
 			{
 				CompleteCodeSystemReference completeCodeSystemRef = (CompleteCodeSystemReference) entry;
 				CodeSystemVersionCatalogEntry csv = pickBestCodeSystemVersion(completeCodeSystemRef.getCodeSystemVersion(), completeCodeSystemRef.getCodeSystem(), 
 						tag, resolvedCodeSystemVersions, readContext).getCodeSystemVersionCatalogEntry();
 				Iterator<EntityReferenceResolver> entities = utilities_.getEntities(csv.getCodeSystemVersionName(), csv.getVersionOf().getContent(),
 						csv.getEntityDescriptions(), null, readContext);
 				while (entities.hasNext())
 				{
 					itemList.add(entities.next());
 				}
 			}
 			else if (entry instanceof SpecificEntityList)
 			{
 				SpecificEntityList sel = (SpecificEntityList) entry;
 
 				// Resolving these entities could be slow - so kick them into the thread pool.
 				ArrayList<Callable<EntityReferenceAndHref>> tasks = new ArrayList<>(sel.getReferencedEntityAsReference().size());
 				{
 					for (final URIAndEntityName entity : sel.getReferencedEntityAsReference())
 					{
 						tasks.add(new Callable<EntityReferenceAndHref>()
 						{
 							@Override
 							public EntityReferenceAndHref call() throws Exception
 							{
 								EntityReferenceAndHref er = utilities_.resolveEntityReference(entity, resolvedCodeSystemVersions, tag, readContext);
 								if (er == null)
 								{
 									logger_.error("The entity '" + entity.toString() + "' from a SpecificEntityList could not be resolved");
 									throw ExceptionBuilder.buildUnknownEntity("The entity '" + entity.toString() + "' from a SpecificEntityList could not be resolved");
 								}
 								return er;
 							}
 						});
 					}
 				}
 
 				try
 				{
 					// This automatically waits for everything to finish.
 					List<Future<EntityReferenceAndHref>> results = threadPool_.invokeAll(tasks);
 					for (Future<EntityReferenceAndHref> futureEntityReference : results)
 					{
 						itemList.add(new EntityReferenceResolver(futureEntityReference.get()));
 					}
 				}
 				catch (ExecutionException e)
 				{
 					if (e.getCause() instanceof RuntimeException)
 					{
 						throw (RuntimeException) e.getCause();
 					}
 					throw new RuntimeException("Unexpected", e.getCause());
 				}
 				catch (InterruptedException e)
 				{
 					if (Timeout.isTimeLimitExceeded())
 					{
 						throw new RuntimeException("Notified of timeout");
 					}
 					throw new RuntimeException("Unexpected Interrupt", e);
 				}
 			}
 			else if (entry instanceof ExternalValueSetDefinition)
 			{
 				throw new UnspecifiedCts2Exception("This service does not know how to resolve an ExternalValueSetDefinition");
 			}
 			tempResults.add(itemList);
 		}
 
 		for (int i = 0; i < vsd.getEntryAsReference().size(); i++)
 		{
 			new SetUtilities<EntityReferenceResolver>().handleSet(vsd.getEntry(i).getOperator(), pendingResult, tempResults.get(i));
 		}
 		
 		if (Timeout.isTimeLimitExceeded())
 		{
 			throw new RuntimeException("Notified of timeout");
 		}
 
 		if (!resolveMissingEntityDesignations(pendingResult, readContext))
 		{
 			throw ExceptionBuilder.buildUnknownEntity("Failed to resolve all Entities - see log for details");
 		}
 		
 		for (EntityReferenceResolver ere : pendingResult)
 		{
 			for (DescriptionInCodeSystem description : ere.getEntityReference().getKnownEntityDescriptionAsReference())
 			{
 				resolvedUsingCodeSystems.add(description.getDescribingCodeSystemVersion());
 			}
 		}
 
 		ResolvedValueSetHeader header = buildResolvedValueSetHeader(vsd.getDefinedValueSet().getContent(), vsd.getDefinedValueSet().getUri(), valueSetDefinitionName,
 				vsd.getAbout(), includesResolvedValueSets, resolvedUsingCodeSystems);
 		
 		//Change the set to a list so we can sort
 		List<EntityReferenceResolver> result = new ArrayList<EntityReferenceResolver>(pendingResult);
 		
 		result = processPostResolveQueryFilter(result, query, readContext);
 
 		if (resolvedSortCriteria.size() > 0)
 		{
 			Collections.sort(result, new EntityReferenceResolverComparator(resolvedSortCriteria));
 		}
 
 		ResultCache resultCache = new ResultCache(result, header);
 
 		resultCache_.put(cacheKey, resultCache);
 
 		return new ResolveReturn(resultCache, page);
 	}
 	
 	private List<EntityReferenceResolver> processPostResolveQueryFilter(List<EntityReferenceResolver> incoming, ResolvedValueSetResolutionEntityQuery query, 
 			ResolvedReadContext readContext)
 	{
 		if (query == null)
 		{
 			return incoming;
 		}
 		
 		List<EntityReferenceResolver> filteredResult = new ArrayList<>();
 		boolean filterApplied = false;
 		
 		//handle the filter and restrictions first, as those are just talking about the incoming set.
 		if (query.getFilterComponent() != null && query.getFilterComponent().size() > 0)
 		{
 			filteredResult = passesFilters((filterApplied ? filteredResult : incoming), query.getFilterComponent());
 			filterApplied = true;
 		}
 		
 		if (query.getResolvedValueSetResolutionEntityRestrictions() != null && 
 				((query.getResolvedValueSetResolutionEntityRestrictions().getEntities() != null 
 					&& query.getResolvedValueSetResolutionEntityRestrictions().getEntities().size() > 0)
 				|| query.getResolvedValueSetResolutionEntityRestrictions().getCodeSystemVersion() != null 
 				|| query.getResolvedValueSetResolutionEntityRestrictions().getTaggedCodeSystem() != null))
 		{
 			filteredResult = passesEntityRestrictions((filterApplied ? filteredResult : incoming), query.getResolvedValueSetResolutionEntityRestrictions(), 
 					readContext);
 			filterApplied = true;
 		}
 		
 		//Then handle the set logic with the other directory result
 		if (query.getQuery() != null)
 		{
 			//TODO BUG - there is an API issue here - discussed with Kevin - it doesn't make any sense for Query to contain multiple directories.
 			//This implementation will treat the first directory for list operations to be the one resolved by this code (incoming) - and the second one for list operations
 			//will be the one in Query.getQuery6Choice().  The value in Query.getQuery6Choice2() will be completely ignored.
 			List<EntityReferenceResolver> right = resolveQuery(query.getQuery(), true);
 			List<EntityReferenceResolver> left = (filterApplied ? filteredResult : incoming);
 			
 			resolveSetLogic(left, right, query.getQuery().getSetOperation());
 			filteredResult = resolveQueryFilterLogic(left, query.getQuery().getFilterComponent(), query.getQuery().getMatchAlgorithm(), query.getQuery().getMatchValue());
 			filterApplied = true;
 		}
 
 		return filterApplied ? filteredResult : incoming;
 	}
 	
 	private List<EntityReferenceResolver> resolveQuery(Query query, boolean ignoreQueryChoice2)
 	{
 		if (ignoreQueryChoice2)
 		{
 			if (query.getQuery6Choice() != null)
 			{
 				return resolveQueryChoice(query.getQuery6Choice().getQuery1(), query.getQuery6Choice().getDirectoryUri1());
 				//Don't process the filters here, as they have to be done after the set logic.
 			}
 			else
 			{
 				throw new UnspecifiedCts2Exception("Query6Choice should be populated if the Query is provided");
 			}
 		}
 		else
 		{
 			List<EntityReferenceResolver> left = new ArrayList<>();
 			List<EntityReferenceResolver> right = new ArrayList<>();
 			
 			if (query.getQuery6Choice() != null)
 			{
 				left = resolveQueryChoice(query.getQuery6Choice().getQuery1(), query.getQuery6Choice().getDirectoryUri1());
 			}
 			if (query.getQuery6Choice2() != null)
 			{
 				right = resolveQueryChoice(query.getQuery6Choice2().getQuery2(), query.getQuery6Choice2().getDirectoryUri2());
 			}
 			
 			List<EntityReferenceResolver> result = resolveSetLogic(left, right, query.getSetOperation());
 			return resolveQueryFilterLogic(result, query.getFilterComponent(), query.getMatchAlgorithm(), query.getMatchValue());
 		}
 	}
 	
 	private List<EntityReferenceResolver> resolveQueryFilterLogic(List<EntityReferenceResolver> incoming, NameOrURIList filterComponent, 
 			NameOrURI matchAlgorithm, String matchValue)
 	{
 		if (filterComponent!= null && matchAlgorithm != null && matchValue != null)
 		{
 			MatchAlgorithmReference matchAlgorithmRef = null;
 			for (MatchAlgorithmReference mar : getSupportedMatchAlgorithms())
 			{
 				if (matchAlgorithm.getName().equals(mar.getContent()))
 				{
 					matchAlgorithmRef = mar;
 					break;
 				}
 			}
 			if (matchAlgorithmRef == null)
 			{
 				throw ExceptionBuilder.buildUnsupportedMatchAlgorithm(matchAlgorithm.getName());
 			}
 			
 			HashSet<ResolvedFilter> resolvedFilters = new HashSet<>();
 			
 			Set<? extends ComponentReference> cr = getSupportedSearchReferences();
 			
 			for (NameOrURI nou : filterComponent.getEntryAsReference())
 			{
 				boolean matched = false;
 				for (ComponentReference crItem : cr)
 				{
 					if (crItem.getAttributeReference().equals(nou.getName()) || crItem.getAttributeReference().equals(nou.getUri()))
 					{
 						ResolvedFilter rf = new ResolvedFilter();
 						rf.setMatchValue(matchValue);
 						rf.setMatchAlgorithmReference(matchAlgorithmRef);
 						rf.setComponentReference(crItem);
 						resolvedFilters.add(rf);
 						matched = true;
 						break;
 					}
 				}
 				if (!matched)
 				{
 					ExceptionBuilder.buildUnsupportedNameOrURI("The Component Reference '" + nou + "' is not supported for filtering");
 				}
 			}
 			return passesFilters(incoming, resolvedFilters);
 		}
 		return incoming;
 	}
 	
 	private List<EntityReferenceResolver> resolveQueryChoice(Query query, String directoryURI)
 	{
 		if (query != null)
 		{
 			return resolveQuery(query, false);
 		}
 		else if (StringUtils.isNotBlank(directoryURI))
 		{
 			ArrayList<EntityReferenceResolver> result = new ArrayList<EntityReferenceResolver>();
 			ArrayList<EntityReferenceAndHref> items = utilities_.resolveEntityDirectory(directoryURI);
 			
 			for (EntityReferenceAndHref er : items)
 			{
 				EntityDirectoryEntry ede =  new EntityDirectoryEntry();
 				ede.setAbout(er.getEntityReference().getAbout());
 				ede.setHref(er.getHref());
 				ede.setKnownEntityDescription(er.getEntityReference().getKnownEntityDescriptionAsReference());
 				ede.setName(er.getEntityReference().getName());
 				
 				result.add(new EntityReferenceResolver(ede));
 			}
 			return result;
 		}
 		return new ArrayList<EntityReferenceResolver>();
 	}
 	
 	private List<EntityReferenceResolver> resolveSetLogic(List<EntityReferenceResolver> left, List<EntityReferenceResolver> right, SetOperator so)
 	{
 		SetUtilities<EntityReferenceResolver> su = new SetUtilities<>();
 		su.handleSet(so, left, right);
 		return left;
 	}
 
 	private List<EntityReferenceResolver> passesEntityRestrictions(List<EntityReferenceResolver> incoming, ResolvedValueSetResolutionEntityRestrictions restrictions,
 			ResolvedReadContext readContext)
 	{
 		List<EntityReferenceResolver> pendingResults = new ArrayList<EntityReferenceResolver>();
 		
 		if (restrictions.getEntities() != null && restrictions.getEntities().size() > 0)
 		{
 			HashSet<String> allowedEntities = new HashSet<String>();
 			for (EntityNameOrURI entity : restrictions.getEntities())
 			{
 				if (StringUtils.isNotEmpty(entity.getUri()))
 				{
 					allowedEntities.add(entity.getUri());
 				}
 				if (entity.getEntityName() != null)
 				{
 					//User may provide a namespace, or they may not.
 					if (StringUtils.isNotEmpty(entity.getEntityName().getNamespace()))
 					{
 						allowedEntities.add(entity.getEntityName().getNamespace() + ":" + entity.getEntityName().getName());
 					}
 					else
 					{
 						allowedEntities.add(entity.getEntityName().getName());
 					}
 				}
 			}
 			
 			
 			for (EntityReferenceResolver ere : incoming)
 			{
 				if (allowedEntities.contains(ere.getEntityReference().getAbout()) 
 						|| allowedEntities.contains(ere.getEntityReference().getName().getNamespace() + ":" + ere.getEntityReference().getName().getName())
 						|| allowedEntities.contains(ere.getEntityReference().getName().getName()))
 				{
 					pendingResults.add(ere);
 				}
 			}
 		}
 		else 
 		{
 			pendingResults = incoming;
 		}
 		
 		if (restrictions.getCodeSystemVersion() != null)
 		{
 			removeEntitiesThatDontMatchCodeSystemVersion(pendingResults, restrictions.getCodeSystemVersion(), readContext);
 		}
 		
 		if (restrictions.getTaggedCodeSystem() != null)
 		{
 			TaggedCodeSystemRestriction tcsr = restrictions.getTaggedCodeSystem();
 			
 			CodeSystemReference csr = new CodeSystemReference();
 			csr.setContent(tcsr.getCodeSystem().getName());
 			csr.setUri(tcsr.getCodeSystem().getUri());
 			
 			NameOrURI tag = new NameOrURI();
 			tag.setName(tcsr.getTag());
 			
 			CodeSystemVersionCatalogEntrySummary csvces = utilities_.lookupCodeSystemVersionByTag(csr, tag, null, false, readContext);
 			
 			if (csvces == null)
 			{
 				throw ExceptionBuilder.buildUnknownCodeSystemVersion("The TaggedCodeSystemRestriction contains an unresolvable code system version");
 			}
 			NameOrURI codeSystemVersion = new NameOrURI();
 			codeSystemVersion.setName(csvces.getCodeSystemVersionName());
 			codeSystemVersion.setUri(csvces.getAbout());
 			
 			removeEntitiesThatDontMatchCodeSystemVersion(pendingResults, codeSystemVersion, readContext);
 		}
 		return pendingResults;
 	}
 	
 	private void removeEntitiesThatDontMatchCodeSystemVersion(List<EntityReferenceResolver> pendingResults, NameOrURI codeSystemVersion, 
 			ResolvedReadContext readContext)
 	{
 		//Require that each entity I have is resolvable in the requested code system version...
 		Iterator<EntityReferenceResolver> it = pendingResults.iterator();
 		while (it.hasNext())
 		{
 			//Resolve this entity again (the resolution we have may not contain all describing code system versions)
 			EntityReferenceResolver ere = it.next();
 			EntityReferenceAndHref er =  utilities_.resolveEntityReference(ere.getEntity(), readContext);
 			if (er == null)
 			{
 				it.remove();
 				continue;
 			}
 			else
 			{
 				boolean ok = false;
 				for (DescriptionInCodeSystem d : er.getEntityReference().getKnownEntityDescription())
 				{
 					if (codeSystemVersion.getName().equals(d.getDescribingCodeSystemVersion().getVersion().getContent())
 							|| codeSystemVersion.getUri().equals(d.getDescribingCodeSystemVersion().getVersion().getUri()))
 					{
 						ok = true;
 						break;
 					}
 				}
 				if (!ok)
 				{
 					//None of the resolved returned code system versions matched the requested... toss it.
 					it.remove();
 				}
 			}
 		}
 	}
 	
 	private List<EntityReferenceResolver> passesFilters(List<EntityReferenceResolver> incoming, Set<ResolvedFilter> filters)
 	{
 		ArrayList<EntityReferenceResolver> result = new ArrayList<>();
 		
 		for (EntityReferenceResolver ere : incoming)
 		{
 			boolean passed = true;
 			for (ResolvedFilter filter : filters)
 			{
 				if (!passesFilter(ere, filter))
 				{
 					passed = false;
 					break;
 				}
 			}
 			if (passed)
 			{
 				result.add(ere);
 			}
 		}
 		return result;
 	}
 	
 	private boolean passesFilter(EntityReferenceResolver ere, ResolvedFilter filter)
 	{
 		ArrayList<String> matchTargets = new ArrayList<String>();
 		if (filter.getComponentReference().equals(StandardModelAttributeReference.DESIGNATION.getComponentReference()))
 		{
 			matchTargets.add(ere.getEntity().getDesignation());
 			for (DescriptionInCodeSystem d : ere.getEntityReference().getKnownEntityDescriptionAsReference())
 			{
 				matchTargets.add(d.getDesignation());
 			}
 		}
 		else if (filter.getComponentReference().equals(StandardModelAttributeReference.RESOURCE_NAME.getComponentReference()))
 		{
 			matchTargets.add(ere.getEntity().getName());
 		}
 		else
 		{
 			throw new UnspecifiedCts2Exception("Unexpected case while evaluating filters");
 		}
 		
 		for (String s : matchTargets)
 		{
 			if (filter.getMatchAlgorithmReference().equals(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference()))
 			{
 				if (s.equals(filter.getMatchValue()))
 				{
 					return true;
 				}
 			}
 			else if (filter.getMatchAlgorithmReference().equals(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference()))
 			{
 				if (s.contains(filter.getMatchValue()))
 				{
 					return true;
 				}
 			}
 			else if (filter.getMatchAlgorithmReference().equals(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference()))
 			{
 				if (s.startsWith(filter.getMatchValue()))
 				{
 					return true;
 				}
 			}
 			else
 			{
 				throw new UnspecifiedCts2Exception("Unexpected case while evaluating filters");
 			}
 		}
 		return false;
 	}
 	
 	private CodeSystemVersionCatalogEntryAndHref pickBestCodeSystemVersion(CodeSystemVersionReference codeSystemVersion, CodeSystemReference codeSystem, 
 			NameOrURI tag, HashMap<String, CodeSystemVersionCatalogEntryAndHref> resolvedCodeSystemVersions, ResolvedReadContext readContext)
 	{
 		CodeSystemVersionCatalogEntryAndHref csv = null;
 		
 		if (codeSystemVersion != null && codeSystemVersion.getVersion() != null)
 		{
 			//We use the code system specified in the ValueSet, if one is provided
 			NameOrURI nameOrURI = new NameOrURI();
 			nameOrURI.setName(codeSystemVersion.getVersion().getContent());
 			nameOrURI.setUri(codeSystemVersion.getVersion().getUri());
 			csv = utilities_.lookupCodeSystemVersion(nameOrURI, codeSystemVersion.getVersion().getHref(), readContext);
 		}
 		else
 		{
 			//If the requested code system matches one of the passed in CodeSystemVersion references, use that.
 			csv = resolvedCodeSystemVersions.get(codeSystem.getUri());
 			
 			if (csv == null)
 			{
 				//still null - no match, pick a CodeSystemVersion that matches the requested tag, or failing that - CURRENT.
 				CodeSystemVersionCatalogEntrySummary csvces = utilities_.lookupCodeSystemVersionByTag(codeSystem, tag, "CURRENT", true, readContext);
 				if (csvces != null)
 				{
 					NameOrURI nameOrURI = new NameOrURI();
 					nameOrURI.setUri(csvces.getAbout());
 					csv = utilities_.lookupCodeSystemVersion(nameOrURI, csvces.getHref(), readContext);
 				}
 			}
 		}
 		
 		
 		if (csv == null)
 		{
 			throw ExceptionBuilder.buildUnknownCodeSystemVersion("Could not resolve the specified or requested CodeSystemVersion for " + codeSystem);
 		}
 		return csv;
 	}
 
 	private ResolvedValueSetHeader buildResolvedValueSetHeader(String valueSetName, String valueSetURI, String valueSetDefinitionName, String valueSetDefinitionURI,
 			List<ResolvedValueSetHeader> includesResolvedValueSets, Collection<CodeSystemVersionReference> resolvedUsingCodeSystems)
 	{
 		ResolvedValueSetHeader resolvedValueSetHeader = new ResolvedValueSetHeader();
 
 		ValueSetDefinitionReference valueSetDefinitionReference = new ValueSetDefinitionReference();
 
 		ValueSetReference vsr = new ValueSetReference(valueSetName);
 		vsr.setHref(utilities_.getUrlConstructor().createValueSetUrl(valueSetName));
 		vsr.setUri(valueSetURI);
 		valueSetDefinitionReference.setValueSet(vsr);
 
 		NameAndMeaningReference valueSetDefinition = new NameAndMeaningReference(valueSetDefinitionName);
 		valueSetDefinition.setUri(valueSetDefinitionURI);
 		valueSetDefinition.setHref(utilities_.getUrlConstructor().createValueSetDefinitionUrl(valueSetName, valueSetDefinitionName));
 		valueSetDefinitionReference.setValueSetDefinition(valueSetDefinition);
 
 		resolvedValueSetHeader.setResolutionOf(valueSetDefinitionReference);
 
 		resolvedValueSetHeader.setIncludesResolvedValueSet(includesResolvedValueSets);
 		resolvedValueSetHeader.getResolvedUsingCodeSystemAsReference().addAll(resolvedUsingCodeSystems);
 
 		return resolvedValueSetHeader;
 	}
 
 	/**
 	 * Returns true if all were resolved without error, false otherwise.
 	 * @return
 	 */
 	private boolean resolveMissingEntityDesignations(Collection<EntityReferenceResolver> items, final ResolvedReadContext readContext) throws UnknownEntity
 	{
 		ArrayList<Callable<Boolean>> tasks = new ArrayList<>();
 		
 		// Throw these entity resolution jobs at the thread pool, as they take a relatively long time against remote services.
 		for (final EntityReferenceResolver item : items)
 		{
 			tasks.add(new Callable<Boolean>()
 			{
 				@Override
 				public Boolean call() throws Exception
 				{
 					try
 					{
 						item.resolveEntity(readContext, utilities_);
 						return true;
 					}
 					catch (UnknownEntity e1)
 					{
 						throw e1;
 					}
 					catch (Exception e)
 					{
 						logger_.error("Unexpected error resolving designation for Entity " + item, e);
 						return false;
 					}
 				}
 			});
 		}
 		try
 		{
 			// Wait for all of our resolve tasks to finish.
 			List<Future<Boolean>> results = threadPool_.invokeAll(tasks);
 			try
 			{
 				for (Future<Boolean> result : results)
 				{
 					if (!result.get())
 					{
 						return false;
 					}
 				}
 			}
 			catch (ExecutionException e)
 			{
 				if (e.getCause() instanceof UnknownEntity)
 				{
 					throw (UnknownEntity)e.getCause();
 				}
 				logger_.error("Unexpected error", e);
 				return false;
 			}
 			return true;
 		}
 		catch (InterruptedException e)
 		{
 			if (Timeout.isTimeLimitExceeded())
 			{
 				throw new RuntimeException("Notified of timeout");
 			}
 			logger_.warn("Unexpected interrupt", e);
 			throw new RuntimeException("Unexpected interrupt", e);
 		}
 	}
 
 	private List<EntityReferenceResolver> resolveAssociations(AssociatedEntitiesReference aer, HashMap<String, CodeSystemVersionCatalogEntryAndHref> resolvedCodeSystemVersions, 
 			NameOrURI tag, ResolvedReadContext readContext)
 	{
 		CodeSystemVersionCatalogEntryAndHref associationVersionCodeSystemInfo = pickBestCodeSystemVersion(aer.getCodeSystemVersion(), aer.getCodeSystem(), tag, 
 				resolvedCodeSystemVersions, readContext);
 
 		URIAndEntityName referencedEntity = aer.getReferencedEntity();
 		if (aer.getReferencedEntity() == null)
 		{
 			throw new UnspecifiedCts2Exception("Referenced Entity is required");
 		}
 		else if (StringUtils.isBlank(referencedEntity.getName()))
 		{
 			//This code should never actually run... but leave it in, since its here.
 			EntityReference er = utilities_.resolveEntityReference(aer.getReferencedEntity(), readContext).getEntityReference();
 			referencedEntity.setName(er.getName().getName());
 			referencedEntity.setNamespace(er.getName().getNamespace());
 		}
 
 		//Kevin said we didn't need to bother resolving the predicate
 
 		HashSet<CustomURIAndEntityName> resultHolder = new HashSet<CustomURIAndEntityName>();
 
 		processLevel(aer.getReferencedEntity(), aer.getDirection(), associationVersionCodeSystemInfo, aer.getTransitivity(), aer.getPredicate().getUri(),
 				LeafOrAll.LEAF_ONLY == aer.getLeafOnly(), resultHolder, readContext);
 
 		ArrayList<EntityReferenceResolver> results = new ArrayList<>(resultHolder.size());
 		
 
 		for (CustomURIAndEntityName entity : resultHolder)
 		{
 			results.add(new EntityReferenceResolver(entity.getEntity(), associationVersionCodeSystemInfo.getAsCodeSystemVersionReference()));
 		}
 
 		return results;
 	}
 
 	private void processLevel(URIAndEntityName entity, AssociationDirection direction, CodeSystemVersionCatalogEntryAndHref associationCodeSystemVersion,
 			TransitiveClosure transitivity, String predicateURI, boolean leafOnly, HashSet<CustomURIAndEntityName> resultHolder, ResolvedReadContext readContext)
 	{
 		AssociationQueryService aqs = utilities_.getLocalAssociationQueryService();
 		ArrayList<CustomURIAndEntityName> thisLevelResults = new ArrayList<>();
 
 		// CodeSystemVersionReference entityCodeSystemVersion = getCodeSystemVersionForEntity(entity, readContext);
 
 		boolean localServiceFail = false;
 		
 		boolean sourceToTarget;
 		if (AssociationDirection.SOURCE_TO_TARGET == direction)
 		{
 			sourceToTarget = true;
 		}
 		else if (AssociationDirection.TARGET_TO_SOURCE == direction)
 		{
 			sourceToTarget = false;
 		}
 		else
 		{
 			throw new UnspecifiedCts2Exception("Unexpected direction parameter - " + direction);
 		}
 
 		
 		if (aqs != null)
 		{
 			try
 			{
 				Page page = new Page();
 				page.setMaxToReturn(500);
 				page.setPage(0);
 	
 				AssociationQuery query = AssociationQueryBuilder.build(readContext);
 	
 				NameOrURI codeSystemVersion = new NameOrURI();
 				codeSystemVersion.setUri(associationCodeSystemVersion.getCodeSystemVersionCatalogEntry().getAbout());
 				query.getRestrictions().setCodeSystemVersion(codeSystemVersion);
 	
 				EntityNameOrURI predicate = new EntityNameOrURI();
 				predicate.setUri(predicateURI);
 				query.getRestrictions().setPredicate(predicate);
 	
 				EntityNameOrURI referencedEntity = new EntityNameOrURI();
 				referencedEntity.setUri(entity.getUri());
 				ScopedEntityName sne = new ScopedEntityName();
 				sne.setName(entity.getName());
 				sne.setNamespace(entity.getNamespace());
 				referencedEntity.setEntityName(sne);
 	
 				if (sourceToTarget)
 				{
 					query.getRestrictions().setSourceEntity(referencedEntity);
 				}
 				else
 				{
 					query.getRestrictions().setTargetEntity(referencedEntity);
 				}
 				while (true)
 				{
 					DirectoryResult<AssociationDirectoryEntry> temp = aqs.getResourceSummaries(query, null, page);
 					for (AssociationDirectoryEntry ade : temp.getEntries())
 					{
 						if (associationCodeSystemVersion.getCodeSystemVersionCatalogEntry().getAbout().equals(ade.getAssertedBy().getVersion().getUri()))
 						{
 							thisLevelResults.add(new CustomURIAndEntityName(sourceToTarget ? ade.getTarget().getEntity() : ade.getSubject()));
 						}
 						else
 						{
 							logger_.warn("We crossed code systems during an association query (gather).  Expected '" 
 									+ associationCodeSystemVersion.getCodeSystemVersionCatalogEntry().getAbout() 
 									+ "' but got '" + ade.getAssertedBy().getVersion().getUri() + "' - ignoring the result! - " + ade);
 						}
 					}
 					if (temp.isAtEnd())
 					{
 						break;
 					}
 					else
 					{
 						page.setPage(page.getPage() + 1);
 					}
 				}
 			}
 			catch (UnknownCodeSystemVersion e)
 			{
 				//TODO TEST if this is the right exception to catch 
 				localServiceFail = true;
 			}
 		}
 		
 		//Try the remote lookup route, if the local didn't work
 		if (localServiceFail || aqs == null)
 		{
 			String href = utilities_.makeAssociationURL(associationCodeSystemVersion.getCodeSystemVersionCatalogEntry().getCodeSystemVersionName(),
 					associationCodeSystemVersion.getCodeSystemVersionCatalogEntry().getVersionOf().getContent(), entity.getName(), sourceToTarget, 
 					associationCodeSystemVersion.getHref(), readContext);
 
 			// TODO BUG switch over to predicate filtering directly, when fixed - https://github.com/cts2/cts2-framework/issues/28
 			thisLevelResults.addAll(gatherAssociationDirectoryResults(href, predicateURI, associationCodeSystemVersion, sourceToTarget));
 		}
 
 		if (TransitiveClosure.DIRECTLY_ASSOCIATED == transitivity)
 		{
 			// noop - only want one level.
 		}
 		else if (TransitiveClosure.TRANSITIVE_CLOSURE == transitivity)
 		{
 			for (CustomURIAndEntityName thisLevelItem : thisLevelResults)
 			{
 		
 				if (resultHolder.contains(thisLevelItem))
 				{
 					// Just detected a cycle. No further processing for this item.
 					logger_.debug("Cycle detected - no further processing");
 					continue;
 				}
 				
 				resultHolder.add(thisLevelItem);
 				
 				if (thisLevelItem.getEntity().getUri().equals(entity.getUri()))
 				{
 					//Referenced itself??
 					//leave it, but don't recurse...
 					logger_.warn("While processing '" + entity + "' to find transitive closure, found a self reference!");
 					continue;
 				}
 
 				int sizeBefore = resultHolder.size();
 				processLevel(thisLevelItem.getEntity(), direction, associationCodeSystemVersion, transitivity, predicateURI, leafOnly, resultHolder, readContext);
 				if (leafOnly && resultHolder.size() > sizeBefore)
 				{
 					// There were children from this node - remove it from the resultHolder since they want leaf only.
 					//It needs to be added before the recursion for cycle detection purposes.
 					if (!resultHolder.remove(thisLevelItem))
 					{
 						throw new RuntimeException("Design error in cycle detection!");
 					}
 				}
 			}
 		}
 		else
 		{
 			throw new UnspecifiedCts2Exception("Invalid Transitivity selection");
 		}
 	}
 
 	private ArrayList<CustomURIAndEntityName> gatherAssociationDirectoryResults(String href, String predicateURI,
 			CodeSystemVersionCatalogEntryAndHref associationCodeSystemVersion, boolean sourceToTarget)
 	{
 		AssociationDirectory result = Cts2RestClient.instance().getCts2Resource(href, AssociationDirectory.class);
 		ArrayList<CustomURIAndEntityName> resultHolder = new ArrayList<>();
 
 		for (AssociationDirectoryEntry ade : result.getEntryAsReference())
 		{
 			if (associationCodeSystemVersion.getCodeSystemVersionCatalogEntry().getAbout().equals(ade.getAssertedBy().getVersion().getUri()))
 			{
 				if (ade.getPredicate().getUri().equals(predicateURI))
 				{
 					resultHolder.add(new CustomURIAndEntityName(sourceToTarget ? ade.getTarget().getEntity() : ade.getSubject()));
 				}
 				logger_.warn("Predicate URIs don't match?  Expected '" + predicateURI + "' but got '" + ade.getPredicate().getUri() + "'.  Not including the result " + ade);
 			}
 			else
 			{
 				logger_.warn("We crossed code systems during an association query (gather).  Expected '" 
 						+ associationCodeSystemVersion.getCodeSystemVersionCatalogEntry().getAbout() 
 						+ "' but got '" + ade.getAssertedBy().getVersion().getUri() + "' - ignoring the result! - " + ade);
 			}
 		}
 		if (result.getComplete() == CompleteDirectory.PARTIAL && StringUtils.isNotBlank(result.getNext()))
 		{
 			resultHolder.addAll(gatherAssociationDirectoryResults(result.getNext(), predicateURI, associationCodeSystemVersion, sourceToTarget));
 		}
 		return resultHolder;
 	}
 
 	private Long makeCacheKey(ValueSetDefinitionReadId definitionId, Set<NameOrURI> codeSystemVersions, NameOrURI tag, ResolvedValueSetResolutionEntityQuery query,
 			SortCriteria sortCriteria, ResolvedReadContext readContext)
 	{
 		long result = 1;
 		result = 37 * result + (definitionId == null ? 0 : definitionId.hashCode());
 		result = 37 * result + (codeSystemVersions == null ? 0 : codeSystemVersions.hashCode());
 		result = 37 * result + (tag == null ? 0 : tag.hashCode());
 		result = 37 * result + hashQuery(query);
 		result = 37 * result + (sortCriteria == null ? 0 : sortCriteria.hashCode());
 		result = 37 * result + (readContext == null ? 0 : readContext.hashCode());
 		return result;
 	}
 
 	private long hashQuery(ResolvedValueSetResolutionEntityQuery query)
 	{
 		long result = 1;
 		if (query != null)
 		{
 			result = 37 * result + ((query.getQuery() == null) ? 0 : query.getQuery().hashCode());
 			result = 37 * result
 					+ ((query.getResolvedValueSetResolutionEntityRestrictions() == null) ? 0 : query.getResolvedValueSetResolutionEntityRestrictions().hashCode());
 			result = 37 * result + ((query.getFilterComponent() == null) ? 0 : query.getFilterComponent().hashCode());
 		}
 		return result;
 	}
 }
