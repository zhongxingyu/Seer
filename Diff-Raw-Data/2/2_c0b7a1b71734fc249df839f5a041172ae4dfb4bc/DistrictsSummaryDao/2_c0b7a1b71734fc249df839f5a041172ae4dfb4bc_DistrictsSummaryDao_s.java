 package ru.yandex.hackaton.server.db.dao;
 
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.SessionFactory;
 import org.hibernate.transform.Transformers;
 import ru.yandex.hackaton.server.db.model.DistrictsSummary;
 import ru.yandex.hackaton.server.db.model.Elementary;
 import ru.yandex.hackaton.server.resources.SearchParams;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Sergey Polovko
  */
 @Singleton
 public class DistrictsSummaryDao extends CrudDao<DistrictsSummary> {
 
     @Inject
     public DistrictsSummaryDao(SessionFactory sessionFactory) {
         super(sessionFactory);
     }
 
     public List<DistrictsSummary> find(SearchParams params) {
         List<DistrictsSummary> result =  list(toQuery(params));
         BigDecimal summaryMax = result.get(0).getSumm();
         if (summaryMax.floatValue() > 0) {
             for(DistrictsSummary ds : result) {
                ds.setSumm(ds.getSumm().multiply(BigDecimal.valueOf(100)).divide(summaryMax, 2, RoundingMode.HALF_UP));
             }
         }
         return result;
     }
 
     private Query toQuery(SearchParams params) {
         String query = "SELECT " + getColumns(params) + " FROM districtssummary ORDER BY summ DESC";
         System.out.println(query);
         return currentSession().createSQLQuery(query)
                 .setResultTransformer(Transformers.aliasToBean(DistrictsSummary.class));
     }
 
     public String getColumns(SearchParams params) {
         StringBuffer result = new StringBuffer("districtssummary.districtid, districtssummary.districtname");
         boolean hasParams = false;
         System.out.println(params.getParams());
         for (String param : params.getParams().keySet()) {
             if (params.getParams().get(param) != 0) {
                 result.append(", " + param);
                 hasParams = true;
             }
         }
         result.append(", ");
 
         result.append("(");
         if (hasParams) {
             for (String param : params.getParams().keySet()) {
                 result.append(param + " * " + params.getParams().get(param) + " + ");
             }
         } else {
             for (String param : params.getParams().keySet()) {
                 result.append(param + " + ");
             }
         }
 
         String res = result.substring(0, result.length() - 3);
         res += ") * districtssummary.size * 1.0";
         res += " as summ";
 
         return res;
     }
 }
