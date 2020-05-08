 package net.shrine.webclient.client.controllers;
 
 import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
 
 import net.shrine.webclient.client.services.QueryService;
 import net.shrine.webclient.shared.domain.Breakdown;
 import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult;
 import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;
 
 import org.fusesource.restygwt.client.MethodCallback;
 
 /**
  * 
  * @author clint
  * @date Apr 23, 2012
  */
 public final class MockQueryService implements QueryService {
     public Integer totalToReturn = total;
 
     public MultiInstitutionQueryResult multiNodeResultsToReturn = multiNodeResults;
 
     public String lastExpr = null;
 
     static final Integer total = 99;
 
    static final MultiInstitutionQueryResult multiNodeResults = new MultiInstitutionQueryResult() {
        {
            this.put("foo", new SingleInstitutionQueryResult(123L, Collections.<String, Breakdown> emptyMap()));
            this.put("bar", new SingleInstitutionQueryResult(9876L, Collections.<String, Breakdown> emptyMap()));
        }
    };
 
     @Override
     public void performQuery(final String expr, final MethodCallback<MultiInstitutionQueryResult> callback) {
         lastExpr = expr;
 
         callback.onSuccess(null, multiNodeResultsToReturn);
     }
 }
