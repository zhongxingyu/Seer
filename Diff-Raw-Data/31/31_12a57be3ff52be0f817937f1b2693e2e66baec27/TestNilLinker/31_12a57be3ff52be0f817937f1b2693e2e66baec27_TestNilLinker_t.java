 package uk.ac.susx.mlcl.erl.tac;
 
 import com.beust.jcommander.internal.Lists;
 import com.beust.jcommander.internal.Sets;
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Resources;
 import nu.xom.ParsingException;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import uk.ac.susx.mlcl.erl.linker.Linker;
 import uk.ac.susx.mlcl.erl.tac.io.LinkIO;
 import uk.ac.susx.mlcl.erl.tac.io.QueryIO;
 import uk.ac.susx.mlcl.erl.tac.io.Tac2012LinkIO;
 import uk.ac.susx.mlcl.erl.tac.kb.EntityType;
 import uk.ac.susx.mlcl.erl.tac.queries.Link;
 import uk.ac.susx.mlcl.erl.tac.queries.Query;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.hamcrest.collection.IsIn.isIn;
 import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
 import static org.junit.Assert.assertThat;
 import static uk.ac.susx.mlcl.erl.test.IsEmptyIterator.emptyIteratorOf;
 
 
 @RunWith(Parameterized.class)
 public class TestNilLinker {
 
     private final URL srcQueries;
 
     public TestNilLinker(String resourceName) {
         srcQueries = Resources.getResource(QueryIO.class, resourceName);
     }
 
     @Parameterized.Parameters(name = "{index}: {0}")
     public static Collection<Object[]> data() {
         final ImmutableList.Builder<Object[]> data = ImmutableList.builder();
         data.add(new Object[]{"tac_2009_kbp_entity_linking_queries.xml"});
         data.add(new Object[]{"tac_2010_kbp_evaluation_entity_linking_queries.xml"});
         data.add(new Object[]{"tac_2010_kbp_training_entity_linking_queries.xml"});
         data.add(new Object[]{"tac_2011_kbp_english_evaluation_entity_linking_queries.xml"});
         data.add(new Object[]{"tac_2012_kbp_english_evaluation_entity_linking_queries.xml"});
         return data.build();
     }
 
     @Test
     public void simpleTest() throws IOException, ParsingException {
         final File dstLinks = File.createTempFile(this.getClass().getName(), "tmp");
 
         final List<Query> queries;
         {
            Linker<Query,Link> instance = new NilLinker();
 
             final QueryIO qio = QueryIO.detectFormat(srcQueries);
             final LinkIO lio = new Tac2012LinkIO();
 
             List<Link> links = Lists.newArrayList();
             queries = qio.readAll(srcQueries);
             for (Query query : queries) {
                 links.add(instance.link(query));
             }
             lio.writeAll(dstLinks, links);
         }
 
         {
             final List<Link> links = LinkIO.detectFormat(dstLinks).readAll(dstLinks);
             final Set<Integer> uniqueNodeIds = Sets.newHashSet();
             int minNilId = Integer.MAX_VALUE;
             int maxNilId = Integer.MIN_VALUE;
 
             Iterator<Link> linksIt = links.iterator();
             Iterator<Query> queriesIt = queries.iterator();
             while (queriesIt.hasNext() && linksIt.hasNext()) {
                 final Link link = linksIt.next();
                 final Query query = queriesIt.next();
 
                 assertThat("link", link, is(not(nullValue())));
                 assertThat("query", query, is(not(nullValue())));
                 assertThat(link.getQueryId(), is(equalTo(query.getId())));
 
                 final String entityNodeId = link.getEntityNodeId();
                 assertThat("entityNodeId", entityNodeId, is(not(nullValue())));
                 assertThat("entityNodeId", entityNodeId, startsWith("NIL"));
                 final int nilId = Integer.parseInt(link.getEntityNodeId().substring(3));
 
                 assertThat("nilId", nilId, is(greaterThanOrEqualTo(0)));
                 assertThat("nilId", nilId, not(isIn(uniqueNodeIds)));
 
                 uniqueNodeIds.add(nilId);
                 minNilId = Math.min(minNilId, nilId);
                 maxNilId = Math.max(maxNilId, nilId);
 
                 assertThat("entity type", link.getEntityType(), is(equalTo(EntityType.UKN)));
 
                 assertThat("web search", link.isWebSearch(), is(equalTo(false)));
 
                 if (query.getDocId().matches("^(APW|AFP|NYT|XIN|CNA|LTW|WPB)_.*")) {
                     assertThat("source genre for " + query.getDocId(), link.getSourceGenre(), is(equalTo(Genre.NW)));
                 } else if (query.getDocId().matches("^eng-(NG|WL)-.*") || query.getDocId().matches("^\\S+\\.com_.*")) {
                     assertThat("source genre for " + query.getDocId(), link.getSourceGenre(), is(equalTo(Genre.WB)));
                 } else if (query.getDocId().matches("^(bolt).*")) {
                     assertThat("source genre for " + query.getDocId(), link.getSourceGenre(), is(equalTo(Genre.NG)));
                 } else {
                     throw new AssertionError("Unknown genre for document id: " + query.getDocId());
                 }
             }
 
             assertThat("links iterator", linksIt, is(emptyIteratorOf(Link.class)));
             assertThat("queries iterator", queriesIt, is(emptyIteratorOf(Query.class)));
 
             assertThat("minNilId", minNilId, is(equalTo(1)));
             assertThat("maxNilId", maxNilId, is(equalTo(uniqueNodeIds.size())));
         }
     }
 }
