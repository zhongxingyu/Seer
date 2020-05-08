 
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
  * <p>Albums is a {@link ModelsCollection} specialization for {@link Album} collections.</p>
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public class Albums extends ModelsCollection {
 
     /** Tracks API end point */
     public static final String END_POINT = "albums";
     
     protected List<Album> collection = new ArrayList<Album>();
     
     /**
      * <p>Constructor for Albums.</p>
      */
     protected Albums(){
         this.pEndPoint = END_POINT;
     }
     
     /**
      * Getter method for the XML tag name of collection's elements.
      * 
      * @return the XML tag name {@link String} for Albums collection elements.
      */
     public String getTagName() {
         return Album.TAG_NAME;
     }
     
     /**
      * <p>Getter method for the Album collection.</p>
      *
      * @return the {@link List} of {@link Album} of this collection.
      */
     public List<? extends Album> getCollection() {
         return this.collection;
     }
     
     /**
      * Adds a Album to the collection: this is mainly used by the parser.
      *
      * @param album a {@link Album} to add to the collection.
      */
     public void addAlbum(Album album) {
         this.collection.add( album );
     }
 
     /**
      * <p>Setter method for the collection list of {@link Album}.</p>
      *
      * @param collection a {@link List} of {@link Album} that represents the collection.
      */
     @SuppressWarnings("unchecked")
     @Override
     public void setCollection(List<?> collection) {
         this.collection = (List<Album>) collection;
     }
     
     /**
      * <p>Getter method for a single {@link Album} contained in the collection.</p>
      *
      * @param index the index of the desired Album.
      * 
      * @return a {@link Album} object.
      */
     public Album get(int index) {
         return collection.get(index);
     }
     
     /**
      * <p>Getter method for a single {@link Album} contained in the collection.</p>
      *
     * @param uuid the uuid of the desired Album.
      * 
      * @return a {@link Album} object.
      */
     public Album get(String token) {
         for (Album album : collection) {
             if ( token.equals( album.getToken() ) )
                 return album;
         }
         return null;
     }
 }
