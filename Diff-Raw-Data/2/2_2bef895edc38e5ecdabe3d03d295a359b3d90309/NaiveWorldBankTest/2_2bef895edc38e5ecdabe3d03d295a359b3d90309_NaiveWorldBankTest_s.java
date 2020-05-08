 package org.rdfindex.visitor;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.rdfindex.dao.MetadataDAOImpl;
 import org.rdfindex.dao.RDFIndexMetadataDAO;
 import org.rdfindex.to.IndexTO;
 
 import test.utils.TestHelper;
 
 public class NaiveWorldBankTest {
 
 	@Test
 	public void testWeightedQueryIndex() throws Exception {
 		RDFIndexMetadataDAO metadata = new MetadataDAOImpl(
 				TestHelper.INDEX_MODEL, 
 				TestHelper.createModel("wb/naive-worldbank.ttl"),
 				TestHelper.createModel("wb/naive-worldbank-observations.ttl"));	
 		RDFIndexVisitor rdfIndexProcessor = new RDFIndexSPARQLGeneratorVisitor(metadata);
 		//Test as visitor
 		List<IndexTO> indexes = metadata.getIndexMetadata();
 		
 		for(IndexTO index:indexes){
 			String sparqlQuery = (String) rdfIndexProcessor.visit(index);			
			Assert.assertEquals(2581 , sparqlQuery.length());
 			//PrettyPrinter.prettyPrint(SPARQLQueriesHelper.observationsAsRDF(indexObservations));
 		
 			
 		}
 	}
 
 }
