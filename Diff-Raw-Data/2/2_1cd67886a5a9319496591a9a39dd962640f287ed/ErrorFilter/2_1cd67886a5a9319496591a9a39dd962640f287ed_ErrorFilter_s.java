 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.google.common.base.Throwables;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class ErrorFilter implements Filter {
 
     private DataSource dataSource;
     private final ExecutorService executor = Executors.newFixedThreadPool(1);
 
     public void init(FilterConfig filterConfig) throws ServletException {
         Sarariman sarariman = (Sarariman)filterConfig.getServletContext().getAttribute("sarariman");
         dataSource = sarariman.getDataSource();
     }
 
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         try {
             chain.doFilter(request, response);
         } catch (final Exception e) {
             final String stackTrace = Throwables.getStackTraceAsString(e);
             request.setAttribute("stacktrace", stackTrace);
            request.getRequestDispatcher("error.jsp").forward(request, response);
 
             // One would think that the httpServletRequest could just be marked final and used in the Runnable, but it gets reused after
             // this call.
             HttpServletRequest httpServletRequest = (HttpServletRequest)request;
             final String path = httpServletRequest.getServletPath();
             final String queryString = httpServletRequest.getQueryString();
             final String method = httpServletRequest.getMethod();
             final String userAgent = httpServletRequest.getHeader("User-Agent");
             final String remoteAddress = httpServletRequest.getRemoteAddr();
 
             final Employee employee = (Employee)request.getAttribute("user");
             Runnable insertTask = new Runnable() {
                 public void run() {
                     try {
                         Connection c = dataSource.getConnection();
                         try {
                             PreparedStatement s = c.prepareStatement(
                                     "INSERT INTO error_log (path, query, method, employee, user_agent, remote_address, exception) " +
                                     "VALUES(?, ?, ?, ?, ?, ?, ?)");
                             try {
                                 s.setString(1, path);
                                 s.setString(2, queryString);
                                 s.setString(3, method);
                                 if (employee == null) {
                                     s.setObject(4, null);
                                 } else {
                                     s.setInt(4, employee.getNumber());
                                 }
 
                                 s.setString(5, userAgent);
                                 s.setString(6, remoteAddress);
                                 s.setString(7, stackTrace);
                                 int numRowsInserted = s.executeUpdate();
                                 assert numRowsInserted == 1;
                             } finally {
                                 s.close();
                             }
                         } finally {
                             c.close();
                         }
                     } catch (SQLException e) {
                         // FIXME: Should we log this exception? Does it kill the Executor?
                         throw new RuntimeException(e);
                     }
                 }
 
             };
             executor.execute(insertTask);
         }
     }
 
     public void destroy() {
         executor.shutdown();
     }
 
 }
