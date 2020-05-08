 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.google.common.collect.ImmutableList;
 import com.stackframe.sarariman.timesheets.Timesheet;
import java.net.MalformedURLException;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * @author mcculley
  */
 public class TimesheetAudit implements Audit {
 
     private final Sarariman sarariman;
     private final Directory directory;
 
     public TimesheetAudit(Sarariman sarariman, Directory directory) {
         this.sarariman = sarariman;
         this.directory = directory;
     }
 
     public String getDisplayName() {
         return "Timesheets";
     }
 
     private List<Timesheet> timesheets(Week week) {
         List<Timesheet> list = new ArrayList<Timesheet>();
         for (Employee employee : directory.getByUserName().values()) {
             list.add(sarariman.getTimesheets().get(employee, week));
         }
 
         return list;
     }
 
     public Collection<AuditResult> getResults() {
         ImmutableList.Builder<AuditResult> listBuilder = ImmutableList.<AuditResult>builder();
         Week lastWeek = DateUtils.week(DateUtils.now()).getPrevious();
         List<Timesheet> timesheets = timesheets(lastWeek);
         for (Timesheet timesheet : timesheets) {
             if (timesheet.isSubmitted()) {
                 Employee employee = timesheet.getEmployee();
                 if (timesheet.isApproved()) {
                     double recorded = timesheet.getPTOHours();
                     if (recorded > 0) {
                         double deducted = PaidTimeOff.getPaidTimeOff(sarariman.getDataSource(), employee, lastWeek,
                                                                      "weeklyPTODeduction");
                         if (recorded != -deducted) {
                            URL timesheetsURL = sarariman.getTimesheets().getURL();
                            try {
                                timesheetsURL = new URL(timesheetsURL.toString() + "?week=" + lastWeek.getName());
                            } catch (MalformedURLException mue) {
                                throw new AssertionError(mue);
                            }

                             listBuilder.add(new AuditResult(AuditResultType.todo,
                                                             String.format("PTO needs to be deducted for %s for week of %s",
                                                                           employee.getDisplayName(), lastWeek.getName()),
                                                            timesheetsURL));
                         }
                     }
                 } else {
                     listBuilder.add(new AuditResult(AuditResultType.todo,
                                                     String.format("timesheet for %s needs review", employee.getDisplayName()),
                                                     timesheet.getURL()));
                 }
             }
         }
 
         return listBuilder.build();
     }
 
 }
