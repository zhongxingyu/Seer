 package dk.nsi.minlog.server.dao.ebean;
 
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

 import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
 import com.avaje.ebean.SqlRow;
 import com.trifork.dgws.WhitelistChecker;
 
 @Repository
 public class WhitelistCheckerDefault implements WhitelistChecker {
     @Inject
     EbeanServer ebeanServer;
 
     @Override
     public Set<String> getLegalCvrNumbers(String whitelist) {
        SqlQuery query = ebeanServer.createSqlQuery("SELECT legal_cvr FROM whitelist WHERE name = :whitelist");
        query.setParameter("whitelist", whitelist);
        final Set<SqlRow> sqlRows = query.findSet(); 
        		
         final Set<String> result = new HashSet<String>();
         for (SqlRow sqlRow : sqlRows) {
             result.add(sqlRow.getString("legal_cvr"));
         }
         return result;
     }
 }
