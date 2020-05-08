 
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
 
 package fm.audiobox.core.api;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.interfaces.CollectionListener;
 import fm.audiobox.core.models.AudioBoxClient;
 import fm.audiobox.core.models.Track;
 import fm.audiobox.core.models.Tracks;
 
 
 /**
  * ModelItem class is an abstract extension of {@link Model}.
  * 
  * <p>
  * 
  * It provides common methods for those XML that represent some single element.
  * 
  * <p>
  * 
  * Each ModelItem inherit a {@link Tracks} object that represents the collection 
  * of tracks that this particular model offers.
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public abstract class ModelItem extends Model {
 
     /** The collection of tracks for this object */
     private Tracks mTracks;
 
     /**
      * <p>Getter method for a particular {@link Track} of the ModelItem {@link Tracks} collection.</p>
      *
      * @param uuid the uuid of the desired track.
      * 
      * @return a {@link Track} object.
      */
     public Track getTrack(String uuid) {
         return this.mTracks.get(uuid);
     }
 
     /**
      * <p>Setter for the {@link Tracks} collection.</p>
      *
      * @param tracks a {@link Tracks} ModelsCollection object.
      */
     public void setTracks(Tracks tracks) {
         this.mTracks = tracks;
     }
 
     
     /**
      * Use this method to get the {@link Tracks} collection of this ModelItem.
      * 
      * <p>
      * 
      * This method accept the parameter <code>async</code>. If <code>true</code> the collection is populated 
      * asynchronously; in this case it may be necessary to specify a {@link CollectionListener} to keep track 
      * of what is happening to the collection.
      *
      * @param async whether to make the request asynchronously.
      * 
      * @return this ModelItems {@link Tracks} collection
      * 
      * @throws ModelException if a custom model class was specified and an error while using it occurs.
      */
     public Tracks getTracks(boolean async) throws ModelException {
         this.mTracks = (Tracks) AudioBoxClient.getModelInstance(AudioBoxClient.TRACKS_KEY, this.getConnector());
         Thread t = populateCollection( this.getEndPoint(), this.mTracks );
         if (async)
             t.start();
         else
             t.run();
 
         return this.mTracks;
     }
     
     /**
      * <p>Same as calling {@link ModelItem#getTracks(boolean) ModelItem.getTracks(false)}.</p>
      *
      * @return this ModelItems {@link Tracks} collection
      * 
      * @throws ModelException if a custom model was specified and an error while using occurs.
      */
     public Tracks getTracks() throws ModelException {
         return this.getTracks(false);
     }
     
     
     /* --------------- */
     /* Private methods */
     /* --------------- */
     
     /**
      * This method is used to make asynchronous requests to AudioBox.fm collections API end points.
      *   
      * @param endpoint the collection API end point to make request to 
      * @param collection the collection ({@link ModelsCollection}) to populate
      */
     private Thread populateCollection( final String endpoint, final ModelsCollection collection ) {
 
         // Final reference to user object
         final ModelItem mi = this;
 
         return new Thread() {
 
             public void run() {
                 try {
                     mi.getConnector().execute(endpoint, mi.getToken(), null, collection, null);
                 } catch (LoginException e) {
                     e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                 }
             }
 
         };
 
     }
 }
