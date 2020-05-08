 package com.rockontrol.yaogan.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.rockontrol.yaogan.dao.IOrganizationDao;
 import com.rockontrol.yaogan.dao.IPlaceDao;
 import com.rockontrol.yaogan.dao.IPlaceParamDao;
 import com.rockontrol.yaogan.dao.IShapefileDao;
 import com.rockontrol.yaogan.dao.IUserDao;
 import com.rockontrol.yaogan.dao.IUserPlaceDao;
 import com.rockontrol.yaogan.model.Organization;
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.PlaceParam;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.rockontrol.yaogan.model.Shapefile.Category;
 import com.rockontrol.yaogan.model.User;
 import com.rockontrol.yaogan.model.UserPlace;
 import com.rockontrol.yaogan.vo.EnvStats;
 
 @Service
 public class YaoganServiceImpl implements IYaoganService {
    public YaoganServiceImpl() {
       this.init();
    }
 
    private String shapeFileHome = "";
    @Autowired
    protected IPlaceDao placeDao;
 
    @Autowired
    protected IPlaceParamDao placeParamDao;
 
    @Autowired
    protected IShapefileDao shapefileDao;
 
    @Autowired
    protected IOrganizationDao orgDao;
 
    @Autowired
    protected IUserPlaceDao upDao;
 
    @Autowired
    private IUserDao userDao;
 
    @Autowired
    private EcoFactorComputeService computeService;
 
   // @Autowired
    private GeoService geoService;
 
    @Override
    public User getUser(User caller, Long userId) {
       return userDao.get(userId);
    }
 
    @Override
    public List<Place> getPlacesOfOrg(User caller, Long orgId) {
       return placeDao.getPlacesOfOrg(orgId);
    }
 
    @Override
    public Place getPlaceById(Long placeId) {
       return placeDao.get(placeId);
    }
 
    @Override
    public Place getPlaceByName(String placeName) {
       return placeDao.getPlaceByName(placeName);
    }
 
    @Override
    public List<Place> getPlacesVisibleToUser(User caller, Long userId) {
       User user = userDao.get(userId);
       if (user.getIsAdmin()) {
          return placeDao.getPlacesOfOrg(user.getOrgId());
       }
       List<Place> list = upDao.getPlacesVisibleToUser(userId);
       return list;
    }
 
    @Override
    public List<Place> getPlacesVisibleToUser(User caller, Long userId, String time) {
       if (StringUtils.isEmpty(time))
          return getPlacesVisibleToUser(caller, userId);
       User user = userDao.get(userId);
       if (user.getIsAdmin()) {
          return shapefileDao.getAvailablePlacesOfOrg(user.getOrgId(), time);
       }
       return shapefileDao.getAvailablePlacesOfUser(userId, time);
    }
 
    @Override
    public List<String> getAvailableTimeOptions(User caller, Long placeId) {
       List<String> list = new ArrayList<String>(
             shapefileDao.getAvailableTimesOfPlace(placeId));
       Collections.sort(list);
       return list;
    }
 
    @Override
    public List<String> getAvailableTimesForUser(User caller, Long userId) {
       User user = userDao.get(userId);
       if (user.getIsAdmin()) {
          return shapefileDao.getAvailableTimesOfOrg(user.getOrgId());
       }
       return shapefileDao.getAvailableTimesOfUser(userId);
    }
 
    @Override
    public List<User> getAllUsersOfOrg(User caller, Long orgId) {
       Organization org = orgDao.get(orgId);
       return org == null ? null : org.getEmployees();
    }
 
    @Transactional
    @Override
    public void sharePlacesToUser(User caller, Long userId, Long[] placeIds) {
       for (int i = 0; i < placeIds.length; i++) {
          UserPlace userPlace = new UserPlace();
          userPlace.setUserId(userId);
          userPlace.setPlaceId(placeIds[i]);
          upDao.save(userPlace);
       }
    }
 
    // 需要实现的借口
    @Transactional
    @Override
    public void unsharePlaceToUser(User caller, Long userId, Long placeId) {
       Long id = upDao.getIdByUserIdPlaceId(userId, placeId);
       upDao.deleteById(id);
    }
 
    @Override
    public EnvStats getEnvStats(User caller, Long placeId, String time) {
       return computeEnvStats(caller, placeId, time);
    }
 
    @Override
    public EnvStats getEnvStats(User caller, Long placeId, String time, String geom_string) {
       // List<Shapefile> list = this.getShapefiles(caller, placeId, time);
       // EnvStats stats = new EnvStats();
       // try {
       // for (Shapefile shapefile : list) {
       // Category category = shapefile.getCategory();
       // if (category.equals(Shapefile.Category.FILE_LAND_TYPE)) {
       // double abio = this.computeService.computeAbio(shapefile.getFilePath());
       // double aveg = this.computeService.computeAveg(shapefile.getFilePath());
       // stats.setAbio(abio);
       // stats.setAveg(aveg);
       // } else if (category.equals(Shapefile.Category.FILE_LAND_SOIL)) {
       // double aero = this.computeService.computeAero(shapefile.getFilePath());
       // stats.setAero(aero);
       // }
       // }
       // } catch (IOException e) {
       // e.printStackTrace();
       // }
       //
       return computeEnvStats(caller, placeId, time, geom_string);
    }
 
    @Transactional
    @Override
    public EnvStats computeEnvStats(User caller, Long placeId, String time) {
       List<Shapefile> list = this.getShapefiles(caller, placeId, time);
       EnvStats stats = new EnvStats();
       Shapefile fractureFile = null;
       Shapefile collapseFile = null;
       Shapefile boundaryFile = null;
       PlaceParam param = placeParamDao.getPlaceParam(placeId, time,
             PlaceParam.GROUND_WATER_DESC);
 
       try {
          for (Shapefile shapefile : list) {
             Category category = shapefile.getCategory();
             switch (category) {
             case FILE_LAND_TYPE:
                double abio = this.computeService.computeAbio(this
                      .combineFullPath(shapefile));
                double aveg = this.computeService.computeAveg(this
                      .combineFullPath(shapefile));
                stats.setAbio(abio);
                stats.setAveg(aveg);
                break;
             case FILE_LAND_SOIL:
                double aero = this.computeService.computeAero(this
                      .combineFullPath(shapefile));
                stats.setAero(aero);
                break;
             case FILE_LAND_FRACTURE:
                fractureFile = shapefile;
                break;
             case FILE_LAND_COLLAPSE:
                collapseFile = shapefile;
                break;
             case FILE_REGION_BOUNDARY:
                boundaryFile = shapefile;
                break;
 
             }
 
          }
       } catch (IOException e) {
          e.printStackTrace();
       }
 
       try {
          if (fractureFile != null && collapseFile != null && boundaryFile != null
                && param != null) {
             double asus = computeService.computeAsus(this.combineFullPath(fractureFile),
                   combineFullPath(collapseFile), combineFullPath(boundaryFile),
                   Double.parseDouble(param.getParamValue()));
             stats.setAsus(asus);
          }
       } catch (IOException e) {
          e.printStackTrace();
       }
       // persist env
       return stats;
    }
 
    @Transactional
    @Override
    public EnvStats computeEnvStats(User caller, Long placeId, String time,
          String geom_string) {
       List<Shapefile> list = this.getShapefiles(caller, placeId, time);
       Shapefile fractureFile = null;
       Shapefile collapseFile = null;
       Shapefile boundaryFile = null;
       PlaceParam param = placeParamDao.getPlaceParam(placeId, time,
             PlaceParam.GROUND_WATER_DESC);
       EnvStats stats = new EnvStats();
       try {
          for (Shapefile shapefile : list) {
 
             Category category = shapefile.getCategory();
             switch (category) {
             case FILE_LAND_TYPE:
                double abio = this.computeService.computeAbio(combineFullPath(shapefile),
                      geom_string);
                double aveg = this.computeService.computeAveg(combineFullPath(shapefile),
                      geom_string);
                stats.setAbio(abio);
                stats.setAveg(aveg);
                break;
             case FILE_LAND_SOIL:
                double aero = this.computeService.computeAero(combineFullPath(shapefile),
                      geom_string);
                stats.setAero(aero);
                break;
             case FILE_LAND_FRACTURE:
                fractureFile = shapefile;
                break;
             case FILE_LAND_COLLAPSE:
                collapseFile = shapefile;
                break;
             case FILE_REGION_BOUNDARY:
                boundaryFile = shapefile;
                break;
 
             }
 
          }
       } catch (IOException e) {
          e.printStackTrace();
       }
 
       try {
          if (fractureFile != null && collapseFile != null && boundaryFile != null) {
             double asus = computeService.computeAsus(combineFullPath(fractureFile),
                   combineFullPath(collapseFile), combineFullPath(boundaryFile),
                   geom_string, Double.parseDouble(param.getParamValue()));
             stats.setAsus(asus);
          }
       } catch (IOException e) {
          e.printStackTrace();
       }
       // persist env
       return stats;
    }
 
    @Override
    public EnvStats[] getEnvStats(User caller, Long placeId, String[] times) {
       EnvStats[] statsArr = new EnvStats[times.length];
       for (int i = 0; i < times.length; i++) {
          statsArr[i] = getEnvStats(caller, placeId, times[i]);
       }
       return statsArr;
    }
 
    @Override
    public EnvStats[] getEnvStats(User caller, Long[] placeIds, String time) {
       EnvStats[] statsArr = new EnvStats[placeIds.length];
       for (int i = 0; i < placeIds.length; i++) {
          statsArr[i] = getEnvStats(caller, placeIds[i], time);
       }
       return statsArr;
    }
 
    @Override
    public List<Shapefile> getShapefiles(User caller, Long placeId, String time) {
       return shapefileDao.getShapefiles(placeId, time);
    }
 
    @Transactional
    @Override
    public Shapefile getShapefile(User caller, Long placeId, String category, String time) {
       List<Shapefile> list = this.getShapefiles(caller, placeId, time);
       for (Shapefile shapefile : list) {
          if (shapefile.getCategory().equals(category)) {
             return shapefile;
          }
       }
       return null;
    }
 
    @Override
    public PlaceParam getPlaceParam(String placeName, String time, String paramName) {
       return placeParamDao.getPlaceParam(placeName, time, paramName);
    }
 
    @Override
    public void saveShapefile(User caller, String placeName, Category type, File file,
          String filePath, String time) {
       String wmsUrl = geoService.publishGeoFile(type, file);
       Shapefile shapefile = new Shapefile();
       shapefile.setCategory(type);
       shapefile.setFileName(file.getName());
       shapefile.setShootTime(time);
       Place place = this.checkAndCreatePlace(caller, placeName);
       shapefile.setPlaceId(place.getId());
       shapefile.setUploadTime(new Date());
       shapefile.setFilePath(filePath);
       shapefile.setWmsUrl(wmsUrl);
       shapefileDao.save(shapefile);
    }
 
    public IPlaceDao getPlaceDao() {
       return placeDao;
    }
 
    public void setPlaceDao(IPlaceDao placeDao) {
       this.placeDao = placeDao;
    }
 
    public IShapefileDao getShapefileDao() {
       return shapefileDao;
    }
 
    public void setShapefileDao(IShapefileDao shapefileDao) {
       this.shapefileDao = shapefileDao;
    }
 
    public IOrganizationDao getOrgDao() {
       return orgDao;
    }
 
    public void setOrgDao(IOrganizationDao orgDao) {
       this.orgDao = orgDao;
    }
 
    public IUserPlaceDao getUpDao() {
       return upDao;
    }
 
    public void setUpDao(IUserPlaceDao upDao) {
       this.upDao = upDao;
    }
 
    public EcoFactorComputeService getComputeService() {
       return computeService;
    }
 
    public void setComputeService(EcoFactorComputeService computeService) {
       this.computeService = computeService;
    }
 
    public void setGeoService(GeoService service) {
       this.geoService = service;
    }
 
    public GeoService getGeoService() {
       return this.geoService;
    }
 
    @Override
    public List<Shapefile> getShapefiles(User caller) {
       return this.shapefileDao.getAvailableFilesOfUser(caller.getId());
    }
 
    @Override
    public void addPlaceParam(PlaceParam param) {
       this.placeParamDao.save(param);
    }
 
    @Override
    public void deletePlaceParam(String placeName, String time, String paramName) {
       this.placeParamDao.deleteParam(placeName, time, paramName);
 
    }
 
    @Override
    public void addPlaceParam(User caller, String placeName, String time,
          String paramName, String paramValue) {
       Place place = this.checkAndCreatePlace(caller, placeName);
       PlaceParam param = new PlaceParam();
       param.setParamName(paramName);
       param.setParamValue(paramValue);
       param.setPlaceId(place.getId());
       param.setTime(time);
       this.placeParamDao.save(param);
    }
 
    private Place checkAndCreatePlace(User caller, String placeName) {
       Place place = placeDao.getPlaceByName(placeName);
       if (place == null) {
          place = new Place();
          place.setName(placeName);
          place.setOrgId(caller.getOrgId());
          placeDao.save(place);
       }
       return place;
    }
 
    private void init() {
       InputStream is = YaoganServiceImpl.class
             .getResourceAsStream("/config/ShapeFileStore.properties");
       Properties props = new Properties();
       try {
          props.load(is);
          this.shapeFileHome = props.getProperty("yaogan.gis.shapefile.home");
          if (shapeFileHome.endsWith("/") || shapeFileHome.endsWith("\\"))
             shapeFileHome = shapeFileHome.substring(0, shapeFileHome.length() - 1);
       } catch (IOException e) {
          e.printStackTrace();
       }
    }
 
    private String combineFullPath(Shapefile file) {
       String repath = file.getFilePath();
       if (repath.startsWith("\\") || repath.startsWith("/"))
          repath = repath.substring(1);
       String path = shapeFileHome + File.separator + repath;
       return path;
    }
 
 }
