 package com.jaxzin.handlecheck.server;
 
 import com.jaxzin.handlecheck.domain.Handle;
 import com.jaxzin.handlecheck.domain.HttpHeader;
 import com.jaxzin.handlecheck.domain.PMF;
 import com.jaxzin.handlecheck.domain.ResponseFragment;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
 * <p></p>
 * <p/>
 * <p>
 * <div style="font-weight:bold">Revision Information:</div>
 * <ul>
 * <li>$Id$</li>
 * <li>$Author$</li>
 * <li>$DateTime$</li>
 * </ul>
 * </p>
 * <p/>
 * <p>Copyright 2009 ESPN.com and Disney Interactive Media Group.  All rights reserved.</p>
 *
 * @author <a href="mailto:Brian.R.Jackson@espn3.com">Brian R. Jackson</a>
 * @version $Change$
  */
 public class AdminServlet extends HttpServlet {
 
     private static final int MAX_BATCH_DELETES = 500;
     private static final int MAX_BATCH_FETCH = 3;
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         if("true".equals(request.getParameter("deleteEverything"))) {
             PersistenceManager pm = PMF.get().getPersistenceManager();
 
             int total, temp;
             Query query = pm.newQuery(Handle.class);
 //            query.setFilter("lastCheckedOn < dateFilter");
 //            query.declareParameters("java.util.Date dateFilter");
 //            query.setRange(0, MAX_BATCH_FETCH);
 //            try {
 //                total = 0;
 //                //noinspection unchecked
 //                while((temp = batchDelete(pm, (List<Object>) query.execute(new Date()))) != 0) {
 //                    total += temp;
 //                }
 //                response.getWriter().println("<p>Deleted "+ total +" Query</p>");
 //            } finally {
 //                query.closeAll();
 //            }
 //
 //            query = pm.newQuery(HandleProviderAvailability.class);
 //            query.setRange(0, MAX_BATCH_FETCH);
 //            try {
 //                total = 0;
 //                //noinspection unchecked
 //                while((temp = batchDelete(pm, (List<Object>) query.execute())) != 0) {
 //                    total += temp;
 //                }
 //                response.getWriter().println("<p>Deleted "+ total +" HandleProviderAvailability</p>");
 //            } finally {
 //                query.closeAll();
 //            }
 //
 //
 //            query = pm.newQuery(HandleProviderResponse.class);
 //            query.setRange(0, MAX_BATCH_FETCH);
 //            try {
 //                total = 0;
 //                //noinspection unchecked
 //                while((temp = batchDelete(pm, (List<Object>) query.execute())) != 0) {
 //                    total += temp;
 //                }
 //                response.getWriter().println("<p>Deleted "+ total +" HandleProviderResponse</p>");
 //            } finally {
 //                query.closeAll();
 //            }
 
             query = pm.newQuery(HttpHeader.class);
             query.setFilter("key == 'server'");
             query.setRange(0, MAX_BATCH_FETCH);
             try {
                 total = batchDelete(pm, (List<Object>) query.execute());
 //                total = 0;
 ////                noinspection unchecked
 //                while((temp = batchDelete(pm, (List<Object>) query.execute())) != 0) {
 //                    total += temp;
 //                }
                 response.getWriter().println("<p>Deleted "+ total +" HttpHeader</p>");
             } finally {
                 query.closeAll();
             }
 
 
             query = pm.newQuery(ResponseFragment.class);
             query.setRange(0, MAX_BATCH_FETCH);
             try {
                 total = batchDelete(pm, (List<Object>) query.execute());
 //                total = 0;
 //                //noinspection unchecked
 //                while((temp = batchDelete(pm, (List<Object>) query.execute())) != 0) {
 //                    total += temp;
 //                }
                 response.getWriter().println("<p>Deleted "+ total +" ResponseFragment</p>");
             } finally {
                 query.closeAll();
             }
         }
     }
 
     private int batchDelete(PersistenceManager pm, Iterable<?> results) {
         int total = 0;
         List<Object> batch = new LinkedList<Object>();
         for (Object result : results) {
             if(batch.size() == MAX_BATCH_DELETES) {
                 pm.deletePersistentAll(batch);
                 total += batch.size();
                 batch.clear();
             }
             batch.add(result);
         }
         if(!batch.isEmpty()) {
             pm.deletePersistentAll(batch);
             total += batch.size();
             batch.clear();
         }
         return total;
     }
 }
