 /**
  * 
  */
 package com.rockontrol.yaogan.service;
 
 import java.io.File;
 
 import org.she.mvc.db.hibernate.service.DefaultService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.rockontrol.yaogan.model.Shapefile.Category;
 
 @Service("shapeFileService")
 public class ShapefileServiceImpl extends DefaultService implements IShapefileService {
 
    @Autowired
    private IPlaceService placeService;
 
   @Autowired
   private GeoService geoService;

    @Override
    @Transactional
    public void saveUploadFile(String region, Category type, File file, String year) {
       Shapefile shapefile = new Shapefile();
       shapefile.setCategory(type);
       shapefile.setFileName(file.getName());
       shapefile.setShootTime(year);
       Place place = placeService.findPlaceByName(region);
       if (place == null) {
          place = new Place();
          place.setName(region);
          placeService.save(place);
          place = placeService.findPlaceByName(region);
       }
       shapefile.setPlaceId(place.getId());
      // shapefile.setWmsUrl(geoService.publishShapeFile(file));
       shapefile.setWmsUrl(null);
       this.save(shapefile);
    }
 
    @Override
    public Shapefile getShapeFile(String areaName, String type, String year) {
       // TODO Auto-generated method stub
       return null;
    }
 
 }
