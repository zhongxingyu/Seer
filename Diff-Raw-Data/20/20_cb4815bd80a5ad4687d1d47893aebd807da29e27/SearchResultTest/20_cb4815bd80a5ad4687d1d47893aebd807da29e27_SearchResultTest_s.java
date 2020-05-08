 package com.threewks.thundr.search.google;
 
 import com.google.appengine.api.search.OperationResult;
 import com.google.appengine.api.search.Results;
 import com.google.appengine.api.search.ScoredDocument;
 import com.google.appengine.api.search.StatusCode;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Future;
 
 import static com.google.appengine.api.search.ScoredDocument.newBuilder;
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class SearchResultTest {
 
 	private SearchResult<String> searchResult;
 	private Future searchAsync = mock(Future.class);
 
 	@Before
 	public void setUp() throws Exception {
 		when(searchAsync.get()).thenReturn(mockResults());
		searchResult = new SearchResult<>(String.class, searchAsync, 1);
 	}
 
 	private MockResults<ScoredDocument> mockResults() {
 		ScoredDocument.Builder doc1Builder = newBuilder();
 		doc1Builder.setId("docId1");
 
 		ScoredDocument.Builder doc2Builder = newBuilder();
 		doc2Builder.setId("docId2");
 
 		List<ScoredDocument> docs = asList(doc1Builder.build(), doc2Builder.build());
 
 		return new MockResults<>(docs);
 	}
 
 	@Test
	public void shouldGetSearchResultIds() {
 		List<String> expectedResults = asList("docId1", "docId2");
 		assertThat(this.searchResult.getSearchResultIds(), is(expectedResults));
 	}
 
 	@Test
	public void shouldGetMatchingRecords() {
 		assertThat(this.searchResult.getMatchingRecordCount(), is(2L));
 	}
 
 	@Test
	public void shouldReturnedRecords() {
 		assertThat(this.searchResult.getReturnedRecordCount(), is(1L));
 	}
 
 	private class MockResults<T> extends Results<T> {
 		private List<T> results;
 
 		public MockResults(List<T> results) {
 			super(new OperationResult(StatusCode.OK, ""), results, 0, 0, null);
 			this.results = results;
 		}
 
 		@Override
 		public Collection<T> getResults() {
 			return results;
 		}
 
 		@Override
 		public long getNumberFound() {
 			return results.size();
 		}
 	}
 }
