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
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 @Entity(name = "Artist")
 @Table(name = "ARTISTS")
 public class Artist {
 
 	@SuppressWarnings("unused")
 	@Id
 	@GeneratedValue
 	@Column(name = "ARTIST_ID")
 	private Long id;
 
 	@Column(name = "UUID", unique = true, length = 36)
 	private String uuid;
 
 	@Column(name = "NAME", unique = true, length = 255)
 	private String name;
 
 	@OneToMany(mappedBy = "artist", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
 	private Set<Album> albums;
 
 	// For proxying.
 	protected Artist() {
 	}
 
 	public Artist(String name) {
 		this.uuid = UUID.randomUUID().toString();
 		this.name = name;
 
 		albums = new HashSet<Album>();
 	}
 
 	public Set<Album> getAlbums() {
 		return Collections.unmodifiableSet(albums);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getUuid() {
 		return uuid;
 	}
 
 	public void addAlbum(Album album) {
 		if (album.getArtist() != this) {
 			album.setArtist(this);
 		}
 		if (!albums.contains(album)) {
 			albums.add(album);
 		}
 	}
 
 	public void removeAlbum(Album album) {
 		albums.remove(album);
 	}
 
 	@Override
 	public int hashCode() {
		return new HashCodeBuilder().append(name).append(name).toHashCode();
 	}
 
 	@Override
 	public boolean equals(final Object other) {
 		if (!(other instanceof Artist))
 			return false;
 		Artist castOther = (Artist) other;
		return new EqualsBuilder().append(name, castOther.name).isEquals();
 	}
 
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).append("uuid", uuid)
 				.append("name", name).toString();
 	}
 
 }
