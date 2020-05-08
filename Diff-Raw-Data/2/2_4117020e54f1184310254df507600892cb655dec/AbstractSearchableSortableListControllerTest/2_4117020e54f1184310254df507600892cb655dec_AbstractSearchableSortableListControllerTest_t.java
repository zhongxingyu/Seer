 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web.base;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.nullValue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyListOf;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
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
 import org.springframework.ui.Model;
 
 import com.google.common.collect.Lists;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AbstractSearchableSortableListControllerTest {
   @Mock
   private EntityManager entityManager;
 
   @Mock
   private TestFullTextSearchService<TestEntity> service;
 
   @InjectMocks
   private TestSearchController subject;
 
   private Model model;
 
   private TestEntity testEntity;
 
   private TestView testView;
 
   private List<TestEntity> testEntities;
 
   @Before
   public void onSetUp() {
     subject = new TestSearchController();
 
     testEntity = new TestEntity(1L);
     testView = new TestView(testEntity);
 
     model = new ModelStub();
     testEntities = Lists.newArrayList(testEntity);
 
     subject.setTestEntities(testEntities);
 
     subject.setTestFullTextService(service);
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void testSearch() throws ParseException {
     FullTextSearchResult<TestEntity> searchResult = new FullTextSearchResult<>(10, testEntities);
 
     when(
         service.searchForInFilteredList(any(Class.class), anyString(), anyInt(), anyInt(),
             any(RichUserDetails.class), anyListOf(Long.class))).thenReturn(searchResult);
 
     subject.search(0, "id", "ASC", "test", model);
 
     assertThat((String) WebUtils.getAttributeFromModel(WebUtils.PARAM_SEARCH, model), is("test"));
     assertThat((List<TestView>) WebUtils.getAttributeFromModel(WebUtils.DATA_LIST, model), contains(testView));
     assertThat((Integer) WebUtils.getAttributeFromModel(WebUtils.MAX_PAGES_KEY, model), is(1));
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void testSearchWithQuotes() throws ParseException {
     FullTextSearchResult<TestEntity> searchResult = new FullTextSearchResult<>(10, testEntities);
 
     when(
         service.searchForInFilteredList(any(Class.class), anyString(), anyInt(), anyInt(),
             any(RichUserDetails.class), anyListOf(Long.class))).thenReturn(searchResult);
 
     subject.search(0, "id", "ASC", "\"test\"", model);
 
     assertThat((String) WebUtils.getAttributeFromModel(WebUtils.PARAM_SEARCH, model), is("\"test\""));
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
    assertThat(subject.mapLabelToTechnicalName("blaphysicalPort:test"), is("blaphysicalPort.nbiPort.nmsPortId:test"));
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
