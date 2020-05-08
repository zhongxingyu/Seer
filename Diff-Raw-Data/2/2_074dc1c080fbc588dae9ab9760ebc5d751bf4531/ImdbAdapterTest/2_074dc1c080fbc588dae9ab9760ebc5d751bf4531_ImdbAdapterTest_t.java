 /* Copyright 2009 British Broadcasting Corporation
    Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.uriplay.remotesite.imdb;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 
 import java.io.IOException;
 import java.util.Set;
 
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.uriplay.media.entity.Content;
 import org.uriplay.media.entity.Description;
 import org.uriplay.persistence.system.Fetcher;
 import org.uriplay.remotesite.FetchException;
 import org.uriplay.remotesite.sparql.SparqlEndpoint;
 import org.uriplay.remotesite.sparql.SparqlQuery;
 
 import com.google.common.collect.Sets;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
 import com.hp.hpl.jena.sparql.core.ResultBinding;
 import com.hp.hpl.jena.sparql.core.Var;
 import com.hp.hpl.jena.sparql.engine.binding.Binding;
 
 /**
  * Unit test for {@link ImdbAdapter}.
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class ImdbAdapterTest extends MockObjectTestCase {
 
 	static final String imdbId = "1208127";
 	
 	static final SparqlQuery DBPEDIA_QUERY = 
 		SparqlQuery.select("dbpedia_resource").whereSubjectOf("dbpprop:imdbId", imdbId).withPrefix("dbpprop", "http://dbpedia.org/property/");
 		
 	static final String IMDB_LINK = "http://www.imdb.com/title/tt1208127/";
 	static final String DBPEDIA_LINK = "http://dbpedia.org/resource/The_Sopranos";
 	static final String WIKIPEDIA_LINK = "http://en.wikipedia.org/wiki/The_Sopranos";
 	static final Resource resource = new ResourceImpl(DBPEDIA_LINK);
 
	final Description WIKIPEDIA_REPRESENTATION = new Content();
 	
 	SparqlEndpoint sparqlEndpoint;
 	Fetcher<Content> fetcher;
 	ImdbAdapter adapter;
 	ResultSet resultSet = mock(ResultSet.class);
 	ImdbSource imdbSource = new ImdbSource(resultSet, IMDB_LINK);
 	Binding binding = mock(Binding.class);
 	Model model = mock(Model.class);
 	ResultBinding resultBinding = new ResultBinding(model, binding);
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		sparqlEndpoint = mock(SparqlEndpoint.class);
 		fetcher = mock(Fetcher.class);
 		adapter = new ImdbAdapter(sparqlEndpoint, fetcher);
 	}
 	
 	public void testQueriesSparqlEndpointForCorrespondingDbediaUriAndPassesToFetcher() throws Exception {
 		
 		forAnImdbIdReferencedByWikipedia();
 		
 		checking(new Expectations() {{ 
 			one(fetcher).fetch(DBPEDIA_LINK); will(returnValue(WIKIPEDIA_REPRESENTATION));
 		}});
 		
 		adapter.fetch(IMDB_LINK);
 	}
 	
 	public void testWrapsExceptionIfHttpClientThrowsException() throws Exception {
 		
 		checking(new Expectations() {{
 			allowing(sparqlEndpoint).execute(DBPEDIA_QUERY); will(throwException(new IOException()));
 		}});
 		
 		try {
 			adapter.fetch(IMDB_LINK);
 			fail("Should have thrown FetchException.");
 		} catch (Exception e) {
 			assertThat(e, instanceOf(FetchException.class));
 		}
 	}
 	
 	public void testAddsSameAsLinkWithImdbUri() throws Exception {
 		
 		forAnImdbIdReferencedByWikipedia();
 		
 		checking(new Expectations() {{ 
 			one(fetcher).fetch(DBPEDIA_LINK); will(returnValue(WIKIPEDIA_REPRESENTATION));
 		}});
 		
 		Description description = adapter.fetch(IMDB_LINK);
 		assertThat(description.getAliases(), is((Set<String>) Sets.newHashSet(WIKIPEDIA_LINK, DBPEDIA_LINK)));
 	}
 	
 	
 	public void testCanFetchResourcesForImdbLinks() throws Exception {
 		assertTrue(adapter.canFetch(IMDB_LINK));
 		assertFalse(adapter.canFetch("http://www.bbc.co.uk"));
 	}
 	
 	private void forAnImdbIdReferencedByWikipedia() {
 		checking(new Expectations() {{
 			one(sparqlEndpoint).execute(DBPEDIA_QUERY); will(returnValue(resultSet));
 			one(resultSet).hasNext(); will(returnValue(true));
 			one(resultSet).next(); will(returnValue(resultBinding));
 			allowing(binding).get(Var.alloc(DBPEDIA_QUERY.getSelectId())); will(returnValue(resource.asNode()));
 			allowing(model).asRDFNode(resource.asNode()); will(returnValue(resource));
 		}});
 	}
 	
 }
