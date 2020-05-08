 package com.finance.controller;
 
 import com.common.Util;
 import com.finance.dao.EntryDao;
 import com.finance.model.EntryCommand;
 import com.highchart.AxisData;
 import com.highchart.GraphData;
 import com.highchart.PieData;
 import com.highchart.Series;
 import com.security.MyUserContext;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: jitse
  * Date: 8/4/13
  * Time: 3:26 PM
  */
 @Controller
 @RequestMapping("/finance/stats")
 public class FinanceStatsController {
 
     @Autowired
     EntryDao entryDao;
 
     @Autowired
     private MyUserContext myUserContext;
 
 
     @RequestMapping(method = RequestMethod.GET)
     public @ResponseBody
     ModelAndView stats () {
         return new ModelAndView("finance/stats");
     }
 
     @RequestMapping(value="expenseByPayee", method = RequestMethod.GET)
     public @ResponseBody
     PieData expenseByPayee () {
        List<EntryCommand> entries = entryDao.getEntriesByUserId(myUserContext.getCurrentUser().getId());
 
         List<List<Object>> pieData = new ArrayList<List<Object>>();
 
         //the data is assumed to be sorted by payee.
         String payee = entries.get(0).getPayee().getName();
         double grandTotal = 0;
         double runningSum = 0;
         for (EntryCommand entry : entries) {
             if (entry.getPayee().getName().equals(payee)) {
                 runningSum += Double.parseDouble(entry.getAmount());
             } else {
                 //save the data from the previous payee;
                 List<Object> series = new ArrayList<Object>();
                 series.add(payee);
                 series.add(new Double(runningSum).intValue());
                 pieData.add(series);
 
                 //initializing a new payee
                 grandTotal += runningSum;
                 runningSum = Double.parseDouble(entry.getAmount());
                 payee = entry.getPayee().getName();
             }
         }
 
         grandTotal += runningSum;
         //add the last batch onto the list
         List<Object> series = new ArrayList<Object>();
         series.add(payee);
         series.add(new Double(runningSum).intValue());
         pieData.add(series);
 
         PieData rval = new PieData();
         rval.setData(pieData);
         rval.setName("Amount Spent");
         rval.setTitle("Total Amount Spent: $" + grandTotal);
 
         return rval;
     }
 
 
     @RequestMapping(value="monthlyExpense", method = RequestMethod.GET)
     public @ResponseBody
     GraphData monthlyExpense () {
         List<EntryCommand> entries = entryDao.getEntriesByUserIdSortByDate(myUserContext.getCurrentUser().getId());
 
         if (entries == null || entries.isEmpty()) {
             return new GraphData();
         }
         
         List<String> monthList = getMonthList(entries);
 
         AxisData axisData = new AxisData();
         axisData.setData(monthList);
 
         Series series = new Series();
         series.setName("Monthly Expense");
         series.setMyData(getMonthlyTotal(entries));
 
         List<Series> seriesList = new ArrayList<Series>();
         seriesList.add(series);
 
         GraphData data = new GraphData();
         data.setyTitle("Amount Spent");
         data.setxAxis(axisData);
         data.setSeries(seriesList);
         data.setChartTitle("Monthly Expenses");
         data.setPieData(getPieDataSeries(entries));
         return data;
     }
 
     private PieData getPieDataSeries(List<EntryCommand> entries) {
         PieData rval = new PieData();
 
         double runningSum = 0.0;
         for (EntryCommand entry : entries) {
             runningSum += Double.parseDouble(entry.getAmount());
         }
 
         List<List<Object>> data = new ArrayList<List<Object>>();
 
         List<Object> series1 = new ArrayList<Object>();
         series1.add("Total Expense");
         series1.add(new Double(runningSum).intValue());
 
         data.add(series1);
         rval.setData(data);
         rval.setName("");
         rval.setTitle("");
 
         return rval;
     }
 
 
     private List<Integer> getMonthlyTotal(List<EntryCommand> entries) {
         List<Integer> rval = new ArrayList<Integer>();
 
         //the data is assumed to be sorted by date.
         int curMonth = entries.get(0).getDate().getMonth();
         double runningSum = 0;
         for (EntryCommand entry : entries) {
             if (entry.getDate().getMonth() == curMonth) {
                 runningSum += Double.parseDouble(entry.getAmount());
             } else {
                 //save the data from the previous month
                 rval.add(new Double(runningSum).intValue());
 
                 //if there are gaps in months between games, we want to fill that with 0s
                 curMonth++;
                 while (curMonth != entry.getDate().getMonth()) {
                     rval.add(0);
                     curMonth++;
 
                     if (curMonth > 15) {
                         throw new RuntimeException("Something went wrong, terminating to save itself");
                     }
                 }
 
                 //initializing a new month
                 runningSum = Double.parseDouble(entry.getAmount());
             }
         }
 
         //add the last batch onto the list
         rval.add(new Double(runningSum).intValue());
 
         return rval;
     }
 
     private List<String> getMonthList(List<EntryCommand> entries) {
         int minMonth = 15;
         int maxMonth = -1;
 
         for (EntryCommand entry : entries) {
             if (entry.getDate().getMonth() < minMonth) {
                 minMonth = entry.getDate().getMonth();
             }
             if (entry.getDate().getMonth() > maxMonth) {
                 maxMonth = entry.getDate().getMonth();
             }
         }
 
         return Util.getMonthList(minMonth, maxMonth);
     }
 
 }
