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
 
 package org.uriplay.beans;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
import java.util.Collections;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.uriplay.media.TransportType;
 import org.uriplay.media.entity.Encoding;
 import org.uriplay.media.entity.Item;
 import org.uriplay.media.entity.Location;
 import org.uriplay.media.entity.Playlist;
 import org.uriplay.media.entity.Version;
 import org.uriplay.query.uri.Profile;
 
 import com.google.common.collect.Sets;
 
 /**
  * Unit test for {@link ProfileFilter}.
  *
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class ProfileFilterTest extends TestCase {
 
 	Filter filter = new ProfileFilter();
 		
 	Playlist playlist = new Playlist();
 	Item item = new Item();
 	Version version = new Version();
 	Encoding encoding = new Encoding();
 	
 	Location location1 = downloadLocation();
 	Location location2 = htmlembedLocation();
 	Location location3 = embedobjectLocation();
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		encoding.addAvailableAt(location1);		
 		encoding.addAvailableAt(location2);		
 		encoding.addAvailableAt(location3);		
 		version.addManifestedAs(encoding);
 		item.addVersion(version);
 		playlist.addItem(item);
 	}
 	
 	public void testDoesNotRemoveAnythingForProfileAll() throws Exception {
 		
 		Encoding encoding2 = new Encoding();
 		version.addManifestedAs(encoding2);
 		Set<Object> beans = Sets.<Object> newHashSet(playlist);
 		
 		assertThat(filter.applyTo(beans, Profile.ALL), is((Object) Sets.newHashSet(playlist)));
 		assertThat(version.getManifestedAs(), is((Object)Sets.newHashSet(encoding, encoding2)));
 		assertThat(encoding.getAvailableAt(), is((Object)Sets.newHashSet(location1, location2, location3)));
 	}
 	
 	public void testDoesNotRemoveLocationsForProfileAllPlayable() throws Exception {
 		
		Set<Object> beans = Sets.<Object> newHashSet(playlist);
 		
		assertThat(filter.applyTo(beans, Profile.PLAYABLE), is((Object) Sets.newHashSet(playlist)));
 		assertThat(encoding.getAvailableAt(), is((Object)Sets.newHashSet(location1, location2, location3)));
 	}
 
 	public void testFiltersLocationsBasedOnTransportTypeForProfileDownload() throws Exception {
 		
 		Set<Object> beans = Sets.<Object> newHashSet(playlist);
 		
		assertThat(filter.applyTo(beans, Profile.DOWNLOAD), is((Object) Sets.newHashSet(playlist)));
 		assertThat(encoding.getAvailableAt(), is((Object)Sets.newHashSet(location1)));
 	}
 	
 	public void testFiltersLocationsBasedOnTransportTypeForProfileWeb() throws Exception {
 		
 		Set<Object> beans = Sets.<Object> newHashSet(playlist);
 		
		assertThat(filter.applyTo(beans, Profile.WEB), is((Object) Sets.newHashSet(playlist)));
 		assertThat(encoding.getAvailableAt(), is((Object)Sets.newHashSet(location2)));
 	}
 	
 	public void testFiltersLocationsBasedOnTransportTypeForProfileEmbed() throws Exception {
 		
 		Set<Object> beans = Sets.<Object> newHashSet(playlist);
 		
		assertThat(filter.applyTo(beans, Profile.EMBED), is((Object) Sets.newHashSet(playlist)));
 		assertThat(encoding.getAvailableAt(), is((Object)Sets.newHashSet(location3)));
 	}
 	
 	public void testFiltersItemsBasedOnProfile() throws Exception {
 		
 		Set<Object> beans = Sets.<Object> newHashSet(item);
 		
		assertThat(filter.applyTo(beans, Profile.DOWNLOAD), is((Object) Sets.newHashSet(item)));
 		assertThat(encoding.getAvailableAt(), is((Object)Sets.newHashSet(location1)));
 	}
 	
 	public void testFiltersMultipleItemsBasedOnProfile() throws Exception {
 	
 		Item item2 = new Item();
 		Version version2 = new Version();
 		Encoding enc2 = new Encoding();
 		Location loc2 = downloadLocation();
 		item2.addVersion(version2);
 		version2.addManifestedAs(enc2);
 		enc2.addAvailableAt(loc2);
 		
		Set<Object> beans = Sets.<Object> newHashSet(item, item2);
 		
		assertThat(filter.applyTo(beans, Profile.DOWNLOAD), is((Object) Sets.newHashSet(item, item2)));
 		assertThat(encoding.getAvailableAt(), is((Object)Sets.newHashSet(location1)));
 		assertThat(enc2.getAvailableAt(), is((Object)Sets.newHashSet(loc2)));
 	}
 	
 	public void testFiltersEncodingsAndLocationsForIphoneProfile() throws Exception {
 		
 		Item item = new Item();
 		Version version = new Version();
 		item.addVersion(version);
 		Encoding audiomp4 = new Encoding();
 		audiomp4.setDataContainerFormat("audio/mp4");
 		audiomp4.setVideoCoding("video/H263");
 		audiomp4.setAudioCoding("audio/mp4");
 		audiomp4.setVideoBitRate(2000);
 		audiomp4.setAudioBitRate(150);
 		audiomp4.setVideoFrameRate(15f);
 		audiomp4.setVideoHorizontalSize(640);
 		audiomp4.setVideoVerticalSize(400);
 		
 		Encoding videomp4 = new Encoding();
 		videomp4.setDataContainerFormat("video/mp4");
 		videomp4.setVideoCoding("video/H263");
 		videomp4.setAudioCoding("audio/mp4");
 		videomp4.setVideoBitRate(4000);
 		videomp4.setAudioBitRate(150);
 		videomp4.setVideoFrameRate(15f);
 		videomp4.setVideoHorizontalSize(640);
 		videomp4.setVideoVerticalSize(480);
 		
 		
 		version.addManifestedAs(audiomp4);
 		version.addManifestedAs(videomp4);
 		
 		Location httpDownloadLocation = downloadLocation();
 		httpDownloadLocation.setTransportSubType("http");
 		audiomp4.addAvailableAt(httpDownloadLocation);
 
 		Location ftpDownloadLocation = downloadLocation();
 		ftpDownloadLocation.setTransportSubType("ftp");
 		videomp4.addAvailableAt(ftpDownloadLocation);
 		
 		Set<Object> beans = Sets.<Object> newHashSet(item);
 		
		assertThat(filter.applyTo(beans, Profile.IPHONE), is((Object) Sets.newHashSet(item)));
 		assertThat(version.getManifestedAs(), is((Object)Sets.newHashSet(audiomp4)));
 		assertThat(audiomp4.getAvailableAt(), is((Object)Sets.newHashSet(httpDownloadLocation)));
 	}
 	
 	public void testFiltersOutEmptyPlaylists() throws Exception {
 		Playlist list = new Playlist();
 		Set<Object> beans = Sets.<Object> newHashSet(list);
 		assertThat(filter.applyTo(beans, Profile.IPHONE).size(), is(0));
 	}
 	
 	private Location htmlembedLocation() {
 		Location location = new Location();
 		location.setTransportType(TransportType.HTMLEMBED.toString());
 		return location;
 	}
 
 	private Location downloadLocation() {
 		Location location = new Location();
 		location.setTransportType(TransportType.DOWNLOAD.toString());
 		return location;
 	}
 	
 	private Location embedobjectLocation() {
 		Location location = new Location();
 		location.setTransportType(TransportType.EMBEDOBJECT.toString());
 		return location;
 	}
 }
