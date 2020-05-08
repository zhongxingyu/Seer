 package com.kwc.testen.rest;
 
 import com.kwc.testen.db.TestResultRepository;
 import com.kwc.testen.model.TestResult;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * @author Marius Kristensen
  */
 @Path("/tests/")
 public class TestRestService {
 
     @GET
     @Path("/")
     @Produces("application/json")
     public List<TestResult> printMessage() {
         TestResultRepository repository = new TestResultRepository();
         return repository.getAllTestResults();
     }
 
     @GET
     @Path("/{param}")
     @Produces("application/json")
     public int getOne(@PathParam("param") String msg) throws SQLException {
         TestResultRepository repository = new TestResultRepository();
         Integer rowCount = repository.countRows();
         return rowCount != null ? rowCount : 0 ;
     }
 
 }
