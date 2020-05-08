 package nl.surfnet.bod.web.base;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import nl.surfnet.bod.search.TestSearchController;
 import nl.surfnet.bod.support.ModelStub;
 import nl.surfnet.bod.util.FullTextSearchResult;
 import nl.surfnet.bod.util.TestEntity;
 import nl.surfnet.bod.util.TestFullTextSearchService;
 import nl.surfnet.bod.util.TestView;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.apache.lucene.queryParser.ParseException;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.data.domain.Sort;
 import org.springframework.ui.Model;
 
 import com.google.common.collect.Lists;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.nullValue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyListOf;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AbstractSearchableSortableListControllerTest {
   @Mock
   private EntityManager entityManager;
 
   @Mock
   private TestFullTextSearchService service;
 
   @InjectMocks
   private TestSearchController subject;
 
   private Model model;
 
   private TestEntity testEntity;
 
   private TestView testView;
 
   private List<TestEntity> testEntities;
 
   private List<TestView> testViews;
 
   @Before
   public void onSetUp() {
     subject = new TestSearchController();
 
     testEntity = new TestEntity(1);
     testView = new TestView(testEntity);
 
     model = new ModelStub();
     testEntities = Lists.newArrayList(testEntity);
     testViews = Lists.newArrayList(testView);
 
     subject.setTestEntities(testEntities);
 
     subject.setTestFullTextService(service);
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void testSearch() throws ParseException {
     FullTextSearchResult<TestView> searchResult = new FullTextSearchResult<>(subject.count(), testViews);
 
     when(
         service.searchForInFilteredList(any(Class.class), anyString(), anyInt(), anyInt(), any(Sort.class),
             any(RichUserDetails.class), anyListOf(TestView.class))).thenReturn(searchResult);
 
     subject.search(0, "id", "ASC", "test", model);
 
     assertThat((String) WebUtils.getAttributeFromModel(WebUtils.PARAM_SEARCH, model), is("test"));
     assertThat((List<TestEntity>) WebUtils.getAttributeFromModel(WebUtils.DATA_LIST, model), hasSize(1));
     assertThat((List<TestView>) WebUtils.getAttributeFromModel(WebUtils.DATA_LIST, model), contains(testView));
     assertThat((Integer) WebUtils.getAttributeFromModel(WebUtils.MAX_PAGES_KEY, model), is(1));
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void testSearchWithQuotes() throws ParseException {
     FullTextSearchResult<TestView> searchResult = new FullTextSearchResult<>(subject.count(), testViews);
 
     when(
         service.searchForInFilteredList(any(Class.class), anyString(), anyInt(), anyInt(), any(Sort.class),
             any(RichUserDetails.class), anyListOf(TestView.class))).thenReturn(searchResult);
 
     subject.search(0, "id", "ASC", "\"test\"", model);
 
     assertThat((String) WebUtils.getAttributeFromModel(WebUtils.PARAM_SEARCH, model), is("\"test\""));
     assertThat((List<TestEntity>) WebUtils.getAttributeFromModel(WebUtils.DATA_LIST, model), hasSize(1));
     assertThat((List<TestView>) WebUtils.getAttributeFromModel(WebUtils.DATA_LIST, model), contains(testView));
     assertThat((Integer) WebUtils.getAttributeFromModel(WebUtils.MAX_PAGES_KEY, model), is(1));
   }
 
   @Test
   public void testTranslateSearchString() {
     assertThat(subject.mapLabelToTechnicalName("test"), is("test"));
     assertThat(subject.mapLabelToTechnicalName("\"test\""), is("\"test\""));
     assertThat(subject.mapLabelToTechnicalName("&quot;test&quot;"), is("&quot;test&quot;"));
   }
 
   @Test
   public void shouldMapTeamToVirtualResourceGroupName() {
     assertThat(subject.mapLabelToTechnicalName("blateam:bla"), is("blavirtualResourceGroup.name:bla"));
     assertThat(subject.mapLabelToTechnicalName("institute:test"), is("physicalResourceGroup.institute.name:test"));
    assertThat(subject.mapLabelToTechnicalName("blaphysicalPort:test"), is("blaphysicalPort.id:test"));
     assertThat(subject.mapLabelToTechnicalName("team:\"some-team\""), is("virtualResourceGroup.name:\"some-team\""));
   }
 
   @Test
   public void shouldNotMapWithoutColon() {
     assertThat(subject.mapLabelToTechnicalName("institutetest"), is("institutetest"));
   }
 
   @Test
   public void shouldNotMapUnknownLabel() {
     assertThat(subject.mapLabelToTechnicalName("person:"), is("person:"));
   }
 
   @Test
   public void shouldNotMapNullAndEmpty() {
     assertThat(subject.mapLabelToTechnicalName(null), nullValue());
     assertThat(subject.mapLabelToTechnicalName(""), is(""));
   }
 
 }
