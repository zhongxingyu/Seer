 package fr.ybo.ybotv.android.service;
 
 
 import android.util.Log;
 import com.omertron.thetvdbapi.TheTVDBApi;
 import com.omertron.thetvdbapi.TheTVDBApiException;
 import com.omertron.thetvdbapi.model.Series;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.exception.YboTvErreurReseau;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.util.AsciiUtils;
 
 import java.util.List;
 
 public class TvDbService {
 
     private static final TvDbService instance = new TvDbService();
 
     public static TvDbService getInstance() {
         return instance;
     }
 
     public Series getTvShow(Programme programme, TheTVDBApi api) throws TheTVDBApiException {

        List<Series> tvshows = api.searchSeries(AsciiUtils.convertNonAscii(programme.getTitle()), null);
         return getCurrentTvShow(programme, tvshows, api);
     }
 
     public TheTVDBApi getTheTVDBApi() {
         return new TheTVDBApi("5C78D31A0A0CF7E3");
     }
 
     public Float getTvShowRating(Programme programme) throws YboTvErreurReseau {
         if (programme == null || programme.getTitle() == null || programme.getDate() == null) {
             return null;
         }
 
         try {
             return getRatingOfTvShow(getTvShow(programme, getTheTVDBApi()));
         } catch (TheTVDBApiException e) {
             throw new YboTvErreurReseau(e);
         }
     }
 
     private Series getCurrentTvShow(Programme programme, List<Series> tvshows, TheTVDBApi api) throws TheTVDBApiException {
         int programmeDate = Integer.parseInt(programme.getDate());
         Log.d(YboTvApplication.TAG, "Series : " + tvshows);
         int currentDate = 0;
         Series currentTvShow = null;
         for (Series tvshow : tvshows) {
             if (tvshow.getFirstAired() != null && tvshow.getFirstAired().length() >= 4) {
                 int tvShowDate = Integer.parseInt(tvshow.getFirstAired().substring(0, 4));
                 if (Math.abs(tvShowDate - programmeDate) < Math.abs(currentDate - programmeDate)) {
                     currentDate = tvShowDate;
                     currentTvShow = tvshow;
                 }
             }
         }
         Log.d(YboTvApplication.TAG, "Selected TvShow : " + currentTvShow);
         if (currentTvShow != null) {
             currentTvShow = api.getSeries(currentTvShow.getId(), null);
             Log.d(YboTvApplication.TAG, "Selected TvShow with details : " + currentTvShow);
         }
         return currentTvShow;
     }
 
     private Float getRatingOfTvShow(Series currentTvShow) {
         if (currentTvShow == null || currentTvShow.getRating() == null) {
             return null;
         }
         Float rating = Float.parseFloat(currentTvShow.getRating()) / 2;
         Log.d(YboTvApplication.TAG, "Rating of " + currentTvShow.getSeriesName() + " : " + rating);
         return rating;
     }
 }
