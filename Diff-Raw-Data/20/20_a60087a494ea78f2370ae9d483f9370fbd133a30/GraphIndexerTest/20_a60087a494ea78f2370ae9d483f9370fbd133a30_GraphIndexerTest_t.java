 package org.triple_brain.module.search;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.codehaus.jettison.json.JSONArray;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.triple_brain.module.model.graph.Vertex;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.triple_brain.module.common_utils.Uris.encodeURL;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 
 public class GraphIndexerTest extends SearchRelatedTest {
 
     @Test
     public void can_index_vertex()throws Exception{
         SolrDocumentList documentList = queryVertex(vertexA);
         assertThat(documentList.size(), is(0));
         graphIndexer.indexVertex(vertexA);
         documentList = queryVertex(vertexA);
         assertThat(documentList.size(), is(1));
         assertThat(
                 labelOfGraphElementSearchResult(documentList.get(0)),
                 is("vertex Azure")
         );
     }
 
     @Test
     public void can_remove_graph_element_from_index(){
         indexGraph();
         GraphSearch graphSearch = GraphSearch.withCoreContainer(coreContainer);
         JSONArray results = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                 "vertex azure",
                 user
         );
         assertThat(results.length(), is(1));
        graphIndexer.deleteGraphElement(vertexA);
         results = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                 "vertex azure",
                 user
         );
         assertThat(results.length(), is(0));
     }
 
     @Test
     public void edges_get_indexed_when_indexing_whole_graph(){
         graphIndexer.indexWholeGraph();
         assertTrue(true);
     }
 
     @Test
     @Ignore(
             "todo when lucene4 be integrated in noe4j " +
                     "for now it conflicts with solr " +
                     "when I upgrade solr to version 4"
     )
     public void indexing_graph_element_doesnt_erase_vertex_specific_fields(){
         indexGraph();
         GraphSearch graphSearch = GraphSearch.withCoreContainer(coreContainer);
         JSONArray vertexASearchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                 "vertex Azure",
                 user
         );
         assertThat(
                 vertexASearchResults.length(), is(1)
         );
         //todo uncomment when lucene4 be integrated in noe4j for now it conflicts with solr when I upgrade solr to version 4
 //        graphIndexer().updateGraphElementIndex(vertexA, user);
         vertexASearchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                 "vertex Azure",
                 user
         );
         assertThat(
                 vertexASearchResults.length(), is(1)
         );
     }
 
     private String labelOfGraphElementSearchResult(SolrDocument solrDocument){
         return (String) solrDocument.getFieldValue("label");
     }
 
     private SolrDocumentList queryVertex(Vertex vertex)throws Exception{
         return resultsOfSearchQuery(
                 new SolrQuery().setQuery(
                         "uri:" + encodeURL(vertex.uri().toString())
                 )
         );
     }
 }
