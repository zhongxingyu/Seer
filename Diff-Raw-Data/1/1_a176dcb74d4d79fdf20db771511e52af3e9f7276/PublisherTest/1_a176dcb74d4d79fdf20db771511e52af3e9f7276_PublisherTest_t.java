 /**
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.ushahidi.swiftriver.core.dropqueue;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 import com.ushahidi.swiftriver.core.api.client.SwiftRiverClient;
 import com.ushahidi.swiftriver.core.api.client.model.Drop;
 import com.ushahidi.swiftriver.core.dropqueue.model.RawDrop;
 
 public class PublisherTest {
 	
 	private BlockingQueue<RawDrop> publishQueue;
 	
 	private SwiftRiverClient mockApiClient;
 	
 	private Publisher publisher;
 
 	@Before
 	public void setup() {
 		publishQueue = new LinkedBlockingQueue<RawDrop>();
 		mockApiClient = mock(SwiftRiverClient.class);
 		
 		publisher = new Publisher();
 		publisher.setPublishQueue(publishQueue);
 		publisher.setApiClient(mockApiClient);
		publisher.setDropBatchSize(100);
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Test
 	public void postDrops() throws IOException, InterruptedException {
 		RawDrop rawDrop = new RawDrop();
 		rawDrop.setTitle("title");
 		rawDrop.setContent("content");
 		rawDrop.setChannel("channel");
 		rawDrop.setDatePublished("date pub");
 		rawDrop.setDropOriginalId("drop original id");
 		
 		List<Long> riverIds = new ArrayList<Long>();
 		riverIds.add(1L);
 		rawDrop.setRiverIds(riverIds);
 		rawDrop.setIdentityAvatar("identity avatar");
 		rawDrop.setIdentityName("identity name");
 		rawDrop.setIdentityOriginalId("identity orig id");
 		rawDrop.setIdentityUsername("identity username");
 		
 		RawDrop.Link link = new RawDrop.Link();
 		link.setUrl("url");
 		List<RawDrop.Link> links = new ArrayList<RawDrop.Link>();
 		links.add(link);
 		rawDrop.setLinks(links);
 		
 		RawDrop.Tag tag = new RawDrop.Tag();
 		tag.setName("tag name");
 		tag.setType("tag type");
 		List<RawDrop.Tag> tags = new ArrayList<RawDrop.Tag>();
 		tags.add(tag);
 		rawDrop.setTags(tags);
 		
 		RawDrop.Place place = new RawDrop.Place();
 		place.setName("place name");
 		place.setLatitude(1.0f);
 		place.setLongitude(2.0f);
 		List<RawDrop.Place> places = new ArrayList<RawDrop.Place>();
 		places.add(place);
 		rawDrop.setPlaces(places);
 
 		RawDrop.Media m = new RawDrop.Media();
 		m.setUrl("url");
 		m.setType("media type");
 		m.setDropImage(true);
 		RawDrop.Thumbnail thumbnail = new RawDrop.Thumbnail();
 		thumbnail.setSize(100);
 		thumbnail.setUrl("thumbnail url");
 		List<RawDrop.Thumbnail> thumbnails = new ArrayList<RawDrop.Thumbnail>();
 		thumbnails.add(thumbnail);
 		m.setThumbnails(thumbnails);
 		List<RawDrop.Media> media = new ArrayList<RawDrop.Media>();
 		media.add(m);
 		rawDrop.setMedia(media);
 		
 		publishQueue.add(rawDrop);
 		
 		publisher.postDrops();
 		
 		ArgumentCaptor<List> argument = ArgumentCaptor
 				.forClass(List.class);
 		verify(mockApiClient).postDrops(argument.capture());
 		List<Drop> drops = argument.getValue();
 		
 		assertEquals(1, drops.size());
 		
 		Drop drop = drops.get(0);
 		assertEquals("title", drop.getTitle());
 		assertEquals("content", drop.getContent());
 		assertEquals("channel", drop.getChannel());
 		assertEquals("date pub", drop.getDatePublished());
 		assertEquals("drop original id", drop.getOriginalId());
 		assertTrue(drop.getRiverIds().contains(1L));
 		assertEquals("identity avatar", drop.getIdentity().getAvatar());
 		assertEquals("identity name", drop.getIdentity().getName());
 		assertEquals("identity orig id", drop.getIdentity().getOriginId());
 		assertEquals("identity username", drop.getIdentity().getUsername());
 		assertEquals(1, drop.getLinks().size());
 		assertEquals("url", drop.getLinks().get(0).getUrl());
 		assertEquals(1, drop.getTags().size());
 		assertEquals("tag name", drop.getTags().get(0).getTag());
 		assertEquals("tag type", drop.getTags().get(0).getType());
 		assertEquals(1, drop.getMedia().size());
 		assertEquals("url", drop.getMedia().get(0).getUrl());
 		assertEquals("media type", drop.getMedia().get(0).getType());
 		assertEquals("url", drop.getImage());
 		assertEquals(1, drop.getMedia().get(0).getThumbnails().size());
 		assertEquals(100, drop.getMedia().get(0).getThumbnails().get(0).getSize());
 		assertEquals("thumbnail url", drop.getMedia().get(0).getThumbnails().get(0).getUrl());
 		assertEquals(1, drop.getPlaces().size());
 		assertEquals("place name", drop.getPlaces().get(0).getName());
 		assertEquals(1.0f, drop.getPlaces().get(0).getLatitude(), 0);
 		assertEquals(2.0f, drop.getPlaces().get(0).getLongitude(), 0);
 	}
 }
