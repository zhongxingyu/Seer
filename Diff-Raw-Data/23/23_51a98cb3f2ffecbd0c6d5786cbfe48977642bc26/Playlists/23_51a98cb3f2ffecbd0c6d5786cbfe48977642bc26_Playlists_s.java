 
 /***************************************************************************
  *   Copyright (C) 2010 iCoreTech research labs                            *
  *   Contributed code from:                                                *
  *   - Valerio Chiodino - keytwo at keytwo dot net                         *
  *   - Fabio Tunno      - fat at fatshotty dot net                         *
  *                                                                         *
  *   This program is free software: you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation, either version 3 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program. If not, see http://www.gnu.org/licenses/     *
  *                                                                         *
  ***************************************************************************/
 
 package fm.audiobox.core.models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fm.audiobox.core.api.ModelsCollection;
 
 
 /**
  * <p>Playlists is a {@link ModelsCollection} specialization for {@link Playlist} collections.</p>
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public class Playlists extends ModelsCollection {
 
     /** Tracks API end point */
     public static final String END_POINT = "playlists";
     
     protected List<Playlist> collection = new ArrayList<Playlist>();
     
     /**
      * <p>Constructor for Playlists.</p>
      */
     protected Playlists(){
         this.pEndPoint = END_POINT;
     }
     
     /**
      * Getter method for the XML tag name of collection's elements.
      * 
      * @return the XML tag name {@link String} for Playlists collection elements.
      */
     public String getTagName() {
         return Playlist.TAG_NAME;
     }
 
     /**
      * <p>Getter method for the Playlists collection.</p>
      *
      * @return the {@link List} of {@link Playlist} of this collection.
      */
     public List<? extends Playlist> getCollection() {
         return this.collection;
     }
     
     /**
      * Adds a Playlist to the collection: this is mainly used by the parser.
      *
      * @param playlist a {@link Playlist} to add to the collection.
      */
     public void addPlaylist(Playlist playlist) {
         this.collection.add(playlist);
     }
     
     /**
      * <p>Setter method for the collection list of {@link Playlist}.</p>
      *
      * @param collection a {@link List} of {@link Playlist} that represents the collection.
      */
     @SuppressWarnings("unchecked")
     @Override
     public void setCollection(List<?> collection) {
         this.collection = (List<Playlist>) collection;
     }
     
     /**
      * <p>Getter method for a single {@link Playlist} contained in the collection.</p>
      *
      * @param index the index of the desired Playlist.
      * 
      * @return a {@link Playlist} object.
      */
     public Playlist get(int index) {
         return collection.get(index);
     }
     
     /**
      * <p>Getter method for a single {@link Playlist} contained in the collection.</p>
      *
     * @param uuid the uuid of the desired Playlist.
      * 
      * @return a {@link Playlist} object.
      */
     public Playlist get(String token) {
         for (Playlist playlist : collection) {
             if ( token.equals( playlist.getToken() ) )
                 return playlist;
         }
         return null;
     }
 }
