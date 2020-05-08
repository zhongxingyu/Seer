 package com.orgsync.api;
 
 import static com.orgsync.api.Util.checkNotNull;
 
 import java.util.List;
 
 import com.ning.http.client.FluentStringsMap;
 import com.ning.http.client.ListenableFuture;
 import com.orgsync.api.model.timesheets.Timesheet;
 
 /**
  * The implementation of the timesheets endpoint.
  * 
  * @author steffyj
  * 
  */
 /* package */class TimesheetsResourceImpl extends BaseResource implements TimesheetsResource {
 
     /* package */TimesheetsResourceImpl(final ApiClientImpl client) {
         super(client, "/timesheets");
     }
 
     @Override
     public ListenableFuture<ApiResponse<List<Timesheet>>> getTimesheets() {
         return list(Timesheet.LIST_TYPE);
     }
 
     @Override
     public ListenableFuture<ApiResponse<List<Timesheet>>> getAccountTimesheets(final int accountId) {
         return list("/accounts/%d", Timesheet.LIST_TYPE, accountId);
     }
 
     @Override
     public ListenableFuture<ApiResponse<List<Timesheet>>> getOrgTimesheets(final int orgId) {
         return list("/orgs/%d", Timesheet.LIST_TYPE, orgId);
     }
 
     @Override
     public ListenableFuture<ApiResponse<List<Timesheet>>> getEventTimesheets(final int eventId,
             final String occurrenceDate) {
         checkNotNull(occurrenceDate);
 
         String endpoint = listFor("/events/%d", eventId);
         FluentStringsMap params = new FluentStringsMap().add("occurrence", occurrenceDate);
 
        return getResponse(RequestParams.get(endpoint, params), Timesheet.LIST_TYPE);
     }
 }
