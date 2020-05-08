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
 import java.util.Date;
 
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
 import me.m1key.audiolicious.services.LibraryRepository;
 
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
 public class JpaLibraryRepositoryIT {
 
 	@EJB
 	private LibraryRepository jpaLibraryRepository;
 	@EJB
 	private RepositoriesTestHelperBean testHelperBean;
 
 	@Deployment
 	public static WebArchive createTestArchive()
 			throws IllegalArgumentException, IOException {
 		return ShrinkWrap
 				.create(WebArchive.class,
 						JpaLibraryRepositoryIT.class.getSimpleName() + ".war")
 				.addAsWebInfResource(EmptyAsset.INSTANCE,
 						ArchivePaths.create("beans.xml"))
 				.addAsResource("log4j.xml", "log4j.xml")
 				.addAsResource("META-INF/persistence.xml",
 						"META-INF/persistence.xml")
 				.addClasses(Album.class, AlbumAndSongInfo.class,
 						AlbumInfo.class, Artist.class,
 						JpaLibraryRepository.class, Library.class,
 						LibraryRepository.class, NullArtist.class,
 						NullEntitiesFactory.class, NullLibrary.class,
 						Rating.class, RatingTo.class, Song.class,
 						SongInfo.class, SongTo.class, Stat.class,
 						StatInfo.class, RepositoriesTestHelperBean.class,
						TrackTo.class)
 				.addAsLibraries(
 						DependencyResolvers
 								.use(MavenDependencyResolver.class)
 								.artifacts("org.slf4j:slf4j-api:1.6.1",
 										"org.slf4j:slf4j-log4j12:1.6.1",
 										"commons-lang:commons-lang:2.6")
 								.resolveAsFiles());
 	}
 
 	@Test
 	public void shouldNotReturnNullForNonExistentLibrary() {
 		assertNotNull("Non existent album should not be null.",
 				jpaLibraryRepository.getByUuid("nonexistentartist"));
 	}
 
 	@Test
 	public void shouldSaveAndRetrieveLibraryByUuid() {
 		Library library = new Library(new Date());
 		jpaLibraryRepository.save(library);
 
 		assertEquals("Should retrieve saved library by UUID.",
 				library.getUuid(),
 				jpaLibraryRepository.getByUuid(library.getUuid()).getUuid());
 	}
 
 	@After
 	public void clearTestData() {
 		testHelperBean.deleteAllLibraries();
 	}
 }
