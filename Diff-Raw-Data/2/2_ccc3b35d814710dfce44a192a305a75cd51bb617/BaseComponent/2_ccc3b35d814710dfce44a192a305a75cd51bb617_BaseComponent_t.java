 package com.antonyh.hutchisontechnical.hippo.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.hippoecm.hst.component.support.bean.BaseHstComponent;
 import org.hippoecm.hst.content.beans.query.HstQuery;
 import org.hippoecm.hst.content.beans.query.HstQueryResult;
 import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
 import org.hippoecm.hst.content.beans.query.filter.Filter;
 import org.hippoecm.hst.content.beans.standard.HippoBean;
 import org.hippoecm.hst.core.component.HstComponentException;
 import org.hippoecm.hst.core.component.HstRequest;
 import org.hippoecm.hst.util.SearchInputParsingUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.antonyh.hutchisontechnical.hippo.componentsinfo.GeneralListInfo;
 import com.antonyh.hutchisontechnical.hippo.componentsinfo.PageableListInfo;
 
 public abstract class BaseComponent extends BaseHstComponent {
 
 	public static final Logger log = LoggerFactory
 			.getLogger(BaseComponent.class);
 
 	public void createAndExecuteSearch(final BaseComponent bc,
 			final HstRequest request, final GeneralListInfo info,
 			final HippoBean scope, final String query,
 			final String attributePrefix) throws HstComponentException {
 
 		createAndExecuteSearch(bc, request, info, scope, query,
 				attributePrefix, null);
 
 	}
 
 	public void createAndExecuteSearch(final BaseComponent bc,
 			final HstRequest request, final GeneralListInfo info,
 			final HippoBean scope, final String query)
 			throws HstComponentException {
 
 		createAndExecuteSearch(bc, request, info, scope, query, "", null);
 
 	}
 
 	/**
 	 * Creates and executes a search, and puts a {@link HstQueryResult},
 	 * {@link PageableListInfo}, crPage, query and optionally a {@link List
 	 * <Integer>} of pages on the request
 	 * 
 	 * (final HstRequest request, final GeneralListInfo info, final HippoBean
 	 * scope, final String query)
 	 * 
 	 * @param request
 	 * @param info
 	 * @param scope
 	 *            the scope to search below.
 	 * @param query
 	 *            the free text query to search for. If <code>null</code> or
 	 *            empty, it will be ignored
 	 */
 
 	protected void createAndExecuteSearch(final BaseComponent bc,
 			final HstRequest request, final GeneralListInfo info,
 			final HippoBean scope, final String query,
 			final String attributePrefix, final String topic)
 			throws HstComponentException {
 		if (scope == null) {
 			throw new HstComponentException(
 					"Scope is not allowed to be null for a search");
 		}
 		int pageSize = info.getPageSize();
 		if (pageSize == 0) {
 			log.warn("Empty pageSize or set to null. This is not a valid size. Use default size");
 		}
 		String docType = info.getDocType();
 		String sortBy = info.getSortBy();
 		String sortOrder = info.getSortOrder();
 		String crPageStr = request.getParameter(attributePrefix + "page");
 
 		int crPage = 1;
 		if (crPageStr != null) {
 			try {
 				crPage = Integer.parseInt(crPageStr);
 			} catch (NumberFormatException e) {
 				throw new HstComponentException("Invalid page number '"
 						+ crPage + "'");
 			}
 		}
 
 		@SuppressWarnings("rawtypes")
 		Class filterClass = getObjectConverter().getAnnotatedClassFor(docType);
 		if (filterClass == null) {
 			throw new HstComponentException("There is no bean for docType '"
 					+ docType + "'. Cannot use '" + docType
 					+ "' as in this search");
 		}
 
 		try {
 			@SuppressWarnings("unchecked")
 			HstQuery hstQuery = getQueryManager(request).createQuery(scope,
 					filterClass, true);
 			hstQuery.setLimit(pageSize);
 			hstQuery.setOffset(pageSize * (crPage - 1));
 
 			if (sortBy != null && !"".equals(sortBy)) {
 				if (sortOrder == null || "".equals(sortOrder)
 						|| "descending".equals(sortOrder)) {
 					hstQuery.addOrderByDescending(sortBy);
 				} else {
 					hstQuery.addOrderByAscending(sortBy);
 				}
 			}
 
 			// Filter filter = hstQuery.createFilter();
 			// hstQuery.setFilter(filter);
 			// // filter.addNotNull("ht:image/hippo:translation/hippo:message");
 
 			String parsedQuery = SearchInputParsingUtils.parse(query, false);
 			if (parsedQuery != null && !parsedQuery.equals(query)) {
 				log.debug(
 						"Replaced query '{}' with '{}' because it contained invalid chars.",
 						query, parsedQuery);
 			}
 			if (!StringUtils.isEmpty(parsedQuery)) {
 				Filter f = hstQuery.createFilter();
 				f.addContains(".", parsedQuery);
 				hstQuery.setFilter(f);
 			}
 
 			if (topic != null) {
 				Filter f = hstQuery.createFilter();
 				f.addNotNull("ht:summary");
 				f.addEqualTo("ht:topic/hippo:docbase", topic);
 				hstQuery.setFilter(f);
 			}
 
			log.trace("query is {}", hstQuery.getQueryAsString(true));
 			// log.trace("--");
 			HstQueryResult result = hstQuery.execute();
 
 			request.setAttribute(attributePrefix + "result", result);
 			request.setAttribute(attributePrefix + "info", info);
 			request.setAttribute(attributePrefix + "crPage", crPage);
 			request.setAttribute(attributePrefix + "query", parsedQuery);
 
 			if (info instanceof PageableListInfo
 					&& ((PageableListInfo) info).isPagesVisible()) {
 				// add pages
 				if (result.getTotalSize() > pageSize) {
 					List<Integer> pages = new ArrayList<Integer>();
 					int numberOfPages = result.getTotalSize() / pageSize;
 					if (result.getTotalSize() % pageSize != 0) {
 						numberOfPages++;
 					}
 					for (int i = 0; i < numberOfPages; i++) {
 						pages.add(i + 1);
 					}
 					request.setAttribute(attributePrefix + "pages", pages);
 				}
 			}
 
 		} catch (QueryException e) {
 			throw new HstComponentException(
 					"Exception occured during creation or execution of HstQuery. ",
 					e);
 		}
 	}
 
 }
