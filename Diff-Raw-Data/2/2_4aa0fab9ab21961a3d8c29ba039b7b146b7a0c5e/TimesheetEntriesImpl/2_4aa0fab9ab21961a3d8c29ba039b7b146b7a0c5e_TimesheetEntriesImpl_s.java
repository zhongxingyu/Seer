 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Range;
 import com.stackframe.collect.RangeUtilities;
 import com.stackframe.sarariman.tasks.Task;
 import com.stackframe.sarariman.tasks.Tasks;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 class TimesheetEntriesImpl implements TimesheetEntries {
 
     private final DataSource dataSource;
     private final Directory directory;
     private final Tasks tasks;
     private final String mountPoint;
 
     TimesheetEntriesImpl(DataSource dataSource, Directory directory, Tasks tasks, String servletPath) {
         this.dataSource = dataSource;
         this.directory = directory;
         this.tasks = tasks;
         this.mountPoint = servletPath;
     }
 
     public TimesheetEntry get(Task task, Employee employee, Date date) {
        return new TimesheetEntryImpl(task, employee, date, dataSource, mountPoint + "editentry.jsp");
     }
 
     public Iterable<TimesheetEntry> getEntries(Range<Date> dateRange) {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 String dateRangeExpression = RangeUtilities.toSQL("date", dateRange);
                 PreparedStatement s = connection.prepareStatement(String.format("SELECT task, employee, date FROM hours WHERE %s ORDER BY DATE DESC", dateRangeExpression));
                 try {
                     ResultSet r = s.executeQuery();
                     try {
                         ImmutableList.Builder<TimesheetEntry> builder = ImmutableList.builder();
                         while (r.next()) {
                             int task = r.getInt("task");
                             int employeeNumber = r.getInt("employee");
                             Employee employee = directory.getByNumber().get(employeeNumber);
                             Date date = r.getDate("date");
                             builder.add(get(tasks.get(task), employee, date));
                         }
 
                         return builder.build();
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
 }
