 package no.guttab.raven.search.query;
 
 
 import no.guttab.raven.annotations.FacetField;
 import no.guttab.raven.annotations.FacetSettings;
 import no.guttab.raven.annotations.IndexFieldName;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 
 @RunWith(MockitoJUnitRunner.class)
 public class FacetFieldQueryProcessorTest {
 
    private FacetFieldQueryProcessor facetQueryProcessor;
    @Mock
    private SolrQuery solrQuery;
 
    @Before
    public void setUp() throws Exception {
       facetQueryProcessor = new FacetFieldQueryProcessor();
    }
 
    @Test
    public void buildQuery_should_set_facet_to_true_when_a_field_is_defined_as_facetField() throws Exception {
       class TestQuery {
          @FacetField
          int areaId;
       }
       facetQueryProcessor.buildQuery(new TestQuery(), solrQuery);
 
       verify(solrQuery).setFacet(true);
 
    }
 
    @Test
    public void buildQuery_should_add_facetField_when_a_field_is_defined_as_facetField() throws Exception {
       class TestQuery {
          @FacetField
          int areaId;
       }
       facetQueryProcessor.buildQuery(new TestQuery(), solrQuery);
 
       verify(solrQuery).addFacetField("areaId");
    }
 
    @Test
    public void buildQuery_should_use_IndexFieldName_for_facetField_when_defined() throws Exception {
       class TestQuery {
          @IndexFieldName("newAreaId")
          @FacetField
          int areaId;
       }
       facetQueryProcessor.buildQuery(new TestQuery(), solrQuery);
 
       verify(solrQuery).setFacet(true);
       verify(solrQuery).addFacetField("newAreaId");
    }
 
    @Test
    public void buildQuery_should_not_add_minCount_for_the_facetField_if_default() throws Exception {
       class TestQuery {
          @FacetField
          String category;
       }
 
      facetQueryProcessor.buildQuery(new TestQuery(), solrQuery);
 
      verify(solrQuery, never()).set(eq("f.category.facet.mincount"), anyInt());
      verify(solrQuery, never()).setParam(eq("f.category.facet.mincount"), any(String.class));
    }
 
    @Test
    public void buildQuery_should_add_minCount_for_the_facetField_if_defined() throws Exception {
       class TestQuery {
          @FacetField(minCount = 1)
          String category;
       }
 
       facetQueryProcessor.buildQuery(new TestQuery(), solrQuery);
 
       verify(solrQuery).set("f.category.facet.mincount", 1);
    }
 
    @Test
   public void buildQuery_should_set_minCount_when_FacetSettings_indicates_it() throws Exception {
       @FacetSettings(minCount = 1)
       class TestQuery {
          @FacetField
          String category;
       }
 
       facetQueryProcessor.buildQuery(new TestQuery(), solrQuery);
 
       verify(solrQuery).setFacetMinCount(1);
    }
 
    @Test
   public void buildQuery_should_not_set_minCount_when_FacetSettings_has_default_minCount() throws Exception {
       @FacetSettings
       class TestQuery {
          @FacetField
          String category;
       }
 
      facetQueryProcessor.buildQuery(new TestQuery(), solrQuery);
 
      verify(solrQuery, never()).setFacetMinCount(anyInt());
    }
 
 }
