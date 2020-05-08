 /**
  * Copyright (C) [2013] [The FURTHeR Project]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.utah.further.fqe.impl.service.query;
 
 import static edu.utah.further.fqe.ds.api.service.results.ResultType.INTERSECTION;
 import static edu.utah.further.fqe.ds.api.service.results.ResultType.SUM;
 import static edu.utah.further.fqe.ds.api.service.results.ResultType.UNION;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.Validate;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import edu.utah.further.core.api.collections.CollectionUtil;
 import edu.utah.further.core.api.constant.Constants;
 import edu.utah.further.core.api.constant.Strings;
 import edu.utah.further.core.api.data.Dao;
 import edu.utah.further.core.api.data.PersistentEntity;
 import edu.utah.further.core.api.text.StringUtil;
 import edu.utah.further.core.data.util.SqlUtil;
 import edu.utah.further.fqe.api.service.query.AggregationService;
 import edu.utah.further.fqe.api.service.query.QueryContextService;
 import edu.utah.further.fqe.api.util.FqeQueryContextUtil;
 import edu.utah.further.fqe.api.ws.to.aggregate.AggregatedResult;
 import edu.utah.further.fqe.api.ws.to.aggregate.AggregatedResultTo;
 import edu.utah.further.fqe.api.ws.to.aggregate.AggregatedResults;
 import edu.utah.further.fqe.api.ws.to.aggregate.AggregatedResultsTo;
 import edu.utah.further.fqe.api.ws.to.aggregate.Category;
 import edu.utah.further.fqe.api.ws.to.aggregate.CategoryTo;
 import edu.utah.further.fqe.ds.api.domain.QueryContext;
 import edu.utah.further.fqe.ds.api.service.results.ResultDataService;
 import edu.utah.further.fqe.ds.api.service.results.ResultSummaryService;
 import edu.utah.further.fqe.ds.api.service.results.ResultType;
 import edu.utah.further.fqe.ds.api.to.ResultContextKeyToImpl;
 import edu.utah.further.fqe.ds.api.util.FqeDsQueryContextUtil;
 import edu.utah.further.fqe.mpi.api.service.IdentifierService;
 
 /**
  * A data source result set aggregation service implementation. Relies on a Hibernate
  * persistent layer of {@link QueryContext}s.
  * <p>
  * -----------------------------------------------------------------------------------<br>
  * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
  * Contact: {@code <further@utah.edu>}<br>
  * Biomedical Informatics, 26 South 2000 East<br>
  * Room 5775 HSEB, Salt Lake City, UT 84112<br>
  * Day Phone: 1-801-581-4080<br>
  * -----------------------------------------------------------------------------------
  * 
  * @author Oren E. Livne {@code <oren.livne@utah.edu>}
  * @version Jun 1, 2010
  */
 @Service("aggregationService")
 @Transactional
 public class AggregationServiceImpl implements AggregationService
 {
 	// ========================= CONSTANTS =================================
 
 	/**
 	 * A logger that helps identify this class' printouts.
 	 */
 	private static final Logger log = getLogger(AggregationServiceImpl.class);
 
 	// ========================= DEPENDENCIES ==============================
 
 	/**
 	 * {@link QueryContext} CRUD service.
 	 */
 	@Autowired
 	private QueryContextService qcService;
 
 	/**
 	 * Service for retrieving count results
 	 */
 	@Autowired
 	private ResultSummaryService resultSummaryService;
 
 	/**
 	 * Service for retrieving data results
 	 */
 	@Autowired
 	private ResultDataService resultDataService;
 
 	/**
 	 * Identifier related operations, particularly around retrieving identifiers related
 	 * to particularly queries
 	 */
 	@Autowired
 	private IdentifierService identifierService;
 
 	/**
 	 * Handles generic DAO operations and searches.
 	 */
 	@Autowired
 	@Qualifier("dao")
 	private Dao dao;
 
 	/**
 	 * Count results smaller than this value are scrubbed.
 	 */
 	private int resultMaskBoundary = 5;
 
 	/**
 	 * Name of histogram category that lumps all small values.
 	 */
 	private String resultMaskOther = "Other";
 
 	/**
 	 * Name of histogram category for missing data.
 	 */
 	private final String missingData = "Missing Data";
 
 	/**
 	 * Categories to include in the histogram, keyed by field name
 	 */
 	private Map<String, String> categories = new HashMap<>();
 
 	// ========================= IMPLEMENTATION: DataService ===============
 
 	/**
 	 * Synchronize the parent FQC state of with a DQC's state (because we are not
 	 * cascading updates in the QC entity, this needs to be done "manually").
 	 * <p>
 	 * TODO: replace by the observer pattern? (parent observes its children)
 	 * 
 	 * @param child
 	 *            DS query context. Assumed to have a persistent parent
 	 * @see edu.utah.further.fqe.api.service.query.AggregationService#updateParentState(edu.utah.further.fqe.ds.api.domain.QueryContext)
 	 */
 	@Override
 	public synchronized void updateParentState(final QueryContext child)
 	{
 		// Need to reload parent entity because of the argument outlined above for the
 		// entity: the might already be associated with the persistent session
 		final QueryContext parent = qcService.findById(child.getParent().getId());
 		if (log.isDebugEnabled())
 		{
 			log.debug("updateParentState()");
 			log.debug("Child  " + child);
 			log.debug("Parent " + parent);
 		}
 		if (parent.isInFinalState())
 		{
 			// Parent already completed, don't update its state
 			return;
 		}
 
 		// Synchronize parent state with updated child state
 		updateStateUponChildStart(parent, child);
 		if (parent.isStarted())
 		{
 			updateExecutingStateForceful(parent);
 		}
 
 		// Save parent changes to database
 		if (log.isDebugEnabled())
 		{
 			log.debug("Saving synchronized parent " + parent);
 		}
 		dao.update(parent);
 	}
 
 	/**
 	 * Update the parent result set count to be the sum of its children counts.
 	 * 
 	 * @param parent
 	 *            federated query context. Assumed to be persistent
 	 * @see edu.utah.further.fqe.api.service.query.AggregationService#updateCounts(edu.utah.further.fqe.ds.api.domain.QueryContext)
 	 */
 	@Override
 	public synchronized void updateCounts(final QueryContext parent)
 	{
 		// Need to reload parent entity because of the argument outlined above for the
 		// entity: the might already be associated with the persistent session
 		final QueryContext reloadedParent = qcService.findById(parent.getId());
 
 		// A simple update of the parent raw result set size for now
 		sumUpCounts(reloadedParent);
 
 		// Save parent changes to database
 		dao.update(reloadedParent);
 	}
 
 	/**
 	 * Generate post-query result views (union, intersection, etc.)
 	 * 
 	 * @param parent
 	 *            federated query context
 	 * @see edu.utah.further.fqe.api.service.query.AggregationService#generateResultViews(edu.utah.further.fqe.ds.api.domain.QueryContext)
 	 */
 	@Override
 	public synchronized void generateResultViews(final QueryContext federatedQueryContext)
 	{
 		final QueryContext parent = qcService.findById(federatedQueryContext.getId());
 		if (parent.getResultViews() != null && parent.getResultViews().size() > 0)
 		{
 			log.debug("Resultviews have already been generated, "
 					+ "query finished early or was sealed by QuerySealer");
 			return;
 		}
 		if (log.isDebugEnabled())
 		{
 			log.debug("generateResultViews() " + parent);
 		}
 		final List<String> queryIds = qcService.findChildrenQueryIdsByParent(parent);
 		switch (parent.getQueryType())
 		{
 			case DATA_QUERY:
 			{
 				addResultViewTo(parent, queryIds, SUM);
 				addResultViewTo(parent, queryIds, UNION);
 				addResultViewTo(parent, queryIds, INTERSECTION);
 				break;
 			}
 			case COUNT_QUERY:
 			{
 				break;
 			}
 		}
 
 		// Save parent changes to database
 		dao.update(parent);
 	}
 
 	/**
 	 * @param federatedQueryContext
 	 * @return
 	 * @see edu.utah.further.fqe.api.service.query.AggregationService#generatedAggregatedResults(edu.utah.further.fqe.ds.api.domain.QueryContext)
 	 */
 	@Override
 	public synchronized AggregatedResults generateAggregatedResults(
 			final QueryContext federatedQueryContext)
 	{
 		final QueryContext parent = qcService.findById(federatedQueryContext.getId());
 		final List<QueryContext> children = qcService.findChildren(parent);
 
 		final List<String> queryIds = new ArrayList<>();
 
 		for (final QueryContext childContext : children)
 		{
 			queryIds.add(childContext.getExecutionId());
 		}
 
 		final Class<?> rootResultClass = resultDataService.getRootResultClass(queryIds);
 
 		// Sanity check
 		Validate.isTrue(PersistentEntity.class.isAssignableFrom(rootResultClass));
 
 		final List<String> fields = new ArrayList<>();
 		final Set<String> aggregationIncludedFields = categories.keySet();
 		for (final Field field : rootResultClass.getDeclaredFields())
 		{
 			// Only consider private and non-excluded fields
 			if (Modifier.isPrivate(field.getModifiers())
 					&& aggregationIncludedFields.contains(field.getName()))
 			{
 				fields.add(field.getName());
 			}
 		}
 
 		// get all virtual ids for sum
 		final List<Long> idsInSum = identifierService.getVirtualIdentifiers(queryIds);
 
 		// get all virtual ids for intersection
 		final Map<Long, Set<Long>> commonToVirtualMap = identifierService
 				.getCommonIdToVirtualIdMap(queryIds, true);
 		final List<Long> idsInIntersection = CollectionUtil.newList();
 		for (final Set<Long> virtuals : commonToVirtualMap.values())
 		{
 			// Add the first virtual id, ignore all the others and make very big assuming
 			// that because they're the same person, they'll also have the same record
 			// information
 			idsInIntersection.add(virtuals.iterator().next());
 		}
 
 		// get all virtual ids for union
 		final List<Long> idsInUnion = new ArrayList<>();
 		idsInUnion.addAll(identifierService.getUnresolvedVirtualIdentifiers(queryIds));
 		idsInUnion.addAll(idsInIntersection);
 
 		final AggregatedResult aggregatedSum = generateAggregatedResult(fields,
 				rootResultClass.getCanonicalName(), queryIds, idsInSum, ResultType.SUM);
 		final AggregatedResult aggregatedUnion = generateAggregatedResult(fields,
 				rootResultClass.getCanonicalName(), queryIds, idsInUnion,
 				ResultType.UNION);
 		final AggregatedResult aggregatedIntersection = generateAggregatedResult(fields,
 				rootResultClass.getCanonicalName(), queryIds, idsInIntersection,
 				ResultType.INTERSECTION);
 
 		final AggregatedResults aggregatedResults = new AggregatedResultsTo();
 		aggregatedResults.addResult(aggregatedSum);
 		aggregatedResults.addResult(aggregatedUnion);
 		aggregatedResults.addResult(aggregatedIntersection);
 		aggregatedResults.setNumDataSources(queryIds.size());
 
 		return aggregatedResults;
 	}
 
 	/**
 	 * Scrub positive counts that are smaller than the mask boundary value. By convention,
 	 * all scrubbed entries are set to {@link Constants#INVALID_VALUE_BOXED_LONG}.
 	 * 
 	 * @param results
 	 *            raw counts
 	 * @return scrubbed results
 	 */
 	@Override
 	public AggregatedResults scrubResults(final AggregatedResults results)
 	{
 		for (final AggregatedResult result : results.getResults())
 		{
 			for (final Category category : result.getCategories())
 			{
 				// FUR-1745: (a) find all categories with small entries
 				final Set<String> smallEntryKeys = CollectionUtil.newSet();
 				for (final Map.Entry<String, Long> entry : category
 						.getEntries()
 						.entrySet())
 				{
 					final Long value = entry.getValue();
 					if (FqeQueryContextUtil.shouldBeMasked(value.longValue(),
 							resultMaskBoundary))
 					{
 						smallEntryKeys.add(entry.getKey());
 						// category.addEntry(entry.getKey(),
 						// Constants.INVALID_VALUE_BOXED_LONG);
 					}
 				}
 
 				// FUR-1745: (b) lump all small entries into one "Other" category
 				if (!smallEntryKeys.isEmpty())
 				{
 					for (final String key : smallEntryKeys)
 					{
 						category.removeEntry(key);
 					}
 					category
 							.addEntry(resultMaskOther, Constants.INVALID_VALUE_BOXED_LONG);
 				}
 				if (category.getName().equals("Age†"))
 				{
 					if (category.removeEntry(Strings.NULL_TO_STRING) != null)
 					{
 						category.addEntry(resultMaskOther,
 								Constants.INVALID_VALUE_BOXED_LONG);
 					}
 				}
 			}
 		}
 		return results;
 	}
 
 	/**
 	 * Dependency-inject a count scrub threshold.
 	 * 
 	 * @param resultMaskBoundary
 	 *            new count scrub threshold
 	 * @see edu.utah.further.fqe.api.service.query.AggregationService#setResultMaskBoundary(int)
 	 */
 	@Override
 	public void setResultMaskBoundary(final int resultMaskBoundary)
 	{
 		this.resultMaskBoundary = resultMaskBoundary;
 	}
 
 	/**
 	 * Set a new value for the resultMaskOther property.
 	 * 
 	 * @param resultMaskOther
 	 *            the resultMaskOther to set
 	 * @see edu.utah.further.fqe.api.service.query.AggregationService#setResultMaskOther(java.lang.String)
 	 */
 	@Override
 	public void setResultMaskOther(final String resultMaskOther)
 	{
 		this.resultMaskOther = resultMaskOther;
 	}
 
 	// ========================= GET/SET METHODS ===========================
 
 	/**
 	 * Return the qcService property.
 	 * 
 	 * @return the qcService
 	 */
 	public QueryContextService getQcService()
 	{
 		return qcService;
 	}
 
 	/**
 	 * Set a new value for the qcService property.
 	 * 
 	 * @param qcService
 	 *            the qcService to set
 	 */
 	public void setQcService(final QueryContextService qcService)
 	{
 		this.qcService = qcService;
 	}
 
 	/**
 	 * Return the resultSummaryService property.
 	 * 
 	 * @return the resultSummaryService
 	 */
 	public ResultSummaryService getResultSummaryService()
 	{
 		return resultSummaryService;
 	}
 
 	/**
 	 * Set a new value for the resultSummaryService property.
 	 * 
 	 * @param resultSummaryService
 	 *            the resultSummaryService to set
 	 */
 	public void setResultSummaryService(final ResultSummaryService resultSummaryService)
 	{
 		this.resultSummaryService = resultSummaryService;
 	}
 
 	/**
 	 * Return the resultDataService property.
 	 * 
 	 * @return the resultDataService
 	 */
 	public ResultDataService getResultDataService()
 	{
 		return resultDataService;
 	}
 
 	/**
 	 * Set a new value for the resultDataService property.
 	 * 
 	 * @param resultDataService
 	 *            the resultDataService to set
 	 */
 	public void setResultDataService(final ResultDataService resultDataService)
 	{
 		this.resultDataService = resultDataService;
 	}
 
 	/**
 	 * Return the dao property.
 	 * 
 	 * @return the dao
 	 */
 	public Dao getDao()
 	{
 		return dao;
 	}
 
 	/**
 	 * Set a new value for the dao property.
 	 * 
 	 * @param dao
 	 *            the dao to set
 	 */
 	public void setDao(final Dao dao)
 	{
 		this.dao = dao;
 	}
 
 	/**
 	 * Return the categories property.
 	 * 
 	 * @return the categories
 	 */
 	public Map<String, String> getCategories()
 	{
 		return categories;
 	}
 
 	/**
 	 * Set a new value for the categories property.
 	 * 
 	 * @param categories
 	 *            the categories to set
 	 */
 	public void setCategories(final Map<String, String> categories)
 	{
 		this.categories = categories;
 	}
 
 	// ========================= PRIVATE METHODS ===========================
 
 	/**
 	 * Generates an aggregated result for a given {@link ResultType} based on the records
 	 * included.
 	 * 
 	 * @param queryIds
 	 * @param fqRootClass
 	 * @param fields
 	 * @param includedIds
 	 */
 	private AggregatedResult generateAggregatedResult(final List<String> fields,
 			final String fqRootClass, final List<String> queryIds,
 			final List<Long> includedIds, final ResultType resultType)
 	{
 		final AggregatedResultTo aggregatedResultTo = new AggregatedResultTo(
 				new ResultContextKeyToImpl(resultType));
 
 		// for each set of ids, do an aggregate count on each field
 		for (final String field : fields)
 		{
 			// We really don't need unlimited IN functionality for query ids but this make
 			// the parameter binding easier
 			final String hql = "SELECT DISTINCT new map(" + field
					+ "as fieldName, COUNT(" + field + ") as fieldCount) FROM "
 					+ fqRootClass + " WHERE "
 					+ SqlUtil.unlimitedInValues(queryIds, "id.datasetId") + " and "
 					+ SqlUtil.unlimitedInValues(includedIds, "id.id");
 
 			final List<Object> parameters = new ArrayList<>();
 			parameters.addAll(queryIds);
 			parameters.addAll(includedIds);
 			final List<Map<String, Object>> results = resultDataService.getQueryResults(
 					hql, parameters);
 
 			final CategoryTo categoryTo = new CategoryTo(categories.get(field));
 
 			for (final Map<String, Object> result : results)
 			{
 				Object name = result.get("fieldName");
 				if (name == null)
 				{
 					name = missingData;
 				}
 				categoryTo.addEntry((String) name, (Long) result.get("fieldCount"));
 			}
 
 			aggregatedResultTo.addCategory(categoryTo);
 		}
 
 		return aggregatedResultTo;
 	}
 
 	/**
 	 * @param parent
 	 * @param queryIds
 	 * @param resultType
 	 * @param intersectionIndex
 	 */
 	private void addResultViewTo(final QueryContext parent, final List<String> queryIds,
 			final ResultType resultType)
 	{
 		FqeDsQueryContextUtil.addResultViewTo(parent, resultType, resultSummaryService
 				.join(queryIds, resultType)
 				.longValue());
 	}
 
 	/**
 	 * @param parent
 	 * @param child
 	 */
 	private void updateStateUponChildStart(final QueryContext parent,
 			final QueryContext child)
 	{
 		if ((child.isStarted() || child.isInFinalState()) && !parent.isStarted()
 				&& !parent.isFailed())
 		{
 			if (log.isDebugEnabled())
 			{
 				log.debug("Starting parent " + parent
 						+ " because there's a running child: " + child);
 			}
 			parent.start();
 		}
 	}
 
 	/**
 	 * Update an executing parent state according to children completion states (FUR-575):
 	 * <ul>
 	 * <li>Transition query to COMPLETED state regardless of staleness if At least
 	 * maxRespondingDataSources DS's have responded.
 	 * </ul>
 	 * 
 	 * @param parent
 	 *            federated query contexts to update
 	 */
 	private synchronized void updateExecutingStateForceful(final QueryContext parent)
 	{
 		final int numRespondingDs = qcService.findCompletedChildren(parent).size();
 		final int maxRespondingDs = parent.getMaxRespondingDataSources();
 		if (numRespondingDs >= maxRespondingDs)
 		{
 			if (log.isDebugEnabled())
 			{
 				log.debug(numRespondingDs + " DS's responded >= maximum required ("
 						+ maxRespondingDs + "). Finishing query early.");
 			}
 			parent.finish();
 			generateResultViews(parent);
 		}
 	}
 
 	/**
 	 * Set the parent's result set count to the sum of all result set counts of all its
 	 * children QC's.
 	 * 
 	 * @param parent
 	 *            federated QC
 	 */
 	private synchronized void sumUpCounts(final QueryContext parent)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Aggregating counts of query ID " + parent.getId());
 		}
 		int sum = 0;
 		for (final QueryContext child : qcService.findCompletedChildren(parent))
 		{
 			final long dataSourceCount = child.getResultContext().getNumRecords();
 			if (StringUtil.isValidLong(dataSourceCount))
 			{
 				sum += dataSourceCount;
 			}
 			if (log.isDebugEnabled())
 			{
 				log.debug("Child DS-ID " + child.getDataSourceId() + " count "
 						+ dataSourceCount);
 			}
 		}
 		parent.getResultContext().setNumRecords(sum);
 		if (log.isDebugEnabled())
 		{
 			log.debug("Total count of query ID " + parent.getId() + ": " + sum);
 		}
 	}
 
 }
