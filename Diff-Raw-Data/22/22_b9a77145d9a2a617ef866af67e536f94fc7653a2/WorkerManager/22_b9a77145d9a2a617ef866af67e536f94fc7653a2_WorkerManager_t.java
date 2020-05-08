 /*
  * Movie Renamer
  * Copyright (C) 2012 Nicolas Magré
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.worker;
 
import fr.free.movierenamer.media.Media;
import fr.free.movierenamer.media.MediaID;
import fr.free.movierenamer.media.MediaImage;
import fr.free.movierenamer.media.MediaPerson;
 import fr.free.movierenamer.media.tvshow.SxE;
 import fr.free.movierenamer.ui.MoviePanel;
 import fr.free.movierenamer.ui.res.IMediaPanel;
 import fr.free.movierenamer.utils.ActionNotValidException;
 import fr.free.movierenamer.utils.Cache;
 import fr.free.movierenamer.utils.Settings;
 import fr.free.movierenamer.worker.provider.*;
 import java.beans.PropertyChangeSupport;
 import java.util.List;
 import java.util.logging.Level;
 
 /**
  * Class WorkerManager
  *
  * @author Nicolas Magré
  * @author QUÉMÉNEUR Simon
  */
 public abstract class WorkerManager {
 
   public enum MovieScrapper {
 
     IMDB,
     TMDB,
     ALLOCINE;
   }
 
   public enum TVShowScrapper {
 
     TVDB,
     TVRAGE,
     ALLOCINETV;
   }
 
   public enum WORKERID {
 
     SEARCHWORKER,
     INFOWORKER,
     THUMBWORKER,
     FANARTWORKER,
     ACTORWORKER
   }
 
   /**
    * Get media search worker
    *
    * @param errorSupport
    * @param media Media
    * @return Worker depend of media type and settings or null
    */
   public static MediaSearchWorker getSearchWorker(PropertyChangeSupport errorSupport, Media media) {
     MediaSearchWorker worker = null;
     Settings.LOGGER.log(Level.INFO, "Search : {0}", media.getSearch());
     Settings config = Settings.getInstance();
     switch (media.getType()) {
       case MOVIE:
         switch (config.movieScrapper) {
           case IMDB:
             worker = new ImdbSearchWorker(errorSupport, media.getSearch());
             break;
           case TMDB:
             worker = new TmdbSearchWorker(errorSupport, media.getSearch());
             break;
           case ALLOCINE:
             worker = new AllocineSearchWorker(errorSupport, media.getSearch());
             break;
           default:
         }
         break;
       case TVSHOW:
         switch (config.tvshowScrapper) {
           case TVDB:
             worker = new TvdbSearchWorker(errorSupport, media.getSearch());
             break;
           case ALLOCINETV:
             worker = new AllocineTvShowSearchWorker(errorSupport, media.getSearch());
             break;
           default:
           case TVRAGE:
             worker = new TvRageSearchWorker(errorSupport, media.getSearch());
             break;
         }
         break;
       default:
         break;
     }
 
     return worker;
   }
 
   public static MovieInfoWorker getMovieInfoWorker(PropertyChangeSupport errorSupport, MediaID id) throws ActionNotValidException {
     MovieInfoWorker worker = null;
    switch (Settings.getInstance().movieScrapper) {
      case IMDB:
        worker = new ImdbInfoWorker(errorSupport, id);
        break;
      case TMDB:
        worker = new TmdbInfoWorker(errorSupport, id);
        break;
      case ALLOCINE:
        worker = new AllocineInfoWorker(errorSupport, id);
        break;
      default:
    }
     Settings.LOGGER.log(Level.INFO, "Information API ID : {0}", id.getID());
     return worker;
   }
 
   public static TvShowInfoWorker getTvShowInfoWorker(PropertyChangeSupport errorSupport, MediaID id, SxE sxe) throws ActionNotValidException {
     TvShowInfoWorker worker = null;
     switch (Settings.getInstance().tvshowScrapper) {
       case TVDB:
         worker = new TvdbInfoWorker(errorSupport, id);
         break;
       case ALLOCINETV:
         worker = new AllocineTvShowInfoWorker(errorSupport, id, sxe);
         break;
       case TVRAGE:
         // TODO
         break;
     }
    Settings.LOGGER.log(Level.INFO, "Information API ID : {0}", id.getID());
     return worker;
   }
 
   public static ImageWorker getMediaImageWorker(List<MediaImage> array, Cache.CacheType cache, IMediaPanel mediaPanel) {
     return new ImageWorker(array, cache, mediaPanel);
   }
 
   public static ActorWorker getMovieActorWorker(List<MediaPerson> actors, MoviePanel moviePanel) {
     return new ActorWorker(actors, moviePanel);
   }
 }
