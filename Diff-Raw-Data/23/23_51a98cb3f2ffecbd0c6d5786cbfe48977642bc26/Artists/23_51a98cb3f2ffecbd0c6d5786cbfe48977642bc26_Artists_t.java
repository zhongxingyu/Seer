 
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
  * <p>Artists is a {@link ModelsCollection} specialization for {@link Artist} collections.</p>
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public class Artists extends ModelsCollection {
 
     /** Tracks API end point */
     public static final String END_POINT = "artists";
     
     protected List<Artist> collection = new ArrayList<Artist>();
     
     /**
      * <p>Constructor for Artists.</p>
      */
     protected Artists(){
         this.pEndPoint = END_POINT;
     }
     
     /**
      * Getter method for the XML tag name of collection's elements.
      * 
      * @return the XML tag name {@link String} for Artists collection elements.
      */
     public String getTagName() {
         return Artist.TAG_NAME;
     }
     
     /**
      * <p>Getter method for the Artist collection.</p>
      *
      * @return the {@link List} of {@link Artist} of this collection.
      */
     public List<? extends Artist> getCollection() {
         return this.collection;
     }
     
     /**
      * Adds a Artist to the collection: this is mainly used by the parser.
      *
      * @param artist a {@link Artist} to add to the collection.
      */
     public void addArtist(Artist artist) {
         this.collection.add(artist);
     }
     
     /**
      * <p>Setter method for the collection list of {@link Artist}.</p>
      *
      * @param collection a {@link List} of {@link Artist} that represents the collection.
      */
     @SuppressWarnings("unchecked")
     @Override
     public void setCollection(List<?> collection) {
         this.collection = (List<Artist>) collection;
     }
     
     /**
      * <p>Getter method for a single {@link Artist} contained in the collection.</p>
      *
      * @param index the index of the desired Artist.
      * 
      * @return a {@link Artist} object.
      */
     public Artist get(int index) {
         return collection.get(index);
     }
     
     /**
      * <p>Getter method for a single {@link Artist} contained in the collection.</p>
      *
     * @param token the uuid of the desired Artist.
      * 
      * @return a {@link Artist} object.
      */
     public Artist get(String token) {
         for (Artist artist : collection) {
             if ( token.equals( artist.getToken() ) )
                 return artist;
         }
         return null;
     }
 }
