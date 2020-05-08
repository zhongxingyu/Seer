 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.monitoring.client.presenter;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.ebayopensource.turmeric.monitoring.client.ConsoleUtil;
 import org.ebayopensource.turmeric.monitoring.client.Dashboard;
 import org.ebayopensource.turmeric.monitoring.client.Util;
 import org.ebayopensource.turmeric.monitoring.client.event.DateFilterSelectionEvent;
 import org.ebayopensource.turmeric.monitoring.client.event.DateFilterSelectionHandler;
 import org.ebayopensource.turmeric.monitoring.client.event.GetServicesEvent;
 import org.ebayopensource.turmeric.monitoring.client.event.GetServicesEventHandler;
 import org.ebayopensource.turmeric.monitoring.client.event.ObjectSelectionEvent;
 import org.ebayopensource.turmeric.monitoring.client.event.ObjectSelectionEventHandler;
 import org.ebayopensource.turmeric.monitoring.client.model.ErrorCriteria;
 import org.ebayopensource.turmeric.monitoring.client.model.ErrorDetail;
 import org.ebayopensource.turmeric.monitoring.client.model.ErrorMetric;
 import org.ebayopensource.turmeric.monitoring.client.model.ErrorTimeSlotData;
 import org.ebayopensource.turmeric.monitoring.client.model.FilterContext;
 import org.ebayopensource.turmeric.monitoring.client.model.Filterable;
 import org.ebayopensource.turmeric.monitoring.client.model.HistoryToken;
 import org.ebayopensource.turmeric.monitoring.client.model.MetricCriteria;
 import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService;
 import org.ebayopensource.turmeric.monitoring.client.model.ObjectType;
 import org.ebayopensource.turmeric.monitoring.client.model.SelectionContext;
 import org.ebayopensource.turmeric.monitoring.client.model.ErrorMetricData;
 import org.ebayopensource.turmeric.monitoring.client.model.TimeSlotData;
 import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.ErrorCategory;
 import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.ErrorSeverity;
 import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.ErrorType;
 import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.Ordering;
 import org.ebayopensource.turmeric.monitoring.client.model.MetricsQueryService.Perspective;
 import org.ebayopensource.turmeric.monitoring.client.presenter.Presenter.TabPresenter;
 import org.ebayopensource.turmeric.monitoring.client.util.GraphUtil;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.HasSelectionHandlers;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasText;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.TreeItem;
 
 /**
  * The Class ErrorPresenter.
  */
 public class ErrorPresenter implements TabPresenter {
     
     /** The min aggregation period. */
     protected long minAggregationPeriod = MetricCriteria.minAggregationPeriod;
 
     /** The Constant ERROR_ID. */
     public final static String ERROR_ID = "Error";
     
     /** The Constant DEFAULT_ERROR_TYPE. */
     public final static ErrorType DEFAULT_ERROR_TYPE = ErrorType.Category;
     
     /** The Constant CATEGORY_METRICS. */
     public final static List<ErrorMetric> CATEGORY_METRICS = Arrays.asList(ErrorMetric.CATEGORY_METRICS);
     
     /** The Constant SEVERITY_METRICS. */
     public final static List<ErrorMetric> SEVERITY_METRICS = Arrays.asList(ErrorMetric.SEVERITY_METRICS);
     
     /** The view. */
     protected Display view;
     
     /** The event bus. */
     protected HandlerManager eventBus;
     
     /** The query service. */
     protected MetricsQueryService queryService;
     
     /** The services list. */
     protected Map<String,Set<String>> servicesList;
     
     /** The selection context. */
     protected SelectionContext selectionContext;
     
     /** The selected duration hrs. */
     protected int selectedDurationHrs;
     
     /** The selected date1. */
     protected long selectedDate1;
     
     /** The selected date2. */
     protected long selectedDate2;
     
     /** The selected metrics. */
     protected List<ErrorMetric> selectedMetrics;
     
     
     /**
      * The Interface Display.
      */
     public interface Display extends org.ebayopensource.turmeric.monitoring.client.Display {
         
         /**
          * Reset.
          */
         public void reset();
         
         /**
          * Error.
          *
          * @param s the s
          */
         public void error(String s);
         
         /**
          * Gets the filter.
          *
          * @return the filter
          */
         public Filterable getFilter();
         
         /**
          * Gets the selector.
          *
          * @return the selector
          */
         public HasSelectionHandlers<TreeItem> getSelector();
         
         /**
          * Sets the services map.
          *
          * @param map the map
          */
         public void setServicesMap(Map<String, Set<String>> map);
         
         /**
          * Sets the selection.
          *
          * @param selections the selections
          */
         public void setSelection(Map<ObjectType,String>selections);
         
         /**
          * Sets the filter label.
          *
          * @param label the new filter label
          */
         public void setFilterLabel (String label);
         
         /**
          * Sets the error detail.
          *
          * @param ed the new error detail
          */
         public void setErrorDetail(ErrorDetail ed);
         
         /**
          * Sets the error metric data.
          *
          * @param m the m
          * @param errorData the error data
          */
         public void setErrorMetricData (ErrorMetric m, ErrorMetricData errorData);
         
         /**
          * Gets the table column.
          *
          * @param metric the metric
          * @param col the col
          * @return the table column
          */
         public List<HasClickHandlers> getTableColumn (ErrorMetric metric, int col);
         
         /**
          * Sets the download url.
          *
          * @param metric the metric
          * @param url the url
          */
         public void setDownloadUrl(ErrorMetric metric, String url);
 
         public void setServiceSystemErrorTrendData(List<ErrorTimeSlotData> dataRanges, String graphTitle);
 
         public void setServiceApplicationErrorTrendData(List<ErrorTimeSlotData> dataRanges, String graphTitle);
 
         public void setServiceRequestErrorTrendData(List<ErrorTimeSlotData> dataRanges, String graphTitle);
     }
     
     /**
      * Instantiates a new error presenter.
      *
      * @param eventBus the event bus
      * @param view the view
      * @param queryService the query service
      */
     public ErrorPresenter (HandlerManager eventBus, Display view, MetricsQueryService queryService) {
         this.eventBus = eventBus;
         this.view = view;
         this.view.setAssociatedId(ERROR_ID);
         this.queryService = queryService;
         bind();
     }
     
     
     /* (non-Javadoc)
      * @see org.ebayopensource.turmeric.monitoring.client.presenter.Presenter#getId()
      */
     public String getId() {
         return ERROR_ID;
     }
     
 
     /* (non-Javadoc)
      * @see org.ebayopensource.turmeric.monitoring.client.presenter.Presenter#go(com.google.gwt.user.client.ui.HasWidgets, org.ebayopensource.turmeric.monitoring.client.model.HistoryToken)
      */
     public void go(HasWidgets container, HistoryToken token) {
         //find out which entities have been selected in context
         selectionContext = SelectionContext.fromHistoryToken(token);
         view.setSelection(selectionContext.getSelections());
         
         //If we haven't already got the list of services, get them
         if (servicesList == null)
             fetchServices();
         
         //find out the filter criteria
         FilterContext historyFilter = FilterContext.fromHistoryToken(token);
         
         //if no dates have been selected, then by default compare the last fully complete
         //hour of today with yesterday
         Date now = new Date();
         long fullTimeLastHour = Util.getLastHour(now);
         long sameTimeYesterday = Util.get24HrsPrevious(fullTimeLastHour);
         
         selectedDate1 = (historyFilter.getDate1()==0? new Date(sameTimeYesterday).getTime() : historyFilter.getDate1());
         selectedDate2 = (historyFilter.getDate2()==0? new Date(fullTimeLastHour).getTime() : historyFilter.getDate2());
         view.getFilter().setHours1(Util.getAvailableHours(selectedDate1));
       
         view.getFilter().setHour1(new Date(selectedDate1).getHours());
         view.getFilter().setDate1(new Date(selectedDate1));
 
         view.getFilter().setDate2(new Date(selectedDate2));
         
         //duration 
         selectedDurationHrs = (historyFilter.getDurationHrs()==0? MetricsQueryService.DEFAULT_DURATION_HRS: historyFilter.getDurationHrs());
         int[] intervals = new int[24];
         for (int i=0;i<24;i++)
             intervals[i]=i+1;
         view.getFilter().setDurations(intervals);
         view.getFilter().setDuration(selectedDurationHrs);
         view.setFilterLabel(makeFilterLabel(selectedDate1, selectedDate2, selectedDurationHrs));
         
         
         //set up the possible selections for the category and severities views
         Filterable.ErrorFilterable errFilter = (Filterable.ErrorFilterable)view.getFilter();
         errFilter.setCategoryViewNames(Util.convertFromEnumToCamelCase(CATEGORY_METRICS));
         errFilter.setSeverityViewNames(Util.convertFromEnumToCamelCase(SEVERITY_METRICS));
         
         //determine which set of metrics has been selected: the category ones or the severity ones
         List<String> metricNamesFromHistory = historyFilter.getMetricNames();
         
         //convert to Enum
         List<ErrorMetric> errorMetricsFromHistory = Util.convertToEnumFromCamelCase(metricNamesFromHistory, ErrorMetric.class);
 
         //work out if history has category or severity metrics or neither
         if (errorMetricsFromHistory.isEmpty()) {
             //no applicable metrics from the history, select category by default
             selectedMetrics = CATEGORY_METRICS;
             errFilter.setSelectedCategoryViewNames(Util.convertFromEnumToCamelCase(selectedMetrics));
         } else {
             selectedMetrics = errorMetricsFromHistory;
             boolean isCategory = false;
             for (ErrorMetric m:selectedMetrics) {
                 if (CATEGORY_METRICS.contains(m)) {
                     isCategory = true;
                     break;
                 }
             }
 
             if (isCategory) {
                 //if the metrics are category metrics
                 errFilter.setSelectedCategoryViewNames(Util.convertFromEnumToCamelCase(selectedMetrics));
             } else {
                 //else the metrics are severity metrics
                 errFilter.setSelectedSeverityViewNames(Util.convertFromEnumToCamelCase(selectedMetrics));
             }
         }
 
         //Clean up from any previous content
         view.reset();
         
         
         //if a specific error is selected, force the metric instead 
         if (selectionContext.getSelection(ObjectType.ErrorId) != null || selectionContext.getSelection(ObjectType.ErrorName) != null) {
             selectedMetrics = Arrays.asList(new ErrorMetric[] {ErrorMetric.ConsumerError});
             //get the error details too
             fetchErrorDetail(selectionContext);
         }
 
         fetchMetrics(selectedMetrics, selectionContext, selectedDate1, selectedDate2, selectedDurationHrs);
         //view.activate();
         ((Dashboard)container).activate(this.view);
     }
     
 
 
 
     /**
      * Bind.
      */
     public void bind () {
 
         //listen for any uploads of the services/operations list by other tabs
         this.eventBus.addHandler(GetServicesEvent.TYPE, new GetServicesEventHandler() {
 
             public void onData(GetServicesEvent event) {
                 //only fetch once?
                 if (servicesList == null) {
                     servicesList = event.getData();
                     view.setServicesMap(event.getData());
                 }
             }
 
         });
 
         //listen for any changes from other tabs to the currently selected service or operation
         this.eventBus.addHandler(ObjectSelectionEvent.TYPE, new ObjectSelectionEventHandler() {
 
             public void onSelection(ObjectSelectionEvent event) {
                 selectionContext = new SelectionContext();
 
                 Map<ObjectType,String> selections = event.getSelections();
                 if (selections != null) {
                     for (Map.Entry<ObjectType, String> entry:selections.entrySet()) {
                         if (entry.getValue() != null)
                             selectionContext.select(entry.getKey(), entry.getValue());
                     }
                 }
             }
         });
 
         //listen for any changes from other tabs to the currently selected dates
         this.eventBus.addHandler(DateFilterSelectionEvent.TYPE, new DateFilterSelectionHandler() {
 
             public void onSelection(DateFilterSelectionEvent event) {
                 selectedDate1 = event.getDate1();
                 selectedDate2 = event.getDate2();
                 selectedDurationHrs = event.getDuration();
                 view.getFilter().setHours1(Util.getAvailableHours(selectedDate1));
                 view.getFilter().setHour1(new Date(selectedDate1).getHours());
                 view.getFilter().setDate1(new Date(selectedDate1));
 
                 view.getFilter().setDate2(new Date(selectedDate2));
                 view.getFilter().setDuration(selectedDurationHrs);
             }
         });
 
         //listen for user selection of date1
         this.view.getFilter().getDate1().addValueChangeHandler(new ValueChangeHandler<Date> () {
 
             public void onValueChange(ValueChangeEvent<Date> event) {
                 Date date = event.getValue();
                 int[] hrs = Util.getAvailableHours(date);
                 view.getFilter().setHours1(hrs);
             }
         });
 
         //listen for user selection of date2
         this.view.getFilter().getDate2().addValueChangeHandler(new ValueChangeHandler<Date> () {
 
             public void onValueChange(ValueChangeEvent<Date> event) {
                 Date date = event.getValue();
                 int[] hrs = Util.getAvailableHours(date);
                 
             }
         });
 
         //handle user selection of some new dates and intervals to see metrics for
       
         this.view.getFilter().getApplyButton().addClickHandler(new ClickHandler() {
 
             public void onClick(ClickEvent event) {
                 //Get the date component
                 long oldDate1 = selectedDate1;
                 long oldDate2 = selectedDate2;
                 selectedDate1 = view.getFilter().getDate1().getValue().getTime();
                 selectedDate2 = view.getFilter().getDate2().getValue().getTime();
 
                 //Get the hour component
                 int hour1 = view.getFilter().getHour1();
                 int hour2 = view.getFilter().getHour1();
                 selectedDate1 += (Util.HRS_1_MS * hour1);
                 selectedDate2 += (Util.HRS_1_MS * hour2);
 
 
                 //Get the selected interval
                 int oldDuration = selectedDurationHrs;
                 selectedDurationHrs = view.getFilter().getDuration();
                 
                 view.setFilterLabel(makeFilterLabel(selectedDate1, selectedDate2, selectedDurationHrs));
 
                 //tell other interested tabs that the selected dates have changed
                 if ((oldDate1 != selectedDate1) ||
                         (oldDate2 != selectedDate2) ||
                         (oldDuration != selectedDurationHrs)) {
                     eventBus.fireEvent(new DateFilterSelectionEvent(selectedDate1, selectedDate2, selectedDurationHrs));
                 }
 
                 view.setFilterLabel(makeFilterLabel(selectedDate1, selectedDate2, selectedDurationHrs));
                 
                 List<String> metrics = ((Filterable.ErrorFilterable)view.getFilter()).getSelectedCategoryViewNames();
                 if (metrics.isEmpty())
                     metrics = ((Filterable.ErrorFilterable)view.getFilter()).getSelectedSeverityViewNames();
         
                 //Get which metrics are required
                 selectedMetrics = Util.convertToEnumFromCamelCase(metrics, ErrorMetric.class);
               
                 //Clean up from previous selections
                 view.reset();
 
                 fetchMetrics(selectedMetrics, selectionContext, selectedDate1, selectedDate2, selectedDurationHrs);
                 //Make a history event so the back/forward buttons work but don't fire it as we don't
                 //want to change pages
                 insertHistory(selectionContext, selectedDate1, selectedDate2, selectedDurationHrs, selectedMetrics, false);
             }
         });
     
 
 
         //handle selection of service or operation from list
         this.view.getSelector().addSelectionHandler(new SelectionHandler<TreeItem> () {
 
             public void onSelection(SelectionEvent<TreeItem> event) {
                 TreeItem selection = event.getSelectedItem();
                 //get service and or operation name corresponding to this selection
                 selectionContext.unselect(ObjectType.ServiceName);
                 selectionContext.unselect(ObjectType.OperationName);
                 selectionContext.unselect(ObjectType.ErrorId);
                 selectionContext.unselect(ObjectType.ErrorName);
       
                 if (selection.getParentItem() != null) {
                     selectionContext = new SelectionContext();
                     //If its a leaf, its an operation
                     if (selection.getChildCount() == 0) {
                         selectionContext.select(ObjectType.OperationName, selection.getText());
                         selectionContext.select(ObjectType.ServiceName, selection.getParentItem().getText());
 
                     } else {
                         //Its a service
                         selectionContext.select(ObjectType.ServiceName, selection.getText());
 
                     }
                 }
                 view.setSelection(selectionContext.getSelections());
 
                 eventBus.fireEvent(new ObjectSelectionEvent(selectionContext.getSelections()));
                 
                 //Get the date component
                 selectedDate1 = view.getFilter().getDate1().getValue().getTime();
                 selectedDate2 = view.getFilter().getDate2().getValue().getTime();
 
                 //Get the hour component
                 int hour1 = view.getFilter().getHour1();
                 int hour2 = view.getFilter().getHour1();
                 selectedDate1 += (Util.HRS_1_MS * hour1);
                 selectedDate2 += (Util.HRS_1_MS * hour2);
 
                 //Get the interval
                 selectedDurationHrs = view.getFilter().getDuration();
 
                 //Get the metrics requested
                 List<String> metrics = ((Filterable.ErrorFilterable)view.getFilter()).getSelectedCategoryViewNames();
                 if (metrics.isEmpty())
                     metrics = ((Filterable.ErrorFilterable)view.getFilter()).getSelectedSeverityViewNames();
                 
                 //Get which metrics are required
                 selectedMetrics = Util.convertToEnumFromCamelCase(metrics, ErrorMetric.class);
 
                 view.reset();
                 //Fetch set of metrics for the selected service/operation for the currently selected dates
                 fetchMetrics(selectedMetrics, selectionContext,  selectedDate1, selectedDate2, selectedDurationHrs);
 
                 //Make a history event so the back/forward buttons work but don't fire it as we don't
                 //want to change pages
                 insertHistory(selectionContext, selectedDate1, selectedDate2, selectedDurationHrs, selectedMetrics,false);
             }
         });
     }
 
 
     /* (non-Javadoc)
      * @see org.ebayopensource.turmeric.monitoring.client.presenter.Presenter.TabPresenter#getStateAsHistoryToken()
      */
     public HistoryToken getStateAsHistoryToken() {
         HistoryToken token = HistoryToken.newHistoryToken(DashboardPresenter.DASH_ID);
         token.addValue(DashboardPresenter.TAB, ERROR_ID);
         if (selectionContext != null)
             selectionContext.appendToHistoryToken(token);
         token.addValue(HistoryToken.SELECTED_DATE1_TOKEN, String.valueOf(selectedDate1));
         token.addValue(HistoryToken.SELECTED_DATE2_TOKEN, String.valueOf(selectedDate2));
         if (selectedDurationHrs > 0)
             token.addValue(HistoryToken.SELECTED_DURATION_TOKEN, String.valueOf(selectedDurationHrs));
         return token;
     }
 
     /**
      * Fetch services.
      */
     protected void fetchServices () {
         queryService.getServices(new AsyncCallback<Map<String,Set<String>>>() {
 
             public void onFailure(Throwable error) {
                 view.error(ConsoleUtil.messages.serverError(error.getLocalizedMessage()));
             }
 
             public void onSuccess(Map<String, Set<String>> services) {
                 servicesList = services;
                 view.setServicesMap(services);
             }
         });
     }
     
     
     /**
      * Fetch error detail.
      *
      * @param sc the sc
      */
     protected void fetchErrorDetail (SelectionContext sc) {
         
         queryService.getErrorDetail(sc.getSelection(ObjectType.ErrorId),
                                     sc.getSelection(ObjectType.ErrorName), 
                                     sc.getSelection(ObjectType.ServiceName), 
                                     new AsyncCallback<ErrorDetail>() {
 
             public void onFailure(Throwable error) {
                 view.error(ConsoleUtil.messages.serverError(error.getLocalizedMessage()));
             }
 
             public void onSuccess(ErrorDetail errorDetail) {
                 view.setErrorDetail (errorDetail);
             }
 
         });
     }
 
     /**
      * Fetch metrics.
      *
      * @param metrics the metrics
      * @param sc the sc
      * @param date1 the date1
      * @param date2 the date2
      * @param durationHrs the duration hrs
      */
     protected void fetchMetrics(final List<ErrorMetric> metrics, SelectionContext sc, long date1, long date2, int durationHrs) {
         if (metrics == null)
             return;
         
         boolean callSystemErrorCountGraph = false;
         boolean callApplicationErrorGraph = false;
         boolean callRequestErrorGraph = false;
         
         List<String> serviceNames = new ArrayList<String>(1);
         List<String> operationNames = new ArrayList<String>(1);
         List<String> consumerNames = new ArrayList<String>(1);
         String chosenErr = null;
         boolean isId = false;
         if (sc != null) {
             for (Map.Entry<ObjectType,String> entry:sc.getSelections().entrySet()) {
                 switch (entry.getKey()) {
                     case ServiceName: {
                         serviceNames.add(entry.getValue());
                         break;
                     }
                     case OperationName: {
                         operationNames.add(entry.getValue());
                         break;
                     }
                     case ConsumerName: {
                         consumerNames.add(entry.getValue());
                         break;
                     }
                     case ErrorId: {
                         if (entry.getValue() != null) {
                             chosenErr = entry.getValue();
                             isId = true;
                         }
                         break;
                     }
                     case ErrorName: {
                         if (entry.getValue() != null) {
                             chosenErr = entry.getValue();
                             isId = false;
                         }
                         break;
                     }
                 }
             }
         }
 
         for (ErrorMetric m:metrics) {
             ErrorCriteria ec = null;
             MetricCriteria mc = MetricCriteria.newMetricCriteria(m.toMetricName(), date1, date2, durationHrs, Ordering.Descending, 10, Perspective.Server, false);
             switch (m) {
                 /* category related metrics */
                 case TopApplicationErrors: {
                     callApplicationErrorGraph = true;
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Category, serviceNames, operationNames, consumerNames, null, false, ErrorCategory.Application, null);
                     break;
                 }
                 case TopRequestErrors: {
                     callRequestErrorGraph = true;
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Category, serviceNames, operationNames, consumerNames, null, false, ErrorCategory.Request, null);
                     break;
                 }
                 case TopSystemErrors: {
                     callSystemErrorCountGraph = true;
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Category, serviceNames, operationNames, consumerNames, null, false, ErrorCategory.System, null);
                     break;
                 }
                 
                 /* severity related metrics */
                 case TopCriticals: {
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Severity, serviceNames, operationNames, consumerNames, null, false, null, ErrorSeverity.Critical);
                     break;
                 }
                 case TopErrors: {                    
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Severity, serviceNames, operationNames, consumerNames, null, false, null, ErrorSeverity.Error);
                     break;
                 }
                 case TopWarnings: {                    
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Severity, serviceNames, operationNames, consumerNames, null, false, null, ErrorSeverity.Warning);
                     break;
                 }
                 
                 /* drill down from category */
                 /*
                 case TopCategoryErrors: {
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Category, serviceNames, operationNames, consumerNames, null, false, category, null);
                     break;
                 }
                 */
                 
                 /* drill down from severity */
                 /*
                 case TopSeverityErrors: { 
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Severity, serviceNames, operationNames, consumerNames, null, false, null, severity);
                     break;
                 }
                 */
                 /* finest drill down to specific error */
                 case ConsumerError: {
                     //work out if we will reference errors by name or by id?
                     ec = ErrorCriteria.newErrorCriteria(ErrorType.Severity, serviceNames, operationNames, consumerNames, chosenErr, isId, null, null);
                     break;
                 }
             }
             fetchMetric(m, ec, mc);
             
             if (callSystemErrorCountGraph)
                 getServiceSystemErrorTrend(selectionContext, date1, date2, durationHrs);
             if (callApplicationErrorGraph)
                 getServiceApplicationErrorTrend(selectionContext, date1, date2, durationHrs);
             if (callRequestErrorGraph)
                 getServiceRequestErrorTrend(selectionContext, date1, date2, durationHrs);
         }
     }
     
     private void getSimpleErrorGraphData(ErrorType errorType, ErrorCategory errorCategory,  ErrorSeverity severity, String roleType, long aggregationPeriod,
                     final SelectionContext selectionContext, long date1, long date2, final int hourSpan,
                     AsyncCallback<List<ErrorTimeSlotData>> callback) {
         GraphUtil.getSimpleErrorGraphData(queryService, errorType, errorCategory, severity, roleType, aggregationPeriod, selectionContext, date1, date2, hourSpan, callback);
     }
     
     private void getServiceSystemErrorTrend(final SelectionContext selectionContext2, final long date1, final long date2, final int durationHrs) {
         AsyncCallback<List<ErrorTimeSlotData>> callback = new AsyncCallback<List<ErrorTimeSlotData>>() {
             @Override
             public void onSuccess(List<ErrorTimeSlotData> dataRanges) {
                 String serviceOpName = selectionContext.getSelection(ObjectType.ServiceName);
                 if (selectionContext.getSelection(ObjectType.OperationName) != null) {
                     serviceOpName += "." + selectionContext.getSelection(ObjectType.OperationName);
                 }
                 String graphTitle = ConsoleUtil.messages.graphTitle("System Errors", serviceOpName, durationHrs);
                 ErrorPresenter.this.view.activate();
                 ErrorPresenter.this.view.setServiceSystemErrorTrendData(dataRanges, graphTitle);
             }
 
             @Override
             public void onFailure(Throwable exception) {
                 GWT.log(exception.getMessage());
             }
         };
         
         this.getSimpleErrorGraphData(ErrorType.Category, ErrorCategory.System, null, "server", minAggregationPeriod, selectionContext, date1, date2, durationHrs,
                         callback);
     }
 
 
     private void getServiceApplicationErrorTrend(SelectionContext selectionContext2, long date1, long date2, final int durationHrs) {
         AsyncCallback<List<ErrorTimeSlotData>> callback = new AsyncCallback<List<ErrorTimeSlotData>>() {
             @Override
             public void onSuccess(List<ErrorTimeSlotData> dataRanges) {
                 String serviceOpName = selectionContext.getSelection(ObjectType.ServiceName);
                 if (selectionContext.getSelection(ObjectType.OperationName) != null) {
                     serviceOpName += "." + selectionContext.getSelection(ObjectType.OperationName);
                 }
                 String graphTitle = ConsoleUtil.messages.graphTitle("Application Errors", serviceOpName, durationHrs);
                 ErrorPresenter.this.view.activate();
                 ErrorPresenter.this.view.setServiceApplicationErrorTrendData(dataRanges, graphTitle);
             }
 
             @Override
             public void onFailure(Throwable exception) {
                 GWT.log(exception.getMessage());
             }
         };
         
         this.getSimpleErrorGraphData(ErrorType.Category, ErrorCategory.Application, null, "server", minAggregationPeriod, selectionContext, date1, date2, durationHrs,
                         callback);
     }
 
 
     private void getServiceRequestErrorTrend(SelectionContext selectionContext2, long date1, long date2, final int durationHrs) {
         AsyncCallback<List<ErrorTimeSlotData>> callback = new AsyncCallback<List<ErrorTimeSlotData>>() {
             @Override
             public void onSuccess(List<ErrorTimeSlotData> dataRanges) {
                 String serviceOpName = selectionContext.getSelection(ObjectType.ServiceName);
                 if (selectionContext.getSelection(ObjectType.OperationName) != null) {
                     serviceOpName += "." + selectionContext.getSelection(ObjectType.OperationName);
                 }
                 String graphTitle = ConsoleUtil.messages.graphTitle("Request Errors", serviceOpName, durationHrs);
                 ErrorPresenter.this.view.activate();
                 ErrorPresenter.this.view.setServiceRequestErrorTrendData(dataRanges, graphTitle);
             }
 
             @Override
             public void onFailure(Throwable exception) {
                 GWT.log(exception.getMessage());
             }
         };
         
         this.getSimpleErrorGraphData(ErrorType.Category, ErrorCategory.Request, null, "server", minAggregationPeriod, selectionContext, date1, date2, durationHrs,
                         callback);
     }
 
 
     /**
      * Fetch metric.
      *
      * @param m the m
      * @param ec the ec
      * @param mc the mc
      */
     protected void fetchMetric (final ErrorMetric m, final ErrorCriteria ec, final MetricCriteria mc) {
 
         queryService.getErrorData(ec, mc, new AsyncCallback<ErrorMetricData>() {
 
             public void onFailure(Throwable err) {
                view.error(err.getLocalizedMessage());
             }
 
             public void onSuccess(ErrorMetricData data) {
                 view.setErrorMetricData(m, data);
                String downloadUrl = queryService.getErrorDataDownloadUrl(ec, mc);
                view.setDownloadUrl(m, downloadUrl);
                 //depending on which metric table it is, we want to hook up click listeners so we can navigate to other tabs
                 switch (m) {
                     case TopApplicationErrors:
                     case TopRequestErrors:
                     case TopSystemErrors: 
                     case TopCriticals:
                     case TopErrors:
                     case TopWarnings: {
                         //add a click handler on errorIds to drill down
                         List<HasClickHandlers> handlers = view.getTableColumn(m, 0);
                         for (HasClickHandlers h:handlers) {
                             h.addClickHandler(new ClickHandler() {
                                 public void onClick(ClickEvent event) {
                                    Object o = event.getSource();
                                    if (o instanceof HasText) {
                                        String errorId = ((HasText)o).getText();
                                        SelectionContext ctx = new SelectionContext();
                                        ctx.selectAll(selectionContext);
                                        ctx.unselect(ObjectType.ErrorName);
                                        ctx.select(ObjectType.ErrorId, errorId);
                                        //insert a history event but don't change tab
                                        view.reset();
                                        fetchErrorDetail(ctx);
                                        fetchMetrics(Arrays.asList(new ErrorMetric[] {ErrorMetric.ConsumerError}), ctx, selectedDate1, selectedDate2, selectedDurationHrs);
                                        insertHistory(ERROR_ID, ctx, selectedDate1, selectedDate2, selectedDurationHrs, false);
                                    }
                                 }                            
                             });
                         }
                         //add a click handler on the error names to drill down
                         handlers = view.getTableColumn(m, 1);
                         for (HasClickHandlers h:handlers) {
                             h.addClickHandler(new ClickHandler() {
                                 public void onClick(ClickEvent event) {
                                     Object o = event.getSource();
                                     if (o instanceof HasText) {
                                         String errorName = ((HasText)o).getText();
                                         SelectionContext ctx = new SelectionContext();
                                         ctx.selectAll(selectionContext);
                                         ctx.unselect(ObjectType.ErrorId);
                                         ctx.select(ObjectType.ErrorName, errorName);
                                         //insert a history event but don't change tab
                                         view.reset();
                                         fetchErrorDetail(ctx);
                                         fetchMetrics(Arrays.asList(new ErrorMetric[] {ErrorMetric.ConsumerError}), ctx, selectedDate1, selectedDate2, selectedDurationHrs);
                                         insertHistory(ERROR_ID, ctx, selectedDate1, selectedDate2, selectedDurationHrs, false);
                                     }
                                 }
                             });
                         }
                         
                         break;
                     }
                     case ConsumerError: {
                         List<HasClickHandlers> handlers = view.getTableColumn(m, 0);
                         for (HasClickHandlers h:handlers) {
                             h.addClickHandler(new ClickHandler() {
                                 public void onClick(ClickEvent event) {
                                     Object o = event.getSource();
                                     if (o instanceof HasText) {
                                         String consumer = ((HasText)o).getText();
                                         SelectionContext ctx = new SelectionContext();
                                         ctx.selectAll(selectionContext);
                                         ctx.select(ObjectType.ConsumerName, consumer);
                                         //insert a history event and change tab
                                         insertHistory(ConsumerPresenter.CONSUMER_ID, ctx, selectedDate1, selectedDate2, selectedDurationHrs, true);
                                     }
                                 }
                             });
                         }
                         break;
                     }
                 }
             }
         });
     }
     
     /**
      * Insert history.
      *
      * @param sc the sc
      * @param d1 the d1
      * @param d2 the d2
      * @param interval the interval
      * @param metrics the metrics
      * @param fire the fire
      */
     protected void insertHistory (SelectionContext sc, long d1, long d2, int interval, Collection<ErrorMetric> metrics, boolean fire) {
         HistoryToken token = HistoryToken.newHistoryToken(DashboardPresenter.DASH_ID, null);
         token.addValue(DashboardPresenter.TAB, ERROR_ID);
         if (sc != null)
             sc.appendToHistoryToken(token);
 
         token.addValue(HistoryToken.SELECTED_DATE1_TOKEN, String.valueOf(d1));
         token.addValue(HistoryToken.SELECTED_DATE2_TOKEN, String.valueOf(d2));
         token.addValue(HistoryToken.SELECTED_DURATION_TOKEN, String.valueOf(interval));
         token.addValue(HistoryToken.SELECTED_METRICS_TOKEN, metrics);
         
         History.newItem(token.toString(), fire);
     }
     
     
     /**
      * Insert history.
      *
      * @param presenterId the presenter id
      * @param sc the sc
      * @param d1 the d1
      * @param d2 the d2
      * @param interval the interval
      * @param fire the fire
      */
     protected void insertHistory (String presenterId, SelectionContext sc, long d1, long d2, int interval, boolean fire) {
            HistoryToken token = HistoryToken.newHistoryToken(DashboardPresenter.DASH_ID, null);
             token.addValue(DashboardPresenter.TAB, presenterId);
             if (sc != null)
                 sc.appendToHistoryToken(token);
             token.addValue(HistoryToken.SELECTED_DATE1_TOKEN, String.valueOf(d1));
             token.addValue(HistoryToken.SELECTED_DATE2_TOKEN, String.valueOf(d2));
             token.addValue(HistoryToken.SELECTED_DURATION_TOKEN, String.valueOf(interval));
             History.newItem(token.toString(), fire);
     }
     
    
     private String makeFilterLabel (long d1, long d2, int durationHrs) {
         String d1s = ConsoleUtil.timeFormat.format(new Date(d1));
         String d2s = ConsoleUtil.timeFormat.format(new Date(d2));
 
         String filterString = d1s+" + "+(durationHrs)+" - ";
         filterString += d2s + " + "+(durationHrs)+" >>";
         return filterString;
     }
 }
