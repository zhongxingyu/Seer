 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.test;
 
 import javax.xml.transform.TransformerException;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.DatasetFactory;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolutionMap;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 
 public class FromWFtoDFtest {
 
     public static void main(final String[] args) throws TransformerException {
     	
 //    	String calliBase = "http://151.100.179.11:4040/";
 //    	Resource dfRoot =
 //    			ResourceFactory.createResource(calliBase + "swows-test/FAO/fao");
     	String calliBase = "http://localhost:4040/";
     	Resource dfRoot =
     			ResourceFactory.createResource(calliBase + "test/fao2/FAO/fao");
     	Resource dfEndpoint =
     			ResourceFactory.createResource(calliBase + "sparql");
     	
     	QuerySolutionMap initialBindings = new QuerySolutionMap();
     	initialBindings.add("dfRoot", dfRoot);
     	initialBindings.add("dfEndpoint", dfEndpoint);
     	
     	String baseUri = "resources/sparql/fromWFtoDF/";
     	
     	String[] queryStrings = {
     			"comp-dataflow.rq",
     			
     			"comp-defaultSink.rq",
     			"comp-namedSink.rq",
     			"comp-defaultSource.rq",
     			"comp-namedSource.rq",
     			
     			"comp-URIsource.rq",
     			"comp-transf.rq",
     			"comp-store.rq",
 
    			"comp-config-sp.rq",
     			"comp-gProd.rq",
     			"comp-dsProd.rq",
     			"comp-dsCons-default.rq",
     			"comp-dsCons-named.rq",
     			"comp-link.rq"
    	};
     	
 //		Dataset inputDataset = DatasetFactory.create(defaultGraphUri, namedGraphUris);
     	//Dataset inputDataset = DatasetFactory.create(defaultModel);
 		Dataset inputDataset =
 				DatasetFactory.create(
 						ModelFactory.createModelForGraph(Graph.emptyGraph));
 		
     	long queryStart, queryEnd;
     	
 		queryStart = System.currentTimeMillis();
 		
 		Model transfResult = null;
     	for (String queryStr : queryStrings) {
     		Query transfQuery = QueryFactory.read(baseUri + queryStr);
     		QueryExecution queryExecution =
     				QueryExecutionFactory.create(transfQuery, inputDataset, initialBindings);
     		if (transfResult == null)
     			transfResult = queryExecution.execConstruct();
     		else
     			queryExecution.execConstruct(transfResult);
     	}	
     	
 
 //		QueryExecution queryExecution =
 //				QueryExecutionFactory.create(transfQuery, inputDataset);
 //		Model inputQueryResult = queryExecution.execConstruct();
 		queryEnd = System.currentTimeMillis();
 		System.out.println("Query execution time: " + (queryEnd - queryStart) );
 
 		System.out.println();
     	System.out.println("**************************");
     	System.out.println("*** Query Result ***");
     	System.out.println("**************************");
 //    	transfResult.write(System.out,"N3");
     	transfResult.write(System.out);
     	System.out.println("****************************");
     	System.out.println();
     	
 //		queryStart = System.currentTimeMillis();
 //		QueryExecution outQueryExecution =
 //				QueryExecutionFactory.create(outputQuery, inputDataset);
 //		Model outputQueryResult = outQueryExecution.execConstruct();
 //		queryEnd = System.currentTimeMillis();
 //		System.out.println("Output Query execution time: " + (queryEnd - queryStart) );
 //		
 //    	System.out.println();
 //    	System.out.println("**************************");
 //    	System.out.println("*** Output Query Result ***");
 //    	System.out.println("**************************");
 //    	outputQueryResult.write(System.out,"N3");
 //    	System.out.println("****************************");
 //    	System.out.println();
 
     }
 		
 }
