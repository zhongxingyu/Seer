 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mosedb.servlet.seriesServlet;
 
 import com.mosedb.business.SeriesService;
 import com.mosedb.models.Episode;
 import com.mosedb.models.Format;
 import com.mosedb.models.Format.MediaFormat;
 import com.mosedb.models.LangId;
 import com.mosedb.models.Series;
 import com.mosedb.servlet.AbstractInfoServlet;
 import com.mosedb.tools.AttributeManager;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Handles all the functionality in seriesInfo.jsp.
  *
  * @author Lasse
  */
 public class UpdateSeriesServlet extends AbstractInfoServlet {
 
     private static final String UPDATE_BUTTON = "update_series";
     private static final String DELETE_BUTTON = "delete_series";
     private static final String NEW_SEASON_NUMBER_DROPBOX = "new_season_select";
     private static final String NEW_SEASON_EPISODE_NUMBER_DROPBOX = "new_season_episode_select";
     private static final String NEW_SEASON_YEAR_DROPBOX = "new_season_year_select";
     private static final String NEW_SEASON_SEEN_CHECKBOX = "new_season_seen_checkbox";
     private static final String NEW_SEASON_FORMAT_DROPBOX = "new_season_format_select";
     private static final String DELETE_SEASON_DROPBOX = "delete_season_select";
     private static final String EPISODE_NAME_INPUT = "episode_name_";
     private static final String EPISODE_YEAR_SELECT = "episode_year_";
     private static final String EPISODE_SEEN_CHECKBOX = "episode_seen_";
     private static final String EPISODE_FORMAT_DROPBOX = "episode_media_format_";
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         request.setCharacterEncoding("UTF-8");
         if (isUserLoggedIn(request)) {
             boolean success = false;
             String clickedButton = request.getParameter("submit");
             if (clickedButton.equals(UPDATE_BUTTON)) {
                 success = updateSeries(request);
             } else if (clickedButton.equals(DELETE_BUTTON)) {
                 success = removeSeries(request);
             }
             if (success) {
                 AttributeManager.setSuccessMessage(request, "Changes updated successfully!");
                 restorePage("seriesInfo.jsp", request, response);
             } else {
                 AttributeManager.setErrorMessage(request, "Failed to update series information!");
                 redirectHome(request, response);
             }
         } else {
             redirectHome(request, response);
         }
     }
 
     /**
      * Updates the series currently in session according to the information in
      * seriesInfo.jsp
      *
      * @param request The request from which the current session is gotten.
      * @return {@code true} if updating succeeded, {@code false} if not.
      */
     private boolean updateSeries(HttpServletRequest request) {
         AttributeManager.removeErrorMessage(request);
         AttributeManager.removeSuccessMessage(request);
 
         SeriesService seriesService = new SeriesService();
         Series series = AttributeManager.getSeries(request.getSession(true));
 
         boolean success;
         boolean totalSuccess = true;
 
         /*
          * update names
          */
         Map<LangId, String> names = getNameMap(request);
         if (names.isEmpty()) {
             totalSuccess = false;
         } else {
             success = seriesService.updateNames(series, names);
             if (success) {
                 series.setNames(names);
             } else {
                 totalSuccess = false;
             }
         }
 
         /*
          * update genres
          */
         List<String> genreList = getGenres(request);
         success = seriesService.updateGenres(series, genreList);
         if (success) {
             series.setGenres(genreList);
         } else {
             totalSuccess = false;
         }
 
         /*
          * update episode info
          */
         updateEpisodes(request, series);
 
         /*
          * add season
          */
         success = addNewSeason(request, series);
         if (!success) {
             totalSuccess = false;
         }
 
         /*
          * remove season, if selected
          */
         success = removeSeason(request);
         if (!success) {
             totalSuccess = false;
         }
 
         return totalSuccess;
     }
 
     /**
      * Removes the series currently in the session from the database and returns
      * a boolean value to {@link #updateSeries(javax.servlet.http.HttpServletRequest)}
      * }.
      *
      * @param request The request from which the current session is gotten.
      * @return {@code true} if deletion succeeded, {@code false} if not.
      */
     private boolean removeSeries(HttpServletRequest request) {
         int seriesid = AttributeManager.getSeries(request.getSession(true)).getId();
         return new SeriesService().removeSeries(seriesid);
     }
 
     /**
      * Removes a season from the series currently in the session according to
      * the corresponding fields on seriesInfo.jsp, and then updates the series
      * and sets it to the session. Returns a boolean value to
      * {@link #updateSeries(javax.servlet.http.HttpServletRequest)}
      *
      * @param request Request from which the season number to delete and the
      * current session is gotten
      * @return {@code true} if deletion succeeded, {@code false} if not.
      */
     private boolean removeSeason(HttpServletRequest request) {
         Series series = AttributeManager.getSeries(request.getSession(true));
         int seriesid = series.getId();
         String seasonString = request.getParameter(DELETE_SEASON_DROPBOX);
         if (seasonString == null || seasonString.isEmpty()) {
             return true;
         }
         int seasonNumber = Integer.parseInt(seasonString);
         HttpSession session = request.getSession(true);
         SeriesService seriesService = new SeriesService();
         boolean success = seriesService.removeSeason(seriesid, seasonNumber);
         series.setEpisodes(seriesService.getAllEpisodes(series.getId()));
         AttributeManager.setEpisodeDropbox(session, getEpisodeDropboxValues());
         AttributeManager.setSeasonDropbox(session, getSeasonDropboxValues(series));
         return success;
     }
 
     /**
      * Adds a season to the series currently in the session according to the
      * corresponding fields on seriesInfo.jsp, and then updates the series and
      * sets it to the session. Returns a boolean value to
      * {@link #updateSeries(javax.servlet.http.HttpServletRequest)}
      *
      * @param request Request from which the season number to delete and the
      * current session is gotten
      * @param series The series to which a season is to be added.
      * @return {@code true} if adding succeeded, {@code false} if not.
      */
     private boolean addNewSeason(HttpServletRequest request, Series series) {
         String seasonNrString = request.getParameter(NEW_SEASON_NUMBER_DROPBOX);
         if (seasonNrString == null) {
             return true;
         }
         int seasonNumber = Integer.parseInt(seasonNrString);
         int nrOfEpisodes = Integer.parseInt(request.getParameter(NEW_SEASON_EPISODE_NUMBER_DROPBOX));
         String yearString = request.getParameter(NEW_SEASON_YEAR_DROPBOX);
         Integer year = null;
         if (!yearString.isEmpty()) {
             year = Integer.parseInt(yearString);
         }
         boolean seen;
         if (request.getParameter(NEW_SEASON_SEEN_CHECKBOX) != null) {
             seen = true;
         } else {
             seen = false;
         }
         MediaFormat mediaFormat = MediaFormat.valueOf(request.getParameter(NEW_SEASON_FORMAT_DROPBOX));
         Format format = new Format(mediaFormat);
 
         boolean success;
         SeriesService seriesService = new SeriesService();
         if (year != null) {
             success = seriesService.addNewSeason(series.getId(), seasonNumber, nrOfEpisodes, seen, year, format);
         } else {
             success = seriesService.addNewSeason(series.getId(), seasonNumber, nrOfEpisodes, seen, format);
         }
         if (!success) {
             return false;
         }
         series.setEpisodes(seriesService.getAllEpisodes(series.getId()));
         AttributeManager.setSeasonDropbox(request.getSession(true), getSeasonDropboxValues(series));
         return true;
     }
 
     /**
      * Updates all the episodes from a series using {@link #updateEpisode(javax.servlet.http.HttpServletRequest, com.mosedb.models.Episode)
      * }. Returns a boolean value to
      * {@link #updateSeries(javax.servlet.http.HttpServletRequest)}.
      *
      * @param request
      * @param series The series which episodes is updated.
      * @return {@code true} if updating succeeded, {@code false} if not.
      */
     private boolean updateEpisodes(HttpServletRequest request, Series series) {
         boolean success = true;
         for (Episode episode : series.getEpisodes()) {
             success = updateEpisode(request, episode) && success;
         }
         return success;
     }
 
     /**
      * Updates the episodes information according to the corresponding fields
      * from seriesInfo.jsp. Returns a boolean value to
      * {@link #updateSeries(javax.servlet.http.HttpServletRequest)}
      *
      * @param request The request from which the episode update info is gotten.
      * @param episode The episode to be updated
      * @return {@code true} if updating succeeded, {@code false} if not.
      */
     private boolean updateEpisode(HttpServletRequest request, Episode episode) {
         boolean changes = false;
         String episodeTag = episode.getSeriesId() + "_" + episode.getSeasonNumber() + "_" + episode.getEpisodeNumber();
 
         String newName = request.getParameter(EPISODE_NAME_INPUT + episodeTag);
         if (!newName.equals(episode.getEpisodeName())) {
             episode.setEpisodeName(newName);
             changes = true;
         }
 
         String newYearString = request.getParameter(EPISODE_YEAR_SELECT + episodeTag);
         Integer newYear = null;
         if (!newYearString.isEmpty()) {
             newYear = Integer.parseInt(newYearString);
         }
         if (newYear != episode.getEpisodeYear()) {
             episode.setEpisodeYear(newYear);
             changes = true;
         }
 
         boolean newSeen = request.getParameter(EPISODE_SEEN_CHECKBOX + episodeTag) != null;
         if (newSeen ^ episode.isSeen()) {
             episode.setSeen(newSeen);
             changes = true;
         }
 
         MediaFormat newMediaFormat = MediaFormat.valueOf(request.getParameter(EPISODE_FORMAT_DROPBOX + episodeTag));
        Format oldFormat = episode.getFormat();
        if (oldFormat == null || newMediaFormat != oldFormat.getMediaFormat()) {
             episode.setFormat(new Format(newMediaFormat));
             changes = true;
         }
 
         if (changes) {
             return new SeriesService().updateEpisode(episode);
         }
         return true;
     }
 }
