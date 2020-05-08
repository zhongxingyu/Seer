 package org.atlasapi.persistence.lookup;
 
 import static org.atlasapi.persistence.lookup.TransitiveLookupWriter.generatedTransitiveLookupWriter;
 import static org.atlasapi.persistence.lookup.entry.LookupEntry.lookupEntryFrom;
 import static org.hamcrest.Matchers.hasItems;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Nullable;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.equiv.ContentRef;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.LookupRef;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentCategory;
 import org.atlasapi.persistence.lookup.entry.LookupEntry;
 import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ContiguousSet;
 import com.google.common.collect.DiscreteDomain;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Range;
 
 public class TransitiveLookupWriterTest extends TestCase {
 
     private final LookupEntryStore store = new InMemoryLookupEntryStore();
     private final TransitiveLookupWriter writer = generatedTransitiveLookupWriter(store);
 
     // Tests that trivial lookups are written reflexively for all content
     // identifiers
     public void testWriteNewLookup() {
 
         Item item = createItem("test", Publisher.BBC);
         
         store.store(LookupEntry.lookupEntryFrom(item));
 
         writeLookup(writer, item, ImmutableSet.<Content> of(), ImmutableSet.of(Publisher.BBC));
 
         LookupEntry uriEntry = Iterables.getOnlyElement(store.entriesForCanonicalUris(ImmutableList.of("testUri")));
         assertEquals(item.getCanonicalUri(), uriEntry.uri());
         assertEquals(item.getAllUris(), uriEntry.aliasUrls());
         assertEquals("testUri", Iterables.getOnlyElement(uriEntry.directEquivalents()).uri());
 
         assertNotNull(uriEntry.created());
         assertNotNull(uriEntry.updated());
 
         assertEquals(item.getCanonicalUri(), Iterables.getOnlyElement(uriEntry.equivalents()).uri());
         assertEquals(item.getPublisher(), Iterables.getOnlyElement(uriEntry.equivalents()).publisher());
         assertEquals(ContentCategory.TOP_LEVEL_ITEM, Iterables.getOnlyElement(uriEntry.equivalents()).category());
 
         LookupEntry aliasEntry = Iterables.getOnlyElement(store.entriesForIdentifiers(ImmutableList.of("testAlias"), true));
         assertEquals(aliasEntry, uriEntry);
 
     }
 
     private Item createItem(String itemName, Publisher publisher) {
         Item item = new Item(itemName + "Uri", itemName + "Curie", Publisher.BBC);
         item.addAliasUrl(itemName + "Alias");
         item.setPublisher(publisher);
         return item;
     }
 
     public void testWriteLookup() {
 
         Item paItem = createItem("test1", Publisher.PA);
         Item bbcItem = createItem("test2", Publisher.BBC);
         Item c4Item = createItem("test3", Publisher.C4);
 
         Set<Publisher> publishers = ImmutableSet.of(Publisher.PA, Publisher.BBC, Publisher.C4, Publisher.ITUNES);
         // Inserts reflexive entries for items PA, BBC, C4
         store.store(LookupEntry.lookupEntryFrom(paItem));
         store.store(LookupEntry.lookupEntryFrom(bbcItem));
         store.store(LookupEntry.lookupEntryFrom(c4Item));
 
         // Make items BBC and C4 equivalent.
         writeLookup(writer, bbcItem, ImmutableSet.<Content> of(c4Item), publishers);
 
         hasEquivs(paItem, paItem);
         hasDirectEquivs(paItem, paItem);
 
         hasEquivs(bbcItem, bbcItem, c4Item);
         hasDirectEquivs(bbcItem, bbcItem, c4Item);
 
         hasEquivs(c4Item, bbcItem, c4Item);
         hasDirectEquivs(c4Item, bbcItem, c4Item);
 
         // Make items PA and BBC equivalent, so all three are transitively
         // equivalent
         writeLookup(writer, paItem, ImmutableSet.<Content> of(bbcItem), publishers);
 
         hasEquivs(paItem, bbcItem, c4Item, paItem);
         hasDirectEquivs(paItem, paItem, bbcItem);
 
         hasEquivs(bbcItem, bbcItem, c4Item, paItem);
         hasDirectEquivs(bbcItem, bbcItem, c4Item, paItem);
 
         hasEquivs(c4Item, bbcItem, c4Item, paItem);
         hasDirectEquivs(c4Item, bbcItem, c4Item);
 
         // Make item PA equivalent to nothing. Item PA just reflexive, item BBC and
         // C4 still equivalent.
         writeLookup(writer, paItem, ImmutableSet.<Content> of(), publishers);
 
         hasEquivs(paItem, paItem);
         hasDirectEquivs(paItem, paItem);
 
         hasEquivs(bbcItem, bbcItem, c4Item);
         hasDirectEquivs(bbcItem, bbcItem, c4Item);
 
         hasEquivs(c4Item, bbcItem, c4Item);
         hasDirectEquivs(c4Item, bbcItem, c4Item);
 
         // Make PA and BBC equivalent again.
         writeLookup(writer, paItem, ImmutableSet.<Content> of(bbcItem), publishers);
 
         hasEquivs(paItem, bbcItem, c4Item, paItem);
         hasDirectEquivs(paItem, paItem, bbcItem);
 
         hasEquivs(bbcItem, bbcItem, c4Item, paItem);
         hasDirectEquivs(bbcItem, bbcItem, c4Item, paItem);
 
         hasEquivs(c4Item, bbcItem, c4Item, paItem);
         hasDirectEquivs(c4Item, bbcItem, c4Item);
 
         // Add a new item from Itunes.
         Item itunesItem = createItem("test4", Publisher.ITUNES);
         store.store(LookupEntry.lookupEntryFrom(itunesItem));
 
         // Make PA equivalent to just Itunes, instead of BBC. PA and Itunes equivalent, BBC and
         // C4 equivalent.
         writeLookup(writer, paItem, ImmutableSet.<Content> of(itunesItem), publishers);
 
         hasEquivs(paItem, paItem, itunesItem);
         hasDirectEquivs(paItem, paItem, itunesItem);
 
         hasEquivs(bbcItem, bbcItem, c4Item);
         hasDirectEquivs(bbcItem, bbcItem, c4Item);
 
         hasEquivs(c4Item, bbcItem, c4Item);
         hasDirectEquivs(c4Item, bbcItem, c4Item);
 
         hasEquivs(itunesItem, paItem, itunesItem);
         hasDirectEquivs(itunesItem, paItem, itunesItem);
 
         // Make all items equivalent.
         writeLookup(writer, paItem, ImmutableSet.<Content> of(c4Item, itunesItem), publishers);
 
         hasEquivs(paItem, paItem, bbcItem, c4Item, itunesItem);
         hasDirectEquivs(paItem, paItem, c4Item, itunesItem);
 
         hasEquivs(bbcItem, paItem, bbcItem, c4Item, itunesItem);
         hasDirectEquivs(bbcItem, bbcItem, c4Item);
 
         hasEquivs(c4Item, paItem, bbcItem, c4Item, itunesItem);
         hasDirectEquivs(c4Item, paItem, bbcItem, c4Item);
 
         hasEquivs(itunesItem, paItem, bbcItem, c4Item, itunesItem);
         hasDirectEquivs(itunesItem, paItem, itunesItem);
 
     }
 
     protected void writeLookup(TransitiveLookupWriter writer, Content subject, ImmutableSet<? extends Content> equivs, Set<Publisher> publishers) {
         writer.writeLookup(ContentRef.valueOf(subject), Iterables.transform(equivs, new Function<Content, ContentRef>() {
             @Override
             public ContentRef apply(@Nullable Content input) {
                 return ContentRef.valueOf(input);
             }
         }), publishers);
     }
 
     private void hasEquivs(Content id, Content... transitiveEquivs) {
         LookupEntry entry = Iterables.getOnlyElement(store.entriesForCanonicalUris(ImmutableList.of(id.getCanonicalUri())));
         assertEquals(ImmutableSet.copyOf(Iterables.transform(ImmutableSet.copyOf(transitiveEquivs),Identified.TO_URI)), ImmutableSet.copyOf(Iterables.transform(entry.equivalents(), LookupRef.TO_URI)));
     }
 
     private void hasDirectEquivs(Content id, Content... directEquivs) {
         LookupEntry entry = Iterables.getOnlyElement(store.entriesForCanonicalUris(ImmutableList.of(id.getCanonicalUri())));
         assertEquals(ImmutableSet.copyOf(Iterables.transform(ImmutableSet.copyOf(directEquivs),Identified.TO_URI)), ImmutableSet.copyOf(Iterables.transform(entry.directEquivalents(), LookupRef.TO_URI)));
     }
 
     public void testBreakingEquivs() {
         
         Brand pivot = new Brand("pivot", "cpivot", Publisher.PA);
         Brand left = new Brand("left", "cleft", Publisher.PA);
         Brand right = new Brand("right", "cright", Publisher.PA);
         
         store.store(LookupEntry.lookupEntryFrom(pivot));
         store.store(LookupEntry.lookupEntryFrom(left));
         store.store(LookupEntry.lookupEntryFrom(right));
 
         Set<Publisher> publishers = ImmutableSet.of(Publisher.PA);
         writeLookup(writer, pivot, ImmutableSet.of(left,right), publishers);
         writeLookup(writer, left, ImmutableSet.of(right), publishers);
         
         writeLookup(writer, pivot, ImmutableSet.of(left), publishers);
         writeLookup(writer, left, ImmutableSet.<Content>of(), publishers);
         
         hasEquivs(pivot, pivot);
         
     }
     
     public void testDoesntWriteEquivalentsForContentOfIgnoredPublishers() {
         
         Item paItem = createItem("paItem",Publisher.PA);
         Item c4Item = createItem("c4Item",Publisher.C4);
         
         store.store(LookupEntry.lookupEntryFrom(paItem));
         store.store(LookupEntry.lookupEntryFrom(c4Item));
         
         writeLookup(writer, paItem, ImmutableSet.<Content> of(), ImmutableSet.of(Publisher.PA));
         writeLookup(writer, c4Item, ImmutableSet.<Content> of(), ImmutableSet.of(Publisher.C4));
 
         hasEquivs(paItem, paItem);
         hasEquivs(c4Item, c4Item);
         
         writeLookup(writer, paItem, ImmutableSet.of(c4Item), ImmutableSet.of(Publisher.PA, Publisher.BBC));
         
         hasEquivs(paItem, paItem);
         hasEquivs(c4Item, c4Item);
         hasEquivs(c4Item, c4Item);
         
     }
     
     public void testDoesntBreakEquivalenceForContentOfIgnoredPublishers() {
         
         Item paItem = createItem("paItem1",Publisher.PA);
         Item c4Item = createItem("c4Item1",Publisher.C4);
         Item bbcItem = createItem("bbcItem1",Publisher.BBC);
         Item fiveItem = createItem("fiveItem1", Publisher.FIVE);
         
         store.store(LookupEntry.lookupEntryFrom(paItem));
         store.store(LookupEntry.lookupEntryFrom(c4Item));
         store.store(LookupEntry.lookupEntryFrom(bbcItem));
         store.store(LookupEntry.lookupEntryFrom(fiveItem));
         
         writeLookup(writer, paItem, ImmutableSet.<Content> of(), ImmutableSet.of(Publisher.PA));
         writeLookup(writer, c4Item, ImmutableSet.<Content> of(), ImmutableSet.of(Publisher.C4));
         writeLookup(writer, bbcItem, ImmutableSet.<Content> of(), ImmutableSet.of(Publisher.BBC));
         writeLookup(writer, fiveItem, ImmutableSet.<Content> of(), ImmutableSet.of(Publisher.FIVE));
         
         //Make PA and BBC equivalent
         writeLookup(writer, paItem, ImmutableSet.of(bbcItem), ImmutableSet.of(Publisher.PA, Publisher.BBC));
 
         hasEquivs(paItem, paItem, bbcItem);
         hasDirectEquivs(paItem, paItem, bbcItem);
         
         hasEquivs(bbcItem, bbcItem, paItem);
         hasDirectEquivs(bbcItem, bbcItem, paItem);
         
         hasEquivs(c4Item, c4Item);
         hasDirectEquivs(c4Item, c4Item);
         
         hasEquivs(fiveItem, fiveItem);
         hasDirectEquivs(fiveItem, fiveItem);
 
         //Make PA and C4 equivalent, ignoring BBC content. PA, BBC, C4 all equivalent.
         writeLookup(writer, paItem, ImmutableSet.of(c4Item), ImmutableSet.of(Publisher.PA, Publisher.C4));
 
         hasEquivs(paItem, paItem, bbcItem, c4Item);
         hasDirectEquivs(paItem, paItem, bbcItem, c4Item);
         
         hasEquivs(bbcItem, bbcItem, paItem, c4Item);
         hasDirectEquivs(bbcItem, paItem, bbcItem);
         
         hasEquivs(c4Item, c4Item, paItem, bbcItem);
         hasDirectEquivs(c4Item, paItem, c4Item);
         
         hasEquivs(fiveItem, fiveItem);
         hasDirectEquivs(fiveItem, fiveItem);
         
         //Make PA and 5 equivalent, including C4 content. PA, BBC, 5 all equivalent. 
         writeLookup(writer, paItem, ImmutableSet.of(fiveItem), ImmutableSet.of(Publisher.PA, Publisher.C4, Publisher.FIVE));
         
         hasEquivs(paItem, paItem, bbcItem, fiveItem);
         hasDirectEquivs(paItem, paItem, bbcItem, fiveItem);
         
         hasEquivs(bbcItem, bbcItem, paItem, fiveItem);
         hasDirectEquivs(bbcItem, paItem, bbcItem);
         
         hasEquivs(c4Item, c4Item);
         hasDirectEquivs(c4Item, c4Item);
         
         hasEquivs(fiveItem, fiveItem, bbcItem, paItem);
         hasDirectEquivs(fiveItem, fiveItem, paItem);
         
     }
     
     public void testDoesntBreakEquivalenceForContentOfIgnoredPublishersWhenLinkingItemIsNotSubject() {
         
         Item paItem = createItem("paItem2",Publisher.PA);
         Item pnItem = createItem("pnItem2",Publisher.PREVIEW_NETWORKS);
         Item bbcItem = createItem("bbcItem2",Publisher.BBC);
         
         store.store(LookupEntry.lookupEntryFrom(paItem));
         store.store(LookupEntry.lookupEntryFrom(pnItem));
         store.store(LookupEntry.lookupEntryFrom(bbcItem));
     
         //Make PA and BBC equivalent
         writeLookup(writer, paItem, ImmutableSet.of(bbcItem), ImmutableSet.of(Publisher.PA, Publisher.BBC));
         
         writeLookup(writer, pnItem, ImmutableSet.of(paItem), ImmutableSet.of(Publisher.PREVIEW_NETWORKS, Publisher.PA));
         
         hasEquivs(paItem, paItem, bbcItem, pnItem);
         hasDirectEquivs(paItem, paItem, bbcItem, pnItem);
         
         hasEquivs(bbcItem, bbcItem, paItem, pnItem);
         hasDirectEquivs(bbcItem, paItem, bbcItem);
         
         hasEquivs(pnItem, pnItem, paItem, bbcItem);
         hasDirectEquivs(pnItem, paItem, pnItem);
         
     }
     
     public void testDoesntWriteEquivalencesWhenEquivalentsDontChange() {
         
         LookupEntryStore store = mock(LookupEntryStore.class);
         TransitiveLookupWriter writer = generatedTransitiveLookupWriter(store);
         
         Item paItem = createItem("paItem3",Publisher.PA);
         Item pnItem = createItem("pnItem3",Publisher.PREVIEW_NETWORKS);
         
         LookupEntry paLookupEntry = lookupEntryFrom(paItem).copyWithDirectEquivalents(ImmutableList.of(LookupRef.from(pnItem)));
         LookupEntry pnLookupEntry = lookupEntryFrom(pnItem).copyWithDirectEquivalents(ImmutableList.of(LookupRef.from(paItem)));
         
         when(store.entriesForCanonicalUris(ImmutableSet.of(pnItem.getCanonicalUri(), paItem.getCanonicalUri())))
             .thenReturn(ImmutableList.of(paLookupEntry, pnLookupEntry));
         when(store.entriesForCanonicalUris(ImmutableList.of(paItem.getCanonicalUri())))
             .thenReturn(ImmutableList.of(paLookupEntry));
         
         writer.writeLookup(ContentRef.valueOf(paItem), ImmutableSet.of(ContentRef.valueOf(pnItem)), ImmutableSet.of(Publisher.PA, Publisher.PREVIEW_NETWORKS));
         
         verify(store).entriesForCanonicalUris(ImmutableSet.of(pnItem.getCanonicalUri(), paItem.getCanonicalUri()));
         verify(store).entriesForCanonicalUris(ImmutableList.of(paItem.getCanonicalUri()));
         verify(store, never()).store(Mockito.isA(LookupEntry.class));
         
         Mockito.validateMockitoUsage();
     }
     
     public void testCanRunTwoWriteSimultaneously() throws InterruptedException {
         
         ExecutorService executor = Executors.newFixedThreadPool(2);
         
         final Item one = createItem("one",Publisher.BBC);
         final Item two = createItem("two",Publisher.PA);
         final Item three = createItem("three",Publisher.ITV);
         final Item four = createItem("four",Publisher.C4);
         final Item five = createItem("five",Publisher.FIVE);
         
         store.store(LookupEntry.lookupEntryFrom(one));
         store.store(LookupEntry.lookupEntryFrom(two));
         store.store(LookupEntry.lookupEntryFrom(three));
         store.store(LookupEntry.lookupEntryFrom(four));
         store.store(LookupEntry.lookupEntryFrom(five));
         
         writeLookup(writer, three, ImmutableSet.of(two, four), Publisher.all());
         
         final CountDownLatch start = new CountDownLatch(1);
         final CountDownLatch finish= new CountDownLatch(2);
         executor.submit(new Callable<Void>() {
             @Override
             public Void call() throws InterruptedException {
                 start.await();
                 writeLookup(writer, one, ImmutableSet.of(two), ImmutableSet.of(Publisher.BBC, Publisher.PA));
                 finish.countDown();
                 return null;
             }
         });
         executor.submit(new Callable<Void>() {
             @Override
             public Void call() throws InterruptedException {
                 start.await();
                 writeLookup(writer, four, ImmutableSet.of(five), ImmutableSet.of(Publisher.C4, Publisher.FIVE));
                 finish.countDown();
                 return null;
             }
         });
         
         start.countDown();
         assertTrue(finish.await(1, TimeUnit.SECONDS));
         
         hasEquivs(one, one, three, two, four, five);
         hasDirectEquivs(one, one, two);
         
         hasEquivs(two, one, three, two, four, five);
         hasDirectEquivs(two, one, two, three);
         
         hasEquivs(three, one, three, two, four, five);
         hasDirectEquivs(three, two, three, four);
         
         hasEquivs(four, one, three, two, four, five);
         hasDirectEquivs(four, three, four, five);
 
         hasEquivs(five, one, three, two, four, five);
         hasDirectEquivs(five, four, five);
     }
     
     @Test
     public void testAbortsWriteWhenSetTooLarge() {
         
         LookupEntryStore store = mock(LookupEntryStore.class);
         TransitiveLookupWriter writer = generatedTransitiveLookupWriter(store);
         
         Item big = createItem("big", Publisher.BBC);
         Item equiv = createItem("equiv", Publisher.PA);
         
         LookupEntry bigEntry = LookupEntry.lookupEntryFrom(big);
         LookupEntry equivEntry = LookupEntry.lookupEntryFrom(equiv);
         
         bigEntry = bigEntry.copyWithEquivalents(Iterables.transform(
            ContiguousSet.create(Range.closedOpen(0, 200), DiscreteDomain.integers()), 
             new Function<Integer, LookupRef>() {
                 @Override
                 public LookupRef apply(Integer input) {
                     return new LookupRef(input+"Uri", input.longValue(), Publisher.BBC_REDUX, ContentCategory.CHILD_ITEM);
                 }
             }
         ));
         
         when(store.entriesForCanonicalUris(argThat(hasItems(big.getCanonicalUri(), equiv.getCanonicalUri()))))
             .thenReturn(ImmutableList.of(bigEntry, equivEntry));
         
         writeLookup(writer, equiv, ImmutableSet.of(big), Publisher.all());
         
         verify(store).entriesForCanonicalUris(argThat(hasItems(big.getCanonicalUri(), equiv.getCanonicalUri())));
         verify(store, never()).store(Mockito.isA(LookupEntry.class));
         
         Mockito.validateMockitoUsage();
         
     }
 }
