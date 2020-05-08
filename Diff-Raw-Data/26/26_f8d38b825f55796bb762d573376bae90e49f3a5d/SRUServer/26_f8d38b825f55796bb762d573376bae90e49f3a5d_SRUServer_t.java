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
         String sparqlQuery = null;
         String errormsg = "";
         String errordetail = "";
         Object queryResult = null;
         String operation = req.getCocoonRequest().getParameter("operation");
         if ("explain".equalsIgnoreCase(operation) || req.getCocoonRequest().getParameters().size() == 0) {
             // Then, we have a valid Explain operation
             Map<String, Object> bizData = new HashMap<String, Object>();
             bizData.put("servername", req.getCocoonRequest().getServerName());
             bizData.put("serverport", req.getCocoonRequest().getServerPort());
             bizData.put("requesturi", req.getCocoonRequest().getRequestURI());
             res.sendPage("sru/explain", bizData);
             return;
         } else
         if ("searchRetrieve".equals(operation)) {
             // The actual querying goes here.
             try {
                Double version = new Double(req.getCocoonRequest().getParameter("version"));
                if (version.doubleValue() > 1.1) {
                    errorcode = 5;
                    errordetail = "1.1";
                    errormsg = "Unsupported version.";
                }
            } catch (NumberFormatException e) {
                errorcode = 6;
                errordetail = "version";
                errormsg = e.getMessage();
            }
            if (req.getCocoonRequest().getParameter("recordSchema") != null) {
                errorcode = 66;
                errordetail = "cql.serverChoice";
                errormsg = "Setting schema is not supported, only serverChoice.";
            }
            if (req.getCocoonRequest().getParameter("sortKey") != null) {
                errorcode = 80;
                errormsg = "Sort not supported.";
            }

            if ("string".equals(req.getCocoonRequest().getParameter("recordPacking"))) {
                errorcode = 71;
                errormsg = "Unsupported record packing.";
            }
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
         } else {
             errorcode = 4;
             errormsg = "Server supports only explain and searchRetrieve.";
         }
         if (errorcode == 0) {
             logger.trace("CQL-converted SPARQL query:\n" + sparqlQuery);
             queryResult = sparqlDispatcher.query(sparqlQuery);
             if (queryResult == null) {
                 errorcode = 47;
                 errormsg = "SPARQL Dispatcher returned a null result, the server may be at fault.";
             }
         }
         if (errorcode > 0) {
             logger.debug("Some SRU error, code: " + errorcode + ". Message: " + errormsg);
             Map<String, Object> bizData = new HashMap<String, Object>();
             bizData.put("errorcode", errorcode);
             bizData.put("errormsg", errormsg);
             bizData.put("errordetail", errordetail);
             res.sendPage("sru/error", bizData);
         } else {
             Map<String, Object> bizData = new HashMap<String, Object>();
             bizData.put("result", queryResult);
             res.sendPage("sru/sru-results", bizData);
         }
 
     }
 
 	public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
 		this.sparqlDispatcher = sparqlDispatcher;
 	}
 
 }
