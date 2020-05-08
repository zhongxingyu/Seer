 /* 
  * Audiolicious - Your Music Library Statistics
  * Copyright (C) 2011, Michal Huniewicz
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.m1key.me
  */
 
 package me.m1key.audiolicious.repositories;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.IOException;
 
 import javax.ejb.EJB;
 
 import me.m1key.audiolicious.commons.qualifiers.NullArtist;
 import me.m1key.audiolicious.commons.qualifiers.NullLibrary;
 import me.m1key.audiolicious.domain.entities.Album;
 import me.m1key.audiolicious.domain.entities.AlbumAndSongInfo;
 import me.m1key.audiolicious.domain.entities.AlbumInfo;
 import me.m1key.audiolicious.domain.entities.Artist;
 import me.m1key.audiolicious.domain.entities.Library;
 import me.m1key.audiolicious.domain.entities.NullEntitiesFactory;
 import me.m1key.audiolicious.domain.entities.Rating;
 import me.m1key.audiolicious.domain.entities.Song;
 import me.m1key.audiolicious.domain.entities.SongInfo;
 import me.m1key.audiolicious.domain.entities.Stat;
 import me.m1key.audiolicious.domain.entities.StatInfo;
 import me.m1key.audiolicious.domain.to.RatingTo;
 import me.m1key.audiolicious.domain.to.SongTo;
 import me.m1key.audiolicious.domain.to.TrackTo;
import me.m1key.audiolicious.domain.to.TrackToType;
 import me.m1key.audiolicious.services.ArtistRepository;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(Arquillian.class)
 public class JpaArtistRepositoryIT {
 
 	private static final String ARTIST_1_NAME = "Foreigner";
 	private static final String ARTIST_2_NAME = "Natacha Atlas";
 
 	@EJB
 	private ArtistRepository jpaArtistRepository;
 	@EJB
 	private RepositoriesTestHelperBean testHelperBean;
 
 	@Deployment
 	public static WebArchive createTestArchive()
 			throws IllegalArgumentException, IOException {
 		return ShrinkWrap
 				.create(WebArchive.class,
 						JpaArtistRepositoryIT.class.getSimpleName() + ".war")
 				.addAsWebInfResource(EmptyAsset.INSTANCE,
 						ArchivePaths.create("beans.xml"))
 				.addAsResource("log4j.xml", "log4j.xml")
 				.addAsResource("META-INF/persistence.xml",
 						"META-INF/persistence.xml")
 				.addClasses(Album.class, AlbumAndSongInfo.class,
 						AlbumInfo.class, Artist.class, ArtistRepository.class,
 						JpaArtistRepository.class, Library.class,
 						NullArtist.class, NullEntitiesFactory.class,
 						NullLibrary.class, Rating.class, RatingTo.class,
 						Song.class, SongInfo.class, SongTo.class, Stat.class,
 						StatInfo.class, RepositoriesTestHelperBean.class,
						TrackTo.class, TrackToType.class)
 				.addAsLibraries(
 						DependencyResolvers
 								.use(MavenDependencyResolver.class)
 								.artifacts("org.slf4j:slf4j-api:1.6.1",
 										"org.slf4j:slf4j-log4j12:1.6.1",
 										"commons-lang:commons-lang:2.6")
 								.resolveAsFiles());
 	}
 
 	@Test
 	public void shouldNotReturnNullForNonExistentArtist() {
 		assertNotNull("Non existent artist should not be null.",
 				jpaArtistRepository.getArtist("nonexistentartist"));
 	}
 
 	@Test
 	public void shouldSaveAndRetrieveArtist() {
 		Artist artist = new Artist(ARTIST_1_NAME);
 		jpaArtistRepository.createArtist(artist);
 
 		Artist retrievedArtist = jpaArtistRepository.getArtist(ARTIST_1_NAME);
 		assertNotNull(retrievedArtist);
 		assertEquals(
 				"Created and retrieved by name artist should be the same.",
 				artist, retrievedArtist);
 	}
 
 	@Test
 	public void shouldSaveTwoArtistsAndRetrieveCorrectArtist() {
 		Artist artist1 = new Artist(ARTIST_1_NAME);
 		Artist artist2 = new Artist(ARTIST_2_NAME);
 		jpaArtistRepository.createArtist(artist1);
 		jpaArtistRepository.createArtist(artist2);
 
 		Artist retrievedArtist1 = jpaArtistRepository.getArtist(ARTIST_1_NAME);
 		Artist retrievedArtist2 = jpaArtistRepository.getArtist(ARTIST_2_NAME);
 		assertNotNull(retrievedArtist1);
 		assertNotNull(retrievedArtist2);
 		assertEquals(
 				"Created and retrieved by name artist should be the same.",
 				artist1, retrievedArtist1);
 		assertEquals(
 				"Created and retrieved by name artist should be the same.",
 				artist2, retrievedArtist2);
 	}
 
 	@After
 	public void clearTestData() {
 		testHelperBean.deleteAllArtists();
 	}
 
 }
