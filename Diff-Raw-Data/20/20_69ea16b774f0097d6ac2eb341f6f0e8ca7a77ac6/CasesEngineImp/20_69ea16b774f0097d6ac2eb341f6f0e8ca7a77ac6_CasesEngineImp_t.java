 package is.idega.idegaweb.egov.bpm.cases.presentation.beans;
 
 import is.idega.idegaweb.egov.application.business.ApplicationBusiness;
 import is.idega.idegaweb.egov.application.business.ApplicationType;
 import is.idega.idegaweb.egov.application.data.Application;
 import is.idega.idegaweb.egov.bpm.IWBundleStarter;
 import is.idega.idegaweb.egov.bpm.cases.CasesBPMProcessView;
 import is.idega.idegaweb.egov.bpm.cases.actionhandlers.CaseHandlerAssignmentHandler;
 import is.idega.idegaweb.egov.bpm.cases.exe.CasesBPMProcessDefinitionW;
 import is.idega.idegaweb.egov.bpm.cases.presentation.UIProcessVariables;
 import is.idega.idegaweb.egov.bpm.cases.search.CasesListSearchCriteriaBean;
 import is.idega.idegaweb.egov.bpm.cases.search.CasesListSearchFilter;
 import is.idega.idegaweb.egov.bpm.cases.search.impl.DefaultCasesListSearchFilter;
 import is.idega.idegaweb.egov.bpm.media.CasesSearchResultsExporter;
 import is.idega.idegaweb.egov.cases.business.CasesBusiness;
 import is.idega.idegaweb.egov.cases.presentation.ClosedCases;
 import is.idega.idegaweb.egov.cases.presentation.MyCases;
 import is.idega.idegaweb.egov.cases.presentation.OpenCases;
 import is.idega.idegaweb.egov.cases.presentation.PublicCases;
 import is.idega.idegaweb.egov.cases.util.CasesConstants;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ejb.FinderException;
 import javax.faces.component.UIComponent;
 import javax.servlet.ServletContext;
 
 import org.jdom.Document;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.idega.block.process.business.CaseBusiness;
 import com.idega.block.process.business.CaseManagersProvider;
 import com.idega.block.process.business.CasesRetrievalManager;
 import com.idega.block.process.business.ProcessConstants;
 import com.idega.block.process.data.Case;
 import com.idega.block.process.data.CaseStatus;
 import com.idega.block.process.event.CaseModifiedEvent;
 import com.idega.block.process.presentation.CaseBlock;
 import com.idega.block.process.presentation.UICasesList;
 import com.idega.block.process.presentation.UserCases;
 import com.idega.block.process.presentation.beans.CaseListPropertiesBean;
 import com.idega.block.process.presentation.beans.CasePresentation;
 import com.idega.block.process.presentation.beans.CasePresentationComparator;
 import com.idega.block.process.presentation.beans.CasesSearchCriteriaBean;
 import com.idega.block.process.presentation.beans.CasesSearchResults;
 import com.idega.block.process.presentation.beans.CasesSearchResultsHolder;
 import com.idega.block.process.presentation.beans.GeneralCasesListBuilder;
 import com.idega.block.web2.business.JQuery;
 import com.idega.block.web2.business.Web2Business;
 import com.idega.bpm.bean.CasesBPMAssetProperties;
 import com.idega.builder.bean.AdvancedProperty;
 import com.idega.builder.business.AdvancedPropertyComparator;
 import com.idega.builder.business.BuilderLogic;
 import com.idega.builder.business.BuilderLogicWrapper;
 import com.idega.business.IBOLookup;
 import com.idega.business.IBOLookupException;
 import com.idega.core.accesscontrol.bean.UserHasLoggedInEvent;
 import com.idega.core.builder.business.BuilderService;
 import com.idega.core.builder.data.ICPage;
 import com.idega.core.business.DefaultSpringBean;
 import com.idega.core.component.bean.RenderedComponent;
 import com.idega.core.component.business.ICObjectBusiness;
 import com.idega.core.component.data.ICObjectInstance;
 import com.idega.core.component.data.ICObjectInstanceHome;
 import com.idega.data.IDOLookup;
 import com.idega.idegaweb.IWApplicationContext;
 import com.idega.idegaweb.IWMainApplication;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.idegaweb.egov.bpm.data.CaseProcInstBind;
 import com.idega.idegaweb.egov.bpm.data.dao.CasesBPMDAO;
 import com.idega.io.MediaWritable;
 import com.idega.jbpm.bean.BPMProcessVariable;
 import com.idega.jbpm.bean.VariableInstanceType;
 import com.idega.jbpm.exe.ProcessDefinitionW;
 import com.idega.jbpm.utils.JBPMConstants;
 import com.idega.jbpm.variables.MultipleSelectionVariablesResolver;
 import com.idega.presentation.IWContext;
 import com.idega.presentation.ListNavigator;
 import com.idega.presentation.paging.PagedDataCollection;
 import com.idega.user.business.UserBusiness;
 import com.idega.user.data.User;
 import com.idega.util.CoreConstants;
 import com.idega.util.CoreUtil;
 import com.idega.util.ListUtil;
 import com.idega.util.StringUtil;
 import com.idega.util.URIUtil;
 import com.idega.util.expression.ELUtil;
 import com.idega.webface.WFUtil;
 
 @Scope(BeanDefinition.SCOPE_SINGLETON)
 @Service("casesEngineDWR")
 public class CasesEngineImp extends DefaultSpringBean implements BPMCasesEngine, ApplicationListener {
 
 	@Autowired
 	private CasesBPMProcessView casesBPMProcessView;
 
 	@Autowired
 	private BuilderLogicWrapper builderLogic;
 
 	@Autowired
 	private GeneralCasesListBuilder casesListBuilder;
 
 	@Autowired
 	private CaseManagersProvider caseManagersProvider;
 
 	@Autowired
 	private Web2Business web2;
 
 	@Autowired
 	private JQuery jQuery;
 
 	@Autowired
 	private CasesBPMDAO casesBPMDAO;
 
 	public static final String	FILE_DOWNLOAD_LINK_STYLE_CLASS = "casesBPMAttachmentDownloader",
 								PDF_GENERATOR_AND_DOWNLOAD_LINK_STYLE_CLASS = "casesBPMPDFGeneratorAndDownloader",
 								DOWNLOAD_TASK_IN_PDF_LINK_STYLE_CLASS = "casesBPMDownloadTaskInPDF",
 								CASE_LOGS_PDF_DOWNLOAD_LINK_STYLE_CLASS = "caselogspdfdownload";
 
 	private static final Logger LOGGER = Logger.getLogger(CasesEngineImp.class.getName());
 
 	@Override
 	public Long getProcessInstanceId(String caseId) {
 		if (caseId == null) {
 			LOGGER.warning("Case ID is not provided!");
 			return null;
 		}
 
 		Long processInstanceId = getCasesBPMProcessView().getProcessInstanceId(caseId);
 		return processInstanceId;
 	}
 
 	@Override
 	public Document getCaseManagerView(CasesBPMAssetProperties properties) {
 		if (properties == null) {
 			return null;
 		}
 
 		IWContext iwc = CoreUtil.getIWContext();
 		if (iwc == null) {
 			return null;
 		}
 
 		String caseIdStr = properties.getCaseId();
 		if (caseIdStr == null || CoreConstants.EMPTY.equals(caseIdStr) || iwc == null) {
 			LOGGER.log(Level.WARNING, "Either not provided:\n caseId="+caseIdStr+", iwc="+iwc);
 			return null;
 		}
 
 		try {
 			CasesBPMAssetsState stateBean = WFUtil.getBeanInstance(CasesBPMAssetsState.beanIdentifier);
 			if (stateBean != null) {
 				stateBean.setUsePDFDownloadColumn(properties.isUsePDFDownloadColumn());
 				stateBean.setAllowPDFSigning(properties.isAllowPDFSigning());
 				stateBean.setHideEmptySection(properties.isHideEmptySection());
 				stateBean.setCommentsPersistenceManagerIdentifier(properties.getCommentsPersistenceManagerIdentifier());
 				stateBean.setShowAttachmentStatistics(properties.isShowAttachmentStatistics());
 				stateBean.setShowOnlyCreatorInContacts(properties.isShowOnlyCreatorInContacts());
 				stateBean.setAutoShowComments(properties.isAutoShowComments());
 				stateBean.setShowLogExportButton(properties.isShowLogExportButton());
 				stateBean.setShowComments(properties.isShowComments());
 				stateBean.setShowContacts(properties.isShowContacts());
 				stateBean.setSpecialBackPage(properties.getSpecialBackPage());
 			}
 
 			Integer caseId = new Integer(caseIdStr);
 			UIComponent caseAssets = getCasesBPMProcessView().getCaseManagerView(iwc, null, caseId, properties.getProcessorType());
 
 			Document rendered = getBuilderLogic().getBuilderService(iwc).getRenderedComponent(iwc, caseAssets, true);
 
 			return rendered;
 		} catch (Exception e) {
 			LOGGER.log(Level.SEVERE, "Exception while resolving rendered component for case assets view", e);
 		}
 
 		return null;
 	}
 
 	@Override
 	public boolean setCaseSubject(String caseId, String subject) {
 		if (caseId == null || subject == null) {
 			return false;
 		}
 
 		CaseBusiness caseBusiness = null;
 		try {
 			caseBusiness = (CaseBusiness) IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), CaseBusiness.class);
 		} catch (IBOLookupException e) {
 			LOGGER.log(Level.SEVERE, "Error getting CaseBusiness", e);
 		}
 		if (caseBusiness == null) {
 			return false;
 		}
 
 		Case theCase = null;
 		try {
 			theCase = caseBusiness.getCase(caseId);
 		} catch (Exception e) {
 			LOGGER.log(Level.SEVERE, "Unable to get case by ID: " + caseId, e);
 		}
 		if (theCase == null) {
 			return false;
 		}
 
 		theCase.setSubject(subject);
 		theCase.store();
 
 		return true;
 	}
 
 	@Override
 	public Document getRenderedCasesByQuery(CasesSearchCriteriaBean criteriaBean) {
 		if (criteriaBean instanceof CasesListSearchCriteriaBean)
 			return getCasesListByUserQuery((CasesListSearchCriteriaBean) criteriaBean);
 
 		getLogger().warning("Criterias " + criteriaBean + " are not instance of " + CasesListSearchCriteriaBean.class + " (actual implementation: " +
 				criteriaBean.getClass().getName() + "), returning null");
 		return null;
 	}
 
 	@Override
 	public Document getCasesListByUserQuery(CasesListSearchCriteriaBean criteriaBean) {
 		if (criteriaBean == null) {
 			LOGGER.log(Level.SEVERE, "Can not execute search - search criterias unknown");
 			return null;
 		}
 
 		if (criteriaBean.isClearResults()) {
 			//	Clearing search result before new search
 			clearSearchResults(criteriaBean.getId());
 		}
 
 		List<Long> piIds = criteriaBean.getProcInstIds();
 		LOGGER.log(Level.INFO, new StringBuilder("Search query: caseNumber: ").append(criteriaBean.getCaseNumber()).append(", description: ")
 				.append(criteriaBean.getDescription()).append(", name: ").append(criteriaBean.getName()).append(", personalId: ")
 				.append(criteriaBean.getPersonalId()).append(", processId: ").append(criteriaBean.getProcessId()).append(", statusId: ")
 				.append(criteriaBean.getStatusId()).append(", dateRange: ").append(criteriaBean.getDateRange()).append(", casesListType: ")
 				.append(criteriaBean.getCaseListType()).append(", contact: ").append(criteriaBean.getContact()).append(", variables provided: ")
 				.append(ListUtil.isEmpty(criteriaBean.getProcessVariables()) ? "none" : criteriaBean.getProcessVariables())
 				.append(", process instance IDs: ").append(ListUtil.isEmpty(piIds) ? "none" :
 					piIds.size() >= 1000 ? new ArrayList<Long>(piIds).subList(0, 999) + " ..." : piIds)
 		.toString());
 
 		IWContext iwc = CoreUtil.getIWContext();
 		if (iwc == null) {
 			LOGGER.warning(IWContext.class + " is unavailable!");
 			return null;
 		}
 
 		addSearchQueryToSession(iwc, criteriaBean);
 
 		PagedDataCollection<CasePresentation> cases = getCasesByQuery(iwc, criteriaBean);
 		if (criteriaBean.isClearResults()) {
 			if (cases == null) {
 				Collection<CasePresentation> externalData = getExternalSearchResults(getSearchResultsHolder(), criteriaBean.getId());
 				if (externalData != null) {
 					setSearchResults(iwc, externalData, criteriaBean);
 				}
 			} else {
 				setSearchResults(iwc, cases.getCollection(), criteriaBean);
 			}
 		}
 
 		CaseListPropertiesBean properties = new CaseListPropertiesBean();
 		properties.setType(ProcessConstants.CASE_LIST_TYPE_SEARCH_RESULTS);
 		properties.setUsePDFDownloadColumn(criteriaBean.isUsePDFDownloadColumn());
 		properties.setAllowPDFSigning(criteriaBean.isAllowPDFSigning());
 		properties.setShowStatistics(criteriaBean.isShowStatistics());
 		properties.setHideEmptySection(criteriaBean.isHideEmptySection());
 		properties.setPageSize(criteriaBean.getPageSize());
 		properties.setPage(criteriaBean.getPage());
 		properties.setInstanceId(criteriaBean.getInstanceId());
 		properties.setShowCaseNumberColumn(criteriaBean.isShowCaseNumberColumn());
 		properties.setShowCreationTimeInDateColumn(criteriaBean.isShowCreationTimeInDateColumn());
 		properties.setCaseCodes(criteriaBean.getCaseCodesInList());
 		properties.setStatusesToShow(criteriaBean.getStatusesToShowInList());
 		properties.setStatusesToHide(criteriaBean.getStatusesToHideInList());
 		properties.setOnlySubscribedCases(criteriaBean.isOnlySubscribedCases());
 		properties.setComponentId(criteriaBean.getComponentId());
 		properties.setCriteriasId(criteriaBean.getCriteriasId());
 		properties.setFoundResults(criteriaBean.getFoundResults());
 		properties.setCasesListCustomizer(criteriaBean.getCasesListCustomizer());
 		properties.setCustomColumns(criteriaBean.getCustomColumns());
 		properties.setShowLoadingMessage(criteriaBean.isShowLoadingMessage());
 
 		UIComponent component = null;
 		if (CasesRetrievalManager.CASE_LIST_TYPE_USER.equals(criteriaBean.getCaseListType())) {
 			properties.setAddCredentialsToExernalUrls(false);
 			component = getCasesListBuilder().getUserCasesList(iwc, cases, null, properties);
 		} else {
 			properties.setShowCheckBoxes(false);
 			component = getCasesListBuilder().getCasesList(iwc, cases, properties);
 		}
 		if (component == null) {
 			LOGGER.warning("Unable to get UIComponent for cases list!");
 			return null;
 		}
 
 		return getBuilderLogic().getBuilderService(iwc).getRenderedComponent(iwc, component, true);
 	}
 
 	private IWResourceBundle getResourceBundle(IWContext iwc) {
 		return iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
 	}
 
 	private void addSearchQueryToSession(IWContext iwc, CasesListSearchCriteriaBean searchParams) {
 		List<AdvancedProperty> searchFields = new ArrayList<AdvancedProperty>();
 
 		Locale locale = iwc.getCurrentLocale();
 		IWResourceBundle iwrb = getResourceBundle(iwc);
 
 		if (!StringUtil.isEmpty(searchParams.getCaseNumber())) {
 			searchFields.add(new AdvancedProperty("case_nr", searchParams.getCaseNumber()));
 		}
 		if (!StringUtil.isEmpty(searchParams.getDescription())) {
 			searchFields.add(new AdvancedProperty("description", searchParams.getDescription()));
 		}
 		if (!StringUtil.isEmpty(searchParams.getName())) {
 			searchFields.add(new AdvancedProperty("name", searchParams.getName()));
 		}
 		if (!StringUtil.isEmpty(searchParams.getPersonalId())) {
 			searchFields.add(new AdvancedProperty("personal_id", searchParams.getPersonalId()));
 		}
 		if (!StringUtil.isEmpty(searchParams.getContact())) {
 			searchFields.add(new AdvancedProperty("contact", searchParams.getContact()));
 		}
 		if (!StringUtil.isEmpty(searchParams.getProcessId())) {
 			String processName = null;
 			try {
 				ProcessDefinitionW bpmCasesManager = ELUtil.getInstance().getBean(CasesBPMProcessDefinitionW.SPRING_BEAN_IDENTIFIER);
 				bpmCasesManager.setProcessDefinitionId(Long.valueOf(searchParams.getProcessId()));
 				processName = bpmCasesManager.getProcessName(locale);
 			} catch(Exception e) {
 				LOGGER.log(Level.WARNING, "Error getting process name by: " + searchParams.getProcessId());
 			}
 			searchFields.add(new AdvancedProperty("cases_search_select_process", StringUtil.isEmpty(processName) ? "general_cases" : processName));
 		}
 		if (!StringUtil.isEmpty(searchParams.getStatusId())) {
 			String status = null;
 			try {
 				status = getCasesBusiness(iwc).getLocalizedCaseStatusDescription(null, getCasesBusiness(iwc).getCaseStatus(searchParams.getStatusId()), locale);
 			} catch (Exception e) {
 				LOGGER.log(Level.WARNING, "Error getting status name by: " + searchParams.getStatusId(), e);
 			}
 			searchFields.add(new AdvancedProperty("status", StringUtil.isEmpty(status) ? iwrb.getLocalizedString("unknown_status", "Unknown") : status));
 		}
 		if (!StringUtil.isEmpty(searchParams.getDateRange())) {
 			searchFields.add(new AdvancedProperty("date_range", searchParams.getDateRange()));
 		}
 		if (!ListUtil.isEmpty(searchParams.getProcessVariables())) {
 			for (BPMProcessVariable variable: searchParams.getProcessVariables()) {
 				String value = variable.getValue();
 				String name = variable.getName();
 				if (CaseHandlerAssignmentHandler.handlerUserIdVarName.equals(name) || CaseHandlerAssignmentHandler.performerUserIdVarName.equals(name)
 						|| name.startsWith(VariableInstanceType.OBJ_LIST.getPrefix()) || name.startsWith(VariableInstanceType.LIST.getPrefix())
 						|| !StringUtil.isEmpty(isResolverExist(MultipleSelectionVariablesResolver.BEAN_NAME_PREFIX + variable.getName()))
 						) {
 					MultipleSelectionVariablesResolver resolver = null;
 					try {
 						resolver = ELUtil.getInstance().getBean(MultipleSelectionVariablesResolver.BEAN_NAME_PREFIX + variable.getName());
 					} catch (Throwable e) {}
 					if (resolver != null)
 						value = resolver.getPresentation(variable);
 				}
 
 				searchFields.add(new AdvancedProperty(iwrb.getLocalizedString(JBPMConstants.VARIABLE_LOCALIZATION_PREFIX.concat(variable.getName()), variable.getName()), value));
 			}
 		}
 		iwc.setSessionAttribute(GeneralCasesListBuilder.USER_CASES_SEARCH_QUERY_BEAN_ATTRIBUTE, searchFields);
 	}
 
 	private List<CasesListSearchFilter> getFilters(ServletContext servletContext, CasesSearchCriteriaBean criterias) {
 		List<CasesListSearchFilter> filtersList = new ArrayList<CasesListSearchFilter>();
 
 		try {
 			WebApplicationContext webAppContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
 			Map<?, ?> filters = webAppContext.getBeansOfType(CasesListSearchFilter.class);
 			if (filters == null || filters.isEmpty()) {
 				return filtersList;
 			}
 
 			for (Object filterObject: filters.values()) {
 				CasesListSearchFilter filter = (CasesListSearchFilter) filterObject;
 				filter.setCriterias(criterias);
 				filtersList.add(filter);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return filtersList;
 	}
 
 	@Override
 	public PagedDataCollection<CasePresentation> getCasesByQuery(CasesSearchCriteriaBean criteriaBean) {
 		if (criteriaBean instanceof CasesListSearchCriteriaBean)
 			return getCasesByQuery(CoreUtil.getIWContext(), (CasesListSearchCriteriaBean) criteriaBean);
 
 		getLogger().warning("Unable to get cases by query " + criteriaBean + " because it is not instance of " +
 				CasesListSearchCriteriaBean.class.getName() + ". Actual implementation: " + criteriaBean.getClass().getName());
 		return null;
 	}
 
 	private boolean isPagingTurnedOn() {
 		return IWMainApplication.getDefaultIWMainApplication().getSettings().getBoolean(UICasesList.DYNAMIC_CASES_NAVIGATOR, Boolean.TRUE);
 	}
 
 	private PagedDataCollection<CasePresentation> getCasesByQuery(IWContext iwc, CasesListSearchCriteriaBean criteriaBean) {
 		User currentUser = null;
 		if (!iwc.isLoggedOn() || (currentUser = iwc.getCurrentUser()) == null) {
 			LOGGER.info("Not logged in, skipping searching");
 			return null;
 		}
 
 		long start = System.currentTimeMillis();
 		String casesProcessorType = criteriaBean.getCaseListType() == null ? CasesRetrievalManager.CASE_LIST_TYPE_OPEN : criteriaBean.getCaseListType();
 		List<Integer> casesIds = null;
 		try {
 			if (criteriaBean.isShowAllCases()) {
 				CasesBusiness casesBusiness = getCasesBusiness(iwc);
 				@SuppressWarnings("unchecked")
 				Collection<CaseStatus> statuses = casesBusiness.getCaseStatuses();
 				StringBuffer allStatuses = null;
 				if (!ListUtil.isEmpty(statuses)) {
 					allStatuses = new StringBuffer();
 					for (Iterator<CaseStatus> statusesIter = statuses.iterator(); statusesIter.hasNext();) {
 						allStatuses.append(statusesIter.next().getStatus());
 						if (statusesIter.hasNext())
 							allStatuses.append(CoreConstants.COMMA);
 					}
 				}
 
 				criteriaBean.setStatusesToShow(allStatuses == null ? null : allStatuses.toString());
 				criteriaBean.setStatusesToHide(null);
 			}
 
 			casesIds = getCaseManagersProvider().getCaseManager().getCaseIds(currentUser, casesProcessorType, criteriaBean.getCaseCodesInList(),
 					criteriaBean.getStatusesToHideInList(), criteriaBean.getStatusesToShowInList(), criteriaBean.isOnlySubscribedCases(),
 					criteriaBean.isShowAllCases(), criteriaBean.getProcInstIds());
 		} catch (Exception e) {
 			LOGGER.log(Level.WARNING, "Some error occured getting cases by criterias: " + criteriaBean, e);
 		}
 		if (ListUtil.isEmpty(casesIds))
 			return null;
 
 		long end = System.currentTimeMillis();
 		LOGGER.info("Cases IDs were resolved in " + (end - start) + " ms");
 
 		if (criteriaBean.getStatusId() != null)
 			criteriaBean.setStatuses(new String[] {criteriaBean.getStatusId()});
 
 		PagedDataCollection<CasePresentation> cases = null;
 
 		if (ListUtil.isEmpty(criteriaBean.getProcInstIds())) {
 			List<CasesListSearchFilter> filters = getFilters(iwc.getServletContext(), criteriaBean);
 			if (ListUtil.isEmpty(filters))
 				return null;
 
 			for (CasesListSearchFilter filter: filters)
 				casesIds = filter.doFilter(casesIds);
 		} else {
 			List<Integer> ids = casesBPMDAO.getCasesIdsByProcInstIds(criteriaBean.getProcInstIds());
 			if (ListUtil.isEmpty(ids))
 				return null;
 
 			casesIds = DefaultCasesListSearchFilter.getNarrowedResults(casesIds, ids);
 		}
 		start = System.currentTimeMillis();
 		LOGGER.info("Searh was executed in " + (start - end) + " ms");
 
 		if (ListUtil.isEmpty(casesIds))
 			return null;
 
 		int totalCount = 0;
 		int count = 0;
 		int startIndex = 0;
 		boolean usePaging = isPagingTurnedOn();
 		if (usePaging) {
 			Comparator<Integer> c = new Comparator<Integer>() {
 				@Override
 				public int compare(Integer o1, Integer o2) {
 					return -o1.compareTo(o2);
 				}
 			};
 			Collections.sort(casesIds, c);
 
 			totalCount = casesIds.size();
 			criteriaBean.setFoundResults(totalCount);
 			if (criteriaBean.getPageSize() <= 0)
 				criteriaBean.setPageSize(20);
 			if (criteriaBean.getPage() <= 0)
 				criteriaBean.setPage(1);
 			criteriaBean.setAllDataLoaded(!(totalCount > criteriaBean.getPageSize()));
 			count = criteriaBean.getPageSize();
 			startIndex = (criteriaBean.getPage() - 1) * count;
 		} else {
 			criteriaBean.setPageSize(-1);
 		}
 
 		boolean noSortingOptions = ListUtil.isEmpty(criteriaBean.getSortingOptions());
 		Locale locale = iwc.getCurrentLocale();
 		if (!ListUtil.isEmpty(casesIds)) {
 			if (usePaging && noSortingOptions) {
 				//	No need to load all the cases, just for one page
 				casesIds = getSubList(casesIds, startIndex, count, totalCount);
 			}
 			cases = getCaseManagersProvider().getCaseManager().getCasesByIds(casesIds, locale);
 		}
 
 		if (cases == null || ListUtil.isEmpty(cases.getCollection()))
 			return null;
 
 		List<CasePresentation> casesToSort = new ArrayList<CasePresentation>(cases.getCollection());
 		if (usePaging && !noSortingOptions) {
 			//	Loaded all the cases (heavy task), will sort by user's preferences
 			casesToSort = getSubList(casesToSort, startIndex, count, totalCount);
 		}
 
 		Collections.sort(casesToSort, getCasePresentationComparator(criteriaBean, locale));
 
 		cases = new PagedDataCollection<CasePresentation>(casesToSort);
 		LOGGER.info("Sorting and paging was executed in " + (System.currentTimeMillis() - start) + " ms");
 		return cases;
 	}
 
 	private <T> List<T> getSubList(List<T> list, int startIndex, int count, int totalCount) {
 		if (startIndex + count < totalCount) {
 			return list.subList(startIndex, (startIndex + count));
 		} else {
 			return list.subList(startIndex, totalCount);
 		}
 	}
 
 	private CasePresentationComparator getCasePresentationComparator(CasesListSearchCriteriaBean searchCriterias, Locale locale) {
 		return (searchCriterias == null || ListUtil.isEmpty(searchCriterias.getSortingOptions())) ?
 				new CasePresentationComparator() :
 				new BPMCasePresentationComparator(locale, searchCriterias);
 	}
 
 	private BuilderLogicWrapper getBuilderLogic() {
 		return builderLogic;
 	}
 
 	private GeneralCasesListBuilder getCasesListBuilder() {
 		return casesListBuilder;
 	}
 
 	private CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
 		try {
 			return IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
 		} catch (IBOLookupException ile) {
 			LOGGER.log(Level.SEVERE, "Error getting CasesBusiness", ile);
 		}
 
 		return null;
 	}
 
 	private CasesBPMProcessView getCasesBPMProcessView() {
 		return casesBPMProcessView;
 	}
 
 	@Override
 	public RenderedComponent getVariablesWindow(String processDefinitionId) {
 		RenderedComponent fake = new RenderedComponent();
 		fake.setErrorMessage("There are no variables for selected process!");
 
 		IWContext iwc = CoreUtil.getIWContext();
 		if (iwc == null) {
 			return fake;
 		}
 
 		List<String> resources = new ArrayList<String>();
 		resources.add(web2.getBundleUriToHumanizedMessagesStyleSheet());
 		resources.add(jQuery.getBundleURIToJQueryLib());
 		resources.add(web2.getBundleUriToHumanizedMessagesScript());
 		fake.setResources(resources);
 
 		IWResourceBundle iwrb = getResourceBundle(iwc);
 		fake.setErrorMessage(iwrb.getLocalizedString("cases_search.there_are_no_variables_for_selected_process", fake.getErrorMessage()));
 
 		if (!iwc.isLoggedOn()) {
 			LOGGER.warning("User must be logged!");
 			return fake;
 		}
 
 		if (StringUtil.isEmpty(processDefinitionId)) {
 			return fake;
 		}
 		Long pdId = null;
 		try {
 			pdId = Long.valueOf(processDefinitionId);
 		} catch(NumberFormatException e) {
 			LOGGER.severe("Unable to convert to Long: " + processDefinitionId);
 		}
 		if (pdId == null) {
 			return fake;
 		}
 
 		BPMProcessVariablesBean variablesBean = null;
 		try {
 			variablesBean = ELUtil.getInstance().getBean(BPMProcessVariablesBean.SPRING_BEAN_IDENTIFIER);
 		} catch(Exception e) {
 			LOGGER.log(Level.SEVERE, "Error getting bean: " + BPMProcessVariablesBean.class.getName(), e);
 		}
 		if (variablesBean == null) {
 			return fake;
 		}
 
 		variablesBean.setProcessDefinitionId(pdId);
 
 		return getBuilderLogic().getBuilderService(iwc).getRenderedComponentByClassName(UIProcessVariables.class.getName(), null);
 	}
 
 	private CasesSearchResultsHolder getSearchResultsHolder() throws NullPointerException {
 		CasesSearchResultsHolder resultsHolder = null;
 		try {
 			resultsHolder = ELUtil.getInstance().getBean(CasesSearchResultsHolder.SPRING_BEAN_IDENTIFIER);
 		} catch(Exception e) {
 			LOGGER.log(Level.WARNING, "Error getting bean for search results holder: " + CasesSearchResultsHolder.class.getName(), e);
 		}
 
 		if (resultsHolder == null) {
 			throw new NullPointerException();
 		}
 
 		return resultsHolder;
 	}
 
 	private boolean setSearchResults(IWContext iwc, Collection<CasePresentation> cases, CasesListSearchCriteriaBean criteriaBean) {
 		iwc.setSessionAttribute(GeneralCasesListBuilder.USER_CASES_SEARCH_SETTINGS_ATTRIBUTE, criteriaBean);
 
 		CasesSearchResultsHolder resultsHolder = null;
 		try {
 			resultsHolder = getSearchResultsHolder();
 		} catch(NullPointerException e) {
 			return false;
 		}
 
 		String id = criteriaBean.getId();
 		resultsHolder.setSearchResults(id, new CasesSearchResults(id, cases, criteriaBean));
 		return true;
 	}
 
 	private String getType(BuilderService service, String pageKey, String instanceId) {
 		String type = getProperty(service, pageKey, instanceId, ":method:1:implied:void:setType:java.lang.String:");
 		return StringUtil.isEmpty(type) ? CasesRetrievalManager.CASE_LIST_TYPE_OPEN : type;
 	}
 
 	private List<String> getCodes(BuilderService service, String pageKey, String instanceId) {
 		String codes = getProperty(service, pageKey, instanceId, ":method:1:implied:void:setCaseCodes:java.lang.String:");
 		return StringUtil.isEmpty(codes) ? null : new ArrayList<String>(Arrays.asList(codes.split(CoreConstants.COMMA)));
 	}
 
 	private List<String> getStatusesToHide(BuilderService service, String pageKey, String instanceId) {
 		String hide = getProperty(service, pageKey, instanceId, ":method:1:implied:void:setCaseStatusesToHide:java.lang.String:");
 		return StringUtil.isEmpty(hide) ? null : new ArrayList<String>(Arrays.asList(hide.split(CoreConstants.COMMA)));
 	}
 
 	private List<String> getStatusesToShow(BuilderService service, String pageKey, String instanceId) {
 		String show = getProperty(service, pageKey, instanceId, ":method:1:implied:void:setCaseStatusesToShow:java.lang.String:");
 		return StringUtil.isEmpty(show) ? null : new ArrayList<String>(Arrays.asList(show.split(CoreConstants.COMMA)));
 	}
 
 	private boolean isShowSubscribedOnly(BuilderService service, String pageKey, String instanceId) {
 		String subscribedOnly = getProperty(service, pageKey, instanceId, ":method:1:implied:void:setOnlySubscribedCases:boolean:");
 		return StringUtil.isEmpty(subscribedOnly) ? false : "T".equals(subscribedOnly) || Boolean.TRUE.toString().equals(subscribedOnly);
 	}
 
 	private String getProperty(BuilderService service, String pageKey, String instanceId, String property) {
 		return service.getProperty(pageKey, instanceId, property);
 	}
 
 	@Override
 	public AdvancedProperty getExportedCases(String instanceId, String uri) {
 		if (StringUtil.isEmpty(instanceId))
 			return null;
 
 		IWContext iwc = CoreUtil.getIWContext();
 		if (iwc == null || !iwc.isLoggedOn())
 			return null;
 
 		BuilderService service = getBuilderLogic().getBuilderService(iwc);
 		String pageKey = service.getPageKeyByURI(uri);
 
 		User user = iwc.getCurrentUser();
 
 		String type = getType(service, pageKey, instanceId);
 		List<String> caseCodes = getCodes(service, pageKey, instanceId);
 		List<String> statusesToHide = getStatusesToHide(service, pageKey, instanceId);
 		List<String> statusesToShow = getStatusesToShow(service, pageKey, instanceId);
 		boolean onlySubscribedCases = isShowSubscribedOnly(service, pageKey, instanceId);
 
 		PagedDataCollection<CasePresentation> cases = getCaseManagersProvider().getCaseManager().getCases(user, type, iwc.getCurrentLocale(),
 				caseCodes, statusesToHide, statusesToShow, Integer.MAX_VALUE, -1, onlySubscribedCases, true);
 
 		AdvancedProperty result = new AdvancedProperty(Boolean.FALSE.toString());
 		String errorMessage = getResourceBundle(iwc).getLocalizedString("unable_to_export_all_cases", "Sorry, unable to export cases to Excel");
 
 		if (cases == null || ListUtil.isEmpty(cases.getCollection())) {
 			result.setValue(errorMessage);
 			return result;
 		}
 
 		CasesSearchResultsHolder resultsHolder = null;
 		try {
 			resultsHolder = getSearchResultsHolder();
 		} catch (NullPointerException e) {
 			result.setValue(errorMessage);
 			return result;
 		}
 
 		List<CasePresentation> casesToExport = new ArrayList<CasePresentation>(cases.getCollection());
 		if (!resultsHolder.setCasesToExport(instanceId, casesToExport)) {
 			result.setValue(errorMessage);
 			return result;
 		}
 
 		result.setId(Boolean.TRUE.toString());
 		URIUtil uriUtil = new URIUtil(iwc.getIWMainApplication().getMediaServletURI());
 		uriUtil.setParameter(MediaWritable.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(CasesSearchResultsExporter.class));
 		uriUtil.setParameter(CasesSearchResultsExporter.ALL_CASES_DATA, instanceId);
 		result.setValue(uriUtil.getUri());
 
 		return result;
 	}
 
 	@Override
 	public AdvancedProperty getExportedSearchResults(String id) {
 		IWContext iwc = CoreUtil.getIWContext();
 		if (iwc == null) {
 			return null;
 		}
 
 		AdvancedProperty result = new AdvancedProperty(Boolean.FALSE.toString());
 
 		CasesSearchResultsHolder resultsHolder = null;
 		try {
 			resultsHolder = getSearchResultsHolder();
 		} catch(NullPointerException e) {
 			result.setValue(getResourceBundle(iwc).getLocalizedString("unable_to_export_search_results", "Sorry, unable to export search results to Excel"));
 			return result;
 		}
 
 		CasesSearchCriteriaBean criterias = resultsHolder.getSearchCriteria(id);
 		if (criterias != null) {
 			criterias.setPage(-1);
 			criterias.setPageSize(Integer.MAX_VALUE);
 		}
 		if (!resultsHolder.isSearchResultStored(id) || !resultsHolder.isAllDataLoaded(id)) {
 			Collection<CasePresentation> cases = getReLoadedCases(resultsHolder, criterias, id);
 			if (ListUtil.isEmpty(cases)) {
 				result.setValue(getResourceBundle(iwc).getLocalizedString("no_search_results_to_export", "There are no search results to export!"));
 				return result;
 			} else if (criterias != null) {
 				criterias.setPageSize(cases.size());
 				criterias.setAllDataLoaded(Boolean.TRUE);
 			}
 		}
 
 		getExternalSearchResults(resultsHolder, id);
 		if (!resultsHolder.doExport(id)) {
 			result.setValue(getResourceBundle(iwc).getLocalizedString("unable_to_export_search_results", "Sorry, unable to export search results to Excel"));
 			return result;
 		}
 
 		result.setId(Boolean.TRUE.toString());
 		URIUtil uriUtil = new URIUtil(iwc.getIWMainApplication().getMediaServletURI());
 		uriUtil.setParameter(MediaWritable.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(CasesSearchResultsExporter.class));
 		uriUtil.setParameter(CasesSearchResultsExporter.ID_PARAMETER, id);
 		result.setValue(uriUtil.getUri());
 
 		return result;
 	}
 
 	private CaseManagersProvider getCaseManagersProvider() {
 		return caseManagersProvider;
 	}
 
 	@Override
 	public boolean clearSearchResults(String id) {
 		IWContext iwc = CoreUtil.getIWContext();
 		iwc.removeSessionAttribute(GeneralCasesListBuilder.USER_CASES_SEARCH_QUERY_BEAN_ATTRIBUTE);
 		iwc.removeSessionAttribute(GeneralCasesListBuilder.USER_CASES_SEARCH_SETTINGS_ATTRIBUTE);
 		setCasesPagerAttributes(-1, -1);
 		return getSearchResultsHolder().clearSearchResults(id);
 	}
 
 	@Override
 	public List<AdvancedProperty> getDefaultSortingOptions(IWContext iwc) {
 		List<AdvancedProperty> defaultSortingOptions = new ArrayList<AdvancedProperty>();
 
 		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(CasesConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
 
 		defaultSortingOptions.add(new AdvancedProperty("getCaseIdentifier", iwrb.getLocalizedString("case_nr", "Case nr.")));
 		defaultSortingOptions.add(new AdvancedProperty("getSubject", iwrb.getLocalizedString("description", "Description")));
 		defaultSortingOptions.add(new AdvancedProperty("getOwnerName", iwrb.getLocalizedString("sender", "Sender")));
 		defaultSortingOptions.add(new AdvancedProperty("getCreated", iwrb.getLocalizedString("created_date", "Created date")));
 		defaultSortingOptions.add(new AdvancedProperty("getCaseStatusLocalized", iwrb.getLocalizedString("status", "Status")));
 
 		return defaultSortingOptions;
 	}
 
 	@Override
 	public void onApplicationEvent(ApplicationEvent event) {
 		if (event instanceof CaseModifiedEvent) {
 			Thread cacheReseter = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						getCache(DefaultCasesListSearchFilter.SEARCH_FILTER_CACHE_NAME, DefaultCasesListSearchFilter.SEARCH_FILTER_CACHE_TTL).clear();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			cacheReseter.start();
 		} else if (event instanceof UserHasLoggedInEvent) {
 			doLoadCases(((UserHasLoggedInEvent) event).getUserId());
 		}
 	}
 
 	private void doLoadCases(final Integer userId) {
 		if (!getApplication().getSettings().getBoolean("load_cases_on_user_login", Boolean.TRUE))
 			return;
 
 		if (userId == null || userId < 0)
 			return;
 
 		Thread loader = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				UserBusiness userBusiness = getServiceInstance(UserBusiness.class);
 				User user = null;
 				try {
 					user = userBusiness.getUser(userId);
 				} catch (Exception e) {}
 				if (user == null)
 					return;
 
 				BuilderLogic builder = BuilderLogic.getInstance();
 				IWApplicationContext iwac = getApplication().getIWApplicationContext();
 				BuilderService service = getBuilderLogic().getBuilderService(iwac);
 
 				@SuppressWarnings("unchecked")
 				List<Class<? extends CaseBlock>> classes = Arrays.asList(
 						OpenCases.class,
 						ClosedCases.class,
 						MyCases.class,
 						PublicCases.class,
 						UserCases.class
 				);
 				for (Class<? extends CaseBlock> theClass: classes) {
 					Collection<ICObjectInstance> instances = null;
 					try {
 						ICObjectInstanceHome instanceHome = (ICObjectInstanceHome) IDOLookup.getHome(ICObjectInstance.class);
 						instances = instanceHome.getByClassName(theClass);
 					} catch (FinderException e) {
 					} catch (Exception e) {}
 					if (ListUtil.isEmpty(instances))
 						continue;
 
 					for (ICObjectInstance instance: instances) {
						if (instance == null)
							continue;
						String uniqueId = instance.getUniqueId();
						if (StringUtil.isEmpty(uniqueId))
 							continue;
 
						String instanceId = ICObjectBusiness.UUID_PREFIX.concat(uniqueId);
 						ICPage page = builder.findPageForModule(iwac, instanceId);
 						if (page == null || page.getDeleted())
 							continue;
 						String pageKey = page.getId();
 
 						String type = getType(service, pageKey, instanceId);
 						List<String> caseCodes = getCodes(service, pageKey, instanceId);
 						List<String> statusesToHide = getStatusesToHide(service, pageKey, instanceId);
 						List<String> statusesToShow = getStatusesToShow(service, pageKey, instanceId);
 						boolean onlySubscribedCases = isShowSubscribedOnly(service, pageKey, instanceId);
 
 						try {
 							getCaseManagersProvider().getCaseManager().getCaseIds(user, type, caseCodes, statusesToHide, statusesToShow,
 									onlySubscribedCases, false);
 						} catch (Exception e) {
 							getLogger().log(Level.WARNING, "Error loading cases for list " + theClass.getName() + " for user " + user +
 									" after login event", e);
 						}
 					}
 				}
 			}
 		});
 		loader.start();
 	}
 
 	@Override
 	public Collection<CasePresentation> getReLoadedCases(CasesSearchCriteriaBean criterias) {
 		Collection<CasePresentation> reLoadedCases = getReLoadedCases(null, criterias, null);
 		if (!ListUtil.isEmpty(reLoadedCases))
 			criterias.setAllDataLoaded(Boolean.FALSE);
 		return reLoadedCases;
 	}
 
 	private Collection<CasePresentation> getReLoadedCases(CasesSearchResultsHolder resultsHolder, CasesSearchCriteriaBean criterias, String id) {
 		if (criterias instanceof CasesListSearchCriteriaBean) {
 			IWContext iwc = CoreUtil.getIWContext();
 			CasesListSearchCriteriaBean listCriterias = (CasesListSearchCriteriaBean) criterias;
 
 			PagedDataCollection<CasePresentation> cases = getCasesByQuery(iwc, listCriterias);
 			if (cases == null)
 				cases = new PagedDataCollection<CasePresentation>(new ArrayList<CasePresentation>());
 
 			listCriterias.setAllDataLoaded(Boolean.TRUE);
 			listCriterias.setPageSize(cases.getTotalCount());
 			setSearchResults(iwc, cases.getCollection(), listCriterias);
 
 			if (resultsHolder != null && id != null) {
 				Collection<CasePresentation> externalData = getExternalSearchResults(resultsHolder, id);
 				if (ListUtil.isEmpty(cases.getCollection())) {
 					return externalData;
 				}
 			}
 
 			return cases.getCollection();
 		}
 
 		getLogger().warning("Unable to get cases by query " + criterias + " because it is not instance of " +
 				CasesListSearchCriteriaBean.class.getName() + ". Actual implementation: " + criterias.getClass().getName());
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<CasePresentation> getExternalSearchResults(CasesSearchResultsHolder resultsHolder, String id) {
 		Map<String, ? extends ExternalCasesDataExporter> externalExporters = null;
 		try {
 			externalExporters = WebApplicationContextUtils.getWebApplicationContext(getApplication().getServletContext()).getBeansOfType(ExternalCasesDataExporter.class);
 		} catch (Exception e) {
 			getLogger().log(Level.WARNING, "Error getting beans of type: " + ExternalCasesDataExporter.class, e);
 		}
 
 		if (externalExporters == null || externalExporters.isEmpty()) {
 			return null;
 		}
 
 		List<CasePresentation> data = new ArrayList<CasePresentation>();
 		for (ExternalCasesDataExporter externalExporter: externalExporters.values()) {
 			List<CasePresentation> externalData = externalExporter.getExternalData(id);
 			resultsHolder.concatExternalData(id, externalData);
 
 			if (!ListUtil.isEmpty(externalData)) {
 				data.addAll(externalData);
 			}
 		}
 
 		return data;
 	}
 
 	@Override
 	public String getCaseStatus(Long processInstanceId) {
 		if (processInstanceId == null) {
 			LOGGER.warning("Process instance id is not provided");
 			return null;
 		}
 
 		CaseProcInstBind bind = null;
 		try {
 			bind = casesBPMDAO.getCaseProcInstBindByProcessInstanceId(processInstanceId);
 		} catch (Exception e) {
 			LOGGER.log(Level.WARNING, "Error while getting instance of " + CaseProcInstBind.class.getName() + " by process instance: " + processInstanceId, e);
 		}
 		if (bind == null) {
 			return null;
 		}
 
 		CasePresentation theCase = null;
 		try {
 			theCase = caseManagersProvider.getCaseManager().getCaseByIdLazily(bind.getCaseId());
 		} catch (Exception e) {
 			LOGGER.log(Level.WARNING, "Error getting case by id: " + bind.getCaseId(), e);
 		}
 
 		return theCase == null ? null : theCase.getLocalizedStatus();
 	}
 
 	@Override
 	public boolean setCasesPagerAttributes(int page, int pageSize) {
 		page = page <= 0 ? 1 : page;
 		pageSize = pageSize <= 0 ? 20 : pageSize;
 
 		String key = "userCases";
 		IWContext iwc = CoreUtil.getIWContext();
 		iwc.setSessionAttribute(ListNavigator.PARAMETER_CURRENT_PAGE + "_" + key, page);
 		iwc.setSessionAttribute(ListNavigator.PARAMETER_NUMBER_OF_ENTRIES + "_" + key, pageSize);
 
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see is.idega.idegaweb.egov.cases.business.CasesEngine#getAvailableProcesses(com.idega.presentation.IWContext)
 	 */
 	@Override
 	public List<AdvancedProperty> getAvailableProcesses(IWContext iwc) {
 		ApplicationBusiness appBusiness = null;
 		try {
 			appBusiness = (ApplicationBusiness) IBOLookup.getServiceInstance(iwc, ApplicationBusiness.class);
 		} catch (IBOLookupException e) {
 			e.printStackTrace();
 		}
 
 		if (appBusiness == null) {
 			return null;
 		}
 
 		ApplicationType appType = null;
 		try {
 			appType = ELUtil.getInstance().getBean("appTypeBPM");
 		} catch(Exception e) {
 			LOGGER.log(Level.WARNING, "Unable to get application type", e);
 			return null;
 		}
 
 		Collection<Application> bpmApps = appBusiness.getApplicationsByType(appType.getType());
 		if (ListUtil.isEmpty(bpmApps)) {
 			return null;
 		}
 
 		CaseManagersProvider caseManagersProvider = ELUtil.getInstance().getBean(CaseManagersProvider.beanIdentifier);
 		if (caseManagersProvider == null) {
 			return null;
 		}
 
 		CasesRetrievalManager caseManager = caseManagersProvider.getCaseManager();
 		if (caseManager == null) {
 			return null;
 		}
 
 		List<AdvancedProperty> allProcesses = new ArrayList<AdvancedProperty>();
 
 		String processId = null;
 		String processName = null;
 		String localizedName = null;
 		Locale locale = iwc.getCurrentLocale();
 		for (Application bpmApp: bpmApps) {
 			processId = null;
 			processName = bpmApp.getUrl();
 			localizedName = processName;
 
 			if (appType.isVisible(bpmApp)) {
 
 				if (StringUtil.isEmpty(processId)) {
 					processId = String.valueOf(caseManager.getLatestProcessDefinitionIdByProcessName(processName));
 				}
 
 				localizedName = caseManager.getProcessName(processName, locale);
 
 				if (!StringUtil.isEmpty(processId)) {
 					allProcesses.add(new AdvancedProperty(processId, localizedName));
 				}
 			}
 			else {
 				LOGGER.warning(new StringBuilder("Application '").append(bpmApp.getName()).append("' is not accessible")
 						.append((iwc.isLoggedOn() ? " for user: " + iwc.getCurrentUser() : ": user must be logged in!")).toString());
 			}
 		}
 
 		if (ListUtil.isEmpty(allProcesses)) {
 			return null;
 		}
 
 		Collections.sort(allProcesses, new AdvancedPropertyComparator(iwc.getCurrentLocale()));
 
 		return allProcesses;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see is.idega.idegaweb.egov.cases.business.CasesEngine#isResolverExist(java.lang.String)
 	 */
 	@Override
 	public String isResolverExist(String beanName) {
 		Map<String, String> cache = getResolversCache();
 		if (cache == null) {
 			getLogger().log(Level.WARNING, "Unable to get cache!");
 			return null;
 		}
 
 		if (cache.containsKey(beanName))
 			return cache.get(beanName);
 
 		try {
 			MultipleSelectionVariablesResolver resolver = ELUtil.getInstance().getBean(beanName);
 
 			if (resolver != null) {
 				String className = resolver.getPresentationClass().getName();
 				cache.put(beanName, className);
 				return className;
 			}
 		} catch (Throwable e) {}
 
 		cache.put(beanName, CoreConstants.EMPTY);
 		return CoreConstants.EMPTY;
 	}
 
 	private Map<String, String> getResolversCache() {
 		return getCache("BEAN_NAMES_FOR_BPM_PROCESS_SEARCH");
 	}
 
 	@Override
 	public boolean showCaseAssets() {
 		try {
 			CasesBPMAssetsState assetsState = ELUtil.getInstance().getBean(CasesBPMAssetsState.beanIdentifier);
 			assetsState.showAssets();
 			return true;
 		} catch (Exception e) {
 			getLogger().log(Level.WARNING, "Error while setting instruction to show assets", e);
 		}
 		return false;
 	}
 }
