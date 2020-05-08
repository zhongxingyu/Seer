 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.search.searcher;
 
 import static org.atlasapi.media.entity.testing.ComplexBroadcastTestDataBuilder.broadcast;
 import static org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder.complexItem;
 import static org.atlasapi.media.entity.testing.VersionTestDataBuilder.version;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Person;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.DummyKnownTypeContentResolver;
 import org.atlasapi.search.model.SearchQuery;
 import org.atlasapi.search.model.SearchResults;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.query.Selection;
 
 public class LuceneContentSearcherTest extends TestCase {
 	
 	private static final ImmutableSet<Publisher> ALL_PUBLISHERS = ImmutableSet.copyOf(Publisher.values());
 	
 	Brand dragonsDen = brand("/den", "Dragon's den");
 	Brand doctorWho = brand("/doctorwho", "Doctor Who");
 	Brand theCityGardener = brand("/garden", "The City Gardener");
 	Brand eastenders = brand("/eastenders", "Eastenders");
 	Brand eastendersWeddings = brand("/eastenders-weddings", "Eastenders Weddings");
 	Brand politicsEast = brand("/politics", "The Politics Show East");
 	Brand meetTheMagoons = brand("/magoons", "Meet the Magoons");
 	Brand theJackDeeShow = brand("/dee", "The Jack Dee Show");
 	Brand peepShow = brand("/peep-show", "Peep Show");
 	Brand euromillionsDraw = brand("/draw", "EuroMillions Draw");
 	Brand haveIGotNewsForYou = brand("/news", "Have I Got News For You");
 	Brand brasseye = brand("/eye", "Brass Eye");
 	Brand science = brand("/science", "The Story of Science: Power, Proof and Passion");
 	Brand theApprentice = brand("/apprentice", "The Apprentice");
 	Item apparent = complexItem().withTitle("Without Apparent Motive").withUri("/item/apparent").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
 	
 	Person jamieOliver = person("/jamie", "Jamie Oliver");
 
 	Item englishForCats = item("/items/cats", "English for cats");
 	Item u2 = item("/items/u2", "U2 Ultraviolet");
 	
 	Item spooks = complexItem().withTitle("Spooks").withUri("/item/spooks").build();
 	Item spookyTheCat = complexItem().withTitle("Spooky the Cat").withUri("/item/spookythecat").withVersions(version().withBroadcasts(broadcast().build()).build()).build();
 	
 	Item jamieOliversCookingProgramme = item("/items/oliver/1", "Jamie Oliver's cooking programme", "lots of words that are the same alpha beta");
 	Item gordonRamsaysCookingProgramme = item("/items/ramsay/2", "Gordon Ramsay's cooking show", "lots of words that are the same alpha beta");
 	
	List<Brand> brands = Arrays.asList(doctorWho, eastendersWeddings, dragonsDen, theCityGardener, eastenders, meetTheMagoons, theJackDeeShow, peepShow, haveIGotNewsForYou, euromillionsDraw, brasseye, science, politicsEast, theApprentice);
 
 	List<Item> items = Arrays.asList(apparent, englishForCats, jamieOliversCookingProgramme, gordonRamsaysCookingProgramme, spooks, spookyTheCat);
 	List<Item> itemsUpdated = Arrays.asList(u2);
 	List<Person> people = Arrays.asList(jamieOliver);
 	
 	LuceneContentSearcher searcher;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		Iterable<Described> allContent = Iterables.concat(brands, items, itemsUpdated, people);
 		searcher = new LuceneContentSearcher(new DummyKnownTypeContentResolver().respondTo(allContent));
 		searcher.contentChange(allContent);
 	}
 	
 	public void testFindingBrandsByTitle() throws Exception {
 		check(searcher.search(title("Aprentice")), theApprentice);
 		check(searcher.search(currentWeighted("apprent")), theApprentice, apparent);
 		check(searcher.search(title("den")), dragonsDen, theJackDeeShow);
 		check(searcher.search(title("dragon")), dragonsDen);
 		check(searcher.search(title("dragons")), dragonsDen);
 		check(searcher.search(title("drag den")), dragonsDen);
 		check(searcher.search(title("drag")), dragonsDen, euromillionsDraw);
 		check(searcher.search(title("dragon's den")), dragonsDen);
 		check(searcher.search(title("eastenders")),  eastenders, eastendersWeddings);
 		check(searcher.search(title("easteners")),  eastenders, eastendersWeddings);
 		check(searcher.search(title("eastedners")),  eastenders, eastendersWeddings);
 		check(searcher.search(title("politics east")),  politicsEast);
 		check(searcher.search(title("eas")),  eastenders, eastendersWeddings, politicsEast);
 		check(searcher.search(title("east")),  eastenders, eastendersWeddings, politicsEast);
 		check(searcher.search(title("end")));
 		check(searcher.search(title("peep show")),  peepShow);
 		check(searcher.search(title("peep s")),  peepShow);
 		check(searcher.search(title("dee")),  theJackDeeShow, dragonsDen);
 		check(searcher.search(title("jack show")),  theJackDeeShow);
 		check(searcher.search(title("the jack dee s")),  theJackDeeShow);
 		check(searcher.search(title("dee show")),  theJackDeeShow);
 		check(searcher.search(title("hav i got news")),  haveIGotNewsForYou);
 		check(searcher.search(title("brasseye")),  brasseye);
 		check(searcher.search(title("braseye")),  brasseye);
 		check(searcher.search(title("brassey")),  brasseye);
 		check(searcher.search(title("The Story of Science Power Proof and Passion")),  science);
 		check(searcher.search(title("The Story of Science: Power, Proof and Passion")),  science);
 		check(searcher.search(title("Jamie")), jamieOliver, jamieOliversCookingProgramme);
 		check(searcher.search(title("Spooks")), spooks, spookyTheCat);
 	}
 	
 	
 	protected static SearchQuery title(String term) {
 		return new SearchQuery(term, Selection.ALL, ALL_PUBLISHERS, 1.0f, 0.0f, 0.0f);
 	}
 	
 	protected static SearchQuery currentWeighted(String term) {
         return new SearchQuery(term, Selection.ALL, ALL_PUBLISHERS, 1.0f, 0.2f, 0.2f);
     }
 
 	public void testLimitingToPublishers() throws Exception {
 		check(searcher.search(new SearchQuery("east", Selection.ALL, ImmutableSet.of(Publisher.BBC, Publisher.YOUTUBE), 1.0f, 0.0f, 0.0f)), eastenders, eastendersWeddings, politicsEast);
 		check(searcher.search(new SearchQuery("east", Selection.ALL, ImmutableSet.of(Publisher.ARCHIVE_ORG, Publisher.YOUTUBE), 1.0f, 0.0f, 0.0f)));
 		
 		Brand east = new Brand("/east", "curie", Publisher.ARCHIVE_ORG);
 		east.setTitle("east");
 		searcher.contentChange(ImmutableList.of(east));
 		check(searcher.search(new SearchQuery("east", Selection.ALL, ImmutableSet.of(Publisher.ARCHIVE_ORG, Publisher.YOUTUBE), 1.0f, 0.0f, 0.0f)), east);
 	}
 	
 	public void testUsesPrefixSearchForShortSearches() throws Exception {
 		check(searcher.search(title("D")),  doctorWho, dragonsDen);
 		check(searcher.search(title("Dr")),  doctorWho, dragonsDen);
 		check(searcher.search(title("a")));
 	}
 	
 	public void testLimitAndOffset() throws Exception {
 		check(searcher.search(new SearchQuery("eas", Selection.ALL, ALL_PUBLISHERS, 1.0f, 0.0f, 0.0f)),  eastenders, eastendersWeddings, politicsEast);
 		check(searcher.search(new SearchQuery("eas", Selection.limitedTo(2), ALL_PUBLISHERS, 1.0f, 0.0f, 0.0f)),  eastenders, eastendersWeddings);
 		check(searcher.search(new SearchQuery("eas", Selection.offsetBy(2), ALL_PUBLISHERS, 1.0f, 0.0f, 0.0f)),  politicsEast);
 	}
 	
 	public void testBroadcastLocationWeighting() {
 	    check(searcher.search(currentWeighted("spooks")), spooks, spookyTheCat);
 	    
 	    check(searcher.search(title("spook")), spooks, spookyTheCat);
 	    check(searcher.search(currentWeighted("spook")), spookyTheCat, spooks);
 	}
 
 	protected static void check(SearchResults result, Identified... content) {
 		assertThat(result.toUris(), is(toUris(Arrays.asList(content))));
 	}
 
 	private static List<String> toUris(List<? extends Identified> content) {
 		List<String> uris = Lists.newArrayList();
 		for (Identified description : content) {
 			uris.add(description.getCanonicalUri());
 		}
 		return uris;
 	}
 
 	protected static Brand brand(String uri, String title) {
 		Brand b = new Brand(uri, uri, Publisher.BBC);
 		b.setTitle(title);
 		return b;
 	}
 	
 	protected static Item item(String uri, String title) {
 		return item(uri, title, null);
 	}
 	
 	protected static Item item(String uri, String title, String description) {
 		Item i = new Item();
 		i.setTitle(title);
 		i.setCanonicalUri(uri);
 		i.setDescription(description);
 		i.setPublisher(Publisher.BBC);
 		return i;
 	}
 	
 	protected static Person person(String uri, String title) {
 	    Person p = new Person(uri, uri, Publisher.BBC);
 	    p.setTitle(title);
 	    return p;
 	}
 }
