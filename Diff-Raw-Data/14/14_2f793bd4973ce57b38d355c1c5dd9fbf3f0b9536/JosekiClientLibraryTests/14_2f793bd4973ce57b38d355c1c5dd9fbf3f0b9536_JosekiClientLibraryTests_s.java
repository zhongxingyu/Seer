 /*
  * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  * [See end of file]
  */
  
 package org.joseki.test;
 
 import java.io.* ;
 import org.apache.commons.logging.* ;
 import junit.framework.*;
 import com.hp.hpl.jena.joseki.* ;
 
 
 import com.hp.hpl.jena.rdf.model.* ;
 import com.hp.hpl.jena.shared.*;
 import com.hp.hpl.jena.util.FileManager ;
 import javax.servlet.http.HttpServletResponse ;
 
 /** Tests of the client library
  * 
  *  Note: depends on external files (configuration, data for models)
  *  in the current directory and an already built RDF server.
  *  Tests must leave the server and its models unchanged.
  * 
  * @author      Andy Seaborne
 * @version     $Id: JosekiClientLibraryTests.java,v 1.2 2004-11-03 14:28:28 andy_seaborne Exp $
  */
 public class JosekiClientLibraryTests extends TestSuite
 {
     static Log log = LogFactory.getLog("JosekiClientLibraryTests") ;
     
     TestServer testServer = null ; 
     PrintWriter writer = null ;
     
     static final Model empty = ModelFactory.createDefaultModel() ;
     
     public JosekiClientLibraryTests(TestServer server)
     {
         super("Joseki Client Library Tests") ;
         testServer = server ;
 
         try {
             writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"))) ;
         } catch (java.io.UnsupportedEncodingException ex) {}
 
         Model testData1 = FileManager.get().loadModel("Data/Test/test_data_1.nt") ;
         Model testData2 = FileManager.get().loadModel("Data/Test/test_data_2.nt") ;
         Model testData1_BV = FileManager.get().loadModel("Data/Test/test_data_1-results.n3") ;
         
         String rdqlAll   = "SELECT * WHERE (?x ?y ?z)"; 
         //String rdqlPath2 = "SELECT * WHERE (?x, ?y, ?z), (?z, ?a, ?b)" ;
         //String rdqlProp  = "SELECT * WHERE (?x, <http://jena/property2>, ?z)" ;
         
         int counter = 1 ;
 
         // ------------------------------------------------
 
         // Ping tests - also test "no such model", "no operation".
         // Empty model has "ping" debug inline.   
         addTest(new PingTest( "Ping-"+(counter++), testServer.emptyModelURI, true)) ;
         // Data model has "ping" dedebugd as a standard operation   
         addTest(new PingTest( "Ping-"+(counter++), testServer.testdata_URI, true)) ;
         
         // Model does not exist
         addTest(new PingTest( "Ping-"+(counter++), testServer.noSuchModelURI, false)) ;
         // Has no Ping operation
         addTest(new PingTest( "Ping-"+(counter++), testServer.scratchModelURI, false)) ;
 
 
         // ------------------------------------------------
         // Fetch
         
         addTest(new FetchTest("Fetch-"+(counter++), testServer.emptyModelURI, "http://anything/", empty)) ;
         
         Model m = FileManager.get().loadModel("Data/Test/fetch-result-1.n3") ;
         // Fetch by URI
         addTest(new FetchTest("Fetch-"+(counter++), testServer.fetchModelURI, "http://jena/object#1", m)) ;
 
         // Fetch by property/value
         addTest(new FetchTest("Fetch-"+(counter++), testServer.fetchModelURI,
                 "http://jena/ns#name", "Object/1", false, m)) ;
         
         m = null ;
 
         // ------------------------------------------------
         // Queries that should succeed
         
         // Plain GET
         addTest(new QueryTest("GET-" + (counter++),
                 null, null,
                 testServer.testdata_URI, testData1));
         
         // Plain GET on the XML data model
         addTest(new QueryTest("GET-" + (counter++),
                 null, null,
                 testServer.testdata2_URI, testData1));
         
         // Set the query language to GET
         addTest(new QueryTest("GET-" + (counter++),
                 "GET", null,
                 testServer.testdata_URI, testData1));
         
         addTest(new QueryTest("GET-" + (counter++),
                 "GET", null,
                 testServer.emptyModelURI, empty));
         
         // RDQL query        
         addTest(new QueryTest("Query-" + (counter++) + "-RDQL",
                 "RDQL", rdqlAll,
                 testServer.testdata_URI, testData1));
                 
         addTest(new QueryTest("Query-" + (counter++) + "-RDQL",
                 "rdql", rdqlAll,
                 testServer.testdata_URI, testData1));
                 
         String p[] = { "format" } ;
         String v[] = { "BV" } ;
         // Same as before but ask for bound variable RDF graph result set.
         addTest(new QueryTest("Query-" + (counter++) + "-RDQL",
                 "rdql", rdqlAll, p, v,
                 testServer.testdata_URI, testData1_BV));
                 
 
         // RDQL query forced over HTTP POST        
         QueryTest t1 = new QueryTest("Query-" + (counter++) + "-RDQL/POST",
                             "RDQL", rdqlAll,
                             testServer.testdata_URI, testData1); 
         t1.usePOST = true ;        
         addTest(t1) ;
         
         
         // SPO tests
         {
            Model results = FileManager.get().loadModel("Data/Test/test_data_SPO_1.nt") ;
             QueryTest t = new QueryTest("Query-" + (counter++) + "-SPO",
                              "SPO", null, 
                              new String[]{"p"},new String[]{"http://jena/property1"}, 
                              testServer.testdata2_URI, results) ;
             
             addTest(t) ;
         }        
         // ------------------------------------------------
         // Queries that should not work
         addTest(new QueryTest("Query-" + (counter++) + "-GET",
                 "GET", null,
                 testServer.noSuchModelURI, HttpServletResponse.SC_NOT_FOUND)) ;
 
         addTest(new QueryTest("Query-" + (counter++) + "-NoSuchLanguage",
                 "ThisLanguageDoesNotExist", null,
                 testServer.emptyModelURI, HttpServletResponse.SC_NOT_IMPLEMENTED)) ;
 
         addTest(new QueryTest("Query-" + (counter++) + "-RDQL-NoQuery1",
                 "RDQL", null,
                 testServer.emptyModelURI, HttpServletResponse.SC_BAD_REQUEST)) ;
 
         addTest(new QueryTest("Query-" + (counter++) + "-RDQL-NoQuery2",
                 "RDQL", "",
                 testServer.emptyModelURI, HttpServletResponse.SC_BAD_REQUEST)) ;
 
         addTest(new QueryTest("Query-" + (counter++) + "-RDQL-ParseError",
                 "RDQL", "SELECT * WHERE rubbish",
                 testServer.emptyModelURI, HttpServletResponse.SC_BAD_REQUEST)) ;
 
         // RDQL query forced over HTTP POST (model does not allow it)        
         QueryTest t2 = new QueryTest("Query-" + (counter++) + "-RDQL/POST-failure",
                             "RDQL", rdqlAll,
                             testServer.emptyModelURI, HttpServletResponse.SC_NOT_IMPLEMENTED); 
         t2.usePOST = true ;        
         addTest(t2) ;
                 
         addTest(new QueryTest("Query-" + (counter++) + "-NoLangGiven",
                 null, "Anything",
                 testServer.emptyModelURI, HttpServletResponse.SC_BAD_REQUEST)) ;
 
 
         // ------------------------------------------------
         // Options
         addTest(new OptionsTest( "Options-"+(counter++), testServer.baseURI, true)) ;
         addTest(new OptionsTest( "Options-"+(counter++), testServer.scratchModelURI, true)) ;
 
         // ------------------------------------------------
         // Operations
         
         addTest(new AddTest( "Add-"+(counter++), testServer.scratchModelURI, testData1, testData1)) ;
         // Must not use bNode data
         addTest(new RemoveTest( "Remove-"+(counter++), testServer.scratchModelURI, testData2, empty)) ;
         
         // ------------------------------------------------
         // Inference: RDFS
         
         Model infResult = null ;
         
         // Currently there is a Jena bug that causes a warning:
         // Workaround for bugs 803804 and 858163: using RDF/XML (not RDF/XML-ABBREV) writer  for inferred graph
         // If the model is immutable (i.e. GET just hands out the underlying model).
         
         infResult = FileManager.get().loadModel("Data/Test/inf-rdfs-result-get.rdf") ;
         addTest(new QueryTest("Inf-RDFS-GET-" + (counter++), null, null, testServer.infRDFSModelURI, infResult)) ;
         
        infResult = FileManager.get().loadModel("Data/Test/inf-rdfs-result-fetch.n3") ;
         addTest(new FetchTest("Inf-RDFS-fetch-" + (counter++),
                     testServer.infRDFSModelURI, "http://example.com/resource", infResult)) ;
 
 
         // Inference: OWL
 
        infResult = FileManager.get().loadModel("Data/Test/inf-owl-result-fetch.n3") ;
         addTest(new FetchTest("Inf-OWL-fetch-" + (counter++),
                               testServer.infOWLModelURI, "http://example.org/data/x", infResult)) ;
 
        infResult = FileManager.get().loadModel("Data/Test/inf-owl-result-rdql.n3") ;
         addTest(new QueryTest("Inf-OWL-RDQL-" + (counter++), "RDQL",
                               "SELECT * WHERE (<http://example.org/data/x> rdf:type ?type)",
                               testServer.infOWLModelURI, infResult)) ;
 
         infResult = null ;
     }
     
 //    public void run(TestResult result)
 //    {
 //        super.run(result) ;
 //        // Run and clearup.
 //        // TestCases have a clearup method - Tests (and TestSuites) don't
 //        if ( server != null )
 //            server.stop() ;
 //    }
     
     // Test classes
     
     class QueryTest extends ClientLibraryTest
     {
         String queryLang ;
         String queryString ;
         boolean successExpected = true ;
         boolean usePOST = false ;
         String[] queryParamNames   = null ;
         String[] queryParamValues  = null ;
         
         
         QueryTest(String testName, String qLang, String qString, String target, Model resultModel)
         {
             this(testName, qLang, qString, null, null, target, resultModel) ;
         }
         
 
         // Same but with query parameters 
 
         QueryTest(String testName, String qLang, String qString,
                                    String params[], String values[],  
                                    String target, Model resultModel)
         {
             super(writer, testName, target, resultModel) ;
             queryLang = qLang ;
             queryString = qString ;
             queryParamNames = params ;
             queryParamValues = values ;
         }
         
         QueryTest(String testName, String qLang, String qString, String target, HttpException httpEx)
         {
             this( testName, qLang, qString, null, null, target, httpEx) ;
         }
             
             
 
         QueryTest(String testName, String qLang, String qString, 
                                    String params[], String values[],  
                                    String target, HttpException httpEx)
         {
             super(writer, testName, target, httpEx) ;
             queryLang = qLang ;
             queryString = qString ;
             queryParamNames = params ;
             queryParamValues = values ;
         }
 
         QueryTest(String testName, String qLang, String qString, String target, int responseCode)
         {
             this(testName, qLang, qString, null, null, target, responseCode) ;
         }
             
         QueryTest(String testName, String qLang, String qString,  
                                    String params[], String values[],
                                    String target, int responseCode)
         {
             super(writer, testName, target, responseCode) ;
             queryLang = qLang ;
             queryString = qString ;
             queryParamNames = params ;
             queryParamValues = values ;
         }
 
         protected Model performTest() throws Throwable
         {
             String tmp = (queryString==null?"<<null>>":queryString) ;
             HttpQuery httpQuery = new HttpQuery(modelURI, queryLang) ;
             if ( queryString != null )
                 httpQuery.addParam("query", queryString) ;
 
             if ( queryParamNames != null )
             {
                 if ( queryParamValues == null )
                     throw new RuntimeException("Failed to set up test: no values for parameters.") ;
                 if ( queryParamNames.length != queryParamValues.length )
                     throw new RuntimeException("Failed to set up test: values and paramtyers mismatch.") ;
                 for ( int i = 0 ; i < queryParamNames.length ; i++ )
                 {
                     httpQuery.addParam(queryParamNames[i], queryParamValues[i]) ; 
                 }
             }
             
             if ( usePOST )
                 httpQuery.setForcePOST() ;
             try {
                 Model model = httpQuery.exec() ;
                 return model ;
             } catch (JenaException jEx)
             {
                 log.warn(this.getName()+": "+tmp) ;
                 jEx.printStackTrace(System.out) ;
                 throw jEx ;
             }
         }
         
     }
     
     class FetchTest extends  ClientLibraryTest
     {
         String modelURI ;
         String resource ;
         String predicate ;
         String objectURI ;
         String objectValue ;
         boolean successExpected = true ;
         
         FetchTest(String testName, String target, String thing, Model resultModel)
         {
             super(writer, testName, target, resultModel) ;
             modelURI = target ;
             resource = thing ;
             predicate = null ;
             objectValue = null ;
             objectURI = null ;
         }
         
         // predicate/value form
         FetchTest(String testName, String target, String predicateURI, 
                   String objURIorValue, boolean isURI, Model resultModel)
         {
             super(writer, testName, target, resultModel) ;
             modelURI = target ;
             resource = null ;
             predicate = predicateURI ;
             
             objectValue = null ;
             objectURI = null ;
             if ( isURI )
                 objectURI = objURIorValue;
             else
                 objectValue= objURIorValue ;
         }
         
         protected Model performTest() throws Throwable
         {
             if ( resource != null )
             {
                 HttpFetch httpFetch = new HttpFetch(modelURI, resource) ; 
                 Model model = httpFetch.exec();
                 return model;
             }
             if ( predicate != null )
             {
                 String node = null ;
                 boolean isResource = false ;
                 
                 if ( objectURI != null )
                 {    
                     node = objectURI ;
                     isResource = true ;
                 }
                 if ( objectValue != null )
                 {
                     node = objectValue ;
                     isResource = false ;
                 }
                 
                 HttpFetch httpFetch = new HttpFetch(modelURI, predicate, node, isResource) ; 
                 Model model = httpFetch.exec();
                 return model;
             }
             fail("Test setup failure: no resource or predciate for fetch") ;
             return null ;
         }
     }
     
     class AddTest extends ClientLibraryTest
     {
         Model inputModel ;
         
         AddTest(String testName, String target, Model d, Model results)
         {
             super(writer, testName, target, results) ;
             inputModel = d ;
             modelURI = target ;
         }
 
         protected Model performTest() throws Throwable
         {
             HttpAdd httpAdd = new HttpAdd(modelURI);
             httpAdd.add(inputModel);
             httpAdd.exec();
 
             HttpQuery httpGet = new HttpQuery(modelURI);
             Model model1 = httpGet.exec();
             return model1 ;
         }
         
         protected void takeDownTest() throws Throwable
         {
             HttpClear httpClear = new HttpClear(modelURI);
             httpClear.exec();
             HttpQuery httpGetZ = new HttpQuery(modelURI);
             Model modelZ = httpGetZ.exec();
             assertTrue(modelZ.size() == 0);
         }
 
     }
     
     class RemoveTest extends ClientLibraryTest
     {
         Model inputModel ;
         
         RemoveTest(String testName, String target, Model d, Model results)
         {
             super(writer, testName, target, results) ;
             inputModel = d ;
         }
 
         protected void setUpTest() throws Throwable
         {
             HttpAdd httpAdd = new HttpAdd(modelURI);
             httpAdd.add(inputModel);
             httpAdd.exec();
 
             HttpQuery httpGet1 = new HttpQuery(modelURI);
             Model model1 = httpGet1.exec();
 
             boolean passesTest = model1.isIsomorphicWith(inputModel) ;
             if (!passesTest)
             {
                 out.println(this.getName() + " Expected --------------------------");
                 dumpModel(inputModel);
                 out.println(this.getName() + " Got -------------------------------");
                 dumpModel(model1);
                 out.println(this.getName() + " -----------------------------------");
             }
             assertTrue("Target model not initialized correctly",passesTest) ;
         }
 
         protected Model performTest() throws Throwable
         {
             log.debug(getName()+" :: Remove("+modelURI+")") ;
             HttpRemove httpRemove = new HttpRemove(modelURI) ;
             httpRemove.remove(inputModel) ;
             Model result = httpRemove.exec() ;
             
             HttpQuery httpGet2 = new HttpQuery(modelURI);
             Model model2 = httpGet2.exec();
             if ( model2.size() != 0 )
             {
                 out.println(this.getName() + " Expected --------------------------");
                 out.println("Empty model") ;
                 out.println(this.getName() + " Got -------------------------------");
                 dumpModel(model2);
                 out.println(this.getName() + " -----------------------------------");
                 
             }
             assertTrue(model2.size() == 0) ;
             StmtIterator sIter = inputModel.listStatements() ;
             for ( ; sIter.hasNext() ; )
             {
                 Statement s = sIter.nextStatement() ;
                 assertTrue( !model2.contains(s) ) ;
             }
             return result ;
         }
         
         protected void takeDownTest() throws Throwable
         {
             // Ensure empty.
             HttpClear httpClear = new HttpClear(modelURI);
             httpClear.exec();
             HttpQuery httpGetZ = new HttpQuery(modelURI);
             Model modelZ = httpGetZ.exec();
             assertTrue(modelZ.size() == 0);
         }
     }
     
     class OptionsTest extends ClientLibraryTest
     {
         OptionsTest(String testName, String target, boolean shouldWork)
         {
             super(writer, testName, target, shouldWork) ;
         }
         
         protected Model performTest() throws Throwable
         {
             HttpOptions httpOptions = new HttpOptions(modelURI) ;
             Model  resultModel = httpOptions.exec() ;
             assertTrue(resultModel.size() != 0 ) ;
             return resultModel ;    
         }
     }
 
     class PingTest extends ClientLibraryTest
     {
         String modelURI ;
         Model data ;
         boolean expectSuccess ;
         
         PingTest(String testName, String target, boolean shouldWork)
         {
             super(writer, testName, target, shouldWork) ;
             modelURI = target ;
             expectSuccess = shouldWork ;
         }
         
         protected Model performTest() throws Throwable
         {
             HttpPing httpPing = new HttpPing(modelURI) ;
             Model resultModel = httpPing.exec() ;
             return resultModel ;
         }
     }
 }
 
 /*
  *  (c) Copyright 2003,2004 Hewlett-Packard Development Company, LP
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
