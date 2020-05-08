 /*******************************************************************************
  * Copyright (c) 2012 Christian Gawron.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package de.cgawron.didl.model;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import de.cgawron.didl.model.ArtistWithRole.Role;
 
 @Entity
 @Inheritance(strategy = InheritanceType.JOINED)
 @DiscriminatorColumn(name = "class")
 public abstract class DIDLObject implements Cloneable
 {
    public static final String NS_UPNP = "urn:schemas-upnp-org:metadata-1-0/upnp/";
    public static final String NS_DIDL = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/";
    public static final String NS_DC = "http://purl.org/dc/elements/1.1/";
 
    private static final String OBJECT = "object";
 
    public static final String ITEM = "object.item";
    public static final String AUDIOITEM = "object.item.audioItem";
    public static final String MUSICTRACK = "object.item.audioItem.musicTrack";
    public static final String AUDIOBROADCAST = "object.item.audioItem.audioBroadcast";
    public static final String CONTAINER = "object.container";
    public static final String ALBUM = "object.container.album";
    public static final String MUSICALBUM = "object.container.album.musicAlbum";
    public static final String MUSICGENRE = "object.container.genre.musicGenre";
 
    private String id;
    private Container parent;
    private String title = "";
    private String creator = "";
    private String clazz;
    private boolean restricted;
    private List<Res> resources;
    private String albumArtURI;
    protected Set<ArtistWithRole> artists = new HashSet<ArtistWithRole>();
    protected Set<String> genre = new HashSet<String>();
 
    protected DIDLObject()
    {
 	  setClazz(OBJECT);
    }
 
    protected DIDLObject(String id, Container parent)
    {
 	  this.id = id;
 	  setParent(parent);
 	  setClazz(OBJECT);
    }
 
    protected DIDLObject(String id, Container parent, String title, String creator)
    {
 	  setId(id);
 	  setParent(parent);
 	  setTitle(title);
 	  setCreator(creator);
    }
 
    @XmlTransient
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parentId")
    public Container getParent() {
 	  return parent;
    }
 
    public void setParent(Container parent) {
 	  this.parent = parent;
    }
 
    @XmlAttribute
    @Transient
    public String getParentID()
    {
 	  if (parent == null)
 		 return "-1";
 	  else
 		 return parent.getId();
    }
 
    @XmlElement(required = true, defaultValue = "", namespace = NS_DC)
    public String getTitle() {
 	  return title;
    }
 
    public void setTitle(String title) {
 	  this.title = title;
    }
 
    @XmlElement(namespace = NS_DC)
    public String getCreator() {
 	  return creator;
    }
 
    public void setCreator(String creator) {
 	  this.creator = creator;
    }
 
    @Column(name = "class")
    @XmlElement(name = "class", required = true, namespace = NS_UPNP)
    public String getClazz() {
 	  return clazz;
    }
 
    protected void setClazz(String clazz) {
 	  this.clazz = clazz;
    }
 
    @XmlAttribute
    public boolean isRestricted() {
 	  return restricted;
    }
 
    public void setRestricted(boolean restricted) {
 	  this.restricted = restricted;
    }
 
    @XmlAttribute
    @Id
    public String getId() {
 	  return id;
    }
 
    public void setId(String id) {
 	  this.id = id;
    }
 
    public void setId(UUID id) {
 	  setId(id.toString());
    }
 
    @XmlElement(name = "res")
    @ManyToMany(cascade = CascadeType.ALL)
    public List<Res> getResources() {
 	  return resources;
    }
 
    public void setResources(List<Res> resources) {
 	  this.resources = resources;
    }
 
    public void addResource(Res res) {
 	  if (resources == null)
 	  {
 		 setResources(new ArrayList<Res>());
 	  }
 	  resources.add(res);
    }
 
    @Override
    public String toString() {
 	  return String.format("DIDLObject [class=%s id=%s, parent=%s, title=%s, creator=%s, clazz=%s, resources=%s]",
 		                   getClass().getName(), id, parent.getId(), title, creator, clazz, resources);
    }
 
    /**
     * Dummy index. Should be overloaded by child classes that want to persist
     * the order of items in a container
     */
    @XmlTransient
    public int getIndex() {
 	  return 0;
    }
 
    public void setIndex(int index) {
    }
 
    @Override
    public DIDLObject clone() throws CloneNotSupportedException {
 	  DIDLObject clone = (DIDLObject) (super.clone());
 	  clone.resources = new ArrayList<Res>(resources);
 	  clone.artists = new HashSet<ArtistWithRole>(artists);
 	  clone.genre = new HashSet<String>(genre);
 	  return clone;
    }
 
    @XmlElement(namespace = DIDLObject.NS_UPNP)
    public String getAlbumArtURI() {
 	  return albumArtURI;
    }
 
    public void setAlbumArtURI(String albumArtURI) {
 	  this.albumArtURI = albumArtURI;
    }
 
    public void setAlbumArtURI(URI albumArtURI) {
 	  if (albumArtURI != null)
 		 this.albumArtURI = albumArtURI.toASCIIString();
 	  else
 		 this.albumArtURI = null;
    }
 
    public void addArtist(String artist, Role role) {
 	  if (artists == null) {
 		 artists = new HashSet<ArtistWithRole>();
 	  }
 	  artists.add(new ArtistWithRole(artist, role));
    }
 
    @XmlElement(name = "artist", namespace = DIDLObject.NS_UPNP)
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "didlobject_artist", joinColumns = @JoinColumn(name = "object_id"))
    public Set<ArtistWithRole> getArtists() {
 	  return artists;
    }
 
    public void setArtists(Set<ArtistWithRole> artists) {
 	  this.artists = artists;
    }
 
    @XmlElement(name = "genre", namespace = DIDLObject.NS_UPNP)
    @ElementCollection
    @Column(name = "genre")
   @JoinTable(name = "didlobject_genre")
    public Set<String> getGenres() {
 	  return genre;
    }
 
    public void setGenres(Set<String> genre) {
 	  this.genre = genre;
    }
 
    public void addGenre(String genre) {
 	  if (this.genre == null)
 		 this.genre = new HashSet<String>();
 	  this.genre.add(genre);
    }
 }
