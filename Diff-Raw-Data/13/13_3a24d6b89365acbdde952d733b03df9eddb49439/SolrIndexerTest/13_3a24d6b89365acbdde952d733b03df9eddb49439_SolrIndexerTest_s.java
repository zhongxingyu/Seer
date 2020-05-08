 package jp.projects.miya.hadoop_solr_indexer;
 
 import org.junit.Test;
 
 /**
  * Unit test for simple App.
  */
 public class SolrIndexerTest {
 	@Test
 	public void testMain() throws Exception {
 		SolrIndexer.main(new String[] {
 				"-D", Constants.CSV_HEADER_PROPERTY + "=id,src_ja",
 				"-D", Constants.SOLR_URL + "=http://10.211.55.16:8983/solr",
 				"-D", Constants.SEND_BUFFER_SIZE + "=1000",
 				"-D", Constants.REQUEST_THREAD_COUNT + "=3",
				"input",
				"output"} );
 	}
 }
