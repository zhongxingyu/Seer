 package grabbers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import play.Logger;
 
 import com.moviejukebox.thetvdb.TheTVDB;
 import com.moviejukebox.thetvdb.model.Banner;
 import com.moviejukebox.thetvdb.model.Banners;
 import com.moviejukebox.thetvdb.model.Episode;
 import com.moviejukebox.thetvdb.model.Series;
 
 import forms.GrabberInfoForm;
 import forms.MovieForm;
 
 public class TheTvDbGrabber implements IInfoGrabber {
 
   private static final String API_KEY = "5868F2154308BB82";
 
   private static final String LANGUAGE = Locale.GERMAN.getLanguage();
 
   private final TheTVDB theTVDB;
 
   private final static EGrabberType TYPE = EGrabberType.THETVDB;
 
   public TheTvDbGrabber() {
     theTVDB = new TheTVDB(TheTvDbGrabber.API_KEY);
   }
 
   @Override
   public List<GrabberSearchMovie> searchForMovie(final String searchTerm) throws GrabberException {
     final List<Series> searchSeries = theTVDB.searchSeries(searchTerm, TheTvDbGrabber.LANGUAGE);
 
     final List<GrabberSearchMovie> returnVal = new ArrayList<GrabberSearchMovie>();
 
     if (CollectionUtils.isEmpty(searchSeries) == false) {
       for (final Series series : searchSeries) {
         final String seriesId = series.getId();
         if (TheTvDbGrabber.LANGUAGE.equals(series.getLanguage()) == false) {
           Logger.debug("Skipping series: " + series.getSeriesName() + "(" + seriesId + ") because language is " + series.getLanguage() + " an not: " + TheTvDbGrabber.LANGUAGE);
           continue;
         }
 
         final List<Episode> allEpisodes = theTVDB.getAllEpisodes(seriesId, TheTvDbGrabber.LANGUAGE);
 
         if (CollectionUtils.isEmpty(allEpisodes) == true) {
           Logger.debug("Skipping series: " + series.getSeriesName() + "(" + seriesId + ") because no episodes where found.");
           continue;
         }
 
         final Banners banners = theTVDB.getBanners(seriesId);
         final List<Banner> seasonBanners = banners.getSeasonList();
 
         final Set<String> seasonIds = new HashSet<String>();
 
         for (final Episode episode : allEpisodes) {
 
           final String seasonId = episode.getSeasonId();
           if (StringUtils.isEmpty(seasonId) == true) {
             Logger.debug("Skipping Episode: " + episode.getId() + " because the seasonId is empty");
             continue;
           }
 
           if (seasonIds.contains(seasonId) == true) {
             continue;
           }
 
           final String movieTitle = buildMovieName(series, episode.getSeasonNumber());
           String posterUrl = null;
 
           if (CollectionUtils.isEmpty(seasonBanners) == false) {
             for (final Banner banner : seasonBanners) {
 
               if (banner.getSeason() == episode.getSeasonNumber()) {
                 posterUrl = getThumbUrl(banner);
                 break;
               }
             }
           }
 
           final String systemId = seriesId + "_" + episode.getSeasonNumber();
           final GrabberSearchMovie searchMovie = new GrabberSearchMovie(systemId, movieTitle, posterUrl, TheTvDbGrabber.TYPE);
           returnVal.add(searchMovie);
           seasonIds.add(seasonId);
 
         }
       }
     }
 
     return returnVal;
 
   }
 
   private String buildMovieName(final Series series, final int seasonId) {
     return series.getSeriesName() + " Season: " + seasonId;
   }
 
   @Override
   public GrabberDisplayMovie getDisplayMovie(final String id) throws GrabberException {
 
     final String[] split = id.split("_");
     final String seriesId = split[0];
     final String seasonId = split[1];
 
     final Series series = theTVDB.getSeries(seriesId, TheTvDbGrabber.LANGUAGE);
     if (series == null) {
       final String message = "Could not find series: " + seriesId;
       Logger.error(message);
       throw new GrabberException(message);
     }
 
     final Banners banners = theTVDB.getBanners(seriesId);
     final List<Banner> seasonList = banners.getSeasonList();
 
     final List<GrabberImage> posterList = new ArrayList<GrabberImage>();
     final Integer season = Integer.valueOf(seasonId);
     for (final Banner banner : seasonList) {
       if (season.equals(banner.getSeason()) == true) {
         posterList.add(new GrabberImage(String.valueOf(banner.getId()), getThumbUrl(banner)));
       }
     }
 
     final List<Banner> fanartList = banners.getFanartList();
     final List<GrabberImage> backdrops = new ArrayList<GrabberImage>();
     for (final Banner banner : fanartList) {
       backdrops.add(new GrabberImage(String.valueOf(banner.getId()), getThumbUrl(banner)));
     }
 
     return new GrabberDisplayMovie(id, buildMovieName(series, season), series.getOverview(), posterList, backdrops, TheTvDbGrabber.TYPE);
   }
 
   private String getThumbUrl(final Banner banner) {
     return (StringUtils.isEmpty(banner.getThumb())) ? banner.getUrl() : banner.getThumb();
   }
 
   @Override
   public MovieForm filleInfoToMovieForm(final GrabberInfoForm grabberInfoForm) throws GrabberException {
 
     final String id = grabberInfoForm.grabberMovieId;
 
     final String[] split = id.split("_");
     final String seriesId = split[0];
     final String seasonId = split[1];
 
     final Series series = theTVDB.getSeries(seriesId, TheTvDbGrabber.LANGUAGE);
     if (series == null) {
       final String message = "Could not find series: " + seriesId;
       Logger.error(message);
       throw new GrabberException(message);
     }
 
     final MovieForm movieForm = new MovieForm();
 
     final Integer season = Integer.valueOf(seasonId);
 
     movieForm.title = buildMovieName(series, season);
     movieForm.plot = series.getOverview();
 
    // TODO: go over all episodes ?
    movieForm.runtime = 0;
 
     final String firstAired = series.getFirstAired();
     if (StringUtils.isEmpty(firstAired) == false) {
       final String[] split2 = firstAired.split("-");
       if (split2.length == 3) {
        movieForm.year = Integer.valueOf(split[0]);
       }
     }
 
     movieForm.genres.addAll(series.getGenres());
 
     movieForm.actors.addAll(series.getActors());
 
     final Banners banners = theTVDB.getBanners(seriesId);
     final List<Banner> posterList = banners.getSeasonList();
     final List<Banner> fanartList = banners.getFanartList();
 
     movieForm.posterUrl = getImageURL(posterList, grabberInfoForm.grabberPosterId);
     movieForm.backDropUrl = getImageURL(fanartList, grabberInfoForm.grabberBackDropId);
 
     return movieForm;
 
   }
 
   /**
    * Gets the url for banner from the list for the given id
    * 
    * @param banners
    * @param idStr
    * @return
    */
   private String getImageURL(final List<Banner> banners, final String idStr) {
 
     if (StringUtils.isEmpty(idStr) == true || CollectionUtils.isEmpty(banners) == true) {
       return null;
     }
     final Integer id = Integer.valueOf(idStr);
 
     for (final Banner banner : banners) {
       if (id.equals(banner.getId()) == true) {
         return banner.getUrl();
       }
     }
 
     return null;
 
   }
 }
