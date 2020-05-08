 package dk.nsi.minlog.server.dao.ebean;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.SqlRow;
 import com.trifork.dgws.WhitelistChecker;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
 
 @Repository
 public class WhitelistCheckerDefault implements WhitelistChecker {
     @Inject
     EbeanServer ebeanServer;
 
     @Override
     public Set<String> getLegalCvrNumbers(String whitelist) {
        final Set<SqlRow> sqlRows = ebeanServer.createSqlQuery("SELECT legal_cvr FROM whitelist WHERE name = '" + whitelist + "'").findSet();
         final Set<String> result = new HashSet<String>();
         for (SqlRow sqlRow : sqlRows) {
             result.add(sqlRow.getString("legal_cvr"));
         }
         return result;
     }
 }
