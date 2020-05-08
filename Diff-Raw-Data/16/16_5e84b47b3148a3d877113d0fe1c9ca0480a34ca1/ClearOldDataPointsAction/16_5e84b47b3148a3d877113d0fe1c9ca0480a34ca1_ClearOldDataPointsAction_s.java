 package com.jute.google.perf.action;
 
 import com.jute.google.framework.Action;
 import com.jute.google.framework.PMF;
import com.jute.google.perf.model.Page;
 import com.jute.google.perf.model.DataPoint;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import java.util.Map;
 import java.util.List;
import java.util.Iterator;
 import java.util.Date;
 
 /**
  * User: hugozhu
  * Date: Jun 19, 2009
  * Time: 5:08:43 AM
  */
 public class ClearOldDataPointsAction  extends Action {
     public String execute(Map context, HttpServletRequest req, HttpServletResponse resp) throws Exception {
         PersistenceManager pm = PMF.get().getPersistenceManager();
         Query query = pm.newQuery(DataPoint.class);
        query.setFilter("date < dateMax");
        query.declareParameters("Date dateMax");
         query.setOrdering("date asc");
        query.setRange(0,1000);
         Date threeMonthAgo = new Date();
        threeMonthAgo.setTime(threeMonthAgo.getTime()-24*3600*30*3);
         List<DataPoint> points = (List<DataPoint>) query.execute(threeMonthAgo);
         pm.deletePersistentAll(points);
         return null;
     }
 }
