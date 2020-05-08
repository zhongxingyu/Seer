 /*
  * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  * [See end of file]
  */
 
 package org.joseki.server.processors.sparql;
 
 import java.io.PrintWriter ;
 import org.apache.commons.logging.*;
 
 import com.hp.hpl.jena.rdf.model.*;
 
 import org.joseki.Joseki ;
 import org.joseki.server.*;
 //import org.joseki.server.http.HttpResultSerializer;
 import org.joseki.vocabulary.JosekiVocab;
 import com.hp.hpl.jena.util.FileUtils ;
 
 
 import com.hp.hpl.jena.query.* ;
 
 /** SPARQL operations
  * 
  * @author  Andy Seaborne
 * @version $Id: SPARQL.java,v 1.14 2004-12-20 18:23:06 andy_seaborne Exp $
  */
 
 public class SPARQL extends QueryProcessorCom
 {
     static private Log log = LogFactory.getLog(SPARQL.class) ;
     
     public SPARQL() { super("SPARQL") ; }
 
 //    /** Processor.exec operation - provided by QueryProcessorCom*/
 //    public void exec(Request request, Response response) throws ExecutionException
 //    {
 //        throw new QueryExecutionException(Response.rcNotImplemented, "SPARQL.execQuery") ;
 //    }
 
     /** QueryProcessor.execQuery */
     public void execQuery(SourceModel src, String queryString,
                           Request request, Response response)
         throws QueryExecutionException
     {
         try {
             if (!(src instanceof SourceModelJena))
                 throw new QueryExecutionException(
                     ExecutionError.rcOperationNotSupported,
                     "Wrong implementation - this RDQL processor works with Jena models");         
             Model model = ((SourceModelJena)src).getModel() ;
             
             if (queryString == null )
             {
                 log.debug("Query: No query argument") ;
                 throw new QueryExecutionException(
                     ExecutionError.rcQueryExecutionFailure,
                     "No query supplied");
             } 
              
             if ( queryString.equals("") )
             {
                 log.debug("Query: No query argument") ;
                 throw new QueryExecutionException(
                     ExecutionError.rcQueryExecutionFailure,
                     "Empty query string");
             }
             
             String queryStringLog = formatForLog(queryString) ;
             log.debug("Query: "+queryStringLog) ;
             
             Query query = null ;
             try {
                 query = Query.create(queryString, Query.SyntaxSPARQL) ;
             } catch (QueryException ex)
             {
                 // TODO Enable errors to be sent as body
                 //    response.setError(ExecutionError.rcQueryParseFailure)
                 //    OutputString out = response.getOutputStream() ;
                 //    out.write(something meaning full)
                 String tmp = queryString +"\n\r" + ex.getMessage() ;
                 throw new QueryExecutionException(ExecutionError.rcQueryParseFailure, "Parse error: \n"+tmp) ;
             } catch (Throwable thrown)
             {
                 log.info("Query unknown error during parsing: "+queryStringLog, thrown) ;
                 throw new QueryExecutionException(ExecutionError.rcQueryParseFailure, "Unknown Parse error") ;
             }
             
            query.setDataSet(model) ;
             
             QueryExecution qe = QueryFactory.createQueryExecution(query) ;
 
             // Test for SELECT in XML result set form
             boolean wantsAppXML = response.accepts("Accept", "application/xml") ;
             
             // Tests needed:
             // is app/xml prefed over app/rdf+xml?
             // test for is type X acceptable
             // response.acceptable(
             
             // Browser tests:
             // is text/* preferred over 
             
             if ( query.isSelectType() &&  wantsAppXML )
             {
                 execQueryXML(query, response) ;
                 log.info("OK - URI="+request.getModelURI()+" : "+queryStringLog) ;
                 return ;
             }
             
             if ( query.isAskType() )
                 execQueryAsk(query, response) ;
             else
             {
                 // SELECT / RDF results, CONSTRUCT or DESCRIBE
                 Model results = execQueryModel(query) ;
                 response.doResponse(results) ;
             }
             log.info("OK - URI="+request.getModelURI()+" : "+queryStringLog) ;
         }
         catch (QueryException qEx)
         {
             log.info("Query execution error: "+qEx) ;
             QueryExecutionException qExEx = new QueryExecutionException(ExecutionError.rcQueryExecutionFailure, qEx.getMessage()) ;
             throw qExEx ;
             //response.doException(qExEx) ;
         }
     }
 
     private Model execQueryModel(Query query) throws QueryExecutionException
     {
         try {
             QueryExecution qe = QueryFactory.createQueryExecution(query) ;
             
             if ( query.isSelectType() )
             {
                 ResultSet results = qe.execSelect() ;
                 ResultSetFormatter rsFmt = new  ResultSetFormatter(results) ;
                 return rsFmt.toModel() ;
             }
             
             if ( query.isConstructType() )
                 return qe.execConstruct() ;
             
             if ( query.isDescribeType() )
                 return qe.execDescribe() ; 
             
             if ( query.isAskType() )
             {
                 log.warn("Not implemented: ASK queries") ;
                 throw new QueryExecutionException(ExecutionError.rcOperationNotSupported, "ASK query") ;
             }
             
             log.warn("Unknown query type") ;
             throw new QueryExecutionException(ExecutionError.rcOperationNotSupported, "Unknown query type") ;
         }
         catch (QueryException qEx)
         {
             log.info("Query execution error: "+qEx) ;
             throw new QueryExecutionException(ExecutionError.rcQueryExecutionFailure, null) ;
         }
     }
     
     
     
     public static void execQueryXML(Query query, Response response)
         throws QueryExecutionException
     {
         try {
             QueryExecution qe = QueryFactory.createQueryExecution(query) ;
             ResultSetFormatter fmt = new ResultSetFormatter(qe.execSelect()) ;
             // TODO Remove any HTTPisms
             response.setMimeType(Joseki.contentTypeXML) ;
             // See doResponse as well - more header setting?  How to abstract?
             response.setResponseCode(Response.rcOK) ;
             response.startResponse() ;
             fmt.outputAsXML(response.getOutputStream()) ;
             response.finishResponse() ;
         }
         //throw new QueryExecutionException(Response.rcNotImplemented, "SPARQL.execQueryXML") ;
         catch (QueryException qEx)
         {
             log.info("Query execution error: "+qEx) ;
             throw new QueryExecutionException(ExecutionError.rcQueryExecutionFailure, null) ;
         }
     }
 
     public static void execQueryAsk(Query query, Response response)
         throws QueryExecutionException
     {
         try {
             QueryExecution qe = QueryFactory.createQueryExecution(query) ;
             boolean result = qe.execAsk() ;
             response.setMimeType(Joseki.contentTypeTextPlain) ;
             response.setResponseCode(Response.rcOK) ;
             response.startResponse() ;
             PrintWriter pw = FileUtils.asPrintWriterUTF8(response.getOutputStream()) ;
             pw.println(result?"yes":"no" ) ;
             pw.flush() ;
             response.finishResponse() ;
         }
         //throw new QueryExecutionException(Response.rcNotImplemented, "SPARQL.execQueryXML") ;
         catch (QueryException qEx)
         {
             log.info("Query execution error: "+qEx) ;
             throw new QueryExecutionException(ExecutionError.rcQueryExecutionFailure, null) ;
         }
         
     }
 
     
     
     public String getInterfaceURI() { return JosekiVocab.queryOperationSPARQL ; }
 
     public void init(Resource binding, Resource implementation) {}
     
     private String formatForLog(String queryString)
     {
         String tmp = queryString ;
         tmp = tmp.replace('\n', ' ') ;
         tmp = tmp.replace('\r', ' ') ;
         return tmp ;
     }
 }
 
 /*
  *  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  *  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
