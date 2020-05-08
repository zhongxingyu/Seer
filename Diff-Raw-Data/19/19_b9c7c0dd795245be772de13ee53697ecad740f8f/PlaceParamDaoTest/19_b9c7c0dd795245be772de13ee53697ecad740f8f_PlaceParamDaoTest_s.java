 package com.rockontrol.yaogan.dao;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.rockontrol.yaogan.BaseTest;
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.PlaceParam;
 
 public class PlaceParamDaoTest extends BaseTest {
    private Place myPlace;
 
    // private PlaceParam myParam;
 
    @Override
    @Before
    public void setUp() {
       super.setUp();
       List<Place> list = this.placeDao.findAll();
       Place place = list.get(0);
       PlaceParam param = new PlaceParam();
       param.setParamName(PlaceParam.GROUND_WATER_DESC);
       param.setParamValue(120 + "");
       param.setTime("2011");
       param.setPlaceId(place.getId());
       this.placeParamDao.save(param);
       myPlace = place;
       // myParam = param;
    }
 
    @Test
    public void testGetPlaceParam() {
       PlaceParam param = placeParamDao.getPlaceParam(myPlace.getId(), "2011",
             PlaceParam.GROUND_WATER_DESC);
       Assert.assertNotNull(param);
    }
 
    // @After
    // public void clean() {
    // placeParamDao.remove(myParam);
    // }
 }
