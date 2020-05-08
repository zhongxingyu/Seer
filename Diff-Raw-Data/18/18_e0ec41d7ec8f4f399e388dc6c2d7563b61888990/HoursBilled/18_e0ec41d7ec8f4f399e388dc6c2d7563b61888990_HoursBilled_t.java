 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.statusboard;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSortedMap;
 import com.stackframe.sarariman.Sarariman;
 import com.stackframe.sarariman.Utilities;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
import java.util.TreeSet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author mcculley
  */
 public class HoursBilled extends HttpServlet {
 
     private Sarariman sarariman;
 
     @Override
     public void init() throws ServletException {
         super.init();
         sarariman = (Sarariman)getServletContext().getAttribute("sarariman");
     }
 
     private final int days = 180;
 
     private Map<Date, BigDecimal> billable(Connection connection) throws SQLException {
         PreparedStatement s = connection.prepareStatement(
                 "SELECT SUM(duration) AS total, date " +
                 "FROM hours " +
                 "JOIN tasks ON hours.task = tasks.id " +
                 "WHERE date > DATE_SUB(NOW(), INTERVAL ? DAY) AND billable = TRUE " +
                "GROUP BY date");
         try {
             s.setInt(1, days);
             ResultSet r = s.executeQuery();
             try {
                 ImmutableSortedMap.Builder<Date, BigDecimal> mapBuilder = ImmutableSortedMap.<Date, BigDecimal>naturalOrder();
                 while (r.next()) {
                     Date date = r.getDate("date");
                     BigDecimal total = r.getBigDecimal("total");
                     mapBuilder.put(date, total);
                 }
 
                 return mapBuilder.build();
             } finally {
                 r.close();
             }
         } finally {
             s.close();
         }
     }
 
     private Map<Date, BigDecimal> overhead(Connection connection) throws SQLException {
         PreparedStatement s = connection.prepareStatement(
                 "SELECT SUM(duration) AS total, date " +
                 "FROM hours " +
                 "JOIN tasks ON hours.task = tasks.id " +
                 "WHERE date > DATE_SUB(NOW(), INTERVAL ? DAY) AND tasks.billable = FALSE AND hours.task != ? " +
                "GROUP BY date");
         try {
             s.setInt(1, days);
             s.setInt(2, sarariman.getPaidTimeOff().getPaidTimeOffTask().getId());
             ResultSet r = s.executeQuery();
             try {
                 ImmutableSortedMap.Builder<Date, BigDecimal> mapBuilder = ImmutableSortedMap.<Date, BigDecimal>naturalOrder();
                 while (r.next()) {
                     Date date = r.getDate("date");
                     BigDecimal total = r.getBigDecimal("total");
                     mapBuilder.put(date, total);
                 }
 
                 return mapBuilder.build();
             } finally {
                 r.close();
             }
         } finally {
             s.close();
         }
     }
 
     private Map<Date, BigDecimal> pto(Connection connection) throws SQLException {
         PreparedStatement s = connection.prepareStatement(
                 "SELECT SUM(duration) AS total, date " +
                 "FROM hours " +
                 "JOIN tasks ON hours.task = tasks.id " +
                 "WHERE date > DATE_SUB(NOW(), INTERVAL ? DAY) AND hours.task = ? " +
                "GROUP BY date");
         try {
             s.setInt(1, days);
             s.setInt(2, sarariman.getPaidTimeOff().getPaidTimeOffTask().getId());
             ResultSet r = s.executeQuery();
             try {
                 ImmutableSortedMap.Builder<Date, BigDecimal> mapBuilder = ImmutableSortedMap.<Date, BigDecimal>naturalOrder();
                 while (r.next()) {
                     Date date = r.getDate("date");
                     BigDecimal total = r.getBigDecimal("total");
                     mapBuilder.put(date, total);
                 }
 
                 return mapBuilder.build();
             } finally {
                 r.close();
             }
         } finally {
             s.close();
         }
     }
 
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         response.setContentType("text/csv;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Connection connection = sarariman.openConnection();
             try {
                 out.println("Date,Billable,Overhead,PTO");
                 Map<Date, BigDecimal> billable = billable(connection);
                 Map<Date, BigDecimal> overhead = overhead(connection);
                 Map<Date, BigDecimal> pto = pto(connection);
                Set<Date> allDates = new TreeSet<Date>(Utilities.<Date, BigDecimal>allKeys(ImmutableList.of(billable, overhead)));
                 for (Date date : allDates) {
                     BigDecimal b = billable.get(date);
                     if (b == null) {
                         b = BigDecimal.ZERO;
                     }
 
                     BigDecimal o = overhead.get(date);
                     if (o == null) {
                         o = BigDecimal.ZERO;
                     }
 
                     BigDecimal p = pto.get(date);
                     if (p == null) {
                         p = BigDecimal.ZERO;
                     }
 
                     out.println(String.format("%s,%s,%s,%s", date, b, o, p));
                 }
 
                 out.println("Colors,green,red,yellow");
             } finally {
                 connection.close();
             }
         } catch (SQLException e) {
             throw new ServletException(e);
         } finally {
             out.close();
         }
     }
 
 }
