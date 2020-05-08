 package com.rockontrol.yaogan.controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.rockontrol.yaogan.model.User;
 import com.rockontrol.yaogan.service.ISecurityManager;
 import com.rockontrol.yaogan.service.IYaoganService;
 import com.rockontrol.yaogan.vo.EnvStats;
 
 @Controller
 @RequestMapping(value = { "/admin", "/user" })
 public class LayersController {
 
    @Autowired
    private IYaoganService _yaoganService;
 
    @Autowired
    private ISecurityManager _secMng;
 
   private final boolean mock = true;
 
    /**
     * 地图图层展示页面
     * 
     * @return
     */
    @RequestMapping("/layers")
    public String layers(Model model) {
       if (!mock) {
          User user = _secMng.currentUser();
          List<Place> placeList = _yaoganService.getPlacesVisibleToUser(user,
                user.getId());
          model.addAttribute("places", placeList);
       } else {
          Place place = new Place();
          place.setId(1L);
          place.setName("平朔");
          List<Place> placeList = Arrays.asList(place);
          model.addAttribute("places", placeList);
       }
 
       return "/layers/indexlayers3";
    }
 
    /**
     * 根据地区查询地图的时间
     * 
     * @param placeId
     * @return
     */
    @RequestMapping(value = "/layers/getAvailableTime")
    @ResponseBody
    public List<String> getAvailableTime(@RequestParam("placeId") Long placeId) {
       if (!mock) {
          User user = _secMng.currentUser();
          List<String> timeList = _yaoganService.getAvailableTimeOptions(user, placeId);
          return timeList;
       } else {
          List<String> timeList = Arrays.asList("2011", "2012");
          return timeList;
       }
    }
 
    /**
     * 根据地区ID和时间查询所有的图层的相关信息
     * 
     * @param placeId
     * @param time
     * @return
     */
    @RequestMapping(value = "/layers/queryMap")
    @ResponseBody
    public List<Map<String, Object>> queryMap(@RequestParam("placeId") Long placeId,
          @RequestParam("time") String time) {
       List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
       if (!mock) {
          User user = _secMng.currentUser();
          List<Shapefile> fileList = _yaoganService.getShapefiles(user, placeId, time);
          for (Shapefile file : fileList) {
             Map<String, Object> map = new HashMap<String, Object>();
             map.put("type", file.getCategory().getType());
             String wmsInfo = file.getWmsUrl();
             String[] infos = wmsInfo.split("\\?");
             map.put("layerName", infos[0]);
             String[] bs = infos[1].split(",");
             double[] bounds = { Double.parseDouble(bs[0]), Double.parseDouble(bs[1]),
                   Double.parseDouble(bs[2]), Double.parseDouble(bs[3]) };
             map.put("bounds", bounds);
             list.add(map);
          }
       } else {
          double[] bounds = { 112.28644133876368, 39.41956384236653, 112.46459523013785,
                39.592808951996105 };
          Map<String, Object> map;
          map = new HashMap<String, Object>();
          map.put("type", "kq");
          map.put("layerName", "yaogan:pingshuo_KQ");
          map.put("bounds", bounds);
          list.add(map);
          map = new HashMap<String, Object>();
          map.put("type", "tdly");
          map.put("layerName", "yaogan:tdly2011");
          map.put("bounds", bounds);
          list.add(map);
          map = new HashMap<String, Object>();
          map.put("type", "dbtx");
          map.put("layerName", "yaogan:dbtx2011");
          map.put("bounds", bounds);
          list.add(map);
          map = new HashMap<String, Object>();
          map.put("type", "trqs");
          map.put("layerName", "yaogan:trqs2011");
          map.put("bounds", bounds);
          list.add(map);
          map = new HashMap<String, Object>();
          map.put("type", "gqyg");
          map.put("layerName", "yaogan:TH01_2011");
          map.put("bounds", bounds);
          list.add(map);
          map = new HashMap<String, Object>();
          map.put("type", "dlf");
          map.put("layerName", "yaogan:5f46a3e1c9fe43fb8756943d08d7966a");
          map.put("bounds", bounds);
          list.add(map);
       }
       return list;
    }
 
    @RequestMapping(value = "/layers/queryEnvStats")
    @ResponseBody
    public EnvStats querySwfd(@RequestParam("placeId") Long placeId,
          @RequestParam("time") String time,
          @RequestParam(required = false, value = "geometry") String geometry) {
       if (!mock) {
          User user = _secMng.currentUser();
          if (geometry == null) {
             return _yaoganService.getEnvStats(user, placeId, time);
          } else {
             return _yaoganService.getEnvStats(user, placeId, time, geometry);
          }
       } else {
          EnvStats envStats = new EnvStats();
          envStats.setAbio(1);
          envStats.setAero(2);
          envStats.setAsus(3);
          envStats.setAveg(4);
          return envStats;
       }
    }
 
 }
