 package com.rockontrol.yaogan.service;
 
 import java.io.File;
 import java.util.List;
 
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.PlaceParam;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.rockontrol.yaogan.model.Shapefile.Category;
 import com.rockontrol.yaogan.model.User;
 import com.rockontrol.yaogan.vo.EnvStats;
 
 public interface IYaoganService {
 
    /**
     * get all places of the organization of orgId.
     * 
     * @param caller
     * @param orgId
     * @return
     */
    public List<Place> getPlacesOfOrg(User caller, Long orgId);
 
    /**
     * get place by id.
     * 
     * @param placeId
     * @return
     */
    public Place getPlaceById(Long placeId);
 
    /**
     * get place by name
     * 
     * @param placeName
     * @return
     */
    public Place getPlaceByName(String placeName);
 
    /**
     * get places that user of userId can access.
     * 
     * @param caller
     * @param userId
     * @return
     */
    public List<Place> getPlacesVisibleToUser(User caller, Long userId);
 
    /**
     * get all available times in which there is data(shapefiles) for the place
     * of placeId.
     * 
     * @param caller
     * @param placeId
     * @return
     */
    public List<String> getAvailableTimeOptions(User caller, Long placeId);
 
    /**
     * get all users in the organization of orgId.
     * 
     * @param caller
     * @param orgId
     * @return
     */
    public List<User> getAllUsersOfOrg(User caller, Long orgId);
 
    /**
     * give the user access to the places.
     * 
     * @param caller
     * @param userId
     * @param placeIds
     */
    public void sharePlacesToUser(User caller, Long userId, Long[] placeIds);
 
    /**
     * revoke the user's access from the places.
     * 
     * @param caller
     * @param userId
     * @param placeId
     */
    public void unsharePlaceToUser(User caller, Long userId, Long placeId);
 
    /**
     * compute the place's statistics.
     * 
     * @param caller
     * @param placeId
     * @param time
     * @return
     */
    public EnvStats computeEnvStats(User caller, Long placeId, String time);
 
    /**
     * compute statistics of the specified area of the place.
     * 
     * @param caller
     * @param placeId
     * @param time
     * @param geom_string
     * @return
     */
    public EnvStats computeEnvStats(User caller, Long placeId, String time,
          String geom_string);
 
    /**
     * get the place's statistics.
     * 
     * @param caller
     * @param placeId
     * @param time
     * @return
     */
    public EnvStats getEnvStats(User caller, Long placeId, String time);
 
    /**
     * get statistics of the specified area of the place.
     * 
     * @param caller
     * @param placeId
     * @param time
     * @param geom_string
     * @return
     */
    public EnvStats getEnvStats(User caller, Long placeId, String time, String geom_string);
 
    /**
     * get statistics of the place in specified time range.
     * 
     * @param caller
     * @param placeId
     * @param time
     * @return
     */
    public EnvStats[] getEnvStats(User caller, Long placeId, String[] times);
 
    /**
     * get statistics of these places in specified time.
     * 
     * @param caller
     * @param placeId
     * @param time
     * @return
     */
    public EnvStats[] getEnvStats(User caller, Long[] placeIds, String time);
 
    /**
     * get shapefiles of place in specified time.
     * 
     * @param caller
     * @param placeId
     * @param time
     * @return
     */
    public List<Shapefile> getShapefiles(User caller, Long placeId, String time);
 
    /**
     * get all shapefiles
     * 
     * @param caller
     * @return
     */
    public List<Shapefile> getShapefiles(User caller);
 
    /**
     * get one shapefile
     * 
     * @param caller
     * @param placeId
     * @param category
     * @param time
     * @return shapefile
     */
    public Shapefile getShapefile(User caller, Long placeId, String category, String time);
 
    /**
     * get place param
     * 
     * @param caller
     * @param placeName
     * @param time
     * @param paramName
     * @return
     */
    public PlaceParam getPlaceParam(String placeName, String time, String paramName);
 
    public void addPlaceParam(PlaceParam param);
 
   public void addPlaceParam(String placeName, String time, String paramName,
         String paramValue);
 
    public void deletePlaceParam(String placeName, String time, String paramName);
 
    /**
     * 
     * @param placeName
     * @param type
     * @param file
     * @param filePath
     *           path relative to shapefile home
     * @param time
     */
    public void saveShapefile(User caller, String placeName, Category type, File file,
          String filePath, String time);
 
 }
