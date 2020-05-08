 /*
  * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  * [See end of file]
  */
 
 package com.hp.hpl.jena.joseki;
 
 import org.apache.commons.logging.* ;
 import java.net.* ;
 
 //import com.hp.hpl.jena.common.* ;
 import com.hp.hpl.jena.rdf.model.* ;
 import com.hp.hpl.jena.util.FileManager;
 //import com.hp.hpl.jena.rdf.model.impl.* ;
 //import com.hp.hpl.jena.vocabulary.* ;
 
 import com.hp.hpl.jena.query.* ;
 
 /** Create an execution object for performing an RDQL query
  *  on a model over HTTP.
  *
  * @author  Andy Seaborne
 * @version $Id: QueryHTTP.java,v 1.6 2004-12-20 18:23:06 andy_seaborne Exp $
  */
 public class QueryHTTP implements QueryExecution
 {
     static final Log logger = LogFactory.getLog(QueryHTTP.class.getName()) ;
     
     Query query ;
     HttpQuery qHTTP ;
     Model resultModel = null ;
     
     public QueryHTTP(Query q, String urlStr, String lang)
     {
         query = q ;
         String queryString = q.toString().replaceAll("\\s{2,}", " ") ;
         qHTTP = new HttpQuery(urlStr, lang) ;
         qHTTP.addParam("query", queryString) ;
     }
 
     public QueryHTTP(Query q, URL u, String lang)
     {
         query = q ;
         String queryString = q.toString().replaceAll("\\s{2,}", " ") ;
         qHTTP = new HttpQuery(u, lang) ;
         qHTTP.addParam("query", queryString) ;
     }
 
      /** Initialise a query execution.  May be called before exec.
      *  If it has not be called, the query engine will initialise
      *  itself during the exec() method.
      */
 
     public void init()
     {
         return ;
     }
     
     // Iterator of ResultBindings
     public ResultSet execSelect()
     {
         try {
             
             resultModel = null ;
             try {
                 resultModel = qHTTP.exec() ;
             } catch (HttpException httpEx)
             {
                 logger.debug("Error on remote invokation: "+httpEx) ;
                 throw httpEx ;
             }
             
             if ( resultModel.size() == 0    )
                 logger.debug("Model size is zero") ;
 
             // Execute the query locally to build result binding.
            query.setDataSet(resultModel) ;
             QueryExecution qexec = QueryFactory.createQueryExecution(query);
             return qexec.execSelect() ;
         } 
         catch (RDFException rdfEx)
         {
             logger.debug("RDFException: "+rdfEx) ;
             return null;
         }
     }
 
     /** Stop in mid execution.
      * No guarantee that the concrete implementation actual will stop or
      * that it will do so immediately.
      */
     public void abort() { }
     
     /** Normal end of use of this execution  */
     public void close() { resultModel = null ; }
     
     /** Get the last result model */ 
     public Model getResultModel() { return resultModel ; }
 
     /** Release last result model (in case its very large). */ 
     public void releaseResultModel() { resultModel = null ; }
     
     public HttpQuery getHttpQuery() { return qHTTP ; }
 
     
     public Model execConstruct()
     {
         logger.warn("Unimplemented: execConstruct)") ;
         return null;
     }
 
     public Model execDescribe()
     {
         logger.warn("Unimplemented: execDescribe)") ;
         return null;
     }
 
     public boolean execAsk()
     {
         logger.warn("Unimplemented: execAsk)") ;
         return false;
     }
 
     public void setFileManager(FileManager arg0)
     {
         throw new java.lang.UnsupportedOperationException(this.getClass().getName()+"setFileManager()") ;
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
