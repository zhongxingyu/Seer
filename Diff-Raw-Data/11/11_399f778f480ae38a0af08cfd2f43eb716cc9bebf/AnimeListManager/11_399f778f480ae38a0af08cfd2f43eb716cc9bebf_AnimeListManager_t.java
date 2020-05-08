 /**
  *     Copyright (C) 2012 Blake Dickie
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.landora.animeinfo.metadata;
 
 import java.util.Calendar;
 import net.landora.animeinfo.data.*;
 import net.landora.video.info.ViewListManager;
 import net.landora.video.info.ViewListState.DiskState;
 
 /**
  *
  * @author bdickie
  */
 public class AnimeListManager implements ViewListManager<AnimeMetadata,AnimeViewListState> {
     
     // <editor-fold defaultstate="collapsed" desc="Singleton">
     /**
      * SingletonHolder is loaded on the first execution of Singleton.getInstance()
      * or the first access to SingletonHolder.instance , not before.
      */
     private static class SingletonHolder {
 
         private final static AnimeListManager instance = new AnimeListManager();
     }
 
     public static AnimeListManager getInstance() {
         return SingletonHolder.instance;
     }
     // </editor-fold>
 
 
     private AnimeListManager() {
         
     }
     
     @Override
     public AnimeViewListState getViewListState(AnimeMetadata videoMetadata) {
         AnimeFile file = videoMetadata.getFile();
         if (file == null) {
            AnimeEpisode episode = videoMetadata.getEpisode();
            file = AnimeDBA.getGenericAnimeFile(episode);
        }
        
        if (file != null) {
             AnimeListItem item = AnimeManager.getInstance().findListItemByFileId(file.getFileId(), true);
             if (item == null)
                 return null;
             else
                 return new AnimeViewListState(item);
        } else
            return null;
     }
 
     @Override
     public AnimeViewListState createViewListState(AnimeMetadata videoMetadata, Calendar viewDate, DiskState diskState) {
         AnimeListState listState = AnimeListState.lookupDiskState(diskState);
         
         AnimeListItem listItem;
         
         AnimeFile file = videoMetadata.getFile();
         if (file == null) {
             AnimeEpisode episode = videoMetadata.getEpisode();
             listItem = AnimeManager.getInstance().getOrCreateGenericListItem(episode, viewDate, listState);
         } else {
             listItem = AnimeManager.getInstance().getOrCreateListItemByFileId(file.getFileId(), viewDate, listState);
         }
         
         if (listItem == null)
             return null;
         else
             return new AnimeViewListState(listItem);
     }
 }
