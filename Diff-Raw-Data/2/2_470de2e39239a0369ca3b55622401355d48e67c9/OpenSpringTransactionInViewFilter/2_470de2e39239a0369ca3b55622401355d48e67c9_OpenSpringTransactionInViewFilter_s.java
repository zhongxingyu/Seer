 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.web.filters;
 
 
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.RulesStack;
 import com.orangeleap.tangerine.util.TaskStack;
 import org.apache.commons.logging.Log;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionDefinition;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.filter.OncePerRequestFilter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class OpenSpringTransactionInViewFilter extends OncePerRequestFilter {
 
 
     protected final Log logger = OLLogger.getLog(getClass());
 
     private Object getBean(HttpServletRequest request, String bean) {
         ServletContext servletContext = request.getSession().getServletContext();
         WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
         return wac.getBean(bean);
     }
 
     private boolean suppressStartTransaction(HttpServletRequest request) {
         String url = request.getRequestURL().toString();
 
         return FilterUtil.isResourceRequest(request)
                 || url.endsWith("/import.htm")
                 ;
     }
 
     protected void doFilterInternal(
             HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
             throws ServletException, IOException {
 
         if (TransactionSynchronizationManager.isActualTransactionActive()
                 || suppressStartTransaction(request)) {
             filterChain.doFilter(request, response);
             return;
         }
 
         if (RulesStack.getStack().size() > 0) {
             logger.error("RulesStack not previously cleared.");
             RulesStack.getStack().clear();
         }
 
         PlatformTransactionManager txManager = (PlatformTransactionManager) getBean(request, "transactionManager");
         logger.debug(request.getRequestURL() + ", txManager = " + txManager);
 
         DefaultTransactionDefinition def = new DefaultTransactionDefinition();
         def.setName("TxName");
         def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
 
         TransactionStatus status = null;
         try {
             status = txManager.getTransaction(def);
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
 
         try {
             filterChain.doFilter(request, response);
         }
         catch (Throwable ex) {
             try {
                 txManager.rollback(status);
             } catch (Exception e) {
                 e.printStackTrace();
             }
             throw new RuntimeException(ex);
         }
 
         try {
             txManager.commit(status);
 
             TaskStack.execute();
 
 
         } catch (Exception e) {
             // Don't generally log transactions marked as rollback only by service or validation exceptions; logged elsewhere.
            logger.debug(e);
         }
 
     }
 
 
 }
