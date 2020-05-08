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
 
 package me.m1key.audiolicious.domain.entities;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import javax.persistence.PersistenceException;
 
import org.junit.Before;
 import org.junit.Test;
 
 public class ArtistHibernateIT extends HibernateIT {
 
 	private static final String ARTIST_1_NAME = "Blue Öyster Cult";
 	private static final String ARTIST_2_NAME = "Foghat";
 	private static final String ARTIST_3_NAME = "The Edgar Winter Group";
 
 	private Artist artist1;
 
	@Before
	public void setup() {
		assertEquals("There should be no artists before none are created.",
				Integer.valueOf(0), numberOfArtists());
	}

 	@Test
 	public void shouldHaveCorrectNumberOfArtists() {
 		createArtists();
 	}
 
 	@Test(expected = PersistenceException.class)
 	public void shouldNotAllowArtistWithDuplicateName() {
 		Artist artist1 = new Artist(ARTIST_1_NAME);
 		Artist artist2 = new Artist(ARTIST_1_NAME);
 
 		persistArtist(artist1);
 		persistArtist(artist2);
 	}
 
 	@Test
 	public void shouldReturnArtistByName() {
 		createArtists();
 		assertTrue(String.format("Should return artist [%s].", ARTIST_1_NAME),
 				artistsContainByName(ARTIST_1_NAME));
 	}
 
 	@Test
 	public void shouldDeleteOnlyOneArtistByName() {
 		createArtists();
 		assertTrue(String.format("Should return artist [%s].", ARTIST_1_NAME),
 				artistsContainByName(ARTIST_1_NAME));
 		deleteArtistByName(ARTIST_1_NAME);
 		assertEquals("There should be two artists after one has been deleted.",
 				Integer.valueOf(2), numberOfArtists());
 		assertFalse(String.format(
 				"Should not return artist [%s] after it was deleted.",
 				ARTIST_1_NAME), artistsContainByName(ARTIST_1_NAME));
 	}
 
 	@Test
 	public void shouldDeleteOnlyOneArtistByUuid() {
 		createArtists();
 		assertTrue(String.format("Should return artist [%s].", ARTIST_1_NAME),
 				artistsContainByName(ARTIST_1_NAME));
 		deleteArtistByUuid(artist1.getUuid());
 		assertEquals("There should be two artists after one has been deleted.",
 				Integer.valueOf(2), numberOfArtists());
 		assertFalse(String.format(
 				"Should not return artist [%s] after it was deleted.",
 				ARTIST_1_NAME), artistsContainByName(ARTIST_1_NAME));
 		assertFalse(String.format(
 				"Should not return artist [%s] after it was deleted.",
 				ARTIST_1_NAME), artistsContainByUuid(artist1.getUuid()));
 	}
 
 	private void createArtists() {
 		artist1 = new Artist(ARTIST_1_NAME);
 		Artist artist2 = new Artist(ARTIST_2_NAME);
 		Artist artist3 = new Artist(ARTIST_3_NAME);
 
 		persistArtist(artist1);
 		persistArtist(artist2);
 		persistArtist(artist3);
 
 		assertEquals("There should be three artists after three are created.",
 				Integer.valueOf(3), numberOfArtists());
 	}
 }
