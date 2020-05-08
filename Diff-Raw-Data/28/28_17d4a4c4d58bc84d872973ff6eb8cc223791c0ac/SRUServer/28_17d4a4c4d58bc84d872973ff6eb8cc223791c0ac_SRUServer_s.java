 package com.computas.sublima.query.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.io.IOException;
 
 import org.apache.cocoon.ProcessingException;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.log4j.Logger;
 import org.z3950.zing.cql.CQLParseException;
 
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.exceptions.UnsupportedCQLFeatureException;
 import com.computas.sublima.query.service.CQL2SPARQL;
 
 public class SRUServer implements StatelessAppleController {
 
 	private SparqlDispatcher sparqlDispatcher;
     static Logger logger = Logger.getLogger(SRUServer.class);
 
     public void process(AppleRequest req, AppleResponse res) throws Exception {
         int errorcode = 0; // The diagnostics code
         String errormsg = "";
         String errordetail = "";
         String operation = req.getCocoonRequest().getParameter("operation");
         if ("explain".equalsIgnoreCase(operation) || req.getCocoonRequest().getParameters().size() == 0) {
             // Then, we have a valid Explain operation
         } else
         if ("searchRetrieve".equals(operation)) {
             // The actual querying goes here.
 
            String sparqlQuery = new String();
             try {
                 CQL2SPARQL converter = new CQL2SPARQL(req.getCocoonRequest().getParameter("query"));
                 sparqlQuery = converter.Level0();
             }
             catch (IOException e) {
                 errorcode = 1;
                 errordetail = e.getMessage();
                 errormsg = e.getMessage();
                 logger.warn("CQL2SPARQL threw IOException: " + e.getMessage());
             }
             catch (CQLParseException e) {
                 errorcode = 10;
                 errormsg = e.getMessage();
             }
             catch (UnsupportedCQLFeatureException e) {
                 errorcode = 48;
                 errordetail = e.getMessage();
                 errormsg = e.getMessage();
             }
             if(sparqlQuery == null || "".equals(sparqlQuery)) {
                 throw new ProcessingException("A SPARQL query string has to be passed.");
             }
            Object queryResult = sparqlDispatcher.query(sparqlQuery);
            Map<String, Object> bizData = new HashMap<String, Object>();
            bizData.put("result", queryResult);
            res.sendPage("rdf/result-list", bizData);
         } else {
             errorcode = 4;
            errormsg = "Server supports only explain and searchRetrieve."
         }
     }
 
 	public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
 		this.sparqlDispatcher = sparqlDispatcher;
 	}
 
 }
