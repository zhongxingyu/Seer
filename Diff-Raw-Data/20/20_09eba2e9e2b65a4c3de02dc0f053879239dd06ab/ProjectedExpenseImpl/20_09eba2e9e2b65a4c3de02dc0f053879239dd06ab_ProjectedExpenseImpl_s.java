 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.projects;
 
 import com.stackframe.sarariman.CostData;
 import com.stackframe.sarariman.Employee;
 import com.stackframe.sarariman.Invoice;
 import com.stackframe.sarariman.LaborCategory;
 import com.stackframe.sarariman.LaborCategoryAssignment;
 import com.stackframe.sarariman.PeriodOfPerformance;
 import com.stackframe.sarariman.Workdays;
 import com.stackframe.sarariman.tasks.Task;
 import com.stackframe.sql.SQLUtilities;
 import java.math.BigDecimal;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class ProjectedExpenseImpl implements ProjectedExpense {
 
     private final Employee employee;
     private final PeriodOfPerformance pop;
     private final Task task;
     private final Workdays workdays;
     private final Map<Long, LaborCategory> categoriesById;
     private final Collection<LaborCategoryAssignment> projectBillRates;
     private final DataSource dataSource;
 
    public ProjectedExpenseImpl(Employee employee, PeriodOfPerformance pop, Task task, Workdays workdays,
                                 Map<Long, LaborCategory> categoriesById, Collection<LaborCategoryAssignment> projectBillRates,
                                 DataSource dataSource) {
         this.employee = employee;
         this.pop = pop;
         this.task = task;
         this.workdays = workdays;
         this.categoriesById = categoriesById;
         this.projectBillRates = projectBillRates;
         this.dataSource = dataSource;
     }
 
     public Employee getEmployee() {
         return employee;
     }
 
     public PeriodOfPerformance getPeriodOfPerformance() {
         return pop;
     }
 
     public Task getTask() {
         return task;
     }
 
     public BigDecimal getCost() {
         Collection<Date> dates = workdays.getWorkdays(pop);
         BigDecimal total = BigDecimal.ZERO;
         for (Date date : dates) {
             try {
                 CostData cost = Invoice.cost(categoriesById, projectBillRates, task.getProject().getId(), employee.getNumber(),
                                             task, SQLUtilities.convert(date), 8, dataSource);
                 // If we are missing any cost data, abort and return 0 to flag it.
                 if (cost.getLaborCategory() == null) {
                     return BigDecimal.ZERO;
                 }
 
                 total = total.add(cost.getCost());
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             }
         }
 
         return total;
     }
 
     public int getHours() {
         return workdays.getWorkdays(pop).size() * 8;
     }
 
 }
