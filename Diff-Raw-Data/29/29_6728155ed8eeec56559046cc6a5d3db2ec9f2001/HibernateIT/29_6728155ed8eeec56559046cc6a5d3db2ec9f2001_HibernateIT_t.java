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
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 
 @RunWith(Arquillian.class)
 public abstract class HibernateIT {
 
 	private EntityManager entityManager;
 
 	@Deployment
 	public static Archive<?> createTestArchive()
 			throws IllegalArgumentException, IOException {
 		return ShrinkWrap
 				.create(JavaArchive.class,
 						HibernateIT.class.getSimpleName() + ".jar")
 				.addAsManifestResource(
 						new File("src/test/resources/META-INF/persistence.xml"),
 						ArchivePaths.create("persistence.xml"))
 				.addClasses(Album.class, Artist.class, Song.class);
 	}
 
 	@Before
 	public void setup() {
 		EntityManagerFactory entityManagerFactory = Persistence
 				.createEntityManagerFactory("testPu");
 		setEntityManager(entityManagerFactory.createEntityManager());
 	}
 
 	@After
 	public void clearTestData() {
 		deleteAllArtists();
 		deleteAllLibraries();
 	}
 
 	protected EntityManager getEntityManager() {
 		return entityManager;
 	}
 
 	protected void setEntityManager(EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 	protected void deleteAllArtists() {
 		List<Artist> allArtists = getAllArtists();
 		List<Library> allLibraries = getAllLibraries();
 
 		getEntityManager().getTransaction().begin();
 		clearStatsOnAllLibraries(allLibraries);
 		removeAllArtists(allArtists);
 		getEntityManager().getTransaction().commit();
 	}
 
 	private void removeAllArtists(List<Artist> allArtists) {
 		for (Artist artist : allArtists) {
 			getEntityManager().remove(artist);
 		}
 	}
 
 	private void clearStatsOnAllLibraries(List<Library> allLibraries) {
 		for (Library library : allLibraries) {
 			library.clearStats();
 		}
 	}
 
 	protected void deleteAllLibraries() {
 		List<Library> allLibraries = getAllLibraries();
 		List<Song> songs = getAllSongs();
 
 		getEntityManager().getTransaction().begin();
 		for (Song song : songs) {
 			song.clearStats();
 		}
 		for (Library library : allLibraries) {
 			getEntityManager().remove(library);
 		}
 		getEntityManager().getTransaction().commit();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<Artist> getAllArtists() {
 		Query select = getEntityManager().createQuery("FROM Artist");
 		return select.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<Library> getAllLibraries() {
 		Query select = getEntityManager().createQuery("FROM Library");
 		return select.getResultList();
 	}
 
 	protected void deleteArtistByName(String artistName) {
 		Artist artistToDelete = getArtistByName(artistName);
 		getEntityManager().getTransaction().begin();
 		for (Album album : artistToDelete.getAlbums()) {
 			for (Song song : album.getSongs()) {
				for (Stat stat : song.getStats()) {
 					stat.removeFromLibrary();
 				}
 			}
 		}
 		getEntityManager().remove(artistToDelete);
 		getEntityManager().getTransaction().commit();
 	}
 
 	protected Artist getArtistByName(String artistName) {
 		List<?> artistsByName = getEntityManager()
 				.createQuery("FROM Artist WHERE name = :name")
 				.setParameter("name", artistName).getResultList();
 		if (artistsByName.isEmpty()) {
 			return null;
 		} else {
 			return (Artist) artistsByName.get(0);
 		}
 	}
 
 	protected void deleteAlbumByArtistNameAlbumName(String artistName,
 			String albumName) {
 		Album album = getAlbumByArtistNameAlbumName(artistName, albumName);
 		Artist artist = getArtistByName(artistName);
 
 		getEntityManager().getTransaction().begin();
 		removeAlbumFromArtist(artist, album);
 		getEntityManager().remove(album);
 		getEntityManager().getTransaction().commit();
 	}
 
 	protected Album getAlbumByArtistNameAlbumName(String artistName,
 			String albumName) {
 		Artist artist = getArtistByName(artistName);
 		getEntityManager().getTransaction().begin();
 		List<?> albums = getEntityManager()
 				.createQuery(
 						"FROM Album WHERE artist = :artist AND name = :name")
 				.setParameter("artist", artist).setParameter("name", albumName)
 				.getResultList();
 		Album album = null;
 		if (albums.size() > 0) {
 			album = (Album) albums.get(0);
 		}
 		getEntityManager().getTransaction().commit();
 		return album;
 	}
 
 	@SuppressWarnings("unchecked")
 	void removeAlbumFromArtist(Artist artist, Album album) {
 		try {
 			Field albumsField = artist.getClass().getDeclaredField("albums");
 			albumsField.setAccessible(true);
 			Set<Album> albums = (Set<Album>) albumsField.get(artist);
 			albums.remove(album);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected Album getAlbumByUuid(String uuid) {
 		getEntityManager().getTransaction().begin();
 		Album album = (Album) getEntityManager()
 				.createQuery("FROM Album WHERE uuid = :uuid")
 				.setParameter("uuid", uuid).getResultList().get(0);
 		getEntityManager().getTransaction().commit();
 		return album;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<Album> getAlbumsByArtistName(String artistName) {
 		Artist artist = getArtistByName(artistName);
 		Query select = getEntityManager().createQuery(
 				"FROM Album WHERE artist = :artist").setParameter("artist",
 				artist);
 		return (List<Album>) select.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<Album> getAllAlbums() {
 		Query select = getEntityManager().createQuery("FROM Album");
 		return select.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<Song> getAllSongs() {
 		Query select = getEntityManager().createQuery("FROM Song");
 		return select.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<Stat> getAllStats() {
 		Query select = getEntityManager().createQuery("FROM Stat");
 		return select.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<Song> getAllSongsByArtistNameAlbumName(String artistName,
 			String artistAlbumName) {
 		Album album = getAlbumByArtistNameAlbumName(artistName, artistAlbumName);
 		getEntityManager().getTransaction().begin();
 		List<Song> songs = getEntityManager()
 				.createQuery("FROM Song WHERE album = :album")
 				.setParameter("album", album).getResultList();
 		getEntityManager().getTransaction().commit();
 		return songs;
 	}
 
 	protected boolean artistsContainByName(String artistName) {
 		getEntityManager().getTransaction().begin();
 		Query select = getEntityManager().createQuery(
 				"FROM Artist WHERE name = :name").setParameter("name",
 				artistName);
 		boolean artistExists = select.getResultList().size() > 0;
 		getEntityManager().getTransaction().commit();
 		return artistExists;
 	}
 
 	protected boolean artistsContainByUuid(String artistName) {
 		getEntityManager().getTransaction().begin();
 		Query select = getEntityManager().createQuery(
 				"FROM Artist WHERE uuid = :uuid").setParameter("uuid",
 				artistName);
 		boolean artistExists = select.getResultList().size() > 0;
 		getEntityManager().getTransaction().commit();
 		return artistExists;
 	}
 
 	protected void deleteArtistByUuid(String artistUuid) {
 		getEntityManager().getTransaction().begin();
 
 		Query select = getEntityManager().createQuery(
 				"FROM Artist WHERE uuid = :uuid").setParameter("uuid",
 				artistUuid);
 		Artist artist = (Artist) select.getResultList().get(0);
 		getEntityManager().remove(artist);
 
 		getEntityManager().getTransaction().commit();
 	}
 
 	protected void persistArtist(Artist artist1) {
 		getEntityManager().getTransaction().begin();
 		try {
 			getEntityManager().persist(artist1);
 		} catch (PersistenceException pe) {
 			getEntityManager().getTransaction().rollback();
 			throw pe;
 		}
 		getEntityManager().getTransaction().commit();
 	}
 
 	protected Integer numberOfArtists() {
 		List<?> allArtists = getEntityManager().createQuery("FROM Artist")
 				.getResultList();
 		return allArtists.size();
 	}
 
 	protected Song getSongByArtistNameAlbumNameSongName(String artistName,
 			String albumName, String songName) {
 		Album album = getAlbumByArtistNameAlbumName(artistName, albumName);
 		getEntityManager().getTransaction().begin();
 		Song song = (Song) getEntityManager()
 				.createQuery("FROM Song WHERE album = :album AND name = :name")
 				.setParameter("album", album).setParameter("name", songName)
 				.getResultList().get(0);
 		getEntityManager().getTransaction().commit();
 		return song;
 	}
 
 }
