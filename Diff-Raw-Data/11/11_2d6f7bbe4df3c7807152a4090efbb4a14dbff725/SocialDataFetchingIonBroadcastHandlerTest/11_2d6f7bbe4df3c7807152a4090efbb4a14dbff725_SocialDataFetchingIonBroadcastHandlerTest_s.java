 package org.atlasapi.remotesite.bbc.ion;
 
 import static org.atlasapi.persistence.content.ResolvedContent.builder;
 import static org.hamcrest.Matchers.hasItem;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.TopicRef;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.KeyPhrase;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.RelatedLink;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.remotesite.SiteSpecificAdapter;
 import org.atlasapi.remotesite.bbc.BbcFeeds;
 import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeMatcher;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 
 @RunWith(JMock.class)
 public class SocialDataFetchingIonBroadcastHandlerTest extends TestCase {
 
     private static final String NO_SERIES = null;
     private static final String NO_BRAND = null;
 
     private final Mockery context = new Mockery();
     private final AdapterLog log = new NullAdapterLog();
     
     @SuppressWarnings("unchecked")
     private final SiteSpecificAdapter<List<RelatedLink>> linkAdapter = context.mock(SiteSpecificAdapter.class, "link adapter");
     @SuppressWarnings("unchecked")
    private final SiteSpecificAdapter<List<KeyPhrase>> tagAdapter = context.mock(SiteSpecificAdapter.class, "tag adapter");
    @SuppressWarnings("unchecked")
     private final SiteSpecificAdapter<List<TopicRef>> topicsAdapter = context.mock(SiteSpecificAdapter.class, "topic adapter");
     
     private final ContentResolver resolver = context.mock(ContentResolver.class);
     private final ContentWriter writer = context.mock(ContentWriter.class);
     
    private final BbcExtendedDataContentAdapter extendedDataAdapter = new BbcExtendedDataContentAdapter(linkAdapter, tagAdapter, topicsAdapter);
     
     private final SocialDataFetchingIonBroadcastHandler handler = new SocialDataFetchingIonBroadcastHandler(extendedDataAdapter, resolver, writer, log);
 
     @Test
     public void testSetsLinksAndTagsForTopLevelItem() {
         
         final String pid = "b00pgl7s";
         IonBroadcast broadcast = broadcast(pid, NO_SERIES, NO_BRAND);
         
         checkingUpdateLinksAndTagsForItem(
                 new Item(BbcFeeds.slashProgrammesUriForPid(pid),"curie",Publisher.BBC),
                 ImmutableList.of(RelatedLink.unknownTypeLink("link url").build()),
                 ImmutableList.of(new KeyPhrase("phrase", Publisher.BBC))
         );
         
         handler.handle(broadcast);
     }
     
 
     @Test
     public void testDoesntWriteContentWhenReferencedContentNotFound() {
 
         final String pid = "b00pgl7s";
         IonBroadcast broadcast = broadcast(pid, NO_SERIES, NO_BRAND);
         
         final String uri = BbcFeeds.slashProgrammesUriForPid(pid);
         context.checking(new Expectations(){{
             one(linkAdapter).fetch(uri);will(returnValue(ImmutableList.of(RelatedLink.unknownTypeLink("link url").build())));
            one(tagAdapter).fetch(uri);will(returnValue(ImmutableList.of(new KeyPhrase("phrase", Publisher.BBC))));
             one(topicsAdapter).fetch(uri);will(returnValue(ImmutableList.of()));
             one(resolver).findByCanonicalUris(with(hasItem(uri))); will(returnValue(builder().build()));
             never(writer).createOrUpdate(with(any(Item.class)));
         }});
 
         handler.handle(broadcast);
     }
 
     @Test
     public void testSetsLinksAndTagsForEpisodeWithSeriesAndBrand() {
         
         final String epPid = "b00pgl7s";
         final String seriesPid = "b00ncsyt";
         final String brandPid = "b006mkw3";
         IonBroadcast broadcast = broadcast(epPid, seriesPid, brandPid);
         
         checkingUpdateLinksAndTagsForItem(
                 new Item(BbcFeeds.slashProgrammesUriForPid(epPid),"curie",Publisher.BBC),
                 ImmutableList.of(RelatedLink.unknownTypeLink("link url").build()),
                 ImmutableList.of(new KeyPhrase("phrase", Publisher.BBC))
         );
         checkingUpdateLinksAndTagsForContainer(
                 new Series(BbcFeeds.slashProgrammesUriForPid(seriesPid),"curie",Publisher.BBC),
                 ImmutableList.of(RelatedLink.unknownTypeLink("link url").build()),
                 ImmutableList.of(new KeyPhrase("phrase", Publisher.BBC))
         );
         checkingUpdateLinksAndTagsForContainer(
                 new Brand(BbcFeeds.slashProgrammesUriForPid(brandPid),"curie",Publisher.BBC),
                 ImmutableList.of(RelatedLink.unknownTypeLink("link url").build()),
                 ImmutableList.of(new KeyPhrase("phrase", Publisher.BBC))
         );
         
         handler.handle(broadcast);
     }
 
     public void checkingUpdateLinksAndTagsForContainer(Container content, final List<RelatedLink> links, final List<KeyPhrase> tags) {
         checkFetchesLinksTagsAndContent(content, links, tags);
         context.checking(new Expectations(){{
             one(writer).createOrUpdate((Container)with(contentWithLinksAndTags(links, tags)));
         }});
     }
 
     public void checkingUpdateLinksAndTagsForItem(Item content, final List<RelatedLink> links, final List<KeyPhrase> tags) {
         checkFetchesLinksTagsAndContent(content, links, tags);
         context.checking(new Expectations(){{
             one(writer).createOrUpdate((Item)with(contentWithLinksAndTags(links, tags)));
         }});
     }
 
     public <T extends Content> void checkFetchesLinksTagsAndContent(final T content, final List<RelatedLink> links, final List<KeyPhrase> tags) {
         final String uri = content.getCanonicalUri();
         context.checking(new Expectations(){{
             one(linkAdapter).fetch(uri);will(returnValue(links));
            one(tagAdapter).fetch(uri);will(returnValue(tags));
             one(topicsAdapter).fetch(uri);will(returnValue(ImmutableList.of()));
             one(resolver).findByCanonicalUris(with(hasItem(uri))); will(returnValue(builder().put(uri, content).build()));
         }});
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testDoesntResolveOrUpdateContentWhenNoLinksOrTagsAreFound() {
         
         final String pid = "b00pgl7s";
         final String uri = BbcFeeds.slashProgrammesUriForPid(pid);
         IonBroadcast broadcast = broadcast(pid, NO_SERIES, NO_BRAND);
         
         context.checking(new Expectations(){{
             one(linkAdapter).fetch(uri);will(returnValue(ImmutableList.of()));
            one(tagAdapter).fetch(uri);will(returnValue(ImmutableList.of()));
             one(topicsAdapter).fetch(uri);will(returnValue(ImmutableList.of()));
             never(resolver).findByCanonicalUris(with(any(Iterable.class)));
             never(writer).createOrUpdate(with(any(Item.class)));
         }});
         
         handler.handle(broadcast);
     }
 
     public IonBroadcast broadcast(String epPid, String seriesPid, String brandPid) {
         IonBroadcast broadcast = new IonBroadcast();
         broadcast.setEpisodeId(epPid);
         broadcast.setSeriesId(seriesPid);
         broadcast.setBrandId(brandPid);
         return broadcast;
     }
 
     private Matcher<Content> contentWithLinksAndTags(final List<RelatedLink> links, final List<KeyPhrase> tags) {
         return new TypeSafeMatcher<Content>() {
 
             @Override
             public void describeTo(Description desc) {
                 desc.appendText("Content with links and tags").appendValue(links).appendValue(tags);
             }
 
             @Override
             public boolean matchesSafely(Content content) {
                 return content.getRelatedLinks().equals(ImmutableSet.copyOf(links)) && content.getKeyPhrases().equals(ImmutableSet.copyOf(tags));
             }
         };
     }
 }
