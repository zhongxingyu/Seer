 
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
  * <p>Genres is a {@link ModelsCollection} specialization for {@link Genre} collections.</p>
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public class Genres extends ModelsCollection {
 
     /** Tracks API end point */
     public static final String END_POINT = "genres";
     
     protected List<Genre> collection = new ArrayList<Genre>();
     
     /**
      * <p>Constructor for Genres.</p>
      */
     protected Genres(){
         this.pEndPoint = END_POINT;
     }
     
     /**
      * Getter method for the XML tag name of collection's elements.
      * 
      * @return the XML tag name {@link String} for Genres collection elements.
      */
     public String getTagName() {
         return Genre.TAG_NAME;
     }
     
     /**
      * <p>Getter method for the Genres collection.</p>
      *
      * @return the {@link List} of {@link Genre} of this collection.
      */
     public List<? extends Genre> getCollection() {
         return this.collection;
     }
     
     /**
      * Adds a Genre to the collection: this is mainly used by the parser.
      *
      * @param genre a {@link Genre} to add to the collection.
      */
     public void addGenre(Genre genre) {
         this.collection.add(genre);
     }
     
     /**
      * <p>Setter method for the collection list of {@link Genre}.</p>
      *
      * @param collection a {@link List} of {@link Genre} that represents the collection.
      */
     @SuppressWarnings("unchecked")
     @Override
     public void setCollection(List<?> collection) {
         this.collection = (List<Genre>) collection;
     }
     
     /**
      * <p>Getter method for a single {@link Genre} contained in the collection.</p>
      *
      * @param index the index of the desired Genre.
      * 
      * @return a {@link Genre} object.
      */
     public Genre get(int index) {
         return collection.get(index);
     }
     
     /**
      * <p>Getter method for a single {@link Genre} contained in the collection.</p>
      *
     * @param uuid the uuid of the desired Genre.
      * 
      * @return a {@link Genre} object.
      */
     public Genre get(String token) {
         for (Genre genre : collection) {
             if ( token.equals( genre.getToken() ) )
                 return genre;
         }
         return null;
     }
 }
